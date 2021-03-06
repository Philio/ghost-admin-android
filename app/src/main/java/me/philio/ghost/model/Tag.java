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

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.UUID;

/**
 * Tag model
 * <p/>
 * Created by phil on 03/12/2014.
 */
@Table(name = "tags", id = BaseColumns._ID)
public class Tag extends Model {

    @Column(name = "blog_id", notNull = true, uniqueGroups = "blog_tag",
            onUniqueConflicts = Column.ConflictAction.REPLACE)
    public Blog blog;

    @Column(name = "remote_id", uniqueGroups = "blog_tag",
            onUniqueConflicts = Column.ConflictAction.REPLACE)
    @SerializedName("id")
    public Integer id;

    @Column(name = "uuid")
    @SerializedName("uuid")
    public UUID uuid;

    @Column(name = "name")
    @SerializedName("name")
    public String name;

    @Column(name = "slug")
    @SerializedName("slug")
    public String slug;

    @Column(name = "description")
    @SerializedName("description")
    public String description;

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

    @Column(name = "image")
    @SerializedName("image")
    public String image;

    @Column(name = "hidden")
    @SerializedName("hidden")
    public boolean hidden;

    // TODO parent - is a parent tag? future feature?

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