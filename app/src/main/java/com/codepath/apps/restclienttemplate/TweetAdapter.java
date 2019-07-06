package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private List<Tweet> mTweets;
    private Context context;

    // pass in the Tweets array in the constructor
    public TweetAdapter(List<Tweet> tweets) {
        mTweets = tweets;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View tweetView = inflater.inflate(R.layout.item_tweet, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    // bind the values based on the position of the element

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        // get the data according to position
        final Tweet tweet = mTweets.get(i);

        // populate the views according to this data
        viewHolder.tvUserName.setText(tweet.user.name);
        viewHolder.tvBody.setText(tweet.body);
        viewHolder.tvTimestamp.setText(getRelativeTimeAgo(tweet.createdAt));
        viewHolder.tvScreenName.setText("@" + tweet.user.screenName);
        viewHolder.tvRetweetCount.setText(String.format("%s", tweet.retweetCount));
        viewHolder.tvFavoriteCount.setText(String.format("%s", tweet.favoriteCount));

        if (tweet.retweeted) {
            viewHolder.ibRetweet.setImageResource(R.drawable.ic_vector_retweet);
        } else {
            viewHolder.ibRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
        }

        if (tweet.favorited) {
            viewHolder.ibLike.setImageResource(R.drawable.ic_vector_heart);
        } else {
            viewHolder.ibLike.setImageResource(R.drawable.ic_vector_heart_stroke);
        }

        Glide.with(context)
                .load(tweet.user.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(viewHolder.ivProfileImage);

        // TODO: media embedding not working as expected
        /*if (tweet.mediaUrl != null) {
            viewHolder.ivMedia.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(tweet.mediaUrl)
                    .into(viewHolder.ivMedia);
            String[] bodyParts = tweet.body.split("https");
            tweet.body = bodyParts[0];
            viewHolder.tvBody.setText(tweet.body);
        }*/

        viewHolder.ibRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterClient client = new TwitterClient(context);
                if (tweet.retweeted) {
                    client.unretweet(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i("TweetAdapter", "Retweet of @" + tweet.user.screenName + "'s tweet undone successfully");
                            tweet.retweeted = false;
                            tweet.retweetCount -= 1;
                            TweetAdapter.this.notifyItemChanged(i);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("TweetAdapter", "Failed to undo retweet");
                            Toast.makeText(context, "Failed to undo retweet", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    client.retweet(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i("TweetAdapter", "Retweet of @" + tweet.user.screenName + "'s tweet sent successfully");
                            tweet.retweeted = true;
                            tweet.retweetCount += 1;
                            TweetAdapter.this.notifyItemChanged(i);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("TweetAdapter", "Failed to send retweet");
                            Toast.makeText(context, "Failed to send retweet", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        viewHolder.ibLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterClient client = new TwitterClient(context);
                if (tweet.favorited) {
                    client.unfavorite(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i("TweetAdapter", "Unliked @" + tweet.user.screenName + "'s tweet successfully");
                            tweet.favorited = false;
                            tweet.favoriteCount -= 1;
                            TweetAdapter.this.notifyItemChanged(i);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("TweetAdapter", "Failed to unlike tweet");
                            Toast.makeText(context, "Failed to unlike tweet", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    client.favorite(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i("TweetAdapter", "Liked @" + tweet.user.screenName + "'s post successfully");
                            tweet.favorited = true;
                            tweet.favoriteCount += 1;
                            TweetAdapter.this.notifyItemChanged(i);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("TweetAdapter", "Failed to like tweet");
                            Toast.makeText(context, "Failed to like tweet", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        String tempDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            tempDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String[] parts = tempDate.split(" ");
        String timeUnit = parts[1];
        switch (timeUnit) {
            case "second": relativeDate = parts[0] + "s"; break;
            case "seconds": relativeDate = parts[0] + "s"; break;
            case "minute": relativeDate = parts[0] + "m"; break;
            case "minutes": relativeDate = parts[0] + "m"; break;
            case "hour": relativeDate = parts[0] + "h"; break;
            case "hours": relativeDate = parts[0] + "h"; break;
            case "day": relativeDate = parts[0] + "d"; break;
            case "days": relativeDate = parts[0] + "d"; break;
            default: relativeDate = tempDate;
        }
        return relativeDate;
    }

    // Clean all elements of the recycler
    public void clear() {
        mTweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        mTweets.addAll(list);
        notifyDataSetChanged();
    }

    // create ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView ivProfileImage;
        public TextView tvUserName;
        public TextView tvBody;
        public TextView tvTimestamp;
        public TextView tvScreenName;
        public ImageButton ibRetweet;
        public ImageButton ibLike;
        public TextView tvRetweetCount;
        public TextView tvFavoriteCount;
        //public ImageView ivMedia;

        public ViewHolder(View itemView) {
            super(itemView);

            // perform findViewById lookups
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            ibRetweet = itemView.findViewById(R.id.ibRetweet);
            ibLike = itemView.findViewById(R.id.ibLike);
            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCount);
            tvFavoriteCount = itemView.findViewById(R.id.tvFavoriteCount);
            //ivMedia = itemView.findViewById(R.id.ivMedia);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Tweet tweet = mTweets.get(pos);
                Intent intent = new Intent(context, TweetDetailActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }
    }

}
