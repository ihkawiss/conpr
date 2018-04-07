# CONPR Summary
Persönliche Zusammenfassung im Modul conpr HS18. Irrtum vorbehalten.

## Einführung

### Amdahl's Law

Die Beschleunigung einer Arbeit, mittels mehreren Kernen (Parallelisierung), kann wie folgt berechnet werden.   

**p** *= Paralell-Anteil* &nbsp;&nbsp; **s** *= Anzahl Prozessoren*  

<img src="https://www.spiria.com/sites/default/files/blog/equation_amdahl-en.png" height=100/>

### Concurrent vs Parallel

Geteilte Arbeit kann grundsätzlich auf zwei Arten von Arbeitern (Threads) ausgeführt werden, concurrent (gleichzeitig) oder paralell.

**Concurrent Program**

- kontrolliert mehrere logische Threads
- viele Dinge gleichzeitig handhaben
- behandeln von Ereignissen die gleichzeitig auftreten können
- Verhalten oft nicht deterministisch

**Paralell Program**  

- verschiedene Arbeiten werden wirklich paralell ausgeführt
- viele Dinge gleichzeitig ausführen
- Problem muss in Teilstücke gebrochen werden
- Verhalten deterministisch

### Risiken und Probleme

- Sicherheit
	- Schlimmes passiert nie (beim Testen)
	- Race Conditions
- Lebendigkeit
	- Gutes passiert eventuell (nicht deterministisch)
	- Deadlock, Livelock, Starvation
- Performance
	- Zu viel Synchronisierung (Amdahl's Law)
	- Context-Switches sind teuer
- Testing
	- Tests sind nicht verlässlich
	- Scheduling ist nicht deterministisch
- Debugging
	- Ist oft unmöglich
	- Heisenbugs (Bug verschwindet wenn man ihn beobachten will)

## Threads

### Prozesse und Threads

- **Ein Prozess ist ein ausführbares Programm im Speicher**
	- isolierter Speicherraum alloziert vom OS
	- Prozesswechsel (Context-Switch) sind sehr teuer
	- Kommunikation via OS (Inter-Prozess-Kommunikation)
	- Kann mehrere Threads beinhalten

- **Ein Thread ist ein gekapselter sequentieller Ablauf**
	- lebt im Adressraum des (Eltern)-Prozesses
	- hat eigenen Ausführungs-Kontext
	- kommuniziert mit anderen Threads über Shared-Memory


### Threading Modes

- Kernel-Level
	- Scheduler des Kernels kontrolliert Threads
	- Kernel bestimmt wann ein Thread CPU-Zeit hat
	- Von den meisten JVM Implementationen genutzt

- User-Level
	- Threads werden von einer Library verwaltet
	- Effiziente Context-Switches (keine Kernel Privilegien)
	- Scheduling durch Applikation supportet
	- Verschiedene Threads laufen nicht auf unterschiedlichen Prozessoren

- Hybrid-Threads
	- Mehrere User-Level-Threads werden weniger Kernel-Level Threads zugewiesen

### Java Threads

Ein Thread besteht aus einer Arbeiter (Thread) und Arbeit (Runnable). Folgend wie ein Thread erstellt werden kann:

```java
// Definition der Arbeit
public class Work implements Runnable { 
	private int nr;

	public R(int nr) { 
		this.nr = nr; 
	} 
	
	@Override
	public void run() {
		for(int i = 0; i < 10; i++) {
			System.out.println(
			"Hello" + nr + " " + i);
		}
	}
}

// Instanzierung der Arbeit
Work w = new Work(100);

// Instanzierung eines Arbeites mit Arbeit als Argument
Thread t1 = new Thread(w);

// Starten des Threads, returnt unverzüglich
t1.start();

// ALTERNATIVE IMPLEMENTATION
// Die Klasse Thread implementiert das Interface Runnable, 
// somit kann von der Klasse Thread geerbt sowie run() überschrieben werden.

// LAMDA EXPRESSION
// Da Runnable ein functional interface ist kann run() mittels LAMDA definiert werden
Thread lamda = new Thread(() -> {
	System.out.println("Ich bin der wohl sinnloseste Thread aller Zeiten.");
});

Thread methodSignature = new Thread(() -> doWork("paramA", 100));
public void doWork(String param, int magicNumber) { ... }
``` 

#### Thread API

**start()**  
- Startet den Thread

**run()**  
- Jener Code der die Arbeit representiert

**sleep(long millis [, int nanos])**  
- legt den Thread für m Millisekunden schlafen

**yield()**  
- signalisiert dem Scheduler, dass der Thread bereit ist CPU-Zeit abzugeben (Scheduler kann das ignorieren)

**join([long milliseconds])**  
- Wartet auf den Prozess (Time-Out in Millisekunden)

**setDeamon(boolean)**  
- Markiert den Thread als Deamon (blockieren JVM nicht, wenn nur noch Deamons -> JVM beendet)

**Thread.getCurrentThread()**
- Gibt aktuellen Thread zurück

**long getId()**  
- Gibt die ID eines Threads zurück

**String getName()**  
- Gibt den Namen eines Threads zurück

**boolean isDeamon()**  
- Teilt mit ob Thread ein Deamon ist oder nicht

**State getState()**  
- Gibt den Status des Threads zurück

**boolean isAlive()**  
- Gibt an ob der Thread noch läuft