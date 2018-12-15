package com.Fawkes.network;

public abstract class Parcel {

	private Sender sender;

	public Parcel (Sender sender) {

		this.sender = sender;

	}

	public Sender getSender () { return sender; }

}
