package com.example.spotifyapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSeekBar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifyapp.models.Playlist;
import com.example.spotifyapp.models.Track;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.spotifyapp.Config.cancelCall;
import static com.example.spotifyapp.Config.mAccessToken;
import static com.example.spotifyapp.Config.mCall;
import static com.example.spotifyapp.Config.mOkHttpClient;
import static com.example.spotifyapp.Config.mSpotifyAppRemote;
import static com.example.spotifyapp.Config.TRACK_URI;

public class PlayerActivity extends AppCompatActivity {
    private Context context;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    TextView mSongName;
    ImageView mCoverArtImageView;
    AppCompatImageButton mPlayPauseButton;
    AppCompatSeekBar mSeekBar;

    List<View> mViews;
    TrackProgressBar mTrackProgressBar;

    Subscription<PlayerState> mPlayerStateSubscription;
    Subscription<PlayerContext> mPlayerContextSubscription;


    private final Subscription.EventCallback<PlayerContext> mPlayerContextEventCallback =
            new Subscription.EventCallback<PlayerContext>() {
                @Override
                public void onEvent(PlayerContext playerContext) {
                }
            };

    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback =
            new Subscription.EventCallback<PlayerState>() {
                @Override
                public void onEvent(PlayerState playerState) {
                    mSongName.setText(playerState.track.artist.name + " - " + playerState.track.name);
                    TRACK_URI = playerState.track.uri;
                    /*// Update progressbar
                    if (playerState.playbackSpeed > 0) {
                        mTrackProgressBar.unpause();
                    } else {
                        mTrackProgressBar.pause();
                    }*/

                    if (playerState.isPaused) {
                        mPlayPauseButton.setImageResource(R.drawable.btn_play);
                    } else {
                        mPlayPauseButton.setImageResource(R.drawable.btn_pause);
                    }

                    mSpotifyAppRemote
                            .getImagesApi()
                            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                            .setResultCallback(
                                    bitmap -> {
                                        mCoverArtImageView.setImageBitmap(bitmap);

                                    });
                   /* mSeekBar.setMax((int) playerState.track.duration);
                    mTrackProgressBar.setDuration(playerState.track.duration);
                    mTrackProgressBar.update(playerState.playbackPosition);

                    mSeekBar.setEnabled(true);*/
                }
            };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);
        context = this;
        mCoverArtImageView = findViewById(R.id.image);
        mPlayPauseButton = findViewById(R.id.play_pause_button);
        mSongName = findViewById(R.id.song_name);
        /*mSeekBar = findViewById(R.id.seek_to);
        mSeekBar.setEnabled(false);
        mSeekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mSeekBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);*/

        /*mTrackProgressBar = new TrackProgressBar(mSeekBar);*/

        mViews =
                Arrays.asList(
                        mPlayPauseButton,
                        findViewById(R.id.skip_prev_button),
                        findViewById(R.id.skip_next_button)
                        // mSeekBar
                );

        SpotifyAppRemote.setDebugMode(true);

        //onDisconnected();
        connect(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        //onDisconnected();
    }

    private void onConnected() {
        for (View input : mViews) {
            input.setEnabled(true);
        }

        onSubscribedToPlayerStateButtonClicked(null);
        onSubscribedToPlayerContextButtonClicked(null);
    }

    private void onDisconnected() {
        for (View view : mViews) {
            view.setEnabled(false);
        }
        mCoverArtImageView.setImageResource(R.drawable.widget_placeholder);
    }


    private void connect(boolean showAuthView) {

        //SpotifyAppRemote.disconnect(mSpotifyAppRemote);

        SpotifyAppRemote.connect(
                getApplication(),
                new ConnectionParams.Builder(Config.CLIENT_ID)
                        .setRedirectUri(Config.REDIRECT_URI)
                        .showAuthView(showAuthView)
                        .build(),
                new Connector.ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        PlayerActivity.this.onConnected();
                        myConnect();
                        System.out.println("myconnect");
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        error.printStackTrace();
                        //PlayerActivity.this.onDisconnected();
                    }
                });
    }


    public void onPlayTrackButtonClicked(View view) {
        playUri(TRACK_URI);
    }


    private void playUri(String uri) {
        mSpotifyAppRemote
                .getPlayerApi()
                .play(uri);
    }

    public void showCurrentPlayerContext(View view) {
        if (view.getTag() != null) {
            showDialog("PlayerContext", gson.toJson(view.getTag()));
        }
    }

    public void showCurrentPlayerState(View view) {
        if (view.getTag() != null) {
            showDialog("PlayerState", gson.toJson(view.getTag()));
        }
    }


    public void onSkipPreviousButtonClicked(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .skipPrevious();
    }

    public void onPlayPauseButtonClicked(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .getPlayerState()
                .setResultCallback(
                        playerState -> {
                            if (playerState.isPaused) {
                                mSpotifyAppRemote
                                        .getPlayerApi()
                                        .resume();
                            } else {
                                mSpotifyAppRemote
                                        .getPlayerApi()
                                        .pause();
                            }
                        });
    }

    public void onSkipNextButtonClicked(View view) {
        mSpotifyAppRemote
                .getPlayerApi()
                .skipNext();
    }

    public void onRemoveUriClicked(View view) {
        mSpotifyAppRemote
                .getUserApi()
                .removeFromLibrary(TRACK_URI);

    }

    public void onSaveUriClicked(View view) {
        mSpotifyAppRemote
                .getUserApi()
                .addToLibrary(TRACK_URI);
        Toast.makeText(context, "Track has been added to favotites", Toast.LENGTH_SHORT).show();
    }


    public void onSubscribedToPlayerContextButtonClicked(View view) {
        if (mPlayerContextSubscription != null && !mPlayerContextSubscription.isCanceled()) {
            mPlayerContextSubscription.cancel();
            mPlayerContextSubscription = null;
        }


        mPlayerContextSubscription =
                (Subscription<PlayerContext>)
                        mSpotifyAppRemote
                                .getPlayerApi()
                                .subscribeToPlayerContext()
                                .setEventCallback(mPlayerContextEventCallback);
    }

    public void onSubscribedToPlayerStateButtonClicked(View view) {

        if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {
            mPlayerStateSubscription.cancel();
            mPlayerStateSubscription = null;
        }

        mPlayerStateSubscription =
                (Subscription<PlayerState>)
                        mSpotifyAppRemote
                                .getPlayerApi()
                                .subscribeToPlayerState()
                                .setEventCallback(mPlayerStateEventCallback)
                                .setLifecycleCallback(
                                        new Subscription.LifecycleCallback() {
                                            @Override
                                            public void onStart() {

                                            }

                                            @Override
                                            public void onStop() {

                                            }
                                        });
    }


    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).create().show();
    }


    private class TrackProgressBar {

        private static final int LOOP_DURATION = 500;
        private final SeekBar mSeekBar;
        private final Handler mHandler;

        private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener =
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mSpotifyAppRemote
                                .getPlayerApi()
                                .seekTo(seekBar.getProgress());
                    }
                };

        private final Runnable mSeekRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        int progress = mSeekBar.getProgress();
                        mSeekBar.setProgress(progress + LOOP_DURATION);
                        mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
                    }
                };

        private TrackProgressBar(SeekBar seekBar) {
            mSeekBar = seekBar;
            mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
            mHandler = new Handler();
        }

        private void setDuration(long duration) {
            mSeekBar.setMax((int) duration);
        }

        private void update(long progress) {
            mSeekBar.setProgress((int) progress);
        }

        private void pause() {
            mHandler.removeCallbacks(mSeekRunnable);
        }

        private void unpause() {
            mHandler.removeCallbacks(mSeekRunnable);
            mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
        }
    }

    private void myConnect() {
        if (getIntent().hasExtra("track")) {
            Gson json = new Gson();
            Track track = json.fromJson(getIntent().getStringExtra("track"), Track.class);

            //mSongName = findViewById(R.id.song_name);
            //mSongName.setText(track.getArtist() + " - " + track.getName());
            TRACK_URI = track.getUri();
            onSubscribedToPlayerStateButtonClicked(null);
            onSubscribedToPlayerContextButtonClicked(null);
            playUri(track.getUri());
        } else if (getIntent().hasExtra("playlist")) {
            Gson json = new Gson();
            Playlist playlist = json.fromJson(getIntent().getStringExtra("playlist"), Playlist.class);
            onSubscribedToPlayerStateButtonClicked(null);
            onSubscribedToPlayerContextButtonClicked(null);
            playUri(playlist.getUri());
        }
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

    public void openTutorial() {

        Intent browserIntent = new Intent(this, TutorialActivity.class);
        startActivity(browserIntent);
    }

    public void addToPlaylist(View view) throws MalformedURLException {
        ArrayList<Playlist> list = new ArrayList<>();
        URL url = new URL("https://api.spotify.com/v1/me/playlists");
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        //cancelCall();
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
                    JSONArray playlists = jsonObject.getJSONArray("items");
                    Intent intent = new Intent(context, ListPlaylistActivity.class);
                    intent.putExtra("playlists", playlists.toString());
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}