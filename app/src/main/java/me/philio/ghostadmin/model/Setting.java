package me.philio.ghostadmin.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.UUID;

/**
 * A setting, doesn't work with themes as value is more complex
 *
 * Created by phil on 03/12/2014.
 */
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

    @Column(name = "remote_id", unique = true)
    @SerializedName("id")
    public int id;

    @Column(name = "uuid")
    @SerializedName("uuid")
    public UUID uuid;

    @Column(name = "key")
    @SerializedName("key")
    public String key;

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

}