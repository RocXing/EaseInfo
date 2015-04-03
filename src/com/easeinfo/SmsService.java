package com.easeinfo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class SmsService extends Service {
	
	private static final String TAG = "SmsService";
	private Uri SMS_INBOX = Uri.parse("content://sms/");
	private SmsObserver smsObserver;
	

	public static PendingIntent pi = null;
	public static AlarmManager am = null;
	
	
	@Override
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
		Log.i(TAG, "SmsService onCreate called.");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		smsObserver = new SmsObserver(this, smsHandler);
		getContentResolver().registerContentObserver(SMS_INBOX, true, smsObserver);
		Log.i(TAG, "SmsService onStartCommand called.");
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		getContentResolver().unregisterContentObserver(smsObserver);
		cleanAlarmManager();
		Intent it = new Intent(SmsService.this, SmsService.class);
		startService(it);
	}

	@Override
	public IBinder onBind(Intent arg0) {
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
	
	public Handler smsHandler = new Handler() {
		//这里可以进行回调的操作
		//TODO

	};
	
	class SmsObserver extends ContentObserver {

		private Intent service;
		private Bundle bundle;
		private Context ct;
		
		public SmsObserver(Context context, Handler handler) {
			super(handler);
			ct = context;
			service = new Intent(ct, TaskHandler.class);
			bundle = new Bundle();
			bundle.putInt("status",TaskHandler.UPLOAD_SMS);
			service.putExtras(bundle);
			Log.i(TAG, "SmsObserver Init");
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Log.i(TAG, "SmsService onChange");
			//每当有新短信到来时，使用我们获取短消息的方法
			//if(am == null)
			//{
				Log.i(TAG, "here");
				
				pi = PendingIntent.getService(ct, TaskHandler.UPLOAD_SMS, service, PendingIntent.FLAG_ONE_SHOT);
				am = (AlarmManager) ct.getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + Config.TIME_DELAY, pi);
				
			//}
			
		}
	}

}
