package com.example.guitartrainer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

public class MediaPlayerUtils {
    public static MediaPlayer createMediaPlayerAsync(Context context, int resId){
        AssetFileDescriptor afd;
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            afd = context.getResources().openRawResourceFd(resId);

            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            afd.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();

        return mediaPlayer;
    }
}
