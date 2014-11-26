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
package me.philio.ghostadmin.io.endpoint;

import me.philio.ghostadmin.model.Post;
import me.philio.ghostadmin.model.PostWrapper;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Query;

/**
 * Ghost API posts endpoint
 *
 * Created by phil on 25/11/2014.
 */
public interface Posts {

    /**
     * Get all posts
     *
     * @param page        Page number to get
     * @param limit       Number of records per page
     * @param status      Page status (all, published, draft)
     * @param staticPages Fetch static pages
     * @param callback    Response callback
     */
    @GET("/posts")
    public void getPosts(
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("status") Post.Status status,
            @Query("staticPages") Boolean staticPages,
            Callback<PostWrapper> callback);

    /**
     * Get a post by id
     *
     * @param id       The id of the post
     * @param callback Response callback
     */
    @GET("/posts/{id}")
    public void getPost(
            @Part("id") int id,
            Callback<PostWrapper> callback);

    /**
     * Get a post by slug
     *
     * @param slug     The post slug
     * @param callback Response callback
     */
    @GET("/posts/slug/{slug}")
    public void getPost(
            @Part("slug") String slug,
            Callback<PostWrapper> callback);

    /**
     * Add a new post
     *
     * @param post     The post
     * @param callback Response callback
     */
    @POST("/posts")
    public void addPost(
            @Body Post post,
            Callback<PostWrapper> callback);

    /**
     * Update an existing post
     *
     * @param id       The id of the post
     * @param post     The post
     * @param callback Response callback
     */
    @PUT("/posts/{id}")
    public void updatePost(
            @Part("id") int id,
            @Body Post post,
            Callback<PostWrapper> callback);

    /**
     * Delete a post
     *
     * @param id       The id of the post
     * @param callback Response callback
     */
    @DELETE("/posts/{id}")
    public void deletePost(
            @Part("id") int id,
            Callback<PostWrapper> callback);

}
