package graphtool.gui.components;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

import graphtool.res.Messages;
import graphtool.utils.CodeExecutionHandler;

/**
 * Komponente zur Anzeige des Ablaufs des Such-Algorithmus.
 * <p>
 * In einer nicht editierbaren TextArea wird wahlweise der Quelltext
 * oder der Pseudocode des Algorithmus angezeigt. Bei der Durchführung
 * wird die jeweils nächste auszuführende Zeile selektiert.
 * @see CodeExecutionHandler#getCodeSectionPseudoCode()
 */
public class GTSourcePanel extends JPanel {
	
	private static final long serialVersionUID = 8526751573899136288L;

	/** Beschriftung der Textanzeige für den Sourcecode. */
	private JLabel sourceTextLabel = null;
	/** Titel für Sourcecode */
	private static final String SOURCECODE_LABEL = 
			Messages.getString("GTSourcePanel.SourceCodeLabel"); //$NON-NLS-1$
	/** Titel für Pseudocode */
	private static final String PSEUDOCODE_LABEL = 
			Messages.getString("GTSourcePanel.PseudoCodeLabel"); //$NON-NLS-1$
	/** Mehrzeilige Textanzeige für den Sourcecode. */
	private JTextArea sourceTextArea = null;
	/** Schriftart für die sourceTextArea */
	private static final Font TA_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	/** Einstellung, ob Pseudo- oder Java-Code dargestellt werden soll */
	private JCheckBox usePseudoCode = null;
	/** Platzhalter, wenn kein Source-/Pseudocode vorhanden ist */
	private static final String NO_CODE = 
			Messages.getString("GTSourcePanel.EmptyCode"); //$NON-NLS-1$
	/** Java-Code */
	private String javaCode = NO_CODE;
	/** Pseudo-Code */
	private String pseudoCode = NO_CODE;
	/** Zeilennummer der selektierten Zeile im Java-Code */
	private int javaCodeLineSelected = 0;
	/** Zeilennummer der selektierten Zeile im Pseudocode */
	private int pseudoCodeLineSelected = 0;
	
	/**
	 * Ablaufanzeige erzeugen. Ruft dazu {@link #initialize()} auf.
	 */
	public GTSourcePanel() {
		initialize();
	}
	
	/**
	 * Erzeugt die grafischen Elemente für die Anzeige des Algorithmus 
	 * und dessen Ablauf. Es kann dabei zwischen Sourcecode (Java)- und
	 * Pseudocode-Darstellung gewechselt werden.
	 * @see CodeExecutionHandler#getCodeSectionPseudoCode()
	 */
	private void initialize() {
		// Oben (NORTH) wird eine Überschrift angebracht, den Rest des Platzes nimmt
		// die Textanzeige ein (CENTER).
		setLayout(new BorderLayout());
		sourceTextLabel = new JLabel(SOURCECODE_LABEL);
		add(sourceTextLabel, BorderLayout.NORTH);
		
		// Mehrzeilige Textanzeige für den Sourcecode. Die Größe wird in
		// Zeilen und Spalten angegeben. Steht mehr Platz zur Verfügung,
		// wird die Komponente vergrößert.
		sourceTextArea = new JTextArea(20, 80);
		// Schriftart festlegen
		sourceTextArea.setFont(TA_FONT);
		// Änderungen am Text durch den Nutzer sollen nicht möglich sein.
		sourceTextArea.setEditable(false);
		// Eine kleine Tabulatorlänge sorgt dafür, dass die Darstellung bei
		// mehrfacher Schachtelung nicht zu breit wird.
		sourceTextArea.setTabSize(2);
		// Die Textauswahl, die als "Cursor" zur Anzeige der aktuellen Position im
		// Programm dient, soll auch sichtbar sein, wenn das Textfeld nicht den
		// Focus hat. Normalerweise wird die Auswahl beim Verlust des Focus
		// unsichtbar gemacht.
		sourceTextArea.setCaret(new DefaultCaret() {
			private static final long serialVersionUID = -8460915001194074694L;
			@Override
			public void setSelectionVisible(boolean visible) {
				// Egal, was das Framework vorgibt: Auswahl immer anzeigen!
				super.setSelectionVisible(true);
			}
		});
		// Initial einmal die Auswahlanzeige aktivieren.
		sourceTextArea.getCaret().setSelectionVisible(true);
		// Die TextArea wird in eine ScrollPane gesteckt für den Fall, dass der
		// Quellcode nicht in den zur Verfügung stehenden Platz passt.
		JScrollPane scroller = new JScrollPane(sourceTextArea);
		add(scroller, BorderLayout.CENTER);
		
		// Checkbox am unteren Ende des Panels gibt an, ob mit Pseudo- oder
		// mit Java-Code gearbeitet werden soll.
		usePseudoCode = new JCheckBox(Messages.getString("GTSourcePanel.ShowPseudoCodeLabel")); //$NON-NLS-1$
		usePseudoCode.addItemListener((ItemEvent evt) -> {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				// Ist die Pseudocode-Checkbox aktiviert worden,
				// Überschrift und Inhalt an Pseudocodedarstellung anpassen.
				sourceTextArea.setText(pseudoCode);
				sourceTextLabel.setText(PSEUDOCODE_LABEL);
				// Selektion in der Textarea muss neu vorgenommen werden,
				// da die Zeilennummer im Sourcecode und im Pseudocode
				// sich unterscheiden.
				setSelection();
			} 
			else {
				// ditto für Sourcecodedarstellung bei deaktivierter Checkbox
				sourceTextArea.setText(javaCode);
				sourceTextLabel.setText(SOURCECODE_LABEL);
				setSelection();
			}
		});
		add(usePseudoCode, BorderLayout.SOUTH);
	}
	
	/** 
	 * Gibt an, ob aktuell Pseudocode oder Java-Code dargestellt wird.
	 * @return true bei Pseudocode, false bei Java-Code
	 */
	public boolean usesPseudoCode() {
		return usePseudoCode.isSelected();
	}
	
	/**
	 * Merkt sich zwei Darstellungsvarianten und stellt diejenige dar, die
	 * zur aktuellen Einstellung der pseudoCode-Checkbox passt.
	 * @param javaCode Java-Quelltext für die Anzeigevariante "Sourcecode"
	 * @param pseudoCode Pseudocode für die Anzeigevariante "Pseudocode"
	 */
	public void useText(String javaCode, String pseudoCode) {
		// Beide Texte zur späteren Verwendung merken
		this.javaCode = javaCode;
		this.pseudoCode = pseudoCode;
		// Passenden Text anzeigen
		if (usesPseudoCode()) {
			sourceTextArea.setText(pseudoCode);
		}
		else {
			sourceTextArea.setText(javaCode);
		}
	}
	
	/**
	 * Setzt die Auswahl, also den "Programmcursor". Die angegebenen Zeilennummen beziehen
	 * sich auf den Inhalt der TextArea. Die erste Zeile trägt die Nummer 0. Die
	 * Zeilennummern werden in den Feldern javaCodeLineSelected und pseudoCodeLineSelected
	 * gespeichert, damit beim Wechsel der Darstellung zwischen Pseudo- und Java-Code die
	 * entsprechende Zeile ausgewählt werden kann. 
	 * @param javaCodeLineNum als ausgewählt anzuzeigende Zeile im Java-Code
	 * @param pseudoCodeLineNum als ausgewählt anzuzeigende Zeile im Pseudocode
	 */
	public void setSelectionLines(int javaCodeLineNum, int pseudoCodeLineNum) {
		javaCodeLineSelected = javaCodeLineNum;
		pseudoCodeLineSelected = pseudoCodeLineNum;
		// Auswahlanzeige vornehmen
		setSelection();
	}
	
	/**
	 * Setzt die Auswahl, also den "Programmcursor", abhängig von der Darstellungsvariante
	 * (Java- oder Pseudocode). Die jeweilige Zeilennummer wird dem Feld 
	 * {@link #pseudoCodeLineSelected} bzw. s {@link #javaCodeLineSelected} entnommen,
	 * die zuvor mit {@link #setSelectionLines(int, int)} gesetzt wurden.
	 * */
	public void setSelection() {
		// Richtige Zeilennummer bestimmen
		int lineNum = usesPseudoCode() ? pseudoCodeLineSelected : javaCodeLineSelected;
		try {
			// Anfangs- und Endposition für die Selektion bestimmen
			int lso = sourceTextArea.getLineStartOffset(lineNum);
			int leo = sourceTextArea.getLineEndOffset(lineNum);
			// Das Entfernen der alten Auswahl ist zeitweise fehlerhaft,
			// daher wird vor einer neuen Auswahl die Cursorposition bewegt.
			// Das scheint das Problem zu beheben.
			sourceTextArea.setCaretPosition(lso);
			sourceTextArea.select(lso, leo);
			sourceTextArea.repaint();
		} catch (BadLocationException e) { 
			// Eine fehlerhafte Auswahlanweisung wird ignoriert.
		}
	}
}

