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

package me.philio.ghost.ui;

import android.accounts.Account;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import me.philio.ghost.R;
import me.philio.ghost.model.Post;
import me.philio.ghost.model.PostDraft;

public class EditorActivity extends BaseActivity implements
        MarkdownFragment.OnFragmentInteractionListener {

    /**
     * Extras
     */
    public static final String EXTRA_ACCOUNT = "account";
    public static final String EXTRA_POST_ID = "post_id";

    /**
     * Account of the current blog
     */
    private Account mAccount;

    /**
     * Id of the post to edit
     */
    private long mPostId;

    /**
     * The preview fragment
     */
    private PreviewFragment mPreviewFragment;

    /**
     * Views
     */
    @InjectView(R.id.viewpager)
    @Optional
    protected ViewPager mViewPager;

    /**
     * Adpater for the view pager
     */
    private EditorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.inject(this);

        // Check extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(EXTRA_ACCOUNT)) {
                mAccount = extras.getParcelable(EXTRA_ACCOUNT);
            }
            if (extras.containsKey(EXTRA_POST_ID)) {
                mPostId = extras.getLong(EXTRA_POST_ID);
            }
        }

        // Create new preview fragment instance
        mPreviewFragment = PreviewFragment.newInstance(null, null, 0, null);

        // Setup viewpager (for mobile)
        if (mViewPager != null) {
            mAdapter = new EditorAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostChanged(Post post, PostDraft draft) {
        mPreviewFragment.updatePreview(draft.title, draft.markdown, post.blog.getId(),
                post.blog.url);
    }

    /**
     * A simple adapter to drive the {@link ViewPager} on mobiles and portrait tablets
     */
    private class EditorAdapter extends FragmentPagerAdapter {

        public EditorAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MarkdownFragment.newInstance(mAccount, mPostId);
                case 1:
                    return mPreviewFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

}
