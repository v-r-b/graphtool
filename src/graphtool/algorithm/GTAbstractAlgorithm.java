package graphtool.algorithm;

import java.awt.Toolkit;

import graphtool.graph.GTGraph;
import graphtool.gui.GTGraphVisualization;
import graphtool.res.Messages;
import graphtool.utils.CodeExecutionHandler;
import graphtool.utils.CollectionObserver;
import graphtool.utils.TextComponentPrintStream;

/**
 * Basisklasse für die Algorithmen, die in diesem Programm Anwendung
 * finden können. Die abstrakte Klasse hält einige Informationen, die
 * von den Algorithmen benötigt werden und bereitet die Verwendung
 * des CodeExecutionHandlers vor.
 * <p>
 * Daneben sind hier einige abstrakte Methoden definiert, die durch
 * die Subklassen zu implementieren sind, da sie bei der Verwendung
 * der Algorithmen durch andere Klassen des Programms benötigt werden.
 * <p>
 * Der eigentliche Algorithmus wird in der {@link #run()}-Methode der
 * Subklasse implementiert.
 * run() wird nicht direkt aufgerufen, sondern über {@link Thread#start()}. 
 *
 * @see graphtool.utils.CodeExecutionHandler
 */
public abstract class GTAbstractAlgorithm implements Runnable {

	/** Visualisierungskomponente für den Graphen */
	protected GTGraphVisualization dgv = null;
	/** Zu bearbeitender Graph */
	protected GTGraph graph = null;

	/** Beobachter von Änderungen an Exemplaren von GTPriorityQueue und GTVertexInfoSet.
	 *  Kann in den Subklassen zur Erzeugung von Knotensammlungen verwendet werden */
	protected CollectionObserver<GTVertexInfo> collectionObserver = null;
	/** Möglichkeit, Meldungen auszugeben. Kann per showInfo()-Methode benutzt werden. */
	private TextComponentPrintStream infoOut = null;
	/** Steuerung der Programmausführung und -darstellung. Wird in den Implementierungen
	 *  der Subklassen verwendet. */
	protected CodeExecutionHandler cxh = null;
	/** Variablenname des CodeExecutionHandlers zur Verwendung in Subklassen */
	protected final static String CX_HANDLER_NAME = "cxh"; //$NON-NLS-1$
	/**
	 * Der angegebene reguläre Ausdruck wird dazu verwendet, die Aufrufe des
	 * CodeExecutionHandlers aus dem Quelltext des Algorithmus herauszufiltern.
	 * Er wird in {@link CodeExecutionHandler#beautifySourceFile(String, String)} benutzt.
	 * Konkret referenziert "cxh[^\\s]*[\\s]*" alle Textbestandteile, die mit "cxh" 
	 * (Variablenname des verwendeten CodeExecutionHandlers) beginnen und 
	 * sich zunächst mit beliebig vielen Non-Whitespace-Zeichen (^[\\s]*) und
	 * im direkten Anschluss mit beliebig vielen Whitespace-Zeichen ([\\s]*)
	 * fortsetzen, also den kompletten Aufruf bis zum nächsten Non-Whitespace-Zeichen.
	 */
	protected final static String CX_BEAUTYFY_REGEX = "cxh[^\\s]*[\\s]*";  //$NON-NLS-1$
	
	/**
	 * Teilschlüssel für den darzustellenden Namen des Algorithmus.
	 * @see #getName()
	 */
	public final static String ALG_NAME_KEY = ".Name"; //$NON-NLS-1$
	
	/** Meldung, dass der Algorithmus abgebrochen wurde. */
	public  final static String ALG_ABORTED_MSG = 
			Messages.getString("GTAbstractAlgorithm.AlgorithmAbortedMessage"); //$NON-NLS-1$

	/**
	 * Setzt die Visualisierungs- und Ausgabekomponenten sowie den Graphen und erzeugt einen
	 * CodeExecutionHandler (Feld cxh) zur schrittweisen Ausführung des Algorithmus.
	 * @param sourceFileEncoding Encoding, in dem die betrachtete Quelldatei vorliegt.
	 * @param sourceFileDir Verzeichnis mit den Quelldateien, sofern nicht im Klassenpfad zu finden.
	 * @param dgv Visualisierungskomponente, die auch eine Referenz auf den
	 *            Graphen liefert.
	 * @param co  CollectionObserver, der die vom Algorithmus verwendeten Knotensammlungen
	 *            vom Typ der inneren Klassen GTVertexInfoSet und GTPriorityQueue
	 *            auf Änderungen überwacht und dann jeweils darstellt. 
	 *            Darf null sein, wenn nicht benötigt.
	 * @param out Ausgabestream für Meldungen.  Darf null sein, wenn nicht benötigt.
	 * @see #showInfo(String, boolean)
	 * @see #getCodeExecutionHandler()
	 * @see GTPriorityQueue
	 * @see GTVertexInfo
	 */	
	public GTAbstractAlgorithm(String sourceFileEncoding, String sourceFileDir,
			GTGraphVisualization dgv, CollectionObserver<GTVertexInfo> co, TextComponentPrintStream out) {
		this.dgv = dgv;
		this.graph = dgv.getGraph();
		this.collectionObserver = co;
		this.infoOut = out;
		cxh = new CodeExecutionHandler(sourceFileEncoding, sourceFileDir, Messages.getTextProvider());
	}
	
	/**
	 * Zeigt den übergebenen Text an. Ist {@link #infoOut} == null, wird bei normalen
	 * Meldungen die Standardausgabe, bei Warnungen die Fehlerausgabe verwendet.
	 * @param text anzuzeigender Text
	 * @param isWarning gibt an, ob es sich um eine Wernung oder eine einfache Meldung handelt
	 * @see #infoOut
	 */
	public void showInfo(String text, boolean isWarning) {
		if (infoOut != null) {
			//infoOut.showInfo(text, isWarning);
			infoOut.clear();
			infoOut.println(text);
		}
		else if (isWarning) {
			System.err.println(text);
		}
		else {
			System.out.println(text);
		}
		if (isWarning) {
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	/**
	 * Liefert den für die Ausführungskontrolle zu verwendenden CodeExecutionHandler.
	 * @return CodeExecutionHandler
	 */
	public CodeExecutionHandler getCodeExecutionHandler() {
		return cxh;
	}
	
	/**
	 * Gibt an, ob der Algorithmus zum Laufen einen Startknoten benötigt.
	 * @return true, wenn ein Startknoten benötigt wird, false sonst.
	 */
	public abstract boolean needsStartNode();

	/**
	 * Gibt an, ob der Algorithmus zum Laufen einen Zielknoten benötigt.
	 * @return true, wenn ein Zielknoten benötigt wird, false sonst.
	 */
	public abstract boolean needsTargetNode();
	
	/**
	 * Liefert einen sprechenden Namen für den Algorithmus.
	 * <p>
	 * Die Standardimplementierung versucht, den Namen aus der Messages-Datei zu lesen,
	 * und zwar mit dem Schlüssel &lt;Klassenname (mit Package)&gt;{@value #ALG_NAME_KEY}.
	 * Ist dieser nicht vorhanden, liefert die Methode den Klassennamen (inkl. Package). 
	 * @return Anzuzeigender Name des Algorithmus
	 */
	public String getName() {
		String className = getClass().getName();
		String name = Messages.getString(className + ALG_NAME_KEY);
		return name != null ? name : className;
	}
	
}