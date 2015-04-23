package com.easeinfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.FontAwesomeText;

public class Setting extends Activity {

	public SharedPreferences sharedPreferences;;
	public Editor editor;
	
	public Boolean remoteConfig;
	public Boolean synConfig;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		Intent service = new Intent(this, MissedCallService.class);
		startService(service);
		service = new Intent(this, SmsService.class);
		startService(service);
		PushService.actionStart(getApplicationContext());
		Log.i("Login", "Starting Service");
		
		
		final ToggleButton mTogBtn1 = (ToggleButton) findViewById(R.id.togbtn1);
		final ToggleButton mTogBtn2 = (ToggleButton) findViewById(R.id.togbtn2);
		final ToggleButton mTogBtn3 = (ToggleButton) findViewById(R.id.togbtn3);
		final ToggleButton mTogBtn4 = (ToggleButton) findViewById(R.id.togbtn4);
		final ToggleButton mTogBtn5 = (ToggleButton) findViewById(R.id.togbtn5);
		final RelativeLayout rl1 = (RelativeLayout) findViewById(R.id.rl1); 
		final RelativeLayout rl2 = (RelativeLayout) findViewById(R.id.rl2); 
		final RelativeLayout rl3 = (RelativeLayout) findViewById(R.id.rl3); 
		final BootstrapButton logout = (BootstrapButton) findViewById(R.id.logout); 
		final BootstrapButton addresssyn = (BootstrapButton) findViewById(R.id.addresssyn);
		TextView msg1 = (TextView) findViewById(R.id.msg1);
		TextView msg2 = (TextView) findViewById(R.id.msg2);
		
		sharedPreferences = getSharedPreferences(Config.DB_NAME, Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		
		synConfig = sharedPreferences.getBoolean("SYN_CONFIG", true);
		remoteConfig = sharedPreferences.getBoolean("REMOTE_CONFIG", true);
		Boolean smsSyn = sharedPreferences.getBoolean("SMS_SYN", true);
		Boolean phoneSyn = sharedPreferences.getBoolean("PHONE_SYN", true);
		Boolean addressSyn = sharedPreferences.getBoolean("ADDRESS_SYN", true);
		int msgnum1 = sharedPreferences.getInt("SMS_SYN_NUM", 0);
		int msgnum2 = sharedPreferences.getInt("PHONE_SYN_NUM", 0);
		Log.i("msgnum1", String.valueOf(msgnum1));
		Log.i("msgnum2", String.valueOf(msgnum2));
		
		msg1.setText("已累计上传"+msgnum1+"条短信");
		msg2.setText(""+msgnum2+"条未接来电");
		
		mTogBtn1.setChecked(synConfig);
		mTogBtn2.setChecked(smsSyn);
		mTogBtn3.setChecked(phoneSyn);
		mTogBtn4.setChecked(addressSyn);
		mTogBtn5.setChecked(remoteConfig);
		
		if(!synConfig && !remoteConfig)
		{
			rl1.setVisibility(View.INVISIBLE);
			rl2.setVisibility(View.INVISIBLE);
			rl3.setVisibility(View.INVISIBLE);
		}
		
		
		logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(Setting.this, "Logout successful, please login", Toast.LENGTH_LONG).show();
				Intent it = new Intent(Setting.this, Login.class);
				sharedPreferences = getSharedPreferences(Config.DB_NAME, Context.MODE_PRIVATE);
				editor = sharedPreferences.edit();
				editor.putString("token", null);
				editor.putString(PushService.PREF_DEVICE_ID, null);
				editor.putInt("SMS_SYN_NUM", 0);
				editor.putInt("PHONE_SYN_NUM", 0);
				editor.commit();
				startActivity(it);
				PushService.actionStop(getApplicationContext());
				finish();
				
			}
		});
		
		addresssyn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(Setting.this, "Uploading address list", Toast.LENGTH_LONG).show();
				Intent it = new Intent(Setting.this,TaskHandler.class);
				Bundle bundle = new Bundle();
				bundle.putInt("status", TaskHandler.UPLOAD_ADDRESS_LIST);
				it.putExtras(bundle);
				startService(it);
				
			}
		});
		
		mTogBtn1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					//选中			
        			editor.putBoolean("SYN_CONFIG", true);
        			synConfig = true;
        			editor.commit();
        			rl1.setVisibility(View.VISIBLE);
        			rl2.setVisibility(View.VISIBLE);
        			rl3.setVisibility(View.VISIBLE);
        			
				}else{
					//未选中
					editor.putBoolean("SYN_CONFIG", false);
					synConfig = false;
        			editor.commit();
        			if(!remoteConfig){
	        			rl1.setVisibility(View.INVISIBLE);
	        			rl2.setVisibility(View.INVISIBLE);
	        			rl3.setVisibility(View.INVISIBLE);
        			}
				}
			}
		});// 添加监听事件
		
		mTogBtn5.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					//选中			
        			editor.putBoolean("REMOTE_CONFIG", true);
        			remoteConfig = true;
        			editor.commit();
        			rl1.setVisibility(View.VISIBLE);
        			rl2.setVisibility(View.VISIBLE);
        			rl3.setVisibility(View.VISIBLE);
        			
				}else{
					//未选中
					editor.putBoolean("REMOTE_CONFIG", false);
					remoteConfig = false;
        			editor.commit();
        			if(!synConfig){
	        			rl1.setVisibility(View.INVISIBLE);
	        			rl2.setVisibility(View.INVISIBLE);
	        			rl3.setVisibility(View.INVISIBLE);
        			}
				}
			}
		});// 添加监听事件
		
		mTogBtn2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					//选中			
        			editor.putBoolean("SMS_SYN", true);
        			editor.commit();
				}else{
					//未选中
					editor.putBoolean("SMS_SYN", false);
        			editor.commit();
				}
			}
		});// 添加监听事件
		
		mTogBtn3.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					//选中			
        			editor.putBoolean("PHONE_SYN", true);
        			editor.commit();
				}else{
					//未选中
					editor.putBoolean("PHONE_SYN", false);
        			editor.commit();
				}
			}
		});// 添加监听事件

		mTogBtn4.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					//选中			
        			editor.putBoolean("ADDRESS_SYN", true);
        			editor.commit();
				}else{
					//未选中
					editor.putBoolean("ADDRESS_SYN", false);
        			editor.commit();
				}
			}
		});// 添加监听事件
		
	}

}