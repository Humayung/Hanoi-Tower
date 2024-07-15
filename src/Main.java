import processing.core.PApplet;
import processing.core.PShape;

import java.util.ArrayList;
import java.util.LinkedList;

public class Main extends PApplet {
    private float animStep = 0.4f;
    private int ringsNum = 12;
    private int rodsNum = 3;
    MoveManager moveManager;

    public static void main(String[] args) {
        PApplet.main("Main", args);
    }

    public void settings() {
        size(900, 600);
    }

    TowerOfHanoi toh;

    public void setup() {
        noStroke();
        rectMode(CENTER);
        textAlign(CENTER, CENTER);
        textSize(8);
        toh = new TowerOfHanoi(ringsNum);
        moveManager = new MoveManager();
    }

    int start = millis();
    boolean autoSolve = false;

    public void draw() {
        background(51);
        toh.show();
        toh.update();


        if (autoSolve && (millis() - start > 500)) {
            solve();
            start = millis();
        }
        textSize(30);
        fill(255);
        text("Towers of Hanoi", width / 2, 50);
        pushMatrix();
        fill(255);
        translate(width / 2, height - 120);
        text("Binary Solve", 0, 30);
        text(seq, 0, 60);
        textSize(25);
        text(from + " -> " + to, 0, 90);
        popMatrix();
        textSize(20);
        fill(255);
        text("Moves: " + step, width / 2, 90);
        textSize(15);
        text("Optimum Moves: " + (int) pow(2, ringsNum), width / 2, 110);
    }

    TowerOfHanoi.Tower.Ring ringSnap;

    public void mousePressed() {
        if (!autoSolve) {
            TowerOfHanoi.Tower closestTower = toh.getClosestTower();
            if (closestTower != null) {
                pick(closestTower);
                mouseDragged();
            }
        }
    }

    public void mouseReleased() {
        if (!autoSolve) {
            TowerOfHanoi.Tower closestTower = toh.getClosestTower();
            put(closestTower);
        }
    }

    public void mouseDragged() {
        if (ringSnap != null) {
            ringSnap.translateTo(mouseX, 100);
        }
    }

    void pick(TowerOfHanoi.Tower tower) {
        ringSnap = tower.popTopRing();
        toh.snapOrigin = tower;
    }

    public void keyPressed() {
        if (key == 'a' || key == 'b') {
            reset();
        }
        if (key == ' ') autoSolve = !autoSolve;
        if(key == 'm') solve();
        if (keyCode == LEFT) moveManager.undo();
    }

    void put(TowerOfHanoi.Tower tower) {
        if (ringSnap != null) {
            if (tower != null) {
                tower.pileUp(ringSnap);
                ringSnap = null;
            } else {
                ringSnap.alignTo(toh.snapOrigin);
                toh.snapOrigin.pile.push(ringSnap);
            }
        }
        toh.snapOrigin = null;
    }

    boolean checkMove(TowerOfHanoi.Tower.Ring a, TowerOfHanoi.Tower.Ring b) {
        return b == null || b.index < a.index;
    }


    int from = 0;
    int to = 0;
    long step = 0;
    String seq = convert(0, rodsNum - 1);
    String prevSeq = convert(0, rodsNum - 1);

    void reset() {
        step = 0;
        toh = new TowerOfHanoi(ringsNum);
    }

    void solve() {
        prevSeq = seq;
        seq = convert(step, rodsNum - 1);
        println(seq + "           " + step);
        for (int i = 0; i < seq.length(); i++) {
            int state = Integer.valueOf(Character.toString(seq.charAt(i)));
            int prevState = Integer.valueOf(Character.toString(prevSeq.charAt(i)));
            if (prevState != state && state > 0) {
                TowerOfHanoi.Tower towerA = toh.rings.get(i).tower;
                pick(towerA);
                put(getLegalMove(ringSnap));
            }
        }

        step = (step + 1) % (int) pow(2, ringsNum);
    }

    TowerOfHanoi.Tower getLegalMove(TowerOfHanoi.Tower.Ring ringA) {
        int ringIndex = ringA.index;
        int towerIndex = ringA.tower.index;
        int nextTowerIndex = ringIndex % 2 != 0 ? (towerIndex + 1) % 3 : (towerIndex - 1) < 0 ? rodsNum - 1 : towerIndex - 1;
        return toh.towers.get(nextTowerIndex);
    }


      /*
    0000 0000
    0001 0001
    0010 0021
    0011 0022
    0100 0122
    0101 0120
    0110 0110
    0111 0111
    1000 2111
    1001 2112
    1010 2102
    1011 2100
    1100 2200
    1101 2201
    1110 2221
    1111 2222

     */

    String convert(long n, int base) {
        String s = "";

        while (n > 0) {
            long a = n % base;
            s += a;
            n = n / base;
        }
        while (s.length() < ringsNum) {
            s += '0';
        }
        return s;
    }

    class TowerOfHanoi {
        ArrayList<Tower> towers;
        PShape floor;
        LinkedList<Tower.Ring> rings;
        Tower snapOrigin;

        TowerOfHanoi(int ringsNum) {
            rings = new LinkedList<>();

            rectMode(CORNER);
            floor = createShape(RECT, 0, height - 120, width, 120);
            floor.setFill(100);
            towers = new ArrayList<>();
            for (int i = 0; i < rodsNum; i++) {
                towers.add(new Tower(i));
            }

            towers.get(0).init(ringsNum);
        }

        void show() {
            for (Tower t : towers) {
                t.show();
            }
            for (Tower.Ring r : rings) {
                r.show();
            }
            if (snapOrigin != null) {
                snapOrigin.highLight();
            }
            drawFloor();
        }

        void update() {
            for (Tower.Ring r : rings) {
                r.animStep();
            }
        }

        void drawFloor() {

            shape(floor, 0, 0);
        }

        Tower getClosestTower() {
            Tower closest = null;
            for (Tower t : towers) {
                closest = t.isSnap();
                if (closest != null) break;
            }
            return closest;
        }

        class Tower {
            int x;
            int y;
            int h = height - 50;
            int w = 15;
            int index;
            boolean highLight;
            LinkedList<Ring> pile;

            Tower(int index) {
                this.index = index;
                pile = new LinkedList<>();
                x = (width - 100) / rodsNum * index + width / 6 + 100 / rodsNum;
                y = height / 2 + 100;
            }

            void show() {
                if (!highLight) {
                    fill(200, 200, 10, 200);
                } else {
                    fill(255, 255, 25);
                }
                highLight = false;
                rectMode(CENTER);
                rect(x, y, w, h, 20);
            }

            void highLight() {
                highLight = true;
            }

            String getCurrentStates() {
                String states = "";
                for (int i = 0; i < rings.size(); i++) {
                    Ring r = rings.get(i);
                    states += r.tower.index;
                }
                return states;
            }

            void init(int ringsNum) {
                for (int i = 0; i < ringsNum; i++) {
                    Ring r = new Ring(this, i);
                    rings.push(r);
                    pile.push(r);
                }
            }

            Ring getTopRing() {
                return pile.size() > 0 ? pile.getFirst() : null;
            }

            Ring popTopRing() {
                return pile.size() > 0 ? pile.pop() : null;
            }

            void pileUp(Ring ring) {
                if (checkMove(ring, getTopRing())) {

                    from = ring.tower.index;
                    to = this.index;
                    if (to != from) {
                        moveManager.push(ring.tower, this);
                        println("Moved! from " + from + " to " + to);
//                        step++;
                    }
                    ring.alignTo(this);
                    pile.push(ring);
                } else {
                    println("Illegal Move! from " + ring.tower.index + " to " + this.index);
                    ring.alignTo(snapOrigin);
                    snapOrigin.pile.push(ring);
                }
            }

            Tower isSnap() {
                int spacing = (width - 100) / (rodsNum * 2);
                if (mouseX < x + spacing && mouseX > x - spacing) {
                    return this;
                }
                return null;
            }

            class Ring {
                int index;
                float x;
                float y;
                int w;
                int h;
                float tx;
                float ty;
                int color;
                Tower tower;

                Ring(Tower tower, int index) {
                    this.index = index;
                    this.tower = tower;
                    h = (height - 300) / ringsNum;
                    w = floor(map(index, 0, ringsNum, (width - 150) / rodsNum, 30));
                    x = tower.x;
                    y = (height - index * h) - h / 2 - 120;
                    color = lerpColor(color(255, 255), color(0, 255, 255, 100), (float) index / ringsNum);
                    translateTo(x, y);
                }

                void show() {
                    textSize(200 / ringsNum);
                    rectMode(CENTER);
                    fill(color);
                    rect(x, y, w, h, 2);
                    fill(51);
                    text(index, x, y);
                }

                void translateTo(float x, float y) {
                    tx = x;
                    ty = y;
                }

                void animStep() {
                    this.x = lerp(this.x, tx, animStep);
                    this.y = lerp(this.y, ty, animStep);
                }

                void alignTo(Tower tower) {
                    this.tower = tower;
                    Ring topRing = tower.getTopRing();
                    float tx = tower.x;
                    float ty = topRing == null ? height - h / 2 - 120 : topRing.y - h;
                    translateTo(tx, ty);
                }
            }
        }


    }

    class MoveManager {
        LinkedList<Movement> moves;

        MoveManager() {
            moves = new LinkedList<>();
        }

        void push(TowerOfHanoi.Tower from, TowerOfHanoi.Tower to) {
            moves.add(0, new Movement(from, to));
        }

        void undo() {
            if (moves.size() > 0) {
                Movement move = moves.get(0);
                moves.remove(0);
                pick(move.to);
                put(move.from);
            }
        }

        class Movement {
            TowerOfHanoi.Tower from;
            TowerOfHanoi.Tower to;

            Movement(TowerOfHanoi.Tower from, TowerOfHanoi.Tower to) {
                this.from = from;
                this.to = to;
            }
        }
    }
}
