package hc.common.enums;

import java.io.Serializable;

public enum AppHttpCodeEnum {
    SUCCESS(200,"操作成功"),
    NEED_LOGIN(300,"请先登录后操作"),
    NO_FOUND_PARAM(400,"未找到参数"),
    PARAM_INVALID(401,"无效参数"),
    SERVER_ERROR(500,"服务器内部出错"),
    DATA_NOT_EXIST(600,"数据不存在"),
    DATA_EXIST(601,"数据已存在"),
    DATA_BREACHES(602,"数据内容违规"),

    PICTURE_TOO_BIG(700,"图片过大"),
    PICTURE_FORMAT_ERROR(701,"图片格式错误"),
    UNKNOWN_ERROR(1000,"遭到外星人入侵");


    private Integer code;
    private String errorMsg;
    AppHttpCodeEnum(Integer code,String errorMsg){
        this.code=code;
        this.errorMsg=errorMsg;
    }
    public int getCode(){return code;}
    public String getErrorMsg(){return errorMsg;}
}
