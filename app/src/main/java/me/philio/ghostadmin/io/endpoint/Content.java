package me.philio.ghostadmin.io.endpoint;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Streaming;

/**
 * An endpoint to grab binary content
 *
 * Created by phil on 10/12/2014.
 */
public interface Content {

    /**
     * Get content from the server
     *
     * @param path Path to the content
     * @return The retrofit response
     */
    @GET("/{path}")
    @Streaming
    public Response getContent(
            @Path(value = "path", encode = false) String path);

}
