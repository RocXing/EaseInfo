package com.easeinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {
	
	private static final String TAG = "BootCompleteReceiver";
	public static Context context;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		BootCompleteReceiver.context = context;
		SharedPreferences sharedPreferences = context.getSharedPreferences(Config.DB_NAME, Context.MODE_PRIVATE);
		Boolean synConfig = sharedPreferences.getBoolean("SYN_CONFIG", true);
		Boolean smsSyn = sharedPreferences.getBoolean("SMS_SYN", true);
		Boolean phoneSyn = sharedPreferences.getBoolean("PHONE_SYN", true);
		Boolean remoteConfig = sharedPreferences.getBoolean("REMOTE_CONFIG", true);
		
		//Intent service = new Intent(context, MonitorService.class);
		//context.startService(service);
		Log.i(TAG, "Boot Complete. Starting Service");
		Intent service;
		
		
		if(synConfig && smsSyn){
    		//Log.i(TAG, "it's good");
    		service = new Intent(context, SmsService.class);
    		context.startService(service);
    	}
    	
    	if(synConfig && phoneSyn){
    		
    		service = new Intent(context, MissedCallService.class);
    		context.startService(service);
    		
    	}
    	
    	if(remoteConfig){
    		new Thread()
    		{
    			public void run()
    			{
    				PushService.actionStart(BootCompleteReceiver.context);
    				Log.i("Login", "Starting Service");
    			}
    		}.start();
    	}
	}

}

