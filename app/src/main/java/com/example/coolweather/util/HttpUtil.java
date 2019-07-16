package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
//        client.Builder().addNetworkInterceptor(new StethoInterceptor());
        Request request =  new Request.Builder().url(address).build();

        //回调/okhttp3.callback参数，这个是okhttp库自带的回调接口
        // enqueue把参数传入，
        //内部已经开启好子线程了，然后在子线程中执行HTTP请求，并将结果回调到okhttp3.callback
        client.newCall(request).enqueue(callback);
    }
}
