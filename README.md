## GraphTool

Ein didaktisches Werkzeug zum Erlernen des Dijkstra-Algorithmus. Es wird veröffentlicht unter der GPL 3.0 (siehe LICENSE-Datei).

    Copyright (C) 2022  Volker Bürckel

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.


## Kurzfassung

Im Rahmen einer Abschlussarbeit im Studiengang Master of Science in Praktischer Informatik an der FernUniversität in Hagen wurde ein didaktisches Werkzeug entwickelt, das beim Erlernen des Dijkstra-Algorithmus und anderer, ähnlicher Suchalgorithmen hilfreich eingesetzt werden kann. Es erlaubt in der vorgelegten Fassung die Generierung und Visualisierung von Graphen sowie die Ausführung des Dijkstra- und des A*-Algorithmus, jeweils bei Darstellung der ausgeführten Programmschritte wahlweise in Java- oder Pseudocode. 

Das Werkzeug ist erweiterbar konstruiert, so dass weitere Algorithmen hinzugefügt werden können oder, beispielsweise für den A*-Algorithmus, andere Heuristiken. Quellcode und Javadoc sind nicht Bestandteil der gedruckten Arbeit, sondern liegen hier auf Github vor.

## Benutzung

Zur Ausführung der Anwendung kann das Repository geklont und lokal verwendet werden. Die Klasse mit der main()-Methode heißt "graphtool.GraphTool" Im Klassenpfad müssen sich folgende Bibliotheken befinden:

 - [failureaccess-1.0.1.jar](https://search.maven.org/search?q=a:failureaccess)
 - [guava-29.0-jre.jar](https://search.maven.org/search?q=g:com.google.guava%20AND%20v:29.0-jre)
 - [guava-gwt-29.0-jre.jar](https://search.maven.org/search?q=g:com.google.guava%20AND%20v:29.0-jre)
 - [json-20211205.jar](https://search.maven.org/search?q=g:org.json%20AND%20a:json)
 - [jung-algorithms-2.1.1.jar](https://search.maven.org/search?q=g:net.sf.jung%20AND%20v:2.1.1)
 - [jung-api-2.1.1.jar](https://search.maven.org/search?q=g:net.sf.jung%20AND%20v:2.1.1)
 - [jung-graph-impl-2.1.1.jar](https://search.maven.org/search?q=g:net.sf.jung%20AND%20v:2.1.1)
 - [jung-io-2.1.1.jar](https://search.maven.org/search?q=g:net.sf.jung%20AND%20v:2.1.1)
 - [jung-visualization-2.1.1.jar](https://search.maven.org/search?q=g:net.sf.jung%20AND%20v:2.1.1)

Alternativ kann die ausführbare JAR-Datei `graphtool.jar` verwendet werden. Sie erwartet die o.g. Bibliotheken in einem Unterverzeichnis `graphtool_lib` des Verzeichnisses, in dem `graphtool.jar` liegt. Der Start erfolgt dann einfach mittels `java -jar graphtool.jar`. 

Die Anwendung setzt Java 8 voraus. Im Arbeitsverzeichnis muss sich die Datei `graphtool.cfg` befinden.
