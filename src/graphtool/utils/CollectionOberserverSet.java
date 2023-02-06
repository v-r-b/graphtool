package graphtool.utils;

import java.util.Collection;
import java.util.HashSet;

/**
 * Implementierung eines HashSets zur Verwaltung von CollectionObservern.
 * Das Hinzufügen und Entfernen von Beobachtern erfolgt über die entsprechenden
 * Methoden von HashSet, die Benachrichtigungsmethoden sind ergänzend implementiert.
 * @param <E> Typ der in den Collections gespeicherten Elemente.
 * @see CollectionObserver
 * @see HashSet#add(Object)
 * @see HashSet#remove(Object)
 */
public class CollectionOberserverSet<E> extends HashSet<CollectionObserver<E>> {
	
	private static final long serialVersionUID = -5288395839659296388L;

	/**
	 * Alle gespeicherten Beobachter werden per Aufruf von elementAdded() über das 
	 * Hinzufügen des Elements in Parameter 2 zur Collection in Parameter 1 informiert.
	 * @param collection Collection, die eine Veränderung erfahren hat
	 * @param element betroffenes Element der Collection
	 * @see CollectionObserver#elementAdded(Collection, Object)
	 */
	public void notifyObserversOnAdd(Collection<E> collection, E element) {
		for (CollectionObserver<E> o : this) {
			if (o != null) {
				o.elementAdded(collection, element);
			}
		}
	}
	
	/**
	 * Alle gespeicherten Beobachter werden per Aufruf von elementRemoved() über das 
	 * Löschen des Elements in Parameter 2 aus der Collection in Parameter 1 informiert.
	 * @param collection Collection, die eine Veränderung erfahren hat
	 * @param element betroffenes Element der Collection
	 * @see CollectionObserver#elementRemoved(Collection, Object)
	 */
	public void notifyObserversOnRemove(Collection<E> collection, Object element) {
		for (CollectionObserver<E> o : this) {
			if (o != null) {
				o.elementRemoved(collection, element);
			}
		}
	}
	
}
