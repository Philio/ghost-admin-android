package me.philio.ghostadmin.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import me.philio.ghostadmin.io.endpoint.Tags;

/**
 * A container for response of a {@link Tags} request
 *
 * Created by phil on 03/12/2014.
 */
public class TagsContainer {

    @SerializedName("tags")
    public List<Tag> tags;

    @SerializedName("meta")
    public Meta meta;

}