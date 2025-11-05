package com.engine.root;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.engine.core.Engine;
import com.engine.core.ErrorHandler;
import com.engine.core.ErrorMenu;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;

public abstract class GdxPongy extends ApplicationAdapter {
    // main lib gdx
    private SpriteBatch batch;
    private CustomShapeRenderer g;
    private Viewport view, fixedView;
    private OrthographicCamera absoluteCamera, fixedCamera;
    private BitmapFont font;
    private int fontDefaultSize; // default font size.
    private GlyphLayout layout;
    private Color systemColor;
    private final Vector3 coordTranslator = new Vector3();
//    private static final int frameRate = 60;

    // Scissor
    private final Rectangle tmpScissorArea = new Rectangle();
    private final Pool<Rectangle> scissorPool;
    private int scissorsEntry = 0;

    // draw settings.
    // gerai sueina testams.
    private float sr, sg, sb, sa = 1; // stroke colors
    private float fr, fg, fb, fa = 1; // fill colors
    private boolean isFill = true, isStroke = true;
    private float strokeWeight = 1;
    private boolean isTint;
    private final Color forceTint = new Color(1,1,1,1); // balta.
    private boolean forceColorHasChanged = false; // TextureContaineriui.
    private int currentActiveCamera; // 0 - absolute, 1 - fixed.

    //System.
    private long startTime; // start times to count for anything if needed... Used in millis method.
//    private int width, height; // dydis pritaikius rezoliucija. Atsisakyta, del libgdx cameros.
    private float worldSizeX, worldSizeY; // default world matomo lango dydis.
//    public boolean keyPressed, mousePressed;
//    public int mouseButton, keyCode;
//    private static Engine main; // nop keliam i engine.
    private static float worldSpeed = 1;
    private static final Array<Disposable> disposables = new Array<>();
    private final Array<PauseListener> pauseListeners = new Array<>();

    // mode settings
    private int ellipseMode = Align.center, rectMode = Align.left;
//    private float translateX, translateY;
//    private float rotate;
    private int alignX = Align.left, alignY = Align.top;

    // distance shader del teksto. Tekstas daug geriau atrodo su juo.
    private DistanceFieldShader distanceFieldShader;
//    private ShaderProgram distanceFieldShader;
    private float textSmoothing = 0.5f;

    protected GdxPongy() {
        scissorPool = new Pool<Rectangle>() {
            @Override
            protected Rectangle newObject() {
                return new Rectangle();
            }
        };
    }

    /* su engine susije. tiesiog senesniam kodui. */

//    /** prepares main instace */
//    public static Engine initialize(StartListener list){
//        return Engine.initialize(list);
//    }

    /** Main programs instance. If it was not initialized then null. */
    public static Engine getInstance() {
//        if (main == null) // turetu tik viena kart ikelt.
//            main = new Engine();
        return Engine.getInstance();
    }

    /* toliau viskas su libgdx ko reik. */

    /** add given listener to list. null will not be added to list. */
    public void addPauseListener(PauseListener e){
        if (e != null){
            pauseListeners.add(e);
        }
    }

    /** removes given listener from list. */
    public void removePauseListener(PauseListener e){
        pauseListeners.removeValue(e, false);
    }

    /** Removes all pause listeners from list. */
    public void clearPauseListeners(){
        pauseListeners.clear();
    }

    /** add disposables to disposable list. This list will be disposed when program exits. */
    public static void addDisposable(Disposable... e){
        for (Disposable a : e){
            addDisposable(a);
        }
    }

    /** add disposable to disposable list. This list will be disposed when program exits. */
    public static void addDisposable(Disposable e){
        if (e != null) // nepridedam null.
            disposables.add(e);
    }

    /** if you want to remove disposable from list and dispose it now you can do it here. Safe to dispose, disposing will occur on main thread.
     * @param dispose it will be removed from list and disposed. If given element doesn't belong to list, it will be disposed anyway. */
    public static void removeDisposable(Disposable e, boolean dispose){
        disposables.removeValue(e, false);
        if (dispose){
            Resources.addDisposable(e);
        }
    }

    @Override
    public void create() {
        // error handleris.
        ErrorHandler errorHandler = new ErrorHandler();
        Thread.currentThread().setUncaughtExceptionHandler(errorHandler);
        Thread.setDefaultUncaughtExceptionHandler(errorHandler);

        // pasizymim kada prasidejo prgrama.
        startTime = TimeUtils.millis();

        // world size nustatymas.
        try{
            worldSizeX = Integer.parseInt(Resources.getProperty("worldSizeX", "1280"));
            worldSizeY = Integer.parseInt(Resources.getProperty("worldSizeY", "720"));
        }catch (NumberFormatException e){
            worldSizeX = 1280;
            worldSizeY = 720;
        }

        // kameros ir viewport kurimas.
//        width = (int) Engine.getWithRez(worldSizeX);
//        height = (int) Engine.getWithRez(worldSizeY);
        absoluteCamera = new OrthographicCamera();
        absoluteCamera.position.set(worldSizeX/2, worldSizeY/2, 0);
        absoluteCamera.update();
        fixedCamera = new OrthographicCamera(worldSizeX, worldSizeY);
        fixedCamera.position.set(worldSizeX/2, worldSizeY/2, 0);
        fixedCamera.update();

        // nustatom fixed kameros world matoma dydi.
//        fixedCamera.viewportWidth = worldSizeX;
//        fixedCamera.viewportHeight = worldSizeY;
//        view = new StretchViewport(width, height, absoluteCamera);
        float maxX = Math.max(worldSizeX, Resources.getPropertyFloat("maxWorldSizeX", 0));
        float maxY = Math.max(worldSizeY, Resources.getPropertyFloat("maxWorldSizeY", 0));
        view = new ExtendViewport(worldSizeX, worldSizeY, maxX, maxY, absoluteCamera);
        // This is used to create default font. Without it we would get division by zero when
        // creating new font as it uses this params, so just set those parameters to not divide by 0.
        view.setWorldSize(worldSizeX, worldSizeY);

        fixedView = new ExtendViewport(worldSizeX, worldSizeY, maxX, maxY, fixedCamera);
        fixedView.setWorldSize(worldSizeX, worldSizeY);
//        view = new ScreenViewport(absoluteCamera);
        InputMultiplexer e = new InputMultiplexer(new InputTranslator(this), new GestureDetector(new GestureTranslator(this)));
        Gdx.input.setInputProcessor(e);

        // bandysim texto dydi keist pagal ekrano.
        // bandom det default fonta jei toks yra.
        String fontLocation = Resources.getProperty("defaultFont", null);
        // loadinam fonta tik jeigu jis nustatytas.
        if (fontLocation != null && !fontLocation.equals("null")) {
            int preferredTextSize = Resources.getPropertyInt("defaultFontSize", 120);
            BitmapFont font = generateFont(fontLocation, preferredTextSize);
            Resources.addFont("defaultFont", font); // idedam i resources saugiai, kad veliau pasalintu jeigu kas.
            setDefaultFont(font);
        }

        // layout.
        layout = new GlyphLayout();
        // system tmp color.
        systemColor = new Color();

        // nustatom matricas, piesimui reikalingus dalykus.
        batch = new SpriteBatch();
        g = new CustomShapeRenderer(this);
        batch.setProjectionMatrix(absoluteCamera.combined);
        g.setProjectionMatrix(absoluteCamera.combined);

//        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        // sukuriam balta spalva. prie jos prieisim su "whiteSystemColor" .
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.drawPixel(0, 0, 0xFFFFFFFF);
        Texture white = new Texture(pix);
        Resources.addTexture(Resources.getProperty("whiteColor", "whiteSystemColor"), white);
        pix.dispose(); // atsikratom pixmap.

        // uzkraunam musu distance field shaderi.
        distanceFieldShader = new DistanceFieldShader();

        setup(); // paleidziam engine setupa.

//        distanceFieldShader = DistanceFieldFont.createDistanceFieldShader();
//        distanceFieldShader.setUniformf("u_smoothing", 0.5f);
    }

    /** generate font from .ttf file with given size and default characters. */
    public static BitmapFont generateFont(String resource, int fontSize){
//        return generateFont(Gdx.files.internal(Resources.getProperty("fontsLocation", "resources/fonts") + "/" + resource), fontSize);
        FileHandle e = Gdx.files.internal(resource);
        if (!e.exists()){
//            if (Engine.getInstance().getFont() == null){
//                throw new RuntimeException("Default font file was not located: " + resource);
//            }else {
//                Engine.getInstance().setError("Cannot locate font file: " + resource, ErrorMenu.ErrorType.MissingResource);
//                return null;
//            }
            throw new RuntimeException("Cannot locate file: " + resource);
        }
        return generateFont(e, fontSize);
    }

    /** generate font from .ttf file with given size and default characters. */
    public static BitmapFont generateFont(FileHandle resource, int fontSize){
        String characters = " ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.-,;:()[]{}<>|/\\^$@%+=#_&~*"
                + "ĄČĘĖĮŠŲŪŽ"
                + "ąčęėįšųūž";
        return generateFont(resource, fontSize, characters);
    }

    /** Generates free font. */
    public static BitmapFont generateFont(FileHandle resource, int fontSize, String characters){
        FreeTypeFontParameter par = new FreeTypeFontParameter();
        par.size = fontSize;
        par.characters = characters;
        return generateFont(resource, par);
    }

    /** Generates font. Size is changed by screen size but you must set it before passing the parameters. filter is changed mag: linear, min - mipmap linear nearest.
     * mip map is generated.
     * If incremental is chosen then free type font generator is not disposed and added to {@link #addDisposable(Disposable)} list. This list is disposed automatically
     * when program is closed. You can manually check this list and dispose this generator from here.*/
    public static BitmapFont generateFont(FileHandle resource, FreeTypeFontParameter par){
        if (!resource.exists()){
//            if (Engine.getInstance().getFont() == null){
//                throw new RuntimeException("Default font file was not located: " + resource);
//            }else {
//                Engine.getInstance().setError("Cannot locate font file: " + resource, ErrorMenu.ErrorType.MissingResource);
//                return null;
//            }
            throw new RuntimeException("Cannot locate font file: " + resource);
        }
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(resource);
//        FreeTypeFontParameter par = new FreeTypeFontParameter();
//        par.size = fontSize;
        // sugaudom dydi pagal ekrano dydi
        int fontSize = par.size;
        int rTextSize;
        int screenX = Gdx.graphics.getWidth(), screenY = Gdx.graphics.getHeight(); // paimam fizini ekrano dydi
        // padalinam is musu world dydzio.
        // taip geriau tekstas atrodo skirtingom rezoliucijom.
        float scaleX = screenX / GdxPongy.getInstance().getScreenWidth(), scaleY = screenY / GdxPongy.getInstance().getScreenHeight();
        if (getLengthFromOne(scaleX) > getLengthFromOne(scaleY)){ // kuris didesni ta ir imsim
            rTextSize = (int) (fontSize * scaleX);
            if (par.borderWidth > 0){
                par.borderWidth = Math.max(1, par.borderWidth*scaleX);
            }
        }else {
            rTextSize = (int) (fontSize * scaleY);
            if (par.borderWidth > 0){
                par.borderWidth = Math.max(1, par.borderWidth*scaleY);
            }
        }
        par.size = rTextSize; // naujas perskaiciuotas dydis pagal musu ekrana.

        // naudosim musu shaderi, todel tokiu filter setting reik...
        par.magFilter = TextureFilter.Linear;
//        par.magFilter = TextureFilter.MipMapLinearLinear;
//        par.minFilter = TextureFilter.Linear;
        par.minFilter = TextureFilter.MipMapLinearNearest;
        par.genMipMaps = true;

        BitmapFont font = generator.generateFont(par);
        font.setUseIntegerPositions(false);
        if (par.incremental){
            // kadanagi incremental, tai mes dar ji naudosim.
            disposables.add(generator); // ji reiks disposint, tai metam i si lista.
        }else {
            // nebereik sito.
            generator.dispose();
        }
        return font;
    }

    /** length from 1. */
    private static float getLengthFromOne(float scale){
        // cia ilgis nuo 1. Paskaiciuoja koks ilgis nuo 1.
        if (scale > 1){
            return scale - 1f;
        }else {
            return 1f - scale;
        }
    }

     /** Delta time times world speed. Use this inside game logic. This way you will
      * be able to manipulate world speed with {@link  #setWorldSpeed(float)} method.
      * For interface animations or stuff which is not inside game logic better to use
      * {@link Gdx#graphics#getDeltaTime()} method.
      *  @return Delta time times world speed. */
    public static float getDelta() {
        return Gdx.graphics.getDeltaTime() * worldSpeed;
    }

    /** Manipulates {@link #getDelta()}. You can slow it down or fast it up. Value 1 - means normal speed, 0.5 - half speed. */
    public static void setWorldSpeed(float speed) {
        if (speed < 0)
            return;
        worldSpeed = speed;
    }

    /** Resets world speed to 1. */
    public static void resetWorldSpeed() {
        worldSpeed = 1;
    }

    /** Current world speed. By default it is 1. */
    public static float getWorldSpeed() {
        return worldSpeed;
    }

    // grazins default frame skaiciu, kuris turetu but.
//    public static int getFrameRate() {
////        return frameRate;
////    }

    /**
     * Absolute camera viewport.
     */
    public Viewport getView() {
        return view;
    }

    /** default fontas. */
    public BitmapFont getFont(){
        return font;
    }

    /** original size of default font. */
    public int getDefaultFontSize(){
        return fontDefaultSize;
    }

    /** set default font. This font will be used everywhere in app where font is not defined.
     * if old font will not be used anymore then it should be disposed. If font was loaded
     * inside Engine then it can be accessed from {@link Resources} with key: defaultFont (NOTE: This only applies if
     * font was loaded from config defined font!).
     * @return font which was used before new font. If default font was not set before then null.*/
    public BitmapFont setDefaultFont(BitmapFont font){
        if (font == null){
//            Engine.getInstance().setError("Engine: font cannot be null", ErrorMenu.ErrorType.WrongPara);
//            return this.font;
            throw new NullPointerException("Font cannot be null");
        }
        BitmapFont old = this.font;
        this.font = font;
        font.getData().setScale(1f);
//        fontDefaultSize = MathUtils.round(font.getCapHeight() * 1.4848f); // turetu padaryt default dydį.
        // geriausias variantas, nes tiksliai atspindi teksto dydi per skirtingas rezoliucijas.
        fontDefaultSize = (int) font.getLineHeight();
//        if (dispose){
//            if (old != null){
//                if (Resources.containsFont(old)){
//                    Resources.removeFont()
//                }else
//                    Resources.addDisposable(old);
//            }
//        }
        return old;
    }

    /**
     * programos kamera, kuri leidžia judėjimą.
     */
    public OrthographicCamera getAbsoluteCamera() {
        return absoluteCamera;
    }

    /** Fixed camera. This should not be moved or changed in any way. */
    public OrthographicCamera getFixedCamera(){
        return fixedCamera;
    }

    /** Current active camera (absolute or fixed). Depends where render method is rendering right now (first absolute rendering occurs then fixed).
     * If this method is called outside render then fixed camera will be returned. */
    public OrthographicCamera getActiveCamera(){
        return currentActiveCamera == 0 ? absoluteCamera : fixedCamera;
    }

    /**
     * translateOrigin screen coordinates to world coordinates
     * @param screenCoords vector with screen coordinates
     * @return same vector with changed values.
     */
    public Vector3 screenToWorldCoords(Vector3 screenCoords){
        return absoluteCamera.unproject(screenCoords, view.getScreenX(), view.getScreenY(), view.getScreenWidth(), view.getScreenHeight());
    }

    /**
     * translates y from top left corner to bottom left corner
     * @param screenCoords screen coordinates
     * @return vector with changed values.
     */
    public Vector3 screenToFixedCoords(Vector3 screenCoords){
        return fixedCamera.unproject(screenCoords, view.getScreenX(), view.getScreenY(), view.getScreenWidth(), view.getScreenHeight());
    }

    /** @return vector with fixed coordinates. */
    public Vector3 screenToFixedCoords(float screenX, float screenY){
        return fixedCamera.unproject(coordTranslator.set(screenX, screenY, 0), view.getScreenX(), view.getScreenY(), view.getScreenWidth(), view.getScreenHeight());
    }

    /** @return vector with world coordinates */
    public Vector3 screenToWorldCoords(float screenX, float screenY){
        return absoluteCamera.unproject(coordTranslator.set(screenX, screenY, 0), view.getScreenX(), view.getScreenY(), view.getScreenWidth(), view.getScreenHeight());
    }

    /** @return vector with screen coordinates. y-axis pointing upwards. If you want to use it again
     * and translate to world coordinates you will have to do this: getScreenHeight() - y. */
    public Vector3 worldToScreenCoords(float worldX, float worldY){
        return absoluteCamera.project(coordTranslator.set(worldX, worldY, 0), view.getScreenX(), view.getScreenY(), view.getScreenWidth(), view.getScreenHeight());
    }

    /** @return vector with screen coordinates. y-axis pointing upwards. . If you want to use it again
     * and translate to world coordinates you will have to do this: getScreenHeight() - y.*/
    public Vector3 fixedToScreendCoords(float fixedX, float fixedY){
        return fixedCamera.project(coordTranslator.set(fixedX, fixedY, 0), view.getScreenX(), view.getScreenY(), view.getScreenWidth(), view.getScreenHeight());
    }

    /** Translates absolute coordinates to fixed coordinates. */
    public Vector3 worldToFixedCoords(float absoluteX, float absoluteY){
        Vector3 e = worldToScreenCoords(absoluteX, absoluteY);
        e = screenToFixedCoords(e.x, Gdx.graphics.getHeight() - e.y); // verciam cords. reik mum fixed. BUG fix reik naudot pixel size, ne world.
        return e;
    }

    /** Translates fixed coordinates to absolute coordinates. */
    public Vector3 fixedToWorldCoords(float fixedX, float fixedY){
        Vector3 e = fixedToScreendCoords(fixedX, fixedY);
        e = screenToWorldCoords(e.x, Gdx.graphics.getHeight() - e.y);
        return e;
    }

    /** grazins naudojama batcha rendirinimui */
    public SpriteBatch getBatch() {
        return batch;
    }

    /** Shape renderer used to draw lines, rects, ellipses etc. */
    public CustomShapeRenderer getShapeRender() {
        return g;
    }

    /** Start clipping mask drawing. Usually simple shapes are used to draw clipping mask. Those shapes
     * will not be drawn. It will be used as clipping mask (pixels outside these mask will not be drawn if inside clipping is used).
     * To draw you drawing call {@link #startDrawWithClippingMask()} to begin drawing.
     * To clip inside mask and allow drawings only outside mask use {@link #startDrawWithClippingMask(boolean)} with <code>false</code> parameter.
     * Call {@link #endClippingMaskDrawing()} after you have finished drawing.*/
    public void startClippingMaskDrawing(){
        batch.end(); // baigiam batch. pagal viska sitas turetu but kvieciamas render loope, kur batch tikrai bus pradetas.
        Gdx.gl.glClearDepthf(1f);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT); // isvalom tuos visus depth

        Gdx.gl.glDepthFunc(GL20.GL_LESS); // ijungiam sita.
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST); // ijungiam pati detph testa.

        Gdx.gl.glDepthMask(true);
        Gdx.gl.glColorMask(false,false,false, false); // isjungiam, kad nepiestu.

        batch.begin(); // pradedam vel, kad butu galima normaliai naudotis.
    }

    /** Ends clipping mask drawing and starts normal drawing within clipping mask area.
     * After you have drawn your clipping mask with {@link #startClippingMaskDrawing()}, call this to
     * draw your drawings. Drawings will not be drawn outside clipping mask. After you have finished drawing you must
     * call {@link #endClippingMaskDrawing()} to stop clipping mask.
     * This method allows drawings only inside clipping mask.*/
    public void startDrawWithClippingMask(){
        startDrawWithClippingMask(true);
    }

    /** Ends clipping mask drawing and starts normal drawing within clipping mask area.
     * After you have drawn your clipping mask with {@link #startClippingMaskDrawing()}, call this to
     * draw your drawings. Drawings will not be drawn outside clipping mask. After you have finished drawing you must
     * call {@link #endClippingMaskDrawing()} to stop clipping mask.
     * @param insideClipping if drawings should be clipped inside clipping mask or outside. */
    public void startDrawWithClippingMask(boolean insideClipping){
        Gdx.gl.glColorMask(true,true,true,true); // padarom, kad piesimas veiktu
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST); // dar karta ijungiam. reik del kazko.
        if (insideClipping) {
            Gdx.gl.glDepthFunc(GL20.GL_EQUAL); // nustatom taip. tai leis tuos visus clipping daryt.
        }else {
            Gdx.gl.glDepthFunc(GL20.GL_NOTEQUAL); // cia bus daroma toks reverse clipping.
        }
        // pats batch turetu but su begin dabar tai tvarkoj cia.
    }

    /** Disables clipping mask. Call {@link #startClippingMaskDrawing()} to begin to draw your clipping mask than call
     * {@link #startDrawWithClippingMask()} to draw your drawings and than call this method to disable clipping mask. */
    public void endClippingMaskDrawing(){
        batch.end(); // isjungiam batch
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST); // isjungiam clipping mask
        batch.begin(); // vel ijungiam batch ir tesiam normalu piesima.
    }

    /** pieš tik nurodytam stačiakampio plote.
     * False atveju nereiktų piešt.
     * Piesiant reiktu naudot if, o if gale <code>popScissors()</code> taip išvengiant klaidų*/
    public boolean pushScissor(float x, float y, float width, float height){
//        pushScissor((int) x, (int) y, (int) width, (int) height);
        Rectangle scissor = scissorPool.obtain();
        tmpScissorArea.set(x, y, width, height);
        OrthographicCamera e = currentActiveCamera == 0 ? absoluteCamera : fixedCamera;
        ScissorStack.calculateScissors(e, view.getScreenX(), view.getScreenY(), view.getScreenWidth(), view.getScreenHeight(),
                batch.getTransformMatrix(), tmpScissorArea, scissor);
        // test
//        noFill(); //
//        stroke(255,0,0);
//        strokeWeight(5);
//        rect(x, y, width, height);
        batch.flush();
        if (ScissorStack.pushScissors(scissor)) {
            scissorsEntry++;
            return true;
        }
        return false;
    }

//    /** leis piest tik nurodyto staciakampio plote, kitur nepies */
//    public void pushScissor(int x, int y, int width, int height) {
//		y = this.height - y - height; // pavertimas is left top i left bottom corner.
//        Gdx.gl20.glEnable(GL20.GL_SCISSOR_TEST);
//        Gdx.gl20.glScissor(x, y, width, height);
//        ScissorStack
//        Rectangle e;
//    }

    /** isjungs piesimo apribojimą */
    public void popScissor() {
        if (scissorsEntry <= 0){
            if (Engine.getInstance() != null)
                Engine.getInstance().setError("Too many popScissor() Calls", ErrorMenu.ErrorType.WrongPara);
            else
                throw new IllegalArgumentException("Too many popScissor() Calls");
            return;
        }
        batch.flush();
        scissorsEntry--;
        scissorPool.free(ScissorStack.popScissors());
//        Gdx.gl20.glDisable(GL20.GL_SCISSOR_TEST);
    }

    @Override
    public void render() {
//        Gdx.gl.glClearColor(0, 0, 0, 1);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        currentActiveCamera = 0;
        batch.setProjectionMatrix(absoluteCamera.combined);
//        g.setProjectionMatrix(absoluteCamera.combined);
        batch.begin();
        draw();
        batch.end();
        currentActiveCamera = 1;
        batch.setProjectionMatrix(fixedCamera.combined);
//        g.setProjectionMatrix(fixedCamera.combined);
        batch.begin();
        fixedDraw();
        batch.end();

        if (forceColorHasChanged) // nuresetinam.
            forceColorHasChanged = false;
    }

    @Override
    public void dispose() {
        for (Disposable e : disposables){
            // Disposing this way will cause crash.
            // FreeTypeFontGenerator should be added separately from font to this list.
            // If font was created using engine's methods, than it is added to this list or
            // already disposed before it even gets here. Trying to dispose two times would crash
            // application.
            // bitmap fontai sitaip disposinsis.
//            if (e instanceof BitmapFont){
//                BitmapFont font = (BitmapFont) e;
//                if (font.getData() instanceof FreeTypeFontGenerator.FreeTypeBitmapFontData){
//                    ((FreeTypeFontGenerator.FreeTypeBitmapFontData) font.getData()).dispose();
//                }
//            }
            e.dispose();
        }
        batch.dispose();
        g.dispose();
        // Only if it was not in Resources.
        if (font != null && !Resources.containsFont(font)) { // du kart geriau nekviest.
            font.dispose();
        }
        Resources.dispose();
        Gdx.app.log("Engine", "System was disposed");
//        System.out.println("System is cleared");
//        Physics.dispose();
    }

    @Override
    public void resize(int width, int height) {
//        System.out.println(width + ":" + height);
        Gdx.app.log("Engine", "Screen size is " + width + ":" + height);
        view.update(width, height, false);
        batch.setProjectionMatrix(absoluteCamera.combined);
//        g.setProjectionMatrix(absoluteCamera.combined);
        g.update();
//        view.apply(false); // sito nereik, kadangi jis kvieciamas view.update metode.

//        fixedCamera.position.set(worldSizeX/2, worldSizeY/2, 0);
        if (Resources.getPropertyInt("updateFixedCamera", 1) == 1){
            // atnaujinam tik po viewport atnaujinimo.
//            fixedCamera.viewportWidth = view.getWorldWidth();
//            fixedCamera.viewportHeight = view.getWorldHeight();
//            // centruojam.
//            fixedCamera.position.set(worldSizeX/2, worldSizeY/2, 0);
//            fixedCamera.update();
            fixedView.update(width, height, false);
        }
    }

    @Override
    public void pause() {
        for (int a = 0; a < pauseListeners.size; a++){
            pauseListeners.get(a).onPause();
        }
    }

    @Override
    public void resume() {
        // reik atnaujint shapeRenderi.
        // kadangi sis pameta savo projection.
        g.update();

        for (int a = 0; a < pauseListeners.size; a++){
            pauseListeners.get(a).onResume();
        }
    }

    private void fillReset() {
        if (!isFill)
            isFill = true;
    }

    private void strokeReset() {
        if (!isStroke) {
            isStroke = true;
        }
    }

    private float translateColor(int color) {
        if (color < 0)
            return 0;
        if (color >= 255)
            return 1;
        return color / 255f;
    }

    /** how thick should stroke line be. */
    public void strokeWeight(float wight) {
        strokeWeight = wight;
//        half = strokeWeight / 2;
    }

    private void setFillAlpha(int al) {
        if (al > 255) {
            fa = 1;
        } else if (al < 0) {
            fa = 0;
        } else {
            fa = translateColor(al);
        }
    }

    /** fill color. 0-255 or ARGB. */
    public void fill(int color) {
        if (color >= 0 && color <= 255) {
            fill(color, color, color);
        } else {
            int a = color >> 24 & 0xFF;
            int r = color >> 16 & 0xFF;
            int g = color >> 8 & 0xFF;
            int b = color & 0xFF;
            fill(r, g, b);
            setFillAlpha(a);
        }
    }

    /** Disables any fill drawing. Fill color for text is still used. */
    public void noFill() {
        isFill = false;
    }

    @Deprecated
    public void fill(int color, int alpha) {
        fill(color);
        setFillAlpha(alpha);
    }

    /** fill color. 0-255. a value is set to full. */
    public void fill(int r, int g, int b) {
        if (r < 0)
            fr = 0;
        else if (r > 255)
            fr = 1;
        else
            fr = translateColor(r);
        if (g < 0)
            fg = 0;
        else if (g > 255)
            fg = 1;
        else
            fg = translateColor(g);
        if (b < 0)
            fb = 0;
        else if (b > 255)
            fb = 1;
        else
            fb = translateColor(b);
        fillReset();
        setFillAlpha(256);
    }

    /** fill color. 0-255. */
    public void fill(int r, int g, int b, int a) {
        fill(r, g, b);
        setFillAlpha(a);
    }

    /** fill color. */
    public void fill(Color color){
        fr = color.r;
        fg = color.g;
        fb = color.b;
        fa = color.a;
        fillReset();
    }

    private void setStrokeAlpha(int al) {
        if (al > 255) {
            sa = 1;
        } else if (al < 0) {
            sa = 0;
        } else {
            sa = translateColor(al);
        }
    }

    /** Set stroke color. 0-255 or ARGB. */
    public void stroke(int color) {
        if (color >= 0 && color <= 255) {
            stroke(color, color, color);
        } else {
            int a = color >> 24 & 0xFF;
            int r = color >> 16 & 0xFF;
            int g = color >> 8 & 0xFF;
            int b = color & 0xFF;
            stroke(r, g, b);
            setStrokeAlpha(a);
        }
    }

    /** Removes any stroke drawing. */
    public void noStroke() {
        isStroke = false;
    }

//    public void stroke(int color, int alpha) {
//        stroke(color);
//        setStrokeAlpha(alpha);
//    }

    /** Set stroke color. 0-255. a value is set to full. */
    public void stroke(int r, int g, int b) {
        if (r < 0)
            sr = 0;
        else if (r > 255)
            sr = 1;
        else
            sr = translateColor(r);
        if (g < 0)
            sg = 0;
        else if (g > 255)
            sg = 1;
        else
            sg = translateColor(g);
        if (b < 0)
            fb = 0;
        else if (b > 255)
            sb = 1;
        else
            sb = translateColor(b);
        strokeReset();
        setStrokeAlpha(256);
    }

    /** Set stroke color. 0-1. */
    public void strokef(float r, float g, float b, float a){
        stroke((int) r * 255, (int) g * 255, (int) b * 255, (int) a * 255);
    }

    /** Set stroke color. 0-255. */
    public void stroke(int r, int g, int b, int a) {
        stroke(r, g, b);
        setStrokeAlpha(a);
    }

    /** Set stroke color */
    public void stroke(Color color){
        sr = color.r;
        sg = color.g;
        sb = color.b;
        sa = color.a;
        strokeReset();
    }

    /** Sets background. 0-255 or ARGB format. Force tint doesn't effect this. */
    public void background(int color) {
        if (color >= 0 && color <= 255) {
            float c = translateColor(color);
            Gdx.gl.glClearColor(c, c, c, 1);
        } else {
            float a = translateColor(color >> 24 & 0xFF);
            float r = translateColor(color >> 16 & 0xFF);
            float g = translateColor(color >> 8 & 0xFF);
            float b = translateColor(color & 0xFF);
            Gdx.gl.glClearColor(r, g, b, a);

        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /** Draws rect in given point and size. */
    public void rect(float x, float y, float w, float h) { // paprastas rect. be rounded corners
//		y = this.height - y;
//		y -= h;
        if (rectMode == Align.center) {
            x -= w / 2;
            y -= h / 2;
        }
        batch.end();
        if (isFill) {
            g.begin(ShapeType.Filled);
            if (fa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(fr * forceTint.r, fg * forceTint.g, fb * forceTint.b, fa * forceTint.a);
//			g.rect(x, y, w, h);
            g.rect(x, y, w, h);
//			g.rectLine(x, y, x, y, w);W
            g.end();
        }
        if (isStroke) {
            g.begin(ShapeType.Filled);
            if (sa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(sr * forceTint.r, sg * forceTint.g, sb * forceTint.b, sa * forceTint.a);
            float half = strokeWeight / 2f;
            g.rectLine(x - half, y, x + w + half, y, strokeWeight);
            g.rectLine(x, y, x, y + h, strokeWeight);
            g.rectLine(x + w, y, x + w, y + h, strokeWeight);
            g.rectLine(x - half, y + h, x + w + half, y + h, strokeWeight);
            g.end();
        }
        batch.begin();
    }

    /** Draw rect in given cords and size, rotates it by given degrees over origin point.  */
    public void rect(float x, float y, float w, float h, float originX, float originY, float degrees) {
//		y = this.height - y;
//		y -= h;
        if (rectMode == Align.center) {
            x -= w / 2;
            y -= h / 2;
        }
        batch.end();
        if (isFill) {
            g.begin(ShapeType.Filled);
            if (fa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(fr * forceTint.r, fg * forceTint.g, fb * forceTint.b, fa * forceTint.a);
//			g.rect(x, y, w, h);
//			g.rect(x, y, translateX, translateY, w, h, 1, 1, rotateImage);
            g.rect(x, y, originX, originY, w, h, 1, 1, degrees);
//			g.rectLine(x, y, x, y, w);W
            g.end();
        }
        if (isStroke) {
            g.begin(ShapeType.Line);
            if (sa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            Gdx.gl.glLineWidth(strokeWeight);
            g.setColor(sr * forceTint.r, sg * forceTint.g, sb * forceTint.b, sa * forceTint.a);
            g.rect(x, y, originX, originY, w, h, 1, 1, degrees);
            g.end();
        }
        batch.begin();
    }

    /** Draws line and makes round endings. Use stroke to draw line. Fill has no effect.*/
    public void line(float x1, float y1, float x2, float y2) {
        line(x1, y1, x2, y2, true, true);
    }

    /** Draws line and makes round endings if specified. Use stroke to draw line. Fill has no effect.
     * @param hasStart - round starting.
     * @param hasEnd - round ending.*/
    public void line(float x1, float y1, float x2, float y2, boolean hasStart, boolean hasEnd) {
//		y1 = this.height - y1;
//		y2 = this.height - y2
        if (isStroke) {
            batch.end();
            g.begin(ShapeType.Filled);
//			g.setProjectionMatrix(absoluteCamera.combined);
            if (sa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(sr * forceTint.r, sg * forceTint.g, sb * forceTint.b, sa * forceTint.a);
            float half = strokeWeight / 2f;
            if (hasStart)
                g.circle(x1, y1, half);
            if (hasEnd)
                g.circle(x2, y2, half);
            g.rectLine(x1, y1, x2, y2, strokeWeight);
            g.end();
            batch.begin();
        }
    }

    /*
     * r1 - desine virsus
     * r2 - kaire virsus
     * r3 - kaire apacia
     * r4 - desine apacia
     * neveikia skirtingi kampai...
     */
    private void rect(float x, float y, float w, float h, float r1, float r2, float r3, float r4) {
        if (rectMode == Align.center) {
            x -= w / 2;
            y -= h / 2;
        }
        batch.end();
        float difv, difa;
        difv = Math.max(r1, r2);
        difa = Math.max(r3, r4);
        float ny = y + difv;
        float leny = h - difa * 2;
        float difr, difl;
        difr = Math.max(r1, r4);
        difl = Math.max(r3, r2);
        float nx = x + difr;
        float len = w - difl * 2;
        if (isFill) {
            g.begin(ShapeType.Filled);
            if (fa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(fr * forceTint.r, fg * forceTint.g, fb * forceTint.b, fa * forceTint.a);
            g.arc(x + r1, y + r1, r1, 180, 90);
            g.arc(x + w - r2, y + r2, r2, 270, 90);
            g.arc(x + w - r3, y + h - r3, r3, 0, 90);
            g.arc(x + r4, y + h - r4, r4, 90, 90);
            // vidinis staciakampis, nuo sonu sumazintas
            // vidinis staciakampis nuo virsaus ir apacios sumazintas.
            g.rect(nx, ny, len, leny);
            // kaire sonas
            g.rect(x, ny, r1, leny);
            // desine sonas
            g.rect(x + w - r3, ny, r3, leny);
            // virsus
            g.rect(nx, y, len, r1);
            // apacia
            g.rect(nx, y + h - r4, len, r4);
            g.end();
        }
        if (isStroke) { // neveikia skirtingi kampai....
            g.begin(ShapeType.Filled);
            if (sa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(sr * forceTint.r, sg * forceTint.g, sb * forceTint.b, sa * forceTint.a);
            g.rectLine(x, ny, x, ny + leny, strokeWeight);
            g.rectLine(x + w, ny, x + w, ny + leny, strokeWeight);
            g.rectLine(nx, y, nx + len, y, strokeWeight);
            g.rectLine(nx, y + h, nx + len, y + h, strokeWeight);
            g.end();
            g.begin(ShapeType.Line);
            Gdx.gl.glLineWidth(strokeWeight);
            g.arc(x + r1, y + r1, r1, 180, 90);
            g.arc(x + w - r2, y + r2, r2, 270, 90);
            g.arc(x + w - r3, y + h - r3, r3, 0, 90);
            g.arc(x + r4, y + h - r4, r4, 90, 90);
            g.end();
        }
        batch.begin();
    }

    /** Draws rect. Use fill to paint inside rect. Use stroke to draw rect. */
    public void rect(float x, float y, float w, float h, float r) {
        rect(x, y, w, h, r, r, r, r);
    }

    /** ARGB format. Sets a to full. */
    public static int color(int r, int g, int b) {
        if (r > 255)
            r = 255;
        else if (r < 0)
            r = 0;
        if (g > 255)
            g = 255;
        else if (g < 0)
            g = 0;
        if (b > 255)
            b = 255;
        else if (b < 0)
            b = 0;
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    /** ARGB format */
    public static int color(int r, int g, int b, int alpha) {
        if (alpha > 255)
            alpha = 255;
        else if (alpha < 0)
            alpha = 0;
        if (r > 255)
            r = 255;
        else if (r < 0)
            r = 0;
        if (g > 255)
            g = 255;
        else if (g < 0)
            g = 0;
        if (b > 255)
            b = 255;
        else if (b < 0)
            b = 0;
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    /** Draws triangle. Use fill to fill triangle. Use stroke to draw triangle. */
    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        batch.end();
        if (isFill) {
            g.begin(ShapeType.Filled);
            if (fa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(fr * forceTint.r, fg * forceTint.g, fb * forceTint.b, fa * forceTint.a);
            g.triangle(x1, y1, x2, y2, x3, y3);
            g.end();
        }
        if (isStroke) {
            g.begin(ShapeType.Line);
            if (sa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(sr * forceTint.r, sg * forceTint.g, sb * forceTint.b, sa * forceTint.a);
            Gdx.gl.glLineWidth(strokeWeight);
            g.triangle(x1, y1, x2, y2, x3, y3);
            g.end();
        }
        batch.begin();
    }

    /** Draws ellipse. Use fill to paint inside ellipse. Use stroke to draw ellipse. */
    public void ellipse(float x, float y, float width, float height) {
//		y = this.height - y;
//		y -= height;
        if (ellipseMode == Align.center) {
            x -= width / 2;
            y -= height / 2;
        }
        batch.end();
        if (isFill) {
            g.begin(ShapeType.Filled);
            if (fa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(fr * forceTint.r, fg * forceTint.g, fb * forceTint.b, fa * forceTint.a);
            g.ellipse(x, y, width, height);
            g.end();
        }
        if (isStroke) {
            g.begin(ShapeType.Line);
            if (sa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(sr * forceTint.r, sg * forceTint.g, sb * forceTint.b, sa * forceTint.a);
            Gdx.gl.glLineWidth(strokeWeight);
            g.ellipse(x, y, width, height);
            g.end();
        }
        batch.begin();
    }

    /** Draws curve. To draw curve stroke must be set. Fill has no effect. */
    public void curve(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        batch.end();
        if (isStroke) {
            g.begin(ShapeType.Line);
            if (sa < 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            g.setColor(sr * forceTint.r, sg * forceTint.g, sb * forceTint.b, sa * forceTint.a);
            Gdx.gl.glLineWidth(strokeWeight);
            g.curve(x1, y1, x2, y2, x3, y3, x4, y4, 20);
            g.end();
        }
        batch.begin();
    }

    /* tint settings */

    /** @return if force color was changed in this frame. */
    public boolean hasForceColorChanged(){
        return forceColorHasChanged;
    }

    /** force tint color. */
    public Color getForceColor(){
        return forceTint;
    }

    /** Forces custom tint. All {@link #tint(int)} method calls will be multiplied with this color settings.*/
    public void forceTint(Color e){
        forceTint.set(e);
        Color r = batch.getColor();
        tint(r);
        forceColorHasChanged = true;
    }

    /** Forces custom tint. All {@link #tint(int)} method calls will be multiplied with this color settings.
     * Color between 0-1.*/
    public void forceTint(float a, float r, float g, float b){
        forceTint.set(r, g, b, a);
        Color e = batch.getColor();
        tint(e);
        forceColorHasChanged = true;
    }

    /** Sets force tint to white which means force tint will no longer take effect. */
    public void noForceTint(){
        forceTint.set(1,1,1,1);
        Color e = batch.getColor();
        tint(e);
        forceColorHasChanged = true;
    }

    /** color between 0-255 or ARGB format int color. */
    public void tint(int color) {
        if (color >= 0 && color <= 255) {
            float col = translateColor(color);
            batch.setColor(col*forceTint.r, col*forceTint.g, col*forceTint.b, 1*forceTint.a);
        } else {
            Color.argb8888ToColor(systemColor, color);
            systemColor.set(systemColor.r * forceTint.r, systemColor.g * forceTint.g, systemColor.b * forceTint.b,
                    systemColor.a * forceTint.a);
            batch.setColor(systemColor);
        }
        isTint = true;
    }

    /** color between 0-255. */
    public void tint(int r, int g, int b) {
        isTint = true;
        batch.setColor(translateColor(r) * forceTint.r, translateColor(g) * forceTint.g, translateColor(b) * forceTint.b,
                1 * forceTint.a);
    }

    /** color between 0-255 */
    public void tint(int r, int g, int b, int a) {
        isTint = true;
        batch.setColor(translateColor(r) * forceTint.r, translateColor(g) * forceTint.g, translateColor(b) * forceTint.b,
                translateColor(a) * forceTint.a);
    }

    /** color between 0-1. */
    public void tintf(float r, float g, float b, float a){
        isTint = true;
        r = MoreUtils.inBounds(r, 0, 1);
        g = MoreUtils.inBounds(g, 0, 1);
        b = MoreUtils.inBounds(b, 0, 1);
        a = MoreUtils.inBounds(a, 0, 1);
        batch.setColor(r * forceTint.r, g * forceTint.g, b * forceTint.b, a * forceTint.a);
    }

    /** tint from color. */
    public void tint(Color color){
        isTint = true;
        systemColor.set(color);
        systemColor.set(systemColor.r * forceTint.r, systemColor.g * forceTint.g, systemColor.b * forceTint.b,
                systemColor.a * forceTint.a);
        batch.setColor(systemColor);
    }

    /** removes tint. */
    public void noTint() {
        if (isTint) {
            batch.setColor(forceTint); // force tint svarbiausiais. jeigu jis baltas, ti nebus jokio efekto. simple.
            isTint = false;
        }
    }

    /* text settings */

    /** Draw text in specified coords. Uses fill color to specify text color. Use method {@link #fill(int)} before this method to change text color.
     * Text align won't kick in. It will be felt weak as bounds are set as same size as text size.*/
    public void text(String text, float x, float y) {
        // nustatom layet su fill spalva
        layout.setText(font, text, systemColor.set(fr * forceTint.r, fg * forceTint.g,
                fb * forceTint.b, fa * forceTint.a), worldSizeX, Align.left, false);
        if (alignX == Align.center) {
            x -= layout.width / 2;
        }
        text(layout, x, y, font);
    }

    /** Draws text in specified cords and bounds. Uses fill color to specify text color. Use method {@link #fill(int)} before this method to change text color.
     * Use {@link #textAlign(int, int)} to align text.*/
    public void text(String text, float x, float y, float width, float height){
        text(text, x, y, width, height, font);
    }

    /** Draws text in specified cords and bounds and using specified font.
     * Uses fill color to specify text color. Use method {@link #fill(int)} before this method to change text color.
     * Use {@link #textAlign(int, int)} to align text.*/
    public void text(String text, float x, float y, float width, float height, BitmapFont font) {
        try {
            layout.setText(font, text, systemColor.set(fr * forceTint.r, fg * forceTint.g,
                    fb * forceTint.b, fa * forceTint.a), width, alignX, true);
            if (alignY == Align.center) {
                y += height / 2 + layout.height / 2;
            } else if (alignY == Align.bottom) {
                y = y + layout.height;
            } else if (alignY == Align.top) {
                y += height;
            }
            text(layout, x, y, font);
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            Gdx.app.log("Engine", "Something went wrong trying to dispaly text: " + e.getMessage());
        }
    }

    /** Draws text from layout with default font. */
    public void text(GlyphLayout layout, float x, float y){
        text(layout, x, y, font);
    }

    /** Draws text from layout with given font. */
    public void text(GlyphLayout layout, float x, float y, BitmapFont font){
        batch.setShader(distanceFieldShader);
        distanceFieldShader.setSmoothing(textSmoothing);
        font.draw(batch, layout, x, y);
        batch.setShader(null);
    }

    /** Set default font text size. */
    public void textSize(float size) {
        float nsize = size / fontDefaultSize;
        font.getData().setScale(nsize, nsize);
    }

    /** text width. After calling this method call {@link #textHeight()} to get text height.
     * Use {@link #textSize(float)} to set size of font and get accurate size of text. */
    public float textWidth(String text) {
        return textWidth(text, font);
    }

    /** text width. Wraps text in given width. After calling this method call {@link #textHeight()} to get text height.
     * Use {@link #textSize(float)} to set size of font and get accurate size of text. */
    public float textWidth(String text, float width){
        return textWidth(text, width, font);
    }

    /** text width. Wraps text in given width. After calling this method call {@link #textHeight()} to get text height.
     * Use {@link #textSize(float)} to set size of font and get accurate size of text. */
    public float textWidth(String text, float width, BitmapFont font) {
        layout.setText(font, text, systemColor.set(fr * forceTint.r, fg * forceTint.g,
                fb * forceTint.b, fa * forceTint.a), width, alignX, true);
        return layout.width;
    }

    /** text width. After calling this method call {@link #textHeight()} to get text height.
     * Use {@link #textSize(float)} to set size of font and get accurate size of text. */
    public float textWidth(String text, BitmapFont font){
        try {
            layout.setText(font, text);
        } catch (IndexOutOfBoundsException e) {
            return textWidth(text, font);
        }
        return layout.width;
    }

    /** depends on textWidth, call <code>textWidth()</code> before this. */
    public float textHeight() {
        return layout.height;
    }

    public void textAlign(int mode) { // sitas tik horizontaliai
        textAlign(mode, Align.top);
    }

    public void textAlign(int modex, int modey) {
        alignX = modex;
        alignY = modey;
    }

//    public void imageMode(int mode) {
//        imageMode = mode;
//    }

    public void rectMode(int mode) {
        rectMode = mode;
    }

    public void ellipseMode(int mode) {
        ellipseMode = mode;
    }

    /** This program's time. Time starts when program starts and ends when program ends. */
    public int millis() {
        return (int) TimeUtils.timeSinceMillis(startTime);
    }

//    /** terminates application. On IOS this should be avoided. */
    // neveik.
//    public void exit() { // ant ios gali neveikt. Kada nors gal patestuosim :D
//        Gdx.app.exit();
//    }

//    public int getWidth() {
//        return width;
//    }
//
//    public int getHeight() {
//        return height;
//    }

    /** World size visible in screen. This is not pixel size! Use <code>Gdx.graphics.getWidth()</code>
     * for pixel size.*/
    public float getScreenWidth(){
        return view.getWorldWidth();
    }

    /** World size visible in screen. This is not pixel size! Use <code>Gdx.graphics.getHeight()</code>
     * for pixel size.*/
    public float getScreenHeight(){
        return view.getWorldHeight();
    }

    /** Preferred screen width (Not real size. To get real size call {@link #getScreenWidth()}).
     * Real size can change if screen has different aspect ratio than expected. */
    public float getPreferredScreenWidth(){
        return worldSizeX;
    }

    /** Preferred screen height (Not real size. To get real size call {@link #getScreenHeight()}).
     * Real size can change if screen has different aspect ratio than expected. */
    public float getPreferredScreenHeight(){
        return worldSizeY;
    }

    /** Screen offset. This offset can be used for different screen aspect ratios. */
    public float getScreenOffsetX(){
        return (getPreferredScreenWidth() - getScreenWidth())/2;
    }

    /** Screen offset. This offset can be used for different screen aspect ratios. */
    public float getScreenOffsetY(){
        return (getPreferredScreenHeight() - getScreenHeight())/2;
    }

//    /** logical pixel width */
//     public int getScreenPixelWidth(){
//        return Gdx.graphics.getWidth();
//    }
//
//    /** logical pixel height */
//    public int getScreenPixelHeight(){
//        return Gdx.graphics.getHeight();
//    }

//    /** translates origin image point. */
//    public void translateOrigin(float x, float y) {
//        translateX = x;
//        translateY = y;
//    }
//
//    /** rotates image. */
//    public void rotateImage(float deg) {
//        rotate = deg;
//    }

	/* Listeners */

    public abstract void draw();

    public abstract void fixedDraw();

    protected abstract void setup();

    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    public boolean keyDown(int keycode) {
        return false;
    }

    public boolean keyUp(int keycode) {
        return false;
    }

    public boolean mouseMoved(float x, float y) {
        return false;
    }

    public boolean keyTyped(char e) {
        return false;
    }

    public boolean scrolled(float amountX, float amountY){
        return false;
    }

//    public boolean onBackPress() {
//        return false;
//    }

    public boolean tap(float x, float y, int count, int button) {
        return false;
    }


    public boolean longPress(float x, float y) {
        return false;
    }

    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    public void pinchStop() {

    }

    /* distance shader valdymas ir pats shader */

    /** Text smoothing. How much text is being smoothed. Default 0.5 */
    public float getTextSmoothing(){
        return textSmoothing;
    }

    /** Set text smoothing. How much text should be smoothed. Default 0.5 */
    public void setTextSmoothing(float textSmoothing){
        this.textSmoothing = textSmoothing;
    }

    private static class DistanceFieldShader extends ShaderProgram {
        DistanceFieldShader () {
            // The vert and frag files are copied from http://git.io/yK63lQ (vert) and http://git.io/hAcw9Q (the frag)
            // frag is edited so alpha value can work.
            super("uniform mat4 u_projTrans;\n"
                            + "attribute vec4 a_position;\n"
                            + "attribute vec2 a_texCoord0;\n"
                            + "attribute vec4 a_color;\n"
                            + "varying vec4 v_color;\n"
                            + "varying vec2 v_texCoord;\n"
                            + "void main() {\n"
                                + "\tgl_Position = u_projTrans * a_position;\n"
                                + "\tv_texCoord = a_texCoord0;\n"
                                + "\tv_color = a_color;\n"
                            + "}",
                    "#ifdef GL_ES\n"
                            + "#define LOWP lowp\n"
                            + "precision mediump float;\n"
                            + "#else\n"
                            + "#define LOWP\n"
                            + "#endif\n"
                            + "uniform sampler2D u_texture;\n"
                            + "uniform float u_lower;\n"
                            + "uniform float u_upper;\n"
                            + "varying LOWP vec4 v_color;\n"
                            + "varying vec2 v_texCoord;\n"
                            + "void main() {\n"
                            + "float distance = texture2D(u_texture, v_texCoord).a;\n"
                            + "\tfloat alpha = smoothstep(u_lower, u_upper, distance) * v_color.a;\n"
                            + "\tgl_FragColor = vec4(v_color.rgb, alpha) * texture2D(u_texture, v_texCoord);\n"// kad neprarast borderiu ir t.t. dauginam is texture2D.
                            + "}");
            if (!isCompiled()) {
                throw new RuntimeException("Shader compilation failed:\n" + getLog());
            }
        }

        /** @param smoothing a value between 0 and 1 */
        void setSmoothing (float smoothing) {
            float delta = 0.5f * MathUtils.clamp(smoothing, 0, 1);
            setUniformf("u_lower", 0.5f - delta);
            setUniformf("u_upper", 0.5f + delta);
        }
    }
}
