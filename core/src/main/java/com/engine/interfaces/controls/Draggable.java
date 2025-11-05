package com.engine.interfaces.controls;

/**
 * Allows interface to be dragged.
 */
public abstract class Draggable extends Clickable {
    private float firstX, firstY;
    private boolean isDragging;

    // pelytes tempimas, pasukimas, vidurio taskas.

//    public Draggable(){
//        this(new FieldStyle());
//    }

    public Draggable(ClickableStyle style){
        super(style);
    }

    /** imitates drag if interface is visible and enabled and clickable (see {@link #setUnClickable(boolean)}). */
    public void imitateDrag(float x, float y){
        if (isEnabled() && isVisible() && !isUnClickable()) {
            isDragging = true;
            imitateClick(x, y);
        }
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (super.pan(x, y, deltaX, deltaY))
            return true;
        if (isPressed() && isEnabled()){
            if (!isDragging)
                isDragging = true;
            onDragging(x, y, x - firstX, y - firstY);
            firstX = x;
            firstY = y;
            return true;
        }
        return false;
    }

    @Override
    protected void onPress(float x, float y) {
        firstX = x;
        firstY = y;
        super.onPress(x, y);
    }

    @Override
    protected void onRelease(float x, float y) {
        if (isDragging) {
            isDragging = false;
            onDrop(x, y, x - firstX, y - firstY);
        }
        super.onRelease(x, y);
    }

    @Override
    public void release() {
        super.release();
        isDragging = false;
    }

    public boolean isDragging(){
        return isDragging;
    }

    protected abstract void onDragging(float x, float y, float deltaX, float deltaY);

    protected abstract void onDrop(float x, float y, float deltaX, float deltaY);
}
