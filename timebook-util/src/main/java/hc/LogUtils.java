package hc;

import hc.thread.UserHolder;

import hc.uniapp.user.pojos.User;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LogUtils {

    public static void info(String msg){
        log.info("-----------");
        msg=isExist(msg);
        log.info(msg);
        log.info("-----------");
    }


    public static void warn(String msg) {
        log.warn("-----------");
        msg=isExist(msg);
        log.warn(msg);
        log.warn("-----------");
    }


    public static void error(String msg) {
        log.error("-----------");
        msg=isExist(msg);
        log.error(msg);
        log.error("-----------");
    }

    private static String isExist(String msg){
        User user= UserHolder.getUser();
        if(user!=null)
            msg ="["+ user.getNickName() + "]: " + msg;
        else
            msg ="[···离线中···]: " + msg;
        return msg;
    }
}
