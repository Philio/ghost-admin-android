package me.philio.ghostadmin.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.io.IOException;
import java.net.HttpURLConnection;

import me.philio.ghostadmin.account.AccountConstants;
import me.philio.ghostadmin.io.GhostClient;
import me.philio.ghostadmin.io.endpoint.Authentication;
import me.philio.ghostadmin.io.endpoint.Posts;
import me.philio.ghostadmin.io.endpoint.Settings;
import me.philio.ghostadmin.io.endpoint.Tags;
import me.philio.ghostadmin.io.endpoint.Users;
import me.philio.ghostadmin.model.Post;
import me.philio.ghostadmin.model.PostTag;
import me.philio.ghostadmin.model.PostsContainer;
import me.philio.ghostadmin.model.Setting;
import me.philio.ghostadmin.model.SettingsContainer;
import me.philio.ghostadmin.model.Tag;
import me.philio.ghostadmin.model.TagsContainer;
import me.philio.ghostadmin.model.Token;
import me.philio.ghostadmin.model.User;
import me.philio.ghostadmin.model.UsersContainer;
import retrofit.RetrofitError;

import static me.philio.ghostadmin.account.AccountConstants.KEY_ACCESS_TOKEN_EXPIRES;
import static me.philio.ghostadmin.account.AccountConstants.TOKEN_TYPE_ACCESS;
import static me.philio.ghostadmin.account.AccountConstants.TOKEN_TYPE_REFRESH;
import static me.philio.ghostadmin.io.ApiConstants.CLIENT_ID;
import static me.philio.ghostadmin.io.ApiConstants.GRANT_TYPE_PASSWORD;
import static me.philio.ghostadmin.io.ApiConstants.GRANT_TYPE_REFRESH_TOKEN;
import static me.philio.ghostadmin.model.Post.Status;

/**
 * Sync adapter
 *
 * Created by phil on 04/12/2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    /**
     * Logging tag
     */
    private static final String TAG = SyncAdapter.class.getName();

    /**
     * Account manager
     */
    private AccountManager mAccountManager;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Sync started for " + account.name);

        try {
            // Refresh the access token
            refreshAccessToken(account);

            // Sync remote
            syncRemote(account);
        } catch (AuthenticatorException | OperationCanceledException | IOException | RetrofitError e) {
            syncResult.stats.numIoExceptions++;
            return;
        }

        Log.d(TAG, "Sync finished for " + account.name);
    }

    /**
     * Access tokens only last 60 minutes so we need to manage this and refresh it frequently. If
     * token has less than 30 minutes remaining it will be refreshed and as a last resort we can
     * use the email/password combination that was saved on login to re-authenticate from scratch.
     * <p/>
     * TODO Review later
     */
    private void refreshAccessToken(Account account) throws AuthenticatorException,
            OperationCanceledException, IOException, RetrofitError {
        // Check expiry first
        Long expiry = Long.parseLong(mAccountManager.getUserData(account,
                AccountConstants.KEY_ACCESS_TOKEN_EXPIRES));
        if (System.currentTimeMillis() + (30 * 60 * 1000) < expiry) {
            Log.d(TAG, "Access token has more than 30 minutes remaining, won't refresh");
            return;
        }

        // Get blog url and refresh token
        String blogUrl = mAccountManager.getUserData(account, AccountConstants.KEY_BLOG_URL);
        String refreshToken = mAccountManager.blockingGetAuthToken(account, TOKEN_TYPE_REFRESH,
                false);

        // Get authentication client
        GhostClient client = new GhostClient(blogUrl);
        Authentication authentication = client.createAuthentication();
        try {
            // Request a new access token
            Token token = authentication
                    .blockingGetAccessToken(GRANT_TYPE_REFRESH_TOKEN, CLIENT_ID, refreshToken);

            // Save new access token
            mAccountManager.setAuthToken(account, TOKEN_TYPE_ACCESS, token.accessToken);
            mAccountManager.setUserData(account, KEY_ACCESS_TOKEN_EXPIRES,
                    Long.toString(System.currentTimeMillis() + (token.expires * 1000)));
        } catch (RetrofitError e) {
            // Check for a 403 as we can try and re-authenticate with an email/password
            if (e.getResponse() != null &&
                    e.getResponse().getStatus() == HttpURLConnection.HTTP_FORBIDDEN) {
                String email = mAccountManager.getUserData(account, AccountConstants.KEY_EMAIL);
                String password = mAccountManager.getPassword(account);
                Token token = authentication.blockingGetAccessToken(GRANT_TYPE_PASSWORD,
                        CLIENT_ID, email, password);

                // Save new tokens
                mAccountManager.setAuthToken(account, TOKEN_TYPE_ACCESS, token.accessToken);
                mAccountManager.setAuthToken(account, TOKEN_TYPE_REFRESH, token.refreshToken);
                mAccountManager.setUserData(account, KEY_ACCESS_TOKEN_EXPIRES,
                        Long.toString(System.currentTimeMillis() + (token.expires * 1000)));
            }
        }
    }

    /**
     * Get blog data from the server, a pretty straight forward fetch all and replace approach for
     * now, but this is far from optimal and ideally needs to pull changes only
     *
     * TODO Look into syncing just changes, remove duplicate code, etc
     */
    private void syncRemote(Account account) throws AuthenticatorException,
            OperationCanceledException, IOException, RetrofitError {
        // Get blog url and access token
        String blogUrl = mAccountManager.getUserData(account, AccountConstants.KEY_BLOG_URL);
        String accessToken = mAccountManager.blockingGetAuthToken(account, TOKEN_TYPE_ACCESS,
                false);
        GhostClient client = new GhostClient(blogUrl, accessToken);

        // Sync users
        Users users = client.createUsers();
        int page = 1;
        int totalPages = 1;
        while (page <= totalPages) {
            UsersContainer usersContainer = users.blockingGetUsers(page);
            for (User user : usersContainer.users) {
                user.save();
            }
            totalPages = usersContainer.meta.pagination.pages;
            page++;
        }

        // Sync settings (no pagination)
        Settings settings = client.createSettings();
        SettingsContainer settingsContainer = settings.blockingGetSettings(Setting.Type.BLOG,
                page);
        for (Setting setting : settingsContainer.settings) {
            setting.save();
        }

        // Sync tags
        Tags tags = client.createTags();
        page = 1;
        totalPages = 1;
        while (page <= totalPages) {
            TagsContainer tagsContainer = tags.blockingGetTags(page);
            for (Tag tag : tagsContainer.tags) {
                tag.save();
            }
            totalPages = tagsContainer.meta.pagination.pages;
            page++;
        }

        // Sync posts
        Posts posts = client.createPosts();
        page = 1;
        totalPages = 1;
        while (page <= totalPages) {
            PostsContainer postsContainer = posts.blockingGetPosts(page, null, Status.ALL, false);
            for (Post post : postsContainer.posts) {
                // Save the post
                post.save();

                // Delete existing tags
                new Delete().from(PostTag.class).where("post_id = ?", post.getId()).execute();

                // Link current tags to the post
                for (Tag tag : post.tags) {
                    Tag dbTag = new Select()
                            .from(Tag.class)
                            .where("remote_id = ?", tag.id)
                            .executeSingle();
                    if (dbTag != null) {
                        PostTag postTag = new PostTag();
                        postTag.post = post;
                        postTag.tag = dbTag;
                        postTag.save();
                    }
                }
            }
            totalPages = postsContainer.meta.pagination.pages;
            page++;
        }
        while (page <= totalPages) {
            PostsContainer postsContainer = posts.blockingGetPosts(page, null, Status.ALL, true);
            for (Post post : postsContainer.posts) {
                // Save the post
                post.save();

                // Delete existing tags
                new Delete().from(PostTag.class).where("post_id = ?", post.getId()).execute();

                // Link current tags to the post
                for (Tag tag : post.tags) {
                    Tag dbTag = new Select()
                            .from(Tag.class)
                            .where("remote_id = ?", tag.id)
                            .executeSingle();
                    if (dbTag != null) {
                        PostTag postTag = new PostTag();
                        postTag.post = post;
                        postTag.tag = dbTag;
                        postTag.save();
                    }
                }
            }
            totalPages = postsContainer.meta.pagination.pages;
            page++;
        }
    }

}
