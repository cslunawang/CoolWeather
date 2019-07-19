package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class AQI {
    @SerializedName("city")
    public AQICity city;

    public class AQICity{

        public String aqi;
        public String pm25;
    }
}
