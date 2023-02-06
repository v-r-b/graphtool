package graphtool.gui.components;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import graphtool.GraphTool;
import graphtool.res.Messages;
import graphtool.utils.ActionButton;

/**
 * Dialog zur Eingabe eines einzelnen Wertes für ein neu erzeugtes Element
 * im Graphen. Dient als Basis für die Eingabedialoge für Ecken und Kanten.
 *
 */
public abstract class GTAbstractInputDialog extends JDialog {

	private static final long serialVersionUID = 5640130857460003128L;

	/** Textfeld zur Eingabe des zu erfassenden Wertes */
	protected JTextField textInput;
	/** Angabe, ob der Dialog mit "OK" beendet wurde. */
	private boolean closedWithOK = false;

	/**
	 * Baut einen Eingabedialog auf und positioniert ihn relativ zur übergebenen
	 * Komponente. Zur Anzeige muss noch setVisible() aufgerufen werden.
	 * Klickt der Nutzer "OK", so schließt sich der Dialog nur, wenn auch {@link #setValue()} 
	 * true liefert. In diesem Fall liefert auch {@link #hasBeenClosedWithOK()} true.
	 * Klickt der Nutzer "Abbrechen", so schließt sich der Dialog in jedem Fall und
	 * {@link #hasBeenClosedWithOK()} liefert false. 
	 * @param positioner Komponente, relativ zu der die Position des Dialog bestimmt werden soll.
	 * @param title Text für die Titelzeile des Dialogfensters.
	 * @param label Beschriftung des Eingabefelds.
	 */
	public GTAbstractInputDialog(Component positioner, String title, String label) {
		// Modalen Dialog mit den angegebenen Titel erzeugen.
		super(GraphTool.getFrame(), title, true);
		// Dialog kann über "OK"-Button oder über "Fenster schließen" geschlossen werden.
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		// Im Dialog werden ein Label, ein Textfeld und darunter zwei Buttons angeordnet. 
		
		JPanel p = new JPanel(new GridLayout(2, 1));
		
		JPanel q = new JPanel(new GridBagLayout());
		// Textfeld mit Beschriftung wie angegeben erzeugen.
	    GridBagConstraints c = new GridBagConstraints();
	    c.ipadx = 10; // Zwischenraum zwischen Labeltext und Textfeld einfügen
	    q.add(new JLabel(label), c);
		q.add(textInput = new JTextField(10));
		p.add(q);
		
		q = new JPanel(new GridLayout(1, 2));
		// OK-Button ruft die abstrakte Methode setValue() auf, die in der Implementierung in den
		// Subklassen den eingegebenen Wert ausliest. Ist dieser OK, wird der Dialog geschlossen.
		JButton b = new ActionButton(Messages.getString("GraphTool.common.OKButtonLabel"),  //$NON-NLS-1$
				(ActionEvent evt) -> {
					if (setValue()) {
						closedWithOK = true;
						dispose();
					}
					else {
						Toolkit.getDefaultToolkit().beep();
					}
				});
		// "Enter" bewirkt das Gleiche wie ein Mausklick auf "OK"
		getRootPane().setDefaultButton(b);
		q.add(b);
		b = new ActionButton(Messages.getString("GraphTool.common.CancelButtonLabel"),  //$NON-NLS-1$
				(ActionEvent evt) -> { closedWithOK = false; dispose(); });
		q.add(b);
		p.add(q);
		
		add(p);

		// Komponenten anordnen, Position für den Dialog bestimmen.
		pack();
		setLocationRelativeTo(positioner);
	}

	/**
	 * Klickt der Nutzer "OK", so schließt sich der Dialog nur, wenn auch {@link #setValue()} 
	 * true liefert. In diesem Fall liefert auch {@link #hasBeenClosedWithOK()} true.
	 * Klickt der Nutzer "Abbrechen", so schließt sich der Dialog in jedem Fall und
	 * {@link #hasBeenClosedWithOK()} liefert false. 
	 * @return true wenn der Dialog erfolgreich mit "OK" geschlossen wurde.
	 */
	public boolean hasBeenClosedWithOK() {
		return closedWithOK;
	}
	
	/**
	 * Liest den im Textfeld eingegebenen Wert aus und überprüft ihn auf
	 * Zulässigkeit. Ist diese gegeben, wird der Wert an das Graphenelement
	 * übergeben und "true" zurückgeliefert. Wenn nicht, wird "false" zurückgegeben.
	 * @return true oder false, je nachdem ob der eingegebene Wert zulässig ist.
	 */
	abstract public boolean setValue();
}

