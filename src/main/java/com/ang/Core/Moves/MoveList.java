package com.ang.Core.Moves;

public class MoveList {
    private Move[]  elements;
    private int     end;

    public MoveList(int maxElements) {
        elements    = new Move[maxElements];
        end         = 0;
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
            System.err.println("Cannot add move " + m.from + " to " + m.to
                    + " to list, maximum reached");
            return;
        }
        elements[end++] = m;
    }

    public void attacksToFront() {
        Move[] newElements = new Move[elements.length];
        int newElementsEnd = 0;
        for (Move move : elements) {
            if (move == null) {
                break;
            }
            if (move.attack) {
                newElements[newElementsEnd++] = move;
            }
        }
        for (Move move : elements) {
            if (move == null) {
                break;
            }
            if (!move.attack) {
                newElements[newElementsEnd++] = move;
            }
        }
        elements = newElements;
    }

    public void sendToFront(Move m) {
        Move[] newElements = new Move[elements.length];
        newElements[0] = m;
        int newElementsEnd = 1;
        for (Move move : elements) {
            if (move == null) {
                break;
            }
            if (!move.equals(m)) {
                newElements[newElementsEnd++] = move;
            }
        }
        elements = newElements;
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
