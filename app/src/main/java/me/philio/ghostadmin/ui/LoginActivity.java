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
package me.philio.ghostadmin.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.activeandroid.ActiveAndroid;

import me.philio.ghostadmin.R;
import me.philio.ghostadmin.account.AccountAuthenticatorActionBarActivity;
import me.philio.ghostadmin.model.Blog;
import me.philio.ghostadmin.model.Token;
import me.philio.ghostadmin.model.User;
import me.philio.ghostadmin.sync.SyncHelper;

import static me.philio.ghostadmin.account.AccountConstants.KEY_ACCESS_TOKEN_EXPIRES;
import static me.philio.ghostadmin.account.AccountConstants.KEY_ACCESS_TOKEN_TYPE;
import static me.philio.ghostadmin.account.AccountConstants.KEY_BLOG_URL;
import static me.philio.ghostadmin.account.AccountConstants.KEY_EMAIL;
import static me.philio.ghostadmin.account.AccountConstants.TOKEN_TYPE_ACCESS;
import static me.philio.ghostadmin.account.AccountConstants.TOKEN_TYPE_REFRESH;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AccountAuthenticatorActionBarActivity implements
        LoginUrlFragment.OnFragmentInteractionListener,
        LoginFragment.OnFragmentInteractionListener {

    /**
     * The url of the blog
     */
    private String mBlogUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Add login url fragment to request the blog url to login to
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, LoginUrlFragment.newInstance())
                    .commit();
        }

        // Make sure back is shown if back stack exists
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onValidUrl(String blogUrl) {
        // Store for later
        mBlogUrl = blogUrl;

        // Replace url fragment with login fragment
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.container, LoginFragment.newInstance(blogUrl))
                .addToBackStack(null)
                .commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSuccess(String email, String password, Token token, User user) {
        // Create the account
        Uri uri = Uri.parse(mBlogUrl);
        Account account = new Account(email + " (" + uri.getHost() + ")",
                getString(R.string.account_type));
        Bundle userdata = new Bundle();
        userdata.putString(KEY_BLOG_URL, mBlogUrl);
        userdata.putString(KEY_EMAIL, email);
        userdata.putString(KEY_ACCESS_TOKEN_TYPE, token.tokenType);
        userdata.putString(KEY_ACCESS_TOKEN_EXPIRES, Long.toString(System.currentTimeMillis() +
                (token.expires * 1000)));

        // Add account to the system
        AccountManager accountManager = AccountManager.get(this);
        accountManager.addAccountExplicitly(account, password, userdata);

        // Set the account auth tokens
        accountManager.setAuthToken(account, TOKEN_TYPE_ACCESS, token.accessToken);
        accountManager.setAuthToken(account, TOKEN_TYPE_REFRESH, token.refreshToken);

        // Create initial database records
        Blog blog = new Blog();
        blog.url = mBlogUrl;
        blog.email = email;
        user.blog = blog;
        ActiveAndroid.beginTransaction();
        try {
            blog.save();
            user.save();
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }

        // Enable sync for the account
        ContentResolver.setSyncAutomatically(account, getString(R.string.content_authority), true);
        SyncHelper.requestSync(account, getString(R.string.content_authority));

        // Set response intent
        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

}



