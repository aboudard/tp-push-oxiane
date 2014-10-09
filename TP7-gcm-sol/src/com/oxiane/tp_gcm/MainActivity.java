package com.oxiane.tp_gcm;

import java.io.IOException;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity {

	private static final String TAG = "TAG";
    private final static int        PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String      EXTRA_MESSAGE                    = "message";
    public static final String      PROPERTY_REG_ID                  = "registration_id";
    private static final String     PROPERTY_APP_VERSION             = "appVersion";
	private static final String BASE_URL = "http://oxiane.com/google-push";
	private String regid;
	private GoogleCloudMessaging gcm;
	private TextView mDisplay;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDisplay = (TextView) findViewById(R.id.text);
		
		gcm = GoogleCloudMessaging.getInstance(this);
        regid = getRegistrationId(MainActivity.this);

        if (regid.isEmpty()) {
            registerInBackground();
        }
        else
        {
        	mDisplay.setText(mDisplay.getText()+" Device already registered. Deviceid = "+regid);
        }
	}

	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }
                    regid = gcm.register(getString(R.string.projectid));
                    msg = "Device registered, registration ID=" + regid;

                    sendRegistrationIdToBackend();
                    
                    storeRegistrationId(MainActivity.this, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                } catch (Exception e) {
                	msg = "Error :" + e.getMessage();
				}
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                 mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
	}

	protected void storeRegistrationId(MainActivity mainActivity, String regid2) {
		final SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        int appVersion = getAppVersion(mainActivity);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regid);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
	}

	protected void sendRegistrationIdToBackend() throws Exception {
		String manufacturer = Build.MANUFACTURER;
		String deviceName = Build.MODEL;
		String nom = URLEncoder.encode(manufacturer+deviceName);
		final String res = Utils.executeHttpGet(BASE_URL+"/register.php?id="+regid+"&nom="+nom);
		MainActivity.this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(MainActivity.this, res, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private String getRegistrationId(MainActivity mainActivity) {
	        final SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	        if (registrationId.isEmpty()) {
	            Log.i(TAG, "Registration not found.");
	            return "";
	        }
	        // Check if app was updated; if so, it must clear the registration ID
	        // since the existing regID is not guaranteed to work with the new
	        // app version.
	        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	        int currentVersion = getAppVersion(mainActivity);
	        if (registeredVersion != currentVersion) {
	            Log.i(TAG, "App version changed.");
	            return "";
	        }
	        return registrationId;
	}
	private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
	 private boolean checkPlayServices() {
	        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	        if (resultCode != ConnectionResult.SUCCESS) {
	            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
	            } else {
	                Log.i(TAG, "This device is not supported.");
	                finish();
	            }
	            return false;
	        }
	        return true;
	    }

}
