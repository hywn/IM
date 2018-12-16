package com.Fawkes.network;

import java.io.Serializable;

public abstract class Parcel implements Serializable {

	private Sender sender;

	public void setSender (Sender sender) { this.sender = sender; }

	public Sender getSender () { return sender; }

}
