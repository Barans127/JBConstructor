package com.jbconstructor.main.forms.editorForm;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.animations.Counter;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.PopUp;
import com.engine.interfaces.controls.Toast;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.dialogs.ConfirmDialog;
import com.engine.interfaces.controls.dialogs.PreparedRightClickMenu;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.views.Panel;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.CheckBox;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.RadioButton;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.listeners.ClickListener;
import com.jbconstructor.main.dialogs.FileChooser;
import com.jbconstructor.main.dialogs.FormCreateDialog;
import com.jbconstructor.main.dialogs.SettingPicker;
import com.jbconstructor.main.root.SelectiveInterfaceController;
import com.jbconstructor.main.forms.StartForm;
import com.jbconstructor.main.managers.Project;
import com.jbconstructor.main.managers.ProjectLoader;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/** For more settings to store. */
public class AdditionalPanel extends Panel {
    private final EditForm editForm;
    private final Drawable whiteTexture; // linijom piest.

    private Hider hider;
    private CheckBox[] boxes;

    private PreparedRightClickMenu rightClickMenu;
    private FormCreateDialog createDialog;
    private ConfirmDialog confirm;
    private int dialogState;

    // exportavimui skirti
    private Exporter exporter;
    private ExportPopUp exportPopUp = new ExportPopUp();

    // id prefix keitimui
//    private PrefixIdDialog prefixIdDialog;
    private SettingPicker settingPicker;

    public AdditionalPanel(final EditForm e){
        editForm = e;
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        whiteTexture = Resources.getDrawable("whiteSystemColor");
        setBackground(whiteTexture);
        tintBackground(0xFF879ab7);
        setPositioning(Window.Position.fixed); // sita bus fixed.
        setPosition(0, p.getScreenHeight()); // uz ekrano ribu.
        float width = p.getScreenWidth()*0.8f, height = 190;
        setSize(width, height);

        hider = new Hider(); // pasislepimo animacijai.
        hider.isHidden = true;

        // save as ir export
        SymbolButton.SymbolButtonStyle sst = new SymbolButton.SymbolButtonStyle();
        sst.textSize = 33;
        sst.background = Resources.getDrawable("halfWhiteColor");
        sst.normalColor = 0x00000000;
        sst.onColor = 0xFFFF5500;
        sst.pressedColor = 0xFFAA5500;
        sst.symbol = Resources.getTextureDrawable("additionalPanelDownListKey");
        sst.text = "";
        sst.x = 15;
        sst.autoSize = false;
        sst.width = 30;
        sst.height = 33;
        sst.y = height-sst.textSize*1.1f;
        SymbolButton more = new SymbolButton(sst);
        more.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                rightClickMenu.show();
            }
        });
        addControl(more);
        sst.autoSize = true;
        sst.text = "Save as";
        sst.symbol = Resources.getTextureDrawable("additionalPanelSaveAsKey");
        sst.x = 55;
        SymbolButton saveAs = new SymbolButton(sst); // save as
        saveAs.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                editForm.formCreateDialog.open(); // tiesiog atidarys toki dialog.
                editForm.formCreateDialog.getNameInput().setText(Project.getProjectName());
                if (Project.getProjectRootFolderPath() != null)
                    editForm.formCreateDialog.getLocationInput().setText(Project.getProjectRootFolderPath());
                if (Project.getResourcesPath() != null)
                    editForm.formCreateDialog.getFolderInput().setText(Project.getResourcesPath());
                act();
//                Project.loadResourcesFromResourcesFolder(); // vel perziurim.
//                Project.getSaveManager().wakeWorker(); // kad pradetu auto save , jei uzmiges buvo.
            }
        });
        addControl(saveAs);
        sst.x = 15;
        sst.y -= sst.textSize;
        sst.text = "Export project";
        sst.symbol = Resources.getTextureDrawable("additionalPanelExportKey");
        SymbolButton export = new SymbolButton(sst); // export button.
        export.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
//                exporter.openFileChooser(); // atidarom folder pasirinkimus.\
                exportPopUp.open();
            }
        });
        addControl(export);

        //group objects
//        sst.y -= sst.textSize*1.1f;
        sst.text = "Manage joints";
        sst.textSize = 28;
        sst.symbol = Resources.getTextureDrawable("additionalPanelJointIcon");
        sst.position = SymbolButton.TextPosition.DOWN;
        SymbolButton group = new SymbolButton(sst); // joints
        group.auto();
        group.customSymbolSize(25, 25);
        group.setPosition(25, sst.y - group.getHeight() - sst.textSize);
        group.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                hider.act();
                editForm.getMainPanel().openControlPanel(2, true);
            }
        });
        addControl(group); // joint.

        // chains.
        sst.y = sst.y - group.getHeight() - sst.textSize;
        sst.x += group.getPosition().x + group.getWidth();
        sst.text = "Manage chains";
        sst.symbol = Resources.getTextureDrawable("additionalPanelChainKey");
        SymbolButton chain = new SymbolButton(sst); // chain mygtukas
        chain.customSymbolSize(25, 25);
        chain.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                openChainEdgePanel();
            }
        });
        addControl(chain);

        // checkboxai.
        boxes = new CheckBox[4];
        CheckBox.CheckBoxStyle cst = new CheckBox.CheckBoxStyle();
        cst.checked = true;
        cst.textOffsetX = 5f;
        cst.checkedBox = Resources.getDrawable("defaultCheckBoxTicked");
        cst.box = Resources.getDrawable("defaultCheckBox");
        cst.x = 250;
        cst.y = height - cst.textSize;
        cst.text = "Show fixed";
        CheckBox fixed = new CheckBox(cst); // fixed kontrolem
        fixed.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                checkBoxChecked(checked, 0);
            }
        });
        addControl(boxes[0] = fixed);
        cst.text = "Show absolute";
        cst.y -= cst.textSize;
        CheckBox absolute = new CheckBox(cst); // absolute kontrolem
        absolute.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                checkBoxChecked(checked, 1);
            }
        });
        addControl(boxes[1] = absolute);
        cst.text = "Show coordinates";
        cst.y -= cst.textSize;
        CheckBox coords = new CheckBox(cst); // kordinaciu rodymas
        coords.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                checkBoxChecked(checked, 2);
            }
        });
        addControl(boxes[2] = coords);
        cst.text = "Show screen bounds";
        cst.y -= cst.textSize;
        final CheckBox bounds = new CheckBox(cst); // screen bounds
        bounds.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                checkBoxChecked(checked, 3);
            }
        });
        addControl(boxes[3] = bounds);

        // settings mygtukas
        sst.x = 835;
        sst.y *= 1.5f;
        sst.symbol = Resources.getTextureDrawable("additionalPanelSettingsKey");
        sst.text = "Editor settings";
        sst.textSize = 45;
        SymbolButton sett = new SymbolButton(sst);
        sett.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
//                prefixIdDialog.open();
                settingPicker.open();
            }
        });
        addControl(sett);
//        Resources.addImage("additionalPanelSwitchKey", "resources/constructorUI/editorPanel/additionalPanel/switchTabs.png");
        sst.symbol = Resources.getTextureDrawable("additionalPanelSwitchKey");
        sst.x = 675;
        sst.text = "Switch forms";
        final SymbolButton swit = new SymbolButton(sst); // switch button.
        // rightClick listener.
        swit.customSymbolSize(50, 50);
        swit.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                editForm.formsDialog.open();
            }
        });
        addControl(swit);

        // right click menu
        PreparedRightClickMenu menu = new PreparedRightClickMenu();
        menu.getStyle().textSize = 33;
        menu.addListItem("New project");
        menu.addListItem("Load project");
        menu.addListItem("Close project");
        menu.setMenuItemClickListener(new PreparedRightClickMenu.MeniuItemClickedListener() {
            @Override
            public void clickedItem(Interface owner, int index, String itemName) {
                if (index == 2){
//                    Project.closeProject(); // uch risky. nieko neissaugo.
                    closeProjectClick();
                }else if (index == 1){
                    loadProjectClick();
                }else {
                    newProjectClick();
                }
            }
        });
        Vector2 pos = more.getPosition();
        menu.setPosition(pos.x, pos.y-menu.getHeight()-getHeight()); // reik atimt ir paneles dydi, nes gi ji virsuj uzlindus.
        rightClickMenu = menu;

        // formu creation dialogas.
        createDialog = new FormCreateDialog();
        createDialog.setFormCreateListener(new FormCreateDialog.CreateFormDialogListener() {
            @Override
            public boolean onCreate(String name, String rootLocation, String folderPath) {
                if (name == null || name.length() == 0){
                    return false;
                }
//                Project.closeProject(); // uzdarom dabartini projekta.
                ProjectLoader loader = new ProjectLoader();
                loader.createNewProject(name, rootLocation, folderPath);
                Project.getFormsManager().switchForms(0, true); // po sukurimo reik butinai perjunkt, arba liks start formoj pastrige.
                return true;
            }

            @Override
            public void cancel() {}
        });
//        editForm.addPopUp(createDialog); // addpop up nebenaudojamas.

        // confimr dialogas
        confirm = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.YesNoCancel);
        confirm.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
            @Override
            public void onYes() {
                if (Project.getProjectRootFolderPath() == null && dialogState != 2){ // ant 2 ignoruot turetu.
                    editForm.formCreateDialog.open(); // tiesiog atidarys toki dialog.
                    editForm.formCreateDialog.getNameInput().setText(Project.getProjectName());
                    if (Project.getProjectRootFolderPath() != null)
                        editForm.formCreateDialog.getLocationInput().setText(Project.getProjectRootFolderPath());
                    if (Project.getResourcesPath() != null)
                        editForm.formCreateDialog.getFolderInput().setText(Project.getResourcesPath());
                    return; // tures is naujo rinkits.
                }
                if (dialogState != 2) {
                    if (!Project.getSaveManager().saveProject()){
                        new AlertToast("Unknown error occurred while trying to save! Please try again!").show();
                        return;
                    }
                }
                projectActions(true);
            }

            @Override
            public void onNo() { // paspaudus no
                projectActions(false);
            }

            @Override
            public void onCancel() { }
        });
//        editForm.addPopUp(confirm); // addpop up nebenaudojamas

        exporter = new Exporter();

//        prefixIdDialog = new PrefixIdDialog(editForm);
        settingPicker = new SettingPicker(editForm);
    }

    /* listeners */
    private void projectActions(boolean yes){
        switch(dialogState){
            case 0: // naujo projekto
                createDialog.open();
                break;
            case 1:
                openFileChooser();
                break;
            case 2: // close project
                if (yes)
                    Project.closeProject();
                break;
            case 3:
//                closeProjectClick();
                dialogState = 2;
                confirm.setType(ConfirmDialog.ConfirmDialogType.YesNo);
                confirm.show("Are you sure you want to close the project?");
                break;
        }
    }

    private void newProjectClick(){ // naujo projecto.
        if (Project.getSaveManager().isProjectSaved()){
            createDialog.open();
        }else {
            dialogState = 0; // tipo new project.
            confirm.setType(ConfirmDialog.ConfirmDialogType.YesNoCancel);
            confirm.show("Save before creating a new project?");
        }
    }

    private void openFileChooser(){ // cia lyg ir tas kur atidaryti kita projecta.
        FileChooser fh = Project.getFileChooser();
        final JFileChooser jh = fh.getFileChooser();
        jh.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jh.setMultiSelectionEnabled(false);
        jh.setFileFilter(new FileNameExtensionFilter("Project files", "jbcml"));
        jh.setDialogTitle("Select project file");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (jh) {
                    FileChooser fh = Project.getFileChooser();
                    jh.setCurrentDirectory(new File("."));
                    fh.open(StartForm.listener, true); // projekto uzkrovimas.
                }
            }
        });
    }

    private void loadProjectClick(){
        if (Project.getSaveManager().isProjectSaved()){
            openFileChooser();
        }else {
            dialogState = 1;
            confirm.setType(ConfirmDialog.ConfirmDialogType.YesNoCancel);
            confirm.show("Save before loading a project?");
        }
    }

    private void closeProjectClick(){
        if (Project.getSaveManager().isProjectSaved()){
            dialogState = 2;
            confirm.setType(ConfirmDialog.ConfirmDialogType.YesNo);
            confirm.show("Are you sure you want to close the project?");
        }else {
            dialogState = 3;
            confirm.setType(ConfirmDialog.ConfirmDialogType.YesNoCancel);
            confirm.show("Save before closing the project?");
        }
    }

    private void checkBoxChecked(boolean state, int index){
        switch (index){
            case 0: // fixed piesimas
                ((SelectiveInterfaceController) editForm.getController()).disableFixedDraw(!state);
                editForm.getSizer().releaseAll();
                editForm.getMainPanel().update();
                break;
            case 1: // absolute piesimas
                ((SelectiveInterfaceController) editForm.getController()).disableAbsoluteDraw(!state);
                editForm.getSizer().releaseAll();
                editForm.getMainPanel().update();
                break;
            case 2: // coord rodymas
                editForm.getMainPanel().showCoords(state);
                break;
            case 3: // ekrano ribos.
                editForm.showScreenBounds(state);
                break;
        }
    }

    void openChainEdgePanel(){
//        editForm.getMainPanel().openChainPanel();
        editForm.getMainPanel().openControlPanel(1, true);
        if (!hider.isHidden)
            act(); // uzdarom sita panele.
    }

    /* perdavimo linija */

    /** 0 - fixed, 1- absolute, 2 - coords, 3 - bounds */
    public CheckBox[] getBoxes() {
        return boxes;
    }

//    public PrefixIdDialog getPrefixIdDialog(){
//        return prefixIdDialog;
//    }

    /** setting picker menu. */
    public SettingPicker getSettingPicker(){
        return settingPicker;
    }

    /* Atsiradimo ir dingimo animacijos. */

    /** appear or dissaper */
    void act(){
        hider.act();
    }

    public void open(){
        if (!isVisible())
            act();
    }

    public void close(){
        if (isVisible())
            act();
    }

    /* override */

    @Override
    public boolean keyDown(int keycode) {
        if (super.keyDown(keycode)){
            return true;
        }else if (keycode == Input.Keys.ESCAPE){
            act();
            return true;
        }else
            return false;
    }

    @Override
    protected void isvaizda(float x, float y) {
        super.isvaizda(x, y);
        p.tint(0);
        float lineWidth = 2f;
        whiteTexture.draw(p.getBatch(), x+240, y, lineWidth, getHeight()); // linija skyriant check box ir pagrindinius
        whiteTexture.draw(p.getBatch(), x, y+110, 240, lineWidth); // linija skyrianti export ir group
        whiteTexture.draw(p.getBatch(), x+640, y, lineWidth, getHeight());
        p.noTint();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (super.touchDown(x, y, pointer, button)) {
            return true;
        }
        Vector2 pos = getPosition();
        if (button == Input.Buttons.LEFT) { // tik kaires mygtukas.
            if (x < pos.x || x > pos.x + getWidth() || y < pos.y || y > pos.y + getHeight()) { // paspausta uz panelio ribu.
                act();
            }
        }
        return true; // jau cia aisku, kad tik panelio zonoj tai vyksta todel true.
    }

    // export dialogas, kur renkamasi kur saugot exportinta continent.
    private class Exporter implements FileChooser.FileChooseListener{
        private FileChooser fh = Project.getFileChooser();
        private JFileChooser jh = fh.getFileChooser();
        private String previousPath; // atsimint paskutine vieta.

        // defaultiskai lai buna true, json ir compress formatu.
        private boolean isJson = true;
        private boolean compress = true;

        private Toast error = new Toast();

        public void openFileChooser(){ // atidarys ir leis pasirink kur saugot
//            FileChooser fh = Project.getFileChooser();
//            JFileChooser jh = fh.getFileChooser();
            jh.setMultiSelectionEnabled(false);
            jh.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // imsim tik
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    jh.setDialogTitle("Export");
                    if (previousPath == null) {
                        if (Project.getProjectRootFolderPath() == null) {
                            jh.setCurrentDirectory(new File("."));
                        } else {
                            jh.setCurrentDirectory(new File(Project.getProjectRootFolderPath()));
                        }
                    }else {
                        jh.setCurrentDirectory(new File(previousPath)); // jei buvo exportinta ir successfull tai vel i ta pacia vieta.
                    }
                    fh.open(Exporter.this, false); // nebutina main thread, gi tik perkelt file, neliecia graphics.
                }
            });
        }

        @Override
        public void fileChosen(JFileChooser chooser) {
            boolean done = Project.getSaveManager().exportProject(chooser.getSelectedFile().getAbsolutePath(), isJson, compress);
            if (done) {
                previousPath = chooser.getSelectedFile().getAbsolutePath(); // atsimenam paskutini.
                error.setText("Project was exported successfully"); // pranesam, kad viskas gerai.
                error.show();
            } else {
                error.setText("Project was not exporter. Error occurred while trying export project. Please try again.");
                error.show();
            }
        }
    }

    private class Hider implements Counter.CounterInformer, Counter.CounterListener{
        private Counter counter;
        private boolean isHidden;
        private float startY;

        private float animationTime = 0.5f;

        Hider(){
            counter = MoreUtils.getCounter();
//            counter.setIrrevocable(true); // tegu baigia savo skaiciavimus.
            // counter pervadintas metodas.
            counter.setUninterruptible(true);
            counter.setCounterInformer(this);
            counter.setCounterListiner(this);
            startY = AdditionalPanel.this.getPosition().y;
            isHidden = !isVisible();
        }

        /** time in seconds. */
        public void setAnimationTime(float time){
            animationTime = time;
        }

        void act(){ // mygtuko paspaudimas.
            if (!counter.isCounting()) {
                if (isHidden) {
                    isHidden = false;
                    setVisible(true);
                    counter.startCount(0, getHeight(), animationTime);
                } else {
                    isHidden = true;
                    counter.startCount(getHeight(), 0, animationTime);
                }
            }
        }

        @Override
        public void update(float oldValue, float currentValue) {
            Vector2 e = AdditionalPanel.this.getPosition();
            AdditionalPanel.this.setPosition(e.x,startY - currentValue);
        }

        @Override
        public void finished(float currentValue) {
            if (isHidden)
                AdditionalPanel.this.setVisible(false); // paslept, nes nematoma.
        }

        @Override
        public boolean cancel(int reason) {
            return false;
        }
    }

    private class ExportPopUp extends PopUp{ // export dialogas, del exportinimo setting pasirinkimo.
        private RadioButton xml, json;
        private CheckBox compress;

        ExportPopUp() {
            super(550, 372);

            // label
            Label.LabelStyle lst = new Label.LabelStyle();
            lst.autoSize = false;
            lst.x = 33;
            lst.y = 309;
            lst.textSize = 56;
            lst.shrinkText = false;
            lst.text = "Export settings...";
            lst.width = 472;
            lst.height = 50;

            addControl(lst.createInterface());

            // json radio button
            RadioButton.RadioButtonStyle rst = new RadioButton.RadioButtonStyle();
            rst.autoSize = false;
            rst.shrinkText = false;
            rst.text = "Export to json format";
            rst.textSize = 56;
            rst.x = 33;
            rst.y = 241;
            rst.width = 532;
            rst.height = 56;
            rst.box = Resources.getDrawable("defaultRadioBox");
            rst.checkedBox = Resources.getDrawable("defaultRadioTicked");

            json = new RadioButton(rst);
            json.setCheckListener(new CheckBox.CheckedListener() {
                @Override
                public void onCheck(boolean checked) {
                    if (checked)
                        exporter.isJson = true;
                }
            });
            addControl(json);

            // xml radio button
            rst.text = "Export to xml format";
            rst.y = 166;

            xml = new RadioButton(rst);
            xml.setCheckListener(new CheckBox.CheckedListener() {
                @Override
                public void onCheck(boolean checked) {
                    if (checked)
                        exporter.isJson = false;
                }
            });
            addControl(xml);

            /// compress check box
            CheckBox.CheckBoxStyle cst = new CheckBox.CheckBoxStyle();
            cst.autoSize = false;
            cst.x = 34;
            cst.y = 91;
            cst.width = 532;
            cst.height = 56;
            cst.textSize = 48;
            cst.shrinkText = false;
            cst.text = "Compress (reduces size)";
            cst.box = Resources.getDrawable("defaultCheckBox");
            cst.checkedBox = Resources.getDrawable("defaultCheckBoxTicked");

            compress = new CheckBox(cst);
            compress.setCheckListener(new CheckBox.CheckedListener() {
                @Override
                public void onCheck(boolean checked) {
                    exporter.compress = checked;
                }
            });
            addControl(compress);

            // export mygtukas
            Button.ButtonStyle bst = new Button.ButtonStyle();
            bst. autoSize = false;
            bst.x = 401;
            bst.y = 17;
            bst.width = 130;
            bst.height = 50;
            bst.textSize = 56;
            bst.text = "Export...";
            bst.background = Resources.getDrawable("whiteSystemColor");
            bst.normalColor = 0xff0000ff;

            Button export = new Button(bst);
            export.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    // iskvieciam export pasirinkimo folderi.
                    close(); // uzdarom musu pop up.
                    AdditionalPanel.this.close(); // uzdarom panele, nereik jos.
                    exporter.openFileChooser();
                }
            });
            addControl(export);
        }

        @Override
        protected void onOpen() {
            super.onOpen();

            // sugaudom export n ustatymus, kad butu tikslaii pardoyta kaip yura
            if (exporter.isJson){
                json.setChecked(true);
            }else
                xml.setChecked(true);

            compress.setChecked(exporter.compress);
        }
    }
}
