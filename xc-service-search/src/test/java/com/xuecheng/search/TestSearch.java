package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private RestClient restClient;

    //搜索全部
    @Test
    public void testSearchAll() throws IOException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置搜索方式
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());//搜索全部
        //设置源字段的过滤,第一个参数是结果集包括哪些字段，第二个参数表示结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索,向ES发起HTTP请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits1) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> map = hit.getSourceAsMap();
            String name =(String) map.get("name");
            String stydymodel =(String) map.get("stydymodel");
            Double price =(Double) map.get("price");
            Date timestamp =(Date) map.get("timestamp");
        }
    }


    //分页查询
    @Test
    public void testSearchPage() throws IOException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1);
        //设置搜索方式
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());//搜索全部
        //设置源字段的过滤,第一个参数是结果集包括哪些字段，第二个参数表示结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索,向ES发起HTTP请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits1) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> map = hit.getSourceAsMap();
            String name =(String) map.get("name");
            String stydymodel =(String) map.get("stydymodel");
            Double price =(Double) map.get("price");
            Date timestamp =(Date) map.get("timestamp");
        }
    }

    }
