package com.jbconstructor.editor.forms.editorForm;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.PopUp;
import com.engine.ui.controls.dialogs.ConfirmDialog;
import com.engine.ui.controls.dialogs.PreparedRightClickMenu;
import com.engine.ui.controls.toasts.AlertToast;
import com.engine.ui.controls.views.ListView;
import com.engine.ui.controls.widgets.Button;
import com.engine.ui.controls.widgets.Label;
import com.engine.ui.controls.widgets.ScrollBar;
import com.engine.ui.controls.widgets.SymbolButton;
import com.engine.ui.controls.widgets.TextBox;
import com.engine.ui.listeners.FocusListener;
import com.jbconstructor.editor.dialogs.AnimationManager;
import com.jbconstructor.editor.editors.CopyManager;
import com.jbconstructor.editor.editors.MoveController;
import com.jbconstructor.editor.root.Element;
import com.jbconstructor.editor.root.Resizer;

import java.util.List;

public class RightClickManager extends PreparedRightClickMenu implements PreparedRightClickMenu.MeniuItemClickedListener{

    /*
    * Kokie meniu bus:
    * Change resource
    * keep ratio - don't keep ratio
    * (visi punktai su animacija bus tik demonstraciniai. I save file tai neis. cia tik perziurai)
    * play animation - stop animation (padarysim, kad animacija neitu pradziai, gales paleist jei nores).
    * manage animation (speed, kuri animacija ir t.t. reik kito dialogo).
    * (toliau normalus vel)
    * flip horizontally
    * flip vertically
    * lock image - unlock image? - (uzrakintas negaletu judet).
    */

    private EditForm editForm; // musu edit forma kuriai priklausom

    private boolean lock;
    private AnimationManager animationManager;
    private CameraInfoPopUp cameraInfoPopUp;

    public RightClickManager(EditForm editForm){

        this.editForm = editForm;

//        getController().setBackground(Resources.getDrawable("whiteSystemColor"));
//        getController().tintBackground(0x00000000);

//        getStyle().textSize = 30;
        SymbolButton.SymbolButtonStyle st = getStyle();
        st.textSize = 30;
        st.normalColor = 0xffaaaaaa;
        st.disabledColor = st.normalColor;
        st.horizontalAlign = Align.left;
        st.textOffsetX = 10;
        st.symbolWidth = 29;
        st.symbolHeight = 29;
//        st.background = Resources.getDrawable("halfWhiteColor");
        ListView listView = (ListView) getController();
        listView.setItemSize(259, 40);
        listView.setBackground(Resources.getDrawable("whiteSystemColor"));
        listView.tintBackground(0xffaaaaaa);
        setSize(260, 200);

        Drawable noLine = st.background;
        WhiteDrawable blackBottomLine = new WhiteDrawable((TextureRegionDrawable) st.background);

        st.background = blackBottomLine;
        addListItem("Camera info");
        st.background = noLine;
        addListItem("Copy", Resources.getDrawable("copyIcon"), 0xffffffff);
        st.background = blackBottomLine;
        addListItem("Paste", Resources.getDrawable("pasteIcon"), 0xffffffff);
        st.background = noLine;
//        addListItem("Undo");
//        st.background = whiteDrawable;
//        addListItem("Redo");
//        st.background = old;
        addListItem("-Change resource");
        addListItem("Keep ratio");
        addListItem("Play animation");
        addListItem("Manage animation");
        addListItem("Flip horizontally");
        addListItem("Flip vertically");
        st.background = blackBottomLine;
        addListItem("Lock");
        st.background = noLine;
//        st.textColor = 0xffff4444;
        addListItem("Delete", Resources.getDrawable("defaultColorPickCross"), 0xffffffff);

        setMenuItemClickListener(this);

        animationManager = new AnimationManager();
        cameraInfoPopUp = new CameraInfoPopUp();
    }

    @Override
    protected boolean beforeRightClickOpen(Control resizer, float x, float y, float offsetX, float offsetY) {
//        if (resizer != null){ // test.
//            float lengh = getController().getItemHeight() * 10;
//            setSize(getController().getWidth(), lengh);
//            getController().setScrollOffset(0, lengh*2); // cia tiesiog apeinam tai, kad paslepia kai kur eilutes...
//            return false;
//        }
        Resizer e = (Resizer) resizer;
        int controlSize = e.getControls().size();
        int size = 3; // camera info, copy ir paste visada bus rodomi.
        // dar paziurim ar galim paste daryt.
//        setControlEnabled(3, editForm.getCopyManager().isCopied()); // nustatom pagal tai.
//        Button paste = (Button) getController().getHost().getControls().get(3); // paste.
//        boolean isCopied = editForm.getCopyManager().isCopied();
//        paste.setEnabled(isCopied);
//        paste.setTextColor(isCopied ? 0xff000000 : 0xff444444);
        disableItem(3, editForm.getCopyManager().isCopied());
        if (controlSize > 0) {
//            setControlEnabled(2, true); // copy. kad butu enabled.
//            Button copy = (Button) getController().getHost().getControls().get(2); // copy.
//            copy.setEnabled(true);
//            copy.setTextColor(0xff000000);
            disableItem(2, true);
            if (controlSize == 1) {
                Control owner = e.getControls().get(0);
                if (owner instanceof Element) {
                    setControlVisible(4, true); // change resource.
                    Button resource = (Button) getController().getHost().getControls().get(4);
                    resource.setText("Change resource");
                    setControlVisible(5, true); // keep ratio.
                    size += 2;
                    // cia keiciam ratio parametra.
                    boolean ratio = ((Element) owner).isKeepRatio();
                    Button button = (Button) getController().getHost().getControls().get(5); // keep ratio.
                    if (ratio) {
                        button.setText("Don't keep ratio");
                    } else {
                        button.setText("Keep ratio");
                    }
                    // toliau ziurim ar animacija
                    if (((Element) owner).getImage() instanceof SpriterDrawable) {
                        setControlVisible(6, true); // play animation.
                        setControlVisible(7, true); // manage animation.
                        size += 2;
                        // paziurim ar animcija eina
                        SpriterDrawable drawable = (SpriterDrawable) ((Element) owner).getImage();
                        Button anime = (Button) getController().getHost().getControls().get(6); // play animation/
                        if (drawable.isUpdateEnabled()) {
                            anime.setText("Stop animation");
                        } else {
                            anime.setText("Play animation");
                        }
                    } else {
                        setControlVisible(6, false); // play animation.
                        setControlVisible(7, false); // manage animation.
                    }
                    size += 4;
                    // ziurim ar uzrakinta
                    Button lock = (Button) getController().getHost().getControls().get(10); // lock mygtukas.
                    if (((Element) owner).isLocked()) {
                        lock.setText("Unlock");
                        this.lock = false;
                    } else {
                        lock.setText("Lock");
                        this.lock = true;
                    }

                    // like.
                    setControlVisible(8, true); // horizontal
                    setControlVisible(9, true); // vertical
                    setControlVisible(10, true); // lock.
                    setControlVisible(11, true); // delete.
                }else {
                    setControlVisible(4, true); // change resource
                    Button resource = (Button) getController().getHost().getControls().get(4);
                    resource.setText("Resources management");
                    size++;
                    setControlVisible(5, false); // keep ratio
                    setControlVisible(6, false); // play animation
                    setControlVisible(7, false); // manage animation
                    setControlVisible(8, false); // horizontal
                    setControlVisible(9, false); // vertical
                    setControlVisible(10, false); // lock.
                    setControlVisible(11, false); // delete.
                }
            }else {
                setControlVisible(4, true); // change resource
                Button resource = (Button) getController().getHost().getControls().get(4);
                resource.setText("Resources management");
                size++;
                setControlVisible(5, false); // keep ratio
                setControlVisible(6, false); // play animation
                setControlVisible(7, false); // manage animation
                setControlVisible(8, true); // horizontal
                setControlVisible(9, true); // vertical
                setControlVisible(10, true); // lock.
                setControlVisible(11, true); // delete.
                Button lock = (Button) getController().getHost().getControls().get(10); // lock
                boolean shouldBeLocked = false;
                for (int a = 0; a < e.getControls().size(); a++){
                    Control face = e.getControls().get(a);
                    if (face instanceof Element && !((Element) face).isLocked()){
                        shouldBeLocked = true;
                        break;
                    }
                }
                if (shouldBeLocked){
                    lock.setText("Lock");
                    this.lock = true;
                }else {
                    lock.setText("Unlock");
                    this.lock = false;
                }
                size += 4;
            }
        }else {
//            setControlEnabled(2, false); // copy. ner ka kopijuot.
//            Button copy = (Button) getController().getHost().getControls().get(2); // copy.
//            copy.setEnabled(false);
//            copy.setTextColor(0xff444444);
            disableItem(2, false);

            setControlVisible(4, true); // change resource
            Button resource = (Button) getController().getHost().getControls().get(4);
            resource.setText("Resources management");
            size++;
            setControlVisible(5, false); // keep ratio
            setControlVisible(6, false); // play animation
            setControlVisible(7, false); // manage animation
            setControlVisible(8, false); // horizontal
            setControlVisible(9, false); // vertical
            setControlVisible(10, false); // lock.
            setControlVisible(11, false); // delete.
        }

        ListView listView = (ListView) getController();
        float lengh = listView.getItemHeight() * size;
        setSize(getController().getWidth(), lengh);
        listView.setScrollOffset(0, lengh*2); // cia tiesiog apeinam tai, kad paslepia kai kur eilutes...
        return false;
    }

    private void disableItem(int id, boolean enable){
        Button copy = (Button) getController().getHost().getControls().get(id); // copy.
        copy.setEnabled(enable);
        copy.setTextColor(enable ? 0xff000000 : 0xff444444);
    }

    @Override
    public void clickedItem(Control owner, int index, String itemName) {
        Resizer e = editForm.getSizer();
        switch (index){
            case 0: // camera info.
//                new CameraInfoPopUp().open();
                cameraInfoPopUp.open();
                break;
            case 1: // copy
                if (e.getControls().size() > 0) {
                    CopyManager copyManager = editForm.getCopyManager();
                    copyManager.copy(e.getControls());
                }
                break;
            case 2:// paste
                editForm.paste();
                break;
            case 3: // change resource.
//                new ResourcesDialog(editForm).open();
//                editForm.getResourcesDialog().open();
                int size = e.getControls().size();
                if (size != 1){
                    editForm.getResourcesDialog().show();
                }else {
                    Control in = e.getControls().get(0);
                    if (in instanceof Element){
                        editForm.getResourcesDialog().show((Element) in);
                    }else {
                        editForm.getResourcesDialog().show(); // rodom paprasta.
                    }
                }
                break;
            case 4: { // keep ratio
//                Resizer e = (Resizer) owner;
                if (e.getControls().size() > 0){
                    Control face = e.getControls().get(0); // tik vienam.
                    if (face instanceof Element){
                        Element resource = (Element) face;
                        resource.setKeepRatio(!resource.isKeepRatio());
                    }
                }
                break;
            }case 5: {// stop animation
//                Resizer e = (Resizer) owner;
                if (e.getControls().size() > 0){
                    Control face = e.getControls().get(0); // tik vienam.
                    if (face instanceof Element){
                        Drawable drawable = ((Element) face).getImage();
                        if (drawable instanceof SpriterDrawable){
                            SpriterDrawable spriterDrawable = (SpriterDrawable) drawable;
                            spriterDrawable.enableUpdate(!spriterDrawable.isUpdateEnabled());
                        }
                    }
                }
                break;
            }case 6: {//manage animation
//                animationManager.open();
//                Resizer e = (Resizer) owner;
                if (e.getControls().size() > 0){
                    Control f = e.getControls().get(0);
                    if (f instanceof Element){
                        Drawable drawable = ((Element) f).getImage();
                        if (drawable instanceof SpriterDrawable){
                            animationManager.show((SpriterDrawable) drawable);
                        }else {
                            ConfirmDialog confirmDialog = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.OK);
                            confirmDialog.show("Resource image is not SpriterDrawable!");
                        }
                    }
                }
                break;
            }case 7: {//flip horizontal
//                Resizer e = (Resizer) owner;
                List<Control> controls = e.getControls();
                if (controls.size() > 0) {
                    for (int a = 0; a < controls.size(); a++) {
                        Control f = controls.get(a);
                        if (f instanceof Element) {
                            Element resource = (Element) f;
                            resource.setFlip(!resource.isFlippedX(), resource.isFlippedY());
                        }
                    }

                    // dabar viska i undo metam.
                    MoveController undo = editForm.undoController;
                    undo.setInterfaces(controls);
                    undo.moved(16); // tiesiog idedam id. Nieko daugiau. flipas vistiek darosi atbuliniu variantu (!isFlipped).
                }
                break;
            }case 8: {// flip vertical
//                Resizer e = (Resizer) owner;
                List<Control> controls = e.getControls();
                if (controls.size() > 0) {
                    for (int a = 0; a < controls.size(); a++) {
                        Control f = controls.get(a);
                        if (f instanceof Element) {
                            Element resource = (Element) f;
                            resource.setFlip(resource.isFlippedX(), !resource.isFlippedY());
                        }
                    }

                    // dabar viska i undo metam.
                    MoveController undo = editForm.undoController;
                    undo.setInterfaces(controls);
                    undo.moved(17); // tiesiog idedam id. Nieko daugiau. flipas vistiek darosi atbuliniu variantu (!isFlipped).
                }
                break;
            }case 9: {// lock
//                Resizer e = (Resizer) owner;
                for (int a = 0; a < e.getControls().size(); a++) {
                    Control f = e.getControls().get(a);
                    if (f instanceof Element) {
                        Element resource = (Element) f;
                        resource.lock(lock);
                    }
                }
                e.autoSize(); // turi susigaudyt kas ivyko. uzrakint viduj viska.
                break;
            } case 10:{ // delete.
                editForm.deleteSelectedItems();
                break;
            }
        }
    }

    private class WhiteDrawable extends TextureRegionDrawable {

        WhiteDrawable(TextureRegionDrawable e){
            super(e);
        }

        @Override
        public void draw(Batch batch, float x, float y, float width, float height) {
            super.draw(batch, x, y, width, height);

            // piesiam juoda linija apacioj.
            TextureRegion e = getRegion();
            Engine p = Engine.getInstance();
            p.tint(0xff000000);
//            e.setPosition(x, y);
//            e.setSize(width, height*0.1f);
//            e.draw(batch);
            batch.draw(e, x, y, width, height*0.03f);
        }
    }

    private class CameraInfoPopUp extends PopUp implements FocusListener, ScrollBar.ScrollListener {
        private Label zoom; // zoom label.

        private TextBox x, y;
        private ScrollBar scrollBar;

        CameraInfoPopUp(){
            super(800, 272);

            // labeliai
            // title label.
            Label.LabelStyle lst = new Label.LabelStyle();
            lst.autoSize = false;
            lst.x = 22;
            lst.y = 207;
            lst.width = 371;
            lst.height = 55;
            lst.textSize = 55;
            lst.text = "Camera info";

            lst.verticalAlign = Align.center;

            // title lable kurimas
            addControl(lst.createInterface());

            // x label
            lst.y = 136;
            lst.text = "x:";
            lst.horizontalAlign = Align.center;
            lst.width = 100;

            // x label kurimas
            addControl(lst.createInterface());

            // y labelis.
            lst.x = 422;
            lst.text = "y:";

            // y label kurimas
            addControl(lst.createInterface());

            // zoom label
            lst.x = 22;
            lst.y = 91;
            lst.horizontalAlign = Align.left;
            lst.width = 400;
            lst.height = 33;
            lst.textSize = 32;
            lst.text = "Zoom: 1x";

            // zoom label kurimas.
            zoom = new Label(lst);
            addControl(zoom);

            // x text box
            TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
            tst.autoSize = false;
            tst.background = Resources.getDrawable("whiteSystemColor");
            tst.x = 146;
            tst.y = 136;
            tst.width = 207;
            tst.height = 55;
            tst.textSize = 55;
            tst.horizontalAlign = Align.center;
            tst.verticalAlign = Align.center;

            // x textbox kurimas
            x = new TextBox(tst);
            x.setFocusListener(this);
            addControl(x);

            // y text box
            tst.x = 539;

            // y textbox kurimas
            y = new TextBox(tst);
            y.setFocusListener(this);
            addControl(y);

            // scroll juosta.
            ScrollBar.ScrollBarStyle sst = new ScrollBar.ScrollBarStyle();
            sst.autoSize = false;
            sst.x = 28;
            sst.y = 36;
            sst.width = 750;
            sst.height = 33;
            sst.normalColor = 0xFFAAAAAA;
            sst.onColor = 0xFFFF0000;
            sst.pressedColor = 0xFF0000FF;
            sst.tintBackground = 0xFF111111;
            sst.fixedStop = false;
            sst.background = Resources.createNinePatchDrawable("defaultScrollBarLine", 22, 22, 0 ,0);
            sst.scrollBar = Resources.getDrawable("defaultPopUpBackground"); // get background netink, nes ten 9patch.

            sst.jumps = 200; // imsim procentaliai.

            // scroll juostos kurimas
            ScrollBar scrollBar = new ScrollBar(sst);
            scrollBar.setScrollListener(this);
            addControl(scrollBar);
            this.scrollBar = scrollBar;
        }

        /* ant atidarymo. */

        @Override
        protected void onOpen() {
            super.onOpen();

            OrthographicCamera camera = p.getAbsoluteCamera();
            x.setText(MoreUtils.roundFloat(camera.position.x, 2) + "");
            y.setText(MoreUtils.roundFloat(camera.position.y, 2) + "");

            zoom.setText("Zoom: " + MoreUtils.roundFloat(camera.zoom, 2) + " - " + (MathUtils.round(100/camera.zoom)) + "%");

            // toliau apskaiciuojam zoom dydi.
            float min = editForm.getMinZoom();
            float max = editForm.getMaxZoom();
            float dif = min - max; // min visada didesnis. 100%

            float percent = (camera.zoom/dif) * scrollBar.getJumps(); // prcentas. ir iskart verciam i jump.
            scrollBar.setValue((int) percent);
        }

        /* listeneriai */

        @Override
        public void onLostFocus(Control e) {
            // cia user keite coord terp x arba y text boxo.
            OrthographicCamera camera = p.getAbsoluteCamera();
            try {
                String x = this.x.getText().trim();
                String y = this.y.getText().trim();
                float fx = Float.parseFloat(x);
                float fy = Float.parseFloat(y);

                camera.position.set(fx, fy, 0);
                editForm.updateCamera();

            }catch (NumberFormatException ex){
                AlertToast toast = new AlertToast("Failed converting to number.");
                toast.show();

                x.setText(MoreUtils.roundFloat(camera.position.x, 2)+"");
                y.setText(MoreUtils.roundFloat(camera.position.y, 2)+"");
            }
        }

        @Override
        public void onFocus(Control e) {}

        // kviecia kai user keicia scroll bar.
        @Override
        public void onScroll(int currentValue) {
            float min = editForm.getMinZoom();
            float max = editForm.getMaxZoom();
            float dif = min - max; // min visada didesnis. 100%

            // apskaiciuojam cameros esama dydi.
            float jumps = scrollBar.getJumps();
            float percent = MoreUtils.roundFloat(currentValue/jumps * dif, 1);

            OrthographicCamera camera = p.getAbsoluteCamera();

            // palengvinam, kad uzsoktu ant 100%
            if (percent > 0.95f && percent < 1.05f) {
                percent = 1f;
            }

            // sumetam viska.
            camera.zoom = percent;
            editForm.updateCamera();

            // update text.
            zoom.setText("Zoom: " + MoreUtils.roundFloat(camera.zoom, 2) + " - " + (MathUtils.round(100/camera.zoom)) + "%");

        }
    }
}
