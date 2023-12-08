package hc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import hc.uniapp.note.dtos.ElasticSearchNoteDto;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static hc.LogUtils.info;

@Component
@Slf4j
public class ElasticSearcherClient {
    private RestHighLevelClient elasticsearchClient;

    @Autowired
    public ElasticSearcherClient(RestHighLevelClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @PreDestroy
    public void closeElasticsearchClient() throws IOException {
        if (elasticsearchClient != null) {
            elasticsearchClient.close();
        }
    }

    @FunctionalInterface
    public interface IdExtractor<T> {
        String getId(T item);
    }
    public Boolean createIndexLibrary(String libraryName,String field){
        CreateIndexRequest request=new CreateIndexRequest(libraryName);
        request.source(field, XContentType.JSON);
        try {
            elasticsearchClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public Boolean existIndexLibrary(String libraryName){
        GetIndexRequest request=new GetIndexRequest(libraryName);
        boolean exists= false;
        try {
            exists = elasticsearchClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return exists;
    }
    public Boolean deleteIIndexLibrary(String libraryName){
        DeleteIndexRequest request=new DeleteIndexRequest(libraryName);
        try {
            elasticsearchClient.indices().delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public <T> Boolean addIndexDocumentById(String libraryName,String id,T t){
        IndexRequest request=new IndexRequest(libraryName).id(id);
        request.source(JSONUtil.toJsonStr(t),XContentType.JSON);
        try {
            elasticsearchClient.index(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public <T> Boolean addIndexDocumentById(String libraryName,T t,IdExtractor<T> idExtractor){
        IndexRequest request=new IndexRequest(libraryName).id(idExtractor.getId(t));
        request.source(JSONUtil.toJsonStr(t),XContentType.JSON);
        try {
            elasticsearchClient.index(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public <T> Boolean addIndexDocumentByIds(String libraryName,List<T> list,IdExtractor<T> idExtractor){
        BulkRequest request=new BulkRequest();
        for(T t:list){
            request.add(new IndexRequest(libraryName)
                    .id(idExtractor.getId(t))
                    .source(JSONUtil.toJsonStr(t),XContentType.JSON));
        }
        try {
            elasticsearchClient.bulk(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public <T>T queryDocumentById(String libraryName,String id,Class<T> type){
        GetRequest request=new GetRequest(libraryName,id);
        GetResponse response;
        T t=null;
        try {
            response = elasticsearchClient.get(request, RequestOptions.DEFAULT);
            String json = response.getSourceAsString();
            t=JSONUtil.toBean(json,type);
        } catch (IOException e) {
            return  t;
        }
        return t;
    }
    public <T>T queryDocumentById(String libraryName,T t,IdExtractor<T> idExtractor,Class<T> type){
        GetRequest request=new GetRequest(libraryName,idExtractor.getId(t));
        GetResponse response;
        try {
            response = elasticsearchClient.get(request, RequestOptions.DEFAULT);
            String json = response.getSourceAsString();
            t=JSONUtil.toBean(json,type);
        } catch (IOException e) {
            return  t;
        }
        return t;
    }
    public Boolean updateDocumentById(String libraryName,String id,String updateField){
        UpdateRequest request=new UpdateRequest(libraryName,id);
        request.doc(updateField);
        try {
            elasticsearchClient.update(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public <T>Boolean updateDocumentById(String libraryName,T t,IdExtractor<T> idExtractor,String updateField){
        UpdateRequest request=new UpdateRequest(libraryName,idExtractor.getId(t));
        request.doc(updateField);
        try {
            elasticsearchClient.update(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public <T>Boolean updateDocumentById(String libraryName,T t,IdExtractor<T> idExtractor,Map<String,String> map){
        UpdateRequest request=new UpdateRequest(libraryName,idExtractor.getId(t));
        request.doc(map);
        try {
            elasticsearchClient.update(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public Boolean deleteDocument(String libraryName,String id){
        DeleteRequest request=new DeleteRequest(libraryName,id);
        try {
            elasticsearchClient.delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public <T>Boolean deleteDocument(String libraryName,T t,IdExtractor<T> idExtractor){
        DeleteRequest request=new DeleteRequest(libraryName,idExtractor.getId(t));
        try {
            elasticsearchClient.delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    /**
     * 搜索整个库文档
     * @param libraryName
     * @param type
     * @return
     * @param <T>
     */
    public <T>List<T> queryMatchAll(String libraryName,Class<T> type){
        SearchRequest request=new SearchRequest(libraryName);
        request.source().query(QueryBuilders.matchAllQuery());
        return handleResponse(type, request);
    }
    public <T>List<T> queryMatchAllAndSort(String libraryName,String sortColumn,Class<T> type){
        SearchRequest request=new SearchRequest(libraryName);
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().sort(sortColumn, SortOrder.DESC);
        return handleResponse(type, request);
    }
    public <T>List<T> queryMatch(String libraryName,String column,String content,Class<T> type){
        SearchRequest request=new SearchRequest(libraryName);
        request.source().query(QueryBuilders.matchQuery(column,content));
        return handleResponse(type, request);
    }

    public <T>List<T> queryMatchAndSort(String libraryName,String column,String content
            ,String sortColumn,Class<T> type){
        SearchRequest request=new SearchRequest(libraryName);
        request.source().query(QueryBuilders.matchQuery(column,content));
        request.source().sort(sortColumn, SortOrder.DESC);
        return handleResponse(type, request);
    }
    public List<ElasticSearchNoteDto> queryHighLightAndSort(String libraryName,String column,String content
            ,String sortColumn,String highColumn){
        SearchRequest request=new SearchRequest(libraryName);
        request.source().query(QueryBuilders.matchQuery(column,content));
        request.source().highlighter(new HighlightBuilder().field(highColumn).requireFieldMatch(false));
        request.source().sort(sortColumn, SortOrder.DESC);
        return highLightResponse(highColumn,request);
    }
    /**
     * 精确搜索
     * @param libraryName
     * @param column
     * @param content
     * @param type
     * @return
     * @param <T>
     */
    public <T>List<T> BoolQuery(String libraryName,String column,String content,Class<T> type){
        SearchRequest request=new SearchRequest(libraryName);
        BoolQueryBuilder boolquery=QueryBuilders.boolQuery();
        boolquery.must(QueryBuilders.termQuery(column, content));
        return handleResponse(type,request);
    }

    private <T> List<T> handleResponse(Class<T> type, SearchRequest request) {
        List<T> list=new ArrayList<>();
        try {
            SearchResponse response=elasticsearchClient.search(request,RequestOptions.DEFAULT);
            SearchHits searchHits=response.getHits();
            long total=searchHits.getTotalHits().value;
            info("共搜索到"+total+"条数据");
            SearchHit[] hits=searchHits.getHits();
            for(SearchHit hit: hits){
                String json=hit.getSourceAsString();
                T t=JSONUtil.toBean(json, type);
                list.add(t);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    private List<ElasticSearchNoteDto> highLightResponse(String highColumn, SearchRequest request){
        List<ElasticSearchNoteDto> list=new ArrayList<>();
        try {
            SearchResponse response=elasticsearchClient.search(request,RequestOptions.DEFAULT);
            SearchHits searchHits=response.getHits();
            long total=searchHits.getTotalHits().value;
            info("共搜索到"+total+"条数据");
            SearchHit[] hits=searchHits.getHits();
            for(SearchHit hit: hits){
                String json=hit.getSourceAsString();
                ElasticSearchNoteDto elasticSearchNoteDto=JSONUtil.toBean(json, ElasticSearchNoteDto.class);
                Map<String, HighlightField> highlightFieldMap=hit.getHighlightFields();
                if(CollUtil.isNotEmpty(highlightFieldMap)){
                    HighlightField highlightField=highlightFieldMap.get(highColumn);
                    if(highlightField!=null){
                        String highlight=highlightField.getFragments()[0].string();
                        elasticSearchNoteDto.setHighLight(highlight);
                    }
                }
                list.add(elasticSearchNoteDto);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
