package com.berightback;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "Callmelater";
    private SharedPreferences sharedPreferences;
    private CallReciever receiver;
    private Spinner spinner1;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CALL_LOG)!=PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_CALENDAR)!=PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CALENDAR)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG,Manifest.permission.READ_CONTACTS,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR},22);
        }
        sharedPreferences = getSharedPreferences("myPrefs", this.MODE_PRIVATE);
        final View controlsView = findViewById(R.id.controls);
        final Switch serviceSwitch = (Switch) findViewById(R.id.switch1);
        final EditText msgField = (EditText) findViewById(R.id.msgField);
        spinner1 = (Spinner) findViewById(R.id.spinner);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new custonRadioCheckedListener());
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        // Set preferences
        String msg = sharedPreferences.getString("text","I am busy right now will call u later!");
        msgField.setText(msg);
        msgField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("text", msgField.getText().toString());
                editor.commit();
                return false;
            }
        });

        radioGroup.check(sharedPreferences.getInt("radio",R.id.whatsappRadio));
        if(sharedPreferences.getInt("remind",-1)==-1) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("remind", 0);
            editor.commit();
        } else {
            spinner1.setSelection(sharedPreferences.getInt("remind", 0));
        }
        if(!sharedPreferences.getBoolean("serviceEnabled",false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("serviceEnabled", false);
            serviceSwitch.setChecked(false);
            editor.commit();
            controlsView.setVisibility(View.INVISIBLE);
        }else{
            if(!isAccessibilityEnabled()){
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                dialog.setTitle("Turn On");
                dialog.setMessage("Please Enable Accessibility service for BeRightBack to help you");
                dialog.show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("serviceEnabled", false);
                serviceSwitch.setChecked(false);
                editor.commit();
                controlsView.setVisibility(View.INVISIBLE);
            }else{
                serviceSwitch.setChecked(true);
                controlsView.setVisibility(View.VISIBLE);
            }
        }

        //UI controlls
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(!isAccessibilityEnabled()){
                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                        dialog.setTitle("Turn On");
                        //Todo set buttons in dialog navigate to settings accessibility screen
                        dialog.setMessage("Please Enable Accessibility service for BeRightBack to help you");
                        dialog.show();
                        serviceSwitch.setChecked(false);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("serviceEnabled", false);
                        editor.commit();
                        controlsView.setVisibility(View.INVISIBLE);
                        return;
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("serviceEnabled", true);
                    editor.commit();
                    controlsView.setVisibility(View.VISIBLE);
                    ComponentName component=new ComponentName(MainActivity.this, CallReciever.class);

                    getPackageManager()
                            .setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                    PackageManager.DONT_KILL_APP);
                }else{
                    controlsView.setVisibility(View.INVISIBLE);
                    ComponentName component=new ComponentName(MainActivity.this, CallReciever.class);

                    getPackageManager()
                            .setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("serviceEnabled", false);
                    editor.commit();
                }
            }
        });

    }

    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        String firstItem = String.valueOf(spinner1.getSelectedItem());

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("remind", pos);
            editor.commit();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg) {

        }

    }

    /* function checks if accessibility is enabled or not*/
    public boolean isAccessibilityEnabled(){

        int accessibilityEnabled = 0;
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(LOGTAG, "ACCESSIBILITY: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(LOGTAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled==1){
            Log.d(LOGTAG, "***ACCESSIBILIY IS ENABLED***: ");


            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Log.d(LOGTAG, "Setting: " + settingValue);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    Log.d(LOGTAG, "Setting: " + accessabilityService);
                    // Check if our app service is enabled
                    if (accessabilityService.equalsIgnoreCase("com.berightback/com.berightback.MyWindowAccessibilityService")){
                        Log.d(LOGTAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }

            Log.d(LOGTAG, "***END***");
        }
        else{
            Log.d(LOGTAG, "***ACCESSIBILIY IS DISABLED***");
        }
        return accessibilityFound;
    }

    private class custonRadioCheckedListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("radio", checkedId);
            editor.commit();
        }
    }
}
