package com.zhj.ESDemo.service;

/**
 * Topic
 * Description
 *
 * @author zhouh
 * @version 1.0
 * Create by 2022/7/24 20:46
 */

import com.alibaba.fastjson.JSON;
import com.zhj.ESDemo.pojo.Content;
import com.zhj.ESDemo.utils.HtmlParseUtil;
import org.apache.commons.codec.binary.StringUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 业务编写
 */
@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

/*    public static void main(String[] args) throws IOException {
        new ContentService().parseContext("java");
    }*/

    /**
     * 1. 解析数据放入es索引中
     */

    public Boolean parseContext(String keywords) throws IOException {
        List<Content> contents = new HtmlParseUtil().parseJD(keywords);
        //把查询到的数据放入es索引中
        BulkRequest bulkRequest = new BulkRequest("jd_goods");
        bulkRequest.timeout("2m");
        for (int i = 0; i < contents.size(); i++) {
            bulkRequest.add(new IndexRequest("jd_goods")
//                    .id(String.valueOf(i+1))//设置文档的id，如果没有指定，会随机生成，自己测试
                    .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    //2. 获取这些数据实现搜索功能
    public List<Map<String, Object>> serchPage(String keyword, int pageNo, int pageSize) throws IOException {
        if(pageNo <=1) {
            pageNo = 1;
        }
        //条件搜索
        SearchRequest jd_goods = new SearchRequest("jd_goods");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
        searchSourceBuilder.query(termQuery);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        highlightBuilder.requireFieldMatch(false);//匹配第一个即可
        searchSourceBuilder.highlighter(highlightBuilder);
        jd_goods.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(jd_goods, RequestOptions.DEFAULT);
        Map<String, Object> map = new HashMap<>();
        ArrayList<Map<String,Object>> list = new ArrayList();
        for (SearchHit hit : search.getHits()) {
            map = hit.getSourceAsMap();
            //System.out.println("hit = " + hit);
            list.add(map);
        }
        return list;
    }

    //2. 获取这些数据实现搜索功能
    public List<Map<String, Object>> serchPageBuilder1(String keyword, int pageNo, int pageSize) throws IOException {
        if(pageNo <=1) {
            pageNo = 1;
        }
        //条件搜索
        //1、构建搜索请求
        SearchRequest jd_goods = new SearchRequest("jd_goods");
        //2、设置搜索条件，使用该构建器进行查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建精确匹配查询条件
//        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
//        TermQueryBuilder termQuery = QueryBuilders.termQuery("title.keyword", keyword);
//        searchSourceBuilder.query(termQuery);



        // 自试 索引查询
        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        searchSourceBuilder.query(queryBuilder);
//        QueryBuilder queryBuilder = QueryBuilders.matchQuery("title", "轻松");   // 成功【单子多字都成功】
//        searchSourceBuilder.query(queryBuilder);



        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);


//        HighlightBuilder.Field field = new HighlightBuilder.Field("title");
//        field.preTags("<font color='red'>");
//        field.postTags("</font>");
//        field.fragmentSize(100);





        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(true);
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        // highlightBuilder.requireFieldMatch(false);//匹配第一个即可
        // highlightBuilder.numOfFragments(0);
        searchSourceBuilder.highlighter(highlightBuilder);
        //3、将搜索条件放入搜索请求中
        jd_goods.source(searchSourceBuilder);
        //4、客户端执行搜索请求
        SearchResponse search = restHighLevelClient.search(jd_goods, RequestOptions.DEFAULT);
        System.out.println("共查询到"+search.getHits().getHits().length+"条数据");

        //5、打印测试
        Map<String, Object> map = new HashMap<>();
        ArrayList<Map<String,Object>> list = new ArrayList();
        for (SearchHit hit : search.getHits()) {

            //
            System.out.println("hit：\n" + hit);
            System.out.println(hit.getHighlightFields() == null);
            System.out.println(hit.getHighlightFields().get("title"));
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            System.out.println("highlightFields = " + highlightFields);
            HighlightField title_high = highlightFields.get("title");
            String fr = "";
            for (Text fragment : title_high.fragments()) {
                System.out.println("fragment = " + fragment);
                fr = fragment.toString();
                map.put("fragment", JSON.toJSONString(fragment.toString()));

            }
            System.out.println("fr = " + fr);
            // map.put("fragment", JSON.toJSONString(fragment));
            map.put("fr", JSON.toJSONString(fr));
            //System.out.println("title_high_______fragments = " + title_high.fragments().toString());
            map = hit.getSourceAsMap();
            //System.out.println("hit = " + hit);
            list.add(map);
        }
        return list;
    }


    //2. 获取这些数据实现搜索功能
    public List<Map<String, Object>> serchPageBuilder(String keyword, int pageNo, int pageSize) throws IOException {
        if(pageNo <=1) {
            pageNo = 1;
        }
        //条件搜索
        //1、构建搜索请求
        SearchRequest jd_goods = new SearchRequest("jd_goods");
        //2、设置搜索条件，使用该构建器进行查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建精确匹配查询条件
//        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
//        TermQueryBuilder termQuery = QueryBuilders.termQuery("title.keyword", keyword);
//        searchSourceBuilder.query(termQuery);




        // 自试 索引查询
//        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("title", keyword);   // 成功【单子多字都成功】
        searchSourceBuilder.query(queryBuilder);
//        QueryBuilder queryBuilder = QueryBuilders.matchQuery("title", "轻松");   // 成功【单子多字都成功】
//        searchSourceBuilder.query(queryBuilder);



        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);


//        HighlightBuilder.Field field = new HighlightBuilder.Field("title");
//        field.preTags("<font color='red'>");
//        field.postTags("</font>");
//        field.fragmentSize(100);






        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(true);
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        // highlightBuilder.requireFieldMatch(false);//匹配第一个即可
        // highlightBuilder.numOfFragments(0);

        searchSourceBuilder.highlighter(highlightBuilder);
        //3、将搜索条件放入搜索请求中
        jd_goods.source(searchSourceBuilder);
        //4、客户端执行搜索请求
        SearchResponse search = restHighLevelClient.search(jd_goods, RequestOptions.DEFAULT);
        System.out.println("共查询到"+search.getHits().getHits().length+"条数据");

        //5、打印测试
        Map<String, Object> map = new HashMap<>();
        ArrayList<Map<String,Object>> list = new ArrayList();
        for (SearchHit hit : search.getHits()) {

            //

            String value = hit.getSourceAsString();
            Content esProductTO = JSON.parseObject(value, Content.class);

            map.put("fragment", JSON.toJSONString(esProductTO.getTitle()));
            System.out.println(esProductTO.getTitle());


            map.put("fr", JSON.toJSONString(esProductTO.getTitle()));


            map = hit.getSourceAsMap();
            //System.out.println("hit = " + hit);
            list.add(map);

//            System.out.println("hit：\n" + hit);
//            System.out.println(hit.getHighlightFields() == null);
//            System.out.println(hit.getHighlightFields().get("title"));
//            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
//            System.out.println("highlightFields = " + highlightFields);
//            HighlightField title_high = highlightFields.get("title");
//            String fr = "";
//            for (Text fragment : title_high.fragments()) {
//                System.out.println("fragment = " + fragment);
//                fr = fragment.toString();
//                map.put("fragment", JSON.toJSONString(fragment.toString()));
//
//            }
//            System.out.println("fr = " + fr);
//            // map.put("fragment", JSON.toJSONString(fragment));
//            map.put("fr", JSON.toJSONString(fr));
//            //System.out.println("title_high_______fragments = " + title_high.fragments().toString());
//            map = hit.getSourceAsMap();
//            //System.out.println("hit = " + hit);
//            list.add(map);
        }
        return list;
    }


//    @Autowired
//    private ElasticsearchRestTemplate elasticsearchTemplate;
//    public static final Integer ROWS = 10;
//
//    public SearchResult search(String keyWord, Integer page) {
//        List<Content> houseList = new ArrayList();
//        Pageable pageable = PageRequest.of(page - 1, ROWS); // 设置分页参数
//        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.matchQuery("title", keyWord).operator(Operator.AND)) // match查询
//                .withPageable(pageable).withHighlightBuilder(getHighlightBuilder("title")) // 设置高亮
//                .build();
//        SearchHit<Content> searchHits = this.elasticsearchTemplate.search(searchQuery, Content.class);
////        System.out.println("共查询到"+searchHits.getHits().getHits().length+"条数据");
//        // List<SearchHit<HouseData>> list = housePage.getSearchHits();
//        for (SearchHit<Content> searchHit : searchHits) { // 获取搜索到的数据
//
//            Content content = (Content) searchHit.getHighlightFields();
//            Content houseData = new Content();
//            BeanUtils.copyProperties(content, houseData);
//
//            // 处理高亮
//            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
//            for (Map.Entry<String, List<String>> stringHighlightFieldEntry : highlightFields.entrySet()) {
//                String key = stringHighlightFieldEntry.getKey();
//                if (StringUtils.equals(key, "title")) {
//                    List<String> fragments = stringHighlightFieldEntry.getValue();
//                    StringBuilder sb = new StringBuilder();
//                    for (String fragment : fragments) {
//                        sb.append(fragment.toString());
//                    }
//                    houseData.setTitle(sb.toString());
//                }
//
//            }
//            houseList.add(houseData);
//        }
//
//        return new SearchResult(((Long) (searchHits.getTotalHits())).intValue(), houseList);
//    }

//    // 设置高亮字段
//    private HighlightBuilder getHighlightBuilder(String... fields) {
//        // 高亮条件
//        HighlightBuilder highlightBuilder = new HighlightBuilder(); // 生成高亮查询器
//        for (String field : fields) {
//            highlightBuilder.field(field);// 高亮查询字段
//        }
//        highlightBuilder.requireFieldMatch(false); // 如果要多个字段高亮,这项要为false
//        highlightBuilder.preTags("<span style=\"color:red\">"); // 高亮设置
//        highlightBuilder.postTags("</span>");
//        // 下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
//        highlightBuilder.fragmentSize(800000); // 最大高亮分片数
//        highlightBuilder.numOfFragments(0); // 从第一个分片获取高亮片段
//
//        return highlightBuilder;
//    }

//    /**
//     * 根据关键字进行检索
//     *
//     * @param builder
//     * @return
//     */
//    @Autowired
//    private ElasticsearchRestTemplate elasticsearchRestTemplate;
//
//    NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
//
//    // 搜索条件构造器构建：NativeSearchQuery
//    NativeSearchQuery searchQuery = searchQueryBuilder.build();
//
//    // 执行搜索，获取封装响应数据结果的SearchHits集合
//    SearchHits<Content> searchHits = elasticsearchRestTemplate.search(searchQuery, Content.class);
//    private Map<String, Object> searchForPage(NativeSearchQueryBuilder builder) {
//        // 关键字的高亮显示
//        // 继续封装检索条件
//        HighlightBuilder.Field field = new HighlightBuilder.Field("title");  //sku的name如果有关键字就进行高亮
//        field.preTags("<font color='red'>");    // 开始标签
//        field.postTags("</font>");              // 结束标签
//        field.fragmentSize(100);                // 显示的字符个数
//        builder.withHighlightFields(field);
//
//        NativeSearchQuery build = builder.build();
//        //AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(build, SkuInfo.class);
//        // 分组结果集
//        SearchHits<Content> searchHits = elasticsearchRestTemplate.search(builder.build(), Content.class);
//        // 对SearchHits集合进行分页封装
//        SearchPage<Content> page = SearchHitSupport.searchPageFor(searchHits, builder.build().getPageable());
//
//        // 取出高亮的结果数据，在该对象中
//        // 遍历: 对返回的内容进行处理(高亮字段替换原来的字段)
//        for(SearchHit<Content> searchHit:searchHits){
//            // 获取searchHit中的高亮内容
//            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
//            // 将高亮的内容填充到content中
//            searchHit.getContent().setName(highlightFields.get("name")==null ? searchHit.getContent().getName():highlightFields.get("name").get(0));
//        }
//
//        Map<String, Object> map = new HashMap<>();
//        // 商品结果集
//        map.put("rows", page.getContent());
//        //总条数
//        map.put("TotalElements", page.getTotalElements());
//        //总页数
//        map.put("TotalPages", page.getTotalPages());
//        // 分页当前页码
//        map.put("pageNum", build.getPageable().getPageNumber() + 1);
//        // 每页显示条数
//        map.put("pageSize", build.getPageable().getPageSize());
//
//        return map;
//    }



}