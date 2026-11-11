package com.gsy.bok.utils;

import com.gsy.bok.domain.LoginInfo;

public class UserContext {

    public static final ThreadLocal<LoginInfo> thread = new ThreadLocal<>();

    public static void set(LoginInfo info){
        thread.set(info);
    }
    public static LoginInfo get(){
        return thread.get();
    }
    public static void remove(){
        thread.remove();
    }
}
