package com.hsojo.appupdatetest.service;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public final class GitHubService {
    public static final String API_URL = "https://api.github.com";

    public static class Asset {
        public final String name;
        public final int size;
        public final String browser_download_url;

        public Asset(String name, int size, String browser_download_url) {
            this.name = name;
            this.size = size;
            this.browser_download_url = browser_download_url;
        }
    }

    public static class Release {
        public final String tag_name;
        public final String name;
        public final String body;
        public final List<Asset> assets;

        public Release(String tag_name, String name, String body, List<Asset> assets) {
            this.tag_name = tag_name;
            this.name = name;
            this.body = body;
            this.assets = assets;
        }
    }

    public interface GitHub {
        @GET("/repos/{owner}/{repo}/releases")
        Call<List<Release>> releases(
                @Path("owner") String owner,
                @Path("repo") String repo
        );
    }

    public static GitHub generate() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GitHubService.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(GitHub.class);
    }
}
