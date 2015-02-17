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

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghost.BuildConfig;
import me.philio.ghost.R;

public class AboutActivity extends BaseActivity {

    /**
     * Views
     */
    @InjectView(R.id.txt_version)
    protected TextView mVersion;
    @InjectView(R.id.txt_copyright)
    protected TextView mCopyright;
    @InjectView(R.id.txt_open_source)
    protected TextView mOpenSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.inject(this);

        // Set version number
        mVersion.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));

        // Set copyright year
        int year = Calendar.getInstance().get(Calendar.YEAR);
        mCopyright.setText(getString(R.string.about_copyright, year > 2014 ?
                "2014 - " + Integer.toString(year) : Integer.toString(year)));

        // Format html
        mOpenSource.setText(Html.fromHtml(getString(R.string.about_open_source)));
        mOpenSource.setMovementMethod(LinkMovementMethod.getInstance());
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

}
