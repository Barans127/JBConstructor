package com.engine.ui.controls.views;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.core.Resources;
import com.engine.ui.controls.PopUp;
import com.engine.ui.controls.ScrollField;
import com.engine.ui.controls.Window;
import com.engine.ui.controls.widgets.ScrollBar;

/**
 * Same as Panel but enables Scrolling.
 */
public class ScrollView extends Panel {
    private ScrollField view;
    private boolean enableScroll, verticalScroll;
    private float scrollSpeed;

    private ScrollBar scrollBar; // bar to indicate scroll.
    private boolean xAxisScroll; // del scroll bar.

    public ScrollView(){
        this(new ScrollViewStyle());
    }

    public ScrollView(ScrollViewStyle style){
        this(style, new ScrollField(style.x, style.y, style.width, style.height, style.virtualWidth, style.virtualHeight, style.autoSize));
    }

    public ScrollView(ScrollViewStyle style, ScrollField field){
        super(style, field);
        view = (ScrollField) host;
        view.setDragSpeed(style.dragSpeed);
        scrollSpeed = style.scrollSpeed;
        verticalScroll = style.verticalScroll;
    }

    /* getters setters. */

    /** Disables autoSizing.*/
    public void setVirtualSize(float x, float y){
        view.setVirtualSize(x, y);
        updateScrollBarValue(); // atnaujinam scroll bar.
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        updateScrollBarValue(); // atnaujinam scroll bar.
    }

    /** Enable or disable autosizing.
     * NOTE: Currently scroll view autosizing doesn't work. Use manual size and virtual size.*/
    public void enableAutoSizing(boolean enable){
        view.enableAutoSizing(enable);
    }

    /** How fast should this view be dragged over. */
    public void setDragSpeed(float speed){
        view.setDragSpeed(speed);
    }

    /** Set scroll offset. This will move scroll view. */
    public void setScrollOffset(float offsetX, float offsetY){
        view.setScrollOffset(offsetX, offsetY);
    }

    /** How fast should this view move with mouse scroll wheel. */
    public void setScrollSpeed(float speed){
        scrollSpeed = speed;
    }

    /** should mouse scroll view move x offset or y offset.
     * true - x moves, false y moves. */
    public void enableVerticalScroll(boolean enableScroll){
        verticalScroll = enableScroll;
    }

    /** Set scroll offset listener to listen when field is moved. */
    public void setScrollOffsetListener(ScrollField.ScrollOffsetListener listener){
        view.setScrollOffsetListener(listener);
    }

    /** Scroll offset listener. If not set null. */
    public ScrollField.ScrollOffsetListener getScrollOffsetListener(){
        return view.getScrollOffsetListener();
    }

    /** Is this view moved by mouse scroll wheel in x direction or y.
     * true - x, false y directions. */
    public boolean isVerticalScroll(){
        return verticalScroll;
    }

    /** Virtual width */
    public float getVirtualWidth(){
        return view.getVirtualWidth();
    }

    /** virtual height */
    public float getVirtualHeight(){
        return view.getVirtualHeight();
    }

    /** how far view is moved by x. */
    public float getScrollOffsetX(){
        return view.getScrollOffsetX();
    }

    /** how far view is moved by y. */
    public float getScrollOffsetY(){
        return view.getScrollOffsetY();
    }

    /** How fast mouse scroll wheel is moving this view. */
    public float getScrollSpeed(){
        return scrollSpeed;
    }

    /** how fast this view move when dragged. */
    public float getDragSpeed(){
        return view.getDragSpeed();
    }

    /* scroll bar idejimas */

    /** Creates and inserts scroll indicator with default parameters into scroll view interface controller (not scroll view itself).
     * If scroll bar was already set and not removed then old scroll view will be returned and new will not be created.
     * NOTE: position will never be recalculated so use this method after inserting scroll view to controller or else manually change position.
     * @param offsetX and offsetY is offset which is added to x and y location.
     * @param widthRatio this is ratio of scroll view width or height. Use value of 1 to get full width or height.
     * @param height of this scroll bar. Usually it is about 10.
     * @param position - position of this scroll bar. 0 - down/right, 1 - up/left (depends on xAxis).
     * @param tintBackground background color. ARGB
     * @param barColor bar color. This color is set for all states (normal, over, pressed). ARGB
     * @param clickable Will it be only for indication or will user be able to click and drag it.
     * @param xAxis Will it indicate x axis or y axis.
     * @return created and inserted scroll bar.*/
    public ScrollBar insertScrollBarIndicator(float offsetX, float offsetY, float widthRatio, float height, int position,  int tintBackground, int barColor, boolean clickable, boolean xAxis){
        auto(); // del visa ko.
        ScrollBar.ScrollBarStyle bst = new ScrollBar.ScrollBarStyle();
        Drawable white = Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor"));
        bst.autoSize = false;
        float size = xAxis ? getWidth() : getHeight();
        bst.width = size * widthRatio;

        // papildomo offset apskaiciavimas, kad centruotu per viduri.
        float additionOffset = 0;
        if (bst.width != size){
            additionOffset = size/2 - bst.width/2;
        }
//        bst.x = 5;
//        bst.y = 5;
        bst.height = height;
        bst.background = white;
        bst.tintBackground = tintBackground;
        bst.scrollBar = white;
        // permatomas bar.
        bst.normalColor = bst.onColor = bst.pressedColor = barColor;
        bst.fixedStop = false;
//        bst.barSize = 100;

        // pozicijos nustatymas.
        Vector2 pos = getPosition();
        float x = pos.x, y = pos.y;
        float cx = 0, cy = 0;
        if (getPositioning() == Window.relativeView){
            // nuo pop up tik kompensuot reik pradet.
            if (v != null && v instanceof PopUp){
                Vector2 vPos = ((PopUp) v).getPosition();
                cx = vPos.x;
                cy = vPos.y;
            }
        }
        if (xAxis){
            x -= cx + additionOffset; // x visada toks pat.
            if (position == 0){
                // down.
                y -= cy;
            }else {
                // up
                y = pos.y + getHeight() - bst.height - cy;
            }

        }else {
            y -= cy + additionOffset;
            if (position == 0){
                // right
                x = pos.x + getWidth() - bst.height - cx;
            }else {
                // left.
                x -= cx;
            }
        }

        x += offsetX;
        y += offsetY;

        bst.x = x;
        bst.y = y;


        return insertScrollBarIndicator(bst, clickable, xAxis);
    }

    /** Creates and inserts scroll indicator into scroll view interface controller (not scroll view itself).
     * If scroll bar was already set and not removed then old scroll view will be returned and new will not be created.
     * @param style of scroll bar. (you must specify location yourself).
     * @param clickable Will it be only for indication of will user be able to click and drag it.
     * @param xAxis Will it indicate x axis or y axis.
     * @return created and inserted scroll bar.*/
    public ScrollBar insertScrollBarIndicator(ScrollBar.ScrollBarStyle style, boolean clickable, boolean xAxis){
        if (scrollBar == null) {
            style.horizontal = xAxis; // dar kart perstatom.
            ScrollBar scrollBar = new ScrollBar(style); // sukuriam scroll bar.
            scrollBar.setUnClickable(!clickable); // padarom clickable arba ne.
            // idedam ta magic listener, kuris ta magic atliek.
            setScrollOffsetListener(new ScrollField.ScrollOffsetListener() {
                @Override
                public void onOffsetChange(float offsetX, float offsetY) {
                    if (xAxisScroll) {
                        ScrollView.this.scrollBar.setValue((int) offsetX);
                    } else {
                        ScrollView.this.scrollBar.setValue((int) offsetY);
                    }
                }
            });
            if (clickable){ // jeigu spaudziama tai turim leist scrolla kai user drags sita.
                scrollBar.setScrollListener(new ScrollBar.ScrollListener() {
                    @Override
                    public void onScroll(int currentValue) {
                        setScrollOffset(xAxisScroll ? currentValue : getScrollOffsetX(), xAxisScroll ? getScrollOffsetY() : currentValue);
                    }
                });
            }
            // jei yra kontroleris tai idedam kontrole.
            if (v != null) {
                v.addControl(scrollBar);
            }
            // nustatom viska
            this.scrollBar = scrollBar;
            this.xAxisScroll = xAxis;
            // atnaujinam jump values.
            updateScrollBarValue();
            return scrollBar; // grazinam sukurta scroll bar.
        }else {
            return scrollBar;
        }
    }

    /** scroll bar which indicates scroll. to set scroll bar use {@link #insertScrollBarIndicator(ScrollBar.ScrollBarStyle, boolean, boolean)} method.
     * If scroll bar was not set then null.*/
    public ScrollBar getScrollBar(){
        return scrollBar;
    }

    /** If scroll bar is set then which axis it is indicating x or y. */
    public boolean isScrollBarScrollingOnXAxis(){
        return xAxisScroll;
    }

    /** If scroll bar was set it can be removed with this. */
    public void removeScrollBarIndicator(){
        if (scrollBar != null){
            if (v != null){
                v.removeControl(scrollBar);
            }
            setScrollOffsetListener(null); // nuimam listener.
            scrollBar = null;
        }
    }

    private void updateScrollBarValue(){
        float virtual, real;
        if (scrollBar != null){
            float size;
            if (xAxisScroll){
                virtual = getVirtualWidth();
                real = getWidth();
                size = virtual - real;
            }else {
                virtual = getVirtualHeight();
                real = getHeight();
                size = virtual - real;
            }
            // paslepiam arba vel rodom jei reik.
            scrollBar.setVisible(size > 0);
            if (size > 0){
                float barLen = scrollBar.getWidthSize();
                scrollBar.setBarWidth(real/virtual * barLen);
                scrollBar.setJumpsCount((int) size);
                scrollBar.setValue((int) (xAxisScroll ? getScrollOffsetX() : getScrollOffsetY()));
            }
        }
    }

    /* override */

    @Override
    public boolean mouseMoved(float x, float y) {
        if (super.mouseMoved(x, y)){
            enableScroll = true;
            return true;
        }
        enableScroll = false;
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (super.scrolled(amountX, amountY)){
            return true;
        }
        // TODO Currently only vertical scroll amount is checked.
        // This is because libGDX changed to amountX and amountY, not sure yet how it works.
        // Previously there was only one value. No time to fix this, sorry.
        if (enableScroll){
            if (verticalScroll)
                setScrollOffset(getScrollOffsetX(), getScrollOffsetY()-amountY*scrollSpeed);
            else
                setScrollOffset(getScrollOffsetX()+amountY*scrollSpeed, getScrollOffsetY());
            return true;
        }
        return false;
    }

    /* style */

    @Override
    public ScrollViewStyle getStyle() {
        ScrollViewStyle st = new ScrollViewStyle();
        copyStyle(st);
        return st;
    }

    public void copyStyle(ScrollViewStyle st){
        super.copyStyle(st);
        st.virtualWidth = getVirtualWidth();
        st.virtualHeight = getVirtualHeight();
        st.dragSpeed = getDragSpeed();
        st.scrollSpeed = getScrollSpeed();
        st.verticalScroll = isVerticalScroll();
    }

    public void readStyle(ScrollViewStyle st){
        super.readStyle(st);
        setVirtualSize(st.virtualWidth, st.virtualHeight);
        setDragSpeed(st.dragSpeed);
        setScrollSpeed(st.scrollSpeed);
        enableVerticalScroll(st.verticalScroll);
    }

    public static class ScrollViewStyle extends PanelStyle{
        /** Allows ScrollView virtual size to grow when new interfaces are added to it. */
        public float virtualWidth = width;
        public float virtualHeight = height;
        public float dragSpeed = 1f;
        /** world unit size. Bigger - faster scrolling. */
        public float scrollSpeed = 15f;
        public boolean verticalScroll = true;

        @Override
        public ScrollView createInterface() {
            return new ScrollView(this);
        }
    }
}
