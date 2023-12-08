package hc.service;

import hc.common.dtos.ResponseResult;

public interface SearchService {
    ResponseResult searchAll(String context);

    ResponseResult searchNote(String content);
}
