package com.example.spotifyapp;

import android.content.Context;

import com.example.spotifyapp.models.Playlist;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class Config {
    public static final String CLIENT_ID = "7ad00244db8347278c98e86064a113db";
    public static final String REDIRECT_URI = "spotify-sdk://auth";
    public static final int AUTH_TOKEN_REQUEST_CODE = 0x10;
    public static final int AUTH_CODE_REQUEST_CODE = 0x11;
    public static Playlist currentPlaylist;
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();
    public static String mAccessToken;
    public static String mAccessCode;
    public static String TRACK_URI;
    public static Call mCall;
    public static SpotifyAppRemote mSpotifyAppRemote;

    public static void connect(boolean showAuthView, Context context) {

        //SpotifyAppRemote.disconnect(mSpotifyAppRemote);

        SpotifyAppRemote.connect(
                context,
                new ConnectionParams.Builder(Config.CLIENT_ID)
                        .setRedirectUri(Config.REDIRECT_URI)
                        .showAuthView(showAuthView)
                        .build(),
                new Connector.ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        error.printStackTrace();
                        //PlayerActivity.this.onDisconnected();
                    }
                });
    }

    public static void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
}
