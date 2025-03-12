package com.ang.Core.Moves;

/**
 * Class for a fixed-size list of moves that can be added to easily
 */
public class MoveList {
    private Move[] elements;
    private int end;

    /**
     * constructs a movelist with a maximum length
     * @param maxElements maximum length of list
     */
    public MoveList(int maxElements) {
        elements = new Move[maxElements];
        end = 0;
    }

    /**
     * adds an element to the list
     * @param m move to be added
     */
    public void add(Move m) {
        if (end >= elements.length) {
            return;

        }
        elements[end++] = m;
    }

    /**
     * combines 2 MoveLists into 1
     * @param ml MoveList to be added to the current one
     */
    public void add(MoveList ml) {
        for (int i = 0; i < ml.length(); i++) {
            Move m = ml.at(i);
            if (m == null) {
                return;

            }
            add(m);
        }
    }

    /**
     * Pushes attacks towards the start of the list
     */
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

    /**
     * Sends a particular move to the start of the list
     * @param m move to be pushed to the start
     */
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

    /**
     * Randomizes the order of the moves in the movelist
     */
    public void randomize() {
        for (int i = 0; i < length(); i++) {
            int randomIndex = (int) Math.floor(Math.random() * i);
            Move temp = elements[i];
            elements[i] = elements[randomIndex];
            elements[randomIndex] = temp;
        }
    }

    /**
     * @return the length of the list
     */
    public int length() {
        return end;

    }

    /**
     * @param i index into internal elements[]
     * @return the element at index i
     */
    public Move at(int i) {
        return elements[i];

    }

    /**
     * checks if the list contains a move to a given square
     * @param pos position to check against
     * @return {@code true} if @param pos is found, else {@code false}
     */
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

    /**
     * checks if the list contains a move from a given square
     * @param pos position to check against
     * @return {@code true} if @param pos is found, else {@code false}
     */
    public boolean containsFrom(int pos) {
        for (Move m : elements) {
            if (m == null) {
                return false;

            }
            if (m.from == pos) {
                return true;

            }
        }
        return false;

    }

    /**
     * checks if the list contains a move
     * @param move the move to check agains
     * @return {@code true} is @param move is found, else {@code false}
     */
    public boolean containsMove(Move move) {
        for (Move m : elements) {
            if (m == null) {
                return false;

            }
            if ((m.flag != MoveFlag.ONLY_ATTACK) && (m.equals(move))) {
                return true;

            }
        }
        return false;

    }
}
