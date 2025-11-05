package com.jbconstructor.main.forms;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;
import com.engine.core.ErrorMenu;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Form;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.Toast;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.interfaces.controls.views.PreparedListView;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.listeners.ClickListener;
import com.engine.root.GdxPongy;
import com.jbconstructor.main.dialogs.FileChooser;
import com.jbconstructor.main.dialogs.FormCreateDialog;
import com.jbconstructor.main.managers.Project;
import com.jbconstructor.main.managers.ProjectLoader;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/* Start form. Here we can choose new project or load previous project. */
public class StartForm extends Form {
    private FormCreateDialog createDialog;
    public static FileChooser.FileChooseListener listener;
    private Toast errorMessage;

    /* sitie skirti praeitu projektu atidarymui is list viewo. */
    private PreparedListView listView;
    private static int maxPreviousProjects = Resources.getPropertyInt("maxPreviousProjects", 20); // default 20
    private Array<String> filesLocation;

    private String chosenPath; // pasirinkto item absolute adresas.
    private Interface selectedItem; // kad lengviau remove mygtuka valdyt butu.
    private int chosenIndex;
    // projekto previous valdymui.
    private SymbolButton remove;

    public StartForm(){
        Engine p = GdxPongy.getInstance();
        float height = p.getScreenHeight();

        // mygtukai.
        // new project.
        SymbolButton.SymbolButtonStyle sbt = new SymbolButton.SymbolButtonStyle();
        sbt.background = Resources.getDrawable("halfWhiteColor");
        sbt.normalColor = 0x00000000;
        sbt.onColor = 0xFFFF5500;
        sbt.pressedColor = 0xFFBB5500;
        sbt.position = SymbolButton.TextPosition.DOWN;
        sbt.symbol = Resources.getTextureDrawable("startFormNewProjectKey");
        sbt.text = "NEW PROJECT";
        sbt.textSize = 61;
        SymbolButton newProject = new SymbolButton(sbt);
        newProject.auto();
        float wallOffset = 40;
        newProject.setPosition(wallOffset, height - newProject.getHeight() - wallOffset);
        newProject.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                newProjectClick();
            }
        });
        addControl(newProject);
        // search button
        sbt.symbol = Resources.getTextureDrawable("startFormSearchKey");
        sbt.text = "SEARCH PROJECT";
        SymbolButton searchProject = new SymbolButton(sbt);
        searchProject.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                searchClick();
            }
        });
        searchProject.auto();
        searchProject.setPosition(wallOffset, newProject.getPosition().y - searchProject.getHeight() - wallOffset);
        addControl(searchProject);

        // label
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.textSize = 67;
        lst.text = "PREVIOUS USED PROJECTS:";
        lst.x = wallOffset * 2 + newProject.getWidth(); // du kartus nuo sienos ir per visa mygtuko dydi.
        lst.y = newProject.getPosition().y + newProject.getHeight() - lst.textSize;
        Label label = new Label(lst);
        addControl(label);

        // list view for used project list
        PreparedListView.PreparedListViewStyle lwst = new PreparedListView.PreparedListViewStyle();
        lwst.background = sbt.background;
        lwst.rowCount = 1;
        lwst.width = 800;
        lwst.height = 500;
        lwst.x = lst.x;
        lwst.y = lst.y - lwst.height - wallOffset/2;
        lwst.itemWidth = lwst.width-1;
        lwst.itemHeight = lwst.height/7;
        lwst.separatorSize = 0;
        lwst.choosenState = 0xFF12e543;
//        lwst.multiSelect = true;
        lwst.activeItemsStyle.horizontalAlign = Align.left;
        lwst.activeItemsStyle.textSize = 61;
        lwst.activeItemsStyle.background = Resources.getDrawable("halfWhiteColor");
        PreparedListView list = new PreparedListView(lwst);
        list.setActiveItemClickListener(new PreparedListView.ActiveItemClickListener() {
            @Override
            public void onActiveItemClick(Interface activeItem, int index, Object userData) {
                if (index >= 0 && index < filesLocation.size) {
                    chosenPath = filesLocation.get(index); // ???? index out of bounds exception????
                }else {
                    GdxPongy.getInstance().setError("List has more items. It should be: " + filesLocation.size, ErrorMenu.ErrorType.ControlsError);
                    return;
                }
                selectedItem = activeItem;
                chosenIndex = index;
                updateItemLocation(0);
            }
        });
//        sbt.horizontalAlign = Align.left;
//        String name = "Projektas numeris ";
//        for (int a = 0; a < 20; a++){
////            sbt.text = name + (a+1);
////            list.addControl(new ChooseButton(sbt, color));
//            list.addItem(name + (a+1));
//        }
        addControl(listView = list);

        // ir load mygtukas
        sbt.background = Resources.getTextureDrawable("startFormLoadKey");
        sbt.text = "";
        sbt.normalColor = 0xFFFFFFFF;
        sbt.textSize = 89;
        Button load = new Button(sbt);
        load.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                editClick();
            }
        });
        load.auto();
        load.setPosition(lwst.x + lwst.width - load.getWidth() - wallOffset/3, lwst.y + wallOffset/3);

        /* nauja forma */
        createDialog = new FormCreateDialog();
        createDialog.setFormCreateListener(new FormCreateDialog.CreateFormDialogListener() {
            @Override
            public boolean onCreate(String name, String rootLocation, String folderPath) {
                return create(name, rootLocation, folderPath);
            }

            @Override
            public void cancel() {}
        });
//        addPopUp(createDialog); // engine nebenaudoja add pop up

        // list
        listener = new FileChooser.FileChooseListener() {
            @Override
            public void fileChosen(JFileChooser chooser) {
                File file = chooser.getSelectedFile();
                ProjectLoader e = new ProjectLoader();
                if (e.openExistingProject(file.getAbsolutePath())){
                    Project.getFormsManager().switchForms(0, true);
                    projectLoaded(Project.getProjectName(), file.getAbsolutePath());
                }else {
                    errorMessage.setText(e.getError());
                    errorMessage.show();
                }
            }
        };

        // toast;
        errorMessage = new AlertToast(); // engine toast sistema yra perrasyta.
//        errorMessage.setTime(10000);

        filesLocation = new Array<>();

        // istrynimo mygtukas
        sbt.symbol = Resources.getTextureDrawable("startFormMinusKey");
        sbt.background = Resources.getDrawable("halfWhiteColor");
        sbt.autoSize = false;
        sbt.width = sbt.height = lwst.itemHeight/2;
        sbt.symbolWidth = sbt.width;
        sbt.symbolHeight = sbt.height;
        sbt.normalColor = 0x00000000;
        sbt.onColor = 0xFFFF5500;
        sbt.pressedColor = 0xFFAA6600;
        sbt.visible = false;
        sbt.x = lwst.x + lwst.width - sbt.width*1.5f;
        SymbolButton button = new SymbolButton(sbt);
        button.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                removeFromList();
            }
        });
        addControl(remove = button);

        addControl(load); // kad sitas butu auskciau visko.
    }

    /* listeners */

    private boolean create(String name, String rootLocation, String folderPath){
        if (Project.getFormsManager().getFormsSize() > 0){ // fatal. kazkokia fujne nutiko.
            GdxPongy.getInstance().setError("Cannot create new Project. Existing project is opened.", ErrorMenu.ErrorType.ControlsError);
            return true;
        }
        ProjectLoader e = new ProjectLoader();
        if (e.createNewProject(name, rootLocation, folderPath)) {
            Project.getFormsManager().switchForms(0, true);
            return true;
        }else
            return false;
    }

    private void newProjectClick(){ // naujo projekto ijungimas. Turetu koki popup ismest, kur ten info pildyt.
        // projekto vardas, vieta ir t.t.
        createDialog.open();
    }

    private void searchClick(){ // turetu atidaryt koki pop up kur leistu pasirink failus.
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
                    fh.open(listener, true);
                }
            }
        });
    }

//    ConfirmDialog con = new InputTextDialog("Tai tik didelis testas");

    private void editClick(){ // load esamus is 'previous used projects'
        if (selectedItem != null){ // del sito zinom ar yra kas pazymeta.
            ProjectLoader e = new ProjectLoader();
            if (e.openExistingProject(chosenPath)){
                Project.getFormsManager().switchForms(0, true);
                projectLoaded(Project.getProjectName(), chosenPath);
                selectedItem = null;
            }else {
                errorMessage.setText(e.getError());
                errorMessage.show();
            }
        }
//        (new SavedFileDecoder()).readSave(Gdx.files.internal("null/textures/test.jbcml"));
        // testuojam confimation
//        con.setText("Nu kas tau y?????sdfsdaodahg gdasog opadg  gahd opghas hgagh dah gopahgoa hgah gagh oahg oadgh aophg ghagh ahg ahg oah goa hgo .");
//        con.open();
    }

    /* override */

    @Override
    protected void onShow() {
        Array<String> names = new Array<>(); // duplikatu perziura, trinam duplikatus.
        for (int a = 0; a < maxPreviousProjects; a++){
            String id = "projectSaveFileName" + a;
            for (int k = 0; k < maxPreviousProjects; k++){
                if (k == a){
                    continue;
                }
                String id2 = "projectSaveFileName" + k;
                if (Resources.config.getString(id).equals(Resources.config.getString(id2))){ // radom duplikata.
                    names.add(id);
                }
            }
        }
        for (String name : names){ // imam duplikatu id ir trinam.
            if (Resources.config.contains(name)){
                Resources.config.remove(name);
            }
        }
        if (names.size > 0){ // buvo duplikatu, perrrasom.
            Resources.config.flush();
        }
        // i lista irasom naujus duomenys.
        filesLocation.clear();
        listView.clear();
        for (int a = maxPreviousProjects-1; a >= 0; a--){
            String id = "projectSaveFileName" + a;
            if (Resources.config.contains(id)){
                String[] parts = Resources.config.getString(id).split(":", 2);
                listView.addItem(parts[0].trim());
                filesLocation.add(parts[1].trim());
            }
        }
        updateItemLocation(0);
    }

    @Override
    protected void background() {
        GdxPongy.getInstance().background(0xFFeaef83); // kazkas i geltona.
    }

    @Override
    public boolean beforeScrolled(float amountX, float amountY) {
        updateItemLocation(amountY);
        return false;
    }

    @Override
    public boolean beforePan(float x, float y, float deltaX, float deltaY) {
        updateItemLocation(0);
        return false;
    }

    @Override
    public boolean beforePanStop(float x, float y, int pointer, int button) {
        updateItemLocation(0);
        return false;
    }

    /* previous project valdymas */

    private void removeFromList(){ // istrin daikta is saraso.
        if (selectedItem == null){
            return;
        }
        Array<String> names = new Array<>();
        for (int a = maxPreviousProjects-1; a >= 0; a--){ // paimam visus ir isvalom visa lista.
            String id = "projectSaveFileName" + a;
            if (Resources.config.contains(id)){
                names.add(Resources.config.getString(id));
                Resources.config.remove(id);
            }
        }
        if (chosenIndex >= 0 && chosenIndex < names.size)
            names.removeIndex(chosenIndex); // istrinam butent sita.
        for (int a = names.size-1, b = 0; a >= 0; a--, b++){
            String id = "projectSaveFileName" + a;
            Resources.config.putString(id, names.get(b)); // sudedam atgal
        }
        Resources.config.flush(); // issaugom
        selectedItem = null; // pranesam, kad niekas nepasirinkta.
        onShow(); // per naujo perziurim lista
//        updateItemLocation(0); // update darom del minus mygtuko.
    }

    private void updateItemLocation(float amountY){
        if (selectedItem == null){
            remove.setVisible(false);
            return;
        }
        float height = listView.getVirtualHeight() - listView.getHeight();
        float offsetY = listView.getScrollOffsetY() - amountY*listView.getScrollSpeed();
        if (offsetY < 0) // paimta is scrollField. Reikia nes kitaip mygtukas atsiduria ne ten kur turetu scrollinant ratuka.
            offsetY = 0;
        else if (offsetY > height) {
            offsetY = height;
        }
        float x = remove.getPosition().x;
        float y = selectedItem.getPosition().y + remove.getHeight()/2 - offsetY;
        if (y < listView.getPosition().y){ // islindimas uz ribu iskart isjunks mygtuka.
            remove.setVisible(false);
        }else if (y > listView.getPosition().y + listView.getHeight() - remove.getHeight()){
            remove.setVisible(false);
        }else { // is ribu neislindo.
            remove.setVisible(true);
            remove.setPosition(x, y);
        }
    }

    /** adds project to last opened projects.
     * @param projectName project name
     * @param location absolute location*/
    public static void projectLoaded(String projectName, String location){
        if (location.toLowerCase().endsWith(".autosave.jbcml")){ // tai autosave failas, todel nereiktu jo imt. padarom kaip paprasta.
            location = location.replace(".autosave.jbcml", ".jbcml");
        }
        String combine = projectName + ":" + location;
        for (int a = 0; a < maxPreviousProjects; a++) { // tikrinam duplikatus.
            String id = "projectSaveFileName" + a;
            if (Resources.config.contains(id)){
                if (Resources.config.getString(id).equals(combine)){
                    moveToListTop(combine);
                    Resources.config.flush();
                    return; // toks jau yra. eeee. reik perstumt i prieki sita.
                }
            }
        }
        if (Resources.config.contains("projectSaveFileName" + (maxPreviousProjects-1))){ // pilnas sarasas, salinam paskutini
            removeLastProject();
        }
        for (int a = 0; a < maxPreviousProjects; a++) { // pridedam i saras jei ner.
            String id = "projectSaveFileName" + a;
            if (!Resources.config.contains(id)){
                Resources.config.putString(id, combine);
                Resources.config.flush();
                return;
            }
        }
        throw new RuntimeException("e, kazkas negerai"); // test3000
    }

    /* kai jau buna per daug projektu ir reik visus perstumt. */
    private static void removeLastProject(){
        Array<String> names = new Array<>();
        for (int a = 1; a < maxPreviousProjects; a++){ // surenkam visus.
            String id = "projectSaveFileName" + a;
            names.add(Resources.config.getString(id)); // issisaugom
            Resources.config.remove(id); // pasalinam.
        }
//        names.removeIndex(0); // istrinam pirma.
        for (int a = 0; a < names.size; a++){ // sudedam atgal taip perstumiant.
            String id = "projectSaveFileName" + a;
            Resources.config.putString(id, names.get(a));
        }
        // sitas del viso pykto, jei kartasi yra daugiau nei turetu but.
        int count = 0;
        while (true){
            String id = "projectSaveFileName" + (maxPreviousProjects+count);
            if (Resources.config.contains(id)) {
                Resources.config.remove(id); // salinam nereikalinga.
            } else {
                break;
            }
            count++;
        }
    }

    private static void moveToListTop(String combine){
        Array<String> names = new Array<>();
        for (int a = 0; a < maxPreviousProjects; a++){ // surenkam visus.
            String id = "projectSaveFileName" + a;
            if (Resources.config.contains(id)) {
                names.add(Resources.config.getString(id)); // issisaugom
                Resources.config.remove(id); // pasalinam.
            }else {
                break; // sarasso pabaiga.
            }
        }
        names.removeValue(combine, false);
        names.add(combine);
        for (int a = 0; a < names.size; a++){ // sudedam atgal taip perstumiant.
            String id = "projectSaveFileName" + a;
            Resources.config.putString(id, names.get(a));
        }
    }

    /* class for list view. */

//    private class ChooseButton extends Button implements Connected{
//        private int steadyNormal, choosenNormal;
//        private int group = -2017;
//
//        ChooseButton(ButtonStyle st, int choosenColor){
//            super(st);
//            steadyNormal = normalColor;
//            choosenNormal = choosenColor;
//        }
//
//        @Override
//        protected void onRemove() {
//            if (v != null){
//                v.removeConnection(this);
//            }
//            super.onRemove();
//        }
//
//        @Override
//        public void setController(InterfacesController v) {
//            super.setController(v);
//            v.addConnection(this);
//        }
//
//        @Override
//        public void onClick() {
//            super.onClick();
//            normalColor = choosenNormal;
//            v.inform(this, 0);
//        }
//
//        @Override
//        public void inform(int reason) {
//            normalColor = steadyNormal; // grazinam i normalia padeti.
//            normalStatus();
//        }
//
//        @Override
//        public int getGroup() {
//            return group;
//        }
//    }
}
