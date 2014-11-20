package com.oxiane.tp_gcm;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class GetProfileDataTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... tokens) {
    	String json = null;
    	HttpClient c = new DefaultHttpClient();
		HttpGet get = new HttpGet("https://www.googleapis.com/oauth2/v1/userinfo" + 
                "?access_token=" + tokens[0] +
                "&access_token_type=bearer");
		get.setHeader("Gdata-version", "1.4");
		get.setHeader("Authorization", "Bearer "+tokens[0]);
		try {
			HttpResponse r = c.execute(get);
			Utils.copyStream(r.getEntity().getContent(), new File(Environment.getExternalStorageDirectory(), "toto.html"));
			
		} catch (IOException e) {
			Log.d("toto", "erreur");
			e.printStackTrace();
		} 
		
		return json;
    }
    @Override
    protected void onPostExecute(String asyncResult) {

        if(asyncResult != null)
        {
        	Log.d("toto", asyncResult);
        }
            //do something with your data, for example deserialize it
//        else
            //do something else
    }
}