package com.jbconstructor.editor.forms.editorForm;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.engine.animations.Counter;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.Field;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.ControlHost;
import com.engine.ui.controls.Window;
import com.engine.ui.controls.toasts.AlertToast;
import com.engine.ui.controls.views.Panel;
import com.engine.ui.controls.widgets.Button;
import com.engine.ui.controls.widgets.CheckBox;
import com.engine.ui.controls.widgets.SymbolButton;
import com.engine.ui.listeners.ClickListener;
import com.jbconstructor.editor.managers.Project;
import com.jbconstructor.editor.root.FieldMover;
import com.jbconstructor.editor.root.Resizer;
import com.jbconstructor.editor.root.SelectiveInterfaceController;

import java.util.List;

/**
 * Created by jbara on 2018-03-29.
 */

public class EditMainPanel extends Panel {
    private final EditForm editForm;

    // paneles
    private SettingsTab settings;
    private ResourcesTab resources;
    private ChainEdgePanel chainer;
    private ZoomPanel zoomPanel;
    private AdditionalPanel additionalPanel;
    private JointControlPanel jointControlPanel;

    // aktyvioji panele.
    private ControlPanel activePanel; // cia tik tas kas yra matoma.

    // viskas tik vienam mygtukui.
    private SymbolButton fieldMover;
    private Drawable rectangle, cross;

    // combo box. visi item.
//    private ComboBox items;
//    private SymbolButton searchButton;
    private SearchBalloon searchBalloon;

    // hide
    private Hide hide;

    public EditMainPanel(final EditForm form){
        // pradiniai
        this.editForm = form;
        setPosition(p.getScreenWidth()*0.8f, 0);
        setSize(p.getScreenWidth()*0.2f, p.getScreenHeight());
        setPositioning(Window.Position.fixed);
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        setBackground(Resources.getDrawable("whiteSystemColor"));
        tintBackground(0xFF879ab7);

        // pasisalinimo mygtukas
        SymbolButton.SymbolButtonStyle bst = new SymbolButton.SymbolButtonStyle();
        bst.autoSize = false;
//        Resources.addImage("mainEditorHideKey", "resources/constructorUI/editorPanel/hideKey.png");
        bst.background = Resources.getTextureDrawable("mainEditorHideKey");
        bst.width = bst.height = p.getScreenWidth()*0.03f;
        bst.x = getPosition().x + p.getScreenWidth()*0.01f;
        bst.y = p.getScreenHeight()*0.93f;
        bst.normalColor = 0x99ef7098;
        bst.onColor = 0xDDef7098;
        bst.pressedColor = 0xFFef7098;
        bst.positioning = Window.Position.absolute;
        bst.rotatable = true;
        Button vanish = new Button("", bst);
        vanish.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                hide.act();
            }
        });
        // radio button prie pasisalinimo.
        CheckBox.CheckBoxStyle rst = new CheckBox.CheckBoxStyle();
//        Resources.addImage("mainEditorShutOffKey", "resources/constructorUI/editorPanel/shutdownOff.png");
//        Resources.addImage("mainEditorShutOnKey", "resources/constructorUI/editorPanel/shutDownOn.png");
        rst.box = Resources.getTextureDrawable("mainEditorShutOffKey");
        rst.checkedBox = Resources.getTextureDrawable("mainEditorShutOnKey");
        rst.x = bst.x + getWidth()*0.75f;
        rst.y = p.getScreenHeight()*0.87f;
        rst.positioning = Window.Position.absolute;
        rst.visible = false;
        rst.autoSize = false;
        rst.width = rst.height = bst.width;
        CheckBox disabler = new CheckBox("", rst);
        disabler.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                disableEditForm(checked);
            }
        });
        hide = new Hide(vanish, disabler);

        // main mygtukai. viska valdantis
        // selector/fieldmover button
//        Resources.addImage("allWayArrow", "resources/constructorUI/physicsEditor/mainPanel/allWayArrow.png");
        bst.normalColor = 0x00000000;
        bst.onColor = 0xFFFF5500;
        bst.pressedColor = 0xFFAA5500;
        bst.positioning = Window.Position.relative;
        bst.rotatable = false;
        bst.background = Resources.getDrawable("halfWhiteColor");
        bst.symbol = Resources.getTextureDrawable("allWayArrow");
        cross = bst.symbol;
//        Resources.addImage("mainEditorRectKey", "resources/constructorUI/editorPanel/rectButton.png");
        rectangle = Resources.getTextureDrawable("mainEditorRectKey");
        bst.width = bst.height = p.getScreenWidth()*0.02f;
        bst.x = p.getScreenWidth()*0.05f;
        SymbolButton selector = new SymbolButton(bst);
        selector.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                fieldMoverPressed();
            }
        });
        addControl(fieldMover = selector);
        // tinklelis
        float jump = p.getScreenWidth()*0.03f;
//        Resources.addImage("mainEditorGridKey", "resources/constructorUI/editorPanel/gridButton.png");
        bst.symbol = Resources.getTextureDrawable("mainEditorPointerKey");
        bst.x += jump;
        Button grid = new SymbolButton(bst);
        grid.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                focusControl(); // fokusuojam kontrole.
            }
        });
        addControl(grid);
        // to front mygtukas
//        Resources.addImage("mainEditorToFrontKey", "resources/constructorUI/editorPanel/bringToFront.png");
        bst.symbol = Resources.getTextureDrawable("mainEditorToFrontKey");
        bst.x += jump;
        Button group = new SymbolButton(bst);
        group.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                bringToFront();
            }
        });
        addControl(group);
        // to back mygtukas.
//        Resources.addImage("mainEditorToBackKey", "resources/constructorUI/editorPanel/sendToBack.png");
        bst.symbol = Resources.getTextureDrawable("mainEditorToBackKey");
        bst.x += jump;
        Button polyline = new SymbolButton(bst);
        polyline.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                sendToBack();
            }
        });
        addControl(polyline);
        // more mygtukas
//        Resources.addImage("mainEditorMoreKey", "resources/constructorUI/editorPanel/moreButton.png");
        bst.symbol = Resources.getTextureDrawable("mainEditorMoreKey");
        bst.x += jump;
        Button more = new SymbolButton(bst);
        more.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                additionalPanel.act();
            }
        });
        addControl(more);
        // save mygtukas
//        Resources.addImage("mainEditorSaveKey", "resources/constructorUI/editorPanel/saveButton.png");
        bst.symbol = Resources.getTextureDrawable("mainEditorSaveKey");
        bst.x = p.getScreenWidth() * 0.03f;
        bst.y = p.getScreenHeight() * 0.88f;
        bst.width = bst.height = p.getScreenWidth()*0.025f;
        Button save = new SymbolButton(bst);
        save.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                save();
            }
        });
        addControl(save);
        // combo box su visais esamais lauke daiktais
//        Resources.addImage("whiteRect", "resources/ui/whiteRect.png");
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
//        ComboBox.ComboBoxStyle cst = new ComboBox.ComboBoxStyle();
//        cst.background = Resources.getDrawable("systemWhiteRect");
//        cst.listBackground = Resources.getDrawable("whiteSystemColor");
//        cst.x = bst.x + jump;
//        cst.y = bst.y;
//        cst.autoSize = false;
//        cst.width = 170;
//        cst.height = 30;
//        cst.normalColor = 0xFF0000FF;
//        cst.onColor = 0xFFFF0000;
//        cst.textSize = 30;
//        cst.horizontalAlign = Align.center;
//        cst.verticalAlign = Align.center;
//        cst.defaultWord = "-non select-";
//        ComboBox items = new ComboBox(cst);
//        items.setIndexChangeListener(new ComboBox.IndexChangeListener() {
//            @Override
//            public void selectedIndexChanged(int old, int current) {
//                itemsBoxSelectItem(current);
//            }
//        });
//        addControl(this.items = items);

        // mygtukas atidaryt search bar.

        int angleNum = 8;
        bst.background = Resources.createNinePatchDrawable("defaultPopUpBackground", angleNum ,angleNum ,angleNum ,angleNum);
        bst.normalColor = 0xff00d7ff;
        bst.onColor = 0xffff0000;
        bst.textSize = 30;
        bst.symbol = Resources.getDrawable("systemWhiteRect");
        bst.position = SymbolButton.TextPosition.RIGHT;
        bst.symbolWidth = 27;
        bst.symbolHeight = 27;
        bst.x = bst.x + jump;
        bst.autoSize = false;
        bst.width = 170;
        bst.height = 30;
        bst.text = "Open";

        // atiduosim search ballon ir viskas. Anas ir handlins si.
        SymbolButton symbolButton = new SymbolButton(bst);
        symbolButton.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
//                searchBalloon.remapView();
                searchBalloon.show();
            }
        });
//        symbolButton.setCustomSymbolSizeOffset(20, 0);
        addControl(symbolButton);

        /// paneliu pridejimas.

        // settings tab.
        addControl(settings = new SettingsTab(form));
        settings.setVisible(false);

        // resources tab
        addControl(resources = new ResourcesTab(form));
        activePanel = resources;

        // chain edge panel
        addControl(chainer = new ChainEdgePanel(form));
        chainer.setVisible(false);

        // zoom panel
        addControl(zoomPanel = new ZoomPanel(form));

        // joint control panel
        addControl(jointControlPanel = new JointControlPanel(form, this));
        jointControlPanel.setVisible(false);


        // pagrindiniu mygtuku pridejimas. kad butu paciam virsuj.
        addControl(vanish);
        addControl(disabler);

        // papildoma sekcija.
        addControl(additionalPanel = new AdditionalPanel(form));
        additionalPanel.setVisible(false);

        // search balloon.
        searchBalloon = new SearchBalloon(editForm, symbolButton);
        // dabar nustatom balloon pozicija.
        Vector2 buttonPos = symbolButton.getPosition();
        float bx = buttonPos.x + symbolButton.getWidth() - searchBalloon.getWidth();
        float by = buttonPos.y - searchBalloon.getHeight();
        searchBalloon.setPosition(bx, by);
    }

    /* veikimas */

    void hide(){
        hide.act();
    }

    void save(){
        if (Project.getProjectRootFolderPath() == null){ // nera kur saugot projekto.
            editForm.formCreateDialog.open();
            editForm.formCreateDialog.getNameInput().setText(Project.getProjectName());
//            if (Project.getProjectRootFolderPath() != null)
//                editForm.formCreateDialog.getLocationInput().setText(Project.getProjectRootFolderPath());
            if (Project.getResourcesPath() != null)
                editForm.formCreateDialog.getFolderInput().setText(Project.getResourcesPath());
//                    Project.loadResourcesFromResourcesFolder(); // gal pasikeite, perzurim.
//                    Project.getSaveManager().wakeWorker(); // kad susigaudytu kas daros, pradetu auto save daryt.
        }else {
            boolean result = Project.getSaveManager().saveProject();
            if (!result){
                new AlertToast("Unknown error occurred while trying to save! Please try again!").show();
            }
        }
    }

    private void disableEditForm(boolean disable){
        if (disable){
            editForm.getSizer().releaseAll();
            editForm.checkControlInfo();
        }
        boolean enable = !disable;
        ((SelectiveInterfaceController) editForm.getController()).enableSelection(enable);
        editForm.getMover().setEnabled(true);
        editForm.getSelector().setEnabled(enable);
        fieldMoverPressed(); // disablins.
    }

    private void fieldMoverPressed(){ // fieldMoverKey paspaustas.
        if (!editForm.isSpaceDown()) {
            FieldMover mover = editForm.getMover();
            boolean state = !mover.isVisible();
            fieldMoverStateChanged(state);
            mover.setEnabled(state);
        }
    }

    private void bringToFront(){
        movingControlls(true);
    }

    private void sendToBack(){
        movingControlls(false);
    }

    /* to front or back. Viska iraso i undo redo ir numeta i gala arba prieki. */
    private void movingControlls(boolean toFront){
        Resizer r = editForm.getSizer();
        if (r.getControls().size() == 0)
            return;
        Array<Float> sett = editForm.undoController.getFlushSettings();
        sett.clear();
        sett.setSize(r.getControls().size());
        editForm.undoController.setInterfaces(r.getControls());
        ControlHost controller = editForm.getController();
        int count = 0;
        for (Control e : controller.getControls()){
            int id = 0;
            for (Control a : r.getControls()){
                if (a == e){
                    sett.set(id, (float) count);
                }
                id++;
            }
            count++;
        }
        editForm.undoController.moved(8);
        for (Control e : r.getControls()){
            ((SelectiveInterfaceController) controller).removeControlWithoutSizerUpdate(e);
            if (toFront)
//                editForm.addEditableControl(e); // bug, nes isiraso i undo kaip kontroles pridejimas
                controller.addControl(e, getController().getControls().size()-3);
            else {
                controller.addControl(e, 1);
            }
        }
    }

//    private void itemsBoxSelectItem(int id){
//        Resizer r = editForm.getSizer();
//        r.releaseAll();
//        InterfacesController e = editForm.getController();
//        r.addEditableControl(e.getControls().get(id+1)); // +1, nes pirmoj vietoj FieldMover, kuri ignoruot reiks.
//        editForm.checkControlInfo();
//
//        // pasirinkus elementa iskart nesam ant jo kamera.
//        focusControl(); // sufokusuos kamera virs pasirinktos kontroles arba virs pirmosios kontroles resizer sarase.
//    }

    private void remapComboBoxView(){
//        SelectiveInterfaceController controller = (SelectiveInterfaceController) editForm.getController();
//        items.clear();
//        for (Interface e : controller.getControls()){
//            if (e instanceof Selector || e instanceof Resizer ||
//                    e instanceof FieldMover || controller.getExceptionList().contains(e)){
//                continue;
//            }
//            items.append(e.getIdName());
//        }
        searchBalloon.remapView();
    }

    // kameros fokusavimas ant pasirinktos kontroles (is resizer saraso). Fokusuojama pirma kontrole sarase esanti ne fixed koordinaciu.
    void focusControl(){
        // sufokusuos pasirinkta kontrole
        // veikimas gan paprastas. Imam resizer. Jei resizer turi elementu, tai fokusuojam per pirmaji elementa. (resizer toks nepastovus buggy todel ne ji fokusuojam).
        // jei resizer neturi elementu. Nieko nedarom. Fixed elementu nefokusuojam, nes jie vistiek nejuda pagal absolute camera.
        Resizer resizer = editForm.getSizer();
        List<Control> array = resizer.getControls();
        if (array.size() > 0){
            // galim imtis veiksmu.
            for (int a = 0; a < array.size(); a++) { // mat ten gali but visokiu elementu, pvz fixed. Fixed ignorinam ir ieskom absolute.
                Control focusable = array.get(a); // elementas ant kurio uzsoksim.
                if (focusable.getPositioning() != Window.fixedView) { // ne fixed. fixed nereikia fokusuot.
                    // susirandam jo middle point.
                    Vector2 pos;
                    if (focusable instanceof Field) {
                        pos = ((Field) focusable).getMiddlePoint();
                    } else {
                        pos = focusable.getPosition();
                    }

                    // Jeigu elementas yra absolute, tai tobula, bet fixed negalim fokusuot.
                    Engine p = Engine.getInstance();
                    OrthographicCamera camera = p.getAbsoluteCamera(); // pasiimam musu kamera.
                    // viskas ok, ramiai dedam cord i kamera.
                    camera.position.set(pos, 0);
//                    camera.update();
                    editForm.updateCamera(); // cia updatins ne tik kamera, bet ir resizer ir kitka etc.
                    break; // sufokusavom kamera. baigiam veiksma.
                }
            }
        }
    }

    /* perdavimo linija */

    boolean isCoordsVisible(){
        return zoomPanel.visibleCoords();
    }

    public void showCoords(boolean hide){
        zoomPanel.hideCoords(hide);
    }

    void mouseCoords(float x, float y){
        zoomPanel.updateCoords(x, y);
    }

    void cameraUpdated(){
        zoomPanel.cameraUpdated();
    }

    void centerZoom(){
        zoomPanel.recenter();
    }

    void itemSelected(Control e){
//        if (e == null){
//            items.setSelectedIndex(-1);
//        }else {
//            String name = e.getIdName();
//            for (int a = 0; a < items.getItems().size(); a++){
//                if (name.equals(items.getItem(a))){
//                    items.setSelectedIndex(a);
//                    return;
//                }
//            }
//        }
        searchBalloon.itemSelected(e);
    }

    void fieldMoverStateChanged(boolean state){
        if (state){
//            fieldMover.setBackground(rectangle);
            fieldMover.setSymbol(rectangle);
        }else {
//            fieldMover.setBackground(cross);
            fieldMover.setSymbol(cross);
        }
    }

    /* paneliu atidavimas */

    public ResourcesTab getResourcesTab() {
        return resources;
    }

    public ChainEdgePanel getChainEdgePanel() {
        return chainer;
    }

    public AdditionalPanel getAdditionalPanel(){
        return additionalPanel;
    }

    /* valdymo paneliu perjunginejimas */

    /** Currently active panel. */
    public ControlPanel getActivePanel(){
        return activePanel;
    }

    /** index: 0 - resources panel.
     *  index: 1 - chain edge panel.
     *  index: 2 - joint control panel.
     *  NOTE: to open settings panel you have to call {@link #settingsChanged(Control)} method*/
    public void openControlPanel(int index, boolean releaseSizerControls){
        activePanel.setVisible(false);
        switch (index){
            case 0:// resources panel
                nonSelected();
                break;
            case 1: // chain panel
                chainer.setVisible(true);
                activePanel = chainer;
                break;
            case 2: // joint control panel
                jointControlPanel.setVisible(true);
                activePanel = jointControlPanel;
                break;
        }
        if (releaseSizerControls){
            editForm.getSizer().releaseAll(); // atzymes viska.
        }
    }

//    void openChainPanel(){
////        settings.setVisible(false);
////        resources.setVisible(false);
////        chainer.setVisible(true);
//        activePanel.setVisible(false);
//        chainer.setVisible(true);
//        activePanel = chainer;
//        editForm.getSizer().releaseAll(); // atzymes viska.
//    }

    public void settingsChanged(Control e){
        if (!settings.isVisible()){
//            settings.setVisible(true);
//            resources.setVisible(false);
//            chainer.setVisible(false);
            activePanel.setVisible(false);
            settings.setVisible(true);
            activePanel = settings;
        }
        settings.settingsChange(e);
    }

    public void nonSelected(){ // perjunks is settings i normal su images.
//        settings.setVisible(false);
//        resources.setVisible(true);
//        chainer.setVisible(false);
        activePanel.setVisible(false);
        resources.setVisible(true);
        activePanel = resources;
    }

    public void update(){
        update = true;
    }

    void updateControlPanelView(){
        activePanel.setVisible(true); // cia toks mini update.
    }

    /* override */

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (super.touchDown(x, y, pointer, button))
            return true;
        Vector2 pos = getPosition();
        return x > pos.x && x < pos.x + getWidth() && y > pos.y && y < pos.y + getHeight(); // kad resizer neatzymettu kontroliu, kai spaudziama panelio teritorijoj
    }

    @Override
    protected void sizeUpdated() {
        super.sizeUpdated();
        remapComboBoxView();
        editForm.checkControlInfo();
    }

    /* papildomos klases */
    private class Hide implements Counter.CounterInformer, Counter.CounterListener{
        private Counter counter;
        private boolean isHidden;
        private float width, startPointX, startPointY;
        private float startOwnerX, startOwnerY;
        private Button owner;
        private CheckBox shutter;
        private float shutterY;
        private float animationTime = 0.5f;

        Hide(Button owner, CheckBox shutter){
            counter = MoreUtils.getCounter();
            counter.setCounterInformer(this);
            counter.setCounterListiner(this);
//            counter.setIrrevocable(true);
            // pasikeite pavadinimas i toki.
            counter.setUninterruptible(true);
            width = EditMainPanel.this.getWidth();
            startPointX = EditMainPanel.this.getPosition().x;
            startPointY = EditMainPanel.this.getPosition().y;
            this.owner = owner;
            this.shutter = shutter;
            shutterY = shutter.getPosition().y;
            owner.auto();
            owner.setOriginMiddle();
            startOwnerX = owner.getPosition().x;
            startOwnerY = owner.getPosition().y;
        }

        /** time in seconds. */
        public void setAnimationTime(float time){
            animationTime = time;
        }

        public void act(){ // main panelio slepimo animacija.
            if (!counter.isCounting()){
                if (isHidden){ // atsiradimas
                    counter.startCount(width, 0, animationTime);
                    shutter.disappear(animationTime);
                    isHidden = false;
                    if (shutter.isChecked()){
//                        shutter.onClick();
                        // jeigu pazymetas, tai atzymesim.
                        shutter.setChecked(false);
                        shutter.getCheckListener().onCheck(false); // kad issijungtu viskas.
                    }
                }else { // slepimas.
                    counter.startCount(0, width, animationTime);
                    shutter.appear(animationTime);
                    isHidden = true;
                }
            }
        }

        @Override
        public void update(float oldValue, float currentValue) {
            EditMainPanel.this.setPosition(startPointX + currentValue, startPointY);
            owner.setPosition(startOwnerX + currentValue*0.75f, startOwnerY); // sits slinksis tik 75% viso slinkimo dydzio.
            shutter.setPosition(startOwnerX + currentValue*0.75f, shutterY);
            float percent = currentValue*100f/width;
            float radius = 180 * (percent/100f);
//            owner.setRadius(radius);
            owner.setAngle(radius);
            // updatinam image uzkrovimo mygtuka (anas tai absolute idetas, tai reik manual kelt ana).
            resources.mainPanelMoved(currentValue);
        }

        @Override
        public void finished(float currentValue) {
            settings.setPosition(0, 0);
            resources.setPosition(0, 0);
        }

        @Override
        public boolean cancel(int reason) {
            return false;
        }
    }

    public interface ControlPanel{
        void setVisible(boolean visible);
    }
}
