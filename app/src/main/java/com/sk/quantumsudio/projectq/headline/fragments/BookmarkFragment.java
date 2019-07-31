package com.sk.quantumsudio.projectq.headline.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sk.quantumsudio.projectq.headline.Activities.ContentActivity;
import com.sk.quantumsudio.projectq.headline.Activities.MainActivity;
import com.sk.quantumsudio.projectq.headline.R;
import com.sk.quantumsudio.projectq.headline.utils.NewsItem;
import com.sk.quantumsudio.projectq.headline.utils.NewsRecyclerAdapter;
import com.sk.quantumsudio.projectq.headline.utils.Preferences;

public class BookmarkFragment extends Fragment implements NewsRecyclerAdapter.OnItemClickListener{
    private static final String TAG = "BookmarkFragment";

    RecyclerView mRecyclerView;
    NewsRecyclerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: layout view just got created");
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: setting up the layout defaults");
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.recycler_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NewsRecyclerAdapter(getContext(), MainActivity.savedNewsList,1);  //adapter attached
        mRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(BookmarkFragment.this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: gets called.");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: gets called.");
        super.onPause();
        saveBookmarkedData();
    }

    @Override
    public void onItemClick(int position) {  //news list recycler items click
        Log.d(TAG, "onItemClick: launching bookmarked news of index: "+position);
        Intent intent = new Intent(getActivity(), ContentActivity.class);
        NewsItem clickedItem = MainActivity.savedNewsList.get(position);
        intent.putExtra(MainFragment.EXTRA_NEWS_TITLE,clickedItem.getNewsTitle());
        intent.putExtra(MainFragment.EXTRA_NEWS_SOURCE,clickedItem.getNewsSource());
        intent.putExtra(MainFragment.EXTRA_PUBLISH_TIME,clickedItem.getPublishTime());
        intent.putExtra(MainFragment.EXTRA_NEWS_CONTENT,clickedItem.getmContentUrl());
        intent.putExtra(MainFragment.EXTRA_NEWS_IMAGE,clickedItem.getImageUrl());
        intent.putExtra(MainFragment.EXTRA_NEWS_DESC, clickedItem.getmNewsDesc());

        startActivity(intent);

    }

    public void saveBookmarkedData(){
        Log.d(TAG, "saveData: Saving bookmarked data into preferences.List length: "+MainActivity.savedNewsList.size());
        SharedPreferences preferences = getActivity().getSharedPreferences(Preferences.KEY_PREFERENCES, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.savedNewsList);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Preferences.KEY_BOOKMARK_LIST,json );
        editor.apply();
    }
}
