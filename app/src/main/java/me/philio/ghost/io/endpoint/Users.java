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

import me.philio.ghost.model.UsersContainer;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Ghost API users endpoint
 * <p/>
 * Created by phil on 03/12/2014.
 */
public interface Users {

    /**
     * Get all users
     *
     * @param page     Page number
     * @param callback Response callback
     */
    @GET("/users")
    public void getUsers(
            @Query("page") int page,
            Callback<UsersContainer> callback);

    /**
     * Get all users, blocks and executes on same thread
     *
     * @param page Page number
     * @return A list of users
     */
    @GET("/users")
    public UsersContainer blockingGetUsers(
            @Query("page") int page);

    /**
     * Get user with id
     *
     * @param id       User id
     * @param callback Response callback
     */
    @GET("/users/{id}")
    public void getUser(
            @Path("id") int id,
            Callback<UsersContainer> callback);

    /**
     * Get current user
     *
     * @param callback Response callback
     */
    @GET("/users/me")
    public void getMe(
            Callback<UsersContainer> callback);

    /**
     * Get current user, blocks and executes on same thread
     */
    @GET("/users/me")
    public UsersContainer blockingGetMe();

    /**
     * Get user with slug
     *
     * @param slug     User slug
     * @param callback Response callback
     */
    @GET("/users/slug/{slug}")
    public void getUserWithSlug(
            @Path("slug") String slug,
            Callback<UsersContainer> callback);

    /**
     * Get user with email
     *
     * @param email    User email
     * @param callback Response callback
     */
    @GET("/users/email/{email}")
    public void getUserWithEmail(
            @Path("email") String email,
            Callback<UsersContainer> callback);

    /**
     * Update user
     *
     * @param usersContainer User to update
     * @param callback       Response callback
     */
    @PUT("/users/{id}")
    public void updateUser(
            @Body UsersContainer usersContainer,
            Callback<UsersContainer> callback);

}