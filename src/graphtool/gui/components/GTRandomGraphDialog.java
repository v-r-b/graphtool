package graphtool.gui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.uci.ics.jung.graph.util.EdgeType;
import graphtool.GraphTool;
import graphtool.graph.GTGraph;
import graphtool.graph.GTGraphGenerator;
import graphtool.res.Messages;
import graphtool.utils.ActionButton;
import graphtool.utils.IntTextField;

/**
 * Dialog zur Eingabe von Informationen, die zur Erzeugung eines
 * Zufallsgraphen verwendet werden sollen.
 */
public class GTRandomGraphDialog extends JDialog {

	private static final long serialVersionUID = 5640130857460003128L;
	
	/** Typ eines zu erzeugenden Zufallsgraphen. */
	public static enum CreationType { 
		/** Eher kreisförmiger Zufallsgraph */
		CIRCLE, 
		/** Eher matrixförmiger Zufallsgraph */
		MATRIX
		}

	/** Angabe, ob der Dialog mit "OK" beendet wurde. */
	private boolean closedWithOK = false;
	/** Typ des erzeugten Graphen */
	private CreationType randomGraphType;
	/** Erzeugter Graph */
	private GTGraph randomGraph = null;
	
	/** Angabe, ob gerichtet oder ungerichtet, für Typ CIRCLE */
	private JCheckBox circleIsDirected = new JCheckBox();
	/** Anzahl Knoten für Typ CIRCLE */
	private IntTextField circleNumVertices = new IntTextField(6);
	/** Anzahl zusätzlicher Kanten für Typ CIRCLE */
	private IntTextField circleNumAdditionalEdges = new IntTextField(3);
	/** Maximales Kantengewicht für Typ CIRCLE */
	private IntTextField circleMaxWeight = new IntTextField(99);

	/** Angabe, ob gerichtet oder ungerichtet, für Typ MATRIX */
	private JCheckBox matrixIsDirected = new JCheckBox();
	/** Anzahl Zeilen/Spalten für Typ MATRIX */
	private IntTextField matrixNumRowsCols = new IntTextField(4);
	/** Angabe, ob toroidal, für Typ MATRIX */
	private JCheckBox matrixIsToroidal = new JCheckBox();
	/** Maximales Kantengewicht für Typ MATRIX */
	private IntTextField matrixMaxWeight = new IntTextField(99);

	/**
	 * Baut einen Eingabedialog auf und positioniert ihn innerhalb des umgebenden
	 * Frames. Zur Anzeige muss noch setVisible() aufgerufen werden.
	 * Ob der Dialog mit OK oder mit Abbrechen (Schließen) beendet wurde,
	 * lässt sich durch Aufruf von hasBeenClosedWithOK() feststellen. Ist dies
	 * so, ist der erzeugte Zufallsgraph mittels getRandomGraph() abrufbar.
	 * @param positioner Komponente, zu der der Dialog relativ ausgerichtet werden soll.
	 * @see #hasBeenClosedWithOK()
	 * @see #getRandomGraph()
	 */
	@SuppressWarnings("nls")
	public GTRandomGraphDialog(Component positioner) {
		// Modalen Dialog mit den angegebenen Titel erzeugen.
		super(GraphTool.getFrame(), Messages.getString("GTRandomGraphDialog.DialogTitle"), true); 
		// Dialog kann über "OK"-Button oder über "Fenster schließen" geschlossen werden.
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// Über eine TabbedPane kann ausgewählt werden, welcher Typ Graph erzeugt werden soll.
		JTabbedPane tabbedPane = new JTabbedPane();
		
		// Eingabebereich für Typ "CIRCLE":
		JPanel panel = new JPanel(new GridLayout(4, 2));
		panel.add(new Label(Messages.getString("GTRandomGraphDialog.IsDirectedGraph")));
		panel.add(circleIsDirected);
		panel.add(new Label(Messages.getString("GTRandomGraphDialog.Circle.NumNodes")));
		panel.add(circleNumVertices);
		panel.add(new Label(Messages.getString("GTRandomGraphDialog.Circle.NumAdditionalEdges")));
		panel.add(circleNumAdditionalEdges);
		panel.add(new Label(Messages.getString("GTRandomGraphDialog.MaxWeight")));
		panel.add(circleMaxWeight);
        tabbedPane.addTab(Messages.getString("GTRandomGraphDialog.Circle.TabTitle"), panel);

		// Eingabebereich für Typ "MATRIX":
		panel = new JPanel(new GridLayout(4, 2));
		panel.add(new Label(Messages.getString("GTRandomGraphDialog.IsDirectedGraph")));
		panel.add(matrixIsDirected);
		panel.add(new Label(Messages.getString("GTRandomGraphDialog.Matrix.NumRowsCols")));
		panel.add(matrixNumRowsCols);
		panel.add(new Label(Messages.getString("GTRandomGraphDialog.Matrix.IsToroidal")));
		panel.add(matrixIsToroidal);
		panel.add(new Label(Messages.getString("GTRandomGraphDialog.MaxWeight")));
		panel.add(matrixMaxWeight);
        tabbedPane.addTab(Messages.getString("GTRandomGraphDialog.Matrix.TabTitle"), panel);

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);

		panel = new JPanel(new GridLayout(1, 2));
		// OK-Button versucht, einen Graphen mit den eingegebenen Werten zu erzeugen.
		// Gelingt dies, wird der Dialog geschlossen. Ansonsten bleibt er zur
		// Korrektur der Eingabe offen.
		JButton b = new ActionButton(
				Messages.getString("GraphTool.common.OKButtonLabel"), //$NON-NLS-1$
				(ActionEvent evt) -> {
					if (createRandomGraph(CreationType.values()[tabbedPane.getSelectedIndex()]) != null) {
						closedWithOK = true;
						dispose();
					}
				});
		// "Enter" bewirkt das Gleiche wie ein Mausklick auf "OK"
		getRootPane().setDefaultButton(b);
		panel.add(b);
		b = new ActionButton(
				Messages.getString("GraphTool.common.CancelButtonLabel"), //$NON-NLS-1$
				(ActionEvent evt) -> { closedWithOK = false; dispose(); });
		panel.add(b);
		add(panel, BorderLayout.SOUTH);

		// Komponenten anordnen, Position für den Dialog bestimmen.
		pack();
		this.setLocationRelativeTo(positioner);
	}

	/**
	 * Gibt an, ob der Dialog mittels OK geschlossen wurde.
	 * Abhängig von dieser Information kann der Nutzer unterschiedliche Aktionen ausführen,
	 * @return true wenn der Dialog mit "OK" geschlossen wurde.
	 */
	public boolean hasBeenClosedWithOK() {
		return closedWithOK;
	}
	
	/**
	 * Erzeugt einen Graphen, sofern die im Dialog eingegebenen Werte
	 * zulässig sind. Nachfolgende Aufrufe von getRandomGraph() liefern
	 * den hier erzeugten Graphen zurück, die von getRandomGraphType()
	 * den Typ des hier zeugten Graphen. Kann die Methode keinen Graphen
	 * erzeugen, liefert sie null.
	 * @param type gibt an, welcher Typ (siehe {@link CreationType}) von Zufallsgraph erzeugt werden soll.
	 * @return erzeugten Graphen oder null.
	 * @see #getRandomGraph()
	 * @see GTGraphGenerator
	 */
	public GTGraph createRandomGraph(CreationType type) {
		randomGraph = null;
		randomGraphType = type;
		switch (type) {
		case CIRCLE:
			try {
				// Der Nachfolgende Aufruf führt zu einer NumberFormatException,
				// wenn die Eingabefelder ungültige Zeichen enthalten. Durch die 
				// Verwendung von IntTextField kann dies nur der Fall sein,
				// wenn ein Feld leer ist.
				randomGraph = GTGraphGenerator.generateCircleGraph(
						circleIsDirected.isSelected() ? EdgeType.DIRECTED : EdgeType.UNDIRECTED,
						circleNumVertices.getValue(), 
						circleNumAdditionalEdges.getValue(), 
						circleMaxWeight.getValue());
			}
			catch (NumberFormatException nfe) {
				// Nutzer auf eine Fehleingabe hinweisen.
				Toolkit.getDefaultToolkit().beep();			
			}
			break;
		case MATRIX:
			try {
				randomGraph = GTGraphGenerator.generateLattice2DGraph(
						matrixIsDirected.isSelected() ? EdgeType.DIRECTED : EdgeType.UNDIRECTED,
						matrixNumRowsCols.getValue(),
						matrixIsToroidal.isSelected(),
						matrixMaxWeight.getValue());
			}
			catch (NumberFormatException nfe) {
				Toolkit.getDefaultToolkit().beep();			
			}
			break;
		default:
			// Dieser Fall darf eigentlich nicht eintreten, es sei denn, bei Erweiterungen
			// von CreationType wird diese Methode nicht entsprechend angepasst.
			(new IllegalArgumentException("CreationType unknown: " + type)).printStackTrace(); //$NON-NLS-1$
			break;
		}
		// Liefert einen neu erzeugten Graphen oder, im Fehlerfall, null.
		return randomGraph;
	}
	
	/**
	 * Liefert den von createRandomGraph() erzeugten Graphen zurück. Kann null sein.
	 * @return den von createRandomGraph() erzeugten Graphen.
	 * @see #createRandomGraph(CreationType)
	 */
	public GTGraph getRandomGraph() {
		return randomGraph;
	}

	/**
	 * Liefert den Typ des von createRandomGraph() erzeugten Graphen zurück. 
	 * Abhängig davon kann z.B. das verwendete Layout gewählt werden.
	 * @return den Typ des von createRandomGraph() erzeugten Graphen.
	 * @see #createRandomGraph(CreationType)
	 */
	public CreationType getRandomGraphType() {
		return randomGraphType;
	}
}

