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

import me.philio.ghostadmin.model.TagsContainer;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Ghost API tags endpoint
 *
 * Created by phil on 03/12/2014.
 */
public interface Tags {

    /**
     * Get all tags
     *
     * @param page     Page number
     * @param callback Response callback
     */
    @GET("/tags")
    public void getTags(
            @Query("page") int page,
            Callback<TagsContainer> callback);

    /**
     * Get all tags
     *
     * @param page Page number
     * @return A list of tags
     */
    @GET("/tags")
    public TagsContainer blockingGetTags(
            @Query("page") int page);

}
