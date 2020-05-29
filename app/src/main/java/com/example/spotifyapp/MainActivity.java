package com.example.spotifyapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.spotifyapp.Config.AUTH_TOKEN_REQUEST_CODE;
import static com.example.spotifyapp.Config.CLIENT_ID;
import static com.example.spotifyapp.Config.REDIRECT_URI;
import static com.example.spotifyapp.Config.mAccessToken;
import static com.example.spotifyapp.Config.mOkHttpClient;

public class MainActivity extends AppCompatActivity {

    private Call mCall;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        context = this;
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
        Config.connect(true, context);
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    public void onRequestTokenClicked(View view) {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
    }


    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[]{"user-read-email", "playlist-modify-public", "playlist-modify-private", "user-library-read", "user-read-private"})
                .setCampaign("your-campaign-token")
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();
        }
    }


    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.help) {
            openTutorial();
        }
        return (super.onOptionsItemSelected(item));
    }

    public void searchSongs(View view) throws IOException {
        EditText name = findViewById(R.id.searchText);
        if (name.getText() != null) {
            URL url = new URL("https://api.spotify.com/v1/search?q=" + name.getText() + "&type=track");
            final Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + mAccessToken)
                    .build();

            cancelCall();
            mCall = mOkHttpClient.newCall(request);

            mCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        final JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray items = jsonObject.getJSONObject("tracks").getJSONArray("items");

                        Intent intent = new Intent(context, SearchActivity.class);
                        intent.putExtra("json", items.toString());
                        intent.putExtra("name", name.getText().toString());
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void openPlayer(View view) {
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }

    public void openPlaylists(View view) throws MalformedURLException {

        URL url = new URL("https://api.spotify.com/v1/me/playlists");
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray items = jsonObject.getJSONArray("items");

                    Intent intent = new Intent(context, PlaylistActivity.class);
                    intent.putExtra("playlists", items.toString());
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void openTutorial() {
        Intent browserIntent = new Intent(this, TutorialActivity.class);
        startActivity(browserIntent);
    }

}
