package graphtool.gui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import edu.uci.ics.jung.io.GraphIOException;
import graphtool.GraphTool;
import graphtool.graph.GTGraph;
import graphtool.res.Messages;
import graphtool.utils.ActionButton;
import graphtool.utils.DoubleClickAdapter;
import graphtool.utils.FileUtils;

/**
 * Dialog zum Laden vorgefertigter Beispielgraphen, die mit der
 * Anwendung ausgeliefert werden. Die Dateien müssen auf eine der
 * in {@link #FILE_TYPES} angegebenen Endungen lauten.
 */
public class GTSampleGraphDialog extends JDialog {

	private static final long serialVersionUID = -7369006308593354038L;

	/** Erlaubte Dateiendungen für die Beispielgraphen. */
	public static final String[] FILE_TYPES = { ".gml" }; //$NON-NLS-1$
	
	/** Angabe, ob der Dialog mit "OK" beendet wurde. */
	private boolean closedWithOK = false;
	/** Geladener Graph */
	private GTGraph sampleGraph = null;
	
	/** Liste mit Beispielgraphen */
	private JList<String> samplesList = null;

	/**
	 * Baut einen Eingabedialog auf und positioniert ihn innerhalb des umgebenden
	 * Frames. Zur Anzeige muss noch setVisible() aufgerufen werden.
	 * Ob der Dialog mit OK oder mit Abbrechen (Schließen) beendet wurde,
	 * lässt sich durch Aufruf von hasBeenClosedWithOK() feststellen. Ist dies
	 * so, ist der erzeugte Zufallsgraph mittels getSampleGraph() abrufbar.
	 * @param positioner Komponente, zu der der Dialog relativ ausgerichtet werden soll.
	 * @param sampleGraphDir Verzeichnis, in dem die Beispielgraphen zu finden sind. Endet mit "/"
	 * @see #hasBeenClosedWithOK()
	 * @see #getSampleGraph()
	 * @see #FILE_TYPES
	 */
	public GTSampleGraphDialog(String sampleGraphDir, Component positioner) {
		// Modalen Dialog mit den angegebenen Titel erzeugen.
		super(GraphTool.getFrame(), Messages.getString("GTSampleGraphDialog.DialogTitle"), true); //$NON-NLS-1$

		// Einträge im angegebenen Verzeichnis lesen und alphabetisch sortieren
		Set<String> entries = FileUtils.listFilenames(sampleGraphDir, FILE_TYPES);
		String[] items = entries == null ? new String[0] : entries.toArray(new String[0]);
		Arrays.sort(items);

		// Dialog kann über "OK"-Button oder über "Fenster schließen" geschlossen werden.
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		setLayout(new BorderLayout());

		// Liste mit den Beispielgraphen erzeugen
		samplesList = new JList<>(items);
		// Keine Mehrfachauswahl erlaubt
		samplesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// JList unterstützt Scrollen nicht direkt, daher JScrollPane benutzen
		add(new JScrollPane(samplesList), BorderLayout.CENTER);
		
		JPanel panel = new JPanel(new GridLayout(1, 2));
		// OK-Button versucht, den gewählten Graphen zu laden.
		// Gelingt dies, wird der Dialog geschlossen. 
		JButton okButton = new ActionButton(
				Messages.getString("GraphTool.common.OKButtonLabel"),  //$NON-NLS-1$
				(ActionEvent evt) -> {
					if (loadSampleGraph(sampleGraphDir) != null) {
						closedWithOK = true;
						dispose();
					}
				});
		// "Enter" bewirkt das Gleiche wie ein Mausklick auf "OK"
		getRootPane().setDefaultButton(okButton);
		// Doppelklick auf die Liste löst ebenfalls den OK-Button aus.
		samplesList.addMouseListener(new DoubleClickAdapter((MouseEvent evt) -> okButton.doClick()));
		panel.add(okButton);
		JButton cancelButton = new ActionButton(
				Messages.getString("GraphTool.common.CancelButtonLabel"),  //$NON-NLS-1$
				(ActionEvent evt) -> { closedWithOK = false; dispose(); });
		panel.add(cancelButton);
		
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
	 * Lädt einen Graphen aus dem angegebenen Verzeichnis. Der Dateiname
	 * entspricht dem selektierten Eintrag in der Liste.
	 * @param sampleGraphDir Verzeichnis, in dem die Beispielgraphen zu finden sind. Endet mit "/"
	 * @return geladener Graph oder null.
	 * @see #getSampleGraph()
	 */
	public GTGraph loadSampleGraph(String sampleGraphDir) {
		sampleGraph = null;

		// Liefert einen neu erzeugten Graphen oder, im Fehlerfall, null.
		if (samplesList.getSelectedIndex() >= 0) {
			// Pfadnamen der Graphendatei zusammenbauen
			String path = sampleGraphDir + samplesList.getSelectedValue();
			// Input-Stream aus dem Pfad erzeugen
			try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
				// GTGraph-Objekt aus dem Stream lesen
				sampleGraph = new GTGraph(is);
			}
			catch (IOException | GraphIOException e) {
				System.err.println("Could not load graph from InputStream"); //$NON-NLS-1$
				e.printStackTrace();
				sampleGraph = null;
			}
		}
		return sampleGraph;
	}
	
	/**
	 * Liefert den von loadSampleGraph() erzeugten Graphen zurück. Kann null sein.
	 * @return den von loadSampleGraph() erzeugten Graphen.
	 * @see #loadSampleGraph(String)
	 */
	public GTGraph getSampleGraph() {
		return sampleGraph;
	}
}

