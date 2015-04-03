package com.easeinfo;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.FontAwesomeText;

public class Register extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		final BootstrapEditText phonenumber = (BootstrapEditText)findViewById(R.id.phonenumber); 
		final BootstrapEditText password = (BootstrapEditText)findViewById(R.id.password); 
		final BootstrapEditText confirm = (BootstrapEditText)findViewById(R.id.confirm); 
		final BootstrapEditText code = (BootstrapEditText)findViewById(R.id.code); 
		final BootstrapButton register = (BootstrapButton)findViewById(R.id.register);
		final BootstrapButton send = (BootstrapButton)findViewById(R.id.send);
		
		register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
				
				Map<String, String> map = new HashMap<String, String>();  
				String mphone = phonenumber.getText().toString();
				String mpassword = password.getText().toString();
				String mconfirm = confirm.getText().toString();
				
				if(!Validator.isMobileNO(mphone)){
					Toast.makeText(Register.this, "Phone number is illegal", Toast.LENGTH_LONG).show();
					return;
				}
				if(!Validator.isRegularPassword(mpassword)){
					Toast.makeText(Register.this, "Length of password should between 5-25 in digits or letters", Toast.LENGTH_LONG).show();
					return;
				}
				
				
				map.put("phone", mphone);
				map.put("password", mpassword);
				map.put("passwordConfirmation", mconfirm);
				
				JSONObject jsonObject = new JSONObject(map);
				Log.d("jsonObject", jsonObject.toString());
				
				Toast.makeText(Register.this, "Connecting to the network", Toast.LENGTH_LONG).show();
				
				JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Config.accountsURL, jsonObject,
				    new Response.Listener<JSONObject>() {
				        @Override
				        public void onResponse(JSONObject response) {
				        	
				        	Log.d("TAG", response.toString());
				        	try{
				        		int status = response.getInt("status");
				        		if(status == 1)
				        		{
				        			Toast.makeText(Register.this, "Registration successful, please login", Toast.LENGTH_LONG).show();
				        			Intent it = new Intent(Register.this, Login.class);
				        			startActivity(it);
				        			finish();
				        		}
				        		else if(status == 0)
				        		{
				        			String msg = response.get("msg").toString();
				        			Toast.makeText(Register.this, msg, Toast.LENGTH_LONG).show();
				        		}
				        	
				        	}catch(Exception e){
				        		//error
				        		Toast.makeText(Register.this, "Unknown error occured", Toast.LENGTH_LONG).show();
				        	}
				        }
				    }, new Response.ErrorListener() {
				        @Override
				        public void onErrorResponse(VolleyError error) {
				        	Log.e("TAG", error.getMessage(), error);  
				        	//byte[] htmlBodyBytes = error.networkResponse.data;
				        	//Log.e("LOGIN-ERROR", new String(htmlBodyBytes), error);
				        	Toast.makeText(Register.this, "Network or server problems occured", Toast.LENGTH_LONG).show();
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
					Toast.makeText(Register.this, "Network is not available", Toast.LENGTH_LONG).show();
				}
				
			}
		});
	}
	
	private boolean isOpenNetwork() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager.getActiveNetworkInfo() != null) {
			return connManager.getActiveNetworkInfo().isAvailable();
		}
		return false;
	}
}