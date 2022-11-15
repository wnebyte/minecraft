package com.github.wnebyte.minecraft.ui;

import org.joml.Vector2f;

class JWindow {

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    Vector2f cursor;

    Vector2f position;

    Vector2f size;

    Vector2f lastElementPosition;

    Vector2f lastElementSize;

    int numCols;

    boolean centerNextElement;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    JWindow() {}

    JWindow(Vector2f position, Vector2f size, Vector2f cursor) {
        this.position = position;
        this.size = size;
        this.cursor = cursor;
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    Vector2f getCursor() {
        return cursor;
    }

    void setCursor(Vector2f cursor) {
        this.cursor = cursor;
    }

    Vector2f getPosition() {
        return position;
    }

    void setPosition(Vector2f position) {
        this.position = position;
    }

    Vector2f getSize() {
        return size;
    }

    void setSize(Vector2f size) {
        this.size = size;
    }

    Vector2f getLastElementPosition() {
        return lastElementPosition;
    }

    void setLastElementPosition(Vector2f lastElementPosition) {
        this.lastElementPosition = lastElementPosition;
    }

    Vector2f getLastElementSize() {
        return lastElementSize;
    }

    void setLastElementSize(Vector2f lastElementSize) {
        this.lastElementSize = lastElementSize;
    }

    int getNumCols() {
        return numCols;
    }

    void setNumCols(int numCols) {
        this.numCols = numCols;
    }

    boolean isCenterNextElement() {
        return centerNextElement;
    }

    void setCenterNextElement(boolean centerNextElement) {
        this.centerNextElement = centerNextElement;
    }
}
