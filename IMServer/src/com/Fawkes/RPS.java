package com.Fawkes;

import com.Fawkes.network.ParcelCommand;
import com.Fawkes.network.Sender;
import com.Fawkes.standard.StandardCommandListener;


public class RPS extends StandardCommandListener {

	// constants
	private final static String[] choices = new String[] { "rock", "paper", "scissors" };

	//
	private RPSPlayer initiator;

	public RPS () {

		super ("/rps <rock, paper, scissors>", "rps");

	}

	@Override
	public void doCommand (ParcelCommand c) {

		int choice = intValue (c.getCommandArgs ()[0]);

		if (c.getCommandArgs ().length != 1 || choice == -1) { sendUsage (c); return; }

		// everything is valid

		// become initiator if there is none
		if (initiator == null) {

			initiator = new RPSPlayer (c.getSender (), choice);
			Server.staticSend (String.format ("You start a game of rock/paper/scissors with your choice of %s.", choices[choice]), c.getSender ().getAddress ());
			Server.staticBroadcast (String.format ("%s has started a game of rock/paper/scissors! Type /rps <rock, paper, scissors> to challenge them.", c.getSender ().getNickname ())); // TODO: %s breaks everything... make sure log() is SEPERATE from the method that displays incoming methods.
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

	public static int intValue (String rps) {

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
