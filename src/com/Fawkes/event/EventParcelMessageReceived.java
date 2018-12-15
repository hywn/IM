package com.Fawkes.event;

import com.Fawkes.network.ParcelMessage;

public class EventParcelMessageReceived extends EventParcelReceived<ParcelMessage> {

	public EventParcelMessageReceived (ParcelMessage parcel) { super (parcel); }

}
