package me.philio.ghostadmin;

import com.activeandroid.app.Application;
import com.activeandroid.util.Log;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by phil on 27/11/2014.
 */
public class GhostApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Init calligraphy library
        CalligraphyConfig.initDefault("fonts/OpenSans-Regular.ttf", R.attr.fontPath);

        // Turn on ActiveAndroid logging
        Log.setEnabled(true);
    }

}
