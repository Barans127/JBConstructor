package com.jbconstructor.main.forms.editorForm;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.InterfacesController;
import com.engine.interfaces.controls.PopUp;
import com.engine.interfaces.controls.Toast;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.dialogs.ConfirmDialog;
import com.engine.interfaces.controls.dialogs.InputTextDialog;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.views.Panel;
import com.engine.interfaces.controls.views.ScrollView;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.CheckBox;
import com.engine.interfaces.controls.widgets.ComboBox;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.controls.widgets.TextBox;
import com.engine.interfaces.listeners.ClickListener;
import com.engine.interfaces.listeners.FocusListener;
import com.jbconstructor.main.dialogs.AngularSelectionDialog;
import com.jbconstructor.main.editors.ChainEdging;
import com.jbconstructor.main.editors.JointManager;
import com.jbconstructor.main.managers.Project;
import com.jbconstructor.main.root.Element;
import com.jbconstructor.main.root.FieldMover;
import com.jbconstructor.main.root.FiguresDrawer;
import com.jbconstructor.main.root.PhysicsHolder;
import com.jbconstructor.main.root.Resizer;
import com.jbconstructor.main.root.SelectiveInterfaceController;
import com.jbconstructor.main.root.Selector;

public class JointControlPanel extends Panel implements EditMainPanel.ControlPanel {
    private EditForm editForm;
    private JointManager jointManager;

    // veikiamieji.
    private ComboBox jointList; // joint list
    private ScrollView settingsView; // panele kur su nustatymais
    private PanelPieceFactory panelPieceFactory; // daiktai kurie atkeliauja ant paneles skirtingiems jointams.

    // panel joint nustatymo kontroles
    private ComboBox jointTypes; // joint type pasirinkimas.
    private ComboBox bodyA, bodyB;
    private CheckBox collideConnected;
    private SymbolButton bodyAButton, bodyBButton;

    // pasirinktas joint.
    private int selectedJoint;

    // ar rodomi visi kunai
    private boolean allBodyVisible = false;

    // joint, kunu ir t.t. piesimas
    private JointControlDrawer drawer;
    // joint inputu valdymas
    private JointControlInputs inputs;
    // body a tai visi body a piesimai, body b visi body b piesimai, o others tai cia kai mygtuka paspaus ir pies absoliuciai viska.
    private Array<FiguresDrawer> bodyAFigures, bodyBFigures, otherVisuals, jointFigures;
    private Vector2 bodyAPosition = new Vector2(), bodyBPosition = new Vector2();
    private float bodyAAngle, bodyBAngle;

    // point panelio valdymas
    private JointControlPointPanel pointPanel;

    public JointControlPanel(EditForm editForm, EditMainPanel editMainPanel){
        this.editForm = editForm;
        jointManager = editForm.getJointManager();

        float width = p.getScreenWidth()*0.2f, height = p.getScreenHeight()*0.85f;
        setSize(width, height);

        /* header sukurimas. */
        // joint id label.
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.autoSize = false;
        lst.x = 5;
        lst.y = 554;
        lst.width = 181;
        lst.height = 40;
        lst.text = "Joint ID:";
        lst.verticalAlign = Align.center;

        addControl(lst.createInterface());

        // combo box, kuris laikys visus joint.
        ComboBox.ComboBoxStyle cst = new ComboBox.ComboBoxStyle();
        cst.background = Resources.getDrawable("systemWhiteRect");
        cst.x = 5;
        cst.y = 504;
        cst.width = 245;
        cst.height = 40;
        cst.autoSize = false;
        cst.defaultWord = "-non selected-";
        cst.horizontalAlign = Align.center;
        cst.verticalAlign = Align.center;
        cst.listBackground = Resources.getDrawable("whiteSystemColor");
        cst.onColor = 0xffff0000;

        ComboBox jointIds = new ComboBox(cst);
        jointIds.setIndexChangeListener(new ComboBox.IndexChangeListener() {
            @Override
            public void selectedIndexChanged(int old, int current) {
                selectedJoint = current; // tiesiog pagal id parenkam joint.
                jointTypeSelected(); // cia duagiau viska sutvarkys.
            }
        });
        addControl(jointIds);
        jointList = jointIds;

        // buttons.
        // create button
        Button.ButtonStyle bst = new Button.ButtonStyle();
        bst.autoSize = false;
        bst.x = 15;
        bst.y = 453;
        bst.width = 109;
        bst.height = 40;
        bst.background = cst.listBackground; // toki pat naudos koki naudoja ir combobox list background.
        bst.text = "Create";
        bst.normalColor = 0xff0000ff;

        Button create = new Button(bst);
        create.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                createNewJoint();
            }
        });
        addControl(create);

        // edit button
        bst.x = 136;
        bst.text = "Edit";

        Button edit = new Button(bst);
        edit.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                new EditDialog().show(jointManager.getJointInfo(selectedJoint));
            }
        });
        addControl(edit);

        /* Toliau reik scroll view, kuriam visa likusi paneles dalis eis. */
        ScrollView.ScrollViewStyle sst = new ScrollView.ScrollViewStyle();
        sst.autoSize = false;
        sst.width = getWidth();
        sst.height = 447;
        sst.virtualWidth = sst.width;
        sst.virtualHeight = sst.height;


        ScrollView jointInfoPanel = new ReverseScrollView(sst);
        addControl(jointInfoPanel);
        settingsView = jointInfoPanel;

        /* default daiktai esantys joint paneli. */
        // joint type label
        lst.textSize = 35;
        lst.y = 403;
        lst.text = "Joint type:";

        jointInfoPanel.addControl(lst.createInterface());

        // joint type combo box. Su jau zinomai daiktais.
        cst.y = 354;
        cst.textSize = 35;

        ComboBox jointList = new ComboBox(cst);
        jointList.setIndexChangeListener(new ComboBox.IndexChangeListener() {
            @Override
            public void selectedIndexChanged(final int old, final int current) {
                if (selectedJoint >= 0) { // reaguosim tik tada jei joint pasirinktas.
                    JointManager.JointInfo info = jointManager.getJointInfo(selectedJoint);
                    if (info.getJointType() >= 0) { // jeigu joint type parinktas, tai rodom ispejima.
                        ConfirmDialog e = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.YesNo);
                        e.setText("Changing joint types will reset all joint info. Process cannot be undone! Do you want to continue?");
                        e.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                            @Override
                            public void onYes() {
                                JointManager.JointInfo info = jointManager.getJointInfo(selectedJoint);
                                jointManager.changeJointType(info, current); // keiciam joint type ir pacia panele.
                                jointTypeSelected();
                                // atnaujinam joint piesima
                                updateJointDrawing(info);

                                Project.save();
                            }

                            @Override
                            public void onNo() {
                                jointTypes.setSelectedIndex(old); // ant no atstatom joint type i senaji.
                            }

                            @Override
                            public void onCancel() {}
                        });
                        e.show();
                    } else { // jeigu joint type nebuvo pasirinktas tai iskart parenkam ir viskas.
                        jointManager.changeJointType(info, current);
                        jointTypeSelected();
                        // atnaujinam joint piesima
                        updateJointDrawing(info);

                        Project.save();
                    }
                }
            }
        });
        jointList.append("Distance joint", "Friction joint", "Gear joint", "Motor joint", "Mouse joint",
                "Prismatic joint", "Pulley joint", "Revolute joint", "Rope joint", "Weld joint",
                "Wheel joint");
        jointInfoPanel.addControl(jointList);
        jointTypes = jointList;

        // bodies label
        lst.y = 304;
        lst.text = "Bodies:";

        jointInfoPanel.addControl(lst.createInterface());

        // a label
        lst.y = 255;
        lst.width = 35;
        lst.text = "A:";
        lst.horizontalAlign = Align.right;

        jointInfoPanel.addControl(lst.createInterface());

        // b label
        lst.y = 205;
        lst.text = "B:";

        jointInfoPanel.addControl(lst.createInterface());

        // a combo box.
        // situos combo box bus sarasai su resources, kurie turi fizikas ir chain kunai.
        cst.x = 50;
        cst.y = 255;
        cst.width = 141;
        cst.defaultWord = "-select-";
        cst.textSize = 25;

        ComboBox bodyAList = new ComboBox(cst);
        bodyAList.setIndexChangeListener(new ComboBox.IndexChangeListener() {
            @Override
            public void selectedIndexChanged(int old, int current) {
                setBodyField(current, bodyA, true);
                // pasiekeite body, todel turi updatint ir pati draweri.
                drawer.removeShape(bodyAFigures);
                bodyAFigures.clear();
                JointManager.JointInfo info = jointManager.getJointInfo(selectedJoint);
                if (info != null) {
                    if (selectBody(bodyA, info.bodyA)) {
                        info.bodyA = null; // nunulinam jeigu tokio body neber.
                    } else { // body yra! reikia piest jo polygons arba chain.
                        findBody(info.bodyAIsResource, info.bodyA, true, info);
                    }
                }
                // atnaujinam joint piesima
                updateJointDrawing(info);

                Project.save();
            }
        });
        jointInfoPanel.addControl(bodyAList);
        bodyA = bodyAList;

        // b combo box
        cst.y = 205;

        ComboBox bodyBList = new ComboBox(cst);
        bodyBList.setIndexChangeListener(new ComboBox.IndexChangeListener() {
            @Override
            public void selectedIndexChanged(int old, int current) {
                setBodyField(current, bodyB, false);
                // surandam body
                JointManager.JointInfo info = jointManager.getJointInfo(selectedJoint);
                drawer.removeShape(bodyBFigures); // salinam piesima.
                bodyBFigures.clear();
                if (info != null) {
                    if (selectBody(bodyB, info.bodyB)) {
                        info.bodyB = null; // nerado, nulinam.
                    } else { // body yra!
                        findBody(info.bodyBIsResource, info.bodyB, false, info);
                    }
                }
                // atnaujinam joint piesima
                updateJointDrawing(info);

                Project.save();
            }
        });
        jointInfoPanel.addControl(bodyBList);
        bodyB = bodyBList;

        // a mygtukas.
        SymbolButton.SymbolButtonStyle yst = new SymbolButton.SymbolButtonStyle();
        yst.x = 203;
        yst.y = 255;
        yst.width = 43;
        yst.height = 40;
        yst.autoSize = false;
        yst.normalColor = 0x00ffffff;
//        yst.text = "T";
        yst.symbol = Resources.getDrawable("mainEditorSelectKey");

        bodyAButton = new SymbolButton(yst);
        bodyAButton.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                if (allBodyVisible) { // jeigu sis navarotas ijungtas, tai isjungiam
                    showAllBodies(false); // uzdarom visus matomus kunus, kurie neturi but matomi.
                    inputs.changeMode(-1); // pasakom inputams, kad viskas
                } else { // jeigu buvo neijungta, tai ijungiam ir keiciam mygtuko spalva
                    bodyAButton.setColors(bodyAButton.getPressedColor(), 0xffff2200, bodyAButton.getOverColor());
                    showAllBodies(true); // pradedam rodyt visus galimus kunus.
                    inputs.changeMode(0); // pasakom, kad renkames a kuna.
                }
            }
        });

        jointInfoPanel.addControl(bodyAButton);

        // b mygtukas
        yst.y = 205;

        bodyBButton = new SymbolButton(yst);
        bodyBButton.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                if (allBodyVisible) { // jeigu kunai jau yra pasirenkami, tai isjungiam si navarota.
                    showAllBodies(false); // isjungiam visus kunus.
                    inputs.changeMode(-1); // isjungiam selectiona inputose.
                } else { // kitu atveju ijungiam. ir keiciam spalva sio button, bus toks efektas, kad ijungta.
                    bodyBButton.setColors(bodyBButton.getPressedColor(), 0xffff2200, bodyBButton.getOverColor());
                    showAllBodies(true); // rodom visus egzistuojancius kunus
                    inputs.changeMode(1); // pasakom, kad cia b kunui renkam.
                }
            }
        });

        jointInfoPanel.addControl(bodyBButton);

        // isjungimo mygtukas.
        yst.x = 226;
        yst.y = 586;
        yst.width = 23;
        yst.height = 23;
        yst.symbol = Resources.getDrawable("defaultColorPickCross");

        SymbolButton closeButton = new SymbolButton(yst);
        closeButton.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                JointControlPanel.this.editForm.getMainPanel().openControlPanel(0, true);
            }
        });
        addControl(closeButton);

        // check box. collide connected
        CheckBox.CheckBoxStyle hst = new CheckBox.CheckBoxStyle();
        hst.autoSize = false;
        hst.checkedBox = Resources.getDrawable("defaultCheckBoxTicked");
        hst.box = Resources.getDrawable("defaultCheckBox");
        hst.x = 5;
        hst.y = 153;
        hst.width = 242;
        hst.height = 40;
        hst.text = "Collide connected";

        CheckBox collideConnected = new CheckBox(hst);
        collideConnected.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) { // keiciam statusa
                JointManager.JointInfo info = jointManager.getJointInfo(selectedJoint);
                if (info != null){
                    if (info.collideConnected != checked) {
                        info.collideConnected = checked;
                        Project.save();
                    }
                }
            }
        });
        jointInfoPanel.addControl(collideConnected);
        this.collideConnected = collideConnected;

        panelPieceFactory = new PanelPieceFactory();

        drawer = new JointControlDrawer(this);
//        visibleBodies = new Array<>();
        bodyAFigures = new Array<>(10);
        bodyBFigures = new Array<>(10);
        otherVisuals = new Array<>(10);
        jointFigures = new Array<>(10); // daugiau 2 kazin ar bus.

        inputs = new JointControlInputs(editForm, this);

        pointPanel = new JointControlPointPanel(this, editForm);
        pointPanel.setVisible(false);
        editMainPanel.addControl(pointPanel); // desis i sia pagrindine panele

        inputs.setPointPanel(pointPanel); // dar idedam sita i controlleri.
        // bet valdymas bus per cia.
    }

    /* listeners. */

    boolean isBodySelected(boolean a){
        JointManager.JointInfo info = jointManager.getJointInfo(selectedJoint);
        if (a){
            return info != null && info.bodyA != null && bodyA.getSelectedIndex() != -1;
        }else
            return info != null && info.bodyB != null && bodyB.getSelectedIndex() != -1;
    }

    /** body point position. */
    Vector2 getBodyPosition(boolean a){
        return a ? bodyAPosition : bodyBPosition;
    }

    float getBodyAngle(boolean a){
        return a ? bodyAAngle : bodyBAngle;
    }

    /** Inputs controller. */
    JointControlInputs getInputsController(){
        return inputs;
    }

    /** @return Joint manager */
    JointManager getJointManager(){
        return jointManager;
    }

    EditForm getEditForm(){
        return editForm;
    }

    /** This method different then {@link #setVisible(boolean)} as it will not affect any drawings or other joint
     * attributes. This method is used by {@link JointControlPointPanel}.*/
    void showPanel(boolean show){
        super.setVisible(show);
    }

    void bodySelected(boolean a, String id){
        ComboBox body = a ? bodyA : bodyB; // kazkuri body pagal situacija
        Array<FiguresDrawer> figures = a ? bodyAFigures : bodyBFigures; // kazkuris body piesimas pagal situacija.
        int index = 0; // index, nustatyt kuris butent body bus.
        boolean foundName = false; // klaidos atveju
        for (String name : body.getItems()){ // surandam ta indexa, kurio mum reik
            if (name.equals(id)){
                foundName = true; // radom, cia tas daiktas.
                break;
            }
            index++;
        }
        if (!foundName){ // jeigu nerado tai kazkas negerai.
            AlertToast toast = new AlertToast("Unknown error occurred!");
            toast.show(Toast.SHORT);
            return;
        }
        setBodyField(index, body, a); // nustatom sita body musu joint info sarase.
        drawer.removeShape(figures); // salinam piesima.
        figures.clear(); // salinam senas figuras.
        JointManager.JointInfo info = jointManager.getJointInfo(selectedJoint); // susirandam jointa.
        if (info != null){ // toliau nustatom taip, kaip viskas turetu but.
            if (selectBody(body, a ? info.bodyA : info.bodyB)){
                if (a)
                    info.bodyA = null;
                else
                    info.bodyB = null;
            }else {
                if (a)
                    findBody(info.bodyAIsResource, info.bodyA, true, info);
                else
                    findBody(info.bodyBIsResource, info.bodyB, false, info);
            }
        }
        // toliau isjungiam all body selectiona.
        showAllBodies(false);

        // atnaujinam joint piesima.
        updateJointDrawing(info);
    }

    private void createNewJoint(){
        final InputTextDialog e = new InputTextDialog("Create new joint. Enter joint id:");
        e.setInputDialogListener(new InputTextDialog.InputDialogListener() {
            @Override
            public void onInput(String input) {
//                System.out.println("Bam. Joint: " + input);
                // patikrinam ar toks id jau yra. Ir ar nepertrumpas
                String error = null;
                if (input == null || input.length() == 0){
                    error = "Id cannot be zero length!";
                }else {
                    // tikrinam ar toks yra, kadangi back ende netikrina...
                    for (int a = 0; a < jointManager.getJointSize(); a++){
                        JointManager.JointInfo info = jointManager.getJointInfo(a);
                        if (info.getJointID().equals(input)){
                            error = "Joint id already exists with name: " + input + "!";
                            break;
                        }
                    }
                }
                if (error == null){
                    // viskas ok. kuriam joint.
                    jointManager.createJoint(input); // kuriam joint
                    selectedJoint = jointManager.getJointSize()-1; // paskutini jointa. nes ten bus naujasis
                    updateJointList(); // atnaujinam joint sarasa.
                    jointList.setSelectedIndex(selectedJoint); // pazymim nauja joint
                    jointTypeSelected(); // viska parinks ko reik cia.

                    Project.save();
                }else {
                    // kazkas ne to. Pranesam ir neuzdarom dialogo.
                    AlertToast toast = new AlertToast();
                    toast.setText(error);
                    toast.show(Toast.SHORT);
                    e.show();
                }
            }

            @Override
            public void cancel() {}
        });
        e.show();
    }

    /* funkcionalumas */

    private void updateJointDrawing(JointManager.JointInfo info){
        if (jointFigures.size > 0){
            drawer.removeShape(jointFigures);
            jointFigures.clear();
        }
        drawer.addJoint(info, jointFigures);
    }

    private void updateJointList(){
        // sitoj vietoj sudedam joint informacija
        jointList.clear();
        for (int a = 0; a < jointManager.getJointSize(); a++) {
            JointManager.JointInfo e = jointManager.getJointInfo(a);
            jointList.append(e.getJointID());
        }
    }

    // cia bus builderis, kuris subuildins paneles.
    private void jointTypeSelected(){
        drawer.clear(); // isvalom senas figuras.
//        visibleBodies.clear(); // sitas tada irgi tuscias paliks.
        bodyAFigures.clear();
        bodyBFigures.clear();
        otherVisuals.clear();
        jointFigures.clear();
        final JointManager.JointInfo info = jointManager.getJointInfo(selectedJoint);
        int index;
        if (info == null){
            settingsView.setVisible(false);
            return;
        }else {
            index = info.getJointType();
            collideConnected.setChecked(info.collideConnected);
            settingsView.setVisible(true);
        }
        jointTypes.setSelectedIndex(index); // kad nusistatytu parinkta typa.

        if (selectBody(bodyA, info.bodyA)){
            info.bodyA = null; // nunulinam jeigu tokio body neber.
        }else { // body yra! reikia piest jo polygons arba chain.
            findBody(info.bodyAIsResource, info.bodyA, true, info);
        }
        if (selectBody(bodyB, info.bodyB)){
            info.bodyB = null; // nerado, nulinam.
        }else { // body yra!
            findBody(info.bodyBIsResource, info.bodyB, false, info);
        }

        // normalus dydis yra 6 blokai. Vienas blokas uzima 50. 300min.
        panelPieceFactory.clearPanel();
        switch (index){
            case 0: { // distance joint panel.
                // 300 min. plius 6 blokai kur bloko dydis yra 50. plius 20 offset.
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 6*50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // dedam blokus.
                panelPieceFactory.addLabel("Local Anchors:");
                panelPieceFactory.addCoordChanger(true, "A:", info.anchorA.x+"",
                        info.anchorA.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorA);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 1);
                                }
                                pointPanel.show(info.bodyA, info.bodyAIsResource, info.anchorA.x, info.anchorA.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorA.x != x || info.anchorA.y != y) {
                                            info.anchorA.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addCoordChanger(true, "B:", info.anchorB.x+"",
                        info.anchorB.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorB);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 2);
                                }
                                pointPanel.show(info.bodyB, info.bodyBIsResource, info.anchorB.x, info.anchorB.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorB.x != x || info.anchorB.y != y) {
                                            info.anchorB.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x +"");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addParameterChanger("Length:", info.length, new ParameterBlock(){
                    // length parameter keitimas
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.length = checkParameterField(value, info.length);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });

                panelPieceFactory.addParameterChanger("FrequencyHz:", info.frequencyHz, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.frequencyHz = checkParameterField(value, info.frequencyHz);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Damping ratio:", info.dampingRatio, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.dampingRatio = checkParameterField(value, info.dampingRatio);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                break;
            }
            case 1:{ // friction joint
                // 300 min, plius 5 blokai. 20 offset
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 5 * 50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // blokai
                panelPieceFactory.addLabel("Local Anchors:");
                panelPieceFactory.addCoordChanger(true, "A:", info.anchorA.x+"",
                        info.anchorA.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorA);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 1);
                                }
                                pointPanel.show(info.bodyA, info.bodyAIsResource, info.anchorA.x, info.anchorA.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorA.x != x || info.anchorA.y != y) {
                                            info.anchorA.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addCoordChanger(true, "B:", info.anchorB.x+"",
                        info.anchorB.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorB);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 2);
                                }
                                pointPanel.show(info.bodyB, info.bodyBIsResource, info.anchorB.x, info.anchorB.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorB.x != x || info.anchorB.y != y) {
                                            info.anchorB.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addParameterChanger("Max Force:", info.maxForce, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxForce = checkParameterField(value, info.maxForce);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Max Torque:", info.maxTorque, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxTorque = checkParameterField(value, info.maxTorque);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                break;
            }
            case 2:{ // gear joint
                // 300 min, plius 4 blokai. 20 offset
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 4 * 50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // blokai
                panelPieceFactory.addLabel("Joints:");
                // sitas uzima 2 blokus.
                ParameterBlock comboJoinst = new ParameterBlock(){
                    @Override
                    void jointSelected(String jointId1, String jointId2) { // parenkam jointus.
                        if (!info.joint1ID.equals(jointId1) || !info.joint2ID.equals(jointId2)) {
                            info.joint1ID = jointId1;
                            info.joint2ID = jointId2;

                            Project.save();
                        }
                        updateJointDrawing(info);
                    }
                };
                panelPieceFactory.addGearJointSpecial(comboJoinst);
                // paziurim ar turim tokius joint, kokie buvo issaugoti pries
                boolean foundJoint1 = info.joint1ID == null, foundJoint2 = info.joint2ID == null; // Jeigu neparinkti joint, tai zymim, kad ale rasti jau.
                if (!foundJoint1 || !foundJoint2) { // jeigu abu neparinkti, tai nereik ir sito viso...
                    for (int a = 0; a < jointManager.getJointSize(); a++) {
                        JointManager.JointInfo e = jointManager.getJointInfo(a);
                        if (e != info && e.getJointType() == 5 || e.getJointType() == 7) { // pacio saves neims. ir joint tik revolute ir prismatic.
                            // pirmojo listo tikrinimas
                            if (!foundJoint1) {
                                if (info.joint1ID.equals(e.getJointID())) {
                                    // Radom musu joint.
                                    foundJoint1 = true;
                                    // dabar turim surast sio joint indexa liste....
                                    for (int k = 0; k < comboJoinst.jointList1.getItems().size(); k++) {
                                        if (comboJoinst.jointList1.getItem(k).equals(e.getJointID())) { // surandam tokiu pat tekstu esanti item ir ji parenkam.
                                            comboJoinst.jointList1.setSelectedIndex(k);
                                            break;
                                        }
                                    }
                                    continue;
                                }
                            }
                            // antrojo listo tikrinimas
                            if (!foundJoint2) {
                                if (info.joint2ID.equals(e.getJointID())) {
                                    foundJoint2 = true;
                                    for (int k = 0; k < comboJoinst.jointList2.getItems().size(); k++) {
                                        if (comboJoinst.jointList2.getItem(k).equals(e.getJointID())) { // surandam tokiu pat tekstu esanti item ir ji parenkam.
                                            comboJoinst.jointList2.setSelectedIndex(k);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!foundJoint1) { // pirmasis joint nerastas, kur dingo?
                        ConfirmDialog e = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.OK);
                        e.show("Joint was not found with id: " + info.joint1ID);
                    }
                    if (!foundJoint2) { // antrais joint nerastas, kur dingo?
                        ConfirmDialog e = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.OK);
                        e.show("Joint was not found with id: " + info.joint2ID);
                    }
                }

                panelPieceFactory.addParameterChanger("Ratio:", info.ratio, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.ratio = checkParameterField(value, info.ratio);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                break;
            }
            case 3:{ // motor joint
                // 300 min, plius 7 blokai. 20 offset
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 7 * 50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // blokai
                panelPieceFactory.addLabel("Linear offset:");
                panelPieceFactory.addCoordChanger(false, null, info.anchorA.x + "",
                        info.anchorA.y + "", false, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorA);
                            }
                        });
                panelPieceFactory.addLabel("Angular offset:");
                panelPieceFactory.addAngularChanger(MoreUtils.roundFloat(info.length * MathUtils.radiansToDegrees,2) + "",
                        new ParameterBlock(){
                            @Override
                            float getStartingAngle() {
                                return info.length;
                            }

                            @Override
                            void angleSelected(float angle, float x, float y, Label angleInfo) {
                                // visada apvalinam po dvieju po kablelio
                                float nangle = MoreUtils.roundFloat(angle * MathUtils.radiansToDegrees, 2);
                                angleInfo.setText(nangle+"");
                                if (info.length != angle) {
                                    info.length = angle;

                                    Project.save();
                                }
                            }
                        });
                panelPieceFactory.addParameterChanger("Max force:", info.maxForce, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxForce = checkParameterField(value, info.maxForce);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Max torque:", info.maxTorque, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxTorque = checkParameterField(value, info.maxTorque);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Correction factor:", info.ratio, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.ratio = checkParameterField(value, info.ratio);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                break;
            }
            case 4:{ // mouse joint
                // 300 min, plius 5 blokai. 20 offset
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 5 * 50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // blokai
                panelPieceFactory.addLabel("Target:");
                panelPieceFactory.addCoordChanger(false, null, info.anchorA.x + "",
                        info.anchorA.y + "", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorA);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 0);
                                }
                                pointPanel.show(0, 0, info.anchorA.x, info.anchorA.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorA.x != x || info.anchorA.y != y) {
                                            info.anchorA.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addParameterChanger("Max force:", info.maxForce, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxForce = checkParameterField(value, info.maxForce);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("FrequencyHz:" , info.frequencyHz, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.frequencyHz = checkParameterField(value, info.frequencyHz);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Damping ratio:", info.dampingRatio, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.dampingRatio = checkParameterField(value, info.dampingRatio);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                break;
            }
            case 5:{ // prismatic joint
                // 300 min, plius 13 bloku. 20 offset
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 13 * 50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // blokai
                panelPieceFactory.addLabel("Local anchors:");
                panelPieceFactory.addCoordChanger(true, "A:", info.anchorA.x+"",
                        info.anchorA.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorA);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 1);
                                }
                                pointPanel.show(info.bodyA, info.bodyAIsResource, info.anchorA.x, info.anchorA.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorA.x != x || info.anchorA.y != y) {
                                            info.anchorA.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addCoordChanger(true, "B:", info.anchorB.x+"",
                        info.anchorB.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorB);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 2);
                                }
                                pointPanel.show(info.bodyB, info.bodyBIsResource, info.anchorB.x, info.anchorB.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorB.x != x || info.anchorB.y != y) {
                                            info.anchorB.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addLabel("Local axis A:");
                panelPieceFactory.addAngularChanger(info.localAxisA.x + "-" + info.localAxisA.y, new ParameterBlock(){
                    @Override
                    float getStartingAngle() {
                        return (float) Math.atan2(info.localAxisA.y, info.localAxisA.x);
                    }

                    @Override
                    void angleSelected(float angle, float x, float y, Label angleInfo) {
                        // apvalinam du skaicius po kablelio
                        float nx = MoreUtils.roundFloat(x, 2), ny = MoreUtils.roundFloat(y, 2);
                        angleInfo.setText(nx + " - " + ny);
                        if (info.localAxisA.x != nx || info.localAxisA.y != ny) {
                            info.localAxisA.set(nx, ny);

                            Project.save();
                        }
                    }
                });
                panelPieceFactory.addLabel("Reference angle:");
                panelPieceFactory.addAngularChanger(MoreUtils.roundFloat(info.length * MathUtils.radiansToDegrees, 2) + "", new ParameterBlock(){
                    @Override
                    float getStartingAngle() {
                        return info.length;
                    }

                    @Override
                    void angleSelected(float angle, float x, float y, Label angleInfo) {
                        // visada apvalinam po dvieju po kablelio
                        float nangle = MoreUtils.roundFloat(angle * MathUtils.radiansToDegrees, 2);
                        angleInfo.setText(nangle+"");
                        if (info.length != angle) {
                            info.length = angle;

                            Project.save();
                        }
                    }
                });
                // parameter su depent values.
                ParameterBlock limit = new ParameterBlock(){
                    @Override
                    void checkBoxChecked(boolean checked) {
                        if (info.enableLimit != checked) {
                            info.enableLimit = checked;

                            Project.save();
                        }
                    }
                };
                panelPieceFactory.addCheckBox("Enable limit", info.enableLimit, limit);
                limit.dependentBlockIds.add(panelPieceFactory.blocks.size, panelPieceFactory.blocks.size+1);
                panelPieceFactory.addParameterChanger("Lower translation:", info.frequencyHz, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.frequencyHz = checkParameterField(value, info.frequencyHz);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Upper translation:", info.dampingRatio, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.dampingRatio = checkParameterField(value, info.dampingRatio);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.checkBlockDependentList(limit);
                // vel dependent dedam del motor values.
                ParameterBlock motor = new ParameterBlock(){
                    @Override
                    void checkBoxChecked(boolean checked) {
                        if (info.enableMotor != checked) {
                            info.enableMotor = checked;

                            Project.save();
                        }
                    }
                };
                panelPieceFactory.addCheckBox("Enable motor", info.enableMotor, motor);
                motor.dependentBlockIds.add(panelPieceFactory.blocks.size, panelPieceFactory.blocks.size+1);
                panelPieceFactory.addParameterChanger("Max motor force:", info.maxForce, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxForce = checkParameterField(value, info.maxForce);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Motor speed:", info.maxTorque, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxTorque = checkParameterField(value, info.maxTorque);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.checkBlockDependentList(motor);
                break;
            }
            case 6:{ // pulley joint
                // 300 min, plius 9 blokai. 20 offset
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 9 * 50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // blokai
                panelPieceFactory.addLabel("Ground Anchors:");
                panelPieceFactory.addCoordChanger(true, "A:", info.localAxisA.x + "",
                        info.localAxisA.y + "", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.localAxisA);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 2);
                                }
                                pointPanel.show(0, 0, info.localAxisA.x, info.localAxisA.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.localAxisA.x != x || info.localAxisA.y != y) {
                                            info.localAxisA.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addCoordChanger(true, "B:", info.groundAnchorB.x+"",
                        info.groundAnchorB.y + "", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.groundAnchorB);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 3);
                                }
                                pointPanel.show(0, 0, info.groundAnchorB.x, info.groundAnchorB.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.groundAnchorB.x != x || info.groundAnchorB.y != y) {
                                            info.groundAnchorB.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addLabel("Local Anchors:");
                panelPieceFactory.addCoordChanger(true, "A:", info.anchorA.x+"",
                        info.anchorA.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorA);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 1);
                                }
                                pointPanel.show(info.bodyA, info.bodyAIsResource, info.anchorA.x, info.anchorA.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorA.x != x || info.anchorA.y != y) {
                                            info.anchorA.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addCoordChanger(true, "B:", info.anchorB.x+"",
                        info.anchorB.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorB);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 4);
                                }
                                pointPanel.show(info.bodyB, info.bodyBIsResource, info.anchorB.x, info.anchorB.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorB.x != x || info.anchorB.y != y) {
                                            info.anchorB.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addParameterChanger("Length A:", info.maxForce, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxForce = checkParameterField(value, info.maxForce);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Length B:", info.maxTorque, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxTorque = checkParameterField(value, info.maxTorque);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Ratio:", info.ratio, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.ratio = checkParameterField(value, info.ratio);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                break;
            }
            case 7:{ // revolute joint
                // 300 min, plius 13 bloku. 20 offset
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 13 * 50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // blokai
                panelPieceFactory.addLabel("Local Anchors:");
                panelPieceFactory.addCoordChanger(true, "A:", info.anchorA.x+"",
                        info.anchorA.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorA);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 1);
                                }
                                pointPanel.show(info.bodyA, info.bodyAIsResource, info.anchorA.x, info.anchorA.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorA.x != x || info.anchorA.y != y) {
                                            info.anchorA.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addCoordChanger(true, "B:", info.anchorB.x+"",
                        info.anchorB.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorB);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 2);
                                }
                                pointPanel.show(info.bodyB, info.bodyBIsResource, info.anchorB.x, info.anchorB.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorB.x != x || info.anchorB.y != y) {
                                            info.anchorB.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addLabel("Reference angle");
                panelPieceFactory.addAngularChanger(MoreUtils.roundFloat(info.length * MathUtils.radiansToDegrees,2)
                        + "", new ParameterBlock(){
                    @Override
                    float getStartingAngle() {
                        return info.length;
                    }

                    @Override
                    void angleSelected(float angle, float x, float y, Label angleInfo) {
                        // visada apvalinam po dvieju po kablelio
                        float nangle = MoreUtils.roundFloat(angle * MathUtils.radiansToDegrees, 2);
                        angleInfo.setText(nangle+"");
                        if (info.length != angle) {
                            info.length = angle;

                            Project.save();
                        }
                    }
                });
                // parameter su depent values.
                ParameterBlock limit = new ParameterBlock(){
                    @Override
                    void checkBoxChecked(boolean checked) {
                        if (info.enableLimit != checked) {
                            info.enableLimit = checked;

                            Project.save();
                        }
                    }
                };
                panelPieceFactory.addCheckBox("Enable limit", info.enableLimit, limit);
                limit.dependentBlockIds.add(panelPieceFactory.blocks.size, panelPieceFactory.blocks.size+1);
//                panelPieceFactory.addParameterChanger("Lower angle:", 0, new ParameterBlock());
//                panelPieceFactory.addParameterChanger("Upper angle:", 0, new ParameterBlock());
                ParameterBlock lowerAngle = new ParameterBlock(){
                    @Override
                    float getStartingAngle() {
                        return info.frequencyHz;
                    }

                    @Override
                    void angleSelected(float angle, float x, float y, Label angleInfo) {
                        // visada apvalinam po dvieju po kablelio
                        float nangle = MoreUtils.roundFloat(angle * MathUtils.radiansToDegrees, 2);
                        angleInfo.setText(nangle+"");
                        if (info.frequencyHz != angle) {
                            info.frequencyHz = angle;

                            Project.save();
                        }
                    }
                };
                lowerAngle.sideLabel = panelPieceFactory.addLabel("Lower angle:");
                panelPieceFactory.addAngularChanger(MoreUtils.roundFloat(info.frequencyHz * MathUtils.radiansToDegrees,2)
                        + "", lowerAngle);
                ParameterBlock upperAngle = new ParameterBlock(){
                    @Override
                    float getStartingAngle() {
                        return info.dampingRatio;
                    }

                    @Override
                    void angleSelected(float angle, float x, float y, Label angleInfo) {
                        // visada apvalinam po dvieju po kablelio
                        float nangle = MoreUtils.roundFloat(angle * MathUtils.radiansToDegrees, 2);
                        angleInfo.setText(nangle+"");
                        if (info.dampingRatio != angle) {
                            info.dampingRatio = angle;

                            Project.save();
                        }
                    }
                };
                upperAngle.sideLabel = panelPieceFactory.addLabel("Upper angle:");
                panelPieceFactory.addAngularChanger(MoreUtils.roundFloat(info.dampingRatio * MathUtils.radiansToDegrees,2)
                        + "", upperAngle);
                panelPieceFactory.checkBlockDependentList(limit);
                // vel dependent dedam del motor values.
                ParameterBlock motor = new ParameterBlock(){
                    @Override
                    void checkBoxChecked(boolean checked) {
                        if (info.enableMotor != checked) {
                            info.enableMotor = checked;

                            Project.save();
                        }
                    }
                };
                panelPieceFactory.addCheckBox("Enable motor", info.enableMotor, motor);
                motor.dependentBlockIds.add(panelPieceFactory.blocks.size, panelPieceFactory.blocks.size+1);
                panelPieceFactory.addParameterChanger("Motor speed:", info.maxForce, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxForce = checkParameterField(value, info.maxForce);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Max motor torque:", info.maxTorque, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxTorque = checkParameterField(value, info.maxTorque);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.checkBlockDependentList(motor);
                break;
            }
            case 8:{ // rope joint
                // 300 min, plius 4 blokai. 20 offset
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 4 * 50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // blokai
                panelPieceFactory.addLabel("Local Anchors:");
                panelPieceFactory.addCoordChanger(true, "A:", info.anchorA.x+"",
                        info.anchorA.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorA);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 1);
                                }
                                pointPanel.show(info.bodyA, info.bodyAIsResource, info.anchorA.x, info.anchorA.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorA.x != x || info.anchorA.y != y) {
                                            info.anchorA.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addCoordChanger(true, "B:", info.anchorB.x+"",
                        info.anchorB.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorB);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 2);
                                }
                                pointPanel.show(info.bodyB, info.bodyBIsResource, info.anchorB.x, info.anchorB.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorB.x != x || info.anchorB.y != y) {
                                            info.anchorB.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addParameterChanger("Max length:", info.length, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.length = checkParameterField(value, info.length);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                break;
            }
            case 9:{ // weld joint
                // 300 min, plius 7 blokai. 20 offset
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 7 * 50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // blokai
                panelPieceFactory.addLabel("Local Anchors:");
                panelPieceFactory.addCoordChanger(true, "A:", info.anchorA.x+"",
                        info.anchorA.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorA);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 1);
                                }
                                pointPanel.show(info.bodyA, info.bodyAIsResource, info.anchorA.x, info.anchorA.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorA.x != x || info.anchorA.y != y) {
                                            info.anchorA.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addCoordChanger(true, "B:", info.anchorB.x+"",
                        info.anchorB.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorB);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 2);
                                }
                                pointPanel.show(info.bodyB, info.bodyBIsResource, info.anchorB.x, info.anchorB.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorB.x != x || info.anchorB.y != y) {
                                            info.anchorB.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addLabel("Reference angle:");
                panelPieceFactory.addAngularChanger(MoreUtils.roundFloat(info.length * MathUtils.radiansToDegrees,2)
                        + "", new ParameterBlock(){
                    @Override
                    float getStartingAngle() {
                        return info.length;
                    }

                    @Override
                    void angleSelected(float angle, float x, float y, Label angleInfo) {
                        // visada apvalinam po dvieju po kablelio
                        float nangle = MoreUtils.roundFloat(angle * MathUtils.radiansToDegrees, 2);
                        angleInfo.setText(nangle+"");
                        if (info.length != angle) {
                            info.length = angle;

                            Project.save();
                        }
                    }
                });
                panelPieceFactory.addParameterChanger("FrequencyHz:" , info.frequencyHz, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.frequencyHz = checkParameterField(value, info.frequencyHz);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Damping ratio:", info.dampingRatio, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.dampingRatio = checkParameterField(value, info.dampingRatio);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                break;
            }
            case 10:{// wheel joint
                // 300 min, plius 10 bloku. 20 offset
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 300 + 10 * 50 + 20);
                // is viso aukscio atimam pradini 300 ir papildoma bloka 50
                panelPieceFactory.setStartingY(settingsView.getVirtualHeight() - 350);
                // blokai
                panelPieceFactory.addLabel("Local anchors:");
                panelPieceFactory.addCoordChanger(true, "A:", info.anchorA.x+"",
                        info.anchorA.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorA);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 1);
                                }
                                pointPanel.show(info.bodyA, info.bodyAIsResource, info.anchorA.x, info.anchorA.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorA.x != x || info.anchorA.y != y) {
                                            info.anchorA.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addCoordChanger(true, "B:", info.anchorB.x+"",
                        info.anchorB.y+"", true, new ParameterBlock(){
                            @Override
                            void cordInputChanged(TextBox x, TextBox y) {
                                checkCordField(x, y, info.anchorB);
                                updateJointDrawing(info);
                            }

                            @Override
                            void cordButtonClick(ParameterBlock e) {
                                if (jointFigures.size > 0){
                                    pointPanel.setJointDrawer(jointFigures.get(0), 2);
                                }
                                pointPanel.show(info.bodyB, info.bodyBIsResource, info.anchorB.x, info.anchorB.y, new JointControlPointPanel.JointControlPointPanelListener() {
                                    @Override
                                    public void onCordChange(float x, float y) {
                                        if (info.anchorB.x != x || info.anchorB.y != y) {
                                            info.anchorB.set(x, y);

                                            Project.save();
                                        }
                                        box1.setText(x + "");
                                        box2.setText(y + "");
                                    }
                                });
                            }
                        });
                panelPieceFactory.addLabel("Local axis A:");
                panelPieceFactory.addAngularChanger(info.localAxisA.x + "-" + info.localAxisA.y, new ParameterBlock(){
                    @Override
                    float getStartingAngle() {
                        return (float) Math.atan2(info.localAxisA.y, info.localAxisA.x);
                    }

                    @Override
                    void angleSelected(float angle, float x, float y, Label angleInfo) {
                        // apvalinam du po kablelio. Kam ten reik daugiau skaiciu...
                        float nx = MoreUtils.roundFloat(x, 2), ny = MoreUtils.roundFloat(y, 2);
                        angleInfo.setText(nx + " - " + ny);
                        if (info.localAxisA.x != nx || info.localAxisA.y != ny) {
                            info.localAxisA.set(nx, ny);

                            Project.save();
                        }
                    }
                });
                // vel dependent dedam del motor values.
                ParameterBlock motor = new ParameterBlock(){
                    @Override
                    void checkBoxChecked(boolean checked) {
                        if (info.enableMotor != checked) {
                            info.enableMotor = checked;

                            Project.save();
                        }
                    }
                };
                panelPieceFactory.addCheckBox("Enable motor", info.enableMotor, motor);
                motor.dependentBlockIds.add(panelPieceFactory.blocks.size, panelPieceFactory.blocks.size+1);
                panelPieceFactory.addParameterChanger("Max motor torque:", info.maxTorque, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxTorque = checkParameterField(value, info.maxTorque);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Motor speed:", info.maxForce, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.maxForce = checkParameterField(value, info.maxForce);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.checkBlockDependentList(motor);
                panelPieceFactory.addParameterChanger("FrequencyHz:" , info.frequencyHz, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.frequencyHz = checkParameterField(value, info.frequencyHz);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                panelPieceFactory.addParameterChanger("Damping ratio:", info.dampingRatio, new ParameterBlock(){
                    @Override
                    void parameterChanged(TextBox value) {
                        try{
                            info.dampingRatio = checkParameterField(value, info.dampingRatio);
                        }catch (RuntimeException ignored){} // nieko nedarom jei klaida.
                    }
                });
                break;
            }
            default: // cia isvalys ir paliks tuscia lyg butu nepasirinkta panele.
                settingsView.setVirtualSize(settingsView.getVirtualWidth(), 447); // tiesiog padarom toki dydi ir  viskas.
                // dabar bus tuscia panele.
                break;
        }
        settingsView.setScrollOffset(0, settingsView.getVirtualHeight()); // visada nuo pradziu prades.
        updateJointDrawing(info);
    }

    /* basic */

    private void showAllBodies(boolean show){
        if (show != allBodyVisible){
            if (show){
                // cia viska sumesim i sarasa.
                // body yra polygons.
                InterfacesController controller = editForm.getController();
                for (Interface e : controller.getControls()){
                    if (e instanceof Element){ // prieisim tik prie resource, nes tik ten polygonai.
                        PhysicsHolder physicsHolder = ((Element) e).getPhysicsHolder();
                        if (physicsHolder.hasShapes()){ // turi butinai ir polygonai egzistuot.
                            drawer.addPhysicsPolygon(((Element) e), otherVisuals);
                        }
                    }
                }

                // chainai.
                ChainEdging edging = editForm.getChainEdging();
                for (ChainEdging.Chains e : edging.getChains()){
                    otherVisuals.add(drawer.addChain(e));
                }
            }else {
                // viska salinam.
                // prie to pacio ir button spalva keiciam i normalia.
                drawer.removeShape(otherVisuals);
                otherVisuals.clear();
                // nuimam mygtuku spalvas.
                bodyAButton.setColors(bodyAButton.getNormalColor(), 0x00ffffff, bodyAButton.getOverColor());
                bodyBButton.setColors(bodyBButton.getNormalColor(), 0x00ffffff, bodyBButton.getOverColor());
            }
            allBodyVisible = show;
        }
    }

    private void findBody(boolean bodyResource, String bodyId, boolean A, JointManager.JointInfo info){
        boolean foundBody = false;
        Array<FiguresDrawer> figuresDrawers = A ? bodyAFigures : bodyBFigures;
        Vector2 bodyPos = A ? bodyAPosition : bodyBPosition;
        if (bodyResource){
            // body yra polygons.
            InterfacesController controller = editForm.getController();
            for (Interface e : controller.getControls()){
                if (e instanceof Element){ // prieisim tik prie resource, nes tik ten polygonai.
                    Element owner = (Element) e;
                    PhysicsHolder physicsHolder = owner.getPhysicsHolder();
                    if (physicsHolder.hasShapes() && e.getIdName().equals(bodyId)){
                        foundBody = true;
                        drawer.addPhysicsPolygon(((Element) e), figuresDrawers);

                        // toliau randam body taska
                        // KODAS PAIMTAS IS JointControlPointPanel.
                        float startX, startY;
                        Vector2 pos = owner.getPosition();
                        if (physicsHolder.isBodyOriginMiddle){ // body pozicijos nutatymas.
                            startX = owner.getWidth() / 2; // nieko ypatingo, viskas vidury.
                            startY = owner.getHeight() / 2;
                        }else {
                            float rWidth = owner.getImage().getMinWidth();
                            float rHeight = owner.getImage().getMinHeight();
                            float wRatio = owner.getWidth() / rWidth;
                            float hRatio = owner.getHeight() / rHeight;
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

                        // nustatom kuno taska.
                        bodyPos.set(pos.x + startX, pos.y + startY);
                        if (A){
                            bodyAAngle = owner.getAngle() * MathUtils.degreesToRadians;
                        }else {
                            bodyBAngle = owner.getAngle() * MathUtils.degreesToRadians;
                        }

                        // idedam tasko figura
                        FiguresDrawer figure = drawer.getEmptyFigureDrawer();
                        figure.addPoint(bodyPos.x, bodyPos.y);
                        figure.pointWeight = 10;
                        figure.pointColor = 0xffff0000;
                        figure.setPointsDrawing(true);

                        drawer.addCustomShape(figure);
                        figuresDrawers.add(figure);
                        break;
                    }
                }
            }
        }else { // body yra chain
            ChainEdging edging = editForm.getChainEdging();
            for (ChainEdging.Chains e : edging.getChains()){
                if (e.name.equals(bodyId)){
                    foundBody = true;
                    figuresDrawers.add(drawer.addChain(e));
                    // cia body tasko pridejimas
                    if (e.x.size > 0) {
                        bodyPos.set(e.x.get(0), e.y.get(0));
                        FiguresDrawer figure = drawer.getEmptyFigureDrawer();
                        figure.addPoint(bodyPos.x, bodyPos.y);
                        figure.setPointsDrawing(true);
                        figure.pointColor = 0xffff0000;
                        figure.pointWeight = 10;

                        drawer.addCustomShape(figure);
                        figuresDrawers.add(figure);
                    }else {
                        bodyPos.set(0,0);
                    }
                    if (A){
                        bodyAAngle = 0;
                    }else {
                        bodyBAngle = 0;
                    }
                    break;
                }
            }
        }
        if (!foundBody){
            // vis del to nerado body.
            if (A){
                info.bodyA = null;
                bodyA.setSelectedIndex(-1);
            }else {
                info.bodyB = null;
                bodyB.setSelectedIndex(-1);
            }
        }
    }

    // kuno pasirinkimui, Kadangi du kunai, tai patogiau viena kart vykdyt toki koda.
    // return true kai body nerastas ir turetu but pasalintas is body saraso.
    private boolean selectBody(ComboBox box, String bodyId){
        // egzistuojanciu body parinkimas.
        if (bodyId != null){
            int count = 0;
            for (String name : box.getItems()){
                if (name.equals(bodyId)){
                    box.setSelectedIndex(count);
                    return false; // radom, nieko nebereik daryt.
                }
                count++;
            }

            // jeigu ateis iki cia, vadinas tokio kuno nerado. Pranesam
            box.setSelectedIndex(-1); // butinai padarom nepazymeta.
            AlertToast toast = new AlertToast("Body was not found with id: " + bodyId);
            toast.show(Toast.SHORT);
            return true;
        }else {
            box.setSelectedIndex(-1); // nepasirinktas kunas
            return false;
        }
    }

    // abiem combo boxam panasiai pasirenkam, tai puikiai tinka sis metodas.
    private void setBodyField(int selectedIndex, ComboBox box, boolean a){
        String id = box.getItem(selectedIndex);
        if (id != null && id.length() > 0){ // negali but null.
            JointManager.JointInfo info = jointManager.getJointInfo(selectedJoint);
            if (info != null){ // joint info butinai turi but legalus.
                // surandam kunas resource ar chain.
                // kadangi chain kaip ir maziau tai tikrinam chain, jei ten nera tai resource.
                ChainEdging edging = editForm.getChainEdging();
                boolean isResource = true;
                for (ChainEdging.Chains e : edging.getChains()){
                    if (e.name.equals(id)){
                        isResource = false;
                        break;
                    }
                }
                if (a) {
                    if (info.bodyAIsResource != isResource || !info.bodyA.equals(id)) {
                        info.bodyA = id;
                        info.bodyAIsResource = isResource;

                        Project.save();
                    }
                }else {
                    if (info.bodyBIsResource != isResource || !info.bodyB.equals(id)) {
                        info.bodyB = id;
                        info.bodyBIsResource = isResource;

                        Project.save();
                    }
                }
            }
        }
    }

    // x ir y laukai
    // container tai cia kur laiko cord terp joint info.
    private void checkCordField(TextBox x, TextBox y, Vector2 container){
        try {
            // sudedam value i musu containeri.
            float valueX = Float.parseFloat(x.getText());
            float valueY = Float.parseFloat(y.getText());

            if (container.x != valueX || container.y != valueY) {
                container.set(valueX, valueY);

                Project.save();
            }
        }catch (NumberFormatException ex){
            ex.printStackTrace();
            // ivykus error pranesam ir grazinam default reiksmes.
            AlertToast toast = new AlertToast();
            toast.setText("Value must be a number!");
            toast.show(Toast.SHORT);
            x.setText(container.x+"");
            y.setText(container.y + "");
        }
    }

    private float checkParameterField(TextBox box, float defValue) throws RuntimeException{
        String text = box.getText();
        try {
            float value = Float.parseFloat(text);

            if (value != defValue){
                Project.save();
            }
            return value;
        }catch (NumberFormatException ex){
            ex.printStackTrace();
            // metam error, nes ne skaiciu ivede.
            AlertToast toast = new AlertToast();
            toast.setText("Value must be a number!");
            toast.show(Toast.SHORT);
            box.setText(defValue+""); // atstatom i senaji teksta.
            throw new RuntimeException("Not a number");
        }
    }

    /* overide metodai */

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
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE){
            if (allBodyVisible) {
                showAllBodies(false); // uzdarom bodies.
                return true;
            }
        }
        return super.keyDown(keycode);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (jointManager != null) {
            pointPanel.setVisible(false); // bet kokiu atveju uzdarom.
            if (visible) {
                // egzistuojanciu body sumetimas i body sarasa.
                // teoriskai sarasas nesikeis kol is panele aktyvi, todel cia galim sumest body sarasa.
                SelectiveInterfaceController controller = (SelectiveInterfaceController) editForm.getController();
                bodyA.clear();
                bodyB.clear();
                for (Interface e : controller.getControls()){
                    if (e instanceof Selector || e instanceof Resizer ||
                            e instanceof FieldMover || controller.getExceptionList().contains(e)){
                        continue;
                    }
                    // interface tinkamas, bet dar reik paziuret ar turi fizikos taskus.
                    // taip pat negali but su fixed view, nu nes fizikos nesikurs ant fixed.
                    if (e instanceof Element && e.getPositioning() != Window.fixedView){
                        PhysicsHolder physicsHolder = ((Element) e).getPhysicsHolder();
                        if (physicsHolder.hasShapes()){ // turi fizikos taskus.
                            bodyA.append(e.getIdName());
                            bodyB.append(e.getIdName());
                        }
                    }
                }
                // dar reik chainus sudet. Tiesiog sudet, nes jie visi tinka
                ChainEdging chains = editForm.getChainEdging();
                for (ChainEdging.Chains e : chains.getChains()){
                    bodyA.append(e.name);
                    bodyB.append(e.name);
                }

                // atnaujinam joint sarasa.
                updateJointList(); // atnaujinam
                // pazymim ta kuris buvo. paziurim ar galim tai padaryt.
                if (selectedJoint >= 0 && selectedJoint < jointList.getSize()){
                    jointList.setSelectedIndex(selectedJoint);
                }

                jointTypeSelected(); // atnaujinam jei kas pasikeite.

                TopPainter.addPaintOnTop(drawer, false); // visos sitos linijos tik ant absolutaus.!
                editForm.setSelectiveListener(inputs); // sudedam sios paneles listeneri.
                editForm.getSelector().setEnabled(false); // su situo mums isjungia panele paspaudus bet kur ne ant kontroles, todel turim disablint
            }else {
                // isjungiam sita, jei buvo ijungtas.
                showAllBodies(false);
                inputs.release();
                TopPainter.removeTopPaint(drawer);
                editForm.setSelectiveListener(null); // isimam listeneri.
                editForm.getSelector().setEnabled(true); // igalinam vel, kad viskas normaliai veiktu.
            }
        }
    }

    /* uz veikima atsakingos klases */

    // sita klase atsakinga uz panelio daliu isdestyma. Dalins dalis pagal reikalavima. Jas salins pati kai ju nebereiks.
    private class PanelPieceFactory{
        /*
        * Reikalingu daiktu sarasas:
        * Paprastas label. Kiekis: 3
        * koordinaciu keitimas. label. du text boxai. mygtukas (symbol button). Redaguojamos dalys. Kiekis: 4
        * paprastu par keitimas. label ir tekstboxas. Kiekis: 4
        * jointu pasirinkimas. special gear joint. label ir combo box. Kiekis: 2.
        * kampu pasirikimas. label ir mygtukas (symbol button? button su tekstu?). Kiekis: 2.
        * paprastas check box. Kiekis: 2
        *
        * Paprastu daliu kiekis
        * Label: 11
        * text box: 11
        * combo box: 2
        * button (symbol button?): 5
        * check box: 2
        */

        /* pacios dalys. */
        private Label[] labels;
        private TextBox[] textBoxes;
        private ComboBox[] comboBoxes;
        private SymbolButton[] symbolButtons;
        private CheckBox[] checkBoxes;

        // nustatyt, kurios dalys naudojamos.
        private int usedLabels, usedTextBoxes, usedComboBoxes, usedSymbolButtons, usedCheckBoxes;

        // reikalingi parametrai
        private float startingY;
        private Array<ParameterBlock> blocks = new Array<>();

        // kampo pasirinkimui.
        private AngularSelectionDialog angularSelectionDialog;

        public PanelPieceFactory(){
            // labeliu kurimas
            Label.LabelStyle lst = new Label.LabelStyle();
            lst.autoSize = false;
            lst.verticalAlign = Align.center;
            lst.textSize = 35;

            labels = new Label[11];
            for (int a = 0; a < labels.length; a++){
                labels[a] = new Label(lst);
            }

            // text box kurimas
            TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
            tst.autoSize = false;
            tst.background = Resources.getDrawable("whiteSystemColor");
            tst.width = 97;
            tst.height = 40;
            tst.textSize = 35;
            tst.verticalAlign = Align.center;
            tst.horizontalAlign = Align.center;

            textBoxes = new TextBox[11];
            for (int a = 0; a < textBoxes.length; a++){
                textBoxes[a] = new TextBox(tst);
            }

            // combo box kurimas
            ComboBox.ComboBoxStyle cst = new ComboBox.ComboBoxStyle();
            cst.autoSize = false;
            cst.listBackground = tst.background;
            cst.background = Resources.getDrawable("systemWhiteRect");
            cst.width = 195;
            cst.height = 40;
            cst.textSize = 35;
            cst.upside = true;
            cst.verticalAlign = Align.center;
            cst.horizontalAlign = Align.center;
            cst.onColor = 0xffff0000;
            cst.pressedColor = 0xff550000;

            comboBoxes = new ComboBox[2];
            for (int a= 0; a < comboBoxes.length; a++){
                comboBoxes[a] = new ComboBox(cst);
            }

            // symbol button
            SymbolButton.SymbolButtonStyle sst = new SymbolButton.SymbolButtonStyle();
            sst.autoSize = false;
            sst.textSize = 35;
            sst.normalColor = 0x00ffffff;
//            sst.symbol
            sst.symbol = Resources.getDrawable("mainEditorSelectKey");

            symbolButtons = new SymbolButton[5];
            for (int a = 0; a < symbolButtons.length; a++){
                symbolButtons[a] = new SymbolButton(sst);
            }

            // checkboxai
            CheckBox.CheckBoxStyle hst = new CheckBox.CheckBoxStyle();
            hst.autoSize = false;
            hst.checkedBox = Resources.getDrawable("defaultCheckBoxTicked");
            hst.box = Resources.getDrawable("defaultCheckBox");
            hst.width = 243;
            hst.height = 40;

            checkBoxes = new CheckBox[2];
            for (int a = 0; a < checkBoxes.length; a++){
                checkBoxes[a] = new CheckBox(hst);
            }

            angularSelectionDialog = new AngularSelectionDialog();
        }

        /* Daiktu dejimas i paneli. */

        // paprasto label pridejimas.
        Label addLabel(String text){
            // susirandam laisva labeli. Idedam nurodyta teksta
            Label e = labels[usedLabels++];
            e.setText(text);
            // nustatom parametrus, kur ir kaip atrodys
            e.setAutoSizing(false);
            e.setPosition(5, startingY);
            e.setSize(243, 40);
            // nuleidziam starting y kitiems blokams
            startingY -= 50;

            // idedam i panele.
            settingsView.addControl(e);
            return e; // grazinam labeli jei kartais kazkas naudos, kadangi blokas nekuriamas labeliui.
        }

        // parametro keitimo laukas.
        void addParameterChanger(String text, float defValue, final ParameterBlock e){
            // susirandam labeli, idesim teksta, ir nustatom pozicija.
            Label label = labels[usedLabels++];
            label.setAutoSizing(false);
            label.setText(text);
            label.setPosition(5, startingY);
            label.setSize(135, 40);

//            float textWidth = label.getTextWidth();

            // susirandam text boxa, idedam listeneri, nustatom pozicija pagal tai kokio ilgo tekstas ir
            // kurioj vietoj turi but y.
            TextBox box = textBoxes[usedTextBoxes++];
            box.setText(defValue+"");
            box.setSize(97, 40);
            box.setFocusListener(new FocusListener() {
                @Override
                public void onLostFocus(Interface k) {
                    e.parameterChanged(e.box1);
                }

                @Override
                public void onFocus(Interface e) {}
            });

            box.setPosition(5 + label.getWidth(), startingY);
            // pamazinam kitiems blokams
            startingY -= 50;

            // sudedam labeli ir text boxa i panele.
            settingsView.addControl(label, box);

            // kuriam blocka, kuris bus atsakingas uz blocko listener pateikima.
            e.mainLabel = label;
            e.box1 = box;
            e.blockID = blocks.size;
            blocks.add(e);
        }

        // gear jointui. combo box bus automatiskai atnaujinami patys.
        void addGearJointSpecial(final ParameterBlock e){
            // surandam du labelius ir du combo boxus. Sis blokas uzims du blokus.
            Label label1 = labels[usedLabels++];
            Label label2 = labels[usedLabels++];
            label1.setText("1:");
            label2.setText("2:");
            ComboBox box1 = comboBoxes[usedComboBoxes++];
            ComboBox box2 = comboBoxes[usedComboBoxes++];

            for (int a = 0; a < jointManager.getJointSize(); a++){
                JointManager.JointInfo info = jointManager.getJointInfo(a);
                if (info.getJointType() == 5 || info.getJointType() == 7){
                    // 5 yra prismatic joint, o 7 revolute joint.
                    // sumetam situos joint i sarasa.
                    box1.append(info.getJointID());
                    box2.append(info.getJointID());
                }
            }

            // nustatom kurioj vietoj stoves pirmasis labelis.
            label1.setAutoSizing(false);
            label1.setPosition(5, startingY);
            label1.setSize(39, 40);

            // idedam pirmaji boxa.
            box1.setPosition(52, startingY);

            // nuleidziam kitam blokui.
            startingY -= 50;

            // antra labeli sutvarkom
            label2.setAutoSizing(false);
            label2.setPosition(5, startingY);
            label2.setSize(39, 40);

            // idedam antra combo boxa.
            box2.setPosition(52, startingY);

            // nuleidziam dar kiteims blokams
            startingY -= 50;

            // sudedam viska i panele
            settingsView.addControl(label1, box1, label2, box2);

            // kuriam bloka del listener pateikimo.
            e.jointList1 = box1;
            e.jointList2 = box2;
            ComboBox.IndexChangeListener listener = new ComboBox.IndexChangeListener() {
                @Override
                public void selectedIndexChanged(int old, int current) {
                    // butinai reik paziuret ar yra pasirinkta kazkas.
                    String text1 = e.jointList1.getSelectedIndex() == -1 ? null : e.jointList1.getText();
                    String text2 = e.jointList2.getSelectedIndex() == -1 ? null : e.jointList2.getText();
                    e.jointSelected(text1, text2);
                    // neziuri ar parinkti tie patis jointai. bet tebunie...
                }
            };
            box1.setIndexChangeListener(listener);
            box2.setIndexChangeListener(listener);
            e.mainLabel = label1;
            e.sideLabel = label2;
            e.blockID = blocks.size;
            blocks.add(e);
        }

        // angle keitimui.
        void addAngularChanger(String value, final ParameterBlock e){
            // imam labeli. Nustatom pozicija, dydi.
            Label label = labels[usedLabels++];
            label.setText(value);
            label.setAutoSizing(false);
            label.setPosition(35, startingY);
            label.setSize(90, 40);

            // mygtukas, pozicija ir listener
            SymbolButton button = symbolButtons[usedSymbolButtons++];
            button.setPosition(139, startingY);
            button.setSize(43, 40);
            button.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    // cia reikes dar sukurt ir angular dialog listeneri....
                    angularSelectionDialog.show(e.getStartingAngle(), new AngularSelectionDialog.AngularSelectionDialogListener() {
                        @Override
                        public void angleSelected(float angle, float x, float y) {
                            e.angleSelected(angle, x, y, e.mainLabel); // tik sitoj vietoj kvieciam blocko listeneri.
                        }

                        @Override
                        public void cancel() {}
                    });
                }
            });

            // nuleidziam kitiems blokams.
            startingY -= 50;

            // sumetam i panele.
            settingsView.addControl(label, button);

            // kuriam bloka
            e.mainLabel = label;
//            e.button = button;
            e.blockID = blocks.size;
            blocks.add(e);
        }

        // checkbox pridejimas.
        void addCheckBox(String text, boolean checked, final ParameterBlock e){
            // surandam checboxa. ir nustatom kur jis bus.
            CheckBox box = checkBoxes[usedCheckBoxes++];
            box.setText(text);
            box.setChecked(checked);
            box.setPosition(5, startingY);

            // nuleidziam kitiems blokams
            startingY -= 50;

            // kuriam bloka del listener ir dependet valdymo.
            e.checkBox = box;
            box.setCheckListener(new CheckBox.CheckedListener() {
                @Override
                public void onCheck(boolean checked) {
                    checkBlockDependentList(e);
                    // kvieciam listener.
                    e.checkBoxChecked(checked);
                }
            });
            // dedam i panele
            settingsView.addControl(box);

            e.blockID = blocks.size;
            blocks.add(e);
        }

        // coord keitimas.
        void addCoordChanger(boolean addLabel, String labelText, String xBoxValue, String yBoxValue,
                             boolean addButton, final ParameterBlock e){
            float separator = 5;
            float x = separator;
            if (addLabel){ // label pridejimas jei to reikia.
                Label label = labels[usedLabels++];
                label.setText(labelText);
                label.setAutoSizing(false);
                label.setSize(45, 40);
                label.setPosition(x, startingY);
                x += label.getWidth() + separator;

                e.mainLabel = label;

                settingsView.addControl(label);
            }

            // listener text boxams
            FocusListener focusListener = new FocusListener() {
                @Override
                public void onLostFocus(Interface k) {
                    e.cordInputChanged(e.box1, e.box2);
                }

                @Override
                public void onFocus(Interface e) {

                }
            };

            // x text boxas
            TextBox textX = textBoxes[usedTextBoxes++];
            textX.setPosition(x, startingY);
            textX.setSize(64, 40);
            textX.setText(xBoxValue);
            textX.setFocusListener(focusListener);

            x += textX.getWidth() + separator;

            // y text boxas
            TextBox textY = textBoxes[usedTextBoxes++];
            textY.setText(yBoxValue);
            textY.setSize(64, 40);
            textY.setPosition(x, startingY);
            textY.setFocusListener(focusListener);

            x += textY.getWidth() + separator;

            settingsView.addControl(textX, textY);

            if (addButton){ // jeigu reik mygtuko pridedam ir ji.
                SymbolButton button = symbolButtons[usedSymbolButtons++];
                button.setSize(43, 40);
                button.setPosition(x, startingY);
                button.setClickListener(new ClickListener() {
                    @Override
                    public void onClick() {
                        e.cordButtonClick(e);
                    }
                });
//                e.button = button;

                settingsView.addControl(button);
            }

            // paslenkam zemiau kitiem blokams.
            startingY -= 50;

            // sumetam likuti i bloka.
            e.box1 = textX;
            e.box2 = textY;
            e.blockID = blocks.size;
            blocks.add(e);
        }

        /* funkcionalumas. */

        public void checkBlockDependentList(ParameterBlock e){
            // perziurim dependent ir jei kazka randam pakeiciam spalva i pilka arba juoda.
            for (float id : e.dependentBlockIds){
                for (ParameterBlock block : blocks){
                    if (id == block.blockID && block.mainLabel != null){
                        if (e.checkBox.isChecked()) {
                            block.mainLabel.setTextColor(0xff000000);
                            if (block.sideLabel != null){
                                block.sideLabel.setTextColor(0xff000000);
                            }
                        } else {
                            block.mainLabel.setTextColor(0xff555555);
                            if (block.sideLabel != null){
                                block.sideLabel.setTextColor(0xff555555);
                            }
                        }
                    }
                }
            }
        }

        void setStartingY(float y){
            startingY = y;
        }

        // isvalys panele. Susigrazins visas dalis atgal.
        void clearPanel(){
            for (int a = 0; a < usedLabels; a++){
                settingsView.removeControl(labels[a]);
                labels[a].setTextColor(0xff000000); // galejo but pilka.
            }
            usedLabels = 0;
            for (int a = 0; a < usedTextBoxes; a++){
                settingsView.removeControl(textBoxes[a]);
                textBoxes[a].setFocusListener(null); // resetinam.
            }
            usedTextBoxes = 0;
            for (int a = 0; a < usedCheckBoxes; a++){
                settingsView.removeControl(checkBoxes[a]);
                checkBoxes[a].setCheckListener(null);
            }
            usedCheckBoxes = 0;
            for (int a = 0; a < usedComboBoxes; a++){
                settingsView.removeControl(comboBoxes[a]);
                comboBoxes[a].clear();
                comboBoxes[a].setIndexChangeListener(null);
            }
            usedComboBoxes = 0;
            for (int a = 0; a < usedSymbolButtons; a++){
                settingsView.removeControl(symbolButtons[a]);
                symbolButtons[a].setClickListener(null);
            }
            usedSymbolButtons = 0;

            blocks.clear();
        }
    }

    private class ParameterBlock{
        private Label mainLabel, sideLabel;
        TextBox box1, box2;
//        private SymbolButton button;
        private CheckBox checkBox;
        private ComboBox jointList1, jointList2;

        private int blockID;
        private final Array<Integer> dependentBlockIds = new Array<>();

        /** Called when parameter block is changed */
        void parameterChanged(TextBox value){}

        /** Called when cord input was changed in text boxes. */
        void cordInputChanged(TextBox x, TextBox y){}

        /** Called when cord button was clicked */
        void cordButtonClick(ParameterBlock e){}

        /** Called on gear joint when joint id was selected */
        void jointSelected(String jointId1, String jointId2){}

        /** Called when check box was ticked */
        void checkBoxChecked(boolean checked){}

        /** Called when user applied angle from angular dialog. */
        void angleSelected(float angle, float x, float y, Label angleInfo){}

        /** angle for angle selection dialog. */
        float getStartingAngle(){
            return 0;
        }
    }

    // reverse scroll view prilipins kontrole su y...
    // pakeitus scroll view virtualu dydi, sis kartu nusitemps ir kontrole i toki pati auksti.
    // pvz kontrole buvo 5 taskais iki virsaus. Padidinus dydi kontrole nutemps i viru prie tiek, kad butu 5 taskais prie virsaus ir atvirksciai.
    private class ReverseScrollView extends ScrollView{

        public ReverseScrollView(ScrollViewStyle style){
            super(style);
        }

        @Override
        public void setVirtualSize(float x, float y) {
            float oldh = getVirtualHeight(); // mum svarbu tik y.
            float offset = y - oldh; // per kiek pakels ar sumazins.
            if (offset > 0) {// kai virtual size padideja, tai reik pakeist dydi prie perstatant kontroles, kad kontroles turetu kur persikelt.
                // negalima sito kviest kai dydis sumazeja.
                super.setVirtualSize(x, y);
            }
            // keiciam interface pozicija.
            for (int a = 0; a < getHost().getControls().size(); a++){
                Interface e = getHost().getControls().get(a);
                if (e.getPositioning() == Window.relativeView){
                    // keisim tik relative pozicija turincias kontroles
                    Vector2 pos = e.getPosition();
                    Vector2 panelPos = getPosition();
                    float rx = pos.x - panelPos.x; // lokalios koordinates.
                    float ry = pos.y - panelPos.y;

                    ry += offset; // cia tas perkelimas
                    e.setPosition(rx, ry); // perstatom.
                }
            }
            if (offset < 0){ // kvieciam sita cia tik tada kai dydis sumazeja, nes pries sita kontroles yra perstatomos zemiau ir taip nepaveikiamos
                // pacio panelio. Scroll view gali tada ramiai sumazet.
                super.setVirtualSize(x, y);
            }

        }
    }

    private class EditDialog extends PopUp{
        private JointManager.JointInfo currentJoint;

        private TextBox idField;

        public EditDialog() {
            super(680, 274);

            // label sukurimas
            Label.LabelStyle lst = new Label.LabelStyle();
            lst.autoSize =false;
            lst.x = 17;
            lst.y = 166;
            lst.width = 396;
            lst.height = 93;
            lst.textSize = 60;
            lst.text = "Joint ID:";
            lst.verticalAlign = Align.center;

            addControl(lst.createInterface());

            // teksto lauko sukurimas
            TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
            tst.autoSize = false;
            tst.x = 17;
            tst.y = 82;
            tst.width = 532;
            tst.height = 70;
            tst.textSize = 50;
            tst.background = Resources.getDrawable("whiteSystemColor");
            tst.verticalAlign = Align.center;

            TextBox idBox = new TextBox(tst);
            idField = idBox;
            addControl(idBox);

            // mygtuku kurimas
            // delete mygtukas
            Button.ButtonStyle bst = new Button.ButtonStyle();
            bst.background = tst.background;
            bst.normalColor = 0xff0000ff;
            bst.autoSize = false;
            bst.x = 491;
            bst.y = 194;
            bst.width = 131;
            bst.height = 50;
            bst.textSize = 50;
            bst.text = "Delete";

            Button delete = new Button(bst);
            delete.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    deleteClick();
                }
            });
            addControl(delete);

            // copy mygtukas
            bst.x = 17;
            bst.y = 16;
            bst.width = 179;
            bst.text = "Create copy";

            Button copy = new Button(bst);
            copy.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    copyClick();
                }
            });
            addControl(copy);

            // cancel
            bst.x = 414;
            bst.width = 120;
            bst.text = "Cancel";

            Button cancel = new Button(bst);
            cancel.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    close(); // tiesiog uzdarom dialoga.
                }
            });
            addControl(cancel);

            // apply
            bst.x = 547;
//            bst.width = 100;
            bst.text = "Apply";

            Button apply = new Button(bst);
            apply.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    applyClick();
                }
            });
            addControl(apply);
        }

        public void show(JointManager.JointInfo jointInfo){
            if (jointInfo == null){
                return; // neatidarom jeigu nera joint.
            }
            currentJoint = jointInfo;
            idField.setText(jointInfo.getJointID()); // irasom id.
            open(); // atidarom
            idField.getFocus(); // fokusuojam teksto lauka.
        }

        private void deleteClick(){
            ConfirmDialog dialog = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.YesNo);
            dialog.setText("Joint will be deleted! Action cannot be undone. Do you still want to delete this joint?");
            dialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                @Override
                public void onYes() {
                    jointManager.removeJoint(currentJoint);
                    EditDialog.this.close(); // uzdarom kartu ir musu edit dialoga, nes joint nebera.
                    // kadangi joint istrinam, reik ir paneles updatint
                    updateJointList(); // bei joint list.
                    selectedJoint = -1; // nieko kito neparenkam.
                    jointTypeSelected();

                    Project.save();
                }

                @Override
                public void onNo() {}

                @Override
                public void onCancel() {}
            });
            dialog.show();
        }

        private void applyClick(){
            String text = idField.getText();

            if (checkIdName(text.trim())) {
                // jei atejo cia tai viskas ok.
                jointManager.changeJointId(currentJoint, text); // keiciam id.
                close(); // uzdarom
                updateJointList(); // kadangi keiciam id varda, tai keiciam ir cia.

                jointList.setSelectedIndex(selectedJoint);

                Project.save();
            }
        }

        private void copyClick(){
            InputTextDialog dialog = new InputTextDialog("Joint copy will be created. Enter new joint id:");
            dialog.setInputDialogListener(new InputTextDialog.InputDialogListener() {
                @Override
                public void onInput(String input) {
                    String text = input.trim();
                    if (checkIdName(text)){
                        // cia jau vardas tinka. Kuriam kopija
                        int index = jointManager.getJointSize();
                        JointManager.JointInfo info = jointManager.copyJoint(text, currentJoint); // kuriam kopija
                        if (info != null){ // susikure.
                            updateJointList(); // atnaujinam sarasa
                            selectedJoint = index; // parenkam naujaji joint
                            jointList.setSelectedIndex(selectedJoint); // sarase irgi parenkam
                            jointTypeSelected(); // nustatom reikiama panele
                            EditDialog.this.close(); // uzdarom edit dialoga.

                            Project.save();
                        }else { // kazko nesusikure
                            showMessage("Creating copy failed. Please try again!");
                        }
                    }
                }

                @Override
                public void cancel() {}
            });
            dialog.getInput().setText(currentJoint.getJointID() + "Copy");
            dialog.open();
        }

        private void showMessage(String message){
            AlertToast toast = new AlertToast(message);
            toast.show(Toast.SHORT);
        }

        /** @return true if name is available. */
        private boolean checkIdName(String text){ // patikrint ar toks id egzistuoja.
            if (text == null || text.length() == 0){
                showMessage("Id cannot be empty!");
                return false;
            }else {
                for (int a = 0; a < jointManager.getJointSize(); a++) {
                    JointManager.JointInfo info = jointManager.getJointInfo(a);
                    if (info.getJointID().equals(text)) {
                        showMessage("Id already exists: " + text);
                        return false;
                    }
                }
                return true;
            }
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ENTER){ // kad patogiau butu.
                applyClick();
                return true;
            }
            return super.keyDown(keycode);
        }
    }
}
