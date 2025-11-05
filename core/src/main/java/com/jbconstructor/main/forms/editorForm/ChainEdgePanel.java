package com.jbconstructor.main.forms.editorForm;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.controls.dialogs.InputTextDialog;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.views.Panel;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.CheckBox;
import com.engine.interfaces.controls.widgets.ComboBox;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.controls.widgets.TextBox;
import com.engine.interfaces.listeners.ClickListener;
import com.engine.interfaces.listeners.FocusListener;
import com.jbconstructor.main.editors.ChainEdging;
import com.jbconstructor.main.managers.Project;
import com.jbconstructor.main.root.PhysicsHolder;

public class ChainEdgePanel extends Panel implements EditMainPanel.ControlPanel {
    private final EditForm editForm;
    private final ChainEdging chain;

    // kontroles
    private ComboBox chainsList;
    private Label linesCount, chainName;
    private TextBox fieldX, fieldY;
    private CheckBox showLines;
    private CheckBox loop, isSensor;
    private SymbolButton moveChainMode;

    private TextBox maskBits, categoryBits, groupIndex;

    public ChainEdgePanel(EditForm e){
        editForm = e;
        float width = p.getScreenWidth()*0.2f, height = p.getScreenHeight()*0.85f;
        setSize(width, height);
//        chain = new ChainEdging(e.undoController);
        chain = e.getChainEdging();
        chain.setChainsListener(new ChainEdging.ChainsListener() {
            @Override
            public void moved(ChainEdging.Chains moved, float x, float y) {
                fieldX.setText(MoreUtils.roundFloat(x, 2)+"");
                fieldY.setText(MoreUtils.roundFloat(y, 2)+"");
            }

            @Override
            public void onChainAdd(ChainEdging.Chains added) {
                listUpdate();
                ChainEdging.Chains index = chain.getActiveChain();
                chainUpdate(index);
            }

            @Override
            public void onChainRemove(ChainEdging.Chains removed) {
                listUpdate();
                chainUpdate(null);
            }

            @Override
            public void onLinesChange(ChainEdging.Chains e, int lineCount) {
                linesCount.setText("Line count: " + lineCount);
            }

            @Override
            public void activeRectSelected(ChainEdging.Chains e, int index, float x, float y) {
                fieldX.setText(MoreUtils.roundFloat(x, 2)+"");
                fieldY.setText(MoreUtils.roundFloat(y, 2)+"");
            }

            private void chainUpdate(ChainEdging.Chains index){
                if (index == null){
                    chainName.setText("no chain");
                    chainName.setTextColor(0xFFFF0000);
                }else {
                    chainName.setTextColor(0);
                    chainName.setText(index.name);
                    chainsList.setSelectedIndex(chain.getActiveChainIndex());
                    isSensor.setChecked(index.isSensor);

                    maskBits.setText(index.maskBits+"");
                    groupIndex.setText(index.groupIndex+"");
                    categoryBits.setText(index.categoryBits+"");
                }
            }
        });

        // buttons
        Button.ButtonStyle st = new Button.ButtonStyle();
        st.text = "New";
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        st.background = Resources.getDrawable("halfWhiteColor");
        st.normalColor = 0xFF0089FF;
        st.onColor = 0xFFFF0000;
        st.pressedColor = 0xFFAA0000;
        st.autoSize = false;
        st.width = width/3;
        st.textSize = 30;
        st.height = 30;
        // new button
        float jump = 40;
        st.x = 5;
        st.y = height-jump*2;
        Button naujs = new Button(st);
        naujs.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                newClick();
            }
        });
        addControl(naujs);
        //edit
        st.y -= jump;
        st.text = "Edit";
        Button edit = new Button(st);
        edit.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                editClick();
            }
        });
        addControl(edit);
        //delete
        st.y -= jump;
        st.text = "Delete";
        Button del = new Button(st);
        del.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                deleteClick();
            }
        });
        addControl(del);

        // edit name mygtukas.
//        st.autoSize = true;
        st.x = 127;
        st.width = 120;
        st.text = "Edit id";

        Button editId = new Button(st);
        editId.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                ChainEdging.Chains line = chain.getActiveChain();
                if (line != null){
                    changeId(line);
                }else {
                    new AlertToast("No chain selected! Please select a chain first!").show();
                }
            }
        });
        addControl(editId);

        // checkbox
        CheckBox.CheckBoxStyle hst = new CheckBox.CheckBoxStyle();
        hst.textSize = 28;
        hst.y = height - jump*2;
        hst.x = 101;
        hst.text = "Show lines";
//        Resources.addImage("CheckBoxBox", "resources/ui/box.png");
//        Resources.addImage("CheckBoxTickedBox", "resources/ui/boxTick.png");
        hst.box = Resources.getDrawable("defaultCheckBox");
        hst.checkedBox = Resources.getDrawable("defaultCheckBoxTicked");
        CheckBox check = new CheckBox(hst);
        addControl(showLines = check);

        // combo chain saraso
        ComboBox.ComboBoxStyle cst = new ComboBox.ComboBoxStyle();
//        Resources.addImage("whiteRect", "resources/ui/whiteRect.png");
        cst.background = Resources.getDrawable("systemWhiteRect");
        cst.listBackground = st.background;
        cst.normalColor = 0xFF0050FF;
        cst.onColor = 0xFFFF0000;
        cst.pressedColor = 0xFFAA0000;
        cst.autoSize = false;
        cst.width = width/3*2-10;
        cst.height = 30;
        cst.textSize = 30;
        cst.x = 10 + st.width;
        cst.y = height - jump*3;
        cst.verticalAlign = Align.center;
        cst.horizontalAlign = Align.center;
        ComboBox box = new ComboBox(cst);
//        box.setIndexChangeListener(new ComboBox.IndexChangeListener() {
//            @Override
//            public void selectedIndexChanged(int old, int current) {
//                chainSelectedFromList(current);
//            }
//        });
        addControl(chainsList = box);

        // labels
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.x = 5;
        lst.autoSize = false;
        lst.width = 200;
        lst.height = 40;
        lst.textSize = 40;
        lst.text = "All chains:";
        lst.y = height-jump;
        addControl(lst.createInterface());
        lst.text = "no chain";
        lst.textColor = 0xFFFF0000;
        lst.y = height -jump*5-10;
        addControl(chainName = lst.createInterface()); // chain vardas

        // filter settings
        lst.y = height - jump*12;
        lst.text = "Filter settings";
        lst.textColor = 0xff000000;
        addControl(lst.createInterface());

//        lst.textColor = 0xFF000000;
        lst.text = "Line count: -";
        lst.textSize = 30;
        lst.y = height - jump*6-10;
        addControl(linesCount = lst.createInterface()); // liniju skaiius
        // current chain label
        lst.text = "Active point:";
        lst.textSize = 40;
        lst.y -=jump-10;
        addControl(lst.createInterface()); // siimple label
        // x, y labels
        lst.text = "x:";
        lst.textSize = 23;
        lst.autoSize = true;
        lst.y -= jump/2;
        addControl(lst.createInterface());
        lst.text = "y:";
        lst.x = width/2;
        addControl(lst.createInterface());

        // mask bit label
        lst.horizontalAlign = Align.right;
        lst.text = "Mask bits:";
        lst.autoSize = false;
        lst.width = 130;
        lst.height = 30;
        lst.x = 5;
        lst.y = height-jump*13;
        addControl(lst.createInterface());

        // category bits
        lst.text = "Category bits:";
        lst.y = height - jump*14;
        addControl(lst.createInterface());

        // group index
        lst.text = "Group index:";
        lst.y = height - jump*15;
        addControl(lst.createInterface());

        // textboxes.
        CoordinatesChangeListener list = new CoordinatesChangeListener();
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.verticalAlign = Align.center;
        tst.horizontalAlign = Align.center;
        tst.background = st.background;
        tst.autoSize = false;
        tst.width = width/3;
        tst.textSize = 30;
        tst.height = 30;
        tst.x = 25;
        tst.y = height - jump*7.6f;
        TextBox xt = new TextBox(tst);
        xt.setFocusListener(list);
        addControl(fieldX = xt);
        tst.x += width/2;
        TextBox yt = new TextBox(tst);
        yt.setFocusListener(list);
        addControl(fieldY = yt);

        // mask bits textBox.
        tst.x -= 10;
        tst.width = 100;
        tst.y = height-jump*12.8f;
        TextBox maskBox = new TextBox(tst);
        maskBox.setFocusListener(new MaskBitCalculator());
        addControl(maskBox);

        this.maskBits = maskBox;

        // category bits
        tst.y = height-jump*13.8f;
        TextBox categoryBox = new TextBox(tst);
        categoryBox.setFocusListener(new PowerOfTwoListener());
        addControl(categoryBox);

        this.categoryBits = categoryBox;

        // group index
        tst.y = height - jump*14.8f;
        TextBox groupBox = new TextBox(tst);
        groupBox.setFocusListener(new GroupIndexListener());
        addControl(groupBox);

        this.groupIndex = groupBox;

        // nu ir tasko del button.
        st.y = height - jump*8.6f;
        st.text = "Delete point";
        st.autoSize = true;
        Button delp = new Button(st);
        delp.auto();
        delp.alignHorizontal(width/2);
        delp.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                deletePointClick();
            }
        });
        addControl(delp);

        hst.text = "Enable loop";
        hst.textSize = 30;
        hst.x = 10;
        hst.y = st.y - jump;
        CheckBox loop = new CheckBox(hst);
        loop.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                ChainEdging.Chains e = chain.getActiveChain();
                if (e != null) {
                    boolean old = e.loop;
                    e.loop = checked;
                    if (old != e.loop){
                        Project.save();
                    }
                }else {
                    ChainEdgePanel.this.loop.setChecked(!checked); // atzymim, kad nebutu bugu.
                    new AlertToast("Please select chain!").show();
                }
            }
        });
        this.loop = loop;
        addControl(loop);

        // is sensor
        hst.text = "Is sensor";
//        hst.shrinkText = false;
        hst.y = hst.y - jump;
        CheckBox sensor = new CheckBox(hst);
        sensor.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                ChainEdging.Chains e = chain.getActiveChain();
                if (e != null){
                    boolean old = e.isSensor;
                    e.isSensor = checked;

                    if (old != e.isSensor){
                        Project.save();
                    }
                }else {
                    new AlertToast("Please select chain!").show();
                }
            }
        });
        addControl(sensor);
        isSensor = sensor;

        // move chain mygtukas.
        SymbolButton.SymbolButtonStyle sst = new SymbolButton.SymbolButtonStyle();
        sst.autoSize = false;
        sst.x = 220;
        sst.y = height -jump*5; // chain name.
        sst.width = 30;
        sst.height = 30;
        sst.symbolWidth = 30;
        sst.symbolHeight = 30;
        sst.normalColor = 0x00ffffff;
        sst.symbol = Resources.getDrawable("mainEditorSelectKey");

        SymbolButton moveChain = new SymbolButton(sst);
        moveChain.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                moveChainClick();
            }
        });
        addControl(moveChain);
        moveChainMode = moveChain;

        // isjungimo mygtukas.
        sst.width = 20;
        sst.height = 20;
        sst.symbolWidth = 20;
        sst.symbolHeight = 20;
        sst.y = height;
        sst.x = width - sst.width-5;
        sst.symbol = Resources.getDrawable("defaultColorPickCross");

        SymbolButton close = new SymbolButton(sst);
        close.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                editForm.checkControlInfo(); // padarys isjungima.
            }
        });
        addControl(close);

//        st.y -= jump*2;
//        st.text = "Close";
//        st.textSize = 35;
//        Button close = new Button(st);
//        close.auto();
//        close.alignHorizontal(width/2);
//        close.setClickListener(new ClickListener() {
//            @Override
//            public void onClick() {
//                editForm.checkControlInfo(); // padarys isjungima.
//
//            }
//        });
//        addControl(close);
    }

    /* listeners */

    private void moveChainClick(){
        if (chain.isMoveChainModeActive()){
            moveChainMode.setNormalColor(0x00ffffff);
            chain.enableChainMoveMode(false); // sitoj vietoj turi isjungt bet kokiu atveju.
        }else {
            // tik jeigu pavyks ijungt sita moda.
            if (chain.enableChainMoveMode(true)) {
                moveChainMode.setNormalColor(0xFFFF5500);
            }
        }
    }

    private void newClick(){
        chain.setActiveChain(-1);
        chainsList.setSelectedIndex(-1); // kad nieko nebutu parinkta.
        chainName.setTextColor(0xFFFF0000);
        chainName.setText("no chain");
        loop.setChecked(false);

        if (chain.isMoveChainModeActive()){
            moveChainClick(); // disablinam.
        }
    }

    private void editClick(){
        int index = chainsList.getSelectedIndex();
        if (index > -1){
            chain.setActiveChain(index);
            chainName.setText(chain.getActiveChain().name);
            chainName.setTextColor(0);
            linesCount.setText("Line count: " + (chain.getActiveChain().x.size-1));
            ChainEdging.Chains e = chain.getActiveChain();
            loop.setChecked(e.loop);
            isSensor.setChecked(e.isSensor);

            maskBits.setText(e.maskBits+"");
            categoryBits.setText(e.categoryBits+"");
            groupIndex.setText(e.groupIndex+"");

            if (chain.isMoveChainModeActive()){
                moveChainClick(); // pakeis i false.
            }
        }else {
            new AlertToast("Please select chain from the list!").show();
        }
    }

    private void deleteClick(){
        int index = chainsList.getSelectedIndex();
        if (index > -1){
            chain.removeChain(index);
            loop.setChecked(false);
            listUpdate();
        }else {
            new AlertToast("No chain selected!").show();
        }
    }

    private void deletePointClick(){
        chain.deleteActivePoint();
    }

//    private void chainSelectedFromList(int index){ // kai pasirinks is saraso.
////        int index = chainsList.getSelectedIndex();
//        if (index > -1){
//            chain.setActiveChain(index);
//        }
//    }

    /* veikimas */

    private void changeId(final ChainEdging.Chains active){
        final InputTextDialog inputTextDialog = new InputTextDialog("Enter new chain name:");
        inputTextDialog.getInput().setMaxLength(20); // ne daugiau 20.
        inputTextDialog.setInputDialogListener(new InputTextDialog.InputDialogListener() {
            @Override
            public void onInput(String input) {
                String text = input.trim();
                if (text.length() == 0){
                    new AlertToast("Chain name cannot be empty.").show();
//                    inputTextDialog.setText(text);
//                    inputTextDialog.show(); // atidarom is naujo.
                    inputTextDialog.open();
                    return;
                }
                // reik varyt per chainus ir ziuret ar tokio ner.
                for (ChainEdging.Chains chain : chain.getChains()){
                    if (text.equals(chain.name)){
                        new AlertToast("Chain name already exists: " + text).show();
//                        inputTextDialog.setText(text);
//                        inputTextDialog.show(); // atidarom is naujo.
                        inputTextDialog.open();
                        return;
                    }
                }

                // viskas ok.
                active.name = text;
                Project.save();

                // dar matoma info keiciam.
                listUpdate();
                new AlertToast("Successfully changed chain name!").show();
                chainName.setText(text);
            }

            @Override
            public void cancel() {}
        });
        inputTextDialog.show();
        inputTextDialog.getInput().setText(active.name); // nustatom varda.
    }

    private void deletePoint(){
        chain.deleteActivePoint();
    }

    private void listUpdate(){ // combo box list is esamu chain.
        if (chainsList == null)
            return;
        chainsList.clear(); // pirma isvalom.
        for (ChainEdging.Chains e : chain.getChains()){
            chainsList.append(e.name);
        }

        chainsList.setSelectedIndex(chain.getActiveChainIndex());
    }

    /* duomenu linija */

//    public boolean showLines(){
//        return showLines.isChecked();
//    }

//    public ChainEdging getChainEdgingManager() {
//        return chain;
//    }

    /* override */

    @Override
    public void setVisible(boolean visible) {
        boolean old = isVisible();
        super.setVisible(visible);
        if (visible){ // ijungian chain edginga.
            if (showLines != null && !old)
                showLines.setChecked(TopPainter.containsMainDraw(chain));
            TopPainter.addPaintOnTop(chain, false); // tik absolute.
            TopPainter.addInputsListener(chain);
            listUpdate(); // updatinam lista.

            // chain pradziai bun nulis. Nice.
            if (chain != null) {
                int color = chain.isMoveChainModeActive() ? 0xFFFF5500 : 0x00ffffff;
                moveChainMode.setNormalColor(color);
            }
        }else { // isjungiam.. paliekam linijas ??
            if (!showLines.isChecked())
                TopPainter.removeTopPaint(chain);
            chain.setActiveChain(-1);
            chainName.setText("no chain");
            chainName.setTextColor(0xFFFF0000);
            TopPainter.removeInputsListener(chain);

            // isjungiam move chain moda.
            if (chain.isMoveChainModeActive()){
                chain.enableChainMoveMode(false);
                moveChainMode.setNormalColor(0x00ffffff);
            }
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (super.touchDown(x, y, pointer, button))
            return true;
        Vector2 pos = editForm.getMainPanel().getPosition();
        float width = editForm.getMainPanel().getWidth();
        float height = editForm.getMainPanel().getHeight();
        boolean inPanel = !(x > pos.x && x < pos.x + width && y > pos.y && y < pos.y + height) && !editForm.getMover().isVisible();
        return inPanel && chain.touchDown();
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return super.pan(x, y, deltaX, deltaY) || chain.pan();
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return super.panStop(x, y, pointer, button) || chain.panStop();
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return super.tap(x, y, count, button) || chain.tap();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (super.keyDown(keycode)){
            return true;
        }else if (keycode == Input.Keys.FORWARD_DEL){
            deletePoint();
            return true;
        }else
            return false;
    }

    private class MaskBitCalculator implements FocusListener{

        @Override
        public void onLostFocus(Interface e) {
            ChainEdging.Chains line = chain.getActiveChain();
            if (line != null){ // jeigu yra chain.
                short old = line.maskBits; // pazesim ar pasikeis kas nors.
                TextBox textBox = (TextBox) e; // zinom 100%, kad cia text box.
                line.maskBits = PhysicsHolder.parseMaskBit(textBox.getText().trim(), line.maskBits);
                textBox.setText(line.maskBits+"");// atstatom jeigu negerai arba liks tas pats.

                if (old != line.maskBits){
                    Project.save(); // jeigu pasikeite, tai issaugom (bent jau pranesam).
                }
            }else {
                new AlertToast("Please select chain!").show();
            }
        }

        @Override
        public void onFocus(Interface e) {}
    }

    private class PowerOfTwoListener implements FocusListener{

        @Override
        public void onLostFocus(Interface e) {
            ChainEdging.Chains line = chain.getActiveChain();
            if (line != null){
                short old = line.categoryBits;
                TextBox textBox = (TextBox) e; // zinom 100%, kad cia text box.
                line.categoryBits = PhysicsHolder.parsePowerOfTwo(textBox.getText().trim(), line.categoryBits);
                textBox.setText(line.categoryBits+"");

                if (old != line.categoryBits){
                    Project.save();
                }
            }else {
                new AlertToast("Please select chain!").show();
            }
        }

        @Override
        public void onFocus(Interface e) {}
    }

    private class GroupIndexListener implements FocusListener{

        @Override
        public void onLostFocus(Interface e) {
            ChainEdging.Chains line = chain.getActiveChain();
            if (line != null){
                short old = line.groupIndex;
                TextBox group = (TextBox) e;

                try{
                    line.groupIndex = Short.parseShort(group.getText().trim());
                    if (old != line.groupIndex){
                        Project.save();
                    }
                }catch (NumberFormatException ex){
                    group.setText(line.groupIndex+"");
                    new AlertToast("Not a number!").show();
                }
            }else {
                new AlertToast("Please select chain!").show();
            }
        }

        @Override
        public void onFocus(Interface e) {}
    }

    private class CoordinatesChangeListener implements FocusListener{

        @Override
        public void onLostFocus(Interface e) {
            ChainEdging.Chains ch = chain.getActiveChain();
            if (ch != null && chain.getActiveRectIndex() > -1){
                if (e == fieldX){
                    float y = ch.y.get(chain.getActiveRectIndex());
                    chain.movePoint(toFloat(fieldX.getText()), y);
                }else if (e == fieldY){
                    float x = ch.x.get(chain.getActiveRectIndex());
                    chain.movePoint(x, toFloat(fieldY.getText()));
                }

            }else {
                new AlertToast("Please select chain!").show();
            }
        }

        @Override
        public void onFocus(Interface e) {}

        private float toFloat(String value){
            if (value == null){
                return 0;
            }
            try{
                return Float.parseFloat(value);
            }catch (NumberFormatException e){
                return 0;
            }
        }
    }
}
