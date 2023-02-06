package graphtool.graph;

import edu.uci.ics.jung.graph.ObservableGraph;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.GraphMetadata.EdgeDefault;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;

/**
 * GTGraph erweitert ObservableGraph und damitGraphDecorator und speichert den eigentlichen Graphen 
 * intern (Dekorierer-Muster), je nach Konstruktoraufruf, als gerichteten oder ungerichteten Graphen.
 * Es ist zu beachten, dass GTGraph nur Kanten mit nicht-negativen Gewichten speichert. 
 * <p>
 * Die Klasse stellt eine Reihe von Konstruktoren zur Verfügung, u.a. auch zum Einlesen
 * von GML-Daten.
 * @see GTVertex
 * @see GTEdge
 */
public class GTGraph extends ObservableGraph<GTVertex, GTEdge> {
	
	private static final long serialVersionUID = 7054507951196662967L;

	/** Speichert die Erzeuger für die verschiedenen Kantentypen */
	private static HashMap<EdgeType, Supplier<GTGraph>> graphFactories = null;
	
	/**
	 * Liefert einen Erzeuger für einen Graphen mit dem angegebenen Kantentyp.
	 * Dieser wird beim ersten Zugriff erstellt und danach wiederverwendet.
	 * @param edgeType Kantentyp des Graphen 
	 * @see EdgeType
	 * @return Leerer Graph mit dem angegebenen Kantentyp.
	 */
	public static Supplier<GTGraph> getFactory(EdgeType edgeType) {
		// Beim ersten Aufruf HashMap erzeugen
		if (graphFactories == null) {
			graphFactories = new HashMap<>();
		}
		// Prüfen, ob für den gewünschten Kantentyp ein Erzeuger vorhanden ist
		if (!graphFactories.containsKey(edgeType)) {
			// wenn nein, erzeugen.
			graphFactories.put(edgeType, () -> new GTGraph(edgeType));  
		}
		// Erzeuger für den gewünschen Kantentyp zurückgeben.
		return graphFactories.get(edgeType);
	}


	/**
	 * Erzeugt eine neue Instanz mit einem gerichteten oder ungerichteten Graphen.
	 * @param edgeType Gewünschter Kantentyp des Graphen.
	 */
	public GTGraph(EdgeType edgeType) { 
		super(edgeType == EdgeType.DIRECTED ? 
				new DirectedSparseGraph<GTVertex, GTEdge>() :
				new UndirectedSparseGraph<GTVertex, GTEdge>());
	}
	
	/**
	 * Erzeugt eine neue Instanz aus einer GraphML-Datei.
	 * @param pathname Pfadname zur Quelldatei.
	 * @throws IOException, GraphIOException
	 */
	public GTGraph(String pathname) throws IOException, GraphIOException { 
		super(null);
		readFromFile(pathname);
	}
	
	/**
	 * Erzeugt eine neue Instanz aus einem InputStream mit GraphML-Daten.
	 * @param gmlStream InputStream mit gültigen GML-Daten.
	 * @throws IOException, GraphIOException
	 */
	public GTGraph(InputStream gmlStream) throws IOException, GraphIOException { 
		super(null);
		readFromStream(gmlStream);
	}
	
	/**
	 * Ersetzt den Inhalt des Graphen durch den des übergebenen Arguments.
	 * Intern erfolgt dazu lediglich eine Zuweisung des delegate-Objekts.
	 * @param newGraph Graph, dessen Inhalte die aktuellen Inhalte ersetzen sollen.
	 */
	public void replaceWith(GTGraph newGraph) {
		// Da es sich bei GTGraph um einen Dekorierer handelt, der über die
		// Inhalte seiner Superklasse hinaus keine Feldinformationen enthält, 
		// sondern lediglich zusätzliche Funktionalität zur Verfügung stellt,
		// reicht es, das delegate-Objekt neu zuzuweisen.
		delegate = newGraph.delegate;
	}

	/**
	 * Die Inhalte des Graphen durch die Inhalte aus einer GraphML-Datei erzeugen.
	 * @param pathname Pfadname zur Quelldatei.
	 * @throws IOException, GraphIOException
	 */
	public void readFromFile(String pathname) throws IOException, GraphIOException {
		// Reader-Objekt auf Dateisystemebene
		try (FileReader r = new FileReader(pathname)) {
			// Reader-Objekt für GraphML-Dateiinhalte
			GraphMLReader2<GTGraph,GTVertex, GTEdge> gmlReader = null;

			// GML-Reader-Objekt erzeugen. Anzugeben ist hierbei der Reader auf Ebene
			// des Dateisystems sowie eine Factory-Funktion für die einzelnen Elementtypen.
			gmlReader = new GraphMLReader2<>(r, 
					GTGraph.createGMLFactory(), 
					GTVertex.createGMLFactory(), 
					GTEdge.createGMLFactory(), 
					(HyperEdgeMetadata heMeta) -> new GTEdge()
					);
			
			// Datei auslesen und Graphen erzeugen.
			// Internen Graphen durch den neu erzeugten ersetzen.
			replaceWith(gmlReader.readGraph());
		}
	}
	

	/**
	 * Die Inhalte des Graphen durch die Inhalte aus einem InputStream erzeugen.
	 * @param gmlStream InputStream mit gültigen GML-Daten.
	 * @throws IOException, GraphIOException
	 */
	public void readFromStream(InputStream gmlStream) throws GraphIOException {
		// Reader-Objekt für GraphML-Dateiinhalte
		GraphMLReader2<GTGraph,GTVertex, GTEdge> gmlReader = null;
		
		// GML-Reader-Objekt erzeugen. Anzugeben ist hierbei der InputStream
		// sowie eine Factory-Funktion für die einzelnen Elementtypen.
		gmlReader = new GraphMLReader2<>(gmlStream, 
				GTGraph.createGMLFactory(), 
				GTVertex.createGMLFactory(), 
				GTEdge.createGMLFactory(), 
				(HyperEdgeMetadata heMeta) -> new GTEdge()
				);
		
		// Datei auslesen und Graphen erzeugen.
		// Internen Graphen durch den neu erzeugten ersetzen.
		replaceWith(gmlReader.readGraph());
	}
	
	/**
	 * Die aufgerufene Instanz von GTGraph in eine Datei schreiben.
	 * @param pathname Pfadname zur Zieldatei.
	 * @throws IOException
	 */
	public void writeToFile(String pathname) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(pathname)) {
			// GraphMLWriter kann nur mit UTF-8 umgehen.
			// Daher wird w nicht als FileWriter deklariert (der das Standard-Encoding
			// der Plattform nutzt; in Windows also ISO-8859-15), sondern als
			// OutputStreamWriter für UTF-8 auf einem FileOutputStream.
			OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8"); //$NON-NLS-1$
			GraphMLWriter<GTVertex, GTEdge> gmlWriter = new GraphMLWriter<>();

			// Die Eigenschaften der Knoten und Kanten mit speichern.
			GTVertex.addDataTransformersToGMLWriter(gmlWriter);
			GTEdge.addDataTransformersToGMLWriter(gmlWriter);

			// Den "eigentlichen Graphen" (das delegate-Objekt) speichern.
			gmlWriter.save(this.delegate, w);
		}
	}

	/** 
	 * Factory für einen Graphen zum Gebrauch durch einen GML-Reader. Je nach Wert von
	 * EdgeDefault in der GML-Datei wird ein gerichteter oder ungerichteter Graph erzeugt.
	 * @return Factory-Funktion. 
	 */
	protected static Function<GraphMetadata,GTGraph> createGMLFactory() { 
		return (GraphMetadata gMeta) -> 
				new GTGraph(gMeta.getEdgeDefault() == EdgeDefault.DIRECTED ?
							EdgeType.DIRECTED : EdgeType.UNDIRECTED);
	}
	
}
