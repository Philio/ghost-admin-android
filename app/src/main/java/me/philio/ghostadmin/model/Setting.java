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
 * Setting model (doesn't work with themes as value is more complex)
 *
 * Created by phil on 03/12/2014.
 */
@Table(name = "settings", id = BaseColumns._ID)
public class Setting extends Model {

    /**
     * Setting type
     */
    public enum Type {

        @SerializedName("blog")
        BLOG,

        @SerializedName("app")
        APP,

        @SerializedName("theme")
        THEME;

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

    /**
     * Setting keys
     */
    public enum Key {

        @SerializedName("title")
        TITLE,

        @SerializedName("description")
        DESCRIPTION,

        @SerializedName("email")
        EMAIL,

        @SerializedName("logo")
        LOGO,

        @SerializedName("cover")
        COVER,

        @SerializedName("defaultLang")
        DEFAULT_LANG,

        @SerializedName("postsPerPage")
        POSTS_PER_PAGE,

        @SerializedName("forceI18n")
        FORCE_I18N,

        @SerializedName("permalinks")
        PERMALINKS,

        @SerializedName("ghost_head")
        GHOST_HEAD,

        @SerializedName("ghost_foot")
        GHOST_FOOT;

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

    @Column(name = "key")
    @SerializedName("key")
    public Key key;

    @Column(name = "value")
    @SerializedName("value")
    public String value;

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