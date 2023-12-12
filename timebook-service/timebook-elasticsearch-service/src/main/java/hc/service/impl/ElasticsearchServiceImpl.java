package hc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import hc.service.ElasticsearchService;
import hc.uniapp.note.dtos.NoteHighDocDto;
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
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static hc.LogUtils.info;

/**
 * es搜索服务
 */
@Service
@Slf4j
public class ElasticsearchServiceImpl implements ElasticsearchService {
    private RestHighLevelClient elasticsearchClient;

    @Autowired
    public ElasticsearchServiceImpl(RestHighLevelClient elasticsearchClient) {
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

    /**
     * 简单创建一个索引库
     * @param libraryName
     * @param field
     * @return
     */
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

    /**
     * 判断一个索引库是否存在
     * @param libraryName
     * @return
     */
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

    /**
     * 删除索引库
     * @param libraryName
     * @return
     */
    public Boolean deleteIIndexLibrary(String libraryName){
        DeleteIndexRequest request=new DeleteIndexRequest(libraryName);
        try {
            elasticsearchClient.indices().delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 新增一个搜索文档
     * @param libraryName
     * @param id
     * @param t
     * @return
     * @param <T>
     */
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

    /**
     * 批量新增文档
     * @param libraryName
     * @param list
     * @param idExtractor
     * @return
     * @param <T>
     */
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

    /**
     * 简单搜索文档
     * @param libraryName
     * @param id
     * @param type
     * @return
     * @param <T>
     */
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

    /**
     * 更新文档
     * @param libraryName
     * @param id
     * @param updateField
     * @return
     */
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

    /**
     * 删除文档
     * @param libraryName
     * @param id
     * @return
     */
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

    /**
     * 按序搜索整个库文档
     * @param libraryName
     * @param sortColumn
     * @param type
     * @return
     * @param <T>
     */
    public <T>List<T> queryMatchAllAndSort(String libraryName,String sortColumn,Class<T> type){
        SearchRequest request=new SearchRequest(libraryName);
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().sort(sortColumn, SortOrder.DESC);
        return handleResponse(type, request);
    }

    /**
     * 简单搜索文档，已集成拼音搜索
     * @param libraryName
     * @param column
     * @param content
     * @param type
     * @return
     * @param <T>
     */
    public <T>List<T> queryMatch(String libraryName,String column,String content,Class<T> type){
        SearchRequest request=new SearchRequest(libraryName);
        request.source().query(QueryBuilders.matchQuery(column,content));
        return handleResponse(type, request);
    }

    /**
     * 按倒序搜索文档
     * @param libraryName
     * @param column
     * @param content
     * @param sortColumn
     * @param type
     * @return
     * @param <T>
     */
    public <T>List<T> queryMatchAndSort(String libraryName,String column,String content
            ,String sortColumn,Class<T> type){
        SearchRequest request=new SearchRequest(libraryName);
        request.source().query(QueryBuilders.matchQuery(column,content));
        request.source().sort(sortColumn, SortOrder.DESC);
        return handleResponse(type, request);
    }

    /**
     * 高亮文档搜索，已集成拼音搜索
     * @param libraryName
     * @param column
     * @param content
     * @param sortColumn
     * @param highColumn
     * @return
     */
    public List<NoteHighDocDto> queryHighLightAndSort(String libraryName, String column, String content
            , String sortColumn, String highColumn){
        SearchRequest request=new SearchRequest(libraryName);
        request.source().query(QueryBuilders.matchQuery(column,content));
        request.source().highlighter(new HighlightBuilder().field(highColumn).requireFieldMatch(false));
        request.source().sort(sortColumn, SortOrder.DESC);
        return highLightResponse(highColumn,request);
    }

    /**
     * 字符自动补全功能
     * @param libraryName
     * @param column
     * @param prefix
     * @return
     */
    public List<String> querySuggest(String libraryName, String column, String prefix){
        SearchRequest request=new SearchRequest(libraryName);
        request.source().suggest(new SuggestBuilder().addSuggestion(
                column,
                SuggestBuilders.completionSuggestion(column)
                        .prefix(prefix)
                        .skipDuplicates(true)
                        .size(10)));
        return suggestResponse(column,request);
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
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private List<NoteHighDocDto> highLightResponse(String highColumn, SearchRequest request){
        List<NoteHighDocDto> list=new ArrayList<>();
        try {
            SearchResponse response=elasticsearchClient.search(request,RequestOptions.DEFAULT);
            SearchHits searchHits=response.getHits();
            long total=searchHits.getTotalHits().value;
            info("共搜索到"+total+"条数据");
            SearchHit[] hits=searchHits.getHits();
            for(SearchHit hit: hits){
                String json=hit.getSourceAsString();
                NoteHighDocDto elasticSearchNoteDto=JSONUtil.toBean(json, NoteHighDocDto.class);
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
    private List<String> suggestResponse(String column,SearchRequest request){
        List<String> list=new ArrayList<>();
        try {
            SearchResponse response = elasticsearchClient.search(request, RequestOptions.DEFAULT);
            Suggest suggest=response.getSuggest();
            CompletionSuggestion suggestion=suggest.getSuggestion(column);
            for(CompletionSuggestion.Entry.Option option: suggestion.getOptions()){
                String text=option.getText().string();
                list.add(text);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}