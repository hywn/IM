package com.Fawkes.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class Connection implements Runnable {

	Socket socket;
	ObjectInputStream inputStream;
	ObjectOutputStream outputStream;
	String address;
	Thread thread;
	public boolean isOpen;

	public Connection() {

	}

	public Connection(Socket socket, ObjectInputStream inputStream,
			ObjectOutputStream outputStream) {

		this.socket = socket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;

		address = socket.getInetAddress().getHostAddress();

		thread = new Thread(this);

		isOpen = true;

	}

	public Object retrieveObject() {

		try {
			return this.inputStream.readObject();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		}

		return null;

	}

	public String getAddress() {
		return address;

	}

	public void start() {
		thread.start();

	}

	public void close() throws IOException {

		onClose();

		isOpen = false;

		inputStream.close();
		outputStream.close();
		socket.close();

	}

	public abstract void onClose();

	public void writeObject(Object o) throws IOException {
		outputStream.writeObject(o);
		outputStream.flush();

	}

	@Override
	public abstract void run();

}
