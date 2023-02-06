package graphtool.res;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * Klasse zur Bereitstellung der externalisierten Strings für die Anwendung.
 * Die Schlüssel-Wert-Paare befinden sich in der Datei BUNDLE_NAME.
 * <p>
 * Die Klasse wurde über den "Externalize Strings Wizard" von Eclipse erzeugt und
 * dann modifiziert.
 * @see #BUNDLE_NAME
 * @see ResourceBundle#getBundle(String)
 */
public class Messages {
	/** Für Debugging-Zwecke: Meldung ausgeben, wenn Einträge nicht gefunden werden */
	private static final boolean SHOW_MESSAGE_ON_MISSING_VALUES = true;
	
	/** Textdatei zum Resource Bundle. Der Dateiname endet auf ".properties" */
	private static final String BUNDLE_NAME = "graphtool.res.messages"; //$NON-NLS-1$
	/** 
	 * Schlüsselendung für den Trenner in einem Array. Ist der Schlüssel für das
	 * Array selbst "einArray", so ist der Schlüssel für den Trenner "einArray.&lt;{@link #DELIMITER_KEY}&gt;".
	 * Gespeichert werden soll hier ein regulärer Ausdruck zur Verwendung mit {@link String#split(String)}
	 */
	private static final String DELIMITER_KEY = "delim"; //$NON-NLS-1$
	/** Standard-Trenner für Arrays, falls kein eigener angegeben wird. */
	private static final String STD_DELIMITER = ":"; //$NON-NLS-1$

	/** Das verwendete Resource Bundle */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/** Singleton-Konstruktor */
	private Messages() {
	}

	/**
	 * Liefert eine Funktion, mit Hilfe derer die Meldungen gelesen werden können.
	 * Diese erwartet als Parameter einen Schlüsselwert und gibt den dazu in der
	 * Messages-Datei gespeicherten Ergebniswert zurück (oder null, falls nicht vorhanden).
	 * Die Funktion nutzt die Methode internalGetString().
	 * @return Funktion zum Lesen der Texte aus der Messages-Datei
	 * @see #internalGetString(String)
	 */
	public static Function<String, String> getTextProvider() {
		return (String key) -> internalGetString(key);
	}
	
	/**
	 * Liefert den Eintrag zum angegebenen Schlüssel.
	 * Ist kein Eintrag vorhanden, liefert die Methode null.
	 * Ist {@link #SHOW_MESSAGE_ON_MISSING_VALUES} true, wird eine Fehlermeldung
	 * ausgegeben, wenn zum angegebenen Schlüssel kein Wert gefunden wurde.
	 * @param key Schlüsselwert
	 * @return Eintrag zum Schlüsselwert oder null.
	 */
	public static String getString(String key) {
		String result = internalGetString(key);
		if (SHOW_MESSAGE_ON_MISSING_VALUES && (result == null)) {
			System.err.println(Messages.class.getName() +
					": Cannot locate key " + key + //$NON-NLS-1$
					" in resource bundle " + BUNDLE_NAME); //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * Liefert den Eintrag zum angegebenen Schlüssel.
	 * Ist kein Eintrag vorhanden, liefert die Methode null.
	 * Es wird keine Fehlermeldung ausgegeben.
	 * @param key Schlüsselwert
	 * @return Eintrag zum Schlüsselwert oder null.
	 */
	private static String internalGetString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return null;
		}
	}
	
	/**
	 * Liefert den Eintrag zum angegebenen Schlüssel.
	 * Ist kein Eintrag vorhanden, liefert die Methode "!" + key + "!".
	 * @param key Schlüsselwert
	 * @return Eintrag zum Schlüsselwert oder Warnhinweis.
	 */
	public static String getStringNotNull(String key) {
		String result = getString(key);
		if (result != null) {
			return result;
		} 
		return '!' + key + '!';
	}
	
	/**
	 * Liefert den Trenner zum durch den Schlüssel bezeichneten Array. Ist der Schlüssel für das
	 * Array selbst "einArray", so ist der Schlüssel für den Trenner "einArray.&lt;{@link #DELIMITER_KEY}&gt;".
	 * <p>
	 * Ist zu diesem Schlüssel kein Wert vorhanden, liefert die Methode den Standardtrenner
	 * {@link #STD_DELIMITER}. Der Trenner ist ein reguärer Ausdruck zur Verwendung mit
	 * {@link String#split(String)}.
	 * @param key Schlüsselwert des Arrays
	 * @return zum Array gespeicherter Trenner oder Standard-Trenner
	 */
	public static String getDelimiter(String key) {
		String delimiter = internalGetString(key + '.' + DELIMITER_KEY);
		return delimiter != null ? delimiter : STD_DELIMITER;
	}

	/**
	 * Ruft {@link #getStringArray(String, String)} mit dem Ergebnis von 
	 * {@link #getDelimiter(String)} als zweitem Parameter auf.
	 * @param key Schlüsselwert zur Weitergabe an {@link #getStringArray(String, String)}
	 * @return String-Array mit anhand des Trenners bestimmten Einträgen oder null.
	 */
	public static String[] getStringArray(String key) {
		return getStringArray(key, getDelimiter(key));
	}
	
	/**
	 * Liefert das Array zum angegebenen Schlüssel. Ein Array ist eine Zeichenkette,
	 * die mit Trennzeichen unterbrochen ist, z.B. "eins:zwei:drei" mit ":" als Trenner.
	 * Welcher Trenner verwendet wird, bestimmt der entsprechende Parameter.
	 * Ist kein Eintrag vorhanden, liefert die Methode null.
	 * @param key Schlüsselwert
	 * @param delimiterRegex Regulärer Ausdruck zum Trennen des Strings mit {@link String#split(String)}.
	 * @return String-Array mit anhand des Trenners bestimmten Einträgen oder null.
	 */
	public static String[] getStringArray(String key, String delimiterRegex) {
		// Einfachen String zum Schlüssel einlesen
		String value = getString(key);
		if (value != null) {
			// Anhand des Trenners aufteilen und zurückgeben.
			return value.split(delimiterRegex);
		}
		return null;
	}
}
