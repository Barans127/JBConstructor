package com.engine.interfaces.controls.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.interfaces.controls.Draggable;
import com.engine.interfaces.controls.InterfacesController;
import com.engine.interfaces.controls.PanelHost;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.listeners.MainDraw;

public class Block extends Draggable {
    private Drawable background;

    private boolean duplicate; // jei true, tai draginant nekeis pozicijos, bet nesiosis aplink...
    private DropListener list;
    private SuperSelectListener select;
    private boolean originInMiddle;
    private int uniqueId; // leis nustatyt kokia kalade ir t.t.
//    private Grid master;
    private MainDraw drawer;

    private boolean superSelect, allowSelect;
    private boolean justUpdate;
    private float dragX, dragY;
    private boolean isFixed;

    public Block(){
        this(new BlockStyle());
    }

    public Block(BlockStyle style){
        super(style);
        originInMiddle = style.originInMiddle;
        background = style.background;
        allowSelect = style.allowSuperSelect;
        if (allowSelect)
            superSelect = style.isSuperSelected;
        uniqueId = style.uniqeId;
        duplicate = !style.allowDragging;
        isFixed = style.isFixedDuplicateDrawing;
        drawer = new MainDraw() {
            @Override
            public void draw() {
                int positioning;
                if (getPositioning() == Window.relativeView) {
//                    int positioning = getPositioning() == Window.relativeView ? getController().getPositioning() : getPositioning();
                    InterfacesController e = getController();
                    int pos = e.getPositioning();
                    if (pos == Window.absoluteView) {
                        positioning = pos;
                        while (e instanceof PanelHost) { // bug kai controleris kitame controleri nezino, kad yra fixed. Todel reik surast tikraji.
                            e = ((PanelHost) e).getController();
                            if (e.getPositioning() == Window.fixedView) {
                                positioning = Window.fixedView;
                                break;
                            }
                            positioning = pos;
                        }
                    }else {
                        positioning = pos;
                    }
                }else {
                    positioning = getPositioning();
                }
                int clientPositioning = isFixed ? Window.fixedView : Window.absoluteView;
                float offsetX = 0, offsetY = 0;
                if (positioning != clientPositioning){ // pozicijos susigaudymas, kad viektu ir su absolute, ir su fixed.
                    Vector3 camera = p.getAbsoluteCamera().position;
                    float x = camera.x - p.getScreenWidth()/2, y = camera.y - p.getScreenHeight()/2;
                    if (positioning != Window.absoluteView) { // fixed, controles absolute.
                        x = -x;
                        y = -y;
                    }
                    offsetX = x;
                    offsetY = y;
                }
                isvaizda(dragX + v.getOffsetX() + offsetX,dragY + v.getOffsetY() + offsetY);
            }

            @Override
            public boolean drop(int reason) {
                return false;
            }
        };
//        if (background == null){
////            p.setError("Block drawables cannot be null", ErrorType.WrongPara);
//        }
        update = true;
//        isFixed();
    }

//    @Override
//    public void setPositioning(Window.Position e) {
//        super.setPositioning(e);
//        isFixed();
//    }

//    @Override
//    public void setController(InterfacesController v) {
//        super.setController(v);
//        isFixed();
//    }

//    private void isFixed(){ // kas per ??? nesamone.
//        if (getPositioning() == Window.relativeView) {
//            isFixed = v != null && v.getPositioning() == Window.fixedView;
//        }else {
//            isFixed = getPositioning() == Window.fixedView;
//        }
//    }

    @Override
    protected void autoSize() { // jei bus texture tai reiks sita naudot.
        if (background == null)
            return;
        xSize = background.getMinWidth();
        ySize = background.getMinHeight();
//        sizeUpdated();
//        nxSize = background.getMinWidth();
//        nySize = background.getMinHeight();
        if (originInMiddle) {
            setOrigin(xSize / 2, ySize / 2);
        }
    }

    @Override
    protected void isvaizda(float x, float y) {
        p.tint(statusColor);
        drawDrawable(background, x, y, false);
//        background.draw(p.getBatch(), x, y, origin.x, origin.y, nxSize, nySize, 1, 1, getAngle());
        p.noTint();

        // superselect
        if (superSelect) {
            p.noFill();
            p.stroke(0);
            p.strokeWeight(3);
            Vector2 radiusOrigin = getRadiusOrigin();
            p.rect(x, y, xSize, ySize, radiusOrigin.x, radiusOrigin.y, getAngle());
        }
    }

//    @Override
//    public void setPosition(float x, float y) {
//        super.setPosition(x, y);
////        if (master != null) {
////            Vector2 middle = getMiddlePoint();
////            master.dragCordChange(middle, x - middle.x, y - middle.y, this);
////        }
//    }


    @Override
    protected void onDragging(float x, float y, float difX, float difY) {
        if (duplicate){
            dragX += difX;
            dragY += difY;
        } else{
            Vector2 pos = getPosition();
            setPosition(pos.x + difX, pos.y + difY);
            if (superSelect && select != null){
                select.onSuperDrag(this);
            }
        }
    }

    @Override
    protected void onDrop(float x, float y, float difX, float difY) {
        if (duplicate){
            if (list != null){
//                Vector2 pos = getPosition();
                list.onDrop(x + v.getOffsetX(), y + v.getOffsetY(), this);
            }
//            if (master != null){
//                master.releaseDrag();
//            }else {
            releaseDrag();
//            }
        }
    }

//    void update(float x, float y) {
//        justUpdate = true;
//        imitateDrag(x, y);
//        if (duplicate) {
//            TopPainter.addPaintOnTop(drawer, isFixed);
//        }
//    }

    @Override
    protected void onPress(float x, float y) {
        super.onPress(x, y);
        if (justUpdate){
            justUpdate = false;
            return;
        }
        if (duplicate) {
            Vector2 pos = getPosition();
            dragX = pos.x;
            dragY = pos.y;
            TopPainter.addPaintOnTop(drawer, isFixed);
        }
        if (allowSelect) {
            if (!superSelect) {
//                if (master != null) {
//                    if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
//                        master.multipleDrag(this, x, y);
//                    } else {
//                        master.singleSelect(this);
//                    }
//                }
                superSelect = true;
                onSuperSelect();
                if (select != null)
                    select.onSuperSelect(this);
            } else {
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    lostSuperSelect();
                }
//                else {
//                    if (master != null) {
//                        master.multipleDrag(this, x, y);
//                    }
//                }
            }
        }
    }

    @Override
    protected void onClick() {
        super.onClick();
//        if (master == null && superSelect){
        if (superSelect){
            lostSuperSelect();
        }
    }

    private void lostSuperSelect() {
        superSelect = false;
        releaseDrag();
        if (select != null)
            select.onSuperDiselect(this);
    }

    /**
     * gridui. Kai multiple buna. neatzymi.
     */
    private void releaseDrag() {
        release();
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public void setDropListener(DropListener e) {
        list = e;
    }

    public void allowSuperSelect(boolean sel) {
        allowSelect = sel;
    }

    /**
     * galimybe su situo pazymet ir iskviest listeneri.
     */
    public void performSuperSelect() {
        if (allowSelect && isEnabled()) {
//            if (single) {
//                master.singleSelect(this);
//            }
            if (!superSelect) {
                superSelect = true;
                onSuperSelect();
                if (select != null)
                    select.onSuperSelect(this);
            }
        }
    }

    public boolean isSelected() {
        return superSelect;
    }

    public void setSelectListener(SuperSelectListener e) {
        select = e;
    }

    public SuperSelectListener getSelectedListener() {
        return select;
    }

    public void setUniqueId(int id) {
        uniqueId = id;
    }

    public void setSelected(boolean e) {
        if (allowSelect && isEnabled())
            superSelect = e;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public Drawable getBackground() {
        return background;
    }

    public void setBackground(Drawable e){
        if (e != null)
            background = e;
    }

//    void setOwner(Grid e) {
//        master = e;
//    }

//    void masterCoords(float x, float y){
//        giveCords(x, y);
//    }

    protected void onSuperSelect(){}

    public interface DropListener {
        void onDrop(float x, float y, Block e);
    }

    public interface SuperSelectListener {
        void onSuperSelect(Block e);
        void onSuperDrag(Block e);
        void onSuperDiselect(Block e);
    }

    @Override
    public void setSize(float xSize, float ySize) {
        super.setSize(xSize, ySize);
        if (originInMiddle) {
            setOrigin(xSize/ 2, ySize/2);
        }
    }

    @Override
    protected void onDisable() {
        releaseDrag();
        superSelect = false;
    }

    @Override
    public void release() {
        super.release();
        if (TopPainter.containsMainDraw(drawer))
            TopPainter.removeTopPaint(drawer);
    }


    /* style */
    /**@return style from this block. */
    @Override
    public BlockStyle getStyle(){
        BlockStyle e = new BlockStyle();
        copyStyle(e);
        return e;
    }

    public void copyStyle(BlockStyle st){
        super.copyStyle(st);
        st.isFixedDuplicateDrawing = isFixed;
        st.allowDragging = duplicate;
        st.uniqeId = getUniqueId();
        st.isSuperSelected = isSelected();
        st.allowSuperSelect = allowSelect;
        st.originInMiddle = originInMiddle;
        st.background = background;
    }

    public void readStyle(BlockStyle st){
        super.readStyle(st);
        isFixed = st.isFixedDuplicateDrawing;
        setDuplicate(st.allowDragging);
        setUniqueId(st.uniqeId);
        setSelected(st.isSuperSelected);
        allowSuperSelect(st.allowSuperSelect);
        originInMiddle = st.originInMiddle;
        background = st.background;
    }

    public static class BlockStyle extends ClickableStyle{
        public Drawable background;
        public boolean originInMiddle = true;
        public boolean allowSuperSelect = false;
        public boolean isSuperSelected = false;
        public int uniqeId = 0;
        /** if true: block will change it's cords and follow finger/mouse. False: Block will stay at it's original coords but copy
         * of it will follow finger/mouse. */
        public boolean allowDragging = false;
        /** if block copy which is following mouse will be drawed at fixed or absolute positioning. */
        public boolean isFixedDuplicateDrawing = true;

        public BlockStyle(){
            rotatable = true;
        }

        @Override
        public Block createInterface() {
            return new Block(this);
        }
    }
}
