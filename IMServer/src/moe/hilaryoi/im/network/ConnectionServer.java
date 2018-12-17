package moe.hilaryoi.im.network;

import moe.hilaryoi.im.gui.ChatWindow;

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

	public ConnectionServer (Socket socket) throws IOException {

		this.socket = socket;
		this.inputStream = new ObjectInputStream (socket.getInputStream ());
		this.outputStream = new ObjectOutputStream (socket.getOutputStream ());

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

	public void trySendParcel (Parcel parcel, ChatWindow)

	public void sendParcel (Parcel parcel) throws IOException { writeObject (parcel); }

	// nothing should be sent except parcels
	private void writeObject (Object o) throws IOException {

		outputStream.writeObject (o);
		outputStream.flush ();

	}

	@Override
	public abstract void run ();

}
