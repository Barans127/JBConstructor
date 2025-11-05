package com.jbconstructor.main.dialogs.physicsEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.Resources;
import com.engine.interfaces.controls.dialogs.ConfirmDialog;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.views.Panel;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.CheckBox;
import com.engine.interfaces.controls.widgets.ComboBox;
import com.engine.interfaces.controls.widgets.ImageButton;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.RadioButton;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.listeners.ClickListener;
import com.jbconstructor.main.managers.Project;
import com.jbconstructor.main.root.PhysicsHolder;
import com.jbconstructor.main.root.PhysicsHolderFlavor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/** main panel. This panel let user choose from which shape he wants to create. */
class PhysicsEditorPanel extends Panel implements ClipboardOwner {
//    private final Resource resource;
    private final PhysicsEditor editor;

    // panel
    private RadioButton[] states;
    private ComboBox shapeList;
    private Button edit, delete, paste;

    //info labels
    private Label fixtureCount, choosenForm;
    private String[] statesNames;

    // veiksmingumas
    int selectedIndex = -1;

    // body elementas
//    private SymbolButton openBodyPanelButton;

    // copy paste fizikoms.
    private PhysicsHolderFlavor flavor = new PhysicsHolderFlavor();

    PhysicsEditorPanel(PhysicsEditor editor){
        states = new RadioButton[5];
        statesNames = new String[]{"Move around", "Polygon shape", "Circle shape", "Chain shape", "Edge shape"};
//        resource = res;
        this.editor = editor;
        float wid = editor.getWidth() * 0.2f;
        setSize(wid, editor.getHeight()); // 20% viso physicsEditor dydzio.
        setPosition(wid * 4, 0);
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        setBackground(Resources.getDrawable("halfWhiteColor"));
        tintBackground(0xFFDAA520);

        { // 5 image buttons. laisva valia. rutulis. polygon shape. edge shape. chain shape.
            RadioButton.RadioButtonStyle st = new RadioButton.RadioButtonStyle();
            // all way arrow
//            Resources.addImage("boxSystemStroke", "resources/ui/box.png");
//            Resources.addImage("allWayArrow", "resources/constructorUI/physicsEditor/mainPanel/allWayArrow.png");
            SpriteDrawable box = (SpriteDrawable) Resources.getDrawable("defaultCheckBox");
            TextureRegion allWay = Resources.getTextureDrawable("allWayArrow").getRegion();
//            st.positioning = Window.Position.fixed;
            st.autoSize = false;
//            st.width = wid * 0.9f;
            st.width = st.height = editor.getHeight() / 9;
            st.checkedBox = new CustomTextureRegionDrawable(allWay, box);
            st.box = new TextureRegionDrawable(allWay);
            st.checked = true;
//            st.checked = true;
            RadioButton arrow = new RadioButton(st);
            arrow.setText("");
            arrow.placeInMiddle(wid/4, editor.getHeight() /9 * 8);
            arrow.setCheckListener(new CheckBox.CheckedListener() {
                @Override
                public void onCheck(boolean checked) {
                    setControlState(0);
                }
            });
            addControl(states[0] = arrow);
            // polygonShape
//            Resources.addImage("polygonShape", "resources/constructorUI/physicsEditor/mainPanel/polygonShape.png");
            TextureRegion polygonShape = Resources.getTextureDrawable("polygonShape").getRegion();
            st.checkedBox = new CustomTextureRegionDrawable(polygonShape, box);
            st.box = new TextureRegionDrawable(polygonShape);
            st.checked = false;
            RadioButton polygon = new RadioButton(st);
            polygon.setText("");
            polygon.placeInMiddle(wid/4*3, editor.getHeight() / 9 * 8);
            polygon.setCheckListener(new CheckBox.CheckedListener() {
                @Override
                public void onCheck(boolean checked) {
                    setControlState(1);
                }
            });
            addControl(states[1] = polygon);
            //circle shape
//            Resources.addImage("circleShape", "resources/constructorUI/physicsEditor/mainPanel/circleShape.png");
            TextureRegion circleShape = Resources.getTextureDrawable("circleShape").getRegion();
            st.checkedBox = new CustomTextureRegionDrawable(circleShape, box);
            st.box = new TextureRegionDrawable(circleShape);
            RadioButton circle = new RadioButton(st);
            circle.setText("");
            circle.placeInMiddle(wid/4, editor.getHeight() / 9 * 7);
            circle.setCheckListener(new CheckBox.CheckedListener() {
                @Override
                public void onCheck(boolean checked) {
                    setControlState(2);
                }
            });
            addControl(states[2] = circle);
            //chain shape
//            Resources.addImage("chainShape", "resources/constructorUI/physicsEditor/mainPanel/chainShape.png");
            TextureRegion chainShape = Resources.getTextureDrawable("chainShape").getRegion();
            st.checkedBox = new CustomTextureRegionDrawable(chainShape, box);
            st.box = new TextureRegionDrawable(chainShape);
            RadioButton chain = new RadioButton(st);
            chain.setText("");
            chain.placeInMiddle(wid/4*3, editor.getHeight()/9*7);
            chain.setCheckListener(new CheckBox.CheckedListener() {
                @Override
                public void onCheck(boolean checked) {
                    setControlState(3);
                }
            });
            addControl(states[3] = chain);
            // edge shape
//            Resources.addImage("edgeShape", "resources/constructorUI/physicsEditor/mainPanel/edgeShape.png");
            TextureRegion edgeShape = Resources.getTextureDrawable("edgeShape").getRegion();
            st.checkedBox = new CustomTextureRegionDrawable(edgeShape, box);
            st.box = new TextureRegionDrawable(edgeShape);
            RadioButton edge = new RadioButton(st);
            edge.setText("");
            edge.placeInMiddle(wid/2, editor.getHeight()/9*6);
            edge.setCheckListener(new CheckBox.CheckedListener() {
                @Override
                public void onCheck(boolean checked) {
                    setControlState(4);
                }
            });
            addControl(states[4] = edge);
        }

        // info labeliai.
        // pasirinkta forma.
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.verticalAlign = Align.center;
        lst.textSize = editor.getHeight()/18;
        lst.autoSize = false;
        lst.width = wid;
        lst.height = editor.getHeight()/12;
        Label info = new Label("Move around", lst); // default pavadinimas butinas, nes pradziai ne ta rodo tada.
        info.placeInMiddle(wid/2, editor.getHeight()/9*5);
        addControl(choosenForm = info);
        // visu esamu fixturu skaicius.
        Label allFixtures = new Label("Fixtures: 0", lst);
        allFixtures.placeInMiddle(wid/2, editor.getHeight()/9*4.5f);
        addControl(fixtureCount = allFixtures);

        // combo box su esamom fixturom
//        Resources.addImage("whiteRect", "resources/ui/whiteRect.png");
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        ComboBox.ComboBoxStyle cst = new ComboBox.ComboBoxStyle();
        cst.textSize = editor.getHeight()/18;
        cst.defaultWord = "-none-";
        cst.background = Resources.getDrawable("systemWhiteRect");
        cst.listBackground = Resources.getDrawable("halfWhiteColor");
        cst.normalColor = 0xFF0000FF;
        cst.pressedColor = 0xFFAA0000;
        cst.onColor = 0xFFFF0000;
        cst.autoSize = false;
        cst.width = wid;
        cst.height = editor.getHeight()/18;
        cst.horizontalAlign = Align.center;
        ComboBox fixtures = new ComboBox(cst);
        fixtures.placeInMiddle(0, editor.getHeight()/9*4f);
        fixtures.setIndexChangeListener(new ComboBox.IndexChangeListener() {
            @Override
            public void selectedIndexChanged(int old, int current) {
                selectedIndex = current;
                if (current >= 0){
                    edit.setEnabled(true);
                    delete.setEnabled(true);
                }
            }
        });
//        fixtures.append("Fixture 1", "Fixture 2", "Fixture 3", "Fixture 4");
        addControl(shapeList = fixtures);

        // mygtukas edit. delete
        Button.ButtonStyle bst = new Button.ButtonStyle();
        bst.autoSize = false;
        bst.width = wid/2.1f;
        bst.height = editor.getHeight()/18;
        bst.background = cst.background; // tinka sitas is praeito style.
        bst.normalColor = 0xFF0000FF;
        bst.pressedColor = 0xFFAA0000;
        bst.onColor = 0xFFFF0000;
        bst.textSize = editor.getHeight()/18;
        bst.enabled = false;
        final Button edit = new Button("Edit", bst);
//        edit.auto();
        edit.placeInMiddle(wid/4, editor.getHeight()/9*3.5f);
        edit.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                edit();
            }
        });
        addControl(this.edit = edit);
        Button delete = new Button("Delete", bst);
//        delete.auto();
        delete.placeInMiddle(wid/4*3, editor.getHeight()/9*3.5f);
        delete.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                delete();
            }
        });
        addControl(this.delete = delete);

        // exit button.
//        Resources.addImage("PopUpExitButton", "resources/ui/exit.png");
        ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
        st.background = Resources.getDrawable("defaultPopUpExit");
        st.autoSize = false;
        st.width = st.height = editor.getHeight() / 18;
        st.x = wid - st.width/2;
        st.y = editor.getHeight() - st.height/2;
        st.normalColor = st.pressedColor = st.onColor = 0xFFAAAAAA;
        ImageButton exit = new ImageButton(st);
        exit.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                PhysicsEditorPanel.this.editor.close();
            }
        });
        addControl(exit);

        // more mygtukas
        SymbolButton.SymbolButtonStyle sbt = new SymbolButton.SymbolButtonStyle();
        sbt.background = Resources.getDrawable("halfWhiteColor");
        sbt.symbol = Resources.getTextureDrawable("additionalPanelDownListKey");
        sbt.normalColor = 0x00000000;
        sbt.onColor = 0xFFFF5500;
        sbt.pressedColor = 0xFFAA5500;
        sbt.rotatable = true;
        sbt.angle = 270f;
        sbt.y = editor.getHeight()/9*5.3f;
        SymbolButton body = new SymbolButton(sbt);
        body.auto();
        body.setOriginMiddle();
        body.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                PhysicsEditorPanel.this.editor.enterBodyPanel();
            }
        });
        addControl(body);
//        openBodyPanelButton = body;

        // copy physics mygtukas.
        sbt.symbolWidth = 40;
        sbt.symbolHeight = 40;
        sbt.symbol = Resources.getDrawable("copyIcon");
        sbt.autoSize = false;
        sbt.width = 195;
        sbt.height = 40;
        sbt.x = 24;
        sbt.y = 190;
        sbt.text = "Copy physics";
        sbt.normalColor = 0xff0066ff;
        sbt.rotatable = false;

        Button physics = new SymbolButton(sbt);
        physics.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                copyPhysics();
            }
        });
        addControl(physics);

        // paste physics.
        sbt.y = 145;
        sbt.symbol = Resources.getDrawable("pasteIcon");
        sbt.text = "Paste physics";

        Button pastePhysics = new SymbolButton(sbt);
        pastePhysics.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                pastePhysics();
            }
        });
        addControl(paste = pastePhysics);
    }

    private void setControlState(int state){ // keicia kai pasirenka kaazkuri radiobutton.
        editor.changeControlState(state);
        choosenForm.setText(statesNames[state]);
    }

    void changeState(int state){
        states[state].setChecked(true);
    }

    int checkState(){ // kad tikrai butu uzimetas tinkamas.
        int a = 0;
        for (RadioButton e : states){
            if (e.isChecked()){
                return a;
            }
            a++;
        }
        return a;
    }

    void updateList(){
        Array<PhysicsEditor.FixtureShapeHolder> shapes = editor.getActiveShapes();
        String name = "Fixture";
//        int counter = 1;
        shapeList.clear();
        for (int counter = 0; counter < shapes.size; counter++){
            shapeList.append(name + " " + (counter+1));
//            counter++;
        }
        fixtureCount.setText("Fixtures: " + shapes.size);
        selectedIndex = -1;
        edit.setEnabled(false);
        delete.setEnabled(false);
    }

    void deselectShape(){
        shapeList.setSelectedIndex(-1);
    }

    /* listeners */

    private void edit(){
        if (selectedIndex >= 0){
            editor.editDeployedShape(selectedIndex);
        }
    }

    private void delete(){
        if (selectedIndex >= 0){
            editor.removeDeployedShape(selectedIndex);
        }
    }

    /* copy paste. */

    // kopijuos esamas fizikas. Fizikos privalo turet fixturu.
    private void copyPhysics(){
        // bandom su flavor.
        PhysicsHolder physicsHolder = editor.element;
        if (physicsHolder == null){
            new AlertToast("No physics found!").show();
            return;
        }
        if (physicsHolder.hasShapes()) {// jeigu turi shapes tada ir kopinam.
            // paimam clipboard.
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            // idedam i flavor musu physics.
            flavor.setPhysicsHolder(physicsHolder);
            clipboard.setContents(flavor, this);

            new AlertToast("Physics successfully copied!").show();

            checkPasteAvailability();
        }else {
            new AlertToast("Physics are empty! No fixtures found!").show();
        }
    }

    // bandys pastint. Priklausys nuo situacijos. Fizikos turi but copintos. Jeigu paste daroma ant jau esanciu fiziku (su fixturom), tai galima rinktis is 2 variantu
    // 1) mixint. Vienodos fixturos nebus pridetos. Senos fixturos paliktos, o naujos pridetos.
    // 2) overridint. senos fixturos istrinamos. Naujos pridedamos.
    // body parametrai visada perrasomi kopijos parametrais.
    private void pastePhysics(){
        Clipboard clipboard;
        Transferable clipboardContent;
        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboardContent = clipboard.getContents(null);
        }catch (IllegalStateException ex){ // clipboard is busy.
            new AlertToast("Clipboard is busy").show();
            return;
        }

        DataFlavor[] flavors = clipboardContent.getTransferDataFlavors();

        if (flavors.length > 0){
            if (flavor.isDataFlavorSupported(flavors[0])){
                // kaip ir musu fizikos.. bandom paimt jas.
                try{
                    final PhysicsHolder physicsHolder = (PhysicsHolder) clipboardContent.getTransferData(flavors[0]);
                    if (physicsHolder.hasShapes()) {
                        // toliau ziurim kokia situacija. Jeigu shapes jau kazkokios yra tai reik kazka daryt...
                        Array<PhysicsEditor.FixtureShapeHolder> shapes = editor.getActiveShapes();// dabar esamos shapes.
                        if (shapes.size > 0) {
                            // reiks klaust. mash up ar perrasom.
                            ConfirmDialog confirmDialog = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.YesNoCancel);
                            confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                                @Override
                                public void onYes() {
                                    mashTogetherPhysics(physicsHolder);
                                }

                                @Override
                                public void onNo() {
                                    overridePhysics(physicsHolder);
                                }

                                @Override
                                public void onCancel() {}
                            });
                            confirmDialog.setText("Current physics has shapes. Do you want to mix these shapes with copied one? (Yes - will mix existing physics with copied. No -" +
                                    " old physics will be rewritten by copied)");
                            confirmDialog.show();
                        } else {
                            // nera nieko nera. Tiesiog perrasom.
                            overridePhysics(physicsHolder);
                        }

                    }else {
                        new AlertToast("Copied physics don't have shapes!").show();
                    }
                }catch (UnsupportedFlavorException | IOException | ClassCastException ex){
                    ex.printStackTrace();
                    new AlertToast("Failed to read physics.").show();
                }
            }else {
                new AlertToast("Physics are not copied!").show();
            }
        }else {
            new AlertToast("Clipboard is empty!").show();
        }
    }

    // maisys fizikas. ziures ar senos vienodos ir jas maisys. Vienodu fixturu nedes du kart.
    private void mashTogetherPhysics(PhysicsHolder holder){
        if (holder.shapes.size() == 0){
            new AlertToast("Copied physics are empty!").show();
            return;
        }

        PhysicsHolder realHolder = editor.element;
        Pool<PhysicsEditor.FixtureShapeHolder> pool = editor.getShapesPool(); // cia sumesim nebenaudojamas shape.

        // pries viska flushinam shapes.
        editor.flushShapes();

        // bandysim matchint.
        Array<PhysicsEditor.FixtureShapeHolder> activeShapes = editor.getActiveShapes();

        // naudosim kaip musu sarasiuka.
        activeShapes.clear();

        // ziurim kopiju.
        for (PhysicsEditor.FixtureShapeHolder fixture : realHolder.shapes){
            for (int a = 0 ; a < holder.shapes.size(); a++){
                if (fixture.equals(holder.shapes.get(a))){
                    // radom identiska.
                    // identiskas metam is kopijos.
                    PhysicsEditor.FixtureShapeHolder e = holder.shapes.remove(a);
                    pool.free(e); // nesvaistom veltui.
                    break;
                }
            }

            // originalias surenkam.
            activeShapes.add(fixture);
        }

        // dabar surinksim kas liko is copijos.
        for (PhysicsEditor.FixtureShapeHolder e : holder.shapes){
            activeShapes.add(e);
        }

        // dabar darom kopijavimo veiksma.
        realHolder.copyHolder(holder);

        // shapes negeros. darom flush. Sumes gerasias shape.
        editor.flushShapes();

        // save daryt nereik, nes flush ta daro.
        updateList(); // bet sarasa reik atnaujint.

        // pranesam, kad viskas ok.
        new AlertToast("Physics mixed!").show();
    }

    // esamu fiziku perrasymas. Senas istrins ir ides naujas.
    private void overridePhysics(PhysicsHolder holder){
        if (holder.shapes.size() == 0){
            new AlertToast("Copied physics are empty!").show();
            return;
        }
        PhysicsHolder realHolder = editor.element;// musu physics holder. Tikrasis.
        Pool<PhysicsEditor.FixtureShapeHolder> pool = editor.getShapesPool(); // cia sumesim nebenaudojamas shape.
        for (PhysicsEditor.FixtureShapeHolder e : realHolder.shapes){
            pool.free(e);
        }

        // toliau copynam. Ten viduj shapes isvalys.
        realHolder.copyHolder(holder);

        // toliau tvarkom active shapes.
        Array<PhysicsEditor.FixtureShapeHolder> activeShapes = editor.getActiveShapes();
        activeShapes.clear(); // visos fixturos jau sumestos i pool.

        for (PhysicsEditor.FixtureShapeHolder e : realHolder.shapes){
            activeShapes.add(e);
        }

        updateList();

        Project.save();

        new AlertToast("Added new physics!").show();
    }

    // tikrint ar matoma. daryt paste mygtuka matoma ar ne.
    void checkPasteAvailability(){

        Clipboard clipboard;
        Transferable clipboardContent;
        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboardContent = clipboard.getContents(null);
        }catch (IllegalStateException ex){ // clipboard is busy.
            Gdx.app.log("Physics", "Error updating paste button. Clipboard is busy");
            return;
        }

        DataFlavor[] flavors = clipboardContent.getTransferDataFlavors();
        if (flavors.length > 0){
            paste.setEnabled(flavor.isDataFlavorSupported(flavors[0]));
        }else {
            paste.setEnabled(false);
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
//        checkPasteAvailability();
        paste.setEnabled(false); // kaip ir kazkas kitur nukopino kazka.
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible && paste != null){
            checkPasteAvailability();
        }
    }

    /* custom drawable */

    static class CustomTextureRegionDrawable extends TextureRegionDrawable{
        private TransformDrawable img2;

        CustomTextureRegionDrawable(TextureRegion e, TransformDrawable second){
            super(e);
            img2 = second;
        }

        @Override
        public void draw(Batch batch, float x, float y, float width, float height) {
            img2.draw(batch, x, y, width, height);
            super.draw(batch, x, y, width, height);
        }

        @Override
        public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
            img2.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
            super.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
        }
    }
}
