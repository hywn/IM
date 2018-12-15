package com.Fawkes;

import com.Fawkes.network.Connection;

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
	private Connection c;

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
		try {
			setIconImage (ImageIO.read (Client.class
				.getResourceAsStream ("/IM.png")));
		}
		catch (IOException e) {
			System.err.println ("COULD NOT SET UP ICON");
		}
		settings.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent click) {

				@SuppressWarnings ("unused")
				SettingsFrame sf = new SettingsFrame ();
			}
		});

		// when you press enter
		messageInput.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent enter) {
				if (!enter.getActionCommand ().replace (" ", "").isEmpty ()) {
					sendIM (enter.getActionCommand ());
					messageInput.setText ("");
				}
			}
		});

		// when you close the window
		WindowListener wListener = new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				if (connected) {

					closeClient ();
				}
				else {
					System.exit (0);
				}
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
		try {
			connectToServer ();

		}
		catch (IOException e) {
			showMessage ("Connection error.\n");
			showMessage ("This could either mean that the IM server has crashed, or that the client could not find a server to connect with!\n");
			showMessage ("Make sure to check your connection settings.\n");

			connected = false;

		}
	}

	public void closeClient () {
		showMessage ("Closing and shutting down...\n");
		sendIM ("@#^^%[END_CLIENT_CONNECTION]%^^#@");
		ableToType (false);

		try {
			c.close ();
			System.exit (0);

		}
		catch (IOException e) {
			e.printStackTrace ();

		}
	}

	public void connectToServer () throws IOException {
		showMessage ("Attempting to connect to " + serverIP + " on port number "
			+ portNo + "...\n");

		Socket connection = new Socket (InetAddress.getByName (serverIP), portNo);

		ObjectOutputStream output = new ObjectOutputStream (
			connection.getOutputStream ());
		output.flush ();

		ObjectInputStream input = new ObjectInputStream (
			connection.getInputStream ());

		c = new Connection (connection, input, output) {

			@Override
			public void run () {
				while (isOpen) {
					String m = (String) this.retrieveObject ();
					playMessageSound ("/messagealert.wav");
					showMessage (m);

				}

			}

			public void onClose () {}
		};

		c.start ();

		connected = true;

		showMessage ("Connected to "
			+ connection.getInetAddress ().getHostAddress () + "\n");
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
			showMessage ("Error closing properties file!");
			System.err.println ("ERROR CLOSING PROPERTIES FILE");
			e.printStackTrace ();
		}
	}

	private void ableToType (final boolean tf) {
		SwingUtilities.invokeLater (() -> messageInput.setEditable (tf));
	}

	public void sendIM (String sentStuff) {
		try {

			String m = username + " - " + sentStuff;
			c.writeObject (m);

		}
		catch (Exception e) {
			chatWindow.append ("\nERROR: COULD NOT SEND");

		}
	}

	private void showMessage (String mes) {

		chatWindow.append (mes);

	}

	private void loadConfig () {
		properties = new Properties ();
		try {
			properties.load (this.getClass ().getResourceAsStream (
				"/config.properties"));
		}
		catch (FileNotFoundException e) {
			System.err.println ("\nCOULD NOT LOAD CONFIG");
		}
		catch (IOException e) {
			e.printStackTrace ();
		}
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