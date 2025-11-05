package com.jbconstructor.main.dialogs;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.PopUp;
import com.engine.interfaces.controls.Toast;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.widgets.AngleSelector;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.CheckBox;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.RadioButton;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.controls.widgets.TextBox;
import com.engine.interfaces.listeners.ClickListener;
import com.engine.interfaces.listeners.FocusListener;

public class AngularSelectionDialog extends PopUp {
    // spin count.
    private Label fullSpinLabel;
    private int spinCount;

    // text boxai
    private TextBox angle, pointX, pointY;

    // angle selector
    private AngleSelector angleSelector;

    // degrees radio button, vieno pilnai uzteks
    private RadioButton degrees;

    // veikiamieji
    private boolean hasApplied;
    private AngularSelectionDialogListener listener;
    private float startingAngle;
    private float ax, ay;

    public AngularSelectionDialog() {
        super(996, 577);

        // angle label
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.textSize = 50;
        lst.autoSize = false;
        lst.width = 173;
        lst.height = 52;
        lst.x = 496;
        lst.y = 484;
        lst.horizontalAlign = Align.right;
        lst.verticalAlign = Align.center;
        lst.text = "Angle:";

        addControl(lst.createInterface());

        // points label
        lst.y = 418;
        lst.text = "Points:";

        addControl(lst.createInterface());

        // bruksniukas
        lst.text = "-";
        lst.x = 799;
        lst.width = 58;
        lst.horizontalAlign = Align.center;

        addControl(lst.createInterface());

        // full spin label
        lst.x = 582;
        lst.y = 336;
        lst.width = 302;
        lst.text = "Full spins: 0";

        fullSpinLabel = new Label(lst);
        addControl(fullSpinLabel);

        // angle textbox
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.text = "0";
        tst.horizontalAlign = Align.center;
        tst.verticalAlign = Align.center;
        tst.autoSize = false;
        tst.width = 222;
        tst.height = 52;
        tst.x = 682;
        tst.y = 484;
        tst.textSize = 50;
        tst.background = Resources.getDrawable("whiteSystemColor");

        // control textbox.
        TextBox angleTextBox = new TextBox(tst);
        angleTextBox.setFocusListener(new FocusListener() {
            @Override
            public void onLostFocus(Interface e) {
                angleTextChange(angle.getText());
            }

            @Override
            public void onFocus(Interface e) {

            }
        });
        addControl(angleTextBox);
        angle = angleTextBox;

        // points x texxt box
        tst.width = 97;
        tst.y = 418;

        // listener
        FocusListener focusListener = new FocusListener() {
            @Override
            public void onLostFocus(Interface e) {
                pointTextChange(pointX.getText(), pointY.getText());
            }

            @Override
            public void onFocus(Interface e) {

            }
        };

        TextBox pointXBox = new TextBox(tst);
        pointXBox.setFocusListener(focusListener);
        addControl(pointXBox);
        pointX = pointXBox;

        // point y text box
        tst.x = 871;

        TextBox pointYBox = new TextBox(tst);
        pointYBox.setFocusListener(focusListener);
        addControl(pointYBox);
        pointY = pointYBox;

        // symbol mygtukai.
        // full rotation left
        SymbolButton.SymbolButtonStyle sbt = new SymbolButton.SymbolButtonStyle();
        sbt.autoSize = false;
        sbt.textSize = 50;
        sbt.x = 557;
        sbt.y = 254;
        sbt.width = 356;
        sbt.height = 52;
        sbt.text = "Full rotation left";
        sbt.background = Resources.getDrawable("whiteSystemColor");
        sbt.normalColor = 0xff0000ff;
//        sbt.position = SymbolButton.TextPosition.RIGHT;

        // sukuriam du sprite, kad viens kito neaffectintu ir lygtu teisingai rotatinti.
        SpriteDrawable arrowsToLeft = new SpriteDrawable(new Sprite(Resources.getTexture("additionalPanelDownListKey")));
        SpriteDrawable arrowsToRight = new SpriteDrawable(new Sprite(arrowsToLeft.getSprite()));

        arrowsToLeft.getSprite().rotate90(true);
        arrowsToRight.getSprite().rotate90(false);


//        addControl(new Button(sbt)); //
        sbt.symbol = arrowsToLeft;
        SymbolButton left = new SymbolButton(sbt);
        left.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                rotationLeftClick();
            }
        });
        addControl(left);

        // full rotation right
        sbt.text = "Full rotation right";
        sbt.y = 186;

//        addControl(new Button(sbt)); reik symbol button, cia tik test.
        sbt.symbol = arrowsToRight;
        sbt.position = SymbolButton.TextPosition.LEFT;
        SymbolButton right = new SymbolButton(sbt);
        right.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                rotationRightClick();
            }
        });
        addControl(right);

        // paprasti mygtukai.
        // normalize
        sbt.text = "NORMALIZE";
        sbt.y = 118;
        sbt.x = 607;
        sbt.width = 250;

        Button normalize = new Button(sbt);
        normalize.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                normalizeClick();
            }
        });
        addControl(normalize);

        // apply
        sbt.text = "Apply";
        sbt.x = 806;
        sbt.y = 23;
        sbt.width = 171;
//        sbt.y = 52;

        Button apply = new Button(sbt);
        apply.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                applyClick();
            }
        });
        addControl(apply);

        // cancel
        sbt.text = "Cancel";
        sbt.x = 612;

        Button cancel = new Button(sbt);
        cancel.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                AngularSelectionDialog.this.close();
            }
        });
        addControl(cancel);

        // reset
        sbt.text = "Reset";
        sbt.x = 412;

        // pagrindiniai mygtukai
        Button reset = new Button(sbt);
        reset.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                resetClick();
            }
        });
        addControl(reset);

        // radio button.
        // degrees
        RadioButton.RadioButtonStyle rst = new RadioButton.RadioButtonStyle();
        rst.autoSize = false;
        rst.x = 51;
        rst.y = 133;
        rst.width = 338;
        rst.height = 52;
        rst.textSize = 50;
        rst.checked = true; // tipo default.
        rst.text = "degrees";
        rst.checkedBox = Resources.getDrawable("defaultRadioTicked");
        rst.box = Resources.getDrawable("defaultRadioBox");

        // listener
        CheckBox.CheckedListener checkedListener = new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                angleUnitChange();
            }
        };

        RadioButton degreesButton = new RadioButton(rst);
        degreesButton.setCheckListener(checkedListener);
        addControl(degreesButton);
        degrees = degreesButton;

        // radians
        rst.checked = false;
        rst.text = "radians";
        rst.y = 63;

        RadioButton radiansButton = new RadioButton(rst);
        radiansButton.setCheckListener(checkedListener);
        addControl(radiansButton);

        // rutuliukas kur rinktis angle
        AngleSelector.AngleSelectorStyle ast = new AngleSelector.AngleSelectorStyle();
        ast.autoSize = false;
        ast.x = 111;
        ast.y = 218;
        ast.width = 332;
        ast.height = 332;
        ast.circle = Resources.getDrawable("angularDialogCircleBackground");
        ast.pointer = Resources.getDrawable("angularDialogPointer");
        ast.pointerThickness = 20;

        angleSelector = new AngleSelector(ast);
        angleSelector.setAngleSelectorListener(new AngleSelector.AngleSelectorListener() {
            @Override
            public void angleChanged(float old, float current) {
                AngularSelectionDialog.this.angleChanged(current);
            }
        });
        addControl(angleSelector);
    }

    private void setNumberText(TextBox e, float value){
        float round = MoreUtils.roundFloat(value, 2);
        e.setText(round+"");
    }

    private void setPoints(){
        float angle = angleSelector.getViewingAngle();
//        int c = spinCount + 1; // pointam sito nereik.
        float x = (float) Math.cos(angle);
        float y = (float) Math.sin(angle);
//        pointX.setText(x+"");
//        pointY.setText(y + "");
        setNumberText(pointX, x);
        setNumberText(pointY, y);
        ax = x;
        ay = y;
    }

    private void countSpins(){ // full spin nuo 0 iki 360.
        float angle = angleSelector.getViewingAngle();
        angle = MoreUtils.abs(angle);
        int count = 0;
        while (angle >= MathUtils.PI2){
            angle -= MathUtils.PI2;
            count++;
        }
        if (spinCount != count) {
            fullSpinLabel.setText("Full spins: " + count);
            spinCount = count;
        }
    }

    /* listeners inputs */

    // kvies kai user tampys ta angle.
    private void angleChanged(float angle){
        if (degrees.isChecked()){
//            this.angle.setText(angle * MathUtils.radiansToDegrees + "");
            setNumberText(this.angle, angle * MathUtils.radiansToDegrees);
        }else {
            this.angle.setText(angle + "");
//            setNumberText(this.angle, angle);
        }
        countSpins();
        setPoints();
    }

    // kai angle tekstas redagujamas
    private void angleTextChange(String text){
        try {
            float e = Float.parseFloat(text);
            if (degrees.isChecked()){ // nustatom musu rodykle ten kur reik.
                angleSelector.setViewingAngle(e * MathUtils.degreesToRadians);
            }else {
                angleSelector.setViewingAngle(e);
            }
            countSpins();
            setPoints();
        }catch (NumberFormatException ex){
            ex.printStackTrace();
            AlertToast toast = new AlertToast("Failed converting angle");
            toast.show(Toast.SHORT);
        }
    }

    /* Kai vienas is x arba y tekstu redaguojami. */
    private void pointTextChange(String x, String y){
        try {
            float nx = Float.parseFloat(x);
            float ny = Float.parseFloat(y);
            ax = nx;
            ay = ny;
            float angle = MathUtils.atan2(ny, nx);
            angleSelector.setViewingAngle(angle); // nustatom kita angle
            if (degrees.isChecked()){ // keiciam angle teksta pagal nustatyta unit.
//                this.angle.setText(angle * MathUtils.radiansToDegrees + "");
                setNumberText(this.angle, angle * MathUtils.radiansToDegrees);
            }else {
                this.angle.setText(angle + "");
            }
            countSpins();
        }catch (NumberFormatException ex){
            ex.printStackTrace();
            AlertToast toast = new AlertToast("Only numbers allowed!");
            toast.show(Toast.SHORT);
        }
    }

    // kvies kai keisis degrees i radians ar atvirksciai
    private void angleUnitChange(){
        angleChanged(angleSelector.getViewingAngle());
    }

    /* ant paneles esantys. */

    private void rotationLeftClick(){
        float angle = angleSelector.getViewingAngle() - MathUtils.PI2;
        angleSelector.setViewingAngle(angle);
        angleChanged(angle);
    }

    private void rotationRightClick(){
        float angle = angleSelector.getViewingAngle() + MathUtils.PI2;
        angleSelector.setViewingAngle(angle);
        angleChanged(angle);
    }

    private void normalizeClick(){
        float angle = angleSelector.getViewingAngle();
        while (angle > MathUtils.PI2){
            angle -= MathUtils.PI2;
        }
        while (angle < -MathUtils.PI2){
            angle += MathUtils.PI2;
        }
        angleSelector.setViewingAngle(angle);
//        countSpins();
        angleChanged(angle);
    }

    /* apatiniai. pagrindiniai */

    private void resetClick(){
        angleSelector.setViewingAngle(startingAngle);
        angleChanged(startingAngle);
    }

    private void applyClick(){
        hasApplied = true;
        close();
        if (listener != null){
            listener.angleSelected(angleSelector.getViewingAngle(), ax, ay);
        }
    }

    /* override */

    @Override
    protected void onClose() {
        if (!hasApplied && listener != null){
            listener.cancel();
        }
        super.onClose();
    }

    /* papildomi */

    public void setAngleSelectorListener(AngularSelectionDialogListener listener){
        this.listener = listener;
    }

    public AngularSelectionDialogListener getAngularSelectorListener(){
        return listener;
    }

    /* issaukiamieji */

    /** Angle in radians.*/
    public void show(float startingAngle, AngularSelectionDialogListener listener){
        setAngleSelectorListener(listener);
        show(startingAngle);
    }

    /** Angle in radians. */
    public void show(float startingAngle){
        hasApplied = false;
        this.startingAngle = startingAngle;
        angleSelector.setViewingAngle(startingAngle);
        angleChanged(startingAngle);
        open();
    }

    /* listener */

    public interface AngularSelectionDialogListener{
        /** @param angle angle in radians.
         *  @param x axis
         *  @param y axis*/
        void angleSelected(float angle, float x, float y);
        void cancel();
    }
}
