package com.jbconstructor.main.forms.editorForm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Connected;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.InterfacesController;
import com.engine.interfaces.controls.Toast;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.dialogs.ConfirmDialog;
import com.engine.interfaces.controls.dialogs.InputTextDialog;
import com.engine.interfaces.controls.dialogs.PreparedRightClickMenu;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.views.ListView;
import com.engine.interfaces.controls.widgets.Block;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.controls.widgets.TabControl;
import com.jbconstructor.main.managers.FormsManager;
import com.jbconstructor.main.managers.Project;
import com.jbconstructor.main.root.Element;

/** Tab which holds all loaded resources. */
public class ImagesTab extends TabControl implements Project.ResourceChangeListener {
    private final EditForm editForm;
    private ListView[] tabs;
    private DropListener dropListener;
//    private PreparedRightClickMenu physicsRightClick, simpleClickMenu;
    private PreparedRightClickMenu simpleClickMenu;

//    private Array<ConnectedBlock> imagesTab, regionTab, ninePatchTab, animeTab;
    private Array<ConnectedBlock>[] resourcesTabs;

    public ImagesTab(EditForm form) {
        super(new TabControlStyle()); // tabo nustatymai.
        editForm = form;
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        // default tabo mygtukai.
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        buttonStyle.background = Resources.getDrawable("whiteSystemColor");
        buttonStyle.textSize = 30;
        float height = p.getScreenHeight() * 0.765f, width = p.getScreenWidth() * 0.2f;
        setMaxSize(width, height);
        setButtonsPosition(ButtonsPosition.TOP);
        setbuttonStyle(buttonStyle);

//        Array<Connected> imagesTab = new Array<>();
//        Array<Connected> regionTab = new Array<>();
//        Array<Connected> ninePatchTab = new Array<>();
//        Array<Connected> animeTab = new Array<>();
        /* Visu tabu resursai. */
        resourcesTabs = new Array[4];
        for (int a = 0; a < resourcesTabs.length; a++){
            resourcesTabs[a] = new Array<>();
        }

        /* patis tabai. */
        tabs = new ListView[5]; // paruosiam listview.
        ListView.ListViewStyle st = new ListView.ListViewStyle();
        st.width = width;
        st.height = height;
        st.itemWidth = st.itemHeight = (width / 2) * 0.95f;
        st.separatorSize = width / 2 * 0.025f;
        st.offsetY = 5;
        st.offsetX = 5;
        st.scrollSpeed = Resources.getPropertyFloat("scrollSpeed", 15f);

        /* right click menu. */
        PreparedRightClickMenu clickMenu = new PreparedRightClickMenu();
        SymbolButton.SymbolButtonStyle style = clickMenu.getStyle();
        style.symbolWidth = 40;
        style.symbolHeight = 40;
//        clickMenu.addListItem("Polygon", Resources.getDrawable("additionalPanelChainKey"), 0xffffffff);
        clickMenu.addListItem("-9patch", Resources.getDrawable("9patchIcon"), 0xffffffff);
        clickMenu.addListItem("Change id", Resources.getDrawable("labelIcon"), 0xffffffff);
        clickMenu.addListItem("Hide", Resources.getDrawable("noEyeIcon"), 0xffffffff);
        clickMenu.addListItem("Remove", Resources.getDrawable("defaultColorPickCross"), 0xffffffff);
//        clickMenu.addListItem("aaaa??");
        clickMenu.setMenuItemClickListener(new PreparedRightClickMenu.MeniuItemClickedListener() {
            @Override
            public void clickedItem(Interface owner, int index, String itemName) {
                if (owner != null) {
//                    if (index == 0) {
//                        editForm.physicsEditor.show((ConnectedBlock) owner);
////                    ((Block) owner).setSelected(true);
//                    }else if (index == 1){
                    if (owner instanceof ConnectedBlock) {
                        if (index == 0) {
                            // TODO 9patch action.
                        } else if (index == 1) {
                            startResourceIdChangingChain(((ConnectedBlock) owner).resource);
                        } else if (index == 2) {
                            ConnectedBlock block = (ConnectedBlock) owner;

                            block.resource.visible = false;
                            block.setVisible(false);
                            tabs[block.resource.tab].update();

                            Project.triggerResourceUpdate();
                        } else if (index == 3) {
                            Project.startResourceDeletionChain(((ConnectedBlock) owner).resource);
                        }
                    }
                }
            }
        });
        simpleClickMenu = clickMenu;

        /* tab uzkrovimas. */
        dropListener = new DropListener();
        { // image tab
            final ListView tab1 = new ListView(st);
            tab1.setBackground(buttonStyle.background);
            tab1.tintBackground(0xFFBFFCC6);
            addTab(tabs[0] = tab1, "img"); // listview idejimas
        }
        { // region tab
            ListView tab1 = new ListView(st);
            tab1.setBackground(buttonStyle.background);
            tab1.tintBackground(0xFFBFFCC6);
            addTab(tabs[1] = tab1, "reg");
        }
        { // 9 patch tab
            ListView tab1 = new ListView(st);
            tab1.setBackground(buttonStyle.background);
            tab1.tintBackground(0xFFBFFCC6);
            addTab(tabs[2] = tab1, "9patch");
        }
        { // anime tab
            ListView tab1 = new ListView(st);
            tab1.setBackground(buttonStyle.background);
            tab1.tintBackground(0xFFBFFCC6);
            addTab(tabs[3] = tab1, "anm");
        }
//        { // physics tab
//            ListView tab1 = new ListView(st);
//            tab1.setBackground(buttonStyle.background);
//            tab1.tintBackground(0xFFBFFCC6);
//            addTab(tabs[4] = tab1, "phcs");
//
//            // right click
//            PreparedRightClickMenu menu = new PreparedRightClickMenu();
//            menu.addListItem("Edit polygons");
//            menu.setMenuItemClickListener(new PreparedRightClickMenu.MeniuItemClickedListener() {
//                @Override
//                public void clickedItem(Interface owner, int index, String itemName) {
//                    editForm.physicsEditor.show((ConnectedBlock)owner);
//                }
//            });
////            menu.addListItem("aaa");
//            physicsRightClick = menu;
//        }
        Project.addResourceChangeListener(this);
        updateUsableResourceList();
    }

    /* usable resource testing */

    /** update usable resource list. */
    void updateUsableResourceList(){

        // Eisim per visus blokus. Ziuresim kurie yra negeri.
        for (Array<ConnectedBlock> array : resourcesTabs){
            for (int a = array.size-1; a >= 0; a--){
                ConnectedBlock block = array.get(a);

                if (!Project.isLoadableResourceValid(block.resource)){
                    // negeras resource. Naikinam is sarasu.
                    tabs[block.resource.tab].removeControl(block);

                    array.removeValue(block, true);
                }
            }
        }

        // dabar eisim per loadable resource ir zesim ar visi sukurti.
        Block.BlockStyle bst = null;
        MAIN:
        for (int a = 0; a < Project.getLoadableResourcesCount(); a++){
            Project.ResourceToHold hold = Project.getLoadableResource(a);

            if (hold == null){
                continue;
            }

            // ziurim ar sukurtas.
            for (ConnectedBlock block : resourcesTabs[hold.tab]){
                if (block.resource == hold){
                    // radom musu bloka.
                    block.setVisible(hold.visible); // padarom visibility update.
                    tabs[hold.tab].update(); // prasom update.
                    continue MAIN;
                }
            }

            // jeigu atejo cia vadina resource blockas neegzistuoja. Kursim nauja.
            if (bst == null){ // sukuriam style jei nebuvo sukurta.
                bst = new Block.BlockStyle();
                bst.allowSuperSelect = true;
            }

            // kuriam nauja blocka. Idedant ji i forma tabas automatiskai atsinaujins.
            bst.background = hold.e;
            ConnectedBlock block = new ConnectedBlock(bst, hold.tab - 4, hold);
            block.setDropListener(dropListener);
            block.setRightClickListener(simpleClickMenu);
            block.setVisible(hold.visible);
            resourcesTabs[hold.tab].add(block);
            getTab(hold.tab).addControl(block);
        }


//        old update method. su bugais sitas.
//        Block.BlockStyle bst = new Block.BlockStyle(); // block leidzia dragint ir dropint.
//        bst.allowSuperSelect = true;
//        for (int a = 0; a < Project.getLoadableResourcesCount(); a++){
//            Project.ResourceToHold e = Project.getLoadableResource(a);
//            ConnectedBlock block;
//
//            if (e == null){ // neturetu niekad but null, bet del viso pikto.
//                continue;
//            }
//
//            boolean create = true;
//            for (ConnectedBlock b : resourcesTabs[e.tab]){
//                if (b.getBackground() == e.e){ // tikrina background nors realiai galetu tikrint pati resource... :D
//                    b.setVisible(e.visible);
//                    getTab(0).update();
//                    create = false;
//                    break;
//                }
//            }
//            // toks image neegzistuoja reik sukurt.
//            if (create) {
//                bst.background = e.e;
//                block = new ConnectedBlock(bst, e.tab-4, e);
//                block.setDropListener(dropListener);
//                block.setRightClickListener(simpleClickMenu);
//                block.setVisible(e.visible);
//                resourcesTabs[e.tab].add(block);
//                getTab(e.tab).addControl(block);
//            }
//        }
    }

    /* id keitimo parametrai (resource id.). */

    // atlaso ido keitimas (ne interface). Pakeis atlaso id ir pakeis visu resursu asocijacijas su siuo atlasu.
    private boolean changeAtlasId(String name, Project.ResourceToHold resourceToHold){
        if (name == null || name.trim().length() == 0){
            new AlertToast("Id cannot be empty!").show(Toast.SHORT);
//            createAlertToast("Id cannot be empty!");
            return false;
        }

        String clipped = name.trim();

        if (clipped.equals("null")){
            new AlertToast("Id cannot be \"null\"").show();
            return false;
        }
        // padarius clip dar reik pazet ar toks atlaso id neegzistuoja
        if (Resources.containsTextureAtlasKey(clipped)){
            new AlertToast("Texture atlas already exists with id: " + clipped).show();
            return false;
        }

        // neegzistuoja, galim keist.
        String old = resourceToHold.getAtlasName();
        TextureAtlas atlas = Resources.removeAtlas(old, false); // salinam senaji is sarasu.
        Resources.addTextureAtlas(clipped, atlas); // dedam atgal i sarasus su nauju vardu.

        boolean error = false;
        // pakeitem resource id. Dabar reiks eit per visus resource ir pakeist atlaso varda...
        for (int a = 0; a < Project.getLoadableResourcesCount(); a++){
            Project.ResourceToHold hold = Project.getLoadableResource(a);
            if (hold != null && hold.getAtlasName() != null){ // nera null pats ir turi atlas
                if (old.equals(hold.getAtlasName())){ // atlas atitinka, reik keist id.
                    if (!hold.changeAtlasName(clipped)) { // updatinam id.
                        error = true;
                    }
                }
            }
        }

        // praneskim, kad kazkas ne taip. Teoriskai sitas niekada neturetu ivykt.
        if (error){
            new AlertToast("Error while trying update resources associations with this atlas yet atlas id changed successfully.").show();
        }

        // pranesam apie pakeitimus projekte.
        Project.getSaveManager().triggerSave();
        return true;
    }

    /* atlas id keitimo interface. Iskviecia dialoga kuriam praso ivest atlaso ida. */
    private void createAtlasIdChangeDialog(final Project.ResourceToHold hold){
        final InputTextDialog dialog = new InputTextDialog("Change atlas id:");
        dialog.getInput().setText(hold.getAtlasName());
        dialog.setInputDialogListener(new InputTextDialog.InputDialogListener() {
            @Override
            public void onInput(String input) {
                if (changeAtlasId(input, hold)){
                    new AlertToast("Successfully changed id!").show();
                }else {
                    dialog.open(); // reopen.
                }
            }

            @Override
            public void cancel() {

            }
        });
        dialog.open();
    }

    /** Method who begins resource id changing chain. Can call texture atlas id change or resource id change (depends on resource). */
    public void startResourceIdChangingChain(final Project.ResourceToHold resource){
        if (resource.tab == 1 || resource.tab == 2){
            // upss texture atlas. Negalim jo id keist.
            ConfirmDialog confirmDialog = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.YesNo);
            confirmDialog.setText("Selected resource is from texture atlas. It's id is defined in the texture atlas itself. " +
                    "You need to manually change id in the texture atlas or you can change the id of texture atlas itself (it will " +
                    "not change selected resource id but will change texture atlas id). Continue?");
            confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                @Override
                public void onYes() {
                    createAtlasIdChangeDialog(resource);
                }

                @Override
                public void onNo() {}

                @Override
                public void onCancel() {}
            });
            confirmDialog.open();
        }else {
            // normalus id keitimas
            final InputTextDialog dialog = new InputTextDialog("Change resource id:");
            dialog.getInput().setText(resource.getIdName());
            dialog.setInputDialogListener(new InputTextDialog.InputDialogListener() {
                @Override
                public void onInput(String input) {
                    // Bandom keist id.
                    if (changeResourceId(input, resource)){
                        AlertToast toast = new AlertToast("Successfully changed resource id");
                        toast.show(Toast.SHORT);
                    }else {
                        dialog.open(); // atidarom is naujo dialoga.
                    }
                }

                @Override
                public void cancel() {}
            });
            dialog.open();
        }
    }

    /* id keitimas. Tik tiems, kuriems galima keist id! Item is atlaso negalim keist, nes ten atlase irasytas, neimanoma pakeist (imanoma, bet atlaso
    * neredaguosim). */
    private boolean changeResourceId(String id, Project.ResourceToHold current){
        // pirma paziurim ar tiesiog id normalus.
        if (id == null || id.trim().length() == 0){
            new AlertToast("Id cannot be empty!").show(Toast.SHORT);
//            createAlertToast("Id cannot be empty!");
            return false;
        }

        // paziurim ar gera holderi padave.
        if (current == null){
            // nerado musu esamo resource holdo. blogai
            new AlertToast("Unknown error! Cannot find resource holder!").show(Toast.SHORT);
            return false;
        }
        // paziurim ar tinkamas holderis.
        if (current.tab == 1 || current.tab == 2){
            new AlertToast("Resource is from texture atlas. Cannot change id if resources are from atlas!").show(Toast.SHORT);
            return false;
        }

        // paklipinam musu id.
        String clipped = id.trim();
        if (clipped.equals("null")){
            new AlertToast("Id cannot be \"null\"").show();
            return false;
        }
        // toliau ziurim ar toks egzistuoja.
//        Project.ResourceToHold current = null;
        for (int a = 0; a < Project.getLoadableResourcesCount(); a++){
            Project.ResourceToHold e = Project.getLoadableResource(a); // imam resource.
            if (e == null){
                new AlertToast("Unknown error occurred. Please try again!").show(Toast.SHORT);
                return false;
            }
            if (e.getIdName().equals(clipped)){
                // vardai vienodi! blogai.
                new AlertToast("Id already exists: " + clipped).show(Toast.SHORT);
                return false;
            }
//            if (e.idName.equals(oldId)){
//                current = e;
//                if (current.tab == 1 || current.tab == 2){
//                    new AlertToast("Resource is from texture atlas. Cannot change resource id!").show(Toast.SHORT);
//                    return false;
//                }
//            }
        }

        // atejus cia naujas id geras, resource rastas.
        // dabar reik pakeist resource id terp holder, Resources klases ir dar
        // pereit per visus esamus elementus edit formoj ir pakeist resource id. Reikes pereit
        // ne tik per dabartine edit forma, bet per visas esamas!
        String old = current.getIdName();
        if (current.changeIdName(clipped)){
            // sekmingai pakeite varda.
            // toliau keliaujam per edit formas ir keiciam id jei resursai toki turi.
            FormsManager formsManager = Project.getFormsManager();
            for (int a = 0; a < formsManager.getFormsSize(); a++){
                EditForm e = formsManager.getForm(a);
                for (Interface control : e.getController().getControls()){
                    if (control instanceof Element){
                        Element resource = (Element) control;
                        if (resource.getResName().equals(old)){
                            // radom resursa su tokiu pat id.
                            // keiciam pavadinima. Drawable keist nereik, nes cia tik id keiciasi.
                            resource.setResName(clipped);
                        }
                    }
                }
            }

//            Project.getSaveManager().triggerSave(); // pranesam apie pakeitimus.
            return true; // sekmingai viskas pavyko.
        }else {
            new AlertToast("Unknown error occurred. Please try again!").show(Toast.SHORT);
            return false;
        }

    }

//    private void createAlertToast(String text){
//        Toast.ToastTextStyle textStyle = new Toast.ToastTextStyle();
//        textStyle.textColor = 0xFFFF0000;
//        textStyle.textSize = 60;
//        AlertToast error = new AlertToast();
//        error.setText(text);
//        error.setToastTextStyle(textStyle);
////        ((AlertToast) error).setTextSize(60);
//        error.setBackground(Resources.getDrawable("systemWhiteRect"));
//        error.show();
//    }

    /* perdavimo linija */

    /** resources tabs. 0 - image tab, 1 - region tab, 2 - 9patch tab, 3 - animation tab, 4 - phcs tab. */
    public ListView getTab(int index) { // del physics editor.
        if (index >= 0 && index < tabs.length)
            return tabs[index];
        else
            return null;
    }

    // sita kviec kai kazkas resource pasikeite. pvz uzloadino images. arba id pakeite... Id keitimas cia vyksta bet vistiek updatins sarasa.
    @Override
    public void resourcesChanged() {
        updateUsableResourceList();
    }

    /* fiziku blokai. */

//    /** Adds physics block to physics tab. */
//    public void addPhysicsBlock(ConnectedBlock e) {
//        tabs[4].addControl(e);
//        e.setDropListener(dropListener);
//        e.setRightClickListener(physicsRightClick);
//    }
//
//    /** Remove physics block from physics tab. */
//    public void removePhysicsBlock(ConnectedBlock e) {
//        tabs[4].removeControl(e);
//        e.setDropListener(null);
//    }

    // drop blokas
    // Blockas kuris atsakingas uz draginima, numetima etc.

    public class ConnectedBlock extends Block implements Connected {
        private int group;
//        public Array<PhysicsEditor.FixtureShapeHolder> shapes;
//        public PhysicsHolder physicsHolder;
//        public final String resourceName;
        public final Project.ResourceToHold resource;

        public ConnectedBlock(BlockStyle st, int group, Project.ResourceToHold hold) {
            super(st);
            this.group = group;
//            shapes = new Arr/;
//            physicsHolder = new PhysicsHolder();
//            resourceName = name;
            resource = hold;
        }

        @Override
        protected void onRemove() {
            super.onRemove();
            v.removeConnection(this);
        }

        @Override
        public void setController(InterfacesController v) {
            super.setController(v);
            v.addConnection(this);
        }

        @Override
        public void inform(int reason) {
            setSelected(false);
        }

        @Override
        public int getGroup() {
            return group;
        }

        @Override
        protected void onSuperSelect() {
//            ImagesTab.this.v.inform(this, 0);/
            v.inform(this, 0);
        }

//        /** Used by <code>PhysicsEditor</code>.
//         * @return Physics tab. */
//        public ListView getPhysicsTab(){
//            return getTab(4);
//        }
    }

    // drop listener.
    // kai detale numetama ant "zemes"

    private class DropListener implements Block.DropListener {


        @Override
        public void onDrop(float x, float y, Block e) { // tik resource drop, ne interface.
            float width, height;
            width = p.getScreenWidth() * 0.8f;
            height = p.getScreenHeight();
            if (x >= 0 && x < width && y >= 0 && y < height) {
                boolean isShiftDown = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
                Element.ElementStyle st = new Element.ElementStyle();
                st.image = e.getBackground();
//                if (st.image instanceof SpriterDrawable){ // butinai nauja instance.
//                    st.image = new SpriterDrawable((SpriterDrawable) st.image);
//                }
                if (isShiftDown) {
                    st.x = x;
                    st.y = y;
                    st.positioning = Window.Position.fixed;
                } else { // kadangi blokai yra tikrai fixed, tai tik ant absolute reik cord perskaiciuot.
                    Vector3 pos = p.screenToWorldCoords(Gdx.input.getX(), Gdx.input.getY()); // perskaiciuoja tiksliai pelytes coord.
                    st.x = pos.x;
                    st.y = pos.y;
//                    OrthographicCamera cam = p.getAbsoluteCamera(); // neveikia su zoom.
//                    st.x += cam.position.x - (p.getScreenWidth()/2);
//                    st.y += cam.position.y - (p.getScreenHeight()/2);

                    //test
//                    OrthographicCamera cam = p.getAbsoluteCamera(); // neveikia su zoom.
//                    st.x = x + cam.position.x - p.getScreenWidth()/2 - (p.getScreenWidth()/cam.zoom - p.getScreenWidth())/2;
//                    st.y = y + cam.position.y - p.getScreenHeight()/2 - (p.getScreenHeight()/cam.zoom - p.getScreenHeight())/2;
                }
                st.x -= st.image.getMinWidth() / 2;
                st.y -= st.image.getMinHeight() / 2;
                if (e instanceof ConnectedBlock){
//                    st.resName = ((ConnectedBlock) e).resourceName;
                    st.resName = ((ConnectedBlock) e).resource.getIdName();
                    Element img = new Element(st);
                    img.setIdName(editForm.getPrefixId());
                    editForm.addEditableControl(img);

//                    img.getPhysicsHolder().copyHolder(((ConnectedBlock) e).physicsHolder);
//                    if (((ConnectedBlock) e).shapes.size > 0){ // turi fizikos tasku. Reik nukopijuot. sikart tikrai kopijuot.
//                        Array<PhysicsEditor.FixtureShapeHolder> list = img.getShapes();
//                        for (PhysicsEditor.FixtureShapeHolder shape : ((ConnectedBlock) e).shapes){
//                            list.add(new PhysicsEditor.FixtureShapeHolder().copy(shape));
//                        }
//                    }
                }
            }
        }
    }
}
