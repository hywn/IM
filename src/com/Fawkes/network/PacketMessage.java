package com.Fawkes.network;

public class PacketMessage extends Packet {

	private static final long serialVersionUID = -956055942236331991L;

	String sender;
	String message;

	public PacketMessage(String message) {
		super(Packet.PACKET_TYPE_MESSAGE);
		this.message = message;

	}

	public String getMessage() {
		return message;

	}

}
