package hc.common.exception;


import hc.common.enums.AppHttpCodeEnum;
import lombok.Data;

@Data
public class ServerErrorException extends RuntimeException{
    private Integer code;
    private String errorMsg;
    public ServerErrorException(AppHttpCodeEnum enums){
        this.code=enums.getCode();
        this.errorMsg=enums.getErrorMsg();
    }
}
