package com.engine.ui.controls;

import com.badlogic.gdx.math.Vector2;
import com.engine.core.MoreUtils;

/**
 * Enables scrolling with interfaces in it.
 */
public class ScrollField extends PanelHost {
    private float offsetX, offsetY;
    private float visibleWidth, visibleHeight;
//    private float nvisibleWidth, nvisibleHeight;
    private float dragSpeed = 0.5f;

    private float startX, startY;
    private boolean isPressed, isDragging;
    private boolean autoSize;

    /* klausytis kiek pasiscrolino sis field. */
    private ScrollOffsetListener scrollOffsetListener;

//    private float prevScrollOffset = -1;

//    private boolean cursorIn;

    /** Creates field with dynamic size which grows when new elements are added. */
    public ScrollField(float x, float y, float width, float height){
        this(x, y, width, height, width, height, true);
    }

    /** Creates Field with fixed virtual size. */
    public ScrollField(float x, float y, float visibleWidth, float visibleHeight, float virtualWidth, float virtualHeight, boolean autoSize) {
        super(x, y, virtualWidth, virtualHeight);
        enableFrustum = true; // by default lai buna true.
//        enableFrustum = false; // neveik kol kas.
//         kad update pasidarytu.
        prevCamY = -1;
        prevCamX = -1;
        this.autoSize = autoSize;
        if (autoSize){
            super.setSize(visibleWidth, visibleHeight); // kad butu vienodas.
        }
        this.visibleWidth = visibleWidth;
        this.visibleHeight = visibleHeight;
    }


    /* frustum metodai */

//    /** Enables scroll frustum (control outside scrollField is not handled). Frustum enabled by default.*/
//    @Override
//    public void setEnableFrustum(boolean enableFrustum) {
//        this.enableFrustum = enableFrustum;
//    }


    @Override
    protected void positioningChanged() {
        // nieko nedarom. Neidomu positioning.
    }

    @Override
    protected boolean amIVisible(Control e) {
        if (enableFrustum){
            if (e instanceof Field) {
                // naudosim prevCamx scroll offset tikrinimui.
                if (prevCamX == offsetX && prevCamY == offsetY) {
                    if (frustumUpdateId == e.frustumUpdateId) {
                        return e.frustumVisible; // viskas sutampa. Niekas nepasikeite.
                    }
                } else { // nesutapo, reiks naujint.
                    prevCamX = offsetX;
                    prevCamY = offsetY;
                    frustumUpdateId++;
                }

                // pirmo rect para.
                Vector2 sPos = getPosition();
                float left = sPos.x;
                float right = sPos.x + getWidth();
                float bottom = sPos.y;
                float top = sPos.y + getHeight();

                // antro rect para.
                Field field = (Field) e;
                if (field.update){
                    // mums reik updato!
                    // darom update cia.
                    field.auto();
                    if (field.getPositioning() == Window.relativeView)
                        onAdd(field); // pasikeitus dydziui, pranes pop up..

                    field.update = false;
                }
                Vector2 pos = field.getMiddlePoint();
                float width;
                float height;
                if (field.isRotatable && field.getAngle() != 0){
//                    width = field.getWidth(); // pakreiptas, tai imam full dydi.
//                    height = field.getHeight();
                    // pakreiptam imam kubo dydi.
                    float max = Math.max(field.getWidth(), field.getHeight());
                    width = max/2;
                    height = max/2;
                }else { // angle 0. Imam normaliai
                    width = field.getWidth() / 2;
                    height = field.getHeight() / 2;
                }
                // naudosim 2x didesni rect nei yra, del rotation pakitimu.
                float rLeft = pos.x - width - offsetX;
                float rRight = pos.x + width - offsetX;
                float rBottom = pos.y - height - offsetY;
                float rTop = pos.y + height - offsetY;

                // naudojam Cartesian coordinates tikrinant ar sis rect patenka i zona.
                e.frustumVisible = left <= rRight && right >= rLeft && top >= rBottom && bottom <= rTop;
                e.frustumUpdateId = frustumUpdateId;
                return e.frustumVisible;
            }else {
                return true; // neturi ribu, nezinom nei kur nei kaip.
            }
        }else {
            return true;
        }
    }

    /* veikimas */

    private boolean isInField(float x, float y){
        Vector2 c = getPosition();
//        Vector3 coords; // kordus vercia kitas kontroleris.....
//        if (getPositioning() == Window.absoluteView) {
//            coords = p.screenToWorldCoords(x, y);
//        } else {
//            coords = p.screenToFixedCoords(x, y);
//        }
        return x >= c.x && x <= c.x + getWidth() && y >= c.y && y <= c.y + getHeight();
    }

    /* override */

    // offset naudoja interface, kad zinotu kur yra.
    @Override
    public float getOffsetX() {
        return controlX - offsetX;
    }

    @Override
    public float getOffsetY() {
        return controlY - offsetY;
    }

    @Override
    boolean interfaceOffsetX(float offsetX) {
        if (autoSize){
            if (offsetX >= 0)
                return false;
            super.setSize(getVirtualWidth() + MoreUtils.abs(offsetX), getVirtualHeight());
            return true;
        }
        return false;
    }

    @Override
    boolean interfaceOffsetY(float offsetY) {
        if (autoSize){
            if (offsetY >= 0)
                return false;
            super.setSize(getVirtualWidth(), getVirtualHeight() + MoreUtils.abs(offsetY));
            return true;
        }
        return false;
    }

    public void setVirtualSize(float virtualWidth, float virtualHeight){
        if (virtualWidth < visibleWidth){
            virtualWidth = visibleWidth;
        }
        if (virtualHeight < visibleHeight){
            virtualHeight = visibleHeight;
        }
        super.setSize(virtualWidth, virtualHeight);
        autoSize = false;

        // kad nebutu offset bug, tai patikrinam offset.
        setScrollOffset(offsetX, offsetY);
    }

    public void setScrollOffset(float offsetX, float offsetY){
        if (this.offsetX == offsetX && this.offsetY == offsetY){
            // offset tokie patys, tai kam cia keist?
            return;
        }
        float width = getVirtualWidth() - getWidth();
        float height = getVirtualHeight() - getHeight();
        if (offsetX < 0)
            offsetX = 0;
        else if (offsetX > width)
            offsetX = width;
        if (offsetY < 0)
            offsetY = 0;
        else if (offsetY > height) {
            offsetY = height;
        }
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        if (scrollOffsetListener != null){
            scrollOffsetListener.onOffsetChange(this.offsetX, this.offsetY);
        }
    }

    /** Set scroll offset listener to listen when field is moved. */
    public void setScrollOffsetListener(ScrollOffsetListener listener){
        this.scrollOffsetListener = listener;
    }

    @Override
    protected void popBackground() {
        Vector2 pos = getPosition();
        if (hasBackground()){
            drawBackground(pos.x + controlX, pos.y + controlY, visibleWidth, visibleHeight);
        }
        disabledView(pos.y + controlX, pos.y + controlY, visibleWidth, visibleHeight);
    }

    @Override
    protected void drawInterfaces(boolean isAbsoluteDraw) {
        Vector2 pos = getPosition();
        if (p.pushScissor(pos.x + controlX, pos.y + controlY, visibleWidth, visibleHeight)) {
            super.drawInterfaces(isAbsoluteDraw);
            p.popScissor();
        }
    }

    @Override
    public float getWidth() {
        return visibleWidth;
    }

    @Override
    public float getHeight() {
        return visibleHeight;
    }

    public float getDragSpeed(){
        return dragSpeed;
    }

    public float getVirtualWidth(){
        return super.getWidth();
    }

    public float getVirtualHeight(){
        return super.getHeight();
    }

    public void enableAutoSizing(boolean enable){
        autoSize = enable;
    }

    public float getScrollOffsetX(){
        return offsetX;
    }

    public float getScrollOffsetY(){
        return offsetY;
    }

    /** Scroll offset listener. Null if not set. */
    public ScrollOffsetListener getScrollOffsetListener(){
        return scrollOffsetListener;
    }

    @Override
    public void setSize(float width, float height) {
        visibleWidth = width;
        visibleHeight = height;
        float vw = getVirtualWidth(), vh = getVirtualHeight(); // virtualus dydis neturi but mazesnis uz matoma.
        boolean change = false;
        if (vw < visibleWidth){
            vw = visibleWidth;
            change = true;
        }
        if (vh < visibleHeight){
            vh = visibleHeight;
            change = true;
        }
        if (change)
            super.setSize(vw, vh);
        float dydis = getVirtualWidth() - getWidth(); // reik offseta perskaiciuot butinai.
        float plotis = getVirtualHeight() - getHeight();
        // normalizuojam offset, bet listener nekvieciam.
        if (offsetX < 0)
            offsetX = 0;
        else if (offsetX > dydis)
            offsetX = dydis;
        if (offsetY < 0)
            offsetY = 0;
        else if (offsetY > plotis) {
            offsetY = plotis;
        }

//        setRezolution();
    }

//    @Override
//    protected void setRezolution() {
//        super.setRezolution();
//        nvisibleHeight = Engine.getWithRez(visibleHeight);
//        nvisibleWidth = Engine.getWithRez(visibleWidth);
//    }

    public void setDragSpeed(float speed){
        if (speed > 0){
            dragSpeed = speed;
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (isActive()){
            if (isInField(x, y)){
                super.touchDown(x + offsetX, y + offsetY, pointer, button);
                if (pointer <= 0){
                    // cia pasizymet, kad ant scroll view.
                    isPressed = true; // paspausta ant butent scroll view
                    startX = x;
                    startY = y;
                }
                return true;
            }
        }
        return false;
//        return isEnabled() && isInField(x, y) && (super.touchDown(x - offsetX, y - offsetY, pointer, button) || pointer <= 0);
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (isPressed){
            isPressed = false;
            isDragging = false;
//            return true;
        }
        return super.tap(x + offsetX, y + offsetY, count, button);
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (isPressed){
            isPressed = false;
            isDragging = false;
//            return true;
        }
        return super.panStop(x + offsetX, y + offsetY, pointer, button);
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (isActive() && isPressed){
            if (super.pan(x + offsetX, y + offsetY, deltaX, deltaY)){
//                isPressed = false;
                return true;
            }else {
                if (!isDragging){
                    isDragging = true;
                    super.release();
                }
                offsetX -= (x - startX) * dragSpeed;
                offsetY -= (y - startY) * dragSpeed; // apverstas y. y atvertimas tik super metode.
                startX = x;
                startY = y;
                float width = getVirtualWidth() - getWidth();
                float height = getVirtualHeight() - getHeight();
                if (offsetX < 0)
                    offsetX = 0;
                else if (offsetX > width)
                    offsetX = width;
                if (offsetY < 0)
                    offsetY = 0;
                else if (offsetY > height) {
                    offsetY = height;
                }

                // dar listener reik iskviest
                if (scrollOffsetListener != null){
                    scrollOffsetListener.onOffsetChange(offsetX, offsetY);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        if (isPressed){
            isPressed = false;
            isDragging = false;
//            return true;
        }
        initialPointer1.x += offsetX;
        initialPointer1.y += offsetY;
        initialPointer2.x += offsetX;
        initialPointer2.y += offsetY;
        pointer1.x += offsetX;
        pointer1.y += offsetY;
        pointer2.x += offsetX;
        pointer2.y += offsetY;
        return super.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
    }

    @Override
    public boolean longPress(float x, float y) {
        if (super.longPress(x + offsetX, y + offsetY)){
            isPressed = false;
            isDragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseMoved(float x, float y) {
        if (!isInField(x, y)){
            release();
            return false;
        }
        super.mouseMoved(x + offsetX, y + offsetY);
        return true; // nes fielde yra pelyte.
//        if (isInField(x, y)){
//            cursorIn = true;
//            return super.mouseMoved(x - offsetX, y - offsetY);
//        }else {
//            if (cursorIn) {
//                super.release(); // why release?
//                cursorIn = false;
//            }
//            return false;
//        }
    }

    @Override
    public void release() {
        isPressed = false;
        isDragging = false;
        super.release();
    }

    public interface ScrollOffsetListener{
        /** Called when scroll field offset is changed.
         * @param offsetX current offset x
         * @param offsetY current offset y */
        void onOffsetChange(float offsetX, float offsetY);
    }
}
