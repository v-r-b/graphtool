package graphtool.gui.components;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import graphtool.algorithm.GTVertexInfo;
import graphtool.res.Messages;
import graphtool.utils.CollectionObserver;
import graphtool.utils.ObservableCollection;
import graphtool.utils.TextComponentPrintStream;

/**
 * Komponente zur Anzeige von Informationen zur Durchführung des Algorithmus.
 * Ein Objekt dieser Klasse kann als Beobachter bei (einer oder mehreren) Datensammlungen 
 * vom Typ ObservableCollection&lt;GTVertexInfo&gt;
 * angemeldet werden und wird dann über Änderungen (Hinzufügen, Entfernen) 
 * dieser Collections informiert. 
 * <p>
 * Die Inhalte der Collections werden in einer TextArea angezeigt. 
 * Die TextArea ist mindestens zwei Zeilen hoch, wächst aber mit, wenn mehr als
 * zwei Collections beobachtet werden. Die TextArea liegt auf einer JScrollPane,
 * bei der der horizontale Rollbalken fest vorgegeben ist, da in vielen Fällen
 * die Fensterbreite nicht für die Darstellung des Inhalts in einer Zeile ausreichen wird.
 * @see CollectionObserver
 * @see ObservableCollection
 */
public class GTInfoPanel extends JPanel implements CollectionObserver<GTVertexInfo> {

	private static final long serialVersionUID = -4141679825375538962L;

	/** Mindesthöhe der TextArea */
	private static final int MIN_ROWS = 2;
	/** Schriftart für die Darstellung der Meldungen. */
	private static final Font TA_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	/**
	 * Button, der gleichzeitig als Label für die Textarea dient und
	 * darüberhinaus mit Aktionen belegt werden kann.
	 */
	private JButton infoButton = null;
	/** 
	 * Mehrzeilige Textanzeige für Meldungen.
	 * Dargestellt werden insbesondere die Inhalte von Collections. 
	 * Initialgröße: {@link #MIN_ROWS} Zeilen à 80 Zeichen. 
	 * */
	private JTextArea textArea = null;

	/** PrintStream, mit dem in die TextArea geschrieben werden kann */
	private TextComponentPrintStream tcps = null;
	
	/** 
	 * Liste der beobachteten Collections. Sie werden in der Reihenfolge
	 * der Verwendung von oben nach unten in der TextArea dargestellt.
	 */
	private ArrayList<Collection<GTVertexInfo>> collections = new ArrayList<>();
	
	/**
	 * Infoanzeige erzeugen. Ruft {@link #initialize()} auf.
	 */
	public GTInfoPanel() {
		initialize();
	}
	
	/**
	 * Erzeugt die grafischen Elemente für die Anzeige von Meldungen
	 * beim Ablauf des Algorithmus. 
	 */
	private void initialize() {
		// Links wird ein Beschriftung angebracht, den Rest des Platzes nimmt
		// die Textanzeige ein.
		setLayout(new BorderLayout());
		infoButton = new JButton(Messages.getString("GTInfoPanel.InfoButtonLabel")); //$NON-NLS-1$ 
		infoButton.setToolTipText(Messages.getString("GTInfoPanel.InfoButtonTooltip")); //$NON-NLS-1$
		add(infoButton, BorderLayout.WEST);

		// Ausgabeelement erzeugen
		textArea = new JTextArea(MIN_ROWS, 80);
		textArea.setToolTipText(Messages.getString("GTInfoPanel.TextAreaTooltip")); //$NON-NLS-1$
		// Schriftart festlegen
		textArea.setFont(TA_FONT);
		// Änderungen am Text durch Nutzereingabe sollen nicht möglich sein.
		textArea.setEditable(false);
		// Die TextArea wird in eine ScrollPane gesteckt für den Fall, dass die
		// Meldung nicht in den zur Verfügung stehenden Platz passt. Da der Fall,
		// dass die Meldungen länger sind als das Fenster breit ist, vergleichsweise
		// häufig vorkommen wird, wird der horizontale Rollbalken fest vorgegeben.
		JScrollPane scroller = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(scroller, BorderLayout.CENTER);
		
		
	}
	
	/** Reicht den Aufruf an den {@link #infoButton} weiter. */
	public void addActionListener(ActionListener l) {
		infoButton.addActionListener(l);
	}
	
	/** Reicht den Aufruf an den {@link #infoButton} weiter. */
	public void removeActionListener(ActionListener l) {
		infoButton.removeActionListener(l);
	}
	
	/** Reicht den Aufruf an den {@link #infoButton} weiter. */
	public ActionListener[] getActionListeners() {
		return infoButton.getActionListeners();
	}
	
	/**
	 * Ein Hinzufügen eines Elements aus einer beobachteten Collection führt
	 * zu einem Aufruf dieser Callback-Methode. Diese sorgt für eine Darstellung
	 * aller Collections, zu denen Meldungen eingehen.
	 * Ruft {@link #adjustTextArea()} und {@link #updateText()} auf.
	 * Mit {@link #clear()} werden die gespeicherten Einstellungen gelöscht und das
	 * Textfeld geleert.
	 * @param collection Collection, die verändert wurde
	 * @param element in collection eingefügtes Element
	 */
	@Override
	public void elementAdded(Collection<GTVertexInfo> collection, GTVertexInfo element) {
		// Falls Meldungen zu einer bislang unbekannten Collection eingehen,
		// wird diese Collection in die Sammlung aufgenommen.
		if (!collections.contains(collection)) {
			collections.add(collection);
		}
		// Gegebenenfalls die Größe anpassen
		adjustTextArea();
		// Text aktualisieren
		updateText();
	}

	/**
	 * Ein Entfernen eines Elements aus einer beobachteten Collection führt
	 * zu einem Aufruf dieser Callback-Methode. Diese sorgt für eine Darstellung
	 * aller Collections, zu denen Meldungen eingehen.
	 * Ruft {@link #adjustTextArea()} und {@link #updateText()} auf.
	 * Mit {@link #clear()} werden die gespeicherten Einstellungen gelöscht und das
	 * Textfeld geleert.
	 * @param collection Collection, die verändert wurde
	 * @param element aus collection entferntes Element
	 */
	//public void elementRemoved(Collection<? extends Object> collection, Object element) {
	@Override
	public void elementRemoved(Collection<GTVertexInfo> collection, Object element) {
		// Falls Meldungen zu einer bislang unbekannten Collection eingehen,
		// wird diese Collection in die Sammlung aufgenommen.
		if (!collections.contains(collection)) {
			collections.add(collection);
		}
		// Gegebenenfalls die Größe anpassen
		adjustTextArea();
		// Text aktualisieren
		updateText();
	}
	
	/**
	 * Löscht die Liste der dargestellten Collections und den Inhalt der TextArea.
	 */
	public void clear() {
		// Alle bislang registrieren Collections werden "vergessen".
		// Erneute Aufrufe von elementAdded() oder elementRemoved() führen
		// zu einem neuen Aufbau der Sammlung.
		collections.clear();
		// Gegebenenfalls die Größe anpassen
		adjustTextArea();
		// Text löschen
		setText(""); //$NON-NLS-1$
	}

	/**
	 * Passt die Größe der TextArea der Anzahl der beobachteten Collections an.
	 * Werden mehr Collections beobachtet als die TextArea sichtbare Zeilen hat,
	 * so wird die Höhe vergrößert. Umgekeht wird die Höhe verkleinert, wenn mehr
	 * Zeilen dargestellt werden als benötigt. Als Untergrenze für die Zeilenzahl
	 * ist {@link #MIN_ROWS} festgelegt.
	 */
	private void adjustTextArea() {
		// aktuelle sichtbare Zeilenanzahl der TextArea
		int actRows = textArea.getRows();
		// Anzahl der benötigten Zeilen für die beobachteten Collections
		int newRows = collections.size();
		// Weniger als MIN_ROWS Zeilen sollen es in der Darstellung nicht sein
		if (newRows < MIN_ROWS) {
			newRows = MIN_ROWS;
		}
		// Falls die neue gewünschte Größe von der aktuellen Größe abweicht
		if (newRows != actRows) {
			// Zeilenzahl anpassen
			textArea.setRows(newRows);
			// neu darstellen.
			revalidate();
		}
	}
	
	/**
	 * Stellt alle Collections, zu denen Meldungen seit dem letzten Aufruf von
	 * clear() eingegangen sind, untereinander in der TextArea dar.
	 */
	public void updateText() {
		Iterator<Collection<GTVertexInfo>> it = collections.iterator();
		StringBuilder sb = new StringBuilder();
		for (int i = collections.size(); it.hasNext(); i--) {
			// Textdarstellung der nächsten Collection anfügen
			sb.append(it.next());
			// Sofern noch weitere Zeilen folgen werden, newline einfügen
			if (i > 1) {
				sb.append('\n');
			}
		}
		setText(sb.toString());
	}

	/**
	 * Setzt den Inhalt des Textfelds.
	 * Der Inhalt der TextArea wird jeweils sowohl bei nachfolgenden Aufrufen
	 * von setText als auch von {@link #updateText()} überschrieben.
	 * @param text Neuer Text, der anzuzeigen ist.
	 */
	public void setText(String text) {
		textArea.setText(text);
	}
	
	/**
	 * Gibt das verwendete Textfeld zurück.
	 * @return das verwendete Textfeld.
	 */
	public JTextComponent getTextComponent() {
		return textArea;
	}

	/**
	 * Liefert einen PrintStream, mit Hilfe dessen in die TextArea geschrieben werden kann.
	 * @return PrintStream zum Schreiben in die TextArea.
	 */
	public TextComponentPrintStream getTextPrintStream() {
		if (tcps == null) {
			tcps = new TextComponentPrintStream(textArea);
		}
		return tcps;
	}
}

