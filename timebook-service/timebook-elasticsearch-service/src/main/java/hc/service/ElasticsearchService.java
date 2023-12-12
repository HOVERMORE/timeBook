package hc.service;

import hc.service.impl.ElasticsearchServiceImpl;
import hc.uniapp.note.dtos.NoteHighDocDto;

import java.util.List;
import java.util.Map;

public interface ElasticsearchService {
    public Boolean createIndexLibrary(String libraryName,String field);
    public Boolean existIndexLibrary(String libraryName);
    public Boolean deleteIIndexLibrary(String libraryName);
    public <T> Boolean addIndexDocumentById(String libraryName,String id,T t);
    public <T> Boolean addIndexDocumentById(String libraryName, T t, ElasticsearchServiceImpl.IdExtractor<T> idExtractor);
    public <T> Boolean addIndexDocumentByIds(String libraryName, List<T> list, ElasticsearchServiceImpl.IdExtractor<T> idExtractor);
    public <T>T queryDocumentById(String libraryName,String id,Class<T> type);
    public <T>T queryDocumentById(String libraryName, T t, ElasticsearchServiceImpl.IdExtractor<T> idExtractor, Class<T> type);
    public Boolean updateDocumentById(String libraryName,String id,String updateField);
    public <T>Boolean updateDocumentById(String libraryName, T t, ElasticsearchServiceImpl.IdExtractor<T> idExtractor, String updateField);
    public <T>Boolean updateDocumentById(String libraryName, T t, ElasticsearchServiceImpl.IdExtractor<T> idExtractor, Map<String,String> map);
    public Boolean deleteDocument(String libraryName,String id);
    public <T>Boolean deleteDocument(String libraryName, T t, ElasticsearchServiceImpl.IdExtractor<T> idExtractor);
    public <T>List<T> queryMatchAll(String libraryName,Class<T> type);
    public <T>List<T> queryMatchAllAndSort(String libraryName,String sortColumn,Class<T> type);
    public <T>List<T> queryMatch(String libraryName,String column,String content,Class<T> type);
    public <T>List<T> queryMatchAndSort(String libraryName,String column,String content
            ,String sortColumn,Class<T> type);
    public List<NoteHighDocDto> queryHighLightAndSort(String libraryName, String column, String content
            , String sortColumn, String highColumn);
    public List<String> querySuggest(String libraryName, String column, String prefix);
    public <T>List<T> BoolQuery(String libraryName,String column,String content,Class<T> type);
}
