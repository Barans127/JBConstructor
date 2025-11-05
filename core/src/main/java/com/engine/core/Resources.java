package com.engine.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.root.PropertiesReader;
import com.engine.root.SoundController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

// statine klase. viskas static.
public final class Resources {
    // programos kalba.
//    private static StringsHolder lang;

    // image holder
//    private final static HashMap<String, Texture> generalUI = new HashMap<>();
    private final static HashMap<String, TextureRegionDrawable> texturesDrawable = new HashMap<>();

    // texture atlas holder
    private final static HashMap<String, TextureAtlas> atlas = new HashMap<>();
    private final static HashMap<String, SpriteDrawable> regionsDrawable = new HashMap<>();

    // SpriterDrawable sarasas.
    private final static HashMap<String, SpriterDrawable> spriterDrawable = new HashMap<>();

    // muzikos failai
//    private final static HashMap<String, Sound> audio = new HashMap<>();
//    private final static HashMap<String, Music> music = new HashMap<>();

    // bitmapfont failai
    private final static HashMap<String, BitmapFont> font = new HashMap<>();

    // naikinamu sarasas
    private final static ArrayList<Disposable> disposables = new ArrayList<>();

    /** program settings. */
    public final static Preferences config;

    // properties is config failo.
    private final static HashMap<String, String> propertiesHolder;

    // Sound controller. If only user needs it.
    private static SoundController soundController;

    private Resources() {
    }

    // nuskaitys config.conf faila ir paruos rezoliucija.
    static {
        propertiesHolder = new HashMap<>();
        PropertiesReader p = readProp("config.conf");
        for (String e : p.getPropertiesKeys()) {
            propertiesHolder.put(e, p.getValue(e));
        }
//        lang = new StringsHolder();
        config = Gdx.app.getPreferences(getProperty("programName", "MainConfigSettings"));
    }

    /** reads settings from given file and appends it to properties. Access them with {@link #getProperty(String, String)} */
    public static void readSettings(String fileLocation){
        PropertiesReader p = readProp(fileLocation);
        for (String e : p.getPropertiesKeys()) {
            propertiesHolder.put(e, p.getValue(e));
        }
    }

    private static PropertiesReader readProp(String path){
        FileHandle input;
        PropertiesReader prop = new PropertiesReader();
        input = Gdx.files.internal(path);
        if (!input.exists()){
            Gdx.app.log("Resources", "Settings was not read. Cannot locate file: " + path);
            return prop;
        }
        prop.load(input, true);
        return prop;
    }

	/* properties iš conf failo. Properties are READ-ONLY */

    /** grazins parametra is conf failo pagal duota rakta, neradus grazins default. */
    public static String getProperty(String key, String defaultValue) {
        if (!propertiesHolder.containsKey(key)) {
            return defaultValue;
        }
        String e = propertiesHolder.get(key);
        if (e == null)
            e = "null";
        return e;
    }

    /** gražins value iškart paversta į float. */
    public static float getPropertyFloat(String key, float defaultValue){
        String ans = getProperty(key, defaultValue+"");
        try {
            return Float.parseFloat(ans);
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }

    /** gražins value iškart paversta į int. */
    public static int getPropertyInt(String key, int defaultValue){
        String ans = getProperty(key, defaultValue+"");
        try {
            return Integer.parseInt(ans);
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }

    public static boolean getPropertyBoolean(String key, boolean defaultValue){
        String ans = getProperty(key, defaultValue+"");
        try {
            return Boolean.parseBoolean(ans);
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }

	/* image procedūros */

    // ides image su originaliu dydziu
//    public static boolean addImage(String key, String file) {
//        if (generalUI.containsKey(key)) {
//            // jei yra raktas, tai nedes vel tokio pat.
//            return false;
//        }
//        if (!MoreUtils.existFile(file)) {
//            Engine p = GdxPongy.getInstance();
//            p.setError(file, ErrorType.MissingResource);
//            return false;
//        }
//        Texture a = new Texture(Gdx.files.internal(file));
//        generalUI.put(key, a);
//        return true;
//    }

    /** Adds given texture to list.
     * If key already exists, than does nothing.
     * Error occurs if key is null or key is already defined in list. */
    public static void addTexture(String key, Texture text){
        if (key == null){
//            GdxPongy.getInstance().setError("Cannot load texture with given key: " + key, ErrorType.WrongPara);
            throw new RuntimeException("Texture key cannot be null");
        }
        if (text == null){
//            GdxPongy.getInstance().setError("Texture cannot be null", ErrorType.WrongPara);
            throw new RuntimeException("Texture cannot be null");
//            return false;
        }
        if (texturesDrawable.containsKey(key)) {
            Gdx.app.log("Resources", "Texture with given key already exists: " + key);
            return;
        }
        texturesDrawable.put(key, new TextureRegionDrawable(new TextureRegion(text)));
    }

    /** loads texture from given path with key as access key.
     * If texture key already exists - than does nothing.
     * Gives error if key is null or file doesn't exist.
     * @return just created and added to list textureRegionDrawable.*/
    public static TextureRegionDrawable loadTexture(String key, String path){
        if (key == null){
//            GdxPongy.getInstance().setError("Cannot load texture with given key: " + key, ErrorType.WrongPara);
            throw new RuntimeException("Texture key cannot be null");
//            return new TextureRegionDrawable();
        }
        if (texturesDrawable.containsKey(key)){
            Gdx.app.log("Resources", "Texture with given key already exists: " + key);
            return texturesDrawable.get(key);
        }
        FileHandle e = Gdx.files.internal(path);
        if (!e.exists()){
//            GdxPongy.getInstance().setError("Cannot locate file: " + path, ErrorType.MissingResource);
            throw new RuntimeException("Cannot locate file: " + path);
//            return new TextureRegionDrawable();
        }
        Texture text = new Texture(e);
        TextureRegionDrawable reg = new TextureRegionDrawable(new TextureRegion(text));
        texturesDrawable.put(key, reg);
        return reg;
    }

    // pasakys ar toks image jau yra.
    /** @return true if given key exists. false otherwise. */
    public static boolean containsTextureKey(String key) {
        return texturesDrawable.containsKey(key);
    }

    public static boolean containsTexture(TextureRegionDrawable e){
        return texturesDrawable.containsValue(e);
    }

    /** @return texture. If not found null. */
    public static Texture getTexture(String key){
        TextureRegionDrawable e = texturesDrawable.get(key);
        if (e == null){
//            GdxPongy.getInstance().setError("Image with given key doesn't exist: " + key, ErrorType.MissingResource);
//            return new Texture(1, 1, Pixmap.Format.RGB888);
            Gdx.app.log("Resources", "Image with given key doesn't exist: " + key);
            return null;
        }
        return e.getRegion().getTexture();
    }

    /** @return textureRegionDrawable. If not found null.  */
    public static TextureRegionDrawable getTextureDrawable(String key){
        //            GdxPongy.getInstance().setError("Image with given key doesn't exist: " + key, ErrorType.MissingResource);
        //            return new TextureRegionDrawable();
        return texturesDrawable.get(key);
    }

//    public static Texture getImage(String key) {
//        Texture a = generalUI.get(key);
//        if (a == null) {
//            GdxPongy.getInstance().setError("Image with given key doesn't exist: " + key, ErrorType.MissingResource);
//            return new Texture(10, 10, Pixmap.Format.RGB888);
//        }
//        return a;
//    }

    /** removes image from list and return it. If texture will no longer be used it should be disposed.
     * @param  dispose texture will be disposed.
     * @return removed and/or disposed texture.*/
    public static Texture removeTexture(String key, boolean dispose){
        if (texturesDrawable.containsKey(key)){
            Texture e = texturesDrawable.remove(key).getRegion().getTexture();
            if (dispose)
                addDisposable(e);
            return e;
        }
        return null;
    }

	/* image atlas procedūros */

//    /** idės paveikslelį, kuriame yra daugiau dalių tai yra texture atlas */
//    public static boolean addAtlas(String file, String key) {
//        if (atlas.containsKey(key)) {
//            return false;
//        }
//        if (!MoreUtils.existFile(file)) {
//            GdxPongy.getInstance().setError(file, ErrorType.MissingResource);
//            return false;
//        }
//        try {
//            atlas.put(key, new TextureAtlas(file));
//        } catch (RuntimeException e) {
//            GdxPongy.getInstance().setError(e.getMessage(), ErrorType.UnknowError);
//            return false;
//        }
//        return true;
//    }

//    /** add atlas. returns false if key already exist or atlas == null */
//    public static boolean addAtlas(TextureAtlas atlas, String key){
//        if (Resources.atlas.containsKey(key) || atlas == null){
//            return false;
//        }
//        Resources.atlas.put(key, atlas);
//        return true;
//    }

    /** @return true if given key exists. false otherwise. */
    public static boolean containsTextureAtlasKey(String key) {
        return atlas.containsKey(key);
    }

    public static boolean containsTextureAtlas(TextureAtlas e){
        return atlas.containsValue(e);
    }

    /**
     * Loads texture atlas from file. Throws error if file is not texture atlas file or file doesn't exist.
     * @param key Atlas key to access it later. If key already exists - does nothing.
     * @param path File path to textureAtlas.
     * @return Newly created and added to list TextureAtlas.
     */
    public static TextureAtlas addTextureAtlas(String key, String path){
        if (key == null){
            throw new RuntimeException("Key cannot be null");
        }
        if (atlas.containsKey(key)){
            return atlas.get(key);
        }
        FileHandle e = Gdx.files.internal(path);
        if (!e.exists()){
            throw new RuntimeException("Cannot locate file: " + path);
        }
        TextureAtlas at;
//        try {
        // tegul meta ta error.
        at = new TextureAtlas(e);
        addTextureAtlas(key, at);
        return at;
//        }catch (GdxRuntimeException ex){
//            GdxPongy.getInstance().setError("Cannot load TextureAtlas from given file: " + path, ErrorType.UnknowError);
//            return new TextureAtlas();
//        }
    }

    /**
     * Adds TextureAtlas to list. If key already exists - than does nothing.
     * Throws error if key or TextureAtlas is null.
     * @param key TextureAtlas key to access it later.
     * @param atlas1 TextureAtlas to add.
     */
    public static void addTextureAtlas(String key, TextureAtlas atlas1){
        if (key == null){
            throw new RuntimeException("Key cannot be null");
        }
        if (atlas.containsKey(key)) {
            Gdx.app.log("Resources", "TextureAtlas with given key already exists: " + key);
            return;
        }
        if (atlas1 == null){
//            GdxPongy.getInstance().setError("TextureAtlas cannot be null", ErrorType.WrongPara);
//            return false;
            throw new RuntimeException("TextureAtlas cannot be null");
        }
        atlas.put(key, atlas1);
        for (TextureAtlas.AtlasRegion e : atlas1.getRegions()){
//            regionsDrawable.put(e.name, new TextureRegionDrawable(new TextureAtlas.AtlasSprite(e)));
            regionsDrawable.put(e.name, new SpriteDrawable(new TextureAtlas.AtlasSprite(e)));
        }
    }

    /** @return region. If not found null. */
    public static SpriteDrawable getRegionDrawable(String region){
//        if (regionsDrawable.containsKey(region)){
            return regionsDrawable.get(region);
//        }else {
//            GdxPongy.getInstance().setError("Region with given key was not found: "+region, ErrorType.WrongPara);
//            return new TextureRegionDrawable();
//            return new SpriteDrawable();
//        }
//        return null;
    }

    public static boolean containsRegionKey(String name){
        return regionsDrawable.containsKey(name);
    }

    public static boolean containsRegion(SpriteDrawable e){
        return regionsDrawable.containsValue(e);
    }

    /**
     * grazins nurodyta region iš texture atlas. Nereikia kviest kas frame, rezultatą reiktų saugot.
     * @param atlasName Atlaso key
     * @param region regiono key
     * @return gražins regioną kaip textureRegion arba null
     */
    public static TextureRegion getRegion(String atlasName, String region) {
        if (!atlas.containsKey(atlasName)) {
//            GdxPongy.getInstance().setError("Region with given key was not found: "+region, ErrorType.WrongPara);
//            return new TextureRegion();
            return null;
        }
        return atlas.get(atlasName).findRegion(region);
    }

    /** Creates nine patch from existing resources. Only texture or texture atlas resource can be used.
     * @throws RuntimeException if resource is not texture or from texture atlas. */
    public static NinePatchDrawable createNinePatchDrawable(String drawableId, int left, int right, int top, int bottom){
        Drawable e = getDrawable(drawableId);
        if (e == null){
            throw new RuntimeException("Drawable with given id not found: " + drawableId);
        }
        if (e instanceof SpriterDrawable){
            throw new RuntimeException("Drawable cannot be instance of SpriterDrawable");
        }
        if (e instanceof NinePatchDrawable){
//            throw new GdxRuntimeException("Drawable is already a NinePatch");
            // tai tiesiog grazinam nauja nine patcha, kam ta error?
            return new NinePatchDrawable((NinePatchDrawable) e);
        }
        TextureRegion region = null;
        if (e instanceof TextureRegionDrawable){
            region = ((TextureRegionDrawable) e).getRegion();
        }else if (e instanceof SpriteDrawable){
            region = new TextureRegion(((SpriteDrawable) e).getSprite());
        }
        if (region == null){
            throw new RuntimeException("Failed to get region");
        }
        NinePatch patch = new NinePatch(region, left, right, top,bottom);
        return new NinePatchDrawable(patch);
    }

    /** This will create NinePatch. Result should be caught and stored instead of calling it each time.
     * NinePatch must be defined in Atlas before creating NinePatch.
     * Throws error if region is not ninePatch.
     * @return Newly created ninePatch.*/
    public static NinePatch getNinePatch(String atlasName, String region){
        if (!atlas.containsKey(atlasName)) {
            return null;
        }
        return atlas.get(atlasName).createPatch(region);
    }

    /** get sprite. If not found null.
     * Result should be caught instead of calling it each time. */
    public static Sprite getSprite(String atlasName, String region){
        if (!atlas.containsKey(atlasName)) {
//            GdxPongy.getInstance().setError("Region with given key was not found: "+region, ErrorType.WrongPara);
//            return new Sprite();
            return null;
        }
        return atlas.get(atlasName).createSprite(region);
    }

    /** removes TextureAtlas from list and return it. If Atlas will no longer be used it should be disposed.
     * @param dispose true atlas will be disposed.
     * @return removed and/or disposed TextureAtlas. if atlas with given key not found null is returned */
    public static TextureAtlas removeAtlas(String key, boolean dispose){
        if (atlas.containsKey(key)){
            TextureAtlas e = atlas.remove(key);
            Array<TextureAtlas.AtlasRegion> reg = e.getRegions();
            Array<String> removeList = new Array<>();
            for (int a = reg.size-1; a >= 0; a--){ // surenkam visus atlaso regionus, kurie ideti i kita hash map.
                for (String name : regionsDrawable.keySet()){
                    if (reg.get(a).name.equals(name)){
                        removeList.add(name);
                    }
                }
            }
            for (String name : removeList){ // pasalinam tuos regionus.
                regionsDrawable.remove(name);
            }
            if (dispose)
                addDisposable(e);
            return e;
        }
        return null;
    }

    /** @return atlas by id. if atlas doessn't exit null is returned. */
    public static TextureAtlas getAtlas(String key){
        if (atlas.containsKey(key)){
            return atlas.get(key);
        }
        return null;
    }

    /** Gives no error if resource with given id is not found.
     * @return checks if drawable is from texture. if not found then checks from atlases regions. if not found null is returned.*/
    public static Drawable getRegionRecursive(String id){
        if (texturesDrawable.containsKey(id)){
            return texturesDrawable.get(id);
        }
        if (regionsDrawable.containsKey(id)){
            return regionsDrawable.get(id);
        }
//        GdxPongy.getInstance().setError("Resource with given key was not found: " + id, ErrorType.MissingResource);
        return null;
    }

    /** checks from all resources with given key. First check if textures has it. Than check if regions have it. Then animations.
     * If drawable with key in those list was not found then null is returned. Return null if resource not found. */
    public static Drawable getDrawable(String key){
        Drawable e = getRegionRecursive(key);
        if (e == null){
            if (containsAnimationKey(key)){
                return getAnimation(key);
            }
        }
//        if (e == null){ // test purposes.
//            Gdx.app.log("Resources", "Drawable was not found with id: " + key);
//            throw new RuntimeException("Bad key: " + key);
//        }
//        if (e == null)
//            GdxPongy.getInstance().setError("Resource with given key was not found: " + key, ErrorType.MissingResource);
        return e;
    }

    /** Removes drawable with given key from list and disposes it. Searches in texture list, animation list and texture atlas list.
     * NOTE: It will not dispose region as it is part of atlas. You need to provide atlas key to dispose region. Disposing atlas will
     * dispose all regions associated with disposed atlas.*/
    public static void removeDrawable(String key){
//        Gdx.app.log("Engine","Searching drawable(s) with id: " + key + " ...");
//        if (containsTextureKey(key)){
        Texture a = removeTexture(key, true);
//        }
//        if (containsAnimationKey(key)){
        SpriterDrawable dr = removeAnimation(key, true);
//        }
//        if (containsTextureAtlasKey(key)){
        TextureAtlas at = removeAtlas(key, true);
//        }
        boolean removed = false;
        if (a != null){
            Gdx.app.log("Engine","Texture with id '" + key + "' was removed and disposed");
            removed = true;
        }
        if (dr != null){
            Gdx.app.log("Engine","Animation with id '" + key + "' was removed and disposed");
            removed = true;
        }
        if (at != null){
            Gdx.app.log("Engine","TextureAtlas with id '" + key + "' was removed and disposed");
            removed = true;
        }
        if (!removed){
            Gdx.app.log("Engine","No drawable found with id: " + key + ". Nothing was disposed.");
        }
        // That was just annoying and useless text.
//        String text;
//        if (removed) {
//            text = "Removing and disposing drawable(s) completed.";
//        }else {
//            text = "Drawable with id: "+ key +", was not found.";
//        }
//        Gdx.app.log("Engine", text);
    }

    /* spriter */

    /** Adds given animation to list.
     * If key already exists - than does nothing.
     * Error occurs if key is null */
    public static void addAnimation(String key, SpriterDrawable e){
        if (key == null){
            throw new RuntimeException("Given key already exists: " + key);
//            return false;
        }
        if (spriterDrawable.containsKey(key)){
            Gdx.app.log("Resources", "Animation with given key already exists: " + key);
            return;
        }
        if (e == null){
//            GdxPongy.getInstance().setError("Animation cannot be null", ErrorType.WrongPara);
//            return false
//            ;
            throw new RuntimeException("SpriterDrawable cannot be null");
        }
        spriterDrawable.put(key, e);
    }

    /** loads animation from given path with key as access key.
     * Gives error if key is null.
     * If animation key already exists - than does nothing.
     * If file is not spriter animation file than throws error.
     * @param atlas - if animation uses atlas resources. null if not from atlas
     * @return just created and added to list animation.*/
    public static SpriterDrawable loadAnimation(String key, String path, String atlas){
        if (key == null){
            throw new RuntimeException("Given key already exists: " + key);
        }
        if (spriterDrawable.containsKey(key)){
            Gdx.app.log("Resources", "Animation with given key already exists: " + key);
            return spriterDrawable.get(key);
        }
        FileHandle e = Gdx.files.internal(path);
        if (!e.exists()){
//            GdxPongy.getInstance().setError("Cannot locate file: " + path, ErrorType.MissingResource);
//            return new SpriterDrawable("", "", true);
            throw new RuntimeException("Cannot locate file: " + path);
        }
        SpriterDrawable drawable;
//        try {
        // lai meta ta error.
        drawable = new SpriterDrawable(path, atlas);
//        }catch (SpriterException ex){
//            GdxPongy.getInstance().setError("Cannot load animation form file: " + path, ErrorType.UnknowError);
//            return new SpriterDrawable("", "", true);
//        }
        spriterDrawable.put(key, drawable);
        return drawable;
    }

    // pasakys ar toks image jau yra.
    /** @return true if given key exists. false otherwise. */
    public static boolean containsAnimationKey(String key) {
        return spriterDrawable.containsKey(key);
    }

    public static boolean containsAnimation(SpriterDrawable e){
        return spriterDrawable.containsValue(e);
    }

    /** @return animation. Return null if animation not found. */
    public static SpriterDrawable getAnimation(String key){
        return spriterDrawable.get(key);
    }

    /** removes animation from list and return it. If animation will no longer be used it should be disposed.
     * @param  dispose animation will be disposed.
     * @return removed and/or disposed animation.*/
    public static SpriterDrawable removeAnimation(String key, boolean dispose){
        if (spriterDrawable.containsKey(key)){
            SpriterDrawable e = spriterDrawable.remove(key);
            if (dispose)
                addDisposable(e);
            return e;
        }
        return null;
    }

    /* muzikos failai */

    /** Initializes SoundController and holds its instance.
     * Disposes automatically when application is closed.
     * Convenient to use if app is planning to use only one SoundController instance in whole app
     * life.
     * @return SoundController instance. If it was initialized before than returns instance and doesn't
     * create new instance. */
    public static SoundController initializeSoundController(){
        if (soundController == null){
            soundController = new SoundController();
        }
        return soundController;
    }

    /** SoundController instance. SoundController must be initialized first before using this
     * method.
     * @return instance of SoundController or null if SoundController was not initialized.*/
    public static SoundController getSoundController(){
        return soundController;
    }


    // sound.
    // Sound moved to SoundController.

//    /** Adds given sound to sound list. */
//    public static void addSound(String key, Sound audio){
//        if (audio == null){
////            GdxPongy.getInstance().setError("audio cannot be null", ErrorType.WrongPara);
////            return;
//            throw new RuntimeException("audio cannot be null");
//        }
//        if (key == null || key.length() == 0){
////            GdxPongy.getInstance().setError("key cannot be null or zero length", ErrorType.WrongPara);
////            return;
//            throw new RuntimeException("key cannot be null or zero length");
//        }
//        if (Resources.audio.containsKey(key)){
////            GdxPongy.getInstance().setError("Key already exists: " + key, ErrorType.WrongPara);
////            return;
//            throw new RuntimeException("Key already exists: " + key);
//        }
//        Resources.audio.put(key, audio);
//    }

//    /** loads sound from given path. */
//    public static void loadSound(String key, String path){
//        FileHandle file = Gdx.files.internal(path);
//        if (!file.exists()){
////            GdxPongy.getInstance().setError("Cannot locate file: " + path, ErrorType.MissingResource);
////            return;
//            throw new RuntimeException("Cannot locate file: " + path);
//        }
////        try {
//        boolean normalLoad = getPropertyInt("enableSounds", 1) == 1;
//
//        Sound e;
//        if (normalLoad) {
//            e = Gdx.audio.newSound(file);
//        }else {
//            e = new NoAudio.NoSound();
//        }
////            Sound e = new TemporaryWorkAround(); // ant ios kai leidziama per mac, ten garso driveriai nesutvarkyti, todel reik nekraut isvis garso.
//        addSound(key, e);
////        }catch (GdxRuntimeException ex){
////            GdxPongy.getInstance().setError("Cannot load sound file: " + ex.getMessage(), ErrorType.UnknowError);
////        }
//    }

//    /** @return if sound list contains such key value. */
//    public static boolean containsSoundKey(String key){
//        return audio.containsKey(key);
//    }
//
//    /** @return sound instance or null if not found. Error occurs if sound was not found. */
//    public static Sound getSound(String key){
//        //        if ( e == null ){
////            GdxPongy.getInstance().setError("Cannot locate sound with given key: " + key, ErrorType.MissingResource);
////        }
//        return audio.get(key);
//    }

//    /** Removes sound from sound list. If sound no longer needed then it should be disposed.
//     * @param dispose if true then sound will be disposed.
//     * @return sound which was removed and/or disposed.*/
//    public static Sound removeSound(String key, boolean dispose){
//        Sound e = audio.get(key);
//        if (e != null && dispose){
//            addDisposable(e);
//        }
//        return e;
//    }

    // music instances

//    /** Adds given music to music list. */
//    public static void addMusic(String key, Music music){
//        if (music == null){
////            GdxPongy.getInstance().setError("Music cannot be null", ErrorType.WrongPara);
////            return;
//            throw new RuntimeException("Music cannot be null");
//        }
//        if (key == null || key.length() == 0){
////            GdxPongy.getInstance().setError("Key for music cannot be null or zero length", ErrorType.WrongPara);
////            return;
//            throw new RuntimeException("Key for music cannot be null or zero length");
//        }
//        if (Resources.music.containsKey(key)){
////            GdxPongy.getInstance().setError("Key already defined in music: " + key, ErrorType.WrongPara);
////            return;
//            throw new RuntimeException("Key already exists: " + key);
//        }
//        Resources.music.put(key, music);
//    }

//    /** Loads music from given path. Internal path is used. */
//    public static void loadMusic(String key, String path){
//        FileHandle file = Gdx.files.internal(path);
//        if (!file.exists()){
////            GdxPongy.getInstance().setError("Cannot locate file: " + path, ErrorType.MissingResource);
////            return;
//            throw new RuntimeException("Cannot locate file: " + path);
//        }
////        try {
//        boolean normalLoad = getPropertyInt("enableSounds", 1) == 1;
//        Music e;
//        if (normalLoad) {
//            e = Gdx.audio.newMusic(file);
//        }else {
//            e = new NoAudio.NoMusic();
//        }
//        addMusic(key, e);
////        }catch (GdxRuntimeException ex){
////            GdxPongy.getInstance().setError("Cannot load music: " + ex.getMessage(), ErrorType.UnknowError);
////        }
//    }

//    /** @return if music contains given key. */
//    public static boolean containsMusicKey(String key){
//        return music.containsKey(key);
//    }

//    /** @return music instance. if not found null is returned. error occurs if music not found. */
//    public static Music getMusic(String key){
//        //        if (e == null){
////            GdxPongy.getInstance().setError("Music with given key was not found: " + key, ErrorType.MissingResource);
////        }
//        return music.get(key);
//    }

//    /** Removes music from music list and/or dispose it. You should dispose music if it is no longer needed.
//     * @param dispose disposes music.
//     * @return music which was removed from list and/or disposed.*/
//    public static Music removeMusic(String key, boolean dispose){
//        Music e = music.remove(key);
//        if (e != null && dispose){
//            addDisposable(e);
//        }
//        return e;
//    }

    /* font handle */

    public static boolean containsFontKey(String key){
        return font.containsKey(key);
    }

    public static boolean containsFont(BitmapFont e){
        return font.containsValue(e);
    }

    /** Add font to resources. Convenient method to create font is to use {@link Engine#generateFont(String, int)} method. */
    public static void addFont(String key, BitmapFont e){
        if (key == null || key.isEmpty()){
//            Engine.getInstance().setError("Resources: key for font cannot be null or zero length", ErrorType.WrongPara);
            throw new RuntimeException("Resources: key for font cannot be null or zero length");
//            return;
        }else if (e == null){
//            Engine.getInstance().setError("Resources: Bitmapfont cannot be null", ErrorType.WrongPara);
//            return;
            throw new RuntimeException("Resources: Bitmapfont cannot be null");
        }
        if (containsFontKey(key)){
//            Engine.getInstance().setError("Resources: Font key already exists. Key: " + key, ErrorType.WrongPara);
//            return;
//            throw new RuntimeException("Resources: Font key already exists. Key: " + key);
            Gdx.app.log("Resources", "Font key already exists.");
            return;
        }
        font.put(key, e);
    }

    /** Removes font from list. If font will not be used anymore then it should be disposed
     * @return font which is removed from list. */
    public static BitmapFont removeFont(String key,  boolean dispose){
        BitmapFont e = font.remove(key);
        if (e != null && dispose){
            Resources.addDisposable(e);
        }
        return e;
    }

    /** @return font. If not found null. */
    public static BitmapFont getFont(String key){
        return font.get(key);
    }

	/* kalbos procedūros */

//    // kalbos nustatymai
//    public static void loadLang() {
//        lang.loadLanguage();
//    }

//    /**
//     * kalbos sąrašas
//     * @return gražina esamų kalbų sąrašą.
//     */
//    public static String[] getLanguageList(){
//        return lang.getLanguageList();
//    }

//    /**
//     * kalbų failai
//     * @return graažiną sąraša failų, kuriuose yra aprašytos kalbos.
//     */
//    public static String[] getLanguagesResources(){
//        return lang.getLanguageListResources();
//    }

//    /**
//     * pasirinkta kalba
//     * @return gražina pasirinktos kalbos indexą.
//     */
//    public static int getSelectedLanguageIndex(){
//        return lang.getSelectedLanguageIndex();
//    }

//    public synchronized static String getString(int index) {
//        return lang.getString(index);
//    }

	/* image naikinimas naudojant main thread */

    // istrins visus image.
    /** Disposes all images and becomes empty. Must be called from main Thread. */
    public static void dispose() {
        { // paprasti image.
            Set<String> key = texturesDrawable.keySet();
            for (String a : key) {
                texturesDrawable.get(a).getRegion().getTexture().dispose();
            }
            texturesDrawable.clear();
        }
        { // atlas image
            Set<String> key = atlas.keySet();
            for (String a : key) {
                atlas.get(a).dispose();
            }
            atlas.clear();
            regionsDrawable.clear(); // sito disposint nereik, nes tai tik regionai atlase.
        }
        { // animuoti.
            Set<String> key = spriterDrawable.keySet();
            for (String a : key){
                spriterDrawable.get(a).dispose();
            }
            spriterDrawable.clear();
        }
//        { // sound efektai.
//            Set<String> key = audio.keySet();
//            for (String a : key){
//                audio.get(a).dispose();
//            }
//            audio.clear();
//        }
//        { // muzikos.
//            Set<String> key = music.keySet();
//            for (String a : key){
//                music.get(a).dispose();
//            }
//            music.clear();
//        }
        { // Sound controller.
            if (soundController != null){
                soundController.dispose();
                soundController = null;
            }
        }
        {// fontai
            Set<String> key = font.keySet();
            for (String a : key){
                BitmapFont font = Resources.font.get(a);
                // jeigu cia free type, tai disposinam.
                // Dunno Why this was used. Now it fails to dispose. It only works if there
                // is nothing to dispose! Removing. In documentation there is nothing about it,
                // so most likely it is not used.
                // Most likely disposing free type generator is enough. It is done elsewhere so no
                // need to worry about it.
//                if (font.getData() instanceof FreeTypeFontGenerator.FreeTypeBitmapFontData){
//                    ((FreeTypeFontGenerator.FreeTypeBitmapFontData) font.getData()).dispose();
//                }
                font.dispose();
            }
            font.clear();
        }
        synchronized (disposables) {
            for (int a = disposables.size() - 1; a >= 0; a--) {
                Disposable disposable = disposables.get(a);
                // Again breaking app closing if this dispose method is used.
//                if (disposable instanceof BitmapFont){
//                    // atskirai disposinam free type.
//                    BitmapFont.BitmapFontData data = ((BitmapFont) disposable).getData();
//                    if (data instanceof FreeTypeFontGenerator.FreeTypeBitmapFontData){
//                        ((FreeTypeFontGenerator.FreeTypeBitmapFontData) data).dispose();
//                    }
//                }
                disposable.dispose();
                disposables.remove(a);
            }
        }
    }

    /* nereikalingu daiktu disposinimas */

    /* Calls everytime after draw method, but before fixDraw. */
    static void disposeDisposables() {
        if (!disposables.isEmpty()) {
            synchronized (disposables){
                Disposable disposable = disposables.get(0);
//                if (disposable instanceof BitmapFont) {
//                    BitmapFont.BitmapFontData data = ((BitmapFont) disposable).getData();
//                    if (data instanceof FreeTypeFontGenerator.FreeTypeBitmapFontData) {
//                        ((FreeTypeFontGenerator.FreeTypeBitmapFontData) data).dispose();
//                    }
//                }
                disposable.dispose();
                disposables.remove(0);
            }
        }
    }

    /** Dispose any disposable. Dispose occurs in main thread. Can be called from any thread. */
    public static void addDisposable(Disposable e) {
        if (e == null)
            return;
        synchronized (disposables) {
            disposables.add(e);
        }
    }
}
