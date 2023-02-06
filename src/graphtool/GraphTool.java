package graphtool;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;

import graphtool.gui.GTGraphControl;
import graphtool.res.Messages;
import graphtool.utils.Configuration;

/**
 * Startklasse für die Anwendung, enthält die main-Methode.
 * <p>
 * Die Klasse stellt darüber hinaus eine statische Methode bereit, die eine 
 * Referenz auf das einzige in der Anwendung existierende Exemplar zurückgibt.
 */
public class GraphTool extends JFrame {

	private static final long serialVersionUID = 3928016449625081185L;
	
	/** Name der Konfigurationsdatei (liegt im Arbeitsverzeichnis) */
	public final static String CONFIG_FILENAME = Messages.getString("GraphTool.ConfigFilename"); //$NON-NLS-1$
	/** Titel des Anwendungsfensters */
	public final static String MAIN_TITLE = Messages.getString("GraphTool.MainTitle"); //$NON-NLS-1$
	/** 
	 * Ort und Größe des Anwendungsfensters. Es erstreckt sich von der linken
	 * oberen Ecke des Bildschirms bis zu der Größe, von der davon ausgegangen
	 * wird, dass aktuelle Bildschirme diese in jedem Fall erfüllen können. 
	 */
	public final static Rectangle MAIN_BOUNDS = new Rectangle(0, 0, 1024, 768);
	
	/** (Einzige) Instanz des Hauptfensters */
	private static GraphTool graphTool = null;
	/** Stream zur Umleitung von stdout und stderr, wird in main() erzeugt */
	static ByteArrayOutputStream earlyConsoleOut = null;
	
	
	/**
	 * Liefert die (einzige) Instanz des Hauptfensters und kann z.B. von
	 * <p><b>GUI-Komponenten</b> zur Bestimmung des Anwendungsframes genutzt werden.
	 * @return Hauptfenster der Anwendung
	 */
	public static GraphTool getFrame() {
		// Objekt neu erzeugen, falls noch nicht vorhanden
		if (graphTool == null) {
			graphTool = new GraphTool();
		}
		return graphTool;
	}
	
	/**
	 * Pfad relativ zum Startverzeichnis der Anwendung oder innerhalb 
	 * einer JAR-Datei (je nach Art der Ausführung), an dem die Ressourcen zu
	 * finden sind, die die Anwendung zur Ausführung benötigt, wie z.B. 
	 * Beschriftungstexte, Beispielgraphen oder sonstige Datenquellen.
	 * @return relative Pfadangabe, endet auf "/"
	 */
	@SuppressWarnings("nls")
	public static String getResourcePathRelative() {
		String resourcePath = Configuration.getDefaultInstance().get("ResourcePathRelative", true);
		if (resourcePath == null) {
			resourcePath = "graphtool/res/";
			System.err.println("Assuming " + resourcePath);
			Toolkit.getDefaultToolkit().beep();
			Configuration.getDefaultInstance().set("ResourcePathRelative", resourcePath);
		}
		if (!resourcePath.endsWith("/")) {
			resourcePath += '/';
		}
		return resourcePath;
	}

	/**
	 * Startmethode für die Anwendung. 
	 * Legt die Konfigurationsdatei auf {link #CONFIG_FILENAME} fest und
	 * erzeugt und startet das Anwendungsfenster.
	 * Die Systemausgabekanäle werden in einen ByteArrayOutputStream umgeleitet.
	 * Auf diese Weise werden bereits vor dem Aufbau der grafischen
	 * Benutzeroberfläche auftretende Fehlermeldungen abgefangen.
	 * Der Stream wird dann an den Konstruktor übergeben.
	 * @param args Es sind "-noerr" und "-noout" erlaubt. Ist "-noerr" angegeben,
	 * erfolgt keine frühe Umleitung der Fehlerausgabe. Analog für "-noout"
	 * und die Standardausgabe.
	 */
	@SuppressWarnings("nls")
	public static void main(String[] args) {
		// Systemausgabekanäle ggf. in einen ByteArrayOutputStream umleiten.
		// Dazu zunächst die Kommandozeilenparameter überprüfen.
		boolean redirectErr = true, redirectOut = true;
		for (String arg: args) {
			if ("-noerr".equals(arg)) {
				// Keine frühe Umleitung der Fehlerausgabe
				redirectErr = false;
			}
			if ("-noout".equals(arg)) {
				// Keine frühe Umleitung der Standardausgabe
				redirectOut = false;
			}
			if ("-h".equals(arg) || "--help".equals(arg) || "/?".equals(arg)) {
				// Hilfe ausgeben und Programm beenden
				System.out.println("use: " + GraphTool.class.getSimpleName() + " [-noout] [-noerr]");
				System.out.println("  -noout: no early redirection of stdout");
				System.out.println("  -noerr: no early redirection of stderr");
				System.exit(0);
			}
		}
		// Wenn mindestens ein Stream umgeleitet werden soll...
		if (redirectOut || redirectErr) {
			earlyConsoleOut = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(earlyConsoleOut);
			if (redirectOut) {
				System.setOut(ps);
			}
			if (redirectErr) {
				System.setErr(ps);
			}
		}
		// Konfigurationsdatei festlegen
		Configuration.setDefaultInstance(CONFIG_FILENAME);
		// Fenster erzeugen und anzeigen
		getFrame().setVisible(true);
	}
	
	/**
	 * Erzeugt das Hauptfenster der Anwendung. 
	 * Die initiale Größe wird auf {@link #MAIN_BOUNDS} und der
	 * Fenstertitel auf {@link #MAIN_TITLE} festgelegt.
	 */
	private GraphTool() {
		setTitle(MAIN_TITLE);
		// Beim Klick auf den Schließen-Button in der Titelleiste 
		// wird die Anwendung beendet.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Das Kontrollfeld wird als Inhalt des Hauptfensters gesetzt.
        setContentPane(new GTGraphControl(earlyConsoleOut));
        // Initialgröße einstellen
		setBounds(MAIN_BOUNDS);
		// An die Komponentengrößen anpassen
        pack();
	}
}
