package moe.hilaryoi.im.server.event;

import moe.hilaryoi.im.network.ParcelMessage;

public class EventParcelMessageReceived extends EventParcelReceived<ParcelMessage> {

	public EventParcelMessageReceived (ParcelMessage parcel) {
		super (parcel);
	}

}
