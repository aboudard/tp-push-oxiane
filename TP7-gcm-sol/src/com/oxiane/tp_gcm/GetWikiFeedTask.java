package com.oxiane.tp_gcm;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

class GetWikiFeedTask extends AsyncTask<String, Void, Void> {
	/**
	 * 
	 */

	@Override
	protected Void doInBackground(String... params) {
		HttpClient c = new DefaultHttpClient();
		HttpGet get = new HttpGet("https://sites.google.com/feeds/content/oxiane.com/wiki");
		get.setHeader("Gdata-version", "1.4");
		get.setHeader("Authorization", "Bearer "+params[0]);
		try {
			HttpResponse r = c.execute(get);
			Utils.copyStream(r.getEntity().getContent(), new File(Environment.getExternalStorageDirectory(), "toto.html"));
			
		} catch (IOException e) {
			Log.d("toto", "erreur");
			e.printStackTrace();
		}
		return null;
	}
}