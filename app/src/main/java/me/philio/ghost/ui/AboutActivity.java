package me.philio.ghost.ui;

import android.os.Bundle;
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
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
