package com.kite.mnemoai.data.network;


import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DeepseekService {

    String DEEPSEEK_BASE_URL = "https://api.deepseek.com";
    @Headers("Authorization: Bearer sk-b0f24b1943d44ecb888f85d5097e25a9")
    @POST("chat/completions")
    Call<DeepseekResponseBody> getDeepseekResponseBody(@Body DeepseekRequestBody body);

    class Factory{
        private static volatile DeepseekService instance;
        public static DeepseekService getInstance(){
            if(instance == null){
                synchronized (Factory.class){
                    if(instance == null){
                        instance = new Retrofit.Builder()
                                .baseUrl(DEEPSEEK_BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build().create(DeepseekService.class);;

                    }
                }
            }
            return instance;
        }
    }
}
