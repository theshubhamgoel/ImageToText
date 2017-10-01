package com.shubham.ImageToText;

public class Position implements Comparable<Position> {
    int i;
    int j;

    public Position(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public Position() {
    }

    @Override
    public String toString() {
        return "Position : " + i + " " + j;
    }

    @Override
    public int compareTo(Position o) {
        return 0;
    }

}
