package com.Fawkes;

import com.Fawkes.network.Connection;

import java.util.ArrayList;


public class RPS implements ServerListener {

	Server server;

	private String player1, player2;
	private String player1Choice, player2Choice;

	String[] choices;

	public RPS (Server server) {
		this.server = server;

		reset ();

		this.choices = new String[] { "rock", "paper", "scissors" };
	}

	public void reset () {
		player1 = "none";
		player2 = "none";
		player1Choice = "none";
		player2Choice = "none";
		server.sendServerMessage ("The game has reset! Type either 'rock', 'paper' or 'scissors' to make a choice.");
	}

	/**
	 * Returns an ArrayList with the usernames of the first players. First index = player1, second index = player2
	 *
	 * @return player usernames
	 */

	public ArrayList<String> getPlayerUsernames () {
		ArrayList<String> players = new ArrayList<String> ();

		players.add (player1);
		players.add (player2);

		return players;
	}


	/**
	 * Get the choice of a player, using "player1" or "player2" as argument
	 *
	 * @param player a string of the player
	 * @return the choice, either rock, paper, or scissors, lowercase
	 */

	public String getChoice (String player) {
		if (player.equalsIgnoreCase ("player1")) {
			return player1Choice;
		}

		if (player.equalsIgnoreCase ("player2")) {
			return player2Choice;
		}

		return "use player1 or player2 as argument";
	}

	public void setChoice (String player, String choice) {
		if (player.equalsIgnoreCase ("player1")) {
			player1Choice = choice;
		}

		if (player.equalsIgnoreCase ("player2")) {
			player2Choice = choice;
		}
	}

	/**
	 * Returns the winner of the game
	 *
	 * @return returns "player1", "player2" or "draw"
	 */

	public String calculateWinner () {

		int result = (intValue (player2Choice) + 3 - intValue (player1Choice)) % 3;

		if (result == 0) return "draw";
		else if (result == 1) return "player2";
		else return "player1";

	}

	private int intValue (String rps) {

		for (int i = 0; i < choices.length; i++) if (rps.equals (choices[i])) return i;

		return -1;

	}

	@Override
	public void playerConnected (Connection playerClient) {
		server.sendOnePersonMessage ("Yo, choose something. Type either 'rock', 'paper' or 'scissors'.", playerClient);
	}

	@Override
	public void messageReceived (String message, Connection playerClient) {

		String messageSplit[] = message.split (" - ");

		if (messageSplit[1].split (" ").length > 1) {
			return;
		}

		// Checks if the msg actually says rock, paper or scissors
		if (!isRps (messageSplit[1])) {

			// Reset game

			if (messageSplit[1].equalsIgnoreCase ("reset")) {
				reset ();
			}

			return;
		}

		String user = messageSplit[0];
		String choice = messageSplit[1].toLowerCase ();

		// Assign choice

		if (checkIfPlayerAlreadyMadeChoiceAndIfNotAssignChoice (user, choice) == true) {
			server.sendOnePersonMessage ("You already made a choice!", playerClient);
			return;
		}

		announceChoiceConfirmation (user, choice, playerClient);

		// Check if both players made a choice and if so calculate + announce winner

		doResults ();

	}


	// Below is a bunch of private methods just for the sake of clean code split up into smaller methods

	private void doResults () {

		if (!player1.equals ("none") && !player2.equals ("none")) {
			String winningChoice = "";
			String winningPlayer = "";
			String losingPlayerChoice = "";

			if (calculateWinner ().equals ("player1")) {
				winningChoice = player1Choice;
				winningPlayer = player1;
				losingPlayerChoice = player2Choice;
				server.sendServerMessage ("The winner is " + winningPlayer + " with their choice " + winningChoice + ", beating " + losingPlayerChoice);
			}

			else if (calculateWinner ().equals ("player2")) {
				winningChoice = player2Choice;
				winningPlayer = player2;
				losingPlayerChoice = player1Choice;
				server.sendServerMessage ("The winner is " + winningPlayer + " with their choice " + winningChoice + ", beating " + losingPlayerChoice);
			}

			else if (calculateWinner ().equals ("draw")) {
				server.sendServerMessage ("It's a draw! " + player1Choice + " vs " + player2Choice + "!");
			}

			server.sendServerMessage ("Type 'reset' if you want to restart the game");

		}

	}

	private void announceChoiceConfirmation (String user, String choice, Connection playerClient) {
		server.sendServerMessage (user + " has made a choice!");
		server.sendOnePersonMessage ("(" + user + ", you chose " + choice + ")", playerClient);
	}

	private boolean checkIfPlayerAlreadyMadeChoiceAndIfNotAssignChoice (String user, String choice) {

		// Returns true if player had already made a choice, otherwise it just assigns the choice and returns false

		if (player1.equals ("none")) {
			player1 = user;
			player1Choice = choice;
			return false;
		}

		else if (player2.equals ("none") && !(player1.equals (user))) {
			player2 = user;
			player2Choice = choice;
			return false;
		}

		return true;
	}

	private boolean isRps (String msg) {
		if (msg.equalsIgnoreCase ("rock") || msg.equalsIgnoreCase ("paper") || msg.equalsIgnoreCase ("scissors")) {return true;}

		return false;
	}
}
