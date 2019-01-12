package moe.hilaryoi.im;

import moe.hilaryoi.im.event.EventManager;
import moe.hilaryoi.im.event.EventParcelAneReceived;
import moe.hilaryoi.im.event.EventParcelCommandReceived;
import moe.hilaryoi.im.event.EventParcelMessageReceived;
import moe.hilaryoi.im.gui.ChatWindow;
import moe.hilaryoi.im.gui.Settings;
import moe.hilaryoi.im.network.*;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;

public class Server extends ChatWindow implements Runnable {

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

	public Server() {

		super("IM Server 0.2");

		server = this;

		addInputListener(enterPressed -> {

			String input = getInput();

			if (input.equals("CLOSE_SERVER")) {
				closeServer();
				return;

			}

			broadcast("MAIN SERVER: " + input);

			clearInput();

		});

		// init
		connections = new HashMap();
		eventManager = new EventManager();
		loadConfig();

		// start thread
		listenForConnections = new Thread(this);

	}

	private void loadConfig() {

		try {

			Settings s = new Settings("server.config");

			port = s.getInteger("port");
			max_connected = s.getInteger("maxconnected");

		}

		catch (IOException e) { e.printStackTrace(); }

	}

	//TODO: I use .getSender.getAddress () way too much

	public void startServer() throws IOException {

		try {
			serverSocket = new ServerSocket(port, max_connected);
		}

		catch (BindException e) {
			log("Server already running!");
			System.exit(0);
		}

		log("Waiting for someone to connect on port " + port + "...");

		listenForConnections.start();

	}

	private void closeServer() {

		log("Closing Server...");

		try {

			for (ConnectionClient connection : connections.values()) connection.close();

			log("Closed all connections. Now exiting...");
			System.exit(0);

		}

		catch (IOException e) {

			log("ERROR CLOSING STREAMS AND SOCKETS");
			e.printStackTrace();

		}

		catch (InterruptedException e) { e.printStackTrace(); }

	}

	private void sendNoLog(String message, ConnectionClient connection) {

		try { connection.sendParcel(new ParcelMessage(message)); }
		catch (IOException e) { log("Could not send message to %s.", connection.getSender().getAddress()); e.printStackTrace(); }

	}


	public void broadcast(String message) {

		for (ConnectionClient connection : connections.values()) sendNoLog(message, connection);

		log(message); // TODO: add counter to log instead of using sendNoLog?, e.g. "Hello (20)"

	}

	public void send(String message, String username) {

		sendNoLog(message, connections.get(username));
		log("[to %s]: %s", username, message); // TODO: config?

	}

	// we could just take address from the object instead of using ConnectionClient but idk maybe someone mods their client ofso and that would be trouble
	private void receive(Object object, ConnectionClient connection) {

		if (!(object instanceof Parcel)) { log("Received non-Parcel object from %s.", connection.getSender().getAddress()); return; }

		Parcel p = (Parcel) object;

		p.setSender(connection.getSender());// this is key; must stamp so that the sender info can be passed along.

		Class c = p.getClass();

		if (c == ParcelMessage.class) {

			ParcelMessage message = (ParcelMessage) p;

			if (message.getBody().startsWith("/")) eventManager.callEvent(new EventParcelCommandReceived(new ParcelCommand(message)));
			else eventManager.callEvent(new EventParcelMessageReceived(message));

		} else if (c == ParcelAne.class) {

			ParcelAne ane = (ParcelAne) p;

			switch (ane.getValue()) {

				case (ParcelAne.ANE_HELLO):
					// TODO: connect ?
					break;
				case (ParcelAne.ANE_GOODBYE):
					endConnection(connection);
					break;
				default:
					log("Unknown Ane value from address %s: %s", connection.getSender().getAddress(), ane.getValue());

			}

			eventManager.callEvent(new EventParcelAneReceived(ane));

		} else {

			log("Unknown Parcel received from address %s.", connection.getSender().getAddress());

		}

	}

	private void endConnection(ConnectionClient connection) {

		try {

			String address = connection.getSender().getAddress();

			log("%s is disconnecting...", address);

			connection.close();

			connections.remove(address);

			log("Successfully disconnected.");

		}

		catch (IOException e) { e.printStackTrace(); }

		catch (InterruptedException e) { e.printStackTrace(); }

	}

	// TODO: maybe only add to connections once they've been verified?

	@Override
	public void run() {

		try {

			while (running) {

				Socket socket = serverSocket.accept();

				// TODO: set this only for connect; after that set to 5 mins or something and after the 5 mins catch SocketTimeoutException -> send an ane with a new code "TIMED_OUT" or osmething. or maybe just keep alive parcels?
				//socket.setSoTimeout (3000); // TODO: config
				// TODO: need timeouts

				String address = socket.getInetAddress().getHostAddress();

				log("Attempting to connect to %s...", address);

				ConnectionClient connection = new ConnectionClient(socket) {

					@Override
					public void run() {

						while (isOpen) {

							receive(this.retrieveObject(), this);

						}

					}

				};

				// TODO: a bit of trouble... clients may connect and not send an Ane and thus never be recognized in chat (no announcement)

				connection.start();

				connections.put(connection.getSender().getUsername(), connection);

				log("Connected to %s.", connection.getSender().getAddress());

			}

		}

		catch (IOException e) { e.printStackTrace(); }

	}

	int id = 0; // TODO: very temporary

	public String getNickname(String address) { return "Guest" + String.format("%03d", id++); }

	public EventManager getEventManager() { return eventManager; }

	// not really safe; exposing huge chunk of raw server
	// TODO: maybe only make public safe data like nicknames and String addresses?
	public Collection<ConnectionClient> getConnections() { return connections.values(); }

	public static Server getServer() { return server; }

	public static void staticLog(String message, Object... params) { server.log(message, params); }

	public static void staticBroadcast(String message) { server.broadcast(message); }

	public static void staticSend(String message, String username) { server.send(message, username); }

}