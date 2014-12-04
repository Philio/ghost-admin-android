package me.philio.ghostadmin.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import me.philio.ghostadmin.io.endpoint.Users;

/**
 * A container for response of a {@link Users} request
 *
 * Created by phil on 03/12/2014.
 */
public class UsersContainer {

    @SerializedName("users")
    public List<User> users;

    @SerializedName("meta")
    public Meta meta;

}
