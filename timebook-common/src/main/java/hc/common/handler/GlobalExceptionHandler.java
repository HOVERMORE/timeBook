package hc.common.handler;


import hc.common.exception.CustomizeException;
import hc.common.exception.DataException;
import hc.common.exception.ParamErrorException;
import hc.common.exception.ServerErrorException;
import hc.common.dtos.ResponseResult;
import hc.common.enums.AppHttpCodeEnum;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static hc.LogUtils.error;


@RestControllerAdvice()
public class GlobalExceptionHandler {


    @ResponseBody
    @ExceptionHandler(CustomizeException.class)
    public ResponseResult CustomizeException(CustomizeException e){
        error("自定义异常： "+e.getMessage());
        return ResponseResult.errorResult(AppHttpCodeEnum.UNKNOWN_ERROR,
                AppHttpCodeEnum.UNKNOWN_ERROR.getErrorMsg());
    }

    @ResponseBody
    @ExceptionHandler(DataException.class)
    public ResponseResult DataException(DataException e){
        error("数据异常： "+e.getErrorMsg());
        return ResponseResult.errorResult(e.getCode(),e.getErrorMsg());
    }

    @ResponseBody
    @ExceptionHandler(ParamErrorException.class)
    public ResponseResult ParamErrorException(ParamErrorException e){
        error("参数异常： "+e.getErrorMsg());
        return ResponseResult.errorResult(e.getCode(),e.getErrorMsg());
    }

    @ResponseBody
    @ExceptionHandler(ServerErrorException.class)
    public ResponseResult ServerErrorException(ServerErrorException e){
        error("服务器异常： "+e.getErrorMsg());
        return ResponseResult.errorResult(e.getCode(),e.getErrorMsg());
    }

}
