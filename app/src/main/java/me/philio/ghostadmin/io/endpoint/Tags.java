package me.philio.ghostadmin.io.endpoint;

import me.philio.ghostadmin.model.TagsContainer;
import retrofit.Callback;
import retrofit.http.GET;

/**
 * Ghost API tags endpoint
 *
 * Created by phil on 03/12/2014.
 */
public interface Tags {

    /**
     * Get all tags
     *
     * @param callback Response callback
     */
    @GET("/tags")
    public void getTags(
            Callback<TagsContainer> callback);

}
