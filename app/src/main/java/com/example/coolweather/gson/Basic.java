package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    //jsonz中某些字段  不太适合  作为java字段命名，
//    @SerializedName("city")注解方式来  建立  json和java字段之间的 映射关系
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    @SerializedName("update")
    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}
