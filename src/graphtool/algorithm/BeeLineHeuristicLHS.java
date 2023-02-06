package graphtool.algorithm;

import java.util.function.ToIntBiFunction;

import org.json.JSONException;

import graphtool.GraphTool;
import graphtool.graph.GTVertex;
import graphtool.utils.JSONDistancesReader;

/**
 * Beispielimplementierung einer Heuristik, die die Luftlinienentfernungen zwischen den deutschen
 * Landeshauptstädten kennt. Diese Angaben sind in einer JSON-Datei gespeichert,
 * die bei {@link #JSON_PATH} im Klassenpfad liegen muss.
 */
public class BeeLineHeuristicLHS implements ToIntBiFunction<GTVertex, GTVertex> {

	/** Objekt, um Entfernungsdaten einzulesen */
	private JSONDistancesReader beeLineInfo = null;
	/** Verzeichnis mit den Daten. In der Konfiguration einstellbar. */
	public static final String RESOURCE_DIR = GraphTool.getResourcePathRelative();
	/** JSON-Datei mit den Entfernungsdaten; wird im Klassenpfad gesucht. */
	private final static String JSON_PATH = RESOURCE_DIR + "LuftlinienLHS.json"; //$NON-NLS-1$
	
	/**
	 * Liest die JSON-Datei ein, die bei {@link #JSON_PATH} im Klassenpfad liegen muss.
	 * Wird die Datei nicht gefunden oder ist sie fehlerhaft, liefern die Aufrufe
	 * von {@link #applyAsInt(GTVertex, GTVertex)} dann jeweils 0.
	 */
	public BeeLineHeuristicLHS() {
		// Reader-Objekt erzeugen und Datei einlesen.
		beeLineInfo = new JSONDistancesReader();
		try {
			beeLineInfo.readFromClasspath(JSON_PATH);
		} catch (JSONException e) {
			// Fehler ausgeben. Die Heuristik liefert dann immer 0
			beeLineInfo = null;
			System.err.println("Could not load JSON file " + JSON_PATH); //$NON-NLS-1$
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Liefert die Entfernungsangabe aus den ausgelesenen Daten, die
	 * zu (from.getName(), to.getName()) passt. Wird kein Wert gefunden,
	 * liefert die Methode 0. Waren die Daten nicht lesbar, wird ebenfalls
	 * 0 zurückgegeben.
	 * @param from Startknoten für die Entfernungsabfrage
	 * @param to Zielknoten für die Entfernungsabfrage
	 * @return Entfernung oder 0.
	 */
	@Override
	public int applyAsInt(GTVertex from, GTVertex to) {
		if (beeLineInfo != null) {
			return beeLineInfo.getDistance(from.getName(), to.getName());
		}
		return 0;
	}

}
