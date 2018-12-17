package com.Fawkes;

import com.Fawkes.event.EventManager;
import com.Fawkes.event.EventParcelAneReceived;
import com.Fawkes.event.EventParcelCommandReceived;
import com.Fawkes.event.EventParcelMessageReceived;
import com.Fawkes.network.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

public class
Server extends JFrame implements Runnable {

	// GUI
	private JTextField messageInput;
	private JTextArea chatWindow;

	// connection info
	private ServerSocket serverSocket;
	private HashMap<String, ConnectionClient> connections; // unique username, connection

	// config
	private int port, max_connected; // will be 0 if config fails

	// thread stuff
	private boolean running = true;
	private Thread listenForConnections;

	// server stuff
	private EventManager eventManager;
	static private Server server; // I wish this were final

	public Server () {

		super ("SuperIM 0.1");

		server = this;

		// init
		connections = new HashMap ();
		eventManager = new EventManager ();
		loadConfig ();

		// GUI
		messageInput = new JTextField ();
		messageInput.addActionListener (enterPressed -> {

			if (messageInput.getText ().contentEquals ("CLOSE_SERVER")) {
				closeServer ();
				return;

			}

			String message = "MAIN SERVER: " + messageInput.getText ();

			broadcast (message);

			messageInput.setText ("");

		});

		chatWindow = new JTextArea ();
		chatWindow.setEditable (false);

		setLayout (new BorderLayout ());

		add (new JScrollPane (messageInput), BorderLayout.SOUTH);
		add (new JScrollPane (chatWindow), BorderLayout.CENTER);

		setSize (500, 800);
		setVisible (true);
		setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);

		// start thread
		listenForConnections = new Thread (this);

	}

	private void loadConfig () {

		try {

			Properties properties = new Properties ();
			FileInputStream FIS = new FileInputStream ("config.properties");

			properties.load (FIS);

			port = Integer.parseInt (properties.getProperty ("port"));
			max_connected = Integer.parseInt (properties.getProperty ("maxconnected"));

			FIS.close ();

		}

		catch (FileNotFoundException e) { System.out.println ("Could not find config file"); }
		catch (IOException e) { e.printStackTrace (); }

	}

	//TODO: I use .getSender.getAddress () way too much

	public void log (String message, Object... params) {

		chatWindow.append (String.format (message, params) + "\n");

	}

	public void startServer () throws IOException {

		try {
			serverSocket = new ServerSocket (port, max_connected);
		}

		catch (BindException e) {
			log ("Server already running!");
			System.exit (0);
		}

		log ("Waiting for someone to connect on port " + port + "...");

		listenForConnections.start ();

	}

	private void closeServer () {

		log ("Closing Server...");

		try {

			for (ConnectionClient connection : connections.values ()) connection.close ();

			log ("Closed all connections. Now exiting...");
			System.exit (0);

		}

		catch (IOException e) {

			log ("ERROR CLOSING STREAMS AND SOCKETS");
			e.printStackTrace ();

		}

		catch (InterruptedException e) { e.printStackTrace (); }

	}

	private void sendParcel (Parcel parcel, ConnectionClient client) {


		try { client.writeObject (parcel); }

		catch (IOException e) {

			log ("Error: Could not send parcel `%s` to %s.", parcel, client.getSender ().getAddress ());
			e.printStackTrace ();

		}

	}

	private void sendNoLog (String message, ConnectionClient connection) {

		sendParcel (new ParcelMessage (message), connection);

	}


	public void broadcast (String message) {

		for (ConnectionClient connection : connections.values ()) sendNoLog (message, connection);

		log (message); // TODO: add counter to log instead of using sendNoLog?, e.g. "Hello (20)"

	}

	public void send (String message, String address) {

		sendNoLog (message, connections.get (address));
		log (message);

	}

	// we could just take address from the object instead of using ConnectionClient but idk maybe someone mods their client ofso and that would be trouble
	private void receive (Object object, ConnectionClient connection) {

		if (!(object instanceof Parcel)) log ("Received non-Parcel object from %s.", connection.getSender ().getAddress ());

		Parcel p = (Parcel) object;

		p.setSender (connection.getSender ());// this is key; must stamp so that the sender info can be passed along.

		Class c = p.getClass ();

		if (c == ParcelMessage.class) {

			ParcelMessage message = (ParcelMessage) p;

			if (message.getBody ().startsWith ("/")) eventManager.callEvent (new EventParcelCommandReceived (new ParcelCommand (message)));
			else eventManager.callEvent (new EventParcelMessageReceived (message));

		}

		else if (c == ParcelAne.class) {

			ParcelAne ane = (ParcelAne) p;

			switch (ane.getValue ()) {

				case (ParcelAne.ANE_HELLO):
					// TODO: connect ?
					break;
				case (ParcelAne.ANE_GOODBYE):
					endConnection (connection);
					break;
				default:
					log ("Unknown Ane value from address %s: %s", connection.getSender ().getAddress (), ane.getValue ());

			}

			eventManager.callEvent (new EventParcelAneReceived (ane));

		}

		else {

			log ("Unknown Parcel received from address %s.", connection.getSender ().getAddress ());

		}

	}

	private void endConnection (ConnectionClient connection) {

		try {

			String address = connection.getSender ().getAddress ();

			log ("%s is disconnecting...", address);

			connection.close ();

			connections.remove (address);

			log ("Successfully disconnected.");

		}

		catch (IOException e) { e.printStackTrace (); }

		catch (InterruptedException e) { e.printStackTrace (); }

	}

	// TODO: maybe only add to connections once they've been verified?

	@Override
	public void run () {
		try {
			while (running) {

				Socket socket = serverSocket.accept ();

				log ("Attempting to connect to %s...", socket.getInetAddress ().getHostAddress ());

				ConnectionClient connection = new ConnectionClient (
					socket,
					new ObjectInputStream (socket.getInputStream ()),
					new ObjectOutputStream (socket.getOutputStream ())) {

					@Override
					public void run () {

						while (isOpen) {

							receive (this.retrieveObject (), this);

						}

					}

				};

				// TODO: a bit of trouble... clients may connect and not send an Ane and thus never be recognized in chat (no announcement)

				connection.start ();

				connections.put (connection.getSender ().getAddress (), connection);

				log ("Connected to %s.", connection.getSender ().getAddress ());

			}
		}

		catch (IOException e) { e.printStackTrace (); }

	}

	public String getNickname (String address) { return "Guest"; }

	public EventManager getEventManager () { return eventManager; }

	// not really safe; exposing huge chunk of raw server
	// TODO: maybe only make public safe data like nicknames and String addresses?
	public Collection<ConnectionClient> getConnections () { return connections.values (); }

	public static Server getServer () { return server; }

	public static void staticLog (String message, Object... params) { server.log (message, params); }

	public static void staticBroadcast (String message) { server.broadcast (message); }

	public static void staticSend (String message, String address) { server.send (message, address); }

}