package com.Fawkes.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class ConnectionServer implements Runnable {

	Socket socket;
	ObjectInputStream inputStream;
	ObjectOutputStream outputStream;
	Thread thread;
	public boolean isOpen;

	public ConnectionServer (Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream) {

		this.socket = socket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;

		thread = new Thread (this);

		isOpen = true;

	}

	//TODO: protocol including images

	public Object retrieveObject () {

		try {
			return this.inputStream.readObject ();

		}

		catch (ClassNotFoundException e) { e.printStackTrace (); }
		catch (IOException e) { e.printStackTrace (); }

		return null;

	}

	//TODO: protocol including images

	public void start () {
		thread.start ();

	}

	public void close () throws IOException, InterruptedException {

		isOpen = false;

		inputStream.close ();
		outputStream.close ();
		socket.close ();

		thread.join (1000);

	}

	public void writeObject (Object o) throws IOException {
		outputStream.writeObject (o);
		outputStream.flush ();

	}

	@Override
	public abstract void run ();

}
