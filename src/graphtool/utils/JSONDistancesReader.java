package graphtool.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Liefert Werte aus einer JSON-Datei, die Distanzinformationen zwischen Orten enthält.
 * Die Orte sind Zeichenketten, die Entfernungen positive ganze Zahlen.
 * Der Dateiaufbau (JSON-Array) ist folgendem Beispiel zu entnehmen
 * (die Entfernungsangaben sind Ganzzahlen &gt;= 0):<p>
 * <pre>
 * [ {
 *     "Saarbrücken": { "Schwerin":576,"Stuttgart":166,"Wiesbaden":129 }
 * } , {
 *     "Schwerin": { "Stuttgart":561,"Wiesbaden":450 } 
 * } , {
 *     "Stuttgart": { "Wiesbaden":159 } 
 * } ] </pre>
 */
public class JSONDistancesReader {

	/** Dateiendung für JSON-ateien */
	public static final String JSON_EXTENSION = ".json"; //$NON-NLS-1$

	/** 
	 * Schlüssel-Werte-Paare zu den Einträgen in der eingelesenen Datei.
	 * Dabei ist der Schlüssel der Name eines Ortes, zu dem eine Reihe von
	 * Entfernungsangaben zu anderen Orten gespeichert sind, die wiederum
	 * als Wert in der Map abgelegt werden. Beispiel für ein Schlüssel-Wert-Paar:<p>
	 * <code>"Schwerin":{"Stuttgart":561,"Wiesbaden":450}</code>
	 */
	private Map<String, JSONObject> distances = null;

	/**
	 * Liest Distanzinformationen aus einer JSON-Datei im Dateisystem.
	 * (Dateibeschreibung siehe hier: {@link #JSONDistancesReader}).
	 * @param pathname Pfadangabe innerhalb des Dateisystems
	 * @throws JSONException falls {@link #read(InputStream)} eine solche Exception wirft
	 * @throws FileNotFoundException falls {@link #read(InputStream)} eine solche Exception wirft
	 * @throws IOException falls beim Schließen der Datei eine solche Exception geworfen wird
	 * @see #read(InputStream)
	 */
	public void read(String pathname) throws JSONException, FileNotFoundException, IOException {
		try (FileInputStream fis = new FileInputStream(pathname)) {
			read(fis);
		}
	}

	/**
	 * Liest Distanzinformationen aus einer JSON-Datei im Klassenpfad.
	 * (Dateibeschreibung siehe hier: {@link #JSONDistancesReader}).
	 * @param pathname Pfadangabe relativ zum Klassenpfad
	 * @throws JSONException falls {@link #read(InputStream)} eine solche Exception wirft
	 * @see #read(InputStream)
	 */
	public void readFromClasspath(String pathname) throws JSONException {
		read(getClass().getClassLoader().getResourceAsStream(pathname));
	}

	/**
	 * Liest Distanzinformationen aus einer JSON-Datei.
	 * (Dateibeschreibung siehe Klassendokumentation: {@link #JSONDistancesReader}).
	 * @param is InputStream, aus dem gelesen werden soll. Darf nicht null sein.
	 * @throws JSONException falls der Aufabu der JSON-Datei nicht passt.
	 * @throws NullPointerException falls der InputStream null ist. 
	 */
	public void read(InputStream is) throws JSONException {
		if (is == null) {
			throw new NullPointerException("JSON InputStream is null!"); //$NON-NLS-1$
		}
		// Inhalte aus der JSON-Datei lesen und in einem JSONArray ablegen.
		JSONTokener tokener = new JSONTokener(is);
		JSONArray distanceJSONObjects = new JSONArray(tokener);
		// Die Map erleichtert das spätere Suchen.
		distances = new HashMap<>();
		for (int i = 0; i < distanceJSONObjects.length(); i++) {
			// Für jeden Eintrag obj im JSONArray:
			JSONObject obj = distanceJSONObjects.getJSONObject(i);
			// Alle Schlüssel-Wert-Paare in die Map eintragen.
			// Im Fall der Distanzeinträge ist der Schlüssel die
			// Start-Stadt und der Wert ein JSONObject mit Entfernungsangaben
			// zu (mehreren) anderen Städten.
			for (String city : obj.keySet()) {
				distances.put(city, obj.getJSONObject(city));
			}
		}
		try {
			// Stream nach dem Lesen schließen
			is.close();
		} catch (IOException e) {
			// Fehlerbehandlung beim Schließen ist nicht notwendig
		}
	}

	/**
	 * Gibt die Entfernung von from nach to zurück.
	 * Dazu wird die Methode simpleGetDistance() verwendet. 
	 * Liefert deren Aufruf mit den Parametern (from, to) -1, wird ein weiterer
	 * Versuch mit den Parametern (to, from) gemacht. Liefert auch dieser -1,
	 * gibt getDistance (im Gegensatz zu simpleGetDistance) 0 zurück. 
	 * War einer der beiden Aufrufe erfolgreich,
	 * liefert getDistance die Entfernung zwischen from und to. 
	 * Wird dieselbe Zeichenkette für from und to übergeben, liefert die Methode 0.
	 * @param from Startpunkt für die Suche (nicht null)
	 * @param to Zielpunkt für die Suche (nicht null)
	 * @return Entfernung zwischen from und to oder 0
	 * @see #simpleGetDistance(String, String)
	 */
	public int getDistance(String from, String to) {
		// Mit (from, to) versuchen...
		int distance = simpleGetDistance(from, to);
		// Falls nicht gefunden, mit (to, from) versuchen...
		if (distance == -1) {
			distance = simpleGetDistance(to, from);
		}
		// Falls nicht gefunden oder falls ein ungültiger Wert
		// gefunden wurde (< 0), 0 zurückgeben, sonst das Ergebnis
		return distance < 0 ? 0 : distance;
	}

	/**
	 * Gibt die Entfernung von from nach to zurück.
	 * Dabei wird from als Schlüssel für die Map {@link #distances} verwendet.
	 * Wurden keine Werte eingelesen (distances == null), wird der Schlüssel from
	 * nicht in der Map gefunden oder ist dort im Ergebnisdatensatz für den Schlüssel 
	 * kein Eintrag für den Wert von to hinterlegt, liefert die Methode -1 als Ergebnis.
	 * Wird dieselbe Zeichenkette für from und to übergeben, liefert die Methode 0.
	 * @param from Startpunkt für die Suche (nicht null)
	 * @param to Zielpunkt für die Suche (nicht null)
	 * @return Entfernung zwischen from und to oder -1
	 */
	public int simpleGetDistance(String from, String to) {
		// Sind die Parameter gültig? Wenn nein -> -1
		if (from == null || to == null) {
			return -1;
		}
		// Wenn Werte vorhanden sind ...
		if (distances != null) {
			// Sind beide Parameter gleich? -> 0
			if (from.equals(to)) {
				return 0;
			}
			// Suche den Datensatz für "from" in der Map
			JSONObject obj = distances.get(from);
			if (obj != null) {
				try {
					// Falls gefunden -> liefere den Wert für "to".
					// Schlägt dies fehl, wirft getInt eine JSONException
					return obj.getInt(to);
				}
				catch (JSONException e) {
					// Kein Eintrag für (from, to) vorhanden -> -1
					return -1;
				}
			}
		}
		// Kein Eintrag für (from, to) vorhanden -> -1
		return -1;
	}

	/**
	 * Liefert die String-Repräsentation der enthaltenen Map.
	 */
	@Override
	public String toString() {
		return distances.toString();
	}

	/**
	 * Liefert eine Zeichenkette, die das JSON-Array der gespeicherten
	 * Distanzen wiedergibt. Dabei ist je Array-Eintrag eine Zeile vorgesehen.
	 * @return Text-Darstellung des JSON-Arrays
	 */
	@SuppressWarnings("nls")
	public String toFormattedString() {
		// Schlüsselwerte aufsteigend sortieren
		String[] keys = distances.keySet().toArray(new String[0]);
		StringBuilder result = new StringBuilder();
		Arrays.sort(keys);
		// Zeilenweise die Ergebnisdatensätze zu den Schlüsselwerten aneinander hängen
		result.append("[\n");
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			result.append("{\"" + key + "\":" + distances.get(key) + (i < keys.length - 1 ? "},\n" : "}\n"));
		}
		result.append("]");
		return result.toString();
	}
}
