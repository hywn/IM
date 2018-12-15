package com.Fawkes.event;

import com.Fawkes.network.ParcelCommand;

public class EventParcelCommandReceived extends EventParcelReceived<ParcelCommand> {

	public EventParcelCommandReceived (ParcelCommand parcel) { super (parcel); }

}
