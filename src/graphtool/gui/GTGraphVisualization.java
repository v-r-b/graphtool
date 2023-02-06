package graphtool.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import graphtool.algorithm.GTVertexInfo;
import graphtool.algorithm.GTVertexInfoCollection;
import graphtool.graph.GTEdge;
import graphtool.graph.GTGraph;
import graphtool.graph.GTVertex;
import graphtool.gui.components.GTEdgeInputDialog;
import graphtool.gui.components.GTVertexInputDialog;
import graphtool.res.Messages;
import graphtool.utils.ActionButton;

/**
 * Hauptklasse für die grafische Darstellung und Manipulation eines Graphen.
 * Sie stellt dem verwendeten {@link VisualizationViewer} eine Reihe von
 * Funktionen bereit, über die die Darstellung der Knoten und Kanten
 * beeinflusst wird. Diese Funktionen sind als innere Klassen von
 * GTGraphVisualization implementiert.
 * <p>
 * Darüber hinaus stellt die Klasse Möglichkeiten zur Verfügung, bestimmte
 * Knotenrollen (Start, Ziel, aktuell) oder die Zugehörigkeit zu bestimmten
 * Knotensammlungen (siehe {@link #registeredVertexInfoCollections})
 * für die Verwendung in einem Suchalgorithmus zu kennzeichnen.
 */
public class GTGraphVisualization extends JPanel {

	/**
	 * Datensatz für die Registrierung einer Collection mit GTVertexInfo-Elementen
	 * nebst Zusatzinformationen.
	 * @see GTGraphVisualization#registerVertexInfoCollection(GTVertexInfoCollection, int, Color)
	 */
	private static class CollectionInfo {
		/** Sammlung von Knoten */
		private GTVertexInfoCollection vertices;
		/** Position der Collection in der Suchreihenfolge, aufsteigend */
		private int order;
		/** Füllfarbe für Knoten in dieser Collection */
		private Color fillColor;
		/** Convenience-Konstruktor zum einfachen Setzen der Felder */
		private CollectionInfo(GTVertexInfoCollection vic, int ord, Color col) {
			vertices = vic; order = ord; fillColor = col;
		}
	}
	

	private static final long serialVersionUID = -6942639771935250962L;

	/** Der eigentliche Graph, mit dem gearbeitet wird.	*/
	private GTGraph graph = null; 

	/** Ausgewählter Startknoten. */
    private GTVertex startVertex = null;
  	/** Ausgewählter Zielknoten. */
    private GTVertex targetVertex = null;
    /** Aktuell betrachteter Knoten. */
    private GTVertex currentVertex = null;
	/** Aktuell betrachtete Kante. */
    private GTEdge currentEdge = null;
    /** Menge von Knotensammlungen, deren Zustand hervorgehoben dargestellt werden soll */
    private Vector<CollectionInfo> registeredVertexInfoCollections = null; 
    /** Pfad, der hervorgehoben dargestellt werden soll. */
    private HashSet<GTEdge> pathToBeShown = null;
    
    /** Komponente, auf der der Graph angezeigt und bearbeitet werden kann. */
    private VisualizationViewer<GTVertex, GTEdge> graphPanel = null;
    /** Realisierung mausbasierter Operationen auf dem Graphen (Anlegen, Löschen etc.) */
    private GTEditingModalGraphMouse graphMouse = null;
  	/** ComboBox zur Auswahl eines Bearbeitungsmodus für den Graphen */
	private JComboBox<ModalGraphMouse.Mode> modeComboBox = null;
	
    /** Schaltfläche zum Auswählen des Startknotens */
    private ActionButton startNodeSelectionButton = null;
    /** Schaltfläche zum Auswählen des Zielknotens */
    private ActionButton targetNodeSelectionButton = null;
    /** True, wenn der nächste Klick auf einen Knoten 
     *  den Start- oder Zielknoten auswählt. */
    private boolean nodeSelectionInProgress = false;
    
    /**
     * Objekt für die grafische Darstellung und Manipulation eines Graphen erzeugen.
     * Nutzt hierfür die Methode initialize().
     * @param graph Graph, der dargestellt werden soll.
     */
    public GTGraphVisualization(GTGraph graph) {
        this.graph = graph;
        initialize();
        graph.addGraphEventListener(new GraphEventListener<GTVertex, GTEdge>() {
			/** 
			 * Beim Löschen eines Knotens wird geprüft, ob es sich um den Start- oder
			 * Zielknoten handelt. Wenn ja, wird das entsprechende Feld auf null gesetzt.
			 * @param evt GraphEvent vom Typ GraphEvent.Vertex (beim Hinzufügen eines Knotens)
			 */
        	@Override
			public void handleGraphEvent(GraphEvent<GTVertex, GTEdge> evt) {
				switch(evt.getType()) {
				// Wenn ein Knoten hinzugefügt wird, handelt es sich beim übergebenen Ereignis
				// um einen GraphEvent.Vertex (Subklasse von GraphEvent), 
				// in dem eine Referenz auf den neu erzeugten Knoten zu finden ist. 
				// Dafür wird ein Eigenschaftendialog angezeigt.
				case VERTEX_REMOVED:
					GTVertex vertex = ((GraphEvent.Vertex<GTVertex,GTEdge>)evt).getVertex();
					if (vertex == startVertex) {
						// Der Startknoten wurde gerade gelöscht. Eigenschaft auf null setzen.
						startVertex = null;
					}
					else if (vertex == targetVertex) {
						// Der Zielknoten wurde gerade gelöscht. Eigenschaft auf null setzen.
						targetVertex = null;
					}
					break;
					// die übrigen Möglichkeiten spielen hier keine Rolle.
					default:
						break;
				}
        	}
        });
    }
    
    /**
     * Erzeugt eine Fläche, auf der der Graph dargestellt und per Maus bearbeitet
     * werden kann sowie eine Reihe von Bedienelementen, die die Darstellung und
     * den Bearbeitungsmodus steuern. 
     */
    private void initialize() {
		setLayout(new BorderLayout());
		
		// Die zentrale Komponente ist die grafische Darstellung
		add(getGraphPanel(), BorderLayout.CENTER);
		
		// Unterhalb werden Kontrollelemente für die Darstellung
		// oder Bearbeitung angefügt.
		JPanel visControlPanel = new JPanel(new GridLayout(3, 2));

		// modeBoxPanel nimmt die modeComboBox und deren Label auf.
		JPanel modeBoxPanel = new JPanel(new GridBagLayout());
		// Label an Position (0;0), so breit wie notwendig, eingerückt.
		GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, 
													  GridBagConstraints.BOTH, new Insets(0, 5, 0, 10), 0, 0);
		modeBoxPanel.add(new JLabel(Messages.getString("GTGraphVisualization.ModeComboBoxLabel")), c); //$NON-NLS-1$
		// Schaltfläche, um den Bearbeitungsmodus zu wählen.
		// Sie kommt aus EditingModalGraphMouse und trägt englische Bezeichner.
    	modeComboBox = graphMouse.getModeComboBox(); 
		// Zustand der Schaltflächen zum Auswählen von Start- und Zielknoten soll abhängig
		// von der in der ComboBox getroffenen Auswahl sein.
		modeComboBox.setToolTipText(Messages.getString("GTGraphVisualization.ModeComboBoxTooltip")); //$NON-NLS-1$
		// ComboBox an Position (1;0), so breit wie möglich, nicht eingerückt.
		c.gridx = 1; c.weightx = 1.0; c.insets = new Insets(0, 0, 0, 0);
    	modeBoxPanel.add(modeComboBox, c);
    	visControlPanel.add(modeBoxPanel);
		modeComboBox.addItemListener((ItemEvent evt) -> {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					adjustNodeSelectionButtons();
				}
			});

		// Verschiedene Möglichkeiten, die Elemente des Graphen automatisch anordnen
		// zu lassen.
		String layoutTooltip = Messages.getString("GTGraphVisualization.UseLayoutTooltip"); //$NON-NLS-1$
		ActionButton button = new ActionButton(
				Messages.getString("GTGraphVisualization.UseKKLayout"),  //$NON-NLS-1$
				(ActionEvent evt) -> useKamadaKawaiLayout());
		button.setToolTipText(layoutTooltip);
		visControlPanel.add(button);
		button = new ActionButton(
				Messages.getString("GTGraphVisualization.UseCirlceLayout"),  //$NON-NLS-1$
				(ActionEvent evt) -> useCircleLayout());
		button.setToolTipText(layoutTooltip);
		visControlPanel.add(button);
		button = new ActionButton(
				Messages.getString("GTGraphVisualization.UseISOMLayout"),  //$NON-NLS-1$
				(ActionEvent evt) -> useMeyersLayout());
		button.setToolTipText(layoutTooltip);
		visControlPanel.add(button);

		// Auswahl eines Startknotens zulassen.
		startNodeSelectionButton = new ActionButton(
				Messages.getString("GTGraphVisualization.ChooseStartVertex"),  //$NON-NLS-1$
				(ActionEvent evt) -> enableVertexSelection((GTVertex v) -> setStartVertex(v)));
		startNodeSelectionButton.setToolTipText(Messages.getString("GTGraphVisualization.ChooseStartVertexTooltip")); //$NON-NLS-1$
		visControlPanel.add(startNodeSelectionButton);
		
		// Auswahl eines Zielknotens zulassen.
		targetNodeSelectionButton = new ActionButton(
				Messages.getString("GTGraphVisualization.ChooseTargetVertex"),  //$NON-NLS-1$ 
				(ActionEvent evt) -> enableVertexSelection((GTVertex v) -> setTargetVertex(v)));
		targetNodeSelectionButton.setToolTipText(Messages.getString("GTGraphVisualization.ChooseTargetVertexTooltip")); //$NON-NLS-1$
		visControlPanel.add(targetNodeSelectionButton);

		// Zustand (aktiv/inaktiv) richtig setzen
		adjustNodeSelectionButtons();
		
		add(visControlPanel, BorderLayout.SOUTH);
    }
 
 
    /**
     * Dient dazu, einen Knoten auszuwählen und eine bestimmte Aktion damit auszuführen.
     * Nachdem diese Methode aufgerufen wurde, wird der nächste per Mausklick ausgewählte 
     * Knoten an den als Parameter bereitgestellten Consumer übergeben. Dieser kann dann
     * z.B. den Startknoten oder den Zielknoten setzen.
     * @param operation Consumer, an den der ausgewählte Knoten übergeben wird.
     */
    private void enableVertexSelection(Consumer<GTVertex> operation) {
    	// Zustand vermerken und Darstellung der Schaltflächen anpassen
    	nodeSelectionInProgress = true;
    	adjustNodeSelectionButtons();
    	// eventuell vorhandene Auswahl löschen
    	graphPanel.getPickedVertexState().clear();
    	// als Listener für die nächste Auswahl registrieren
    	graphPanel.getPickedVertexState().addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					// Knoten verarbeiten lassen
 					operation.accept((GTVertex)e.getItem());
 					// Knotenauswahl im Graphen im Anschluss löschen
 			        graphPanel.getPickedVertexState().clear();
 			        
 					// Die Auswahl ist eine einmalige Aktion. Der gerade neu erzeugte
 					// ItemListener wird nach erfolgter Auswahl sofort wieder verworfen.
 					graphPanel.getPickedVertexState().removeItemListener(this);
 			    	// Zustand vermerken und Darstellung der Schaltflächen anpassen
 					nodeSelectionInProgress = false;
 					adjustNodeSelectionButtons();
 				}
 			}
         	
         });
     }
        
    /**
     * Passt den Zustand der Schaltflächen zum Auswählen von Start- und Zielknoten (enabled,
     * not enabled) an die Auswahl an, die in der ModeComboBox getroffen wurde.
     * Die Schaltfläche ist nur aktiv, wenn der Modus "PICKING" ist.
     */
    private void adjustNodeSelectionButtons() {
    	if (modeComboBox != null) {
    		// Aktivieren, wenn nicht gerade schon eine Knotenauswahl im Gange ist
    		// und wenn der richtige Bearbeitungsmodus gewählt ist (PICKING)
    		boolean doEnable =
    				!nodeSelectionInProgress &&
    				modeComboBox.getSelectedItem() == ModalGraphMouse.Mode.PICKING;
    		
    		if (startNodeSelectionButton != null) {
    			startNodeSelectionButton.setEnabled(doEnable);
    		}
    		if (targetNodeSelectionButton != null) {
    			targetNodeSelectionButton.setEnabled(doEnable);
    		}
    	}
    }
    
    /**
     * Erstellung der grafischen Komponente zur Anzeige und Bearbeitung des Graphen.
     * Die Methode weist die Komponente dem Feld graphPanel zu. Außerdem erzeugt
     * sie ein Objekt zur Verarbeitung von Mausaktionen auf dem Graphen und weist
     * dieses dem Fed graphMouse zu.
     * @see #graphPanel
     * @see #graphMouse
     */
    private void createGraphPanel() {
    	// Statisches Layout wählen zum interaktiven Erzeugen des Graphen
        Layout<GTVertex, GTEdge> layout = new StaticLayout<>(graph);        
        // Anzeigekomponente mit gewähltem Layout erzeugen
        graphPanel = new VisualizationViewer<>(layout);
        // Den Darstellungsbereich durch Einrahmung hervorheben
        graphPanel.setBorder(BorderFactory.createLineBorder(Color.black, 3));

        // Renderer-Eigenschaften für die Darstellung von Knoten und Kanten setzen:
        // Beschriftung, Farbe und Linienstärke, Zur Erläuterung: siehe die einzelnen
        // inneren Klassen, die hier verwendet werden.
        graphPanel.getRenderContext().setVertexLabelTransformer(new VertexNameLabeler());
        graphPanel.getRenderContext().setVertexFillPaintTransformer(new VertexFiller());
        graphPanel.getRenderContext().setVertexDrawPaintTransformer(new VertexColDrawer());
        graphPanel.getRenderContext().setVertexStrokeTransformer(new VertexStrokeDrawer());
        graphPanel.getRenderContext().setEdgeLabelTransformer(new EdgeWeightLabeler());
        graphPanel.getRenderContext().setEdgeStrokeTransformer(new EdgeStrokeDrawer());
        graphPanel.getRenderContext().setEdgeDrawPaintTransformer(new EdgeColDrawer());
        
        // Mausaktionen auf Knoten und Kanten ermöglichen,
        graphMouse = new GTEditingModalGraphMouse(graphPanel.getRenderContext(), 
        					GTVertex.getFactory(), GTEdge.getFactory());
        graphPanel.setGraphMouse(graphMouse);
        graphPanel.setToolTipText(Messages.getString("GTGraphVisualization.GraphPanelTooltip")); //$NON-NLS-1$
        // Im Modus "Graph erzeugen / verändern" beginnen
        graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
     }
    
    /**
     * Liefert den Handler für Maus-Ereignisse im Zusammenhang mit dem Graphen.
     * Ist dieser noch nicht erzeugt, wird zunächst {@link #createGraphPanel()} aufgerufen.
     * @return Handler für Maus-Ereignisse
     * @see #graphMouse
     */
    public GTEditingModalGraphMouse getGraphMouse() {
    	if (graphMouse == null) {
    		// createGraphPanel erzeugt auch die GraphMouse
    		createGraphPanel();
    	}
    	return graphMouse;
    }
    
    /**
     * Liefert die grafische Komponente zur Darstellung und Bearbeitung des Graphen.
     * Ist diese noch nicht erzeugt, wird zunächst {@link #createGraphPanel()} aufgerufen.
     * @return Komponente grafische Komponente zur Darstellung und Bearbeitung des Graphen
     */
    public VisualizationViewer<GTVertex,GTEdge> getGraphPanel() {
    	if (graphPanel == null) {
    		createGraphPanel();
    	}
    	return graphPanel;
    }
    
    /**
     * CircleLayout verwenden und Ansicht neu zeichnen.
     */
    public void useCircleLayout() {
    	graphPanel.getModel().setGraphLayout(new CircleLayout<>(graph));
    	graphPanel.repaint();
    }
    
    /**
     * KKLayout verwenden und Ansicht neu zeichnen.
     */
    public void useKamadaKawaiLayout() {
    	graphPanel.getModel().setGraphLayout(new KKLayout<>(graph));
    	graphPanel.repaint();
    }
    
    /**
     * ISOMLayout verwenden und Ansicht neu zeichnen.
     */
    public void useMeyersLayout() {
    	graphPanel.getModel().setGraphLayout(new ISOMLayout<>(graph));
    	graphPanel.repaint();
    }

    /**
     * Liefert den Graphen selbst zurück, der hier dargestellt wird.
     * @return dargestellter Graph
     */
    public GTGraph getGraph() {
    	return graph;
    }
    
    /**
     * Ersetzt den Graphen, der hier dargestellt wird.
     * Löscht eventuell vorhandene Voreinstellungen mittels {@link #clearSettings()} und
     * sorgt für eine Aktualisierung der grafischen Darstellung.
     * Das Layout wird auf CircleLayout eingestellt.
     * @param newGraph neuer Graph.
     */
    public void setGraph(GTGraph newGraph) {
    	clearSettings();
    	graph.replaceWith(newGraph);
		useCircleLayout();
    	getGraphPanel().repaint();
    }

   /**
     * Liefert den aktuell ausgewählten Startknoten.
     * @return aktueller Startknoten
     */
    public GTVertex getStartVertex() {
		return startVertex;
	}

    /**
     * Setzt den Startknoten. Wird der bisherige Zielknoten als neuer
     * Startknoten gewählt, wird die Auswahl des Zielknotens gelöscht.
     * @param startVertex neuer Startknoten
     */
	public void setStartVertex(GTVertex startVertex) {
		// Zielknoten löschen, wenn dieser nun Startknoten sein soll. 
		if (startVertex == targetVertex) {
			targetVertex = null;
		}
		this.startVertex = startVertex;
	}

    /**
     * Liefert den aktuell gewählten Zielknoten.
     * @return Aktueller Zielknoten.
     */
    public GTVertex getTargetVertex() {
		return targetVertex;
	}

    /**
     * Setzt den Zielknoten. Wird der bisherige Startknoten als neuer
     * Zielknoten gewählt, wird die Auswahl des Startknotens gelöscht.
     * @param targetVertex Neuer Zielknoten.
     */
	public void setTargetVertex(GTVertex targetVertex) {
		// Startknoten löschen, wenn dieser nun Zielknoten sein soll. 
		if (targetVertex == startVertex) {
			startVertex = null;
		}
		this.targetVertex = targetVertex;
	}

	/** 
	 * Liefert den Knoten, der als "aktuell in Betrachtung" dargestellt wird.
	 * @return als "in Betrachtung" grafisch zu markierender Knoten
	 */
    public GTVertex getCurrentVertex() {
		return currentVertex;
	}

    /**
     * Setzt den Knoten, der grafisch als "in Betrachtung" gekennzeichnet werden soll.
     * @param currentVertex als "in Betrachtung" grafisch zu markierender Knoten
     */
	public void setCurrentVertex(GTVertex currentVertex) {
		this.currentVertex = currentVertex;
    	graphPanel.repaint();
	}
	
	/** 
	 * Liefert die Kante, die als "aktuell in Betrachtung" dargestellt wird.
	 * @return als "in Betrachtung" grafisch zu markierende Kante
	 */
    public GTEdge getCurrentEdge() {
		return currentEdge;
	}

    /**
     * Setzt die Kante, der grafisch als "in Betrachtung" gekennzeichnet werden soll.
     * @param currentEdge als "in Betrachtung" grafisch zu markierende Kante
     */
	public void setCurrentEdge(GTEdge currentEdge) {
		this.currentEdge = currentEdge;
    	graphPanel.repaint();
	}
	
	/**
     * Die als erster Parameter übergebene Collection wird mit den übrigen Zusatzinformationen
     * registriert. Bei der Darstellung eines Knotens (Farbe und Entfernungsinformation) werden
     * die registrierten Collections in aufsteigender Reihenfolge (nach dem Wert von order sortiert)
     * durchsucht. Die erste Collection, in der ein Knoten auf diese Weise gefunden wird,
     * bestimmt über die Darstellung:<ul>
     * <li>von dort wird der Entfernungswert gelesen
     * <li>der bei der Registrierung der Collection übergebene Farbwert wird für die
     *     Füllung des dargestellten Knotens verwendet.
     * </ul> 
     * @param c Collection von GTVertexInfo-Elementen, in der später nach einem Knoten gesucht werden kann.
     * @param order Position dieser Collection in der Reihenfolge des späteren Durchsuchens 
     * 				(je kleiner der Wert, desto früher erfolgt die Suche in dieser Collection).
     * @param fillColor Füllfarbe für die Darstellung von Knoten in dieser Collection.
     */
	public void registerVertexInfoCollection(GTVertexInfoCollection c, int order, Color fillColor) {
		// Liste von Collection-Infos erzeugen, wenn noch nicht vorhanden
		if (registeredVertexInfoCollections == null) {
	        registeredVertexInfoCollections = new Vector<>(); 
		}
		// Neue Collection samt Zusatzinformationen hinzufügen
		registeredVertexInfoCollections.add(new CollectionInfo(c, order, fillColor));
		// Aufsteigend nach dem Feld "order" sortieren, damit später die Suche
		// in der richtigen Reihenfolge stattfindet.
		registeredVertexInfoCollections.sort((o1, o2) -> o1.order - o2.order);
    	graphPanel.repaint();
	}

	/**
	 * Löscht die Registrierungen aller Collections.
	 * @see #registerVertexInfoCollection(GTVertexInfoCollection, int, Color)
	 */
	public void clearRegisteredVertexInfoCollections() {
		if (registeredVertexInfoCollections != null) {
			registeredVertexInfoCollections.clear();
		}
    	graphPanel.repaint();
	}

	/**
	 * Durchsucht die registrierten Knotensammlungen auf einen Eintrag, der den
	 * Knoten v enthält. Die Sammlungen werden nacheinander durchsucht. Die Reihenfolge
	 * bestimmt sich hierbei über den Eintrag "order", der bei der Registrierung
	 * mitgegeben wurde.
	 * @param v Knoten, nach dem gesucht werden soll.
	 * @return Registrierungseintrag für die Collection, in der der Knoten gefunden wurde oder null.
	 * @see #registerVertexInfoCollection(GTVertexInfoCollection, int, Color)
	 */
	private CollectionInfo findRegisteredCollectionEntryFor(GTVertex v) {
    	// Knotensammlungen in aufsteigender Ordnung nach v durchsuchen
		// Die Reihenfolge wird eingehalten, da beim Hinzufügen einer Collection
		// in registerVertexInfoCollection() die Sammlung 
		// registeredVertexInfoCollections jeweils sortiert wird.
		if (registeredVertexInfoCollections != null) {
			for (int i = 0; i < registeredVertexInfoCollections.size(); i++) {
				CollectionInfo ci = registeredVertexInfoCollections.get(i);
				if ((ci != null) && (ci.vertices != null) && ci.vertices.contains(v)) {
					// ersten passenden Eintrag gefunden
					return ci;
				}
			}
		}
    	return null;
	}
	
	/**
	 * Läuft den Pfad vom angegebenen Knoten rückwärts bis zum Startknoten ({@link #startVertex})
	 * ab und vermerkt die Kanten zwischen diesen Knoten in der Sammlung {@link #pathToBeShown}.
	 * Diese wird dann als Pfad im Graphen hervorgehoben dargestellt.
	 * @param vertexInfo Endknoten des darzustellenden Pfads
	 */
	public void recursiveShowPathTo(GTVertexInfo vertexInfo) {
		GTVertexInfo vInfo = vertexInfo;
		// Kantensammlung erzeugen oder, falls vorhanden, leeren
		if (pathToBeShown == null) {
			pathToBeShown = new HashSet<>();
		}
		else {
			pathToBeShown.clear();
		}

		// Im Folgenden ist v der aktuelle Knoten (vInfo das Info-Element dazu) und
		// pi der Vorgängerknoten (piInfo das Info-Element dazu)
		GTVertexInfo piInfo = vInfo.getPredecessor();
		// Solange es einen Vorgängerknoten gibt
		while (piInfo != null) {
			GTVertex v = vInfo.getVertex();
			GTVertex pi = piInfo.getVertex();
			// Kante zwischen aktuellem und Vorgängerknoten finden
			GTEdge edge = graph.findEdge(pi, v);
			if (edge != null) {
				// wenn gefunden, in den Pfad einfügen
				pathToBeShown.add(edge);
			}
			else {
				// ansonsten Fehlermeldung ausgeben (sollte nicht vorkommen)
				System.err.printf(
						Messages.getString("GTGraphVisualization.ErrorMissingEdge_fmt_s_s"+"\n"),  //$NON-NLS-1$ //$NON-NLS-2$
						v.getName(), pi.getName());
			}
			// Wenn der Startknoten erreicht ist, abbrechen.
			if (pi == startVertex) {
				// Pfad komplett gefunden
				break;
			}
			// Nächste Kantensuche vorbereiten
			vInfo = piInfo;
			piInfo = vInfo.getPredecessor();
		}
		// Pfad darstellen
		graphPanel.repaint();
	}

	
	/**
	 * Alle Einstellungen für Hervorhebungen löschen. Dies betrifft den
	 * aktuellen Knoten, die aktuelle Kante, die registrierten
	 * Knotensammlungen sowie den darzustellenden Pfad.
	 * @see #setCurrentVertex(GTVertex)
	 * @see #setCurrentEdge(GTEdge)
	 * @see #clearRegisteredVertexInfoCollections()
	 */
	public void clearEmphases() {
		setCurrentVertex(null);
		setCurrentEdge(null);
		clearRegisteredVertexInfoCollections();
		pathToBeShown = null;
	}
	
	/**
	 * Alle Einstellungen löschen. Dazu gehören Start- und Zielknoten und
	 * alles, was von {@link #clearEmphases()} gelöscht wird. 
	 */
	public void clearSettings() {
		clearEmphases();
		setStartVertex(null);
		setTargetVertex(null);
		nodeSelectionInProgress = false;
		adjustNodeSelectionButtons();
	}

 	/** Steuerung der Beschriftung eines Knotens. */
	private class VertexNameLabeler implements Function<GTVertex, String> {
		/** 
		 * Knoten werden mit dem Knotennamen beschriftet. 
		 * Sofern Informationen zur Entfernung zum Startknoten vorhanden sind,
		 * wird diese hinter dem Namen angezeigt (ggf. unendlich). 
		 * @param input darzustellender Knoten, zu dem die Beschriftung verlangt wird.
		 * @return Knotenbeschriftung, ggf. inkl. Entfernungsangabe
		 */
		@Override
		public String apply(GTVertex input) {
			// Falls Distanzinformationen vorliegen können, den Namen und
			// die Entfernung zum Startknoten (ggf. unendlich) anzeigen.
			if ((registeredVertexInfoCollections != null) &&
					(registeredVertexInfoCollections.size() > 0)) {
				int distance = getDistance(input);
				if (distance == Integer.MAX_VALUE) {
					return input.getName()+": \u221E";	// unendlich //$NON-NLS-1$
				}
				return input.getName()+": "+ distance; //$NON-NLS-1$
			}
			// Sonst nur den Namen darstellen.
			return input.getName();
		}
		
		   /**
	     * Sucht in einer Sammlung nach Distanzinformationen zu einem Knoten.
	     * Wird der Knoten in der Sammlung gefunden, gibt die Methode die dort
	     * gespeicherte Distanz zurück, sonst Integer.MAX_VALUE als Symbol
	     * für unendlich.
	     * @param c Collection, in der der Knoten gesucht werden soll.
	     * @param v in c zu suchender Knoten.
	     * @return Distanzinformation zum Knoten, wenn gefunden, oder Integer.MAX_VALUE.
	     */
	    private int getDistance(GTVertexInfoCollection c, GTVertex v) {
	    	if (c != null) {
	    		GTVertexInfo vi = c.findElementFor(v);
	    		if(vi != null) {
	    			return vi.getDistance();
	    		}
			}
	    	return Integer.MAX_VALUE;
	    }

	    /**
	     * Sucht in allen registrierten Knotensammlungen nach einem Eintrag
	     * für den Knoten v. Die Sammlungen werden in der Reihenfolge ihres
	     * bei der Registrierung angegebenen order-Werts durchsucht.
	     * Die Suche stoppt beim ersten Treffer.
	     * Wird der Knoten gefunden, gibt die Methode die dort
	     * gespeicherte Distanz zurück, sonst Integer.MAX_VALUE als Symbol
	     * für unendlich.
	     * @param v zu suchender Knoten.
	     * @return Distanzinformation zum Knoten, wenn gefunden, oder Integer.MAX_VALUE.
	     */
	    private int getDistance(GTVertex v) {
	    	CollectionInfo ci = findRegisteredCollectionEntryFor(v);
	    	if (ci != null) {
	    		return getDistance(ci.vertices, v);
	    	}
	    	return Integer.MAX_VALUE;
	    }

	}

	/** Steuerung der Füllfarbe eines Knotens. */
	private class VertexFiller implements Function<GTVertex, Paint> {
		/** 
	     * Sucht in allen registrierten Knotensammlungen nach einem Eintrag
	     * für den Knoten v. Die Sammlungen werden in der Reihenfolge ihres
	     * bei der Registrierung angegebenen order-Werts durchsucht.
	     * Die Suche stoppt beim ersten Treffer.
		 * Der Knoten wird mit der Farbe gefüllt, die bei der Registrierung der 
		 * Collection angegeben wurde, in der der Knoten gefunden wurde.
		 * Wird der Knoten nicht gefunden, wird {@link Color#WHITE} zurückgegeben.
		 * @param v zu zeichnender Knoten
		 * @return Farbe zum Füllen des Knotens.
		 * @see GTGraphVisualization#registerVertexInfoCollection(GTVertexInfoCollection, int, Color)
		 */
		@Override
		public Paint apply(GTVertex v) {
	    	CollectionInfo ci = findRegisteredCollectionEntryFor(v);
	    	if (ci != null) {
	    		if ((ci.vertices != null) && ci.vertices.contains(v)) {
	    			// gefunden!
	    			return ci.fillColor;
	    		}
	    	}
	    	return Color.white;
		}
	}

	/** Steuerung der Farbe des Knotenrands. */
	private class VertexColDrawer implements Function<GTVertex, Paint> {
		/** 
		 * Legt die Farbe der Knotenränder fest.
		 * <p><ul>
		 * <li>Startknoten grün, 
		 * <li>Zielknoten rot,
		 * <li>aktueller Knoten gelb,
		 * <li>alle anderen Knoten schwarz.
		 * </ul>
		 * @param v zu zeichnender Knoten
		 * @return Anweisung zum Zeichnen des Knotens.
		 * @see GTGraphVisualization#startVertex 
		 * @see GTGraphVisualization#targetVertex
		 */
		@Override
		public Paint apply(GTVertex v) {
			if (v == startVertex) { 
				return Color.GREEN;
			}
			else if (v == targetVertex) { 
				return Color.RED;
			}
			else if (v == currentVertex) {
				return Color.YELLOW;
			}
			else {
				return Color.BLACK;
			}
		}
	}

	/** Steuerung der Linienstärke des Knotenrands. */
	private class VertexStrokeDrawer implements Function<GTVertex, Stroke> {
		/** Einfacher Rahmen. */ 
		final Stroke plainStroke = new BasicStroke(1.0f);
		/** Hervorgehobener Rahmen. */
		final Stroke emphasizedStroke = new BasicStroke(5.0f);
		/** 
		 * Der aktuell betrachtete Knoten wird mit einem fetten Rand hervorgehoben.
		 * @param v zu zeichnender Knoten
		 * @return Anweisung zum Zeichnen des Knotens.
		 * @see GTGraphVisualization#currentVertex 
		 */
		@Override
		public Stroke apply(GTVertex v) {
			if (v == currentVertex) { 
				return emphasizedStroke;
			}
			return plainStroke;
		}
	}

	
	/** Steuerung der Beschriftung der Kanten. */
	private class EdgeWeightLabeler implements Function<GTEdge, String> {
		/** 
		 * Kanten werden mit ihrem Gewicht beschriftet. 
		 * @param e zu zeichnende Kante
		 * @return Beschriftung der Kante.
		 */
		@Override
		public String apply(GTEdge e) {
			return String.valueOf(e.getWeight());
		}
		
	}
	
	/** Steuerung der Linienstärke der Kante. */
	private class EdgeStrokeDrawer implements Function<GTEdge, Stroke> {
		/** Einfacher Strich. */ 
		final Stroke plainStroke = new BasicStroke(1.0f);
		/** Hervorgehobener Strich. */
		final Stroke emphasizedStroke = new BasicStroke(3.0f);
		/** 
		 * Die aktuell betrachtete Kante wird fett hervorgehoben.
		 * @param e zu zeichnende Kante
		 * @return Anweisung zum Zeichnen der Kante.
		 * @see GTGraphVisualization#currentEdge 
		 */
		@Override
		public Stroke apply(GTEdge e) {
			if (e == currentEdge) { 
				return emphasizedStroke;
			}
			return plainStroke;
		}
	}

	/** Steuerung der Farbe der Kanten. */
	private class EdgeColDrawer implements Function<GTEdge, Paint> {
		/** 
		 * Soll ein Pfad im Graphen markiert werden, so werden die zugehörigen
		 * Kanten rot dargestellt, sonst schwarz.
		 * @param e zu zeichnende Kante
		 * @return Anweisung zum Zeichnen der Kante.
		 * @see GTGraphVisualization#pathToBeShown 
		 */
		@Override
		public Paint apply(GTEdge e) {
			if ((pathToBeShown != null) && pathToBeShown.contains(e)) { 
				return Color.RED;
			}
			return Color.BLACK;
		}
	}
}

