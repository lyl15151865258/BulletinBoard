package com.zhongbenshuo.bulletinboard.network;

import com.google.gson.JsonObject;
import com.zhongbenshuo.bulletinboard.bean.OpenAndCloseDoorRecord;
import com.zhongbenshuo.bulletinboard.bean.Result;
import com.zhongbenshuo.bulletinboard.bean.Weather;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Retrofit网络请求构建接口
 * Created at 2018/11/28 13:48
 *
 * @author LiYuliang
 * @version 1.0
 */

public interface ZbsApi {

    /**
     * 查询服务器RSA公钥
     *
     * @return 返回值
     */
    @GET("user/rsaPublicKey.do")
    Observable<Result> getRSAPublicKey();

    /**
     * 远程开关门
     *
     * @return 返回值
     */
    @POST("user/openAndCloseDoorRecord.do")
    Observable<Result> openAndCloseDoorRecord(@Body OpenAndCloseDoorRecord params);

    /**
     * 高德天气接口
     *
     * @return 返回值
     */
    @POST("weather/weatherInfo")
    @FormUrlEncoded
    Observable<Weather> searchWeather(@Field("key") String key, @Field("city") String city, @Field("extensions") String extensions, @Field("output") String output);

    /**
     * 查询最新的版本信息
     *
     * @return 返回值
     */
    @POST("user/searchNewVersion.do")
    Observable<Result> searchNewVersion(@Body JsonObject params);

    /**
     * 下载软件
     *
     * @return 文件
     */
    @Streaming
    @GET
    Observable<ResponseBody> executeDownload(@Header("Range") String range, @Url() String url);

}
