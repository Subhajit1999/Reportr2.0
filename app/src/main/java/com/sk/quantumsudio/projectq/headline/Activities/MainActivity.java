package com.sk.quantumsudio.projectq.headline.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.sk.quantumsudio.projectq.headline.fragments.MainFragment;
import com.sk.quantumsudio.projectq.headline.R;
import com.sk.quantumsudio.projectq.headline.utils.NewsItem;
import com.sk.quantumsudio.projectq.headline.utils.Preferences;
import com.sk.quantumsudio.projectq.headline.utils.ViewPagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static String APP_STORE_URL;
    public static int checkedItem;
    public static RequestQueue requestQueue;
    public static ArrayList<NewsItem> savedNewsList;

    Toolbar homeToolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter adapter;
    TextView breakingNews, marqueeText;
    MaterialSearchView searchView;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    String marqueeNews, CountryId;
    BroadcastReceiver broadcastReceiver;
    RelativeLayout root;
    LinearLayout linear;
    NavigationView navigationView;

    ImageView imageToggle;
    LinearLayout errorView;
    Button retry;
    LinearLayout bottombar,errorLayout;
    TextView errorTitle,errorDesc;
    int id=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starting onCreate of MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBarGradiant(MainActivity.this);
        APP_STORE_URL = "http://play.google.com/store/apps/details?id=" + getPackageName();

        onWidgetsInit(); //all widgets initialization
        setUpSearchView();   //material searchView
        setUpNavigationDrawer();  //material navigation drawer
        checkNetworkAvailabilty();  //checks network connections in loop

        viewPager.setOnTouchListener(new View.OnTouchListener() {  //to prevent swipe refresh to be triggered while horizontal scrolling occurred
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MainFragment.refreshLayout.setEnabled(false);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        MainFragment.refreshLayout.setEnabled(true);
                        break;
                }
                return false;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity) {
        Log.d(TAG, "setStatusBarGradiant: Changing the status bar color");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.gredient_primary);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.background_dark));
            window.setBackgroundDrawable(background);
        }
    }

    public void onWidgetsInit() {
        Log.d(TAG, "onWidgetsInit: Widgets initialization of mainActivity");
        homeToolbar = findViewById(R.id.home_toolbar);  //toolbar
        setSupportActionBar(homeToolbar);

        viewPager = findViewById(R.id.viewpager);  //setup viewPager
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout = findViewById(R.id.tabs);  //setting up tabs with viewPager
        tabLayout.setupWithViewPager(viewPager);

        breakingNews = findViewById(R.id.tv_breakingNews);  //for moving marquee text
        marqueeText = findViewById(R.id.text_marquee);
        marqueeText.setSelected(true);

        searchView = findViewById(R.id.search_view);

        drawer = findViewById(R.id.drawer_layout);
        root = findViewById(R.id.rel);
        linear = findViewById(R.id.linear);

        navigationView = findViewById(R.id.navigation_view);
        errorView = findViewById(R.id.error_view);

        requestQueue = (RequestQueue) Volley.newRequestQueue(getApplicationContext());
    }

    public void setUpSearchView() {    //setting up material searchView
        Log.d(TAG, "setUpSearchView: Setting up materialSearchView");
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Intent intent = new Intent(MainActivity.this,GeneralActivity.class);
                intent.putExtra(Preferences.KEY_FRAGMENT_ID,1);
                intent.putExtra(Preferences.KEY_SEARCH_QUERY,query);
                startActivity(intent);
                searchView.closeSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
                linear.setAlpha(0.5f);
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
                linear.setAlpha(1);
            }
        });
    }

    public void setUpNavigationDrawer() {   //setting up material Navigation Drawer
        Log.d(TAG, "setUpNavigationDrawer: setting up navigation drawer");
        toggle = new ActionBarDrawerToggle(this, drawer, homeToolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.bookmarks:
                        //saved news fragment
                        Intent intent = new Intent(MainActivity.this,GeneralActivity.class);
                        intent.putExtra(Preferences.KEY_FRAGMENT_ID,0);
                        startActivity(intent);
                        break;
                    case R.id.action_app_share:
                        //share app intent
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Checkout this Awesome NewsFeed App \"Reportr\" to get latest news everyday across the World, among various categories.\n\n Download from Play Store: \n"+MainActivity.APP_STORE_URL);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Share App to..."));
                        break;
                    case R.id.action_app_rate:
                        //rate app intent
                        Uri uri = Uri.parse("market://details?id=" + getPackageName());
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        }
                        try {
                            startActivity(goToMarket);    //if the device has play store app installed
                        } catch (Exception e) {   //if not
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                        }
                        break;
                    case R.id.action_feedback:
                        //feedback mail intent
                        Intent feedback = new Intent(Intent.ACTION_SEND);
                        feedback.setData(Uri.parse("mailto:"));
                        String[] emailTo = {"developercontact.subhajitkar@gmail.com"};
                        feedback.putExtra(Intent.EXTRA_EMAIL,emailTo);
                        feedback.putExtra(Intent.EXTRA_SUBJECT,"feedback related "+getString(R.string.app_name)+" Android app");
                        feedback.putExtra(Intent.EXTRA_TEXT,"Replace this text to whatever you want.");
                        feedback.setType("message/rfc822");
                        startActivity(Intent.createChooser(feedback,"Send Feedback Email with..."));
                        break;
                    case R.id.action_app_about:
                        //about page
                        Intent aboutIntent = new Intent(MainActivity.this,AboutActivity.class);
                        startActivity(aboutIntent);
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  //attaches the menu with the toolbar
        Log.d(TAG, "onCreateOptionsMenu: attaching menu inside toolbar");
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search_news);  //sets up searchView with the menu search item
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {  //handles menu items click functions
        Log.d(TAG, "onOptionsItemSelected: menu click handle in mainActivity");
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_country) {
            countryDialogMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: overrides the default back functionality");
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    private void parseJson() {
        Log.d(TAG, "parseJson: parsing json data for mainActivity marquee");
        marqueeNews = getString(R.string.read_news_anytime_anywhere);

        String url = "https://newsapi.org/v2/top-headlines?country=" + CountryId + "&apiKey=" + Preferences.Auth_Key;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("articles");
                    for (int i = 0; i < 10; i++) {
                        JSONObject news = jsonArray.getJSONObject(i);

                        String title = news.getString("title");

                        marqueeNews = marqueeNews + ". (" + (i + 1) + ") " + title + ". ";
                    }
                    marqueeText.setText(marqueeNews);
                } catch (JSONException e) {
                    marqueeText.setText(R.string.marquee_newsParseError);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                marqueeText.setText(R.string.marquee_error);
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
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
                    Snackbar snackbar = Snackbar
                            .make(root, "Internet connection active.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    parseJson();
                } else {
                    //handle network error when connection is gone
                    String title ="Internet connection error.";
                    String desc = "No Internet connection. Check the network and try again.";
                    errorView.setVisibility(View.VISIBLE);
                    Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down);
                    errorView.startAnimation(aniSlide);
                    customErrorHandler(title,desc);
                    Snackbar snackbar = Snackbar
                            .make(root, "No Internet connection. Check the network.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void countryDialogMenu() {
        Log.d(TAG, "countryDialogMenu: AlertDialog menu in mainActivity");
        //setting up the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose your country");
        builder.setIcon(R.drawable.ic_action_country_dark);

        //Adding a list of items with radio buttons
        builder.setSingleChoiceItems(Preferences.countryList, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user checked item
                CountryId = Preferences.countryId[which];
                checkedItem = which;
            }
        });
        //Adding ok and cancel buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user clicked ok
                parseJson();    //refresh the news with the changed country preferences

            }
        });
        builder.setNegativeButton("Cancel", null);

        //Creating and showing the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);  //restoring back the values from preference

        if (preferences!=null){
            checkedItem = preferences.getInt(Preferences.KEY_COUNTRY, 0);
        }else{
            checkedItem=0;
        }
        CountryId = Preferences.countryId[checkedItem];
        loadBookmarkedData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        drawer.closeDrawers();

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Preferences.KEY_COUNTRY, MainActivity.checkedItem);  //saving country
        editor.apply();
    }

    public void loadBookmarkedData(){
        Log.d(TAG, "loadData: Loading data from preferences");
        SharedPreferences preferences = getSharedPreferences(Preferences.KEY_PREFERENCES,MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(Preferences.KEY_BOOKMARK_LIST, "");
        if (!json.isEmpty()){
            Type type = new TypeToken<ArrayList<NewsItem>>() {
            }.getType();
            MainActivity.savedNewsList = gson.fromJson(json, type);
            Log.d(TAG, "loadBookmarkedData: list Size: "+savedNewsList.size());
        }else{
            savedNewsList = new ArrayList<>();
            Log.d(TAG, "loadBookmarkedData: list Size: "+savedNewsList.size());
        }
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
}
