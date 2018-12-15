package com.Fawkes.standard;

import com.Fawkes.Server;
import com.Fawkes.event.EventHandler;
import com.Fawkes.event.EventParcelAneReceived;
import com.Fawkes.event.EventParcelMessageReceived;
import com.Fawkes.event.Listener;
import com.Fawkes.network.Connection;
import com.Fawkes.network.ParcelAne;
import com.Fawkes.network.ParcelMessage;
import com.Fawkes.network.Sender;

public class Announcer implements Listener {

	@EventHandler
	public void onAne (EventParcelAneReceived e) {

		ParcelAne a = e.getParcel ();
		Sender s = a.getSender ();

		switch (a.getValue ()) {

			case (ParcelAne.ANE_HELLO):
				Server.staticBroadcast (String.format ("MAIN SERVER: %s (%s) has joined the chat.", s.getAddress (), s.getNickname ()));
				break;
			case (ParcelAne.ANE_GOODBYE):
				Server.staticBroadcast (String.format ("MAIN SERVER: %s (%s) has left the chat.", s.getAddress (), s.getNickname ()));
				Server.staticBroadcast (currConnected ());

		}

	}

	// TODO: this is pretty bad because it uses raw connections and it doesn't have nicknames
	private String currConnected () {

		StringBuilder b = new StringBuilder ("MAIN SERVER: Currently connected: ");
		String connector = "";

		Server s = Server.getServer ();

		for (Connection c : s.getConnections ()) {

			b.append (connector); connector = ", ";
			b.append (c.getAddress ());

		}

		return b.toString ();

	}

	@EventHandler
	public void onMessageReceive (EventParcelMessageReceived e) {

		ParcelMessage m = e.getParcel ();

		Server.staticBroadcast (String.format ("%s: %s", m.getSender ().getNickname (), m.getBody ()));

	}

}
