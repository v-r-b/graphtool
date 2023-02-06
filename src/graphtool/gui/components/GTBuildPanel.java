package graphtool.gui.components;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

import edu.uci.ics.jung.graph.util.EdgeType;
import graphtool.GraphTool;
import graphtool.graph.GTGraph;
import graphtool.gui.GTGraphControl;
import graphtool.res.Messages;
import graphtool.utils.ActionButton;

/**
 * Panel mit Schaltflächen zum Erzeugen, Laden und Speichern von Graphen.
 * Es wird in das Hauptpanel GTGraphControl eingebettet.
 */
public class GTBuildPanel extends GTAbstractPanel {

	private static final long serialVersionUID = -4865842885694355865L;

	/** Verzeichnis mit den Beispielgraphen. In der Konfiguration einstellbar. */
	public static final String RESOURCE_DIR = GraphTool.getResourcePathRelative();

	/**
	 * Grafische Elemente für das Verändern eines Graphen erzeugen.
	 * Ruft {@link #initialize()} auf.
	 */
	public GTBuildPanel(GTGraphControl mainPanel) {
		super(mainPanel);
		initialize();
	}

	/**
	 * Grafische Elemente für das Verändern eines Graphen erzeugen.
	 */
	private void initialize() {
		ActionButton button;

		// Die Elemente werden, alle in gleicher Größe, untereinander angeordnet.
		setLayout(new GridLayout(12, 1));
		
		// Button zur Erzeugung eines neuen gerichteten Graphen hinzufügen. 
		button = new ActionButton(
				Messages.getString("GTBuildPanel.NewEmptyDirectedGraph"),  //$NON-NLS-1$
				(ActionEvent evt) -> {
					try {
						// Den Graphen durch einen leeren gerichteten Graphen ersetzen
						getGraphVis().setGraph(new GTGraph(EdgeType.DIRECTED));
					} catch (Exception e) {
						System.err.println("Could not create empty directed graph"); //$NON-NLS-1$
						e.printStackTrace();
					}
				});
		button.setToolTipText(Messages.getString("GTBuildPanel.NewEmptyGraphTooltip")); //$NON-NLS-1$
		// Neu erzeugten Button zum Panel hinzufügen.
		add(button);

		// ditto für einen ungerichteten Graphen
		button = new ActionButton(
				Messages.getString("GTBuildPanel.NewEmptyUndirectedGraph"),  //$NON-NLS-1$
				(ActionEvent evt) -> {
					try {
						// Den Graphen durch einen leeren ungerichteten Graphen ersetzen
						getGraphVis().setGraph(new GTGraph(EdgeType.UNDIRECTED));
					} catch (Exception e) {
						System.err.println("Could not create empty undirected graph"); //$NON-NLS-1$
						e.printStackTrace();
					}
				});
		button.setToolTipText(Messages.getString("GTBuildPanel.NewEmptyGraphTooltip")); //$NON-NLS-1$
		add(button);

		// ditto für das Erzeugen eines Zufallsgraphen
		button = new ActionButton(
				Messages.getString("GTBuildPanel.CreateRandomGraph"),  //$NON-NLS-1$
				(ActionEvent evt) -> {
					GTRandomGraphDialog rgdia = new GTRandomGraphDialog(GTBuildPanel.this);
					rgdia.setVisible(true);
					if (rgdia.hasBeenClosedWithOK() && (rgdia.getRandomGraph() != null)) {
						getGraphVis().setGraph(rgdia.getRandomGraph());
						// Bei matrixartigen Graphen anderes Layout verwenden
						if (rgdia.getRandomGraphType() == GTRandomGraphDialog.CreationType.MATRIX) {
							getGraphVis().useKamadaKawaiLayout();
						}
					}
				});
		button.setToolTipText(Messages.getString("GTBuildPanel.CreateRandomGraphTooltip")); //$NON-NLS-1$
		add(button);
		
		// ditto für das Laden eines Beispielgraphen
		button = new ActionButton(
				Messages.getString("GTBuildPanel.LoadSampleGraph"),  //$NON-NLS-1$
				(ActionEvent evt) -> {
					try {
						GTSampleGraphDialog sgdia = new GTSampleGraphDialog(
								RESOURCE_DIR, GTBuildPanel.this);
						sgdia.setVisible(true);
						if (sgdia.hasBeenClosedWithOK() && (sgdia.getSampleGraph() != null)) {
							getGraphVis().setGraph(sgdia.getSampleGraph());
						}
					}
					catch (Exception e) {
						System.err.println("Could not load sample graph"); //$NON-NLS-1$
						e.printStackTrace();
					}
				});
		button.setToolTipText(Messages.getString("GTBuildPanel.LoadSampleGraphTooltip")); //$NON-NLS-1$
		add(button);
		
		// ditto für das Laden eines Graphen
		button = new ActionButton(
				Messages.getString("GTBuildPanel.LoadGraph"),  //$NON-NLS-1$
				(ActionEvent evt) -> {
					try {
						GTFileChooser fc = new GTFileChooser();
						int result = fc.showOpenDialog(GTBuildPanel.this);
						if (result == GTFileChooser.APPROVE_OPTION) {
							getGraphVis().setGraph(new GTGraph(fc.getAbsolutePathSelected()));
						}
					} catch (Exception e) {
						System.err.println("Could not load graph"); //$NON-NLS-1$
						e.printStackTrace();
					}
				});
		button.setToolTipText(Messages.getString("GTBuildPanel.LoadGraphTooltip")); //$NON-NLS-1$
		add(button);
		
		// ditto für das Speichern eines Graphen
		button = new ActionButton(
				Messages.getString("GTBuildPanel.SaveGraph"),  //$NON-NLS-1$
				(ActionEvent evt) -> {
				try {
					GTFileChooser fc = new GTFileChooser();
					int result = fc.showSaveDialog(GTBuildPanel.this);
			        if (result == GTFileChooser.APPROVE_OPTION) {
				        getGraphVis().getGraph().writeToFile(fc.getAbsulutePathSelectedWithExt());
			        }
				} catch (IOException e) {
					System.err.println("Could not save graph"); //$NON-NLS-1$
					e.printStackTrace();
				}
		});
		button.setToolTipText(Messages.getString("GTBuildPanel.SaveGraphTooltip")); //$NON-NLS-1$
		add(button);
	}
}

