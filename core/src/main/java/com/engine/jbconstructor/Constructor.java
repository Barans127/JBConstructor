package com.engine.jbconstructor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.engine.core.ErrorMenu;
import com.engine.core.ImagesLoader;
import com.engine.core.Resources;
import com.engine.root.GdxWrapper;

/** Decodes JBConstructor files. Loads resources and holds templates which can be used to build forms.
 * It does not use {@link com.badlogic.gdx.utils.Disposable} to dispose resources as it uses {@link com.engine.core.Resources} class
 * which manages and disposes all resources. You can still call {@link #destroy()} method to dispose all resources
 * associated with this class instance.*/
public class Constructor{ // dispose nereik, nes naudojama Resource klase. Si klase ir disposins viska.
    private SavedFileDecoder decoder;
    private boolean isCreated;

    private Array<Template> forms;

    private ConstructorInitializeListener listener;

    /* error handlinimas */
    boolean ignoreNullResources = false;

    public Constructor(){
        decoder = new SavedFileDecoder();
        forms = new Array<>();
    }

    /* listener */

    public void setConstructorInitializeListener(ConstructorInitializeListener e){
        listener = e;
    }

    public ConstructorInitializeListener getListener() {
        return listener;
    }

    /* gavimo metodai. */

    /** Ignores errors when creating entity and it's resource not found. Default false.
     * If drawable not found then null drawable is replaced with white system drawable.*/
    public void setIgnoreNullResources(boolean ignoreNullResources){
        this.ignoreNullResources = ignoreNullResources;
    }

    /** @return if resources being ignored if not found. */
    public boolean isIgnoreNullResources(){
        return ignoreNullResources;
    }

    /** Decoder of this constructor. It holds info which was read from JBConstructor file. */
    public SavedFileDecoder getDecoder(){
        return decoder;
    }

    /** @return Template by given id. if constructor was not loaded or index is out of bounds then null is returned. */
    public Template getTemplate(int index){
        if (index >= 0 && index < forms.size){
            return forms.get(index);
        }
        return null;
    }

    /** @return Template list size. If no initialize was called then list is always empty. */
    public int getTemplatesSize(){
        return forms.size;
    }

    /** @return template with given name. if not found null is returned. */
    public Template getTemplate(String name){
        for (Template e : forms){
            if (e.getIdName().equals(name)){
                return e;
            }
        }
        return null;
    }

    public Array<String> getTemplatesNameList() {
        Array<String> names = new Array<>();
        for (int a = 0; a < forms.size; a++){
            names.add(forms.get(a).getIdName());
        }
        return names;
    }

    /** All templates list. */
    public Array<Template> getTemplates() {
        return forms;
    }

    /** @return true if templates were created. */
    public boolean isCreated(){
        return isCreated;
    }

    /* Visas veikimas. skaitymas, uzkrovimas. */

    /** reads JBConstructor save file. Also initialize resources (asynchronous loading will be used). Creates everything needed.
     * @param isJson is JBConstructor save file json format.
     * @param isCompressed is JBConstructor save file compressed*/
    public void decode(String jbcFile, boolean isJson, boolean isCompressed){
        decode(jbcFile, isJson, isCompressed, true, false);
    }

    /** reads JBConstructor saved file.
     * @param isJson is JBConstructor save file json format.
     * @param isCompressed is JBConstructor save file compressed
     * @param initializeResources should resources be loaded? You may load them manually.
     * @param fastInitialize Only works if initializeResources true. Initialize all resources now. If there are a lot of them
     * program might look as stuck. In that case think about asynchronous loading.*/
    public void decode(String jbcFile, boolean isJson, boolean isCompressed, boolean initializeResources, boolean fastInitialize){
        FileHandle e = Gdx.files.internal(jbcFile);
        if (!e.exists()){
            GdxWrapper.getInstance().setError("Cannot locate file: " + jbcFile, ErrorMenu.ErrorType.MissingResource);
            return;
        }
        decode(e, isJson, isCompressed, initializeResources, fastInitialize);
    }

    /** reads JBConstructor saved file.
     * @param isJson is JBConstructor save file json format.
     * @param isCompressed is JBConstructor save file compressed
     * @param initializeResources should resources be loaded? You may load them manually.
     * @param fastInitialize Only works if initializeResources true. Initialize all resources now. If there are a lot of them
     * program might look as stuck. In that case think about asynchronous loading.*/
    public void decode(FileHandle jbcFile, boolean isJson, boolean isCompressed, boolean initializeResources, boolean fastInitialize){
        if (!isCreated) {
            decoder.readSave(jbcFile, isJson, isCompressed);
            if (decoder.getErrorMessage() != null) {
                // ka su klaida darom?
                GdxWrapper.getInstance().setError(decoder.getErrorMessage(), ErrorMenu.ErrorType.ControlsError); // ir visks.
                return;
            }
            if (!decoder.isExportSaveFile()) {
                GdxWrapper.getInstance().setError("File must be exported save file.", ErrorMenu.ErrorType.ControlsError);
                return;
            }
            if (initializeResources) {
                initializeResources(fastInitialize);
            }
        }
    }

    /* resursu uzkrovimas */

    /** Load resources if file is decoded and resources was not loaded before. Save file must be exported file type.
     * all Resources will be loaded in {@link com.engine.core.Resources} class. */
    public void initializeResources(boolean fastInitialize){
        initializeResources(fastInitialize, new ImagesLoader());
    }

    /** Load resources if file is decoded and resources was not loaded before. Save file must be exported file type.
     * all Resources will be loaded in {@link com.engine.core.Resources} class.
     * ImagesLoader finish listener will be replaced with this Constructor listener, so it can determine if it can create all templates.*/
    public void initializeResources(boolean fastInitialize, ImagesLoader e){
        if (!isCreated && decoder.isSaveFileDecoded() && decoder.isExportSaveFile()){
//            ImagesLoader e = new ImagesLoader();
            // reik nustatyt kur yra resource failas.
            String filePath = decoder.getSaveFile().path();
            String root;
            if (filePath.contains("/")){
                root = filePath.substring(0, filePath.lastIndexOf("/")) + "/"; // aprasymo pradzia.
            }else {
                root = ""; // tiesiog nieko.
            }
            root += decoder.getProjectRoot();
            e.readFileForResources(root);
            if (fastInitialize){
                e.loadAllNow();
                createResources();
                isCreated = true;
                if (listener != null) {
                    listener.onInitializeFinnish();
                    listener = null; // ai nebereik, vistiek nebenaudosim.
                }
            }else {
                e.setImagesLoaderListener(new ImagesLoader.ImagesLoaderListener() {
                    @Override
                    public void onFinishLoading() {
                        createResources();
                        isCreated = true;
                        if (listener != null) {
                            listener.onInitializeFinnish();
                            listener = null;
                        }
                    }
                });
                e.startLoadAsynchronous(true);
            }
        }
    }

    /* Visi chain, images.. absoliuciai viskas. Galima bus kviest tik po to kai bus viskas uzkrauta. */
    private void createResources(){
        for (SavedFileDecoder.ProjectForm form : decoder.getProjectForms()){
            Template e = new Template(this);
            boolean done = e.create(form); // viska sugaudys ko reik.
            if (done) // viskas gerai. jei bus negerai, ignruosim visa forma.
                forms.add(e); // idedam i sarasa. jau paruosta.
        }
    }

    /* listener. */

    public interface ConstructorInitializeListener{
        /** Called when resources are loaded. */
        void onInitializeFinnish();
    }

    /* constructor sunaikinimas */

    /** Disposes all resources. You need to manually remove all templates from form else you might get disposed images drawing (black rectangle).
     * This method does not remove templates from forms. It will not remove interfaces from froms.
     * This will call {@link Template#destroyPhysicsBodies()} and than will release all templates.
     * After that all resources associated with this {@link Constructor} will be disposed.*/
    public void destroy(){
        if (isCreated) {
            String filePath = decoder.getSaveFile().path();
            String root;
            if (filePath.contains("/")) {
                root = filePath.substring(0, filePath.lastIndexOf("/")) + "/"; // aprasymo pradzia.
            } else {
                root = ""; // tiesiog nieko.
            }
            root += decoder.getProjectRoot(); // pasigaunam resource faila su visom resource names.
            FileHandle resources = Gdx.files.internal(root);
            String info;
            try {
                info = resources.readString(); // nuskaitom eilutes.
            } catch (GdxRuntimeException ex) { // del viso pikto klaida metam.
                GdxWrapper.getInstance().setError("Cannot read from given file: " + resources.path(), ErrorMenu.ErrorType.UnknowError);
                return;
            }

            String[] lines = info.split("\\r?\\n"); // atskiriam eilutes.
            for (String line : lines) {
                String content;
                if (line.contains("//")) { // nuimam komentarus
                    content = line.split("//", 2)[0];
                } else {
                    content = line; // ner komentaro.
                }
                content = content.trim(); // nukerpam galunes
                if (!content.contains(":")) { // neturi reikalingojo bruksnio, reisk kazkokia bloga eilute.
                    continue; // o tai gal klaida nuskaitant?
                }
                String[] parts = content.split(":", 2); // kertam per ta dvitaski
                String name = parts[1].trim(); // paimam id name.
                if (name.length() > 0) {
                    // cia trinimas vyks.
                    Resources.removeDrawable(name); // ir tiesiog naikinam pagal id name.
                }
            }
            isCreated = false;
            forms.clear();
        }else {
            Gdx.app.log("Constructor", "Cannot destroy resources as they were never created.");
        }
    }
}
