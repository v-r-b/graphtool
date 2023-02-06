package graphtool.utils;

import javax.swing.JTextField;

/**
 * Eingabefeld, das nur Ziffern zulässt.
 */
public class IntTextField extends JTextField {
	
	private static final long serialVersionUID = -1921280610523766697L;

	/**
	 * Initialisiert das neue Textfeld mit dem Wert Null.
	 */
	public IntTextField() {
		this(0);
	}
	
	/**
	 * Initialisiert das neue Textfed mit dem angegebenen Wert.
	 * @param initialValue Startwert des Textfelds.
	 */
	public IntTextField(int initialValue) {
		super(String.valueOf(initialValue));
	}

	/**
	 * Liefert den int-Wert des Inhalts des Eingabefelds.
	 * @return den Wert des Feldinhalts als int.
	 * @throws NumberFormatException wenn der Feldinhalt leer ist.
	 */
	public int getValue() throws NumberFormatException {
		return Integer.valueOf(getText()).intValue();
	}
	
	/**
	 * Lässt nur die Ziffern [0..9]* oder eine leere Zeichenkette zu. 
	 * Bei anderen Werten des Parameters wird der Feldinhalt nicht verändert.
	 * @param text Neuer Inhalt des Textfelds.
	 */
	@SuppressWarnings("nls")
	@Override
	public void setText(String text) {
		if (text.matches("[0-9]*") || (text == "")) {
			super.setText(text);
		}
	}

	/**
	 * Lässt nur die Eingabe von Ziffern [0..9]* oder eine leere Zeichenkette zu. 
	 * Bei anderen Werten des Parameters wird der Feldinhalt nicht verändert.
	 * @param text Neuer Inhalt des Textfelds.
	 */
	@SuppressWarnings("nls")
	@Override
	public void replaceSelection(String text) {
		if (text.matches("[0-9]*") || (text == "")) {
			super.replaceSelection(text);
		}
	}
}

