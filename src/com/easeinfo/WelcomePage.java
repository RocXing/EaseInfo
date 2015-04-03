package com.easeinfo;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.Window;

public class WelcomePage extends Activity {

	private final int SPLASH_DISPLAY_LENGHT = 1800;
	public String token = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*final Window win = getWindow();
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		*/
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置无标题
		setContentView(R.layout.activity_welcome);
		
		SharedPreferences sharedPreferences = getSharedPreferences(Config.DB_NAME, Context.MODE_PRIVATE);
		token = sharedPreferences.getString("token", null);
		
		new Handler().postDelayed
		(
				new Runnable()
				{
					public void run()
					{
						
						Intent it;
						if(token!=null)
						{
							it = new Intent(WelcomePage.this,Setting.class);
						}
						else
						{
							it = new Intent(WelcomePage.this,Login.class);
						}
						startActivity(it);
						finish();
					}
				}
		,SPLASH_DISPLAY_LENGHT);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
