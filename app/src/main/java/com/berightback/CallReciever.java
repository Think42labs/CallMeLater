package com.berightback;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Created by Think42Labs on 6/2/16.
 */
//
public class CallReciever extends BroadcastReceiver {
    private static String previousState = null;
    private ContentResolver mResolver;
    private SharedPreferences sharedPreferences;
    public static String message;
    private static String number;
    public static boolean doFillMessage = false;


    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences = context.getSharedPreferences("myPrefs", context.MODE_PRIVATE);
        mResolver = context.getApplicationContext()
                .getContentResolver();
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String msg = "Phone state " + state + " ";
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            CallReciever.previousState = TelephonyManager.EXTRA_STATE_RINGING;
            number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            msg += intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state) && TelephonyManager.EXTRA_STATE_RINGING.equals(CallReciever.previousState)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Permission not granted", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Cursor managedCursor = mResolver.query(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.NUMBER + " = ? ",
                    new String[]{number}, CallLog.Calls.DATE + " DESC");

            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            if (managedCursor.getCount() > 0) {
                managedCursor.moveToFirst();
                if (managedCursor.getInt(type) != CallLog.Calls.MISSED_TYPE) {
                    message = sharedPreferences.getString("text","I am busy right now will call u later!");
                    String contactName = getContactName(number,context);
                    message = contactName+", "+message;

                    String contactNum = number;
                    if (contactNum.charAt(0) == '+') {
                        contactNum = contactNum.substring(1, contactNum.length());
                    }
                    try{
                        if(sharedPreferences.getInt("radio",R.id.whatsappRadio)==R.id.whatsappRadio){
                            //Todo check for network connection and choose messenging as default
                            openWhatsApp(context, contactNum + "@s.whatsapp.net");
                            doFillMessage = true;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    doFillMessage = false;
                                }
                            },5000);
                        }else{
                            openSms(context,message,contactNum);
                        }


                    }catch (Exception e){
                        Log.e("Call reciever",e.getMessage());
                        Toast.makeText(context,"Whatsapp not installed",Toast.LENGTH_SHORT).show();
                    }

                    CallReciever.previousState = null;
                    String remind = context.getResources().getStringArray(R.array.reninderDelays)[sharedPreferences.getInt("remind", 0)];
                    int minsToRemindAfter = Integer.parseInt(remind.split(" ")[0]);
                    long startTime = (new Date()).getTime() + (minsToRemindAfter * 1000 * 60);
                    long endTime = (new Date()).getTime() + (10 * 1000 * 60);
                    Hashtable calenders = CalendarHelper.listCalendarId(context);
                    if(calenders==null){
                        return;
                    }
                    Enumeration enu = calenders.keys();
                    String reminderName = contactNum+"";
                    if(contactName.length() > 0){
                        reminderName = contactName;
                    }
                    if(enu.hasMoreElements()){
                        CalendarHelper.MakeNewCalendarEntry(context,"Call "+ reminderName +" back","Call "+contactName+" back",null,startTime,endTime,false,true,1,2);
                    }

                }

            }
        } else {
            CallReciever.previousState = null;
        }
    }

    private void openWhatsApp(Context ctx, String id) {

        Cursor c = ctx.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Contacts.Data._ID}, ContactsContract.Data.DATA1 + "=?",
                new String[]{id}, null);
        if (c.moveToFirst()) {


        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.contacts/data/" + c.getString(0)));

        ctx.startActivity(i
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        } else{
            Toast.makeText(ctx,"Contact not added or not a whatsapp number",Toast.LENGTH_SHORT).show();
        }
        c.close();
    }

    private void openSms(Context ctx, String message, String number){
        Uri uri = Uri.parse("smsto:"+number);
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        it.putExtra("sms_body", message);
        ctx.startActivity(it
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    /*
     * Returns contact's id
     */
    private String getContactName(String phoneNumber, Context context) {
        ContentResolver mResolver = context.getContentResolver();

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));

        Cursor cursor = mResolver.query(uri, new String[]{
                ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);

        String contactId = "";

        if (cursor.moveToFirst()) {
            do {
                contactId = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            } while (cursor.moveToNext());
        }

        cursor.close();
        cursor = null;
        return contactId;
    }

}
