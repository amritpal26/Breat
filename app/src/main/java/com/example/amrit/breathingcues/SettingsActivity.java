package com.example.amrit.breathingcues;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFERENCE_KEY_SOUND_SWITCH = "pref_sound";
    private final static String PREFERENCE_KEY_VIBRATION_SWITCH = "pref_vibration";

//    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        return intent;
    }
}