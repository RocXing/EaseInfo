package com.easeinfo;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.FontAwesomeText;

public class Login extends Activity {
	public String userphone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		
		Intent service = new Intent(this, MissedCallService.class);
		startService(service);
		service = new Intent(this, SmsService.class);
		startService(service);
		
		
		final BootstrapEditText phonenumber = (BootstrapEditText)findViewById(R.id.phonenumber);
		final BootstrapEditText password = (BootstrapEditText)findViewById(R.id.password);
		final BootstrapButton login = (BootstrapButton)findViewById(R.id.login);
		final BootstrapButton toregister = (BootstrapButton)findViewById(R.id.toregister);
		
		toregister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(Login.this,Register.class);
				startActivity(it);
			}
		});
		
		
		login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
				
				Map<String, String> map = new HashMap<String, String>();  
				String mphone = phonenumber.getText().toString();
				String mpassword = password.getText().toString();
				
				if(!Validator.isMobileNO(mphone)){
					Toast.makeText(Login.this, "Phone number is illegal", Toast.LENGTH_LONG).show();
					return;
				}
				if(!Validator.isRegularPassword(mpassword)){
					Toast.makeText(Login.this, "Length of password should between 5-25 in digits or letters", Toast.LENGTH_LONG).show();
					return;
				}
				userphone = mphone;
				
				map.put("phone", mphone);
				map.put("password", mpassword);
				map.put("client", "app");
				JSONObject jsonObject = new JSONObject(map);
				Log.d("jsonObject", jsonObject.toString());
				
				Toast.makeText(Login.this, "Connecting to the network", Toast.LENGTH_LONG).show();
				
				JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Config.sessionsURL, jsonObject,
				    new Response.Listener<JSONObject>() {
				        @Override
				        public void onResponse(JSONObject response) {
				        	
				        	Log.d("TAG", response.toString());
				        	try{
				        		int status = response.getInt("status");
				        		if(status == 1)
				        		{
				        			Toast.makeText(Login.this, "Login successful", Toast.LENGTH_LONG).show();
				        			Intent it = new Intent(Login.this, Setting.class);
				        			startActivity(it);
				        			
				        			String token = response.get("token").toString();
				        			//save token
				        			SharedPreferences sharedPreferences = getSharedPreferences(Config.DB_NAME, Context.MODE_PRIVATE);
				        			Editor editor = sharedPreferences.edit();
				        			editor.putString("token", token);
				        			editor.putString(PushService.PREF_DEVICE_ID, userphone);
				        			editor.commit();
				        			finish();
				        		}
				        		else if(status == 0)
				        		{
				        			String msg = response.get("msg").toString();
				        			Toast.makeText(Login.this, msg, Toast.LENGTH_LONG).show();
				        		}
				        	
				        	}catch(Exception e){
				        		//error
				        		Toast.makeText(Login.this, "Unknown error occured", Toast.LENGTH_LONG).show();
				        	}
				        }
				    }, new Response.ErrorListener() {
				        @Override
				        public void onErrorResponse(VolleyError error) {
				        	Log.e("TAG", error.getMessage(), error);  
				        	//byte[] htmlBodyBytes = error.networkResponse.data;
				        	//Log.e("LOGIN-ERROR", new String(htmlBodyBytes), error);
				        	Toast.makeText(Login.this, "Network or server problems occured", Toast.LENGTH_LONG).show();
				        }
				    })
				
				    {
				    @Override
				    public Map<String, String> getHeaders() {
				        HashMap<String, String> headers = new HashMap<String, String>();
				        //headers.put("Accept", "application/json");
				        headers.put("Content-Type", "application/json");
				        headers.put("Authorization", "");
				        return headers;
				    }
				};
				if(isOpenNetwork()){
					requestQueue.add(jsonRequest);}
				else{
					Toast.makeText(Login.this, "Network is not available", Toast.LENGTH_LONG).show();
				}
					
			}
		});
		
		new Thread()
		{
			public void run()
			{
				PushService.actionStart(getApplicationContext());
				Log.i("Login", "Starting Service");
			}
		}.start();
		
	}
	
	private boolean isOpenNetwork() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager.getActiveNetworkInfo() != null) {
			return connManager.getActiveNetworkInfo().isAvailable();
		}
		return false;
	}

}