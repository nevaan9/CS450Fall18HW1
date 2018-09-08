package com.example.stopwatch;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    // Gain Access to our UI elements
    // Make them instance variables
    private TextView tv_seconds = null;
    private TextView tv_milliseconds = null;
    private TextView tv_minutes = null;
    private Button btn_main = null;
    private Button btn_reset = null;
    private Timer t = null;
    private Counter ctr = null; // Timer Task
    private int stoppedAtCount = 0;
    private int seconds = 0;
    private int minutes = 0;

    // Initialize the audio attributes
    private AudioAttributes aa = null;
    private SoundPool soundPool = null;
    private int bloopSOund = 0;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activity main is the activity_main.xml file
        setContentView(R.layout.activity_main);

        // initialize tv_seconds
        // R is a class that get generated by android; it corresonds to all the layout files, recource files etc
        this.tv_seconds = findViewById(R.id.tv_seconds);
        this.tv_milliseconds = findViewById(R.id.tv_milliseconds);
        this.tv_minutes = findViewById(R.id.tv_minutes);
        this.btn_main = findViewById(R.id.btn_main);
        this.btn_reset = findViewById(R.id.btn_reset);

        // Load the audio stuff
        this.aa = new AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        this.soundPool = new SoundPool
                .Builder()
                .setMaxStreams(1)
                .setAudioAttributes(aa)
                .build();

        this.bloopSOund = this.soundPool.load(this, R.raw.bloop, 1);

        // add the bloop to the ctr
        this.tv_seconds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(bloopSOund, 1f,1f, 1,0, 1f);

                // Animate the seconds count
                // Animator inflator inflates an object inside a layout
                Animator anim = AnimatorInflater.loadAnimator(MainActivity.this, R.animator.counter);
                anim.setTarget(tv_seconds);
            }
        });

        // Now we have access to the instance variables/UI elements
        this.btn_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (btn_main.getText().toString().equals("Start")) {
                    // This is an ANNONYMOUS INNER CLASS
                    // Annonymous inner classes have access to variables in their parent element

                    // Start the Timer
                    MainActivity.this.t = new Timer();

                    // We cannot use this, as this corresponds to the OnClickListener Class
                    t.scheduleAtFixedRate(ctr, 0 , 1);

                    // Change the button colors and text
                    changeButton(btn_main, "Stop", getResources().getColor(R.color.holo_red_light));
                } else if (btn_main.getText().toString().equals("Stop")) {
                    // Save the count value
                    stoppedAtCount = ctr.count;

                    // Stop the counter
                    t.cancel();

                    // Change the Stop button to a Resume Button
                    changeButton(btn_main, "Resume", getResources().getColor(R.color.holo_blue_light));

                } else if (btn_main.getText().toString().equals("Resume")) {
                    // Start the timer again
                    MainActivity.this.t = new Timer();

                    // Reset the timerTask
                    MainActivity.this.ctr = new Counter();
                    MainActivity.this.ctr.count = stoppedAtCount;

                    // Schdeule the work
                    t.scheduleAtFixedRate(ctr, 0 , 1);

                    // Change the Resume button to a Stop Button
                    changeButton(btn_main, "Stop", getResources().getColor(R.color.holo_red_light));
                }
            }
        });

        // The stop button
        this.btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cancel the timer
                t.cancel();

                // Reset the seconds and minutes instance variables
                MainActivity.this.seconds = 0;
                MainActivity.this.minutes = 0;

                // Update the UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.tv_milliseconds.setText("00");
                        MainActivity.this.tv_seconds.setText("00");
                        MainActivity.this.tv_minutes.setText(String.valueOf("00"));
                    }
                });

                // Reset the timerTask
                MainActivity.this.ctr = new Counter();
                changeButton(btn_main, "Start", getResources().getColor(R.color.holo_green_light));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Reload the count from a previous run; if first time running, start at 0
        // preferences to share state --> Preferences work well if we have a small amount of data we want to reload
        int count = getPreferences(MODE_PRIVATE).getInt("COUNT", 0);


        //this.tv_seconds.setText(Integer.toString(count)); Make the UI update tot the save count
        this.ctr = new Counter();
        this.ctr.count = count;

        // pop ups
        // This is a factory method (a design pattern)
        Toast.makeText(this, "Stopwatch is starting", Toast.LENGTH_LONG)
                .show();

        // Debug messages - Use Log class to do this!
        // Log.i("Hello");
        // Log.wtf();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Save the counter
        getPreferences(MODE_PRIVATE)
                .edit()
                .putInt("COUNT", ctr.count)
                .apply();
    }

    class Counter extends TimerTask {

        private int count = 0;
        String millisecond_text = "00";
        String seconds_text = "00";
        String minute_text = "00";

        @Override
        public void run() {
            // We cannot update UI elements inside another thread. If we need to, we need to put it inside the follwoing fucntion.
            // The count will take care of the milliseconds
            if (this.count/10 > 100) {
                if (MainActivity.this.seconds == 59) {
                    MainActivity.this.seconds = -1;
                    MainActivity.this.minutes++;
                }
                MainActivity.this.seconds++;
                this.count = -1;
            }
            // Tell Android to run the following code on the UI thread
            // A Runnable is an object that implements a run method
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    millisecond_text = (Counter.this.count/10 < 10) ? "0" + Counter.this.count/10 : Integer.toString(Counter.this.count/10);
                    seconds_text = (MainActivity.this.seconds < 10) ? "0" + MainActivity.this.seconds : Integer.toString(MainActivity.this.seconds);
                    minute_text = (MainActivity.this.minutes < 10) ? "0" + MainActivity.this.minutes : Integer.toString(MainActivity.this.minutes);

                    MainActivity.this.tv_milliseconds.setText(millisecond_text);
                    MainActivity.this.tv_seconds.setText(String.valueOf(seconds_text));
                    MainActivity.this.tv_minutes.setText(String.valueOf(minute_text));

                    Counter.this.count++;
                }
            });
        }
    }

    // Class Helper Functions
    protected void changeButton(Button btn, String buttonText, int buttonColor) {
        btn.setText(buttonText);
        btn.setBackgroundColor(buttonColor);
    }
}
