package me.philio.ghostadmin.io.endpoint;

import me.philio.ghostadmin.model.TagsContainer;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Ghost API tags endpoint
 *
 * Created by phil on 03/12/2014.
 */
public interface Tags {

    /**
     * Get all tags
     *
     * @param page     Page number
     * @param callback Response callback
     */
    @GET("/tags")
    public void getTags(
            @Query("page") int page,
            Callback<TagsContainer> callback);

    /**
     * Get all tags
     *
     * @param page Page number
     * @return A list of tags
     */
    @GET("/tags")
    public TagsContainer blockingGetTags(
            @Query("page") int page);

}
