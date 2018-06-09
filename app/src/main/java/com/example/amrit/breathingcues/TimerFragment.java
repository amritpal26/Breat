package com.example.amrit.breathingcues;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class TimerFragment extends android.support.v4.app.Fragment {

    private final String PREFERENCE_KEY_SOUND_SWITCH = "pref_sound";
    private final String PREFERENCE_KEY_VIBRATION_SWITCH = "pref_vibration";

    MaterialProgressBar timerProgressBar;

    enum TimerState {
        RUNNING, PAUSED, STOPPED, NEW
    }

    TimerState timerState;
    CountDownTimer timer;
    Spinner timerSpinner;

    SharedPreferences preferences;
    boolean soundEnabled;
    boolean vibrationEnabled;

    long millisCoveredInPreviousRuns;
    long currentTimerTimeMillis;
    long millisRemaining;
    long progress;

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_timer, container, false);

        timerProgressBar = (MaterialProgressBar) view.findViewById(R.id.timerActivityProgressBar);
        setupSpinner(R.id.timerActivityTimerSpinner);
        timerState = TimerState.NEW;
        millisCoveredInPreviousRuns = 0;

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        soundEnabled = preferences.getBoolean(PREFERENCE_KEY_SOUND_SWITCH, false);
        vibrationEnabled = preferences.getBoolean(PREFERENCE_KEY_VIBRATION_SWITCH, false);

        setupPauseButton();
        setupPlayButton();
        setupStopButton();

        return view;
    }


    private void setupPauseButton() {
        ImageButton pauseBtn = (ImageButton) view.findViewById(R.id.timerPauseBtn);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerState == TimerState.RUNNING && timerState != TimerState.NEW && timerState != TimerState.STOPPED) {
                    timerState = TimerState.PAUSED;
                    millisCoveredInPreviousRuns += currentTimerTimeMillis;
                    timer.cancel();
                }
            }
        });
    }

    private void setupPlayButton() {
        ImageButton playBtn = (ImageButton) view.findViewById(R.id.timerPlayBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerState == TimerState.STOPPED || timerState == TimerState.NEW) {
                    timerState = TimerState.RUNNING;
                    long secsToRun = getTimeFromSpinner(R.id.timerActivityTimerSpinner);
                    final long millisToRun = secsToRun * 1000;
                    timerProgressBar.setMax((int) millisToRun);
                    startTimer(millisToRun);
                }
                else if(timerState == TimerState.PAUSED){
                    startTimer(millisRemaining);
                }
            }
        });
    }

    private void setupStopButton() {
        ImageButton pauseBtn = (ImageButton) view.findViewById(R.id.timerStopBtn);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerState != TimerState.NEW) {
                    timerState = TimerState.STOPPED;
                    onStopTimer(0);
                }
            }
        });
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

    private void startTimer(final long millisToRun) {
        timerState = TimerState.RUNNING;
        currentTimerTimeMillis = 0;

        timer = new CountDownTimer(millisToRun, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentTimerTimeMillis = millisToRun - millisUntilFinished;
                millisRemaining = millisUntilFinished;

                upDateUI(currentTimerTimeMillis);
            }

            @Override
            public void onFinish() {
                onStopTimer(millisCoveredInPreviousRuns + millisToRun);

            }
        }.start();
    }

    private void onStopTimer(long finalProgress) {
        timerState = TimerState.STOPPED;
        millisCoveredInPreviousRuns = 0;
        currentTimerTimeMillis = 0;
        timer.cancel();
        upDateUI(finalProgress);
    }

    private void upDateUI(long timerTimeMillis) {
        timerTimeMillis += millisCoveredInPreviousRuns;
        int minutesUntilFinished = (int) timerTimeMillis / 60000;
        int secondsInMinuteUntilFinished = (int) (timerTimeMillis/1000) - minutesUntilFinished * 60;
        String secondsStr = "" + secondsInMinuteUntilFinished;

        if (secondsInMinuteUntilFinished <= 9){
            secondsStr = "0" + secondsStr;
        }

        TextView timerText = (TextView) view.findViewById(R.id.timerActivityTimertextView);
        timerText.setText(minutesUntilFinished + ":" + secondsStr);
        timerProgressBar.setProgress((int) timerTimeMillis);
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
