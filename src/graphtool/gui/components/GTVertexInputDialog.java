package graphtool.gui.components;

import java.awt.Component;

import graphtool.graph.GTVertex;
import graphtool.res.Messages;

/** Spezialisierung von GTAbstractInputDialog für Knoten. */
public class GTVertexInputDialog extends GTAbstractInputDialog {

	private static final long serialVersionUID = 7578996271700814160L;

	/** Knoten, dessen Eigenschaften verändert werden sollen. */
	private GTVertex vertex;
	
	/**
	 * Eingabedialog für Knoten erzeugen.
	 * @param positioner Komponente, relativ zu der die Position des Dialog bestimmt werden soll.
	 * @param vertex Knoten, dessen Eigenschaften bearbeitet werden sollen.
	 */
	public GTVertexInputDialog(Component positioner, GTVertex vertex) {
		// Dialogtitel und Textfeldbeschriftung für den typ Knoten 
		super(positioner, 
				Messages.getString("GTVertexInputDialog.DialogTitle"), 		//$NON-NLS-1$
				Messages.getString("GTVertexInputDialog.InputValueLabel")); //$NON-NLS-1$
		// Knoten merken für setValue()
		this.vertex = vertex;
		// Aktuellen Namen des Knoten ins Eingabefeld schreiben.
		textInput.setText(vertex.getName());
		// Text auswählen, damit er direkt überschrieben werden kann.
		textInput.selectAll();
	}
	
	/**
	 * Weist dem im Konstruktor übergebenen Knoten einen neuen Namen zu.
	 * Ist das Textfeld nicht leer, wird dem Knoten der neue
	 * im Dialog eingegebene Name zugewiesen und true zurückgegeben. 
	 * Ist das Eigabefeld leer, so bleibt der
	 * Knoten unverändert und es wird false zurückgegeben. 
	 * @return true wenn der Knotenname neu zugewiesen wurde, false sonst.
	 */
	@Override
	public boolean setValue() {
		// Inhalt des Eingabefelds auslesen.
		String name = textInput.getText();
		// Zulässig sind alle Zeichenketten, die nicht leer sind.
		if (name.length() > 0) {
			vertex.setName(name);
			return true;
		}
		return false;
	}
}

