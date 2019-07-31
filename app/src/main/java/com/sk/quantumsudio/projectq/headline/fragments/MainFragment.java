package com.sk.quantumsudio.projectq.headline.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.sk.quantumsudio.projectq.headline.Activities.ContentActivity;
import com.sk.quantumsudio.projectq.headline.Activities.MainActivity;
import com.sk.quantumsudio.projectq.headline.R;
import com.sk.quantumsudio.projectq.headline.utils.Preferences;
import com.sk.quantumsudio.projectq.headline.utils.NewsItem;
import com.sk.quantumsudio.projectq.headline.utils.NewsRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainFragment extends Fragment implements NewsRecyclerAdapter.OnItemClickListener {
    private static final String TAG = "MainFragment";
    public static final String EXTRA_NEWS_TITLE = "newsTitle";
    public static final String EXTRA_NEWS_SOURCE = "newsSource";
    public static final String EXTRA_PUBLISH_TIME = "publishTime";
    public static final String EXTRA_NEWS_CONTENT = "newsContentUrl";
    public static final String EXTRA_NEWS_IMAGE = "newsImageUrl";
    public static final String EXTRA_NEWS_DESC = "newsDescription";

    String Url;
    int position,identifier=0;  //0 for home
    RecyclerView mRecyclerView;
    NewsRecyclerAdapter adapter;
    ArrayList<NewsItem> mNewsList;
    BroadcastReceiver broadcastReceiver;
    public static RecyclerRefreshLayout refreshLayout;  //using as static for using in MainActivity for touch efficiency purpose


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: creating view of fragment: " + position);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: view created of fragment: " + position);
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.recycler_news);  //setting recyclerView basic property
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NewsRecyclerAdapter(getContext(), mNewsList,0);  //adapter attached
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

        refreshLayout = view.findViewById(R.id.swipe_refresh);  //swipe refresh
        refreshLayout.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkNetworkAvailabilty();
            }
        });
        checkNetworkAvailabilty();  //parsing json checking the network connection
    }

    public static Fragment getInstance(int position) {
        Log.d(TAG, "getInstance: to pass the position of tabs from viewpagerAdapter: " + position);
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        MainFragment mainFragment = new MainFragment();
        mainFragment.setArguments(bundle);
        return mainFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {   //getting the position of currently active fragment
            position = getArguments().getInt("pos");
            Log.d(TAG, "onCreate: Fragment Position: " + position);
        }
        mNewsList = new ArrayList<>();  //getting the data from bundle
        Bundle bundle = getArguments();
        if (bundle != null){
            identifier = bundle.getInt(Preferences.KEY_FRAGMAIN_ID);
            Url = bundle.getString(Preferences.KEY_SEARCH_URL);
        }
    }

    public void parseJson(String url) {
        Log.d(TAG, "parseJson: Inside of ParseJson() function of fragment: " + position);

        if(url.equals("")){
            url = "https://newsapi.org/v2/top-headlines?country=" + Preferences.countryId[MainActivity.checkedItem] + "&category=" + Preferences.tabTitles[position].toLowerCase() + "&apiKey=" + Preferences.Auth_Key;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("articles");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject news = jsonArray.getJSONObject(i);

                        String imageUrl = news.getString("urlToImage");
                        String publishTime = news.getString("publishedAt");
                        String title = news.getString("title");
                        String contentUrl = news.getString("url");
                        String newsDescription = news.getString("description");

                        JSONObject sourceObject = news.getJSONObject("source");  //getting the bottom-level source json object
                        String source = sourceObject.getString("name");

                        mNewsList.add(new NewsItem(imageUrl, source, publishTime, title,contentUrl,newsDescription));
                        adapter.notifyDataSetChanged();  //adding this line fixed json parsed but no news loading issue
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "onResponse: Json Parsing data error.");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: Json parsing connection error");
                error.printStackTrace();
            }
        });
        MainActivity.requestQueue.add(request);
    }

    public void checkNetworkAvailabilty() {
        Log.d(TAG, "checkNetworkAvailability: checking network broadcasts of fragment: " + position);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();

                NetworkInfo info = (NetworkInfo) extras.getParcelable("networkInfo");
                NetworkInfo.State state = info.getState();

                if (state == NetworkInfo.State.CONNECTED) {
                    //to tasks when connection is back
                    Log.d(TAG, "onReceive: network connection is back");
                    if (!mNewsList.isEmpty()) {
                        mNewsList.clear();
                    }
                    if (identifier==0){
                        parseJson("");  //for home news fragments
                    }else if(identifier==1){
                        parseJson(Url);  //for search fragment
                    }
                } else {
                    //handle network error when connection is gone
                    Log.d(TAG, "onReceive: network connection gone");
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onItemClick(int position) {  //news list recycler items click
        Intent intent = new Intent(getActivity(), ContentActivity.class);
        NewsItem clickedItem = mNewsList.get(position);
        intent.putExtra(EXTRA_NEWS_TITLE,clickedItem.getNewsTitle());
        intent.putExtra(EXTRA_NEWS_SOURCE,clickedItem.getNewsSource());
        intent.putExtra(EXTRA_PUBLISH_TIME,clickedItem.getPublishTime());
        intent.putExtra(EXTRA_NEWS_CONTENT,clickedItem.getmContentUrl());
        intent.putExtra(EXTRA_NEWS_IMAGE,clickedItem.getImageUrl());
        intent.putExtra(EXTRA_NEWS_DESC, clickedItem.getmNewsDesc());

        startActivity(intent);

    }
}
