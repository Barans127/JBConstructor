package com.engine.interfaces.controls.dialogs;

import com.badlogic.gdx.Input;
import com.engine.core.ErrorMenu;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Form;
import com.engine.interfaces.controls.PopUp;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.listeners.ClickListener;

/** Confirm dialog. Default text size is - 40. Buttons text size - 30. */
public class ConfirmDialog extends PopUp {
    protected Button yes, no, cancel;
    protected Label text;
    private ConfirmDialogListener listener;

    // biski valdymo
    private float minWidth = 650, minHeight = 126; // default tokie lai buna
    private float jumpSize = 40;
    private float buttonMarginX = 20, buttonMarginY = 5, textMarginX = 10, textMarginY = 5;

    private boolean escapeActive = false;

    public ConfirmDialog(ConfirmDialogType type){
        this(type, new Label.LabelStyle());
    }

    public ConfirmDialog(ConfirmDialogType type, String text){
        this(type, new Label.LabelStyle());
        setText(text);
    }

    public ConfirmDialog(ConfirmDialogType type, final Label.LabelStyle labelStyle) {
        super(100, 100);

        // label
        text = new Label(labelStyle);
        addControl(text);

        // buttons paruosimas
        Button.ButtonStyle st = new Button.ButtonStyle();
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        st.background = Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor"));
        st.textSize = 33;
        st.text = "Yes";
        st.normalColor = 0xFF0000FF;
        yes = new Button(st);
        st.text = "No";
        no = new Button(st);
        st.text = "Cancel";
        cancel = new Button(st);
        addControl(cancel, no, yes);
        yes.auto(); // kad iskart pasigautu dydzius.
        no.auto();
        cancel.auto();
        yes.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                ConfirmDialog.this.close();
                onYes();
                if (listener != null) {
                    listener.onYes();
                }
            }
        });
        no.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                ConfirmDialog.this.close();
                onNo();
                if (listener != null) {
                    listener.onNo();
                }
            }
        });
        cancel.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                ConfirmDialog.this.close();
                onCancel();
                if (listener != null) {
                    listener.onCancel();
                }
            }
        });

        setType(type);
        if (labelStyle.text == null || labelStyle.text.equals("")) // jei tik tekstas bus tuscias.
            setText("This is confirm dialog ^^\nText was not set for this dialog.");
    }

    /* parametru keitimas. */

    /** Text margin. Use this to move text from side walls. Value with less than zero is ignored. */
    public void setTextMarginX(float marginX){
        if (marginX >= 0){
            textMarginX = marginX;
        }
    }

    /** Text margin. Use this to move text from side walls. Value with less than zero is ignored. */
    public void setTextMarginY(float marginY){
        if (marginY >= 0){
            textMarginY = marginY;
        }
    }

    /** buttons margin. Value with less than zero is ignored. */
    public void setButtonMargin(float marginX, float marginY){
        if (marginX >= 0){
            buttonMarginX = marginX;
        }
        if (marginY >= 0){
            buttonMarginY = marginY;
        }
    }

    /** minHeight will be 3.8 ratio of minWidth */
    public void setMinSizeByRatio(float minWidth){
        setMinSizeByRatio(minWidth, 3.8f);
    }

    /** Call this when you manipulated label text size or buttons size. */
    public void autoMinSize(){
//        float textSize = text.getTextSize()*1.5f; // pusantro karto didesnis nei label dydis
        float textSize = text.getTextSize();
        yes.auto();
        no.auto();
        cancel.auto(); // sugaudom mygtuku dydzius.
        float width, height;
        width = buttonMarginX*2 + cancel.getWidth() + buttonMarginX + no.getWidth() + buttonMarginX + yes.getWidth();
        height = textSize + buttonMarginY*2 + yes.getHeight();
        setMinSize(width, height);
    }

    /** minWidth value is divided by ratio to get height. */
    public void setMinSizeByRatio(float minWidth, float ratio){
        if (ratio == 0){
            p.setError("ConfirmDialog: ratio cannot be 0", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        this.minWidth = minWidth;
        this.minHeight = minWidth/ratio;
    }

    /** minimum size of this confirmation. This size will be increased if text doesn't fit.
     * You should change minimum size if you changed textSize of label or buttons size. */
    public void setMinSize(float minWidth, float minHeight){
        this.minWidth = minWidth;
        this.minHeight = minHeight;
    }

    /** minimum width */
    public float getMinWidth(){
        return minWidth;
    }

    /** minimum height*/
    public float getMinHeight(){
        return minHeight;
    }

    /** if text size is bigger than min size, than dialog will be increased by this. */
    public void setJumpSize(float jumpSize){
        this.jumpSize = jumpSize;
    }

    /** if text size is bigger than min size, than dialog will be increased by this. */
    public float getJumpSize(){
        return jumpSize;
    }

    public void changeTextStyle(Label.LabelStyle st){
        text.readStyle(st);
    }

    public Label getLabel() {
        return text;
    }

    public String getText(){
        return text.getText();
    }

    /** how much each button is separated */
    public float getButtonMarginX(){
        return buttonMarginX;
    }

    /** how much button is lifted from bottom. */
    public float getButtonMarginY() {
        return buttonMarginY;
    }

    /** how much text is pushed away from sides */
    public float getTextMarginX() {
        return textMarginX;
    }

    /** how much text is pushed from up/down sides. */
    public float getTextMarginY(){
        return textMarginY;
    }

    /** @return if this dialog reacts to escape or back button. If dialog type is YesNo then escape or back calls no.
     * If dialog type YesNoCancel then escape or back calls cancel. */
    public boolean isEscapeActive(){
        return escapeActive;
    }

    /** Set how dialog reacts to escape or back button. If dialog type is YesNo then escape or back calls no.
     * If dialog type YesNoCancel then escape or back calls cancel. */
    public void setEscapeActive(boolean active){
        escapeActive = active;
    }

    /** Custom buttons names. */
    public void setButtonsText(String yes, String no, String cancel){
        this.yes.setText(yes);
        this.cancel.setText(cancel);
        this.no.setText(no);
        autoMinSize();
        updateBounds();
    }

    /** set Custom buttons. value of null will not change current button style */
    public void setCustomButtonsStyle(Button.ButtonStyle yes, Button.ButtonStyle no, Button.ButtonStyle cancel){
        if (yes != null)
            this.yes.readStyle(yes);
        if (no != null)
            this.no.readStyle(no);
        if (cancel != null)
            this.cancel.readStyle(cancel);
    }

    /** set custom style for all buttons. Value of null is ignored. */
    public void setCustomButtonsStyle(Button.ButtonStyle style){
        setCustomButtonsStyle(style, style, style);
    }

    /* veikimas */

    /** updates bounds by current text size and buttons size. */
    public void updateBounds(){
        String text = this.text.getText();
//        float width = p.textWidth(text, this.text.getTextController().getFontScaled());
//        float height = p.textHeight();
        boolean isNotSet = true;
        float minWidth = this.minWidth;
//        float minHeight = this.minHeight*0.8f-10;
        float minHeight = this.minHeight - textMarginY*2;
        boolean increaseHeight = false;
        while (isNotSet){
            p.textWidth(text, minWidth-textMarginX*2, this.text.getTextController().getFontScaled()); // bug fix kai netelpa zodis. Reikejo margin atimt, kitaip didesni dydi duoda nei yra.
            float theight = p.textHeight();
            if (theight > minHeight){
                if (increaseHeight){
                    minHeight += jumpSize;
                    increaseHeight = false;
                }else {
                    minWidth += jumpSize;
                    increaseHeight = true;
                }
            }else { // tekstas tilpo
                isNotSet = false;
            }
        }
        if (minWidth > p.getScreenWidth())// kad nelystu is ekrano ribu.
            minWidth = p.getScreenWidth();
        // turi im visa bendra dydi.
        float rMinHeight = minHeight+yes.getHeight() + buttonMarginY*2 + textMarginY*2;
        if (rMinHeight > p.getScreenHeight())
            rMinHeight = p.getScreenHeight();
//        setSize(minWidth, minHeight+yes.getTextSize()*1.5f); // dialogo dydis
        setSize(minWidth, rMinHeight);
//        setPosition(p.getScreenWidth()/2-minWidth/2, p.getScreenHeight()/2-minHeight/2); // ??? du kart?
        this.text.setSize(minWidth-textMarginX*2, minHeight - textMarginY*2); // teksto lauko dydis
//        this.text.setPosition(textMarginX, yes.getTextSize()); // tik jo texto dydis?
        this.text.setPosition(textMarginX, yes.getHeight() + buttonMarginY*2 + textMarginY);
        yes.setPosition(minWidth - yes.getWidth() - buttonMarginX, buttonMarginY); // mygtuku naujos pozicijos.
        no.setPosition(minWidth - yes.getWidth() - no.getWidth() - buttonMarginX*2, buttonMarginY);
        cancel.setPosition(minWidth - yes.getWidth() - no.getWidth() - cancel.getWidth() - buttonMarginX*3, buttonMarginY);

        setPosition(p.getPreferredScreenWidth()/2 - getWidth()/2, p.getPreferredScreenHeight()/2 - getHeight()/2); // is naujo sucentruojam per viduri.
    }

    /** sets dialog text. */
    public void setText(String text){
        this.text.setText(text);
        updateBounds();
    }

    /** show dialog with given text */
    public void show(String text){
        setText(text);
        open();
    }

    /** opens dialog with already defined text. */
    public void show(){
        open();
    }

    /** opens dialog with already defined text. */
    public void show(Form form){
        open(form);
    }

    /* listerners, type */

    public void setType(ConfirmDialogType type){
        switch (type){
            case OK:
                yes.setText("OK");
                no.setVisible(false);
                cancel.setVisible(false);
                enableCloseButton(true);
                break;
            case YesNo:
                yes.setText("Yes");
                no.setVisible(true);
                cancel.setVisible(false);
                enableCloseButton(false);
                break;
            case YesNoCancel:
                yes.setText("Yes");
                no.setVisible(true);
                cancel.setVisible(true);
                enableCloseButton(false);
                break;
        }
    }

    public void setConfirmDialogListener(ConfirmDialogListener e){
        listener = e;
    }

    public ConfirmDialogListener getListener() {
        return listener;
    }

    public enum ConfirmDialogType{
        OK, YesNo, YesNoCancel;
    }

    protected void onYes(){}

    protected void onNo(){}

    protected void onCancel(){}

    public interface ConfirmDialogListener{
        /** yes or Ok button was clicked */
        public void onYes();
        /** no button was clicked */
        public void onNo();
        /** cancel button was clicked */
        public void onCancel();
    }

    // override, tik del escape active cia.

    @Override
    public boolean keyDown(int keycode) {
        // escape metoda mes handlinsim kitaip.
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            if (escapeActive) {
                ConfirmDialog.this.close(); // pirma uzdarom, visada listener ant galo!
                if (cancel.isVisible()) {
                    onCancel();
                    if (listener != null) {
                        listener.onCancel();
                    }
                } else if (no.isVisible()) {
                    onNo();
                    if (listener != null) {
                        listener.onNo();
                    }
                } else {
                    onYes();
                    if (listener != null) {
                        listener.onYes();
                    }
                }
                return true;
            }else {
                return false; // nieko nedarom. Ignorinam.
            }
        }
        return super.keyDown(keycode);
    }
}
