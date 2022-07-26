package com.zhj.ESDemo;


import com.alibaba.fastjson.JSONObject;
import com.zhj.ESDemo.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Topic
 * Description
 *
 * @author zhouh
 * @version 1.0
 * Create by 2022/7/25 12:07
 */
@SpringBootTest
public class EsApiTests {

    @Autowired
    @Qualifier("restHighLevelClient")
    RestHighLevelClient client1;

    /**
     * 创建索引测试
     */
    @Test
    void createIndex() throws IOException {
        //1、构建 创建索引的请求
        CreateIndexRequest request = new CreateIndexRequest("user_index");//索引名
        //2、客户端执行请求,获取响应
        CreateIndexResponse response = client1.indices().create(request, RequestOptions.DEFAULT);
        //3、打印
        System.out.println("创建成功，创建的索引名为：" + response.index());
    }

    /**
     * 获取索引测试
     */
    @Test
    void getIndex() throws IOException {
        //1、构建 获取索引的请求
        GetIndexRequest request = new GetIndexRequest("jd_goods");
        //2、客户端判断该索引是否存在
        boolean exists = client1.indices().exists(request, RequestOptions.DEFAULT);
        //3、打印
        System.out.println("该索引是否存在："+exists);
    }

    /**
     * 删除索引测试
     */
    @Test
    void deleteIndex() throws IOException {
        //1、构建 删除索引请求
        DeleteIndexRequest request = new DeleteIndexRequest("jd_goods");
        //2、客户段执行删除的请求
        AcknowledgedResponse response = client1.indices().delete(request, RequestOptions.DEFAULT);
        //3、打印
        System.out.println("是否删除成功："+response.isAcknowledged());
    }

    /**
     * 创建文档
     */
    @Test
    void createDocument() throws IOException {
        User user = new User().setId(1).setUsername("张三");

        //1、构建请求
        IndexRequest request = new IndexRequest("user_index");

        //2、设置规则  PUT /user_index/user/_doc/1
        request.id("1");//设置id
        request.timeout(TimeValue.timeValueSeconds(1));//设置超时时间

        //3、将数据放入到请求中,以JSON的格式存放
        request.source(JSONObject.toJSONString(user), XContentType.JSON);

        //4、客户端发送请求,获取响应结果
        IndexResponse response = client1.index(request, RequestOptions.DEFAULT);

        //5、打印
        System.out.println("响应结果："+response.toString());
    }

    /**
     * 获取文档
     */
    @Test
    void getDocument() throws IOException {
        //获取id为1的文档的信息
        GetRequest request = new GetRequest("user_index","1");

        boolean exists = client1.exists(request, RequestOptions.DEFAULT);
        System.out.println("文档是否存在："+exists);
        //如果存在，获取文档信息
        if (exists){
            GetResponse response = client1.get(request, RequestOptions.DEFAULT);
            System.out.println("文档内容为："+response.getSourceAsString());
        }
    }

    /**
     * 更新文档
     */
    @Test
    void updateDocument() throws IOException {
        //更新id为1的文档的信息
        UpdateRequest request = new UpdateRequest("user_index", "1");

        User user = new User().setUsername("李四");
        request.doc(JSONObject.toJSONString(user), XContentType.JSON);

        //客户端执行更新请求
        UpdateResponse response = client1.update(request, RequestOptions.DEFAULT);
        System.out.println("更新状态：" +response.status());
    }

    /**
     * 7.删除文档
     */
    @Test
    void deleteDocument() throws IOException {
        //构建删除请求
        DeleteRequest request = new DeleteRequest("user_index", "1");
        //客户端执行删除请求，并获取响应结果
        DeleteResponse response = client1.delete(request, RequestOptions.DEFAULT);
        //打印
        System.out.println("删除状态："+response.status());
    }

    /**
     * 8.批量插入数据
     */
    @Test
    void createBulkDocument() throws IOException {
        //构建批量插入的请求
        BulkRequest request = new BulkRequest();
        //设置超时时间
        request.timeout("10s");

        //设置数据
        List<User> list = new ArrayList<>();
        list.add(new User().setId(1).setUsername("张三"));
        list.add(new User().setId(2).setUsername("李四"));
        list.add(new User().setId(3).setUsername("王五"));
        list.add(new User().setId(4).setUsername("赵六"));

        //批量插入请求设置
        for (int i = 0; i < list.size(); i++) {
            request.add(
                    new IndexRequest("user_index")//设置索引
                            .id(String.valueOf(i+1))//设置文档的id，如果没有指定，会随机生成，自己测试
                            .source(JSONObject.toJSONString(list.get(i)), XContentType.JSON)//设置要添加的资源，类型为JSON
            );
        }
        BulkResponse response = client1.bulk(request, RequestOptions.DEFAULT);
//        System.out.println("批量插入是否失败："+response.hasFailures());
        System.out.println("批量插入是否成功："+!response.hasFailures());
    }

    /**
     * 9.查询
     */
    @Test
    void query() throws IOException {
        //1、构建搜索请求
        SearchRequest request = new SearchRequest("user_index");

        //2、设置搜索条件，使用该构建器进行查询
        SearchSourceBuilder builder = new SearchSourceBuilder();//生成构建器

        //查询条件我们可以用工具类QueryBuilders来构建
        //QueryBuilders.termQuery()：精确匹配
        //QueryBuilders.matchAllQuery()：全文匹配

        //构建精确匹配查询条件
        //构建精确匹配查询条件
//        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("username.keyword", "四");   // 只能精准查询，查询"四"出不来结果
//        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("username.keyword", "李四");
//        QueryBuilder  termQueryBuilder = QueryBuilders.matchQuery("username", "李");   // 前缀查询，但是不能带.keyword【作用是不让分词】
        QueryBuilder  termQueryBuilder = QueryBuilders.fuzzyQuery("username", "四");   // 前缀查询，但是不能带.keyword【作用是不让分词】
//        QueryBuilder termQueryBuilder = QueryBuilders.matchAllQuery();
//        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
//        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery("username", "张");
        builder.query(termQueryBuilder);

        //3、将搜索条件放入搜索请求中
        request.source(builder);
        //4、客户端执行搜索请求
        SearchResponse response = client1.search(request, RequestOptions.DEFAULT);

        //5、打印测试
        SearchHit[] hits = response.getHits().getHits();
        System.out.println("共查询到"+hits.length+"条数据");
        System.out.println("查询结果：");
        for (int i = 0; i < hits.length; i++) {
            System.out.println(hits[i].getSourceAsString());
        }
    }

    /**
     * 9.查询自建表 jd_goods
     */
    @Test
    void queryJD() throws IOException {
        //1、构建搜索请求
        SearchRequest request = new SearchRequest("jd_goods");

        //2、设置搜索条件，使用该构建器进行查询
        SearchSourceBuilder builder = new SearchSourceBuilder();//生成构建器   // 构造器模式

        //查询条件我们可以用工具类QueryBuilders来构建
        //QueryBuilders.termQuery()：精确匹配
        //QueryBuilders.matchAllQuery()：全文匹配

        //构建精确匹配查询条件
        //构建精确匹配查询条件
//        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("img", "//img14.360buyimg.com/n1/s200x200_12084/5a0d1660-3c26-4a46-bcc3-c8426cb96901.jpg");
//        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
//        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery("img", "*中*");   // 失败
//        builder.query(wildcardQueryBuilder);

        // 索引查询
//        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
//        QueryBuilder queryBuilder = QueryBuilders.fuzzyQuery("title", "中");   //成功 单个字”中“成功，多个例”中文“就会失败
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("title", "轻松");   // 成功【单子多字都成功】

        // more like test
//        QueryBuilder queryBuilder = QueryBuilders.moreLikeThisQuery(new String[]{"title"}, new String[]{"中文"},
//                new MoreLikeThisQueryBuilder.Item[]{new MoreLikeThisQueryBuilder.Item("jd_goods", "doc", "1")});   // 成功【单子多字都成功】
//        QueryBuilder queryBuilder = QueryBuilders.moreLikeThisQuery(new String[]{"title"}, null, null);   // 成功【单子多字都成功】
//        QueryBuilder queryBuilder = QueryBuilders.moreLikeThisQuery(new String[] {"title"}, new String[] {"中文"}, null);   // 成功【单子多字都成功】
//        QueryBuilder queryBuilder = QueryBuilders.moreLikeThisQuery(new String[] {"title"})
//                .likeTexts(new String[]{"中文"}).minTermFreq(1).maxQueryTerms(12);   // 成功【单子多字都成功】

        builder.query(queryBuilder);
//        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery("username", "*中*文*");

        //3、将搜索条件放入搜索请求中
        request.source(builder);
        //4、客户端执行搜索请求
        SearchResponse response = client1.search(request, RequestOptions.DEFAULT);

        //5、打印测试
        SearchHit[] hits = response.getHits().getHits();
        System.out.println("共查询到"+hits.length+"条数据");
        System.out.println("查询结果：");
        for (int i = 0; i < hits.length; i++) {
            System.out.println(hits[i].getSourceAsString());
        }
    }






}
