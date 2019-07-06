package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.time.LocalDateTime;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

public class TweetDetailActivity extends AppCompatActivity {

    private ImageView ivProfile;
    private TextView tvUserName;
    private TextView tvScreenName;
    private TextView tvBody;
    private TextView tvTimestamp;
    private ImageButton ibRetweet;
    private ImageButton ibLike;
    private TextView tvRetweetCount;
    private TextView tvLikeCount;
    private EditText etReply;
    private Button bReply;
    private TextView tvCharCount;
    private ImageView ivMedia;

    private Tweet tweet;
    private TwitterClient client;

    private final TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            tvCharCount = findViewById(R.id.tvCharCount);
            //This sets a textview to the current length
            tvCharCount.setText(String.valueOf(280 - s.length()));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        ivProfile = findViewById(R.id.ivProfile);
        tvUserName = findViewById(R.id.tvUserName);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvBody = findViewById(R.id.tvBody);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        ibRetweet = findViewById(R.id.ibRetweet);
        ibLike = findViewById(R.id.ibLike);
        tvRetweetCount = findViewById(R.id.tvRetweetCount);
        tvLikeCount = findViewById(R.id.tvLikeCount);
        etReply = findViewById(R.id.etReply);
        bReply = findViewById(R.id.bReply);
        tvCharCount = findViewById(R.id.tvCharCount);
        ivMedia = findViewById(R.id.ivMedia);

        tweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        client = TwitterApp.getRestClient(this);

        Glide.with(this)
                .load(tweet.user.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfile);

        tvUserName.setText(tweet.user.name);
        tvScreenName.setText("@" + tweet.user.screenName);
        tvBody.setText(tweet.body);
        tvTimestamp.setText(getDetailTime(tweet.createdAt));

        if (tweet.retweeted) {
            ibRetweet.setImageResource(R.drawable.ic_vector_retweet);
        } else {
            ibRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
        }

        if (tweet.favorited) {
            ibLike.setImageResource(R.drawable.ic_vector_heart);
        } else {
            ibLike.setImageResource(R.drawable.ic_vector_heart_stroke);
        }

        tvRetweetCount.setText(String.format("%s", tweet.retweetCount));
        tvLikeCount.setText(String.format("%s", tweet.favoriteCount));
        etReply.setText("@" + tweet.user.screenName);

        if (tweet.mediaUrl != null) {
            Glide.with(this)
                    .load(tweet.mediaUrl)
                    .into(ivMedia);
        }

        ibRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterClient client = new TwitterClient(TweetDetailActivity.this);
                if (tweet.retweeted) {
                    client.unretweet(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i("TweetAdapter", "Retweet of @" + tweet.user.screenName + "'s tweet undone successfully");
                            tweet.retweeted = false;
                            tweet.retweetCount -= 1;
                            ibRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
                            tvRetweetCount.setText(String.format("%s", tweet.retweetCount));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("TweetAdapter", "Failed to undo retweet");
                            Toast.makeText(TweetDetailActivity.this, "Failed to undo retweet", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    client.retweet(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i("TweetAdapter", "Retweet of @" + tweet.user.screenName + "'s tweet sent successfully");
                            tweet.retweeted = true;
                            tweet.retweetCount += 1;
                            ibRetweet.setImageResource(R.drawable.ic_vector_retweet);
                            tvRetweetCount.setText(String.format("%s", tweet.retweetCount));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("TweetAdapter", "Failed to send retweet");
                            Toast.makeText(TweetDetailActivity.this, "Failed to send retweet", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        ibLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterClient client = new TwitterClient(TweetDetailActivity.this);
                if (tweet.favorited) {
                    client.unfavorite(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i("TweetAdapter", "Unliked @" + tweet.user.screenName + "'s tweet successfully");
                            tweet.favorited = false;
                            tweet.favoriteCount -= 1;
                            ibLike.setImageResource(R.drawable.ic_vector_heart_stroke);
                            tvLikeCount.setText(String.format("%s", tweet.favoriteCount));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("TweetAdapter", "Failed to unlike tweet");
                            Toast.makeText(TweetDetailActivity.this, "Failed to unlike tweet", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    client.favorite(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i("TweetAdapter", "Liked @" + tweet.user.screenName + "'s post successfully");
                            tweet.favorited = true;
                            tweet.favoriteCount += 1;
                            ibLike.setImageResource(R.drawable.ic_vector_heart);
                            tvLikeCount.setText(String.format("%s", tweet.favoriteCount));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("TweetAdapter", "Failed to like tweet");
                            Toast.makeText(TweetDetailActivity.this, "Failed to like tweet", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        bReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText tweetText = etReply;
                final String tweetString = tweetText.getText().toString();
                client.sendTweet(tweetString, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i("TweetDetailActivity", "Tweet with text" + tweetString + "sent successfully");
                        tweetText.setText("");
                        Intent intent = new Intent(getApplicationContext(), TimelineActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d("TweetDetailActivity", "Failed to send tweet");
                        Toast.makeText(getApplicationContext(), "Failed to send tweet", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    private String getDetailTime(String rawTimeString) {
        String detailTime;
        int hourDiff = 7; // TODO: hardcoded and shouldn't be (I think this is the difference from GMT)

        String[] timePieces = rawTimeString.split(" ");
        String[] timeNums = timePieces[3].split(":");
        int hour = Integer.parseInt(timeNums[0]) - hourDiff;
        if (hour > 12) {
            detailTime = (hour - 12) + ":" + timeNums[1] + " PM";
        }
        else if (hour < 0) {
            detailTime = (hour + 12) + ":" + timeNums[1] + " PM";
        }
        else {
            detailTime = hour + ":" + timeNums[1] + " AM";
        }
        String monthName = timePieces[1];
        int monthNum;
        switch (monthName) {
            case "Jan": monthNum = 1; break;
            case "Feb": monthNum = 2; break;
            case "Mar": monthNum = 3; break;
            case "Apr": monthNum = 4; break;
            case "May": monthNum = 5; break;
            case "Jun": monthNum = 6; break;
            case "Jul": monthNum = 7; break;
            case "Aug": monthNum = 8; break;
            case "Sep": monthNum = 9; break;
            case "Oct": monthNum = 10; break;
            case "Nov": monthNum = 11; break;
            case "Dec": monthNum = 12; break;
            default:
                monthNum = 0;
                Log.d("TweetDetailActivity", String.format("monthName was %s", monthName));
        }
        String monthDay = timePieces[2];
        if (monthDay.charAt(0) == '0') {
            monthDay = monthDay.substring(1);
        }
        String year = timePieces[5].substring(2);
        detailTime += " " + monthNum + "/" + monthDay + "/" + year;
        return detailTime;
    }
}
