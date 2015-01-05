package me.philio.ghost.ui;

import android.os.Bundle;
import android.view.MenuItem;

import me.philio.ghost.R;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Add the settings fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, SettingsFragment.newInstance(null))
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
