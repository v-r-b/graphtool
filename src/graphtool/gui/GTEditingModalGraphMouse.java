package graphtool.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import com.google.common.base.Supplier;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EdgeSupport;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.EditingPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.SimpleEdgeSupport;
import edu.uci.ics.jung.visualization.picking.PickedState;
import graphtool.graph.GTEdge;
import graphtool.graph.GTGraph;
import graphtool.graph.GTVertex;
import graphtool.gui.components.GTEdgeInputDialog;
import graphtool.gui.components.GTVertexInputDialog;
import graphtool.res.Messages;

/**
 * Dient zur direkten grafischen Bearbeitung eines Graphen. Es ist z.B. möglich,<ul>
 * <li>den Graphen zu verschieben, zu drehen und zu zoomen,
 * <li>Kanten und Knoten zu erzeugen, zu ändern oder zu löschen
 * </ul>
 */
public class GTEditingModalGraphMouse extends EditingModalGraphMouse<GTVertex,GTEdge> {

	/**
	 * Diese Klasse dient zur Externalisierung der Beschriftungen in der modeComboBox.
	 * Die modeComboBox ist das Ergebnis des Aufrufs {@link GTEditingModalGraphMouse#getModeComboBox()}.  
	 */
	public static class ModeBoxRenderer extends BasicComboBoxRenderer.UIResource {
		
		private static final long serialVersionUID = -6378917363486099477L;
		
		/** Übersetzung von Einträgen in Beschriftungen */
		private HashMap<ModalGraphMouse.Mode, String> labelMap = null;
		
		/** 
		 * Lädt für alle Einträge in der übergebenen ComboBox die Beschriftungen
		 * per Messages-Klasse und vermerkt diese in der labelMap.
		 * @param modeBox ComboBox, die beschriftet werden soll.
		 */
		public ModeBoxRenderer(JComboBox<ModalGraphMouse.Mode> modeBox) {
			labelMap = new HashMap<>();
			for (int i = 0; i < modeBox.getItemCount(); i++) {
				// Zu jedem Eintrag (Mode) in der ComboBox ...
				ModalGraphMouse.Mode key = modeBox.getItemAt(i);
				// ... besorge die passende Beschriftung.
				@SuppressWarnings("nls")
				String value = Messages.getString("GTEditingModalGraphMouse." + key + "Label");
				if (value != null) {
					labelMap.put(key, value);
				}
				// Falls nicht gefunden, nimm den Namen des Eintrags.
				else {
					labelMap.put(key,  key.toString());
				}
			}
		}

		/**
		 * Liefert zum Objekt value die passende Beschriftung aus der labelMap.
		 * @see BasicComboBoxRenderer.UIResource#getListCellRendererComponent(JList, Object, int, boolean, boolean)
		 */
		@SuppressWarnings("rawtypes")
		@Override
		public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (c instanceof JLabel) {
				((JLabel)c).setText(labelMap.get(value));
			}
			return c;
		}
	}
	
	
	/**
	 * Ruft den Konstruktor der Superklasse auf. Zur Beschreibung der Parameter siehe dort.
	 * Außerdem erhält die ModeComboBox einen Renderer, der anstelle der Namen der Modi
	 * die passenden Beschriftungen aus der Messages-Datei verwendet.
	 */
	@SuppressWarnings("unchecked")
	public GTEditingModalGraphMouse(RenderContext<GTVertex,GTEdge> rc,
			Supplier<GTVertex> vertexFactory, Supplier<GTEdge> edgeFactory) {
		super(rc, vertexFactory, edgeFactory);
		JComboBox<ModalGraphMouse.Mode> modeComboBox = getModeComboBox();
		modeComboBox.setRenderer(new ModeBoxRenderer(modeComboBox));
	}

	/**
	 * Erzeugt und lädt die Plugins für die grafische Bearbeitung des Graphen.
	 * <p>
	 * Es wird zunächst die Standardimplementierung der Superklasse aufgerufen.
	 * Anpassungen:<ul>
	 * <li>Das in der Superklasse erzeugte PopupEditingPlugin ({@link EditingPopupGraphMousePlugin}), das
	 * nicht kompatibel zu GTGraph ist, wird entfernt und durch eine kompatible Version ersetzt.
	 * <li>Dem in der Superklasse erzeugten EditingPlugin ({@link EditingGraphMousePlugin}) wird
	 * eine zu GTGraph kompatible {@link EdgeSupport}-Implementierung bereitgestellt.
	 * </ul>
	 * @see TypedEditingPopupGraphMousePlugin
	 * @see TypedEdgeSupport
	 */
	@Override
	protected void loadPlugins() {
		// Standard-Plugins laden
		super.loadPlugins();
		// Das PopupEditing-Plugin, das bei loadPlugins() hinzugefügt wurde, entfernen, 
		// weil es nicht kompatibel zu GTGraph ist. 
		remove(popupEditingPlugin);
		// Stattdessen kompatibles Popup-Plugin erzeugen und hinzufügen
		popupEditingPlugin = new TypedEditingPopupGraphMousePlugin(vertexFactory, edgeFactory);
		add(popupEditingPlugin);
		// Dafür sorgen, dass Kanten des richtigen Typs erzeugt werden
		editingPlugin.setEdgeSupport(new TypedEdgeSupport(edgeFactory));
	}

	/**
	 * Klasse zur Unterstützung der Erstellung von Kanten. Während SimpleEdgeSupport immer
	 * nur ungerichtete Kanten erzeugt, orientiert sich diese Klasse am Kantentyp des verwendeten
	 * Graphen. Außerdem eliminiert diese Klasse das Problem, dass eine Exception geworfen wird,
	 * wenn der Endpunkt einer erzeugten Kante kein Knoten ist.
	 */
	private class TypedEdgeSupport extends SimpleEdgeSupport<GTVertex,GTEdge> {
		/** Konstruktor */
		public TypedEdgeSupport(Supplier<GTEdge> edgeFactory) {
			super(edgeFactory);
		}

		/**
		 * Abweichend von der Methode der Superklasse orientiert sich diese Methode am Standard-
		 * Kantentyp des verwendeten Graphen, statt immer ungerichtete Kanten zu erzeugen.
		 * @param vv Visualisierungskomponente für den Graphen
		 * @param startVertex Startknoten für die Erzeugung der Kante oder null. 
		 * @param startPoint Koordinate des Kantenanfangs
		 * @param edgeType gibt an, ob eine gerichtete oder ungerichtete Kante erzeugt werden soll.
		 */
		@Override
		public void startEdgeCreate(BasicVisualizationServer<GTVertex,GTEdge> vv,
				GTVertex startVertex, Point2D startPoint, EdgeType edgeType) {
			// graph.getDefaultEdgeType() gibt an, welche Art von Kante (gerichtet oder
			// ungerichtet) im verwendeten Graphen erwartet wird. Die erzeugte Kante
			// hat dann auch diesen Typ.
			Graph<GTVertex, GTEdge> graph = vv.getGraphLayout().getGraph();
			super.startEdgeCreate(vv, startVertex, startPoint, graph.getDefaultEdgeType());
		}

		/**
		 * Abweichend von der Methode der Superklasse ignoriert diese Methode Kanten, deren
		 * Endpunkt kein Knoten ist (endVertex == null), statt eine Exception zu werfen.
		 * @param vv Visualisierungskomponente für den Graphen
		 * @param endVertex Zielknoten für die Erzeugung der Kante oder null. 
		 */
		@Override
		public void endEdgeCreate(BasicVisualizationServer<GTVertex,GTEdge> vv, GTVertex endVertex) {
			if(endVertex == null) {
				// Ein Aufruf der Methode der Superklasse mit startVertex == null bewirkt,
				// dass die von startEdgeCreate angefangene Erzeugung einer Kante ignoriert wird.
				startVertex = null;
			}
			super.endEdgeCreate(vv, endVertex);
			if(endVertex == null) {
				// Allerdings ist in diesem Fall noch vv.repaint() aufzurufen, um die in der
				// Grafik sichtbare, im "Leeren" endende Kante zu löschen.
				vv.repaint();
			}
		}
	}

	/**
	 * Plugin zur Anzeige eines Popup-Menüs bei Betätigen der rechten Maustaste.
	 * Die Implementierung ist sehr eng an die der Superklasse angelehnt, die jedoch
	 * nicht für eine Verwendung mit {@link GTGraph} geeignet ist. Grund hierfür ist, dass
	 * GTGraph als Subklasse von {@link ObservableGraph} weder {@link DirectedGraph}
	 * noch {@link UndirectedGraph} implementiert, {@link EditingPopupGraphMousePlugin}
	 * jedoch den Kantentyp anhand der Klassenzugehörigkeit zu erkennen versucht.
	 * <p>
	 * Diese Implementierung erkennt den Kantentyp anhand des Ergebnisses von 
	 * {@link GTGraph#getDefaultEdgeType()}. Außerdem wurden die Menüs um Möglichkeiten
	 * erweitert, Knoten umzubenennen und Kantengewichte zu ändern. Darüberhinaus wurden
	 * die Zeichenketten der Menütexte externalisiert.
	 */
	private class TypedEditingPopupGraphMousePlugin extends EditingPopupGraphMousePlugin<GTVertex,GTEdge> {

		/** 
		 * Ruft den Konstruktor der Superklasse identischen Parametern auf. 
		 * @param vertexFactory Factory zur Erzeugung eines Knotens
		 * @param edgeFactory Factory zur Erzeugung einer Kante
		 */
		public TypedEditingPopupGraphMousePlugin(Supplier<GTVertex> vertexFactory, Supplier<GTEdge> edgeFactory) {
			super(vertexFactory, edgeFactory);
		}


		/**
		 * Anzeige eines Popup-Menüs bei Betätigen der rechten Maustaste.
		 * <p>
		 * Beschreibung siehe {@link TypedEditingPopupGraphMousePlugin}
		 * @param e MouseEvent, dessen Erzeugung zum Aufruf der Methode geführt hat.
		 */
		@SuppressWarnings({ "unchecked", "serial" })
		@Override
		protected void handlePopup(MouseEvent e) {
			// Zunächst einige Objekte zusammenstellen, die im Folgenden benötigt werden:
			// - Anzeigekomponente
			VisualizationViewer<GTVertex,GTEdge> vv = (VisualizationViewer<GTVertex,GTEdge>)e.getSource();
			// - Layout für die Darstellung
			Layout<GTVertex,GTEdge> layout = vv.getGraphLayout();
			// - der Graph selbst
			Graph<GTVertex,GTEdge> graph = layout.getGraph();
			// - der Punkt, an dem geklickt wurde
			Point2D p = e.getPoint();

			// PickSupport dient quasi zur Übersetzung von Koordinaten in Kanten und Knoten
			GraphElementAccessor<GTVertex,GTEdge> pickSupport = vv.getPickSupport();
			if(pickSupport != null) {

				// Selektierten Knoten bestimmen (falls es ein Knoten ist). Sonst ist vertex==null.
				GTVertex vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
				// Selektierte Kante bestimmen (falls es eine Kante ist). Sonst ist edge==null.
				GTEdge edge = pickSupport.getEdge(layout, p.getX(), p.getY());
				// Informationen zu den aktuell ausgewählten Knoten besorgen
				PickedState<GTVertex> pickedVertexState = vv.getPickedVertexState();
				// Informationen zu den aktuell ausgewählten Kanten besorgen
				PickedState<GTEdge> pickedEdgeState = vv.getPickedEdgeState();

				// Popup-Menü zusammenbauen
				JPopupMenu popup = new JPopupMenu();

				//
				// Fall 1 : Der Rechtsklick erfolgte auf einen Knoten:
				//   - falls Knoten ausgewählt sind, soll die Kantenerzeugung ermöglicht werden
				//   - der angeklickte Knoten kann gelöscht werden
				//   - der Knoten kann umbenannt werden
				//
				if(vertex != null) {
					// Die aktuelle Knotenauswahl bestimmen
					Set<GTVertex> picked = pickedVertexState.getPicked();
					// Sofern mindestens ein Knoten ausgewählt ist, wird als Option im Menü angeboten,
					// Kanten zwischen dem angeklickten und den ausgewählten Knoten zu erzeugen.
					if(picked.size() > 0) {
						// Menü für die Kantenerzeugung mit dem richtigen Titel erstellen
						JMenu createEdgeMenu = new JMenu((graph.getDefaultEdgeType() == EdgeType.DIRECTED) ?
							Messages.getString("GTEditingModalGraphMouse.NewEdgeDirected") : //$NON-NLS-1$
							Messages.getString("GTEditingModalGraphMouse.NewEdgeUndirected")); //$NON-NLS-1$
						popup.add(createEdgeMenu);
						// Aktionen für die Kantenerzeugung von/zu allen ausgewählten Knoten hinzufügen
						for(final GTVertex other : picked) {
							addCreateEdgeActionS(createEdgeMenu, vertex, other, graph, vv);
						}
					}
					popup.add(new AbstractAction(Messages.getString("GTEditingModalGraphMouse.DeleteNode")) { //$NON-NLS-1$
						@Override
						public void actionPerformed(ActionEvent e) {
							// Knoten aus der Auswahl entfernen
							pickedVertexState.pick(vertex, false);
							// Knoten aus dem Graphen löschen
							graph.removeVertex(vertex);
							vv.repaint();
						}});
					popup.add(new AbstractAction(Messages.getString("GTEditingModalGraphMouse.RenameNode")) { //$NON-NLS-1$
						@Override
						public void actionPerformed(ActionEvent e) {
							GTVertexInputDialog vid = new GTVertexInputDialog(vv, vertex);
							vid.setVisible(true);
							vv.repaint();
						}});
				} 
				//
				// Fall 2 : Der Rechtsklick erfolgte auf eine Kante
				//   - die angeklickte Kante kann gelöscht werden
				//   - das Kantengewicht kann geändert werden
				//
				else if(edge != null) {
					popup.add(new AbstractAction(Messages.getString("GTEditingModalGraphMouse.DeleteEdge")) { //$NON-NLS-1$
						@Override
						public void actionPerformed(ActionEvent e) {
							// Kante aus der Auswahl entfernen
							pickedEdgeState.pick(edge, false);
							// Kante aus dem Graphen löschen
							graph.removeEdge(edge);
							vv.repaint();
						}});
					popup.add(new AbstractAction(Messages.getString("GTEditingModalGraphMouse.ChangeEdgeWeight")) { //$NON-NLS-1$
						@Override
						public void actionPerformed(ActionEvent e) {
							GTEdgeInputDialog eid = new GTEdgeInputDialog(vv, edge);
							eid.setVisible(true);
							vv.repaint();
						}});
				} 
				//
				// Fall 3 : Der Rechtsklick erfolgte in den freien Bereich
				//   - es kann ein neuer Knoten erzeugt werden

				else {
					popup.add(new AbstractAction(Messages.getString("GTEditingModalGraphMouse.NewNode")) { //$NON-NLS-1$
						@Override
						public void actionPerformed(ActionEvent e) {
							// Neuen Knoten erzeugen
							GTVertex newVertex = vertexFactory.get();
							// Knoten dem Graphen hinzufügen
							graph.addVertex(newVertex);
							// Position des neuen Knotens festlegen
							layout.setLocation(newVertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p));
							vv.repaint();
						}
					});
				}
				
				// Sofern im PopupMenü mindestens eine Aktion enthalten ist -> Menü anzeigen
				if(popup.getComponentCount() > 0) {
					popup.show(vv, e.getX(), e.getY());
				}
			}
		}

		/**
		 * Fügt eine oder zwei Aktionen einem Menü hinzu, um eine Kante zu erzeugen.
		 * <p>
		 * Für gerichtete Graphen werden zwei Aktionen hinzugefügt (vom/zum Knoten me),
 		 * wenn die beiden übergebenen Knoten nicht übereinstimmen 
		 * (da ansonsten zwei identische Optionen im Menü erscheinen würden). 
		 * <p>
		 * Für ungerichtete Graphen ist es nicht sinnvoll, zwischen von und nach zu 
		 * unterscheiden. Daher wird in diesen Fällen nur eine Aktion erzeugt.
		 * 
		 * @param menu Menü, das erweitert werden soll
		 * @param me Knoten, der angeklickt wurde
		 * @param other Knoten, von/zu dem eine Kantenerzeugung ermöglich werden soll
		 * @param graph Graph, in dem die Knoten liegen
		 * @param vv Visualisierungskomponente
		 */
		@SuppressWarnings({ "serial", "nls" })
		private void addCreateEdgeActionS(JMenu menu, GTVertex me, GTVertex other, 
				Graph<GTVertex,GTEdge> graph, VisualizationViewer<GTVertex,GTEdge> vv) {
			EdgeType edgeType = graph.getDefaultEdgeType();
			//
			// Schritt 1: Aktion für das Erzeugen einer Kante von other zu me hinzufügen
			//
			menu.add(new AbstractAction("["+other.getName()+","+me.getName()+"]") { 
				@Override
				public void actionPerformed(ActionEvent e) {
					graph.addEdge(edgeFactory.get(), other, me, edgeType);
					vv.repaint();
				}
			});
			//
			// Schritt 2: 
			// Falls es sich um gerichtete Kanten handelt und der erste Eintrag sich
			// nicht auf eine Schleife bezog, soll noch eine weitere Option in
			// Gegenrichtung hinzugefügt werden.
			//
			if ((edgeType == EdgeType.DIRECTED) && !me.equals(other)) {
				// Aktion für das Erzeugen einer Kante von me zu other hinzufügen
				menu.add(new AbstractAction("["+me.getName()+","+other.getName()+"]") {
					@Override
					public void actionPerformed(ActionEvent e) {
						graph.addEdge(edgeFactory.get(), me, other, edgeType);
						vv.repaint();
					}
				});
			}
		}

		/** 
		 * Ausgabemethode für Debuggingzwecke.
		 * @param e MouseEvent, zu dem nähere Informationen ausgegeben werden sollen.
		 */
		@SuppressWarnings({ "unchecked", "nls", "unused", "boxing" })
		private void printMouseInfo(MouseEvent e) {
			System.out.println(e);

			final VisualizationViewer<GTVertex,GTEdge> vv =
					(VisualizationViewer<GTVertex,GTEdge>)e.getSource();
			final Layout<GTVertex,GTEdge> layout = vv.getGraphLayout();
			final Point2D p = e.getPoint();
			GraphElementAccessor<GTVertex,GTEdge> pickSupport = vv.getPickSupport();
			
			System.out.println("Click at "+p);
			if(pickSupport != null) {

				final GTVertex vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
				final GTEdge edge = pickSupport.getEdge(layout, p.getX(), p.getY());
				final PickedState<GTVertex> pickedVertexState = vv.getPickedVertexState();
				final PickedState<GTEdge> pickedEdgeState = vv.getPickedEdgeState();
				System.out.println("Klick auf Knoten: "+ (vertex==null?"---":vertex.getName()));
				System.out.println("Ausgewählte Knoten: "+pickedVertexState.getPicked()); 
				System.out.println("klick auf Kante: "+(edge==null?"---":edge.getWeight()));
				System.out.println("Ausgewählte Kanten: "+pickedEdgeState.getPicked());
			}
		}

	}
}