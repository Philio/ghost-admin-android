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
package me.philio.ghost.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response meta data
 *
 * Created by phil on 26/11/2014.
 */
public class Meta {

    @SerializedName("pagination")
    public Pagination pagination;

    /**
     * Pagination info
     */
    public class Pagination {

        @SerializedName("page")
        public int page;

        @SerializedName("limit")
        public int limit;

        @SerializedName("pages")
        public int pages;

        @SerializedName("total")
        public int total;

        @SerializedName("next")
        public int next;

        @SerializedName("prev")
        public int prev;

    }

}