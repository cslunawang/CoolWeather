package com.example.coolweather.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.coolweather.R;
import com.example.coolweather.db.City;
import com.example.coolweather.db.Country;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    /**
     * 省市县  列表
     */
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;

    /**
     * 选中的、、列的级别
     */
    private Province selectedProvince;
    private City selectedCity;
    private Country selectedCountry;
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        //view  视图、容器、
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        //适配器/上下文、列表/simple_list_item_1内置的子项布局id
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        Log.d("dataList","begin");
        return view;
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("dataList","onclick"+currentLevel+"   "+LEVEL_PROVINCE);
                if (currentLevel == LEVEL_PROVINCE) {


                    selectedProvince = provinceList.get(i);
                    Log.d("dataList","beforeCity"+selectedProvince+provinceList.get(0));
                    //遍历市
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    //遍历县
                    queryCountries();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTRY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        //每次首先query
        queryProvinces();
    }

        /**
         * 查询所有的省 市 县，优先从数据库中查，如果没有查询到服务器
         */
        private final void queryProvinces(){
            titleText.setText("中国");
            backButton.setVisibility(View.GONE);//无法在返回上级
            //DataSupport
            Log.d("dataList",DataSupport.findAll(Province.class).size()+"");
            provinceList = DataSupport.findAll(Province.class);

            if(provinceList.size()>0){
                dataList.clear();
                for(Province province:provinceList){
                    dataList.add(province.getProvinceName());
                    Log.d("dataList",province.getProvinceName());
                }
                //展示出来/适配器适应改变
                adapter.notifyDataSetChanged();
                listView.setSelection(0);//设置默认选中
                currentLevel = LEVEL_PROVINCE;//
            }
            else{
                //查看本地数据库没有数据的时候，访问服务器
                String address = "http://guolin.tech/api/china";
                Log.d("dataList","queryServer");
                queryFromServer(address,"province");
            }
        }
        private void queryCities(){
            titleText.setText(selectedProvince.getProvinceName());
            backButton.setVisibility(View.VISIBLE);//可以显示
            //获取城市列表/本地有/本地没有访问服务器
            Log.d("dataList","citylist");
            cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
            Log.d("dataList",cityList.size()+"");
            if(cityList.size()>0){
                dataList.clear();
                for(City city : cityList){
                    dataList.add(city.getCityName());
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_CITY;
            }
            else {
                Log.d("dataList","beforeSearch");
                int provinceCode = selectedProvince.getProvinceCode();
                String address = "http://guolin.tech/api/china/"+provinceCode;
                queryFromServer(address,"city");
            }
        }
        private void queryCountries(){
            titleText.setText(selectedCity.getCityName());
            backButton.setVisibility(View.VISIBLE);
            countryList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(Country.class);

            if(countryList.size()>0){
                //表现出来
                dataList.clear();
                for(Country country:countryList){
                    dataList.add(country.getCountryName());
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_COUNTRY;
            }
            else{
                int province = selectedProvince.getProvinceCode();
                int city = selectedCity.getCityCode();
                String address = "http://guolin.tech/api/china/"+province+"/"+city;
//                Log.d("dataList","address   "+address);
                queryFromServer(address,"country");
            }
        }
        /**
         * 从服务器读取数据
         */
        private void queryFromServer(String address,final String type){
            showProgressDialog();//展示进度条
            HttpUtil.sendOkHttpRequest(address, new Callback() {

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText =  response.body().string();//返回的字符串
                    Log.d("dataList","responseText"+responseText);
                    boolean result = false;
                    //判断类型/返回数据写入数据库
                    if("province".equals(type)){
                        result = Utility.handleProvinceResponse(responseText);
                    }
                    else if("city".equals(type)){
                        result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                    }
                    else if("country".equals(type)){
                        result = Utility.handleCountryResponse(responseText,selectedCity.getId());
                    }
                    //如果获取成功/显示ui
                    if(result){//getActivity.runOnUiThread子线程切换到  主线程
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //关闭进度条
                                closeProgressDialog();
                                if("Province".equals(type)){
                                    queryProvinces();
                                }
                                else if("city".equals(type)){
                                    queryCities();
                                }
                                else if("country".equals(type)){
                                    queryCountries();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    //通过runonuithread回到主线程处理逻辑
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });

        }

        /**
         * 显示进度
         */
        private void showProgressDialog(){
            if(progressDialog == null){
                //getActivity
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("正在加载");
                progressDialog.setCanceledOnTouchOutside(false);
            }
        }
        private void closeProgressDialog(){
            if(progressDialog != null){
                progressDialog.dismiss();//diamiss关闭
            }
        }

}





