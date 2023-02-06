package graphtool.algorithm;

import java.util.HashSet;

import graphtool.graph.GTVertex;
import graphtool.utils.CollectionObserver;
import graphtool.utils.ObservableCollection;
import graphtool.utils.CollectionOberserverSet;

/**
 * Implementierung einer Menge, die gleichzeitig das Protokoll von
 * ObservableCollection und von GTVertexInfoCollection erfüllt.
 * Neben den "nativen" Methoden, die sich auf Elemente vom Typ
 * GTVertexInfo beziehen, werden zusätzlich Methoden eingeführt, die
 * mit Elementen vom Typ GTVertex arbeiten.
 */
public class GTVertexInfoSet	extends HashSet<GTVertexInfo> 
								implements 	ObservableCollection<GTVertexInfo>,
											GTVertexInfoCollection {

	private static final long serialVersionUID = -5859463773973749704L;
	
	/** Name für die Warteschange. Wird in toString() verwendet. */
	private String name = null;
	
	/** Liste von Beobachtern, die über Inhaltsänderungen informiert werden sollen. */
	private CollectionOberserverSet<GTVertexInfo> observers = new CollectionOberserverSet<>();
	

	/**
	 * Instanz der Klasse mit dem angegebenen Namen erzeugen.
	 * Der Name wird in der {@link #toString()}-Methode verwendet.
	 * @param name Benutzerdefinierter Name der Instanz.
	 */
	public GTVertexInfoSet(String name) {
		this.name = name;
	}
	
	/**
	 * Instanz der Klasse mit dem angegebenen Namen erzeugen und den
	 * angegebenen Beobachter registrieren.
	 * Der Name wird in der {@link #toString()}-Methode verwendet.
	 * @param name Benutzerdefinierter Name der Instanz.
	 * @param observer Beobachterobjekt
	 * @see #addObserver(CollectionObserver)
	 */
	public GTVertexInfoSet(String name, CollectionObserver<GTVertexInfo> observer) {
		this(name);
		if (observer != null) {
			addObserver(observer);
		}
	}
	
	/**
	 * Fügt der Menge ein Element hinzu. Die Beobachter der
	 * Menge werden über das Hinzufügen informiert.
	 */
	@Override
	public boolean add(GTVertexInfo e) {
		boolean result = super.add(e);
		observers.notifyObserversOnAdd(this, e);
		return result;
	}
	
	/**
	 * Entfernt ein Element aus der Menge. Die Beobachter der
	 * Menge werden über das Entfernen informiert.
	 */
	@Override
	public boolean remove(Object e) {
		boolean result = super.remove(e);
		observers.notifyObserversOnRemove(this, e);
		return result;
	}
	
	/**
	 * Liefert denjenigen Info-Eintrag aus der Menge, in dem der gegebene
	 * Knoten im Feld "vertex" gespeichert ist. Die Nutzung ist nur sinnvoll,
	 * wenn sichergestellt ist, dass jeder Eintrag im Feld "vertex" 
	 * eines Elements nur einmal in der Menge vorhanden ist, dass
	 * also zu einem Knoten nur genau ein Vorgänger und ein Abstand
	 * gespeichert sind. Ist dies nicht gegeben, macht die Methode keine
	 * Zusicherung dazu, welches der passenden Elemente zurückgegeben wird.
	 * 
	 * @param vertex Zu suchender Knoten (vertex in GTVertexInfo)
	 * @return Info-Element oder null.
	 */
	@Override
	public GTVertexInfo findElementFor(GTVertex vertex) {
		for (GTVertexInfo vi : this) {
			if(vi.getVertex().equals(vertex)) {
				return vi;
			}
		}
		return null;
	}
	
	/**
	 * Gibt als Ergänzung zu {@link #contains(Object)} an, ob ein Info-Eintrag
	 * zum angegebenen Knoten in der Menge enthalten ist. Zur Verdeutlichung:
	 * der Knoten vertex selbst ist nicht Element der Menge, sondern es existiert
	 * (mindestens) ein Eintrag e vom Typ GTVertexInfo, für den gilt: 
	 * e.getVertex().equals(vertex). Der Wert von {@link #contains(GTVertex)}
	 * ist äquivalent zu {@link #findElementFor(GTVertex)} != null.
	 * 
	 * @param vertex Zu suchender Knoten (vertex in GTVertexInfo)
	 * @return true, wenn ein passendes Info-Element gefunden wurde.
	 */
	@Override
	public boolean contains(GTVertex vertex) {
		return findElementFor(vertex) != null;
	}

	/**
	 * Gibt die Elemente der Menge aus (siehe {@link HashSet#toString()}.
	 * Wurde bei der Erzeugung ein Name angegeben, wird der in der Superklasse
	 * erzeugten Zeichenkette ein "<name>:" vorangestellt.
	 */
	@Override
	public String toString() {
		if (name != null) {
			return name + ":" + super.toString(); //$NON-NLS-1$
		}
		return super.toString();
	}
	
	/**
	 * Fügt einen Beobachter hinzu, der über Änderungen bei den Elementen
	 * informiert wird.
	 * @param observer Beobachterobjekt
	 * @see CollectionOberserverSet#add(Object)
	 */
	@Override
	public boolean addObserver(CollectionObserver<GTVertexInfo> observer) {
		return observers.add(observer);
	}
	
	/**
	 * Entfernt einen Beobachter aus der Liste. 
	 * @param observer Beobachterobjekt
	 * @see CollectionOberserverSet#remove(Object)
	 */
	@Override
	public boolean removeObserver(CollectionObserver<GTVertexInfo> observer) {
		return observers.remove(observer);
	}
}
