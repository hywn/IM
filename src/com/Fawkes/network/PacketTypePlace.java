package com.Fawkes.network;

public class PacketTypePlace extends Packet {

	private static final long serialVersionUID = 8281025099352834619L;

	int x, y;

	public PacketTypePlace(int x, int y) {
		super(Packet.PACKET_TYPE_PLACE);
		this.x = x;
		this.y = y;

	}

	public int getX() {
		return x;

	}

	public int getY() {
		return y;

	}

}
