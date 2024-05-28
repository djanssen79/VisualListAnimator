/**
 * VisualListAnimator - Grafische Animation von Listenoperationen (v.2024a)
 * (c) 2024 Daniel Janssen, Gymnasium Dionysianum Rheine
 * 
 * Die Klasse visualisiert Listenoperationen schrittweise oder als Animation.
 * Dazu stehen folgende Methoden zur Verfügung (Dokumentation dort).
 * 
 *   addlist
 *   removeList
 *   animate
 * 
 * 
 * Verwendungsbeispiel:
 * 
 *   List<Integer> list1 = new List<Integer>();
 *   List<Integer> list2 = new List<Integer>();
 *   VisualListAnimator<Integer> vla = new VisualListAnimator<Integer>();
 *   vla.addList(list1, "list1");
 *   vla.addList(list2, "list2");
 *       
 *   list1.append(42);   
 *   vla.animate(list1, "APPEND", "Hinzufügen der Zahl 42");
 *       
 *   list1.toFirst();    
 *   vla.animate(list1, "TOFIRST", "Zum ersten Element");
 *       
 *   list2.append(list1.getContent());
 *   vla.animate(list1, "GETCONTENT", "Zugriff auf das aktuelle Element");
 *   vla.animate(list2, "APPEND", "Hinzufügen der Zahl 42");
 * 
 * 
 * Hinweise:
 * 
 *   Die Klasse List (Abitur NRW) wird zusätzlich benötigt
 * 
 *   Die Klasse, die als ContentType verwendet wird, kann die Methode toString() überschreiben, 
 *   damit bis zu drei Zeilen an Informationen pro Listenelement angezeigt werden können.
 *   Die intendierten Zeilen des Strings müssen durch Doppelpunkte ":" getrennt sein.
 *
 *
 * Beispiel:
 *   public class Benutzer {
 *      private String name;
 *      private int benutzerID;
 *      
 *      public String toString() {
 *          return "ID " + benutzerID + ":" + name;
 *      }
 *   }
 * 
 * 
 * Weitere Hinweise:
 * 
 *   Bei Append, Insert und Concat: Wenn null oder etwas Ungültiges übergeben wird, führt dieses 
 *   z.Zt. noch zu falschem Verhalten. Auch besitzt die Klasse keinen eigenen Garbage-Collector o.ä., 
 *   d.h. Listen könnten zwar theoretisch in der aufrufenden Klasse verschwunden sein 
 *   (z.B. durch rekursiven Aufruf), werden jedoch in der Visualisierung trotzdem noch angezeigt. 
 *   Dies lässt sich nur teilweise mit einem manuellen Aufruf von removeList umgehen.
 */
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JSlider;
import java.util.Hashtable;
import javax.swing.JLabel;
import java.util.ArrayList;
import java.awt.Toolkit;

/**
 * Die Klasse VisualListAnimator ist generisch und erwartet als ContentType denselben Typen 
 * den die Klasse List verwendet.
 */
public class VisualListAnimator<ContentType> extends JFrame {

    private ArrayList<VPanel<ContentType>> panels;
    private JPanel jp;
    private JPanel navigation;
    private JButton play;
    private JButton pause;
    private JButton next;
    private JSlider slider;
    private boolean isAnimated = false;
    private final Object lock = new Object();
    private boolean buttonPressed = false;    
    private String aktBefehl = "";
    private VPanel aktiveVPan;
    private int startYCoord = 60;
    private int dpHeight = 170;            

    /**
     * Eine neue Instanz der Klasse VisualListAnimator wird erstellt. 
     * Es stehen folgende Methoden zur Verfügung:
     * 
     * addList - Hinzufügen einer Liste bzw. einer Referenzkopie (s. unten)
     * removeList - Entfernen der Liste
     * animate - Anzeige und grafische Animation einer Aktion
     */
    public VisualListAnimator() {     
        panels = new ArrayList<VPanel<ContentType>>();
        setTitle("Visual List");
        setLocation(0, 0);        
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        jp = new JPanel();
        jp.setLayout(new GridLayout(1,1));
        JScrollPane jsp = new JScrollPane(jp);
        jsp.getHorizontalScrollBar().setUnitIncrement(8);
        jsp.getVerticalScrollBar().setUnitIncrement(8);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(jsp, BorderLayout.CENTER);        

        navigation = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigation.add(new JLabel("  "));
        pause = new JButton("||");
        pause.setForeground(Color.BLUE);
        next = new JButton(">");
        play = new JButton(">>");

        pause.addActionListener(new ActionListener() {                        
                public void actionPerformed(ActionEvent e) {
                    play.setForeground(Color.BLACK);
                    pause.setForeground(Color.BLUE);  
                    isAnimated = false;
                    synchronized (lock) {
                        buttonPressed = true;                                
                        lock.notify();
                    }                  
                }
            });

        next.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    synchronized (lock) {
                        buttonPressed = true;                                
                        lock.notify();
                    }                  
                }
            });

        play.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    play.setForeground(Color.BLUE);
                    pause.setForeground(Color.BLACK);  
                    isAnimated = true;
                    synchronized (lock) {
                        buttonPressed = true;                                
                        lock.notify();
                    } 
                }
            });
        navigation.add(pause);
        navigation.add(next);
        navigation.add(play);                
        slider = new JSlider();                
        slider.setMinimum(0);
        slider.setMaximum(100);
        slider.setPaintTicks(true);
        slider.setValue(50);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTrack(true);                
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(Integer.valueOf(0), new JLabel("Langsam") );                
        labelTable.put(Integer.valueOf(100), new JLabel("Schnell") );
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true); 
        navigation.add(slider);
        add(navigation, BorderLayout.NORTH);
        setSize(getBestWindowWidth(),getBestWindowHeight());
        setVisible(true);
    }   

    /**
     * Die im Parameter übergebene Referenz auf eine Liste wird unter dem als String 
     * übergebenem Referenznamen der Visualisierung hinzugefügt. 
     * Existiert das referenzierte List-Objekt innerhalb der Visualisierung noch nicht, 
     * so wird es als neue Liste in die Visualisiserung aufgenommen.
     * Zeigt die Referenz jedoch auf eine bereits vorhandene Liste, so wird lediglich 
     * der übergebene Referenzname als zusätzlicher Referenzname der automatisch
     * korrekt ermittelten Liste hinzugefügt.
     * 
     * Beispiel:
     *  List<Integer> list1 = new List<Integer>()
     *  addList(list1, "list1") -> die Liste list1 wird neu aufgenommen
     *           
     *  List<Integer> list2 = new List<Integer>()
     *  addList(list2, "list2") -> die Liste list2 wird neu aufgenommen
     *           
     *  List<Integer> list3 = list1;
     *  addList(list3, "list3") -> zur Liste list1 wird ein neuer Referenzname "list3 hinzugefügt
     * 
     * @param pList Liste, die der Visualisierung hinzugefügt werden soll
     * @param pReferenceName Referenzname der Liste als String
     */
    public void addList(List<ContentType> pList, String pReferenceName) {

        boolean gefunden = false;
        int count = -1;
        for (int i=0; i<panels.size(); i++) {
            if (panels.get(i).gibOriginalList().equals(pList)) {
                gefunden = true;
                count = i;
            }
        }

        // falls es eine neue Originalliste ist
        if (!gefunden) {
            VPanel<ContentType> neu = new VPanel<ContentType>();
            neu.setzeOriginalList(pList);
            neu.addRefName(pReferenceName);
            panels.add(neu);
            jp.setLayout(new GridLayout(panels.size(),1));
            for (int i=0; i<panels.size(); i++) {
                jp.add(panels.get(i)); 
            }            
        }
        else { // dann nur die neue Referenz hinzufügen
            panels.get(count).addRefName(pReferenceName);
            aktiveVPan = panels.get(count);
        }
        
        setSize(getBestWindowWidth(), getBestWindowHeight());
        revalidate();
    }

    /**
     * Die unter dem übergebenen Referenznamen zu findende Liste
     * wird aus der Visualisierung entfernt.
     * 
     * @param pList zu entfernende Liste
     */
    public void removeList(List<ContentType> pList) {
        VPanel tmp = null;        
        for (int i=0; i<panels.size(); i++) {
            if (panels.get(i).gibOriginalList().equals(pList)) {                
                tmp = panels.get(i);
            }
        }

        if (tmp != null) {
            panels.remove(tmp);
            jp.removeAll();
            jp.setLayout(new GridLayout(panels.size(),1));
            for (int i=0; i<panels.size(); i++) {
                jp.add(panels.get(i)); 
            }            
            revalidate();
        }
    }    

    /**
     * Die mittels der Methode addList zuerst übergebene Liste wird mit dem (als String) 
     * übergebenen Befehl bearbeitet und das Ergebnis wird visuell zusammen mit dem 
     * (als String) übergebenen Erklärtext angezeigt. 
     * 
     * Mögliche Befehle (übergeben als String) sind:
     * 
     * TOFIRST
     * TOLAST
     * NEXT
     * GETCONTENT
     * SETCONTENT
     * APPEND
     * INSERT
     * CONCAT
     * REMOVE
     * MARK   (versieht das aktuelle Listen-Element mit einer dauerhaften Spezialmarkierung)
     * UNMARK (hebt alle Markierungen wieder auf)
     * 
     * Beispiel:
     *  animate(list1, "TOFIRST", "Zum ersten Element gehen");
     * 
     * @param pCommand einer der möglichen Befehle (als String)
     * @param pExplanationText ein Text mit einer Erläuterung (als String)
     */
    public void animate(String pCommand, String pExplanationText) {
        if (panels.size() > 0) {
            aktiveVPan = panels.get(0);
            update_GUI(0, pCommand, pExplanationText);            
        }
    }

    /**
     * Die im ersten Parameter (unter Angabe der Referenz als String) übergebene Liste 
     * wird mit dem (als String) übergebenen Befehl bearbeitet und das Ergebnis wird 
     * visuell zusammen mit dem (als String) übergebenen Erklärtext angezeigt.
     * 
     * Mögliche Befehle (übergeben als String) sind:
     * 
     * TOFIRST
     * TOLAST
     * NEXT
     * GETCONTENT
     * SETCONTENT
     * APPEND
     * INSERT
     * CONCAT
     * REMOVE
     * MARK   (versieht das aktuelle Listen-Element mit einer dauerhaften Spezialmarkierung)
     * UNMARK (hebt alle Markierungen wieder auf)
     * 
     * Beispiel:
     *  animate(list1, "TOFIRST", "Zum ersten Element gehen");
     * 
     * @param pList die (korrekte) Referenz auf die Liste (als String), für die eine Änderung angezeigt werden soll
     * @param pCommand einer der möglichen Befehle (als String)
     * @param pExplanationText ein Text mit einer Erläuterung (als String)
     */
    public void animate(List<ContentType> pList, String pCommand, String pExplanationText) {
        int count = -1;
        for (int i=0; i<panels.size(); i++) {
            if (panels.get(i).gibOriginalList().equals(pList)) {
                count = i;
            }
        }
        if (count != -1) {
            aktiveVPan = panels.get(count);
            update_GUI(count, pCommand, pExplanationText);            
        }
    }    

    private int getSliderValue() {
        return slider.getMaximum() - slider.getValue();
    }

    private int getBestWindowWidth() {
        int max = 0;
        for (int i=0; i<panels.size(); i++) {
            if (panels.get(i).gibAnzElem() > max) {
                max = panels.get(i).gibAnzElem();
            }
        }        
        int prefWidth = max * 70 + 150 + 20;
        int width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        if (prefWidth > width) {
            prefWidth = width;
        }        
        if (prefWidth < 640) {
            prefWidth = 640;
        }
        return prefWidth;    
    }

    private int getBestWindowHeight() {        
        int prefHeight = panels.size() * dpHeight + 60 + navigation.getHeight();
        int height = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        if (prefHeight > height) {
            prefHeight = height;
        }        
        if (panels.size() == 0) {
            prefHeight = 290;
        }
        return prefHeight;    
    }

    private void update_GUI(int vPNum, String pBefehl, String pText) {
        aktBefehl = "";        

        if (pBefehl.equals("TOFIRST")) {
            if (!panels.get(vPNum).gibList().isEmpty()) {
                panels.get(vPNum).setzeListPosition(0);                
                panels.get(vPNum).gibList().toFirst();
            }
        }
        else if (pBefehl.equals("NEXT")) {
            if (panels.get(vPNum).gibListPosition() == panels.get(vPNum).gibAnzElem()) {
                panels.get(vPNum).setzeListPosition(-1);
            }
            else if (!panels.get(vPNum).gibList().isEmpty() && panels.get(vPNum).gibListPosition() != -1) {
                panels.get(vPNum).setzeListPosition(panels.get(vPNum).gibListPosition() + 1);
                panels.get(vPNum).gibList().next();
            }

        }
        else if (pBefehl.equals("TOLAST")) {
            if (!panels.get(vPNum).gibList().isEmpty()) {
                panels.get(vPNum).setzeListPosition(panels.get(vPNum).gibAnzElem()-1);
                panels.get(vPNum).gibList().toLast();
            }
        }
        else if (pBefehl.equals("SETCONTENT")) {
            if (panels.get(vPNum).gibListPosition() != -1) {
                panels.get(vPNum).gibList().setContent(panels.get(vPNum).gibOriginalList().getContent());
                aktBefehl = "SETCONTENT";
            }
        }        
        else if (pBefehl.equals("APPEND")) {
            // letztes Element aus der Originalliste anfügen und Original-Zeiger zurücksetzen
            panels.get(vPNum).gibOriginalList().toLast();
            panels.get(vPNum).gibList().append(panels.get(vPNum).gibOriginalList().getContent());
            panels.get(vPNum).resetOriginalListPointer();
            panels.get(vPNum).setzeAnzElem(panels.get(vPNum).gibAnzElem() + 1);                        
            panels.get(vPNum).gibDP().resizeDP();
            aktBefehl = "APPEND";
        }        
        else if (pBefehl.equals("CONCAT")) {
            // neue tiefe Kopie erzeugen
            panels.get(vPNum).setzeAnzElemAlt(panels.get(vPNum).gibAnzElem());
            panels.get(vPNum).setzeList(new List<ContentType>());
            panels.get(vPNum).gibOriginalList().toFirst();
            panels.get(vPNum).setzeAnzElem(0);            
            while (panels.get(vPNum).gibOriginalList().hasAccess()) {
                panels.get(vPNum).gibList().append(panels.get(vPNum).gibOriginalList().getContent());
                panels.get(vPNum).gibOriginalList().next();
                panels.get(vPNum).setzeAnzElem(panels.get(vPNum).gibAnzElem() + 1);
            }
            panels.get(vPNum).resetOriginalListPointer();                        
            panels.get(vPNum).gibDP().resizeDP();
            aktBefehl = "CONCAT";

            // nach einem CONCAT kann eine andere Liste nun leer sein, was überprüft werden muss
            for (int i=0; i<panels.size(); i++) {
                List<ContentType> orig = panels.get(i).gibOriginalList();
                if (orig.isEmpty()) {// && panels.get(i).gibListPosition() != -1 && panels.get(i).gibAnzElem() != 0) {
                    panels.get(i).setzeAnzElem(0);
                    panels.get(i).setzeList(new List<ContentType>());
                    panels.get(i).setzeListPosition(-1);
                }
            }            
        }
        else if (pBefehl.equals("REMOVE")) {
            if (!panels.get(vPNum).gibList().isEmpty() && panels.get(vPNum).gibListPosition() != -1) {
                panels.get(vPNum).gibList().remove();            
                panels.get(vPNum).setzeAnzElem(panels.get(vPNum).gibAnzElem() - 1);
                panels.get(vPNum).gibDP().resizeDP();
                aktBefehl = "REMOVE";
            }
        }
        else if (pBefehl.equals("GETCONTENT")) {
            if (panels.get(vPNum).gibListPosition() != -1) {
                aktBefehl = "GETCONTENT";
            }            
        } 
        else if (pBefehl.equals("INSERT")) {
            // falls Liste leer -> Element einfügen, listPosition bleibt -1
            if (panels.get(vPNum).gibList().isEmpty()) {
                panels.get(vPNum).gibOriginalList().toFirst();
                panels.get(vPNum).gibList().append(panels.get(vPNum).gibOriginalList().getContent());            
                panels.get(vPNum).resetOriginalListPointer();
                panels.get(vPNum).setzeAnzElem(panels.get(vPNum).gibAnzElem() + 1);                
                panels.get(vPNum).gibDP().resizeDP();
                aktBefehl = "INSERT";
            }
            // Liste nicht leer und hasAccess == true -> Element einfügen, listPosition bleibt wie sie ist
            else if (!panels.get(vPNum).gibList().isEmpty() && panels.get(vPNum).gibListPosition() != -1) {
                panels.get(vPNum).gibOriginalList().toFirst();
                for (int i=0; i<panels.get(vPNum).gibListPosition(); i++) {
                    panels.get(vPNum).gibOriginalList().next();
                }
                panels.get(vPNum).gibList().insert(panels.get(vPNum).gibOriginalList().getContent());            
                panels.get(vPNum).resetOriginalListPointer();
                panels.get(vPNum).gibOriginalList().next();
                panels.get(vPNum).gibList().next();
                panels.get(vPNum).setzeListPosition(panels.get(vPNum).gibListPosition() + 1);                
                panels.get(vPNum).setzeAnzElem(panels.get(vPNum).gibAnzElem() + 1);                
                panels.get(vPNum).gibDP().resizeDP();
                aktBefehl = "INSERT";
            }
        }    
        else if (pBefehl.equals("MARK")) {
            if (panels.get(vPNum).gibListPosition() != -1) {
                aktBefehl = "MARK";
            }            
        }
        else if (pBefehl.equals("UNMARK")) {            
            aktBefehl = "UNMARK";            
        }

        if (pText != null) {
            panels.get(vPNum).setzeText(pText);
        }

        setSize(getBestWindowWidth(), getBestWindowHeight());
        repaint();

        // warten
        try {
            if (isAnimated) {            
                Thread.sleep(getSliderValue()*20);            
            }
            else {
                synchronized (lock) {
                    while (!buttonPressed) {
                        lock.wait();
                    }
                    buttonPressed = false;
                }
            }
        }
        catch (Exception e) {
        }        

    }

    private class VPanel<ContentType> extends JPanel {

        private List<ContentType> list;
        private List<ContentType> originalList;
        private String text;
        private ArrayList<String> refNames;
        private int anzElem;
        private int anzElemAlt;
        private int listPosition;
        private int mark;
        private DrawPanel dp;        

        public VPanel() {
            this.setLayout(new BorderLayout());
            list = new List<ContentType>();
            originalList = new List<ContentType>();
            text = "";
            refNames = new ArrayList<String>();
            anzElem = 0;
            anzElemAlt = 0;
            listPosition = -1;
            mark = -1;
            dp = new DrawPanel(this, 640);
            this.add(dp, BorderLayout.CENTER);            
        }

        public void resetOriginalListPointer() {
            if (listPosition == -1) {
                originalList.toLast();
                originalList.next();
            }
            else {
                // Zeiger der Originalliste zurücksetzen
                originalList.toFirst();            
                int pos = 0;
                while (pos < listPosition) {
                    originalList.next();
                    pos++;
                }
            }
        }

        public List<ContentType> gibList() {
            return list;
        }

        public void setzeList(List<ContentType> pList) {
            list = pList;
        }

        public List<ContentType> gibOriginalList() {
            return originalList;
        }

        public void setzeOriginalList(List<ContentType> pOriginalList) {
            originalList = pOriginalList;             
            list = new List<ContentType>();
            originalList.toFirst();
            anzElem = 0;            
            while (originalList.hasAccess()) {
                list.append(originalList.getContent());
                originalList.next();
                anzElem++;
            }
            listPosition = -1;
        }

        public String gibRefNames() {
            String s = "";
            for (int i=0; i<refNames.size(); i++) {
                s += refNames.get(i) + ", ";
            }
            s = s.substring(0, s.length()-2);
            return s;
        }

        public void addRefName(String pRefName) {
            refNames.add(pRefName);
        }

        public void removeRefName(String pRefName) {            
            refNames.remove(pRefName);
        }

        public int gibAnzElem() {
            return anzElem;
        }

        public void setzeAnzElem(int pAnzElem) {
            anzElem = pAnzElem;
        }

        public int gibListPosition() {
            return listPosition;
        }

        public void setzeListPosition(int pListPosition) {
            listPosition = pListPosition;
        }

        public DrawPanel gibDP() {
            return dp;
        }

        public void setzeDP(DrawPanel pDP) {
            dp = pDP;
        }

        public String gibText() {
            return text;
        }

        public void setzeText(String pText) {
            text = pText;            
        }

        public int gibAnzElemAlt() {
            return anzElemAlt;
        }

        public void setzeAnzElemAlt(int pAnzElemAlt) {
            anzElemAlt = pAnzElemAlt;
        }

        public int gibMark() {
            return mark;
        }

        public void setzeMark(int pMark) {
            mark = pMark;
        }

    }
    private class DrawPanel extends JPanel implements ActionListener{

        private VPanel vParent;

        public DrawPanel(VPanel pVPanelParent, int pWidth) {
            vParent = pVPanelParent;            
            setPreferredSize(new Dimension(pWidth, dpHeight));
            setBorder(BorderFactory.createEtchedBorder());
        }

        public void resizeDP() {
            setPreferredSize(new Dimension(vParent.gibAnzElem() * 70 + 150, dpHeight));            
        }

        public void actionPerformed(ActionEvent e) {        
            repaint(); // neuzeichnen, ruft automatisch auch die Methode paintComponent auf
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g); 
            zeichneSzene((Graphics2D)g); 
        }

        private void zeichneSzene(Graphics2D g) {
            // Alle Knoten mitsamt Inhalt zeichnen
            vParent.gibList().toFirst();
            int pos = 50;            
            while (vParent.gibList().hasAccess()) {                
                g.setStroke(new BasicStroke(1));
                g.setColor(Color.WHITE);
                g.fillRect(pos-5, startYCoord-15, 50, 50);
                if (aktBefehl.equals("CONCAT") && (pos >= vParent.gibAnzElemAlt()*70+50) && aktiveVPan.equals(vParent)) {
                    g.setStroke(new BasicStroke(4));
                    g.setColor(new Color(0f, 1f, 0f, 0.5f));
                }
                else {
                    g.setStroke(new BasicStroke(1));
                    g.setColor(Color.BLACK);
                }                
                g.drawRect(pos-5, startYCoord-15, 50, 50);
                g.setStroke(new BasicStroke(1));
                g.setColor(Color.BLACK);
                String[] s = vParent.gibList().getContent().toString().split(":");
                for (int i=0; i<s.length; i++) {
                    g.drawString(s[i],pos, startYCoord+i*15);
                }
                g.drawLine(pos+45, startYCoord+10, pos+65, startYCoord+10);
                g.drawLine(pos+60, startYCoord+5, pos+65, startYCoord+10);
                g.drawLine(pos+60, startYCoord+15, pos+65, startYCoord+10);
                pos += 70;
                vParent.gibList().next();
            }
            g.drawString("null", vParent.gibAnzElem()*70+50, startYCoord+15);
            g.drawLine(35, startYCoord+10, 35, startYCoord-40);
            g.drawLine(35, startYCoord+10, 45, startYCoord+10);
            g.drawLine(40, startYCoord+5, 45, startYCoord+10);
            g.drawLine(40, startYCoord+15, 45, startYCoord+10);
            g.drawLine(35, startYCoord-40, 60, startYCoord-40);
            if (vParent.gibRefNames() != null) {                
                g.drawString(vParent.gibRefNames(), 70, startYCoord-35);
            }

            // Text mit Erläuterungen
            if (vParent.gibText() != null) {                
                g.drawString(vParent.gibText(), 35, startYCoord+90);
            }

            // Aktuellen current-Knoten blau markieren
            if (vParent.gibListPosition() != -1) {
                g.setStroke(new BasicStroke(4));
                g.setColor(new Color(0f, 0f, 1f, 1.0f));
                g.drawRect(vParent.gibListPosition()*70+50-5, startYCoord-15, 50, 50);
                g.setStroke(new BasicStroke(3));
                g.drawLine(vParent.gibListPosition()*70+50-5+25-3, startYCoord+45, vParent.gibListPosition()*70+50-5+25-3, startYCoord+60);
                g.drawLine(vParent.gibListPosition()*70+50-5+25-3, startYCoord+45, vParent.gibListPosition()*70+50-5+25-3-5, startYCoord+50);
                g.drawLine(vParent.gibListPosition()*70+50-5+25-3, startYCoord+45, vParent.gibListPosition()*70+50-5+25-3+5, startYCoord+50);                
            }
            else { 
                // null markieren
                g.setStroke(new BasicStroke(4));
                g.setColor(new Color(0f, 0f, 1f, 1.0f));
                g.drawRect(vParent.gibAnzElem()*70+50-5, startYCoord-15, 50, 50); 
                g.setStroke(new BasicStroke(3));
                g.drawLine(vParent.gibAnzElem()*70+50-5+25-3, startYCoord+45, vParent.gibAnzElem()*70+50-5+25-3, startYCoord+60);
                g.drawLine(vParent.gibAnzElem()*70+50-5+25-3, startYCoord+45, vParent.gibAnzElem()*70+50-5+25-3-5, startYCoord+50);
                g.drawLine(vParent.gibAnzElem()*70+50-5+25-3, startYCoord+45, vParent.gibAnzElem()*70+50-5+25-3+5, startYCoord+50);                
            }

            if (aktBefehl.equals("SETCONTENT") || aktBefehl.equals("GETCONTENT")) {
                if (aktiveVPan.equals(vParent)) {
                    g.setStroke(new BasicStroke(4));
                    g.setColor(new Color(1f, 1f, 0f, 1.0f));
                    g.drawRect(vParent.gibListPosition()*70+50-5, startYCoord-15, 50, 50);
                }
            }

            if (aktBefehl.equals("APPEND")) {
                if (aktiveVPan.equals(vParent)) {
                    g.setStroke(new BasicStroke(4));
                    g.setColor(new Color(0f, 1f, 0f, 1.0f));
                    g.drawRect((vParent.gibAnzElem()-1)*70+50-5, startYCoord-15, 50, 50);
                }
            }

            if (aktBefehl.equals("INSERT")) {
                if (aktiveVPan.equals(vParent)) {
                    g.setStroke(new BasicStroke(4));
                    g.setColor(new Color(0f, 1f, 0f, 1.0f));
                    g.drawRect((vParent.gibListPosition()-1)*70+50-5, startYCoord-15, 50, 50);
                }
            }

            // Markierungen
            if (aktBefehl.equals("MARK")) {
                if (aktiveVPan.equals(vParent)) {
                    vParent.setzeMark(vParent.gibListPosition());                    
                }
            }

            if (aktBefehl.equals("UNMARK")) {
                if (aktiveVPan.equals(vParent)) {
                    vParent.setzeMark(-1);
                }
            }

            if (vParent.gibMark() != -1) {
                g.setStroke(new BasicStroke(4));
                g.setColor(new Color(1f, 0f, 1f, 1.0f));
                g.drawRect(vParent.gibMark()*70+50-5, startYCoord-15, 50, 50);
            }

            // Zeiger wieder auf Listposition setzen
            if (vParent.gibListPosition() != -1) {
                vParent.gibList().toFirst();
                for (int i=0; i<vParent.gibListPosition(); i++) {
                    vParent.gibList().next();
                }
            }
        }  
    }
}