package com.Fawkes.network;

public class Sender { // maybe not the best design

	private String address, username;

	public Sender (String address, String username) { this.address = address; this.username = username; }

	public String getAddress () {
		return address;
	}

	public String getUsername () {
		return username;
	}

}
