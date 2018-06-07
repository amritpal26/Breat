package com.example.amrit.breathingcues;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ActionFragment extends android.support.v4.app.Fragment {

    private enum BreathingState{
        PAUSED,INHALE, EXHALE,HOLD
    }

    CountDownTimer timer;
    private long inhaleTimeSec ;
    private long exhaleTimeSec;
    private long holdTimeSec;
    private long timerTimeSec;
    private BreathingState breathingState = BreathingState.PAUSED;

    MediaPlayer beepSound;
    Vibrator vibrator;

    MaterialProgressBar currentActionProgressBar;
    MaterialProgressBar timerProgressBar;

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_action, container, false);

        currentActionProgressBar = (MaterialProgressBar) view.findViewById(R.id.currentActionProgressBar);
        timerProgressBar = (MaterialProgressBar) view.findViewById(R.id.timerActivityProgressBar);

        beepSound = MediaPlayer.create(getActivity(), R.raw.beep);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        setupSpinner(R.id.inhaleSpinner);
        setupSpinner(R.id.exhaleSpinner);
        setupSpinner(R.id.holdSpinner);
        setupSpinner(R.id.timerSpinner);
        setupStartClick();

        return view;
    }

//    @Nullable
//    @Override
//    public void onCreate(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_breathing);
//
//
//        currentActionProgressBar = (MaterialProgressBar) view.findViewById(R.id.currentActionProgressBar);
//        timerProgressBar = (MaterialProgressBar) findViewById(R.id.timerActivityProgressBar);
//
//        beepSound = MediaPlayer.create(this, R.raw.beep);
//        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//
//        setupSpinner(R.id.inhaleSpinner);
//        setupSpinner(R.id.exhaleSpinner);
//        setupSpinner(R.id.holdSpinner);
//        setupSpinner(R.id.timerSpinner);
//        setupStartClick();
//    }

    private void setupStartClick() {
        final TextView commandTextView = (TextView) view.findViewById(R.id.breathingActionCommandTextView);

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

        timerProgressBar.setMax((int) timerTimeSec * 100);
        currentActionProgressBar.setMax((int) cycleTimeSec * 100);

        final TextView actionCommandTextView = (TextView) view.findViewById(R.id.breathingActionCommandTextView);
        final TextView actionTimerTextView = (TextView) view.findViewById(R.id.breathingActionTime);
        final TextView clockTextView = (TextView) view.findViewById(R.id.clockTextView);
        actionTimerTextView.setVisibility(View.VISIBLE);

        timer = new CountDownTimer(timerTimeSec * 1000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {

                long millisElapsed = (timerTimeSec * 1000) - millisUntilFinished;
                long millisElapsedForUI = (timerTimeSec * 1000) - millisUntilFinished;
                long cycleNumber = (millisElapsed/1000) / (cycleTimeSec);
                long previousCycleNumber = 0;

                if(((millisElapsed/1000) - (cycleNumber * cycleTimeSec)) < inhaleTimeRangeSec){
                    if (breathingState != BreathingState.INHALE){
                        beepSound.start();
                        vibrator.vibrate(500);
                        breathingState = BreathingState.INHALE;
                    }
                    long inhaleTimerSec = inhaleTimeSec - ((millisElapsed / 1000) - cycleNumber * cycleTimeSec);
                    actionCommandTextView.setText("Inhale");
                    actionTimerTextView.setText(inhaleTimerSec + "");
                }
                else if(((millisElapsed/1000) - (cycleNumber * cycleTimeSec)) < holdTimeRangeSec){
                    if (breathingState != BreathingState.HOLD){
                        beepSound.start();
                        breathingState = BreathingState.HOLD;
                        vibrator.vibrate(500);
                    }

                    breathingState = BreathingState.HOLD;
                    long holdTimerSec = holdTimeSec - ((millisElapsed / 1000) - (cycleNumber * cycleTimeSec) - inhaleTimeRangeSec);

                    actionCommandTextView.setText("Hold");
                    actionTimerTextView.setText(holdTimerSec + "");
                }
                else if(((millisElapsed/1000) - (cycleNumber * cycleTimeSec)) < exhaleTimeRangeSec){
                    if (breathingState != BreathingState.EXHALE){
                        beepSound.start();
                        breathingState = BreathingState.EXHALE;
                        vibrator.vibrate(500);
                    }

                    breathingState = BreathingState.EXHALE;
                    long exhaleTimerSec = exhaleTimeSec - ((millisElapsed / 1000) - (cycleNumber * cycleTimeSec) - holdTimeRangeSec);

                    actionCommandTextView.setText("Exhale");
                    actionTimerTextView.setText(exhaleTimerSec + "");
                }


                if(previousCycleNumber != cycleNumber){
                    currentActionProgressBar.setProgress(0);
                    currentActionProgressBar.setMax((int) timerTimeSec);
                    previousCycleNumber++;
                }
                else{
                    long progressInSec = (millisElapsedForUI/10) - (cycleNumber * cycleTimeSec);
                    currentActionProgressBar.setProgress((int) progressInSec);
                }

                timerProgressBar.setProgress((int) (millisElapsedForUI/10));
                clockTextView.setText(getTimeMinutesString((int)millisElapsed /1000));
            }

            @Override
            public void onFinish() {
                breathingState = BreathingState.PAUSED;
                actionCommandTextView.setText("Start Again");
                actionTimerTextView.setVisibility(View.INVISIBLE);
            }
        }.start();
    }


    private void setupSpinner(int spinnerId) {
        Spinner spinner = (Spinner) view.findViewById(spinnerId);

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

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, stringSecondsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private int getTimeFromSpinner(int spinnerId) {
        Spinner spinner = (Spinner) view.findViewById(spinnerId);
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_breathing, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            item.getMenuInfo();
//            Intent intent = TimerActivity.makeIntent(Breathing.this);
//            startActivity(intent);
//            timer.cancel();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}