package com.engine.ui.controls.views;

import com.badlogic.gdx.Gdx;
import com.engine.core.ErrorMenu;
import com.engine.ui.controls.Field;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.ScrollField;
import com.engine.ui.controls.Window;
import com.engine.ui.controls.widgets.Button;
import com.engine.root.GdxWrapper;

import java.util.List;

public class ListView extends ScrollView {
    private float itemWidth, itemHeight;
    private float offsetX, offsetY, offsetEndX, offsetEndY;
    private boolean isVertical;
    private float separator;

    private int rowCount; // uzteks vieno.

    public ListView(){
        this(new ListViewStyle());
    }

    public ListView(ListViewStyle style){
        this(style, new ScrollField(style.x, style.y, style.width, style.height, style.virtualWidth, style.virtualHeight, style.autoSize));
    }

    public ListView(ListViewStyle st, ScrollField field){
        super(st, field);
        itemHeight = st.itemHeight;
        itemWidth = st.itemWidth;
        offsetX = st.offsetX;
        offsetY = st.offsetY;
        offsetEndX = st.offsetEndX;
        offsetEndY = st.offsetEndY;
        isVertical = st.isVertical;
        enableVerticalScroll(isVertical);
        separator = st.separatorSize;
        if (isVertical)
            rowCount = st.rowCount;
        else
            rowCount = st.columnCount;
    }

    /* gaunamieji metodai. */

    public float getItemWidth(){
        return itemWidth;
    }

    public float getItemHeight(){
        return itemHeight;
    }

    public float getOffsetX(){
        return offsetX;
    }

    public float getOffsetY(){
        return offsetY;
    }

    public float getOffsetEndX(){
        return offsetEndX;
    }

    public float getOffsetEndY(){
        return offsetEndY;
    }

    public boolean isVertical(){
        return isVertical;
    }

    /** returns row count. if isVertical true than row count is return. if false than column count is returned. */
    public int getRowCount(){
        return rowCount;
    }

    public float getSeparatorSize(){
        return separator;
    }

    /* keitimo metodai */

    public void setSeparatorSize(float size){
        if (size <= 0)
            size = 1;
        separator = size;
//        updateControlsLocation();
        update = true;
    }

    public void setItemSize(float sizeX, float sizeY){
        if (sizeX <= 0)
            sizeX = 1;
        if (sizeY <= 0){
            sizeY = 1;
        }
        itemWidth = sizeX;
        itemHeight = sizeY;
//        updateControlsSize();
        update = true;
    }

    public void setItemOffset(float offsetX, float offsetY, float offsetEndX, float offsetEndY){
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetEndX = offsetEndX;
        this.offsetEndY = offsetEndY;
        update = true;
//        updateControlsLocation();
    }

    public void setViewVertical(boolean isVertical){
        this.isVertical = isVertical;
        enableVerticalScroll(isVertical);
//        updateControlsLocation();
        update = true;
    }

    /** if is vertical than row count, if false than column count */
    public void setRowCount(int count){
        if (count > 0){
            rowCount = count;
//            updateControlsLocation();
            update = true;
        }
    }

    /* veikimo metodai */

    /** Removes all items from this list view. */
    public void removeAllItems(){
        Button e = getHost().getCloseButton(); // tik sita ignoruosim, is pop up atejes, neveikt gal be jo.
        for (int a = getHost().getControls().size() -1; a >= 0; a--){
            Control f = getHost().getControls().get(a);
            if (f != e){
                removeControl(f);
            }
        }
    }

    /** Updates list view control location and size. */
    public void update(){
        update = true;
    }

    private void updateControlsSize(){
        List<Control> list = getHost().getControls();
        for (Control e : list){
            if (e instanceof Field){
                ((Field) e).setSize(itemWidth, itemHeight); // keiciam visu dydi i naujaji dydi.
            }else {
                GdxWrapper.getInstance().setError("Interface without bounds found in listView. " +
                        "All listView items must have bounds.", ErrorMenu.ErrorType.ControlsError);
                return;
            }
        }
        updateControlsLocation();
    }

    private void updateControlsLocation(){
//        PanelHost host = getHost();
//        boolean old = host.isFrustumEnabled();
//        host.setEnableFrustum(false); // isjungiam, jis trugdo.
        List<Control> list = getHost().getControls();
//        int size = list.size() - 1; // reik ignoruot turnoff mygtuka....
        int size = 0;
        for (Control e : list){ // bug fix kai padarydavo didesni dydi, nes nematomos kontroles budavo iskaiciuotos. Sitaip ir close mygtuka ignorina.
            if (e.isVisible()){
                size++;
            }
        }
        int num = size / rowCount;
        int hasMore = size % rowCount;
        if (isVertical){
            float oldY; // uzteks tik y del scroll offset apskaiciavimo. x nesikeicia
            float wid = offsetX + (rowCount * itemWidth) + (rowCount - 1) * separator + offsetEndX;
            float hei = offsetY + (num * itemHeight) + (hasMore > 0 ? itemHeight : 0) +
                    (num - 1 + (hasMore > 0 ? 1 : 0)) * separator + offsetEndY;
            if (hei < getHeight()){ // jeigu matomas dydis yra didesnis uz virtualu.
                hei = getHeight();
            }
            oldY = getVirtualHeight() - getScrollOffsetY(); // scroll skirtumas pries keicciant dydi.
            setVirtualSize(wid, hei); // nustatom visa bendra dydi.
            setScrollOffset(getScrollOffsetX(), hei - oldY); // padedam ten kur stovejo, atrodo lyg nebuvo paslinkta.
            float x = offsetX, y = hei - offsetY - itemHeight; // item daiktu statymo vietos.
            int count = 0; // counter del eiluciu nustatymo.
            boolean ignoreFirst = true; // turnOff mygtuko ignoravimui.
            for (Control e : list){ // turn off mygtukas reik ignoruot....
                if (ignoreFirst){
                    ignoreFirst = false;
                    continue;
                }
                if (e instanceof Field){
                    if (!e.isVisible()){ // ignoruojam nematomus.
                        continue;
                    }
                    e.setPosition(x, y);
                    x += itemWidth + separator;
                    count++;
                    if (count >= rowCount){
                        x = offsetX;
                        y -= itemHeight + separator;
                        count = 0;
                    }
                }else {
                    GdxWrapper.getInstance().setError("Interface without bounds found in listView",
                            ErrorMenu.ErrorType.ControlsError);
                    return;
                }
            }
        }else {
//            float oldX, oldY; // kad net nereikia.
//            oldX = getScrollOffsetX();
//            oldY = getScrollOffsetY();
            float hei = offsetY + (rowCount * itemHeight) + (rowCount - 1) * separator + offsetEndY;
            float wid = offsetX + (num * itemWidth) + (hasMore > 0 ? itemWidth : 0) +
                    (num - 1 + (hasMore > 0 ? 1 : 0)) * separator + offsetEndX;
//            float oldX = getVirtualWidth() - getScrollOffsetX(); // scroll skirtumas pries keicciant dydi.
            setVirtualSize(wid, hei); // nustatom visa bendra dydi.
//            setScrollOffset(wid - oldX, getOffsetY()); // padedam ten kur stovejo, atrodo lyg nebuvo paslinkta.
            float x = offsetX, y = hei - offsetY - itemHeight;
            int count = 0;
            boolean ignoreFirst = true;
            for (Control e : list){
                if (ignoreFirst){
                    ignoreFirst = false;
                    continue;
                }
                if (e instanceof Field){
                    if (!e.isVisible()){ // ignoruojam nematomus.
                        continue;
                    }
                    e.setPosition(x, y);
                    y -= itemHeight + separator;
                    count++;
                    if (count >= rowCount){
                        x += itemWidth + separator;
                        y = hei - offsetY - itemHeight;
                        count = 0;
                    }
                }else {
                    GdxWrapper.getInstance().setError("Interface without bounds found in listView",
                            ErrorMenu.ErrorType.ControlsError);
                    return;
                }
            }
        }

//        host.setEnableFrustum(old);
    }

    /* override methods */

    @Override
    protected void sizeUpdated() {
        updateControlsSize();
    }

    @Override
    public void addControl(Control e) {
        if (e instanceof Field) {
            if (e.getPositioning() != Window.relativeView) // tik relative
                e.setPositioning(Window.Position.relative);
            ((Field) e).setSize(itemWidth, itemHeight); // daikto dydzio butinai.
            super.addControl(e);
//            updateControlsLocation();
            update = true;
        }else {
//            System.out.println("Only field can be added as interface doesn't have bounds");
            Gdx.app.log("ListView", "Interface instance must be Field instance (Field have size which list view uses).");
        }
    }

    @Override
    public void removeControl(Control e) {
        super.removeControl(e);
//        updateControlsLocation();
        update = true;
    }

    /* style */

    @Override
    public ListViewStyle getStyle() {
        ListViewStyle st = new ListViewStyle();
        copyStyle(st);
        return st;
    }

    public void copyStyle(ListViewStyle st){
        super.copyStyle(st);
        st.itemWidth = getItemWidth();
        st.itemHeight = getItemHeight();
        st.offsetX = getOffsetX();
        st.offsetY = getOffsetY();
        st.offsetEndX = getOffsetEndX();
        st.offsetEndY = getOffsetEndY();
        st.isVertical = isVertical();
        st.rowCount = st.columnCount = getRowCount(); // abudu vienodi, nes tik viena naudos..
        st.separatorSize = getSeparatorSize();
    }

    public void readStyle(ListViewStyle st){
        super.readStyle(st);
        setItemSize(st.itemWidth, st.itemHeight);
        setItemOffset(st.offsetX, st.offsetY, st.offsetEndX, st.offsetEndY);
        setViewVertical(st.isVertical);
        setRowCount(st.isVertical ? st.rowCount : st.columnCount);
        setSeparatorSize(st.separatorSize);
    }

    public static class ListViewStyle extends ScrollViewStyle{
        public float itemWidth = width / 4;
        public float itemHeight = height / 4;
        public float offsetX = 0, offsetEndX = 0;
        public float offsetY = 0, offsetEndY = 0;
        public boolean isVertical = true;
        /** if vertical true than this is used */
        public int rowCount = 2;
        /** if vertical false than this is used */
        public int columnCount = 2;
        public float separatorSize = itemWidth / 4;

        @Override
        public ListView createInterface() {
            return new ListView(this);
        }
    }
}
