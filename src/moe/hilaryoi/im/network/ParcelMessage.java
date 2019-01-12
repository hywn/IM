package moe.hilaryoi.im.network;

public class ParcelMessage extends Parcel {


	private String body;

	public ParcelMessage (String body) {

		this.body = body;

	}

	public String getBody () {
		return body;
	}

}
