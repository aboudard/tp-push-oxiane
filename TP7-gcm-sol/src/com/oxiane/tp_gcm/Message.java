package com.oxiane.tp_gcm;

import java.util.Date;

public class Message {
	private Date date;
	public Message(String from, String message, Personne dest) {
		this.text = message;
		this.from = from;
		this.to = dest;
		this.setDate(new Date());
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String text;
	public String from;
	public Personne to;
}
