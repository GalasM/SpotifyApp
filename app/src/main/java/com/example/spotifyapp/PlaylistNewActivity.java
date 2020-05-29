package com.example.spotifyapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.example.spotifyapp.models.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.spotifyapp.Config.cancelCall;
import static com.example.spotifyapp.Config.currentPlaylist;
import static com.example.spotifyapp.Config.mAccessToken;
import static com.example.spotifyapp.Config.mCall;
import static com.example.spotifyapp.Config.mOkHttpClient;

public class PlaylistNewActivity extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_playlist_layout);
        context = this;
        if (getIntent().hasExtra("favorites")) {
            ArrayList<Track> tracks = new ArrayList<>();
            try {
                JSONArray playlists = new JSONArray(getIntent().getStringExtra("favorites"));
                for (int i = 0; i < playlists.length(); i++) {
                    JSONObject x = playlists.getJSONObject(i);
                    Track t = new Track();
                    JSONObject track = x.getJSONObject("track");
                    t.setName(track.getString("name"));
                    t.setUri(track.getString("uri"));
                    t.setArtist(track.getJSONArray("artists").getJSONObject(0).getString("name"));
                    tracks.add(t);
                }

                ListView list = (ListView) findViewById(R.id.favorite_songs);
                ArrayAdapter<Track> listAdapter = new PlaylistNewActivity.PlaylistNewAdapter(this, R.layout.new_playlist_row, tracks);
                list.setAdapter(listAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        Intent intent = new Intent(this, TutorialActivity.class);
        intent.putExtra("help", "new_playlist");
        startActivity(intent);
    }


    private class PlaylistNewAdapter extends ArrayAdapter<Track> {
        private ArrayList<Track> items;

        public PlaylistNewAdapter(Context context, int textViewResourceId, ArrayList<Track> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, final View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.new_playlist_row, null);
            }
            Track o = items.get(position);
            if (o != null) {
                TextView name = (TextView) v.findViewById(R.id.song_name_new);
                name.setText(o.getArtist()+" - "+o.getName());

                AppCompatImageButton add = v.findViewById(R.id.add_song);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            removeItemFromAdapter(o);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return v;
        }

        public void removeItemFromAdapter(Track track) throws JSONException {
            this.remove(track);
            addTrackToPlaylist(track);
        }

    }

    public void addTrackToPlaylist(Track t) throws JSONException {
        URL url = null;
        try {
            url = new URL("https://api.spotify.com/v1/playlists/" + currentPlaylist.getId() + "/tracks");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject();
        JSONArray tracks = new JSONArray();
        tracks.put(t.getUri());
        json.putOpt("uris", tracks);
        final Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString()))
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
            public void onResponse(Call call, Response response) {

            }
        });
        if(mCall.isExecuted())
            Toast.makeText(context, "Track has been added to playlist", Toast.LENGTH_SHORT).show();
    }
}
