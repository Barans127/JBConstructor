package com.jbconstructor.editor.dialogs.physicsEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.Engine;
import com.engine.core.ErrorMenu;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.PopUp;
import com.engine.ui.controls.Window;
import com.engine.ui.controls.widgets.Button;
import com.engine.ui.controls.widgets.ImageButton;
import com.engine.ui.controls.widgets.Label;
import com.engine.ui.listeners.ClickListener;
import com.engine.root.GdxWrapper;
import com.jbconstructor.editor.managers.Project;
import com.jbconstructor.editor.root.Element;
import com.jbconstructor.editor.root.PhysicsHolder;

import java.io.Serializable;
import java.util.ArrayList;

public class PhysicsEditor extends PopUp {
    private Engine p = GdxWrapper.getInstance();
    Element resource; // vaizdui.
    private Drawable points, originPoint;

    // draginimui.
//    private float offsetX, offsetY;
    int editPoint = -1;
    //didinimui.
    private float zoom = 1f, zoomSensitivity;

    // dydzio valdymui.
    private float widthRatio = 1f, heightRatio = 1f;
    private float rwidth, rheight;

    //rodymui
    private boolean isDone, isNewPoint; // ar baigta figura.
    private float oldX, oldY; // moved undo veiksmui.

    //valdymui
    // -1 - body state panel.
    // 0 - tampymas, resizinimas.
    // 1 - physics tasku, polygon taskai.
    // 2 - physics ratas, ellipse
    // 3 - physics zeme, chainShape
    // 4 - edgeShape
    private int movingState = 0;

    // space valdymas
    private int oldState;
    private boolean spaceActive;

    // daugiau nei vienas kunas.
    private Pool<FixtureShapeHolder> shapes; // kad nekurt kiekviena kart.
    private Array<FixtureShapeHolder> activeShapes; // visi sukurti kunai
    private FixtureShapeHolder currentShape, editableShape; // redaguojamas kunas.
    public static final int POLYGON = 0, CIRCLE = 1, CHAIN = 2, EDGE = 3;

    private float startX, startY;
    private boolean isPressed;

    // interfaces.
    private PhysicsEditorPanel mainPanel;
    private PhysicsEditorPanel2 editPanel;
    private PhysicsEditorBodyPanel bodyPanel;
    PhysicsEditorFixturePanel fixturePanel;
//    private SymbolButton bodyPanelButton;

    // irasymui i kunus.
//    private boolean isPanelElement, needsPanelElementFlush;
    PhysicsHolder element;
//    private ImagesTab.ConnectedBlock panelElement; // jeigu kursim shaped panelio tabui (physics)
//    private Element editorElement; // jeigu tai tiesiog resource is editorio lauko.

    // 80% dydzio ekrano..
    public PhysicsEditor() {
        super(GdxWrapper.getInstance().getScreenWidth()*0.95f, GdxWrapper.getInstance().getScreenHeight()*0.95f);
        zoomSensitivity = Resources.getPropertyFloat("physicsEditorZoomSensitivity", 20f);
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        points = Resources.getDrawable("halfWhiteColor");
        originPoint = Resources.getTextureDrawable("repositionIcon");
        shapes = new Pool<FixtureShapeHolder>() {
            @Override
            protected FixtureShapeHolder newObject() {
                return new FixtureShapeHolder();
            }
        };
        hideCloseButton(true);
        activeShapes = new Array<>();
        currentShape = shapes.obtain();
        resource = new Element(new Element.ElementStyle());
        resource.setPositioning(Window.Position.fixed);
        addControl(resource);

        //paneles.
        PhysicsEditorPanel e = new PhysicsEditorPanel(this);
        addControl(mainPanel = e);
        PhysicsEditorPanel2 e2 = new PhysicsEditorPanel2(this);
        e2.setVisible(false);
        addControl(editPanel = e2);
        PhysicsEditorBodyPanel e3 = new PhysicsEditorBodyPanel(this);
        e3.setVisible(false);
        addControl(bodyPanel = e3);
        fixturePanel = new PhysicsEditorFixturePanel(this);

        // fiziniai mygtukai virs paneles.
        // zoom label
        Label.LabelStyle st = new Label.LabelStyle();
        st.autoSize = false;
        st.textSize = 40;
        st.width = getWidth() * 0.2f;
        st.height = 40;
        st.horizontalAlign = Align.center;
        st.verticalAlign = Align.center;
        st.x = 972;
        st.y = 103;
//        final Label zoom = new Label("Zoom:", st);
//        zoom.placeInMiddle(getWidth()*0.9f, getHeight()/9 * 3);
//        addControl(zoom);
        // recenter label
        Label center = new Label("Zoom control:", st);
//        center.placeInMiddle(getWidth() * 0.9f, getHeight()/7f);
        addControl(center);
        // zoom mygtukai. desinis. +
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        Button.ButtonStyle bst = new Button.ButtonStyle();
        bst.autoSize = false;
        bst.background = Resources.getDrawable("halfWhiteColor");
        bst.width = 40;
        bst.height = 40;
        bst.normalColor = 0xFF0000FF;
        bst.x = 1040;
        bst.y = 57;
        Button plius = new Button("+", bst);
//        plius.placeInMiddle(getWidth()*.85f, getHeight()/9*2.2f);/
        plius.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                PhysicsEditor.this.zoom += 3/zoomSensitivity; // kodel 3?
                resource.setSize(rwidth * PhysicsEditor.this.zoom,
                        rheight * PhysicsEditor.this.zoom);
            }
        });
        addControl(plius);
        // kairys. -
        bst.y = 10;
        Button minus = new Button("-", bst);
//        minus.placeInMiddle(getWidth()*0.95f, getHeight()/9*2.2f);
        minus.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                PhysicsEditor.this.zoom -= 3/zoomSensitivity; // kodel 3?
                resource.setSize(rwidth * PhysicsEditor.this.zoom,
                        rheight * PhysicsEditor.this.zoom);
            }
        });
        addControl(minus);
        // reposition.
//        Resources.addImage("repositionIcon", "resources/constructorUI/physicsEditor/mainPanel/reposition.png");
        ImageButton.ImageButtonStyle ist = new ImageButton.ImageButtonStyle();
        ist.autoSize = false;
        ist.background = Resources.getTextureDrawable("repositionIcon");
        ist.width = 76;
        ist.height = 76;
        ist.x = 1090;
        ist.y = 16;
        ImageButton position = new ImageButton(ist);
//        position.placeInMiddle(getWidth()*0.9f, 0);
        position.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
//                offsetX = offsetY = 0;
                PhysicsEditor.this.zoom = 1f;
                resource.setSize(rwidth,rheight);
                resource.placeInMiddle(getWidth()/2, getHeight()/2);
            }
        });
        addControl(position);
    }

    /* veikimas viduj. */

    void enterBodyPanel(){
//        if (editorElement != null) {
        exitEditPanel();
        mainPanel.setVisible(false);
//        bodyPanel.show(editorElement);
        bodyPanel.show(element, resource.getImage());
        movingState = -1; // body panel state.
//        }
    }

    void exitBodyPanel(){
        bodyPanel.setVisible(false);
        mainPanel.setVisible(true);
        mainPanel.deselectShape();
        movingState = mainPanel.checkState();
    }

    void changeControlState(int state){
        movingState = state;
    }

    void setEnd(boolean end){
        isDone = end;
        if (isDone){
            if (currentShape.x.size() < 3){ // negali but end kai maziau 3 tasku.
                isDone = false;
            }
        }
    }

    boolean hasEnd(){
        return isDone;
    }

    void exitEditPanel(){
        editPanel.setVisible(false);
        mainPanel.setVisible(true);
        if (editableShape != null){
            shapes.free(editableShape);
            editableShape = null;
        }
        mainPanel.deselectShape();
        movingState = mainPanel.checkState();
    }

    void deployShape(){
        activeShapes.add(currentShape);
//        currentShape = null;
        currentShape = shapes.obtain();
//        updateShapesList();
        mainPanel.updateList();
    }

    void removeDeployedShape(int index){
        if (index < 0 || index >= activeShapes.size){
            return;
        }
        FixtureShapeHolder e = activeShapes.get(index);
        activeShapes.removeIndex(index);
        shapes.free(e);
//        updateShapesList();
        mainPanel.updateList();

        Project.getSaveManager().triggerSave(); // istryne viena fixtura, reik autosav kviest.!
    }

    void editDeployedShape(int index){
        if (index < 0 || index >= activeShapes.size){
            return;
        }
        FixtureShapeHolder e = activeShapes.get(index);
        activeShapes.removeIndex(index);
        editableShape = e;
//        currentShape = shapes.obtain();
//        currentShape.x.addAll(editableShape.x); // kopijavimas.
//        currentShape.y.addAll(editableShape.y);
//        currentShape.radius = editableShape.radius;
//        currentShape.type = editableShape.type;
        currentShape.copy(editableShape);

        movingState = editableShape.type + 1; // teisingo state nustatymas.
        if (movingState == 1){
            isDone = true;
        }
        editPanel.show(movingState, currentShape); // perjungimas i kita panele.
        mainPanel.setVisible(false);
        mainPanel.selectedIndex = -1;
    }

    void returnEditableShape(){
        if (editableShape != null) {
            activeShapes.add(editableShape);
            editableShape = null;
        }
    }

    public Array<FixtureShapeHolder> getActiveShapes(){
        return activeShapes;
    }

    public Pool<FixtureShapeHolder> getShapesPool(){
        return shapes;
    }

//    private void updateShapesList(){
//        mainPanel.updateList(activeShapes);
//    }

//    public FixtureShapeHolder getFixtureShapeInstance(){
//        return shapes.obtain();
//    }

    /* piesimas */

    private void drawShape(FixtureShapeHolder e, int color, boolean hasEnd, float strokeWeight, boolean isInProgress){
        Vector2 pos = resource.getPosition();
        p.noFill();
        p.stroke(color); // spalva????
        p.strokeWeight(strokeWeight);
//        float zoom;
        float zoomx = widthRatio*this.zoom, zoomy = heightRatio * this.zoom;
        if (e.type == CIRCLE){
            float radius = e.radius * zoom * Math.min(widthRatio, heightRatio); // imam maziausia ratio.
            p.ellipse(pos.x + e.x.get(0)*zoomx, pos.y + e.y.get(0)*zoomy, radius, radius);
        }else {
            if (e.x.size() > 1) {
                for (int a = 0; a < e.x.size(); a++) {
                    if (a + 1 < e.x.size()){
                        p.line(pos.x + e.x.get(a)*zoomx, pos.y + e.y.get(a)*zoomy, pos.x + e.x.get(a+1)*zoomx,
                                pos.y + e.y.get(a+1)*zoomy);
                    }else if (hasEnd && e.type == POLYGON) { // paskutinis sujungimui. tik polygonui..
                        p.line(pos.x + e.x.get(a)*zoomx, pos.y + e.y.get(a)*zoomy,
                                pos.x + e.x.get(0)*zoomx, pos.y + e.y.get(0)*zoomy); // sujunks su pirmu.
                    }
                }
            }
        }
        float size;
        for (int a = 0; a < e.x.size(); a++){ // tasku isdeliojimas.
            if (isInProgress && a == editPoint) {
                size = 6f * zoom;
                p.tint(0xFFFF0000);
            }else {
                size = 3f * zoom;
                p.tint(0xFFFFFFFF);
            }
            points.draw(p.getBatch(), pos.x + e.x.get(a)*zoomx - size/2, pos.y + e.y.get(a)*zoomy - size/2, size, size);
        }
        p.noTint();
    }

    /* pop up valdymas. */

    /** cleans PhysicsEditor. Leaving it without any shapes or elements. */
    public void clear(){
//        panelElement = null;
//        editorElement = null;
        element = null; // nelaikom.
//        for (FixtureShapeHolder e : activeShapes){ // bug kai po uzdarimo nebelieka sukurtu fixturu
//            shapes.free(e);
//        }
        activeShapes.clear();
        if (currentShape != null) {
            currentShape.reset();
        }
        if (editableShape != null){
            shapes.free(editableShape);
            editableShape = null;
        }
    }

    /** flushes all created shapes. */
    void flushShapes(){
//        if (isPanelElement){
//            Form form = getForm();
//            if (form instanceof EditForm) {
//                ImagesTab imgTab = ((EditForm) form).getResourceTab().getImgTab();
//                Project.ResourceToHold holder = Project.getLoadableResource(panelElement.getBackground());
//                if (activeShapes.size == 0) { // jeigu nieko ner
//                    if (!needsPanelElementFlush) { // jeigu blokas yra physics panelej. pasalinam. jeigu ner nieko nedarom
//                        imgTab.removePhysicsBlock(panelElement);
//                        needsPanelElementFlush = true; // del viso pikto jei kartais pasikeis situacija, kad zinotu, kad nebera paneli.
//                    }
//                    if (holder != null){
//                        holder.shapes.clear(); // isvalom, nieko neliko.
//                    }
//                } else{
//                    if (needsPanelElementFlush) { // vadinas blokas dar neidetas i imagesTab, nors jo reikia.
//                        imgTab.addPhysicsBlock(panelElement);
//                        needsPanelElementFlush = false;
//                    }
////                    panelElement.shapes.clear(); // pridedam nustatytas shapes.
////                    panelElement.shapes.addAll(activeShapes);
//                    PhysicsHolder physicsHolder = panelElement.physicsHolder;
//                    physicsHolder.shapes.clear();
//                    physicsHolder.shapes.addAll(activeShapes);
//                    if (holder != null){
//                        holder.shapes.clear();
//                        holder.shapes.addAll(activeShapes);
//                    }
//                }
//            }else {
//                p.setError("PhysicsEditor must be added to EditorForm", ErrorMenu.ErrorType.ControlsError);
//            }
//        }else { // editorio element. Nera skirtumo ar visas istryne ar ne.
//            Array<FixtureShapeHolder> realShapes = editorElement.getShapes();
        ArrayList<FixtureShapeHolder> realShapes = element.shapes;
        // jei dydziai nesutampa, tai akivaizdu, kad neidentiskos shapes.
        boolean identical = realShapes.size() == activeShapes.size; // jeigu dydziai  vienodi, tai gali but, kad shapes identiskos.
        if (realShapes.size() == activeShapes.size){
            for (int a = 0; a < realShapes.size(); a++){
                FixtureShapeHolder realShape = realShapes.get(a);
                FixtureShapeHolder activeShape = activeShapes.get(a);
                if (realShape.equals(activeShape)){
                    continue;
                }

                identical = false;
                break;
            }
        }
        if (!identical) {
            // shapes neatitinka. metam naujas ir kvieciam save.
            realShapes.clear(); // isvalom, su dingusiom turetu but susitvarke activeShapes, nes realiai vistiek tas pacias instance ima.
//        realShapes.addAll(activeShapes); // idedam naujas.
            for (FixtureShapeHolder e : activeShapes) {
                realShapes.add(e);
            }

            Project.save(); // butinai reik. Gi pasikeite fizikos!
        }
//        }
    }

//    /** drawable, kuria reik keist. Cia is paneles ir kelsis viskas i nauja connected bloka. */
//    public void show(ImagesTab.ConnectedBlock block){ // atsidaro.
//        Drawable e = block.getBackground();
////        mainPanel.openBodyPanelButton.setVisible(false);
//        if (e == null){
//            p.setError("PhysicsEditor: Value of Drawable cannot be zero.", ErrorMenu.ErrorType.WrongPara);
//            return;
//        }
//        resource.setAutoSizing(true);
//        resource.setImage(e);
////        offsetX = offsetY = 0;
//        zoom = 1;
//        widthRatio = heightRatio = 1f;
//        rwidth = e.getMinWidth();
//        rheight = e.getMinHeight();
//        resource.updated();
//        resource.placeInMiddle(getWidth()/2, getHeight()/2);
////        activeShapes.clear();
////        activeShapes.addAll(resource.getShapes());
//        isPanelElement = true;
//        Form form = (Form) Engine.getInstance().getActiveForm(); // apejimas senos sistemos kai is pop up forma imdavo.
//        if (form instanceof EditForm){
////            ListView phcs = ((EditForm) form).getResourcesTabs(4);
//            ListView phcs = block.getPhysicsTab();
//            List<Interface> controls = phcs.getHost().getControls(); // is physics tab
//            boolean needCreate = true;
//            for (int a = 0; a < controls.size(); a++){ // per visas esancias kontroles, bet tik ConnectedBlock imsim
//                Interface work = controls.get(a);
//                if (work instanceof ImagesTab.ConnectedBlock){ // jeigu buten connected blokas
//                    if (e == ((ImagesTab.ConnectedBlock) work).getBackground()){ // jeigu blokas turi ta pati drawable, reisk tai reikiamas blokas.
//                        // radom, jau egzistuoja shapes, kopinam visas
//                        panelElement = (ImagesTab.ConnectedBlock) work;
//                        needsPanelElementFlush = false;
//                        PhysicsHolder physicsHolder = panelElement.physicsHolder;
//                        activeShapes.addAll(physicsHolder.shapes);
//                        needCreate = false;
//                    }
//                }
//            }
//            if (needCreate){ // blokas neegzistuoja.
//                ImagesTab img = ((EditForm) form).getResourceTab().getImgTab();
//                Block.BlockStyle style = new Block.BlockStyle();
//                style.allowSuperSelect = true;
//                style.background = e;
//                panelElement = img.new ConnectedBlock(style, -5, block.resource); // bug fix kai pamesdavo savo resource.
//                needsPanelElementFlush = true; // sitas tuo atveju jei reik sukurt nauja. Sena vistiek gales istrint.
//            }
//        }else {
//            p.setError("PhysicsEditor cannot find EditForm", ErrorMenu.ErrorType.ControlsError);
//            return;
//        }
//        updateShapesList();
//        open();
//    }

    /** Kelsis tiesiogiai i resource. */
    public void show(Element e){
//        mainPanel.openBodyPanelButton.setVisible(true);
        if (e == null){
            p.setError("PhysicsEditor: Value of Resource cannot be zero.", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        resource.setAutoSizing(false);
//        resource.setImage(e.getImage());
        resource.setImageNoCopy(e.getImage()); // turim det image be kopijos!
        resource.setSize(e.getWidth(), e.getHeight());
        rwidth = e.getWidth();
        rheight = e.getHeight();
        widthRatio = e.getWidth() / e.getImage().getMinWidth();
        heightRatio = e.getHeight() / e.getImage().getMinHeight();
//        offsetX = offsetY = 0;
        zoom = 1;
        resource.updated();
        resource.placeInMiddle(getWidth()/2, getHeight()/2);

        PhysicsHolder holder = e.getPhysicsHolder();
//        activeShapes.addAll(resource.getShapes());
        activeShapes.clear(); // kaip ir is seniau like, tai negalim mest i pool.
//        activeShapes.addAll(holder.shapes);
        for (FixtureShapeHolder fixture : holder.shapes){
            activeShapes.add(fixture);
        }
//        isPanelElement = false;
//        editorElement = e;
        element = holder;
//        updateShapesList();
        mainPanel.updateList();

        // dar paziurim del paste mygtuko.
        mainPanel.checkPasteAvailability();

        open();
    }

    /* override */

    @Override
    public void onClose() {
        flushShapes(); // idedam sukurtas shape i elementa.
        clear(); // isvalom pati editoriu.
        super.onClose(); // cia listener kvietimas.
    }

    @Override
    protected void drawInterfaces(boolean isAbsoluteDraw) {
        if (!isAbsoluteDraw) { // tik fixed.
            Vector2 pos = getPosition();
            if (p.pushScissor(pos.x, pos.y, getWidth(), getHeight())) {
                super.drawInterfaces(false);
                // visos jau egzistuojancios shape.
                if (p.pushScissor(pos.x, pos.y, getWidth() * 0.8f, getHeight())) { // kad nepiestu ant paneles
                    int color = 35;
                    int a = 0;
                    for (FixtureShapeHolder e : activeShapes) {
                        float stroke = a == mainPanel.selectedIndex ? 6f : 3f;
                        drawShape(e, GdxWrapper.color(color, (color * 500) % 255, 255 - color), true, stroke, false);
                        color += 40;
                        if (color > 255)
                            color -= 255;
                        a++;
                    }
                    // dabar redaguojama.
                    drawShape(currentShape, 0, isDone, 4.5f, true);

                    if (movingState == -1){ // origin point.
//                        Vector2 origin = editorElement.getBodyOrigin();
                        Vector2 origin = element.bodyOrigin;
                        Vector2 rpos = resource.getPosition();
                        float nx = rpos.x + origin.x * widthRatio * zoom;
                        float ny = rpos.y + origin.y * heightRatio * zoom;
                        float size = 20f * zoom;
                        originPoint.draw(p.getBatch(), nx - size/2, ny - size/2, size, size); // gausi per viduri.
                    }

                    p.popScissor();
                }
                p.popScissor();
            }
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (!isActive()){
            return false;
        }
        Vector2 pos = getPosition();
        Vector3 tr = p.screenToFixedCoords(x, y);
        if (tr.x > pos.x && tr.x < pos.x + getWidth()*0.8f && tr.y > pos.y && tr.y < pos.y + getHeight()){ // iki paneles.
            startX = tr.x;
            startY = tr.y;
            isPressed = true;
            if (movingState > 0){
                Vector2 res = resource.getPosition();
                for (int a = 0; a < currentShape.x.size(); a++){
                    float size = 3f * zoom;
                    float zoomx = zoom*widthRatio, zoomy = zoom*heightRatio;
                    if (tr.x > res.x + currentShape.x.get(a)*zoomx - size/2 && tr.x < res.x + currentShape.x.get(a)*zoomx + size/2 &&
                            tr.y > res.y + currentShape.y.get(a)*zoomy - size/2 && tr.y < res.y + currentShape.y.get(a)*zoomy + size/2){
                        editPoint = a;
                        oldX = currentShape.x.get(a);
                        oldY = currentShape.y.get(a);
                        isNewPoint = false;

                        editPanel.updateCordLocation(); // atnaujinam cord lokacija.
                        return true;
                    }
                }
                float cx = (tr.x - res.x) / (zoom*widthRatio);
                float cy = (tr.y - res.y) / (zoom*heightRatio);
                if (currentShape.x.size() == 0){
                    currentShape.type = movingState-1;
                    editPanel.show(movingState, currentShape); // perjungimas i kita panele.
                    mainPanel.setVisible(false);
                    mainPanel.selectedIndex = -1;
                }else if (movingState == 2){
                    isNewPoint = false;
                    oldX = currentShape.radius;
                    oldY = 99999;
                    return true; // tik vienas taskas.
                }else if (movingState == 4){
                    if (currentShape.x.size() == 2){ // daugiau 2 tasku but negali.
                        isNewPoint = false;
                        editPoint = -1; // kad nejudintu be reikalo ir nenesiotu.

                        editPanel.updateCordLocation(); // atnaujinam cord lokacija.
                        return true;
                    }
                }
                if (!isDone) {
                    currentShape.x.add(cx); // skaiciuojas nuo image cord.
                    currentShape.y.add(cy);
                    editPoint = currentShape.x.size() - 1;
                    isNewPoint = true;
                    if (movingState == 2)
                        currentShape.radius = resource.getHeight()/zoom;

                    editPanel.updateCordLocation(); // atnaujinam cord lokacija.
//                    editPanel.update(cx, cy, PhysicsEditorPanel2.POINT, editPoint);
                }else {
                    editPoint = -1;

                    editPanel.updateCordLocation(); // atnaujinam cord lokacija.
                }
            }else if (movingState == -1){
                bodyPanel.updateOriginLocation(tr.x, tr.y, widthRatio, heightRatio, zoom);
            }
            return true;
        }
        return super.touchDown(x, y, pointer, button);
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (isPressed){
            Vector3 tr = p.screenToFixedCoords(x, y);
            Vector2 pos = resource.getPosition();
            switch (movingState){
                case -1:
                    bodyPanel.updateOriginLocation(tr.x, tr.y, widthRatio, heightRatio, zoom);
                    break;
                case 0:
//                    offsetX += x - startX;
//                    offsetY += y - startY;
                    resource.setPosition(pos.x + (tr.x - startX) , pos.y + (tr.y - startY));
//                    offsetX += tr.x - startX;
//                    offsetY += tr.y - startY;
                    startX = tr.x;
                    startY = tr.y;
                    break;
                case 1: // polygon.
                case 3: // chain shape. Viskas tinka.
                case 4:
                    if (editPoint >= 0) {
                        int last = editPoint;
                        currentShape.x.set(last, (tr.x - pos.x) / (zoom*widthRatio)); // skaiciuojas nuo image cord.
                        currentShape.y.set(last, (tr.y - pos.y) / (zoom*heightRatio));

                        editPanel.updateCordLocation(); // atnaujinam cord lokacija.
                    }
                    break;
                case 2: // ellipse.
                    if (oldY == 99999) {
                        float cx = currentShape.x.get(0) + pos.x; // daugiau vieno ner.
                        float cy = currentShape.y.get(0) + pos.y;
                        float dist1 = MoreUtils.dist(cx, cy, startX, startY);
                        float dist2 = MoreUtils.dist(cx, cy, tr.x, tr.y);
                        currentShape.radius += dist2 - dist1;
                        startX = tr.x;
                        startY = tr.y;
                    }else {
                        currentShape.x.set(0, (tr.x - pos.x) / (zoom*widthRatio)); // skaiciuojas nuo image cord.
                        currentShape.y.set(0, (tr.y - pos.y) / (zoom*heightRatio));

                        editPanel.updateCordLocation(); // atnaujinam cord lokacija.
                    }
                    break;
            }
            return true;
        }
        return super.pan(x, y, deltaX, deltaY);
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (isPressed){
            Vector3 tr = p.screenToFixedCoords(x, y);
            Vector2 pos = resource.getPosition();
            if (movingState > 0 && editPoint >= 0) {
                if (isNewPoint){
                    editPanel.update((tr.x - pos.x) / (zoom*widthRatio), (tr.y - pos.y) / (zoom*heightRatio),
                            PhysicsEditorPanel2.POINT, editPoint);
                }else {
                    editPanel.update(oldX, oldY, // senoji buvimo vieta.
                            PhysicsEditorPanel2.MOVED, editPoint);
                }
            }
            isPressed = false;
            return true;
        }
        return super.panStop(x, y, pointer, button);
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (isPressed){
            if (movingState > 0 && movingState != 2){
                if (movingState == 1 && editPoint == 0 && currentShape.x.size() >= 3) { // tik polygone gali pabaiga uzdet.
                    isDone = true;
                    editPanel.update(0, 0, PhysicsEditorPanel2.END, -1);
                }else if (isNewPoint){
                    Vector3 tr = p.screenToFixedCoords(x, y);
                    Vector2 pos = resource.getPosition();
                    editPanel.update((tr.x - pos.x) / (zoom*widthRatio), (tr.y - pos.y) / (zoom*heightRatio),
                            PhysicsEditorPanel2.POINT, editPoint);
                }
            }
            isPressed = false;
            return true;
        }
        return super.tap(x, y, count, button);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (super.scrolled(amountX, amountY)){
            return true;
        }
        zoom -= amountY/zoomSensitivity; // Only using vertical scroll variable. For now...
        resource.setSize(rwidth * zoom, rheight*zoom);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (super.keyUp(keycode)){
            return true;
        }
        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)
                && keycode == Input.Keys.ESCAPE){
            if (mainPanel.isVisible()) {
                close();
                return true;
            }else if (bodyPanel.isVisible()){
                exitBodyPanel();
                return true;
            }else
                return false;
        }else if (spaceActive && keycode == Input.Keys.SPACE){
            if (oldState != -1) {
                if (mainPanel.isVisible()) {
                    mainPanel.changeState(oldState);
                } else {
                    editPanel.enableDragging(false);
                }
            }
            spaceActive = false;
            movingState = oldState;
            if (isPressed)
                isPressed = false;
            return true;
        }else if (keycode == Input.Keys.FORWARD_DEL && movingState != 2){ // neleis trint rutulio
            if (editPoint >= 0){
                float x, y;
                x = currentShape.x.get(editPoint);
                y = currentShape.y.get(editPoint);
//                currentShape.x.removeIndex(editPoint);
//                currentShape.y.removeIndex(editPoint);
                currentShape.x.remove(editPoint);
                currentShape.y.remove(editPoint);
                editPanel.update(x, y, PhysicsEditorPanel2.DELETED, editPoint);
                editPoint = -1;
                editPanel.updateCordLocation(); // atnaujinam cord lokacija.
                if (isDone && currentShape.x.size() < 3){ // jeigu lieka maziau nei 3 taskai, figura negali but uzbaigta.
                    isDone = false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (super.keyDown(keycode)){
            return true;
        }
        if (keycode == Input.Keys.SPACE && movingState != 0 && !isPressed){ // jeigu dragina keist nereik.
            oldState = movingState;
            movingState = 0;
            if (oldState != -1) {
                if (mainPanel.isVisible()) {
                    mainPanel.changeState(movingState);
                } else {
                    // editor panel...
                    if (editPanel.isDragging()) {
                        return false;
                    }
                    editPanel.enableDragging(true);
                }
            }
            spaceActive = true;
            return true;
        }
        return false;
    }

    public static class FixtureShapeHolder implements Pool.Poolable, Serializable {
        /** kg/m^2. It's mass. */
        public float density = 0;
        /** [0, 1], sliding other body */
        public float friction = 0.2f;
        /** [0, 1]. bouncing */
        public float restitution = 0;
        public boolean isSensor = false;
        /** The collision category bits. Normally you would just set one bit. */
        public short categoryBits = 0x0001;

        /** The collision mask bits. This states the categories that this shape would accept for collision. */
        public short maskBits = -1;

        /** Collision groups allow a certain group of objects to never collide (negative) or always collide (positive). Zero means no
         * collision group. Non-zero group filtering always wins against the mask bits. */
        public short groupIndex = 0;


        public ArrayList<Float> x = new ArrayList<>(), y = new ArrayList<>();
        // elipsem.
        public float radius;
        /**
        0 - polygon
        1 - circle
        2 - chain
        3 - edge
         */
        public int type;

        public FixtureShapeHolder(){}

        /** @param copy object to copy form. This FixtureShapeHolder will copy all attributes from given object. */
        public FixtureShapeHolder(FixtureShapeHolder copy){
            copy(copy);
        }

        public FixtureShapeHolder copy(FixtureShapeHolder copy){
            x.clear(); // pries tai gi isvalyt reik.
            y.clear();
            x.addAll(copy.x);
            y.addAll(copy.y);
            radius = copy.radius;
            type = copy.type;
            density = copy.density;
            friction = copy.friction;
            restitution = copy.restitution;
            maskBits = copy.maskBits;
            categoryBits = copy.categoryBits;
            groupIndex = copy.groupIndex;
            isSensor = copy.isSensor;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this){
                return true;
            }else if (o instanceof FixtureShapeHolder) {
                FixtureShapeHolder shape = (FixtureShapeHolder) o;
                // toliau tikrinam viska is eiles.
                if (shape.type != type){
                    return false;
                }

                if (x.size() != shape.x.size() || y.size() != shape.y.size()){
                    return false;
                }

                if (shape.density != density){
                    return false;
                }

                if (shape.friction != friction){
                    return false;
                }

                if (shape.restitution != restitution){
                    return false;
                }

                if (shape.isSensor != isSensor){
                    return false;
                }

                if (shape.categoryBits != categoryBits){
                    return false;
                }

                if (shape.maskBits != maskBits){
                    return false;
                }

                if (shape.groupIndex != groupIndex){
                    return false;
                }

                if (type == 1){
                    if (shape.radius != radius){
                        return false;
                    }
                }

                // tikrinam taskus.
                // tasku masyvu dydziai vienodi.
                for (int a = 0; a < x.size() && a < y.size(); a++){
                    float x = this.x.get(a);
                    float y = this.y.get(a);
                    float cx = shape.x.get(a);
                    float cy = shape.y.get(a);
                    if (x != cx || y != cy){
                        return false;
                    }
                }

                // identiskos.
                return true;
            }else {
                return false;
            }
        }

        @Override
        public void reset() {
            x.clear();
            y.clear();
            radius = 0;
            type = 0;
            density = 0;
            friction = 0.2f;
            restitution = 0;
            isSensor = false;
            categoryBits = 0x0001;
            maskBits = -1;
            groupIndex = 0;
        }
    }
}
