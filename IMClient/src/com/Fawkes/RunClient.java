package com.Fawkes;

public class RunClient {
	public static void main(String[] args) {

		Client client = new Client();

		try {
			client.startRunning();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();

		}

	}
}
