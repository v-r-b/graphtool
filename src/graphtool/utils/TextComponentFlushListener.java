package graphtool.utils;

/** 
 * Listener-Interface für Flushes in TextComponentOutputStream und TextComponentPrintStream
 * @see TextComponentOutputStream
 * @see TextComponentPrintStream
 */
@FunctionalInterface
public interface TextComponentFlushListener {
	/** Listener über den Aufruf von flush() im zugrundeliegenden Stream informieren */
	public void streamFlushed();
}