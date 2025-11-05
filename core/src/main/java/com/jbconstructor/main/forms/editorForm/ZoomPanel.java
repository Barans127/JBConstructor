package com.jbconstructor.main.forms.editorForm;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.views.Panel;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.listeners.ClickListener;

public class ZoomPanel extends Panel {
    private final EditForm editForm;

    private Label fixed, absolute, zoom;

    public ZoomPanel(EditForm form){
        editForm = form;
        setPositioning(Window.Position.fixed);
        float width, height;
        width = height = p.getScreenWidth()*0.2f;
        setSize(width, height);

        // labels
        float jumps = 20;
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.text = "fx: xxx fy: yyy";
        lst.textSize = 20;
        lst.x = jumps;
        lst.y = 6*jumps;
        // fixed cord label
        Label fixed = new Label(lst);
        fixed.setUnClickable(true); // kad neitu spaust ant jo.
        addControl(this.fixed = fixed);

        lst.text = "ax: xxx at: yyy";
        lst.y -=jumps;
        // absolute cord label
        Label absolute = new Label(lst);
        absolute.setUnClickable(true);
        addControl(this.absolute = absolute);

        lst.text = "zoom: 100%";
        lst.y -= jumps;
        // zoom label.
        Label zoom = new Label(lst);
        zoom.setUnClickable(true);
        addControl(this.zoom = zoom);

        // zoom mygtukai. center, plius, minus
        SymbolButton.SymbolButtonStyle bst = new SymbolButton.SymbolButtonStyle(); // tinka ir paprastiem mygtukam.
        bst.background = Resources.getDrawable("halfWhiteColor");
        bst.normalColor = 0x00000000;
        bst.onColor = 0x88FF0000;
        bst.pressedColor = 0xAAFF0000;
//        Resources.addImage("repositionIcon", "resources/constructorUI/physicsEditor/mainPanel/reposition.png");
        bst.symbol = Resources.getTextureDrawable("repositionIcon");
        bst.autoSize = false;
        bst.width = jumps;
        bst.height = jumps;
        bst.textSize = 25;
        bst.x = jumps;
        bst.y = jumps;
        // reposition
        SymbolButton reposition = new SymbolButton(bst);
        reposition.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                recenter();
            }
        });
        addControl(reposition);
        // minus
        bst.x += bst.width *1.5f;
        bst.text = "-";
        Button minus = new Button(bst);
        minus.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                zoomMinus();
            }
        });
        addControl(minus);
        // plius
        bst.x = jumps;
        bst.y += jumps*1.5f;
        bst.text = "+";
        Button plius = new Button(bst);
        plius.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                zoomPlius();
            }
        });
        addControl(plius);
    }

    boolean visibleCoords(){
        return fixed.isVisible();
    }

    void hideCoords(boolean visible){
        fixed.setVisible(visible);
        absolute.setVisible(visible);
    }

    void updateCoords(float x, float y){
        if (fixed.isVisible()) { // uzteks ir vieno label info. turbut...
            Vector3 fixedCords = p.screenToFixedCoords(x, y);
            fixed.setText("fx: " + round(fixedCords.x) + " fy: " + round(fixedCords.y));
            fixedCords = p.screenToWorldCoords(x, y);
            absolute.setText("ax: " + round(fixedCords.x) + " ay: " + round(fixedCords.y));
        }
    }

    void cameraUpdated(){
        OrthographicCamera cam = p.getAbsoluteCamera();
        int percent = round(100/cam.zoom);
        zoom.setText("zoom: " + percent + "%");
    }

    private int round(float value){
        return MathUtils.round(value);
    }

    private void zoomPlius(){
        float sensitivity = editForm.getZoomSensitivity();
        zoomCamera(-sensitivity);
    }

    private void zoomMinus(){
        float sensitivity = editForm.getZoomSensitivity();
        zoomCamera(sensitivity);
    }

    void recenter(){
        OrthographicCamera cam = p.getAbsoluteCamera();
        cam.position.x = p.getScreenWidth()/2f;
        cam.position.y = p.getScreenHeight()/2f;
        cam.zoom = 1f;
        editForm.updateCamera();
    }

    private void zoomCamera(float value){
        OrthographicCamera cam = p.getAbsoluteCamera();
        cam.zoom += value;
        float v = MoreUtils.abs(cam.zoom-1);
        float tvalue = MoreUtils.abs(value);
        if (v < tvalue){ // turetu but prie vieneto.
            cam.zoom = 1f;
        }
        editForm.updateCamera();
    }
}
