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

package me.philio.ghost.io.endpoint;

import me.philio.ghost.model.Post;
import me.philio.ghost.model.PostsContainer;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Ghost API posts endpoint
 * <p/>
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
    @GET("/posts?include=tags")
    public void getPosts(
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("status") Post.Status status,
            @Query("staticPages") Boolean staticPages,
            Callback<PostsContainer> callback);

    /**
     * Get all posts, blocks and executes on same thread
     *
     * @param page        Page number to get
     * @param limit       Number of records per page
     * @param status      Page status (all, published, draft)
     * @param staticPages Fetch static pages
     * @return A list of posts
     */
    @GET("/posts?include=tags")
    public PostsContainer blockingGetPosts(
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("status") Post.Status status,
            @Query("staticPages") Boolean staticPages);

    /**
     * Get a post with id
     *
     * @param id       The id of the post
     * @param callback Response callback
     */
    @GET("/posts/{id}?include=tags")
    public void getPost(
            @Path("id") int id,
            Callback<PostsContainer> callback);

    /**
     * Get a post with slug
     *
     * @param slug     The post slug
     * @param callback Response callback
     */
    @GET("/posts/slug/{slug}?include=tags")
    public void getPostWithSlug(
            @Path("slug") String slug,
            Callback<PostsContainer> callback);

    /**
     * Add a new post
     *
     * @param postsContainer The post
     * @param callback       Response callback
     */
    @POST("/posts?include=tags")
    public void addPost(
            @Body PostsContainer postsContainer,
            Callback<PostsContainer> callback);

    /**
     * Update an existing post
     *
     * @param id             The id of the post
     * @param postsContainer The post
     * @param callback       Response callback
     */
    @PUT("/posts/{id}?include=tags")
    public void updatePost(
            @Path("id") int id,
            @Body PostsContainer postsContainer,
            Callback<PostsContainer> callback);

    /**
     * Delete a post
     *
     * @param id       The id of the post
     * @param callback Response callback
     */
    @DELETE("/posts/{id}")
    public void deletePost(
            @Path("id") int id,
            Callback<PostsContainer> callback);

}
