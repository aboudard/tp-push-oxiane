package com.oxiane.tp_gcm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ConversationActivity extends Activity {
	private final class ConversationAdapter extends BaseAdapter {
		private final String account;

		private ConversationAdapter(String account) {
			this.account = account;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = null;
			Message m = getItem(position);
			if(m.to.getNom().equals(account))
			{
				if(convertView == null)
				{
					v = getLayoutInflater().inflate(R.layout.message_recu_item, null);
				}
				else
				{
					v = convertView;
				}
				((TextView)v.findViewById(R.id.txt_date)).setText("Reçu : "+ new SimpleDateFormat("dd/MM/yyyy HH:mm").format(m.getDate()));
			}
			else
			{
				if(convertView == null)
				{
					v = getLayoutInflater().inflate(R.layout.message_envoye_item, null);
				}
				else
				{
					v = convertView;
				}
				((TextView)v.findViewById(R.id.txt_date)).setText("Envoyé : "+ new SimpleDateFormat("dd/MM/yyyy HH/mm").format(m.getDate()));
			}
			
			((TextView)v.findViewById(R.id.tv)).setText(m.text);
			return v;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Message getItem(int position) {
			return messages.get(position);
		}

		@Override
		public int getCount() {
			return messages.size();
		}
	}

	private ArrayList<Message> messages;
	private ListView lv;
	private BroadcastReceiver inAppReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent !=null)
			{
				String from = intent.getStringExtra("from");
				String text = intent.getStringExtra("message");
				Personne to = new Personne(getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE).getString("account", null),
						getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE).getString(MainActivity.PROPERTY_REG_ID, null));
				
				Message m = new Message(from, text, to);
				
				if(messages != null)
					messages.add(m);
				if(lv!=null && lv.getAdapter()!=null)
					((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
			}
			
		}
	};


	private String account;
	private String myRegId;	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String withNom = getIntent().getStringExtra("withName");
		final String withId = getIntent().getStringExtra("withID");
		account = getIntent().getStringExtra("account");
		myRegId = getIntent().getStringExtra("regid");
		setTitle(withNom);
		setContentView(R.layout.conversation);
		
		final TextView sendText = (TextView) findViewById(R.id.edt);
		final View sendBtn = findViewById(R.id.btn_send);
		lv = (ListView) findViewById(R.id.listConv);
		
		sendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				sendBtn.setEnabled(false);
				new AsyncTask<Void, Void, String>() {

					@Override
					protected String doInBackground(Void... params) {
						return Utils.sendMessage(sendText.getText().toString(), myRegId, withId);
					}
					@Override
					protected void onPostExecute(String result) {
						Log.d("retour send", " "+result);
						sendBtn.setEnabled(true);
						messages.add(new Message(account, sendText.getText().toString(), new Personne(withNom, withId)));
						((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
						lv.smoothScrollToPosition(messages.size()-1);
						sendText.setText("");
					}
				}.execute(new Void[]{});
			}
		});
		
		
		
		messages = new ArrayList<Message>();
		
		lv.setAdapter(new ConversationAdapter(account));
	}
	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(inAppReceiver, new IntentFilter(GcmIntentService.MESSAGE_RECU_IN_APP_RECEIVER));
	}
	@Override
	protected void onStop() {
		super.onStop();
		if(inAppReceiver!=null)
			unregisterReceiver(inAppReceiver);
	}
}
