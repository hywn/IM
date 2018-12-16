package moe.hilaryoi.im.network;

import moe.hilaryoi.im.Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class ConnectionClient extends ConnectionServer {

	Sender sender;

	public ConnectionClient (Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream) {

		super (socket, inputStream, outputStream);

		String address = socket.getInetAddress ().getHostAddress ();

		sender = new Sender (address, Server.getServer ().getNickname (address));

	}

	public Sender getSender () {
		return sender;

	}

}
