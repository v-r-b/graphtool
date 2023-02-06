package graphtool.utils;

import java.io.PrintStream;

import javax.swing.text.JTextComponent;

/**
 * PrintStream, der seine Ausgabe in eine JTextComponent schreibt.
 * Dazu wird die Klasse {@link TextComponentOutputStream} verwendet.
 * <p>
 * Per System.setOut() oder System.setErr() ist es z.B. möglich, die
 * Standardausgabe oder die Fehlerausgabe in die Textkomponente umzuleiten.
 * @see System#setOut(PrintStream)
 * @see System#setErr(PrintStream)
 */
public class TextComponentPrintStream extends PrintStream {
	
	/**
	 * Erzeugt den Stream für die übergebene Textkomponente.
	 * @param out Textkomponente, die die Ausgabe aufnehmen soll.
	 * @param l Listener, der informiert werden soll, wenn im zugrundeliegenden
	 *          TextComponentOutputStream die flush-Methode aufgerufen wird,
	 *          also Zeichen in die Textkomponente geschrieben werden.
	 */
	@SuppressWarnings("resource")
	public TextComponentPrintStream(JTextComponent out, TextComponentFlushListener l) {
		// Ausgabe in einen TextComponentOutputStream umleiten und autoFlush einschalten
		super(new TextComponentOutputStream(out, l), true);
	}
	
	/**
	 * Erzeugt den Stream für die übergebene Textkomponente.
	 * @param out Textkomponente, die die Ausgabe aufnehmen soll.
	 */
	@SuppressWarnings("resource")
	public TextComponentPrintStream(JTextComponent out) {
		// Ausgabe in einen TextComponentOutputStream umleiten und autoFlush einschalten
		super(new TextComponentOutputStream(out), true);
	}
	
	/**
	 * Löscht den Inhalt der Textkomponente.
	 * <p>
	 * Der Aufruf ist äquivalent zu einem Aufruf von write(CLEAR_SCREEN).
	 * @see TextComponentOutputStream#write(int)
	 * @see TextComponentOutputStream#CLEAR_SCREEN
 	 */
	public void clear() {
		// FF-Byte an den OutputStream schicken
		write(TextComponentOutputStream.CLEAR_SCREEN);
	}
}
