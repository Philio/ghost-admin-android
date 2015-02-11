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

import me.philio.ghost.model.Token;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Ghost API authentication endpoint
 * <p/>
 * Created by phil on 24/11/2014.
 */
public interface Authentication {

    /**
     * Request an access token with login credentials
     *
     * @param grantType Should be "password"
     * @param clientId  Should be "ghost-admin" otherwise blog requires manual configuration
     * @param email     The email address of the user
     * @param password  The password of the user
     * @param callback  Callback to handle the response
     */
    @FormUrlEncoded
    @POST("/token")
    public void getAccessToken(
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("username") String email,
            @Field("password") String password,
            Callback<Token> callback);

    /**
     * Request an access token with login credentials, blocks and executes on same thread
     *
     * @param grantType Should be "password"
     * @param clientId  Should be "ghost-admin" otherwise blog requires manual configuration
     * @param email     The email address of the user
     * @param password  The password of the user
     * @return A token
     */
    @FormUrlEncoded
    @POST("/token")
    public Token blockingGetAccessToken(
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("username") String email,
            @Field("password") String password);

    /**
     * Request an access token with a refresh token
     *
     * @param grantType    Should be "refresh_token"
     * @param clientId     Should be "ghost-admin" otherwise blog requires manual configuration
     * @param refreshToken The refresh token from a previous access token request
     * @param callback     Callback to handle the response
     */
    @FormUrlEncoded
    @POST("/token")
    public void getAccessToken(
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("refresh_token") String refreshToken,
            Callback<Token> callback);

    /**
     * Request an access token with a refresh token, blocks and executes on same thread
     *
     * @param grantType    Should be "refresh_token"
     * @param clientId     Should be "ghost-admin" otherwise blog requires manual configuration
     * @param refreshToken The refresh token from a previous access token request
     * @return A token
     */
    @FormUrlEncoded
    @POST("/token")
    public Token blockingGetAccessToken(
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("refresh_token") String refreshToken);

}
