package com.Fawkes.event;

public abstract class Event {

	private boolean cancelled;

	public void setCancelled (boolean cancelled) { this.cancelled = cancelled; }

	public boolean isCancelled () { return cancelled; }

}
