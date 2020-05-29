package com.example.spotifyapp.utlis;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.spotifyapp.PlaylistActivity;
import com.example.spotifyapp.PlaylistNewActivity;
import com.example.spotifyapp.R;
import com.example.spotifyapp.interfaces.DialogCallback;
import com.example.spotifyapp.interfaces.FiltrPlaylistCallback;
import com.example.spotifyapp.interfaces.NamePlaylistCallback;
import com.example.spotifyapp.widgets.CustomDialog;


public class GlobalUtils {

    public static void showDialog(Context context, final DialogCallback dialogCallback){
        final CustomDialog dialog = new CustomDialog(context, R.style.CustomDialogTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v =inflater.inflate(R.layout.dialog_layout,null);

        dialog.setContentView(v);

        Button btn_done = (Button)dialog.findViewById(R.id.btn_done);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogCallback != null) {
                    dialogCallback.callback(0);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public static void showNewPlaylistDialog(Context context, final NamePlaylistCallback dialogCallback){
        final CustomDialog dialog = new CustomDialog(context, R.style.CustomDialogTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v =inflater.inflate(R.layout.dialog_new_playlist_layout,null);
        dialog.setContentView(v);

        Button btn_done = (Button)dialog.findViewById(R.id.btn_done);

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogCallback != null) {
                    EditText name = dialog.findViewById(R.id.name_new_playlist);
                    dialogCallback.callbackPlaylist(name.getText().toString());
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public static void showFiltrDialog(Context context, final FiltrPlaylistCallback dialogCallback){
        final CustomDialog dialog = new CustomDialog(context, R.style.CustomDialogTheme);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v =inflater.inflate(R.layout.dialog_filtr,null);
        dialog.setContentView(v);

        Button btn_name = (Button)dialog.findViewById(R.id.sort_name);
        Button btn_max = (Button)dialog.findViewById(R.id.sort_size_max);
        Button btn_min = (Button)dialog.findViewById(R.id.sort_size_min);
        Button btn_date = (Button)dialog.findViewById(R.id.sort_date);

        btn_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogCallback != null) {
                    dialogCallback.callbackFiltr("name");
                    dialog.dismiss();
                }
            }
        });

        btn_max.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogCallback != null) {
                    dialogCallback.callbackFiltr("max");
                    dialog.dismiss();
                }
            }
        });

        btn_min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogCallback != null) {
                    dialogCallback.callbackFiltr("min");
                    dialog.dismiss();
                }
            }
        });
        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogCallback != null) {
                    dialogCallback.callbackFiltr("date");
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }
}
