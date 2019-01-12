package moe.hilaryoi.im.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ChatWindow extends JFrame {

	// GUI
	private JTextField messageInput;
	private JTextArea chatWindow;

	public ChatWindow (String title) {

		super (title);

		// GUI
		messageInput = new JTextField ();


		chatWindow = new JTextArea ();
		chatWindow.setEditable (false);

		setLayout (new BorderLayout ());

		add (new JScrollPane (messageInput), BorderLayout.SOUTH);
		add (new JScrollPane (chatWindow), BorderLayout.CENTER);

		setSize (500, 800);
		setVisible (true);
		setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);

	}

	public void log (String message, Object... params) {

		chatWindow.append (String.format (message, params) + "\n");

	}

	// on enter
	public void addInputListener (ActionListener listener) { messageInput.addActionListener (listener); }

	protected String getInput () { return messageInput.getText (); }

	protected void clearInput () { messageInput.setText (""); }

	protected void setInputEditable (boolean enabled) { messageInput.setEditable (enabled); } // SwingUtilities.invokeLater (() -> messageInput.setEditable (tf));

}
