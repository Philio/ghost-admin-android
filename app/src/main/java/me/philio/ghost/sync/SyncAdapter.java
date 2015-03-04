/*
 * Copyright 2014 Phil Bayfield
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.philio.ghost.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.philio.ghost.account.AccountConstants;
import me.philio.ghost.io.GhostClient;
import me.philio.ghost.io.endpoint.Authentication;
import me.philio.ghost.io.endpoint.Posts;
import me.philio.ghost.io.endpoint.Settings;
import me.philio.ghost.io.endpoint.Tags;
import me.philio.ghost.io.endpoint.Users;
import me.philio.ghost.model.Blog;
import me.philio.ghost.model.Post;
import me.philio.ghost.model.PostConflict;
import me.philio.ghost.model.PostDraft;
import me.philio.ghost.model.PostTag;
import me.philio.ghost.model.PostsContainer;
import me.philio.ghost.model.Setting;
import me.philio.ghost.model.SettingsContainer;
import me.philio.ghost.model.Tag;
import me.philio.ghost.model.TagsContainer;
import me.philio.ghost.model.Token;
import me.philio.ghost.model.User;
import me.philio.ghost.model.UsersContainer;
import me.philio.ghost.util.DatabaseUtils;
import me.philio.ghost.util.ImageUtils;
import retrofit.RetrofitError;

import static me.philio.ghost.account.AccountConstants.KEY_ACCESS_TOKEN_EXPIRES;
import static me.philio.ghost.account.AccountConstants.TOKEN_TYPE_ACCESS;
import static me.philio.ghost.account.AccountConstants.TOKEN_TYPE_REFRESH;
import static me.philio.ghost.io.ApiConstants.CLIENT_ID;
import static me.philio.ghost.io.ApiConstants.GRANT_TYPE_PASSWORD;
import static me.philio.ghost.io.ApiConstants.GRANT_TYPE_REFRESH_TOKEN;
import static me.philio.ghost.model.Post.Status;

/**
 * Sync adapter
 * <p/>
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

    /**
     * Ghost REST client
     */
    private GhostClient mClient;

    /**
     * List of images to download
     */
    private List<Pair<String, Uri>> mImageQueue = new ArrayList<>();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Sync started for " + account.name);

        Intent intent = new Intent(SyncConstants.ACTION_SYNC_STARTED);
        intent.putExtra(SyncConstants.EXTRA_ACCOUNT, account);
        getContext().sendBroadcast(intent);

        try {
            // Get user data
            String blogUrl = mAccountManager.getUserData(account, AccountConstants.KEY_BLOG_URL);
            String email = mAccountManager.getUserData(account, AccountConstants.KEY_EMAIL);

            // Setup client
            mClient = new GhostClient(blogUrl);

            // Refresh the access token
            refreshAccessToken(account);

            // Set access token
            String accessToken = mAccountManager.blockingGetAuthToken(account, TOKEN_TYPE_ACCESS,
                    false);
            mClient.setAccessToken(accessToken);

            // Load the blog record
            Blog blog = getBlog(blogUrl, email);

            // Sync local changes
            syncLocal(blog, syncResult);

            // Sync remote changes
            syncRemote(blog, syncResult);
        } catch (AuthenticatorException | OperationCanceledException | IOException | RetrofitError |
                NoSuchAlgorithmException e) {
            Log.e(TAG, "Sync error: " + e.getMessage());
            syncResult.stats.numIoExceptions++;
        }

        Log.d(TAG, "Sync finished for " + account.name);
        Log.d(TAG, "Inserts: " + syncResult.stats.numInserts);
        Log.d(TAG, "Updates: " + syncResult.stats.numUpdates);
        Log.d(TAG, "Deletes: " + syncResult.stats.numDeletes);
        Log.d(TAG, "Errors: " + syncResult.hasError());

        intent.setAction(SyncConstants.ACTION_SYNC_FINISHED);
        intent.putExtra(SyncConstants.EXTRA_RESULT, syncResult);
        getContext().sendBroadcast(intent);
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

        // Get refresh token
        String refreshToken = mAccountManager.blockingGetAuthToken(account, TOKEN_TYPE_REFRESH,
                false);

        // Get authentication client
        Authentication authentication = mClient.createAuthentication();
        try {
            // Request a new access token
            Token token = authentication
                    .blockingGetAccessToken(GRANT_TYPE_REFRESH_TOKEN, CLIENT_ID, refreshToken);

            // Save new access token
            mAccountManager.setAuthToken(account, TOKEN_TYPE_ACCESS, token.accessToken);
            mAccountManager.setUserData(account, KEY_ACCESS_TOKEN_EXPIRES,
                    Long.toString(System.currentTimeMillis() + (token.expires * 1000)));
        } catch (RetrofitError e) {
            // Check for a 401/403 as we can try and re-authenticate with an email/password
            if (e.getResponse() != null &&
                    (e.getResponse().getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED ||
                            e.getResponse().getStatus() == HttpURLConnection.HTTP_FORBIDDEN)) {
                String email = mAccountManager.getUserData(account, AccountConstants.KEY_EMAIL);
                String password = mAccountManager.getPassword(account);
                Token token = authentication.blockingGetAccessToken(GRANT_TYPE_PASSWORD,
                        CLIENT_ID, email, password);

                // Save new tokens
                mAccountManager.setAuthToken(account, TOKEN_TYPE_ACCESS, token.accessToken);
                mAccountManager.setAuthToken(account, TOKEN_TYPE_REFRESH, token.refreshToken);
                mAccountManager.setUserData(account, KEY_ACCESS_TOKEN_EXPIRES,
                        Long.toString(System.currentTimeMillis() + (token.expires * 1000)));
            } else {
                // Rethrow the exception if something else went wrong
                throw e;
            }
        }
    }

    /**
     * Sync local changes back to the server
     *
     * @param blog       The blog associated with the account
     * @param syncResult Sync result
     */
    private void syncLocal(Blog blog, SyncResult syncResult) {
        List<Post> updatedPosts = new Select()
                .from(Post.class)
                .where("blog_id = ? AND sync_local_changes = ? AND remote_conflicted = ? AND remote_deleted = ?",
                        blog.getId(), true, false, false)
                .execute();

        // If nothing to update, return
        if (updatedPosts.size() == 0) {
            return;
        }

        // Sync changes
        Posts posts = mClient.createPosts();
        for (Post post : updatedPosts) {
            Log.d(TAG, "Syncing post");

            // If editing an existing post get version from server
            if (post.id != null) {
                PostsContainer postsContainer = posts.blockingGetPost(post.id);
                Post remotePost = postsContainer.posts.get(0);
                if (!remotePost.updatedAt.equals(post.updatedAt)) {
                    // Post edited remotely, create conflict
                    createConflict(post, remotePost, syncResult);
                    continue;
                }
            }

            // Load draft and update post
            PostDraft postDraft = new Select()
                    .from(PostDraft.class)
                    .where("post_id = ?", post.getId())
                    .executeSingle();
            post.title = postDraft.title;
            post.markdown = postDraft.markdown;

            // Create post container
            PostsContainer postsContainer = new PostsContainer();
            postsContainer.posts = new ArrayList<>();
            postsContainer.posts.add(post);

            // Send request
            PostsContainer responseContainer;
            if (post.id == null) {
                responseContainer = posts.blockingAddPost(postsContainer);
            } else {
                responseContainer = posts.blockingUpdatePost(post.id, postsContainer);
            }
            Post remotePost = responseContainer.posts.get(0);

            // Update local version from remote response
            if (post.id == null) {
                post.id = remotePost.id;
            }
            post.uuid = remotePost.uuid;
            post.title = remotePost.title;
            post.slug = remotePost.slug;
            post.markdown = remotePost.markdown;
            post.html = remotePost.html;
            post.image = remotePost.image;
            post.featured = remotePost.featured;
            post.page = remotePost.page;
            post.status = remotePost.status;
            post.language = remotePost.language;
            post.metaTitle = remotePost.metaTitle;
            post.metaDescription = remotePost.metaDescription;
            post.createdAt = remotePost.createdAt;
            post.createdBy = remotePost.createdBy;
            post.updatedAt = remotePost.updatedAt;
            post.updatedBy = remotePost.updatedBy;
            post.publishedAt = remotePost.publishedAt;
            post.publishedBy = remotePost.publishedBy;
            post.author = remotePost.author;

            // Update synced local revision
            post.localRevision = postDraft.revision;
            post.localRevisionEdit = postDraft.revisionEdit;
            post.syncLocalChanges = false;

            post.save();
            syncResult.stats.numUpdates++;
        }
    }

    /**
     * Get blog data from the server, a pretty straight forward fetch all and replace approach for
     * now, but this is far from optimal and ideally needs to pull changes only but this
     * functionality doesn't yet look like it exists in the Ghost API.
     * TODO Hopefully this can be optimised at a later date, depends on the API
     *
     * @param blog       The blog associated with the account
     * @param syncResult Sync result
     */
    private void syncRemote(Blog blog, SyncResult syncResult) throws AuthenticatorException,
            OperationCanceledException, IOException, RetrofitError, NoSuchAlgorithmException {
        // Clear the image queue
        mImageQueue.clear();

        // Sync users
        Users users = mClient.createUsers();
        int page = 1;
        int totalPages = 1;
        while (page <= totalPages) {
            UsersContainer usersContainer = users.blockingGetUsers(page);
            for (User user : usersContainer.users) {
                user.blog = blog;
                saveUser(user, syncResult);
                saveContent(blog, user.image, ContentProvider.createUri(User.class, user.getId()), syncResult);
                saveContent(blog, user.cover, ContentProvider.createUri(User.class, user.getId()), syncResult);
            }
            totalPages = usersContainer.meta.pagination.pages;
            page++;
        }

        // Sync settings (no pagination)
        Settings settings = mClient.createSettings();
        SettingsContainer settingsContainer = settings.blockingGetSettings(Setting.Type.BLOG,
                1);
        for (Setting setting : settingsContainer.settings) {
            setting.blog = blog;
            saveSetting(setting, syncResult);

            // If setting contains an image queue it for download
            switch (setting.key) {
                case LOGO:
                case COVER:
                    if (setting.value != null && !setting.value.trim().isEmpty()) {
                        Log.d(TAG, "Queuing image download: " + setting.value);
                        mImageQueue.add(new Pair<>(setting.value,
                                ContentProvider.createUri(Setting.class, null)));
                    }
                    break;
            }
        }

        // Sync tags
        Tags tags = mClient.createTags();
        page = 1;
        totalPages = 1;
        while (page <= totalPages) {
            TagsContainer tagsContainer = tags.blockingGetTags(page);
            for (Tag tag : tagsContainer.tags) {
                tag.blog = blog;
                saveTag(tag, syncResult);
            }
            totalPages = tagsContainer.meta.pagination.pages;
            page++;
        }

        // Sync posts
        Posts posts = mClient.createPosts();
        page = 1;
        totalPages = 1;
        List<Integer> remoteIds = new ArrayList<>();
        while (page <= totalPages) {
            PostsContainer postsContainer = posts.blockingGetPosts(page, null, Status.ALL, false);
            for (Post post : postsContainer.posts) {
                remoteIds.add(post.id);
                post.blog = blog;
                savePost(post, syncResult);
                if (post.image != null && !post.image.trim().isEmpty()) {
                    Log.d(TAG, "Queuing image download: " + post.image);
                    mImageQueue.add(new Pair<>(post.image,
                            ContentProvider.createUri(Post.class, post.getId())));
                }
                findImages(post);
            }
            totalPages = postsContainer.meta.pagination.pages;
            page++;
        }

        // Sync pages
        page = 1;
        totalPages = 1;
        while (page <= totalPages) {
            PostsContainer postsContainer = posts.blockingGetPosts(page, null, Status.ALL, true);
            for (Post post : postsContainer.posts) {
                remoteIds.add(post.id);
                post.blog = blog;
                savePost(post, syncResult);
                if (post.image != null && !post.image.trim().isEmpty()) {
                    Log.d(TAG, "Queuing image download: " + post.image);
                    mImageQueue.add(new Pair<>(post.image,
                            ContentProvider.createUri(Post.class, post.getId())));
                }
                findImages(post);
            }
            totalPages = postsContainer.meta.pagination.pages;
            page++;
        }

        // Check for deleted posts and remove/flag
        List<Post> dbPosts = new Select().from(Post.class).where("blog_id = ?", blog.getId())
                .execute();
        for (Post post : dbPosts) {
            if (post.id != null && !remoteIds.contains(post.id)) {
                Log.d(TAG, "Remotely deleted post found");

                // Check latest draft for local changes
                boolean localChanges = false;
                PostDraft postDraft = new Select()
                        .from(PostDraft.class)
                        .where("post_id = ?", post.getId())
                        .executeSingle();
                if (postDraft != null && (post.localRevision != postDraft.revision ||
                        post.localRevisionEdit != postDraft.revisionEdit)) {
                    Log.d(TAG, "A newer draft exists");
                    localChanges = true;
                }

                // Mark the post as deleted remotely, or just delete it
                if (localChanges) {
                    Log.d(TAG, "Deleted post has local changes");
                    if (!post.remoteDeleted) {
                        post.remoteDeleted = true;
                        post.save();
                        syncResult.stats.numUpdates++;
                    }
                } else {
                    post.delete();
                    syncResult.stats.numDeletes++;
                }
            }
        }

        // Download images
        int queuePosition = 0;
        for (Pair pair : mImageQueue) {
            queuePosition++;
            Log.d(TAG, String.format("Starting image download (%d/%d): %s", queuePosition,
                    mImageQueue.size(), pair.first));
            saveContent(blog, (String) pair.first, (Uri) pair.second, syncResult);
        }
    }

    /**
     * Load or create the blog record
     *
     * @param blogUrl URL of the blog
     * @param email   Email of the user
     */
    private Blog getBlog(String blogUrl, String email) {
        // Load the blog record
        Blog blog = DatabaseUtils.getBlog(blogUrl, email);

        // If record is missing, recreate it
        if (blog == null) {
            blog = new Blog();
            blog.url = blogUrl;
            blog.email = email;
            blog.save();
        }

        return blog;
    }

    /**
     * Look within post markdown for images to download and cache for offline viewing
     *
     * @param post Post to search
     */
    private void findImages(Post post) {
        Matcher matcher = Pattern.compile("!\\[.*\\]\\((.*)\\)").matcher(post.markdown);
        while (matcher.find()) {
            Log.d(TAG, "Queuing content image download: " + matcher.group(1));
            mImageQueue.add(new Pair<>(matcher.group(1),
                    ContentProvider.createUri(Post.class, post.getId())));
        }
    }

    /**
     * Save content (expects images)
     *
     * @param blog            Blog record
     * @param path            Image path
     * @param notificationUri URI to notify of changes
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private void saveContent(Blog blog, String path, Uri notificationUri, SyncResult syncResult)
            throws NoSuchAlgorithmException, IOException {
        // Check that the path looks like something valid
        if (path == null || path.trim().isEmpty()) {
            return;
        }

        // Make sure the path is a full URL
        path = ImageUtils.getUrl(blog, path);

        // Generate a filename
        String filename = ImageUtils.getFilename(getContext(), blog, path);
        Log.d(TAG, "Local file: " + filename);

        // Make sure destination directory exists
        if (!ImageUtils.ensureDirectory(filename.substring(0, filename.lastIndexOf('/')))) {
            Log.e(TAG, "Content directory missing");
            return;
        }

        // Check if the file exists
        if (ImageUtils.fileExists(filename)) {
            Log.d(TAG, "File exists, skipping");
            return;
        }

        // Connect
        URL url = new URL(path);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(60000);
        connection.connect();

        // Save the image as a temporary file as IO errors on bitmap decode never throw an error
        Log.d(TAG, "Saving temporary file: " + filename + ".tmp");
        File file = new File(filename + ".tmp");
        IOUtils.copy(connection.getInputStream(), new FileOutputStream(file));

        // Decode the file
        Log.d(TAG, "Decoding to file: " + filename);
        ImageUtils.decodeScale(new FileInputStream(file), filename, 2048, 2048);
        if (!file.delete()) {
            Log.e(TAG, "Failed to delete temporary file");
        }
        if (notificationUri != null) {
            getContext().getContentResolver().notifyChange(notificationUri, null);
        }
        syncResult.stats.numInserts++;
    }

    /**
     * Save a user record if necessary
     *
     * @param user       User record
     * @param syncResult Sync result
     */
    private void saveUser(User user, SyncResult syncResult) {
        // Get the local record
        User dbUser = new Select()
                .from(User.class)
                .where("blog_id = ? AND remote_id = ?", user.blog.getId(), user.id)
                .executeSingle();

        // Check if user was actually updated
        if (dbUser != null && user.updatedAt.compareTo(dbUser.updatedAt) <= 0) {
            Log.d(TAG, "User is unchanged");
            return;
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
     * @param setting    Setting record
     * @param syncResult Sync result
     */
    private void saveSetting(Setting setting, SyncResult syncResult) {
        // Get the local record
        Setting dbSetting = new Select()
                .from(Setting.class)
                .where("blog_id = ? AND remote_id = ?", setting.blog.getId(), setting.id)
                .executeSingle();

        // Check if setting was actually updated
        if (dbSetting != null && setting.updatedAt.compareTo(dbSetting.updatedAt) <= 0) {
            Log.d(TAG, "Setting is unchanged");
            return;
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
     * @param tag        Tag record
     * @param syncResult Sync result
     */
    private void saveTag(Tag tag, SyncResult syncResult) {
        // Get the local record
        Tag dbTag = new Select()
                .from(Tag.class)
                .where("blog_id = ? AND remote_id = ?", tag.blog.getId(), tag.id)
                .executeSingle();

        // Check if tag was actually updated
        if (dbTag != null && tag.updatedAt.compareTo(dbTag.updatedAt) <= 0) {
            Log.d(TAG, "Tag is unchanged");
            return;
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
     * @param post       Post record
     * @param syncResult Sync result
     */
    private void savePost(Post post, SyncResult syncResult) {
        // Get the local record
        Post dbPost = new Select()
                .from(Post.class)
                .where("blog_id = ? AND remote_id = ?", post.blog.getId(), post.id)
                .executeSingle();

        // Check if post was actually updated
        if (dbPost != null && dbPost.updatedAt != null &&
                post.updatedAt.compareTo(dbPost.updatedAt) <= 0) {
            Log.d(TAG, "Post is unchanged");
            return;
        }

        // Save the record
        ActiveAndroid.beginTransaction();
        try {
            Post savedPost;

            if (dbPost != null) {
                // Check latest draft for local changes
                boolean localChanges = false;
                PostDraft postDraft = new Select()
                        .from(PostDraft.class)
                        .where("post_id = ?", dbPost.getId())
                        .executeSingle();
                if (postDraft != null && (dbPost.localRevision != postDraft.revision ||
                        dbPost.localRevisionEdit != postDraft.revisionEdit)) {
                    Log.d(TAG, "A newer draft exists");
                    localChanges = true;
                }

                // If local updates exist, skip but check for conflicts
                if (localChanges && !dbPost.updatedAt.equals(post.updatedAt)) {
                    Log.d(TAG, "Record is conflicted, changed locally and remotely");
                    if (!dbPost.remoteConflicted) {
                        // Mark the post as conflicted
                        dbPost.remoteConflicted = true;
                        dbPost.save();
                        syncResult.stats.numUpdates++;
                    }
                    createConflict(dbPost, post, syncResult);
                    return;
                }

                // Update the draft if necessary
                if (postDraft != null) {
                    postDraft.title = post.title;
                    postDraft.markdown = post.markdown;
                    postDraft.save();
                    syncResult.stats.numUpdates++;
                }

                // Update the post
                dbPost.uuid = post.uuid;
                dbPost.title = post.title;
                dbPost.slug = post.slug;
                dbPost.markdown = post.markdown;
                dbPost.html = post.html;
                dbPost.image = post.image;
                dbPost.featured = post.featured;
                dbPost.page = post.page;
                dbPost.status = post.status;
                dbPost.language = post.language;
                dbPost.metaTitle = post.metaTitle;
                dbPost.metaDescription = post.metaDescription;
                dbPost.createdAt = post.createdAt;
                dbPost.createdBy = post.createdBy;
                dbPost.updatedAt = post.updatedAt;
                dbPost.updatedBy = post.updatedBy;
                dbPost.publishedAt = post.publishedAt;
                dbPost.publishedBy = post.publishedBy;
                dbPost.author = post.author;
                dbPost.save();
                syncResult.stats.numUpdates++;
                savedPost = dbPost;
            } else {
                post.save();
                syncResult.stats.numInserts++;
                savedPost = post;
            }

            // Delete any existing post/tag associations
            List<PostTag> dbPostTags = new Select()
                    .from(PostTag.class)
                    .where("post_id = ?", savedPost.getId())
                    .execute();
            for (PostTag dbPostTag : dbPostTags) {
                dbPostTag.delete();
                syncResult.stats.numDeletes++;
            }

            // Link current tags to the post
            for (Tag tag : post.tags) {
                // Get the tag record from the database as the object created from JSON doesn't
                // save correctly unless saved first itself
                Tag dbTag = new Select()
                        .from(Tag.class)
                        .where("remote_id = ?", tag.id)
                        .executeSingle();
                if (dbTag != null) {
                    PostTag postTag = new PostTag();
                    postTag.post = savedPost;
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

    /**
     * Create a conflict record when a pot is changed both locally and remotely
     *
     * @param local      Local version of a post
     * @param remote     Remote version of a post
     * @param syncResult Sync result
     */
    private void createConflict(Post local, Post remote, SyncResult syncResult) {
        // Check if conflict record exits
        PostConflict postConflict = new Select()
                .from(PostConflict.class)
                .where("post_id = ?", local.getId())
                .executeSingle();

        // Create or update the record
        if (postConflict == null) {
            postConflict = new PostConflict();
            postConflict.blog = local.blog;
            postConflict.post = local;
            postConflict.title = remote.title;
            postConflict.markdown = remote.markdown;
            postConflict.updatedAt = remote.updatedAt;
            postConflict.updatedBy = remote.updatedBy;
            postConflict.save();
            syncResult.stats.numInserts++;
        } else if (!postConflict.updatedAt.equals(remote.updatedAt)) {
            postConflict.title = remote.title;
            postConflict.markdown = remote.markdown;
            postConflict.updatedAt = remote.updatedAt;
            postConflict.updatedBy = remote.updatedBy;
            postConflict.save();
            syncResult.stats.numUpdates++;
        }
    }

}
