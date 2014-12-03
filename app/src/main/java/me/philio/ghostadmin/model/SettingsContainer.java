package me.philio.ghostadmin.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import me.philio.ghostadmin.io.endpoint.Settings;

/**
 * A container for response of a {@link Settings} request
 *
 * Created by phil on 03/12/2014.
 */
public class SettingsContainer {

    @SerializedName("settings")
    public List<Setting> settings;

    @SerializedName("meta")
    public Meta meta;

}