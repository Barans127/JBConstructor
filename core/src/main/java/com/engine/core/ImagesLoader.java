package com.engine.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.interfaces.controls.Form;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.widgets.LoadIcon;
import com.engine.interfaces.controls.widgets.ProgressBar;
import com.engine.interfaces.listeners.MainDraw;

/** This class allows images to be added to Resources list, and load images few images per frame to avoid freezing main thread */
public class ImagesLoader implements MainDraw{
    // info del krovimo.
    private int imgPerFrame = 3;
    private Array<String> names, paths;

    // listener is vaizdine informacija.
    private LoadIcon loadIcon;
    private ProgressBar progressBar;
    private float ratio; // kokia dali uzims.
    private SoundLoader soundLoader; // su situo gali but, kad dalinsis.
    private int startCount;
    private ImagesLoaderListener loaderListener;

    // asynchronous load.
    private boolean loadImg;
    private Window windowToSwitch;

    private String spriterAtlasEnding = "Resources.txt";

    public ImagesLoader(){
        names = new Array<>();
        paths = new Array<>();
        LoadIcon.LoadIconStyle style = new LoadIcon.LoadIconStyle();
        Engine p = Engine.getInstance();
        float size = p.getScreenWidth() / 64f;
        style.iconRadiusX = size;
        style.iconRadiusY = size;
        style.positioning = Window.Position.fixed;
        style.x = 10;
        style.y = p.getScreenHeight()-size-10;
        loadIcon = new LoadIcon(style);
    }

    /** this ending is atached to spriter's file name and texture atlas is searched by it. This atlas is used for spriter animation.
     * Default is <code>'Resources.txt'</code> */
    public void setSpriterAtlasEnding(String ending){
        if (ending != null){
            spriterAtlasEnding = ending;
        }
    }

    /** for asynchronous loading. listiner is called when loading is completed. */
    public void setImagesLoaderListener(ImagesLoaderListener e){
        loaderListener = e;
    }

    /** @return Spriter atlas ending. */
    public String getSpriterAtlasEnding(){
        return spriterAtlasEnding;
    }

    public ImagesLoaderListener getImagesLoaderListener() {
        return loaderListener;
    }

    /** change atributes if needed. */
    public LoadIcon getLoadIcon() {
        return loadIcon;
    }

    /** how many textures load per frame when asynchronous load is active.
     * Default: 3 */
    public void setResourcesLoadNumberPerFrame(int number){
        imgPerFrame = number;
    }

    /** @return number of how many resources are loaded per frame when asynchronous load is active. */
    public int getResourcesLoadNumberPerFrame(){
        return imgPerFrame;
    }

    /** size of paths list which hasn't been loaded yet. */
    public int getListSize(){
        return paths.size;
    }

    /** @return paths list count which is set when asynchronous loading started. */
    public int getStartPathCount(){
        return startCount;
    }

    /** @return if images are loading. */
    public boolean isLoading(){
        return loadImg;
    }

    /** loads all resources from paths list. */
    public void loadAllNow(){
        for (int a = paths.size-1; a >= 0; a--){
            loadResource(names.get(a), paths.get(a));
        }
        names.clear();
        paths.clear();
    }

    /** loads few textures per frame. You can change number of texture are loaded per frame.
     * @param showLoadingIcon adds loading icon to current form (upper left corner). Removes it when loading is done. */
    public void startLoadAsynchronous(boolean showLoadingIcon){
        startLoadAsynchronous(showLoadingIcon, null);
    }

    /** loads few textures per frame. You can change number of texture are loaded per frame.
     * @param showLoadingIcon adds loading icon to current form (upper left corner). Removes it when loading is done.
     * @param windowToSwitch when loading is completed if form is not null then this forms will be switched to given*/
    public void startLoadAsynchronous(boolean showLoadingIcon, Window windowToSwitch){
        if (paths.size == 0){ // ner tolko kazka daryt be jo.
            Gdx.app.log("ImagesLoader", "List of resources is empty. Ignoring...");
            return;
        }
        loadImg = true;
        startCount = paths.size; // pasizmim kiek buvo pradziai.
//        if (progressBar != null){
//            progressBar.reset(0f); // pradesim kraut.
//        }
        TopPainter.addPaintOnTop(this);
        if (showLoadingIcon){
            Window e = Engine.getInstance().getActiveForm();
            if (e instanceof Form){
                ((Form) e).addControl(loadIcon);
            }
        }
        this.windowToSwitch = windowToSwitch;
    }

    /** All progress of loading will be reflected on this progress bar.
     * This only works if asynchronous loading is started.
     * Set to null to disable reflection to progress bar.*/
    public void setProgressBar(ProgressBar e){
        ratio = 1f;
        progressBar = e;
        if (progressBar != null){
            progressBar.reset(0f);
        }
    }

    /** All progress of loading will be reflected on this progress bar.
     * progress will be divided by ratio to this loader and to sound loader.
     * Example: ratio: 0.7 - this loader sets 70% progress bar while sound loader 30%*/
    public void shareProgressBar(ProgressBar e, SoundLoader soundLoader, float ratio){
        if (soundLoader == null || e == null){
            setProgressBar(e);
            return;
        }
        e.reset(0f); // resetinam.
        progressBar = e;
        this.soundLoader = soundLoader;
        if (ratio < 0 || ratio > 1f){
            ratio = 0.5f;
        }
        this.ratio = ratio;
        soundLoader.progressBarWasShared(1f - ratio, this);
    }

    // keist procenta jei iamges loader nespejo.
    void setPercentage(){
        synchronized (progressBar){
            progressBar.setPercentage(loadedPercent());
        }
    }

    // dabaigt iki galo, jei images loader nespejo.
    void finishPercentage(){
        synchronized (progressBar){
            progressBar.setPercentage(1f);
        }
    }

    // bendras procentas.
    private float loadedPercent(){
        float start = (float) (startCount - paths.size); // kadangi mazeja dydis, tai reik dydi pakelt.
        float fullSize = (float) startCount; // reik, kad float butu ir skaiciuotu normaliai.
        float rPercent = (start / fullSize) * ratio;
        float soundPercent = soundLoader == null ? 0f : soundLoader.percent; // sound loaderio percent.
        return soundPercent + rPercent;
    }

    /** Adds given paths and names to list for later loading. Paths and names array size must be same. */
    public void setResourcesPathForLoading(String[] path, String[] names){
        if (loadImg){
            Engine.getInstance().setError("ImagesLoader: cannot add new resources paths while loading.", ErrorMenu.ErrorType.ControlsError);
            return;
        }
        if (path.length != names.length){
            Engine.getInstance().setError("ImagesLoader: path and name arrays must be same size", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        for (int a = 0; a < path.length; a++){
            if (path[a] == null || path[a].length() == 0 || names[a] == null || names[a].length() == 0) // ignoruojam tuscius..
                continue;
            this.paths.add(MoreUtils.fixPath(path[a].trim()));
            this.names.add(names[a].trim());
        }
    }

    /** Reades resources file where is all name and paths.
     * paths are read like internal - root are where this file is located.
     * File example, loading single Texture: resources/ui/balta.png : whiteSystemColor. Path : idKey
     * File example, loading TextureAtlas: resources/textures/atlas.txt : atlasExample. Path : idKey
     * loader automatically determines if file is texture or textureAtlas.
     * reads lines and copys all info to array and gets ready to load all textures.
     * You can start loading after this method.*/
    public void readFileForResources(String path){
        FileHandle e = Gdx.files.internal(path);
        if (!e.exists()){
            Engine.getInstance().setError("ImagesLoader: Cannot locate file: " + path, ErrorMenu.ErrorType.MissingResource);
            return;
        }
        readFileForResources(e);
    }

    /** Reads resources file where is all name and paths.
     * paths are read like internal - root are where this file is located.
     * File example, loading single Texture: resources/ui/balta.png : whiteSystemColor. Path : idKey
     * File example, loading TextureAtlas: resources/textures/atlas.txt : atlasExample. Path : idKey
     * loader automatically determines if file is texture or textureAtlas.
     * reads lines and copys all info to array and gets ready to load all textures.
     * You can start loading after this method.*/
    public void readFileForResources(FileHandle resources){
        if (loadImg){
            Engine.getInstance().setError("ImagesLoader: cannot add new resources paths while loading.", ErrorMenu.ErrorType.ControlsError);
            return;
        }
        String info;
        try {
            info = resources.readString();
        }catch (GdxRuntimeException ex){
            Engine.getInstance().setError("ImagesLoader: Cannot read from given file: " + resources.path(), ErrorMenu.ErrorType.UnknowError);
            return;
        }
        // nustatom kur failas prasideda. priklijuosim pradzia prie aprasimo
        String filePath = resources.path();
        String root;
        if (filePath.contains("/")){
            root = filePath.substring(0, filePath.lastIndexOf("/")) + "/"; // aprasymo pradzia.
        }else {
            root = ""; // tiesiog nieko.
        }

        String[] lines = info.split("\\r?\\n");
        for (String line : lines){
            String content;
            if (line.contains("//")){ // nuimam komentarus
                content = line.split("//", 2)[0];
            }else {
                content = line;
            }
            content = content.trim(); // nukerpam galunes

            // del kazkokiu encodinimo bullshit sitas nematomas simbolis atsirand tekste,
            //del ko visa image loader logika pabeg ir anas galvoj, kad eilute ne tuscia. Krc
            // sita nesamone, kuri net yra nematoma reik trint, kitaip nulauz image loader ir kartu programa.
            if (content.contains("\uFEFF")){ // kazkoks chujne, kuris cia viska gadin.
                content = content.replaceAll("\uFEFF", "");
            }

//            if (!content.contains(":")){ // neturi reikalingojo bruksnio, reisk kazkokia bloga eilute.
//                continue; // o tai gal klaida nuskaitant?
//            }
            if (content.length() == 0){
                continue; // tuscia eilute.
            }
            String path, name;
            if (content.contains(":")) {
                String[] parts = content.split(":", 2);
                path = MoreUtils.fixPath(root + parts[0].trim()); // nukirsim folderius jei kartais dvigubas taskas.
                name = parts[1].trim();
            }else {
                // neturi dvitaskio. Imsim id toki koks yra failo vardas.
                path = MoreUtils.fixPath(root + content); // tiesiog taip.
                name = MoreUtils.getFileName(path);
            }
            if (path.length() == 0 || name.length() == 0) { // kazkas negerai.
                continue; // ignoruojam
            }
            names.add(name);
            paths.add(path);
        }
    }

//    /** path fix for android. Android does not accept -> "." or "..". We need to take care of that.
//     * It is useful when root+path is combined. Example root: resources/image/
//     * path: ../image.jpg. From these two we get: resources/image.jpg */
//    private String fixPath(String path){
//        if (path.contains("./") || path.contains("..")){
//            // imam zemyn dvigubus taskus...
//            while (path.contains("..")){ // ziurim ar yra tokiu kur nori grizt per folderi atgal.
////				int a = test.indexOf("..");
//                int left = 2; // 2, nes du bruksniukai turi but.
//                String[] parts = path.split("\\.\\.", 2); // kertam i dvi dali per du taskus.
//                int index = parts[0].length()-1; // imam paskutini simboli.
//                while (left > 0){
//                    char e = parts[0].charAt(index); // paziurim koks simbolis
//                    if (e == '/'){ // jeigu sitas bruksniukas...
//                        left--; // paziurim kelinta kart randam
//                        if (left == 0){ // antra kart radom, kertam zemyn.
//                            parts[0] = parts[0].substring(0, index); // nukertam iki to symnolio.
//                        }
//                    }
//                    index--;
//                    if (index < 0) // jei kartais kazkas kazko nerastu tai tiesiog stabdom viska.
//                        break;
//                }
//                path = parts[0] + parts[1]; // suklijuojam atgal jau be dvigubu tasku.
//            }
//            // dabar beliko vietisus taskus imt zemyn...
//            String[] pointParts = path.split("\\./");// kertam per tokius vietisus taskus.
//            String fin = "";
//            for (String sss : pointParts) { // sudedam atgal be tasku (juos pats split nukirto).
//                fin += sss;
//            }
//            if (fin.startsWith("/")) // dar paziurim kad neprasidedu situo bruksniuku, kitaip andoird vel neras kur failas..
//                fin = fin.substring(1); // jeigu prasideda, tai kertam ta bruksniuka zemyn.
////            System.out.println("susitvarkom su tasku: " + fin);
//            return fin;
//        }else
//            return path;
//    }

    /** clean loader. Makes all paths and names empty. */
    public void clean(){
        names.clear();
        paths.clear();
    }

    /** Cancels asynchronous load. */
    public void cancel(){
        synchronized (this) { // jeigu kartais is kito thread kviestu.
            loadImg = false;
        }
    }

    /** loads single resource */
    private void loadResource(String name, String path){
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()){
            Engine.getInstance().setError("ImagesLoader: Cannot locate file: " + path, ErrorMenu.ErrorType.MissingResource);
            return;
        }
        boolean hasTextureKey = false, hasAtlasKey = false, hasAnimationKey = false;
        if (Resources.containsTextureAtlasKey(name)){
            hasAtlasKey = true;
        }
        if (Resources.containsTextureKey(name)){
            hasTextureKey = true;
        }
        if (Resources.containsAnimationKey(name)){
            hasAnimationKey = true;
        }
//        InputStream fileStream = file.read();
//        BufferedImage img; // patikrin ar image
//        boolean isTexture;
//        try {
//            img = ImageIO.read(fileStream);
//            isTexture = img != null;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
        try {
            if (file.path().toLowerCase().endsWith(".txt") || file.path().toLowerCase().endsWith(".atlas")) { // uzkraunam texture atlas
                if (hasAtlasKey) {
                    Engine.getInstance().setError("ImagesLoader: TextureAtlas idKey already exists: " + name + "\nFor file: " + path, ErrorMenu.ErrorType.WrongPara);
                }else {
                    TextureAtlas atlas = new TextureAtlas(file);
//                    for (Texture e : atlas.getTextures()){ // sitas nebutina.. gali filtras iskart but linear. ir cia hardcoded...
//                        e.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//                    }
                    Resources.addTextureAtlas(name, atlas);
                }
            } else if (file.path().toLowerCase().endsWith(".scml")){ // uzkraunam spriter drawable
                if (hasAnimationKey){
                    Engine.getInstance().setError("ImagesLoader: Animation idKey already exists: " + name + "\nFor file: " + path, ErrorMenu.ErrorType.WrongPara);
                }else {
                    String atlasFile = file.path().substring(0, file.path().lastIndexOf(".")) + spriterAtlasEnding;
//                    String atlasFile = file.path().substring(0, file.path().lastIndexOf(".")) + "Resources.txt"; // sita irgi reiketu keist, kad nebutu hard coded.
                    TextureAtlas atlas;
                    FileHandle atl = Gdx.files.internal(atlasFile);
//                    if (atlas == null) { // dar karta patikrinam, gal ten tiesiog faila pdave, o salia sitas stovi.
                    if (atl.exists()) {
                        try {
                            atlas = new TextureAtlas(atl); // nereik i resources mest, nes gi vistiek dispose darys terp drawable.
//                            for (Texture e : atlas.getTextures()){ // nu kokie filtro keitimai... nedarom hard coded. turi nustatyt pats atlaso faile koks filtras.
//                                e.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//                            }
                        } catch (GdxRuntimeException ex2) {
                            atlas = null;
                        }
                    } else
                        atlas = null;
//                    }
                    SpriterDrawable drawable = new SpriterDrawable(file.path(), null, true); // aha, kaip del atlaso?
                    if (atlas == null){
//                        drawable.load(new File(file.path()));
                        drawable.load(file.file());
                    }else {
//                        String Aname = atlasFile.substring(atlasFile.lastIndexOf("/")+1, atlasFile.length()-1);
                        drawable.loadFromTextureAtlas(file.file(), atlas);
                    }
                    Resources.addAnimation(name, drawable);
                }
            }else { // paprasti images belieka.
                if (hasTextureKey) {
                    Engine.getInstance().setError("ImagesLoader: Texture idKey already exists: " + name + "\nFor file: " + path, ErrorMenu.ErrorType.WrongPara);
                } else {
                    Texture e = new Texture(file);
                    e.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear); // sitas tvarko, kadangi nera nustatyta filtro texture, tai default tebunie sitas.
                    Resources.addTexture(name, e);
                }
            }
        }catch (GdxRuntimeException ex){
            Engine.getInstance().setError("ImagesLoader: Error loading resource from file: " + path, ErrorMenu.ErrorType.UnknowError);
        }
    }

    @Override
    public void draw() {
        if (loadImg){
            int count = imgPerFrame;
            for (int a = paths.size-1, b = 0; a >= 0 && b < count; a--, b++){
                loadResource(names.get(a), paths.get(a));
                names.removeIndex(a); // uzkrovus salinam.
                paths.removeIndex(a);
            }
            if (paths.size == 0){ // neber ka uzkraut.
                loadImg = false;
                if (progressBar != null){
                    if (soundLoader == null)
                        progressBar.setPercentage(1f);
                    else {
//                        progressBar.setPercentage();
                        synchronized (progressBar) {
                            if (soundLoader.isLoading()) { // dar sound nebaige. Leiskim jam pabaigt.
                                progressBar.setPercentage(loadedPercent());
                            } else {
                                progressBar.setPercentage(1f); // vienu zodziu baige ir tas iri sitas.
                            }
                        }
                        soundLoader = null;
                    }
                }
            }else {
                if (progressBar != null){
//                    float start = (float) (startCount - paths.size); // kadangi mazeja dydis, tai reik dydi pakelt.
//                    float fullSize = (float) startCount; // reik, kad float butu ir skaiciuotu normaliai.
//                    float rPercent = (start / fullSize) * ratio;
//                    float soundPercent = soundLoader == null ? 0f : soundLoader.percent; // sound loaderio percent.
                    synchronized (progressBar) {
                        progressBar.setPercentage(loadedPercent()); // nustatom nauja procenta.
                    }
                }
            }
        }else {
            if (loadIcon.getController() != null){
                loadIcon.getController().removeControl(loadIcon);
            }
            TopPainter.removeTopPaint(this);
            if (loaderListener != null)
                loaderListener.onFinishLoading();
            if (windowToSwitch != null) {
                int m = Engine.getInstance().getFormKeyIndex(windowToSwitch);
                if (m >= 0)
                    Engine.getInstance().achangeState(m);
            }
        }
    }

    @Override
    public boolean drop(int reason) {
        return true; // img krovimo nestabdysim.
    }

    public interface ImagesLoaderListener{
        void onFinishLoading();
    }
}
