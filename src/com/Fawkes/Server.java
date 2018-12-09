package com.Fawkes;

import com.Fawkes.network.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

public class Server extends JFrame implements Runnable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JTextField messageInput;
	private JTextArea chatWindow;
	private ServerSocket serverSocket;

	private ArrayList<Connection> connections = new ArrayList<Connection> ();

	private final int PORTNO;
	private final int MAXPEOPLE;
	private Properties properties;
	private FileInputStream FIS;

	public static Server server;

	private Thread listenForConnections;

	public boolean running = true;

	private ServerListener listener;

	public Server () {
		super ("IM Beta Server 0.5");

		server = this;

		loadConfig ();

		PORTNO = Integer.parseInt (getSetting ("port"));
		MAXPEOPLE = Integer.parseInt (getSetting ("maxconnected"));
		try {
			FIS.close ();
		}
		catch (IOException e) {
			e.printStackTrace ();
		}
		messageInput = new JTextField ();
		chatWindow = new JTextArea ();

		messageInput.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent enterPressed) {

				if (messageInput.getText ().contentEquals ("CLOSE_SERVER")) {
					closeServer ();
					return;

				}

				String message = "MAIN SERVER - " + messageInput.getText ();

				sendServerMessage (message);
				showMessage (message);

				messageInput.setText ("");
			}
		});

		chatWindow.setEditable (false);

		add (new JScrollPane (messageInput), "South");
		add (new JScrollPane (chatWindow), "Center");

		setSize (500, 800);
		setVisible (true);
		setDefaultCloseOperation (3);

		listenForConnections = new Thread (this);

	}

	public void setListener (ServerListener toSet) {
		listener = toSet;
	}


	public void startServer () throws IOException {
		try {
			serverSocket = new ServerSocket (PORTNO, MAXPEOPLE);
		}
		catch (BindException e) {
			showMessage ("Server already running!");
			System.exit (0);
		}
		showMessage ("Waiting for someone to connect on port " + PORTNO + "...");

		listenForConnections.start ();
	}

	public void showMessage (String mes) {
		chatWindow.append (mes + "\n");
	}

	public void sendServerMessage (String message) {

		for (Connection connection : connections) {
			try {

				String m = new String (message + "\n");

				connection.writeObject (m);


			}
			catch (IOException e) {
				System.err.println ("Error: Could not send message \"" + message
					+ "\" to " + connection.getAddress ());
				e.printStackTrace ();
			}
		}

	}

	public void sendOnePersonMessage (String message, Connection connection) {
		try {

			String m = new String (message + "\n");

			connection.writeObject (m);

			showMessage (message);

		}
		catch (IOException e) {
			System.err.println ("Error: Could not send message \"" + message
				+ "\" to " + connection.getAddress ());
			e.printStackTrace ();
		}
	}

	private void closeServer () {
		showMessage ("Closing Server...");
		try {

			for (Connection connection : connections) {
				connection.close ();

			}

			showMessage ("Closed all streams.");

			System.exit (0);
		}
		catch (IOException e) {
			System.out.println ("ERROR CLOSING STREAMS AND SOCKETS");
			e.printStackTrace ();
		}
	}

	public void handleMessage (String message, Connection connection) {

		if (message.endsWith ("@#^^%[END_CLIENT_CONNECTION]%^^#@")) {
			endConnection (connection);
			return;

		}

		String actualMsg = message.split (" - ")[1];

		// Checks if the client is sending username

		if (actualMsg.split ("=")[0].equalsIgnoreCase ("u")) {
			String user = actualMsg.split ("=")[1];
			sendServerMessage ("MAIN SERVER - " + user
				+ " has joined the chat. (" + connection.getAddress () + ")");
		}

		// Only send message if it's NOT a rock paper scissors message

		else if (!message.toLowerCase ().contains ("rock") && !message.toLowerCase ().contains ("paper")
			&& !message.toLowerCase ().contains ("scissors")) {
			sendServerMessage (message);
		}


		showMessage (message);

		listener.messageReceived (message, connection);
	}

	private void endConnection (Connection connection) {
		try {
			String address = connection.getAddress ();
			showMessage ("" + address + " is disconnecting...");

			connection.close ();

			connections.remove (connection);

			showMessage ("Successfully disconnected.");
			sendServerMessage (address + " has left the chat!");
			sendServerMessage (getConnectionDetails ());

		}
		catch (IOException e) {
			e.printStackTrace ();
		}
	}

	private void loadConfig () {
		properties = new Properties ();
		try {
			FIS = new FileInputStream ("config.properties");
			properties.load (FIS);
		}
		catch (FileNotFoundException e) {
			System.out.println ("Could not load config file");
		}
		catch (IOException e) {
			e.printStackTrace ();
		}
	}

	private String getSetting (String key) {
		return properties.getProperty (key);
	}

	private String getConnectionDetails () {
		String complete = "MAIN SERVER - Connected currently:";
		for (Connection connection : connections) {
			complete += connection.getAddress () + " ";

		}
		complete = complete.substring (0, complete.length () - 1);
		complete = complete.concat ("");
		return complete;
	}

	@Override
	public void run () {
		try {
			while (running) {
				Socket socket = serverSocket.accept ();

				Connection connection = new Connection (socket,
					new ObjectInputStream (socket.getInputStream ()),
					new ObjectOutputStream (socket.getOutputStream ())) {

					@Override
					public void onClose () {

					}

					@Override
					public void run () {
						while (isOpen) {

							String m = (String) this.retrieveObject ();
							handleMessage (m, this);

						}

					}
				};

				String address = connection.getAddress ();

				showMessage ("Connected to " + address);

				/*

				I (Nomar) replaced this with a thing in handleMessage() that checks for username

				sendServerMessage("MAIN SERVER - " + address
						+ " has joined the chat.");

				*/

				connection.start ();

				connections.add (connection);

				listener.playerConnected (connection);
			}
		}
		catch (IOException e) {
			e.printStackTrace ();
		}

	}
}