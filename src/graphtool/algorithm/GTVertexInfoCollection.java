package graphtool.algorithm;

import java.util.Collection;

import graphtool.graph.GTVertex;

/**
 * Collection aus GTVertexInfo-Elementen. Zusätzlich zu den üblichen
 * Methoden der Collection, die sich auf Objekte vom Typ GTVertexInfo
 * beziehen, werden Methoden eingeführt, die sich auf Objekte vom
 * Typ GTVertex beziehen. 
 */
public interface GTVertexInfoCollection extends Collection<GTVertexInfo> {
	/** 
	 * Liefert ein GTVertexInfo-Element vi aus der Collection, für das gilt:
	 * vi.getVertex() == vertex. Ist kein solches Element vorhanden, liefert
	 * die Methode null. 
	 * <p>
	 * Sind mehrere Elemente in der Collection vorhanden,
	 * für die dies zutrifft, macht findElementFor(.) keine Zusicherung darüber,
	 * welches der passenden Elemente zurückgegeben wird.
	 * 
	 * @param vertex Knoten, zu dem ein Element aus der Collection gesucht werden soll.
	 * @return passendes GTVertexInfo-Element oder null.
	 */
	public GTVertexInfo findElementFor(GTVertex vertex);
	/** 
	 * Gibt an, ob in der Collection (mindestens) ein GTVertexInfo-Element vi 
	 * enthalten ist, für das gilt: vi.getVertex() == vertex. 
	 * 
	 * @param vertex Knoten, zu dem ein Element aus der Collection gesucht werden soll.
	 * @return true, wenn ein passendes GTVertexInfo-Element gefunden wurde.
	 */
	public boolean contains(GTVertex vertex);
}
