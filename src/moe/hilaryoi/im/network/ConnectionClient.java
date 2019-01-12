package moe.hilaryoi.im.network;

import moe.hilaryoi.im.server.Server;

import java.io.IOException;
import java.net.Socket;

public abstract class ConnectionClient extends ConnectionServer {

	Sender sender;

	public ConnectionClient (Socket socket) throws IOException {

		super (socket);

		String address = socket.getInetAddress ().getHostAddress ();

		sender = new Sender (address, Server.getServer ().getNickname (address));

	}

	public Sender getSender () {
		return sender;

	}

}
