package com.Fawkes;

import java.io.IOException;

public class RunServer {
	public static void main (String[] args) {

		Server server = new Server ();
		RPS game = new RPS (server);

		try {
			server.startServer ();
			server.setListener (game);
			server.showMessage ("STARTING RPS GAME - Type R for Rock, P for Paper, S for Scissors");

		}
		catch (IOException e) {
			System.err.println ("Error: Could not start server");
			e.printStackTrace ();
		}
	}
}
