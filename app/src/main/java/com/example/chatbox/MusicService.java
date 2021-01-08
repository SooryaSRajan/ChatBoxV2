package com.example.chatbox;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;

public class MusicService extends Service {

    private static final String TAG = "Service";
    private final IBinder binder = new LocalBinder();
    private String audioUri = null;
    private MediaPlayer mediaPlayer;
    private int stateOfAudio = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        MusicService getLocalService(){
            return MusicService.this;
        }
    }

    public void setAudioPath(String mUri){
        audioUri = mUri;
    }

    public void preparePlayer(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer =null;
        }
        Log.e(TAG, "preparePlayer: " + audioUri );
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }

    public void playAudio(){
        Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show();
        mediaPlayer.start();
    }

    public void pauseAudio(){
        Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
        mediaPlayer.pause();
    }

    public int getMaxProgress(){
        if(mediaPlayer!=null)
            return mediaPlayer.getDuration();
        else
            return 0;
    }

    public int getProgressOfAudio(){
        if(mediaPlayer != null)
            return mediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    public void setProgress(int progress){
        if(mediaPlayer != null)
            mediaPlayer.seekTo(progress);
    }

    public String getAudioPath(){
        return audioUri;
    }
}
