package graphtool.gui.components;

import javax.swing.JPanel;

import graphtool.gui.GTGraphControl;
import graphtool.gui.GTGraphVisualization;

/**
 * Gemeinsame Superklasse für GTBuildPanel und GTControlPanel.
 * Über diese gemeinsame Basis erfolgt der Zugriff auf die Hauptkomponenten
 * der grafischen Benutzeroberfläche, nämlich<ul>
 * <li>die Visualisierungskomponente für den Graphen
 * <li>das BuildPanel
 * <li>das ControlPanel
 * <li>das SourcePanel und
 * <li>das InfoPanel
 * </ul>
 */
public abstract class GTAbstractPanel extends JPanel {

	private static final long serialVersionUID = -5224478215580788396L;
	
	/**
	 * Haupt-Panel für die Manipulation von Graphen. Über dieses erfolgt
	 * auch die gegebenenfalls notwendige Kommunikation der einzelnen
	 * Panels untereinander bzw. der Zugriff auf gemeinsam genutzte Elemente.
	 */
	private GTGraphControl graphControl = null;
	
	/**
	 * Konstruktor. Weist dem Feld {@link #graphControl} den Wert des
	 * Parameters zu.
	 * @param mainPanel Objekt von Typ GTGraphControl, über das die Kommunikation erfolgt.
	 */
	public GTAbstractPanel(GTGraphControl mainPanel) {
		this.graphControl = mainPanel;
	}

	/**
	 * Liefert die Visualisierungskomponente für Graphen zurück.
	 * @return die Visualisierungskomponente für Graphen
	 * @see GTGraphControl#getGraphVis()
	 */
	public GTGraphVisualization getGraphVis() {
		return graphControl.getGraphVis();
	}

	/**
	 * Liefert das Panel mit den Steuerelementen zur Manipulation des Graphen zurück.
	 * @return das Build-Panel
	 * @see GTGraphControl#getBuildPanel()
	 */
	public GTBuildPanel getBuildPanel() {
		return graphControl.getBuildPanel();
	}

	/**
	 * Liefert das Panel mit den Steuerelementen zur Durchführung des Algorithmus zurück.
	 * @return das Control-Panel
	 * @see GTGraphControl#getControlPanel()
	 */
	public GTControlPanel getControlPanel() {
		return graphControl.getControlPanel();
	}
	
	/**
	 * Liefert das Panel mit der Anzeige des Quellcodes zurück.
	 * @return das Source-Panel
	 * @see GTGraphControl#getSourcePanel()
	 */
	public GTSourcePanel getSourcePanel() {
		return graphControl.getSourcePanel();
	}
	
	/**
	 * Liefert das Panel mit der Anzeige von Informationen zur Durchführung des Algorithmus
	 * @return das Info-Panel
	 * @see GTGraphControl#getInfoPanel()
	 */
	public GTInfoPanel getInfoPanel() {
		return graphControl.getInfoPanel();
	}
	
}
