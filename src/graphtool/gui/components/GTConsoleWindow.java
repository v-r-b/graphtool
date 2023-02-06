package graphtool.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import graphtool.res.Messages;

/**
 * Dialog zur Anzeige der Konsolenausgabe. 
 */
public class GTConsoleWindow extends JDialog {

	private static final long serialVersionUID = -22832442864427084L;

	/** Titel des Fensters */
	public static final String CONSOLE_TITLE = 
			Messages.getString("GTConsoleWindow.ConsoleWindowTitle"); //$NON-NLS-1$
	/** Schriftart für die verwendete TextArea */
	public static final Font TA_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	/** Vordergrundfarbe */
	public static Color FOREGROUND_COLOR = Color.GREEN;
	/** Hintergrundfarbe */
	public static Color BACKGROUND_COLOR = Color.BLACK;

	/**
	 * Erzeugt einen Nicht-Modalen Dialog mit einer TextArea und einem Schließen-Button.
	 * Die TextArea ist nicht editierbar und dient nur zum Ausgeben von Text.
	 * @param textComponent JTextArea, die den Konsolentext enthält
	 */
	public GTConsoleWindow(JTextArea textComponent) {
		// Nicht-Modalen Dialog erzeugen.
		setTitle(CONSOLE_TITLE);
		setModal(false);
		// Dialog kann über "Schließen"-Button oder über "Fenster schließen" geschlossen werden.
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		setLayout(new BorderLayout());
		
		// Eigenschaften der TextKomponente einstellen
		textComponent.setFont(TA_FONT);
		textComponent.setRows(35);
		textComponent.setColumns(120);
		textComponent.setEditable(false);
		textComponent.setBackground(BACKGROUND_COLOR);
		textComponent.setForeground(FOREGROUND_COLOR);
		textComponent.setLineWrap(true);
		// TextArea nimmt den meisten Raum ein und wird in eine
		// ScrollPane gepackt für den Fall, dass der Text breiter oder länger ist
		// als der zur Verfügung stehende Platz
		JScrollPane scroller = new JScrollPane(textComponent);
		add(scroller, BorderLayout.CENTER);
		
		// Button zum Schließen wird unten angezeigt
		JButton closeButton = new JButton(
				Messages.getString("GraphTool.common.CloseButtonLabel")); //$NON-NLS-1$
		closeButton.addActionListener((ActionEvent evt) -> dispose());
		add(closeButton, BorderLayout.SOUTH);
		
		// Enter bewirkt ebenfalls ein Schließen des Fensters
		getRootPane().setDefaultButton(closeButton);

		// Komponenten anordnen, Position für den Dialog bestimmen.
		pack();
	}
}

