package com.jsmobile.plugins.sms;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

/**
 * This class echoes a string called from JavaScript.
 */
//Changed from CDNsms to sms
public class Sms extends CordovaPlugin {
    private static final String SMS_GENERAL_ERROR = "SMS_GENERAL_ERROR";
    private static final String NO_SMS_SERVICE_AVAILABLE = "NO_SMS_SERVICE_AVAILABLE";
    private static final String SMS_FEATURE_NOT_SUPPORTED = "SMS_FEATURE_NOT_SUPPORTED";
    private static final String SENDING_SMS_ID = "SENDING_SMS";
    private static final int SMS_REQUEST_CODE = 101;
    private String SMS_ACTION = "";
    
    

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
        SMS_ACTION = action;
        
        if ( ContextCompat.checkSelfPermission( getActivity(), new String[] {  android.Manifest.permission.SEND_SMS,android.Manifest.permission.WRITE_SMS} ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( getActivity(), new String[] {  android.Manifest.permission.SEND_SMS,android.Manifest.permission.WRITE_SMS}, SMS_REQUEST_CODE);
            return false;
        }
        return sendSMSMessage(SMS_ACTION);
    }

    private void sendSMS(String phoneNumber, String message, final CallbackContext callbackContext) throws JSONException {
        PendingIntent sentPI = PendingIntent.getBroadcast(getActivity(), 0, new Intent(SENDING_SMS_ID), 0);

        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                case Activity.RESULT_OK:
                    callbackContext.sendPluginResult(new PluginResult(Status.OK, "SMS message is sent successfully"));
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    try {
                        JSONObject errorObject = new JSONObject();
                        
                        errorObject.put("code", NO_SMS_SERVICE_AVAILABLE);
                        errorObject.put("message", "SMS is not sent because no service is available");
                        
                        callbackContext.sendPluginResult(new PluginResult(Status.ERROR, errorObject));   
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                    break;
                default:
                    try {
                        JSONObject errorObject = new JSONObject();
                        
                        errorObject.put("code", SMS_GENERAL_ERROR);
                        errorObject.put("message", "SMS general error");
                        
                        callbackContext.sendPluginResult(new PluginResult(Status.ERROR, errorObject));
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                    
                    break;
                }
            }
        }, new IntentFilter(SENDING_SMS_ID));

        SmsManager sms = SmsManager.getDefault();
        
        sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        
        if(requestCode = SMS_REQUEST_CODE) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    sendSMSMessage(SMS_ACTION);
                }  else {
                    JSONObject errorObject = new JSONObject();
      
                    errorObject.put("code", "Permission");
                    errorObject.put("message", "SMS feature Permission is denied");

                    callbackContext.sendPluginResult(new PluginResult(Status.ERROR, errorObject));
                }
                return;
        }
            // Other 'case' lines to check for other
            // permissions this app might request.
        
    }

    private boolean sendSMSMessage(String action){
        if (action.equals("sendMessage")) {
            String phoneNumber = args.getString(0);
            String message = args.getString(1);
            
            boolean isSupported = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
            
            if (! isSupported) {
                JSONObject errorObject = new JSONObject();
                
                errorObject.put("code", SMS_FEATURE_NOT_SUPPORTED);
                errorObject.put("message", "SMS feature is not supported on this device");
                
                callbackContext.sendPluginResult(new PluginResult(Status.ERROR, errorObject));
                return false;
            }
            
            this.sendSMS(phoneNumber, message, callbackContext);
            
            return true;
        }
        
        return false;
    }
    
    private Activity getActivity() {
        return this.cordova.getActivity();
    }
}

