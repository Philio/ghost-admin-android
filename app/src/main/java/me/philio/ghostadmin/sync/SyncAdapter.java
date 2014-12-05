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

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

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
            syncRemote(account, syncResult);
        } catch (AuthenticatorException | OperationCanceledException | IOException | RetrofitError e) {
            syncResult.stats.numIoExceptions++;
            return;
        }

        Log.d(TAG, "Sync finished for " + account.name);
        Log.d(TAG, "Inserts: " + syncResult.stats.numInserts);
        Log.d(TAG, "Updates: " + syncResult.stats.numUpdates);
        Log.d(TAG, "Deletes: " + syncResult.stats.numDeletes);
        Log.d(TAG, "Errors: " + syncResult.hasError());
    }

    /**
     * Access tokens only last 60 minutes so we need to manage this and refresh it frequently. If
     * token has less than 30 minutes remaining it will be refreshed and as a last resort we can
     * use the email/password combination that was saved on login to re-authenticate from scratch.
     *
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
     * now, but this is far from optimal and ideally needs to pull changes only but this
     * functionality doesn't yet look like it exists in the Ghost API.
     *
     * TODO Hopefully this can be optimised at a later date, depends on the API
     */
    private void syncRemote(Account account, SyncResult syncResult) throws AuthenticatorException,
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
                saveUser(user, syncResult);
            }
            totalPages = usersContainer.meta.pagination.pages;
            page++;
        }

        // Sync settings (no pagination)
        Settings settings = client.createSettings();
        SettingsContainer settingsContainer = settings.blockingGetSettings(Setting.Type.BLOG,
                page);
        for (Setting setting : settingsContainer.settings) {
            saveSetting(setting, syncResult);
        }

        // Sync tags
        Tags tags = client.createTags();
        page = 1;
        totalPages = 1;
        while (page <= totalPages) {
            TagsContainer tagsContainer = tags.blockingGetTags(page);
            for (Tag tag : tagsContainer.tags) {
                saveTag(tag, syncResult);
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
                savePost(post, syncResult);
            }
            totalPages = postsContainer.meta.pagination.pages;
            page++;
        }
        while (page <= totalPages) {
            PostsContainer postsContainer = posts.blockingGetPosts(page, null, Status.ALL, true);
            for (Post post : postsContainer.posts) {
                savePost(post, syncResult);
            }
            totalPages = postsContainer.meta.pagination.pages;
            page++;
        }
    }

    /**
     * Save a user record if necessary
     *
     * @param user
     * @param syncResult
     */
    private void saveUser(User user, SyncResult syncResult) {
        // Get the local record
        User dbUser = new Select().from(User.class).where("remote_id = ?", user.id).executeSingle();

        // Check to see if record needs saving
        if (dbUser != null) {
            // Check if user was actually updated
            if (user.updatedAt.compareTo(dbUser.updatedAt) <= 0) {
                Log.d(TAG, "User is unchanged");
                return;
            }
        }

        // Save the record
        user.save();
        if (dbUser == null) {
            syncResult.stats.numInserts++;
        } else {
            syncResult.stats.numUpdates++;
        }
    }

    /**
     * Save a setting record if necessary
     *
     * @param setting
     * @param syncResult
     */
    private void saveSetting(Setting setting, SyncResult syncResult) {
        // Get the local record
        Setting dbSetting = new Select().from(Setting.class).where("remote_id = ?", setting.id)
                .executeSingle();

        // Check to see if record needs saving
        if (dbSetting != null) {
            // Check if setting was actually updated
            if (setting.updatedAt.compareTo(dbSetting.updatedAt) <= 0) {
                Log.d(TAG, "Setting is unchanged");
                return;
            }
        }

        // Save the record
        setting.save();
        if (dbSetting == null) {
            syncResult.stats.numInserts++;
        } else {
            syncResult.stats.numUpdates++;
        }
    }

    /**
     * Save a tag record if necessary
     *
     * @param tag
     * @param syncResult
     */
    private void saveTag(Tag tag, SyncResult syncResult) {
        // Get the local record
        Tag dbTag = new Select().from(Tag.class).where("remote_id = ?", tag.id).executeSingle();

        // Check to see if record needs saving
        if (dbTag != null) {
            // Check if tag was actually updated
            if (tag.updatedAt.compareTo(dbTag.updatedAt) <= 0) {
                Log.d(TAG, "Tag is unchanged");
                return;
            }
        }

        // Save the record
        tag.save();
        if (dbTag == null) {
            syncResult.stats.numInserts++;
        } else {
            syncResult.stats.numUpdates++;
        }
    }

    /**
     * Save a post record if necessary
     *
     * @param post
     * @param syncResult
     */
    private void savePost(Post post, SyncResult syncResult) {
        // Get the local record
        Post dbPost = new Select().from(Post.class).where("remote_id = ?", post.id).executeSingle();

        // Check to see if record needs saving
        if (dbPost != null) {
            // If local updates exist, skip but check for conflicts
            if (dbPost.updatedLocally) {
                Log.d(TAG, "Won't update locally changed records");
                if (dbPost.updatedAt != post.updatedAt) {
                    Log.d(TAG, "Record is conflicted, changed locally and remotely");
                    dbPost.remoteConflict = true;
                    dbPost.save();
                }
                return;
            }

            // Check if post was actually updated
            if (post.updatedAt.compareTo(dbPost.updatedAt) <= 0) {
                Log.d(TAG, "Post is unchanged");
                return;
            }
        }

        // Save the record
        ActiveAndroid.beginTransaction();
        try {
            // Save the post
            post.save();
            if (dbPost == null) {
                syncResult.stats.numInserts++;
            } else {
                syncResult.stats.numUpdates++;
            }

            // Delete any existing post/tag associations
            List<PostTag> dbPostTags = new Select().from(PostTag.class)
                    .where("post_id = ?", post.getId()).execute();
            for (PostTag dbPostTag : dbPostTags) {
                dbPostTag.delete();
                syncResult.stats.numDeletes++;
            }

            // Link current tags to the post
            for (Tag tag : post.tags) {
                // Get the tag record from the database as the object created from JSON doesn't
                // save correctly unless saved first itself
                Tag dbTag = new Select().from(Tag.class).where("remote_id = ?", tag.id)
                        .executeSingle();
                if (dbTag != null) {
                    PostTag postTag = new PostTag();
                    postTag.post = post;
                    postTag.tag = dbTag;
                    postTag.save();
                    syncResult.stats.numInserts++;
                }
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

}
