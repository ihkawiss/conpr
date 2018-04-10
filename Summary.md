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

#### Uncaught Exceptions

```java
interface UncaughtExceptionHandler {
	void uncaughtException(Thread t, Throwable e);
}

// instance
t1.setUncaughtExceptionHandler(...);

// static
Thread.setDefaultUncaughtExceptionHandler(...);
```

## Locks

### Race Conditions

Bearbeiten mehrere Threads gleichzeitig ein und dieselbe Ressource, wie z.B. einen Counter (Integer), so kann dies zu inkonsistenten Zuständen führen - dies bezeichnet man als Race Conditions. Klassisches Beispiel ist die Bank, wobei gleichzeitig ein Thread eine Einzahlung und ein anderer eine Abhebung durchführt. Beide werden den Kontostand (Integer/Float) aktualisieren. Wird dieser von einem Thread gelesen, kann der Kontostand in der Zwischenzeit jedoch schon wieder geändert haben, wodurch die Aktion zum falschen Kontostand führen kann (Lost-Update). Die bearbeitete Ressource ist also nicht Thread-Safe, nicht dagegen geschützt.

### Synchronization

Um Race Condtions auf **shared, mutable state** zu verhindern kann Synchronization verwendet werden.

**state**: Daten im Heap, instance und static fields  
**mutable**: Variable kann in ihrem "Leben" ändern  
**shared**: Variable kann von mehrern Threads verwendet werden

### Synchronization mit Java Features

Mit dem Keyword **synchronized** wird in Java auf einem Objekt ein Lock erstellt, also ```lock.lock()``` ausgegührt. Der Lock wird beim Eintreten (**monitorenter**) in den synchronized Block vergeben. Wenn Lock vergeben ist kommt ein Thread in eine Warteschlange. Der Lock wird freigegeben sobald der synchronized Block verlassen wird (**monitorexit**), dies auch bei einer Exception.

```java
int i = 0;
public void threadSafe() {
	synchronized (this) { // monitorenter
		i++; // atomic here!
	} // monitorexit
} 

// Kurzformen
public synchronized void threadSafe() {...} // equal to synchronized(this)
public static synchronized void doSomeSpookyStuff() {...} // equal to synchronized(Ghost.class)
```

### Reentrancy of Synchronized

Ein synchronized Block in Java ist immer **reentrant**, was bedeutet, dass ein Thread welcher einen Lock hält jeden Lock öffnen kann welcher mit dem selben Objekt synchronisiert wurde.

```java
synchronized(x) {
   synchronized(x) { /* no deadlock */ }
}

synchronized f() { g(); }
synchronized g() { /* no deadlock */ }
```

### Atomarität

- **Synchronized Blocks auf dem gleichen Lock**
	- werden atomar ausgeführt

- **Synchronized Blocks auf verschiedenen Locks**
	- werden nicht atomar ausgeführt, Interleavings also möglich

### Design von Locks

- ```synchronized(this)```
	- Implementationsdetails einsehbar
	- Macht Code angreifbar

- ```synchronized(lock)```
	- lock object kann private sein
	- explizit
	- oft zu bevorzugen

### Synchronization mit Locks

```java.util.concurrent.locks.Lock``` bietet mehr Flexibilität im Umgang mit Locks als dies ```synchronized``` tut.

- Fairness
- Non-blocking locking Strukturen
- Thread kann lock zeitlich abgestimmt erhalten
- Thread kann prüfen ob Lock verfügbar ist und in dann entgegennehmen

```java
public interface Lock {
   void lock();
   void unlock();
	boolean tryLock();
	boolean tryLock(long timeout, TimeUnit unit)
}

final Lock lock = ...;
...
lock.lock();
try {
   // access resources protected by this lock
}
finally {
   lock.unlock();
}
```

#### ReentrantLock

- ```lock``` returnt sofort wenn Thread lock bereits hält
- Weitere Methoden auf einem ReentrantLock
	- ```Thread getOwner()``` Gibt den Thread zurück der den Lock aktuell hält
	- ```boolean isHeldByCurrentThread()``` Ob der Lock durch den aktuellen Thread gehalten wird
	- ```int getHoldCount()``` Locks die der Thread auf diesem Lock hält
	- ```int getQueueLentgh()``` Threads die warten
- **Lock muss immer durch jenen Thread freigegeben werden der ihn auch erhalten hat**

### Deadlocks

Unter den folgenden Umständen können Deadlocks auftreten. Zur Verhinderung mind. eine Bedingung verhindern.

- **Mutal Exclusion** (Mutex=synchronized)
	- Zugriff auf Resource ist exklusiv
- **Hold and Wait**
	- Threads verlangen nach weitern Ressourcen wenn Sie schon welche halten
- **No Preemption**
	- Resourcen werden nur von Threads freigegeben
- **Circular Wait**
	- Zwei oder mehr Prozesse/Threads formen einen Kreis, wo jeder auf Resourcen wartet die druch den nächsten gehalten wird.

## Condition Synchronization

Möchten mehrere Threads auf eine Ressource zugreifen, welche an eine Bedingung geknüpft ist, können Probleme auftreten:

- Deadlocks  
- Inkonsistente Zustände
- Warten nicht möglich
- Unnötiges Warten bzw. Try & Wait & Try & Wait ...

### Pattern für Condition Synchronization

```java
public class CarPark extends Thread {
	
	// condition variable
	private int places;
	
	// constructor
	public CarPark(int initialPlaces) { this.places = initialPlaces; }

	// lock object
	private final Object LOCK = new Object();

	public enter() {
		synchronized(LOCK) {
			while(places == 0) { // full, so wait!
				try { lock.wait() } // leaves critical section (sleep), lock is released! 
				catch (InterruptedException e) { // NOP }
			}
			places--; // actual parking goes here
		}		
	}
	
	public synchronized leave() {
		synchronized(LOCK) {
         places++; 
         lock.notifyAll();
		}
	}

}
```

### Uniform Waiters

Werden mehrere Bedingungen eingesetzt, so würden beim verwenden des selben Locks alle auf die gleiche Bedingung warten. Grade wenn nur ```lock.notify()``` verwendet würde, könnte dies theoretisch dazu führen, dass ein Deadlock entsteht (immer selber Thread der aufgeweckt wird und die Condition nicht ändern kann/will). Lösung: Für jede Bedingung ein eigenes Condition-Objekt verwenden.

```java
public class Queue {
	private final static int SIZE = 10;
	
	private final Object[] buf = new Object[SIZE];
	private int tail = 0, head = 0;
	
	private final Lock lock = new ReentrantLock();
	private final Condition notEmpty = lock.newCondition(); 
	
	private final Condition notFull = lock.newCondition();

	public Object dequeue() { lock.lock();
		try {
          while (tail == head) { // while empty
				try { notEmpty.await(); 
			} catch (Exception e) {} }
          
          Object e = buf[head]; 
          head = (head + 1) % SIZE;
			notFull.signal(); 
			return e; 
		} finally { 
			lock.unlock(); 
		}
	}
	
	public void enqueue(Object c) { lock.lock();
		try {
    		while ((tail + 1) % SIZE == head) {
				try { notFull.await(); } catch (Exception e) {} 
			}
    
    		buf[tail] = c; tail = (tail + 1) % SIZE;
			notEmpty.signal(); 
		} 
		finally {
			lock.unlock(); 
		}
	}
}
```

### Stopping Threads

Blockierende oder sehr lange dauernde Operationen können dazu führen, dass die Anwendung pontentiell nie fertig wird da sie für immer wartet. Blockierende und lang dauernde Operationen müssen daher *cancelable* sein.

**Cancel Operations** *(auf Thread Object)*

- ```stop()``` ***DEPRECATED*** Freigeben aller Monitors, Inconsitenz wird sichtbar für andere Threads
- ```interrupt()``` 
	- Setzt das interrupt Flag
	- Bei wait / sleep / join wird eine ```InterruptedException``` geworfen
	- Lesen des Flags mit ```isInterrupted()```

	
**Handling InterruptedException**

```
try { ... }
catch (InterruptedException ie) {
	// Possible actions:
	
	// 1) Ignore exception
	// wenn angenommen wird, dass interrupt nie aufgerufen wird
	// wenn der Thread nicht interrupted werden sollte
	
	// 2) Propagate the exception
	// aufräumen der bishereigen Arbeit
	// rethrow der Exception
	
	// 3) Defer the exception
	// Exception wird am falschen Ort gefangen (z.B. im Runnable)
	// An richtigen Ort leiten, z.B. Thread.currentThread().interrupt();
}
```

## Java Memory Model

Alle Threads des selben Prozesses teilen gemeinsam den gleichen Hauptspeicher. Jedem Thread steht ein Arbeitsbereich zur Verfügung. In der Realität werden auf der CPU aber Caching-Levels angeboten und verwendet (Performance). Dies führt dazu, dass nicht alle Zustände immer im Speicher verfügbar sind, bzw. diese nicht immer durch alle Threads gelesen werden können. Das Java Memory Model definiert Garantien unter welchen Umständen Schreibvorgänge in Variabeln in anderen Threads sichtbar werden. 

### Happens Before Relation

Die Happens-Before-Relation ist eine Regel, welche Sichtbarkeiten nach **Aktionen** (*variable read/write, monitor lock/unlock, thread start/join*) in der JVM/JMM definiert und garantiert.

#### Regeln

1. Jede Aktion in einem Thread ***happens before*** jeder Aktion die später im Programmablauf ausgeführt werden.
2. Ein ```unlock``` auf einem ```monitor lock``` ***happens before*** allen nachfolgenden ```locks``` auf dem selben ```monitor lock```.
3. Ein Schreiben auf eine ```volatile``` Variable ***happens before*** allen nachfolgenden Lesezugriffen auf die selbe Variable.
4. Der Aufruf ```Thread.start()``` ***happens before*** jeder nachfolgenden Aktion in dem gestarteten Thread.
5. Aktionen in einem Thread t1 ***happens before*** ein anderer Thread die Terminierung von t1 feststellt.
6. Die ***happens before*** Reihenfolge ist transitiv (A -hb-> B && B -hb-> C => A -hb-> C)

#### Volatile

Variabeln welche mit dem Keyword ```volatile``` markiert werden dürfen nicht im local Cache gespeichert werden. Zudem werden alle Read/Write Aktionen auf die als ```volatile``` markierte Variable atomar ausgeführt. ```Synchronized``` ist also nicht nötig.

**Regeln für das Verwenden von ```volatile```**

1. Der neue Wert muss unabhängig vom alten sein, ansosten ```synchronized```
2. Der neue Wert muss unabhängig von anderen Werten sein, ansonsten ```synchronized```

### Double Checked Locking Problem

Das oben genannte Problem tritt bei der Implementierung von Singletons auf. Erst wird geprüft, ob das statische Feld ```null``` ist - falls ja, muss vor Initialiserung der Lock auf die Klasse geholt werden. Nach erhalt des Locks könnte die Instanz bereits erstellt sein, ein erneutes Prüfen auf ```null```ist also notwenig. Sie folgender Code:

```java
public class Singleton {
	private static Singleton instance;

	public static Singleton getInstance() {
		if (instance == null) {
			synchronized (Singleton.class) {
				if (instance == null) {
					instance = new Singleton();
				}
			}
		}
		return instance;
	}

	private Singleton() {
		/* initialization */ 
	}
	
	// other methods
}
```

**Lösung**

```java
public class Singleton {
	private volatile static Singleton instance;

	public static Singleton getInstance() {
		if (instance == null) {
			synchronized (Singleton.class) {
				if (instance == null) {
					instance = new Singleton();
				}
			}
		}
		return instance;
	}

	private Singleton() {
		/* initialization */ 
	}
	// other methods
}
```

## Safe Object Sharing

Wenn mehrere Threads die selbe ***mutable state variable*** teilen ohne angemessene Synchronisierung ist die Anwendung fehlerhaft. Es gibt drei Wege dies zu beheben:

1. Synchronisierung bei Verwendung der Variable nutzen
2. State Variable **immutable** machen 
3. State Variable nicht über mehrere Threads teilen

### Code Sample: Uninitialized Object

```java
final class Account {
	private int balance;
	public Account(int balance) { this.balance = balance; } 
	public String toString() { return "" + balance; }
}

class Company {
	private Account account = null;
	public Account getAccount() {
	if(account == null) account = new Account(10000);
	      return account;
   }
}

// Thread initialisiert Account
// T1 -> company.getAccount().toString();

// Thread sieht Änderung an Account nicht, L1/2 CPU Cache
// Selbst wenn, Visibilität von balance ist nicht garantiert!
// T2 -> company.getAccount().toString();
```

**Fix für obiges Beispiel**

```java
// Caching des Accounts verbieten
private volatile Account account = null;

// Visibilität von balance garantieren
private final int balance;
```

### JMM Garantien

- Final fields (Primitive- sowie Referenz- Typen) sind sichtbar **nach** Initialisierung
	-	Referenz Typen müssen über die ```final``` Referenz genutzt

Final fields werden also nach Durchlaufen des Konstruktors in den Hauptspeicher geschrieben. Dies geschieht bevor die Adresse des Objekts sichtbar wird. Initialisierungs-Garantie gilt nur wenn das Objekt erst nach Erstellung benutzt wird, ```this``` während Kontruktor also nicht im Zusammenhang mit dem State verwenden.

**Listener GOOD**

```java
public class ThisNotEscape {
	public final int i;
	
	private ThisNotEscape() {
		// DO NOT REGISTER LISTENER ON THIS WHILE CONSTRUCTING 
		i = 42; 
	}
	
	// SAFE LISTENER REGISTRATION
	public static ThisNotEscape create(Button source) { 
		final ThisNotEscape notEscape = new ThisNotEscape(); 	
		source.registerListener(new ClickListener() {
			public void buttonClicked() { 
				notEscape.doSomething();
			} 
		});
	
		return notEscape; 
	}
	... 
}
```

### Final vs Volatile

**final**

- Nur final Felder werden nach Konstruktor in RAM geschrieben
- Nur der Erste Zugriff führt zu einem Refresh
- Danach wird kein Refresh mehr ausgeführt
- **Änderungen in Referenztyp werden nicht sichtbar**

**volatile**

- Jeder Lesezugriff garantiert die neusten Daten zu sehen
- Keine Garantien für Änderungen im Referenztyp
- **Kein MUTEX, lediglich visibilität (r/w atomar)!**

### Immutablility

Finale Felder müssen richtig konstruiert werden, kein Escaping von ```this```. Felder können nach Initialisierung nicht geändert werden. **Sind immer THREAD-SAFE!**

**Strict Immutable**

- Alle Felder sind final (empfohlen)
- Cann in jede Richtung publiziert werden (visibility = consistency)

**Effectively Immutable**

- State ändert nicht, jedoch nicht ```final```
- Muss sicher publiziert werden => ```volatile```

**Wie man Objekte sichtbar macht**

- Referenz in einer ```volatile```Variable speichern
- Referenz in ein Feld speichern dass durch einen Lock geschützt ist.
- Objekt mit einem ```static``` Initialisierer erstellen
- Referenz in einem ```final``` Feld speichern und richtig konstruieren.

### Method Local Variables

Variabeln welche nicht zwischen Threads **shared** sind können nur durch den ausführenden Thread verwendet werden. Solche Variabeln sollen dann aber auch nicht publiziert werden!

**Schlecht:**

```java
public class BadFormatter {
	// variable wird publiziert (HEAP) obwohl nicht nötig!
	private static final SimpleDateFormat f = new SimpleDateFormat();
	public static String format(Date d) { 
		return f.format(d);
	} 
}
```

**Besser**

Falls das wirklich ein Perfomance Problem wäre, ```ThreadLocal``` benutzen.

```java
public class GoodFormatter {
	public static String format(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat();
		return sdf.format(d); 
	}
}
```

### ThreadLocal

Mit einer ```ThreadLocal``` Variable kann für jeden Thread eine seperate Kopie dieser Variable zur Verfügung gestellt werden. Typischerweise ```private static``` fields.

**Interface**

```java
class ThreadLocal<T> { 
	public T get();	// returns thread local value
	public void set(T value);	// sets value for current thread
	protected T initialValue(); // defines initial value
	public void remove();
}

// sample usage
private static ThreadLocal<SimpleDateFormat> local = 
ThreadLocal.withInitial(() -> new SimpleDateFormat());

public static String format(Date d) { 
	return local.get().format(d);
}

// Anwendungsbeispiel
java.util.ThreadLocalRandom
Schlechte Performance ohne ThreadLocal
```



<br> <br> <br>
## Probleme

### java.util Strukturen

HashMap, HashTree, List usw. sind nicht ThreadSafe, Lösung:

```java
List newList = Collections.synchronizedList(oldList);
Map newMap = 
```

**TODO: SUMMARY BANK SOLUTIONS HERE...**