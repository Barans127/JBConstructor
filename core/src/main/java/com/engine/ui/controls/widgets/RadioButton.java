package com.engine.ui.controls.widgets;

import com.engine.ui.controls.Connected;
import com.engine.ui.controls.ControlHost;


public class RadioButton extends CheckBox implements Connected {
    private int groupIndex;

    public RadioButton(){
        this(new RadioButtonStyle());
    }

    public RadioButton(RadioButtonStyle st){
        super(st);
        groupIndex = st.group;
    }

    public RadioButton(String name){
        this(name, new RadioButtonStyle());
    }

    public RadioButton(String name, RadioButtonStyle st){
        super(name, st);
        groupIndex = st.group;
    }

    /* Radio buttons funkc. */

    /** Grupuoja radioButtons. Toj pačioj grupėj esantiem RadioButtonai, gali būti pažymėtas tik vienas. Vieną pažymėjus kiti bus atžymėti. */
    public void setGroup(int groupIndex){
        this.groupIndex = groupIndex;
        if (isChecked()){ // kad patikrintu ar nera tos pacios grupes
            v.inform(this, 0);
        }
    }

    /* Override */

    @Override
    protected void onRemove() {
        if (v != null){
            v.removeConnection(this);
        }
    }

    @Override
    public void setController(ControlHost v) {
        if (this.v != null){
            this.v.removeConnection(this);
        }
        super.setController(v);
        if (v != null){
            v.addConnection(this);
            if (isChecked()){
                v.inform(this, 0);
            }
        }
    }

    @Override
    void checkStateTriggered() {
        if (!isChecked()) {
            v.inform(this, 0);
            super.checkStateTriggered();
        }
    }

    @Override
    public void setChecked(boolean check) {
        if (check){
            v.inform(this, 0);
        }
        super.setChecked(check);
    }

    @Override
    public void inform(int reason) {
        setChecked(false);
    }

    @Override
    public int getGroup() {
        return groupIndex;
    }

    /* style */

    @Override
    public RadioButtonStyle getStyle(){
        RadioButtonStyle e = new RadioButtonStyle();
        copyStyle(e);
        return e;
    }

    public void copyStyle(RadioButtonStyle st){
        super.copyStyle(st);
        st.group = getGroup();
    }

    public void readStyle(RadioButtonStyle st){
        super.readStyle(st);
        setGroup(st.group);
    }

    public static class RadioButtonStyle extends CheckBoxStyle{
        public int group = 0;

        public RadioButtonStyle(){
            text = "New RadioButton";
        }

        @Override
        public RadioButton createInterface() {
            return new RadioButton(this);
        }
    }
}
