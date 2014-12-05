package me.philio.ghostadmin.io.endpoint;

import me.philio.ghostadmin.model.UsersContainer;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Ghost API users endpoint
 *
 * Created by phil on 03/12/2014.
 */
public interface Users {

    /**
     * Get all users
     *
     * @param page     Page number
     * @param callback Response callback
     */
    @GET("/users")
    public void getUsers(
            @Query("page") int page,
            Callback<UsersContainer> callback);

    /**
     * Get all users, blocks and executes on same thread
     *
     * @param page Page number
     * @return A list of users
     */
    @GET("/users")
    public UsersContainer blockingGetUsers(
            @Query("page") int page);

    /**
     * Get user with id
     *
     * @param id       User id
     * @param callback Response callback
     */
    @GET("/users/{id}")
    public void getUser(
            @Path("id") int id,
            Callback<UsersContainer> callback);

    /**
     * Get current user
     *
     * @param callback Response callback
     */
    @GET("/users/me")
    public void getMe(
            Callback<UsersContainer> callback);

    /**
     * Get user with slug
     *
     * @param slug     User slug
     * @param callback Response callback
     */
    @GET("/users/slug/{slug}")
    public void getUserWithSlug(
            @Path("slug") String slug,
            Callback<UsersContainer> callback);

    /**
     * Get user with email
     *
     * @param email    User email
     * @param callback Response callback
     */
    @GET("/users/email/{email}")
    public void getUserWithEmail(
            @Path("email") String email,
            Callback<UsersContainer> callback);

    /**
     * Update user
     *
     * @param usersContainer User to update
     * @param callback       Response callback
     */
    @PUT("/users/{id}")
    public void updateUser(
            @Body UsersContainer usersContainer,
            Callback<UsersContainer> callback);

}