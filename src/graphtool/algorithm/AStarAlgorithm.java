package graphtool.algorithm;

import java.awt.Color;
import java.util.concurrent.CompletionException;
import java.util.function.ToIntBiFunction;

import graphtool.graph.GTEdge;
import graphtool.graph.GTVertex;
import graphtool.gui.GTGraphVisualization;
import graphtool.utils.CodeExecutionHandler;
import graphtool.utils.CollectionObserver;
import graphtool.utils.Configuration;
import graphtool.utils.TextComponentPrintStream;

/**
 * Implementierung des A*-Algorithmus, die auf einem gegebenen Graphen arbeitet.
 * <p>
 * Der eigentliche Algorithmus ist in der {@link #run()}-Methode implementiert, die
 * in einem eigenen Thread gestartet und deren Ablauf mittels 
 * {@link CodeExecutionHandler} kontrolliert wird.
 * <p>
 * Die Darstellung der Knoten und Kanten im Graphen ist wie folgt: 
 * <ul>
 * <li>Standarddarstellung: schmale schwarze Linie, bei Knoten weiß gefüllt
 * <li>Startknoten: rote Umrandung
 * <li>Zielknoten: grüne Umrandung
 * <li>aktueller Knoten (u): breite Umrandung
 * <li>aktuelle Kante (von u ausgehend): breite Linie
 * <li>alle Knoten in OpenList (und nicht in ClosedList): hellgrau gefüllt
 * <li>alle Knoten in ClosedList: dunkelgrau gefüllt
 * </ul>
 */
public class AStarAlgorithm extends GTAbstractAlgorithm {

	/**
 	 * Die Standardheuristik liefert konstant Null. Damit arbeitet der A*-
	 * Algoritmus wie der Dijkstra-Algorithmus.
	 * @see #heuristic
	 */
	private final static ToIntBiFunction<GTVertex, GTVertex> STD_HEURISTIC = 
			((GTVertex vtx, GTVertex tgt) -> 0);
	
	/**
	 * Schlüssel für die Hinterlegung einer Heuristikfunktion in der Konfigurationsdatei.
	 * Der hinterlegte Wert muss ein voll qualifizierter Klassenname sein. Die Klasse
	 * muss vom Typ ToIntBiFunction&lt;GTVertex, GTVertex&gt; sein. Beispieleintrag:
	 * <p>
	 * <code>AStarHeuristic=graphtool.algorithm.BeeLineHeuristicLHS</code>
	 */
	public final static String HEURISTIC_CONFIGKEY = "AStarHeuristic"; //$NON-NLS-1$
	
	/**
	 * Vom Algorithmus verwendete Heuristik. 
	 * <p>
	 * A* verwendet eine Heuristik, die die Entfernung von einem Knoten vtx
	 * (Parameter 1) zum Zielknoten tgt (Parameter 2) abschätzt. Der Schätzwert
	 * darf den tatsächlichen Entfernungswert nicht übersteigen.
	 * Die Standardheuristik liefert konstant Null. Damit arbeitet der A*-
	 * Algoritmus wie der Dijkstra-Algorithmus.
	 * <p>
	 * Durch Aufruf von {@link #useHeuristic(ToIntBiFunction)} kann eine
	 * andere Heuristik gewählt werden.
	 */
	private ToIntBiFunction<GTVertex, GTVertex> heuristic = STD_HEURISTIC;

	/**
	 * Ruft den Konstruktor der Superklasse mit den angegebenen Parametern auf
	 * und bereitet die zu visualisierende run()-Methode aus dem Sourcecode der Klasse auf.
	 * Ist in der Konfigurationsdatei ein Eintrag bei {@value #HEURISTIC_CONFIGKEY} 
	 * vorhanden, wird diese Klasse als Heuristik verwendet. 
	 * Ansonsten wird die Standardheuristik verwendet, die konstant 0 liefert.
	 * @param dgv Visualisierungskomponente, die auch eine Referenz auf den
	 *            Graphen liefert.
	 * @param co  CollectionObserver, der die vom Algorithmus verwendeten Knotensammlungen
	 *            vom Typ der inneren Klassen GTVertexInfoSet und GTPriorityQueue
	 *            auf Änderungen überwacht und dann jeweils darstellt. 
	 *            Darf null sein, wenn nicht benötigt.
	 * @param out Ausgabestream für Meldungen.  Darf null sein, wenn nicht benötigt.
	 * @see GTAbstractAlgorithm#GTAbstractAlgorithm(String, String, GTGraphVisualization, CollectionObserver, TextComponentPrintStream)
	 * @see CodeExecutionHandler#prepareSourceCode(String, String)
	 * @see #HEURISTIC_CONFIGKEY
	 * @see #heuristic
	 */
	@SuppressWarnings({ "unchecked", "nls" }) // Cast der Heuristik-Funktion bei useHeuristic(.)
	public AStarAlgorithm(GTGraphVisualization dgv, CollectionObserver<GTVertexInfo> co, TextComponentPrintStream out) {
		// Dem Konstruktor der Superklasse zusätzlich Informationen zur Quelldatei mitgeben.
		super("UTF-8", Configuration.getDefaultInstance().get("SourcePath", false), dgv, co, out);
		
		// Falls in der Konfigurationsdatei eine Heuristik-Klasse hinterlegt
		// wurde, diese laden, instanziieren und zur Verwendung vormerken.
		String heuristicName = Configuration.getDefaultInstance().get(HEURISTIC_CONFIGKEY, false);
		if(heuristicName != null) {
			try {
				Object h = getClass().getClassLoader().loadClass(heuristicName).newInstance();
				useHeuristic((ToIntBiFunction<GTVertex, GTVertex>)h);
			} catch (Exception e) {
				// Eintrag war fehlerhaft
				System.err.println("Could not load heuristic " + heuristicName); //$NON-NLS-1$
				e.printStackTrace();
			}
		}
		// Quelltext des Algorithmus laden und aufbereiten. Die Methode
		// muss von hier aus aufgerufen werden und nicht aus der Superklasse,
		// da aus dem Ort des Aufrufs die Datei für den Quelltext bestimmt wird.
		cxh.prepareSourceCode(CX_HANDLER_NAME, CX_BEAUTYFY_REGEX);
	}

	/**
	 * Setzt die vom Algorithmus zu verwendende Heuristik. 
	 * A* verwendet eine Heuristik, die die Entfernung vom aktuellen Knoten v
	 * (Parameter 1) zum Zielknoten z (Parameter 2) abschätzt. Der Schätzwert
	 * darf den tatsächlichen Entfernungswert nicht übersteigen.
	 * @param heuristic zu verwendende Heuristik h(v, z).
	 */
	public void useHeuristic(ToIntBiFunction<GTVertex, GTVertex> heuristic) {
		this.heuristic = heuristic;
	}
	
	/**
	 * Der A*-Algorithmus sucht den kürzesten Weg von genau <b>einem definierten
	 * Startknoten</b> zu genau einem definierten Zielknoten.
	 * @return true
	 */
	@Override
	public boolean needsStartNode() {
		return true;
	}
	
	/**
	 * Der A*-Algorithmus sucht den kürzesten Weg von genau einem definierten
	 * Startknoten zu genau <b>einem definierten Zielknoten</b>.
	 * @return true
	 */
	@Override
	public boolean needsTargetNode() {
		return true;
	}
	
	/**
	 * Heuristik. Gibt die geschätzte Entfernung von u zum Zielknoten an.
	 * Um diese zu bestimmen, wird die Funktion verwendet, die in {@link #heuristic}
	 * abgelegt ist.
	 * @param u aktueller Knoten
	 * @return geschätzte Entfernung zum Zielknoten 
	 */
	public int h(GTVertex u) {
		return heuristic.applyAsInt(u, dgv.getTargetVertex());
	}
	
	/**
	 * Startet den eigentlichen Algorithmus. Darstellung: siehe {@link AStarAlgorithm}.
	 * <p> 
	 * Start- und Zielknoten müssen vor dem Aufruf definiert sein. 
	 * <p>
	 * run() wird nicht direkt aufgerufen, sondern über [Thread.]start(). 
	 * <p>
	 * Der Algorithmus ist in Pseudocode wie folgt beschrieben. Die einzelnen Pseudocode-
	 * Zeilen finden sich dann auch als Kommentare in der Implementierung er Methode.
	 * <pre>
	 * OpenList = Leere Vorrangwarteschlange 
	 * ClosedList = ∅
	 * OpenList.add(s)
	 * solange nicht OpenList.isEmpty()
	 *     u:= OpenList.extractMin()
	 *     wenn u == z,dann BEENDE_SUCHE (Weg gefunden!)
	 *     für alle Nachbarn n des Knotens u 
	 *         wenn n ∉ ClosedList
	 *             g' = u.g + w(u,n)
	 *             wenn nicht (OpenList.contains(n)  und  (g' ≥ n.g))
	 *                 n.pi = u
	 *                 n.g = g'
	 *                 n.f = g' + h(n)
	 *                 wenn OpenList.contatins(n)
	 *                     OpenList.updateKey(n)
	 *                 sonst
	 *                     OpenList.add(n)
	 *     ClosedList:= ClosedList U {u}
	 * BEENDE_SUCHE (kein Weg gefunden!)
	 * </pre>
	 */
	@SuppressWarnings("nls")	// String-Literale für OpenList und ClosedList
	@Override
	public void run() {
		// Startknoten
		GTVertex start = dgv.getStartVertex();
		// Zieknoten
		GTVertex target = dgv.getTargetVertex();
		// Vorrangwarteschlange
		GTPriorityQueue openList = null;
		// Menge mit besuchten Knoten
		GTVertexInfoSet closedList = null;
		// Knoteninfo-Variablen
		GTVertexInfo uInfo = null, nInfo = null;
		// Knotenvariablen
		GTVertex n = null, u = null;
		// Abstadsvariable
		int gNeu = 0;
		
		try {
			cxh.startCodeHandling(true);
			// @pcode BEGINN
			// @pcode OpenList = Leere Vorrangwarteschlange
			cxh.bp(); openList = new GTPriorityQueue("OpenList", collectionObserver);
			dgv.registerVertexInfoCollection(openList, 2, Color.LIGHT_GRAY);
			// @pcode ClosedList = Leere Menge 
			cxh.bp(); closedList = new GTVertexInfoSet("ClosedList", collectionObserver); 
			dgv.registerVertexInfoCollection(closedList, 1, Color.DARK_GRAY);
			// @pcode OpenList.add(start)
			cxh.bp(); openList.add(start, 0, 0, null);
			// @pcode solange nicht OpenList.isEmpty()
			cxh.bp(); while (!openList.isEmpty()) {
				// @pcode u = OpenList.extractMin()
				cxh.bp(); uInfo = openList.extractMin();
				// hilfsweise u einführen
				u = uInfo.getVertex();
				dgv.setCurrentVertex(u);

				// @pcode closedList.add(u)
				cxh.bp(); closedList.add(uInfo);
				
				// @pcode wenn u == z, dann BEENDE_SUCHE(Weg gefunden)
				cxh.bp(); if (u.equals(target)) {
					cxh.bp(); dgv.recursiveShowPathTo(uInfo);
					break;
				}
				
				// @pcode für alle Nachbarn n des Knotens u
				cxh.bp(); for (GTEdge e : graph.getOutEdges(u)) {
					n = graph.getOpposite(u, e);
					// Kante im Graphen hervorheben
					dgv.setCurrentEdge(e);
					// @pcode wenn nicht ClosedList.contains(n)
					cxh.bp(); if (!closedList.contains(n)) {
						// @pcode g' = u.g + w(u, n)
						cxh.bp(); gNeu = uInfo.getPartDistance() + e.getWeight();
						// @pcode wenn OpenList.contains(n) und (g' >= n.g), dann tue nichts
						cxh.bp(); if (openList.contains(n)) {
							nInfo = openList.findElementFor(n);
							if (gNeu >= nInfo.getPartDistance()) {
								cxh.bp(); continue;
							}
						}
						// Eintrag für die OpenList anlegen oder aktualisieren
						nInfo = new GTVertexInfo(n, gNeu, gNeu + h(n), uInfo);
						// @pcode Wenn OpenList.contains(n)
						cxh.bp(); if (openList.contains(n)) {
							// @pcode OpenList.updateKey(n)
							cxh.bp(); openList.updateKey(nInfo);
						}
						else {
							// @pcode OpenList.add(n)
							cxh.bp(); openList.add(nInfo);
						}
					}
				}
				dgv.setCurrentEdge(null);
			}
			dgv.setCurrentVertex(null);
			// @pcode ENDE
			cxh.endCodeHandling(true);
		} 
		// Soll der Algorithmus gestoppt werden, so wird dies erreicht, indem für das
		// Werfen einer CompletionException gesorgt wird. Die CompletionException wurde
		// gewählt, weil sie als Subklasse von RuntimeException eine Unchecked Exception
		// ist und in der Methode, die sie wirft, nicht deklariert werden muss.
		catch (CompletionException e) {
			showInfo(ALG_ABORTED_MSG, true);
			// Aufräumarbeiten durchführen
			dgv.clearEmphases();
			openList = null;
			cxh.stopExecution();
		}
	}	
}