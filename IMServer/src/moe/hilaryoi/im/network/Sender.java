package moe.hilaryoi.im.network;

public class Sender { // maybe not the best design

	private String address, nickname;

	public Sender (String address, String nickname) { this.address = address; this.nickname = nickname; }

	public String getAddress () {
		return address;
	}

	public String getNickname () {
		return nickname;
	}

}
