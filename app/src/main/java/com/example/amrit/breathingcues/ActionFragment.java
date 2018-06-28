package com.example.amrit.breathingcues;

import android.app.Fragment;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
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

import com.nex3z.expandablecircleview.ExpandableCircleView;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ActionFragment extends android.support.v4.app.Fragment {

    private final String PREFERENCE_KEY_SOUND_SWITCH = "pref_sound";
    private final String PREFERENCE_KEY_VIBRATION_SWITCH = "pref_vibration";

    private static final int EXPAND_DURATION = 100;

    private enum BreathingState {
        NEW_TIMER,PAUSED, INHALE, EXHALE, HOLD
    }

    CountDownTimer timer;
    private long inhaleTimeSec;
    private long exhaleTimeSec;
    private long hold_1_TimeSec;
    private long hold_2_TimeSec;
    private long previousTimerTimeMillis = 0;
    private long currentRunMillisElapsed;
    private long timerTimeOnSpinnerSec;
    long currentCycleNumber;
    private BreathingState breathingState = BreathingState.NEW_TIMER;

    MediaPlayer beepSound;
    MediaPlayer endSound;
    Vibrator vibrator;

//    MaterialProgressBar currentActionProgressBar;
    MaterialProgressBar timerProgressBar;
    ExpandableCircleView currentActionProgress;

    SharedPreferences preferences;
    boolean soundEnabled;
    boolean vibrationEnabled;

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_action, container, false);

//        currentActionProgressBar = (MaterialProgressBar) view.findViewById(R.id.currentActionProgressBar);

        timerProgressBar = (MaterialProgressBar) view.findViewById(R.id.timerActivityProgressBar);
        currentActionProgress = (ExpandableCircleView) view.findViewById(R.id.currentActionProgressBar);
        currentActionProgress.setExpandAnimationDuration(EXPAND_DURATION);

        final TextView commandTextView = (TextView) view.findViewById(R.id.breathingActionCommandTextView);
        final TextView actionTimerTextView = (TextView) view.findViewById(R.id.breathingActionTime);
        commandTextView.bringToFront();
        actionTimerTextView.bringToFront();

        beepSound = MediaPlayer.create(getActivity(), R.raw.beep);
        endSound = MediaPlayer.create(getActivity(), R.raw.final_sound);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        soundEnabled = preferences.getBoolean(PREFERENCE_KEY_SOUND_SWITCH, false);
        vibrationEnabled = preferences.getBoolean(PREFERENCE_KEY_VIBRATION_SWITCH, false);

        setupSpinner(R.id.inhaleSpinner);
        setupSpinner(R.id.exhaleSpinner);
        setupSpinner(R.id.holdSpinner);
        setupSpinner(R.id.holdSpinner2);
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
                    if (breathingState != BreathingState.PAUSED && breathingState != BreathingState.NEW_TIMER) {
                        pauseTimer();
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
        final TextView actionTimerTextView = (TextView) view.findViewById(R.id.breathingActionTime);

        commandTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (breathingState == BreathingState.NEW_TIMER)
                    startTimer(0);

                else if(breathingState != BreathingState.PAUSED && breathingState != BreathingState.NEW_TIMER){
                    pauseTimer();
                }
                else if(breathingState == BreathingState.PAUSED){
                    startTimer(previousTimerTimeMillis);
                }
            }
        });

        actionTimerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(breathingState == BreathingState.PAUSED && breathingState != BreathingState.NEW_TIMER){
                    previousTimerTimeMillis = 0;
                    startTimer(0);
                }
                else if(breathingState != BreathingState.PAUSED || breathingState != BreathingState.NEW_TIMER){
                    pauseTimer();
                }
            }
        });

    }

    private void pauseTimer() {
        final TextView commandTextView = (TextView) view.findViewById(R.id.breathingActionCommandTextView);
        final TextView actionTimerTextView = (TextView) view.findViewById(R.id.breathingActionTime);

        breathingState = BreathingState.PAUSED;
        actionTimerTextView.setText("Restart");
        commandTextView.setText("Continue");
        previousTimerTimeMillis += currentRunMillisElapsed;
        timer.cancel();
    }

    private void startTimer(final long currentTimerMillis) {
        inhaleTimeSec = getTimeFromSpinner(R.id.inhaleSpinner);
        hold_1_TimeSec = getTimeFromSpinner(R.id.holdSpinner);
        exhaleTimeSec = getTimeFromSpinner(R.id.exhaleSpinner);
        hold_2_TimeSec = getTimeFromSpinner(R.id.holdSpinner2);
        timerTimeOnSpinnerSec = getTimeFromSpinner(R.id.timerSpinner);

        final long inhaleTimeRangeSec = inhaleTimeSec;
        final long hold_1_TimeRangeSec = inhaleTimeSec + hold_1_TimeSec;
        final long exhaleTimeRangeSec = hold_1_TimeRangeSec + exhaleTimeSec;
        final long hold_2_TimeRangeSec = exhaleTimeRangeSec + hold_2_TimeSec;

        final long cycleTimeSec = inhaleTimeSec + hold_1_TimeSec + exhaleTimeSec + hold_2_TimeSec;

        timerProgressBar.setMax((int) timerTimeOnSpinnerSec * 100);
//        currentActionProgressBar.setMax((int) cycleTimeSec * 100);

        final TextView actionCommandTextView = (TextView) view.findViewById(R.id.breathingActionCommandTextView);
        final TextView actionTimerTextView = (TextView) view.findViewById(R.id.breathingActionTime);
        final TextView clockTextView = (TextView) view.findViewById(R.id.clockTextView);
        actionTimerTextView.setVisibility(View.VISIBLE);

        timer = new CountDownTimer((timerTimeOnSpinnerSec * 1000) - previousTimerTimeMillis, 10) {
            @Override
            public void onTick(long millisUntilFinished) {

                currentRunMillisElapsed = (timerTimeOnSpinnerSec * 1000) - millisUntilFinished - previousTimerTimeMillis;
                long millisElapsedTotal = currentRunMillisElapsed + previousTimerTimeMillis;

                currentCycleNumber = (millisElapsedTotal / 1000) / (cycleTimeSec);

                if (((millisElapsedTotal / 1000) - (currentCycleNumber * cycleTimeSec)) < inhaleTimeRangeSec) {
                    if (breathingState != BreathingState.INHALE) {
                        if (soundEnabled)
                            beepSound.start();
                        if (vibrationEnabled)
                            vibrator.vibrate(500);
                        breathingState = BreathingState.INHALE;
                    }
                    long inhaleTimerSec = (inhaleTimeSec) - ((millisElapsedTotal / 1000) - (currentCycleNumber * cycleTimeSec));

                    long inhaleReverseTimerMillis = millisElapsedTotal - (currentCycleNumber * cycleTimeSec * 1000);
                    long progress = (inhaleReverseTimerMillis / (2 * inhaleTimeSec * 10)) + 50;
                    currentActionProgress.setProgress((int) progress);

                    actionCommandTextView.setText("Inhale");
                    actionTimerTextView.setText( inhaleTimerSec+ "");


                }
                else if (((millisElapsedTotal / 1000) - (currentCycleNumber * cycleTimeSec)) < hold_1_TimeRangeSec) {
                    if (breathingState != BreathingState.HOLD) {
                        if (soundEnabled)
                            beepSound.start();
                        breathingState = BreathingState.HOLD;
                        if (vibrationEnabled)
                            vibrator.vibrate(500);
                    }

                    breathingState = BreathingState.HOLD;
                    long holdTimerSec = hold_1_TimeSec - ((millisElapsedTotal / 1000) - (currentCycleNumber * cycleTimeSec) - inhaleTimeRangeSec);
                    long holdTimerMillis = (millisElapsedTotal - ((currentCycleNumber * cycleTimeSec) - inhaleTimeRangeSec) * 1000) - (hold_1_TimeSec * 1000);

                    actionCommandTextView.setText("Hold");
                    actionTimerTextView.setText(holdTimerSec + "");

//                    currentActionProgressBar.setProgress((int) (holdTimerMillis/ (cycleTimeSec * 1000)));
                }
                else if (((millisElapsedTotal / 1000) - (currentCycleNumber * cycleTimeSec)) < exhaleTimeRangeSec) {
                    if (breathingState != BreathingState.EXHALE) {
                        if (soundEnabled)
                            beepSound.start();
                        breathingState = BreathingState.EXHALE;
                        if (vibrationEnabled)
                            vibrator.vibrate(500);
                    }

                    breathingState = BreathingState.EXHALE;
                    long exhaleTimerSec = exhaleTimeSec - ((millisElapsedTotal / 1000) - (currentCycleNumber * cycleTimeSec) - hold_1_TimeRangeSec);
                    long exhaleTimerMillis = (millisElapsedTotal - ((currentCycleNumber * cycleTimeSec) - hold_1_TimeRangeSec) * 1000) - (exhaleTimeSec * 1000);

                    actionCommandTextView.setText("Exhale");
                    actionTimerTextView.setText(exhaleTimerSec + "");

                    long exhaleReverseTimerMillis = (currentCycleNumber * cycleTimeSec * 1000) + (hold_1_TimeRangeSec * 1000) - millisElapsedTotal;
                    long progress = ((exhaleReverseTimerMillis / (2 * exhaleTimeSec * 10))) + 100;
                    currentActionProgress.setProgress((int) progress);

//                    currentActionProgressBar.setProgress((int) (exhaleTimerMillis/ (cycleTimeSec * 1000)));
                }
                else if (((millisElapsedTotal / 1000) - (currentCycleNumber * cycleTimeSec)) < hold_2_TimeRangeSec) {
                    if (breathingState != BreathingState.HOLD) {
                        if (soundEnabled)
                            beepSound.start();
                        breathingState = BreathingState.HOLD;
                        if (vibrationEnabled)
                            vibrator.vibrate(500);
                    }

                    breathingState = BreathingState.HOLD;
                    long holdTimerSec = hold_2_TimeSec - ((millisElapsedTotal / 1000) - (currentCycleNumber * cycleTimeSec) - exhaleTimeRangeSec);
                    long holdTimerMillis = (millisElapsedTotal - ((currentCycleNumber * cycleTimeSec) - exhaleTimeRangeSec) * 1000) - (hold_2_TimeSec * 1000);

                    actionCommandTextView.setText("Hold");
                    actionTimerTextView.setText(holdTimerSec + "");

//                    currentActionProgressBar.setProgress((int) (holdTimerMillis/ (cycleTimeSec * 1000)));
                }

                long progressInMillisBy10 = (millisElapsedTotal / 10) - (currentCycleNumber * cycleTimeSec * 100);
//                currentActionProgressBar.setProgress((int) progressInMillisBy10);

                timerProgressBar.setProgress((int) (millisElapsedTotal / 10));
                clockTextView.setText(getTimeMinutesString((int) millisElapsedTotal / 1000));
            }

            @Override
            public void onFinish() {
                breathingState = BreathingState.PAUSED;
                actionCommandTextView.setText("Start Again");
                actionTimerTextView.setVisibility(View.INVISIBLE);
                breathingState = BreathingState.NEW_TIMER;
                timerProgressBar.setProgress((int) timerTimeOnSpinnerSec * 100);
                beepSound.stop();
                endSound.start();

//                currentActionProgressBar.setProgress((int) cycleTimeSec * 100);
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

        return minutes + ":" + secondsString;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(breathingState != BreathingState.PAUSED  && breathingState != BreathingState.NEW_TIMER) {
            pauseTimer();
        }
    }
}