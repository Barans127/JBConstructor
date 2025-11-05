package com.jbconstructor.main.dialogs;

import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Connected;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.InterfacesController;
import com.engine.interfaces.controls.PopUp;
import com.engine.interfaces.controls.dialogs.PreparedRightClickMenu;
import com.engine.interfaces.controls.dialogs.RightClickMenu;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.views.ListView;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SImage;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.controls.widgets.TabControl;
import com.engine.interfaces.listeners.ClickListener;
import com.jbconstructor.main.editors.MoveController;
import com.jbconstructor.main.forms.editorForm.EditForm;
import com.jbconstructor.main.forms.editorForm.ImagesTab;
import com.jbconstructor.main.forms.editorForm.ResourcesTab;
import com.jbconstructor.main.managers.Project;
import com.jbconstructor.main.root.Element;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ResourcesDialog extends PopUp implements Project.ResourceChangeListener {
    private EditForm editForm;

    // prepared tikrai netinka. Reiks custom button kurtis ir naudot paprasta list view.
    // prepared nesugebes issaugot resource pacio kame gali but bedu, del to geriausia variantas
    // naudot paprasta list view su custom symbolButton...
//    private PreparedListView[] tabs;
    private ListView[] tabs;
    private TabControl tab;

//    private Array<Object> selectedItems; // cia desim pasirinktus item.

    // list items. kontrole ir t.t.
    private boolean multiSelect;
    private Button changeButton;
    private Element element;
    private PreparedRightClickMenu rightClickMenu;

    // patys button.
    // visi buttons ir tie kurie buvo selectinti.
    private Array<SelectionButton> active, selected;
    private Pool<SelectionButton> buttonPool;

    // info
    private ResourceInfoDialog dialog;

    public ResourcesDialog(EditForm editForm) {
        super(1080, 660);

        this.editForm = editForm;

        // button pool.
        buttonPool = new Pool<SelectionButton>() {
            @Override
            protected SelectionButton newObject() {
                SymbolButton.SymbolButtonStyle sst = new SymbolButton.SymbolButtonStyle();
                sst.autoSize = false;
                sst.width = 190;
                sst.height = 220;
                sst.symbolWidth = 150;
                sst.symbolHeight = 150;
                sst.normalColor = 0x00ffffff;
                sst.position = SymbolButton.TextPosition.DOWN;
                SelectionButton button = new SelectionButton(sst);
                button.setRightClickListener(rightClickMenu);
                return button;
            }
        };
        active = new Array<>();
        selected = new Array<>();

        // title
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.autoSize = false;
        lst.x = 18;
        lst.y = 591;
        lst.width = 525;
        lst.height = 53;
        lst.textSize = 50;
        lst.text = "Resources";

        addControl(lst.createInterface());

        // footer. Mygtukai apacioj.
        // visible mygtukas
//        Button.ButtonStyle bst = new Button.ButtonStyle();
        SymbolButton.SymbolButtonStyle bst = new SymbolButton.SymbolButtonStyle();
        bst.autoSize = false;
        bst.x = 18;
        bst.y = 57;
        bst.width = 214;
        bst.height = 35;
        bst.textSize = 30;
        bst.text = "Set visible";
        bst.normalColor = 0xff2222ff;
        bst.symbol = Resources.getDrawable("eyeIcon");
        bst.symbolWidth = 35;
        bst.symbolHeight = 35;

        Button visible = new SymbolButton(bst);
        visible.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                showResourcesClick();
            }
        });
        addControl(visible);

        // hidden mygtukas
        bst.text = "Set hidden";
//        bst.x = 245;
        bst.y = 11;
        bst.symbol = Resources.getDrawable("noEyeIcon");

        Button hidden = new SymbolButton(bst);
        hidden.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                hideVisibleResourcesClick();
            }
        });
        addControl(hidden);

        bst.text = "Change id";
        bst.x = 244;
        bst.y = 32;
        bst.height = 40;
        bst.textSize = 35;
        bst.symbol = Resources.getDrawable("labelIcon");
        Button id = new SymbolButton(bst);
        id.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                editResourceIdClick();
            }
        });
        addControl(id);

        bst.symbol = Resources.getDrawable("defaultColorPickCross");
        bst.symbolWidth = 50;
        bst.symbolHeight = 50;
        bst.x = 524;
        bst.y = 24;
        bst.height = 53;
        bst.normalColor = 0xffff4444;
        bst.onColor = 0xffff0000;
        bst.pressedColor = 0xff992222;
        bst.text = "DELETE";

        Button delete = new SymbolButton(bst);
        delete.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                removeResourcesClick();
            }
        });
        addControl(delete);

        // change mygtukas
        bst.x = 850;
        bst.text = "CHANGE";
        bst.normalColor = 0xff1144ff;

        Button change = new Button(bst);
        change.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                changeResourceClick();
            }
        });
        addControl(change);
        changeButton = change;

        // resurcu pridejimo mygtukas.
        bst.x = 904;
        bst.y = 593;
        bst.width = 50;
        bst.height = 50;
        bst.symbol = Resources.getDrawable("mainEditorLoadFileKey");
        bst.normalColor = 0x00ffffff;
        bst.onColor = 0xFFFF5500;
        bst.pressedColor = 0xFFAA5500;
        bst.text = "";

        Button file = new SymbolButton(bst);
        file.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                loadResourcesClick();
            }
        });
        addControl(file);

        // Tab valdymas.
        TabControl.TabControlStyle tst = new TabControl.TabControlStyle();
        tst.x = 18;
        tst.y = 100;
        tst.buttonsStyle.textSize = 40;
        tst.buttonsStyle.disabledColor = 0xff555555;
        tst.buttonsStyle.normalColor = 0xff0000ff;

        TabControl tabControl = new TabControl(tst);
        addControl(tabControl);
        this.tab = tabControl;

        // list view. prepared list view.
        tabs = new ListView[4];

        ListView.ListViewStyle pst = new ListView.ListViewStyle();
        pst.autoSize = false;
        pst.rowCount = 5;
        pst.width = 1100;
        pst.height = 450;
        pst.itemWidth = 190;
        pst.itemHeight = 220;
//        pst.activeItemsStyle.symbolWidth = 150;
//        pst.activeItemsStyle.symbolHeight = 150;
//        pst.activeItemsStyle.normalColor = 0x00ffffff;
//        pst.activeItemsStyle.position = SymbolButton.TextPosition.DOWN;
//        pst.normalState = 0x00ffffff;
        pst.offsetY = 10;
//        pst.multiSelect = true;
        // tabu pridejimas.
        String[] names = {
                "Texture", "Region", "9patch", "Animation"
        };
        for (int a = 0; a < tabs.length; a++){
//            PreparedListView e = new PreparedListView(pst);
            ListView e = new ListView(pst);
            tabs[a] = e;
            tabControl.addTab(e, names[a]);
        }

        Project.addResourceChangeListener(this);

        RightClickMenu.RightClickMenuStyle rst = new RightClickMenu.RightClickMenuStyle();
        rst.width = 300;
        rst.itemWidth = 300;
        PreparedRightClickMenu rightClickMenu = new PreparedRightClickMenu(rst);

        rightClickMenu.addListItem("Deselect items");
        rightClickMenu.addListItem("Deselect other tabs");
        rightClickMenu.addListItem("Resource info");

        this.rightClickMenu = rightClickMenu;

        rightClickMenu.setMenuItemClickListener(new PreparedRightClickMenu.MeniuItemClickedListener() {
            @Override
            public void clickedItem(Interface owner, int index, String itemName) {
                if (index == 0) {// deselect
                    informGroup(-20200425, 0); // viska atzymim.
                }else if (index == 1){
                    // cia info apie resource.
                    int size = selected.size;
                    informGroup(-20200425, tab.getOpenedTabIndex()+1); // plius vienas, nes tab nuo 0 skaiciuojas, o pas mus 0 - atzymi visus.

                    int count = size - selected.size;

                    new AlertToast("Deselected " + count + " item(s)").show();
                }else if (index == 2){
                    if (owner instanceof SelectionButton) {
                        SelectionButton button = (SelectionButton) owner;

                        dialog.showInfo(button.resource);
                    }
                }
            }
        });

        dialog = new ResourceInfoDialog();
    }

    /* button listeneriai. */

    private void editResourceIdClick(){
        // pries viska mum reik surast elementa, kuris yra pazymetas.
        if (selected.size == 0){
            new AlertToast("No selected item! Please select item first!").show();
            return;
        }
        if (selected.size > 1){
            new AlertToast("First selected element will have it's id changed!").show();
        }

        SelectionButton chosen = selected.get(0); // pats pirmasis elementas.

        // id change chain yra terp image tab...
        // kvaila bet taip yra.
        ImagesTab imagesTab = editForm.getMainPanel().getResourcesTab().getImgTab();

        // pradedam id change operacija.
        imagesTab.startResourceIdChangingChain(chosen.resource);
    }

    private void showResourcesClick(){
        if (selected.size == 0){
            new AlertToast("No resources selected!").show();
        }else {
            boolean requireUpdate = false;
            for (SelectionButton button : selected){
                if (!button.resource.visible){
                    button.resource.visible = true; // kol visible nera issaugoma projekto neissaugosim.
                    button.setSymbolTint(0xffffffff);
                    button.setTextColor(0xff000000);
                    requireUpdate = true;
                }
            }

            if (requireUpdate){
                Project.triggerResourceUpdate();
            }
        }
    }

    private void hideVisibleResourcesClick(){
        if (selected.size == 0){
            new AlertToast("No resources selected!").show();
        }else {
            boolean requireUpdate = false;
            for (SelectionButton button : selected){
                if (button.resource.visible){
                    button.resource.visible = false; // kol visible nera issaugoma projekto neissaugosim.
                    button.setSymbolTint(0x99ffffff);
                    button.setTextColor(0x99000000);
                    requireUpdate = true;
                }
            }

            if (requireUpdate){
                Project.triggerResourceUpdate();
            }
        }
    }

    private void removeResourcesClick(){
        if (selected.size == 0){
            new AlertToast("No resource selected!").show();
            return;
        }

        // sukuriam array.
        Project.ResourceToHold[] holds = new Project.ResourceToHold[selected.size];

        // imetam resursus.
        for (int a = 0; a < holds.length; a++){
            holds[a] = selected.get(a).resource;
        }

        Project.startResourceDeletionChain(holds);
    }

    private void changeResourceClick(){
        if (element != null){
            if (selected.size == 0){
                new AlertToast("No resource selected! Please select resource first!").show();
            }else{
                // viskas ok. Resource parinktas. Keisim i sita resource.
                SelectionButton button = selected.get(0); // imam pirmaji item... Tiesiog pirmaji.
                Project.ResourceToHold resource = button.resource;

                String old = element.getResName();
                if (old.equals(resource.getIdName())){
                    new AlertToast("Same resource selected!").show();
                    close();
                }else {
                    element.setResName(resource.getIdName());
                    element.setImage(resource.e);

                    // darom undo judesi.
                    MoveController undo = editForm.getUndoController();

                    // imetam keiciama kontrole.
                    Array<Interface> array = undo.getList();
                    array.clear();
                    array.add(element);

                    // registruojam judesi.
                    undo.moved(18, old);

                    // uzdarom dialog po sito.
                    close();

                    new AlertToast("Successfully changed resource!").show();

                    Project.save(); // pasikeitimas projekte.
                }
            }
        }else {
            new AlertToast("Element not found!").show();
        }
    }

    private void loadResourcesClick(){
        // kodas paimtas is resources tab klases.
        FileChooser ch = Project.getFileChooser();
        JFileChooser jh = ch.getFileChooser();
//                    jh.setSelectedFiles(new File[]{});
        jh.setMultiSelectionEnabled(true);
        jh.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jh.setDialogTitle("Select Resource folder or resources");
        jh.setFileFilter(new FileNameExtensionFilter("Project resources", "jpg", "png", "gif", "bmp", "txt", "scml"));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FileChooser ch = Project.getFileChooser();
                JFileChooser jh = ch.getFileChooser();
                if (Project.getProjectRootFolderPath() == null) {
                    jh.setCurrentDirectory(new File("."));
                }else {
                    jh.setCurrentDirectory(new File(Project.getProjectRootFolderPath()));
                }

                // kad nekurt savo listener tai imsim ta, kuris stovi resource tabe. Gaidiskai, bet taip yra...
                ResourcesTab.OpenImages list = editForm.getResourceTab().getOpenImagesListener();
                ch.open(list, true); // kadangi image krovimas tai tik main thread.
            }
        });
    }

    /* sio dialogo atidarymas*/

    /** Opens dialog to change resource for given element. Multi select disabled (resource management reduced). */
    public void show(Element element){
        if (element == null){
            show(); // jeigu null tai atidarom paprastu rezimu.
            return;
        }

        changeButton.setVisible(true);
        this.element = element;
        multiSelect = false;

        open();
    }

    /** Opens normal resource dialog (no changing resource). Allows to manage resources (multi select enabled). */
    public void show(){
        changeButton.setVisible(false);
        multiSelect = true;

        open();
    }

    @Override
    protected void onOpen() {
        super.onOpen();

        updateList();

        // dabar atzymim selected visus.
        informGroup(-20200425, 0); // cia viska automatiskai atzymes.

        if (element != null){
            for (SelectionButton button : active){
                if (button.resource.getIdName().equals(element.getResName())){
                    // musu resource.
                    button.setNormalColor(0xffe0ff35);
                    break;
                }
            }
        }
    }

    @Override
    protected void onClose() {
        super.onClose();

        // mums sito nebereik.
        element = null;
    }

    /* atnaujjinimas saraso. */

    @Override
    public void resourcesChanged() {
        if (isOpen()){
            updateList();
        }
    }

    // sarasu atnaujinimas.
    private void updateList(){
        // einam per jau sena sarasa ir ziurim kurie nebevalid.
        for (int a = active.size-1; a >= 0; a--){
            SelectionButton button = active.get(a);

            if (!Project.isLoadableResourceValid(button.resource)){
                // resource nera valid. Turbut buvo pasalinta isasaraso.
                // salinam musu button.
                active.removeValue(button, true);
                // salinam is tabo
                tabs[button.resource.tab].removeControl(button);

                // metam i pool perpanaudojimui.
                buttonPool.free(button);
            }
        }

        // varom per esamus button. Ziurim ko reik ir ko nebereik. (naujus dadesim, senus updatinsim).
        MAIN:
        for (int a = 0; a < Project.getLoadableResourcesCount(); a++){
            Project.ResourceToHold e = Project.getLoadableResource(a);

            if (e == null){
                continue;
            }

            // varom per egzistuojancous.
            for(SelectionButton button : active){
                if (button.resource == e){
                    // radom musu resource.
                    // tiesiog atnaujinam info.
                    button.setSymbol(e.e);
                    button.setText(e.getIdName());
                    int color = e.visible ? 0xffffffff : 0x99ffffff;
                    button.setSymbolTint(color);
                    int tColor = e.visible ? 0xff000000 : 0x99000000;
                    button.setTextColor(tColor);

                    continue MAIN;
                }
            }

            // kuriam nauja.
            // button neegzistuoja. Kuriam nauja.
            SelectionButton button = buttonPool.obtain();
            button.resource = e;
            button.setText(e.getIdName());
            button.setSymbol(e.e);
            int color = e.visible ? 0xffffffff : 0x99ffffff;
            button.setSymbolTint(color);
            int tColor = e.visible ? 0xff000000 : 0x99000000;
            button.setTextColor(tColor);

            tabs[e.tab].addControl(button);
            active.add(button);
        }
    }

    /* Custom button. */

    private class SelectionButton extends SymbolButton implements Connected, Pool.Poolable {
        Project.ResourceToHold resource;

        private Drawable notVisible;

        private boolean isSelected;

        SelectionButton(SymbolButtonStyle st) {
            super(st);

            notVisible = Resources.getDrawable("noEyeIcon");
        }

        /* listenerss... */

        @Override
        protected void onClick() {
            if (multiSelect) {
                if (isSelected){
                    // atsizymes.
                    inform(0);
                    return;
                }
            } else {
                // pasizymes...
                // bet kitus atzymes.
                ResourcesDialog.this.inform(this, 0);
            }
            isSelected = true;
            setNormalColor(0xFFFF5500);
            selected.add(this);
        }

        /* reusable... situos button reusins.. */

        @Override
        public void reset() {
            resource = null;

            if (isSelected){
                selected.removeValue(this, true);
            }
            isSelected = false;

            setNormalColor(0x00ffffff);

            removeConnection(this); // del visa pikto.
        }

        @Override
        protected void onRemove() {
            super.onRemove();

            // naikinam jungti.
            removeConnection(this);
        }

        @Override
        public void setController(InterfacesController v) {
            super.setController(v);

            // connection dedam i bendra.
            ResourcesDialog.this.addConnection(this);
        }

        /* bendraus tarpusavy. */

        @Override
        public void inform(int reason) { // sita kvies ten interface kontrolleris.
            // reason 0. Visi atsizymi.
            boolean deselect;
            if (reason == 0){
                deselect = true;
            }else { // toliau pagal rusi. jeigu atitinka tai neatzymim. pvz 1 - neatzymes img tabo.
                deselect = reason != resource.tab+1;
            }

            if (deselect) {
                isSelected = false;
                setNormalColor(0x00ffffff);

                selected.removeValue(this, true);
            }
        }

        @Override
        public int getGroup() {
            return -20200425;// kaip visad sukurimo data...
        }

        // piesimas

        @Override
        protected void isvaizda(float x, float y) {
            super.isvaizda(x, y);
            if (!resource.visible){
                // 40 ant 40
                float size = 40;

                float endX = getWidth()-size;
                float endY = getHeight()-size;

                notVisible.draw(p.getBatch(), endX + x, endY + y, size, size);
            }
        }
    }

    private static class ResourceInfoDialog extends PopUp{
        private Drawable visible, notVisible;
        private SImage sImage, visibilityImage;
        private Label label;

        ResourceInfoDialog() {
            super(914, 392);

            visible = Resources.getDrawable("eyeIcon");
            notVisible = Resources.getDrawable("noEyeIcon");

            // kuriam musu label
            Label.LabelStyle lst = new Label.LabelStyle();
            lst.autoSize = false;
            lst.x = 18;
            lst.y = 13;
            lst.width = 528;
            lst.height = 366;
            lst.textSize = 35;

            label = new Label(lst);
            addControl(label);

            // image.
            SImage.SImageStyle sst = new SImage.SImageStyle();
            sst.autoSize = false;
            sst.x = 550;
            sst.y = 20;
            sst.width = 350;
            sst.height = 350;

            sImage = new SImage(sst);
            addControl(sImage);

            // icon.
            sst.x = 852;
            sst.y = 323;
            sst.width = 40;
            sst.height = 40;

            visibilityImage = new SImage(sst);
            addControl(visibilityImage);
        }

        void showInfo(Project.ResourceToHold hold){
            if (hold != null) {
                // image dalis.
                Drawable drawable = hold.visible ? visible : notVisible;
                visibilityImage.setImage(drawable);

                sImage.setImage(hold.e);

                // teksto dalis.
                // id name.
                String id = "Resource id: " + hold.getIdName();

                // path.
                String path;
                String type;
                boolean haveAtlas = false;
                if (hold.tab == 0){
                    if (hold.getIdName().equals(Resources.getProperty("whiteColor", "whiteSystemColor"))){
                        path = "Resource is system image (created by system)";
                    }else {
                        FileTextureData data;
                        if (hold.e instanceof TextureRegionDrawable) {
                            data = (FileTextureData) ((TextureRegionDrawable) hold.e).getRegion().getTexture().getTextureData();
                        } else if (hold.e instanceof SpriteDrawable) {
                            data = (FileTextureData) ((SpriteDrawable) hold.e).getSprite().getTexture().getTextureData();
                        } else {
                            return;
                        }
                        path = data.getFileHandle().path();
                    }
                    type = "Resource type: image";
                }else if (hold.tab == 2){
                    if (hold.atlasFilePath == null){
                        path = "9patch created from resource: " + hold.getAtlasName();
                    }else {
                        path = "9patch owned by TextureAtlas";
                        haveAtlas = true;
                    }
                    type = "Resource type: 9patch";
                }else if (hold.tab == 3){
                    path = ((SpriterDrawable) hold.e).getName();
                    type = "Resource type: animation";
                }else {
                    path = "Region owned by TextureAtlas";
                    type = "Resource type: region of texture atlas";
                    haveAtlas = true;
                }
                String size = "\nSize: " + (int) hold.e.getMinWidth() + " x " + (int) hold.e.getMinHeight();

                String atlas;
                if (haveAtlas){
                    atlas = "\n\nAtlas id: " + hold.getAtlasName() + "\nAtlas path: " + hold.atlasFilePath;
                }else {
                    atlas = "";
                }

                label.setText(id + "\n" + path + "\n\n" + type + size + atlas);

                open();
            }
        }
    }
}
