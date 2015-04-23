package com.easeinfo;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CallLog.Calls;
import android.provider.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class TaskHandler extends Service {
	
	public int status;
	
	public static int DEFAULT = 0;
	public static int UPLOAD_SMS = 1;
	public static int UPLOAD_MISSED_CALL = 2;
	public static int UPLOAD_ADDRESS_LIST = 3;
	public static int UPLOAD_SMS_MISSED_CALL = 4;
	
	public Boolean smsSyn;
	public Boolean phoneSyn;
	
	public RequestQueue requestQueue;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("TAG", "TaskHandler onCreate called.");
		requestQueue = Volley.newRequestQueue(getApplicationContext());
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("TAG", "TaskHandler onStartCommand called.");
		try{
			Bundle bundle = intent.getExtras();
			status = bundle.getInt("status");
		}catch(Exception e){
			status = DEFAULT;
		}
		
		String token = getUserToken();
		SharedPreferences sharedPreferences = getSharedPreferences(Config.DB_NAME, Context.MODE_PRIVATE);
		Boolean synConfig = sharedPreferences.getBoolean("SYN_CONFIG", true);
		Boolean remoteConfig = sharedPreferences.getBoolean("REMOTE_CONFIG", true);
		smsSyn = sharedPreferences.getBoolean("SMS_SYN", true);
		phoneSyn = sharedPreferences.getBoolean("PHONE_SYN", true);
		Boolean addressSyn = sharedPreferences.getBoolean("ADDRESS_SYN", true);
		
		if(token==null) return super.onStartCommand(intent, flags, startId);
		
		if(status == UPLOAD_SMS && synConfig && smsSyn)
		{
			Log.i("TAG", "sms.");
			getSmsFromPhone();
			SmsService.cleanAlarmManager();
		}
		else if(status == UPLOAD_MISSED_CALL && synConfig && phoneSyn)
		{
			Log.i("TAG", "phone.");
			getMissedCallFromPhone();
			MissedCallService.cleanAlarmManager();
			
		}
		else if(status == UPLOAD_ADDRESS_LIST)
		{
			getAddressList();
		}
		else if(status == UPLOAD_SMS_MISSED_CALL && remoteConfig)
		{
			getSmsMissedCall();
		}
		else if(status == DEFAULT)
		{
			
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getUserToken()
	{
		SharedPreferences sharedPreferences = getSharedPreferences(Config.DB_NAME, Context.MODE_PRIVATE);
		String token = sharedPreferences.getString("token", null);
		return token;
	}
	
	public void NumIncrease(String key)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(Config.DB_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		int number = sharedPreferences.getInt(key, 0);
		number++;
		editor.putInt(key, number);
		editor.commit();
		Log.i(key, String.valueOf(number));
		
	}
	
	public void getSmsFromPhone()
	{
		Uri SMS_INBOX = Uri.parse("content://sms/");
		String[] projection = new String[] { "address","body","person","date", "read" };//"_id", "address", "person",, "date", "type
		String where = "date > "
				+ (System.currentTimeMillis() - Config.SMS_LATENCY * 1000);
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
		if (null == cur)
		{
			Log.v("status","empty");
			return;
		}
		if(cur.moveToNext()) 
		{
			int read = cur.getInt(cur.getColumnIndex("read"));
			if(read == 0)
			{
				
				String number = cur.getString(cur.getColumnIndex("address"));//手机号
				String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
				String body = cur.getString(cur.getColumnIndex("body"));
				String date = cur.getString(cur.getColumnIndex("date"));
				
				Log.v("number",number);
				Log.v("date",date);
				if(name == null){
					name = "陌生人";
				}
				Log.v("name",name);
				Log.v("body",body);
				
				Map<String, String> map = new HashMap<String, String>();  
				map.put("sender_phone", number);
				map.put("sender_name", name);
				map.put("content", body);
				map.put("status", "P");
				map.put("date", date);
				
				JSONObject jsonObject = new JSONObject(map);
				Log.d("jsonObject", jsonObject.toString());
				
				JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Config.messagesURL, jsonObject,
					    new Response.Listener<JSONObject>() {
					        @Override
					        public void onResponse(JSONObject response) {
					        	
					        	Log.d("TAG", response.toString());
					        	NumIncrease("SMS_SYN_NUM");
					        	try{
					        		int status = response.getInt("status");
					        		if(status == 1)
					        		{
					        			JSONObject message = response.getJSONObject("message");
					        		}
					        		else if(status == 0)
					        		{
					        			String error = response.getString("error");
					        		}
					        	
					        	}catch(Exception e){
					        		//error
					        	}
					        }
					    }, new Response.ErrorListener() {
					        @Override
					        public void onErrorResponse(VolleyError error) {
					        	Log.e("TAG", error.getMessage(), error);  
					        	//byte[] htmlBodyBytes = error.networkResponse.data;
					        	//Log.e("LOGIN-ERROR", new String(htmlBodyBytes), error);
					        }
					    })
					
					    {
					    @Override
					    public Map<String, String> getHeaders() {
					        HashMap<String, String> headers = new HashMap<String, String>();
					        //headers.put("Accept", "application/json");
					        headers.put("Content-Type", "application/json");
					        String token = getUserToken();
					        headers.put("Authorization", "Bearer " + token);
					        return headers;
					    }
					};
				if(isOpenNetwork()){
					requestQueue.add(jsonRequest);
				}
			}
		}
		cur.close();
	}
	
	public void getMissedCallFromPhone(){
		
		String[] projection = new String[] {Calls.NUMBER, Calls.NEW, Calls.DATE, Calls.CACHED_NAME, Calls.DURATION};
		String where = "type = 3 AND (date + duration) > "
				+ (System.currentTimeMillis() - Config.PHONE_LATENCY * 1000);
		ContentResolver cr = getContentResolver();
		Cursor csr = cr.query(Calls.CONTENT_URI, projection, where, null, Calls.DEFAULT_SORT_ORDER);
        if (csr != null) {
            if(csr.moveToNext()) {
                int callnew = csr.getInt(csr.getColumnIndex(Calls.NEW));
                String number = csr.getString(csr.getColumnIndex(Calls.NUMBER));
                String date = csr.getString(csr.getColumnIndex(Calls.DATE));
                String name = csr.getString(csr.getColumnIndex(Calls.CACHED_NAME));
                
                Log.v("duration", csr.getString(csr.getColumnIndex(Calls.DURATION)));
                
                if(callnew == 1) {
                	Log.v("Missedcall", date + " you have a missed call from " + number);
                	if(name == null)
                		name = "陌生人";
                	Log.v("Missedcall",name);
                	
                	Map<String, String> map = new HashMap<String, String>();  
    				map.put("caller_phone", number);
    				map.put("caller_name", name);
    				map.put("status", "P");
    				map.put("date", date);
    				
    				JSONObject jsonObject = new JSONObject(map);
    				Log.d("jsonObject", jsonObject.toString());
    				
    				JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Config.callsURL, jsonObject,
    					    new Response.Listener<JSONObject>() {
    					        @Override
    					        public void onResponse(JSONObject response) {
    					        	
    					        	Log.d("TAG", response.toString());
    					        	try{
    					        		int status = response.getInt("status");
    					        		NumIncrease("PHONE_SYN_NUM");
    					        		if(status == 1)
    					        		{
    					        			JSONObject message = response.getJSONObject("message");
    					        		}
    					        		else if(status == 0)
    					        		{
    					        			String error = response.getString("error");
    					        		}
    					        	
    					        	}catch(Exception e){
    					        		//error
    					        	}
    					        }
    					    }, new Response.ErrorListener() {
    					        @Override
    					        public void onErrorResponse(VolleyError error) {
    					        	Log.e("TAG", error.getMessage(), error);  
    					        	//byte[] htmlBodyBytes = error.networkResponse.data;
    					        	//Log.e("LOGIN-ERROR", new String(htmlBodyBytes), error);
    					        }
    					    })
    					
    					    {
    					    @Override
    					    public Map<String, String> getHeaders() {
    					        HashMap<String, String> headers = new HashMap<String, String>();
    					        //headers.put("Accept", "application/json");
    					        headers.put("Content-Type", "application/json");
    					        String token = getUserToken();
    					        headers.put("Authorization", "Bearer " + token);
    					        return headers;
    					    }
    					};
    					if(isOpenNetwork()){
    						requestQueue.add(jsonRequest);}
    					else{
    						//Toast.makeText(TaskHandler.this, "Network is not available", Toast.LENGTH_LONG).show();
    					}
                }
            }
            // release resource
            csr.close();
        }

		
	}
	
	public void getAddressList()
	{
		ContentResolver cr = getContentResolver();
		String[] projection = new String[] {Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID};
		Cursor cur = cr.query(Phone.CONTENT_URI, projection, null, null, Phone.DISPLAY_NAME);
		JSONArray jsonArray = new JSONArray();
		if(cur == null) {
			Toast.makeText(TaskHandler.this, "Address list is empty", Toast.LENGTH_LONG).show();
			return;
		}
		while(cur.moveToNext())
		{
			String number = cur.getString(cur.getColumnIndex(Phone.NUMBER));
			//当手机号码为空的或者为空字段 跳过当前循环
			if (TextUtils.isEmpty(number))
				number = "空";
			//得到联系人名称
			String name = cur.getString(cur.getColumnIndex(Phone.DISPLAY_NAME));
			
			Map<String, String> map = new HashMap<String, String>();  
			map.put("phone", number);
			map.put("name", name);
			
			JSONObject jsonObject = new JSONObject(map);
			Log.d("jsonObject", jsonObject.toString());
			jsonArray.put(jsonObject);
			
			
		}
		cur.close();
		
		Map<String, JSONArray> postmap = new HashMap<String, JSONArray>();  
		postmap.put("list", jsonArray);
		JSONObject postObject = new JSONObject(postmap);
		
		JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Config.contactsURL, postObject,
			    new Response.Listener<JSONObject>() {
			        @Override
			        public void onResponse(JSONObject response) {
			        	
			        	Log.d("TAG", response.toString());
			        	try{
			        		int status = response.getInt("status");
			        		if(status == 1)
			        		{
			        			Toast.makeText(TaskHandler.this, "Upload successful", Toast.LENGTH_LONG).show();
			        		}
			        		else if(status == 0)
			        		{
			        			Toast.makeText(TaskHandler.this, "Upload address list failed", Toast.LENGTH_LONG).show();
			        		}
			        	
			        	}catch(Exception e){
			        		//error
			        	}
			        	
			        }
			    }, new Response.ErrorListener() {
			        @Override
			        public void onErrorResponse(VolleyError error) {
			        	Log.e("TAG", error.getMessage(), error);  
			        	//byte[] htmlBodyBytes = error.networkResponse.data;
			        	//Log.e("LOGIN-ERROR", new String(htmlBodyBytes), error);
			        	Toast.makeText(TaskHandler.this, "Upload address list failed", Toast.LENGTH_LONG).show();
			        		
			        	}
			        })
			
			{
			    @Override
			    public Map<String, String> getHeaders() {
			        HashMap<String, String> headers = new HashMap<String, String>();
			        //headers.put("Accept", "application/json");
			        headers.put("Content-Type", "application/json");
			        String token = getUserToken();
			        headers.put("Authorization", "Bearer " + token);
			        return headers;
			    }
			};
			
		if(isOpenNetwork()){
			requestQueue.add(jsonRequest);}
		else{
			Toast.makeText(TaskHandler.this, "Network is not available", Toast.LENGTH_LONG).show();
		}
	}
	
	public void getSmsMissedCall()
	{
		
		Map<String, JSONArray> mainmap = new HashMap<String, JSONArray>();  
		
		//smsSyn、phoneSyn
		if(smsSyn)
		{
			Uri SMS_INBOX = Uri.parse("content://sms/");
			String[] projection = new String[] { "address","body","person","date", "read" };//"_id", "address", "person",, "date", "type
			String where = "date > "
					+ (System.currentTimeMillis() - Config.UPLOAD_LATENCY * 1000);
			ContentResolver cr = getContentResolver();
			Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
			JSONArray jsonArray = new JSONArray();
			if (null == cur)
			{
				Log.v("sms","empty");
				
			}
			else
			{
				while(cur.moveToNext()) 
				{
					int read = cur.getInt(cur.getColumnIndex("read"));
					if(read == 0)
					{
						
						String number = cur.getString(cur.getColumnIndex("address"));//手机号
						String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
						String body = cur.getString(cur.getColumnIndex("body"));
						String date = cur.getString(cur.getColumnIndex("date"));
						
						Log.v("number",number);
						Log.v("date",date);
						if(name == null){
							name = "陌生人";
						}
						Log.v("name",name);
						Log.v("body",body);
						
						Map<String, String> map = new HashMap<String, String>();  
						map.put("sender_phone", number);
						map.put("sender_name", name);
						map.put("content", body);
						map.put("status", "P");
						map.put("date", date);
						
						JSONObject jsonObject = new JSONObject(map);
						Log.d("jsonObject", jsonObject.toString());
						jsonArray.put(jsonObject);
						NumIncrease("SMS_SYN_NUM");
					}
				}
				cur.close();
			}
			mainmap.put("smsList", jsonArray);
		}
		else{
			mainmap.put("smsList", new JSONArray());
		}
		
		if(phoneSyn){
			String[] projection = new String[] {Calls.NUMBER, Calls.NEW, Calls.DATE, Calls.CACHED_NAME, Calls.DURATION};
			String where = "type = 3 AND (date + duration) > "
					+ (System.currentTimeMillis() - Config.UPLOAD_LATENCY * 1000);
			ContentResolver cr = getContentResolver();
			Cursor csr = cr.query(Calls.CONTENT_URI, projection, where, null, Calls.DEFAULT_SORT_ORDER);
			JSONArray jsonArray = new JSONArray();
			if (csr != null) 
	        {
	            while(csr.moveToNext()) 
	            {
	                int callnew = csr.getInt(csr.getColumnIndex(Calls.NEW));
	                String number = csr.getString(csr.getColumnIndex(Calls.NUMBER));
	                String date = csr.getString(csr.getColumnIndex(Calls.DATE));
	                String name = csr.getString(csr.getColumnIndex(Calls.CACHED_NAME));
	                
	                Log.v("duration", csr.getString(csr.getColumnIndex(Calls.DURATION)));
	                
	                if(callnew == 1) {
	                	Log.v("Missedcall", date + " you have a missed call from " + number);
	                	if(name == null)
	                		name = "陌生人";
	                	Log.v("Missedcall",name);
	                	
	                	Map<String, String> map = new HashMap<String, String>();  
	    				map.put("caller_phone", number);
	    				map.put("caller_name", name);
	    				map.put("status", "P");
	    				map.put("date", date);
	    				
	    				JSONObject jsonObject = new JSONObject(map);
	    				Log.d("jsonObject", jsonObject.toString());
	    				jsonArray.put(jsonObject);
	    				NumIncrease("PHONE_SYN_NUM");
	    				
	                }
	            }
	            // release resource
	            csr.close();
	        }
			mainmap.put("callList", jsonArray);
	        
        }
		else{
			mainmap.put("callList", new JSONArray());
		}
		JSONObject mainJson = new JSONObject(mainmap);
		
		JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Config.webuploadURL, mainJson,
			    new Response.Listener<JSONObject>() {
			        @Override
			        public void onResponse(JSONObject response) {
			        	
			        	Log.d("TAG", response.toString());
			        	PushService.actionSayDone(getApplicationContext());
			        	//PushService.actionStart(getApplicationContext());
			        }
			    }, new Response.ErrorListener() {
			        @Override
			        public void onErrorResponse(VolleyError error) {
			        	Log.e("TAG", error.getMessage(), error);  
			        	//byte[] htmlBodyBytes = error.networkResponse.data;
			        	//Log.e("LOGIN-ERROR", new String(htmlBodyBytes), error);
			        }
			    })
			
			    {
			    @Override
			    public Map<String, String> getHeaders() {
			        HashMap<String, String> headers = new HashMap<String, String>();
			        //headers.put("Accept", "application/json");
			        headers.put("Content-Type", "application/json");
			        String token = getUserToken();
			        headers.put("Authorization", "Bearer " + token);
			        return headers;
			    }
			};
		if(isOpenNetwork()){
			requestQueue.add(jsonRequest);
		}
	}
	
	private boolean isOpenNetwork() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager.getActiveNetworkInfo() != null) {
			return connManager.getActiveNetworkInfo().isAvailable();
		}
		return false;
	}
}
