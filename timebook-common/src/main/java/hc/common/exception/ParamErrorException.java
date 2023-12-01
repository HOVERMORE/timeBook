package hc.common.exception;


import hc.common.enums.AppHttpCodeEnum;
import lombok.Data;

@Data
public class ParamErrorException extends RuntimeException{
    private Integer code;
    private String errorMsg;
    public ParamErrorException(AppHttpCodeEnum enums){
        this.code=enums.getCode();
        this.errorMsg=enums.getErrorMsg();
    }
}
