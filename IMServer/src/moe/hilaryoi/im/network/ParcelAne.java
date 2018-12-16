package moe.hilaryoi.im.network;

public class ParcelAne extends Parcel {

	public static final byte ANE_HELLO = 0, ANE_GOODBYE = 1;

	private byte value;

	public ParcelAne (byte value) {

		this.value = value;

	}

	public byte getValue () { return value; }

}
