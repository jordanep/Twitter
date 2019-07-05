package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {

    EditText etCompose;
    Button sendButton;
    TextView tvCharCount;
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

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sendButton = findViewById(R.id.sendButton);
        tvCharCount = findViewById(R.id.tvCharCount);

        client = TwitterApp.getRestClient(this.getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        etCompose = findViewById(R.id.etCompose);
        etCompose.addTextChangedListener(textWatcher);
    }



    public void finishCompose(View view) {
        final EditText tweetText = findViewById(R.id.etCompose);
        final String tweetString = tweetText.getText().toString();
        client.sendTweet(tweetString, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("ComposeActivity", "Tweet with text" + tweetString + "sent successfully");
                tweetText.setText("");
                Intent intent = new Intent(getApplicationContext(), TimelineActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("ComposeActivity", "Failed to send tweet");
                Toast.makeText(getApplicationContext(), "Failed to send tweet", Toast.LENGTH_LONG).show();
            }
        });
    }
}
