package graphtool.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.text.JTextComponent;

/**
 * OutputStream, der seine Ausgabe in eine JTextComponent schreibt.
 */
public class TextComponentOutputStream extends OutputStream {
	/** 
	 * Zeichen zum Löschen des Inhalts der Textkomponente.
	 * Dieses ist angelehnt an das Zeichen FF (form feed) in ASCII
	 * bzw. Strg+L zum Löschen eines Terminals.
	 */
	public static final int CLEAR_SCREEN = 0xc;

	/** Textkomponente, die die Ausgabe aufnehmen soll */
	private JTextComponent textComponent = null;
	/** Gesammelter Text bis zum nächsten flush() */
	private ByteArrayOutputStream outBytes = null;
	/** Listener, der über den Aufruf von flush() informiert werden soll */
	private TextComponentFlushListener flushListener = null;

	/**
	 * Erzeugt den Stream für die übergebene Textkomponente.
	 * @param out Textkomponente, die die Ausgabe aufnehmen soll.
	 */
	public TextComponentOutputStream(JTextComponent out) {
		textComponent = out;
		outBytes = new ByteArrayOutputStream();
	}
	
	public TextComponentOutputStream(JTextComponent out, TextComponentFlushListener l) {
		this(out);
		flushListener = l;
	}

	/**
	 * Wandelt die bislang in outBytes gepufferten Bytes in eine
	 * Zeichenkette um und hängt diese an den Text in textComponent an.
	 */
	@Override
	public void flush() {
		// Bytes zu einer Zeichenkette dekodieren
		String bufferedText = outBytes.toString();
		// Puffer löschen
		outBytes.reset();
		// Zeichenkette an den vorhandenen Text anhängen
		textComponent.setText(textComponent.getText() + bufferedText);
		// Listener informieren, sofern registriert
		if (flushListener != null) {
			flushListener.streamFlushed();
		}
	}
	
	/**
	 * Fügt das übergebene Byte dem Ausgabepuffer hinzu. Der Puffer wird
	 * beim nächsten Aufruf von flush() in die Textkomponente geschrieben.
	 * <p>
	 * Wird der Wert 12 übergeben (ASCII-Code 12 ist FF=form feed), wird
	 * das Byte nicht an den Puffer agehängt, sondern es
	 * wird der Inhalt der Textkomponente gelöscht.
	 */
	@Override
	public void write(int b) throws IOException {
		if (b == CLEAR_SCREEN) {
			// kompletten Text löschen
			textComponent.setText(""); //$NON-NLS-1$
		}
		else {
			// Byte puffern bis zum nächsten Aufruf von flush()
			outBytes.write(b);
		}
	}
}