package graphtool.graph;

import java.util.Random;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * Sammlung statischer Methoden zur Erzeugung von Graphen mit Zufallswerten.
 */
public class GTGraphGenerator {

	/**
	 * Erzeugt einen matrixförmigen Graphen der Größe latticeSize x latticeSize mit
	 * den angegebenen Parametern.
	 * @param edgeType gibt an, ob gerichtet oder ungerichtet.
	 * @param latticeSize Anzahl Zeilen und Spalten der Matrix.
	 * @param isToroidal gibt an, ob in den Zeilen und Spalten zusätzliche Verbindungen 
	 *                   von unten nach oben und von rechts nach links erstellt werden sollen.
	 * @param maxWeight Kantengewichte liegen zwischen 1 und maxWeight
	 * @return den erzeugten Graphen.
	 * @see Lattice2DGenerator
	 */
	public static GTGraph generateLattice2DGraph(EdgeType edgeType, int latticeSize, 
												 boolean isToroidal, int maxWeight) {
		// Generator des Frameworks aufrufen
		Lattice2DGenerator<GTVertex, GTEdge> generator = new Lattice2DGenerator<>(
				GTGraph.getFactory(edgeType),
				GTVertex.getFactory(),
				GTEdge.createRandomWeightFactory(maxWeight),
				latticeSize, isToroidal);
		return (GTGraph)generator.get();
	}
	
	/**
	 * Erzeugt einen kreisförmigen Graphen mit zusätzlichen Querverbindungen zwischen den Knoten.
	 * Das Ergebnis hat, je nach Rahmenbedingungen, ggf. weniger zusätzliche Kanten als
	 * im Parameter maxNumAdditionalEdges angegeben, aber nicht mehr.
	 * @param edgeType gibt an, ob gerichtet oder ungerichtet.
	 * @param numVertices Anzahl der Knoten im Kreis.
	 * @param maxNumAdditionalEdges maximale Anzahl zusätzlicher Kanten. 
	 * @param maxWeight Kantengewichte liegen zwischen 1 und maxWeight
	 * @return den erzeugten Graphen.
	 */
	public static GTGraph generateCircleGraph(EdgeType edgeType, int numVertices, 
											  int maxNumAdditionalEdges, int maxWeight) {
		// Neu erzeugter ungerichteter Graph
		GTGraph graph = new GTGraph(edgeType);
		
		// Falls die übergebenen Parameter eine weitere Ausgestaltung nicht
		// zulassen, wird der leere Graph zurückgegeben.
		if ((numVertices < 1) || (maxNumAdditionalEdges < 0) || (maxWeight < 1)) {
			return graph;
		}
		
		// Im Weiteren werden eine Knoten- und eine Kantenfabrik, ein Zwischenspeicher
		// für die Knoten sowie eine Zufallszahlenquelle benötigt:
		Supplier<GTVertex> vertexFactory = GTVertex.getFactory();
		Supplier<GTEdge> edgeFactory = GTEdge.createRandomWeightFactory(maxWeight);
		GTVertex[] vertices = new GTVertex[numVertices];
		Random random = new Random();

		// Graphen mit der angegebenen Anzahl von Knoten erzeugen
		// und diese zusätzlich im Array ablegen
		for (int i = 0; i < numVertices; i++) {
			vertices[i] = vertexFactory.get();
			graph.addVertex(vertices[i]);
		}
		
		// Die benachbarten Knoten zunächst alle paarweise miteinander verbinden
		for (int i = 1; i < numVertices; i++) {
			graph.addEdge(edgeFactory.get(), vertices[i-1], vertices[i]);
		}
		
		// Kante vom letzten zum ersten Knoten erzeugen. 
		// Es entsteht damit ein Ring.
		graph.addEdge(edgeFactory.get(), vertices[numVertices-1], vertices[0]);
		
		// Weitere zufällige Kanten als Querverbindungen erzeugen. Dabei werden
		// maxNumAdditionalEdges Verbindungsversuche unternommen.
		// Versuche, bereits verknüpfte Knoten erneut zu verbinden, werden 
		// von addEdge() ignoriert.
		
		for (int n = 0; n < maxNumAdditionalEdges; n++) {
			// Zwei zufällig Positionen erzeugen und aufsteigend sortieren
			int v1 = random.nextInt(numVertices);
			int v2 = random.nextInt(numVertices);
			graph.addEdge(edgeFactory.get(), vertices[v1], vertices[v2]);
		}

		return graph;
	}
}
