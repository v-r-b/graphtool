package graphtool.gui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.function.BiConsumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import graphtool.res.Messages;
import graphtool.utils.HTMLEditorPane;

/**
 * Dialog zur Anzeige einer oder mehrerer HTML-Dateien. 
 */
public class GTAboutDialog extends JDialog {

	private static final long serialVersionUID = -22832442864427084L;

	/** Titel des Fensters vor dem Laden der HTML-Seite */
	public static final String DIALOG_TITLE = "Info"; //$NON-NLS-1$
	
	/**
	 * Erzeugt einen Dialog zur Anzeige von Informationen zur Anwendung.
	 * Die Informationen werden in HTML-Dateien erwartet. Werden mehrere
	 * HTML-Dateien angegeben, so wird jede in einem Tab dargestellt, der
	 * den Titel der geladenen HTML-Seite trägt.
	 * @param owner Frame, auf dem der Dialog angezeigt werden soll
	 * @param modal gibt an, ob der Dialog modal sein soll
	 * @param pathnames Ein oder mehrere Pfade zu HTML-Dateien (werden im Klassenpfad gesucht)
	 */
	public GTAboutDialog(Frame owner, boolean modal, String... pathnames) {
		// Modalen Dialog erzeugen.
		super(owner, DIALOG_TITLE, modal);
		// Dialog kann über "Schließen"-Button oder über "Fenster schließen" geschlossen werden.
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		setLayout(new BorderLayout());
		
		// Wenn nur eine Datei geladen werden soll, ...
		if (pathnames.length == 1) {
			// ... dann die HTML-Anzeigekomponente direkt im Dialog anzeigen
			addHTMLComponent(pathnames[0], (Component comp, String title) -> {
				add(comp, BorderLayout.CENTER);
				// Titel des Dialogfelds auf den HTML-Titel ändern
				setTitle(title);
			});
		}
		// Wenn es mehr als eine Datei ist, ...
		else if (pathnames.length > 1) {
			// ... dann die Komponenten in einer TabbedPane anordnen.
			JTabbedPane tabbedPane = new JTabbedPane();
			for (String path : pathnames) {
				// Je eine HTML-Anzeigekomponente in einem Tab anzeigen
				addHTMLComponent(path, (Component comp, String title) -> {
					// Tab bekommt den Titel aus dem HTML-Dokument
					tabbedPane.addTab(title, comp);
				});
			}
			// TabbedPane im Dialog anzeigen.
			add(tabbedPane, BorderLayout.CENTER);
		}
		// Button zum Schließen wird unterhalb angezeigt
		JButton closeButton = new JButton(
				Messages.getString("GraphTool.common.CloseButtonLabel"));  //$NON-NLS-1$
		closeButton.addActionListener((ActionEvent evt) -> dispose());
		add(closeButton, BorderLayout.SOUTH);
		
		// Enter bewirkt ebenfalls ein Schließen des Fensters
		getRootPane().setDefaultButton(closeButton);

		// Komponenten anordnen, Position für den Dialog bestimmen.
		pack();
		setLocationRelativeTo(owner);
	}
	
	/**
	 * Fügt eine HTML-Anzeigekomponente dem Dialog hinzu.
	 * Durch die Angabe der Funktion zum Hinzufügen kann flexibel darauf
	 * reagiert werden, ob eine oder mehrere HTML-Komponenten im Dialog
	 * angezeigt werden sollen.
	 * @param pathname Pfadname zum HTML-Dokumen.
	 * @param adder Funktion zum Hinzufügen der Komponente zum Dialog. Die Funktion wird mit den
	 * 				Parametern &lt;HTML-Komponente (in ScrollPane)&gt; &lt;Titel aus dem HTML-Dokument&gt;
	 * 				aufgerufen.
	 */
	private static void addHTMLComponent(String pathname, BiConsumer<Component, String> adder) {
		// HTML-Anzeigekomponente einrichten
		HTMLEditorPane htmlPane = new HTMLEditorPane(pathname);
		// Da die Seite asynchron geladen wird, ist der Titel erst nach einem
		// entsprechenden PropertyChangeEvent "page" aus der Komponente auslesbar.
		// Das Hiinzufügen passiert daher erst, wenn der Event auftritt:
		htmlPane.addPropertyChangeListener("page", new PropertyChangeListener() { //$NON-NLS-1$
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Object newValO = evt.getNewValue();
				if ((newValO != null) && (newValO instanceof URL)) {
					String title = htmlPane.getPageTitle();
					// Die HTML-Anzeige wird in eine ScrollPane gepackt für den Fall, 
					// dass der Text breiter oder länger ist als der zur Verfügung stehende Platz
					adder.accept(new JScrollPane(htmlPane), title);
					// Dies ist eine einmalige Aktion, daher Listener gleich wieder entfernen.
					// Andernfalls würde bei künftigen "page"-Events immer wieder eine neue
					// Komponente hinzugefügt.
					htmlPane.removePropertyChangeListener("page", this); //$NON-NLS-1$
				}
			}
		});
	}
}