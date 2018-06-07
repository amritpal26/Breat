package com.example.amrit.breathingcues;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class TimerFragment extends android.support.v4.app.Fragment {

    MaterialProgressBar timerProgressBar;

    enum TimerState {
        RUNNING,PAUSED,STOPPED
    }

    TimerState timerState;
    CountDownTimer timer;
    Spinner timerSpinner;

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_timer, container, false);

        timerProgressBar = (MaterialProgressBar) view.findViewById(R.id.timerActivityProgressBar);
        setupSpinner(R.id.timerActivityTimerSpinner);
        TimerState timerState = TimerState.RUNNING;

        setupPauseButton();
        setupPlayButton();
        setupStopButton();

        return view;
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_timer);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        timerProgressBar = (MaterialProgressBar) findViewById(R.id.timerActivityProgressBar);
//        setupSpinner(R.id.timerActivityTimerSpinner);
//        TimerState timerState = TimerState.RUNNING;
//
//        setupPauseButton();
//        setupPlayButton();
//        setupStopButton();
//    }

    private void setupPauseButton() {
        FloatingActionButton pauseBtn = (FloatingActionButton) view.findViewById(R.id.pause);
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
        FloatingActionButton playBtn = (FloatingActionButton) view.findViewById(R.id.play);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });
    }

    private void setupStopButton() {
        FloatingActionButton pauseBtn = (FloatingActionButton) view.findViewById(R.id.stop);
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

        TextView timerText = (TextView) view.findViewById(R.id.timerActivityTimertextView);
        timerText.setText(minutesUntilFinished + ":" + secondsStr);
    }

    private void setupSpinner(int spinnerId) {
        timerSpinner = (Spinner) view.findViewById(spinnerId);

        ArrayList<String> stringSecondsList = new ArrayList<String>();
        int[] intSecondsList = getResources().getIntArray(R.array.secondsListContinous);
        for(int i = 0; i < intSecondsList.length; i++){
            stringSecondsList.add(intSecondsList[i] + " s ");
        }

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, stringSecondsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timerSpinner.setAdapter(adapter);
    }

    private int getTimeFromSpinner(int spinnerId) {
        Spinner spinner = (Spinner) view.findViewById(spinnerId);
        int timeOnSpinner;
        int positionOfItemSelected = spinner.getSelectedItemPosition();
        timeOnSpinner = getResources().getIntArray(R.array.secondsListContinous)[positionOfItemSelected];

        return timeOnSpinner;
    }

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, TimerFragment.class);
        return intent;
    }
}
