package com.oxiane.tp_gcm;

import java.util.Calendar;
import java.util.Date;

public class Personne {

	private static final int AFK_TIMER = 5;
	private String nom;
	private String regId;
	private Date lastOnline;

	public Personne(String substring, String regId) {
		this.setNom(substring);
		this.setRegId(regId);
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getRegId() {
		return regId;
	}

	public void setRegId(String regId) {
		this.regId = regId;
	}

	public void setLastOnline(Date d) {
		this.lastOnline = d;
	}
	public enum OnlineStatus{ ONLINE, UNKNOWN, AWAY}
	public OnlineStatus getOnlineStatus()
	{
		if(lastOnline == null)
			return OnlineStatus.UNKNOWN;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1*AFK_TIMER);
		if(lastOnline.after(cal.getTime()))
			return OnlineStatus.ONLINE;
		else
			return OnlineStatus.AWAY;
	}

	public int getOnlineDrawable() {
		switch(getOnlineStatus())
		{
		case AWAY: return android.R.drawable.presence_away;
		case ONLINE: return android.R.drawable.presence_online;
		case UNKNOWN: return android.R.drawable.presence_offline;
		}
		return 0;
	}

}
