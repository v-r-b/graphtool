package graphtool.utils;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * Spezialisierung von JButton mit vorgegebener Größe und der Möglichkeit,
 * den ActionListener gleich bei der Erzeugung mitzugeben.
 */
public class ActionButton extends JButton {
	
	private static final long serialVersionUID = 6718196709804384849L;
javax.swing.AbstractAction a;
	/** 
	 * Bevorzugte Standardgröße dieser Schaltfläche.
	 * Je nach Layout finden die Angaben für Höhe und Breite unterschiedlich Berücksichtigung. 
	 */
	public static final Dimension PREFERRED_SIZE = new Dimension(150, 35);
	
	/**
	 * Erzeugt eine Schaltfläche mit der angegebenen Größe.
	 * Je nach Layout finden die Angaben für Höhe und Breite unterschiedlich Berücksichtigung. 
	 * @param label Beschriftung der Schaltfläche.
	 * @param al ActionListener, darf null sein.
	 */
	public ActionButton(String label, ActionListener al, Dimension size) {
		super(label);
		setPreferredSize(size);
		if (al != null) {
			addActionListener(al);
		}
	}

	/**
	 * Erzeugt eine Schaltfläche mit der Standardgröße.
	 * @param label Beschriftung des Buttons.
	 * @param al ActionListener, darf null sein.
	 * @see #PREFERRED_SIZE
	 */
	public ActionButton(String label, ActionListener al) {
		this(label, al, PREFERRED_SIZE);
	}
}

