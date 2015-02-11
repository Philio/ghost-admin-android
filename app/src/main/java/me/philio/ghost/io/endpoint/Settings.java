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

import me.philio.ghost.model.Setting;
import me.philio.ghost.model.SettingsContainer;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Ghost API settings endpoint
 * <p/>
 * Created by phil on 03/12/2014.
 */
public interface Settings {

    /**
     * Get all settings
     *
     * @param type     Type of settings to get
     * @param page     Page number
     * @param callback Response callback
     */
    @GET("/settings")
    public void getSettings(
            @Query("type") Setting.Type type,
            @Query("page") int page,
            Callback<SettingsContainer> callback);

    /**
     * Get all settings, blocks and executes on same thread
     *
     * @param type Type of settings to get
     * @param page Page number
     * @return A list of settings
     */
    @GET("/settings")
    public SettingsContainer blockingGetSettings(
            @Query("type") Setting.Type type,
            @Query("page") int page);

    /**
     * Get a setting with key
     *
     * @param Key      The setting key
     * @param callback Response callback
     */
    @GET("/settings/{key}")
    public void getSetting(
            @Path("key") String Key,
            Callback<SettingsContainer> callback);

    /**
     * Update settings
     *
     * @param settings Settings to update
     * @param callback Response callback
     */
    @PUT("/settings")
    public void updateSettings(
            @Body SettingsContainer settings,
            Callback<SettingsContainer> callback);

}
