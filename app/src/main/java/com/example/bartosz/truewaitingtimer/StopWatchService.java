package com.example.bartosz.truewaitingtimer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

import java.io.IOException;

/**
 * Created by Bartosz on 8/11/2016.
 */
public class StopWatchService extends Service implements MediaPlayer.OnPreparedListener {


    final int delay = 10;
    final int NOTIFICATION_ID = 34219;

    MediaPlayer mediaPlayer= null;
    Handler h = null;

    int counter = 0;

    public boolean bActive = false;
    int state = 0;

    public SpannableString sDisp = new SpannableString("00h00m00s");

    long startTime = 0;
    long offset1 = 0;
    long offset2 = 0;

    private boolean bMute;

    private Runnable TimerRunnable = new Runnable() {
        @Override
        public void run() {
            //if (mService != null) timeTextView.setText(mService.getTimeString());

            Intent i = new Intent(StopWatchService.this, MainActivity.class);

            SpannableString nTime = getTimeString();
            boolean temp = true;
            if(counter >= 400) temp = !temp;

            int icon = R.drawable.ic_watch_black_24dp;

            if(temp){
                icon = R.drawable.ic_watch5_black_24dp;
            }

            NotificationCompat.Builder builder =  new NotificationCompat.Builder(StopWatchService.this)
                    .setSmallIcon(icon)
                    .setContentTitle(nTime)
                    .setTicker("Waiting Timer Notification")
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(false)
                    .setContentIntent(PendingIntent.getActivity(StopWatchService.this, 0, i, 0));


            startForeground(NOTIFICATION_ID, builder.build());
            counter++;
            if(h != null)h.postDelayed(this, delay);
        }
    };

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        StopWatchService getService() {
            // Return this instance of LocalService so clients can call public methods
            return StopWatchService.this;
        }
    }
    public void onCreate() {
        super.onCreate();
    }

    public void StartMusic()
    {
        bActive = true;
        state = 1;
        sDisp = new SpannableString("00h00m00s");
        sDisp.setSpan(new RelativeSizeSpan(0.2f), 2,2, 0); // set size
        sDisp.setSpan(new RelativeSizeSpan(0.2f), 5,5, 0); // set size
        sDisp.setSpan(new RelativeSizeSpan(0.2f), 8,8, 0); // set size

        offset1 = 0;
        offset2 = 0;

        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        AssetFileDescriptor afd = null;
        try {
            afd = getAssets().openFd("miningbyMoonlight.mp3");
            mediaPlayer = new MediaPlayer();
            updateMute();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        startTime = System.currentTimeMillis();
    }
    public void onPrepared(MediaPlayer player) {
        player.start();

        final String CUSTOM_ACTION = "FROMNOTIFICATION";
        Intent i = new Intent(this, MainActivity.class);

        h = new Handler();
        //milliseconds
        h.postDelayed(TimerRunnable, delay);
    }
    public void StopMusic() {
        bActive = true;
        state = 2;
        if(mediaPlayer != null) {
            mediaPlayer.pause();
        }
        if(h != null){
            h.removeCallbacks(TimerRunnable);
        }

        stopForeground(true);

    }
    public void ResumeMusic() {
        bActive = true;
        offset2 = offset1;
        startTime = System.currentTimeMillis();

        if(mediaPlayer != null) {
            updateMute();
            mediaPlayer.start();
        }

        h = new Handler();
        //milliseconds
        h.postDelayed(TimerRunnable, delay);
        state = 1;

    }



    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void onDestroy(){
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        NotificationManager notificationManager= (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        super.onDestroy();
    }
    private String convert(int v){
        String result;

        if( v < 10) {
            result = "0" + v;
        }
        else{
            result = "" + v;
        }

        return result;
    }
    public SpannableString getTimeString() {
        if(bActive == false) {
            return sDisp;
        }

        long time= System.currentTimeMillis();
        offset1 = time - startTime + offset2;

        int seconds = (int) offset1 / 1000;
        int minutes = seconds / 60;
        int hours =  minutes / 60;

        minutes = minutes - hours * 60;
        seconds = seconds - minutes * 60 - hours * 60 * 60;

        String sSec = convert(seconds);
        String sMin = convert(minutes);
        String sHour = convert(hours);

        sDisp = new SpannableString(sHour + "h" + sMin  + "m" + sSec +"s");

        sDisp.setSpan(new RelativeSizeSpan(0.2f), 2,2, 0); // set size
        sDisp.setSpan(new RelativeSizeSpan(0.2f), 5,5, 0); // set size
        sDisp.setSpan(new RelativeSizeSpan(0.2f), 8,8, 0); // set size

        return sDisp;
    }
    public void setMute(boolean bM){
        bMute = bM;
        updateMute();
    }
    public boolean getMute(){
        return bMute;
    }
    public void updateMute(){
        if(mediaPlayer != null) {
            if (bMute) {
                mediaPlayer.setVolume(0, 0);

            } else {
                mediaPlayer.setVolume(1, 1);
            }

        }

    }
}


