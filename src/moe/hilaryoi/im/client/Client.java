package moe.hilaryoi.im.client;

import moe.hilaryoi.im.gui.ChatWindow;
import moe.hilaryoi.im.gui.Settings;
import moe.hilaryoi.im.network.ConnectionServer;
import moe.hilaryoi.im.network.ParcelAne;
import moe.hilaryoi.im.network.ParcelMessage;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends ChatWindow {

	private static final long serialVersionUID = 1L;

	// gui
	private JMenuBar menuBar;
	private JMenu options;
	private JMenuItem settings;

	// connections
	private ConnectionServer connection;

	// settings, misc
	private String username;
	private String serverIP;
	private int portNo;
	private boolean connected = false;
	private boolean playSound;

	public Client() {

		super("IMClient 0.2");

		loadConfig();

		// creates gui
		menuBar = new JMenuBar();
		options = new JMenu("Options");
		settings = new JMenuItem("Settings");

		menuBar.add(options);
		options.add(settings);

		// creates icon
		try { setIconImage(ImageIO.read(Client.class.getResourceAsStream("/IM.png"))); }

		catch (IOException e) { System.err.println("COULD NOT SET UP ICON"); }

		// when you press enter
		addInputListener(enter -> {

			if (!enter.getActionCommand().replace(" ", "").isEmpty()) {

				sendMessage(enter.getActionCommand());

				clearInput();

			}

		});

		// when you close the window
		WindowListener wListener = new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {

				if (connected) closeClient();

				else System.exit(0);

			}

		};

		// gui stuff
		add(menuBar, "North");

		addWindowListener(wListener);

	}

	public void startRunning() {

		try { connectToServer(); }

		catch (IOException e) {

			log("ConnectionServer error.");
			log("This could either mean that the IM server has crashed, or that the client could not find a server to connect with!");
			log("Make sure to check your connection settings.");

			connected = false;

		}

	}

	public void closeClient() {

		log("Closing and shutting down...");

		try { connection.sendParcel(new ParcelAne(ParcelAne.ANE_GOODBYE)); }
		catch (IOException e) { log("Could not send goodbye ane."); e.printStackTrace(); }

		setInputEditable(false);

		try {

			connection.close();
			System.exit(0);

		}

		catch (IOException e) { e.printStackTrace(); }

		catch (InterruptedException e) { e.printStackTrace(); }

	}

	public void connectToServer() throws IOException {

		log("Attempting to connect to %s on port %s...", serverIP, portNo);

		Socket socket = new Socket(InetAddress.getByName(serverIP), portNo);

		connection = new ConnectionServer(socket) {

			@Override
			public void run() {
				while (isOpen) {
					ParcelMessage m = (ParcelMessage) this.retrieveObject(); // TODO: I guess to only be able to receive Strings is okay for now...but
					playMessageSound("/messagealert.wav");
					log(m.getBody());

				}

			}

		};

		connection.start();

		connected = true;

		try { connection.sendParcel(new ParcelAne(ParcelAne.ANE_HELLO)); }
		catch (IOException e) { log("Could not send hello ane."); e.printStackTrace(); }

		log("Connected to %s.", socket.getInetAddress().getHostAddress());

	}

	public void sendMessage(String message) {

		try { connection.sendParcel(new ParcelMessage(message)); }
		catch (IOException e) { log("Could not send message."); e.printStackTrace(); } // TODO: no address ?

	}

	// plays sound
	public synchronized void playMessageSound(String file) {
		if (playSound) {
			try {
				Clip clip = AudioSystem.getClip();
				AudioInputStream inputStream = AudioSystem
					.getAudioInputStream(Client.class
								     .getResourceAsStream(file));

				clip.open(inputStream);
				clip.start();
			}
			catch (Exception e) {
				System.err.println("ERROR PLAYING SOUND");
			}
		}
	}

	private void loadConfig() {

		try {

			Settings s = new Settings("client.config");

			username = s.getString("username");
			portNo = s.getInteger("port");
			playSound = s.getBoolean("play_sound");
			serverIP = s.getString("ip");

		}

		catch (IOException e) { e.printStackTrace(); }

	}

}