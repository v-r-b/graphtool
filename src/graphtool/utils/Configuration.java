package graphtool.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Ermöglicht das Lesen und Schreiben von Konfigurationsdateien.
 * Für das Dateiformat wird java.util.Properties verwendet.
 * <p>
 * Über die statische Methode getDefaultInstance() wird eine Instanz
 * bereitgestellt, die eine zur main()-Methode passende Datei verwendet.
 * @see #getDefaultInstance()
 * @see Properties
 */
public class Configuration {
	
	/**
	 * Instanz, deren zugehörige Datei den Namen der Klasse trägt, deren main()-Methode
	 * beim Programmstart aufgerufen wurde (in Kleinbuchstaben + CONFIG_EXTENSION);
	 * @see #CONFIG_EXTENSION
	 */
	private static Configuration defaultConfig = null;
	/** Dateiendung für Konfigurationsdateien */
	public static final String CONFIG_EXTENSION = ".cfg"; //$NON-NLS-1$
	
	/** Dateiname der Konfigurationsdatei */
	private String propsFileName = null;
	/** Konfigurationsdaten */
	private Properties props = null;
	
	/**
	 * Gibt die Instanz zurück, die durch setDefaultInstance() erzeugt wurde
	 * oder (falls setDefaultInstance() nicht verwendet wird) die, deren
	 * Dateinamen sich aus dem Namen der Klasse zusammensetzt, deren main()-Methode 
	 * beim Programmstart aufgerufen wurde (in Kleinbuchstaben) und der Endung CONFIG_EXTENSION.
	 * <p>
	 * Der Inhalt der Datei wird geladen.
	 * Ist die entsprechende Datei nicht vorhanden, versucht die Methode sie neu
	 * anzulegen. Scheitert auch dies, wird null zurückgegeben.
	 * 
	 * @return Standard-Konfiguration oder null
	 * @see #setDefaultInstance(String)
	 * @see #CONFIG_EXTENSION
	 */
	public static Configuration getDefaultInstance() {
		// Sofern noch keine Standardkonfiguration vorhanden -> anlegen
		if (defaultConfig == null) {
			// Standardname, falls die Bestimmung des Dateinamens aus dem
			// StackTrace nicht funktioniert.
			String fileName = null;
			// Klasse mit der aufgerufenen main()-Methode finden.
			StackTraceElement[] trace = Thread.currentThread().getStackTrace();
			// *Erste* main-Methode im Stack Trace suchen. Die Methode ist nicht
			// ganz sauber, denn wenn weiter oben im Stack eine weitere main()-Methode
			// enthalten ist, wird diese verwendet. Der Grund für die Suche nach main()
			// (statt den untersten Eintrag im Stack zu verwenden) ist, dass beim 
			// Start aus einem JAR die main()-Methode des JAR-Loaders zuunterst im
			// Stack liegt statt die main()-Methode der Hauptklasse des Programms.
			for (StackTraceElement elt : trace) {
				fileName = elt.getFileName();
				if (elt.getMethodName().equals("main")) { //$NON-NLS-1$
					// bei erster gefundener "main"-Methode abbrechen.
					break;
				}
			}
			if (fileName == null) {
				fileName = "config"; //$NON-NLS-1$
			}
			else {
				// ".java" löschen
				fileName = fileName.substring(0, fileName.indexOf('.'));
			}
			// Namen in Kleinbuchstaben umwandeln und um Endung ergänzen
			fileName = fileName.toLowerCase() + CONFIG_EXTENSION;
			setDefaultInstance(fileName);
		}
		return defaultConfig;
	}
	
	/**
	 * Erzeugt eine Instanz mit dem als Parameter angegebenen Dateinamen.
	 * Spätere Aufrufe von getDefaultInstance() liefern dann diese Instanz. 
	 * Die Methode kann genutzt werden, wenn der Dateiname nicht automatisch
	 * bestimmt werden soll (wie dies ohne Aufruf von setDefaultInstance() bei
	 * Verwendung von getDefaultInstance() der Fall wäre. 
	 * <p>
	 * Der Inhalt der Datei wird geladen.
	 * Ist die entsprechende Datei nicht vorhanden, versucht die Methode sie neu
	 * anzulegen. Scheitert auch dies, wird null zurückgegeben.
	 * 
	 * @param fileName Dateiname für die programmweite Standardkonfiguration
	 * @return Standard-Konfiguration oder null
	 */
	public static Configuration setDefaultInstance(String fileName) {
		try {
			// Versuchen, eine Instanz mit diesem Namen anzulegen
			defaultConfig = new Configuration(fileName);
		}
		catch(Exception e) {
			// Bei Problemen null zurückgeben.
			// Ausnahmebehandlung erfolgt bereits im verwendeten Konstruktor
			defaultConfig = null;
		}
		return defaultConfig;
	}
	
	/**
	 * Erzeugt eine Instanz, deren Dateiname als Parameter angegeben wurde.
	 * <p>
	 * Der Inhalt der Datei wird geladen. Für das Dateiformat wird java.util.Properties verwendet.
	 * Ist die entsprechende Datei nicht vorhanden, versucht der Konstruktor, sie neu
	 * anzulegen. Scheitert auch dies, wird eine Exception geworfen.
	 * 
	 * @param fileName Dateiname der Konfigurationsdatei
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see Properties
	 */
	public Configuration(String fileName) throws FileNotFoundException, IOException {
		propsFileName = fileName;
		props = new Properties();
		// Versuchen, ob die Datei gelesen werden kann.
		try {
			load();
		}
		catch (FileNotFoundException e) {
			System.err.println("Could not find config file " + fileName +  //$NON-NLS-1$
								".\nCreating it in directory " +  //$NON-NLS-1$ 
								FileUtils.getCWD());
			// Datei nicht gefunden; Vermutlich noch nicht angelegt. -> Speichern
			save();
		}
		catch (Exception e) {
			System.err.println("Could not save config file " + fileName); //$NON-NLS-1$
			e.printStackTrace();
			// Im Falle IOException (beim Laden oder Speichern) oder 
			// FileNotFoundException (beim Speichern): Exception durchreichen
			throw e;
		}
	}

	/**
	 * Liest die Konfigurationsdatei, deren Namen dem Konstruktor mitgegeben wurde.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see Properties#load(InputStream)
	 */
	public void load() throws FileNotFoundException, IOException {
		try (FileInputStream fis = new FileInputStream(propsFileName)) {
			props.load(fis);
		}
	}
	
	/**
	 * Schreibt die Konfigurationsdatei, deren Namen dem Konstruktor mitgegeben wurde.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see Properties#store(java.io.OutputStream, String)
	 */
	public void save() throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(propsFileName)) {
			props.store(fos, null);
		}
	}

	/**
	 * Liefert den Wert zum angegebenen Schlüssel.
	 * Gibt null zurück, wenn der Schlüssel nicht gefunden wird.
	 * @param key Schlüssel
	 * @return zugehöriger Wert oder null
	 * @see Properties#getProperty(String)
	 */
	public String get(String key, boolean showError) {
		String result = props.getProperty(key);
		if (showError && (result == null)) {
			System.err.println(getClass().getName() +
					": Could not locate key " + key +  //$NON-NLS-1$
					" in " + propsFileName); //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * Liefert ein Array von Werten zum angegebenen Basisschlüssel.
	 * Nutzt dazu die get()-Methode wie folgt:
	 * Die tatsächlichen Schlüssel bilden sich aus &lt;Basisschlüssel&gt;.&lt;num&gt;, 
	 * wobei &lt;num&gt; eine Ganzzahl ist, beginnend bei 0 und fortlaufend. 
	 * Beispiel (mit "EinSchluessel" als Basisschlüssel):<ul>
	 * <li>EinSchluessel.0=erster Wert
	 * <li>EinSchluessel.1=zweiter Wert
	 * </ul>
	 * Gibt null zurück, wenn &lt;Basisschlüssel&gt;.0 nicht gefunden wird.
	 * @param baseKey Basisschlüssel
	 * @return Array mit zugehörigen Werten (s.o.) oder null
	 * @see #get(String, boolean)
	 */
	public String[] getNumbered(String baseKey, boolean showError) {
		// Ergebnis hat zunächst unbekannte Größe
		ArrayList<String> result = new ArrayList<>();
		int i = 0;
		// Erster Wert gehört zum Schlüssel <Basisschlüssel>.0
		String aValue = get(baseKey + '.' + i, false);
		// Solange Schlüssel (und Werte) gefunden werden...
		while (aValue != null) {
			// dem Ergebnis hinzufügen
			result.add(aValue);
			// Zähler inkrementieren und nächsten Eintrag suchen
			aValue = get(baseKey + '.' + (++i), false);
		}
		// Null zurückgeben, wenn die Liste leer geblieben ist
		if (showError && (result.size() == 0)) {
			System.err.println(getClass().getName() +
					": Could not locate base key " + baseKey + //$NON-NLS-1$ 
					" in " + propsFileName); //$NON-NLS-1$
		}
		return result.size() > 0 ? result.toArray(new String[0]) : null;
	}
	
	/**
	 * Prüft, ob ein Wert zum angegebenen Schlüssel den Wert "true" hat.
	 * Groß-/Kleinschreibung wird beim Vergleich nicht beachtet.
	 * @param key Schlüssel
	 * @return true, wenn der zugehörige Wert "true" ist.
	 */
	public boolean isTrue(String key) {
		String value = props.getProperty(key);
		return Boolean.toString(true).equalsIgnoreCase(value);
	}
	
	/**
	 * Setzt den Wert zum angegebenen Schlüssel
	 * @param key Schlüssel
	 * @param value zugehöriger Wert
	 */
	public void set(String key, String value) {
		props.setProperty(key, value);
	}
	
	/**
	 * Gibt alle Schlüssel-Wert-Paare auf den übergebenen Stream aus.
	 * @p Ausgabe-PrintStream. Kann z.B. Systen.out oder System.err sein.
	 */
	public void print(PrintStream p) {
		for (Object o: props.keySet()) {
			p.println(o.toString() + '=' + props.get(o));
		}
	}
}
