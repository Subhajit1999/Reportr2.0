package com.sk.quantumsudio.projectq.headline.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.quantumsudio.projectq.headline.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.NewsViewHolder> {
    private static final String TAG = "NewsRecyclerAdapter";
    private Context mContext;
    private ArrayList<NewsItem> mNewsItemsList;
    private OnItemClickListener mListener;
    private int mId;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public NewsRecyclerAdapter(Context context, ArrayList<NewsItem> newsItemsList,int id){
        Log.d(TAG, "NewsRecyclerAdapter: constructor");
        mContext = context;
        mNewsItemsList = newsItemsList;
        mId = id;     //0 for home fragments and 1 for bookmark fragment
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.d(TAG, "onCreateViewHolder: Element view of recycler list");
        View v;
        if (mId==0){
            v = LayoutInflater.from(mContext).inflate(R.layout.layout_newslist_element,viewGroup,false);
        }else{
            v = LayoutInflater.from(mContext).inflate(R.layout.layout_bookmarklist_element,viewGroup,false);
        }
        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, final int i) {
        Log.d(TAG, "onBindViewHolder: Setting up the values to the views");
        NewsItem currentItem = mNewsItemsList.get(i);  //stores the current item position

        String newsImageUrl = currentItem.getImageUrl();  //gets the value of respective fields
        String newsSource = currentItem.getNewsSource();
        String newsTime = currentItem.getPublishTime();
        String newsTitle = currentItem.getNewsTitle();

        if (mId==0){  //when in mainFragment
            //sets the values to the respective views
            holder.mNewsSource.setText(newsSource);
            holder.mNewsTitle.setText(newsTitle);

            try {           //tries to reformat the json date format into the familiar one
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(newsTime);
                if (printStandardDate(date) != null) {
                    holder.mNewsTime.setText(printStandardDate(date));
                } else {
                    holder.mNewsTime.setText("");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(newsImageUrl != null && newsImageUrl.length()>5){
                Picasso.with(mContext)
                        .load(newsImageUrl)
                        .placeholder(R.drawable.headlines)
                        .error(R.drawable.image_error)
                        .fit().centerInside()
                        .into(holder.mNewsImage);
            }else{
                holder.mNewsImage.setImageResource(R.drawable.image_error);
                holder.mNewsImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        }else{  //when in bookmarkFragment
            holder.mNewsSource.setText(newsSource);
            holder.mNewsTitle.setText(newsTitle);
        }
    }
    public static String printStandardDate(Date date) {
        return DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT).format(date);
    }

    @Override
    public int getItemCount() {
        return mNewsItemsList.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder{
        private ImageView mNewsImage;
        private TextView mNewsSource,mNewsTime,mNewsTitle;

        private NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            //initialize the views
            mNewsImage = itemView.findViewById(R.id.news_image);
            mNewsSource = itemView.findViewById(R.id.news_source);
            mNewsTime = itemView.findViewById(R.id.news_time);
            mNewsTitle = itemView.findViewById(R.id.news_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
            if(mId != 0){  //delete button click of bookmarkFragment
                mNewsImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mNewsItemsList.remove(getAdapterPosition());
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }
}
