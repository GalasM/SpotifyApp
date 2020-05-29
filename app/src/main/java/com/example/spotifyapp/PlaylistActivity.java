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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.example.spotifyapp.interfaces.FiltrPlaylistCallback;
import com.example.spotifyapp.interfaces.NamePlaylistCallback;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.spotifyapp.Config.cancelCall;
import static com.example.spotifyapp.Config.connect;
import static com.example.spotifyapp.Config.currentPlaylist;
import static com.example.spotifyapp.Config.mAccessToken;
import static com.example.spotifyapp.Config.mCall;
import static com.example.spotifyapp.Config.mOkHttpClient;

public class PlaylistActivity extends AppCompatActivity {
    Context context;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_layout);
        context = this;
        ListView list = (ListView) findViewById(R.id.playlists);
        ArrayList<Playlist> playlists = getPlaylists();
        ArrayAdapter<Playlist> listAdapter = new PlaylistAdapter(this, R.layout.search_row, playlists);
        if(getIntent().hasExtra("filtr"))
            switch (getIntent().getStringExtra("filtr")){
                case "name":
                    playlists.sort(Comparator.comparing(Playlist::getName));
                    listAdapter.sort(Comparator.comparing(Playlist::getName));
                    break;
                case "min":
                    playlists.sort(Comparator.comparing(Playlist::getSize));
                    listAdapter.sort(Comparator.comparing(Playlist::getSize));
                    break;
                case "max":
                    playlists.sort(Comparator.comparing(Playlist::getSize).reversed());
                    listAdapter.sort(Comparator.comparing(Playlist::getSize).reversed());
                    break;
                case "date":
                    break;
        }

        list.setAdapter(listAdapter);

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
        intent.putExtra("help", "playlist");
        startActivity(intent);
    }

    public ArrayList<Playlist> getPlaylists() {
        ArrayList<Playlist> list = new ArrayList<>();
        if (getIntent().hasExtra("playlists")) {
            try {
                JSONArray playlists = new JSONArray(getIntent().getStringExtra("playlists"));
                for (int i = 0; i < playlists.length(); i++) {
                    JSONObject x = playlists.getJSONObject(i);
                    Playlist p = new Playlist(x.getString("name").toUpperCase());
                    p.setUri(x.getString("uri"));
                    p.setId(x.getString("id"));
                    p.setSize(x.getJSONObject("tracks").getLong("total"));
                    list.add(p);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    public class PlaylistAdapter extends ArrayAdapter<Playlist> {

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
                v = vi.inflate(R.layout.playlist_row, null);
            }
            Playlist o = items.get(position);
            if (o != null) {
                final Button name = (Button) v.findViewById(R.id.playlist_name);
                name.setText(o.getName());
                name.setOnClickListener(v1 -> {
                    Intent intent = new Intent(context, PlayerActivity.class);
                    Gson json = new Gson();
                    String j = json.toJson(o);
                    intent.putExtra("playlist", j);
                    startActivity(intent);
                });
                AppCompatImageButton edit = (AppCompatImageButton) v.findViewById(R.id.edit_playlist);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToEditPlaylist(o);
                    }
                });

                AppCompatImageButton remove = (AppCompatImageButton) v.findViewById(R.id.remove_playlist);
                remove.setOnClickListener(new View.OnClickListener() {
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

        public void removeItemFromAdapter(Playlist playlist) throws JSONException {
            this.remove(playlist);
            removePlaylist(playlist.getId());
        }

        public void goToEditPlaylist(Playlist playlist){
            URL url = null;
            try {
                url = new URL("https://api.spotify.com/v1/playlists/" + playlist.getId() + "/tracks");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
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
                        JSONArray items = jsonObject.getJSONArray("items");
System.out.println("asd "+items.length());
                        Intent intent = new Intent(context, PlaylistEditActivity.class);
                        intent.putExtra("tracks", items.toString());
                        intent.putExtra("playlist_name", playlist.getName());
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void showDialog(View view) {
        GlobalUtils.showNewPlaylistDialog(this, new NamePlaylistCallback() {
            @Override
            public void callbackPlaylist(String name) {
                try {
                    Toast.makeText(context, "Playlist has been added to playlist", Toast.LENGTH_SHORT).show();
                    getUserId(name);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                goToNewPlaylist();
            }
        });
    }

    public void goToNewPlaylist() {
        URL url = null;
        try {
            url = new URL("https://api.spotify.com/v1/me/tracks");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
                    JSONArray items = jsonObject.getJSONArray("items");
                    Intent intent = new Intent(context, PlaylistNewActivity.class);
                    intent.putExtra("favorites", items.toString());
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void removePlaylist(String id) {
        URL url = null;
        try {

            url = new URL("https://api.spotify.com/v1/playlists/" + id + "/followers");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        final Request request = new Request.Builder()
                .url(url)
                .delete()
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

    public void getUserId(String name) throws MalformedURLException {
        URL url = null;
        connect(true, this);

        url = new URL("https://api.spotify.com/v1/me");

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
                    id = jsonObject.getString("id");
                    addNewPlaylist(id, name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void addNewPlaylist(String id, String name) throws MalformedURLException, JSONException {
        URL url = null;
        String userId = id;
        JSONObject json = new JSONObject();
        json.put("name", name);
        try {
            url = new URL("https://api.spotify.com/v1/users/" + userId + "/playlists");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        final Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString()))
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
                    final JSONObject jsonObject;
                    jsonObject = new JSONObject(response.body().string());
                    String uri = jsonObject.getString("uri");
                    String id = jsonObject.getString("id");
                    currentPlaylist = new Playlist(id, name, uri);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showFiltrs(View view) {
        GlobalUtils.showFiltrDialog(this, new FiltrPlaylistCallback() {

            @Override
            public void callbackFiltr(String type) {
                try {
                    openPlaylists(type);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void openPlaylists(String type) throws MalformedURLException {

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
                    intent.putExtra("filtr", type);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
