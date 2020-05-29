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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;


import com.example.spotifyapp.interfaces.DialogCallback;
import com.example.spotifyapp.models.Playlist;
import com.example.spotifyapp.models.Track;
import com.example.spotifyapp.utlis.GlobalUtils;
import com.google.gson.Gson;

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
import static com.example.spotifyapp.Config.mAccessToken;
import static com.example.spotifyapp.Config.mCall;
import static com.example.spotifyapp.Config.currentPlaylist;
import static com.example.spotifyapp.Config.mOkHttpClient;

public class PlaylistEditActivity extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_playlist_layout);
        context = this;
        ListView list = (ListView) findViewById(R.id.playlists_edit);
        if (getIntent().hasExtra("playlist_name")) {
            TextView name = (TextView) findViewById(R.id.edit_name);
            name.setText("Edited: " + getIntent().getStringExtra("playlist_name"));
        }

            if(getIntent().hasExtra("tracks")){
                try {
                    ArrayList<Track> arrayList = new ArrayList<>();
                    JSONArray items = new JSONArray(getIntent().getStringExtra("tracks"));
                for (int i = 0; i < items.length(); i++) {
                    JSONObject x = items.getJSONObject(i);
                    Track t = new Track();
                    JSONObject track = x.getJSONObject("track");
                    t.setName(track.getString("name"));
                    t.setUri(track.getString("uri"));
                    t.setArtist(track.getJSONArray("artists").getJSONObject(0).getString("name"));
                    arrayList.add(t);
                }
                    ArrayAdapter<Track> listAdapter = new PlaylistEditActivity.PlaylistEditAdapter(context, R.layout.playlist_edit_row, arrayList);
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
        intent.putExtra("help", "edit_playlist");
        startActivity(intent);
    }

    public void changeName(View view) {
        TextView text = findViewById(R.id.edit_name);
        EditText newName = findViewById(R.id.inputName);
        text.setText("Edited: " + newName.getText().toString());
        Toast.makeText(this, "Name has been changed", Toast.LENGTH_SHORT).show();
        try {
            changeNamePlaylist(newName.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showDialog(View view) {
        GlobalUtils.showDialog(this, new DialogCallback() {
            @Override
            public void callback(int ratings) {
            }
        });
    }

    private class PlaylistEditAdapter extends ArrayAdapter<Track> {

        private ArrayList<Track> items;

        public PlaylistEditAdapter(Context context, int textViewResourceId, ArrayList<Track> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.playlist_edit_row, null);
            }
            final Track o = items.get(position);
            if (o != null) {
                TextView name = (TextView) v.findViewById(R.id.song_name_edit);
                name.setText(o.getArtist() + " - " + o.getName());
                AppCompatImageButton delete = v.findViewById(R.id.remove_song);
                delete.setOnClickListener(new View.OnClickListener() {
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
            removeTrackFromPlaylist(track);
        }
    }

    public void removeTrackFromPlaylist(Track track) throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray tracks = new JSONArray();
        JSONObject uri = new JSONObject();
        uri.putOpt("uri", track.getUri());
        tracks.put(uri);
        json.putOpt("tracks", tracks);


        URL url = null;
        try {

            url = new URL("https://api.spotify.com/v1/playlists/" + currentPlaylist.getId() + "/tracks");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        final Request request = new Request.Builder()
                .url(url)
                .delete(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString()))
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
            }
        });
    }

    public void changeNamePlaylist(String name) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);

        URL url = null;
        try {

            url = new URL("https://api.spotify.com/v1/playlists/" + currentPlaylist.getId());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        final Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString()))
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
            }
        });
    }
}
