package com.wangxu.studentonline.ihttp.http.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wangxu.studentonline.app.App;
import com.wangxu.studentonline.ihttp.http.http.inetworkcallback.INetworkcallback;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by dell on 2017/10/29.
 */

public class Okhttputils implements  Ihttp{

    private static Okhttputils oKhttpUtils;
    private OkHttpClient okHttpClient;
    private Okhttputils(){

        okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder builder = request.newBuilder();
                String cookie = getCookie();
                if (null != cookie && !"".equals(cookie)) {
                    builder.addHeader("Cookie", cookie);
                }
                Response response = chain.proceed(builder.build());

                return response;
            }
        }).addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //得到请求的Request对象
                Request request = chain.request();
                //服务器返回的响应
                Response response = chain.proceed(request);
                if (request.url().toString().contains("action/api/login_validate")) {
                    //获取响应头
                    Headers headers = response.headers();

                    //获取响应头中的所有key,value值

                    Set<String> names = headers.names();
                    String cookie = "";
                    for (String key : names) {
                        String value = headers.get(key);
                        if ("Set-Cookie".equals(key)) {
                            //多个Set-Cookie以分号进行分割
                            cookie = cookie + value + ";";
                        }
                    }
                    if (cookie.length() > 0) {
                        cookie = cookie.substring(0, cookie.length() - 1);
                    }

                    saveCookie(cookie);
                }
                return response;
            }
        }).build();

    }

    public static Okhttputils getInstance(){
        if(oKhttpUtils == null)
            synchronized (Okhttputils.class) {
                if (oKhttpUtils == null)
                    oKhttpUtils = new Okhttputils();
            }
        return oKhttpUtils;
    }


    @Override
    public void get(String url, Map<String, String> params, final INetworkcallback networkcallback) {
        if(params != null && params.size() > 0){
            //得到所有的key
            Set<String> keys = params.keySet();
            StringBuffer sb = new StringBuffer(url);
            sb.append("?");
            //遍历所有的key
            for (String key : keys){
                //根据key值获取value
                String value = params.get(key);
                sb.append(key).append("=").append(value).append("&");
            }

            url = sb.substring(0,sb.length()-1);
            Log.e("url",url);
        }
        //第二步：创建Request对象
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                App.App.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        networkcallback.onError(555+"",e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();

                App.App.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        networkcallback.onsuccess(string);
                        Log.e("Tcg",string);
                    }
                });
            }
        });

    }

    @Override
    public void get(String url, final INetworkcallback networkcallback) {

        Request request = new Request.Builder().url(url).build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                App.App.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        networkcallback.onError("666",e.getMessage());
                    }
                });


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String xmlData = response.body().string();
                App.App.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        XStream stream = new XStream();
//                        stream.alias("oschina", Home.class);
//                        stream.alias("news",Home.NewsBean.class);
//                        Home fromXML = (Home)stream.fromXML(xmlData);
//
//                        networkcallback.onsuccess(fromXML);
                        networkcallback.onsuccess(xmlData);

                    }
                });

            }
        });
    }

    @Override
    public void post(String url, Map<String, String> params, final INetworkcallback networkcallback) {

        FormBody.Builder builder = new FormBody.Builder();
        if(params != null && params.size() > 0){
            Set<String> keys = params.keySet();
            for (String key : keys){
                String value = params.get(key);
                builder.add(key,value);

            }
        }
        Request request = new Request.Builder().url(url).post(builder.build()).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                networkcallback.onError("666",e.getMessage());
            }

            @Override
            public void onResponse(Call call,  Response response) throws IOException {

                final String xmlData = response.body().string();
                networkcallback.onsuccess(xmlData);

            }
        });
    }

    @Override
    public void loadImage(String imgUrl, ImageView imageView) {

        Glide.with(App.App).load(imgUrl).into(imageView);
    }



//    /**
//     * 自动解析json至回调中的JavaBean
//     * @param jsonData
//     * @param callBack
//     * @param <T>
//     * @return
//     */
//    private <T> T getGeneric(String jsonData,INetworkcallback<T> callBack){
//        Gson gson = new Gson();
//        //通过反射获取泛型的实例
//        Type[] types = callBack.getClass().getGenericInterfaces();//得到这个类所实现的所有接口的集合
//        Type[] actualTypeArguments = ((ParameterizedType) types[0]).getActualTypeArguments();//获取该接口中所有的参数
//        Type type = actualTypeArguments[0];//取第一个参数，就是对应JavaBean
//        T t = gson.fromJson(jsonData,type);//通过gson转到对应的JavaBean
//        return t;
//    }




    private String getCookie() {
        SharedPreferences preferences = App.App.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        String cookie = preferences.getString("Cookie", "");
        return cookie;
    }

    private void saveCookie(String cookie) {
        SharedPreferences preferences = App.App.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("Cookie", cookie);
        edit.commit();
    }




}


