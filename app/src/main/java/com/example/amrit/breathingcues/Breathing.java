package com.example.amrit.breathingcues;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class Breathing extends AppCompatActivity {

    private enum BreathingState{
        PAUSED,INHALE, EXHALE,HOLD
    }

    CountDownTimer timer;
    private long inhaleTimeSec ;
    private long exhaleTimeSec;
    private long holdTimeSec;
    private long timerTimeSec;
    private BreathingState breathingState = BreathingState.PAUSED;
    MaterialProgressBar currentActionProgressBar;
    MaterialProgressBar timerProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentActionProgressBar = (MaterialProgressBar) findViewById(R.id.currentActionProgressBar);
        timerProgressBar = (MaterialProgressBar) findViewById(R.id.timerProgressBar);


        setupSpinner(R.id.inhaleSpinner);
        setupSpinner(R.id.exhaleSpinner);
        setupSpinner(R.id.holdSpinner);
        setupSpinner(R.id.timerSpinner);
        setupStartClick();
    }

    private void setupStartClick() {
        final TextView commandTextView = (TextView) findViewById(R.id.breathingActionCommandTextView);

        commandTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(breathingState == BreathingState.PAUSED)
                    startTimer();
            }
        });
    }

    private void startTimer() {
        inhaleTimeSec = getTimeFromSpinner(R.id.inhaleSpinner);
        holdTimeSec = getTimeFromSpinner(R.id.holdSpinner);
        exhaleTimeSec = getTimeFromSpinner(R.id.exhaleSpinner);
        timerTimeSec = getTimeFromSpinner(R.id.timerSpinner);

        final long inhaleTimeRangeSec = inhaleTimeSec;
        final long holdTimeRangeSec = inhaleTimeSec + holdTimeSec;
        final long exhaleTimeRangeSec = holdTimeRangeSec + exhaleTimeSec;


        final long cycleTimeSec = inhaleTimeSec + holdTimeSec + exhaleTimeSec;
        final long numberOfCompleteCycles = timerTimeSec / cycleTimeSec;

        timerProgressBar.setMax((int) timerTimeSec);
        currentActionProgressBar.setMax((int) cycleTimeSec);

        final TextView actionCommandTextView = (TextView) findViewById(R.id.breathingActionCommandTextView);
        final TextView actionTimerTextView = (TextView) findViewById(R.id.breathingActionTime);
        final TextView clockTextView = (TextView) findViewById(R.id.clockTextView);
        actionTimerTextView.setVisibility(View.VISIBLE);

        timer = new CountDownTimer(timerTimeSec * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long millisElapsed = (timerTimeSec * 1000) - millisUntilFinished;
                long cycleNumber = (millisElapsed/1000) / (cycleTimeSec);

                if(((millisElapsed/1000) - (cycleNumber * cycleTimeSec)) < inhaleTimeRangeSec){
                    breathingState = BreathingState.INHALE;
                    long inhaleTimerSec = inhaleTimeSec - ((millisElapsed / 1000) - cycleNumber * cycleTimeSec);

                    actionCommandTextView.setText("Inhale");
                    actionTimerTextView.setText(inhaleTimerSec + "");
                }
                else if(((millisElapsed/1000) - (cycleNumber * cycleTimeSec)) < holdTimeRangeSec){
                    breathingState = BreathingState.HOLD;
                    long holdTimerSec = holdTimeSec - ((millisElapsed / 1000) - (cycleNumber * cycleTimeSec) - inhaleTimeRangeSec);

                    actionCommandTextView.setText("Hold");
                    actionTimerTextView.setText(holdTimerSec + "");
                }
                else if(((millisElapsed/1000) - (cycleNumber * cycleTimeSec)) < exhaleTimeRangeSec){
                    breathingState = BreathingState.EXHALE;
                    long exhaleTimerSec = exhaleTimeSec - ((millisElapsed / 1000) - (cycleNumber * cycleTimeSec) - holdTimeRangeSec);

                    actionCommandTextView.setText("Exhale");
                    actionTimerTextView.setText(exhaleTimerSec + "");
                }

                long progressInSec = (millisElapsed/1000) - (cycleNumber * cycleTimeSec);
                currentActionProgressBar.setProgress((int) progressInSec);
                timerProgressBar.setProgress((int) (millisElapsed/1000));


                clockTextView.setText(getTimeMinutesString((int)millisElapsed /1000));
            }

            @Override
            public void onFinish() {
                breathingState = BreathingState.PAUSED;
                actionCommandTextView.setText("Start");
                actionTimerTextView.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

//    private void startTimer() {
//        inhaleTimeSec  = getTimeFromSpinner(R.id.inhaleSpinner);
//        exhaleTimeSec  = getTimeFromSpinner(R.id.exhaleSpinner);
//        holdTimeSec = getTimeFromSpinner(R.id.holdSpinner);
//        timerTimeSec = getTimeFromSpinner(R.id.timerSpinner);
//        breathingState = BreathingState.INHALE;
//
//        final int cycleTimeInMillis = (inhaleTimeSec + exhaleTimeSec + holdTimeSec) * 1000;
//        progressBar.setMax(cycleTimeInMillis);
//
//        final TextView actionCommandTextView = (TextView) findViewById(R.id.breathingActionCommandTextView);
//        final TextView actionTimerTextView = (TextView) findViewById(R.id.breathingActionTime);
//        actionTimerTextView.setVisibility(View.VISIBLE);
//
//        timer = new CountDownTimer(timerTimeSec * 1000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                int millisElapsed = (int) ((timerTimeSec * 1000) - millisUntilFinished);
//                int cycleNumber = millisElapsed / cycleTimeInMillis;
//
//                actionCommandTextView.setText("INHALE");
//                if ((millisElapsed - (cycleNumber * cycleTimeInMillis)) < inhaleTimeSec) {
//                    breathingState = BreathingState.INHALE;
//                    int inhaleTimeElapsedMillis = millisElapsed - (cycleNumber * cycleTimeInMillis);
//                    int inhaleTimeLeftMillis = (int) (millisUntilFinished - (inhaleTimeElapsedMillis + (cycleNumber * cycleTimeInMillis)));
//
//
//                    actionTimerTextView.setText(inhaleTimeLeftMillis * 1000 + "");
//                }
//                else if ((millisElapsed - (cycleNumber * cycleTimeInMillis)) < exhaleTimeSec) {
//                    breathingState = BreathingState.EXHALE;
//                    int exhaleTimeElapsedMillis = millisElapsed - (cycleNumber * cycleTimeInMillis) - inhaleTimeSec;
//                    int exhaleTimeLeftMillis = (int) (millisUntilFinished - (exhaleTimeElapsedMillis + (cycleNumber * cycleTimeInMillis) + inhaleTimeSec));
//
//                    actionCommandTextView.setText("EXHALE");
//                    actionTimerTextView.setText(exhaleTimeLeftMillis * 1000 + "");
//                }
//                else if ((millisElapsed - (cycleNumber * cycleTimeInMillis)) < inhaleTimeSec){
//                    breathingState = BreathingState.EXHALE;
//                    int holdTimeElapsedMillis = millisElapsed - (cycleNumber * cycleTimeInMillis) - inhaleTimeSec - exhaleTimeSec;
//                    int holdTimeLeftMillis = (int) (millisUntilFinished - (holdTimeElapsedMillis + (cycleNumber * cycleTimeInMillis) + inhaleTimeSec + exhaleTimeSec));
//
//                    actionCommandTextView.setText("EXHALE");
//                    actionTimerTextView.setText(holdTimeLeftMillis * 1000 + "");
//                }
//
//                int progress = cycleTimeInMillis - inhaleTimeSec * 1000;
//                progressBar.setProgress((int) (progress));
//
//
//            }
//
//            @Override
//            public void onFinish() {
//                breathingState = BreathingState.INHALE;
//            }
//        }.start();
//
//    }


    private void setupSpinner(int spinnerId) {
        Spinner spinner = (Spinner) findViewById(spinnerId);

        ArrayList <String> stringSecondsList = new ArrayList<String>();
        if (spinnerId != R.id.timerSpinner){
            int[] intSecondsList = getResources().getIntArray(R.array.secondsListContinous);
            for(int i = 0; i < intSecondsList.length; i++){
                stringSecondsList.add(intSecondsList[i] + " s ");
            }
        }
        else {
            int[] intSecondsList = getResources().getIntArray(R.array.secondsListTimer);
            for(int i = 0; i < intSecondsList.length; i++){
                String timerString = getTimeMinutesString(intSecondsList[i]);
                stringSecondsList.add(timerString);
            }
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, stringSecondsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private int getTimeFromSpinner(int spinnerId) {
        Spinner spinner = (Spinner) findViewById(spinnerId);
        int timeOnSpinner;
        int positionOfItemSelected = spinner.getSelectedItemPosition();

        if (spinnerId != R.id.timerSpinner)
            timeOnSpinner = getResources().getIntArray(R.array.secondsListContinous)[positionOfItemSelected];
        else
            timeOnSpinner = getResources().getIntArray(R.array.secondsListTimer)[positionOfItemSelected];

        return timeOnSpinner;
    }

    private String getTimeMinutesString(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String secondsString = seconds + "";
        if (seconds < 9)
            secondsString = "0" + secondsString;
        String timeString = minutes + ":" + secondsString;

        return timeString;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_breathing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
