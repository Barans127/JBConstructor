package com.engine.interfaces.controls.dialogs;

import com.badlogic.gdx.Input;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.TextBox;

public class InputTextDialog extends ConfirmDialog {
    private TextBox input;
    private InputDialogListener listener;

    public InputTextDialog(String text){
        this(new Label.LabelStyle());
        setText(text);
    }

    public InputTextDialog(Label.LabelStyle labelStyle){
        this(labelStyle, new TextBox.TextBoxStyle());
        input.setBackground(yes.getBackground()); // tiks vistiek
        updateBounds();
    }

    public InputTextDialog(Label.LabelStyle labelStyle, TextBox.TextBoxStyle textBoxStyle) {
        super(ConfirmDialogType.YesNoCancel, labelStyle);
        no.setVisible(false);
//        no.setSize(1, 1);
        yes.setText("OK");
        input = new TextBox(textBoxStyle);
        addControl(input, getControls().size()-1); // kad butu ant virsaus.
    }

    public void setInputDialogListener(InputDialogListener e){
        listener = e;
    }

    public InputDialogListener getInputDialogListener() {
        return listener;
    }

    public TextBox getInput() {
        return input;
    }

    public void show(String labelText, String textBoxText){
        setText(labelText);
        input.setText(textBoxText);
        open();
    }

    @Override
    public void show() {
        super.show();
        input.setText("");
    }

    //    @Override
//    public void onClose() {
//        super.onClose();
//        input.setText(""); // isvalom.
//    }

    @Override
    protected void onOpen() {
        super.onOpen();
        input.getFocus(); // kad iskart atsidarius butu galima rasyt.
    }

    @Override
    protected void onYes() {
        if (listener!=null){
            listener.onInput(input.getText().trim());
        }
    }

    @Override
    protected void onCancel() {
        if (listener != null)
            listener.cancel();
    }

    @Override
    public void updateBounds() {
        super.updateBounds();
        input.setSize(getWidth()*0.9f, input.getHeight());
        setSize(getWidth(), getHeight() + input.getHeight());
        text.setPosition(10, getHeight()-text.getHeight()-10);
        input.setPosition(getWidth()*0.05f, cancel.getHeight()+10);
        cancel.setPosition(getWidth() - yes.getWidth() - cancel.getWidth() - 40, 5); // truputi skyrias nei super metode.
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER){
            yes.performClick();
            return true;
        } else return super.keyDown(keycode);
    }

    public interface InputDialogListener{
        void onInput(String input);
        void cancel();
    }
}
