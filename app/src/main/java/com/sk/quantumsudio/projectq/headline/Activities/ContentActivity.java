package com.sk.quantumsudio.projectq.headline.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sk.quantumsudio.projectq.headline.fragments.MainFragment;
import com.sk.quantumsudio.projectq.headline.R;
import com.sk.quantumsudio.projectq.headline.utils.NewsItem;
import com.sk.quantumsudio.projectq.headline.utils.NewsRecyclerAdapter;
import com.sk.quantumsudio.projectq.headline.utils.Preferences;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ContentActivity extends AppCompatActivity {
    private static final String TAG = "ContentActivity";
    public final Context mContext = ContentActivity.this;

    CoordinatorLayout coordinator;
    CollapsingToolbarLayout collapsingToolbar;
    NestedScrollView scrollView;
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    ImageView newsImage;
    TextView newsTitle, newsSource, publishTime, newsDescription;
    FloatingActionButton fab;
    String title, source, time, contentUrl, imageUrl, newsDesc;
    WebView newsContent;
    ProgressBar progress;
    boolean appBarExpanded;
    CardView card1, card2;
    Button readMore;
    int fab_flag = 0;
    BroadcastReceiver broadcastReceiver;

    ImageView imageToggle;
    Button retry;
    LinearLayout bottombar,errorLayout,errorView;
    TextView errorTitle,errorDesc;
    int id=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ContentActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        //getting the data of the received position from the list
        Intent intent = getIntent();
        title = intent.getStringExtra(MainFragment.EXTRA_NEWS_TITLE);
        source = intent.getStringExtra(MainFragment.EXTRA_NEWS_SOURCE);
        time = intent.getStringExtra(MainFragment.EXTRA_PUBLISH_TIME);
        contentUrl = intent.getStringExtra(MainFragment.EXTRA_NEWS_CONTENT);
        imageUrl = intent.getStringExtra(MainFragment.EXTRA_NEWS_IMAGE);
        newsDesc = intent.getStringExtra(MainFragment.EXTRA_NEWS_DESC);

        collapsingToolbar = findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        coordinator = findViewById(R.id.coordinator);
        newsImage = findViewById(R.id.expandedImage);
        scrollView = findViewById(R.id.scroll_view);
        newsTitle = findViewById(R.id.tv_news_title);
        newsSource = findViewById(R.id.tv_source);
        publishTime = findViewById(R.id.tv_publishtime);
        newsContent = findViewById(R.id.webViewContent);
        progress = findViewById(R.id.progress_bar);
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        readMore = findViewById(R.id.bt_read_more);
        newsDescription = findViewById(R.id.tv_desc);
        errorView = findViewById(R.id.error_view);

        //floating action button
        fab = findViewById(R.id.fab_bookmark);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fab_flag == 0) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_pressed));
                } else {
                    fab.setImageResource(R.drawable.ic_fav);
                }
                bookmarkNews();
            }
        });
        setupToolBar();
        setUpAppBar();
        setupValues();
        checkNetworkAvailabilty();

        //read more button
        readMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                card1.setVisibility(View.GONE);
                card2.setVisibility(View.VISIBLE);
                loadWebContent();  //for loading web content
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: getting called");
        super.onResume();

        //checking if the news is already bookmarked or not
        for (int i=0;i<MainActivity.savedNewsList.size();i++) {
            Log.d(TAG, "onResume: Inside loop");
            if (MainActivity.savedNewsList.get(i).getmContentUrl().equals(contentUrl)) {
                Log.d(TAG, "onResume: Item found");
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_pressed));
                fab_flag=1;
            }
        }
    }

    public void setupToolBar() {
        Log.d(TAG, "setupToolBar: Setting up toolbar");
        //adding arrow back to the toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newsContent.canGoBack()) {
                    newsContent.goBack();
                } else {
                    onSupportNavigateUp();
                }
            }
        });
    }

    public void setUpAppBar() {
        Log.d(TAG, "setUpAppBar: Setting appbar to control collapsing toolbar");
        //Applying the custom toolbar background in the collapsing toolbar
        appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(final AppBarLayout appBarLayout, int verticalOffset) {

                //  Vertical offset == 0 indicates appBar is fully expanded.
                if (Math.abs(verticalOffset) > 200) {
                    appBarExpanded = false;
                    toolbar.setBackground(ContextCompat.getDrawable(mContext, R.drawable.gredient_primary));
                    invalidateOptionsMenu();
                } else {
                    appBarExpanded = true;
                    toolbar.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.transparent));
                    invalidateOptionsMenu();
                }
            }
        });
    }

    public void setupValues() {
        Log.d(TAG, "setupValues: setting up values to the views");

        if (imageUrl != null && imageUrl.length()>5){
            Picasso.with(mContext)
                    .load(imageUrl)
                    .placeholder(R.drawable.headlines)
                    .error(R.drawable.image_error)
                    .fit().centerInside()
                    .into(newsImage);
        }else{
            newsImage.setImageResource(R.drawable.image_error);
            newsImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        newsTitle.setText(title);
        newsSource.setText(source);
        try {           //tries to reformat the json date format into the simpler one
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(time);
            if (NewsRecyclerAdapter.printStandardDate(date) != null) {
                publishTime.setText(String.format(" * %s", NewsRecyclerAdapter.printStandardDate(date)));
            } else {
                publishTime.setText("");
            }
            newsDescription.setText(newsDesc);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void loadWebContent() {
        Log.d(TAG, "loadWebContent: Loading web site of news");

        newsContent.getSettings().setLoadsImagesAutomatically(true);
        newsContent.getSettings().setJavaScriptEnabled(true);
        newsContent.getSettings().setDomStorageEnabled(true);
        newsContent.getSettings().setSupportZoom(true);
        newsContent.getSettings().setBuiltInZoomControls(true);
        newsContent.getSettings().setDisplayZoomControls(true);
        newsContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        newsContent.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progress.setVisibility(View.INVISIBLE);
            }
        });
        newsContent.loadUrl(contentUrl);

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: system default back function");
        if (newsContent.canGoBack()) {
            newsContent.goBack();
        }
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: Attaching menu in the toolbar");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contentarea, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: Menu items click event");
        switch (item.getItemId()) {
            case R.id.action_share:

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Checkout this News about \"" + title + "\"\n Link: " + contentUrl + "\nDownload the \"Reportr\" App to get latest news everyday.\nLink: " + MainActivity.APP_STORE_URL + "\nDownload Now-:)");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share News through..."));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkNetworkAvailabilty() {
        Log.d(TAG, "checkNetworkAvailability: cvhecking network broadcasts in mainactivity");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();

                NetworkInfo info = (NetworkInfo) extras.getParcelable("networkInfo");
                NetworkInfo.State state = info.getState();

                if (state == NetworkInfo.State.CONNECTED) {
                    //to tasks when connection is back
                    errorView.setVisibility(View.GONE);
                    if (card2.getVisibility()==View.VISIBLE){
                        newsContent.reload();
                    }
                } else {
                    //handle network error when connection is gone
                    String title ="Internet Connection error.";
                    String desc = "No Internet connection. Check the network and try again.";
                    errorView.setVisibility(View.VISIBLE);
                    Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down);
                    errorView.startAnimation(aniSlide);
                    customErrorHandler(title,desc);
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void customErrorHandler(String topbarTitle, String bottombarText){

        imageToggle = findViewById(R.id.toggle_button);
        imageToggle.setImageResource(R.drawable.ic_expand_more);
        bottombar = findViewById(R.id.bottomBar);
        errorLayout = findViewById(R.id.error_layout);
        errorTitle = findViewById(R.id.top_bar_title);
        errorDesc = findViewById(R.id.bottom_bar_text);

        retry = findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottombar.setVisibility(View.GONE);
                checkNetworkAvailabilty();
            }
        });

        errorTitle.setText(topbarTitle);
        errorDesc.setText(bottombarText);

        imageToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: image clicked");
                if (id==0){
                    Log.e(TAG, "onClick: less");
                    //collapsed
                    imageToggle.setImageResource(R.drawable.ic_expand_less);
                    bottombar.setVisibility(View.VISIBLE);
                    Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down);
                    bottombar.startAnimation(aniSlide);
                    id=1;
                }else if (id==1){
                    Log.e(TAG, "onClick: more");
                    //expanded
                    //Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up);
                    //bottombar.startAnimation(aniSlide);
                    imageToggle.setImageResource(R.drawable.ic_expand_more);
                    bottombar.setVisibility(View.GONE);
                    id=0;
                }
            }
        });
    }

    public void bookmarkNews() {
        Log.d(TAG, "bookmarkNews: Bookmarking current news");

        if (fab_flag == 0) {   //add item
            MainActivity.savedNewsList.add(new NewsItem(imageUrl, source, time, title, contentUrl, newsDesc));
            Snackbar snackbar = Snackbar
                    .make(coordinator, "News saved to Favourite.", Snackbar.LENGTH_SHORT);
            snackbar.show();
            fab_flag = 1;
        } else {     //remove item
            for (int i=0;i<MainActivity.savedNewsList.size();i++){
                if (MainActivity.savedNewsList.get(i).getmContentUrl().equals(contentUrl) ){
                    MainActivity.savedNewsList.remove(i);
                    Snackbar snackbar = Snackbar
                            .make(coordinator, "Saved News removed from Favourite.", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
            fab_flag = 0;
        }
        //saving list
        saveBookmarkedData();
    }

    public void saveBookmarkedData(){
        Log.d(TAG, "saveData: Saving bookmarked data into preferences.List length: "+MainActivity.savedNewsList.size());
        SharedPreferences preferences = getSharedPreferences(Preferences.KEY_PREFERENCES,MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.savedNewsList);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Preferences.KEY_BOOKMARK_LIST,json );
        editor.apply();
    }
}
