package com.example.amrit.breathingcues;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class TimerActivity extends AppCompatActivity {

    MaterialProgressBar timerProgressBar;

    enum TimerState {
        RUNNING,PAUSED,STOPPED
    }

    TimerState timerState;
    CountDownTimer timer;
    Spinner timerSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        timerProgressBar = (MaterialProgressBar) findViewById(R.id.timerActivityProgressBar);
        setupSpinner(R.id.timerActivityTimerSpinner);
        TimerState timerState = TimerState.RUNNING;

        setupPauseButton();
        setupPlayButton();
        setupStopButton();
    }

    private void setupPauseButton() {
        FloatingActionButton pauseBtn = (FloatingActionButton) findViewById(R.id.pause);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerState != TimerState.PAUSED) {
                    timerState = TimerState.PAUSED;
                    timer.cancel();
                }
            }
        });
    }

    private void setupPlayButton() {
        FloatingActionButton playBtn = (FloatingActionButton) findViewById(R.id.play);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });
    }

    private void setupStopButton() {
        FloatingActionButton pauseBtn = (FloatingActionButton) findViewById(R.id.stop);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerState != TimerState.STOPPED) {
                    onTimerFinished();
                }
            }
        });
    }

    private void startTimer() {
        final long secsToRun = getTimeFromSpinner(R.id.timerActivityTimerSpinner);
        timerProgressBar.setMax((int) secsToRun);


        if (timerState != TimerState.RUNNING) {
            timerState = TimerState.RUNNING;
            timer = new CountDownTimer(secsToRun * 1000, 10) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long currentTimerTime = secsToRun - (millisUntilFinished / 1000);
                    timerProgressBar.setProgress((int) currentTimerTime);
                    upDateUI(currentTimerTime);
                }

                @Override
                public void onFinish() {
                    onTimerFinished();
                }
            }.start();
        }
    }

    private void onTimerFinished() {
        timerState = TimerState.STOPPED;
        timer.cancel();
        timerProgressBar.setProgress(0);
    }

    private void upDateUI(long currentTimerTime) {
        int minutesUntilFinished = (int) currentTimerTime / 60;
        int secondsInMinuteUntilFinished = (int) currentTimerTime - minutesUntilFinished * 60;
        String secondsStr = "" + secondsInMinuteUntilFinished;
        if (secondsInMinuteUntilFinished <= 9){
            secondsStr = "0" + secondsStr;
        }

        TextView timerText = (TextView) findViewById(R.id.timerActivityTimertextView);
        timerText.setText(minutesUntilFinished + ":" + secondsStr);
    }

    private void setupSpinner(int spinnerId) {
        timerSpinner = (Spinner) findViewById(spinnerId);

        ArrayList<String> stringSecondsList = new ArrayList<String>();
        int[] intSecondsList = getResources().getIntArray(R.array.secondsListContinous);
        for(int i = 0; i < intSecondsList.length; i++){
            stringSecondsList.add(intSecondsList[i] + " s ");
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, stringSecondsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timerSpinner.setAdapter(adapter);
    }

    private int getTimeFromSpinner(int spinnerId) {
        Spinner spinner = (Spinner) findViewById(spinnerId);
        int timeOnSpinner;
        int positionOfItemSelected = spinner.getSelectedItemPosition();
        timeOnSpinner = getResources().getIntArray(R.array.secondsListContinous)[positionOfItemSelected];

        return timeOnSpinner;
    }

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, TimerActivity.class);
        return intent;
    }
}
