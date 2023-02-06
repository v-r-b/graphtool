package graphtool.gui.components;

import java.awt.Component;

import graphtool.graph.GTEdge;
import graphtool.res.Messages;

/** Spezialisierung von GTAbstractInputDialog für Kanten. */
public class GTEdgeInputDialog extends GTAbstractInputDialog {

	private static final long serialVersionUID = -3625480351232914114L;

	/** Kante, deren Eigenschaften verändert werden sollen. */
	private GTEdge edge;
	
	/**
	 * Eingabedialog für Kanten erzeugen.
	 * @param positioner Komponente, relativ zu der die Position des Dialog bestimmt werden soll.
	 * @param edge Kante, deren Eigenschaften bearbeitet werden sollen.
	 */
	public GTEdgeInputDialog(Component positioner, GTEdge edge) {
		super(positioner, 
				Messages.getString("GTEdgeInputDialog.DialogTitle"), 	  //$NON-NLS-1$ 
				Messages.getString("GTEdgeInputDialog.InputValueLabel")); //$NON-NLS-1$ 
		this.edge = edge;
		// Aktuelles Gewicht der Kante ins Eingabefeld schreiben.
		textInput.setText(String.valueOf(edge.getWeight()));
		// Text auswählen, damit er direkt überschrieben werden kann.
		textInput.selectAll();
	}
	
	/**
	 * Versucht, den Inhalt des Eingabefelds in eine Ganzzahl umzuwandeln.
	 * Gelingt dies, wird der im Konstruktor übergebenen Kante das neue
	 * im Dialog eingegebene Gewicht zugewiesen und true zurückgegeben. 
	 * Gelingt es nicht oder ist die Ergebniszahl nicht &gt;= 0, so bleibt die
	 * Kante unverändert und es wird false zurückgegeben. 
	 * @return true wenn das Kantengewicht neu zugewiesen wurde, false sonst.
	 */
	@Override
	public boolean setValue() {
		// Inhalt des Eingabefelds auslesen.
		String sValue = textInput.getText();
		// Initialisierung mit -1 für den Fall einer NumberFormatException.
		int iValue;
		try {
			// Versuchen, aus der Eingabe eine Ganzzahl zu machen
			iValue = Integer.valueOf(sValue).intValue();
		}
		catch (NumberFormatException e) {
			// Keine gültige Zahl eigegeben.
			// Die "Behandlung" dieses Falls wird der aufrufenden
			// Methode überlassen; es wird "false" zurückgegeben.
			return false;
		}
		// Zulässig sind Eingaben >= 0
		if (iValue >= 0) {
			edge.setWeight(iValue);
			return true;
		}
		// Negative Zahl eingegeben.
		return false;
	}
}

