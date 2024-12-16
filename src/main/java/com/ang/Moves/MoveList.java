package com.ang.Moves;

public class MoveList {
    private Move[] elements;
    private int end;

    public MoveList(int maxElements) {
        elements = new Move[maxElements];
        end = 0;
    }

    public void add(MoveList ml) {
        for (int i = 0; i < ml.length(); i++) {
            if (ml.at(i) == null) {
                return;
            }
            elements[end++] = ml.at(i);
        }
    }

    public void add(Move m){
        if (end >= elements.length) {
            System.err.println("Cannot add move to list, maximum reached");
            return;
        }
        elements[end++] = m;
    }

    public int length() {
        return end;
    }

    public Move at(int i) {
        return elements[i];
    }

    public boolean contains(int pos) {
        for (Move m : elements) {
            if (m == null) {
                return false;
            }
            if (m.to == pos) {
                return true;
            }
        }
        return false;
    }
}
