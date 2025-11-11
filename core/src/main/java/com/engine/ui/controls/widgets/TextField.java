package com.engine.ui.controls.widgets;

import com.engine.ui.controls.Draggable;
import com.engine.ui.controls.Control;

public class TextField extends Draggable {

    public TextField(TextFieldStyle st){
        super(st);
    }

    /* size nustatymai */

    @Override
    protected void autoSize() {

    }

    /* isvaizda */

    @Override
    protected void isvaizda(float x, float y) {

    }

    /* fokusavimas */

    @Override
    protected void onFocus() {

    }

    /* inputs */
    /* mouse inputs. */

    @Override
    protected void onPress(float x, float y) {
        super.onPress(x, y);
    }

    @Override
    protected void onDragging(float x, float y, float deltaX, float deltaY) {

    }

    @Override
    protected void onDrop(float x, float y, float deltaX, float deltaY) {

    }

    /* keyboard inputs. */

    @Override
    public boolean keyDown(int keycode) {
        return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return super.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char e) {
        return super.keyTyped(e);
    }

    /* style */

    /** Creates and returns text field style. */
    public TextFieldStyle getStyle(){
        TextFieldStyle e = new TextFieldStyle();
        copyStyle(e);
        return e;
    }

    /** copy all attributes to given textFieldStyle. Style must not be null - style is not checked! */
    public void copyStyle(TextFieldStyle st){
        super.copyStyle(st);
    }

    /** reads and sets all attributes from given style. Style must not be null - style is not checked!*/
    public void readStyle(TextFieldStyle st){
        super.readStyle(st);
    }

    public static class TextFieldStyle extends ClickableStyle {


        @Override
        public Control createInterface() {
            return null;
        }
    }
}
