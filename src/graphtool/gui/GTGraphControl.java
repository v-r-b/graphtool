package graphtool.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import graphtool.GraphTool;
import graphtool.graph.GTEdge;
import graphtool.graph.GTGraph;
import graphtool.graph.GTVertex;
import graphtool.gui.components.GTBuildPanel;
import graphtool.gui.components.GTConsoleWindow;
import graphtool.gui.components.GTControlPanel;
import graphtool.gui.components.GTEdgeInputDialog;
import graphtool.gui.components.GTSourcePanel;
import graphtool.gui.components.GTInfoPanel;
import graphtool.gui.components.GTVertexInputDialog;
import graphtool.gui.components.GTAboutDialog;
import graphtool.res.Messages;
import graphtool.utils.TextComponentPrintStream;

/**
 * GUI-Element zur Steuerung des Programms.
 * Hier finden sich alle GUI-Elemente 
 * <p><ul>
 * <li>zum Erzeugen von Graphen,
 * <li>zur Darstellung und Manipulation von Graphen,
 * <li>zum Ausführen der Algorithmen sowie
 * <li>zur Anzeige weiterer Informationen.
 * </ul>
 */
public class GTGraphControl extends JPanel implements ChangeListener {
	
	private static final long serialVersionUID = -1409645084916312574L;

	/** HTML-Datei mit den Informationen zur Anwendung */
	public static final String ABOUT_FILE_PATH = 
			GraphTool.getResourcePathRelative() + "about.html"; //$NON-NLS-1$
	/** HTML-Datei mit den Lizenzinformationen */
	public static final String LICENSE_FILE_PATH = 
			GraphTool.getResourcePathRelative() + "licenses.html"; //$NON-NLS-1$
	/** Titel des buildPanels. */
	public static final String BUILD_TAB_TITLE = 
			Messages.getString("GTGraphControl.ModificationTabTitle"); //$NON-NLS-1$
	/** Steuerelemente zur Manipulation des Graphen. */
	private GTBuildPanel buildPanel = null;

    /** Titel des controlPanels. */
    public static final String CONTROL_TAB_TITLE = 
    		Messages.getString("GTGraphControl.ExecutionTabTitle"); //$NON-NLS-1$
	/** Steuerelemente zur Ausführung des Algorithmus. */
	private GTControlPanel controlPanel = null;
	/** Anzeige des Sourcecodes. */
	private GTSourcePanel sourcePanel = null;
	
	/** Informationen bei Durchführung des Algorithmus anzeigen. */
	private GTInfoPanel infoPanel = null;
	/** Lizenzinformationen anzeigen */
	private GTAboutDialog aboutDialog = null;
	/** TextArea für den Text der Standard- und Fehlerausgabe */
	private JTextArea consoleText = null;
	/** Dialog zur Anzeige der Standard- und Fehlerausgabe */
	private GTConsoleWindow consoleWindow = null;
	/** Schaltfläche zum Öffnen der Konsole */
	private JButton consoleButton = null;
	/** Stream für die Ausgabe von Stdout und stderr in eine Textkomponente */
	private TextComponentPrintStream tcps = null;
	
	/** Visualisierung des Graphen. */
	private GTGraphVisualization graphVis = null;

	/**
	 * Ruft GTGraphControl(null) auf.
	 * @see #GTGraphControl(ByteArrayOutputStream)
	 */
	public GTGraphControl() {
		this(null);
	}

	/**
	 * Leitet die Standard- und Fehlerausgabe in ein Konsolenfenster um
	 * und erzeugt das GUI-Element zur Steuerung des Programms.
	 * Ruft dazu {@link #initialize()} auf.
	 * Es kann optional ein Stream als Parameter übergeben werden, in den bereits
	 * vor dem Aufruf des Konstruktors die Ausgaben auf die Konsole umgeleitet
	 * wurden. Diese Inhalte werden dann nach der Erzeugung des Konsolenfensters
	 * dorthin geschrieben.
	 * @param earlyConsoleOut Stream, in den die Konsolenausgabe umgeleitet wurde oder null. 
	 * @see TextComponentPrintStream
	 * @see GTConsoleWindow
	 */
	public GTGraphControl(ByteArrayOutputStream earlyConsoleOut) {
		// Die Ausgaben umleiten. Dies wird bereits vor initialize()
		// erledigt, um früh auftretende Ausgaben mit einzusammeln.
		consoleText = new JTextArea();
		// Der consoleButton wird eingefärbt, wenn das Konsolenfenster nicht aktiv ist
		// und eine Meldung ausgegeben wird. 
		// So wird der Nutzer auf neue Meldungen aufmerksam gemacht.
		tcps = new TextComponentPrintStream(consoleText, () -> {
			// Wenn das Konsolenfenster nicht bereits aktiv ist, ...
			if (!getConsoleWindow().isActive()) {
				// dann Farbschema der Konsole zur Markierung 
				// des „Konsole“-Buttons verwenden
				setConsoleWarning(true);
			}
		});

		// Systemausgabekanäle umleiten in den neu erzeugten Stream
		System.setOut(tcps);
		System.setErr(tcps);
		// Wurde bereits vor dem Aufruf des Konstruktors eine Umleitung vorgenommen,
		// wird der Text ausgelesen. Ist Text vorhanden, wird dieser in den neu
		// erzeugten Stream geschrieben und die Warnung für vorhandene Konsolenausgaben
		// wird nach dem Aufbau der grafischen Oberfläche eingeschaltet.
		if (earlyConsoleOut != null) {
			String cOut = earlyConsoleOut.toString();
			if (cOut.length() > 0) {
				System.out.println(cOut);
			}
			initialize();
			if (cOut.length() > 0) {
				setConsoleWarning(true);
			}
		}
		// Ansonsten (earlyConsoleOut == null) wird nur der Aufbau der
		// grafischen Benutzeroberfläche vorgenommen.
		else {
			initialize();
		}
	}
	
	/**
	 * Erzeugt eine JTabbedPane, in der die Steuerungselemente für die 
	 * Erzeugung etc. von Graphen in der einen, die für die Durchführung der
	 * Algorithmen in der anderen Ebene untergebracht werden sowie ein
	 * Visualisierungselement mit einem leeren Graphen.
	 */
	private void initialize() {
		// Neuen, leeren Graphen erzeugen sowie ein Visualisierungselement
		GTGraph graph = getGraphVis().getGraph();				

		// Events, die beim Verändern des Graphen erzeugt werden, sind zu verarbeiten 
		graph.addGraphEventListener(new GEListener());
		
		// TabbedPane mit BuildPanel, ControlPanel und SourcePanel erzeugen.
		// Damit die Steuerelemente am oberen Rand des Panels platziert
		// werden, findet ein BorderLayout Anwendung. Das SourcePanel wird
		// unterhalb des ControlPanels so platziert, dass es den Rest des 
		// zur Verfügung stehenden Platzes einnimmt.
		JTabbedPane tabbedPane = new JTabbedPane();
		
		JPanel padPane = new JPanel(new BorderLayout());
		padPane.add(getBuildPanel(), BorderLayout.NORTH);
        tabbedPane.addTab(BUILD_TAB_TITLE, null, padPane,
        		Messages.getString("GTGraphControl.ModificationTabTooltip")); //$NON-NLS-1$
        
        padPane = new JPanel(new BorderLayout());
		padPane.add(getControlPanel(), BorderLayout.NORTH);
		padPane.add(getSourcePanel(), BorderLayout.CENTER);
		tabbedPane.addTab(CONTROL_TAB_TITLE, null, padPane,
				Messages.getString("GTGraphControl.ExecutionTabTooltip")); //$NON-NLS-1$
		
		tabbedPane.addChangeListener(this);

		// Die beiden Elemente tabbedPane (mit Build- und SourcePanel) und 
		// graphVis (Graphendarstellung mit Einstellungsmöglichkeiten) werden
		// in einer SplitPane untergebracht, um insbesondere den für den
		// Graphen zu verwendenden Platz steuern zu können. Das InfoPanel
		// wird unterhalb der SplitPane installiert.
		setLayout(new BorderLayout());
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, graphVis);
		// Wird das Fenster vergrößert, bekommt die rechte Komponente
		// (hier: der Graph) alle neuen Platz zugeteilt.
		splitPane.setResizeWeight(0.0);
		add(splitPane, BorderLayout.CENTER);
		
		JPanel infoPane = new JPanel(new BorderLayout());
		infoPane.add(getInfoPanel(), BorderLayout.CENTER);
		consoleButton = new JButton(Messages.getString("GTGraphControl.ConsoleButtonLabel")); //$NON-NLS-1$
		consoleButton.setToolTipText(Messages.getString("GTGraphControl.ConsoleButtonTooltip")); //$NON-NLS-1$
		consoleButton.addActionListener((ActionEvent evt) -> getConsoleWindow().setVisible(true));
		infoPane.add(consoleButton, BorderLayout.EAST);
		add(infoPane, BorderLayout.SOUTH);
		// Der Nutzer wird durch das Einfärben des consoleButton auf neue Meldungen aufmerksam
		// gemacht (siehe Konstruktor). 
		// Wird das Fenster dann geöffnet, soll die Farbe zurückgesetzt werden.
		getConsoleWindow().addWindowListener(new WindowAdapter() {
			/** 
			 * Farben der Schaltfläche auf Normal zurücksetzen, wenn das 
			 * Konsolenfenster geöffnet wird 
			 */
			@Override
			public void windowActivated(WindowEvent evt) {
				setConsoleWarning(false);
			}
		});
		getInfoPanel().addActionListener((ActionEvent evt) -> showAboutDialog());

	}
	
	/**
	 * Markiert die Konsolenschaltfläche farblich im Farbschema der Konsole
	 * oder setzt diese Markierung zurück.
	 * @param doMark true, wenn die Markierung gesetzt, false wenn sie gelöscht werden soll.
	 */
	private void setConsoleWarning(boolean doMark) {
		if (consoleButton != null) {
			// Farbschema der Konsole zur Markierung verwenden
			consoleButton.setForeground(doMark ? GTConsoleWindow.FOREGROUND_COLOR : null);
			consoleButton.setBackground(doMark ? GTConsoleWindow.BACKGROUND_COLOR : null);
		}
		
	}
	
	public void showAboutDialog() {
		if (aboutDialog == null) {
			aboutDialog = new GTAboutDialog(GraphTool.getFrame(), true, ABOUT_FILE_PATH, LICENSE_FILE_PATH);
		}
		// Dialog mittig im Hauptfenster platzieren, Höhe/Breite = 90% des Hauptfensters
		Point p = GraphTool.getFrame().getLocation();
		Dimension size = GraphTool.getFrame().getSize();
		aboutDialog.setLocation((int)(p.x + size.width * 0.05), (int)(p.y + size.height * 0.05));
		size.height *= 0.9; 
		size.width *= 0.9;
		aboutDialog.setPreferredSize(size);
		aboutDialog.setSize(size);
		// Anzeigen!
		aboutDialog.setVisible(true);	
	}
	
	/**
	 * Sorgt dafür, dass beim Umschalten auf das ControlPanel zur Ausführung des Algorithmus
	 * der Maus-Modus auf "Picking" umgeschaltet wird, da die nächste Aktion wahrscheinlich
	 * das Auswählen eines Startknotens ist. Beim Zurückgehen auf das BuildPanel
	 * wird wieder auf "Editing" umgeschaltet.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane)e.getSource();
			String activeTitle = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
			if (activeTitle.equals(BUILD_TAB_TITLE)) {
				graphVis.getGraphMouse().setMode(ModalGraphMouse.Mode.EDITING);
			}
			else if (activeTitle.equals(CONTROL_TAB_TITLE)) {
				graphVis.getGraphMouse().setMode(ModalGraphMouse.Mode.PICKING);
			}
		}
	}
	
	/**
	 * Liefert das Visualisierungselement für den Graphen zurück.
	 * Sofern noch nicht vorhanden, wird es neu erzeugt.
	 * @return Visualisierungselement für den Graphen
	 */
	public GTGraphVisualization getGraphVis() {
		if (graphVis == null) {
			GTGraph graph = new GTGraph(EdgeType.DIRECTED);				
			graphVis = new GTGraphVisualization(graph);
		}
		return graphVis;
	}

	/**
	 * Liefert das Panel mit den Steuerelementen zur Erzeugung etc. des
	 * Graphen zurück. Sofern noch nicht vorhanden, wird es neu erzeugt.
	 * @return Panel mit den Steuerelementen zur Erzeugung etc. des Graphen
	 */
	public GTBuildPanel getBuildPanel() {
		if (buildPanel == null) {
			buildPanel = new GTBuildPanel(this);
		}
		return buildPanel;
	}

	/**
	 * Liefert das Panel mit den Steuerelementen zur Durchführung der
	 * Algorithmen zurück. Sofern noch nicht vorhanden, wird es neu erzeugt.
	 * @return Panel mit den Steuerelementen zur Durchführung der Algorithmen
	 */
	public GTControlPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new GTControlPanel(this);
		}
		return controlPanel;
	}
	
	/**
	 * Liefert das Panel mit der Anzeige des Quellcodes zurück.
	 * Sofern noch nicht vorhanden, wird es neu erzeugt.
	 * @return Panel mit der Anzeige des Quellcodes
	 */
	public GTSourcePanel getSourcePanel() {
		if (sourcePanel == null) {
			sourcePanel = new GTSourcePanel();
		}
		return sourcePanel;
	}
	
	/**
	 * Liefert das Panel mit der Anzeige der Informationen zur Durchführung des Algorithmus zurück.
	 * Dies sind im Wesentlichen Inhalte von Warteschlangen oder Mengen, die
	 * bei der Durchführung der Algorithmen verwendet werden.
	 * Sofern noch nicht vorhanden, wird es neu erzeugt.
	 * @return Panel mit der Info-Anzeige
	 */
	public GTInfoPanel getInfoPanel() {
		if (infoPanel == null) {
			infoPanel = new GTInfoPanel();
		}
		return infoPanel;
	}
	
	/**
	 * Liefert das Fenster mit der Anzeige der Meldungen auf stdout und stderr zurück.
	 * Sofern noch nicht vorhanden, wird es neu erzeugt.
	 * @return Panel mit der Anzeige der Ausgaben
	 */
	public GTConsoleWindow getConsoleWindow() {
		if (consoleWindow == null) {
			if (consoleText == null) {
				consoleText = new JTextArea();
			}
			consoleWindow = new GTConsoleWindow(consoleText);
		}
		return consoleWindow;
	}
	
	/** 
	 * Listener-Klasse für GraphEvents, die beim Hinzufügen eines Knotens oder eine Kante
	 * zu einem Graphen einen Dialog anzeigt, um die Eigenschaften des neuen Elements zu bearbeiten.
	 */
	private class GEListener implements GraphEventListener<GTVertex,GTEdge> {
		/** 
		 * Beim Hinzufügen eines Knotens oder eine Kante zu einem Graphen
		 * wird ein Dialog angezeigt, um die Eigenschaften des neuen Elements zu bearbeiten.
		 * @param evt GraphEvent, und zwar<ul>
		 *            <li>vom Typ GraphEvent.Vertex (beim Hinzufügen eines Knotens) oder
		 *            <li>vom Typ GraphEvent.Edge (beim Hinzufügen einer Kante)</ul>
		 */
		@Override
		public void handleGraphEvent(GraphEvent<GTVertex,GTEdge> evt) {
			switch(evt.getType()) {
			// Wenn ein Knoten hinzugefügt wird, handelt es sich beim übergebenen Ereignis
			// um einen GraphEvent.Vertex (Subklasse von GraphEvent), 
			// in dem eine Referenz auf den neu erzeugten Knoten zu finden ist. 
			// Dafür wird ein Eigenschaftendialog angezeigt.
			case VERTEX_ADDED:
				GTVertex vertex = ((GraphEvent.Vertex<GTVertex,GTEdge>)evt).getVertex(); 
				GTVertexInputDialog vid = new GTVertexInputDialog(getGraphVis().getGraphPanel(), vertex);
				vid.setVisible(true);
				// Wurde der Dialog abgebrochen (nicht mit OK beendet), so wird der vom Framework
				// gerade neu hinzugefügte Knoten unmittelbar wieder aus dem Graphen entfernt.
				if (!vid.hasBeenClosedWithOK()) {
					evt.getSource().removeVertex(vertex);
				}
				break;
			// ditto für Kanten und GraphEvent.Edge.
			case EDGE_ADDED:
				GTEdge edge = ((GraphEvent.Edge<GTVertex,GTEdge>)evt).getEdge();
				GTEdgeInputDialog eid = new GTEdgeInputDialog(getGraphVis().getGraphPanel(), edge); 
				eid.setVisible(true);
				if (!eid.hasBeenClosedWithOK()) {
					evt.getSource().removeEdge(edge);
				}
				break;
			// die übrigen Möglichkeiten spielen hier keine Rolle.
			default:
				break;
			}
			
		}
	}
}