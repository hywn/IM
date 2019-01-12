package moe.hilaryoi.im.server.event;

// java dumb; have to make one of these for each type of parcel
public abstract class EventParcelReceived<P> extends Event {

	private P parcel;

	public EventParcelReceived (P parcel) {this.parcel = parcel;}

	public P getParcel () {return parcel;}

}
