package com.berightback;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.berightback.CallReciever;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Think42Labs on 6/8/16.
 */
public class MyWindowAccessibilityService extends AccessibilityService {


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("Passing data",  "Event recieved: "+event.getEventType());
        AccessibilityNodeInfo source = event.getSource();
        if (source == null||!CallReciever.doFillMessage) {
            return;
        }
        AccessibilityNodeInfo foundSource = null;
            if (source.getPackageName().equals("com.whatsapp")){
                Log.d("Passing data",  "Whatsapp Event recieved");
                foundSource = findNode(getRootInActiveWindow());

                if(foundSource!=null) {
                    if(foundSource.getText()!=null){
                        if(foundSource.getText().toString().equals(CallReciever.message)){
                            return;
                        }
                    }

                    ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("MyAppName", CallReciever.message);
                    clipboardManager.setPrimaryClip(clipData);
                    // TODO: Action paste is no supported below API 18
                    foundSource.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    Log.d("Passing data",  CallReciever.message);
                    CallReciever.doFillMessage = false;
                }
            }
    }

    private AccessibilityNodeInfo findNode(AccessibilityNodeInfo source){
        AccessibilityNodeInfo foundSource = null;
        if(source!=null)
            for (int i = 0;i<source.getChildCount();i++){
                if(source.getChild(i).getClassName().toString().equals("android.widget.EditText")){
                    foundSource = source.getChild(i);
                    return foundSource;
                }else{
                    AccessibilityNodeInfo temp = findNode(source.getChild(i));
                    if(temp!=null){
                        return temp;
                    }
                }
            }
        return foundSource;
    }
    @Override public void onInterrupt() {
        Log.d("TaAG","interrupted");
    }
}