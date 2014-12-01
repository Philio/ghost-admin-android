package me.philio.ghostadmin.model;

import com.google.gson.annotations.SerializedName;

/**
 * An error from the API
 *
 * Created by phil on 01/12/2014.
 */
public class Error {

    @SerializedName("message")
    public String message;

    @SerializedName("type")
    public String type;

}
