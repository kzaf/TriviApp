package com.zaf.triviapp.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    SharedPreferences mySharedPref;

    public SharedPref(Context context) {
        mySharedPref = context.getSharedPreferences("Trivi_App_Settings", Context.MODE_PRIVATE);
    }

    // This will save the night mode: True/False
    public void setNightModeEnabled(Boolean state){
        mySharedPref.edit().putBoolean("NightMode", state).apply();
    }

    // This will load the Night Mode State
    public Boolean loadNightModeState(){
        return mySharedPref.getBoolean("NightMode", false);
    }

    public void setVibrateEnabled(Boolean state){
        mySharedPref.edit().putBoolean("Vibrate", state).apply();
    }

    public Boolean loadVibrateState(){
        return mySharedPref.getBoolean("Vibrate", true);
    }

}
