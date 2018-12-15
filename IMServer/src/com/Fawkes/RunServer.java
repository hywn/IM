package com.Fawkes;

import com.Fawkes.event.EventManager;
import com.Fawkes.standard.Announcer;

import java.io.IOException;

public class RunServer {
	public static void main (String[] args) {

		Server server = new Server ();

		EventManager m = server.getEventManager ();

		m.addListener (new Announcer ()); // maybe put in server itself
		m.addListener (new RPS ());

		try {
			server.startServer ();

		}
		catch (IOException e) {
			System.err.println ("Error: Could not start server");
			e.printStackTrace ();
		}
	}
}
