package com.sk.quantumsudio.projectq.headline.utils;

public class NewsItem {
    private String mImageUrl,mNewsSource,mPublishTime,mNewsTitle,mContentUrl,mNewsDesc;

    public NewsItem(String ImageUrl, String NewsSource, String PublishTime, String NewsTitle, String contentUrl, String newsDesc){
        mImageUrl = ImageUrl;
        mNewsSource = NewsSource;
        mPublishTime = PublishTime;
        mNewsTitle = NewsTitle;
        mContentUrl = contentUrl;
        mNewsDesc = newsDesc;
    }
    public String getImageUrl(){
        return mImageUrl;
    }

    public String getNewsSource() {
        return mNewsSource;
    }

    public String getPublishTime() {
        return mPublishTime;
    }

    public String getNewsTitle() {
        return mNewsTitle;
    }

    public String getmContentUrl() {
        return mContentUrl;
    }

    public String getmNewsDesc(){
        return mNewsDesc;
    }
}
