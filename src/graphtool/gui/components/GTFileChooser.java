package graphtool.gui.components;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Dateiauswahldialog mit folgenden Eigenschaften:
 * <p><ul>
 * <li>Dateifiltermöglichkeiten gemäß {@link #FILE_TYPES}
 * <li>Voreinstellung der Filterung auf den ersten Filter
 * <li>keine Mehrfachauswahl.
 * </ul>
 * Der Dialog kann zum Öffnen und zum Speichern verwendet werden.
 */
public class GTFileChooser extends JFileChooser {

	private static final long serialVersionUID = 4815969684761006407L;
	
	/** Endungen und Erläuterungen für die erlaubten Dateitypen */
	@SuppressWarnings("nls")
	public static final String[][] FILE_TYPES = { 
			{ ".gml", "Graph Modeling Language" } 
			};
	/** 
	 * Die FileFilter-Objekte zu den Endungen und Beschreibungen aus FILE_TYPES.
	 * Zur Verwendung voreingestellt wird der erste Eintrag.
	 * @see #FILE_TYPES
	 */
	private FileFilter[] filtersUsed = null;
	
	/**
	 * Erzeugt einen Dateiauswahldialog.
	 */
	public GTFileChooser() {
		// Filer für die FILE_TYPES erzeugen und hinzufügen
		addFileFilters();
		// Auch die Auswahl "Alle Dateien" soll erlaubt sein
	 	setAcceptAllFileFilterUsed(true);
	 	// Ersten Typfiler einstellen
		setFileFilter(filtersUsed[0]);
		// Mehrfachauswahl ausschalten
		setMultiSelectionEnabled(false);
	}
	
	/**
	 * Fügt für alle Einträge in FILE_TYPES je einen Filter hinzu.
	 * @see #FILE_TYPES
	 */
	private void addFileFilters() {
		filtersUsed = new FileFilter[FILE_TYPES.length];
		for (int i = 0; i < FILE_TYPES.length; i++) {
			// Einen FileFilter je Eintrag erstellen
			String[] entry = FILE_TYPES[i];
			FileFilter ff = new FileFilter() {
				@Override
				public boolean accept(File file) {
					// file muss ein Verzeichnis sein oder
					// auf die in entry hinterlegte Endung passen
					return file.isDirectory() || file.getName().toLowerCase().endsWith(entry[0]);
				}
				@SuppressWarnings("nls")
				@Override
				public String getDescription() {
					// In der Combobox werden die Beschreibung und die Endung angezeigt 
					return entry[1] + " (*" + entry[0] + ")";
				}
			};
			// Filter speichern für die spätere Verwendung
			filtersUsed[i] = ff;
			// Filter beim Dialog registrieren
			addChoosableFileFilter(ff);
		}
	}

	/**
	 * Liefert für einen Öffnen-Dialog den absoluten Pfad zur ausgewählten Datei.
	 * Convenience-Methode für getSelectedFile().getAbsolutePath().
	 * @return den absoluten Pfad zur ausgewählten Datei.
	 * @see JFileChooser#getSelectedFile()
	 * @see File#getAbsolutePath()
	 */
	public String getAbsolutePathSelected() {
		return getSelectedFile().getAbsolutePath();
	}
	
	/**
	 * Liefert für einen Speichern-Dialog den absoluten Pfad zur ausgewählten Datei.
	 * Ist ein Dateifilter aktiv, so wird geprüft, ob der gewählte Dateiname auf die
	 * zu diesem Filter gehörende Dateiendung passt. Wenn nein, wird der Pfadname
	 * um die Endung ergänzt. Dies geschieht insbesondere dann, wenn ein neuer Dateiname
	 * eingegeben wird, ohne die Endung separat zu tippen.
	 * Ist der "Alle Dateien"-Filter aktiv, erfolgt keine Ergänzung.
	 * @return den absoluten Pfad zur ausgewählten Datei.
	 */
	public String getAbsulutePathSelectedWithExt() {
		// Nicht modifizierten Pfad holen
		String path = getAbsolutePathSelected();
		// aktuellen Dateifilter bestimmen
		FileFilter curFilter = getFileFilter();
		for (int i = 0; i < filtersUsed.length; i++) {
			// Wenn der Filter gleich einem Eintrag in filtersUsed ist...
			if (curFilter == filtersUsed[i]) {
				// Sofern der Pfad (noch) nicht auf die gewünschte Endung endet...
				if (!path.toLowerCase().endsWith(FILE_TYPES[i][0])) {
					// Endung ergänzen.
					path += FILE_TYPES[i][0];
				}
			}
		}
		return path;
	}
}

