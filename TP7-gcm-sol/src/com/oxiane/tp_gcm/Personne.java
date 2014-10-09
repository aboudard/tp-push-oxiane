package com.oxiane.tp_gcm;

public class Personne {

	private String nom;
	private String regId;

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

}
