package graphtool.utils;

import java.awt.Toolkit;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Die Klasse dient dazu, während der Ausführung von Programmcode an allen Stellen,
 * an denen ein Aufruf der Methode bp() steht, vorher definierte Aktionen auszuführen.
 * Diese Aktionen werden nach dem Erzeugen einer Instanz von CodeExecutionHandler mittels
 * addAction() registriert.
 * <p>
 * Der betrachtete Programmabschnitt befindet sich hierbei innerhalb einer Methode und
 * wird von den Aufrufen startCodeHandling() und endCodeHandling() eingefasst.
 * @see #bp()
 * @see #addAction(Action)
 * @see #startCodeHandling(boolean)
 * @see #endCodeHandling(boolean)
 */
public class CodeExecutionHandler {
	
	/**
	 * Interface zur Nachverfolgung des Starts und Endes des betrachteten Programmabschnitts.
	 * Genauer gesagt werden die registrieren Beobachter darüber informiert, dass
	 * eine der Methoden<ul>
	 * <li> {@link CodeExecutionHandler#startCodeHandling(boolean)} -&gt; methodStarted, 
	 * <li> {@link CodeExecutionHandler#endCodeHandling(boolean)} -&gt; methodTerminated oder 
	 * <li> {@link CodeExecutionHandler#stopExecution()} -&gt; methodTerminated 
	 * </ul>aufgerufen wurde.
	 * @see CodeExecutionHandler#addStartStopListener(StartStopListener) 
	 */
	public static interface StartStopListener {
		/** 
		 * Betrachteter Programmabschnitt wurde gestartet.
		 * @param methodName Name der Methode, aus der startCodeHandling() aufgerufen wurde
		 * @see CodeExecutionHandler#startCodeHandling(boolean) 
		 */
		public void codeSectionStarted(String methodName);
		/** 
		 * Betrachteter Programmabschnitt wurde beendet (oder abgebrochen).
		 * @param methodName Name der Methode, aus der endCodeHandling() oder stopExecution()
		 *                   aufgerufen wurde.
		 * @see CodeExecutionHandler#endCodeHandling(boolean) 
		 * @see CodeExecutionHandler#stopExecution() 
		 */
		public void codeSectionTerminated(String methodName);
	}
	
	/**
	 * Interface für nutzerdefinierte Aktionen. Diese Aktionen werden bei jedem
	 * Aufruf von bp() ausgeführt. Der zu implementierenden
	 * Methode accept(String, Integer) werden beim Aufruf der Text der aktuellen
	 * Codezeile sowie die Zeilennummer (1-basiert) als Parameter übergeben.
	 * Das Interface wurde nur zwecks besserer Lesbarkeit gegenüber dem parametrisierten
	 * Interface BiConsumer eingeführt.
	 * @see CodeExecutionHandler#addAction(Action)
	 * @see CodeExecutionHandler#bp()
	 */
	public static interface Action extends BiConsumer<String,Integer> {
		// Die Methode wird nur zu Dokumentationszwecken aufgeführt 
		/**
		 * Aktion, die über addAction() registriert wird und von bp() aufgerufen werden soll.
		 * @param lineOfCode aktuelle Quellcodezeile
		 * @param lineNum1based aktuelle Zeilennummer (1-basiert)
		 * @see CodeExecutionHandler#addAction(Action)
		 * @see CodeExecutionHandler#bp()
		 */
		@Override
		void accept(String lineOfCode, Integer lineNum1based);
	}

	/** Verzeichnis mit den Quelldateien, sofern nicht im Klassenpfad zu finden. */
	private String sourceFileDir = null;
	/** Encoding der Quelldateien */
	private String sourceFileEncoding = null;
	
	// Textersetzungen, die über die textProvider-Funktion im Konstruktor vorgenommen werden.
	/** Textersetzung für startMethod(...) in der Quellcodeanzeige (Schlüsselwert) */
	public static final String START_CODE_REPLACEMENT_KEY = "CodeExecutionHandler.StartCodeReplacement"; //$NON-NLS-1$
	/** Textersetzung für startMethod(...) in der Quellcodeanzeige */
	private String startCodeReplacement = "BEGINN"; //$NON-NLS-1$
	
	/** Textersetzung für endMethod(...) in der Quellcodeanzeige (Schlüsselwert) */
	public static final String END_CODE_REPLACEMENT_KEY = "CodeExecutionHandler.EndCodeReplacement"; //$NON-NLS-1$
	/** Textersetzung für endMethod(...) in der Quellcodeanzeige */
	private String endCodeReplacement = "ENDE";  //$NON-NLS-1$
	
	/** Fehlermeldung bei nicht gefundener Quelldatei (Schlüsselwert) */
	public static final String ERROR_CANNOT_LOAD_SOURCEFILE_KEY = "CodeExecutionHandler.ErrorCannotLoadSourceFile_fmt_s"; //$NON-NLS-1$
	/** Fehlermeldung bei nicht gefundener Quelldatei */
	private String errorCannotLoadSourceFile = "Kann Quelldatei %s nicht laden!"; //$NON-NLS-1$ 
	
	/** Fehlermeldung bei nicht gefundener Markierung des Codeabschnitts  (Schlüsselwert) */
	public static final String ERROR_NO_MARKERS_IN_SOURCEFILE_KEY = "CodeExecutionHandler.ErrorNoMarkersInSourceFile"; //$NON-NLS-1$
	/** Fehlermeldung bei nicht gefundener Markierung des Codeabschnitts */
	private String errorNoMarkersInSourceFile = "Methode ist nicht ordnungsgemäß markiert!"; //$NON-NLS-1$ 
	
	/** Fehlermeldung bei nicht gefundener Markierung des Codeabschnitts oder fehlendem Pseudocode (Schlüsselwert) */
	public static final String ERROR_NO_MARKERS_OR_NO_PCODE_IN_SOURCEFILE_KEY = "CodeExecutionHandler.ErrorNoMarkersOrNoPCodeInSourceFile"; //$NON-NLS-1$
	/** Fehlermeldung bei nicht gefundener Markierung des Codeabschnitts oder fehlendem Pseudocode */
	private String errorNoMarkersOrNoPCodeInSourceFile = "Methode ist nicht ordnungsgemäß markiert oder enthält keinen Pseudocode!"; //$NON-NLS-1$ 
	
	/** Ersetzung bei nicht gefundenem Quelltext in der angegebenen Zeile (Schlüsselwert) */
	public static final String LINE_NOT_AVAILABLE_KEY = "CodeExecutionHandler.LineNotAvailable"; //$NON-NLS-1$
	/** Ersetzung bei nicht gefundenem Quelltext in der angegebenen Zeile */
	private String lineNotAvailable = "<nicht verfügbar>"; //$NON-NLS-1$ 
	
	
	/** Tag für Pseudocode-Zeilen. Diese werden als Kommentare in den Java-Code notiert */
	public static final String PCODE_TAG = "// @pcode"; //$NON-NLS-1$
	/** Markierung für den Start des betrachteten Codes */
	public static final String START_CODE_TAG = "startCodeHandling"; //$NON-NLS-1$
	/** Markierung für das Ende des betrachteten Codes */
	public static final String END_CODE_TAG = "endCodeHandling"; //$NON-NLS-1$
	
	/** Name der ausgeführten Methode; wird bei startCodeHandling festgestellt */
	private String methodName;
	/** Erste Zeile des betrachteten Codebereichs (Aufrufstelle von startCodeHandling()). */
	private int javaCodeFirstLine = -1;
	/** Letzte Zeile des betrachteten Codebereichs (Aufrufstelle von endCodeHandling()). */
	private int javaCodeLastLine = -1;
	/** Inhalt der Quelldatei (eine Zeile = ein Element) */
	private String[] sourceFile = {};
	/** Beobachter des Starts und des Endes der betrachteten Codebereichs */
	private ArrayList<StartStopListener> startStopListeners = new ArrayList<>();
	/** Registrierte Aktionen zum Aufruf durch {@link #bp()} */
	private ArrayList<Action> userActions = new ArrayList<>();

	/**
	 * Erzeugt einen CodeExecutionHandler mit vom Standard abweichenden Einstellungen.
	 * Die als drittes Argument erwartete Funktion soll in der Lage sein, zu den auf "_KEY" 
	 * endenden Konstanten als Schlüsselwerte (z.B. START_CODE_REPLACEMENT_KEY) jeweils
	 * einen Ersetzungstext zu liefern. Auf diese Weise können die Texte, die CodeExecutionHandler
	 * verwendet, externalisiert werden, ohne dass die Klasse für die Externalisierung hier
	 * bekannt sein muss. Wird null übergeben, wird nicht versucht, die Texte abzufragen.
	 * Stattdessen werden Standardwerte verwendet (z.B. startCodeReplacement).
	 * @param sourceFileEncoding Encoding, in dem die betrachtete Quelldatei vorliegt 
	 * 							 oder null (dann wird das Standard-Encoding der Plattform verwendet).
	 * @param sourceFileDir Verzeichnis mit den Quelldateien, sofern nicht im Klassenpfad zu finden 
	 * 						oder null (dann wird das aktuelle Verzeichnis verwendet).
	 * @param textProvider Funktion, die zu einem Schlüssel eine Zeichenkette liefert, 
	 * 					   oder null (dann werden die Standardtexte verwendet).
	 * @see #startCodeReplacement
	 * @see #START_CODE_REPLACEMENT_KEY
	 */
	public CodeExecutionHandler(String sourceFileEncoding, String sourceFileDir, 
								Function<String, String> textProvider) {
		// Wenn ein Encoding angegeben wurde und dieses unterstützt wird, merken.
		if ((sourceFileEncoding != null) && Charset.isSupported(sourceFileEncoding)) {
				this.sourceFileEncoding = sourceFileEncoding;
		}
		// Sonst Standard-Encoding verwenden
		else {
			this.sourceFileEncoding = Charset.defaultCharset().name();
		}
		// Wenn ein Verzeichnis für die Quelldateien angegeben wurde, merken.
		if (sourceFileDir != null) {
			this.sourceFileDir = sourceFileDir;
		}
		// Sonst das aktuelle Verzeichnis verwenden
		else {
			this.sourceFileDir = System.getProperty("user.dir"); //$NON-NLS-1$
		}
		// Wenn eine Textfunktion bereitgestellt wurde, ...
		if (textProvider != null) {
			// Für alle Schlüssel prüfen, ob ein Wert vorhanden ist.
			// Wenn ja, zuweisen. Wenn nein, Standardwert belassen.
			startCodeReplacement = getValue(textProvider, START_CODE_REPLACEMENT_KEY, startCodeReplacement);
			endCodeReplacement = getValue(textProvider, END_CODE_REPLACEMENT_KEY, endCodeReplacement);
			errorCannotLoadSourceFile = getValue(textProvider, ERROR_CANNOT_LOAD_SOURCEFILE_KEY, errorCannotLoadSourceFile);
			errorNoMarkersInSourceFile = getValue(textProvider, ERROR_NO_MARKERS_IN_SOURCEFILE_KEY, errorNoMarkersInSourceFile);
			errorNoMarkersOrNoPCodeInSourceFile = getValue(textProvider, ERROR_NO_MARKERS_OR_NO_PCODE_IN_SOURCEFILE_KEY, errorNoMarkersOrNoPCodeInSourceFile);
			lineNotAvailable = getValue(textProvider, LINE_NOT_AVAILABLE_KEY, lineNotAvailable);
		}
	}
	
	/**
	 * Ermittelt mithilfe der angegebenen Funktion den Text zum Schlüsselwert. Ist ein solcher
	 * Text verfügbar, wird der zurückgegeben. Wenn nein (Funktion liefert null), wird der als
	 * dritter Parameter angegebene Standardwert zurückgegeben.
	 * @param textProvider Funktion zum Ermitteln eines Textwerts bei Angabe eines Schlüsselwerts.
	 * @param key Schlüsselwert, der der Funktion bereitgestellt wird.
	 * @param defaultValue Standardwert für die Rückgabe.
	 * @return Ergebnis der Funktion (wenn != null) oder Standardwert.
	 */
	private static String getValue(Function<String, String> textProvider, String key, String defaultValue) {
		String value = textProvider.apply(key);
		return value != null ? value : defaultValue;
	}
	
	/**
	 * Die Quelldatei der Klasse, aus der der Aufruf dieser Methode kam, 
	 * wird ausfindig gemacht und mittels loadSourceFile() geladen. 
	 * <p>
	 * Sofern ein regulärer Ausdruck angegeben ist, werden entsprechende 
	 * Verschönerungsmaßnahmen am Quelltext durchgeführt (siehe beautifySourceFile()).
	 * @param handlerName Variablenname des in der Quelldatei verwendeten
	 *                    CodeExecutionHandlers. Dieser Name steht dort vor
	 *                    jedem Aufruf einer Methode von CodeExecutionHandler,
	 *                    also z.B. cxh.startCodeHandling(true) oder cxh.bp(). Im
	 *                    Beispiel ist "cxh" dann der handlerName.
	 * @param regex Regulärer Ausdruck zur Verschönerung des Quelltexts
	 * @return true bei Erfolg; false, wenn die Quelldatei nicht geladen werden konnte.
	 * @see #loadSourceFile(String, String, String)
	 * @see #beautifySourceFile(String, String)
	 */
	public boolean prepareSourceCode(String handlerName, String regex) {
		// Infos zur aufrufenden Methode aus dem Stack besorgen
		StackTraceElement ste = getCallersStackTraceElement();
		// Dazugehörige Quelldatei laden
		if (!loadSourceFile(ste.getClassName(), ste.getFileName(), handlerName)) {
			Toolkit.getDefaultToolkit().beep();
			System.err.printf(errorCannotLoadSourceFile + '\n', ste.getFileName());
			return false;
		}
		// Quelldatei ggf. verschönern.
		if (regex != null) {
			beautifySourceFile(handlerName, regex);
		}
		return true;
	}

	/**
	 * Der Aufruf dieser Methode markiert den Beginn des zu betrachtenden Codeabschnitts.
	 * Die registrieren Beobachter (StartStopListeners) werden informiert. 
	 * Wird true als Parameter übergeben, so wird vor der Information der
	 * Beobachter noch bp() aufgerufen.
	 * @param isBreakpoint Gibt an, ob bp() aufgerufen werden soll.
	 * @see #addStartStopListener(StartStopListener)
	 * @see #bp()
	 */
	public void startCodeHandling(boolean isBreakpoint) {
		// Infos zur aufrufenden Methode aus dem Stack besorgen
		StackTraceElement ste = getCallersStackTraceElement();
		// Namen der Methode speichern.
		methodName = ste.getMethodName();
		// Falls isBreakpoint == true -> bp()-Methode aufrufen
		if (isBreakpoint) {
			bp();
		}
		// Alle Beobachter aufrufen, die sich registriert haben
		notifyListeners(true);
	}

	/**
	 * Der Aufruf dieser Methode markiert das Ende des zu betrachtenden Codeabschnitts.
 	 * Die registrieren Beobachter (StartStopListeners) werden informiert. 
	 * Wird true als Parameter übergeben, so wird vor der Information der
	 * Beobachter noch bp() aufgerufen.
	 * @param isBreakpoint Gibt an, ob bp() aufgerufen werden soll.
	 * @see #addStartStopListener(StartStopListener)
	 * @see #bp()
	 */
	public void endCodeHandling(boolean isBreakpoint) {
		// Falls isBreakpoint == true -> bp()-Methode aufrufen
		if (isBreakpoint) {
			bp();
		}
		// Alle Beobachter aufrufen, die sich registriert haben
		notifyListeners(false);
	}
	
	/**
	 * Dient dazu zu signalisieren, dass der betrachtete Codeabschnitt 
	 * durch Abbruch beendet wurde. Die Beobachter werden hier,
	 * wie bei endMethod(), über das Ende informiert. 
	 * bp() wird nicht aufgerufen.
	 * @see #addStartStopListener(StartStopListener)
	 */
	public void stopExecution() {
		// Alle Beobachter aufrufen, die sich registriert haben
		notifyListeners(false);
	}
	
	/** 
	 * Alle registrieren nutzerdefinierten Aktionen verwerfen. 
	 * @see #addAction(Action)
	 */
	public void clearActions() {
		userActions.clear();
	}
	
	/**
	 * Nutzerdefinierte Aktion zum Aufruf durch bp() registrieren.
	 * @param action Zu registrierende Aktion.
	 * @see #bp()
	 */
	public void addAction(Action action) {
		userActions.add(action);
	}

	/**
	 * Liefert den Namen der Methode, in der startCodeHandling() 
	 * aufgerufen wurde.
	 * @return Namen der Methode
	 * @see #startCodeHandling(boolean)
	 */
	public String getCodeSectionMethodName() {
		return methodName;
	}
	
	/**
	 * Liefert die Zeilennummer der Zeile in der betrachteten Methode,
	 * in der der startCodeHandling()-Aufruf steht.
	 * @return Zeilennummer des startCodeHandling()-Aufrufs in der betrachteten Methode.
	 * @see #startCodeHandling(boolean)
	 * @see #prepareSourceCode(String, String)
	 */
	public int getCodeSectionLineOffset() {
		return javaCodeFirstLine;
	}
	
	/**
	 * Liefert zur 1-basierten Zeilennummer einer Codezeile im Quelltext eine ebenfalls
	 * 1-basierte Zeilennummer innerhalb des betrachteten Codes.
	 * @param javaCodeLineNumAbs Zeilennummer in der Quelldatei (1-basiert)
	 * @return Zeilennummer im Java-Code des Code-Abschnitts (1-basiert)
	 */
	public int getLineNumInCodeSection(int javaCodeLineNumAbs) {
		return javaCodeLineNumAbs - javaCodeFirstLine + 1;
	}
	
	/**
	 * Liefert zur 1-basierten Zeilennummer einer Codezeile im Quelltext eine ebenfalls
	 * 1-basierte Zeilennummer innerhalb des betrachteten Pseudocodes.
	 * @param javaCodeLineNumAbs Zeilennummer in der Quelldatei (1-basiert)
	 * @return Zeilennummer im Pseudo-Code des Code-Abschnitts.
	 */
	public int getLineNumInPseudoCode(int javaCodeLineNumAbs) {
		int pCodeLineNum = 0;	// die erste "@pcode"-Zeile hat dann die Nummer 1
		
		// Vom Beginn der Methode an die PCODE_TAG-Zeilen zählen
		// bis zur übergebenen Zeile javaCodeLineNumAbs
		for (int line = javaCodeFirstLine; line <= javaCodeLineNumAbs; line++) {
			// Die Zeilennummern sind 1-basiert, das Array 0-basiert, daher "line-1"
			if (sourceFile[line-1].trim().startsWith(PCODE_TAG)) {
				pCodeLineNum++;
			}
		}
		return pCodeLineNum;
	}

	/**
	 * Liefert einen String mit so vielen Tabs, wie mindestens vor jeder Zeile des
	 * betrachteten Codes stehen. Wird von den Methoden verwendet, die den betrachteten
	 * Java- bzw. Pseudocode liefern.
	 * @return String mit "führenden" Tabs.
	 * @see #getCodeSectionJavaCode()
	 * @see #getCodeSectionPseudoCode()
	 */
	private String getCommonLeadingTabs() {
		// Führende Tabs; initial leer
		StringBuilder leadingTabs = new StringBuilder();

		// Falls die Felder für die erste Zeile und die letzte Zeile korrekt gesetzt wurden, ...
		if ((javaCodeFirstLine > -1 && javaCodeLastLine > -1) && (javaCodeFirstLine < javaCodeLastLine)) {
			// Zunächst feststellen, wie weit der gesamte Quelltext mit Tabulatorzeichen 
			// eingerückt ist.
			
			// Mindestanzahl der führenden Tabs mit -1 initialisieren
			int minTab = -1;
			// Quelltext durchgehen und auf Tabs untersuchen
			// Die Zeilennummern sind 1-basiert, das Array 0-basiert, daher "line-1"
			for (int line = javaCodeFirstLine; line <= javaCodeLastLine; line++) {
				String aLine = sourceFile[line-1];
				if (aLine.length() > 0) {
					// Anzahl führender Tabs in der aktuellen Zeile bestimmen
					int actTab = 0;
					while (actTab < aLine.length() && aLine.charAt(actTab) == '\t') {
						actTab++;
					}
					// minTab setzen, wenn es zuvor noch nie gesetzt wurde oder
					// wenn eine Zeile gefunden wurde, die weniger weit eingerückt
					// war als die vorhergehenden Zeilen.
					if (minTab == -1 || actTab < minTab) {
						minTab = actTab;
					}
				}
			}
			// minTab gibt jetzt an, wie viele führende Tabs in *jeder* Zeile des
			// Quellcodes der betrachteten Methode *mindestens* zu finden sind.

			// String zusammenbauen, der aus genau minTabs Tabs besteht
			for (int i = 0; i < minTab; i++) {
				leadingTabs.append('\t');
			}
		}
		return leadingTabs.toString();
	}
	
	/**
	 * Liefert den Quelltext des betrachteten Codebereichs. Führende Tabs werden
	 * so weit entfernt, dass die am wenigsten eingerückte Zeile des SourceCodes
	 * keine führenden Tabs mehr hat.
	 * @return Quelltext des betrachteten Codebereichs
	 * @see #prepareSourceCode(String, String)
	 * @see #getCommonLeadingTabs()
	 */
	public String getCodeSectionJavaCode() {
		// Quelltext zunächst als leeren String initialisieren
		StringBuilder source = new StringBuilder();

		// Falls die Felder für die erste Zeile und die letzte Zeile korrekt gesetzt wurden, ...
		if ((javaCodeFirstLine > -1 && javaCodeLastLine > -1) && (javaCodeFirstLine < javaCodeLastLine)) {
			String leadingTabs = getCommonLeadingTabs();
			
			// Quelltext der Methode durchgehen und die gemeinsamen
			// führenden Tabs aus jeder Zeile entfernen. Dabei wir der String
			// source aufgebaut, der den so angepassten Quelltext der Methode
			// enthält. Die Zeilen sind darin mit newlines getrennt.
			// Die Zeilennummern sind 1-basiert, das Array 0-basiert, daher "line-1"
			for (int line = javaCodeFirstLine; line <= javaCodeLastLine; line++) {
				source.append(sourceFile[line-1].replaceFirst(leadingTabs, "")); //$NON-NLS-1$
				source.append('\n');
			}
			return source.toString();
		}
		// ...ansonsten eine Fehlermeldung als "Quelltext" zurückgeben.
		return errorNoMarkersInSourceFile;
	}
	

	/**
	 * Liefert den Pseudocode des betrachteten Codebereichs. Dieser besteht aus
	 * allen Kommentarzeilen innerhalb dieses Bereichs, die mit PCODE_TAG beginnen. 
	 * @return Pseudocode des betrachteten Codebereichs
	 * @see #prepareSourceCode(String, String)
	 * @see #PCODE_TAG
	 */
	public String getCodeSectionPseudoCode() {
		// Quelltext zunächst als leeren String initialisieren
		StringBuilder source = new StringBuilder();

		// Falls die Felder für die erste Zeile und die letzte Zeile korrekt gesetzt wurden, ...
		if ((javaCodeFirstLine > -1 && javaCodeLastLine > -1) && (javaCodeFirstLine < javaCodeLastLine)) {

			String leadingTabs = getCommonLeadingTabs();

			// Quelltext der Methode durchgehen und die gemeinsamen
			// führenden Tabs aus jeder Zeile entfernen. Dabei wird der String
			// source aus allen Zeilen aufgebaut, die Pseudo-Code enthalten.
			// Die Zeilen sind darin mit newlines getrennt.
			// Die Zeilennummern sind 1-basiert, das Array 0-basiert, daher "line-1"
			for (int line = javaCodeFirstLine; line <= javaCodeLastLine; line++) {
				String aLine = sourceFile[line-1].replaceFirst(leadingTabs, ""); //$NON-NLS-1$
				if (aLine.trim().startsWith(PCODE_TAG)) {
					// Text "// @pcode" nebst nachfolgendem Whitespace löschen
					// und Pseudocode-Zeile nebst newline hinzufügen
					source.append(aLine.replaceFirst(PCODE_TAG + "[\\s]* ", "")); //$NON-NLS-1$ //$NON-NLS-2$
					source.append('\n');
				}
			}
			return source.toString();
		}
		// ...ansonsten eine Fehlermeldung als "Quelltext" zurückgeben.
		return errorNoMarkersOrNoPCodeInSourceFile;
	}
	
	/**
	 * Ruft alle registrieren Aktionen auf und übergibt diesen als Parameter
	 * die aktuelle Quellcodezeile sowie die aktuelle Zeilennummer (1-basiert). Diese Methode kann
	 * genutzt werden, wenn sich die Ausführung in derselben Methode befindet, in der
	 * der startCodeHandling()-Aufruf getätigt wurde (aber z.B. nicht in anonymen Klassen).
	 * Die Aktionen werden nacheinander in der Reihenfolge ihrer Registierung aufgerufen.
	 * @see #bp(String)  
	 * @see #addAction(Action)
	 */
	public void bp() {
		bp(methodName);
	}
	
	/**
	 * Ruft alle registrieren Aktionen auf und übergibt diesen als Parameter
	 * die aktuelle Quellcodezeile sowie die aktuelle Zeilennummer (1-basiert). Diese Methode kann
	 * genutzt werden, wenn sich die Codeausführung in einer anderen als derjenigen
	 * Methode befindet, von der aus startCodeHandling() aufgerufen wurde, beispielsweise bei der
	 * Verwendung von anonymen Klassen. 
	 * Der Methodenname wird zur Suche des richtigen Eintrags im StackTrace benötigt.
	 * @param methodName Name der Methode, in der der bp()-Aufruf steht.
	 * @see #addAction(Action)
	 */
	public void bp(String methodName) {
		if (userActions != null) {
			// richtige Stelle im StackTrace suchen
			StackTraceElement ste = findStackTraceElement(methodName);
			// Alle registrieren Aktionen für diese Stelle ausführen
			for (Action a : userActions) {	
				a.accept(getJavaSourceCode(ste.getLineNumber()), Integer.valueOf(ste.getLineNumber()));
			}
		}
	}
	
	/**
	 * Ruft eine Methode caller() eine andere Methode callee() auf, so kann
	 * die Methode callee() durch Aufruf von getCallersStackTraceElement()
	 * den Eintrag im StackTrace erhalten, der zu ihrem Aufruf in caller() gehört. 
	 * Es handelt sich hierbei um den Eintrag an Position 3 des Stacks.
	 * Wird von prepareSourceCode und startCodeHandling benötigt.
	 * @return Stack-Eintrag des Aufrufs von callee() in caller()
	 * @see #prepareSourceCode(String, String)
	 * @see #startCodeHandling(boolean)
	 */
	private static StackTraceElement getCallersStackTraceElement() {
		// Das Element an Stelle 3 gib die Methode an, aus der der Aufruf
		// derjenigen Methode kam, die getCallersStackTraceElement() nutzt.
		return Thread.currentThread().getStackTrace()[3];
	}

	/**
	 * Sucht im StackTrace so lange, bis zum ersten Mal der als Parameter übergebene Name 
	 * als Methodenname gefunden wird und gibt dieses Element aus dem StackTrace zurück.
	 * Wird von bp(String) benötigt.
	 * @param methodName der im StackTrace zu suchenden Methode.
	 * @return Zur aktuellen Aufrufstelle von passendes StackTrace-Element
	 * @see #bp(String)
	 */
	private static StackTraceElement findStackTraceElement(String methodName) {
		// aktuellen StackTrace besorgen.
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		
		// Durchgehen und bei Fund des gesuchten Methodennamens Element zurückgeben
		for (StackTraceElement ste : stackTrace) {
			if (ste.getMethodName().equals(methodName)) {
				return ste;
			}
		}
		// Wenn nicht gefunden (sollte nicht vorkommen), null zurückgeben.
		return null;
	}
	
	
	/**
	 * Liefert die zur Zeilennummer passende Zeile aus dem Sourcecode.
	 * @param lineNumberAbs 1-basierte Zeilennummer
	 * @return zugehörige Java-Codezeile
	 */
	private String getJavaSourceCode(int lineNumberAbs) {
		// Array ist 0-basiert, Zeilennummer 1-basiert
		lineNumberAbs--;
		
		// Wenn die Zeilennummer ungültig ist, Fehlertext zurückgeben
		if ((lineNumberAbs < 0) || (lineNumberAbs > sourceFile.length)) {
			return lineNotAvailable;
		}
		// passende Zeile liefern
		return sourceFile[lineNumberAbs];
	}
	
	/**
	 * Lädt die Quelldatei. Das Array sourceFile[] ist im Anschluss entweder mit dem
	 * Quellcode gefüllt oder es handelt sich um ein leeres Array. Wird ein Wert für
	 * den Parameter className angegeben, so wird zunächst versucht, den Quelltext im
	 * Klassenpfad zu finden. Wurde null als className angegeben oder gelingt es nicht,
	 * die Quelldatei im Klassenpfad zu finden, so wird versucht, den als fileName
	 * übergebenen Dateinamen zu verwenden. Die Datei wird in dem Verzeichnis gesucht,
	 * das in der Konfigurationsdatei unter dem Schlüssel "SourcePath" hinterlegt ist.
	 * Es muss also mindestens einer der Parameter className oder fileName sinnvoll
	 * übergeben werden.
	 * 
	 * @param className Name der Klasse, deren Quelltext geladen werden soll
	 * @param fileName Dateiname der Quelldatei, die geladen werden soll.
	 * @param handlerName zur Erklärung siehe {@link #prepareSourceCode(String, String)}
	 * @return true bei Erfolg, false sonst.
	 */
	@SuppressWarnings("resource")
	private boolean loadSourceFile(String className, String fileName, String handlerName) {
		// Hier ist die Quelldatei zu finden (Pfad)
		String sourceFilePath = null;
		// Scanner zum zeilenweisen Einlesen der Quelldatei
		Scanner scanner = null;
		// Inhalt der Quelldatei (als leere Liste initialisiert)
		ArrayList<String> contents = new ArrayList<>();
		
		// Wurde ein Klassenname angegeben, wird zunächst im Klassenpfad nach der 
		// Quelldatei gesucht.
		if (className != null) {
			// Mehrteiliger Klassenname (z.B. "graphtool.alrorithm.DijkstraAlgorithm") wird
			// umgewandelt in Pfadnamen (z.B. "/graphtool/algorithm/DijkstraAlgorithm.java").
			sourceFilePath = '/'+ className.replace('.', '/') + ".java"; //$NON-NLS-1$
			// Dann wird versucht, diese Datei zu öffnen. 
			try {
				scanner = new Scanner(getClass().getResourceAsStream(sourceFilePath), sourceFileEncoding);
			} catch (Exception e) {
				if (scanner != null) {
					scanner.close();
				}
				scanner = null;
			}
		}
		
		// Wurde keine Quelldatei im Klassenpfad geöffnet und ist fileName ein gültiger String...
		if ((scanner == null) && (fileName != null)) {
			// dann wird der Pfad zur Datei gebildet aus dem in der Konfigurationsdatei
			// voreingestellten Verzeichnispfad plus dem Dateinamen.
			if (sourceFileDir != null) {
				// Pfadnamen zusammensetzen und versuchen, die Datei zu öffnen
				if (sourceFileDir.endsWith(File.separator)) {
					sourceFilePath = sourceFileDir + fileName;
				}
				else {
					sourceFilePath = sourceFileDir + File.separator + fileName;
				}
				try {
					scanner = new Scanner(new File(sourceFilePath), sourceFileEncoding);
				} catch (Exception e) {
					if (scanner != null) {
						scanner.close();
					}
					scanner = null;
				}
			}
		}

		// Hat scanner an dieser Stelle noch keinen gültigen Wert, konnte die Quelldatei
		// nicht geöffnet werden. Die Methde wird abgebrochen.
		if (scanner == null) {
			return false;
		}

		// Codezeilen beim Einlesen in eine ArrayList schreiben, dabei
		// Anfang und Ende der zu betrachtenden Methode suchen und diese in den
		// Feldern javaCodeFirstLine und javaCodeLastLine speichern.
		for (int lineNum = 1; scanner.hasNext(); lineNum++) {
			String line = scanner.nextLine();
			if (line.trim().startsWith(handlerName + '.' + START_CODE_TAG + '(')) {
				javaCodeFirstLine = lineNum;
			}
			else if (line.trim().startsWith(handlerName + '.' + END_CODE_TAG + '(')) {
				javaCodeLastLine = lineNum;
			}
			contents.add(line);
		}
		// Datei schließen und Inhalte in ein Array vom Typ String[] übertragen.
		scanner.close();
		sourceFile = contents.toArray(new String[] {});
		// Ist mindestens eine Zeile eingelesen worden, meldet die Methode Erfolg.
		return contents.size() > 0;
	}
	
	/**
	 * Passt die eingelesene Quelldatei zur besseren Darstellung an. Für alle geladenen 
	 * Zeilen wird eine Ersetzung String.replaceAll(regex, "") durchgeführt, d.h. der
	 * angegebene Ausdruck wird in jeder einzelnen Zeile gelöscht, sofern vorhanden.
	 * Auf diese Weise können z.B. die Aufrufe der Methoden des CodeExecutionHandlers
	 * aus dem Quelltext entfernt werden.
	 * <p> 
	 * Beispiel: Heißt die verwendete Instanz des CodeExecutionHandlers "cxh", 
	 * so löscht der Aufruf <code>aString.replaceAll("cxh[^\\s]*[\\s]*");", "");</code>
	 * alle mit "cxh" beginnenden Aufrufe (a) bis zum nächsten Whitespace und 
	 * (b) darüber hinaus bis zum nächsten Non-Whitespace.
	 * <p>
	 * Von: <code>cxh.bp(someParameter); someCall(someOtherParameter); // comment</code><br>
	 * bleibt dann nur: <code>someCall(someOtherParameter); // comment</code>.
	 * <p>
	 * Außerdem wird der Aufruf von startCodeHandling() durch {@link #startCodeReplacement} 
	 * und der Aufruf von endCodeHandling() durch {@link #endCodeReplacement} ersetzt.
	 * 
	 * @param handlerName Name der Instanz des CodeExecutionHandlers (s.o.)
	 * @param regex Regulärer Ausdruck zur Entfernung in den eingelesenen Zeilen.
	 */
	private void beautifySourceFile(String handlerName, String regex) {
		// Alles nach dem Methodennamen, bis zum Ende der Zeile:
		String methodCallRegex = "\\(.*"; //$NON-NLS-1$
		
		// Die Quelldatei zeilenweise durchgehen 
		for (int i = 0; i < sourceFile.length; i++) {
			String aLine = sourceFile[i];
			// startMethod-Aufrufe durch "BEGINN" ersetzen
			aLine = aLine.replaceFirst(
					handlerName + '.' + START_CODE_TAG + methodCallRegex, 
					startCodeReplacement);
			// endMethode-Aufrufe durch "ENDE" ersetzen
			aLine = aLine.replaceFirst(
					handlerName + '.' + END_CODE_TAG + methodCallRegex,
					endCodeReplacement);
			// Handler-Aufrufe entfernen und neuen Text zurückspeichern
			sourceFile[i] = aLine.replaceAll(regex, ""); //$NON-NLS-1$
		}
	}
	
	/**
	 * Einen Beobachter hinzufügen, der informiert werden möchte, wenn die
	 * betrachtete Methode gestartet (Aufruf von startCodeHandling) oder beendet
	 * wurde (Aufruf von endCodeHandling).
	 * @param l Beobachter.
	 * @see #notifyListeners(boolean)
	 * @see #startCodeHandling(boolean)
	 * @see #endCodeHandling(boolean)
	 */
	public void addStartStopListener(StartStopListener l) {
		startStopListeners.add(l);
	}
	
	/**
	 * Liste der Beobachter leeren.
	 * @see #addStartStopListener(StartStopListener)
	 */
	public void clearStartStopListeners() {
		startStopListeners.clear();
	}
	
	/**
	 * Alle Beobachter informieren, die wissen möchten, wenn die
	 * betrachtete Methode gestartet (Aufruf von startMethod) oder beendet
	 * wurde (Aufruf von endMethod).
	 * @param onStart gibt an, ob die Benachrichtigung anlässlich des Starts
	 *                (onStart = true) oder des Endes (onStart = false) erfolgt.
	 * @see StartStopListener#codeSectionStarted(String)
	 * @see StartStopListener#codeSectionTerminated(String)
	 */
	public void notifyListeners(boolean onStart) {
		for (StartStopListener l : startStopListeners) {
			if (onStart) {
				l.codeSectionStarted(methodName);
			}
			else {
				l.codeSectionTerminated(methodName);
			}
		}
	}
}
