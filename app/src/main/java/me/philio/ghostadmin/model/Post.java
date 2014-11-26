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
package me.philio.ghostadmin.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.UUID;

/**
 * A post
 *
 * Created by phil on 26/11/2014.
 */
public class Post {

    /**
     * Post status
     */
    public enum Status {

        @SerializedName("all")
        ALL,

        @SerializedName("published")
        PUBLISHED,

        @SerializedName("draft")
        DRAFT;

        /**
         * When used in requests this ensures that the enum is converted to a lowercase string
         *
         * @return string representation of the enum
         */
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

    }

    public void blah() {
    }

    @SerializedName("id")
    public int id;

    @SerializedName("uuid")
    public UUID uuid;

    @SerializedName("title")
    public String title;

    @SerializedName("slug")
    public String slug;

    @SerializedName("markdown")
    public String markdown;

    @SerializedName("html")
    public String html;

    @SerializedName("image")
    public String image;

    @SerializedName("featured")
    public boolean featured;

    @SerializedName("page")
    public boolean page;

    @SerializedName("status")
    public Status status;

    @SerializedName("language")
    public String language;

    @SerializedName("meta_title")
    public String metaTitle;

    @SerializedName("meta_description")
    public String metaDescription;

    @SerializedName("created_at")
    public Date createdAt;

    @SerializedName("created_by")
    public int createdBy;

    @SerializedName("updated_at")
    public Date updatedAt;

    @SerializedName("updated_by")
    public int updatedBy;

    @SerializedName("published_at")
    public Date publishedAt;

    @SerializedName("published_by")
    public int publishedBy;

    @SerializedName("tags")
    public int[] tags;

    @SerializedName("author")
    public int author;

}