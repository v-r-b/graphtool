package graphtool.gui.components;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.concurrent.CompletionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import graphtool.algorithm.AStarAlgorithm;
import graphtool.algorithm.DijkstraAlgorithm;
import graphtool.algorithm.GTAbstractAlgorithm;
import graphtool.graph.GTEdge;
import graphtool.graph.GTVertex;
import graphtool.gui.GTGraphControl;
import graphtool.gui.GTGraphVisualization;
import graphtool.res.Messages;
import graphtool.utils.ActionButton;
import graphtool.utils.CodeExecutionHandler;
import graphtool.utils.CollectionObserver;
import graphtool.utils.Configuration;
import graphtool.utils.TextComponentPrintStream;

/**
 * Komponente zum Steuern des Ablaufs der Suchalgorithmen. 
 * Im Konstruktor wird die Liste der zu verwendenden Algorithmen aus der
 * Konfiguration gelesen und die Algorithmenklassen werden geladen
 * und instanziiert.
 * <p>
 * GTControlPanel besteht aus Schaltflächen zum Starten, Unterbrechen und Stoppen
 * des Suchalgorithmus. Außerdem ist eine Geschwindigkeitsänderung bei der
 * Ausführung sowie eine Einzelschrittdurchführung möglich.
 */
public class GTControlPanel extends GTAbstractPanel 
							implements CodeExecutionHandler.StartStopListener {
	
	private static final long serialVersionUID = -3761066085195868076L;

	/** 
	 * Basisschlüssel, um die zu verwendenden Algorithmen aus der Konfigurationsdatei 
	 * zu lesen. Die Schlüssel bilden sich aus &lt;Basisschlüssel&gt;.&lt;num&gt;, wobei
	 * num eine Ganzzahl ist, beginnend bei 0 und fortlaufend. 
	 * <p>
	 * Beispiel: Algorithm.0=graphtool.algorithm.DijkstraAlgorithm
	 */
	public static final String ALGORITHM_BASE_KEY = "Algorithm"; //$NON-NLS-1$
	
	/** Liste aller verwendeter Algorithmen */
	private GTAbstractAlgorithm[] algorithms = null;
	/** Thread, in dem der Algorithmus ausfegührt wird. */
	private Thread algT = null;
	/** Implementierung des Algorithmus (in der run()-Methode. */
	private GTAbstractAlgorithm alg = null;
	/** Nachverfolgung und Steuerung des Programmablaufs */
	private CodeExecutionHandler cxh = null;

	/** Auswahl des zu verwendenden Algorithmus */
	private JComboBox<String> algChooser = null;
	/** Start-Button für die Durchführung des Algorithmus */
	private JButton startButton = null;
	/** Beschrftung des StartButtons vor dem Start */
	private final String startButtonTextStart = Messages.getString("GTControlPanel.LaunchRun"); //$NON-NLS-1$
	/** Beschrftung des StartButtons während des Laufs */
	private final String startButtonTextRunning = Messages.getString("GTControlPanel.IsRunning"); //$NON-NLS-1$

	/** Button zum Umschalten zwischen Lauf und Pause */
	private JButton pauseButton = null;
	/** Beschrftung des PauseButtons während des Laufs */
	private final String pauseButtonTextRunning = Messages.getString("GTControlPanel.Pause"); //$NON-NLS-1$
	/** Beschrftung des PauseButtons während des Haltens */
	private final String pauseButtonTextWaiting = Messages.getString("GTControlPanel.Continue"); //$NON-NLS-1$
	/** Wartezeit zwischen zwei Programmschritten beim Ablauf des Programms */
	private int sleepMillis = 1000;
	/** Gibt an, ob die automatische Ausführung unterbrochen ist. Dann
	 *  kann per Hand der jeweils nächste Schritt ausgelöst werden. */
	private boolean pause = false;

	/** Stopp-Button zum Abbrechen des Algorithmus */
	private JButton stopButton = null;
	/** Beschriftung des StopButtons */
	private final String stopButtonText = Messages.getString("GTControlPanel.CancelRun"); //$NON-NLS-1$
	/** Gibt an, ob die Ausführung abgebrochen werden soll. */
	private boolean stop = false;
	/** Meldung bei Abbruch des Durchlaufs aufgrund von Änderungen am Graphen */
	private final String changeStopText = Messages.getString("GTControlPanel.GraphChangeStopMessage"); //$NON-NLS-1$
	
	/** Beschriftung des Buttons zur Ausführung des nächsten Schritts */
	private final String stepButtonText = Messages.getString("GTControlPanel.TakeOneStep"); //$NON-NLS-1$
	
	/**
	 * Lädt die zu verwendenden Algorithmen und erstellt die grafischen Elemente für
	 * die Steuerung (per Aufruf von initialize()). 
	 * @see initialize
	 */
	public GTControlPanel(GTGraphControl mainPanel) {
		super(mainPanel);
	
		// Die Algorithmen laut Konfiguration laden
		algorithms = readAlgorithms();
		
		// Sofern keine gefunden wurden, Minimaleinstellung verwenden
		// Dies sind der Dijkstra- und der A*-Algorithmus
		if (algorithms == null) {
			algorithms = new GTAbstractAlgorithm[] {
					new DijkstraAlgorithm(getGraphVis(), getInfoPanel(), getInfoPanel().getTextPrintStream()),
					new AStarAlgorithm(getGraphVis(), getInfoPanel(), getInfoPanel().getTextPrintStream())
			};
		}

		// Grafische Komponenten erstellen
		initialize();

        getGraphVis().getGraph().addGraphEventListener(new GraphEventListener<GTVertex, GTEdge>() {
			/** 
			 * Wird während des Laufs des Algorithmus ein Knoten oder eine Kante gelöscht 
			 * oder hinzugefügt, so wird der Algorithmus beim nächsten Ausführungsschriff gestoppt.
			 * @param evt GraphEvent, der über die Änderung im Graphen informiert
			 */
        	@Override
			public void handleGraphEvent(GraphEvent<GTVertex, GTEdge> evt) {
        		// Wenn der Algorithmus läuft --> stoppen!
        		if ((algT != null) && algT.isAlive()) {
        			stop = true;
        			alg.showInfo(changeStopText, true);
        		}
        	}
        });

		// Verwendung des ersten Algorithmus in der Liste vorbereiten
		prepareForAlgorithm(algChooser.getSelectedIndex());
	}

	/** 
	 * Liest die zu verwendenden Algorithmen aus der Konfigurationsdatei.
	 * Dazu wird der Basisschlüssel {@link #ALGORITHM_BASE_KEY} verwendet, um
	 * die Einträge zu finden (Bildungsvorschrift: siehe dort).
	 * Die Werte zu den Schlüsseln sind die voll qualifizierten Klassennamen
	 * der Klassen, die die Algorithmen implementieren.<p>
	 * Beispiel: Algorithm.0=graphtool.algorithm.DijkstraAlgorithm. 
	 * Die Methode liefert null, wenn keine Algorithmen geladen werden konnten.
	 * @return Array von Algorithmen, die verwendet werden sollen, oder null.
	 * @see #newAlgorithm(String)
	 */
	private GTAbstractAlgorithm[] readAlgorithms() {
		// Ergbnisliste; Größe noch offen
		ArrayList<GTAbstractAlgorithm> algList = new ArrayList<>();
		// Liste der Klassennamen aus der Konfiguration laden
		String[] algClassNames = Configuration.getDefaultInstance().getNumbered(ALGORITHM_BASE_KEY, true);
		// Falls mindestens ein Eintrag gefunden wurde:
		if (algClassNames != null) {
			// Alle Einträge durchgehen; Klassen laden und instanziieren
			for (String className : algClassNames) {
				GTAbstractAlgorithm a = newAlgorithm(className);
				if (a != null) {
					// Eintrag dem Ergebnis hinzufügen, sofern erfolgreich geladen
					algList.add(a);
				}
			}
		}
		return algList.size() > 0 ? algList.toArray(new GTAbstractAlgorithm[0]) : null;
	}
	
	/**
	 * Erzeugt ein Objekt zum übergebenen Klassennamen. Die geladene Klasse muss
	 * eine Subklasse von GTAbstractAlgorithm sein und über einen Konstruktor verfügen,
	 * dessen einziger Parameter vom Typ {@link GTGraphVisualization} ist.
	 * @param className Name der zu instanziierenden Klasse
	 * @return Objekt dieser Klasse oder null bei Misserfolg
	 */
	private GTAbstractAlgorithm newAlgorithm(String className) {
		try {
			// Klasse laden
			Class<?> cls = getClass().getClassLoader().loadClass(className);
			// Konstruktor mit den richtigen Parametern suchen
			Constructor<?> con = cls.getConstructor(
					GTGraphVisualization.class, 
					CollectionObserver.class, 
					TextComponentPrintStream.class);
			// Neu erzeugtes Objekt zurückgeben.
			return (GTAbstractAlgorithm)con.newInstance(
					getGraphVis(), 
					getInfoPanel(), 
					getInfoPanel().getTextPrintStream());
		} catch (Exception e) {
			// Eintrag war fehlerhaft
			System.err.println(className + 
					" doesn't seem to be a valid subclass of " + //$NON-NLS-1$ 
					GTAbstractAlgorithm.class.getName());
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Die Ausführungsumgebung wird zur Verwendung des angegebenen
	 * Algorithmus vorbereitet. Dazu gehören z.B. das Setzen des richtigen 
	 * CodeExecutionHandlers, das Laden des Sourcecodes und das Registrieren des InfoPanels
	 * als Observer der vom Algorithmus verwendeten Collections.
	 * @param algNum laufende Nummer im Array {@link #algorithms}
	 */
	private void prepareForAlgorithm(int algNum) {
		alg = algorithms[algNum];
		// Markierungen etc. im Graphen sowie die Inhalte der Infoanzeige löschen
		getGraphVis().clearSettings();
		getInfoPanel().clear();
		// Den CodeExecutionHandler besorgen. Dieser bietet die Möglichkeit, die
		// Ausführung des Algorithmus zu beeinflussen und zu verfolgen.
		// Dazu muss der SourceCode entsprechend präpariert werden.
		cxh = alg.getCodeExecutionHandler();
		cxh.clearStartStopListeners();
		cxh.addStartStopListener(this);
		
		// Den Quellcode der untersuchten Methode sowie den Pseudocode in die Quellcodeanzeige laden.
		getSourcePanel().useText(cxh.getCodeSectionJavaCode(), cxh.getCodeSectionPseudoCode());
		// Überall, wo im Quellcode des Algorithmus die Methode bp() des 
		// CodeExecutionHandlers aufgerufen wird, werden alle zuvor registrierten 
		// Aktionen ausgeführt.
		// Der Mechanismus für die verlangsamte automatisierte und für die schrittweise
		// Durchführung wird hier als Aktion registriert.  
		cxh.clearActions();
		cxh.addAction((String line, Integer lineNum) ->  {
			// Übergeben wurden der Text der Codezeile (wird ignoriert) und
			// die Zeilennummer innerhalb der Quelldatei. Daraus zunächst die
			// Zeilennummer innerhalb der Methode berechnen und diese Zeile 
			// in der Sourcecode-Anzeige selektieren (Ablaufanzeige).
			// Die Berechnung findet sowohl für Java- als auch für Pseudocode statt.
			// Achtung: Die Zeilennummern im Programm sind 1-basiert, die innerhalb
			// der TextArea aber 0-basiert, daher jeweils " - 1".
			getSourcePanel().setSelectionLines(
					cxh.getLineNumInCodeSection(lineNum.intValue()) - 1,
					cxh.getLineNumInPseudoCode(lineNum.intValue()) - 1);
			// Den Graphen neu zeichnen. Dies wird erledigt, damit alle Schritte
			// des Algorithmus, die das Aussehen des Graphen beeinflussen, auch
			// sichtbar werden, z.B. die Darstellung des aktuellen Knotens oder der
			// aktuellen Kante.
			getGraphVis().getGraphPanel().repaint();
			// Falls der Stop-Button betätigt wurde (und stop damit == true ist):
			if (stop) {
				// An den Anfang springen
				getSourcePanel().setSelectionLines(0, 0);
				// Infoanzeige leeren
				getInfoPanel().clear();
				// Graphen neu zeichnen
				getGraphVis().getGraphPanel().repaint();
				// Thread, in dem der Algorithmus läuft, durch Excpetion beenden
		 		// Die Exception wird in der run()-Methode des Algorithmus gefangen.
				throw new CompletionException("Stop Button Pressed", new InterruptedException()); //$NON-NLS-1$
			}
			try { 
				// Falls der Pause-Button bedrückt wurde (und damit pause == true ist):
				if (pause) {
					// Wenn das Pausieren aktiv ist, soll nicht weitergemacht werden,
					// bevor nicht ein Interrupt an den Thread gesendet wird, in dem
					// der Algorithmus läuft. Dies kann z.B. durch das Drücken einer
					// Schaltfläche erfolgen.
					// De facto wird das "ewige" Warten nach MAX_VALUE Millisekunden
					// unterbrochen. So lange wird der Rechner nicht halten.
					Thread.sleep(Long.MAX_VALUE);
				}
				// Bevor der Pause-Button gedrückt wurde bzw. nach dem Drücken des Weiter-Buttons:
				else {
					// Im verlangsamten Automatisierten Betrieb wartet der Thread
					// mit der Ausführung des auf einen Aufruf von bp() folgenden
					// Schritts sleepMillis Millisekunden. Der Wert kann zur Laufzeit
					// angepasst werden, um die Durchführung zu beschleunigen oder
					// zu verzögern. Auch in diesem Modus ist eine Fortsetzung von
					// Hand durch einen Interrupt möglich.
					Thread.sleep(sleepMillis);
				}
			} 
			catch (InterruptedException e) {
				// Hier ist nichts zu tun. Es ist lediglich wichtig, dass eine
				// Unterbrechung möglich ist. Darauf muss nicht besonders
				// reagiert werden. Das Programm läuft dann einfach weiter.
			}
		});
	}
	
	/**
	 * Gibt an, ob die Voraussetzungen für das Starten des Suchalgorithmus vorliegen.
	 * Insbesondere geht es darum festzustellen, ob ggf. benötigte Start- und
	 * Zielknoten ausgewählt wurde.
	 * @return true wenn ein Start möglich ist.
	 */
	@SuppressWarnings("resource")
	private boolean canStartAlgorithm() {
		boolean askForStart, askForTarget;
		TextComponentPrintStream infoOut = getInfoPanel().getTextPrintStream();
		
		// Wird ein Startknoten benötigt und ist noch nicht gesetzt?
		askForStart = alg.needsStartNode() && (getGraphVis().getStartVertex() == null);
		// Wird ein Zielknoten benötigt und ist noch nicht gesetzt?
		askForTarget = alg.needsTargetNode() && (getGraphVis().getTargetVertex() == null);
		// Passende Meldung anzeigen, sofern etwas fehlt.
		if (askForStart) {
			if (askForTarget) {
				// Beide Knoten müssen noch selektiert werden
				infoOut.clear();
				infoOut.println(Messages.getString("GTControlPanel.ChooseStartAndTargetNodesFirst")); //$NON-NLS-1$
			}
			else {
				// Nur der Startknoten muss noch selektiert werden
				infoOut.clear();
				infoOut.println(Messages.getString("GTControlPanel.ChooseStartNodeFirst")); //$NON-NLS-1$
			}
		}
		else if (askForTarget) {
			// Nur der Zielknoten muss noch selektiert werden
			infoOut.clear();
			infoOut.println(Messages.getString("GTControlPanel.ChooseTargetNodeFirst")); //$NON-NLS-1$
		}
		// Liefert true, wenn keine der benötigten Knotenangaben fehlt.
		return !(askForStart || askForTarget);
	}

	/**
	 * Erzeugt die grafischen Elemente für die Steuerung des Algorithmus.
	 */
	private void initialize() {
		ActionButton button;

		// Die Elemente werden, alle in gleicher Größe, untereinander angeordnet.
		setLayout(new GridLayout(4, 1));

		// Auswahl des Algorithmus ermöglichen. 
		algChooser = new JComboBox<>();
		for (GTAbstractAlgorithm a : algorithms) {
			algChooser.addItem(a.getName());
		}
		add(algChooser);
		algChooser.setSelectedIndex(0);
		// Wird eine Auswahl getroffen, wird die Umgebung für den
		// ausgewählten Algorithmus vorbereitet
		algChooser.addItemListener((ItemEvent evt) -> {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				prepareForAlgorithm(algChooser.getSelectedIndex());
			}
		});

		// Je ein Button zum Starten und Beenden des Threads, in dem der Algorithmus läuft. 
		JPanel startStopPane = new JPanel(new GridLayout(1, 2));
		startButton = new ActionButton(startButtonTextStart, 
				(ActionEvent evt) -> {
					// Prüfen, ob die Voraussetzungen für einen Start gegeben sind.
					if(canStartAlgorithm()) {
						// Such-Algorithmus in einem eigenen Thread ausführbar machen.
						// Dies ist notwendig, um die Thread-Mechanismen zur Unterbrechung und
						// Fortsetzung des Programmablaufs nutzen zu können.
						algT = new Thread(null, alg, alg.getClass().getSimpleName());
						stop = false;
						getInfoPanel().clear();
						algT.start();
					}
				});
		startButton.setToolTipText(Messages.getString("GTControlPanel.LaunchRunTooltip")); //$NON-NLS-1$
		startStopPane.add(startButton);
		stopButton = new ActionButton(stopButtonText,
				(ActionEvent evt) -> {
					// Algorithmus abbrechen.
					stop = true;
					// Thread unterbrechen für den Fall, dass er gerade in einer
					// Warteschleife ist, da ansonsten das Betätigen von "Stopp" erst
					// dann einen Effekt hätte, wenn die Wartezeit abgelaufen ist oder
					// der nächste Programmschritt per "Einen Schritt weiter"
					// angestoßen würde.
					algT.interrupt();
				});
		// Stop kann erst ausgewählt werden, wenn der Algorithmus läuft
		stopButton.setEnabled(false);
		stopButton.setToolTipText(Messages.getString("GTControlPanel.CancelRunTooltip")); //$NON-NLS-1$
		startStopPane.add(stopButton);
		add(startStopPane);

		// Als nächstes werden drei Buttons nebeneinander angeordnet, die eine
		// Beschleunigung, ein Pausieren/Fortsetzen und eine Verlangsamung
		// des Programmablaufs steuern. Zur Anordnung wird ein waagerechtes
		// GridLayout verwendet. Hier sind alle Elemente gleich breit.
		JPanel speedPane = new JPanel(new GridLayout(1, 3));
		button = new ActionButton(Messages.getString("GTControlPanel.Slower"),  //$NON-NLS-1$
				(ActionEvent evt) -> {
					// Wartezeit um 100 Millisekunden verlängern
					sleepMillis += 100;
				});
		button.setToolTipText(Messages.getString("GTControlPanel.SlowerTooltip")); //$NON-NLS-1$
		speedPane.add(button);

		pauseButton = new ActionButton(pauseButtonTextRunning,
				(ActionEvent evt) -> {
					// Pausezustand wechseln
					setPauseMode(!pause);
					if (!pause) {
						// Wenn der Thread läuft, dann Interrupt senden, sonst nichts tun.
						if ((algT != null) && (algT.isAlive())) {
							// Fortsetzung durch einen Interrupt anstoßen.
							algT.interrupt();
						}
					}
				});
		pauseButton.setToolTipText(Messages.getString("GTControlPanel.PauseContinueTooltip")); //$NON-NLS-1$
		speedPane.add(pauseButton);

		button = new ActionButton(Messages.getString("GTControlPanel.Faster"), //$NON-NLS-1$
				(ActionEvent evt) -> {
					if (sleepMillis >= 100) {
						// Wartezeit um 100 Millisekunden verkürzen, wenn noch möglich
						sleepMillis -= 100;
					}
				});
		button.setToolTipText(Messages.getString("GTControlPanel.FasterTooltip")); //$NON-NLS-1$
		speedPane.add(button);

		// Die drei Buttons nebeneinander werden dem senkrechten GridLayout hinzugefügt.
		add(speedPane);

		// Zuletzt ein Button für die Ausführung des nächsten Programmschritts
		button = new ActionButton(stepButtonText, 
				(ActionEvent evt) -> {
					// Wird die Schaltfläche "Einen Schritt weiter" betätigt, so soll
					// unmittelbar der nächste Programmschritt ausgeführt werden. Dazu
					// wird ein Interrupt an den Thread geschickt.
					if ((algT != null) && (algT.isAlive())) {
						// In diesem Fall läuft der Algorithmus gerade und kann
						// den nächsten Schritt ausführen (sleep unterbrechen).
						algT.interrupt();
					}
					else {
						// Falls der Algorithmus nicht läuft, muss er gestartet werden.
						// Der Ausführungsmodus ist dann "Pause"
						setPauseMode(true);
						startButton.doClick();
					}
				});
		button.setToolTipText(Messages.getString("GTControlPanel.TakeOneStepTooltip")); //$NON-NLS-1$
		add(button);
	}

	/**
	 * Setzt den Wert des pause-Felds und aktualisiert die Beschriftung
	 * des Pause-Buttons.
	 * @param pause true, wenn der Pausemodus aktiviert wird, false sonst.
	 */
	public void setPauseMode(boolean pause) {
		this.pause = pause;
		pauseButton.setText(pause ? pauseButtonTextWaiting : pauseButtonTextRunning);
	}

	/**
	 * Wird vom CodeExecutionHandler beim Start der behandelten Methode
	 * aufgerufen. Sorgt für die richtige Beschriftung und Bedienbarkeit
	 * von Algorithmenauswahl und Start- und Stopp-Button:<ul>
	 * <li>die Algorithmenauswahl wird deaktiviert,
	 * <li>der Start-Button wird deaktiviert und mit {@link #startButtonTextRunning} beschriftet,
	 * <li>der Stopp-Button wird aktiviert.</ul>
	 */
	@Override
	public void codeSectionStarted(String methodName) {
		algChooser.setEnabled(false);
		startButton.setEnabled(false);
		startButton.setText(startButtonTextRunning);
		stopButton.setEnabled(true);
		
	}

	/**
	 * Wird vom CodeExecutionHandler am Ende der behandelten Methode
	 * aufgerufen. Sorgt für die richtige Beschriftung und Bedienbarkeit
	 * von Algorithmenauswahl und Start- und Stopp-Button:<ul>
	 * <li>die Algorithmenauswahl wird deaktiviert,
	 * <li>der Start-Button wird aktiviert und mit {@link #startButtonTextStart} beschriftet,
	 * <li>der Stopp-Button wird deaktiviert.</ul>
	 */
	@Override
	public void codeSectionTerminated(String methodName) {
		algChooser.setEnabled(true);
		startButton.setEnabled(true);
		startButton.setText(startButtonTextStart);
		stopButton.setEnabled(false);
		// Pausemodus ausschalten (könnte noch aus einem vorhergehenden Durchlauf gesetzt sein)
		setPauseMode(false);
	}
}

