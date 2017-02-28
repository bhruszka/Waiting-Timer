package com.example.bartosz.truewaitingtimer;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    StopWatchService mService;
    boolean mBound = false;
    boolean bState = false;

    TextView timeTextView;

    Boolean bActive;
    Button MainButton;
    Button Button3;
    Button Button2;
    ImageButton ImageButtonMute;

    TextView textView2;
    Handler h = null;
    final int delay = 10;
    private Runnable TimerRunnable = new Runnable() {
        @Override
        public void run() {
            if(mService != null) timeTextView.setText(mService.sDisp);
            else timeTextView.setText("00:00:00");
            if(h != null)h.postDelayed(this, delay);
        }
    };

    class ResumeOnClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            mService.ResumeMusic();
            startTimer();

            updateLooks(getResources().getConfiguration().orientation);
        }
    }

    class SendOnClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {

            if(mService != null) {
                String time = new String();
                if(mService.state == 2){
                    time = time + mService.sDisp +". Hurry up!";
                }
                else{
                    time = "I've waited for " + mService.sDisp + ". I'm not waiting any longer.";
                }

                Intent intentsms = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
                intentsms.putExtra("sms_body", time);
                startActivity(intentsms);
            }
            else{
                Context context = getApplicationContext();
                CharSequence text = "Something went wrong, sorry";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("boo","boo2");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainButton = (Button)findViewById(R.id.button);
        Button2 = (Button) findViewById(R.id.sendbutton);
        Button3 = (Button)findViewById(R.id.resumebutton);
        ImageButtonMute = (ImageButton) findViewById(R.id.imageButtonMute);

        timeTextView = (TextView) findViewById(R.id.textView);
        SpannableString s= new SpannableString("00h00m00s");

        s.setSpan(new RelativeSizeSpan(0.2f), 2,2, 0); // set size
        s.setSpan(new RelativeSizeSpan(0.2f), 5,5, 0); // set size
        s.setSpan(new RelativeSizeSpan(0.2f), 8,8, 0); // set size
        timeTextView.setText(s);
        textView2 = (TextView) findViewById(R.id.textView2);
        TextView aboutMusicText = (TextView)findViewById(R.id.aboutMusicText);

        updateLooks(getResources().getConfiguration().orientation);

        Intent intent = new Intent(this, StopWatchService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startTimer();



        if(MainButton != null)
        MainButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mService == null) {
                    Context context = getApplicationContext();
                    CharSequence text = "Something wen wrong. Pleas try again.";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }
                if( MainButton.getText() == "STOP WAITING" ) {
                    mService.StopMusic();
                }
                else {
                    mService.StartMusic();
                }
                updateLooks(getResources().getConfiguration().orientation);
            }
        });

        if(aboutMusicText != null)
            aboutMusicText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("About Music");
                    alertDialog.setMessage("Mining by Moonlight Kevin MacLeod (incompetech.com)\n" +
                            "Licensed under Creative Commons:\n"+  "By Attribution 3.0 License\n" +
                            "http://creativecommons.org/licenses/by/3.0/");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                }
            });

        if(ImageButtonMute != null)
            ImageButtonMute.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mService != null)
                    {
                        if( mService.getMute() ){
                            mService.setMute(false);
                            ImageButtonMute.setImageResource(R.drawable.ic_volume_up_white_36dp);
                        }
                        else{
                            mService.setMute(true);
                            ImageButtonMute.setImageResource(R.drawable.ic_volume_off_white_36dp);
                        }
                    }

                }
            });

        if(Button2 != null) Button2.setOnClickListener(new SendOnClickListener());

        if(Button3 != null) Button3.setOnClickListener(new ResumeOnClickListener());
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLooks(newConfig.orientation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(h != null){
            h.removeCallbacks(TimerRunnable);
        }
        h = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(h != null){
            h.removeCallbacks(TimerRunnable);
        }
        h = null;
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            StopWatchService.LocalBinder binder = (StopWatchService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void startTimer(){
        h = new Handler();
        //milliseconds
        h.postDelayed(TimerRunnable, delay);

    }

    final String mainB1 = "START WAITING";
    final String mainB2 = "START WAITING AGAIN";
    final String mainBStop = "STOP WAITING";
    final String sendS1 = "SEND URGING MESSAGE";
    final String sendS2 = "SEND \"I'M NOT WAITING ANYMORE\" MESSAGE";

    final String iWATING = "I'VE BEEN WAITING FOR:";
    final String uWAITING = "YOU'VE BEEN WAITING FOR:";

    private void updateLooks(int configuration){
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            textView2.setText(iWATING);
            Button3.setVisibility(View.INVISIBLE);
            MainButton.setVisibility(View.INVISIBLE);
            Log.e("boo","boo5");
        }
        else {

            int state = 0;
            if (mService != null) state = mService.state;
            MainButton.setVisibility(View.VISIBLE);
            if (state == 0) {
                Button2.setVisibility(View.INVISIBLE);
                Button3.setVisibility(View.INVISIBLE);
                MainButton.setText(mainB1);
                textView2.setText(uWAITING);
                return;
            }
            if (state == 1) {
                Button2.setVisibility(View.VISIBLE);
                Button3.setVisibility(View.INVISIBLE);
                MainButton.setText(mainBStop);
                textView2.setText(uWAITING);
                return;
            }
            if (state == 2) {
                Button2.setVisibility(View.VISIBLE);
                Button3.setVisibility(View.VISIBLE);
                MainButton.setText(mainB2);
                Button2.setText(sendS2);
                textView2.setText(uWAITING);
                return;
            }
        }

    }



}
