package com.example.amrit.breathingcues;

import android.app.Fragment;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ActionFragment extends android.support.v4.app.Fragment {

    private final String PREFERENCE_KEY_SOUND_SWITCH = "pref_sound";
    private final String PREFERENCE_KEY_VIBRATION_SWITCH = "pref_vibration";

    private enum BreathingState {
        PAUSED, INHALE, EXHALE, HOLD
    }

    CountDownTimer timer;
    private long inhaleTimeSec;
    private long exhaleTimeSec;
    private long holdTimeSec;
    private long timerTimeSec;
    private BreathingState breathingState = BreathingState.PAUSED;

    MediaPlayer beepSound;
    Vibrator vibrator;

    MaterialProgressBar currentActionProgressBar;
    MaterialProgressBar timerProgressBar;

    SharedPreferences preferences;
    boolean soundEnabled;
    boolean vibrationEnabled;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        soundEnabled = preferences.getBoolean(PREFERENCE_KEY_SOUND_SWITCH, false);
        vibrationEnabled = preferences.getBoolean(PREFERENCE_KEY_VIBRATION_SWITCH, false);

        setupSpinner(R.id.inhaleSpinner);
        setupSpinner(R.id.exhaleSpinner);
        setupSpinner(R.id.holdSpinner);
        setupSpinner(R.id.timerSpinner);
        setupStartClick();

        ViewPager viewPager = getActivity().findViewById(R.id.container);
        setupOnPageChangeListener();

        return view;
    }

    private void setupOnPageChangeListener() {
        ViewPager viewPager = getActivity().findViewById(R.id.container);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    if (breathingState != BreathingState.PAUSED) {
                        timer.cancel();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setupStartClick() {
        final TextView commandTextView = (TextView) view.findViewById(R.id.breathingActionCommandTextView);

        commandTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (breathingState == BreathingState.PAUSED)
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
                long cycleNumber = (millisElapsed / 1000) / (cycleTimeSec);

                if (((millisElapsed / 1000) - (cycleNumber * cycleTimeSec)) < inhaleTimeRangeSec) {
                    if (breathingState != BreathingState.INHALE) {
                        if (soundEnabled)
                            beepSound.start();
                        if (vibrationEnabled)
                            vibrator.vibrate(500);
                        breathingState = BreathingState.INHALE;
                    }
                    long inhaleTimerSec = inhaleTimeSec - ((millisElapsed / 1000) - cycleNumber * cycleTimeSec);
                    actionCommandTextView.setText("Inhale");
                    actionTimerTextView.setText(inhaleTimerSec + "");
                } else if (((millisElapsed / 1000) - (cycleNumber * cycleTimeSec)) < holdTimeRangeSec) {
                    if (breathingState != BreathingState.HOLD) {
                        if (soundEnabled)
                            beepSound.start();
                        breathingState = BreathingState.HOLD;
                        if (vibrationEnabled)
                            vibrator.vibrate(500);
                    }

                    breathingState = BreathingState.HOLD;
                    long holdTimerSec = holdTimeSec - ((millisElapsed / 1000) - (cycleNumber * cycleTimeSec) - inhaleTimeRangeSec);

                    actionCommandTextView.setText("Hold");
                    actionTimerTextView.setText(holdTimerSec + "");
                } else if (((millisElapsed / 1000) - (cycleNumber * cycleTimeSec)) < exhaleTimeRangeSec) {
                    if (breathingState != BreathingState.EXHALE) {
                        if (soundEnabled)
                            beepSound.start();
                        breathingState = BreathingState.EXHALE;
                        if (vibrationEnabled)
                            vibrator.vibrate(500);
                    }

                    breathingState = BreathingState.EXHALE;
                    long exhaleTimerSec = exhaleTimeSec - ((millisElapsed / 1000) - (cycleNumber * cycleTimeSec) - holdTimeRangeSec);

                    actionCommandTextView.setText("Exhale");
                    actionTimerTextView.setText(exhaleTimerSec + "");
                }

                long progressInMillisBy10 = (millisElapsedForUI / 10) - (cycleNumber * cycleTimeSec * 100);
                currentActionProgressBar.setProgress((int) progressInMillisBy10);

                timerProgressBar.setProgress((int) (millisElapsedForUI / 10));
                clockTextView.setText(getTimeMinutesString((int) millisElapsed / 1000));
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

        ArrayList<String> stringSecondsList = new ArrayList<String>();
        if (spinnerId != R.id.timerSpinner) {
            int[] intSecondsList = getResources().getIntArray(R.array.secondsListContinous);
            for (int i = 0; i < intSecondsList.length; i++) {
                stringSecondsList.add(intSecondsList[i] + " sec");
            }
        } else {
            int[] intSecondsList = getResources().getIntArray(R.array.secondsListTimer);
            for (int i = 0; i < intSecondsList.length; i++) {
                String timerString = getTimeMinutesString(intSecondsList[i]);
                stringSecondsList.add(timerString);
            }
        }

        ArrayAdapter adapter = new ArrayAdapter(this.getActivity(), R.layout.drop_down_layout, stringSecondsList);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setDropDownWidth(20);
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
}