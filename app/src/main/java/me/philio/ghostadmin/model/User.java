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
import java.util.UUID;

/**
 * User model
 *
 * Created by phil on 03/12/2014.
 */
@Table(name = "users", id = BaseColumns._ID)
public class User extends Model {

    @Column(name = "blog_id")
    public Blog blog;

    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE,
            notNull = true)
    @SerializedName("id")
    public int id;

    @Column(name = "uuid")
    @SerializedName("uuid")
    public UUID uuid;

    @Column(name = "name")
    @SerializedName("name")
    public String name;

    @Column(name = "slug")
    @SerializedName("slug")
    public String slug;

    @Column(name = "email")
    @SerializedName("email")
    public String email;

    @Column(name = "image")
    @SerializedName("image")
    public String image;

    @Column(name = "cover")
    @SerializedName("cover")
    public String cover;

    @Column(name = "bio")
    @SerializedName("bio")
    public String bio;

    @Column(name = "website")
    @SerializedName("website")
    public String website;

    @Column(name = "location")
    @SerializedName("location")
    public String location;

    @Column(name = "accessibility")
    @SerializedName("accessibility")
    public String accessibility;

    @Column(name = "status")
    @SerializedName("status")
    public String status;

    @Column(name = "language")
    @SerializedName("language")
    public String language;

    @Column(name = "meta_title")
    @SerializedName("meta_title")
    public String metaTitle;

    @Column(name = "meta_description")
    @SerializedName("meta_description")
    public String metaDescription;

    @Column(name = "last_login")
    @SerializedName("last_login")
    public Date lastLogin;

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

    // TODO roles, needed?

    /**
     * A flag to indicate that the record has local updates
     */
    @Column(name = "updated_locally")
    public boolean updatedLocally;

    /**
     * A flag to indicate that the record was updated locally and remotely and is conflicted
     */
    @Column(name = "remote_conflict")
    public boolean remoteConflict;

}