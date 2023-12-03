package hc.common.dtos;

import hc.common.enums.AppHttpCodeEnum;

import java.io.Serializable;

/**
 * 结果返回类
 * @param <T>
 */
public class ResponseResult<T> implements Serializable {
    private String msg;


    private Integer code;
    private T data;

    public ResponseResult(){
        this.code=200;
    }
    public ResponseResult(Integer code,T data){
        this.code=code;
        this.data=data;
    }
    public ResponseResult(Integer code,String errorMsg,T data){
        this.code=code;
        this.msg=errorMsg;
        this.data=data;
    }
    public ResponseResult(Integer code,String errorMsg){
        this.code=code;
        this.msg=errorMsg;
    }
    public static ResponseResult errorResult(Integer code,String errorMsg){
        ResponseResult responseResult=new ResponseResult();
        return responseResult.error(code,errorMsg);
    }

    public static ResponseResult errorResult(AppHttpCodeEnum enums){
        return setAppHttpCodeEnum(enums,enums.getErrorMsg());
    }
    public static ResponseResult errorResult(AppHttpCodeEnum enums,String errorMsg){
        return setAppHttpCodeEnum(enums,errorMsg);
    }

    public static ResponseResult okResult(Integer code, String msg) {
        ResponseResult result=new ResponseResult();
        return result.ok(code,null,msg);
    }

    public static ResponseResult okResult(Object data){
        ResponseResult result=setAppHttpCodeEnum(AppHttpCodeEnum.SUCCESS,AppHttpCodeEnum.SUCCESS.getErrorMsg());
        if(data!=null){
            result.setData(data);
        }
        return result;
    }

    public static ResponseResult okResult(){
        ResponseResult result=setAppHttpCodeEnum(AppHttpCodeEnum.SUCCESS,AppHttpCodeEnum.SUCCESS.getErrorMsg());

        return result;
    }

    public ResponseResult<?> ok(Integer code, T data, String errorMsg) {
        this.code=code;
        this.data=data;
        this.msg=errorMsg;
        return this;
    }

    public ResponseResult<?> ok(Integer code, T data) {
        this.code = code;
        this.data = data;
        return this;
    }
    public ResponseResult<?> ok(T data){
        this.data=data;
        return this;
    }

    public ResponseResult error(Integer code, String errorMsg) {
        this.code=code;
        this.msg=errorMsg;
        return this;
    }


    private static ResponseResult setAppHttpCodeEnum(AppHttpCodeEnum enums, String errorMsg) {
        return okResult(enums.getCode(),errorMsg);
    }



    public String getErrorMsg() {
        return msg;
    }

    public void setErrorMsg(String errorMsg) {
        this.msg = errorMsg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                ", data=" + data +
                '}';
    }
}
