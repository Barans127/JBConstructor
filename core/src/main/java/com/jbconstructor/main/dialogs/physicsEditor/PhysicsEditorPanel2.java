package com.jbconstructor.main.dialogs.physicsEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.views.Panel;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.CheckBox;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.controls.widgets.TextBox;
import com.engine.interfaces.listeners.ClickListener;
import com.engine.interfaces.listeners.FocusListener;
import com.jbconstructor.main.managers.Project;

/**
 * control panel. This panel is responsible for shapes editing.
 */
/* Panelė kuri redaguoja formas ir jas kuria. Deploy atveju turėtų sukurt phcs tabe naujas struktūras ar redaguot senas. */

class PhysicsEditorPanel2 extends Panel {
    private PhysicsEditor editor;
    private CheckBox dragger;
    // undo, redo, end, status, discard.
    private Button[] buttons;

    // veikimas
    private int currentState;
    private PhysicsEditor.FixtureShapeHolder shape;
//    private ArrayList<Float> redox, redoy;

    // undo
    private Pool<ActionEntry> undoPool;
    private Array<ActionEntry> redos, undos;
    public static final int POINT = 0, END = 1, MOVED = 2, DELETED = 3;

    // deploy
    private TextureRegion deployStatus;
    private boolean isReadyDeploy;

    // papildomi
    private Label shapeName;
    private TextBox xBox, yBox; // jie dar nieko nedaro.

    PhysicsEditorPanel2(PhysicsEditor editor){
//        resource = res;
//        this.editor = editor;
        buttons = new Button[5];
        undoPool = new Pool<ActionEntry>() {
            @Override
            protected ActionEntry newObject() {
                return new ActionEntry();
            }
        };
        redos = new Array<>();
        undos = new Array<>();
//        redox = new ArrayList<>();
//        redoy = new ArrayList<>();
        this.editor = editor;
        float wid = editor.getWidth() * 0.2f;
        setSize(wid, editor.getHeight()); // 20% viso physicsEditor dydzio.
        setPosition(wid * 4, 0);
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        setBackground(Resources.getDrawable("halfWhiteColor"));
        tintBackground(0xFFDAA520);
        { // buttonai.
            // undo redo
//            Resources.addImage("editorUndoRedo", "resources/constructorUI/physicsEditor/controlPanel/undoredo.png");
            //undo
            Button.ButtonStyle bst = new Button.ButtonStyle();
            bst.background = new TextureRegionDrawable(new TextureRegion(Resources.getTexture("editorUndoRedo"), 0, 0, 0.5f, 1f));
            bst.autoSize = false;
            bst.width = bst.height = editor.getHeight() / 9;

            Button undo = new Button(bst);
            undo.placeInMiddle(wid/4, editor.getHeight()/9*8);
            undo.setText("");
            undo.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    undo();
                }
            });
            undo.setEnabled(false);
            addControl(buttons[0] = undo);

            // redo
            bst.background = new TextureRegionDrawable(new TextureRegion(Resources.getTexture("editorUndoRedo"), 0.5f, 0, 1f, 1f));

            final Button redo = new Button(bst);
            redo.placeInMiddle(wid/4*3, editor.getHeight()/9*8);
            redo.setText("");
            redo.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    redo();
                }
            });
            redo.setEnabled(false);
            addControl(buttons[1] = redo);

            // moving box
//            Resources.addImage("allWayArrow", "resources/constructorUI/physicsEditor/mainPanel/allWayArrow.png");
//            Resources.addImage("boxSystemStroke", "resources/ui/box.png");
            CheckBox.CheckBoxStyle cst = new CheckBox.CheckBoxStyle();
            cst.box = Resources.getTextureDrawable("allWayArrow");
            cst.checkedBox = new PhysicsEditorPanel.CustomTextureRegionDrawable(
                    ((SpriteDrawable)Resources.getDrawable("defaultCheckBox")).getSprite(), (TextureRegionDrawable) cst.box);
            cst.autoSize = false;
            cst.width = cst.height = editor.getHeight() / 9;

            CheckBox move = new CheckBox(cst);
            move.setText("");
            move.placeInMiddle(wid/2, editor.getHeight()/9*7);
            move.setCheckListener(new CheckBox.CheckedListener() {
                @Override
                public void onCheck(boolean checked) {
                    PhysicsEditorPanel2.this.editor.changeControlState(checked ? 0 : currentState);
                }
            });
            addControl(dragger = move);

            // box checkbox???
            // finish button
//            Resources.addImage("editorEndButton", "resources/constructorUI/physicsEditor/controlPanel/endButton.png");
            bst.background = Resources.getTextureDrawable("editorEndButton");

            Button end = new Button(bst);
            end.placeInMiddle(wid/4, editor.getHeight()/9*6);
            end.setText("");
            end.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    finish();
                }
            });
            addControl(buttons[2] = end);

            // status button
//            Resources.addImage("editorIndicationButton", "resources/constructorUI/physicsEditor/controlPanel/indication.png");
            deployStatus = new TextureRegion(Resources.getTexture("editorIndicationButton"), 0, 0, 0.5f, 1f);
            bst.background = new TextureRegionDrawable(deployStatus);

            Button status = new Button(bst);
            status.placeInMiddle(wid/4*3, editor.getHeight()/9*6);
            status.setText("");
            status.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    deploy();
                }
            });
            addControl(buttons[3] = status);

            // discard
//            Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
//            bst.background = Resources.getDrawable("halfWhiteColor");
//            bst.autoSize = true;
//            final Button discard = new Button("DISCARD", bst);
//            discard.placeInMiddle(wid/2, editor.getHeight()/9*4);
//            discard.setClickListener(new ClickListener() {
//                @Override
//                public void onClick() {
//                    discard();
//                }
//            });
//            addControl(buttons[4] = discard);
        }

        SymbolButton.SymbolButtonStyle sbt = new SymbolButton.SymbolButtonStyle();
        sbt.background = Resources.getDrawable("halfWhiteColor");
        sbt.symbol = Resources.getTextureDrawable("additionalPanelDownListKey");
        sbt.normalColor = 0x00000000;
        sbt.onColor = 0xFFFF5500;
        sbt.pressedColor = 0xFFAA5500;
        sbt.rotatable = true;
        sbt.angle = 90f;
        sbt.y = editor.getHeight()/9*4.5f;
        SymbolButton body = new SymbolButton(sbt);
        body.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                PhysicsEditorPanel2.this.editor.fixturePanel.show(shape);
            }
        });
        body.auto();
        body.setOriginMiddle();
        addControl(body);

        // per naujo perdaryts discard mygtukas. grazesnis.
        sbt.symbol = Resources.getDrawable("defaultColorPickCross");
        sbt.symbolWidth = 40;
        sbt.symbolHeight = 40;
        sbt.x = 22;
        sbt.y = 162;
        sbt.height = 40;
        sbt.normalColor = 0xffff4444;
        sbt.onColor = 0xffff0000;
        sbt.pressedColor = 0xff992222;
        sbt.text = "DISCARD";
        sbt.rotatable = false;
        sbt.autoSize = false;
        sbt.width = 195;
//        sbt.height = 40;

        // discard.
        SymbolButton discard = new SymbolButton(sbt);
        discard.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                discard();
            }
        });
        addControl(discard);

        // labeliai point taskui. ir shape vardo.
        // point location.
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.autoSize = false;
        lst.width = editor.getWidth();
        lst.height = 40;
        lst.y = 276;
        lst.text = "Point location";
        lst.verticalAlign = Align.center;

        addControl(lst.createInterface());

        // x ir y labeliai
        //x
        lst.text = "x:";
        lst.width = 26;
        lst.x = 5;
        lst.y = 226;

        addControl(lst.createInterface());

        // y
        lst.text = "y:";
        lst.x = 127;

        addControl(lst.createInterface());

        // shape pavadinimo label
        lst.x = 78;
        lst.y = 341;
        lst.text = "Shape type";
        lst.width = 165;

        Label shapeType = new Label(lst);
        addControl(shapeType);
        shapeName = shapeType;

        // x ir y text boxai.
        // x text box.
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.autoSize = false;
        tst.verticalAlign = Align.center;
        tst.horizontalAlign = Align.center;
        tst.x = 39;
        tst.y = 226;
        tst.width = 80;
        tst.height = 40;

        TextBox xBox = new TextBox(tst);
        addControl(xBox);

        // y text box
        tst.x = 155;

        TextBox yBox = new TextBox(tst);
        addControl(yBox);
        this.xBox = xBox;
        this.yBox = yBox;

        // nustatom listenerius.
        TextBoxChangedListener listener = new TextBoxChangedListener();
        xBox.setFocusListener(listener);
        yBox.setFocusListener(listener);
    }

    void show(int state, PhysicsEditor.FixtureShapeHolder shape){
        setVisible(true);
        currentState = state;
        this.shape = shape;
        buttons[2].setEnabled(currentState == 1);
        buttons[0].setEnabled(false);
        buttons[1].setEnabled(false);
        dragger.setChecked(false);
        isReadyDeploy = false;

        // shape vardas.
        String name = null;
        switch (shape.type){
            case PhysicsEditor.POLYGON:
                name = "Polygon shape";
                break;
            case PhysicsEditor.EDGE:
                name = "Edge shape";
                break;
            case PhysicsEditor.CHAIN:
                name = "Chain shape";
                break;
            case PhysicsEditor.CIRCLE:
                name = "Circle shape";
                break;
        }
        shapeName.setText(name);

        checkDeployStatus();
    }

    void enableDragging(boolean e){
        dragger.setChecked(e);
    }

    void updateCordLocation(){
        int index = editor.editPoint;
        if (index > -1){
            xBox.setText(MoreUtils.roundFloat(shape.x.get(index), 2) + "");
            yBox.setText(MoreUtils.roundFloat(shape.y.get(index), 2) + "");
        }else {
            xBox.setText("");
            yBox.setText("");
        }
    }

    // kai ka nors padaro. pranes, kad kazkas pasikeite.
    void update(float x, float y, int action, int id){
//        redox.clear();// isvalys redo. kol kas tik su polygon tinka.
//        redoy.clear();
        if (redos.size > 0){
            undoPool.freeAll(redos);
            redos.clear();
        }
        ActionEntry e = undoPool.obtain();
        e.x = x;
        e.y = y;
        e.action = action;
        e.id = id;
        undos.add(e);
        buttons[1].setEnabled(false);
        buttons[0].setEnabled(true);
        checkDeployStatus();
    }

    boolean isDragging(){
        return dragger.isChecked();
    }

    private void undo(){
        if (undos.size == 0){ // BUG FIX, luzdavo jei size 0. Akivaizdziai...
            return; // negalim daugiau undo daryt.
        }
        int last = undos.size - 1;
        ActionEntry current = undos.get(last);
        undos.removeIndex(last);
        redos.add(current);
        switch (currentState){ // 0 niekada nebus
            case 1:
            case 3:
            case 4:
                switch (current.action){
                    case POINT:
                        if (shape.x.size() == 0){
                            break;
                        }
                        int id = shape.x.size() - 1;
                        current.x = shape.x.get(id);
                        current.y = shape.y.get(id);
//                        shape.x.removeIndex(id);
//                        shape.y.removeIndex(id);
                        shape.x.remove(id);
                        shape.y.remove(id);
                        if (editor.hasEnd() && shape.x.size() < 3){
                            editor.setEnd(false);
                        }

                        editor.editPoint = -1;
                        updateCordLocation();
                        break;
                    case END:
                        editor.setEnd(!editor.hasEnd());
                        break;
                    case MOVED:
                        float x, y;
                        if (shape.x.size() == 0){
                            break;
                        }
                        x = shape.x.get(current.id);
                        y = shape.y.get(current.id);
                        shape.x.set(current.id, current.x);
                        shape.y.set(current.id, current.y);
                        current.x = x;
                        current.y = y;

                        updateCordLocation();
                        break;
                    case DELETED: // grazins atgal.
//                        shape.x.insert(current.id, current.x);
//                        shape.y.insert(current.id, current.y);
                        shape.x.add(current.id, current.x);
                        shape.y.add(current.id, current.y);

                        editor.editPoint = -1;
                        updateCordLocation();
                        break;
                }
//                if (shape.x.size() == 1){ // paskutinio nelies.
//                    buttons[0].setEnabled(false);
//                }
                break;
            case 2:
                switch (current.action){
                    case POINT:
                        buttons[0].setEnabled(false);
                        break;
                    case MOVED:
                        if (current.y == 99999){
                            float old = shape.radius;
                            shape.radius = current.x;
                            current.x = old;
                        }else {
                            float x, y;
                            x = shape.x.get(0);
                            y = shape.y.get(0);
                            shape.x.set(0, current.x);
                            shape.y.set(0, current.y);
                            current.x = x;
                            current.y = y;

                            updateCordLocation();
                        }
                        break;
                    case DELETED:
//                        shape.x.insert(current.id, current.x);
//                        shape.y.insert(current.id, current.y);
                        shape.x.add(current.id, current.x);
                        shape.y.add(current.id, current.y);

                        editor.editPoint = -1;
                        updateCordLocation();
                        break;
                }
                break;
        }
        buttons[1].setEnabled(true);
        if (undos.size == 0){
            buttons[0].setEnabled(false);
        }
        checkDeployStatus();
    }

    private void redo(){
        if (redos.size == 0){ // ner kur grizt..
            buttons[1].setEnabled(false);
            return;
        }
        ActionEntry e = redos.get(redos.size - 1);
        redos.removeIndex(redos.size-1);
        switch (currentState){
            case 1:
            case 3:
            case 4:
                switch (e.action){
                    case POINT:
                        shape.x.add(e.x);
                        shape.y.add(e.y);

                        editor.editPoint = -1;
                        updateCordLocation();
                        break;
                    case MOVED:
                        float x, y;
                        x = shape.x.get(e.id);
                        y = shape.y.get(e.id);
                        shape.x.set(e.id, e.x);
                        shape.y.set(e.id, e.y);
                        e.x = x;
                        e.y = y;

                        updateCordLocation();
                        break;
                    case END:
                        editor.setEnd(!editor.hasEnd());
                        break;
                    case DELETED: // istrint.
//                        shape.x.removeIndex(e.id);
//                        shape.y.removeIndex(e.id);
                        shape.x.remove(e.id);
                        shape.y.remove(e.id);
                        if (editor.hasEnd() && shape.x.size() < 3){
                            editor.setEnd(false);
                        }

                        editor.editPoint = -1;
                        updateCordLocation();
                        break;
                }
                break;
            case 2:
                switch (e.action){
                    case POINT:
                        break;
                    case MOVED:
                        if (e.y == 99999){
                            float old = shape.radius;
                            shape.radius = e.x;
                            e.x = old;
                        }else {
                            float x, y;
                            x = shape.x.get(0);
                            y = shape.y.get(0);
                            shape.x.set(0, e.x);
                            shape.y.set(0, e.y);
                            e.x = x;
                            e.y = y;
                            updateCordLocation();
                        }
                        break;
                    case DELETED:
//                        shape.x.removeIndex(e.id);
//                        shape.y.removeIndex(e.id);
                        shape.x.remove(e.id);
                        shape.y.remove(e.id);

                        editor.editPoint = -1;
                        updateCordLocation();
                        break;
                }
                break;
        }
        if (redos.size == 0){ // neber kur, redo mygtuko blokavimas.
            buttons[1].setEnabled(false);
        }
        undos.add(e);
        buttons[0].setEnabled(true);
        checkDeployStatus();
    }

    private void finish(){
        if (currentState == 1){ // apsidraudimui.
            if (shape.x.size() >= 3){ // minimum 3 taskai
                editor.setEnd(!editor.hasEnd());
                update(0, 0, END, -1);
            }
        }
    }

    private void discard(){
//        shape.x.clear();
//        shape.y.clear();
//        shape.radius = 0;
//        shape.type = 0;
        shape.reset();
        shape = null;
        editor.returnEditableShape(); // jeigu tai buvo editinama.
        editor.exitEditPanel();
        editor.setEnd(false); // kad nebugintu veliau, nenusiresetina.
        undoPool.freeAll(undos); // visi veiksmai pasalinami.
        undos.clear();
        undoPool.freeAll(redos);
        redos.clear();
    }

    private void deploy(){
        if (isReadyDeploy){
            editor.deployShape();
            editor.exitEditPanel();
            editor.setEnd(false);
            undoPool.freeAll(undos); // visi veiksmai pasalinami.
            undos.clear();
            undoPool.freeAll(redos);
            redos.clear();

            Project.getSaveManager().triggerSave(); // idejo naujau fixtura, todel reiktu autosave daryt...
        }
    }

    private void checkDeployStatus(){
        if (shape.x.size() == 0){
            isReadyDeploy = false;
            deployStatus.setRegion(0.5f, 0, 1f, 1f);
            return;
        }
        switch (currentState){
            case 1:
                if (shape.x.size() < 3 || !editor.hasEnd()){ // min 3 taskai ir butu baigta figura.
                    isReadyDeploy = false;
                    deployStatus.setRegion(0.5f, 0, 1f, 1f);
                } else {
                    isReadyDeploy = true;
                    deployStatus.setRegion(0f, 0, 0.5f, 1f);
                }
                break;
            case 2:
                if (shape.radius <= 0){ // kad bent rutulys butu su teigiamu radiusiu.
                    isReadyDeploy = false;
                    deployStatus.setRegion(0.5f, 0, 1f, 1f);
                }else {
                    isReadyDeploy = true;
                    deployStatus.setRegion(0f, 0, 0.5f, 1f);
                }
                break;
            case 3:
            case 4:
                if (shape.x.size() < 2){ // maziausiai 2 taskai turi but. edge tik 2.
                    isReadyDeploy = false;
                    deployStatus.setRegion(0.5f, 0, 1f, 1f);
                } else {
                    isReadyDeploy = true;
                    deployStatus.setRegion(0f, 0, 0.5f, 1f);
                }
                break;
        }
    }

    /* override */

    @Override
    // kad butu lengviau.
    public boolean keyDown(int keycode) {
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) { // turi but paspaustas ctrl
            if (keycode == Input.Keys.Z){ // ctrls + shift + z = redo.
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)){
                    redo();
                }else  // ctrl + z = undo.
                    undo();
                return true;
            }
        }
        return super.keyDown(keycode);
    }

    private class ActionEntry implements Pool.Poolable{
        public float x;
        public float y;
        public int action; // koks veiksmas įvyko.
        public int id; // kuris taskas.

        @Override
        public void reset() {
            x = y = action = id = 0;
        }
    }

    private class TextBoxChangedListener implements FocusListener{

        @Override
        public void onLostFocus(Interface e) {
            if (editor.editPoint > -1) { // tik jeigu active taskas prieinamas.
                int index = editor.editPoint;
                if (e == xBox) {
                    // x keitimas
                    float old = shape.x.get(index);
                    float nValue = getValue(xBox.getText().trim(), old);
                    if (nValue == old){
                        xBox.setText(old + "");
                    }else {
                        shape.x.set(index, nValue);
                        // itraukiam i undo lista.
                        update(old, shape.y.get(index), // senoji buvimo vieta.
                                PhysicsEditorPanel2.MOVED, index);
                    }
                } else if (e == yBox) {
                    // y keitimas.
                    float old = shape.y.get(index);
                    float nValue = getValue(yBox.getText().trim(), old);
                    if (nValue == old){
                        yBox.setText(old + "");
                    }else {
                        shape.y.set(index, nValue);
                        // traukiam i undo lista
                        update(shape.x.get(index), old, // senoji buvimo vieta.
                                PhysicsEditorPanel2.MOVED, index);
                    }
                }
            }
        }

        @Override
        public void onFocus(Interface e) {}

        private float getValue(String value, float defaultValue){
            try {
                return Float.parseFloat(value);
            }catch (NumberFormatException ex){
                new AlertToast("Not a number: " + value + "!").show();
                return defaultValue;
            }
        }
    }
}
