package moe.hilaryoi.im.event;

import moe.hilaryoi.im.network.ParcelCommand;

public class EventParcelCommandReceived extends EventParcelReceived<ParcelCommand> {

	public EventParcelCommandReceived (ParcelCommand parcel) { super (parcel); }

}
