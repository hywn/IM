package moe.hilaryoi.im.event;

import moe.hilaryoi.im.network.ParcelMessage;

public class EventParcelMessageReceived extends EventParcelReceived<ParcelMessage> {

	public EventParcelMessageReceived (ParcelMessage parcel) {
		super (parcel);
	}

}
