package graphtool.algorithm;

import java.util.Arrays;
import java.util.Collection;
import java.util.PriorityQueue;

import graphtool.graph.GTVertex;
import graphtool.utils.CollectionObserver;
import graphtool.utils.ObservableCollection;
import graphtool.utils.CollectionOberserverSet;

/**
 * Implementierung einer Vorrangwarteschlange, die gleichzeitig das
 * Protokoll von ObservableCollection und von GTVertexInfoCollection erfüllt.
 * Neben den "nativen" Methoden, die sich auf Elemente vom Typ
 * GTVertexInfo beziehen, werden zusätzlich Methoden eingeführt, die
 * mit Elementen vom Typ GTVertex arbeiten.
 */
public class GTPriorityQueue extends PriorityQueue<GTVertexInfo> 
					implements  ObservableCollection<GTVertexInfo>,
								GTVertexInfoCollection {

	private static final long serialVersionUID = 8954663104999063220L;

	/** Name für die Warteschange. Wird in toString() verwendet. */
	private String name = null;
	
	/** Liste von Beobachtern, die über Inhaltsänderungen informiert werden wollen. */
	private CollectionOberserverSet<GTVertexInfo> observers = new CollectionOberserverSet<>();
	
	/**
	 * Instanz der Klasse mit dem angegebenen Namen erzeugen.
	 * Der Name wird in der {@link #toString()}-Methode verwendet.
	 * @param name Benutzerdefinierter Name der Instanz.
	 */
	public GTPriorityQueue(String name) {
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
	public GTPriorityQueue(String name, CollectionObserver<GTVertexInfo> observer) {
		this(name);
		if (observer != null) {
			addObserver(observer);
		}
	}
	
	/**
	 * Fügt der Warteschlange ein Element hinzu. Die Beobachter der
	 * Warteschlange werden über das Hinzufügen informiert.
	 */
	@Override
	public boolean add(GTVertexInfo e) {
		boolean result = super.add(e);
		if (result) { 
			// Hinzufügen war erfolgreich
			observers.notifyObserversOnAdd(this, e);
		}
		return result;
	}
	
	/**
	 * Convenience-Methode für add(new GTVertexInfo(vertex, distance, predecessor)).
	 * @param vertex Knoten, zu dem Informationen gespeichert werden.
	 * @param distance Für Vergleiche relevanter Abstandswert des Knotens.
	 * @param predecessor Vorgängerknoten des Knotens.
	 * @return true (wie in {@link Collection#add} spezifiziert)
	 * @see GTVertexInfo#GTVertexInfo(GTVertex, int, GTVertexInfo)
	 */
	public boolean add(GTVertex vertex, int distance, GTVertexInfo predecessor) {
		return add (new GTVertexInfo(vertex, distance, predecessor));
	}
	
	/**
	 * Convenience-Methode für add(new GTVertexInfo(vertex, partDistance, distance, predecessor)).
	 * @param vertex Knoten, zu dem Informationen gespeichert werden.
	 * @param partDistance Zusätzlicher Abstandswert für diesen Knoten.
	 * @param distance Für Vergleiche relevanter Abstandswert des Knotens.
	 * @param predecessor Vorgängerknoten des Knotens.
	 * @return true (wie in {@link Collection#add} spezifiziert)
	 * @see GTVertexInfo#GTVertexInfo(GTVertex, int, GTVertexInfo)
	 */
	public boolean add(GTVertex vertex, int partDistance, int distance, GTVertexInfo predecessor) {
		return add (new GTVertexInfo(vertex, partDistance, distance, predecessor));
	}
	
	/**
	 * Entfernt ein Element aus der Warteschlange. Die Beobachter der
	 * Warteschlange werden über das Entfernen informiert.
	 */
	@Override
	public boolean remove(Object e) {
		boolean result = super.remove(e);
		if (result) {
			// Entfernen war erfolgreich
			observers.notifyObserversOnRemove(this, e);
		}
		return result;
	}
	
	/**
	 * Aktualisiert einen Eintrag in der Warteschlange. Die Methode
	 * besteht aus einem Entfernen des Elements, dessen Feld vertex mit
	 * dem Feld vertex des Parameters übereinstimmt, gefolgt von 
	 * einem Hinzufügen des übergebenen Elements. Die Beobachter werden
	 * jeweils informiert.
	 * @param updatedElement Neuer Inhalt des zu aktualisierenden Elements
	 */
	public boolean updateKey(GTVertexInfo updatedElement) {
		GTVertexInfo old = findElementFor(updatedElement.getVertex());
		if (old != null) {
			// Wenn gefunden -> entfernen. Die Beobachter werden informiert.
			remove(old);
		}
		// Neues Element hinzufügen. Die Beobachter werden informiert.
		return add(updatedElement);
	}
	
	/**
	 * Entfernt das Kopfelement aus der Warteschlange. Die Beobachter der
	 * Warteschlange werden über das Entfernen informiert.
	 * @return das Kopfelement aus der Warteschlange.
	 */
	@Override
	public GTVertexInfo remove() {
		GTVertexInfo element = super.remove();
		observers.notifyObserversOnRemove(this, element);
		return element;
	}
	
	/**
	 * Identisch zu {@link #remove()}. Wurd vorgesehen, da im Skript anstelle des
	 * Wortes remove das Wort extractMin verwendet wird.
	 */
	public GTVertexInfo extractMin() {
		return remove();
	}

	/**
	 * Liefert denjenigen Info-Eintrag aus der Warteschlange, in dem der gegebene
	 * Knoten im Feld "vertex" gespeichert ist. Die Nutzung ist nur sinnvoll,
	 * wenn sichergestellt ist, dass jeder Eintrag im Feld "vertex" 
	 * eines Elements nur einmal in der Warteschlange vorhanden ist, dass
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
	 * Gibt in Ergänzung zu contains(GTVertexInfo vi) an, ob ein Info-Eintrag
	 * zum angegebenen Knoten in der Warteschlange enthalten ist. Zur Verdeutlichung:
	 * der Knoten vertex selbst ist nicht Element der Warteschlange, sondern es existiert
	 * (mindestens) ein Eintrag e vom Typ GTVertexInfo, für den gilt: 
	 * e.getVertex().equals(vertex). Das Ergebnis Wert von {@link #contains(GTVertex)}
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
	 * Die Methode wurde hier überschrieben, um zu gewährleisten, dass bei der Umwandlung
	 * einer GTPriorityQueue in eine Zeichenkette die enthaltenen Elemente in der
	 * richtigen (aufsteigenden) Reihenfolge aneinandergehängt werden.
	 * Wurde bei der Erzeugung ein Name angegeben, wird ein "<name>:" vorangestellt.
	 * Angelehnt an die Implementierung in {@link java.util.AbstractCollection#toString()}.
	 */
	@SuppressWarnings("nls") // String-Literale beim Zusammenbau des Ergebnisses
	@Override
    public String toString() {
		// Zeichenkette ist umschlossen von "[]"
        StringBuilder sb = new StringBuilder();
        // Erstes Element ist, so gesetzt, der Name, gefolgt von ":"
        if (name != null) {
        	sb.append(name + ":");
        }
        sb.append("[");
    	if (this.size() > 0) {
            // Die Elemente werden in ein Array kopiert und sortiert.
            // So ist sichergestellt, dass sie in der richtigen (aufsteigenden)
            // Reihenfolge aneinandergehängt werden.
    		GTVertexInfo[] asArray = this.toArray(new GTVertexInfo[0]);
    		Arrays.sort(asArray);
            for (int i = 0; i < asArray.length; i++) {
    			if (i < asArray.length - 1) {
    				// aktuellen Eintrag und ", " anhängen.
    				sb.append(asArray[i] + ", ");
    			}
    			else {
    				// Beim letzten Eintrag wird kein "," angehängt
    				sb.append(asArray[i]);
    			}
    		}		
    	}
        return sb.append(']').toString();
    }

	/**
	 * Fügt einen Beobachter hinzu, der über Änderungen bei den Elementen
	 * informiert wird.
	 */
	@Override
	public boolean addObserver(CollectionObserver<GTVertexInfo> observer) {
		return observers.add(observer);
	}
	
	/**
	 * Entfernt einen Beobachter aus der Liste. 
	 */
	@Override
	public boolean removeObserver(CollectionObserver<GTVertexInfo> observer) {
		return observers.remove(observer);
	}
	
}
