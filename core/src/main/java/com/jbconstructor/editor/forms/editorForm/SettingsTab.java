package com.jbconstructor.editor.forms.editorForm;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.Field;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.Toast;
import com.engine.ui.controls.Window;
import com.engine.ui.controls.views.Panel;
import com.engine.ui.controls.widgets.Button;
import com.engine.ui.controls.widgets.ComboBox;
import com.engine.ui.controls.widgets.Label;
import com.engine.ui.controls.widgets.SImage;
import com.engine.ui.controls.widgets.Switch;
import com.engine.ui.controls.widgets.TabControl;
import com.engine.ui.controls.widgets.TextBox;
import com.engine.ui.listeners.ClickListener;
import com.engine.ui.listeners.FocusListener;
import com.jbconstructor.editor.dialogs.ColorPicker;
import com.jbconstructor.editor.root.Resizer;
import com.jbconstructor.editor.root.Element;

public class SettingsTab extends TabControl implements EditMainPanel.ControlPanel {
    private final EditForm editForm;

    // main settings
    // x, y, width, height, degrees, name
    private TextBox[] mainsBox;
    private ComboBox positionings;

    // tint, kad butu galima pradangint.
    private Button tint, polygons;
    private Element selectedResource;
//    private Label tintLabel;
    //listener
    private ColorPickerList color;
    private MainTextBoxesFocusLost text;

    public SettingsTab(EditForm form) {
        super(new TabControlStyle());
        editForm = form;
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        buttonStyle.background = Resources.getDrawable("halfWhiteColor");
        float height = p.getScreenHeight() * 0.8f, width = p.getScreenWidth()*0.2f;
        setMaxSize(width, height);
//        setPosition(100, 100);
//        setPositioning(Window.Position.absolute);
        setButtonsPosition(ButtonsPosition.TOP);
        setbuttonStyle(buttonStyle);
        color = new ColorPickerList();
        text = new MainTextBoxesFocusLost();

        // panel default parametrai.
        Panel.PanelStyle st = new Panel.PanelStyle();
        st.width = width;
        st.height = height;
        { // main panel
            // pati panel.
            mainsBox = new TextBox[6];
            Panel tab1 = new Panel(st);
            tab1.setBackground(Resources.getDrawable("whiteSystemColor"));
            tab1.tintBackground(0xFFBFFCC6);
            addTab(tab1, "Main");
            // vidus

            // id name lanel
            Label.LabelStyle lst = new Label.LabelStyle();
            lst.textSize = getButtonStyle().textSize;
            float topY = height - lst.textSize;
//            float jump = lst.textSize;
            float jump = 40;
            lst.x = 0;
            lst.y = topY;
            tab1.addControl(new Label("ID name", lst));

            // position lanel
            lst.y = topY - jump*2;
            tab1.addControl(new Label("Position", lst)); // position.

            // x label
            lst.y -= jump - (jump*0.3f);
            lst.textSize = lst.textSize/1.5f;
            tab1.addControl(new Label("x:", lst));

            // y label
            lst.x = width / 2;
            lst.text = "y:";
            tab1.addControl(new Label( lst));

            // degrees label
            lst.x = 0;
//            lst.y = height/sizes*4;
            lst.y -= jump*4;
            tab1.addControl(new Label("degrees:", lst));

            // width label
            lst.textSize = getButtonStyle().textSize/2f;
//            lst.y = height/sizes * 6;
            lst.y += jump*2;
            tab1.addControl(new Label("width:", lst));

            // height label
            lst.x = width / 2 + 25;
            lst.y += 30;
            lst.text = "height:";
            tab1.addControl(new Label( lst));

            // size label
            lst.x = 0;
//            lst.y = height/sizes * 7;
            lst.y = height - jump*5;
            lst.textSize = getButtonStyle().textSize;
            tab1.addControl(new Label("Size", lst));

            // rotation label
//            lst.y = height/sizes*5;
            lst.y -= jump*2;
//            lst.textSize = getButtonStyle().textSize;
            tab1.addControl(new Label("Rotation", lst));

            // positioning label
//            lst.y = height/sizes*3;
            lst.y -= jump*2;
            tab1.addControl(new Label("Positioning", lst));

            // tint label
            lst.y -= jump*2.5f;
            tab1.addControl(new Label("Tint", lst));

            // combo
            ComboBox.ComboBoxStyle cst = new ComboBox.ComboBoxStyle();
            cst.defaultWord = "-Select-";
//            Resources.addImage("blueRect", "resources/ui/blueRect.png");
            cst.background = Resources.getDrawable("systemWhiteRect");
            cst.listBackground = Resources.getDrawable("whiteSystemColor");
//            cst.y = height/sizes * 2;
            cst.y = height - jump*10;
            cst.x = width/9;
            cst.horizontalAlign = Align.center;
            cst.verticalAlign = Align.center;
            cst.onColor = 0xFFFF00FF;
            cst.pressedColor = 0xFF0000AA;
            cst.upside = true;
            ComboBox box = new ComboBox(cst);
            box.setIndexChangeListener(new ComboBox.IndexChangeListener() {
                @Override
                public void selectedIndexChanged(int old, int current) {
                    positioningChanged(old, current);
                }
            });
            box.append("Absolute", "Fixed", "Relative");
            box.setSelectedIndex(0);
            tab1.addControl(positionings = box);

            // text fields.
            // name text field.
            TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
            tst.verticalAlign = Align.center;
            tst.horizontalAlign = Align.center;
            tst.textSize = tst.textSize/1.5f;
            tst.background = cst.listBackground;
//            tst.y = height/sizes * 8;
            tst.autoSize = false;
            tst.height = 27;
            tst.y = height - jump*2;
            tst.x = width*0.025f;
            tst.width = width*0.95f;
            TextBox name = new TextBox(tst); // name textField
            name.lockText(true);
            name.setText("");
            name.setFocusListener(text);
            tab1.addControl(mainsBox[5] = name);

            // x field.
            tst.x = width/10;
            tst.width = width/3+10;
            tst.y = height - jump*4 + (jump*0.2f);
            final TextBox cordX = new TextBox(tst); //x
//            cordX.setTextListener(new OnlyNumbers());
            cordX.setFocusListener(text);
            tab1.addControl(mainsBox[0] = cordX);

            // y field.
            tst.x = width/10 + width/2;
            TextBox cordY = new TextBox(tst); // y
            cordY.setFocusListener(text);
            tab1.addControl(mainsBox[1] = cordY);

            // degrees field.
//            tst.y = height/sizes*4;
            tst.y -= jump*4;
            tst.x = width/2-20;
            TextBox deg = new TextBox(tst); // degrees
            deg.setFocusListener(text);
            tab1.addControl(mainsBox[4] = deg);

            // width field.
            tst.x = width / 4.5f;
//            tst.y = height/sizes * 6;
            tst.y += jump*2;
            TextBox wid = new TextBox(tst); // width
            wid.setFocusListener(text);
            tab1.addControl(mainsBox[2] = wid);

            // height field.
            tst.x = width/2 + 30;
            TextBox hei = new TextBox(tst); // height
            hei.setFocusListener(text);
            tab1.addControl(mainsBox[3] = hei);

            // mygtukas tintinimui.
            Button.ButtonStyle bst = new Button.ButtonStyle();
            bst.background = cst.listBackground;
            bst.autoSize = false;
            bst.width = width/2;
            bst.height = tst.height*2;
            bst.y = height-jump*12f;
            bst.x = width/2 - bst.width/4;
            Button tint = new Button("", bst);
            tint.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    Resizer e = editForm.getSizer();
                    color.state = 0;
                    if (e.getControls().size() == 1){
                        Control a = e.getControls().get(0);
                        if (a instanceof SImage){
                            editForm.colorPicker.show(((SImage) a).getImageTint(), ((SImage) a).getImage());
                        }else {
                            editForm.colorPicker.show(a.getNormalColor());
                        }
                        editForm.colorPicker.setColorPickerListener(color);
                    }else if (e.hasMultipleControls()){
                        for (Control a : e.getControls()){
                            if (a instanceof SImage){
                                editForm.colorPicker.show(((SImage) a).getImageTint(), ((SImage) a).getImage());
                                editForm.colorPicker.setColorPickerListener(color);
                                return;
                            }
                        }
                        Control a = e.getControls().get(0); // jeigu ner img
                        editForm.colorPicker.show(a.getNormalColor());
                        editForm.colorPicker.setColorPickerListener(color);
                    }
                }
            });
            tab1.addControl(this.tint = tint);

            // polygon editinimui.
            bst.y -= jump*2f;
            bst.text = "Edit polygons";
            bst.textSize = 25f;
            bst.width = width/3*2;
            Button polygons = new Button(bst);
            polygons.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    if (selectedResource != null)
                        editForm.physicsEditor.show(selectedResource);
                }
            });
            polygons.alignHorizontal(width/2);
            tab1.addControl(this.polygons = polygons);
        }
        { // interface tab.
            float sizes = 13f;
            Panel tab2 = new Panel(st);
            tab2.setBackground(Resources.getDrawable("whiteSystemColor"));
            tab2.tintBackground(0xFFBFFCC6);
            addTab(tab2, "Interface");
            // labels
            Label.LabelStyle lst = new Label.LabelStyle();
            lst.y = height/sizes * 12f;
            tab2.addControl(new Label("Colors", lst));
            lst.y = height/sizes * 9f;
            float old = lst.textSize;
            lst.textSize = lst.textSize/2;
            tab2.addControl(new Label("FOCUSABLE", lst));
            lst.y = height/sizes * 8f;
            tab2.addControl(new Label("VISIBLE", lst));
            lst.y = height/sizes * 7f;
            tab2.addControl(new Label("ENABLED", lst));
            lst.y = height/sizes * 6f;
            tab2.addControl(new Label("AUTOSIZE", lst));
            lst.y = height/sizes * 5f;
            tab2.addControl(new Label("ROTATABLE", lst));
            lst.y = height/sizes * 4f;
            lst.textSize = old;
            tab2.addControl(new Label("Text control", lst));
            lst.y = height/sizes*2f;
            tab2.addControl(new Label("Text align", lst));
            lst.textSize = lst.textSize / 1.5f;
            lst.y = height/sizes * 3f;
            tab2.addControl(new Label("Size", lst));
            lst.y = height/sizes;
            lst.textSize = old/2;
            tab2.addControl(new Label("Vertical", lst));
            lst.x = width/2;
            tab2.addControl(new Label("horizontal", lst));
            // spalvu mygtukai
            Button.ButtonStyle bst = new Button.ButtonStyle();
            bst.background = Resources.getDrawable("halfWhiteColor");
            bst.y = height/sizes * 11;
            bst.textSize = 24;
            Button normal = new Button("Normal", bst);
//            final ColorPicker e = new ColorPicker();
//            editForm.addPopUp(e);
//            normal.setClickListener(new ClickListener() {
//                @Override
//                public void onClick() {
//                    e.open();
//                }
//            });
            tab2.addControl(normal);
            bst.x = width/2;
            Button on = new Button("Over", bst);
            tab2.addControl(on);
            bst.x = 0;
            bst.y = height/sizes*10;
            Button pressed = new Button("Pressed", bst);
            tab2.addControl(pressed);
            bst.x = width/2;
            Button disabled = new Button("Disabled", bst);
            tab2.addControl(disabled);
            // switchai.
//            Resources.addImage("PopUpDefault", "resources/ui/popupdefault.png");
            Switch.SwitchStyle sst = new Switch.SwitchStyle();
            sst.tintOn = 0xFF00FF00;
            sst.tintOff = 0xFFFF0000;
            sst.background = Resources.getDrawable("defaultPopUpBackground");
            sst.scrollBar = Resources.getDrawable("defaultPopUpBackground");
            sst.x = width/2;
            sst.y = height/sizes * 9f;
            sst.autoSize = false;
            sst.width = width/3;
            sst.height = height/sizes*0.7f;
            sst.barSize = sst.width/2;
            Switch focusable = new Switch(sst);
            tab2.addControl(focusable);
            sst.y = height/sizes * 8f;
            Switch visible = new Switch(sst);
            tab2.addControl(visible);
            sst.y = height/sizes * 7f;
            Switch enabled = new Switch(sst);
            tab2.addControl(enabled);
            sst.y = height/sizes * 6f;
            Switch autosize = new Switch(sst);
            tab2.addControl(autosize);
            sst.y = height/sizes * 5f;
            Switch rotatable = new Switch(sst);
            tab2.addControl(rotatable);
        }
    }

    /* perdavimo linija */

    void settingsChange(Control e){ // TODO round hardocintas skaicius.
        Vector2 pos = e.getPosition();
        mainsBox[0].setText(MoreUtils.roundFloat(pos.x, 2) + "");
        mainsBox[1].setText(MoreUtils.roundFloat(pos.y, 2) + "");
        mainsBox[4].setText(e.getAngle() + "");
        int color;
        if (e instanceof Resizer){
            if (((Resizer) e).isControlsPositioningSame()){
                positionings.setSelectedIndex(((Resizer) e).getControls().get(0).getPositioning());
            }else {
                positionings.setText("Mixed");
            }
            mainsBox[5].setText("Multiple selected");
            mainsBox[5].lockText(true);
            color = ((Resizer) e).getControls().get(0).getNormalColor();
            polygons.setVisible(false);
        }else {
            positionings.setSelectedIndex(e.getPositioning());
            mainsBox[5].setText(e.getIdName());
            mainsBox[5].lockText(false);
            color = e.getNormalColor();
            if (e instanceof Element){
                selectedResource = (Element) e;
                polygons.setVisible(true);
            }else {
                polygons.setVisible(false);
            }
        }
        tint.setColors(color, color, color);
        if (e instanceof Field) {
            Field f = (Field) e;
            mainsBox[2].setText(MoreUtils.roundFloat(f.getWidth(),2)+"");
            mainsBox[3].setText(MoreUtils.roundFloat(f.getHeight(), 2) + "");
        }
        // toliau jei tai daugiau nei paveiksliukas turetu eit.
    }

    // 0 - absolute, 1 - fixed, 2 - relative
    private void positioningChanged(int old, int pos){ // image suolis i kita lokacija jei is fixed i absolute, o absolute judinta. visks gerai.
        Resizer e = editForm.getSizer();
        Window.Position rpos;
        switch (pos){
            case 0:
                rpos = Window.Position.absolute;
                break;
            case 1:
                rpos = Window.Position.fixed;
                break;
            default:
                rpos = Window.Position.relative;
                break;
        }
        float[] oldPos = new float[e.getControls().size()];
        int count = 0;
        for (Control a : e.getControls()){
            oldPos[count] = a.getPositioning();
            a.setPositioning(rpos);
            count++;
        }
        editForm.undoController.setInterfaces(e.getControls());
        editForm.undoController.moved(3, oldPos);
        e.update();
    }

    private float toFloat(String text){
        float ans;
        try {
            ans = Float.parseFloat(text);
        } catch (NumberFormatException ex){
            Toast e = new Toast(Toast.SHORT);
            e.setText("This is not a number!");
            e.show();
            return 0;
        }
        return ans;
    }

    private class MainTextBoxesFocusLost implements FocusListener{ // x, y, width, height, degrees. Gal dar ką?

        @Override
        public void onLostFocus(Control e) {
            int id = -1;
            for (int a = 0; a < mainsBox.length; a++){
                if (e == mainsBox[a]){
                    id = a;
                    break;
                }
            }
            Resizer r = editForm.getSizer();
            switch (id){
                case 0: //x
                    r.move(toFloat(mainsBox[id].getText()), r.getPosition().y + r.getBoxSize()/2, true, false, false);
                    break;
                case 1: // y
                    r.move(r.getPosition().x + r.getBoxSize()/2, toFloat(mainsBox[id].getText()), false, true, false);
                    break;
                case 2: // width
                    r.changeSize(toFloat(mainsBox[id].getText()), r.getHeight(), true, false, false);
                    break;
                case 3: // height
                    r.changeSize(r.getWidth(), toFloat(mainsBox[id].getText()), false, true, false);
                    break;
                case 4: // degrees
                    r.rotate(toFloat(mainsBox[id].getText()));
                    break;
                case 5: // name.
                    if (!r.hasMultipleControls()){
                        Control main = r.getControls().get(0);
                        String old = main.getIdName();
                        if (!main.setIdName(mainsBox[id].getText())){
                            mainsBox[id].setText(main.getIdName());
                            return;
                        }
                        editForm.undoController.setInterfaces(r.getControls());
                        editForm.undoController.moved(5, old);
//                        editForm.updateList();
                    }
            }
        }

        @Override
        public void onFocus(Control e) {

        }
    }

    private class ColorPickerList implements ColorPicker.ColorPickerListener{
        // 0 - tint visa img.
        int state;

        @Override
        public void setColor(Color color) {
            switch (state){
                case 0: // resource Image tintinimas.
                    int spalva = Color.argb8888(color);
                    tint.setColors(spalva, spalva, spalva);
                    editForm.colorPicker.setColorPickerListener(null);
                    Resizer sizer = editForm.getSizer();
                    Array<Control> list = editForm.undoController.getList();
                    list.clear();
                    Array<Float> set = editForm.undoController.getFlushSettings();
                    set.clear();
                    boolean isTinted = false;
                    for (Control e : sizer.getControls()){
                        if (e instanceof SImage){
                            int previous = ((SImage) e).getImageTint();
                            set.add((float) previous);
                            ((SImage) e).tintImage(color);
                            list.add(e);
                            isTinted = true;
                        }
                    }
                    if (isTinted){
                        editForm.undoController.moved(4);
                    }
                    break;
            }
        }

        @Override
        public void cancel() {
            editForm.colorPicker.setColorPickerListener(null);
        }
    }
}
