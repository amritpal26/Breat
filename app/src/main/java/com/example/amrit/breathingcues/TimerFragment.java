package com.example.amrit.breathingcues;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
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

    MediaPlayer beepSound;
    Vibrator vibrator;

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

        timerState = TimerState.NEW;
        millisCoveredInPreviousRuns = 0;

        beepSound = MediaPlayer.create(getActivity(), R.raw.beep);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        soundEnabled = preferences.getBoolean(PREFERENCE_KEY_SOUND_SWITCH, false);
        vibrationEnabled = preferences.getBoolean(PREFERENCE_KEY_VIBRATION_SWITCH, false);

        setupSpinner(R.id.timerActivityTimerSpinner);
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
                if (timerState == TimerState.RUNNING && timerState != TimerState.NEW && timerState != TimerState.STOPPED) {
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
                if (timerState == TimerState.STOPPED || timerState == TimerState.NEW) {
                    timerState = TimerState.RUNNING;
                    long secsToRun = getTimeFromSpinner(R.id.timerActivityTimerSpinner);
                    final long millisToRun = secsToRun * 1000;
                    timerProgressBar.setMax((int) millisToRun);
                    startTimer(millisToRun);
                    disableSpinners();
                } else if (timerState == TimerState.PAUSED) {
                    startTimer(millisRemaining);
                    disableSpinners();
                }
            }
        });
    }

    private void setupStopButton() {
        ImageButton pauseBtn = (ImageButton) view.findViewById(R.id.timerStopBtn);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerState != TimerState.NEW) {
                    timerState = TimerState.STOPPED;
                    onStopTimer(0);
                    enableSpinners();
                }
            }
        });
    }

    private void setupSpinner(int spinnerId) {
        timerSpinner = (Spinner) view.findViewById(spinnerId);

        ArrayList<String> stringSecondsList = new ArrayList<String>();
        int[] intSecondsList = getResources().getIntArray(R.array.secondsListTimer2);
        for (int i = 0; i < intSecondsList.length; i++) {
            stringSecondsList.add(getTimeMinutesString(intSecondsList[i]) + " sec ");
        }

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, stringSecondsList);
        adapter.setDropDownViewResource(R.layout.drop_down_layout);
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
                if (soundEnabled)
                    beepSound.start();
                if (vibrationEnabled)
                    vibrator.vibrate(500);
                enableSpinners();
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
        int secondsInMinuteUntilFinished = (int) (timerTimeMillis / 1000) - minutesUntilFinished * 60;
        String secondsStr = "" + secondsInMinuteUntilFinished;

        if (secondsInMinuteUntilFinished <= 9) {
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
        timeOnSpinner = getResources().getIntArray(R.array.secondsListTimer2)[positionOfItemSelected];

        return timeOnSpinner;
    }

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, TimerFragment.class);
        return intent;
    }

    private void enableSpinners() {
        Spinner spinner = (Spinner) view.findViewById(R.id.timerActivityTimerSpinner);
        spinner.setEnabled(true);
    }

    private void disableSpinners() {
        Spinner spinner = (Spinner) view.findViewById(R.id.timerActivityTimerSpinner);
        spinner.setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (timerState == TimerState.RUNNING) {
            timerState = TimerState.PAUSED;
            millisCoveredInPreviousRuns += currentTimerTimeMillis;
            timer.cancel();
        }
    }

    private String getTimeMinutesString(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String secondsString = seconds + "";
        if (seconds < 9)
            secondsString = "0" + secondsString;
        return minutes + ":" + secondsString;
    }

    @Override
    public void onResume() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        soundEnabled = preferences.getBoolean(PREFERENCE_KEY_SOUND_SWITCH, false);
        vibrationEnabled = preferences.getBoolean(PREFERENCE_KEY_VIBRATION_SWITCH, false);
        Log.i("Call", "onresume: " + soundEnabled + " " + vibrationEnabled);
        super.onResume();
    }
}
