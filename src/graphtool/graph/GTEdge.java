package graphtool.graph;

import java.util.Random;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;

/**
 * Eine Kante besitzt ein nicht-negatives, ganzzahliges Gewicht.
 * <p>
 * Die Klasse stellt u.a. eine Reihe von statischen Factory-Methoden sowie eine Methode
 * zur Serialisierung durch einen GraphMLWriter zur Verfügung.
 */
public class GTEdge {
	
	/** Schlüssel der Gewichtseigenschaft in der GML-Datei */
	private static final String WEIGHT_KEY = "weight"; //$NON-NLS-1$

	/** Erzeuger für Kanten mit Standardgewicht */
	private static Supplier<GTEdge> edgeFactory = null;
	/** Zufallsgenerator für Kantengewichte */
	private static Random randomSource = null;

	/** Standardgewicht für eine neu erzeugte Kante */
	private static int DEFAULT_WEIGHT = 1;
	/** Gewicht der Kante */
	private int weight;
	
	/**
	 * Erzeugt eine neue Kante mit Standardgewicht.
	 */
	public GTEdge() {
		this(DEFAULT_WEIGHT);
	}
	
	/**
	 * Erzeugt eine neue Kante mit dem angegebenen Gewicht.
	 * @param weight Gewicht für die Kante. Muss nicht-negativ sein.
	 * @throws IllegalArgumentException wenn das Gewicht kleiner als 0 ist.
	 */
	public GTEdge(int weight) throws IllegalArgumentException {
		setWeight(weight);
	}

	/**
	 * Kantengewicht setzen.
	 * @param weight Gewicht für die Kante. Muss nicht-negativ sein.
	 * @throws IllegalArgumentException wenn das Gewicht kleiner als Null ist.
	 */
	public void setWeight(int weight) throws IllegalArgumentException {
		if (weight < 0) {
			throw new IllegalArgumentException("weight must be non-negative!"); //$NON-NLS-1$
		}
		this.weight = weight;
	}
	
	/**
	 * Liefert das Kantengewicht.
	 * @return aktuelles Kantengewicht.
	 */
	public int getWeight() {
		return weight;
	}
	
	/** 
	 * Die String-Darstellung für Kanten entspricht dem Wert ihres Gewichts. 
	 */
	@Override
	public String toString() {
		return String.valueOf(weight);
	}

	/**
	 * Liefert einen Erzeuger für Kanten mit Standardgewicht.
	 * Dieser wird beim ersten Zugriff erstellt und danach wiederverwendet.
	 * @return Erzeuger für Kanten mit Standardgewicht.
	 */
	public static Supplier<GTEdge> getFactory() {
		// Sofern der Erzeuger noch nicht existiert -> erstellen
		if (edgeFactory == null) {
			edgeFactory = () -> new GTEdge();
		}
		return edgeFactory;
	}
	
	/**
	 * Erstellt einen Erzeuger für Kanten mit Zufallsgewichten.
	 * Bei jedem Aufruf der Methode wird, im Gegensatz zu {@link #getFactory()},
	 * jedes Mal ein neuer Erzeuger mit einem zufälligen Gewicht erstellt.
	 * @param maxWeight Obere Grenze für Gewichte (untere Grenze ist 1).
	 * @return Erzeuger für Kanten mit Zufallsgewicht.
	 */
	public static Supplier<GTEdge> createRandomWeightFactory(int maxWeight) { 
		// Zufallszahlengenerator beim ersten Zugriff erzeugen.
		if (randomSource == null) {
			randomSource = new Random();
		}
		// nextInt liefert eine Zahl zwischen 0 und dem
		// übergebenen Argument-1, wir wollen aber ein Gewicht
		// zwischen 1 und dem übergebenen Argument, daher "+1"
		return () -> new GTEdge(randomSource.nextInt(maxWeight) + 1);
	}
	
	/** 
	 * Factory für eine Kante zum Gebrauch durch einen GML-Reader. Liest das Gewicht.
	 * Die apply()-Methode liest das Kantengewicht ein.
	 * @return Factory-Funktion. 
	 */
	protected static Function<EdgeMetadata,GTEdge> createGMLFactory() {
		return (EdgeMetadata eMeta) -> {
			// Lies das Kantengewicht aus der übergebenen Wertetabelle.
			// Schlägt dies fehl, verwende den Standardkonstruktor für Kanten.
			String weightS = eMeta.getProperty(WEIGHT_KEY);
			if (weightS == null) {
				return new GTEdge();
			}
			return new GTEdge(Integer.valueOf(weightS).intValue());
		};
	}
	
	/**
	 * Erweitert den GraphMLWriter, so dass die Eigenschaften der Kante mit ausgegeben
	 * werden.
	 * @param gmlWriter GraphMLWriter-Objekt, das erweitert werden soll.
	 * @see GraphMLWriter#addEdgeData(String, String, String, Function)
	 */
	protected static void addDataTransformersToGMLWriter(GraphMLWriter<GTVertex, GTEdge> gmlWriter) {
		// Unter dem Schlüssel WEIGHT_KEY wird je Kante der Wert von getWeight() ergänzt.
		gmlWriter.addEdgeData(WEIGHT_KEY, null, null, (GTEdge edge) -> String.valueOf(edge.getWeight())); 
	}
}