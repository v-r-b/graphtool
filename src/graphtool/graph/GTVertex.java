package graphtool.graph;

import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.NodeMetadata;
import graphtool.res.Messages;

/**
 * Ein Knoten besitzt einen eindeutigen Bezeichner sowie einen Namen.
 * <p>
 * Die Klasse stellt u.a. statische Factory-Methoden sowie eine Methode
 * zur Serialisierung durch einen GraphMLWriter zur Verfügung.
 */
public class GTVertex {

	/** Schlüssel der Namenseigenschaft in GML-Datei */
	private static final String NAME_KEY = "name"; //$NON-NLS-1$

	/** Erzeuger für Knoten mit Standardnamen */
	private static Supplier<GTVertex> vertexFactory = null;

	/** Eindeutiger Bezeichner eines Knotens. */
	private UUID uuid = null;
	/** Fortlaufende Knotennummer */
	private static int curNum = 1;

	/** Standardname eines Knotens (Name = &lt;Standardname&gt;&lt;Nummer&gt;). */
	private static final String DEFAULT_NAME_PREFIX = 
			Messages.getString("GTVertex.DefaultNamePrefix"); //$NON-NLS-1$
	/** Name des Knoten. */
	private String name;
	
	/** 
	 * Erzeugt einen Knoten mit neuer UUID und einem fortlaufend numerierten 
	 * Knotennamen.
	 * @see #DEFAULT_NAME_PREFIX
	 */
	public GTVertex() {
		this(UUID.randomUUID(), DEFAULT_NAME_PREFIX + (curNum++));
	}
	
	/** 
	 * Erzeugung eines Knotens mit neuer UUID und dem angegeben Namen. 
	 * @param name Name des neuen Knotens.
	 */
	public GTVertex(String name) {
		this(UUID.randomUUID(), name);
	}
	
	/**
	 * Erzeugung eines Knotens mit gegebener UUID und gegebenem Namen.
	 * @param uuid Eindeutiger Bezeichner, z.B. aus einer GraphML-Datei eingelesen.
	 * @param name Name des Knotens (zur Anzeige).
	 */
	protected GTVertex(UUID uuid, String name) {
		this.uuid = uuid;
		setName(name);
	}
	
	/**
	 * Knotennamen ändern.
	 * @param name Neuer Name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Knotennamen zurückliefern.
	 * @return Aktueller Knotenname.
	 */
	public String getName() {
		return name;
	}
	

	/**
	 * Die Zeichenkettendarstellung entspricht der UUID.
	 */
	@Override
	public String toString() {
		return uuid.toString();
	}

	/**
	 * Liefert einen Erzeuger für Knoten mit Standardnamen.
	 * Dieser wird beim ersten Zugriff erstellt und danach wiederverwendet.
	 * @return Erzeuger für Knoten mit Standardnamen.
	 */
	public static Supplier<GTVertex> getFactory() {
		// Sofern der Erzeuger noch nicht existiert -> erstellen
		if (vertexFactory == null) {
			vertexFactory = () -> new GTVertex();
		}
		return vertexFactory;
	}
	
	/** 
	 * Factory für einen Knoten zum Gebrauch durch einen GML-Reader. 
	 * Die apply()-Methode liest die UUID und den Namen ein.
	 * @return Factory-Funktion. 
	 */
	protected static Function<NodeMetadata,GTVertex> createGMLFactory() { 
		return (NodeMetadata nMeta) -> {
			// Lies die UUID aus der ID des Knotens. Schlägt dies fehl,
			// verwende eine neu erzeugte UUID.
			UUID uuid = null;
			try {
				uuid = UUID.fromString(nMeta.getId());
			}
			catch (IllegalArgumentException e) {
				uuid = UUID.randomUUID();
			}
			// Lies den Namen aus der übergebenen Wertetabelle.
			// Schlägt dies fehl, verwende den Standardnamen für Knoten.
			String name = nMeta.getProperty(NAME_KEY);
			if (name == null) {
				System.err.printf(Messages.getString(
						"GTVertex.ErrorCannotReadName_fmt_s") + '\n', nMeta.getId()); //$NON-NLS-1$
				return new GTVertex(uuid, DEFAULT_NAME_PREFIX + (curNum++));
			}
			return new GTVertex(uuid, name);
		};
	}

	/**
	 * Erweitert den GraphMLWriter, so dass die Eigenschaften des Knotens mit ausgegeben
	 * werden.
	 * @param gmlWriter GraphMLWriter-Objekt, das erweitert werden soll.
	 * @see GraphMLWriter#addVertexData(String, String, String, Function)
	 */
	protected static void addDataTransformersToGMLWriter(GraphMLWriter<GTVertex, GTEdge> gmlWriter) {
		// Unter dem Schlüssel NAME_KEY wird je Knoten der Wert von getName() ergänzt.
		gmlWriter.addVertexData(NAME_KEY, null, null, (GTVertex v) -> v.getName());
	}
}