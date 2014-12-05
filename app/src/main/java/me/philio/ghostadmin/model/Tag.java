package me.philio.ghostadmin.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.UUID;

/**
 * Tag model
 *
 * Created by phil on 03/12/2014.
 */
@Table(name = "tags", id = BaseColumns._ID)
public class Tag extends Model {

    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE,
            notNull = true)
    @SerializedName("id")
    public int id;

    @Column(name = "uuid")
    @SerializedName("uuid")
    public String uuid;

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

}