package com.jbconstructor.editor.dialogs.physicsEditor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.toasts.AlertToast;
import com.engine.ui.controls.views.Panel;
import com.engine.ui.controls.widgets.CheckBox;
import com.engine.ui.controls.widgets.ComboBox;
import com.engine.ui.controls.widgets.Label;
import com.engine.ui.controls.widgets.SymbolButton;
import com.engine.ui.controls.widgets.TextBox;
import com.engine.ui.listeners.ClickListener;
import com.engine.ui.listeners.FocusListener;
import com.jbconstructor.editor.managers.Project;
import com.jbconstructor.editor.root.PhysicsHolder;

/** Used to change body parameters. BodyType, body origin point. */
public class PhysicsEditorBodyPanel extends Panel {
    private final PhysicsEditor editor;

//    private Element resource; // sitam ir bus keiciami settings.
    private PhysicsHolder physicsHolder;
    private Drawable drawable;

    private float ox, oy; // origin taskai.

    private TextBox x, y;
    private CheckBox originMiddle;
    private ComboBox bodyType;

    private CheckBox fixedRotation, bulletBody;
    private TextBox linearDamping, angularDamping, gravityScale, angularVelocity, linearX, linearY;

    PhysicsEditorBodyPanel(PhysicsEditor main){
        editor = main;
        float wid = editor.getWidth() * 0.2f; // panelio nustatymai.
        setSize(wid, editor.getHeight()); // 20% viso physicsEditor dydzio.
        setPosition(wid * 4, 0);
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        setBackground(Resources.getDrawable("halfWhiteColor"));
        tintBackground(0xFFDAA520);

        // items.
        // labels.
//        float jumpSize = 30;
//        float height = editor.getHeight()*0.9f;

        Label.LabelStyle lst = new Label.LabelStyle();
        lst.text = "Body info";
        lst.y = 635;
        lst.textSize = 48;
        lst.autoSize = false;
        lst.width = wid;
        lst.height = 48;
        lst.horizontalAlign = Align.center;
        lst.verticalAlign = Align.center;
        addControl(lst.createInterface()); // pavadinimas.

        // body label
        lst.horizontalAlign = Align.left;
//        lst.verticalAlign = Align.top;
        lst.textSize = 30;
        lst.text = "Body type:";
        lst.width = wid;
        lst.height = lst.textSize;
        lst.textOffsetX = 5;
        lst.x = 0;
        lst.y = 598;

        // body
        addControl(lst.createInterface());

        // origin
        lst.text = "Body origin point:";
        lst.y = 529;
        addControl(lst.createInterface());

        // linear velocity
        lst.text = "Linear velocity";
        lst.y = 190;
        addControl(lst.createInterface());

        // linear damping.
        lst.textSize = 22;
        lst.shrinkTextStep = 4;
        lst.text = "Linear damping";
        lst.width = 150;
        lst.textOffsetX = 2f;
        lst.y = 344;
        addControl(lst.createInterface());

        // angular damping
        lst.shrinkTextStep = 2f;
        lst.text = "Angular damping";
        lst.y = 308;
        addControl(lst.createInterface());

        // gravity scale
        lst.text = "Gravity scale";
        lst.y = 271;
        addControl(lst.createInterface());

        // angular velocity
        lst.text = "Angular velocity";
        lst.y = 232;
        addControl(lst.createInterface());

        // x ir y
        // x..
//        lst.autoSize = true;
        // origin x.
        lst.textSize = 30;
        lst.width = 32;
        lst.height = 30;
        lst.text = "x:";
        lst.x = 5;
        lst.y = 493;
        addControl(lst.createInterface());

        // linear velocity x.
        lst.y = 153;
        addControl(lst.createInterface());

        // y..
        lst.text = "y:"; // y
        lst.x = 122;
        lst.y = 493;
        addControl(lst.createInterface());

        // linear velocity y.
        lst.y = 153;
        addControl(lst.createInterface());

        // combo box.
        ComboBox.ComboBoxStyle cst = new ComboBox.ComboBoxStyle();
        cst.textSize = 30;
        cst.autoSize = false;
        cst.width = wid*0.8f;
        cst.height = 30;
        cst.horizontalAlign = Align.center;
        cst.verticalAlign = Align.center;
        cst.background = Resources.getDrawable("systemWhiteRect");
        cst.listBackground = Resources.getDrawable("halfWhiteColor");
        cst.normalColor = 0xFF0000FF;
        cst.onColor = 0xFFFF0000;
        cst.pressedColor = 0xFFAA0000;
//        cst.y = height-jumpSize*3;
        cst.x = 31;
        cst.y = 563;
        ComboBox types = new ComboBox(cst);
        types.setIndexChangeListener(new ComboBox.IndexChangeListener() {
            @Override
            public void selectedIndexChanged(int old, int current) {
                if (current == 0){
//                    resource.setBodyType(BodyDef.BodyType.StaticBody);
                    physicsHolder.bodyType = BodyDef.BodyType.StaticBody;
                }else if (current == 1){
//                    resource.setBodyType(BodyDef.BodyType.KinematicBody);
                    physicsHolder.bodyType = BodyDef.BodyType.KinematicBody;
                }else {
//                    resource.setBodyType(BodyDef.BodyType.DynamicBody);
                    physicsHolder.bodyType = BodyDef.BodyType.DynamicBody;
                }

                if (physicsHolder.hasShapes()) // jei nera shapes tai ner tolko saugot.
                    Project.getSaveManager().triggerSave(); // body keitimas, issaugom.
            }
        });
        types.append("Static", "Kinematic", "Dynamic");
        types.setSelectedIndex(2);
//        types.placeInMiddle(wid/2, types.getPosition().y);
        addControl(bodyType = types);

        // text boxes.
        // origin x, y text box.
        TextBoxTextChanged list = new TextBoxTextChanged();
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.verticalAlign = Align.center;
        tst.horizontalAlign = Align.center;
        tst.autoSize = false;
        tst.width = 80;
        tst.height = 30;
        tst.x = 38;
        tst.y = 494;
        tst.background = cst.listBackground;
        tst.textSize = 30;
        TextBox xbox = new TextBox(tst); // x
        addControl(x = xbox);
        xbox.setFocusListener(list);
        tst.x = 158;
        TextBox ybox = new TextBox(tst);
        addControl(y = ybox);
        ybox.setFocusListener(list);

        // linear damping.
        tst.x = 149;
        tst.y = 344;
        tst.width = 90;
        TextBox linearBox = new TextBox(tst);
        linearBox.setFocusListener(list);
        addControl(linearDamping = linearBox);

        // angular damping.
        tst.y = 308;
        TextBox angularBox = new TextBox(tst);
        angularBox.setFocusListener(list);
        addControl(angularDamping = angularBox);

        // gravity scale
        tst.y = 270;
        TextBox gravityBox = new TextBox(tst);
        gravityBox.setFocusListener(list);
        addControl(gravityScale = gravityBox);

        // angular velocity.
        tst.y = 231;
        TextBox velocityBox = new TextBox(tst);
        velocityBox.setFocusListener(list);
        addControl(angularVelocity = velocityBox);

        //linear damping x ir y.
        //x
        tst.x = 38;
        tst.y = 154;
        tst.width = 80;
        TextBox xLinear = new TextBox(tst);
        xLinear.setFocusListener(list);
        addControl(linearX = xLinear);

        //y
        tst.x = 158;
        TextBox yLinear = new TextBox(tst);
        yLinear.setFocusListener(list);
        addControl(linearY = yLinear);


        // checkbox.
        // origin in middle.
        CheckBox.CheckBoxStyle hst = new CheckBox.CheckBoxStyle();
        hst.box = Resources.getDrawable("defaultCheckBox");
        hst.checkedBox = Resources.getDrawable("defaultCheckBoxTicked");
        hst.textSize = 30;
        hst.y = 453;
        hst.x = 5;
        hst.text = "Origin in middle";
        CheckBox middle = new CheckBox(hst);
        middle.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
//                resource.setBodyOriginMiddle(checked);
                physicsHolder.isBodyOriginMiddle = checked;
//                Vector2 pos = resource.getBodyOrigin();
                Vector2 pos = physicsHolder.bodyOrigin;
                if (physicsHolder.isBodyOriginMiddle){
                    pos.set(drawable.getMinWidth()/2, drawable.getMinHeight()/2);
                }
                ox = pos.x;
                oy = pos.y;
                updateCoords();
            }
        });
        addControl(originMiddle = middle);

        // fixed rotation
        hst.y = 418;
        hst.text = "Fixed rotation";
        CheckBox fixedRotation = new CheckBox(hst);
        fixedRotation.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                if (physicsHolder.fixedRotation != checked){
                    physicsHolder.fixedRotation = checked;

                    if (physicsHolder.hasShapes()) // jei nera shapes tai ner tolko saugot.
                        Project.save();
                }
            }
        });
        addControl(this.fixedRotation = fixedRotation);

        // bullet body
        hst.y = 385;
        hst.text = "Bullet body";
        CheckBox bullet = new CheckBox(hst);
        bullet.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                if (physicsHolder.bulletBody != checked){
                    physicsHolder.bulletBody = checked;

                    if (physicsHolder.hasShapes()) // jei nera shapes tai ner tolko saugot.
                        Project.save();
                }
            }
        });
        addControl(this.bulletBody = bullet);

//        // close button
//        Button.ButtonStyle bst = new Button.ButtonStyle();
//        bst.text = "Close";
//        bst.textSize = jumpSize;
//        bst.background = Resources.getDrawable("halfWhiteColor");
//        bst.normalColor = 0xFF0000FF;
//        Button close = new Button(bst);
//        close.placeInMiddle(wid/2, height - jumpSize*10);
//        close.setClickListener(new ClickListener() {
//            @Override
//            public void onClick() {
//                editor.exitBodyPanel();
//            }
//        });
//        addControl(close);

        // close mygtukas
        SymbolButton.SymbolButtonStyle sst = new SymbolButton.SymbolButtonStyle();
        sst.autoSize = false;
        sst.x =  223;
        sst.y = 661;
        sst.width = 20;
        sst.height = 20;
        sst.symbol = Resources.getDrawable("defaultColorPickCross");
        sst.symbolWidth = 20;
        sst.symbolHeight = 20;
        sst.normalColor = 0x00ffffff;

        SymbolButton close = new SymbolButton(sst);
        close.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                editor.exitBodyPanel();
            }
        });
        addControl(close);
    }

    private void updateCoords(){
        x.setText(round(ox)+"");
        y.setText(round(oy)+"");

        if (physicsHolder.hasShapes()) // jei nera shapes tai ner tolko saugot.
            Project.getSaveManager().triggerSave(); // issaugom nuo pasikeitimu.
    }

    public void show(PhysicsHolder e, Drawable drawable){
        setVisible(true);
        this.drawable = drawable;
//        resource = e;
        physicsHolder = e;
        originMiddle.setChecked(physicsHolder.isBodyOriginMiddle);
        Vector2 middle = e.bodyOrigin;
        if (physicsHolder.isBodyOriginMiddle){ // turim patys perstatyt origin.
            middle.set(drawable.getMinWidth()/2, drawable.getMinHeight()/2);
        }
        x.setText(round(middle.x)+"");
        y.setText(round(middle.y)+"");
        ox = middle.x;
        oy = middle.y;
        BodyDef.BodyType type = e.bodyType;
        if (type == BodyDef.BodyType.StaticBody){
            bodyType.setSelectedIndex(0);
        }else if (type == BodyDef.BodyType.KinematicBody){
            bodyType.setSelectedIndex(1);
        }else {
            bodyType.setSelectedIndex(2);
        }

        // kiti parametrai. Nauji.
        //fixed rotation.
        fixedRotation.setChecked(e.fixedRotation);
        // bullet.
        bulletBody.setChecked(e.bulletBody);
        // linear damping.
        linearDamping.setText(round(e.linearDamping)+"");
        // angular damping
        angularDamping.setText(round(e.angularDamping)+"");
        // gravity scale
        gravityScale.setText(round(e.gravityScale)+"");
        // angular velocity
        angularVelocity.setText(round(e.angularVelocity) + "");
        // linear.
        Vector2 linear = e.linearVelocity;
        linearX.setText(round(linear.x)+"");
        linearY.setText(round(linear.y)+"");
    }

    private float round(float v){
        return MoreUtils.roundFloat(v, 2);
    }

    void updateOriginLocation(float x, float y, float widthRatio, float heightRatio, float zoom){
        if (!physicsHolder.isBodyOriginMiddle) {
            Vector2 startPos = editor.resource.getPosition();
            ox = (x - startPos.x) / widthRatio / zoom;
            oy = (y - startPos.y) / heightRatio / zoom;
            physicsHolder.bodyOrigin.set(ox, oy);
            updateCoords();
        }
    }

    /* input listeners. */

    // bendras.
    private float getValue(String text, float compareValue, TextBox box){
        try{
            float value = Float.parseFloat(text);
            if (value == compareValue){
                return compareValue;
            }

//            physicsHolder.linearDamping = value;
            if (physicsHolder.hasShapes()) // jei nera shapes tai ner tolko saugot.
                Project.save(); // value naujas. issaugom projekta.

            return value;
        }catch (NumberFormatException ex){
            box.setText(round(compareValue)+"");
            new AlertToast(text + " is not a number!").show();
            return compareValue;
        }
    }

    private class TextBoxTextChanged implements FocusListener{

        @Override
        public void onLostFocus(Control e) {
            if (e == linearDamping){// linear damping
                String text = linearDamping.getText();
                physicsHolder.linearDamping = getValue(text, physicsHolder.linearDamping, linearDamping);
            }else if (e == angularDamping){ // angular damping
                String text = angularDamping.getText();
                physicsHolder.angularDamping = getValue(text, physicsHolder.angularDamping, angularDamping);
            }else if (e == gravityScale){ // gravity scale
                String text = gravityScale.getText();
                physicsHolder.gravityScale = getValue(text, physicsHolder.gravityScale, gravityScale);
            }else if (e == angularVelocity){ // angular velocity
                String text = angularVelocity.getText();
                physicsHolder.angularVelocity = getValue(text, physicsHolder.angularVelocity, angularVelocity);
            }else if (e == linearX){ // linear velocity x.
                String text = linearX.getText();
                physicsHolder.linearVelocity.x = getValue(text, physicsHolder.linearVelocity.x, linearX);
            }else if (e == linearY){ // linear velocity y.
                String text = linearY.getText();
                physicsHolder.linearVelocity.y = getValue(text, physicsHolder.linearVelocity.y, linearY);
            }else if (!originMiddle.isChecked()) {
                if (e == x) {
                    try {
                        ox = Float.parseFloat(x.getText());
//                        resource.setBodyOrigin(ox, oy);
                        if (ox == physicsHolder.bodyOrigin.x){
                            return;// nieks nepasikeite.
                        }
                        physicsHolder.bodyOrigin.set(ox, oy);

                        if (physicsHolder.hasShapes()) // jei nera shapes tai ner tolko saugot.
                            Project.getSaveManager().triggerSave(); // keiciasi cord.
                    } catch (NumberFormatException ex) {
                        x.setText("ERROR");
                    }
                } else if (e == y){ // y cia.
                    try {
                        oy = Float.parseFloat(y.getText());
                        if (oy == physicsHolder.bodyOrigin.y){
                            return;
                        }
//                        resource.setBodyOrigin(ox, oy);
                        physicsHolder.bodyOrigin.set(ox, oy);

                        if (physicsHolder.hasShapes()) // jei nera shapes tai ner tolko saugot.
                            Project.getSaveManager().triggerSave(); // keiciasi cord.
                    } catch (NumberFormatException ex) {
                        y.setText("ERROR");
                    }
                }
            }
        }

        @Override
        public void onFocus(Control e) { }
    }
}
