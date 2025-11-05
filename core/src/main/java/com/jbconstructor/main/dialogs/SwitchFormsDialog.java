package com.jbconstructor.main.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.engine.core.ErrorMenu;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.PopUp;
import com.engine.interfaces.controls.Toast;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.dialogs.ConfirmDialog;
import com.engine.interfaces.controls.dialogs.InputTextDialog;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.views.PreparedListView;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.controls.widgets.TextBox;
import com.engine.interfaces.listeners.ClickListener;
import com.engine.root.GdxPongy;
import com.jbconstructor.main.managers.FormsManager;
import com.jbconstructor.main.managers.Project;

/** class which allows to switch between edit forms. */
public class SwitchFormsDialog extends PopUp {
    private FormsManager formsManager = Project.getFormsManager();// formu manageris.
//    private final EditForm owner;
    private String formName;

    // formos
    private PreparedListView formsTabs;
    private Drawable whiteColor, formEmptySS;
//    private ArrayList<Interface> forms;
//    private SymbolButton.SymbolButtonStyle style; // kad butu lengva pridet formas.

    // some dialogs.
    private InputTextDialog createForm, changeText;
    private ConfirmDialog aprroveDialog;

    // editinimo irankiai
    private Button edit, delete, open;
    private int lastIndex = -1, formIndex;
    private Toast error;

    public SwitchFormsDialog(final String formName) {
        super(GdxPongy.getInstance().getScreenWidth()*0.9f, GdxPongy.getInstance().getScreenHeight()*0.6f);
//        this.owner = owner;
        this.formName = formName;
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        whiteColor = Resources.getDrawable("whiteSystemColor");
        tintBackground(0xE51f2b2d);

        // label
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.text = "Choose form:";
        lst.textSize = 80;
        lst.y = getHeight()-lst.textSize;
        addControl(new Label(lst));

        // listview
        PreparedListView.PreparedListViewStyle lwst = new PreparedListView.PreparedListViewStyle();
        lwst.separatorSize = 0;
        lwst.isVertical = false;
        lwst.columnCount = 1;
        lwst.width = getWidth();
        lwst.height = getHeight()-lst.textSize;
//        lwst.background = whiteColor;
        lwst.itemHeight = lwst.height;
        lwst.itemWidth = lwst.height;
        lwst.scrollSpeed = 50;
        lwst.normalState = 0x00000000;
        lwst.choosenState = 0x9512e543;
        lwst.activeItemsStyle.normalColor = 0x00000000;
        lwst.activeItemsStyle.onColor = 0xE5FF5500;
        lwst.activeItemsStyle.pressedColor = 0xFFFF5500;
        lwst.activeItemsStyle.position = SymbolButton.TextPosition.DOWN;
        lwst.activeItemsStyle.symbolWidth = lwst.itemWidth;
        lwst.activeItemsStyle.symbolHeight = lwst.itemHeight*0.7f;
        formsTabs = lwst.createInterface();
        formsTabs.setActiveItemClickListener(new PreparedListView.ActiveItemClickListener() {
            @Override
            public void onActiveItemClick(Interface activeItem, int index, Object userData) {
                activeItemClick(activeItem, index);
            }
        });
        addControl(formsTabs);
//        forms = (ArrayList<Interface>) formsTabs.getHost().getControls();


        // add button/
        SymbolButton.SymbolButtonStyle bst = new SymbolButton.SymbolButtonStyle();
        bst.background = whiteColor;
        bst.normalColor = 0x00000000;
        bst.onColor = 0xE5FF5500;
        bst.pressedColor = 0xFFFF5500;
//        Resources.addImage("mainEditorFormIcon", "resources/constructorUI/editorPanel/formIcon.png");
        formEmptySS = Resources.getTextureDrawable("mainEditorFormIcon");
        // test
//        formsTabs.addItem("Edit form", formEmptySS);
//        formsTabs.addItem("Edit form2", formEmptySS);
//        bst.text = "Edit Form";
        bst.position = SymbolButton.TextPosition.DOWN;
        bst.symbolWidth = lwst.itemWidth;
        bst.symbolHeight = lwst.itemHeight*0.7f;

//        for (int a = 0; a < 10; a++){
//            bst.text += a;
//            formsTabs.addControl(bst.createInterface());
//        }

//        Resources.addImage("mainEditorAddIconKey", "resources/constructorUI/editorPanel/addForm.png");
//        Drawable old = bst.symbol;
        bst.symbol = Resources.getTextureDrawable("mainEditorAddIconKey");
        bst.text = "";
        SymbolButton lastButton = new SymbolButton(bst);
        formsTabs.addControl(lastButton);
        lastButton.setSymbolSize(lastButton.getWidth()*0.5f, lastButton.getHeight()*0.5f);
        lastButton.setCustomSymbolSizeOffset(lastButton.getWidth()/2 - lastButton.getSymbolWidth()/2,
                lastButton.getHeight()/2 - lastButton.getSymbolHeight()/2);
        lastButton.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                createNewForm();
            }
        });
//        style = bst; // veliau prireiks.

        // editinimo mygtukai, atsiras tik pasirinkus forma.
//        Resources.addImage("mainEditorEditIconKey", "resources/constructorUI/editorPanel/editButton.png");
        bst.normalColor = 0x9512e543;
        bst.positioning = Window.Position.absolute;
        bst.visible = false;
        bst.background = Resources.getTextureDrawable("mainEditorEditIconKey");
        bst.autoSize = false;
        bst.width = 40;
        bst.height = 40;
        edit = new Button(bst);
        edit.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                changeText.show();
                changeText.getInput().setText(formsManager.getFormName(lastIndex));
            }
        });
        addControl(edit);

        bst.background = Resources.getTextureDrawable("mainEditorDeleteIconKey");
        delete = new Button(bst);
        delete.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                if (lastIndex != formsManager.getFormIndex(SwitchFormsDialog.this.formName)) { // atidarysim tik jei forma tai ne dabar atidaryta forma.
                    aprroveDialog.show();
                }
            }
        });
        addControl(delete);

        bst.width = 80;
        bst.height = 80;
        bst.background = Resources.getTextureDrawable("mainEditorOpenIconKey");
        open = new Button(bst);
        open.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                switchForms();
            }
        });
        addControl(open);

        // dialogs
        // new form dialog.
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.maxLength = 30;
        tst.background = whiteColor;
        createForm = new InputTextDialog(new Label.LabelStyle(), tst);
        createForm.setText("Enter new form's name:");
        createForm.setInputDialogListener(new InputTextDialog.InputDialogListener() {
            @Override
            public void onInput(String input) {
                if (input != null && input.length() > 0) {
                    boolean e = !formsManager.createEditForm(input);
                    checkFormsChange();
                    if (e){ // nesukure formos.
//                        System.out.println("Form was not created with name: " + input);
                        error.setText("Forms name already exists: " + input);
                        error.show();
//                        createForm.show(input);
                        createForm.show();
                        createForm.getInput().setText(input);
                    }else {
                        Project.save();
                    }
                }else {
                    error.setText("Forms name cannot be empty");
                    error.show();
                    createForm.show();
                }
            }

            @Override
            public void cancel() {}
        });
        aprroveDialog = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.YesNo, new Label.LabelStyle());
        aprroveDialog.setText("Delete this form? After deletion every process in this form will be lost.");
        aprroveDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
            @Override
            public void onYes() { // istrint pasirinkta forma. iskyrus jei tai dabar esama forma.
                formsManager.removeForm(lastIndex);
                formsTabs.deselectItems();
                lastIndex = -1;
                open.setVisible(false);
                delete.setVisible(false);
                edit.setVisible(false);
                formIndex = formsManager.getFormIndex(SwitchFormsDialog.this.formName); // perskaiciuojam formos id.
                checkFormsChange();

                Project.save();
            }

            @Override
            public void onNo() {

            }

            @Override
            public void onCancel() {

            }
        });
        changeText = new InputTextDialog(new Label.LabelStyle(), tst);
        changeText.setText("Change form's name:");
        changeText.setInputDialogListener(new InputTextDialog.InputDialogListener() {
            @Override
            public void onInput(String input) {
                if (input != null && input.length() > 0){
                    if (formsManager.changeFormsName(input.trim(), lastIndex)) { // vardas buvo pakeistas
//                        SwitchFormsDialog.this.formName = input.trim();
                        checkFormsChange();

                        Project.save();
                    } else { // vardo nepavyko pakeist.
                        error.setText("Forms name already exist: " + input);
                        error.show();
                        changeText.show();
                        changeText.getInput().setText(input);
                    }
                }else { // tuscias laukas paliktas.
                    error.setText("Forms name cannot be empty");
                    error.show();
                    changeText.show();
                }
            }

            @Override
            public void cancel() {

            }
        });
        Toast.ToastTextStyle textStyle = new Toast.ToastTextStyle();
        textStyle.textColor = 0xFFFF0000;
        textStyle.textSize = 60;
        error = new AlertToast();
        error.setToastTextStyle(textStyle);
//        ((AlertToast) error).setTextSize(60);
        error.setBackground(Resources.getDrawable("systemWhiteRect"));
    }

    /* veikimas */

    private  void updateEditItemsLocation(){
        Interface e = formsTabs.getHost().getControls().get(lastIndex+1);
        Vector2 pos = e.getPosition();
        float x = pos.x + formsTabs.getHost().getOffsetX(), y = pos.y + formsTabs.getHost().getOffsetY();
        float right = x + formsTabs.getItemWidth();
        if (x < getPosition().x || right > getPosition().x + getWidth()){
            open.setVisible(false);
            edit.setVisible(false);
            delete.setVisible(false);
        }else {
            if (lastIndex == formIndex){
                open.setEnabled(false);
                delete.setEnabled(false);
            }else {
                open.setEnabled(true);
                delete.setEnabled(true);
            }
            open.setVisible(true);
            edit.setVisible(true);
            delete.setVisible(true);
        }
        edit.setPosition(x, y + formsTabs.getItemHeight()-edit.getHeight());
        delete.setPosition(x+formsTabs.getItemWidth()-delete.getWidth(), y + formsTabs.getItemHeight()-edit.getHeight());
        open.placeInMiddle(x + formsTabs.getItemWidth()/2, y + formsTabs.getItemHeight()/2);
    }

    /* listeners */

    private void switchForms(){ // perjunks i kita forma.
        close(); // pats uzsidarys.
        formsManager.switchForms(lastIndex, true);
    }

    private void activeItemClick(Interface e, int index){
        if (index == lastIndex){
            formsTabs.deselectItems();
            open.setVisible(false);
            delete.setVisible(false);
            edit.setVisible(false);
            lastIndex = -1;
        }else {
            lastIndex = index;
            updateEditItemsLocation();
        }
    }

    // tures sukurt nauja forma, gal iskart atidaryt ja? O, gal pirma paklaust visu detaliu.
    private void createNewForm(){
        createForm.show();
    }

    /* Snekejimas su formManager */

    private void checkFormsChange(){
        int formsSize = formsManager.getFormsSize();
        int size = formsTabs.getHost().getControls().size();
        if (formsSize < size-2){ // vadinas yra daugiau tabu nei turetu but.
            for (int a = size-2; a > formsSize; a--){
                formsTabs.removeControl(formsTabs.getHost().getControls().get(a));
            }
        }
        for (int a = 0, b = 1; a < formsSize; a++, b++){
            if (a == size-2){ // trukst dalies.
                Drawable ss = formsManager.getFormSS(a) == null ? formEmptySS : formsManager.getFormSS(a);
                if (ss == formEmptySS){
                    formsTabs.getActiveStyle().symbolTint = 0xFFFFFFFF;
                }else {
                    formsTabs.getActiveStyle().symbolTint = 0x99FFFFFF;
                }
                formsTabs.addItem(formsManager.getFormName(a), ss, size-1);
                size++;
            }else { // pacheckinam ar visks gerai.
                Interface e = formsTabs.getHost().getControls().get(b);
                if (e instanceof SymbolButton){
                    SymbolButton s = (SymbolButton) e;
                    String text = formsManager.getFormName(a);
                    if (!(s.getText().equals(text))){
                        s.setText(text);
                    }
                    Drawable ss = formsManager.getFormSS(a);
                    if (ss != s.getSymbol()){ // jeigu ss neatitinka.
                        if (ss == null){ // ss net nera.
                            if (s.getSymbol() != formEmptySS){ // jeigu ss buvo, tai reik nurodyt, kad neber
                                s.setSymbol(formEmptySS);
                                s.setSymbolTint(0xFFFFFFFF);
                            }
                        }else {
                            s.setSymbol(ss); // nustatom nauja ss.
                            s.setSymbolTint(0x99FFFFFF);
                        }
                    }
                }else {
                    p.setError("SwitchFormsDialog: Interface wrong instance. SymbolButton instance was expected. Found: " +
                            e.getClass().getSimpleName(), ErrorMenu.ErrorType.ControlsError);
                    return;
                }
            }
        }
    }

    /* override metodai */

    @Override
    public boolean scrolled(float amountX, float amountY) {
        boolean rez = super.scrolled(amountX, amountY);
        if (lastIndex > -1) {
            updateEditItemsLocation();
        }
        return rez;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (lastIndex > -1)
            updateEditItemsLocation();
        return super.pan(x, y, deltaX, deltaY);
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (lastIndex > -1)
            updateEditItemsLocation();
        return super.panStop(x, y, pointer, button);
    }

    // pakeitus engine dali su pop up pridejimu (nebereik to daryt) visas sis metodas neteko esmes, nes pop up perkelt niekur nereik.
//    @Override
//    protected void onFormChange(Form e, Form old) {
//        if (old!=null){
//            old.removePopUp(createForm);
//            old.removePopUp(aprroveDialog);
//            old.removePopUp(changeText);
//        }
//        if (e != null){
//            e.addPopUp(createForm);
//            e.addPopUp(aprroveDialog);
//            e.addPopUp(changeText);
//        }
//    }

    @Override
    public void onOpen() {
        super.onOpen();
        edit.setVisible(false);
        open.setVisible(false);
        delete.setVisible(false);
        formsTabs.deselectItems();
        lastIndex = -1;
        // ss padarymas
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
        // this loop makes sure the whole screenshot is opaque and looks exactly like what the user is seeing
        for(int i = 4; i < pixels.length; i += 4) {
            pixels[i - 1] = (byte) 255;
        }
        Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
        Texture e = new Texture(pixmap);
        pixmap.dispose();
        formIndex = formsManager.getFormIndex(formName);
        formsManager.setFormSS(e, formIndex);
        checkFormsChange();
//        formsManager.setFormSS(e);
    }

//    @Override
//    public void onClose() {
//        super.onClose();
//
//    }

    @Override
    protected void drawBackground(float x, float y, float w, float h) {
        p.tint(0x5e1f2b2d);
        whiteColor.draw(p.getBatch(), 0, 0, p.getScreenWidth(), p.getScreenHeight());
        super.drawBackground(x, y, w, h);
    }

    /* liev metodai, bisk prastai */

    /** Do not use this as it can cause bugs. Used by {@link FormsManager} */
    public void nameWasChanged(String name){
        formName = name;
    }
}
