package com.example.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.coolweather.db.City;
import com.example.coolweather.db.Country;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * 解析类 处理response 写入数据库中
 */
public class Utility {

    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            Log.d("weatherContent",weatherContent);
            //解析 并 存入数据库
            return new Gson().fromJson(weatherContent,Weather.class);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)) {//TextUtils
            try{
                JSONArray allPronvinces = new JSONArray(response);
                for(int i = 0;i < allPronvinces.length();i++){
                    JSONObject provinceObject = allPronvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 解析返回的市数据
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCites = new JSONArray(response);
                for (int i = 0;i < allCites.length();i++){
                    JSONObject cityObject = allCites.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 获取返回的县级数据
     */
    public static boolean handleCountryResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCountries = new JSONArray(response);
                for(int i = 0;i < allCountries.length();i++){
                    JSONObject countryObject =  allCountries.getJSONObject(i);
                    Country country = new Country();
                    country.setCountryName(countryObject.getString("name"));
                    country.setId(countryObject.getInt("id"));
                    country.setCityId(cityId);
                    country.setWeatherId(countryObject.getString("weather_id"));
                    country.save();
                }
                return true;
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }





}
