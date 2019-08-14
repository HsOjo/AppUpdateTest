package com.hsojo.appupdatetest.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public class DownloadUtil {
    public static InputStream download(String url) {
        OkHttpClient ohc = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://github.com").callFactory(ohc).build();
        Call<ResponseBody> call_download = retrofit.create(Download.class).download(url);
        try {
            return Objects.requireNonNull(call_download.execute().body()).byteStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    interface Download {
        @GET
        @Streaming
        Call<ResponseBody> download(@Url String url);
    }
}
