package moe.hilaryoi.im.standard;

import moe.hilaryoi.im.Server;
import moe.hilaryoi.im.event.EventHandler;
import moe.hilaryoi.im.event.EventParcelCommandReceived;
import moe.hilaryoi.im.event.Listener;
import moe.hilaryoi.im.network.ParcelCommand;

public abstract class StandardCommandListener implements Listener { // listener that checks for name and usage and sends usage message when usage is incorrect

	private final String USAGE_PREPEND = "Usage: ";

	private String usage;
	private String[] names; // all the things the user can type to call the command

	public StandardCommandListener (String usage, String... names) {

		this.usage = USAGE_PREPEND + usage;
		this.names = names;

	}

	public abstract void doCommand (ParcelCommand command);

	@EventHandler
	public void onCommand (EventParcelCommandReceived e) {

		if (!nameMatches (e.getParcel ().getCommandName ())) return; // the event must go on

		e.setCancelled (true); // the event has reached its final destination

		doCommand (e.getParcel ());

	}

	protected void sendUsage (String username) { // TODO: should this be ParcelCommand to make it easier in some cases ?

		Server.staticSend (usage, username);

	}

	private boolean nameMatches (String cmdName) {

		for (String name : names) if (cmdName.equals (name)) return true;

		return false;

	}

}
