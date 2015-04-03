package com.easeinfo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.util.Log;

public class MissedCallService extends Service {

	private static final String TAG = "MissedCallService";
	private MissedCallObserver missedCallObserver;
	
	public static PendingIntent pi = null;
	public static AlarmManager am = null;
	
	public void onCreate() {
		super.onCreate();
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);    
		Notification notification = new Notification.Builder(this)
								.setSmallIcon(R.drawable.ic_launcher)
								.setTicker("EaseInfo服务运行中")
								.setContentTitle("EaseInfo")
								.setContentText("EaseInfo服务运行中")
								.setContentIntent(pendingIntent)
								.setWhen(System.currentTimeMillis()).build();
		notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
		this.startForeground(1000, notification);
		Log.i(TAG, "MissedCallService onCreate called.");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		missedCallObserver = new MissedCallObserver(this, phoneHandler);
		getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, false, missedCallObserver);   
		Log.i(TAG, "MissedCallService onStartCommand called.");
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		getContentResolver().unregisterContentObserver(missedCallObserver);
		cleanAlarmManager();
		Intent it = new Intent(MissedCallService.this, MissedCallService.class);
		startService(it);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void cleanAlarmManager()
	{
		if(am != null)
		{
			am.cancel(pi);
		}
		if(pi != null)
		{
			pi.cancel();
		}
		am = null;
		pi = null;
	}
	
	public Handler phoneHandler = new Handler() {
		//这里可以进行回调的操作
		//TODO

	};
	
	public class MissedCallObserver extends ContentObserver 
	{
		
		private Intent service;
		private Bundle bundle;
		private Context ct;
		
	    public MissedCallObserver(Context context, Handler handler) {
	        super(handler);
	        ct = context;
	        service = new Intent(ct, TaskHandler.class);
			bundle = new Bundle();
			bundle.putInt("status",TaskHandler.UPLOAD_MISSED_CALL);
			service.putExtras(bundle);
			Log.i(TAG, "MissedCallObserver Init");
	    }
	  
	    @Override
	    public void onChange(boolean selfChange) {
	    	super.onChange(selfChange);
	    	Log.i(TAG, "MissedCallService onChange");
			//if(am == null)
			//{
				Log.i(TAG, "here");
				pi = PendingIntent.getService(ct, TaskHandler.UPLOAD_MISSED_CALL, service, PendingIntent.FLAG_ONE_SHOT);
				am = (AlarmManager) ct.getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + Config.TIME_DELAY, pi);
				
			//}
	        
	    }
	    
	    @Override    
	    public boolean deliverSelfNotifications() {
	    	return super.deliverSelfNotifications();
	    }     

	}


}
