package com.ang.Core;

public class IntList {
    public int[] elements;
    private int end;
    public int defaultVal;

    public IntList() {}

    public IntList(int maxElements, int defaultValue) {
        elements = new int[maxElements];
        defaultVal = defaultValue;
        for (int i = 0; i < maxElements; i++) {
            elements[i] = defaultValue; 
        }
    }

    public IntList(int maxElements) {
        elements = new int[maxElements];
        defaultVal = 0;
        end = 0;
    }

    public IntList copy() {
        IntList tempList = new IntList();

        tempList.elements = this.elements.clone();
        tempList.end = this.end;
        tempList.defaultVal = this.defaultVal;

        return tempList;
    }

    public void add(int e) {
        if (end >= elements.length) {
            return;
        }
        elements[end++] = e;
    }

    public void rem(int e) {
        for (int i = 0; i < end; i++) {
            if (elements[i] == e) {
                elements[i] = elements[--end];
                elements[end--] = defaultVal;
            }
        }
    }

    public int length() {
        return end;
    }

    public int at(int i) {
        return elements[i];
    }

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
