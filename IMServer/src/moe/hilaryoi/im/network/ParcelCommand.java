package moe.hilaryoi.im.network;

import java.util.Arrays;

public class ParcelCommand extends Parcel {

	// I don't want this to be an actual Parcel because then it clutters the client code

	private String commandName;
	private String[] commandArgs;

	public ParcelCommand (ParcelMessage rawMessage) {

		setSender (rawMessage.getSender ());

		// removes initial slash and splits by space
		String[] parts = rawMessage.getBody ().replaceFirst ("/", "").split (" ");

		commandName = parts[0];
		commandArgs = Arrays.copyOfRange (parts, 1, parts.length);

	}

	public String getCommandName () { return commandName; }

	public String[] getCommandArgs () { return commandArgs; }


}
