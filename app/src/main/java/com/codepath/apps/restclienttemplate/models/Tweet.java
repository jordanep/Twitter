package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Tweet {

    // list out the attributes
    public String body;
    public long uid; // database ID for the tweet
    public User user;
    public String createdAt;
    public boolean retweeted;
    public boolean favorited;
    public int retweetCount;
    public int favoriteCount;
    public String mediaUrl;

    public Tweet() {

    }

    // deserialize the JSON
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        // extract the values from the JSON
        tweet.body = jsonObject.getString("text");
        tweet.uid = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.favoriteCount = jsonObject.getInt("favorite_count");

        JSONObject entities = jsonObject.getJSONObject("entities");
        JSONArray media = entities.getJSONArray("media");
        if (media != null) {
            tweet.mediaUrl = media.getString(6);
        }

        return tweet;
    }

}
