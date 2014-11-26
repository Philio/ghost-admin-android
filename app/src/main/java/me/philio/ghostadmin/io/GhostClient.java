/*
 * Copyright 2014 Phil Bayfield
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.philio.ghostadmin.io;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;

import me.philio.ghostadmin.io.endpoint.Authentication;
import me.philio.ghostadmin.io.endpoint.Discovery;
import me.philio.ghostadmin.io.endpoint.Posts;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Request;
import retrofit.client.UrlConnectionClient;
import retrofit.converter.GsonConverter;

/**
 * Ghost API client
 *
 * Created by phil on 24/11/2014.
 */
public class GhostClient {

    /**
     * The path of the API from the ghost web root
     */
    private static final String BASE_PATH = "/ghost/api/v0.1";

    /**
     * The URL of the blog
     */
    private String mBlogUrl;

    /**
     * Access token of authenticated user
     */
    private String mAccessToken;

    /**
     * Empty constructor
     */
    public GhostClient() {

    }

    /**
     * Set the URL of the blog
     *
     * @param blogUrl URL of the blog
     */
    public GhostClient(String blogUrl) {
        // Strip trailing slash if present
        if (blogUrl.endsWith("/")) {
            mBlogUrl = blogUrl.substring(0, blogUrl.length() - 1);
        } else {
            mBlogUrl = blogUrl;
        }
    }

    /**
     * Set the URL of the blog and the access token
     *
     * @param blogUrl     URL of the blog
     * @param accessToken Access token of an authenticated user
     */
    public GhostClient(String blogUrl, String accessToken) {
        this(blogUrl);
        mAccessToken = accessToken;
    }

    /**
     * Get the blog URL
     *
     * @return The current blog URL
     */
    public String getBlogUrl() {
        return mBlogUrl;
    }

    /**
     * Set a blog URL
     *
     * @param blogUrl URL of the blog
     */
    public void setBlogUrl(String blogUrl) {
        mBlogUrl = blogUrl;
    }

    /**
     * Get the access token
     *
     * @return The current access token
     */
    public String getAccessToken() {
        return mAccessToken;
    }

    /**
     * Set the access token
     *
     * @param accessToken Access token of an authenticated user
     */
    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    /**
     * Create REST client for discovering a valid blog URL
     *
     * @return discovery client
     */
    public Discovery createDiscovery() {
        // Modify the connection to ignore redirects as a correct blog URL should only return a 401
        UrlConnectionClient client = new UrlConnectionClient() {
            @Override
            protected HttpURLConnection openConnection(Request request) throws IOException {
                HttpURLConnection connection = super.openConnection(request);
                connection.setInstanceFollowRedirects(false);
                return connection;
            }
        };

        // Build adapter and create REST client
        return new RestAdapter.Builder()
                .setClient(client)
                .setEndpoint(mBlogUrl + BASE_PATH)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build()
                .create(Discovery.class);
    }

    /**
     * Create REST implementation for authentication
     *
     * @return authentication client
     */
    public Authentication createAuthentication() {
        return new RestAdapter.Builder()
                .setEndpoint(mBlogUrl + BASE_PATH + "/authentication")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build()
                .create(Authentication.class);
    }

    /**
     * Create REST implementation for posts
     *
     * @return posts client
     */
    public Posts createPosts() {
        return create(Posts.class);
    }

    /**
     * Create REST implementation for given endpoint/path
     *
     * @param endpoint A retrofit endpoint
     * @param <T>
     * @return
     */
    private <T> T create(Class<T> endpoint) {
        RestAdapter builder = new RestAdapter.Builder()
                .setEndpoint(mBlogUrl + BASE_PATH)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if (mAccessToken != null) {
                            request.addHeader("Authorization", "Bearer " + mAccessToken);
                        }
                    }
                })
                .setConverter(
                        new GsonConverter(new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                .create()))
                .build();
        return builder.create(endpoint);
    }

}
