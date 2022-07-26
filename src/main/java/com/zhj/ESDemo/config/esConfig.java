package com.zhj.ESDemo.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topic
 * Description
 *
 * @author zhouh
 * @version 1.0
 * Create by 2022/7/24 20:35
 */
@Configuration //xml文件
public class esConfig {

    //spring <bean id="restHighLevelClient" class="RestHighLevelClient"/>
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        //如果是集群则构建多个，否则构建一个即可
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
        // new HttpHost("localhost", 9201, "http")));
        return client;
    }
}
