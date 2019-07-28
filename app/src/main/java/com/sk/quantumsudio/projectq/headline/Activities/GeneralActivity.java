package com.sk.quantumsudio.projectq.headline.Activities;

import android.content.Intent;
import java.util.Arrays;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.quantumsudio.projectq.headline.fragments.BookmarkFragment;
import com.sk.quantumsudio.projectq.headline.R;
import com.sk.quantumsudio.projectq.headline.fragments.MainFragment;
import com.sk.quantumsudio.projectq.headline.utils.NewsRecyclerAdapter;
import com.sk.quantumsudio.projectq.headline.utils.Preferences;

public class GeneralActivity extends AppCompatActivity {
    private static final String TAG = "GeneralActivity";

    int id;
    Toolbar toolbar;
    String searchQuery,searchUrl;
    LinearLayout searchLayout;
    ImageView image;
    TextView searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: onCreate of content activity");
        super.onCreate(savedInstanceState);
        MainActivity.setStatusBarGradiant(this);
        setContentView(R.layout.activity_general);

        searchLayout = findViewById(R.id.linear);
        searchText = findViewById(R.id.tv_search);
        image = findViewById(R.id.iv_search);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {  //toolbar navigate button click
            @Override
            public void onClick(View v) {
                onSupportNavigateUp();
            }
        });

        Intent intent = getIntent();
        id = intent.getIntExtra(Preferences.KEY__FRAGMENT_ID, 0);

        if (id==0){    //setting up toolbar title
            getSupportActionBar().setTitle("Saved News");
        }else if (id==1){
            getSupportActionBar().setTitle("Search News");

            //getting and showing the search layout
            searchQuery = intent.getStringExtra(Preferences.KEY__SEARCH_QUERY);
            searchLayout.setVisibility(View.VISIBLE);
            searchText.setText(String.format("Showing Search result for: \"%s\"", searchQuery));
            Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down);
            searchLayout.startAnimation(aniSlide);

            if (searchQuery.contains(" ")){  //reformatting multi words search query
                searchQuery = searchQuery.toLowerCase().replace(" "," AND ");
                Log.e(TAG, "onCreate: searchQuery: "+searchQuery);
            }
            //creating the url
            searchUrl = "https://newsapi.org/v2/everything?q="+searchQuery+"&apiKey="+Preferences.Auth_Key;
        }
        if (id == 0) {  //bookmark fragment
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                    new BookmarkFragment()).commit();

        } else if (id == 1) { //search fragment

            Bundle bundle = new Bundle();   //sending the id and url with bundle
            bundle.putInt(Preferences.KEY__FRAGMAIN_ID,1);
            bundle.putString(Preferences.KEY_SEARCH_URL,searchUrl);
            // set Fragment class Arguments
            MainFragment fragobj = new MainFragment();
            fragobj.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                    fragobj).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: creating menu in toolbar");
        MenuInflater inflater = getMenuInflater();
        if (id == 0) {
            inflater.inflate(R.menu.bookmark_menu, menu);
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: System default back function");
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
