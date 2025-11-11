package com.jbconstructor.editor.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.core.Resources;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.dialogs.ConfirmDialog;
import com.engine.ui.controls.toasts.AlertToast;
import com.engine.root.GdxWrapper;
import com.jbconstructor.editor.dialogs.FileChooser;
import com.jbconstructor.editor.forms.editorForm.EditForm;
import com.jbconstructor.editor.root.Element;
import com.jbconstructor.editor.root.FileLoader;

import java.io.File;
import java.util.List;

/** class which holds project name, destination and so on. */
public class Project {
    private static String projectName;
    private static String root; // project root folder.
    private static String resourcesPath = "textures"; //default
//    private static boolean hasProjectRootDirectory;
    private static final FormsManager formsManager = new FormsManager();
    private static final SaveManager saveManager = new SaveManager();
    /** for easy access file chooser. */
    private static final FileChooser fileChooser = new FileChooser();

    private static boolean isProjectOpen;

    // loaded texturos naudojimui.
//    private static final Array<String> textureKeys = new Array<>(), regionsKeys = new Array<>(); // pirmiem dviem tabam 'texture ir regioniu'.

    // loaded texturos naudojimui. Tikrai dabar. visi instance tik cia. editforma tik pasitikrint gales.
    private static final Array<ResourceToHold> loadAbleResources = new Array<>();
    private static int textureCount = 0, textureAtlasCount = 0;
    private static final Array<String> exceptionNames = new Array<>(); // sitas sarasas bus naudojamas, tik tada kai resource dingsta ir, kad netycia vietoj
    // jos neatsirastu kitas resource.

    private static final Array<ResourceChangeListener> listeners = new Array<>();

    private Project(){} // nekursim instanciju, tebunie static.

    public static boolean isProjectOpen(){
        return isProjectOpen;
    }

    public static String getResourcesPath(){
        return resourcesPath;
    }

    public static void setResourcesPath(String path){
        if (path != null) {
            File e = new File(path);
            resourcesPath = e.getAbsolutePath().replace("\\", "/");
        }else {
            resourcesPath = null;
        }
    }

    /** @return path to root folder of this project. */
    public static String getProjectRootFolderPath(){
        return root;
    }

    public static void setProjectRootFolderPath(String path){
        if (path != null) {
            File e = new File(path);
            root = e.getAbsolutePath().replace("\\", "/");
        }else {
            root = null;
        }
    }

    /** @return this project name */
    public static String getProjectName(){
        return projectName;
    }

    public static void setProjectName(String name){
        projectName = name;
    }

    /** @return false if project just created and was never saved. */
    public static boolean hasProjectRootDirectory(){
        return root != null;
    }

    /** @return save manager. Save manager allows save project easy. */
    public static SaveManager getSaveManager(){
        return saveManager;
    }

    public static FormsManager getFormsManager(){
        return formsManager;
    }

    /* resursu uzkrovimas. */

    /** scans default texture folder for resources. if resource already exist in panel then resource is ignored. */
    public static void loadResourcesFromResourcesFolder(){
        if (resourcesPath != null) {
            FileLoader loader = new FileLoader();
            File file = new File(resourcesPath);
            loader.loadResources(file.getAbsolutePath());
            load(loader, true);
        }
    }

    /** loads textures to usable resource list.
     * @param loader loader which already has loaded all resources. */
    public static void load(FileLoader loader, boolean silent){
//        if (loadAbleResources == null){
//            loadAbleResources = new Array<>();
//            FileLoader loader = new FileLoader();
//            loader.loadResources("textures"); // viso uzkrovimas is texture folder.
        int count = 0;
        boolean loaded = false;
        Array<String> text = loader.getTexturesPathList();
        TEXTURES:
        for (String name : text){ // single texture load.
            for (ResourceToHold e : loadAbleResources){ // tikrinam ar duplikatu nera
                if (e.tab == 0 && e.atlasName == null){ // ne is atlaso.
                    if (e.idName.equals(Resources.getProperty("whiteColor", "whiteSystemColor"))){
                        continue; // sita ignorinam.
                    }
                    TextureRegionDrawable dr = (TextureRegionDrawable) e.e;
                    String path = ((FileTextureData)dr.getRegion().getTexture().getTextureData()).getFileHandle().file().getAbsolutePath();
                    path = path.replace("\\", "/");
                    if (path.equalsIgnoreCase(name)){
                        continue TEXTURES;
                    }
                }
            }
            String id = "defaultImage" + textureCount;
            while (Resources.containsTextureKey(id) || checkIfResourceIdAlreadyUsed(id)){
                textureCount++;
                id = "defaultImage" + textureCount;
            }
            TextureRegionDrawable e = Resources.loadTexture(id, name);
            loadAbleResources.add(new ResourceToHold(id, e, 0, null, null));
            count++;
            loaded = true; // kazka uzkrove.
            textureCount++;
        }
        Array<TextureAtlas> atlases = loader.getAtlases();
        ATLASES:
        for (int k = 0; k < atlases.size; k++){ // region load.
            TextureAtlas a = atlases.get(k);
            for (ResourceToHold e : loadAbleResources){
                if (e.atlasName != null){
                    if (e.atlasFilePath.equalsIgnoreCase(loader.atlasPath(a))){
                        for (SpriterDrawable dr : loader.getAnimations()){ // paziurim ar sitas naujas atlasas nera vienoj is animaciju.
                            if (dr.getAtlas() == a){
                                // atlasas priklauso siai animacijai.
                                dr.getDrawer().getLoader().changeTextureAtlas(Resources.getAtlas(e.atlasName), true);
                            }
                        }
                        Resources.addDisposable(a); // disposinam, veltui uzkrauta.
                        continue ATLASES;
                    }
                }
            }
            String id = "defaultTextureAtlas" + textureAtlasCount;
            while (Resources.containsTextureAtlasKey(id)){ // ant atlaso nereik tikrint ar yra resource vardas. Vistiek nenaudoj atlaso vardu...
                textureAtlasCount++;
                id = "defaultTextureAtlas" + textureAtlasCount;
            }
            Resources.addTextureAtlas(id, a);
            for (TextureAtlas.AtlasRegion reg : a.getRegions()){
//                boolean ninePatch = reg.splits != null;
                // New libGDX method to store info about ninePatch.
                // If this is null, than it is not ninePatch.
                boolean ninePatch = reg.findValue("split") != null;
                if (ninePatch){ // uzkraus nine patch
                    loadAbleResources.add(new ResourceToHold(reg.name,
                            new NinePatchDrawable(Resources.getNinePatch(id, reg.name)), 2,
                            id, loader.atlasPath(a)));
                }else { // paprastas region.
                    loadAbleResources.add(new ResourceToHold(reg.name, Resources.getRegionDrawable(reg.name), 1,
                            id, loader.atlasPath(a)));
                }
                count++;
            }
            loaded = true; // uzkrove atlas.
            textureAtlasCount++;
        }
        // animations.
        Array<SpriterDrawable> anime = loader.getAnimations();
        ANIMATION:
        for (int a = 0; a < anime.size; a++){
            SpriterDrawable e = anime.get(a);
            for (ResourceToHold hold : loadAbleResources){ // tikrinam duplikata.
                if (hold.tab == 3){
                    if (hold.atlasFilePath.equals(e.getName())){
                        e.dispose(); // disposinam ir ziurim i kita.
                        continue ANIMATION;
                    }
                }
            }
            int animeCount = 0;
            String name = "defaultAnimeName" + animeCount;
            while (Resources.containsAnimationKey(name) || checkIfResourceIdAlreadyUsed(name)){
                animeCount++;
                name = "defaultAnimeName" + animeCount;
            }
            Resources.addAnimation(name, e);
            // texture atlas neidetas i resources, todel jis vardo kaip ir netur, bet galim taip bent pasizymet, kad toks yra.
            // siaip idetas, bet nereik to vardo.
//            loadAbleResources.add(new ResourceToHold(name, e, 3, null, e.getAtlas() == null ? null : "", e.getName()));
            // keiciam atlaso issaugojima. Mums jo nereik, todel dedam iskart null ka nesimaisytu.
            loadAbleResources.add(new ResourceToHold(name, e, 3, null, e.getName()));
            count++;
            loaded = true;// idejo animation.
        }
//        }

        if (loaded){
            triggerResourceUpdate();

            new AlertToast("Loaded " + count + " resource(s)!").show();

            if (!silent){
                Project.save(); // jeigu ne silent tai darom ir save.
            }
        }else if (!silent){
            new AlertToast("Resources to load not found or same resources selected.").show();
        }
    }

    /** check if resource key is used but resource itself is missing */
    private static boolean checkIfResourceIdAlreadyUsed(String id){
        for (String ex : exceptionNames){
            if (ex.equals(id)){
                return true;
            }
        }
        return false;
    }

    static void addNameException(String name){
        if (!exceptionNames.contains(name, false)) {
            exceptionNames.add(name);
        }
    }

    static void instertLoadableResources(Array<ResourceToHold> res){
//        for (ResourceToHold e : loadAbleResources){ // ziurim duplikatus.
//            for (ResourceToHold a : res) {
//                if (e.idName.equals(a.idName)){ // duplikatas. nukopinam tik shapes, daugiau viska palieka, vistiek tas pats.
//                    if (a.shapes.size > 0){
//                        e.shapes.clear();
//                        e.shapes.addAll(a.shapes);
//                        res.removeValue(a, true);
//                        break; // einam toliau.
//                    }
//                }
//            }
//
//        }
        loadAbleResources.addAll(res);
    }

    /* resurcu valdymas. ir projekto valdymas. */

    public static int getLoadableResourcesCount(){
        return loadAbleResources.size;
    }

    public static ResourceToHold getLoadableResource(int index){
        if (index >= 0 && index < loadAbleResources.size){
            return loadAbleResources.get(index);
        }else {
            return null;
        }
    }

    /** Checks if resource is still in project.
     * @return true if resource is still valid. False otherwise. Value of null will return false.*/
    public static boolean isLoadableResourceValid(ResourceToHold e){
//        for (ResourceToHold res  : loadAbleResources){
//            if (res == e){
//                return res;
//            }
//        }
//        return null;
        return loadAbleResources.contains(e, true);
    }

    /** Get resource by id.
     * @return resource holder instance. If resource with given id not found then null. Id with value of null or zero length will return
     * null.*/
    public static ResourceToHold getLoadableResource(String id){
        if (id == null || id.isEmpty()){
            return null;
        }
        for (ResourceToHold res : loadAbleResources){
            if (res.idName.equals(id)){
                return res;
            }
        }
        return null;
    }

//    /** loaded textures for use in editors. Edit form uses it to determine if texture resources already has been loaded. */
//    public static Array<String> getTextureKeys(){
//        return textureKeys;
//    }
//
//    /** loaded textures atlases regions for use in editors. Edit form uses it to determine if texture resources already has been loaded. */
//    public static Array<String> getTextureRegionsKeys(){
//        return regionsKeys;
//    }

    /** With this file chooser can be called */
    public static FileChooser getFileChooser(){
        return fileChooser;
    }

    /** closes project. removes all edit forms and removes and dispose all resources. */
    public static void closeProject(){
        if (GdxWrapper.getInstance().getActiveForm() instanceof EditForm){ // reikia perjungt i StartForma.
            GdxWrapper.getInstance().achangeState("mainStartForm"); // perjunks i start forma, kad nemaisytu istrintos editformos.
        }
        for (int a = formsManager.getFormsSize() - 1; a >= 0; a--){ // panaikins visas formas.
            formsManager.removeForm(a);
        }
        projectName = null;
        root = null;
        resourcesPath = null;
        textureAtlasCount = 0; // nuresetinam.
        textureCount = 0;
        for (ResourceToHold e : loadAbleResources){ // disposins viska.
            if (e.getIdName().equals(Resources.getProperty("whiteColor", "whiteSystemColor"))){
                continue; // ignorinam sita. Jo netrinsim(imanoma istrint). Pasisalins tyliai ir tiek, atidarius kita project is naujo isides.
            }
            if (e.tab == 3){ // spriter animation.
                Resources.removeAnimation(e.idName, true);
            }else if (e.atlasName != null){ // reiskia is atlasoo.
                Resources.removeAtlas(e.atlasName, true);
            } else {
                Resources.removeTexture(e.idName, true);
            }
        }
        loadAbleResources.clear(); // isvalom.

        listeners.clear();
        isProjectOpen = false;
        saveManager.projectCloses();

        // title nuimam
        Gdx.graphics.setTitle("JBConstructor");
    }

    static void openProject(){
        isProjectOpen = true;

        // cia iterpsim systemos image TODO cia.
        String id = Resources.getProperty("whiteColor", "whiteSystemColor");
        Drawable white = Resources.getDrawable(id);
        ResourceToHold hold = new ResourceToHold(id, white, 0, null, null);
        hold.visible = false; // darom nematoma.
        loadAbleResources.insert(0, hold);

        triggerResourceUpdate();

        // keiciam title i projekta varda.
        Gdx.graphics.setTitle("JBConstructor - " + getProjectName());

//        if (hasProjectRootDirectory()) {
//            FileHandle handle = Gdx.files.absolute(getProjectRootFolderPath());
//            if (handle.exists())
//                StartForm.projectLoaded(Project.getProjectName(), handle.file().getAbsolutePath());
//        }
    }

    /** Triggers save. Short cut to {@link SaveManager#triggerSave()}.*/
    public static void save(){
        saveManager.triggerSave();
    }

    /* resourcu klase. */

    /** Starts resource deletion chain. Chain includes confirm dialogs which asks user if he wants to continue...*/
    public static void startResourceDeletionChain(final ResourceToHold... holds){
        // paziurim ar kazka isvis padave
        if (holds.length == 0){
            return;
        }

        final Array<String> atlases = new Array<>();
        // paziurim ar visos value yra valid ir paziurim type.
        for (ResourceToHold e : holds){
            if (e == null){
//                Gdx.app.log("Project", "Cannot start deletion chain with null values.");
                new AlertToast("Some values are null!").show();
                return;
            }

            // tab: 0 - texture, 1 - atlaso region, 2 - nine patch (gali but is atlaso arba kurtas atskirai)
            // 3 - animation.
            // sugaudom atlasus.
            if (e.tab == 1){ // atlaso failas.
                if (!atlases.contains(e.atlasName, false)){
                    atlases.add(e.atlasName);
                }
            }else if (e.tab == 2 && e.atlasFilePath != null){
                // irgi is atlas.
                if (!atlases.contains(e.atlasName, false)){
                    atlases.add(e.atlasName);
                }
            }
        }

        // sugaudem atlasus.. Dabar telieka paklaust.
        ConfirmDialog dialog = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.YesNo);
        if (atlases.size > 0){
            dialog.setText("Some resources are from texture atlas. Deleting texture atlas will delete all regions/9patches associated with it. Do you" +
                    " really want to continue?");
        }else {
            dialog.setText("Do you really want to delete?");
        }

        dialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
            @Override
            public void onYes() {
                checkResourceDeletionAvailability(holds, atlases);
            }

            @Override
            public void onNo() {}

            @Override
            public void onCancel() {}
        });
        dialog.show();
    }

    /** After user agreed to delete resources one more dialog is left to check. Now check if resources are used by elements on forms. */
    private static void checkResourceDeletionAvailability(final ResourceToHold[] holds, final Array<String> atlases){
        // dabar sunkioji dalis. Reiks tikrint visus resursus ir ziuret ar nera naudojamas vienas
        // is trinamu elementu.
        // keliaujam per visas formas ir visus elementus.
        boolean inUse = false;
        MAIN:
        for (int a = 0; a < formsManager.getFormsSize(); a++){ // visos esancios formos.
            EditForm form = formsManager.getForm(a);

            List<Control> array = form.getController().getControls(); // formoje esantys elementai.
            for (Control e : array){
                if (e instanceof Element){ // ar tai tikra musu elementas.
                    // musu element.
                    Element element = (Element) e;

                    // dabar eisim per visus holdus (isskyrus regionus ir ninepatch is atlas). Jie atskirai ziuresis.
                    for (ResourceToHold hold : holds){
                        if (hold.tab == 1 || (hold.tab == 2 && hold.atlasFilePath != null)){ // sitie kitur ziurisi (nes ne visi gali but.)
                            continue;
                        }

                        if (element.getResName().equals(hold.idName)){
                            // TURIm NAUDOTOJA!!
                            inUse = true;
                            break MAIN; // net nebeziurim toliau..
                        }
                    }

                    // Dabar imsim atlasus ir ziuresim idus..
                    // cia ir persiziuri tie, regionai, kuriuos auksciau ignorinom.
                    for (String atlasId : atlases){
                        TextureAtlas atlas = Resources.getAtlas(atlasId);
                        if (atlas != null){
                            for (TextureAtlas.AtlasRegion region : atlas.getRegions()){
                                if (element.getResName().equals(region.name)){
                                    // RADOM naudotoja!
                                    inUse = true;
                                    break MAIN;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (inUse){
            // naudojta textures.
            ConfirmDialog confirmDialog = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.YesNo);
            confirmDialog.setText("Resources are in use! If you delete some elements will lose it's resource. Do you still want to proceed?");
            confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                @Override
                public void onYes() {
                    deleteResources(holds, atlases);
                }

                @Override
                public void onNo() {}

                @Override
                public void onCancel() {}
            });
            confirmDialog.show();
        }else {
            // trinam drasiai.
            deleteResources(holds, atlases);
        }
    }

    // cia tiesiog trinam jau resursus. Be gailescio.
    private static void deleteResources(ResourceToHold[] holds, Array<String> atlases){
        // pirma naikinsim holdus kurie nera is atlas.
        int count = 0;
        for (ResourceToHold hold : holds){
            if (hold.tab == 1 || (hold.tab == 2 && hold.atlasFilePath != null)){
                continue; // atlas file.
            }

            // system image praleidziam.
            if (!hold.getIdName().equals(Resources.getProperty("whiteColor", "whiteSystemColor"))) {
                hold.delete();
                count++;
            }
        }

        // dabar ieskosim atlaso.
        for (String atlasId : atlases) {
            TextureAtlas atlas = Resources.getAtlas(atlasId); // pasiimam atlas
            if (atlas != null) { // nu ne null turi but..
                for (TextureAtlas.AtlasRegion region : atlas.getRegions()) { // varom per jo regionus ir ieskom.
                    // is visu regionu iskosim musu holderio.
                    for (int a = loadAbleResources.size - 1; a >= 0; a--) {// per esamus holdus ieskom sio atlaso regionu.
                        ResourceToHold res = loadAbleResources.get(a);
                        if (res.idName.equals(region.name)) { // atlas regionas.
                            // radom.
                            res.delete(); // trinam regiona.
                            count++;
                            break; // judiam tolyn per regionus.
                        }
                    }
                }

                // kai perejom per visus regionus, tai belieka naikint pati atlas.
                Resources.removeAtlas(atlasId, true);
            }else {
                Gdx.app.log("Project", "Cannot find atlas: " + atlasId);
            }
        }

        new AlertToast("Deleted " + count + " resource(s) from project!").show();
        triggerResourceUpdate();

        Project.save(); // ir save kvieciam.
    }

    /** Add resource change listener */
    public static void addResourceChangeListener(ResourceChangeListener e){
        if (e != null){
            listeners.add(e);
        }
    }

    /** Remove resource change listener */
    public static void removeResourceChangeListener(ResourceChangeListener e){
        listeners.removeValue(e, true);
    }

    /** Resource change listeners. */
    public static Array<ResourceChangeListener> getResourceChangeListeners(){
        return listeners;
    }

    /** Calls resource update listeners. */
    public static void triggerResourceUpdate(){
        for (ResourceChangeListener e : listeners){
            e.resourcesChanged();
        }
    }

    public interface ResourceChangeListener{
        /** Called if something triggered resources or changed something in them. Update your lists here. */
        void resourcesChanged();
    }

    public static class ResourceToHold{ // cia tik naudojami resource, su editor nieko bendro neturi.
        /** shapes can be edited anytime */
//        public final Array<PhysicsEditor.FixtureShapeHolder> shapes; // physics taskai
        public final Drawable e; // pats resource.
        /** 0 - simple single texture
         * 1 - region drawable from texture atlas.
         * 2 - 9patch..
         * 3 - anime??*/
        public final int tab; // kuriam tabui pvz: img arba region.
        /** id name which drawable can be accessed from <Code>Resources</Code> class
         * if atlasName is null then this is texture, if false then this is from atlas. */
        private String idName; // name
        /** if resource was removed to trash then this should be set to false. */
        public boolean visible = true;
        String atlasName;
        public final String atlasFilePath;

        public ResourceToHold(String name, Drawable e, int tab,
                              String atlasName, String atlasFilePath){
            idName = name;
            this.tab = tab;
            this.e = e;
//            this.shapes = new Array<>();
//            if (shapes != null)
//                this.shapes.addAll(shapes);
            this.atlasName = atlasName;
            this.atlasFilePath = atlasFilePath;
        }

        /** if this resource is region then atlas name which holds this region. If it is not region then null. */
        public String getAtlasName(){
            return atlasName;
        }

        /** If atlas name was changed, then it is updated here.
         * @return true if name was updated. False otherwise. */
        public boolean changeAtlasName(String name){
            if (tab == 1 || tab == 2){
                // tik regionai.
                String clipped = name.trim();
                if (Resources.containsTextureAtlasKey(clipped)){
                    // turi egzistuot.
                    atlasName = clipped;

                    triggerResourceUpdate();
                    return true;
                }
            }
            return false;
        }

        /** This resource holder id name. */
        public String getIdName(){
            return idName;
        }

        /** Change resource id. Id can be change only if resource is not from atlas!
         * Id will be changed here and on {@link Resources} class. Id used in already created entities will not be changed!
         * @return true if name was changed false otherwise.*/
        public boolean changeIdName(String idName){
            if (idName == null || idName.trim().isEmpty()){
                Gdx.app.log("Project", "Resource id was not changed!");
                return false;
            }
            if (this.idName.equals(Resources.getProperty("whiteColor", "whiteSystemColor"))){
                Gdx.app.log("Project", "Cannot change id of system white color.");
                return false;
            }
            String clipped = idName.trim();
            if (tab != 1 && tab != 2){
                if (tab == 0){
                    // image paprastas. Is texture.
                    if (Resources.containsTextureKey(clipped)){
                        Gdx.app.log("Project", "Id already used: " + clipped);
                        return false; // sitas name jau yra.
                    }
                    if (Resources.containsTextureKey(this.idName)){
                        Texture e = Resources.removeTexture(this.idName, false);
                        Resources.addTexture(clipped, e);
                        this.idName = clipped;

                        getSaveManager().triggerSave(); // butinai issaugom.
                        triggerResourceUpdate();
                        return true;
                    }
                }else if (tab == 3){
                    // spriter animation.
                    if (Resources.containsAnimationKey(clipped)){
                        Gdx.app.log("Project", "Id already used: " + clipped);
                        return false;
                    }
                    if (Resources.containsAnimationKey(this.idName)){
                        SpriterDrawable e = Resources.removeAnimation(this.idName, false);
                        Resources.addAnimation(clipped, e);
                        this.idName = clipped;
                        getSaveManager().triggerSave(); // save butinai.

                        triggerResourceUpdate();
                        return true;
                    }
                }
                // id is texture atlas negalim keist, nes atlase id vardas irasytas, o mes atlaso failo neredaguosim!
            }
            Gdx.app.log("Project", "Resource id was not changed!");
            return false;
        }

        /** Deletes this resource. Removes all connections with it. */
        void delete(){
            if (idName.equals(Resources.getProperty("whiteColor", "whiteSystemColor"))){
                Gdx.app.log("Project", "Cannot delete white system color.");
                return; // sito trint negalima.
            }
            // einam per resursus ziurim, naikinsim sita img. Nereik ziuret atlas ar ne. Tiesiog trinam pati resource id.
            // pirma surandam dalis, kurios naudoja si resource ir jas nuimsim.
            Drawable nullDrawable = Resources.getDrawable("basicUsageNoImageKey");// drawable, kuris rodomas kai nera drawable.

            for (int a = 0; a < formsManager.getFormsSize(); a++){
                EditForm form = formsManager.getForm(a);

                List<Control> array = form.getController().getControls();
                for (Control e : array){
                    if (e instanceof Element){
                        // musu element.
                        Element element = (Element) e;

                        if (element.getResName().equals(idName)){
                            // tinka resource. Salinam drawable.
                            element.setResName("null");
                            element.setImage(nullDrawable);
                        }
                    }
                }
            }

            // pasalinus drawable naikinam sita resource.
            // kad ir region is atlas nesvarbu.. Nieko nenutiks. ninePatch irgi nenuskriaus originalo.
//            Resources.removeDrawable(idName);
            // pacio atlas nenaikinsim.. Ji kitu budu reiks trint.
            if (tab == 3){ // spriter animation.
                Resources.removeAnimation(idName, true);
            }else if (tab == 0){ // paprasta texture.
                Resources.removeTexture(idName, true);
            }

            // su resource susitvarkyta, toliau salinam is projekto sarasu si visa elementa.
            loadAbleResources.removeValue(this, true);
        }
    }
}
