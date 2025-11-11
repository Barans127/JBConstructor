package com.jbconstructor.editor.forms.editorForm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.ControlHost;
import com.engine.ui.controls.TopPainter;
import com.engine.ui.listeners.MainDraw;
import com.jbconstructor.editor.dialogs.physicsEditor.PhysicsEditor;
import com.jbconstructor.editor.editors.ChainEdging;
import com.jbconstructor.editor.editors.JointManager;
import com.jbconstructor.editor.root.Element;
import com.jbconstructor.editor.root.FiguresDrawer;
import com.jbconstructor.editor.root.PhysicsHolder;

/** Class who manages figures drawings. It can draw physics polygons, chains, joint itself */
public class JointControlDrawer implements MainDraw{
    private JointControlPanel controlPanel;
    // pool musu figuru piesimo
    private Pool<FiguresDrawer> figuresDrawerPool;

    // dabar aktyvios ir piesiamos figuros.
    private Array<FiguresDrawer> figures;

    // special for mouse joint.
    private Drawable pointer = Resources.getDrawable("repositionIcon");

    private float tmpAngleHolder;

    public JointControlDrawer(JointControlPanel controlPanel){
        figuresDrawerPool = new Pool<FiguresDrawer>() {
            @Override
            protected FiguresDrawer newObject() {
                return new FiguresDrawer();
            }
        };
        figures = new Array<>();
        this.controlPanel = controlPanel;
    }

    /* figuru pridejimas */

    /** Draws resource physics polygons. Multiple shapes might be used if resource has more then 1 fixture. */
    public void addPhysicsPolygon(Element resource, Array<FiguresDrawer> arrayToFill){ // sito redaguot nereiks.
        PhysicsHolder physicsHolder = resource.getPhysicsHolder();
        for (PhysicsEditor.FixtureShapeHolder e : physicsHolder.shapes){
            FiguresDrawer drawer = figuresDrawerPool.obtain();
            Vector2 resourcePos = resource.getPosition();
            float widthRatio, heightRatio;
            widthRatio = resource.getWidth() / resource.getImage().getMinWidth();
            heightRatio = resource.getHeight() / resource.getImage().getMinHeight();
            if (e.type == PhysicsEditor.CIRCLE){
                if (e.x.size() > 0 && e.y.size() > 0) {
                    if (resource.getAngle() == 0) {
                        drawer.addPoint(resourcePos.x + e.x.get(0) * widthRatio, resourcePos.y + e.y.get(0) * heightRatio);
                    }else {
                        // detale versta, reik verst visa figura.
                        rotatePoint(resource, e.x.get(0), e.y.get(0), widthRatio, heightRatio, drawer);
                    }
                }
                drawer.setFigureEllipse(true, e.radius * Math.min(widthRatio, heightRatio));
            }else {
                for (int a = 0; a < e.x.size(); a++){
                    if (resource.getAngle() == 0) {
                        drawer.addPoint(resourcePos.x + e.x.get(a) * widthRatio, resourcePos.y + e.y.get(a) * heightRatio);
                    }else {
                        rotatePoint(resource, e.x.get(a), e.y.get(a), widthRatio, heightRatio, drawer);
                    }
                }
                drawer.setLoop(e.type == PhysicsEditor.POLYGON); // TODO physics editor nepalaiko chain loop.!!!
            }
            insertShapeToDrawer(drawer);
            if (arrayToFill != null){
                arrayToFill.add(drawer);
            }
            // nustatom spalva.
//            if (resource.getBodyType() == BodyDef.BodyType.StaticBody){ // static
            if (physicsHolder.bodyType == BodyDef.BodyType.StaticBody){ // static
                drawer.lineColor = 0xff7fe67f;
//            }else if (resource.getBodyType() == BodyDef.BodyType.KinematicBody){ // kinematic
            }else if (physicsHolder.bodyType == BodyDef.BodyType.KinematicBody){ // kinematic
                drawer.lineColor = 0xff7f7fe6;
            }else { // dynamic
                drawer.lineColor = 0xffe6b3b3;
            }
            drawer.lineWeight = 5f;
        }
    }

    // physics polygons reikalingas dalykas.
    private void rotatePoint(Element resource, float x, float y, float widthRatio, float heightRatio, FiguresDrawer drawer){
        Vector2 resourcePos = resource.getPosition();
        // detale versta, reik verst visa figura.
        float rx = resourcePos.x + x * widthRatio,
                ry = resourcePos.y + y * heightRatio;
        Vector2 middlePoint = resource.getMiddlePoint();
        float dist = MoreUtils.dist(middlePoint.x, middlePoint.y, rx, ry);
        float angle = (float) Math.atan2(ry - middlePoint.y, rx - middlePoint.x) + resource.getAngle() * MathUtils.degreesToRadians;
        rx = (float) (Math.cos(angle) * dist) + middlePoint.x;
        ry = (float) (Math.sin(angle) * dist) + middlePoint.y;

        // viska pavertus tik dabar galim det taska.
        drawer.addPoint(rx, ry);
    }

    /** Draws chain figure from this chain */
    public FiguresDrawer addChain(ChainEdging.Chains chain){ // sitas neredaguojamas
        FiguresDrawer e = figuresDrawerPool.obtain();
        for (int a = 0; a < chain.x.size; a++){
            e.addPoint(chain.x.get(a), chain.y.get(a));
            e.setLoop(chain.loop);
        }
        insertShapeToDrawer(e);
        // nustatom spalva
        e.lineColor = 0xff7fe67f;
        e.lineWeight = 5f;
        return e;
    }

    /** Draws joint from this joint info. */
    public void addJoint(JointManager.JointInfo jointInfo, Array<FiguresDrawer> fillList){ // cia imanoma redaguot.
        if (jointInfo != null) {
            switch (jointInfo.getJointType()) {
                // paprasti anchor tipo jointai.
                case 0: // distance
                case 1: // friction
                case 3: // motor
                case 5: // prismatic
                case 7: // revolute
                case 8: // rope
                case 9: // weld
                case 10: { // wheel
                    if (controlPanel.isBodySelected(true) && controlPanel.isBodySelected(false)) {
                        // piest galim tik jei abu kunai yra pasirinkti.
                        float ax, ay, bx, by;
                        if (jointInfo.getJointType() == 3) {
                            // motor jointo ignorinsim anchorus
                            ax = ay = bx = by = 0;
                        } else {
                            ax = jointInfo.anchorA.x;
                            ay = jointInfo.anchorA.y;
                            bx = jointInfo.anchorB.x;
                            by = jointInfo.anchorB.y;
                        }
                        // pradedam nuo a body.
                        Vector2 bodyA = controlPanel.getBodyPosition(true);
                        FiguresDrawer figure = figuresDrawerPool.obtain();
                        figure.addPoint(bodyA.x, bodyA.y);
                        float aAngle = controlPanel.getBodyAngle(true);
                        if (aAngle != 0) { // perskaiciuojam anchor.
                            float px = bodyA.x + ax;
                            float py = bodyA.y + ay;
                            float dist = MoreUtils.dist(bodyA.x, bodyA.y, px, py);
                            float rAngle = (float) (Math.atan2(py - bodyA.y, px - bodyA.x) + aAngle);
                            ax = (float) (Math.cos(rAngle) * dist);
                            ay = (float) (Math.sin(rAngle) * dist);
                        }
                        figure.addPoint(bodyA.x + ax, bodyA.y + ay);
//                        }
                        // toliau keliaujam i b body
                        Vector2 bodyB = controlPanel.getBodyPosition(false);
//                        if (bx != 0 || by != 0) {
                            // jei yra anchor b kunui, tai dedam
                        float bAngle = controlPanel.getBodyAngle(false);
                        if (bAngle != 0){ // perskaiciuojam kampa, jei kunas pasuktas.
                            float px = bodyB.x + bx;
                            float py = bodyB.y + by;
                            float dist = MoreUtils.dist(bodyB.x, bodyB.y, px, py);
                            float rAngle = (float) (Math.atan2(py - bodyB.y, px - bodyB.x) + bAngle);
                            bx = (float) (Math.cos(rAngle) * dist);
                            by = (float) (Math.sin(rAngle) * dist);
                        }
                        figure.addPoint(bodyB.x + bx, bodyB.y + by);
//                        }
                        // sujungiam su kuno tasku
                        figure.addPoint(bodyB.x, bodyB.y);

                        // dedam i piesima
                        addCustomShape(figure);

                        if (fillList != null) {
                            fillList.add(figure);
                        }

                        // nustatom spalva jointo
                        figure.lineColor = 0xff7fcccc;
                    }
                    break;
                }
                case 4: { // mouse. Tik target piesimas.
                    FiguresDrawer figure = figuresDrawerPool.obtain();
                    figure.customPoint = pointer;
                    figure.setPointsDrawing(true);
                    figure.pointWeight = 50;

                    figure.addPoint(jointInfo.anchorA.x, jointInfo.anchorA.y);

                    addCustomShape(figure);

                    if (fillList != null){
                        fillList.add(figure);
                    }
                    // nustatom spalva jointo
                    figure.lineColor = 0xff7fcccc;
                    break;
                }
                case 2: // gear joint.
                    JointManager.JointInfo info1 = controlPanel.getJointManager().getJointInfo(jointInfo.joint1ID);
                    JointManager.JointInfo info2 = controlPanel.getJointManager().getJointInfo(jointInfo.joint2ID);
                    // piest galim tik tuo atveju jei jointai egzistuoja
                    // ir jei jointai yra butent to type kurio reik!
                    int color;
                    if (info1 != null && (info1.getJointType() == 5 || info1.getJointType() == 7)){
                        if (info1.getJointType() == 5){
                            color = 0xffe50000;
                        }else {
                            color = 0xff0000e5;
                        }
                        drawDifferentJoint(info1,color, fillList);
                    }
                    if (info2 != null && (info2.getJointType() == 5 || info2.getJointType() == 7)){
                        if (info2.getJointType() == 5){
                            color = 0xff990000;
                        }else {
                            color = 0xff000066;
                        }
                        drawDifferentJoint(info2,color, fillList);
                    }
                    break;
                case 6: { // pulley joint
                    if (controlPanel.isBodySelected(true) && controlPanel.isBodySelected(false)) {
                        Vector2 bodyA = controlPanel.getBodyPosition(true);
                        Vector2 bodyB = controlPanel.getBodyPosition(false);
                        FiguresDrawer figure = figuresDrawerPool.obtain();
                        // a kuno taskai
                        figure.addPoint(bodyA.x, bodyA.y);
                        float aAngle = controlPanel.getBodyAngle(true);
                        float ax = jointInfo.anchorA.x;
                        float ay = jointInfo.anchorA.y;
                        if (aAngle != 0) { // perskaiciuojam anchor.
                            float px = bodyA.x + ax;
                            float py = bodyA.y + ay;
                            float dist = MoreUtils.dist(bodyA.x, bodyA.y, px, py);
                            float rAngle = (float) (Math.atan2(py - bodyA.y, px - bodyA.x) + aAngle);
                            ax = (float) (Math.cos(rAngle) * dist);
                            ay = (float) (Math.sin(rAngle) * dist);
                        }
                        figure.addPoint(bodyA.x + ax, bodyA.y + ay);
                        // ground taskai
                        // ground tasku perskaiciuot nereik.
                        figure.addPoint(jointInfo.localAxisA.x, jointInfo.localAxisA.y);
                        figure.addPoint(jointInfo.groundAnchorB.x, jointInfo.groundAnchorB.y);
                        // b kuno taskai
                        float bAngle = controlPanel.getBodyAngle(false);
                        float bx = jointInfo.anchorB.x;
                        float by = jointInfo.anchorB.y;
                        if (bAngle != 0){ // perskaiciuojam kampa, jei kunas pasuktas.
                            float px = bodyB.x + bx;
                            float py = bodyB.y + by;
                            float dist = MoreUtils.dist(bodyB.x, bodyB.y, px, py);
                            float rAngle = (float) (Math.atan2(py - bodyB.y, px - bodyB.x) + bAngle);
                            bx = (float) (Math.cos(rAngle) * dist);
                            by = (float) (Math.sin(rAngle) * dist);
                        }
                        figure.addPoint(bodyB.x + bx, bodyB.y + by);
                        figure.addPoint(bodyB.x, bodyB.y);

                        // dedam i piesima
                        addCustomShape(figure);

                        if (fillList != null){
                            fillList.add(figure);
                        }
                        // nustatom spalva jointo
                        figure.lineColor = 0xff7fcccc;
                    }
                    break;
                }
            }
        }
    }

    // gear joint vieno joint piesimas
    private void drawDifferentJoint(JointManager.JointInfo info, int color, Array<FiguresDrawer> fillList){
        if (info.bodyA != null && info.bodyB != null){
            // visu pirma kunai turi but parinkti
            Vector2 posA = addDifferentJointBody(info.bodyA, info.bodyAIsResource, fillList);
            float bodyAAngle = tmpAngleHolder;
            if (posA == null){
                return; // pozocijos turi butinai egzistuot
            }
            Vector2 posB = addDifferentJointBody(info.bodyB, info.bodyBIsResource, fillList);
            float bodyBAngle = tmpAngleHolder;
            if (posB == null){
                return; // abi turi egzistuot
            }
            // toliau pacio joint piesimas.
            FiguresDrawer figure = figuresDrawerPool.obtain();
            figure.addPoint(posA.x, posA.y);
            // a anchor perskaiciavimas
            float ax, ay;
            if (bodyAAngle == 0){
                ax = info.anchorA.x;
                ay = info.anchorA.y;
            }else {
                float dx = posA.x + info.anchorA.x;
                float dy = posA.y + info.anchorA.y;
                float dist = MoreUtils.dist(posA.x, posA.y, dx, dy);
                float rangle = (float) (Math.atan2(dy - posA.y, dx - posA.x) + bodyAAngle);
                ax = (float) (Math.cos(rangle) * dist);
                ay = (float) (Math.sin(rangle) * dist);
            }
            figure.addPoint(posA.x + ax, posA.y + ay);
            // b anchor perskaiciavimas
            if (bodyBAngle == 0){
                ax = info.anchorB.x;
                ay = info.anchorB.y;
            }else {
                float dx = posB.x + info.anchorB.x;
                float dy = posB.y + info.anchorB.y;
                float dist = MoreUtils.dist(posB.x, posB.y, dx, dy);
                // plius angle, nes is jau nustumto reik atstumt i matoma, be kampo.
                float rangle = (float) Math.atan2(dy - posB.y, dx - posB.x) + bodyBAngle;
                ax = (float) (Math.cos(rangle) * dist);
                ay = (float) (Math.sin(rangle) * dist);
            }
            figure.addPoint(posB.x + ax, posB.y + ay);
            figure.addPoint(posB.x, posB.y);

            figure.lineColor = color;

            if (fillList != null){
                fillList.add(figure);
            }

            addCustomShape(figure);
        }
    }

    // gear joint vieno kuno piesimas.
    private Vector2 addDifferentJointBody(String bodyId, boolean bodyResource, Array<FiguresDrawer> figuresDrawers){
        Vector2 bodyPos = new Vector2();
        EditForm editForm = controlPanel.getEditForm();
        if (bodyResource){
            // body yra polygons.
            ControlHost controller = editForm.getController();
            for (Control e : controller.getControls()){
                if (e instanceof Element){ // prieisim tik prie resource, nes tik ten polygonai.
                    Element owner = (Element) e;
                    PhysicsHolder physicsHolder = owner.getPhysicsHolder();
//                    if (owner.getShapes().size > 0 && e.getIdName().equals(bodyId)){
                    if (physicsHolder.hasShapes() && e.getIdName().equals(bodyId)){
                        addPhysicsPolygon(((Element) e), figuresDrawers);

                        // toliau randam body taska
                        // KODAS PAIMTAS IS JointControlPointPanel.
                        float startX, startY;
                        Vector2 pos = owner.getPosition();
//                        if (owner.isBodyOriginMiddle()){ // body pozicijos nutatymas.
                        if (physicsHolder.isBodyOriginMiddle){ // body pozicijos nutatymas.
                            startX = owner.getWidth() / 2; // nieko ypatingo, viskas vidury.
                            startY = owner.getHeight() / 2;
                        }else {
                            float rWidth = owner.getImage().getMinWidth();
                            float rHeight = owner.getImage().getMinHeight();
                            float wRatio = owner.getWidth() / rWidth;
                            float hRatio = owner.getHeight() / rHeight;
//                            Vector2 origin = owner.getBodyOrigin();
                            Vector2 origin = physicsHolder.bodyOrigin;
                            float rOriginX = origin.x * wRatio;
                            float rOriginY = origin.y * hRatio;
                            float angle = owner.getAngle() * MathUtils.degreesToRadians; // pozicija perskaiciuot tik jei body pasuktas.
                            if (angle != 0) { // perskaiciuojam pozicija
                                rOriginX += pos.x;
                                rOriginY += pos.y;
                                Element resource = (Element) e;
                                Vector2 middlePoint = resource.getMiddlePoint();
                                float dist = MoreUtils.dist(middlePoint.x, middlePoint.y, rOriginX, rOriginY);
                                float rAngle = (float) Math.atan2(rOriginY - middlePoint.y, rOriginX - middlePoint.x) + angle;
                                rOriginX = (float) (Math.cos(rAngle) * dist) + resource.getWidth()/2;
                                rOriginY = (float) (Math.sin(rAngle) * dist) + resource.getHeight()/2;
                            }
                            startX = rOriginX;
                            startY = rOriginY;
                        }

                        // nustatom kuno taska.
                        bodyPos.set(pos.x + startX, pos.y + startY);
                        tmpAngleHolder = owner.getAngle() * MathUtils.degreesToRadians;
                        return bodyPos;
                    }
                }
            }
        }else { // body yra chain
            ChainEdging edging = editForm.getChainEdging();
            for (ChainEdging.Chains e : edging.getChains()){
                if (e.name.equals(bodyId)){
                    if (figuresDrawers != null) {
                        figuresDrawers.add(addChain(e));
                    }else {
                        addChain(e);
                    }
                    // cia body tasko pridejimas
                    if (e.x.size > 0) {
                        bodyPos.set(e.x.get(0), e.y.get(0));
//                        FiguresDrawer figure = drawer.getEmptyFigureDrawer();
//                        figure.addPoint(bodyPos.x, bodyPos.y);
//                        figure.setPointsDrawing(true);
//                        figure.pointColor = 0xffff0000;
//                        figure.pointWeight = 10;
//
//                        drawer.addCustomShape(figure);
//                        figuresDrawers.add(figure);
                    }else {
                        bodyPos.set(0,0);
                    }
                    tmpAngleHolder = 0;
                    return bodyPos;
                }
            }
        }
        return null;
    }

    /** Add custom shape to shape list.  */
    public void addCustomShape(FiguresDrawer e){
        if (e != null)
            insertShapeToDrawer(e); // tiesiog taip
    }

    /* figuru paleidimas, isemimas, valdymas */

    private void insertShapeToDrawer(FiguresDrawer e){
        figures.add(e);
//        TopPainter.addPaintOnTop(e);
    }

    private void removeShapeFromDrawer(FiguresDrawer e){
        figures.removeValue(e, true); // salinam is aktyvaus saraso
//        TopPainter.removeTopPaint(e); // stabdom piesima.
        figuresDrawerPool.free(e); // atiduodam atgal i poola.
    }

    /* priejimas prie saraso */

    /** size of active shapes. */
    public int getActiveShapesSize(){
        return figures.size;
    }

    /** @return active shape by index. If index out of bounds then null is returned. */
    public FiguresDrawer getActiveShape(int index){
        if (index >= 0 && index < figures.size){
            return figures.get(index);
        }else {
            return null;
        }
    }

    /* naikinimas is saraso */

    public void removeShape(Array<FiguresDrawer> figures){
        for (FiguresDrawer e : figures)
            removeShape(e);
    }

    /** Removes this shape from list. Prevent it from drawing. */
    public void removeShape(FiguresDrawer e){
        if (e != null){
            removeShapeFromDrawer(e);
        }
    }

    /** Remove shape by index. If index out of bounds nothing happens */
    public void removeShape(int index){
        if (index >= 0 && index < figures.size){
            removeShapeFromDrawer(figures.get(index));
        }else {
            Gdx.app.log("JointControlDrawer", "Cannot find shape. Index out of bounds!");
        }
    }

    /** Removes all active shapes. */
    public void clear(){
        for (int a = figures.size - 1; a >= 0; a--){
            removeShapeFromDrawer(figures.get(a));
        }
    }

    /* paemimas is pool */

    /** @return empty figure drawer for custom use. */
    public FiguresDrawer getEmptyFigureDrawer(){
        return figuresDrawerPool.obtain();
    }

    /* Override */

    @Override
    public void draw() {
        for (FiguresDrawer e : figures){
            e.draw();
        }
    }

    @Override
    public boolean drop(int reason) {
        return reason == TopPainter.popupShowUpDrop;
    }
}
