import geomerative.RG;
import geomerative.RShape;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Created by Yoyo on 03/05/2017.
 */
public class CircuitBoard3 extends PApplet {


    public static void main(String[] args) {
        PApplet.main("CircuitBoard3");
    }


    private boolean started = false;
    private int num_nodes = 200;
    private int maxDiameter = 1;
    private int n = 0;
    private int current_size = 0;
    private int prev_size = 0;
    private float node_radius = 0;
    private ArrayList<PVector> nodes = new ArrayList<>();
    private ArrayList<Float> nodeDiams = new ArrayList<>();
    private ArrayList<ArrayList<PVector>> node_lines = new ArrayList<>();
    private ArrayList<ArrayList<PVector>> node_speed = new ArrayList<>();
    private int grid_size = 5;
    private int step = 5;
    private ArrayList<PVector> bounds = new ArrayList<>();
    private boolean switchBool = false;

    private int[][] grid;
    private PVector[] dirs = new PVector[]{
            new PVector(0, 1),
            new PVector(1, 0),
            new PVector(-1, 0),
            new PVector(0, -1)
    };

    private PVector[] pivot_dirs = new PVector[]{
            new PVector(1, 1),
            new PVector(1, -1),
            new PVector(-1, 1),
            new PVector(-1, -1)
    };


    RShape svg;
    RShape svg2;
    String filename = "";

    public void settings() {
        size(1080, 1080);

    }

    public void setup() {
        grid = new int[width / grid_size][height / grid_size];

        node_radius = (float) step / 2f;

        println(grid.length + " " + grid[0].length);

        ellipseMode(CENTER);
        rectMode(CENTER);

        RG.init(this);
        //svg=RG.loadShape("/Volumes/Yoyo/Dev/Design/skin.svg");
        svg = RG.loadShape("/Volumes/Yoyo/Dev/Design/brain-black-silhouette.svg");
        //  svg = RG.loadShape("/Volumes/Yoyo/Dev/Design/heart.svg");
        // svg = RG.loadShape("/Volumes/Yoyo/heart.svg");

        String filepath = "/Users/Yoyo/Downloads/standing-human-body-silhouette-svgrepo-com.svg";
        filename = filepath.substring(filepath.lastIndexOf("/") + 1);
        svg = RG.loadShape(filepath);
        //svg.centerIn(g, width/2);
        svg2 = new RShape();


    }

    public void init() {

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = 0;
            }
        }

        if (bounds.size() == 0) {
            bounds = (ArrayList<PVector>) Arrays.stream(svg.getPoints()).map(rpoint -> new PVector(rpoint.x, rpoint.y)).collect(Collectors.toList());
        }

        int currentI = 0;
        int previousI = -1;
        while (nodes.size() < num_nodes) {
            println(currentI);
            //previousI = currentI;
            PVector newPos = new PVector(width + 1, height + 1);
            float diameter = random(2 * node_radius, 2 * node_radius * maxDiameter);

            while (!(isInsidePolygon(newPos, diameter, bounds) && isGridFree(newPos, diameter))) {
//                if(newPos.x<width+1) {
//                    diameter -= 2 * node_radius;
//                }
                // if (newPos.x==width+1 || diameter < 2 * node_radius) {

                newPos.x = random(width);
                newPos.y = random(height);
                newPos.x = newPos.x - newPos.x % grid_size;
                newPos.y = newPos.y - newPos.y % grid_size;

                diameter = random(2 * node_radius, 2 * node_radius * maxDiameter);
                //}
            }


            if (isInsidePolygon(newPos, diameter, bounds) && isGridFree(newPos, diameter)) {


                nodes.add(newPos);
                nodeDiams.add(diameter);
                fillGrid(newPos, diameter);

                node_lines.add(new ArrayList<>());
                node_speed.add(new ArrayList<>());

                PVector randDir = dirs[(int) random(4)].copy();
                PVector offset = randDir.copy();
                offset.mult(diameter * 0.5f);
                randDir.mult(step);
                int count = 30;
                PVector border = PVector.add(newPos, offset);
                border.x -= border.x % grid_size;
                border.y -= border.y % grid_size;
                PVector candidate = PVector.add(border, randDir);
                while ((!isInsidePolygon(candidate, bounds) || !isGridFree(candidate)) && count > 0) {
                    randDir = dirs[(int) random(4)].copy();
                    offset = randDir.copy();
                    offset.mult(diameter * 0.5f);
                    randDir.mult(step);
                    border = PVector.add(newPos, offset);
                    border.x -= border.x % grid_size;
                    border.y -= border.y % grid_size;
                    candidate = PVector.add(border, randDir);
                    count--;
                }

                if (isInsidePolygon(candidate, bounds) && isGridFree(candidate)) {
                    int p = nodes.indexOf(newPos);
                    node_lines.get(p).add(border.copy());
                    node_speed.get(p).add(new PVector(0, 0));

                    node_speed.get(p).add(randDir.copy());
                    node_lines.get(p).add(candidate);


                    //println(candidate.x + " " + candidate.y);
                    //println("pos: " + getGPos(candidate)[0] + " " + getGPos(candidate)[1]);
                    fillGrid(candidate);
                } else {
                    node_lines.remove(node_lines.size() - 1);
                    node_speed.remove(node_speed.size() - 1);
                    clearGrid(newPos, diameter);
                    nodeDiams.remove(nodeDiams.size() - 1);
                    nodes.remove(nodes.size() - 1);
                }
            }

            currentI = nodes.size();

        }

        println("coucou");
        println(nodes.size());
        int[] removeI = IntStream.range(0, nodes.size()).filter(i -> nodes.get(i) == null || node_speed.get(i) == null || node_lines.get(i) == null || node_speed.get(i).size() == 0).toArray();

        for (int i = removeI.length - 1; i >= 0; i--) {
            clearGrid(nodes.get(removeI[i]), nodeDiams.get(removeI[i]));

            nodes.remove(removeI[i]);
            node_speed.remove(removeI[i]);
            node_lines.remove(removeI[i]);
            nodeDiams.remove(removeI[i]);
        }
        num_nodes = nodes.size();

        println(nodes.size());

        //removeIntruders();
        current_size = Arrays.stream(grid).mapToInt(row -> Arrays.stream(row).sum()).sum();
        prev_size = 0;


        populate();

    }

    public void populate() {
        while (prev_size != current_size) {
            prev_size = current_size;
            for (int i = 0; i < num_nodes; i++) {

                PVector current_speed = node_speed.get(i).get(node_speed.get(i).size() - 1).copy();
                PVector candidate = node_lines.get(i).get(node_lines.get(i).size() - 1).copy();
                PVector previous_candidate = node_lines.get(i).get(node_lines.get(i).size() - 2).copy();


                PVector candidateP2 = PVector.add(candidate, PVector.mult(current_speed, 2));
                PVector candidateP1 = PVector.add(candidate, current_speed);

                if (current_speed.mag() == step && isInsidePolygon(candidateP2, bounds) && isGridFree(candidateP2)
                        && isInsidePolygon(candidateP1, bounds) && isGridFree(candidateP1) && notCrossing(candidateP1, candidate)) {

                    fillGrid(candidateP1);
                    node_lines.get(i).add(candidateP1);
                    node_speed.get(i).add(current_speed.copy());

                } else {
                    PVector new_dir = pivot_dirs[0].copy();
                    new_dir.mult(step);

                    int count = 1;
                    PVector new_candidate = PVector.add(candidate, new_dir);
                    while (!(isInsidePolygon(new_candidate, bounds)
                            && isGridFree(new_candidate)
                            && PVector.dist(new_candidate, previous_candidate) > step
                            && notCrossing(new_candidate, candidate)) && count < 4
                    ) {
                        new_dir = pivot_dirs[count].copy();
                        new_dir.mult(step);
                        new_candidate = PVector.add(candidate, new_dir);
                        count++;
                    }

                    if (isInsidePolygon(new_candidate, bounds)
                            && isGridFree(new_candidate)
                            && PVector.dist(new_candidate, previous_candidate) > step
                            && notCrossing(new_candidate, candidate)
                    ) {


                        fillGrid(new_candidate);
                        node_lines.get(i).add(new_candidate);
                        node_speed.get(i).add(new_dir.copy());

                        PVector new_dir2 = new_dir.copy();
                        new_dir2.x = 0;

                        PVector new_candidate2 = PVector.add(new_candidate, new_dir2);
                        if (!(isInsidePolygon(new_candidate2, bounds)
                                && isGridFree(new_candidate2)
                                && notCrossing(new_candidate2, new_candidate))) {
                            new_dir2 = new_dir.copy();
                            new_dir2.y = 0;
                            new_candidate2 = PVector.add(new_candidate, new_dir2);
                        }

                        if ((isInsidePolygon(new_candidate2, bounds)
                                && isGridFree(new_candidate2) && notCrossing(new_candidate2, new_candidate))) {

                            fillGrid(new_candidate2);
                            node_lines.get(i).add(new_candidate2);
                            node_speed.get(i).add(new_dir2.copy());
                        }
                    }

                }
            }

            if (!switchBool) {
                // removeIntruders();
            }

            //switchBool = !switchBool;


            //println("finished" + n);
            n++;

            current_size = Arrays.stream(grid).mapToInt(row -> Arrays.stream(row).sum()).sum();
        }
        started = true;
    }


    public void draw() {
        background(255);
        // if (!started)
        svg.draw(this);

//        pushStyle();
//        stroke(128);
//        IntStream.range(0, grid.length)
//                .forEach(i -> IntStream.range(0, grid[0].length)
//                        .forEach(j -> {
//                            if (grid[i][j] == 1) {
//                                fill(128);
//                            } else {
//                                fill(255);
//                            }
//                            rect(i * grid_size, j * grid_size, grid_size, grid_size);
//                        }));
//
//        popStyle();


        fill(255);
        // noFill();
        stroke(0);

        rect(0, 0, 50, 50);


        if (started) {

            for (int i = 0; i < nodes.size(); i++) {
                svg2.setStroke(1);

                svg2.addChild(RShape.createLine(nodes.get(i).x, nodes.get(i).y, node_lines.get(i).get(0).x, node_lines.get(i).get(0).y));
                line(nodes.get(i).x, nodes.get(i).y, node_lines.get(i).get(0).x, node_lines.get(i).get(0).y);
                for (int j = 0; j < node_lines.get(i).size() - 1; j++) {
                    pushStyle();

                    line(
                            node_lines.get(i).get(j).x,
                            node_lines.get(i).get(j).y,
                            node_lines.get(i).get(j + 1).x,
                            node_lines.get(i).get(j + 1).y);

                    svg2.addChild(RShape.createLine(node_lines.get(i).get(j).x,
                            node_lines.get(i).get(j).y,
                            node_lines.get(i).get(j + 1).x,
                            node_lines.get(i).get(j + 1).y));

                    popStyle();

                }

                ellipse(nodes.get(i).x, nodes.get(i).y, nodeDiams.get(i), nodeDiams.get(i));
                svg2.addChild(RShape.createEllipse(nodes.get(i).x, nodes.get(i).y, nodeDiams.get(i), nodeDiams.get(i)));

                if (node_lines.get(i).size() > 0) {
                    svg2.addChild(RShape.createEllipse(node_lines.get(i).get(node_lines.get(i).size() - 1).x, node_lines.get(i).get(node_lines.get(i).size() - 1).y, node_radius, node_radius));
                    ellipse(node_lines.get(i).get(node_lines.get(i).size() - 1).x, node_lines.get(i).get(node_lines.get(i).size() - 1).y, node_radius, node_radius);
                }
            }

            RG.saveShape("/Volumes/Yoyo/Dev/" + year() + "-" + month() + "-" + day() + "." + hour() + minute() + "." + second() + "_" + filename, svg2);

            // endRecord();
            exit();


        } else {
            for (int i = 0; i < bounds.size(); i++) {
                line(bounds.get(i).x, bounds.get(i).y, bounds.get((i + 1) % bounds.size()).x, bounds.get((i + 1) % bounds.size()).y);
            }
        }

    }

    public void mousePressed() {
        if (mouseX < 50 && mouseY < 50) {
            thread("init");
        } else if (mouseX > 50 && mouseX < 100 && mouseY < 50) {

        } else {
            bounds.add(new PVector(mouseX, mouseY));
        }

    }

    private static boolean isInsidePolygon(PVector X, ArrayList<PVector> polygon) {
        int i, j = polygon.size() - 1;
        int sides = polygon.size();
        boolean oddNodes = false;
        for (i = 0; i < sides; i++) {
            if ((polygon.get(i).y < X.y && polygon.get(j).y >= X.y || polygon.get(j).y < X.y && polygon.get(i).y >= X.y) && (polygon.get(i).x <= X.x || polygon.get(j).x <= X.x)) {
                oddNodes ^= (polygon.get(i).x + (X.y - polygon.get(i).y) / (polygon.get(j).y - polygon.get(i).y) * (polygon.get(j).x - polygon.get(i).x) < X.x);
            }
            j = i;
        }
        return oddNodes;
    }

    private static boolean isInsidePolygon(PVector X, float diameter, ArrayList<PVector> polygon) {
        for (int i = 0; i < 10; i++) {
            float posX = X.x + diameter * 0.5f * cos(i * TWO_PI / 10);
            float posY = X.y + diameter * 0.5f * sin(i * TWO_PI / 10);

            if (!isInsidePolygon(new PVector(posX, posY), polygon))
                return false;
        }

        return true;
    }

    public static boolean isInsidePolygon(int X, int Y, int[][] polygon) {
        int i, j = polygon.length - 1;
        int sides = polygon.length;
        boolean oddNodes = false;
        for (i = 0; i < sides; i++) {
            if ((polygon[i][1] < Y && polygon[j][1] >= Y || polygon[j][1] < Y && polygon[i][1] >= Y) && (polygon[i][0] <= X || polygon[j][0] <= X)) {
                oddNodes ^= (polygon[i][0] + (Y - polygon[i][1]) / (polygon[j][1] - polygon[i][1]) * (polygon[j][0] - polygon[i][0]) < X);
            }
            j = i;
        }
        return oddNodes;
    }

    private boolean notCrossing(PVector current, PVector previous) {
        for (ArrayList<PVector> node_line : node_lines) {
            for (int j = 0; j < node_line.size() - 1; j++) {

                if (crossingVect(previous, current, node_line.get(j), node_line.get(j + 1)))
                    return false;

            }
        }
        return true;
    }

    private boolean crossingVect(PVector v1p1, PVector v1p2, PVector v2p1, PVector v2p2) {
        PVector v1 = PVector.sub(v1p2, v1p1);
        PVector v2 = PVector.sub(v2p2, v2p1);

        float a = v1.y / v1.x;
        float b = v1p1.y - a * v1p1.x;

        float c = v2.y / v2.x;
        float d = v2p1.y - c * v2p1.x;

        float x = (b - d) / (c - a);
        float y = a * x + b;


        PVector intersect = new PVector(x, y);

        return PVector.dist(v2p1, intersect) < PVector.dist(v2p1, v2p2) && v2.dot(PVector.sub(intersect, v2p1)) > 0
                && PVector.dist(v1p1, intersect) < PVector.dist(v1p1, v1p2) && v1.dot(PVector.sub(intersect, v1p1)) > 0;
    }

    private void removeIntruders() {

        ArrayList<PVector> intruders = new ArrayList<>();
        for (int i = 0; i < node_lines.size(); i++) {
            for (int k = i + 1; k < node_lines.size(); k++) {
                int last_indexI = node_lines.get(i).size() - 1;
                int last_indexK = node_lines.get(k).size() - 1;

                if (last_indexI > 1) {
                    PVector v1 = PVector.sub(node_lines.get(i).get(last_indexI - 1), node_lines.get(i).get(last_indexI));
                    PVector v2 = PVector.sub(node_lines.get(k).get(last_indexK - 1), node_lines.get(k).get(last_indexK));

                    if (PVector.dot(v1, v2) == 0) {

                        intruders.add(new PVector(i, last_indexI));


                    }
                }
            }
        }


        intruders.sort((v2, v1) -> {
            if (v1.x < v2.x)
                return 1;
            if (v1.x == v2.x) {
                if (v1.y < v2.y)
                    return 1;
                if (v1.y == v2.y)
                    return 0;
                if (v1.y > v2.y)
                    return -1;
            }

            return -1;
        });

        intruders = (ArrayList<PVector>) intruders.stream().distinct().collect(Collectors.toList());

        //intruders.forEach(v->println(v));

        for (int i = intruders.size() - 1; i >= 0; i--) {
            println(intruders.size());
            clearGrid(node_lines.get((int) intruders.get(i).x).get((int) intruders.get(i).y));
            node_lines.get((int) intruders.get(i).x).remove((int) intruders.get(i).y);
            node_speed.get((int) intruders.get(i).x).remove((int) intruders.get(i).y);
        }
    }

    private int[] getGPos(PVector v) {
        return new int[]{(int) (v.x / grid_size), (int) (v.y / grid_size)};
    }

    private boolean isGridFree(PVector v) {
        return (v.x >= 0 && v.x < width && v.y >= 0 && v.y < height) && grid[getGPos(v)[0]][getGPos(v)[1]] == 0;
    }

    private boolean isGridFree(PVector v, float diameter) {
        if (!(v.x >= 0 && v.x < width && v.y >= 0 && v.y < height))
            return false;

        int[] min = getGPos(PVector.sub(v, new PVector(diameter * 0.5f, diameter * 0.5f)));
        int[] max = getGPos(PVector.add(v, new PVector(diameter * 0.5f, diameter * 0.5f)));

        for (int i = min[0]; i <= max[0]; i++) {
            for (int j = min[1]; j <= max[1]; j++) {

                if (grid[i][j] == 1)
                    return false;
            }
        }

        return true;
    }

    private void fillGrid(PVector v) {
        grid[getGPos(v)[0]][getGPos(v)[1]] = 1;
    }

    private void fillGrid(PVector v, float diameter) {
        int[] min = getGPos(PVector.sub(v, new PVector(diameter * 0.5f, diameter * 0.5f)));
        int[] max = getGPos(PVector.add(v, new PVector(diameter * 0.5f, diameter * 0.5f)));

        for (int i = min[0]; i <= max[0]; i++) {
            for (int j = min[1]; j <= max[1]; j++) {

                grid[i][j] = 1;
            }
        }
    }

    private void clearGrid(PVector v) {
        grid[getGPos(v)[0]][getGPos(v)[1]] = 0;
    }

    private void clearGrid(PVector v, float diameter) {
        int[] min = getGPos(PVector.sub(v, new PVector(diameter * 0.5f, diameter * 0.5f)));
        int[] max = getGPos(PVector.add(v, new PVector(diameter * 0.5f, diameter * 0.5f)));

        for (int i = min[0]; i <= max[0]; i++) {
            for (int j = min[1]; j <= max[1]; j++) {

                grid[i][j] = 0;
            }
        }
    }

}
