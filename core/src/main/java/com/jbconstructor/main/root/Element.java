package com.jbconstructor.main.root;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.core.Engine;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.widgets.SImage;
import com.jbconstructor.main.forms.editorForm.EditForm;

/** Simple element to represent an entity.
 * Texture, Texture regio, ninePatch, Animation. With physics polygons or without.
 */
public class Element extends SImage {
    // physics
    private PhysicsHolder physicsHolder = new PhysicsHolder();

    //id
    private String resName;

    // flipped image or not
    private boolean flipX, flipY;
    private boolean lock = false;

    // autoSize mums reiks tik viena kart - pirma kart idejus i editoriu. Daugiau mums jos nereiks.
    private boolean firstTime;

    public Element(ElementStyle style) {
        super(style);

        firstTime = true;
        readStyle(style);
    }

    /* works. */

    @Override
    protected void autoSize() {
        if (firstTime) {
            super.autoSize();
            firstTime = false;
        }
    }

    private void checkFlip(boolean x, boolean y){
         Drawable e = getImage();
        if (e instanceof SpriteDrawable){
            Sprite sprite = ((SpriteDrawable) e).getSprite();
            sprite.flip(x, y);
        }else if (e instanceof SpriterDrawable){
            SpriterDrawable drawable = (SpriterDrawable) e;
            drawable.flipAnimation(x, y);
        }else { // nebuvo galimybes flipping todel ir pazymim, kad ne.
            flipX = false;
            flipY = false;
        }
        // del kitu drawable tokiu kaip : nine patch ar ten maciau yra tile drawable tai neflipina ju arba neradau kaip.
    }

    public void updated(){
        auto();
        update = false;
    }

    /* like */

    /** lock it or unlock it. */
    public void lock(boolean lock){
        this.lock = lock;
    }

    /** flip image. */
    public void setFlip(boolean flipX, boolean flipY){
        boolean ox = flipX != this.flipX;
        boolean oy = flipY != this.flipY;
        this.flipY = flipY;
        this.flipX = flipX;
        checkFlip(ox, oy);
    }

    /** Is it locked */
    public boolean isLocked(){
        return lock;
    }

    /** is it flipped in x */
    public boolean isFlippedX(){
        return flipX;
    }

    /** is it flipped in y */
    public boolean isFlippedY(){
        return flipY;
    }

    /** Resource id. Usually drawable access id. */
    public String getResName(){
        return resName;
    }

    /** Changes resource id. Doesn't change drawable itself. */
    public void setResName(String name){
        resName = name;
    }

    /** physics holder of this resource. */
    public PhysicsHolder getPhysicsHolder(){
        return physicsHolder;
    }

    public void setImageNoCopy(Drawable e){
        super.setImage(e);
    }

    @Override
    public void setImage(Drawable e) {
        if (e == null){
            return;
        }
//        if (isAuto()){ // isjungsim auto size jeigu pries tai image jau buvo.
//            if (getImage() != null){
//                setAutoSizing(false);
//            }
//        }
//        Drawable e = st.image;
        // padarom kopijas, kad nebutu duplikatu ir leistu keist kaikuriuos parametrus.
        if (e instanceof SpriterDrawable){ // cia musu animacijos
            SpriterDrawable spriterDrawable = new SpriterDrawable((SpriterDrawable) e);
            super.setImage(spriterDrawable);
            spriterDrawable.enableUpdate(false);

            // animacijom reik vistiek flip patikrint.
            checkFlip(flipX, flipY); // paziurim kur turi ziuret.
        }else if (e instanceof TextureRegionDrawable){ // cia turetu but texturos ir texture regionai is atlas.
            Sprite sprite;
            TextureRegion region = ((TextureRegionDrawable) e).getRegion();
            if (region instanceof TextureAtlas.AtlasRegion){
                TextureAtlas.AtlasRegion atlasRegion = (TextureAtlas.AtlasRegion) region;
                sprite = new TextureAtlas.AtlasSprite(atlasRegion);
//                if (atlasRegion.rotate){// esme ta, kad sprite is good guy ir atvercia musu image jei jis paverstas, bet..
//                    // ideja ta, kad musu sistemai nepatinka tokie savavaliski veiksmai todel turim graziai pasakyt dedei sprite, kad
//                    // mums nereik tokiu pavertimu :)
//                    sprite.rotate90(false);
//                }

            }else {
                sprite = new Sprite(region);
            }
            // Kad drawable turetu originalu dydi.
            sprite.setSize(region.getRegionWidth(), region.getRegionHeight());
            super.setImage(new SpriteDrawable(sprite));

            // kadangi cia texture region buvo, tai reik flippint.
            checkFlip(flipX, flipY); // paziurim kur turi ziuret.
        }else if (e instanceof SpriteDrawable){ // dazniausiai yra kuriami sprite drawables.
            Sprite sprite = ((SpriteDrawable) e).getSprite();
            Sprite nsprite;
            if (sprite instanceof TextureAtlas.AtlasSprite){
                nsprite = new TextureAtlas.AtlasSprite((TextureAtlas.AtlasSprite) sprite);
            }else {
                nsprite = new Sprite(sprite);
            }
            // BUG apejimas, kai drawable ne toki dydi pasiima.
            nsprite.setSize(sprite.getRegionWidth(), sprite.getRegionHeight());

            // flipa reik atskirai patikrint, nes sprite atsimena ana.
            boolean fx = false, fy = false;
            // paziurim ar x flip atitinka.
            if (nsprite.isFlipX() != flipX){
                fx = true;
            }
            // y flip ar atitinka.
            if (nsprite.isFlipY() != flipY){
                fy = true;
            }

            // jei kazkuri neatitinka tai flipinam.
            if (fx || fy){
                nsprite.flip(fx, fy);
            }

            super.setImage(new SpriteDrawable(nsprite));
        }
    }

    /* style */

    @Override
    public ElementStyle getStyle() {
        ElementStyle st = new ElementStyle();
        copyStyle(st);
        return st;
    }

    public void copyStyle(ElementStyle st){
        super.copyStyle(st);
//        st.shapes.clear();
        st.resName = getResName();
//        st.bodyOrigin.set(bodyOrigin);
//        st.bodyType = bodyType;
//        st.isBodyOriginMiddle = isOriginMiddle;

        st.flipX = flipX;
        st.flipY = flipY;
        st.physicsHolder.copyHolder(physicsHolder);
//        if (v != null){
//            Form form = v.getForm();
//            if (form != null && form instanceof EditForm){
//        for (PhysicsEditor.FixtureShapeHolder e : shapes){ // tiesiog sukuriam copies fixturu.
//            st.shapes.add(new PhysicsEditor.FixtureShapeHolder().copy(e));
//        }
//                return;
//            }
//        }
//        st.shapes.addAll(shapes);
    }

    public void readStyle (ElementStyle st){
        super.readStyle(st);
//        shapes.clear();
//        InterfacesController v = getController();
//        shapes.clear();
//        shapes.addAll(st.shapes);
        resName = st.resName;
//        bodyOrigin.set(st.bodyOrigin);
//        bodyType = st.bodyType;
//        isOriginMiddle = st.isBodyOriginMiddle;

        flipX = st.flipX;
        flipY = st.flipY;

        physicsHolder.copyHolder(st.physicsHolder);

        setImage(st.image);
//        Drawable e = st.image;
//        // padarom kopijas, kad nebutu duplikatu ir leistu keist kaikuriuos parametrus.
//        if (e instanceof SpriterDrawable){ // cia musu animacijos
//            SpriterDrawable spriterDrawable = new SpriterDrawable((SpriterDrawable) e);
//            setImage(spriterDrawable);
//            spriterDrawable.enableUpdate(false);
//
//            // animacijom reik vistiek flip patikrint.
//            checkFlip(flipX, flipY); // paziurim kur turi ziuret.
//        }else if (e instanceof TextureRegionDrawable){ // cia turetu but texturos ir texture regionai is atlas.
//            Sprite sprite;
//            TextureRegion region = ((TextureRegionDrawable) e).getRegion();
//            if (region instanceof TextureAtlas.AtlasRegion){
//                TextureAtlas.AtlasRegion atlasRegion = (TextureAtlas.AtlasRegion) region;
//                sprite = new TextureAtlas.AtlasSprite(atlasRegion);
////                if (atlasRegion.rotate){// esme ta, kad sprite is good guy ir atvercia musu image jei jis paverstas, bet..
////                    // ideja ta, kad musu sistemai nepatinka tokie savavaliski veiksmai todel turim graziai pasakyt dedei sprite, kad
////                    // mums nereik tokiu pavertimu :)
////                    sprite.rotate90(false);
////                }
//
//            }else {
//                sprite = new Sprite(region);
//            }
//            // Kad drawable turetu originalu dydi.
//            sprite.setSize(region.getRegionWidth(), region.getRegionHeight());
//            setImage(new SpriteDrawable(sprite));
//
//            // kadangi cia texture region buvo, tai reik flippint.
//            checkFlip(flipX, flipY); // paziurim kur turi ziuret.
//        }else if (e instanceof SpriteDrawable){ // dazniausiai yra kuriami sprite drawables.
//            Sprite sprite = ((SpriteDrawable) e).getSprite();
//            Sprite nsprite;
//            if (sprite instanceof TextureAtlas.AtlasSprite){
//                nsprite = new TextureAtlas.AtlasSprite((TextureAtlas.AtlasSprite) sprite);
//            }else {
//                nsprite = new Sprite(sprite);
//            }
//            // BUG apejimas, kai drawable ne toki dydi pasiima.
//            nsprite.setSize(sprite.getRegionWidth(), sprite.getRegionHeight());
//            setImage(new SpriteDrawable(nsprite));
//        }
        // nine patch neina flip daryt. Arba neradau kaip. zodziu jei nori susirask pats.
//        else if (e instanceof NinePatchDrawable){
//
//        }

        // bug su sprite, kadangi sprite save kopijuoja tai ir flip palieka toki pat. Su situo gaunasi double flip.
//        checkFlip(flipX, flipY); // paziurim kur turi ziuret.
    }

    public static class ElementStyle extends SImageStyle{
        /** physics polygons. */
        public final PhysicsHolder physicsHolder = new PhysicsHolder();
//        public final Array<PhysicsEditor.FixtureShapeHolder> shapes = new Array<>();
        public String resName;

//        /** origin which will be used in physics. Rotating point. */
//        public final Vector2 bodyOrigin = new Vector2();

//        /** body type in physics. Default is dynamic. */
//        public BodyDef.BodyType bodyType = BodyDef.BodyType.DynamicBody;

//        /** set body origin to middle */
//        public boolean isBodyOriginMiddle = true;

        /** flip image. */
        public boolean flipX, flipY;

        public ElementStyle(){
            focusable = false;
            keepRatio = false; // engine update feature. jis blogas cia, nereikalingas.
        }

        @Override
        public Element createInterface() {
            Element e = new Element(this);
            Window window = Engine.getInstance().getActiveForm();
            if (window instanceof EditForm){
                e.setIdName(((EditForm) window).getPrefixId());
            }
            return e;
        }
    }
}
