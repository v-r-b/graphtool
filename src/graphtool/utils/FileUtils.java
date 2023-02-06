package graphtool.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Sammlung statischer Methoden rund um Dateien.
 * Dank an Greg Briggs (http://www.uofr.net/~greg/java/get-resource-listing.html) 
 * und StackOverflow-Nutzer „j-hap“ (https://stackoverflow.com/a/70542570) 
 * für die Inspiration zur Implementierung der Methode listFilenames(String, String[]). 
 */
public class FileUtils {
	/**
	 * Convenience-Methode für listFilenames mit nur einer Dateiendung als Filter.
	 * @param dirPath Verzeichnis, dessen Inhalt aufgelistet werden soll.
	 * @param fileExtension Zeichenkette, auf die der Dateiname enden muss oder null für alle.
	 * @return Dateieinträge im übergebenen Verzeichnis oder null.
	 * @see #listFilenames(String, String[])
	 */
	public static Set<String> listFilenames(String dirPath, String fileExtension) {
		if (fileExtension == null) {
			return listFilenames(dirPath, (String[])null);
		}
		return listFilenames(dirPath, new String[] {fileExtension});
	}
	
	/**
	 * Liefert die Namen der Dateien im angegebenen Verzeichnis. 
	 * Das Verzeichnis kann im Dateisystem oder in der Jar-Datei sein, aus der
	 * der Aufruf erfolgt. Kann das Verzeichnis nicht gefunden werden, liefert
	 * die Methode null. Ist das Verzeichnis vorhanden, enthält aber keine
	 * Dateien, so liefert die Methode eine leere Menge. Das Ergebnis
	 * enthält nur Dateien, keine Verzeichnisse. Es handelt sich um einfache
	 * Dateinamen, nicht um volle Pfadangaben.
	 * Dank an Greg Briggs (http://www.uofr.net/~greg/java/get-resource-listing.html) 
	 * und StackOverflow-Nutzer „j-hap“ (https://stackoverflow.com/a/70542570) 
	 * für die Inspiration zur Implementierung der Methode.
	 * @param dirPath Verzeichnis, dessen Inhalt aufgelistet werden soll.
	 * @param fileExtensions Array mit Zeichenketten, auf die der Dateiname enden muss oder null für alle.
	 * @return Dateieinträge im übergebenen Verzeichnis oder null.
	 */
	@SuppressWarnings("nls")
	public static Set<String> listFilenames(String dirPath, String[] fileExtensions) {
		// Ergebnismenge initialisieren
		HashSet<String> result = new HashSet<>();
		// URL zum angegebenen Verzeichnis holen
		URL dirURL = ClassLoader.getSystemResource(dirPath);

		if (dirURL == null) {
			// Das angegebene Verzeichnis ist nicht zu finden
			(new FileNotFoundException("Cannot find resource \"" + dirPath + "\"")).printStackTrace();
			return null;
		}

		// Nullwertbehandlung bei der Dateiendung
		if (fileExtensions == null) {
			fileExtensions = new String[] {""};
		}

		if (dirURL.getProtocol().equals("file")) {
			// Das Verzeichnis liegt im Dateisystem
			try {
				// Alle Dateiendungen durchgehen
				for (String fe : fileExtensions) {
					// zunächst alle Verzeichniseinträge besorgen, die auf fe enden
					File[] files = new File(dirURL.toURI()).listFiles((File dir, String name) -> name.endsWith(fe));
					// Dann nur die Einträge behalten, die keine Verzeichnisse sind.
					for (File file : files) {
						if(file.isFile()) {
							result.add(file.getName());
						}
					}
				}
			} catch (URISyntaxException e) {
				// Verzeichnis kann nicht geöffnet werden -> null
				System.err.println("Could not convert " + dirURL.getFile() + " to URI"); 
				e.printStackTrace();
				return null;
			}
			return result;
		}

		else if (dirURL.getProtocol().equals("jar")) {
			// Das Verzeichnis liegt in einem Jar
			String jarPath = "<unknown>"; 
			try {
				// Pfadnamen der Jar-Datei herausfinden und Jar öffnen
				// Die URL sieht ungefähr so aus: file:/ein/verzeichnis/EinJar.jar!<dirPath>
				// Um den Dateipfad zur URL zu erhalten, wird decodiert (wegen Leer- und Sonderzeichen),
				// dann wird alles hinter dem "!" sowie das Protokoll "file:" weggeschnitten. 
				String path = URLDecoder.decode(dirURL.getPath(), "UTF-8");
				jarPath = path.substring(0, path.lastIndexOf(".jar!") + 4).replaceFirst("file:", "");
				try (JarFile jar = new JarFile(jarPath)) {
					// Jetzt alle Einträge im Jar auslesen und filtern
					Enumeration<JarEntry> entries = jar.entries();
					while(entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						// Wir suchen nur Dateien, keine Verzeichnisse
						if (!entry.isDirectory()) {
							String name = entry.getName();
							// ...und zwar nur im angegebenen Verzeichnis...
							if (name.startsWith(dirPath)) {
								// ...und mit der richtigen Dateiendung. Dazu die Endungen durchgehen
								for (String fe : fileExtensions) {
									if (name.endsWith(fe)) {
										// Bei Fund: Dateinamen (ohne Verzeichnisangabe) hinzufügen
										result.add(name.substring(dirPath.length()));
										// For-Schleife beenden, da bereits eine passende Endung gefunden wurde
										break;
									}
								}
							}						
						}
					}
				}
			} catch (IOException e) {
				// Verzeichnis kann nicht geöffnet werden -> null
				System.err.println("Could not handle jar file " + jarPath);
				e.printStackTrace();
				return null;
			}
			return result;
		}
		
		else {
			// Unbekanntes Protokoll -> null
			(new FileNotFoundException("Unsupported protocol \"" + dirURL.getProtocol() + "\"")).printStackTrace();
			return null;
		}
	}

	/**
	 * Gibt das aktuelle Arbeitsverzeichnis zurück. Dieses wird als Systemeigenschaft "user.dir"
	 * direkt aus der System-Klasse gelesen.
	 * <p>
	 * Hinweis für den Start einer Anwendung per Doppelklick:
	 * @return
	 */
	public static String getCWD() {
		String cwd = System.getProperty("user.dir"); //$NON-NLS-1$
		if (!cwd.endsWith(File.separator)) {
			cwd = cwd + File.separator;
		}
		return cwd;
	}

}
