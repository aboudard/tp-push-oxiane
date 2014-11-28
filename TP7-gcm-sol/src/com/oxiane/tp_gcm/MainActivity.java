package com.oxiane.tp_gcm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity {

	private static final String TAG = "TAG";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String BASE_URL = "http://oxiane.com/google-push";
	private final String SCOPES = "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://sites.google.com/feeds/";
	private static final int CHOOSE_ACCOUNT = 45;
	private String regid;
	private GoogleCloudMessaging gcm;
	private TextView mDisplay;
	private String accountName;
	private Account account;
	public String authToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDisplay = (TextView) findViewById(R.id.text);
		accountName = getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE).getString("account", null);
		if(accountName == null){
			showAccount();
		}
		else
		{
			setTitle(accountName);
		}
		
		if(account == null && accountName !=null)
		{
			AccountManager accountManager= AccountManager.get(this);
	        for (Account a : accountManager.getAccounts())
	        {
	        	if(a.name.contains(accountName))
	        	{
	        		Log.d("toto", "acc: "+a.name+ " "+a.type);
	        		this.account = a;
	        		accountManager.getAuthToken(a, SCOPES, null, true, new OxAccManagerCallBack(), null);
	        		break;
	        	}
	        }
		}
	}
	public void initGcm() {

		gcm = GoogleCloudMessaging.getInstance(this);
		regid = getRegistrationId(MainActivity.this);

		if (regid.isEmpty()) {
			registerInBackground();
		} else {
			mDisplay.setText(mDisplay.getText()
					+ " Device already registered. Deviceid = " + regid);
		}
		showList();
	}
	private void showList() {
		final ListView lv = (ListView)findViewById(R.id.list);
		
		
		if(lv!=null)
		{
			TextView empty = new TextView(this);
			empty.setText("Pas de contacts disponibles");
			empty.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, android.R.drawable.gallery_thumb);
			lv.setEmptyView(empty);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Personne p = (Personne) arg0.getAdapter().getItem(arg2);
				Intent i = new Intent(MainActivity.this, ConversationActivity.class);
				i.putExtra("withName", p.getNom());
				i.putExtra("withID", p.getRegId());
				i.putExtra("account", accountName);
				i.putExtra("regid", regid);
				startActivity(i);
				
			}
		});
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Personne p = (Personne) arg0.getAdapter().getItem(arg2);
				Intent i = new Intent(Intent.ACTION_SENDTO);
				i.putExtra(Intent.EXTRA_EMAIL, p.getNom());
				startActivity(i);
				return false;
			}
		});
		new AsyncTask<Void, Void, ArrayList<Personne>>(){

			@Override
			protected ArrayList<Personne> doInBackground(Void... params) {
				//http://oxiane.com/google-push/list.php
				ArrayList<Personne> personnes = new ArrayList<Personne>();
				try {
					String res = Utils.executeHttpGet(BASE_URL+"/list.php?regid="+regid);
					StringReader sr = new StringReader(res);
					BufferedReader br = new BufferedReader(sr);
				    String line = "";
				    while ((line = br.readLine()) != null) {
				    	String[] split = line.split("<br/>");
				    	for(String tmp  : split)
				    	{
				    	  if(!tmp.endsWith("lignes "))
				    	  {
				    		  int indexOf = tmp.indexOf("||");
				    		  if(indexOf> -1)
				    		  {
				    			  
				    			  String nom = tmp.substring(0, indexOf);
				    			  if(!nom.equalsIgnoreCase(accountName))
				    			  {
					    			  String s = tmp.substring(indexOf+2);
					    			  indexOf = s.indexOf("||");
					    			  Personne p;
					    			  if(indexOf > -1)
					    			  {
					    				  //"2014-11-20 11:49:59" 
					    				  Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE).parse(s.substring(indexOf+2));
					    				  p = new Personne(nom, s);
					    				  p.setLastOnline(d);
					    			  }
					    			  else
					    				  p = new Personne(nom, s);
					    			  
					    			  boolean isAlreadyAdded = false;
					    			  for(Personne tmpPersonne : personnes)
					    			  {
					    				  if(tmpPersonne.getNom().equals(nom))
					    				  {
					    					  isAlreadyAdded = true;
					    					  break;
					    				  }
					    			  }
					    			  if(!isAlreadyAdded)
					    				  personnes.add(p);
				    			  }
				    		  }
				    	  }
				    	}
				    }
				} catch (Exception e) {
					e.printStackTrace();
				}
				return personnes;
			}
			protected void onPostExecute(final java.util.ArrayList<Personne> result) {
				if(result.size() > 0)
				{
					lv.setAdapter(new BaseAdapter() {
						
						@Override
						public View getView(int arg0, View arg1, ViewGroup arg2) {
							TextView tv = null;
							if(arg1 == null)
							{
								tv = new TextView(MainActivity.this);
							}
							else
								tv = (TextView) arg1;
							
							tv.setText(getItem(arg0).getNom());
							
							tv.setCompoundDrawablesWithIntrinsicBounds(getItem(arg0).getOnlineDrawable(), 0, 0, 0);
							
							return tv;
						}
						
						@Override
						public long getItemId(int arg0) {
							return arg0;
						}
						
						@Override
						public Personne getItem(int arg0) {
							return result.get(arg0);
						}
						
						@Override
						public int getCount() {
							return result.size();
						}
					});
				}
			}
			}.execute(new Void[]{});
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == CHOOSE_ACCOUNT)
		{
			if(!data.getExtras().getString("authAccount").contains("@oxiane.com"))
			{
				Toast.makeText(MainActivity.this, "Vous devez sélectionner un compte Google @oxiane.com", Toast.LENGTH_SHORT).show();
				showAccount();
				return;
			}
			accountName = data.getExtras().getString("authAccount");
			
			final SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
			prefs.edit().putString("account", accountName).commit();
			setTitle(accountName);
			
			AccountManager accountManager= AccountManager.get(this);
	        for (Account a : accountManager.getAccounts())
	        {
	        	if(a.name.contains(accountName))
	        	{
	        		Log.d("toto", "acc: "+a.name+ " "+a.type);
	        		this.account = a;
	        		accountManager.getAuthToken(a, SCOPES, null, true, new OxAccManagerCallBack(), null);
	        		break;
	        	}
	        }
		}
	}
	private final class OxAccManagerCallBack implements
			AccountManagerCallback<Bundle> {
		public void run(AccountManagerFuture<Bundle> future) {

		    try {
		        Bundle bundle = future.getResult();

		        //bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
		        //bundle.getString(AccountManager.KEY_ACCOUNT_TYPE);

		        authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
		        
		        getPreferences(Activity.MODE_PRIVATE).edit().putString("token", authToken).commit();

		    } catch (Exception e) {
		        System.out.println("getAuthTokenByFeatures() cancelled or failed:");
		        e.printStackTrace();
		        authToken = "failure";
		    }

		    if(!authToken.equals("failure")) {

		        new GetProfileDataTask().execute(authToken);
		        
		        new GetWikiFeedTask().execute(authToken);
		        
		        initGcm();
		    }
		}
	}
	
	
	public void showAccount()
	{
		Intent intent = AccountManager.newChooseAccountIntent(null, null,
		        new String[] { "com.google" }, true, "Sélectionnez votre compte Oxiane", null,
		        null, null);
		startActivityForResult(intent, CHOOSE_ACCOUNT);
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
				
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {

						try {
							sendRegistrationIdToBackend();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}
					protected void onPostExecute(Void result) {
						showList();
					};
				}.execute(new Void[]{});
			}
		}.execute(null, null, null);
	}

	protected void storeRegistrationId(MainActivity mainActivity, String regid2) {
		final SharedPreferences prefs = getSharedPreferences(
				MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
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
		final String res = Utils.executeHttpGet(BASE_URL + "/register.php?id="
				+ regid + "&nom=" + accountName+"&device="+(manufacturer + deviceName).replaceAll(" ", ""));
		MainActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(MainActivity.this, res, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private String getRegistrationId(MainActivity mainActivity) {
		final SharedPreferences prefs = getSharedPreferences(
				MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(mainActivity);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

}
