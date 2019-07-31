package com.sk.quantumsudio.projectq.headline.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.sk.quantumsudio.projectq.headline.R;
import com.sk.quantumsudio.projectq.headline.utils.Preferences;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AboutActivity";

    Element versionElement, tocElement, privacyElement;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        versionElement = new Element();
        tocElement = new Element();
        tocElement.setTitle("Terms and Conditions");
        tocElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "file:///android_asset/terms_and_conditions.html";
                Intent intent = new Intent(AboutActivity.this,GeneralActivity.class);
                intent.putExtra(Preferences.KEY_WEBFRAG_URL,url);
                intent.putExtra(Preferences.KEY_WEBFRAG_TITLE,"Terms & Conditions");
                intent.putExtra(Preferences.KEY_FRAGMENT_ID,2);
                startActivity(intent);
            }
        });
        privacyElement = new Element();
        privacyElement.setTitle("Privacy Policy");
        privacyElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "file:///android_asset/privacy_policy.html";
                Intent intent = new Intent(AboutActivity.this,GeneralActivity.class);
                intent.putExtra(Preferences.KEY_WEBFRAG_URL,url);
                intent.putExtra(Preferences.KEY_WEBFRAG_TITLE,"Privacy Policy");
                intent.putExtra(Preferences.KEY_FRAGMENT_ID,2);
                startActivity(intent);
            }
        });

        MainActivity.setStatusBarGradiant(this);
        versionElement.setTitle(getResources().getString(R.string.version_2_0));

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.app_logo_about)
                .setDescription(getResources().getString(R.string.description))
                .addItem(versionElement)
                .addGroup("Connect with us:")
                .addEmail("developercontact.subhajitkar@gmail.com")
                .addPlayStore(getPackageName())
                .addGroup("Legal Information:")
                .addItem(tocElement)
                .addItem(privacyElement)
                .create();

        setContentView(aboutPage);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("About");
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gredient_primary));
        }
    }
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: system default back function");
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
