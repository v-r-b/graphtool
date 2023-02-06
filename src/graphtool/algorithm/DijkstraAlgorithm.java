package graphtool.algorithm;

import java.awt.Color;
import java.util.concurrent.CompletionException;

import graphtool.graph.GTEdge;
import graphtool.graph.GTVertex;
import graphtool.gui.GTGraphVisualization;
import graphtool.utils.CodeExecutionHandler;
import graphtool.utils.CollectionObserver;
import graphtool.utils.Configuration;
import graphtool.utils.TextComponentPrintStream;

/**
 * Implementierung des Dijkstra-Algorithmus, die auf einem gegebenen Graphen arbeitet.
 * <p>
 * Der eigentliche Algorithmus ist in der {@link #run()}-Methode implementiert, die
 * in einem eigenen Thread gestartet und deren Ablauf mittels 
 * {@link CodeExecutionHandler} kontrolliert wird.
 * <p>
 * Die Darstellung der Knoten und Kanten im Graphen ist wie folgt: 
 * <ul>
 * <li>Standarddarstellung: schmale schwarze Linie, bei Knoten weiß gefüllt
 * <li>Startknoten: rote Umrandung
 * <li>aktueller Knoten (u): breite Umrandung
 * <li>aktuelle Kante (von u ausgehend): breite Linie
 * <li>alle Knoten in P (und nicht in S): hellgrau gefüllt
 * <li>alle Knoten in S: dunkelgrau gefüllt </ul>
 */
public class DijkstraAlgorithm extends GTAbstractAlgorithm { 
	/**
	 * Ruft den Konstruktor der Superklasse mit den angegebenen Parametern auf
	 * und bereitet die zu visualisierende run()-Methode aus dem Sourcecode der Klasse auf.
	 * @param dgv Visualisierungskomponente, die auch eine Referenz auf den
	 *            Graphen liefert.
	 * @param co  CollectionObserver, der die vom Algorithmus verwendeten Knotensammlungen
	 *            vom Typ der inneren Klassen GTVertexInfoSet und GTPriorityQueue
	 *            auf Änderungen überwacht und dann jeweils darstellt. 
	 *            Darf null sein, wenn nicht benötigt.
	 * @param out Ausgabestream für Meldungen.  Darf null sein, wenn nicht benötigt.
	 * @see GTAbstractAlgorithm#GTAbstractAlgorithm(String, String, GTGraphVisualization, CollectionObserver, TextComponentPrintStream)
	 * @see CodeExecutionHandler#prepareSourceCode(String, String)
	 */	
	@SuppressWarnings("nls")
	public DijkstraAlgorithm(
			GTGraphVisualization dgv, CollectionObserver<GTVertexInfo> co, TextComponentPrintStream out) {
		// Dem Konstruktor der Superklasse zusätzlich Informationen zur Quelldatei mitgeben.
		super("UTF-8", Configuration.getDefaultInstance().get("SourcePath", false), dgv, co, out);
		// Quelltext des Algorithmus laden und aufbereiten. Die Methode
		// muss von hier aus aufgerufen werden und nicht aus der Superklasse,
		// da aus dem Ort des Aufrufs die Datei für den Quelltext bestimmt wird.
		cxh.prepareSourceCode(CX_HANDLER_NAME, CX_BEAUTYFY_REGEX);
	}
	
	/**
	 * Der Dijkstra-Algorithmus sucht kürzeste Wege von genau <b>einem definierten
	 * Startknoten</b> aus.
	 * @return true
	 */
	@Override
	public boolean needsStartNode() {
		return true;
	}
	
	/**
	 * Der Dijkstra-Algorithmus sucht kürzeste Wege von einem definierten
	 * Startknoten aus zu <b>allen übrigen</b> erreichbaren Knoten, kennt
	 * also <b>keinen definierten Zielknoten</b>.
	 * @return false
	 */
	@Override
	public boolean needsTargetNode() {
		return false;
	}
	
	/**
	 * Startet den eigentlichen Algorithmus. Darstellung: siehe {@link DijkstraAlgorithm}.
	 * <p> 
	 * Der Startknoten muss vor dem Aufruf definiert sein. 
	 * <p>
	 * run() wird nicht direkt aufgerufen, sondern über [Thread.]start(). 
	 * <p>
	 * Der Algorithmus ist in Pseudocode wie folgt beschrieben. Die einzelnen Pseudocode-
	 * Zeilen finden sich dann auch als Kommentare in der Implementierung er Methode.
	 * <pre>
	 * für alle u aus graph.getVertices()
	 *     markiere u als "nicht besucht"
	 * S:= leere Menge
	 * P:= leere Vorrangwarteschlange
	 * P.add((s, 0, NIL))
	 * solange nicht P.isEmpty()
	 *     (u, d, pi):= P.extractMin()
	 *     wenn u ist markiert als "nicht besucht"
	 *         markiere u als "besucht"
	 *         S:= S U {(u, d, pi)}
	 *         für alle Nachbarn n des Knotens u, die markiert sind als "nicht besucht"
	 *             P.add((n, d + w(u, n), u))
	 * </pre>
	 */
	@SuppressWarnings("nls") // String-Literale für S und P
	@Override
	public void run() {
		// Menge der besuchten Knoten
		GTVertexInfoSet S = null;
		// Vorrangwarteschlange
		GTPriorityQueue P = null;
		// Knotentripel-Variable
		GTVertexInfo u_d_pi = null;
		// Knotenvariablen
		GTVertex n = null, u = null;
		// Startknoten
		GTVertex start = dgv.getStartVertex();
	
		// Darstellung des Graphen initialisieren
		dgv.clearEmphases();
		try {
			cxh.startCodeHandling(true);
			// @pcode BEGINN
			// @pcode S:= Leere Menge
			cxh.bp(); S = new GTVertexInfoSet("S", collectionObserver);
			// Darstellung der besuchten Knoten im Graphen sicherstellen
			dgv.registerVertexInfoCollection(S, 1, Color.DARK_GRAY);
			// @pcode P:= leere Vorrangwarteschlange
			cxh.bp(); P = new GTPriorityQueue("P", collectionObserver);
			// Darstellung der Knoten in der Vorrangwarteschlange im Graphen sicherstellen
			dgv.registerVertexInfoCollection(P, 2, Color.LIGHT_GRAY);
			// @pcode P.add(start, 0, NIL)
			cxh.bp(); P.add(start, 0, null);
			// @pcode solange nicht P.isEmpty() {
			while(!P.isEmpty()) {
				// @pcode (u, d, pi):= P.extractMin()
				cxh.bp(); u_d_pi = P.extractMin();
				// (hilfsweise u einführen)
				u = u_d_pi.getVertex();
				dgv.setCurrentVertex(u);
				// @pcode wenn u ist bislang "nicht besucht" = "S kennt nicht u" {
				cxh.bp(); if (!S.contains(u)) { 
					// @pcode S:= S ∪ {(u, d, pi)}
					cxh.bp(); S.add(u_d_pi);
					// @pcode für alle Nachbarn n des Knotens u, für die gilt: 
					cxh.bp(); for (GTEdge e : graph.getOutEdges(u)) {
						// Kante im Graphen hervorheben
						dgv.setCurrentEdge(e);
						// Knoten auf der anderen Seite der Kante bestimmen
						n = graph.getOpposite(u, e);
						// @pcode n ist bislang "nicht besucht" = "S kennt nicht n" {
						cxh.bp(); if(!S.contains(n)) {
							// @pcode P.add((n, d + w(u, n), u))
							cxh.bp(); P.add(n, u_d_pi.getDistance() + e.getWeight(), u_d_pi);
						}
					// @pcode }
					cxh.bp(); }
					dgv.setCurrentEdge(null);
				// @pcode }
				}
			// @pcode }
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
			cxh.stopExecution();
		}
	}

}