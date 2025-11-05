package com.engine.animations.spriter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.brashmonkey.spriter.Animation;
import com.brashmonkey.spriter.Mainline;
import com.brashmonkey.spriter.Player;
import com.brashmonkey.spriter.PlayerTweener;
import com.brashmonkey.spriter.Rectangle;
import com.brashmonkey.spriter.SCMLReader;
import com.brashmonkey.spriter.SpriterException;
import com.engine.animations.Counter;
import com.engine.core.Engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/** Allows to draw spriter animations. Spriter animation cannot be sized instead it uses {@link #setScale(float)}
 * or {@link #setScaledSize(float, float)}. Also animation can only be uniform scaled as size is not supported.
 * Check size  before using this drawable in buttons or somewhere else. To check size use {@link #getScaledWidth()}
 * or {@link #getScaledHeight()} . */
public class SpriterDrawable extends BaseDrawable implements Disposable{
    private SpriterDrawer drawer; // piesejas.
    private SpriterLoader loader;
    private Counter count; // animacjis perjungimui

    private PlayerTweener activePlayer; // pats veiksmas

    private boolean update = true; // ar drawe updatint.
    private long frameId; // pades nustatyt ar update call daroma daugiau nei viena kart per frame.

    // some para
    private float animationSpeed = 15f;
    private boolean looping = true, finished;
    private Player.PlayerListener listener;
    private AnimationChangedListener animationChangedListener;
    private AnimationFinishedListener animationFinishedListener;

    // tiesiog atsimint.
    private float oldX, oldY, oldWidth, oldHeight; // kad nescalintu ir neupdintu vietos ir dydzio parametru be reikalo.
    private float offsetX, offsetY, scale;
    private float normalOffsetX, normalOffsetY, flippedOffsetX, flippedOffsetY;

    // yra spriteri entity sistema. Siaip mes dazniausiai naudojam visada 1, bet jei kartais ateiti prireiks daugiau is vieno failo, tai kodel gi ne.
    private int entityIndex = 0;

    public SpriterDrawable(SpriterDrawable drawable){
        this(drawable, 0);
    }

    /** creates drawable from another drawable. This is good to copy drawable as same resources are in use. */
    public SpriterDrawable(SpriterDrawable drawer, int entityIndex){
        this.entityIndex = entityIndex;
        SpriterDrawer e = drawer.getDrawer();
        if (e == null){
//            GdxPongy.getInstance().setError("Another drawable cannot be null", ErrorMenu.ErrorType.WrongPara);
//            return;
            throw new RuntimeException("Drawable cannot be null");
        }
        this.drawer = e;
        loader = e.getLoader();
        if (loader ==  null){
//            GdxPongy.getInstance().setError("SpriterDrawable: loader cannot be null", ErrorMenu.ErrorType.UnknowError);
            throw new RuntimeException("Drawable doesn't have loader.");
        }
        activePlayer = new PlayerTweener(loader.getData().getEntity(this.entityIndex));
        activePlayer.updatePlayers = false;
        activePlayer.setWeight(0f);

        count = new Counter();
        count.setUninterruptible(true); // kad nenumustu niekas, gi vistiek baigsis pats.
        count.setCounterInformer(new Counter.CounterInformer() {
            @Override
            public void update(float oldValue, float currentValue) {
                if (currentValue == count.getGoalValue()){
                    activePlayer.getFirstPlayer().setAnimation(activePlayer.getSecondPlayer().getAnimation().id);
                    activePlayer.setWeight(0);
                }else {
                    activePlayer.setWeight(currentValue);
                }
            }
        });
        count.setCounterListiner(new Counter.CounterListener() {
            @Override
            public void finished(float currentValue) {
                animationChanged();
            }

            @Override
            public boolean cancel(int reason) {
                return false;
            }
        });
        setPosition(0, 0);
//        int speed = activePlayer.getFirstPlayer().speed;
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = 0;
//        activePlayer.update();
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = speed;
        updateNeed();
//        Rectangle rect = activePlayer.getBoudingRectangle(null);
        // BUTINAI! turim imt is kazkurio player nes bendras suda mala.
        Rectangle rect = activePlayer.getFirstPlayer().getBoudingRectangle(null);
        setMinWidth(rect.right - rect.left); // susigaudo originalu dydi.
        setMinHeight(rect.top - rect.bottom);
        setLeftWidth(rect.left);
        setBottomHeight(rect.bottom);
        setTopHeight(rect.top);
        setRightWidth(rect.right);
        checkOffset(rect);
    }

    /** @param pathToSCML your scml file location
     *  @param atlasName if scml file resources are from atlas, then atlas file name. null if not from atlas*/
    public SpriterDrawable(String pathToSCML, String atlasName){
        this(pathToSCML, atlasName, false);
    }

    /** @param pathToSCML your scml file location
     *  @param atlasName if scml file resources are from atlas, then atlas file name. null if not from atlas*/
    public SpriterDrawable(String pathToSCML, String atlasName, boolean loadManually){
        FileHandle file = Gdx.files.internal(pathToSCML);
        if (!file.exists()){
//            GdxPongy.getInstance().setError("SpriterDrawable: file was not found: " + pathToSCML, ErrorMenu.ErrorType.MissingResource);
//            return;
            throw new RuntimeException("file was not found: " + pathToSCML);
        }
        InputStream stream = file.read();
        SCMLReader reader;
        try {
            reader = new SCMLReader(stream);
        }catch (SpriterException ex){
//            GdxPongy.getInstance().setError("SpriterDrawable: Error parsing SCML file. Is it correct file? " +
//                    file.name(), ErrorMenu.ErrorType.UnknowError);
//            return;
            throw new RuntimeException("Error parsing SCML file. Is it correct file? " +"\n" + file.name());
        }
        try {
            stream.close();
        } catch (IOException e) {
//            e.printStackTrace();
            Gdx.app.log("SpriterDrawable", "Error closing stream: " + e.getMessage());
        }
        SpriterLoader loader = new SpriterLoader(reader.getData());
        load(loader, file.file(), atlasName, !loadManually);
    }

    /** loads from existing loader.
     *  @param atlasName if scml file resources are from atlas, then atlas file name. null if not from atlas*/
    public SpriterDrawable(SpriterLoader loader, FileHandle scmlFile, String atlasName){
        this(loader, scmlFile, atlasName, false);
    }

    public SpriterDrawable(SpriterLoader loader, FileHandle scmlFile, String atlasName, boolean loadManually){
        this(loader, scmlFile, atlasName, loadManually, 0);
    }

    /** loads from existing loader.
     *  @param atlasName if scml file resources are from atlas, then atlas file name. null if not from atlas*/
    public SpriterDrawable(SpriterLoader loader, FileHandle scmlFile, String atlasName, boolean loadManually, int entityIndex){
        this.entityIndex = entityIndex;
        if (loader == null || scmlFile == null){
//            Engine.getInstance().setError("SpriterDrawable: loader or scmlFile cannot be null", ErrorMenu.ErrorType.WrongPara);
//            return;
            throw new RuntimeException("loader or scml file cannot be null");
        }
        load(loader, scmlFile.file(), atlasName, !loadManually);
    }

    private void load(SpriterLoader loader, File root, String atlasFile, boolean loadResources){ // vienkartiniam uzloadinimui.
        this.loader = loader;
        if (loadResources) {
            if (atlasFile == null || atlasFile.isEmpty()) {
                loader.load(root);
            } else {
                loader.loadFromTextureAtlas(root, atlasFile);
            }
            setName(root.getAbsolutePath());
            initialize();
        }
    }

    public void load(File root){
        loader.load(root.getParent());
        setName(root.getAbsolutePath());
        initialize();
    }

    public void loadFromTextureAtlas(File root, String atlasFile){
        loader.loadFromTextureAtlas(root, atlasFile);
        setName(root.getAbsolutePath());
        initialize();
    }

    public void loadFromTextureAtlas(File root, TextureAtlas atlas){
        loader.loadFromTextureAtlas(root, atlas);
        setName(root.getAbsolutePath());
        initialize();
    }

    private void initialize(){
        drawer = new SpriterDrawer(loader);
        activePlayer = new PlayerTweener(drawer.getLoader().getData().getEntity(entityIndex));
        activePlayer.updatePlayers = false;
        activePlayer.setWeight(0f);

        count = new Counter();
        count.setUninterruptible(true);
        count.setCounterInformer(new Counter.CounterInformer() {
            @Override
            public void update(float oldValue, float currentValue) {
                if (currentValue == count.getGoalValue()){
                    activePlayer.getFirstPlayer().setAnimation(activePlayer.getSecondPlayer().getAnimation().id);
                    activePlayer.setWeight(0);
                }else {
                    activePlayer.setWeight(currentValue);
                }
            }
        });
        count.setCounterListiner(new Counter.CounterListener() {
            @Override
            public void finished(float currentValue) {
                animationChanged();
            }

            @Override
            public boolean cancel(int reason) {
                return false;
            }
        });
        setPosition(0, 0);
//        int speed = activePlayer.getFirstPlayer().speed;
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = 0;
//        activePlayer.update();
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = speed;
        updateNeed();
//        Rectangle rect = activePlayer.getBoudingRectangle(null);
        // BUTINAI! turim imt is kazkurio player kitaip nesamones gaunas.
        Rectangle rect = activePlayer.getFirstPlayer().getBoudingRectangle(null);
//        Rectangle rect2 = activePlayer.getSecondPlayer().getBoudingRectangle(null);
//        Rectangle rect3 = activePlayer.getBoudingRectangle(null);
        setMinWidth(rect.right - rect.left); // susigaudo originalu dydi.
        setMinHeight(rect.top - rect.bottom);
        setLeftWidth(rect.left);
        setBottomHeight(rect.bottom);
        setTopHeight(rect.top);
        setRightWidth(rect.right);
        checkOffset(rect);
    }

    private void checkOffset(Rectangle rect){
        int speed = activePlayer.getFirstPlayer().speed;
        int time = activePlayer.getFirstPlayer().getTime();
        float scale = activePlayer.getScale();
        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = 0; // padarom, kad update nestumtu laiko.
        setAnimationTime(0);
        setScale(1f); // padarom original dydzio.
        activePlayer.setPosition(0, 0); // pastatom ant nulio.
        activePlayer.getFirstPlayer().update();
        activePlayer.getSecondPlayer().update();
        activePlayer.update(); // pries tai update darom.
//        Rectangle rect = activePlayer.getBoudingRectangle(null); // kam du kartus?

        offsetX = rect.left; // sugaudom offset, pagal scale - 1
        offsetY = rect.bottom;

        // flip atveju labai skyriasi tie offsetai, todel issisaugom ir pasiruosiam flipui jeigu kas.
        normalOffsetX = offsetX;
        normalOffsetY = offsetY;
//        float middleWidth = rect.left + getMinWidth()/2;
//        flippedOffsetX = offsetX + (middleWidth - offsetX) * 2 - rect.right*2;
//        flippedOffsetX = rect.left - rect.right/2;
//        float middleHeight = rect.bottom + getMinHeight()/2;
//        flippedOffsetY = offsetY + (middleHeight - offsetY) * 2 - rect.top*2;
        flippedOffsetX = -rect.right;
        flippedOffsetY = -rect.top;

//        float nx = activePlayer.getX() - rect.left + x;
//        float ny = activePlayer.getY() - rect.bottom + y;
//        activePlayer.setPosition(nx, ny);
//        activePlayer.update();
        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = speed;
        setAnimationTime(time);
        setScale(scale);
    }

    private void updateNeed(){
        int speed = activePlayer.getFirstPlayer().speed;
        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = 0;
        activePlayer.getFirstPlayer().update();
        activePlayer.getSecondPlayer().update();
        activePlayer.update();
        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = speed;
    }

    private void animationChanged(){
        if (animationChangedListener != null){
            animationChangedListener.animationChanged();
        }
    }

    // para redagavimas

    /** sets new entity index. If drawable was loaded then you need to reload for new index to take effect. */
    public void setEntityIndex(int entityIndex, boolean reload){
        this.entityIndex = entityIndex;
        if (reload)
            initialize();
    }

    /** Set listener to listen when animation change is completed. */
    public void setAnimationChangedListener(AnimationChangedListener e){
        animationChangedListener = e;
    }

    /** Set listener to listen when animation is completed. */
    public void setAnimationFinishedListener(AnimationFinishedListener listener){
        animationFinishedListener = listener;
        if (listener != null){
            if (this.listener == null){
                // nera musu listener. reik sukurt.
                boolean old = looping; // pasizymim.
                // kodel per loop? Nes ancient code taip parasytas...
                setAnimationLoop(false); // sioj vietoj sukurs musu finished listeneri.
                setAnimationLoop(old); // atstatom atgal.
            }
        }
    }

    /** Animation finished listener. If not set null. */
    public AnimationFinishedListener getAnimationFinishedListener(){
        return animationFinishedListener;
    }

    public void flipAnimation(boolean flipx, boolean flipy){
        activePlayer.flip(flipx, flipy);
        if (flipx){
            if (activePlayer.flippedX() == 1){
                offsetX = normalOffsetX;
            }else {
                offsetX = flippedOffsetX;
            }
        }
        if (flipy){
            if (activePlayer.flippedY() == 1){
                offsetY = normalOffsetY;
            }else {
                offsetY = flippedOffsetY;
            }
        }
//        Rectangle rect = activePlayer.getFirstPlayer().getBoudingRectangle(null);
//        checkOffset(rect);

        oldX = oldY = Float.NEGATIVE_INFINITY; // cia kad update padarytu po flip.
        updateNeed();
    }

    public void setPosition(float x, float y){
//        int speed = activePlayer.getFirstPlayer().speed;
////        int time = activePlayer.getFirstPlayer().getTime();
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = 0; // padarom, kad update nestumtu laiko.
//        activePlayer.update(); // pries tai update darom.
//        Rectangle rect = activePlayer.getBoudingRectangle(null);
//        float nx = activePlayer.getX() - rect.left + x;
//        float ny = activePlayer.getY() - rect.bottom + y;
//        activePlayer.setPosition(nx, ny);
//        activePlayer.update();
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = speed;
        activePlayer.setPosition(-offsetX*scale + x, -offsetY*scale + y); // tiesiog taip.
        updateNeed();
    }

    /** only uniform scale. There isn't size settings only scale and only uniform.*/
    public void setScale(float scale){
        this.scale = scale; // nustatom scale, naudosim animacijos pozicijai sugaudyt.
        activePlayer.setScale(scale);
//        int speed = activePlayer.getFirstPlayer().speed;
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = 0;
//        activePlayer.getFirstPlayer().update();
//        activePlayer.getSecondPlayer().update();
//        activePlayer.update();
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = speed;
        updateNeed();
    }

    /** rotate animation.
     * @param angle in degrees*/
    public void setAngle(float angle){
        activePlayer.setAngle(angle);
        updateNeed();
//        int speed = activePlayer.getFirstPlayer().speed;
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = 0;
//        activePlayer.getFirstPlayer().update();
//        activePlayer.getSecondPlayer().update();
//        activePlayer.update();
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = speed;
    }

    /** if true then player.update() will be called in draw method. if false update must be called manually or animation will stay frozen.
     * Convenient to stop animation. */
    public void enableUpdate(boolean update){
        this.update = update;
    }

    /** default speed is 15 */
    public void setAnimationSpeed(float speed){
        animationSpeed = speed;
    }

    /** animation timeline time. Where should animation time begin or if animation is frozen, where to stop. */
    public void setAnimationTime(int time){
        activePlayer.setTime(time);
        activePlayer.getFirstPlayer().setTime(time);
        activePlayer.getSecondPlayer().setTime(time);
//        int speed = activePlayer.getFirstPlayer().speed;
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = 0;
//        activePlayer.getFirstPlayer().update();
//        activePlayer.getSecondPlayer().update();
//        activePlayer.update();
//        activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = speed;
        updateNeed();
    }

    /** sets scale by given size. Drawable will be fitted in given size. Size ratio of drawable cannot be changed as only uniform scale is supported*/
    public void setScaledSize(float width, float height){
//        float size;
//        if (width > height){ // imsim mazesni, kad sutilptu.
//            size = height;
//            if (oldSize != size){
//                float scale = size / getMinHeight();
//                setScale(scale);
//                oldSize = size;
//            }
//        }else {
//            size = width;
//            if (oldSize != size){
//                float scale = size / getMinWidth();
//                setScale(scale);
//                oldSize = size;
//            }
//        }
        tryScaleSize(width, height);
    }

    private boolean tryScaleSize(float width, float height){
        float size;
        boolean updatePos = false;
        if (oldWidth != width || oldHeight != height) { // pries tai buves dydis neturi sutapt.
            if (width > height) { // imsim mazesni, kad sutilptu.
                size = height;
                float scale = size / getMinHeight();
                if (getMinWidth() * scale > width) { // vadinas negalim naudot sito scale, nes islena uz ribu.
                    scale = width / getMinWidth();
                }
                setScale(scale); // scalinam.
                updatePos = true; // pazymim, kad keitem
            } else { // aukstis didesnis.
                size = width;
                float scale = size / getMinWidth();
                if (getMinHeight() * scale > height) { // vadinas negalim naudot sito scale, nes islena uz ribu.
                    scale = height / getMinHeight();
                }
                setScale(scale); // scalinam
                updatePos = true; // pranesam, kad pakeitem dydi.
            }
            // pasizymim naujus dydzius.
            oldWidth = width;
            oldHeight = height;
        }
        return updatePos;
    }

    /** if false animation will be played till end and than stop. This will be reset to true every time when animation changes.
     * NOTE: USE THIS ONLY AFTER ANIMATION CHANGE OR ELSE THERE WILL BE NO EFFECT.*/
    public void setAnimationLoop(boolean looping){
        this.looping = looping;
        if (!looping && listener == null){
            listener = new Player.PlayerListener() {
                @Override
                public void animationFinished(Animation animation) {
                    if (!SpriterDrawable.this.looping && !finished){
                        if (count.isCounting()){
                            count.cancel();
                            activePlayer.setWeight(0f);
                        }
//                        int length = animation.length-1;
                        int length = activePlayer.getFirstPlayer().getAnimation().length-((int)animationSpeed);
                        finished = true;
//                        setAnimationTime(0f); // turetu nebejudet.
                        setAnimationTime(length);
                        update = false;
                    }

                    if (animationFinishedListener != null){
                        animationFinishedListener.animationFinished(animation);
                    }
                }

                @Override
                public void animationChanged(Animation oldAnim, Animation newAnim) {

                }

                @Override
                public void preProcess(Player player) {

                }

                @Override
                public void postProcess(Player player) {

                }

                @Override
                public void mainlineKeyChanged(Mainline.Key prevKey, Mainline.Key newKey) {

                }
            };
            activePlayer.getFirstPlayer().addListener(listener);
        }
    }

    /** Entity index */
    public int getEntityIndex(){
        return entityIndex;
    }

    public boolean isAnimationChanging(){
        return count.isCounting();
    }

    public boolean isAnimationLooping(){
        return looping;
    }

    /** @return this animation texture atlas, if animation does not use atlas then null is returned. */
    public TextureAtlas getAtlas(){
        return loader.getAtlas();
    }

    /** @return current animation speed. */
    public float getAnimationSpeed(){
        return animationSpeed;
    }

    /** Current active Animation. It has name, length, id about current active animation. */
    public Animation getAnimation(){
        return activePlayer.getFirstPlayer().getAnimation();
    }

    public PlayerTweener getActivePlayer() {
        return activePlayer;
    }

    public boolean isUpdateEnabled(){
        return update;
    }

    /** drawer who responsible for spriter animation drawing. */
    public SpriterDrawer getDrawer() {
        return drawer;
    }

    /** @return current width. */
    public float getScaledWidth(){
        return getMinWidth()*activePlayer.getScale();
    }

    /** @return current height */
    public float getScaledHeight(){
        return getMinHeight()*activePlayer.getScale();
    }

    // veikimas

    /** change animations. use time to determine for how long animation changes occurs.
     * if animation not found, nothing happens.
     * @param time in seconds. if time zero then animation will be switch immediately*/
    public void switchAnimations(int index, float time){
        if (index < 0 || index >= activePlayer.getSecondPlayer().getEntity().animations()){
            return;
        }
        if (time > 0) {
            if (count.isCounting()){ // ale uzbaigiam darba uz ji.
                activePlayer.getFirstPlayer().setAnimation(activePlayer.getSecondPlayer().getAnimation().id);
                activePlayer.setWeight(0);
            }
            count.startCount(0, 1, time);
            activePlayer.getSecondPlayer().setAnimation(index);
        }else {
            // nu nu?
            activePlayer.getFirstPlayer().setAnimation(index); // tiesiog taip
        }
        looping = true;
        finished = false;
        update = true;
    }

    public void switchAnimations(String name, float time){
        Animation e = activePlayer.getSecondPlayer().getEntity().getAnimation(name);
        if (e == null){
            return;
        }
        if (time > 0){
            count.startCount(0, 1, time);
            activePlayer.getSecondPlayer().setAnimation(e);
        }else {
            activePlayer.getFirstPlayer().setAnimation(e); // tiesiog taip
            activePlayer.setWeight(0f); // nepamirstam sito, nes be counterio nieks neperjunks.
        }
        setAnimationTime(0);
        looping = true;
        finished = false;
        update = true;
    }

    /** updates all player. Calling this method will update players even if update is set to false. */
    public void update(){
//        System.out.println(Gdx.graphics.getFrameId());
        // butinai nustatom laika, del lago ir visa kita, reik perskaiciuot.
        //Animacija bus galima paveikt su Engine.worldSpeed.
        if (frameId != Gdx.graphics.getFrameId()) {
            float delta = Math.min(Engine.getDelta(), 0.25f); // kad nebutu nesamoniu su sokinejimais
            // Again just assuming we have 60 timeframe rate... Came from old code, so just left it like this *60...
            activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = (int) (animationSpeed * delta * 60f);
            frameId = Gdx.graphics.getFrameId();
        } else {
//            int speed = activePlayer.getFirstPlayer().speed;
            activePlayer.getFirstPlayer().speed = activePlayer.getSecondPlayer().speed = 0;
        }
//        if (!update && finished){
//            int length = activePlayer.getFirstPlayer().getAnimation().length-1;
//            activePlayer.getFirstPlayer().setTime(length);
//            activePlayer.getSecondPlayer().setTime(length); // kazko sitas bugina, kai animacija nebuna loop.
//            activePlayer.setTime(length*2);
//        }
        activePlayer.getFirstPlayer().update(); // reik update, nes gali but entity vienodu dydziu.
        if (count.isCounting()){ // neupdatinam antro, kam jo reik jei nenaudojam?
            activePlayer.getSecondPlayer().update();
        }
        activePlayer.update();
        if (!Gdx.graphics.isContinuousRendering()) { // kad animacija veiktu sklandziai.
            Gdx.graphics.requestRendering();
        }
    }

    // piesimas
    /** draws with current settings. */
    public void draw(){
        if (update)
            update();
//        else {
//            float old = animationSpeed;
//            animationSpeed = 0;
//            update(); // darom update vistiek, kad nepamestu vietos (kazkodel pameta.), tai turetu isprest ta beda.
//            animationSpeed = old;
//        }
        drawer.draw(activePlayer);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
//        float size;
//        boolean updatePos = false;
//        if (oldWidth != width || oldHeight != height) {
//            if (width > height) { // imsim mazesni, kad sutilptu.
//                size = height;
////                if (oldSize != size) {
//                float scale = size / getMinHeight();
//                if (getMinWidth() * scale > width) { // vadinas negalim naudot sito scale, nes islena uz ribu.
//                    scale = width / getMinWidth();
//                }
//                setScale(scale);
////                    oldSize = size;
//                updatePos = true;
////                }
//            } else {
//                size = width;
////                if (oldSize != size) {
//                float scale = size / getMinWidth();
//                if (getMinHeight() * scale > height) { // vadinas negalim naudot sito scale, nes islena uz ribu.
//                    scale = height / getMinHeight();
//                }
//                setScale(scale);
////                    oldSize = size;
//                updatePos = true;
////                }
//            }
//            oldWidth = width;
//            oldHeight = height;
//        }
        boolean updatePos = tryScaleSize(width, height);
        if (updatePos || oldX != x || oldY != y){ // pozicija butinai tik po dydzio, nes nesusigaudys tikros pozicijos.
            setPosition(x, y);
            oldX = x;
            oldY = y;
        }
        draw();
//        Engine p = Engine.getInstance();
//        p.getShapeRender().setTransformMatrix(p.getBatch().getTransformMatrix());
//        p.noFill();
//        p.stroke(0);
//        p.rect(x, y, width, height);
    }

    // dispose

    @Override
    public void dispose() {
        drawer.dispose();
    }

    /** Animation change listener */
    public interface AnimationChangedListener{
        /** Called when animation changing completed. */
        void animationChanged();
    }

    public interface AnimationFinishedListener{
        /** Called every time when animation is finished. */
        void animationFinished(Animation animation);
    }
}
