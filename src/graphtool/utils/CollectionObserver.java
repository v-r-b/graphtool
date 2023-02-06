package graphtool.utils;

import java.util.Collection;

/**
 * Callback-Interface für die Beobachtung von Collections.
 * Beobachter werden darüber informiert, dass einer Collection ein
 * Element hinzugefügt oder dass ein Element daraus entfernt wurde.
 * @param <E> Typ der in den Collections gespeicherten Elemente.
 * @see ObservableCollection
 */
public interface CollectionObserver<E> {
	/**
	 * Der Collection im Parameter 1 wurde das Element im Parameter 2 hinzugefügt.
	 * @param collection Collection, die eine Veränderung erfahren hat
	 * @param element betroffenes Element der Collection
	 */
	public void elementAdded(Collection<E> collection, E element);
	/**
	 * Aus der Collection im Parameter 1 wurde das Element im Parameter 2 entfernt.
	 * @param collection Collection, die eine Veränderung erfahren hat
	 * @param element betroffenes Element der Collection
	 */
	public void elementRemoved(Collection<E> collection, Object element);
}
