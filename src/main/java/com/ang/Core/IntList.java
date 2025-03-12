package com.ang.Core;

/**
 * Class for a fixed-size list of integers that can be added to easily
 */
public class IntList {
    public int[] elements;
    private int end;
    public int defaultVal;

    /**
     * Constructs a blank IntList
     */
    public IntList() {}

    /**
     * Constructs with a maximum length and default value for each element
     * @param maxElements maximum length of list
     * @param defaultValue default value for each element
     */
    public IntList(int maxElements, int defaultValue) {
        elements = new int[maxElements];
        defaultVal = defaultValue;
        for (int i = 0; i < maxElements; i++) {
            elements[i] = defaultValue; 
        }
    }

    /**
     * Constructs with only a maximum length
     * @param maxElements maximum length of list
     */
    public IntList(int maxElements) {
        elements = new int[maxElements];
        defaultVal = 0;
        end = 0;
    }

    /**
     * Creates an unlinked copy of the list
     * @return
     */
    public IntList copy() {
        IntList tempList = new IntList();
        tempList.elements = this.elements.clone();
        tempList.end = this.end;
        tempList.defaultVal = this.defaultVal;
        return tempList;

    }

    /**
     * Adds an element to the list
     * @param e element to add
     */
    public void add(int e) {
        if (end >= elements.length) {
            return;

        }
        elements[end++] = e;
    }

    /**
     * Removes an element from the list - removes all intances if there are duplicates
     * @param e element to remove
     */
    public void rem(int e) {
        if (end == 0) {
            System.err.println("Cannot remove from empty array - IntList.");
            return;

        }
        for (int i = 0; i < end; i++) {
            if (elements[i] == e) {
                elements[i] = elements[--end];
                elements[end] = defaultVal;
            }
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
    public int at(int i) {
        return elements[i];

    }

    /**
     * @param target the int to search for
     * @return {@code true} if the element is contained in the list, else {@code false}
     */
    public boolean contains(int target) {
        for (int e : elements) {
            if (e == defaultVal) {
                return false;

            }
            if (e == target) {
                return true;

            }
        }
        return false;
        
    }
}
