package com.iosoft2020;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.fastjson.JSON;
import com.iosoft2020.pojo.User;

@SpringBootTest
class SpringBootElasticSearchDemoApplicationTests {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Test
    void contextLoads() {
    }

    @Test
    public void testCreateIndex() throws IOException {

        CreateIndexRequest createIndexRequest = new CreateIndexRequest("hello_index");

        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest,
                RequestOptions.DEFAULT);

        System.err.println(createIndexResponse);

    }

    @Test
    public void testExistIndex() throws IOException {

        GetIndexRequest getIndexRequest = new GetIndexRequest("hello_index");

        boolean isExist = restHighLevelClient.indices().exists(getIndexRequest,
                RequestOptions.DEFAULT);

        System.err.println(isExist);

    }

    @Test
    public void testDeleteIndex() throws IOException {

        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("hello_index");

        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().delete(deleteIndexRequest,
                RequestOptions.DEFAULT);

        System.err.println(acknowledgedResponse.isAcknowledged());

    }

    @Test
    public void testAddDocument() throws IOException {

        User user = new User("iosoft2020", 18);

        IndexRequest indexRequest = new IndexRequest("hello_index");
        indexRequest.id("1");
        indexRequest.timeout(TimeValue.timeValueSeconds(1));
        indexRequest.timeout("1s");

        indexRequest.source(JSON.toJSONString(user), XContentType.JSON);

        IndexResponse indexResponse = restHighLevelClient.index(indexRequest,
                RequestOptions.DEFAULT);

        System.err.println(indexResponse);
        System.err.println(indexResponse.status());
    }

    @Test
    public void testExistDocument() throws IOException {

        GetRequest getRequest = new GetRequest("hello_index", "1");
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");

        boolean isExist = restHighLevelClient.exists(getRequest,
                RequestOptions.DEFAULT);

        System.err.println(isExist);

    }

    @Test
    public void testGetDocument() throws IOException {

        GetRequest getRequest = new GetRequest("hello_index", "1");

        GetResponse getResponse = restHighLevelClient.get(getRequest,
                RequestOptions.DEFAULT);

        System.err.println(getResponse);
        System.err.println(getResponse.getSourceAsString());

    }

    @Test
    public void testUpdateDocument() throws IOException {

        UpdateRequest updateRequest = new UpdateRequest("hello_index", "1");
        updateRequest.timeout("1s");

        User user = new User("iosoft2021", 18);

        updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest,
                RequestOptions.DEFAULT);

        System.err.println(updateResponse.status());

    }

    @Test
    public void testDeleteDocument() throws IOException {

        DeleteRequest deleteRequest = new DeleteRequest("hello_index", "1");
        deleteRequest.timeout("1s");

        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest,
                RequestOptions.DEFAULT);

        System.err.println(deleteResponse.status());

    }

    @Test
    public void testBulkDocument() throws IOException {

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("100s");

        List<User> users = Arrays.asList(new User("iosoft1", 1), new User("iosoft1", 2), new User("iosoft1", 3));

        IntStream.rangeClosed(0, 2).forEach(index -> {
            bulkRequest.add(new IndexRequest("hello_index").id("" + (index + 1)).source(JSON.toJSONString(users
                    .get(index)),
                    XContentType.JSON));
        });
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest,
                RequestOptions.DEFAULT);
        System.err.println(bulkResponse.hasFailures());

    }

    @Test
    public void testSearchDocument() throws IOException {

        SearchRequest searchRequest = new SearchRequest("hello_index");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "iosoft1");
//        MatchAllQueryBuilder matchAllQuery = QueryBuilders.matchAllQuery();
        searchSourceBuilder.query(termQueryBuilder);

        searchSourceBuilder.from(1);
        searchSourceBuilder.size(1);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest,
                RequestOptions.DEFAULT);

        System.err.println(JSON.toJSONString(searchResponse.getHits()));

        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            searchHit.getSourceAsMap().entrySet().forEach(entry -> {
                System.err.println(entry.getKey() + "=" + entry.getValue());

            });
        }

    }

}
