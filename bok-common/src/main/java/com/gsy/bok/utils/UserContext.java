package com.gsy.bok.utils;

public class UserContext {

    public static final ThreadLocal<String> thread = new ThreadLocal<>();

    public static void set(String data){
        thread.set(data);
    }
    public static String get(){
        return thread.get();
    }
    public static void remove(){
        thread.remove();
    }
}
