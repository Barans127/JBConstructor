package com.engine.ui.controls.widgets;

/** Switch with two states. on and off. */
public class Switch extends ScrollBar {
    private int tintOff, tintOn;

    public Switch(){
        this(new SwitchStyle());
    }

    public Switch(SwitchStyle st){
        super(st);
        if (st.tintOff == -1){
            tintOff = st.tintBackground;
        }else
            tintOff = st.tintOff;
        if (st.tintOn == -1)
            tintOn = st.tintBackground;
        else
            tintOn = st.tintOn;
        super.setJumpsCount(2); // tik dvi pozicijos.
        changeBackground();
//        super.setFixedStop(true); // tegul pats naudotojas renkas.
    }

    public void setState(boolean value) {
        setValue(value ? 1 : 0);
    }

    /** on ar off. */
    public boolean isOn(){
        return getValue() == 1;
    }

    private void changeBackground(){
        if (isOn()){
            setBackgroundTint(tintOn);
        }else
            setBackgroundTint(tintOff);
    }

    @Override
    protected void scrollTriggered() {
        changeBackground();
        super.scrollTriggered();
    }

    @Override
    public void setValue(int value) {
        super.setValue(value);
        changeBackground();
    }

    @Override
    public void setJumpsCount(int jumps) { } // nereikia.

    /* style */

    @Override
    public SwitchStyle getStyle(){
        SwitchStyle e = new SwitchStyle();
        copyStyle(e);
        return e;
    }

    public void copyStyle(SwitchStyle st){
        super.readStyle(st);
        st.tintOff = tintOff;
        st.tintOn = tintOn;
    }

    public void readStyle(SwitchStyle st){
        super.readStyle(st);
        if (st.tintOff == -1){
            tintOff = st.tintBackground;
        }else
            tintOff = st.tintOff;
        if (st.tintOn == -1)
            tintOn = st.tintBackground;
        else
            tintOn = st.tintOn;
    }

    public static class SwitchStyle extends ScrollBarStyle {
        /**
         * tints background by state
         */
        public int tintOff = -1, tintOn = -1;

        @Override
        public Switch createInterface() {
            return new Switch(this);
        }
    }
}
