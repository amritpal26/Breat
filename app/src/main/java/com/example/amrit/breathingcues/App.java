package com.example.amrit.breathingcues;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

public class App extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final int REQUEST_CODE_REMINDER_1 = 100;
    private final int REQUEST_CODE_REMINDER_2 = 200;
    private final String CHANNEL_ID = "Reminders";
    private final String PREF_REMINDER_1_KEY = "timePref_reminder_1";
    private final String PREF_REMINDER_2_KEY = "timePref_reminder_2";
    private final String PREF_IS_REMINDER_1_KEY = "reminder_1";
    private final String PREF_IS_REMINDER_2_KEY = "reminder_2";
    SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle the preference change here
        setupReminders();
    }

    private void setupReminders(){
        // Create notifications
        Log.i("CHANGE", "yes");
        createNotificationChannel();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isReminderOneEnabled = preferences.getBoolean(PREF_IS_REMINDER_1_KEY, false);
        boolean isReminderTwoEnabled = preferences.getBoolean(PREF_IS_REMINDER_2_KEY, false);

        long reminderOneMillis = preferences.getLong(PREF_REMINDER_1_KEY, 0);
        long reminderTwoMillis = preferences.getLong(PREF_REMINDER_2_KEY, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), ReminderReceiver.class);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(getApplicationContext(),
                REQUEST_CODE_REMINDER_1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(),
                REQUEST_CODE_REMINDER_2, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(isReminderOneEnabled) {
            alarmManager.cancel(pendingIntent1);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, AlarmManager.INTERVAL_DAY, reminderOneMillis, pendingIntent1);
        }
        else{
            alarmManager.cancel(pendingIntent1);
        }

        if(isReminderTwoEnabled) {
            alarmManager.cancel(pendingIntent2);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, AlarmManager.INTERVAL_DAY, reminderTwoMillis, pendingIntent2);
        }
        else{
            alarmManager.cancel(pendingIntent2);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reminder sound";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
