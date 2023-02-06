package graphtool.utils;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Erweiterung von MouseAdapter um die Funktionalität der
 * Ausführung einer nutzerdefinierten Aktion bei einem
 * Doppelklick mit der primären Maustaste.
 */
public class DoubleClickAdapter extends MouseAdapter {
	/** Auszuführende Aktion bei Doppelklick */
	private Consumer<MouseEvent> action;
	
	/**
	 * Erzeugt einen DoubleClickAdapter mit der angegebenen Aktion.
	 * @param action Aktion, die bei Doppelklick ausgeführt werden soll.
	 */
	public DoubleClickAdapter(Consumer<MouseEvent> action) {
		this.action = action;
	}
	
	/**
	 * Stellt fest, ob mit der primären Taste doppelt geklickt wurde
	 * und führt in diesem Fall die dem Konstruktor übergebene Funktion aus.
	 * Der Funktion wird der auslösende MouseEvent als Parameter übergeben.
	 * @param evt auslösender MouseEvent
	 */
	@Override
	public void mouseClicked(MouseEvent evt) {
		super.mouseClicked(evt);
		if (evt.getButton() == MouseEvent.BUTTON1
				&& evt.getClickCount() == 2) {
			action.accept(evt);
		}
	}
}
