package com.example.spotifyapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spotifyapp.models.Playlist;

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

import static com.example.spotifyapp.Config.TRACK_URI;
import static com.example.spotifyapp.Config.mAccessToken;
import static com.example.spotifyapp.Config.mCall;
import static com.example.spotifyapp.Config.mOkHttpClient;


public class ListPlaylistActivity extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_playlist_layout);
        context = this;
        ArrayList<Playlist> list = new ArrayList<>();
        if (getIntent().hasExtra("playlists")) {
            try {
                JSONArray playlists = new JSONArray(getIntent().getStringExtra("playlists"));
                for (int i = 0; i < playlists.length(); i++) {
                    JSONObject x = playlists.getJSONObject(i);
                    Playlist p = new Playlist(x.getString("name"));
                    p.setUri(x.getString("uri"));
                    p.setId(x.getString("id"));
                    list.add(p);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ListView listView = (ListView) findViewById(R.id.playlists);
        ArrayAdapter<Playlist> listAdapter = new ListPlaylistActivity.PlaylistAdapter(this, R.layout.new_playlist_row, list);
        listView.setAdapter(listAdapter);
    }


    private class PlaylistAdapter extends ArrayAdapter<Playlist> {

        private ArrayList<Playlist> items;

        public PlaylistAdapter(Context context, int textViewResourceId, ArrayList<Playlist> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_playlist_row, null);
            }
            Playlist o = items.get(position);
            if (o != null) {
                final Button name = (Button) v.findViewById(R.id.playlist_name);
                name.setText(o.getName());
                name.setOnClickListener(v1 -> {
                    try {
                        addTrackToPlaylist(o);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                });
            }
            return v;
        }


        public void addTrackToPlaylist(Playlist p) throws JSONException {
            URL url = null;
            try {
                url = new URL("https://api.spotify.com/v1/playlists/" + p.getId() + "/tracks");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            JSONObject json = new JSONObject();
            JSONArray tracks = new JSONArray();
            tracks.put(TRACK_URI);
            json.putOpt("uris", tracks);
            final Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString()))
                    .addHeader("Authorization", "Bearer " + mAccessToken)
                    .build();

            // cancelCall();
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
            Toast.makeText(context, "Track has been added to playlist", Toast.LENGTH_SHORT).show();
        }
    }
}
