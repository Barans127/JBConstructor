package com.jbconstructor.main.forms.editorForm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Form;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.jbconstructor.main.dialogs.ColorPicker;
import com.jbconstructor.main.dialogs.FormCreateDialog;
import com.jbconstructor.main.dialogs.ResourcesDialog;
import com.jbconstructor.main.dialogs.SwitchFormsDialog;
import com.jbconstructor.main.dialogs.physicsEditor.PhysicsEditor;
import com.jbconstructor.main.editors.ChainEdging;
import com.jbconstructor.main.editors.CopyManager;
import com.jbconstructor.main.root.FieldMover;
import com.jbconstructor.main.editors.JointManager;
import com.jbconstructor.main.editors.MoveController;
import com.jbconstructor.main.root.Resizer;
import com.jbconstructor.main.root.SelectiveInterfaceController;
import com.jbconstructor.main.root.Selector;
import com.jbconstructor.main.managers.Project;

public class EditForm extends Form {
    private Engine p = Engine.getInstance();
    private float visibleWidth, visibleHeight;
//    private String formName;

    // selectoriai ir editoriai
    private Resizer sizer;
    private FieldMover mover;
    private Selector selector;

    private EditMainPanel mainPanel;

    // aktyvieji
    // nustatyt ar nuspaustas space down ir ar screen bounds matomas.
    private boolean spaceDown, showScreenBounds = true;
    private Drawable whiteColor;
    private int screenBoundsColor; // screen bount spalva.
    private float lineWidth; // screen bound storis.
    private String prefixId; // prefix naudojamas kurt naujus elementus.
    private boolean rightClick; // nustatyt ar galim ijungt right click panele.

    // dialogs
    final ColorPicker colorPicker;
    public final PhysicsEditor physicsEditor;
    final SwitchFormsDialog formsDialog;
    final FormCreateDialog formCreateDialog;
    private RightClickManager rightClickManager;
    private ResourcesDialog resourcesDialog; // resource valdymas.

    // zoom settings
    private float zoomSensitivity, maxZoom, minZoom;

    // undo redo
    MoveController undoController;

    // copy paste controleris
    private CopyManager copyManager;

    // joint manager
    private JointManager jointManager;

    // chain manager
    private ChainEdging chainEdging;

    // listeneriai
    // selective listener, leidziantis kitoms dalims gaut inputa.
    private SelectiveInterfaceController.SelectedInterfaceListener outListener;

    public EditForm(String formName){
        super(new SelectiveInterfaceController());

        // keiciam bacground is 255 i ffffffff.
        setBackgroundColor(0xffffffff); // kitu atveju color pickeris kvaila spalva rodo.

        // config nustatymai.
        visibleWidth = Resources.getPropertyFloat("visibleScreenWidth", p.getScreenWidth());
        visibleHeight = Resources.getPropertyFloat("visibleScreenHeight", p.getScreenHeight());
        zoomSensitivity = Resources.getPropertyFloat("zoomSensitivity", 0.1f);
        maxZoom = Resources.getPropertyFloat("maxZoom", 0.1f);
        minZoom = Resources.getPropertyFloat("minZoom", 4);
        lineWidth = Resources.getPropertyFloat("screenBoundsLineWeight", 3f);
        try {
            screenBoundsColor = Integer.parseUnsignedInt(Resources.getProperty("screenBoundsLineColor", "FFFF5500"), 16);
        }catch (NumberFormatException ex){
            screenBoundsColor = 0xFFFF5500;
        }
        whiteColor = Resources.getDrawable("whiteSystemColor");
        // manageriai ir pop upai, valdymui.
        colorPicker = new ColorPicker();
        undoController = new MoveController();
        copyManager = new CopyManager();
        jointManager = new JointManager();
        chainEdging = new ChainEdging(undoController);
//        addPopUp(colorPicker); // engine nebenaudoja addPopUp metodo.
        // selectoriai
        {
            // selection controller
            SelectiveInterfaceController controller = (SelectiveInterfaceController) getController();
            controller.setMainForm(this);
            // selectorius
            Selector selector = new Selector();
            addControl(this.selector = selector);
            // resizeris
            Resizer resizer = new Resizer(undoController);
            resizer.setPositioning(Position.fixed);
            resizer.setSize(100, 100);
            resizer.placeInMiddle(640, 360);
            addControl(this.sizer = resizer);

            // paprastų paspaudimų selectas
            controller.setResizer(this.sizer);
            controller.setSelector(selector);

            controller.setSelectionListener(new SelectiveInterfaceController.SelectedInterfaceListener() {
                @Override
                public void interfaceSelected(Interface e) {
                    if (outListener != null){
                        outListener.interfaceSelected(e);
                    }else {
                        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                            if (sizer.containsControl(e)) {
                                sizer.removeEditableControl(e);
                            } else {
                                sizer.addEditableControl(e);
                            }
                        } else {
                            sizer.releaseAll();
                            sizer.addEditableControl(e);
                            sizer.autoSize(); // bug kai kontrole grizta ne ten kur turetu.
                            Vector3 cord = p.screenToFixedCoords(Gdx.input.getX(), Gdx.input.getY()); // fixed, nes resizer yra fixed.
                            sizer.imitateClick(cord.x, cord.y); // leis vos tik pasirinkus controle ja dragint. be sito nepatogiai gaunas.
                            // be sito pasizymi control, bet nesidragina. reik dar karta spaust ant jos.
                        }
                        checkControlInfo();
                    }
                }
            });
            // field moveris.
            mover = new FieldMover();
            controller.addException(mover);
            mover.setPositioning(Position.fixed);
            addControl(mover);
            mover.setEnabled(false);
            mover.setResizer(sizer);
        }

        // phyisics editor panele.
        physicsEditor = new PhysicsEditor();

        // form switch pop up.
        formsDialog = new SwitchFormsDialog(formName);

        // save as pop up. Keist info ir t.t.
        formCreateDialog = new FormCreateDialog();
        formCreateDialog.getMainLabel().setText("Change project info");
        formCreateDialog.getAcceptButton().setText("Change");
        formCreateDialog.setFormCreateListener(new FormCreateDialog.CreateFormDialogListener() {
            @Override
            public boolean onCreate(String name, String rootLocation, String folderPath) {
                if (name == null || name.length() == 0) {
//                    error = "Project name cannot be null or zero length";
                    return false;
                }
                boolean hasDirectory = false;
                if (rootLocation != null && rootLocation.length() > 0){
                    hasDirectory = true; // TODO Reik gi patikrint ar jau kartais toks neegzistuoja.
                }
                String path;
                if (folderPath != null && folderPath.length() > 0){
                    path = folderPath;
                }else {
                    path = hasDirectory ? rootLocation : null;
                }
//                boolean newDirectory = Project.getProjectRootFolderPath() == null && hasDirectory; // ar pries tai nebuvo ir dabar nauja nustate.
//                Project.getFormsManager().createEditForm(projectName);
                Project.setProjectName(name);
                Project.setProjectRootFolderPath(hasDirectory ? rootLocation : null);
                Project.setResourcesPath(path);
                Project.loadResourcesFromResourcesFolder(); // dar karta praskenuos. gal ka ras.
                Gdx.graphics.setTitle("JBConstructor - " + name); // pakeiciam i nauja title.
                if (!Project.getSaveManager().saveProject()) { // po visko reik issaugot projekta.
                    new AlertToast("Unknown error occurred while trying to save! Please try again!").show();
                    Project.getSaveManager().triggerSave(); // pazymim, kad neissaugota.
                }
//                if (Project.getSaveManager().isProjectSaved()){
//                    StartForm.projectLoaded(name, rootLocation); // projektas issisaugojo, viskas gerai.
//                }
//                if (newDirectory){
//                    StartForm.projectLoaded(name, rootLocation); // pazymim, kad sitas buvo atidarytas.
//                }
//                Project.loadResourcesFromResourcesFolder(); // gal pasikeite, perzurim.
//                Project.getSaveManager().wakeWorker(); // kad susigaudytu kas daros, pradetu auto save daryt.
                return true;
            }

            @Override
            public void cancel() {}
        });

        // pagrindine panele. Matoma pati pirma.
        EditMainPanel main = new EditMainPanel(this);

        // idedam sia panele i exceptionus, kad nevaldytume kaip paprasto daikto.
        ((SelectiveInterfaceController) getController()).addException(main);
        addControl(mainPanel = main);

        // resizer listiner. kai keicia cord, dydi ir kitka.
        sizer.setChangeListener(new Resizer.ChangeListener() {
            @Override
            public void onSizeChange(Interface e, float x, float y, float width, float height) { } // nope

            @Override
            public void onPositionChange(Interface e, float x, float y) {} // nope

            @Override
            public void onChange(float x, float y, float width, float height, boolean isMulti) {
                if (isMulti){
                    mainPanel.settingsChanged(sizer);
                    mainPanel.itemSelected(null);
                }else {
                    mainPanel.settingsChanged(sizer.getControls().get(0)); // pirma kontrole.
                    mainPanel.itemSelected(sizer.getControls().get(0));
                }
            }

            @Override
            public void onRotation(float old, float rnew){
                if (sizer.hasMultipleControls()){
                    mainPanel.settingsChanged(sizer);
                    mainPanel.itemSelected(null);
                }else {
                    mainPanel.settingsChanged(sizer.getControls().get(0));
                    mainPanel.itemSelected(sizer.getControls().get(0));
                }
            }
        });

        // right click menu.
        rightClickManager = new RightClickManager(this);

        resourcesDialog = new ResourcesDialog(this);
//        sizer.setRightClickListener(rightClickManager);
    }

    // working

    /** prefix id to be used when adding new entities. */
    public void setPrefixId(String id){
        if (id != null && id.length() == 0){
            id = null;
        }
        prefixId = id;
    }

    /** Updates panels information about which entity selected etc. */
    public void checkControlInfo() {
        if (sizer.hasMultipleControls()) {
            mainPanel.settingsChanged(sizer);
            mainPanel.itemSelected(null);
        } else if (sizer.getControls().size() == 0){
            mainPanel.nonSelected();
            mainPanel.itemSelected(null);
        }else {
            Interface e = sizer.getControls().get(0);
            mainPanel.settingsChanged(e);
            mainPanel.itemSelected(e);
        }
    }

//    // demo
//
//    private void demonstrationControls(){
//        Resources.addImage("imgSystemImage", "textures/badlogic.jpg");
//        img = new SImage(Resources.getImage("imgSystemImage"));
//        img.setPosition(100, 100);
//
////        addControl(img);
//
//        SImage img2 = new SImage(Resources.getImage("imgSystemImage"));
//        img2.setPosition(200, 200);
//        img2.setSize(300, 200);
//        img2.setEnabled(false);
//        img2.setPositioning(Position.fixed);
////        img2.setRotatable(false);
//        addEditableControl(img);
//        addEditableControl(img2);
//    }

    /** Adds editable entity to form. */
    public void addEditableControl(Interface e){
        // zemiau 3, nes virsuj resizer, fieldmover ir valdymo panele.
        addControl(e, getController().getControls().size()-3);
        mainPanel.update();
        Array<Interface> list = undoController.getList();
        list.clear();
        list.add(e);
        // additional setting tik, kad nekiltu bug ir nemestu null pointer exception. nieko nereiskia sitoj vietoj.
        undoController.moved(1, 0 ); // interface pridejimas.
    }

    /** Updates combo box list with about existing entities. */
    public void updateList(){
        if (mainPanel != null) {
            mainPanel.update();
        }
    }

    /** If you manipulate with camera settings then use this method for camera update. It updates info
     * visible on panels. */
    public void updateCamera(){
        OrthographicCamera camera = p.getAbsoluteCamera();
        if (camera.zoom < maxZoom){
            camera.zoom = maxZoom;
        }else if (camera.zoom > minZoom){
            camera.zoom = minZoom;
        }
        camera.update();
        mainPanel.cameraUpdated(); // pranes apie pokycius.
        sizer.update();
    }

    /** paste interfaces from copy manager. If there are no copies then nothing happens.
     * Paste interfaces. Inserts them into edit form and notes everything to Undo redo controller. */
    public void paste(){
        Array<Interface> copies = copyManager.paste();
        undoController.getFlushSettings().clear();
        if (copies.size > 0){ // reik irgi kazka turet.
            undoController.setInterfaces(copies);
            sizer.releaseAll(); // paleidziam visas pazymetas.
            for (Interface e : copies){
                undoController.getFlushSettings().add(0f);
//                            addEditableControl(e); // idedam nauja i forma
                addControl(e, getController().getControls().size()-3);
                // wuts dis??
//                            checkResourceDrawable(e); // tiesiog pazes ar ne spriter drawable.
                sizer.addEditableControl(e); // pazymim.
            }
            undoController.moved(1);
            mainPanel.update();
        }
    }

    /** Deletes all selected items. If itemas are not selected then nothing happens.
     * Notes changes to undo controller. */
    public boolean deleteSelectedItems(){
        if (sizer.getControls().size() > 0){
            undoController.setInterfaces(sizer.getControls()); // nustatom ka istrinam
            undoController.getFlushSettings().clear(); // del viso pikto padarom tuscia
            for (Interface e : sizer.getControls()){
                int count = 0;
                for (Interface a : getController().getControls()){
                    if (e == a){
                        undoController.getFlushSettings().add((float) count); // sugaudom trinamu interface vietos id.
                    }
                    count++;
                }
            }
            undoController.moved(9, getController()); // nustatom, kad kontroles yra istrintos.
            for (Interface e : sizer.getControls()){ // trinam visas kontroles is formos.
                ((SelectiveInterfaceController) getController()).removeControlWithoutSizerUpdate(e);
            }
            sizer.releaseAll();
            return true;
        }
        return false;
    }

    // on line

//    /** formsManager will use this. Do not use this as bugs with form change may occur. */
//    public void formNameWasChanged(String name){
//        formsDialog.nameWasChanged(name);  // tokia linija kazkokia.
//    }

    /* Pop up, dialogs. */

    /** Dialog which controls project resources.
     *  */
    public ResourcesDialog getResourcesDialog(){
        return resourcesDialog;
    }

    /** Color picker dialog. */
    public ColorPicker getColorPicker(){
        return colorPicker;
    }

    /** Dialog for switching forms. */
    public SwitchFormsDialog getSwitchFormsDialog(){
        return formsDialog;
    }

    /** Dialog which is shown when user right click on editable space. */
    public RightClickManager getRightClickManager() {
        return rightClickManager;
    }

    /** Physics editor dialog. */
    public PhysicsEditor getPhysicsEditor() {
        return physicsEditor;
    }

    /** Form create dialog. */
    public FormCreateDialog getFormCreateDialog() {
        return formCreateDialog;
    }

    //    /** background color. ARGB format. */
//    public int getBackgroundColor(){
//        return backgroundColor;
//    }

    /** this id is used for creation of items. when item is newly created then this prefix is used to build it's id. */
    public String getPrefixId(){
        return prefixId;
    }

    /* matomo ekrano ribos. Tos kurios piesiamos. */

    /** Visible bounds. It's not real visible bounds it's just bounds which show what size screen would look like. */
    public void setVisibleScreenBounds(float width, float height){
        visibleWidth = width;
        visibleHeight = height;
    }

    /** Screen bound width. */
    public float getVisibleWidth(){
        return visibleWidth;
    }

    /** screen bound height */
    public float getVisibleHeight(){
        return visibleHeight;
    }

    /** Screen bound line thickness. */
    public void setLineWidth(float width){
        lineWidth = MoreUtils.abs(width);
    }

    /** should screen bounds be shown. */
    public void showScreenBounds(boolean show){
        showScreenBounds = show;
    }

    /** ARGB format */
    public void setScreenBoundsColor(int color){
        screenBoundsColor = color;
    }

    /* inside. */

    /** Resizer itself. */
    Resizer getSizer(){
        return sizer;
    }

    /** camera mover. */
    FieldMover getMover(){
        return mover;
    }

    /** Selector. */
    Selector getSelector(){
        return selector;
    }

    boolean isSpaceDown(){
        return spaceDown;
    }

    /* zoom valdymas */

    float getZoomSensitivity(){
        return zoomSensitivity;
    }

    public float getMaxZoom(){
        return maxZoom;
    }

    public float getMinZoom(){
        return minZoom;
    }

    public void setMaxZoom(float zoom){
        maxZoom = zoom;
    }

    public void setMinZoom(float zoom){
        minZoom = zoom;
    }

    /** is screen bounds visible in edit form. */
    public boolean isShowingScreendBounds(){
        return showScreenBounds;
    }

    /** is absolute and fixed coords visible in edit form. */
    public boolean isCoordsVisible(){
        return mainPanel.isCoordsVisible();
    }

//    public ListView getResourcesTabs(int index){
//        return mainPanel.getResourcesTab().getResourcesTab(index);
//    }

    /* managers. */

    /** Main edit panel. Visible on left corner. */
    public EditMainPanel getMainPanel(){
        return mainPanel;
    }

    /** Resource tab. Resources and interfaces. */
    public ResourcesTab getResourceTab(){
        return mainPanel.getResourcesTab();
    }

    /** joint manager. */
    public JointManager getJointManager(){
        return jointManager;
    }

    /** chain manager. */
    public ChainEdging getChainEdging(){
        return chainEdging;
    }

    /** Undo redo controller. */
    public MoveController getUndoController() {
        return undoController;
    }

    /** Copy paste manager. */
    public CopyManager getCopyManager() {
        return copyManager;
    }

    // listener veikimas
    // selective listener

    /** Set listener to get inputs of which interface user has selected. Set this to null to use default <code>Resizer</code> listener. */
    public void setSelectiveListener(SelectiveInterfaceController.SelectedInterfaceListener e){
        outListener = e;
    }

    /** if not set then null. */
    public SelectiveInterfaceController.SelectedInterfaceListener getSelectiveInterfaceListener(){
        return outListener;
    }

    /* override */

    @Override
    protected void onShow() {
        // atnaujinam resursus
        // atnaujinam cia, kad daznai nekviestu (kaskart pasirinkus detale ir atzymejus be reikalo tikrintu).
        getResourceTab().getImgTab().updateUsableResourceList();
        // dar atnaujinam jei reik.
        mainPanel.updateControlPanelView();

        // bug fix, kai vos ijungus edit forma rodomas neteisingas zoom.
        mainPanel.cameraUpdated(); // atnaujinam, kad pasizymetu, koks yra zoom dydis.
    }

//    @Override
//    protected void background() {
//        p.background(backgroundColor);
//    }

    /* screen bounds drawing. */

    @Override
    protected void lastDraw() {
        float start = -15f;
        float end = 33f;
//        super.background();
//        p.stroke(0);
//        p.strokeWeight(3f);
//        p.line(start, 0, end, 0);
//        p.line(0, start, 0, end); // cros blogai piesia.
        // kryziaus piesimas.
        p.tint(0);
        whiteColor.draw(p.getBatch(), start, 0, end, lineWidth);
        whiteColor.draw(p.getBatch(), 0, start, lineWidth, end);
        // ekrano ribos piesimas.
        if (showScreenBounds){
            p.tint(screenBoundsColor);
            whiteColor.draw(p.getBatch(), 0, 0, lineWidth, visibleHeight); // kaire
            whiteColor.draw(p.getBatch(), 0, visibleHeight, visibleWidth + lineWidth, lineWidth); // virsus
            whiteColor.draw(p.getBatch(), visibleWidth, 0, lineWidth, visibleHeight); // desine
            whiteColor.draw(p.getBatch(), 0, 0, visibleWidth, lineWidth);
        }
        p.noTint();
    }

    /* inputs. */

    /* klaviaturos inputs. */

    @Override
    public boolean afterKeyDown(int keycode) {
//        new AngularSelectionDialog().open();
        if (!isPopUpShown()) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)){ // undo redo
                if (keycode == Input.Keys.Z){
                    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)){ // redo
                        undoController.redo();
                    }else { // undo
                        undoController.undo();
                    }
                    if (sizer.getControls().size() > 0) {
                        sizer.autoSize();
                        if (sizer.hasMultipleControls()) {
                            mainPanel.settingsChanged(sizer);
                        } else {
                            mainPanel.settingsChanged(sizer.getControls().get(0));
                        }
                    }
                    return true;
                } else if (keycode == Input.Keys.C){ // copy.
                    if (sizer.getControls().size() > 0){ // reikia kazka turet kopijuot.
                        copyManager.copy(sizer.getControls());
                    }
                    return true;
                } else if (keycode == Input.Keys.V){ // paste.
                    paste();
//                    Array<Interface> copies = copyManager.paste();
//                    undoController.getFlushSettings().clear();
//                    if (copies.size > 0){ // reik irgi kazka turet.
//                        undoController.setInterfaces(copies);
//                        sizer.releaseAll(); // paleidziam visas pazymetas.
//                        for (Interface e : copies){
//                            undoController.getFlushSettings().add(0f);
////                            addEditableControl(e); // idedam nauja i forma
//                            addControl(e, getController().getControls().size()-3);
//                            // wuts dis??
////                            checkResourceDrawable(e); // tiesiog pazes ar ne spriter drawable.
//                            sizer.addEditableControl(e); // pazymim.
//                        }
//                        undoController.moved(1);
//                        mainPanel.update();
//                    }
                    return true;
                }else if (keycode == Input.Keys.A){ // opens or closes additional panel
                    mainPanel.getAdditionalPanel().act();
                    return true;
                }else if (keycode == Input.Keys.X){ // hide main panel
                    mainPanel.hide();
                    return true;
                }else if (keycode == Input.Keys.Q){ // chain edging
                    mainPanel.getAdditionalPanel().openChainEdgePanel();
                    return true;
                }else if (keycode == Input.Keys.SPACE){ // center
                    mainPanel.centerZoom();
                    return true;
                }else if (keycode == Input.Keys.F){ // opens prefix dialog. for faster access.
                    mainPanel.getAdditionalPanel().getSettingPicker().getPrefixIdDialog().open();
                    return true;
                }else if (keycode == Input.Keys.J){ // opens joints panel
                    mainPanel.openControlPanel(2, true);
                    return true;
                }else if (keycode == Input.Keys.E){
                    mainPanel.focusControl();
                    return true;
                }
            } else if (keycode == Input.Keys.SPACE) {
                spaceDown = true;
                mover.setEnabled(true);
                mainPanel.fieldMoverStateChanged(true);
//            mover.enableDragging(true);
//            mover.enableKeyJump(true);
                return true;
            } else if (keycode == Input.Keys.FORWARD_DEL){
                return deleteSelectedItems();
//                if (sizer.getControls().size() > 0){
//                    undoController.setInterfaces(sizer.getControls()); // nustatom ka istrinam
//                    undoController.getFlushSettings().clear(); // del viso pikto padarom tuscia
//                    for (Interface e : sizer.getControls()){
//                        int count = 0;
//                        for (Interface a : getController().getControls()){
//                            if (e == a){
//                                undoController.getFlushSettings().add((float) count); // sugaudom trinamu interface vietos id.
//                            }
//                            count++;
//                        }
//                    }
//                    undoController.moved(9, getController()); // nustatom, kad kontroles yra istrintos.
//                    for (Interface e : sizer.getControls()){ // trinam visas kontroles is formos.
//                        ((SelectiveInterfaceController) getController()).removeControlWithoutSizerUpdate(e);
//                    }
//                    sizer.releaseAll();
//                    return true;
//                }
            }else if (keycode == Input.Keys.ESCAPE){
                sizer.releaseAll();
                checkControlInfo();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean beforeKeyDown(int keycode) {
        if (!isPopUpShown()){
            if (keycode == Input.Keys.TAB){
                formsDialog.open();
                return true;
            }
        }

        // tie kurie visad veiks.
        // tie kurie nuo ctrl visada eis.
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)){
            // save visur ir visada.
            if (keycode == Input.Keys.S){ // save
                mainPanel.save();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean beforeKeyUp(int keycode) {
        if (spaceDown && keycode == Input.Keys.SPACE){
            spaceDown = false;
            mover.setEnabled(false);
            mainPanel.fieldMoverStateChanged(false);
//            mover.enableDragging(false);
//            mover.enableKeyJump(false);
            return true;
        }
        return false;
    }

    /* mouse inputs. */

    @Override
    protected boolean afterTouchDown(float x, float y, int pointer, int button) {
        if (button == Input.Buttons.RIGHT){
            rightClick = true; // pranesam, kad gales drasiai naudot right click.
            return true;
        }
        return false;
    }

    @Override
    protected boolean afterTap(float x, float y, int count, int button) {
        return openRightClickMenu(x, y, button);
    }

    @Override
    protected boolean afterPanStop(float x, float y, int pointer, int button) {
        return openRightClickMenu(x, y, button);
    }

    // atidarom right click menu panele.
    private boolean openRightClickMenu(float x, float y, int button){
        if (!rightClick || rightClickManager.isShown()){ // jeigu nebuvo leidimo arba matomas jau dialogas, tai false..
            rightClick = false; // nuimt reik bet kokiu atveju.
            return false; // iskart false metam.
        }

        rightClick = false; // nuimam right click.
        if (button == Input.Buttons.RIGHT){ // dar kart paziurim ar tikrai right mygtukas.
            Vector3 pos = p.screenToFixedCoords(x, y); // verciam screen i fixed coordinates.
            // kad pasispaustu.
            touchDown(x, y, 0, Input.Buttons.LEFT); // imituojam touch down.
            tap(x, y, 0, Input.Buttons.LEFT); // ir iskart atleidziam.
            // cia reik patikrint ar kartais mouse nera paneles teritorijoj.
            if (!rightClickManager.beforeRightClickOpen(sizer, pos.x, pos.y, 0, 0)){ // paklausiam ar galim atidaryt.
                float height = rightClickManager.getHeight(); // atsidaro ne ten kur reik, todel reik nunest per jo dydi i apacia.
                rightClickManager.setPosition(pos.x, pos.y - height); // nustatom nauja pozicija.
                rightClickManager.show(); // parodom.
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean beforeMouseMove(float x, float y) {
        mainPanel.mouseCoords(x, y);
        return false;
    }

    @Override
    public boolean beforePan(float x, float y, float deltaX, float deltaY) {
        mainPanel.mouseCoords(x, y);
        return false;
    }

    @Override
    public void release() {
        super.release();
        spaceDown = false;
        mover.setEnabled(false);
    }

    @Override
    public boolean afterScrolled(float amountX, float amountY) {
        OrthographicCamera camera = p.getAbsoluteCamera();
        camera.zoom += amountY*zoomSensitivity; // Only vertical scroll used...
//        camera.zoom = 4;
        updateCamera();
        return true;
    }
}
