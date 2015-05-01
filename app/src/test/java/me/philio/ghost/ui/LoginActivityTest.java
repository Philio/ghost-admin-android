/*
 * Copyright 2015 Phil Bayfield
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

import android.view.MenuItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import me.philio.ghost.BuildConfig;
import me.philio.ghost.R;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class LoginActivityTest {

    private LoginActivity loginActivity;

    @Before
    public void setUp() {
        loginActivity = setupActivity(LoginActivity.class);
    }

    @Test
    public void shouldAddFragment() {
        assertThat(loginActivity.getSupportFragmentManager().findFragmentById(R.id.container),
                instanceOf(LoginUrlFragment.class));
    }

    @Test
    public void shouldReplaceFragmentOnValidUrl() {
        loginActivity.onValidUrl("http://myblog.ghost.org/");
        assertThat(loginActivity.getSupportFragmentManager().findFragmentById(R.id.container),
                instanceOf(LoginFragment.class));
    }

    @Test
    public void shouldFinishActivityWhenHomePressed() {
        MenuItem item = new RoboMenuItem(android.R.id.home);
        loginActivity.onOptionsItemSelected(item);
        assertThat(loginActivity.isFinishing(), is(true));
    }

    @Test
    public void shouldFinishActivityWhenBackPressed() {
        loginActivity.onBackPressed();
        assertThat(loginActivity.isFinishing(), is(true));
    }

    @Test
    public void shouldPopBackStackWhenHomePressed() {
        MenuItem item = new RoboMenuItem(android.R.id.home);
        loginActivity.onValidUrl("http://myblog.ghost.org/");
        loginActivity.onOptionsItemSelected(item);
        assertThat(loginActivity.getSupportFragmentManager().getBackStackEntryCount(), is(0));
    }

    @Test
    public void shouldPopBackStackWhenBackPressed() {
        loginActivity.onValidUrl("http://myblog.ghost.org/");
        loginActivity.onBackPressed();
        assertThat(loginActivity.getSupportFragmentManager().getBackStackEntryCount(), is(0));
    }

}
