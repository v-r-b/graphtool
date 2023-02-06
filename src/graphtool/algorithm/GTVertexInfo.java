package graphtool.algorithm;

import graphtool.graph.GTVertex;

/**
 * GTVertexInfo speichert Zusatzinformationen zu Knoten des Typs GTVertex.
 * Dies sind der Knoten selbst, bis zu zwei Abstandswerte und ein Vorgänger.
 */
public class GTVertexInfo implements Comparable<GTVertexInfo> {
	/** Knoten, zu dem Zusatzinformationen abgelegt werden sollen. */
	private GTVertex vertex;
	/** Abstandswert, der für den Vergleich relevant ist; siehe {@link #compareTo(GTVertexInfo)}. */
	private int distance = 0;
	/** Zusätzlicher Abstandswert, z.B. zur Verwendung mit dem A*-Algorithmus */
	private int partDistance = -1;
	/** Vorgängerknoten */
	private GTVertexInfo predecessor = null;
		
	/**
	 * Erzeugt einen Info-Eintrag zur Verwendung mit einem Entfernungswert. 
	 * @param vertex Knoten, zu dem Informationen gespeichert werden.
	 * @param distance Für Vergleiche relevanter Abstandswert des Knotens.
	 * @param predecessor Vorgängerknoten des Knotens.
	 */
	public GTVertexInfo(GTVertex vertex, int distance, GTVertexInfo predecessor) {
		this.vertex = vertex;
		this.distance = distance;
		this.predecessor = predecessor;
	}
	
	/**
	 * Erzeugt einen Info-Eintrag zur Verwendung mit zwei Entfernungswerten. 
	 * @param vertex Knoten, zu dem Informationen gespeichert werden.
	 * @param partDistance Zusätzlicher Abstandswert für diesen Knoten.
	 * @param distance Für Vergleiche relevanter Abstandswert des Knotens.
	 * @param predecessor Vorgängerknoten des Knotens.
	 */
	public GTVertexInfo(GTVertex vertex, int partDistance, int distance, GTVertexInfo predecessor) {
		this.vertex = vertex;
		this.distance = distance;
		this.partDistance = partDistance;
		this.predecessor = predecessor;
	}
	
	/**
	 * Gibt das Attribut "vertex" zurück.
	 * @return Knoten, zu dem Informationen gespeichert sind.
	 */
	public GTVertex getVertex() {
		return vertex;
	}
	
	/**
	 * Gibt das Attribut "distance" zurück.
	 * @return Zu "vertex" gespeicherter Abstand vom Startknoten.
	 */
	public int getDistance() {
		return distance;
	}
	
	/**
	 * Gibt das Attribut "fullDistance" zurück.
	 * @return Zu "vertex" gespeicherter Abstand zwischen Start- und Zielknoten über "vertex".
	 */
	public int getPartDistance() {
		return partDistance;
	}
	
	/**
	 * Gibt das Attribut "predecessor" zurück.
	 * @return Zu "vertex" gespeicherter Vorgänger.
	 */
	public GTVertexInfo getPredecessor() {
		return predecessor;
	}

	/**
	 * Informationen als Tripel (wenn partDistance &lt; 0) oder Quadrupel 
	 * zurückgeben. Dabei sind die jeweiligen Knotennamen (bei null wird "NIL" verwendet) 
	 * und die Distanzen relevant. Beispiel: "(Hamburg, 0, NIL)"
	 * @return Zeichenkette wie beschrieben.
	 */ 
	@SuppressWarnings("nls") // String-Literale beim Zusammenbau des Ergebnisses
	@Override
	public String toString() {
		return	"(" +
				(vertex == null ? "NIL" : vertex.getName()) + ", " +
				(partDistance < 0 ? "" : partDistance + ", ") +
				distance + ", " + 
				(	predecessor == null ? "NIL" : "pi: " + 
					(predecessor.vertex == null ? "NIL" : predecessor.vertex.getName())
				)+ ")";
	}

	/**
	 * Vergleicht zwei Objekte der Klasse GVertexInfo anhand ihres Distanzwerts.
 	 * Liefert <ul>
	 * <li>einen Wert &lt; 0, wenn this.distance &lt; e.distance, 
	 * <li>Null, wenn this.distance == e.distance und
	 * <li>einen Wert &gt; 0, wenn this.distance &gt; e.distance.
	 * </ul>
	 * Tatsächlich gibt die Methode <code>this.distance - e.distance</code> zurück.
	 * @param e zu vergleichendes Objekt.
	 * @return Wert wie beschrieben. 
	 * @see #equals(Object)
	 * @throws NullPointerException, wenn e null ist.
	 * @implNote Achtung: Diese Klasse implementiert eine natürliche Ordnung,
	 * 			 die inkonsistent mit equals ist. Während für zwei Objekt a und b
	 * 			 a.compareTo(b) Null liefert, wenn a.distance == b.distance
	 * 			 (unabhängig von weiteren Bedingungen),
	 * 			 liefert a.equals(b) genau dann true, wenn a und b dasselbe Objekt
	 * 			 sind, also a == b ist.
	 */
	@Override
	public int compareTo(GTVertexInfo e) {
		// Vergleicht zwei Abstände zum Startknoten
		return distance - e.distance;
	}
}
