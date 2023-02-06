package graphtool.utils;

import java.util.Collection;

/**
 * Interface für eine Collection, deren Veränderungen  durch Hinzufügen und
 * Entfernen von Elementen beobachtet werden können.
 * @param <E> Typ der in den Collections gespeicherten Elemente.
 * @see CollectionObserver
 */
public interface ObservableCollection<E> extends Collection<E> {
	/**
	 * Fügt einen Beobachter hinzu, der über Änderungen bei den Elementen
	 * informiert wird.
	 * @param observer hinzuzufügender Beobachter.
	 * @return true, wenn der Beobachter nicht schon in der Liste war, false sonst
	 */
	public boolean addObserver(CollectionObserver<E> observer);
	
	/**
	 * Entfernt einen Beobachter aus der Liste. 
	 * @param observer zu entfernender Beobachter.
	 * @return true, wenn der Beobachter in der Liste war, false sonst
	 */
	public boolean removeObserver(CollectionObserver<E> observer);
}
