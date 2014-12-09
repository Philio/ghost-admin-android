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

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Post model
 *
 * Created by phil on 26/11/2014.
 */
@Table(name = "posts", id = BaseColumns._ID)
public class Post extends Model {

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

    @Column(name = "blog_id")
    public Blog blog;

    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE,
            notNull = true)
    @SerializedName("id")
    public int id;

    @Column(name = "uuid")
    @SerializedName("uuid")
    public UUID uuid;

    @Column(name = "title")
    @SerializedName("title")
    public String title;

    @Column(name = "slug")
    @SerializedName("slug")
    public String slug;

    @Column(name = "markdown")
    @SerializedName("markdown")
    public String markdown;

    @Column(name = "html")
    @SerializedName("html")
    public String html;

    @Column(name = "image")
    @SerializedName("image")
    public String image;

    @Column(name = "featured")
    @SerializedName("featured")
    public boolean featured;

    @Column(name = "page")
    @SerializedName("page")
    public boolean page;

    @Column(name = "status")
    @SerializedName("status")
    public Status status;

    @Column(name = "language")
    @SerializedName("language")
    public String language;

    @Column(name = "meta_title")
    @SerializedName("meta_title")
    public String metaTitle;

    @Column(name = "meta_description")
    @SerializedName("meta_description")
    public String metaDescription;

    @Column(name = "created_at")
    @SerializedName("created_at")
    public Date createdAt;

    @Column(name = "created_by")
    @SerializedName("created_by")
    public int createdBy;

    @Column(name = "updated_at")
    @SerializedName("updated_at")
    public Date updatedAt;

    @Column(name = "updated_by")
    @SerializedName("updated_by")
    public int updatedBy;

    @Column(name = "published_at")
    @SerializedName("published_at")
    public Date publishedAt;

    @Column(name = "published_by")
    @SerializedName("published_by")
    public int publishedBy;

    @Column(name = "author")
    @SerializedName("author")
    public int author;

    @SerializedName("tags")
    public List<Tag> tags;

    // TODO fields - what is this?

    /**
     * A flag to indicate that the record has local updates
     */
    @Column(name = "updated_locally")
    public boolean updatedLocally;

    /**
     * A flag to indicate that the record was updated locally and remotely and is conflicted
     */
    @Column(name = "remote_conflicted")
    public boolean remoteConflicted;

    /**
     * A flag to indicate that the record was updated locally and deleted remotely
     */
    @Column(name = "remote_deleted")
    public boolean remoteDeleted;

}