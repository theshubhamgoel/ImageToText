package com.shubham.ImageToText;

public class LetterBox {
    int top;
    int bottom;
    int left;
    int right;

    public LetterBox(int top, int bottom, int left, int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public LetterBox() {

    }

    @Override
    public String toString() {
        return "Boundaries are : " + top + " " + bottom + " " + left + " " + right;
    }

}
