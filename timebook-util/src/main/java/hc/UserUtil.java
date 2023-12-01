package hc;

import hc.uniapp.user.pojos.User;

public class UserUtil {

    private static  User user;

    public static void saveUser(User u){
        user=u;
    }
    public static User getUser(){
        return user;
    }

}
