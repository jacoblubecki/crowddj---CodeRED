package com.lubecki.crowddj.twitter.webapi;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lubecki.crowddj.Secrets;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by tbrown on 1/17/15.
 */
public class TwitterAPI {
    private static final String SERVER_ADDRESS = "https://api.twitter.com/1.1";
    private static TwitterAPI instance;
    private final RestAdapter restAdapter;
    private RequestInterceptor reqInterceptor;
    private TwitterService service;

    public Gson gson;

    public static TwitterAPI getInstance() {
        if (instance == null) {
            instance = new TwitterAPI();
        }
        return instance;
    }

    public TwitterAPI() {
        gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        reqInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade requestFacade) {
                requestFacade.addHeader("Authorization", "Bearer " + Secrets.AUTH_TOKEN);
                //requestFacade.addHeader("Authorization", "Basic Tk9yOXliM3V1TWcyakdFSlFuWTVzSjF0MDpuVGNqWVhMWFJ1UkwyY0U3ejlXMmRMWUxrakhaRlE3NFl0TVBDMnNIWUF3NXhSaHo2Rg==");
            }
        };

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(SERVER_ADDRESS)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(reqInterceptor)
                .build();

        service = restAdapter.create(TwitterService.class);
    }

    public TwitterService getService() {
        return service;
    }
}