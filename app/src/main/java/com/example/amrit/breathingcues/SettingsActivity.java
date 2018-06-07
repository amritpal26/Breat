package com.example.amrit.breathingcues;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private final String PREFERENCE_KEY_SOUND_SWITCH = "pref_sound";
    private final String PREFERENCE_KEY_VIBRATION_SWITCH = "pref_vibration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean sound = sharedPreferences.getBoolean(PREFERENCE_KEY_SOUND_SWITCH, false);
        boolean vibration = sharedPreferences.getBoolean(PREFERENCE_KEY_VIBRATION_SWITCH, false);

        Toast.makeText(this, "sound: " + sound + "Vibration: " + vibration, Toast.LENGTH_SHORT).show();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        return intent;
    }


}
