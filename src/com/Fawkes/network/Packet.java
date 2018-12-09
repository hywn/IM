package com.Fawkes.network;

public class Packet implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public static final int PACKET_TYPE_MESSAGE = 0;
	public static final int PACKET_TYPE_SHOOT = 1;
	public static final int PACKET_TYPE_PLACE = 2;

	int type;

	public Packet(int type) {
		this.type = type;

	}

	public int getType() {
		return type;

	}

}
