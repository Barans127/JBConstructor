package com.engine.interfaces.controls.views;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Connected;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.listeners.ClickListener;

/** This class adds active items which can mark themselves. Also you can drag and change position of item. */
public class PreparedListView extends ListView {
    private ActiveItemClickListener listener;
    private SymbolButton.SymbolButtonStyle activeStyle;
//    private boolean enablePositionChange;
    private int normal, choose;

    private boolean multiSelect;

    // kad nebutu kuriami daiktai per naujo ir per naujo atnaujinant sarasus.
    private Pool<Button> buttonPool;
    private Pool<SymbolButton> symbolButtonPool;

    public PreparedListView(){
        this(new PreparedListViewStyle());
    }

    public PreparedListView(PreparedListViewStyle st){
        super(st);
//        enablePositionChange = st.enablePositionChange;
        activeStyle = st.activeItemsStyle;
        normal = st.normalState;
        choose = st.choosenState;
        multiSelect = st.multiSelect;

        buttonPool = new Pool<Button>() {
            @Override
            protected Button newObject() {
                return new Button(activeStyle);
            }
        };

        symbolButtonPool = new Pool<SymbolButton>() {
            @Override
            protected SymbolButton newObject() {
                return new SymbolButton(activeStyle);
            }
        };
    }

    /* item pridejimas. */

    public void addItem(String name, Drawable e){
        addItem(name, e, -1);
    }

    public void addItem(String name, Drawable e, Object userData){
        addItem(name, e, -1, userData);
    }

    public void addItem(String name, Drawable e, int index){
        addItem(name, e, index, null);
    }

    public void addItem(String name, Drawable e, int index, Object userData){
        if (name == null)
            name = "null";
        if (e == null){
            return; // ka cia bepadarys.
        }
        activeStyle.text = name;
        activeStyle.symbol = e;
//        SymbolButton b = activeStyle.createInterface();
        SymbolButton b = symbolButtonPool.obtain();
        b.readStyle(activeStyle); // perpanaudotam reset.
        ButtonClick c = new ButtonClick(b, normal, choose);
        c.userData = userData;
        b.setClickListener(c);
        getHost().addConnection(c);
        b.setSize(getItemWidth(), getItemHeight());
        getHost().addControl(b, index); // index ten patikrins ir sutvarkys pagal save.
//        updateControlsLocation();
        update = true;
    }

    public void addItem(String name){
        addItem(name, -1);
    }

    public void addItem(String name, Object userData){
        addItem(name, -1, userData);
    }

    public void addItem(String name, int index) { // button pridejimas.
        addItem(name, index, null);
    }

    public void addItem(String name, int index, Object userData){ // button pridejimas.
        if (name == null)
            name = "null";
        activeStyle.text = name;
//        Button b = new Button(activeStyle);
        Button b = buttonPool.obtain();
        b.readStyle(activeStyle);
        ButtonClick c = new ButtonClick(b, normal, choose);
        c.userData = userData;
        b.setClickListener(c);
        getHost().addConnection(c);
        b.setSize(getItemWidth(), getItemHeight());
        getHost().addControl(b, index);
//        updateControlsLocation();
        update = true;
    }

    /* para keitimas */

    public void setMultiSelectable(boolean selectable){
        multiSelect = selectable;
    }

    public void setActiveColors(int normalColor, int choosenColor){
        normal = normalColor;
        choose = choosenColor;
    }

    public int getNormalStateColor(){
        return normal;
    }

    public int getChooseStateColor(){
        return choose;
    }

//    public void enablePositionChange(boolean enable){
//        enablePositionChange = enable;
//    }
//
//    public boolean isPositionChangeEnabled(){
//        return enablePositionChange;
//    }

    public boolean isMultiSelectable(){
        return multiSelect;
    }

    /** manipulate this before adding items. */
    public SymbolButton.SymbolButtonStyle getActiveStyle() {
        return activeStyle;
    }

    public void setActiveItemClickListener(ActiveItemClickListener e){
        listener = e;
    }

    public ActiveItemClickListener getListener() {
        return listener;
    }

    /* Active Item */

    /** removes all active items. */
    public void clear(){ // removes all active items.
        for (int a = getHost().getControls().size() - 1; a >= 0; a--){
            Interface e = getHost().getControls().get(a);
            if (e instanceof Button){
                if (((Button) e).getClickListener() instanceof ButtonClick){
                    // pirma connection nutraukiam
//                    getHost().removeConnection((Connected) ((Button) e).getClickListener());
                    // isims connection terp remove metodo.
                    removeControl(e); // tada remove, nes remove isima listeneri.
                }
            }
        }
    }

    @Override
    public void removeControl(Interface e) {
        super.removeControl(e);
        // active item grazinimsa i pool
        if (e instanceof Button && ((Button) e).getClickListener() instanceof ButtonClick){
            // active item. grazinam i pool.
            // nepamirstam pasalint connection.
            ButtonClick buttonClick = (ButtonClick) ((Button) e).getClickListener();
            getHost().removeConnection(buttonClick);
            if (e instanceof SymbolButton){
                SymbolButton but = (SymbolButton) e;
                but.setClickListener(null);
                symbolButtonPool.free(but);
            }else {
                Button but = (Button) e;
                but.setClickListener(null);
                buttonPool.free(but);
            }
        }
    }

    /** @return selected item index or first selected item index. NOTE: only active items from {@link PreparedListView} have indexes. return -1 if nothing is selected.*/
    public int getSelectedItemIndex(){
        int index = 0;
        for (Connected e : getHost().getConnections()){
            if (e instanceof ButtonClick){
                if (((ButtonClick) e).own.getNormalColor() == ((ButtonClick) e).chosenState){
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    private ButtonClick getSelectedItemClickListener(){
        for (Connected e : getHost().getConnections()){
            if (e instanceof ButtonClick){
                if (((ButtonClick) e).own.getNormalColor() == ((ButtonClick) e).chosenState){
                    return ((ButtonClick) e);
                }
            }
        }
        return null;
    }

    /** @return selected item. if no item is selected then null is returned. if multi select is on then first selected item is returned. */
    public Button getSelectedItem(){
        ButtonClick e = getSelectedItemClickListener();
        if (e != null){
            return e.own;
        }
        return null;
    }

    /** @return user data which was set when adding item to list. If data was not set then null. If no item selected then null.*/
    public Object getSelectedItemUserData(){
        ButtonClick buttonClick = getSelectedItemClickListener();
        return buttonClick == null ? null : buttonClick.userData;
    }

    /** NOTE: new array is created.
     * @return all selected items. if no items are selected then zero length array is returned. */
    public Array<Button> getSelectedItems(){
//        Array<Button> array = new Array<>();
        return getSelectedItems(new Array<Button>());
//        for (Connected e : getHost().getConnections()){
//            if (e instanceof ButtonClick){
//                if (((ButtonClick) e).own.getNormalColor() == ((ButtonClick) e).chosenState){
//                    array.add(((ButtonClick) e).own);
//                }
//            }
//        }
//        return array;
    }

    /** NOTE: Given array will be cleared. If array is null then new array will be created.
     * @return all selected items. if no items are selected then zero length array is returned. */
    public Array<Button> getSelectedItems(Array<Button> array){
        if (array == null){
            return getSelectedItems();
        }
        array.clear();
        for (Connected e : getHost().getConnections()){
            if (e instanceof ButtonClick){
                if (((ButtonClick) e).own.getNormalColor() == ((ButtonClick) e).chosenState){
                    array.add(((ButtonClick) e).own);
                }
            }
        }
        return array;
    }

    /** All selected items user data. If data was not set then null.
     * NOTE: new array will be created. */
    public Array<Object> getSelectedItemsUserData(){
        return getSelectedItemsUserData(new Array<>());
    }

    /** All selected items user data. If data was not set then null.
     * If array null then new array will be created. */
    public Array<Object> getSelectedItemsUserData(Array<Object> array){
        if (array == null){
            return getSelectedItemsUserData();
        }
        array.clear();
        for (Connected e : getHost().getConnections()){
            if (e instanceof ButtonClick){
                if (((ButtonClick) e).own.getNormalColor() == ((ButtonClick) e).chosenState){
                    array.add(((ButtonClick) e).userData);
                }
            }
        }
        return array;
    }

    /** deselects all items. */
    public void deselectItems(){
        getHost().informGroup(-20171207, 0);
    }

    public interface ActiveItemClickListener{
        void onActiveItemClick(Interface activeItem, int index, Object userData);
    }

    private class ButtonClick implements ClickListener, Connected{
        private int normalState, chosenState;
        private Button own;

        private Object userData;

        public ButtonClick(Button owner, int normalState, int chosenState){
            own = owner;
            this.normalState = normalState;
            this.chosenState = chosenState;
        }

        @Override
        public void onClick() {
//            if (listener != null) {
            int count = 0;
            for (Interface e : getHost().getControls()) {
                if (own == e){
                    int state = chosenState;
                    if (multiSelect) {
                        state = own.getNormalColor() == chosenState ? normalState : chosenState;
                    }else {
                        PreparedListView.this.getHost().inform(this, 0);
                    }
                    own.setColors(own.getPressedColor(), state, own.getOverColor());
                    if (listener != null)
                        listener.onActiveItemClick(own, count-1, userData); // ignoruojam pirmaji.
                    break;
                }
                count++;
            }
//            }
        }

        @Override
        public void inform(int reason) {
            own.setColors(own.getPressedColor(), normalState, own.getOverColor());
        }

        @Override
        public int getGroup() {
            return -20171207;
        }
    }

    /* Custom controller */

//    private class CustomScrollField extends ScrollField{
//
//        public CustomScrollField(float x, float y, float width, float height) {
//            super(x, y, width, height);
//        }
//
//        @Override
//        protected void pan(Interface c, float x, float y, float deltaX, float deltaY) { // tik sito reiks.
//            super.pan(c, x, y, deltaX, deltaY);
//        }
//
//        @Override
//        public void removeControl(Interface e) {
//            super.removeControl(e);
//            if (e instanceof Button){
//                if (((Button) e).getClickListener() instanceof ButtonClick){
//                    removeConnection((Connected) ((Button) e).getClickListener());
//                }
//            }
//        }
//    }

    /* style */

    @Override
    public PreparedListViewStyle getStyle() {
        PreparedListViewStyle st = new PreparedListViewStyle();
        copyStyle(st);
        return st;
    }

    public void copyStyle(PreparedListViewStyle st){
        super.copyStyle(st);
        st.multiSelect = multiSelect;
//        st.activeItemsStyle =
//        st.enablePositionChange = enablePositionChange;
        st.normalState = normal;
        st.choosenState = choose;
    }

    public void readStyle(PreparedListViewStyle st){
        super.readStyle(st);
        multiSelect = st.multiSelect;
        activeStyle = st.activeItemsStyle;
//        enablePositionChange(st.enablePositionChange);
        setActiveColors(st.normalState, st.choosenState);
    }

    public static class PreparedListViewStyle extends ListViewStyle{
//        public boolean enablePositionChange = true;
        /** this style will be used to create active items. */
        public final SymbolButton.SymbolButtonStyle activeItemsStyle = new SymbolButton.SymbolButtonStyle();
        public int choosenState, normalState;
        /** allows multi select. */
        public boolean multiSelect = false;

        public PreparedListViewStyle(){
            normalState = normalColor;
            choosenState = 0xFFFF5500; // default.

//            Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
            activeItemsStyle.background = Resources.getDrawable("whiteSystemColor");
        }

        @Override
        public PreparedListView createInterface() {
            return new PreparedListView(this);
        }
    }
}
