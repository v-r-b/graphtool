package graphtool.utils;

import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

/**
 * Komponente zur Anzeige einer HTML-Datei. Sie unterstützt lokale Links. 
 */
public class HTMLEditorPane extends JEditorPane implements HyperlinkListener {

	private static final long serialVersionUID = 4282692613201564267L;

	/** Anzeigedokument für eine nicht gefundene Resource */
	@SuppressWarnings("nls")
	public static final String DOCUMENT_404 =	"<html>" +
													"<head>" + 
														"<title>Error</title>" +
													"</head>" +
													"<body>" +
														"404 - not found!" +
													"</body>" +
												"</html>";
	
	/**
	 * Erzeugt eine JEditorPane mit dem Inhalt der angegebenen HTML-Datei.
	 * Die JEditorPane ist nicht editierbar und dient nur zur Anzeige der Datei.
	 * @param pathname Pfad zur HTML-Datei (wird im Klassenpfad gesucht)
	 */
	@SuppressWarnings("nls")
	public HTMLEditorPane(String pathname) {
		// Nicht editierbare HTML-Anzeige 
		setEditable(false);
		setContentType("text/html");
		URL myURL= getClass().getClassLoader().getResource(pathname);
		try {
			// HTML-Dokument lesen
			setPage(myURL);
			// Links behandeln
			addHyperlinkListener(this);
		} 
		catch (IOException e) {
			// Bei Fehler eine 404-Seite anzeigen
			setText(DOCUMENT_404);
		}
	}

	/**
	 * Liefert den Titel der geladenen HTML-Seite, falls vorhanden.
	 * @return Seitentitel oder null.
	 * @implNote Das Laden der Seite im Konstruktor erfolgt asynchron. Das bedeutet,
	 * dass der Titel nicht unmittelbar nach der Erzeugung der HTMLEditorPane
	 * ausgelesen werden kann, sondern erst, wenn das Laden der Seite abgeschlossen ist.
	 * Dies ist daran zu erkennen, dass die Komponente einen PropertyChangeEvent "page"
	 * feuert. Innerhalb der Eventbehandlung ist der frühestmögliche Zeitpunkt, auf
	 * den Titel zuzugreifen.
	 */
	public String getPageTitle() {
		try {
			HTMLDocument doc = (HTMLDocument)getDocument();
			return (String)doc.getProperty(HTMLDocument.TitleProperty);
		}
		catch (ClassCastException | NullPointerException e) {
			return null;
		}
	}

	/**
	 * Erlaubt die Verfolgung von Links innerhalb des gegebenen Dokuments.
	 * @param evt Hyperlink-Event. Ausgewertet wird der Typ {@link HyperlinkEvent.EventType#ACTIVATED}.
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent evt) {
        try {
        	// Wenn ein Link verfolgt werden soll, ...
        	if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        		// ... der in dieser EditorPane angeklickt wurde ...
        		if (this.equals(evt.getSource()) &&
        				// ... und der sich von der aktuellen URL nur durch die Referenz unterscheidet ...
        				getPage().getPath().equals(evt.getURL().getPath())) {

        			// Dann zur Referenz springen
        			scrollToReference(evt.getURL().getRef());
        		}
        	}
        } catch (Exception e) {
        	// ignorieren, da die Verfolgung von Hyperlinks nicht wesentlich ist
        }
	}
}