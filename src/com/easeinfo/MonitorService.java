package com.easeinfo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class MonitorService extends Service {

	private static final String TAG = "MonitorService";
	private ScreenBroadcastReceiver mScreenReceiver;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate called.");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand called.");
		
		mScreenReceiver = new ScreenBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
	    filter.addAction(Intent.ACTION_SCREEN_ON);
	    filter.addAction(Intent.ACTION_SCREEN_OFF);
	    filter.addAction(Intent.ACTION_USER_PRESENT);
	    registerReceiver(mScreenReceiver, filter);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(mScreenReceiver);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	
	private class ScreenBroadcastReceiver extends BroadcastReceiver {
	    private String action = null;
	    private Intent service = null;
	    
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        action = intent.getAction();
	        
	        if (Intent.ACTION_SCREEN_ON.equals(action)) {   
	            // 开屏
	        	Log.i(TAG, "开屏");
	        	if(Config.SMS_SYN){
		        	service = new Intent(context, SmsService.class);
	        		stopService(service);
	        	}
	        	if(Config.PHONE_SYN){
	        		service = new Intent(context, MissedCallService.class);
	        		stopService(service);
	        	}
	        	
	        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
	            //锁屏
	        	if(Config.SMS_SYN){
	        		//Log.i(TAG, "it's good");
	        		service = new Intent(context, SmsService.class);
	        		startService(service);
	        	}
	        	
	        	if(Config.PHONE_SYN){
	        		
	        		service = new Intent(context, MissedCallService.class);
	        		startService(service);
	        		
	        	}
	        	
	        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
	            // 解锁
	        	Log.i(TAG, "解锁");
	        	if(Config.SMS_SYN){
		        	service = new Intent(context, SmsService.class);
	        		stopService(service);
	        	}
	        	if(Config.PHONE_SYN){
	        		service = new Intent(context, MissedCallService.class);
	        		stopService(service);
	        	}
	        }
	    }
	}
}

