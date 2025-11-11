package com.jbconstructor.editor.dialogs.physicsEditor;

import com.badlogic.gdx.utils.Align;
import com.engine.core.Resources;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.dialogs.PopMeniu;
import com.engine.ui.controls.views.Panel;
import com.engine.ui.controls.widgets.CheckBox;
import com.engine.ui.controls.widgets.Label;
import com.engine.ui.controls.widgets.TextBox;
import com.engine.ui.listeners.FocusListener;
import com.jbconstructor.editor.root.PhysicsHolder;

/** allows to change fixtures settings. settings will be available when fixture is in edit mode. */
public class PhysicsEditorFixturePanel extends PopMeniu {

    private TextBox[] boxes;

    private CheckBox sensor;

    private PhysicsEditor.FixtureShapeHolder holder; // tam kuriam keisim viska.

    public PhysicsEditorFixturePanel(PhysicsEditor main){
        float wid = main.getWidth() * 0.2f; // panelio nustatymai.
        setSize(wid, main.getHeight()); // 20% viso physicsEditor dydzio.
        setPosition(main.getPosition().x + main.getWidth()*0.8f, main.getPosition().y);
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        Panel o = getController();
        o.setBackground(Resources.getDrawable("whiteSystemColor"));
        o.tintBackground(0xFFDAA520);
        setAnimatingTime(0.5f);

        // items..
        // labels..
        float jumpSize = 40;
//        float jumpSpace = jumpSize*1.3f;
        float jumpSpace = 46;

        Label.LabelStyle lst = new Label.LabelStyle();
        lst.verticalAlign = Align.center;
        lst.textOffsetX = 10;
        lst.textSize = jumpSize;
        lst.autoSize = false;
        lst.width = wid;
        lst.height = jumpSize;
        lst.y = main.getHeight() - jumpSpace;
        lst.text = "Density:";
        o.addControl(lst.createInterface()); // density
        lst.y -= jumpSpace*2;
        lst.text = "Friction [0-1]:";
        o.addControl(lst.createInterface()); // friction
        lst.text = "Restitution [0-1]:";
        lst.y -= jumpSpace*2;
        o.addControl(lst.createInterface()); // restitution
        lst.text = "Filter settings";
        lst.y -= jumpSpace*3;
        o.addControl(lst.createInterface()); // filter
        lst.text = "Mask bits:";
        lst.y -= jumpSpace;
        o.addControl(lst.createInterface()); // mask bits
        lst.text = "Category bits:";
        lst.y -= jumpSpace*2;
        o.addControl(lst.createInterface()); // category bits
        lst.text = "Group index:";
        lst.y -= jumpSpace*2;
        o.addControl(lst.createInterface()); // group.

        // text boxes.
        TextChangedListener e = new TextChangedListener();
        boxes = new TextBox[6];
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.verticalAlign = Align.center;
        tst.textOffsetX = 5;
        tst.textSize = jumpSize;
        tst.background = Resources.getDrawable("halfWhiteColor");
        tst.autoSize = false;
        tst.width = wid*0.8f;
        tst.height = jumpSize;
        tst.x = wid*0.1f;
        tst.y = main.getHeight() - jumpSpace*2;
        TextBox den = new TextBox(tst);
        o.addControl(den); // density
        den.setFocusListener(e);
        boxes[0] = den;
        tst.y -= jumpSpace*2;
        TextBox fr = new TextBox(tst);
        o.addControl(fr); // fricion
        fr.setFocusListener(e);
        boxes[1] = fr;
        tst.y -= jumpSpace*2;
        TextBox res = new TextBox(tst);
        o.addControl(res); // restitutun
        res.setFocusListener(e);
        boxes[2] = res;
        tst.y -= jumpSpace*4;
        TextBox mask = new TextBox(tst);
        o.addControl(mask); // mask bits
        mask.setFocusListener(e);
        boxes[3] = mask;
        tst.y -= jumpSpace*2;
        TextBox cat = new TextBox(tst);
        o.addControl(cat); // category bits
        cat.setFocusListener(e);
        boxes[4] = cat;
//        tst.height -= jumpSpace*2;
        tst.y -= jumpSpace*2;
        TextBox group = new TextBox(tst);
        o.addControl(group); // group index.
        group.setFocusListener(e);
        boxes[5] = group;

        // check box
        CheckBox.CheckBoxStyle cst = new CheckBox.CheckBoxStyle();
        cst.box = Resources.getDrawable("defaultCheckBox");
        cst.checkedBox = Resources.getDrawable("defaultCheckBoxTicked");
        cst.textSize = jumpSize;
        cst.text = "Is sensor";
        cst.x = 10;
        cst.y = main.getHeight() - jumpSpace*7;
        CheckBox sensor = new CheckBox(cst);
        sensor.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                if (holder != null){
                    holder.isSensor = checked;
                }
            }
        });
        o.addControl(sensor); // sensor.
        this.sensor = sensor;
    }

    public void show(PhysicsEditor.FixtureShapeHolder holder){
        if (holder != null){
            this.holder = holder;
            boxes[0].setText(holder.density+"");
            boxes[1].setText(holder.friction+"");
            boxes[2].setText(holder.restitution+"");
            boxes[3].setText(holder.maskBits+"");
            boxes[4].setText(holder.categoryBits+"");
            boxes[5].setText(holder.groupIndex+"");
            sensor.setChecked(holder.isSensor);
            show();
        }
    }

    private class TextChangedListener implements FocusListener{

        @Override
        public void onLostFocus(Control e) {
            if (holder != null) {
                if (e == boxes[0]) { // density
                    try {
                        holder.density = Float.parseFloat(boxes[0].getText());
                    }catch (NumberFormatException ex){
                        boxes[0].setText(holder.density+"");
                    }
                } else if (e == boxes[1]) { // friction
                    try {
                        holder.friction = Float.parseFloat(boxes[1].getText());
                    }catch (NumberFormatException ex){
                        boxes[1].setText(holder.friction+"");
                    }
                } else if (e == boxes[2]) { // resittution
                    try {
                        holder.restitution = Float.parseFloat(boxes[2].getText());
                    }catch (NumberFormatException ex){
                        boxes[2].setText(holder.restitution+"");
                    }
                } else if (e == boxes[3]) { // mask bits
//                    try {
//                        holder.maskBits = Short.parseShort(boxes[3].getText());
//                    }catch (NumberFormatException ex){
//                        boxes[3].setText(holder.maskBits+"");
//                    }
//                    holder.maskBits = parseNum(boxes[3].getText().trim(), holder.maskBits);
                    holder.maskBits = PhysicsHolder.parseMaskBit(boxes[3].getText().trim(), holder.maskBits);
                    boxes[3].setText(holder.maskBits+"");
                } else if (e == boxes[4]) { // categorybits
//                    try {
//                        holder.categoryBits = Short.parseShort(boxes[4].getText());
//                    }catch (NumberFormatException ex){
//                        boxes[4].setText(holder.categoryBits+"");
//                    }
//                    holder.categoryBits = parsePowerOfTwo(boxes[4].getText().trim(), holder.categoryBits);
                    holder.categoryBits = PhysicsHolder.parsePowerOfTwo(boxes[4].getText().trim(), holder.categoryBits);
                    boxes[4].setText(holder.categoryBits+"");
                } else if (e == boxes[5]) { // group index
                    try {
                        holder.groupIndex = Short.parseShort(boxes[5].getText());
                    }catch (NumberFormatException ex){
                        boxes[5].setText(holder.groupIndex+"");
                    }
                }
            }
        }

        // category bitam. turi but power of two.
//        private short parsePowerOfTwo(String text, short defaultValue){
//            if (text.length() == 0){
//                return 0;
//            }
//
//            try {
//                short e;
//                if (text.startsWith("0x")){ // ziurim ar hex skaicius.
//                    if (text.length() == 2){ // nedarase.
//                        throw new NumberFormatException("missing numbers");
//                    }
//                    // verciam.
//                    e = readHexNumber(text.substring(2));
//                }else { // paprastas skaicius.
//                    e = Short.parseShort(text);
//                }
//
//                // darom power of two.
//                return (short) MathUtils.nextPowerOfTwo(e);
//            }catch (NumberFormatException ex){
//                AlertToast alertToast = new AlertToast("Failed converting to number: " + text + "\nTip: For hex numbers use 0x before number");
//                alertToast.show();
//                return defaultValue;
//            }
//        }

//        // mask bitam.
//        // skaitys viskas kas eina per "|", ir dar skaitys hex skaicius, jie turi prasidet su "0x"
//        private short parseNum(String text, short defaultValue){
//            // ant tusciu nieko
//            if (text.length() == 0){
//                return 0;
//            }
//            try {
//                // turi ta bitu kirtimo dalyka.
//                if (text.contains("|")) {
//                    // turi ta kirtimo linija.
//                    String[] pieces = text.split("\\|");
//                    // pirma visus paversim i skaicius.
//                    short[] numbers = new short[pieces.length];
//                    for (int a = 0; a < pieces.length; a++) {
//                        String n = pieces[a].trim();
//                        if (n.length() == 0){ // tuscia. dedam 0.
//                            numbers[a] = 0;
//                            continue;
//                        }
//                        if (n.startsWith("0x")) { // hex skaicius.
//                            // hex skaicius.
//                            if (n.length() == 2){ // nedarasytas iki galo.
//                                throw new NumberFormatException("missing numbers");
//                            }
//                            numbers[a] = readHexNumber(n.substring(2));
//                        }else { // paprastas skaicius.
//                            numbers[a] = Short.parseShort(n);
//                        }
//                    }
//
//                    // o dabar viska suklijuojam.
//                    short anw = 0;
//                    for (short e : numbers){
//                        anw |= e;
//                    }
//                    return anw;
//                } else {
//                    // vientisas skaicius.
//                    if (text.startsWith("0x")){ // hex skaicius.
//                        if (text.length() == 2){ // nedarasytas iki galo.
//                            throw new NumberFormatException("missing numbers");
//                        }
//                        return readHexNumber(text.substring(2));
//                    }else { // paprastas skaicius.
//                        return Short.parseShort(text);
//                    }
//                }
//            }catch (NumberFormatException ex){ // kazkas kazkur negerai.
//                AlertToast alertToast = new AlertToast("Failed converting to number: " + text + "\nTip: For hex numbers use 0x before number");
//                alertToast.show();
//                return defaultValue;
//            }
//        }

        // nuskaityt hex number. short.
//        private short readHexNumber(String text) throws NumberFormatException{
//            return Short.parseShort(text, 16);
//        }

        @Override
        public void onFocus(Control e) { }
    }

}
