package hc.service;

import hc.common.dtos.ResponseResult;

public interface SearchService {
    ResponseResult searchAlbumOrImage(String context);

    ResponseResult searchNote(String content);

    ResponseResult searchSuggestion(String prefix);
}
