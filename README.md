VisualListAnimator - Grafische Animation von Listenoperationen (v.2024a)
  
  (c) 2024 Daniel Janssen, Gymnasium Dionysianum Rheine
  
  Die Klasse visualisiert Listenoperationen schrittweise oder als Animation.
  Dazu stehen folgende Methoden zur Verfügung (Dokumentation dort).
  
    addlist
    removeList
    animate
  
  
  Verwendungsbeispiel:
  
    List<Integer> list1 = new List<Integer>();
    List<Integer> list2 = new List<Integer>();
    VisualListAnimator<Integer> vla = new VisualListAnimator<Integer>();
    vla.addList(list1, "list1");
    vla.addList(list2, "list2");
        
    list1.append(42);   
    vla.animate(list1, "APPEND", "Hinzufügen der Zahl 42");
        
    list1.toFirst();    
    vla.animate(list1, "TOFIRST", "Zum ersten Element");
        
    list2.append(list1.getContent());
    vla.animate(list1, "GETCONTENT", "Zugriff auf das aktuelle Element");
    vla.animate(list2, "APPEND", "Hinzufügen der Zahl 42");
  
  
  Hinweise:
  
    Die Klasse List (Abitur NRW) wird zusätzlich benötigt
  
    Die Klasse, die als ContentType verwendet wird, kann die Methode toString() überschreiben, 
    damit bis zu drei Zeilen an Informationen pro Listenelement angezeigt werden können.
    Die intendierten Zeilen des Strings müssen durch Doppelpunkte ":" getrennt sein.
 
 
  Beispiel:
  
    public class Benutzer {
       private String name;
       private int benutzerID;
       
       public String toString() {
           return "ID " + benutzerID + ":" + name;
       }
    }
  
  
  Weitere Hinweise:
  
    Bei Append, Insert und Concat: Wenn null oder etwas Ungültiges übergeben wird, führt dieses 
    z.Zt. noch zu falschem Verhalten. Auch besitzt die Klasse keinen eigenen Garbage-Collector o.ä., 
    d.h. Listen könnten zwar theoretisch in der aufrufenden Klasse verschwunden sein 
    (z.B. durch rekursiven Aufruf), werden jedoch in der Visualisierung trotzdem noch angezeigt. 
    Dies lässt sich nur teilweise mit einem manuellen Aufruf von removeList umgehen.
