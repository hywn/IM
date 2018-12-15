package com.Fawkes;

import com.Fawkes.event.EventHandler;
import com.Fawkes.event.EventParcelCommandReceived;
import com.Fawkes.event.Listener;
import com.Fawkes.network.ParcelCommand;
import com.Fawkes.network.Sender;


public class RPS implements Listener {

	// constants
	private final static String[] choices = new String[] { "rock", "paper", "scissors" };
	private static final String USAGE = "Usage: /rps <rock, paper, scissors>";

	//
	private RPSPlayer initiator;

	@EventHandler
	public void onCommand (EventParcelCommandReceived e) {

		ParcelCommand c = e.getParcel ();

		if (!c.getCommandName ().equalsIgnoreCase ("rps")) return;

		e.setCancelled (true);

		if (c.getCommandArgs ().length != 1) { Server.staticSend (USAGE, c.getSender ().getAddress ()); return; }

		int choice = intValue (c.getCommandArgs ()[0]);

		if (choice == -1) { Server.staticSend (USAGE, c.getSender ().getAddress ()); return; }

		// everything is valid

		// become initiator if there is none
		if (initiator == null) {

			initiator = new RPSPlayer (c.getSender (), choice);
			Server.staticSend (String.format ("You start a game of rock/paper/scissors with your choice of %s.", choices[choice]), c.getSender ().getAddress ());
			Server.staticBroadcast ("%s has started a game of rock/paper/scissors! Type /rps <rock, paper, scissors> to challenge them.");
			return;

		}

		Server.staticSend (String.format ("You challenge %s with your choice of %s.", initiator.getSender ().getNickname (), choices[choice]), c.getSender ().getAddress ());

		// calculate the game
		RPSPlayer challenger = new RPSPlayer (c.getSender (), choice); // just for ease of coding

		int result = (challenger.getChoice () + 3 - initiator.getChoice ()) % 3;

		String winner;
		if (result == 0) winner = "nobody";
		else if (result == 1) winner = challenger.getSender ().getNickname ();
		else winner = initiator.getSender ().getNickname ();

		// announce winners
		Server.staticBroadcast (String.format ("%s challenges %s... %s challenges %s... %s wins!",
			challenger.getSender ().getNickname (),
			initiator.getSender ().getNickname (),
			choices[challenger.getChoice ()],
			choices[initiator.getChoice ()],
			winner));

		// reset game
		initiator = null;

	}

	private int intValue (String rps) {

		for (int i = 0; i < choices.length; i++) if (rps.equals (choices[i])) return i;

		return -1;

	}

	private class RPSPlayer {

		private Sender sender;
		private int choice;

		RPSPlayer (Sender sender, int choice) { this.sender = sender; this.choice = choice; }

		public Sender getSender () { return sender; }

		public int getChoice () { return choice; }

	}

}
