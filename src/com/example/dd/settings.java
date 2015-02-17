package com.example.dd;

import android.os.Bundle;
import android.preference.PreferenceActivity;
 
public class settings extends PreferenceActivity {
 
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        addPreferencesFromResource(R.xml.settings);
 
    }
}
