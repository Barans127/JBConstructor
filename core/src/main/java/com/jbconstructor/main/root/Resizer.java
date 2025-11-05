package com.jbconstructor.main.root;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.core.ErrorMenu;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Draggable;
import com.engine.interfaces.controls.Field;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.Window;
import com.jbconstructor.main.editors.MoveController;

import java.util.ArrayList;
import java.util.List;

public class Resizer extends Draggable{
    // sesi kvadraciukai resizinimui
    private Drawable boxes;
    private int activeBox = -1; // nustatys kuris box dabar aktyvus.
    private float boxSize; // boxo kvadrato dydis.
    // rotation
    private boolean rotateActive;
    private float mouseAlpha;
    private float stx, sty;
    private int rotationRound;

    // leidimai
    private boolean disableDrag, disableSizing, disableMoving;

    private ArrayList<Interface> controls;

    private ChangeListener listener;

    private MoveController undoredo;

    // atsiminimui.
    private float startRotation;

    public Resizer(MoveController e){
        super(new ResizerStyle());
        boxSize = Resources.getPropertyFloat("resizerBoxSize", 10);
        rotationRound = Resources.getPropertyInt("roundDegrees", 0);
        controls = new ArrayList<>();
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png"); // kvadraciukam piest.
        boxes = Resources.getDrawable("halfWhiteColor");
        update = true;
        setFocusable(false);
        setRotatable(true);
        undoredo = e;
    }

    public void setBoxSize(float boxSize){
        if (boxSize <= 0){
            p.setError("Resizer: boxSize cannot be <= 0.", ErrorMenu.ErrorType.ControlsError);
            return;
        }
        this.boxSize = boxSize;
    }

    /** Example 0 - 0 number after dot. 1 - one number after dot and so on.*/
    public void setRotationRound(int round){
        rotationRound = round;
    }

    /** kvies kai keiciamas dydis su boxais. Direct change - pozicijos ir dydzio nustatymai nebus perskaiciuojami
     * pagal kameros zoom dydi.*/
    private void onChange(float x, float y, float width, float height){
        float dx, dy, dwidth, dheight;
        for (Interface e : controls){
            // pozicijos susigaudymas
//            float zoom = 1f;
            int positioning = getPositioning() == Window.relativeView ? getController().getPositioning() : getPositioning();
            int clientPositioning = e.getPositioning() == Window.relativeView ? e.getController().getPositioning() : e.getPositioning();
//                float offsetX = 0, offsetY = 0;
            if (positioning != clientPositioning) { // jei pozicija skyriasi, reik imt pazet ar cameros dydis kitoks.
                float zoom = p.getAbsoluteCamera().zoom;
                dx = x * zoom;
                dy = y * zoom;
                dwidth = width * zoom;
                dheight = height*zoom;
            }else {
                dx = x;
                dy = y;
                dwidth = width;
                dheight = height;
            }
            // change
            Vector2 pos = e.getPosition();
            e.setPosition(pos.x + dx, pos.y + dy);
            if (width != 0 || height != 0) { // keiciant cord nekeis autoSize.
                float wid = 0, hei = 0;
                if (e instanceof Field) {
                    Field f = (Field) e;
//                boolean isA = f.isAuto(); // causes bug: neleidzia keist dydzio
                    f.setSize(f.getWidth() + dwidth, f.getHeight() + dheight);
                    wid = f.getWidth();
                    hei = f.getHeight();
                    f.setOriginMiddle();
//                f.setAutoSizing(isA); // kad nepamestu po setSize kvietimo.
                }
                if (listener != null){
                    listener.onSizeChange(e, pos.x, pos.y, wid, hei);
                }
            }else if (listener != null){
                listener.onPositionChange(e, pos.x, pos.y);
            }
        }
        if (listener != null){
//            Vector2 p = getPosition();
            listener.onChange(x, y, width, height, controls.size() > 1);
        }
    }

    /** visi skaiciai tiesiogiai, nieks nekeiciama, koks paduotas toki ir pakeis. */
    private void onPositionChange(float x, float y, boolean cx, boolean cy, boolean callList){
        float nx, ny;
        for (Interface e : controls){
            Vector2 pos = e.getPosition();
            if (cx) nx = x;
            else nx = pos.x;
            if (cy) ny = y;
            else ny = pos.y;
            undoredo.getFlushSettings().add(pos.x, pos.y, 0f, 0f); // reik 4 minimum.
            e.setPosition(nx, ny);
            if (callList && listener != null){
                listener.onPositionChange(e, nx, ny);
            }
        }
    }

    /** width ir height tiesioginiai. offsetai tik skirtumai. */
    private void onSizeChange(float offsetX, float offsetY, float width, float height, boolean cx, boolean cy, boolean callList){
        float nx, ny;
        for (Interface e : controls){
            float oldx, oldy, oldw = 0, oldh = 0;
            // pozicijos susigaudymui, offset perskaiciavimui.
            int positioning = getPositioning() == Window.relativeView ? getController().getPositioning() : getPositioning();
            int clientPositioning = e.getPositioning() == Window.relativeView ? e.getController().getPositioning() : e.getPositioning();
            float x = offsetX, y = offsetY;
//                float offsetX = 0, offsetY = 0;
            if (positioning != clientPositioning) { // jei pozicija skyriasi, reik imt pazet ar cameros dydis kitoks.
                float zoom = p.getAbsoluteCamera().zoom;
                x = offsetX / zoom;
                y = offsetY / zoom;
            }
            Vector2 pos = e.getPosition();
            oldx = pos.x;
            oldy = pos.y;
            e.setPosition(oldx + x, oldy + y);
            if (e instanceof Field) {
                oldw = ((Field) e).getWidth();
                oldh = ((Field) e).getHeight();
                if (cx) nx = width;
                else nx = oldw;
                if (cy) ny = height;
                else ny = oldh;
                ((Field) e).setSize(nx ,ny);
                if (callList && listener != null){
                    listener.onSizeChange(e, pos.x, pos.y, nx, ny);
                }
            }
            undoredo.getFlushSettings().add(oldx, oldy, oldw, oldh);
        }
    }

    private void onRotate(float deg, float old){
        if (hasMultipleControls()) {
            Vector2 mid = getMiddlePoint();
            for (Interface e : controls){
                float midX, midY;
                midX = mid.x;
                midY = mid.y;
                // pasigaut vidurio cord, nes fix ir absolute gali skirtis.
                int positioning = getPositioning() == Window.relativeView ? getController().getPositioning() : getPositioning();
                int clientPositioning = e.getPositioning() == Window.relativeView ? e.getController().getPositioning() : e.getPositioning();
                if (positioning != clientPositioning){ // pozicijos susigaudymas, kad viektu ir su absolute, ir su fixed.
//                    Vector3 camera = p.getAbsoluteCamera().position;
//                    float x = camera.x - p.getScreenWidth()/2, y = camera.y - p.getScreenHeight()/2;
//                    if (positioning != Window.absoluteView) { // fixed, controles absolute.
//                        x = -x;
//                        y = -y;
//                    }
//                    midX -= x;
//                    midY -= y;
                    if (positioning == Window.fixedView){ // resizer fixed, kontrole absolute.
                        Vector3 cord = p.fixedToScreendCoords(mid.x, mid.y); // resizer coord versim i absolute cord
                        cord = p.screenToWorldCoords(cord.x, Gdx.graphics.getHeight() - cord.y);
                        midX = cord.x;
                        midY = cord.y;
                    }else { // resizer absolute, kontrole fixed. nelabai tai bus...
                        Vector3 cord = p.worldToScreenCoords(mid.x, mid.y);
                        cord = p.screenToFixedCoords(cord.x, Gdx.graphics.getHeight() - cord.y);
                        midX = cord.x;
                        midY = cord.y;
                    }
                }
                Vector2 emid;
                if (e instanceof Field) emid = ((Field) e).getMiddlePoint();
                else emid = e.getPosition();
                float eRadius = e.getAngle();
                float dist = MoreUtils.dist(midX, midY, emid.x, emid.y);
                float angleDif = (MathUtils.degreesToRadians * (deg - old));
                float angle = (float) Math.atan2(emid.y - midY, emid.x - midX) + angleDif;
                float rx, ry; // vidurio cord.
                rx = (float) (Math.cos(angle) * dist + midX);
                ry = (float) (Math.sin(angle) * dist + midY);
                e.setAngle(eRadius + (deg - old));
                if (e instanceof Field){
                    ((Field) e).placeInMiddle(rx, ry);
                }else {
                    e.setPosition(rx, ry);
                }
            }
        }else
            controls.get(0).setAngle(deg); // jei ne multi, tai reisk viena tik yr ir visks.
        if (listener != null)
            listener.onRotation(old, deg);
    }

    public boolean isControlsPositioningSame(){
        int count = 0;
        int pos = -1;
        for (Interface e : controls){
            if (e.getPositioning() != pos){
                pos = e.getPositioning();
                count++;
            }
        }
        return count < 2;
    }

    public void setChangeListener(ChangeListener e){
        listener = e;
    }

    /** Adds bounds which allows change interface size and position. Disables interface autoSizing
     * if size is changed. */
    public void addEditableControl(Interface e){
        if (controls.contains(e) || e == this){ // nedes tokiu pat ir nedes saves.
            return;
        }
        controls.add(e);
        if (e instanceof Field){
            ((Field) e).setOriginMiddle();
        }
        setVisible(true);
        update = true;
    }

    /** Adds bounds which allows change interface size and position. Disables interface autoSizing
     * if size is changed. */
    public void addEditableControl(Interface... e){
        for (Interface a : e){
            addEditableControl(a);
        }
    }

    public boolean containsControl(Interface e){
        return controls.contains(e);
    }

    public void removeEditableControl(Interface e){
        controls.remove(e);
        update = true;
    }

    /** Removes all editable interfaes. */
    public void releaseAll(){
        controls.clear();
        update = true;
    }

    public boolean hasMultipleControls(){
        return controls.size() > 1;
    }

    public List<Interface> getControls(){
        return controls;
    }

    public float getBoxSize(){
        return boxSize;
    }

    public void update(){
        update = true;
    }

    /** allows change location with all interfaces in it */
    public void move(float x, float y, boolean changeX, boolean changeY, boolean callList){
        if (disableMoving || !(changeX || changeY)){ // nekeist abieju.
            return;
        }
        Vector2 pos = getPosition();
        undoredo.setInterfaces(controls);
//        undoredo.moved(0, changeX ? x - pos.x : 0, changeY ? y - pos.y : 0, 0, 0, 0, 0, null);
        undoredo.getFlushSettings().clear();
        onPositionChange(x, y, changeX, changeY, callList);
        undoredo.moved(0);
        x -= boxSize/2;
        y -= boxSize/2;
//        float difX = x - pos.x;
//        float difY = y - pos.y;
//        onChange(difX, difY, 0, 0);
        setPosition(changeX ? x : pos.x, changeY ? y : pos.y);
        if (callList && listener != null){
            listener.onChange(pos.x, pos.y, getWidth(), getHeight(), hasMultipleControls());
        }
        update = true;
    }

    /** changes all interfaces size in it */
    public void changeSize(float wid, float hei, boolean changeWid, boolean changeHei, boolean callList){
        if (disableSizing || !(changeHei || changeWid))
            return;
//        float difx, dify;
        float width, height;
        if (changeWid) width = wid;
        else width = getWidth();
        if (changeHei) height = hei;
        else height = getHeight();
//        difx = width - getWidth();
//        dify = height - getHeight();
//        Vector2 mid = getMiddlePoint();
        Vector2 pos = getPosition();
        float px, py;
        if (getAngle() != 0){ // pozicijos perstatymas pagal esama kampa.
            Vector2 rot = rotatePoint(pos.x, pos.y, getAngle());
            px = rot.x;
            py = rot.y;
        }else {
            px = pos.x;
            py = pos.y;
        }
        float point1x, point1y; // pirmasis taskas
        float angle1 = getAngle()*MathUtils.degreesToRadians;
        point1x = (float) (Math.cos(angle1) * width + px);
        point1y = (float) (Math.sin(angle1) * width + py);
        float point2x, point2y; // antrasis pagrindinis taskas
        float angle2 = (getAngle()+90) * MathUtils.degreesToRadians; // keliam i virsu
        point2x = (float) (Math.cos(angle2) * height + point1x);
        point2y = (float) (Math.sin(angle2) * height + point1y);
        float offsetX, offsetY;
        offsetX = (px + (point2x - px)/2) - pos.x;
        offsetY = (py + (point2y - py)/2) - pos.y;
        undoredo.setInterfaces(controls); // undo irasymui.
        undoredo.getFlushSettings().clear();
//        undoredo.moved(6, offsetX - width/2, offsetY - height/2, difx, dify, 0, 0, null);
        setSize(width, height);
        placeInMiddle(pos.x + offsetX, pos.y + offsetY);
        onSizeChange(offsetX - width/2, offsetY - height/2, width, height, changeWid, changeHei, callList);
        undoredo.moved(6);
        if (callList && listener != null){
            listener.onChange(pos.x, pos.y, getWidth(), getHeight(), hasMultipleControls());
        }
//        onChange(offsetX - width/2, offsetY - height/2, difx, dify);
        update = true;
    }

    /** rotates all interfaces */
    public void rotate(float degrees){
        if (disableDrag)
            return;
        while (degrees > 180)
            degrees -= 360;
        while (degrees < -180)
            degrees += 360;
        undoredo.setInterfaces(controls);
//        undoredo.moved(2, degrees - getRadius(), 0, 0, 0, 0, 0, null);
        if (hasMultipleControls()){
            Vector2 mid = getMiddlePoint();
            undoredo.moved(7, degrees - getAngle(), mid.x , mid.y);
        }else {
            undoredo.moved(2, degrees - getAngle());
        }
        onRotate(degrees, getAngle());
        setAngle(degrees);
    }

    @Override
    public void setAutoSizing(boolean auto) {} // nereikia

    @Override
    public void autoSize() {
        if (controls.size() == 0){ // nereikia tad jo
            setVisible(false);
            return;
        }
        float cx = Float.MAX_VALUE, cy = Float.MAX_VALUE, cwidth, cheight;
        float maxWidPoint = boxSize*3, maxHeiPoint = boxSize*3;
        float degree = 360; // arciau 0 ims.
        boolean drag = false, sizing = false, move = false; // draudimai
        for (Interface e : controls){
            // paziurim ar lockintas.
            if (e instanceof Element){
                if (((Element) e).isLocked()){
                    drag = true;
                    sizing = true;
                    move = true;
                }
            }

            Vector2 pos = e.getPosition();
            float eDeg = e.getAngle();
//            float xPoint, yPoint;
//            if (eDeg != 0 && e instanceof Field) { // detale paversta.
//                while (eDeg > 180) // jei kartais perdidelis kampo skaicius.
//                    eDeg -= 360;
//                while (eDeg < -180)
//                    eDeg += 360;
//                Vector2 rot;
//                if (eDeg < -90){ // -180 - -90
//                    rot = rotatePoint(pos.x + ((Field) e).getWidth(), pos.y, e.getRadius());
//                } else if (eDeg <= 0){ // -90 - 0
//                    rot = rotatePoint(pos.x, pos.y, e.getRadius());
//                } else if (eDeg <= 90){ // 0 - 90
//                    rot = rotatePoint(pos.x, pos.y + ((Field) e).getWidth(), e.getRadius());
//                }else { // 90 - 180
//                    rot = rotatePoint(pos.x + ((Field) e).getWidth(), pos.y + ((Field) e).getHeight(), e.getRadius());
//                }
//                xPoint = rot.x + offsetX;
//                yPoint = rot.y + offsetY;
//            } else { // kai nepaversta nieko ypatinga.
//                xPoint = pos.x + offsetX;
//                yPoint = pos.y + offsetY;
//            }
//            if (cx > xPoint) { // kur bus pradzia.
//                cx = xPoint - boxSize / 2;
//            }
//            if (cy > yPoint) {
//                cy = yPoint - boxSize / 2;
//            }
            float ex, ey;
//            if (p.getAbsoluteCamera().zoom != 1){ // del zoom reik kito perskaiciavimo.
//            }
//            else {
            float zoom = 1f;
            int positioning = getPositioning() == Window.relativeView ? getController().getPositioning() : getPositioning();
            int clientPositioning = e.getPositioning() == Window.relativeView ? e.getController().getPositioning() : e.getPositioning();
//                float offsetX = 0, offsetY = 0;
            if (positioning != clientPositioning){ // pozicijos susigaudymas, kad viektu ir su absolute, ir su fixed.
                if (positioning == Window.fixedView){ // resizer fixed, kontrole absolute.
                    Vector3 cord = p.worldToScreenCoords(pos.x, pos.y);
                    cord = p.screenToFixedCoords(cord.x, Gdx.graphics.getHeight() - cord.y);
                    ex = cord.x;
                    ey = cord.y;
                }else { // resizer absolute, kontrole fixed. nelabai tai bus...
                    Vector3 cord = p.fixedToScreendCoords(pos.x, pos.y);
                    cord = p.screenToWorldCoords(cord.x, Gdx.graphics.getHeight() - cord.y);
                    ex = cord.x;
                    ey = cord.y;
                }
                zoom = p.getAbsoluteCamera().zoom;
//                    Vector3 camera = p.getAbsoluteCamera().position;
//                    float x = camera.x - p.getScreenWidth()/2, y = camera.y - p.getScreenHeight()/2;
//                    if (positioning != Window.absoluteView) { // fixed, controles absolute.
//                        x = -x;
//                        y = -y;
//                    }
//                    offsetX = x;
//                    offsetY = y;
            }else { // tiek resizer, tiek kontrole yra toj pacioj koordinaciu sistemoj
                ex = pos.x;
                ey = pos.y;
            }
//                ex = pos.x + offsetX;
//                ey = pos.y + offsetY;
//            }
            if (cx > ex) { // kur bus pradzia.
                cx = ex - boxSize / 2;
            }
            if (cy > ey) {
                cy = ey - boxSize / 2;
            }
            float wid = 0, hei = 0;
            if (e instanceof Field) { // kokio dydzio.
                wid = ((Field) e).getWidth() / zoom;
                hei = ((Field) e).getHeight() / zoom;
                if (!((Field) e).isRotatable()){
                    drag = true;
                }
            }else {
                sizing = true;
            }
            if (maxWidPoint < ex + wid){
                maxWidPoint = ex + wid;
//                cwidth += pos.x + wid - cx - cwidth;
            }
            if (maxHeiPoint < ey + hei){
                maxHeiPoint = ey + hei;
//                cheight += pos.y + hei - cy - cheight;
            }
            if (MoreUtils.sq(eDeg) < MoreUtils.sq(degree)){ // kuris arciau 0
                degree = eDeg;
            }
        }
        cwidth = maxWidPoint - cx;
        cheight = maxHeiPoint - cy;
        disableSizing = sizing;
        disableDrag = drag;
        disableMoving = move;
        setPosition(cx, cy);
        setSize(cwidth + boxSize/2, cheight + boxSize/2);
        setOriginMiddle();
        setAngle(degree);
        sizeUpdated();
    }

    @Override
    protected void isvaizda(float x, float y) {
        transformMatrix(x, y);
//        p.noFill();
//        p.stroke(0,255,0);
//        p.rect(x, y, xSize, ySize);

//        p.fill(255,0,0);
//        p.noStroke();
////        p.rect(mouseX, mouseY, 20, 20);
//        for (int a =0;a < ax.size; a++){
//            p.ellipse(ax.get(a), ay.get(a), 5, 5);
//        }

//        Vector2 mid = getMiddlePoint();
//        p.fill(0);
//        p.ellipse(mid.x, mid.y, 5, 5);
    }

    @Override
    protected void drawTransformed(float x, float y) {
        float c = y;
        for (float a = 0, b = x; a < 9; a++, b += getWidth()/2 - boxSize/2){
            if (a == 4)
                continue;
            if (a == 3 || a == 6){
                b = x;
                c += getHeight()/2 - boxSize/2;
            }
            if (activeBox == a) {
                if (disableSizing) p.tint(255,0,0);
                else p.tint(0);
            }else if (rotateActive) {
                if (disableDrag) p.tint(255,0,0);
                else p.tint(0, 255, 0);
            }else
                p.tint(120);
//            drawDrawable(boxes, b, c, boxSize, boxSize, false); // sukelia rotation bug.
            boxes.draw(p.getBatch(), b, c, boxSize, boxSize);
            p.noTint();
        }
    }

    /**
     * @param x mouse x
     * @param y mouse y
     * @param startX interface cord x
     * @param startY interface cord y
     */
    private void activeBoxDrag(float x, float y, float startX, float startY, float startDifX, float startDifY, boolean isRight){
        float sx, sy, ex, ey; // invert, kad leistu keist puses (is kaires tampo ar desines)
        if (isRight){
            sx = x;
            sy = y;
            ex = stx;
            ey = sty;
        }else {
            sx = stx;
            sy = sty;
            ex = x;
            ey = y;
        }
        float nx, ny, offsetX, offsetY; // skirtumui (kiek paslinko etc)
        float a, b, c; // pytagora naudosem1!!
        c = MoreUtils.dist(x, y, stx, sty); // istrizaine
        float alfa = (float) (Math.atan2(sy - ey, sx - ex) - (getAngle()*MathUtils.degreesToRadians)); // istrizaines kampas
        b = (float) (Math.cos(alfa) * c); // plotis
        a = (float) Math.sqrt(MoreUtils.sq(c) - MoreUtils.sq(b)); // ilgis
        nx = xSize;
        ny = ySize;
        setSize(b, a); // naujas dydis
        setOriginMiddle(); // kad nepamestu rotate.
        offsetX = (x + (stx - x)/2) - startX;
        offsetY = (y + (sty - y)/2) - startY;
        update = false; // strigciojimas be sito.
        placeInMiddle(startX + offsetX, startY + offsetY); // nauju cord radimas
        onChange(offsetX - xSize/2 + startDifX, offsetY - ySize/2 + startDifY, b-nx, a-ny); // image perstatymas
    }

    private void activeBoxSideDrag(float x, float y, float startX, float startY, float startXDif, float startYDif, boolean isRight, boolean isUp,
                                   boolean onTop){
        float sx, sy, ex, ey; // invert, kad leistu keist puses (is kaires tampo ar desines)
        int deg, top;
        if (onTop)
            top = 1;
        else
            top = -1;
        if (isRight){
            sx = x;
            sy = y;
            ex = stx;
            ey = sty;
            deg = 0;
        }else {
            sx = stx;
            sy = sty;
            ex = x;
            ey = y;
            deg = 180;
        }
        float c = MoreUtils.dist(x, y, stx, sty); // istrizaine tarp peles ir galutinio tasko
        float alfa = (float) (Math.atan2(sy - ey, sx - ex) - (getAngle()*MathUtils.degreesToRadians)); // istrizaines kampas
        float a, b;
        if (isUp){
            a = MathUtils.sin(alfa) * c;
            b = getWidth();
        }else {
            a = getHeight();
            b = MathUtils.cos(alfa) * c; // plotis
        }
        float rx, ry; // pirmas taskas iki pagrindinio.
        float rad = (getAngle() + deg)*MathUtils.degreesToRadians;
        rx = (float) (Math.cos(rad) * b + stx);
        ry = (float) (Math.sin(rad) * b + sty);
        float rad2 = (getAngle() + 90*top) * MathUtils.degreesToRadians;
        float nx = (float) (Math.cos(rad2) * a + rx); // pagrindinis taskas, tarsi coord taskas
        float ny = (float) (Math.sin(rad2) * a + ry);
        float offsetX, offsetY;
        offsetX = (nx + (stx - nx)/2) - startX; // skirtumas, per kiek turi pasislinkt coord.
        offsetY = (ny + (sty - ny)/2) - startY;
        float oldW, oldH;
        oldW = getWidth();
        oldH = getHeight();
        setSize(b, a);
        setOriginMiddle();
        update = false;
        placeInMiddle(startX + offsetX, startY + offsetY);
        onChange(offsetX - xSize/2 + startXDif, offsetY - ySize/2 + startYDif, b - oldW, a - oldH);
    }

    @Override
    public void onDragging(float x, float y, float deltaX, float deltaY) {
        if (activeBox >= 0) { // dragina active boxa. tures keist parametrus.
            if (disableSizing) return;
            Vector2 pos = getPosition();
            switch (activeBox) {
                case 0: // is apacios kairej
                    activeBoxDrag(x, y, pos.x, pos.y, 0, 0, false);
                    break;
                case 1: // is apacios per viduri
                    activeBoxSideDrag(x, y, pos.x, pos.y, 0, 0, false, true, false);
                    break;
                case 2: // is apacios desinej
                    activeBoxDrag(x, y, pos.x + xSize, pos.y, xSize, 0, true);
                    break;
                case 3: // is vidurio kairej
                    activeBoxSideDrag(x, y, pos.x, pos.y, 0, 0, false, false, false);
                    break;
                case 5: // is vidurio desinej.
                    activeBoxSideDrag(x, y, pos.x + xSize, pos.y, xSize, 0, true, false, false);
                    break;
                case 6: // is virsaus kairej
                    activeBoxDrag(x, y, pos.x, pos.y + ySize, 0, ySize, false);
                    break;
                case 7: // is virsaus per viduri.
                    activeBoxSideDrag(x, y, pos.x, pos.y + ySize, 0, ySize, true, true, true);
                    break;
                case 8: // is virsaus desinej.
                    activeBoxDrag(x, y, pos.x + xSize, pos.y + ySize, xSize, ySize, true);
                    break;
            }
        }else if (rotateActive){ // sukiojimas
            if (disableDrag) return;
            float currentAlpha, old;
            old = getAngle();
            Vector2 pos = getMiddlePoint();
            currentAlpha = MathUtils.atan2(y - pos.y, x - pos.x) - mouseAlpha;
            while (currentAlpha > MathUtils.PI){ // kad butu tarp -180 ir 180
                currentAlpha -= MathUtils.PI2;
            }
            while (currentAlpha < -MathUtils.PI){
                currentAlpha += MathUtils.PI2;
            }
//            float deg = MathUtils.round(currentAlpha*MathUtils.radiansToDegrees);
            float deg = MoreUtils.roundFloat(currentAlpha*MathUtils.radiansToDegrees, rotationRound);
            setAngle(deg);
            onRotate(deg, old);
        } else { // dragina visa. turi tik cord keist.
            if (!disableMoving) {
                Vector2 pos = getPosition();
                setPosition(pos.x + deltaX, pos.y + deltaY);
                onChange(deltaX, deltaY, 0, 0);
            }
        }
    }

    @Override
    public void onDrop(float x, float y, float deltaX, float deltaY) {
        undoredo.setInterfaces(controls);
//        Vector2 pos = getPosition();
        if (activeBox >= 0){
//            undoredo.moved(6, pos.x - startX, pos.y - startY, getWidth() - startWidth,
//                    getHeight() - startHeight, 0, 0, null);
            undoredo.moved(6);
        }else if (rotateActive){
            if (controls.size() == 1) {
//                undoredo.getFlushSettings().clear();
//                undoredo.getFlushSettings().add(getRadius() - startRotation);
                undoredo.moved(2, getAngle() - startRotation);
//                undoredo.moved(2, getRadius() - startRotation, 0, 0, 0, 0, 0, null);
            }else {
                Vector2 mid = getMiddlePoint();
//                undoredo.moved(7, getRadius() - startRotation, mid.x, mid.y, 0, 0, 0, null);
                undoredo.moved(7, getAngle() - startRotation, mid.x, mid.y);
            }
        }else {
//            undoredo.moved(0, pos.x - startX, pos.y - startY, 0,0 , 0, 0, null);
            undoredo.moved(0);
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return !(Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
                && super.touchDown(x, y, pointer, button);
    }

    @Override
    public void onPress(float x, float y) {
        super.onPress(x, y);
        float mx = x, my = y;
        if (getAngle() != 0){
            Vector2 rot = rotatePoint(x, y, -getAngle()); // rotate point paveikia middle point vectoriu... todel negalima naudot po "getMiddlePoint()" metodo.
            mx = rot.x;
            my = rot.y;
        }
        Vector2 pos = getPosition();
        float sizeY = pos.y;
        for (float a = 0, sizeX = pos.x; a < 9; a++, sizeX += getWidth()/2 - boxSize/2){ // 3 eiles
            if (a == 4) // vidurinio ner
                continue;
            if (a == 3 || a == 6){ // nuresetins cord kai eilute keices.
                sizeX = pos.x;
                sizeY += getHeight()/2 - boxSize/2;
            }
            float size = boxSize * 1.5f;
            if (mx > sizeX && mx < sizeX+boxSize && my > sizeY && my < sizeY + boxSize){  // boxe
                activeBox = (int) a;
                switch (activeBox){
                    case 0:
                    case 1:
                    case 3:
                        stx = pos.x + xSize;
                        sty = pos.y + ySize;
                        break;
                    case 2:
                    case 5:
                        stx = pos.x;
                        sty = pos.y + ySize;
                        break;
                    case 6:
                        stx = pos.x + xSize;
                        sty = pos.y;
                        break;
                    case 7:
                    case 8:
                        stx = pos.x;
                        sty = pos.y;
                        break;
                }
                if (getAngle() != 0){
                    Vector2 rot = rotatePoint(stx, sty, getAngle());
                    stx = rot.x;
                    sty = rot.y;
                }
                break;
            }else if (mx > sizeX-size/2 && mx < sizeX+size && my > sizeY-size/2 && my < sizeY + size){ // aplink box, leis rotate.
                Vector2 mid = getMiddlePoint();
                rotateActive = true;
                mouseAlpha = MathUtils.atan2(y - mid.y, x - mid.x) - getAngle() * MathUtils.degreesToRadians;
                break;
            }
        }
//        startX = pos.x;
//        startY = pos.y;
//        startWidth = getWidth();
//        startHeight = getHeight();
        startRotation = getAngle();
        if (!rotateActive){
            undoredo.getFlushSettings().clear();
            for (Interface e : controls){
                Vector2 epos = e.getPosition();
                float width = 0, height = 0;
                if (e instanceof Field){
                    width = ((Field) e).getWidth();
                    height = ((Field) e).getHeight();
                }
                undoredo.getFlushSettings().add(epos.x, epos.y, width, height);
            }
        }
    }

    @Override
    public void release() {
        super.release();
        activeBox = -1;
        rotateActive = false;
    }

    public interface ChangeListener{
        /**
         * called when interface size has been changed
         * @param e interface which size is changed
         * @param x interface x position
         * @param y interface y position
         * @param width  interface width
         * @param height interface height
         */
        void onSizeChange(Interface e, float x, float y, float width, float height);

        /**
         * called when interface has been moved.
         * @param e interface which position was changed.
         * @param x interface x position
         * @param y interface y position
         */
        void onPositionChange(Interface e, float x, float y);

//        public void onRotation(Interface e, float radiusOld, float radiusNew);

        /**
         * called when size is changed.
         * @param x y width height - difference in change
         * @param isMulti true if resizer changes more than one control, false otherwise
         */
        void onChange(float x, float y, float width, float height, boolean isMulti);

        void onRotation(float radiusOld, float radiusNew);
    }

    @Override
    public InterfaceStyle getStyle() {
        return null;
    }

    public static class ResizerStyle extends ClickableStyle{
        @Override
        public Interface createInterface() {
            return null;
        }
    }
}
