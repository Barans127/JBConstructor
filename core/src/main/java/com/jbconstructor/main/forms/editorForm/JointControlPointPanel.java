package com.jbconstructor.main.forms.editorForm;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.Toast;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.views.Panel;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.controls.widgets.TextBox;
import com.engine.interfaces.listeners.ClickListener;
import com.engine.interfaces.listeners.FocusListener;
import com.engine.interfaces.listeners.MainDraw;
import com.jbconstructor.main.editors.ChainEdging;
import com.jbconstructor.main.root.Element;
import com.jbconstructor.main.root.FieldMover;
import com.jbconstructor.main.root.FiguresDrawer;
import com.jbconstructor.main.root.PhysicsHolder;
import com.jbconstructor.main.root.Resizer;
import com.jbconstructor.main.root.SelectiveInterfaceController;
import com.jbconstructor.main.root.Selector;

public class JointControlPointPanel extends Panel implements MainDraw {
    private JointControlPanel controlPanel;
    private JointControlInputs inputs;
    private EditForm editForm;

    // valdymo kontroles
    private TextBox xField, yField;

    // veikiamieji
    private float startX, startY;
    private float cx, cy;
    private boolean applied = false;
    private float angle;

    // vaizdas
    private Drawable pointer = Resources.getDrawable("repositionIcon");

    // listener
    private JointControlPointPanelListener listener;
    private FiguresDrawer jointDrawer;
    private int pointIndex;
    private float prevX, prevY;

    public JointControlPointPanel(JointControlPanel panel, EditForm editForm){
        this.editForm = editForm;
        controlPanel = panel;
        inputs = controlPanel.getInputsController();
        float width = p.getScreenWidth()*0.2f, height = p.getScreenHeight()*0.85f;
        setSize(width, height); // standartinis panel dydis.

        // isjungimo mygtukas
        SymbolButton.SymbolButtonStyle sst = new SymbolButton.SymbolButtonStyle();
        sst.autoSize = false;
        sst.x = 226;
        sst.y = 586;
        sst.width = 23;
        sst.height = 23;
        sst.symbol = Resources.getDrawable("defaultColorPickCross");
        sst.background = Resources.getDrawable("whiteSystemColor");
        sst.normalColor = 0x00ffffff;
        sst.text = "";

        SymbolButton close = new SymbolButton(sst);
        close.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                JointControlPointPanel.this.setVisible(false);
                controlPanel.showPanel(true);
            }
        });
        addControl(close);

        // main label
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.autoSize = false;
        lst.x = 5;
        lst.y = 554;
        lst.width = 181;
        lst.height = 40;
        lst.textSize = 40;
        lst.text = "Local point:";
        lst.verticalAlign = Align.center;

        addControl(lst.createInterface());

        // x label
        lst.x = 5;
        lst.y = 509;
        lst.text = "x:";
        lst.width = 35;
        lst.height = 40;

        addControl(lst.createInterface());

        // y label
        lst.x = 131;
        lst.text = "y:";

        addControl(lst.createInterface());

        // x text box
        CordListener listener = new CordListener();
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.autoSize = false;
        tst.x = 37;
        tst.y = 509;
        tst.width = 90;
        tst.height = 35;
        tst.background = Resources.getDrawable("whiteSystemColor");
        tst.verticalAlign = Align.center;
        tst.horizontalAlign = Align.center;
        tst.textSize = 30;
        tst.minTextSize = 10;

        TextBox xBox = new TextBox(tst);
        xField = xBox;
        xBox.setFocusListener(listener);
        addControl(xBox);

        // y text box
        tst.x = 163;

        TextBox yBox = new TextBox(tst);
        yField = yBox;
        yBox.setFocusListener(listener);
        addControl(yBox);

        // center mygtukas
        Button.ButtonStyle bst = new Button.ButtonStyle();
        bst.autoSize = false;
        bst.x = 84;
        bst.y = 460;
        bst.width = 88;
        bst.height = 40;
        bst.text = "Center";
        bst.background = Resources.getDrawable("whiteSystemColor");
        bst.normalColor = 0xff0000ff;

        Button center = new Button(bst);
        center.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                pointChangedPlace(startX, startY);
            }
        });
        addControl(center);

        // cancel mygtukas
        bst.x = 24;
        bst.y = 410;
        bst.width = 98;
        bst.text = "Cancel";

        Button cancel = new Button(bst);
        cancel.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                JointControlPointPanel.this.setVisible(false);
                controlPanel.showPanel(true);
            }
        });
        addControl(cancel);

        // apply mygtukas
        bst.x = 138;
        bst.text = "Apply";

        Button apply = new Button(bst);
        apply.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                apply();
            }
        });
        addControl(apply);
    }

    /* listener */

    private void apply(){
        // paduodam listeneriui viska ko reik
        if (listener != null){
            if (angle == 0) {
                listener.onCordChange(MoreUtils.roundFloat(cx - startX, 2),
                        MoreUtils.roundFloat(cy - startY, 2));
            }else {
                float dist = MoreUtils.dist(cx, cy, startX, startY);
                float rangle = (float) (Math.atan2(cy - startY, cx - startX) - angle);
                float vx = (float) (Math.cos(rangle) * dist);
                float vy = (float) (Math.sin(rangle) * dist);
                listener.onCordChange(MoreUtils.roundFloat(vx,2),
                        MoreUtils.roundFloat(vy, 2));
            }
        }
        applied = true;
        // o toliau tiesiog uzdarom.
        JointControlPointPanel.this.setVisible(false);
        controlPanel.showPanel(true);
    }

    // coord absolute
    void pointChangedPlace(float x, float y){
        float tolerance = 1f;
        cx = x;
        cy = y;
        if (angle == 0) {
            if (x-tolerance < startX && x + tolerance > startX){
                cx = startX;
            }else
                cx = x;
            if (y - tolerance < startY && y + tolerance > startY)
                cy = startY;
            else
                cy = y;
            xField.setText(MoreUtils.roundFloat(cx - startX, 2) + "");
            yField.setText(MoreUtils.roundFloat(cy - startY, 2) + "");
        }else {
            float dist = MoreUtils.dist(cx, cy, startX, startY);
            // minus angle nes is matomu stumiam pagal esama kampa.
            float rangle = (float) (Math.atan2(cy - startY, cx - startX) - angle);
            float vx = (float) (Math.cos(rangle) * dist);
            float vy = (float) (Math.sin(rangle) * dist);
            if (vx - tolerance < startX && vx + tolerance > startX){
                vx = startX;
            }
            if (vy - tolerance < startY && vy + tolerance > startY){
                vy = startY;
            }
            // dabar tik irasom.
            xField.setText(MoreUtils.roundFloat(vx, 2) + "");
            yField.setText(MoreUtils.roundFloat(vy, 2) + "");
        }

        if (jointDrawer != null){
            jointDrawer.editPoint(cx, cy, pointIndex);
        }
    }

    /* veikiamieji. */

    public void setJointDrawer(FiguresDrawer e, int index){
        if (e != null){
            if (index < e.getPointsSize()) {
                prevX = e.getPointX(index);
                prevY = e.getPointY(index);
                pointIndex = index;
            }else {
                jointDrawer = null;
                return;
            }
        }
        jointDrawer = e;
    }

    public void show(String resName, boolean fromResource, float currentX, float currentY, JointControlPointPanelListener listener){
        if (fromResource){ // polygon kunas
            SelectiveInterfaceController controller = (SelectiveInterfaceController) editForm.getController();
            for (Interface e : controller.getControls()){
                if (e instanceof Selector || e instanceof Resizer ||
                        e instanceof FieldMover || controller.getExceptionList().contains(e)){
                    continue;
                }
                // interface tinkamas, bet dar reik paziuret ar turi fizikos taskus.
                // taip pat negali but su fixed view, nu nes fizikos nesikurs ant fixed.
                if (e instanceof Element && e.getPositioning() != Window.fixedView){
                    Element owner = (Element) e;
                    if (owner.getPhysicsHolder().hasShapes() && e.getIdName().equals(resName)){ // turi fizikos taskus.
                        // KODAS PAIMTAS IS PolygonBody KLASES!
                        // Remade a little bit to fit needs.
                        PhysicsHolder physicsHolder = owner.getPhysicsHolder();
                        float startX, startY;
                        Vector2 pos = owner.getPosition();
//                        if (owner.isBodyOriginMiddle()){ // body pozicijos nutatymas.
                        if (physicsHolder.isBodyOriginMiddle){
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
                        show((pos.x+startX), (pos.y+startY), currentX, currentY, listener);
                        angle = owner.getAngle() * MathUtils.degreesToRadians;
                        return;
                    }
                }
            }
        }else { // chain kunas
            ChainEdging edging = editForm.getChainEdging();
            for (ChainEdging.Chains e : edging.getChains()){
                if (e.name.equals(resName)){
                    // radom kuna. Imam coord.
                    // chain body taskas yra pirmasis chain elementas.
                    if (e.x.size > 0){
                        show(e.x.get(0), e.y.get(0), currentX, currentY, listener);
                        angle = 0; // chain body visada 0.
                        return;
                    }
                }
            }
        }
    }

    public void show(float startX, float startY, float currentX, float currentY, JointControlPointPanelListener e){
        // visu pirma musu pagrindine panele turi buti matoma.
        if (controlPanel.isVisible()){
            this.startX = startX;
            this.startY = startY;
//            cx = startX;
//            cy = startY;
            xField.setText(MoreUtils.roundFloat(currentX, 2) + "");
            yField.setText(MoreUtils.roundFloat(currentY, 2) + "");
            if (jointDrawer == null) {
                cx = startX + currentX;
                cy = startY + currentY;
            }else {
                cx = jointDrawer.getPointX(pointIndex);
                cy = jointDrawer.getPointY(pointIndex);
            }
//            pointChangedPlace(startX + currentX, startY + currentY);
            // padarom pagrindine panele nematoma
            controlPanel.showPanel(false);
            setVisible(true); // tada jungiam sita point panele.
            inputs.changeMode(2);
            listener = e;
            applied = false;
        }
    }

    /* Override */

    @Override
    public void draw() {
        float w = 30, h = 30;
        float x = cx - w/2, y = cy - h/2;
        pointer.draw(p.getBatch(), x, y, w, h);

        p.fill(0);
        p.noStroke();
        p.ellipse(startX, startY, 10, 10);
    }

    @Override
    public boolean drop(int reason) {
        if (reason == TopPainter.formChangeDrop){
            JointControlPointPanel.this.setVisible(false);
            controlPanel.showPanel(true);
            return true;
        }
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible){
            TopPainter.addPaintOnTop(this, false);
        }else {
            TopPainter.removeTopPaint(this);
            if (!applied){
                // cia grazinam joint atgal i vieta
                if (jointDrawer != null){
                    jointDrawer.editPoint(prevX, prevY, pointIndex);
                }
            }
            jointDrawer = null; // pametam kiekviena kart.
            angle = 0;
        }
    }

    /* Reikia overridint situs metodus kitu atveju inputai nebepasiekia controlerio, todel si panele turi atlikt input darba.
    * TopPainter irgi nepadeda.. Jis vagia visus inputus is paneles. */

        @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (super.panStop(x, y, pointer, button)){
            return true;
        }
        Vector3 cord = Engine.getInstance().fixedToWorldCoords(x, y); // butinai i absolute verciam.
        return inputs.panStop(cord.x, cord.y, pointer, button);
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (super.pan(x, y, deltaX, deltaY)){
            return true;
        }
        Vector3 cord = Engine.getInstance().fixedToWorldCoords(x, y); // butinai i absolute verciam.
        return inputs.pan(cord.x, cord.y, deltaX, deltaY);
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (super.touchDown(x, y, pointer, button)){ // neduosim inputu jei paspaude ant kazka
            return true;
        }
        Vector2 pos = getPosition();
        // cia duodamos fixed koordinates, bet mums reikia absolute koordinaciu, todel reiks perverst.
        // inputai veiks tik tuo atveju jeigu spaudimas nebus paneles teritorijoj
        if (!(x > pos.x && x < pos.x + getWidth() && y > pos.y && y < pos.y + Engine.getInstance().getScreenHeight())) {
            Vector3 cord = Engine.getInstance().fixedToWorldCoords(x, y);
            return inputs.touchDown(cord.x, cord.y, pointer, button);
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (super.tap(x, y, count, button)){
            return true;
        }
        return inputs.tap(x, y, count, button); // kintamuju nenaudoja ten, tai ju net nerek
    }

    @Override
    public boolean keyDown(int keycode) {
        if (super.keyDown(keycode)){
            return true;
        }else if (keycode == Input.Keys.ESCAPE){
            JointControlPointPanel.this.setVisible(false);
            controlPanel.showPanel(true);
            return true;
        }else if (keycode == Input.Keys.ENTER){
            apply();
            return true;
        }
        return false;
    }

    /* listener */

    public interface JointControlPointPanelListener{
        void onCordChange(float x, float y);
    }

    private class CordListener implements FocusListener{

        @Override
        public void onLostFocus(Interface e) {
            float valueX, valueY;
            try {
                String n = xField.getText();
                valueX = Float.parseFloat(n);
            }catch (NumberFormatException ex){
                createError(xField);
                return;
            }
            try{
                String n = yField.getText();
                valueY = Float.parseFloat(n);
            }catch (NumberFormatException ex){
                createError(yField);
                return;
            }

            if (angle != 0) {
                float ax = startX + valueX;
                float ay = startY + valueY;
                float dist = MoreUtils.dist(startX, startY, ax, ay);
                float rAngle = (float) Math.atan2(ay - startY, ax - startX) + angle;
                valueX = (float) (Math.cos(rAngle) * dist);
                valueY = (float) (Math.sin(rAngle) * dist);

            }
            cx = valueX + startX;
            cy = valueY + startY;

            if (jointDrawer != null){
                jointDrawer.editPoint(cx, cy, pointIndex);
            }
        }

        private void createError(TextBox box){
            AlertToast toast = new AlertToast("Value must be valid number!");
            toast.show(Toast.SHORT);
            box.setText("ERROR!");
        }

        @Override
        public void onFocus(Interface e) {

        }
    }
}
