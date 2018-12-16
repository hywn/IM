package com.Fawkes;

import moe.hilaryoi.im.network.ConnectionServer;
import moe.hilaryoi.im.network.Parcel;
import moe.hilaryoi.im.network.ParcelAne;
import moe.hilaryoi.im.network.ParcelMessage;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

public class Client extends JFrame {

	private static final long serialVersionUID = 1L;

	// gui
	private JTextField messageInput;
	private JTextArea chatWindow;
	private JMenuBar menuBar;
	private JMenu options;
	private JMenuItem settings;

	// connections
	private ConnectionServer c;

	// settings, misc
	private String username;
	private String serverIP;
	private int portNo;
	private Properties properties;
	private boolean connected = false;
	private boolean playSound;

	public Client () {

		super ("PoodleIM Beta Client 0.6");

		loadConfig ();
		loadValues ();

		// creates gui
		messageInput = new JTextField ();
		chatWindow = new JTextArea ();
		menuBar = new JMenuBar ();
		options = new JMenu ("Options");
		settings = new JMenuItem ("Settings");

		menuBar.add (options);
		options.add (settings);

		// creates icon
		try { setIconImage (ImageIO.read (Client.class.getResourceAsStream ("/IM.png"))); }

		catch (IOException e) { System.err.println ("COULD NOT SET UP ICON"); }

		settings.addActionListener (click -> new SettingsFrame ());

		// when you press enter
		messageInput.addActionListener (enter -> {

			if (!enter.getActionCommand ().replace (" ", "").isEmpty ()) {

				sendMessage (enter.getActionCommand ());

				messageInput.setText ("");

			}

		});

		// when you close the window
		WindowListener wListener = new WindowAdapter () {

			@Override
			public void windowClosing (WindowEvent e) {

				if (connected) closeClient ();

				else System.exit (0);

			}

		};

		// gui stuff
		add (messageInput, "South");
		add (new JScrollPane (chatWindow), "Center");
		add (menuBar, "North");

		chatWindow.setEditable (false);
		addWindowListener (wListener);
		setSize (500, 800);
		setVisible (true);
	}

	private void log (String message, Object... objects) { chatWindow.append (String.format (message + "%n", objects)); }

	// plays sound
	public synchronized void playMessageSound (String file) {
		if (playSound) {
			try {
				Clip clip = AudioSystem.getClip ();
				AudioInputStream inputStream = AudioSystem
					.getAudioInputStream (Client.class
						.getResourceAsStream (file));

				clip.open (inputStream);
				clip.start ();
			}
			catch (Exception e) {
				System.err.println ("ERROR PLAYING SOUND");
			}
		}
	}

	public void loadValues () {

		username = getSetting ("username");

		portNo = Integer.parseInt (getSetting ("port"));

		playSound = Boolean.parseBoolean (getSetting ("play_sound"));

		serverIP = getSetting ("ip");

	}

	public void startRunning () throws ClassNotFoundException {

		try { connectToServer (); }

		catch (IOException e) {

			log ("ConnectionServer error.");
			log ("This could either mean that the IM server has crashed, or that the client could not find a server to connect with!");
			log ("Make sure to check your connection settings.");

			connected = false;

		}

	}

	public void closeClient () {

		log ("Closing and shutting down...");

		sendParcel (new ParcelAne (ParcelAne.ANE_GOODBYE));

		ableToType (false);

		try {

			c.close ();
			System.exit (0);

		}

		catch (IOException e) { e.printStackTrace (); }

		catch (InterruptedException e) { e.printStackTrace (); }

	}

	public void connectToServer () throws IOException {

		log ("Attempting to connect to %s on port %s...", serverIP, portNo);

		Socket connection = new Socket (InetAddress.getByName (serverIP), portNo);

		ObjectOutputStream output = new ObjectOutputStream (connection.getOutputStream ());
		output.flush ();

		ObjectInputStream input = new ObjectInputStream (connection.getInputStream ());

		c = new ConnectionServer (connection, input, output) {

			@Override
			public void run () {
				while (isOpen) {
					ParcelMessage m = (ParcelMessage) this.retrieveObject (); // TODO: I guess to only be able to receive Strings is okay for now...but
					playMessageSound ("/messagealert.wav");
					log (m.getBody ());

				}

			}

		};

		c.start ();

		connected = true;

		sendParcel (new ParcelAne (ParcelAne.ANE_HELLO));

		log ("Connected to %s.", connection.getInetAddress ().getHostAddress ());

	}

	private void saveAndExit () {

		try {

			FileOutputStream out = new FileOutputStream (new File (

				this.getClass ().getResource ("/config.properties").toURI ()));

			properties.store (out, "Auto Generated PoodleIM Config File");

			if (connected) {
				closeClient ();

			}

			System.exit (0);

		}

		catch (Exception e) {
			log ("Error closing properties file!");
			System.err.println ("ERROR CLOSING PROPERTIES FILE");
			e.printStackTrace ();
		}

	}

	private void ableToType (final boolean tf) { SwingUtilities.invokeLater (() -> messageInput.setEditable (tf)); }

	public void sendMessage (String message) {

		sendParcel (new ParcelMessage (message));

	}

	public void sendParcel (Parcel parcel) {

		try {

			c.writeObject (parcel);

		}

		catch (Exception e) { chatWindow.append ("\nERROR: COULD NOT SEND"); e.printStackTrace (); }

	}


	private void loadConfig () {

		properties = new Properties ();

		try { properties.load (this.getClass ().getResourceAsStream ("/config.properties")); }

		catch (FileNotFoundException e) { System.err.println ("\nCOULD NOT LOAD CONFIG"); }
		catch (IOException e) { e.printStackTrace (); }

	}

	private String getSetting (String key) {
		return properties.getProperty (key);

	}

	private class SettingsFrame extends JFrame implements ActionListener {
		private static final long serialVersionUID = 1L;
		private JButton setSettings;
		private JTextField portNoInput;
		private JTextField usernameInput;
		private JTextField IPInput;
		private JRadioButton toggleSound;
		private JLabel pnl;
		private JLabel ipl;
		private JLabel unl;

		public SettingsFrame () {
			setLayout (new FlowLayout ());

			setSettings = new JButton ("\tSet Settings\t");
			portNoInput = new JTextField (String.valueOf (portNo));
			IPInput = new JTextField (String.valueOf (serverIP), 8);
			usernameInput = new JTextField (username);
			toggleSound = new JRadioButton ("TOGGLE SOUND", playSound);
			pnl = new JLabel ("Port Number:");
			ipl = new JLabel ("IP Address:");
			unl = new JLabel ("Username:");
			setSettings.addActionListener (this);

			add (setSettings, "South");
			add (pnl);
			add (portNoInput, "Before");
			add (ipl);
			add (IPInput);
			add (unl);
			add (usernameInput);
			add (toggleSound);

			setDefaultCloseOperation (3);
			setSize (180, 175);
			setResizable (false);
			setVisible (true);
		}

		public void actionPerformed (ActionEvent arg0) {
			if ((!portNoInput.getText ().isEmpty ())
				&& (!portNoInput.getText ().isEmpty ())
				&& (!usernameInput.getText ().isEmpty ())
				&& (!IPInput.getText ().isEmpty ())) {

				properties.setProperty ("port", portNoInput.getText ());
				properties.setProperty ("username", usernameInput.getText ());
				properties.setProperty ("ip", IPInput.getText ());
				properties.setProperty ("play_sound",
					String.valueOf (toggleSound.isSelected ()));
				saveAndExit ();
			}
		}
	}
}