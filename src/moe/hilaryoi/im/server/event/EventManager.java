package moe.hilaryoi.im.server.event;

import moe.hilaryoi.im.server.Server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class EventManager {

	private ArrayList<Listener> listeners; // TODO: bake as array for speed

	public EventManager () {

		listeners = new ArrayList ();

	}

	public void addListener (Listener listener) { listeners.add (listener); }


	public void callEvent (Event event) {

		System.out.println (event.getClass ());

		for (Listener listener : listeners) {

			if (event.isCancelled ()) break;

			callEvent (listener, event);

		}

	}

	// separate because might need it separate one day ?
	private void callEvent (Listener listener, Event event) {

		for (Method method : listener.getClass ().getMethods ()) {

			if (!method.isAnnotationPresent (EventHandler.class)) continue;
			if (method.getParameterCount () != 1) continue;
			if (method.getParameterTypes ()[0] != event.getClass ()) continue;

			try {

				method.invoke (listener, event);

			}

			catch (InvocationTargetException e) {
				Server.staticLog ("Error while invoking EventHandler method; check system logs.");
				e.printStackTrace ();

			}

			catch (IllegalAccessException e) {
				Server.staticLog ("Error while invoking EventHandler method; check system logs.");
				e.printStackTrace ();

			}

		}

	}

}
