package com.engine.jbconstructor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.ui.controls.Window;
import com.engine.ui.listeners.DrawListener;
import com.engine.ui.listeners.SoundFrustum;
import com.engine.physics.Physics;
import com.engine.root.GdxWrapper;

/** Single image or animation with physics polygons or not. */
public class Entity {
    private Drawable resource; // naudojama resource.
    private boolean isPolygonsLoaded = false;

    // duomenys
    private SavedFileDecoder.ResourceInfo resourceInfo; // sio entity pradiniai duomenys. Gali but null.
    private final Vector2 position;
    private float width, height, angle; // angle tai tiesiog pradinis angle.
    private int positioning; // ant fixed neveiks physics taskai.
    private final String idName;
    private int tint = 0xFFFFFFFF;
    private boolean visible = true; // tiesiog, kad vartotojas galetu padaryt ji nematoma. Aisku bus matoma default.
    private boolean drawOnScreen;

    // papildomi
//    private boolean hasPolygons; // sitas nustatyt ar turi ir/ar gali turet polygon taskus.
    private DrawListener drawListener; // jei kartais reikes kazka dapiest papildomai, tai sito pagalba tai galima padaryt.
    private SoundFrustum soundFrustum;
    private int soundDelay; // skaitliukas skaiciuot kada leist sound delay.

    //physics kunas
//    private BodyDef.BodyType bodyType;
    private Vector2 bodyOrigin;
    private boolean bodyOriginMiddle;
    private PolygonBody body; // tik vienas kunas gali but ant entity.

    public Entity(String idName){
        this.idName = idName;
        position = new Vector2();
        bodyOrigin = new Vector2();
//        body = new PolygonBody(); // sukuriam nauja body.

    }

    /* para keitimas. */

    /** Sound controller which knows where object is around the camera and how to play the sound by object position. */
    public void setSoundFrustum(SoundFrustum soundFrustum){
        this.soundFrustum = soundFrustum;
    }

    /** Listen when this entity is being drawn; */
    public void setDrawListener(DrawListener e){
        drawListener = e;
    }

    /* pats keitimas */
    /** set this entity visible or not. If entity has polygon body then polygon body active flag is set to entity's visible state.
     * Not safe to call if you are changing active flag of polygon! Check if world is not stepping first! */
    public void setVisible(boolean visible){
        setVisible(visible, visible); // des toki pati koks ir matomumas.
    }

    /** set this entity visible or not. Also if entity has polygon body - change it's active flag.
     * Not safe to call if you are changing active flag of polygon! Check if world is not stepping first! */
    public void setVisible(boolean visible, boolean activePolygon){
        this.visible = visible;
        if (!visible){
            // viskas, neber detales.
            if (soundFrustum != null){
                soundFrustum.stop();
            }
        }
        if (body.getBody() != null) { // visu pirma turi egzistuot toks.
            boolean e = body.getBody().isActive();
            if (e != activePolygon) { // neatitinka, reik keist.
                if (body.getWorld().isWorldStepping()){
                    // dedam error, box2d error uzknisantys, sitaip bus lengviau sugaudyt kur klaida.
                    Engine.getInstance().setError("Entity", "World is stepping! Cannot change visibility of body!");
                }else {
                    body.getBody().setActive(activePolygon);
                }
            }
        }
    }

    public void setBodyOriginMiddle(boolean middle){
        bodyOriginMiddle = middle;
        if (middle){ // nes i viduri, tai reik perskaiciuot.
            bodyOrigin.set(width/2, height /2);
        }
    }

//    public void setBodyType(BodyDef.BodyType e){
//        bodyType = e;
//    }

    public void setBodyOrigin(Vector2 origin){
        setBodyOrigin(origin.x, origin.y);
    }

    public void setBodyOrigin(float x, float y){
        if (!bodyOriginMiddle) { // nekeiciam jeigu middle.
            float realWidth = getResource().getMinWidth();
            float realHeight = getResource().getMinHeight();
            float widthRatio = getWidth() / realWidth;
            float heightRatio = getHeight() / realHeight;
            bodyOrigin.set(x * widthRatio, y * heightRatio);
        }
    }

    /** set angle in radians. */
    public void setAngle(float angle){
        this.angle = angle;
    }

    public void setPosition(Vector2 e){
        setPosition(e.x, e.y);
    }

    public void setPosition(float x, float y){
        position.set(x, y);
    }

    /** Change this before creating physics. Changing while physics active might attract undefined behaviour. */
    public void setSize(float width, float heigth){
        this.width = width;
        this.height = heigth;
        if (isBodyOriginMiddle()){
            bodyOrigin.set(width/2, heigth/2);
//            if (resource instanceof SpriterDrawable){
//                ((SpriterDrawable) resource).setScaledSize(width, heigth);
//                ((SpriterDrawable) resource).getActivePlayer().setPivot(((SpriterDrawable) resource).getScaledWidth()/2,
//                        ((SpriterDrawable) resource).getScaledHeight()/2);
//            }
        }
    }

    /** rotation in degrees */
    public void rotate(float degrees){
        angle = degrees * MathUtils.degreesToRadians;
    }

    /** polygons cannot work with fixed positioning. if positioning is set to fixed and there is polygon body then the body is destroyed. */
    public void setPositioning(Window.Position e){
        positioning = e.getPosition();
        if (positioning == Window.fixedView){
            destroyPolygons();
        }
    }

    /** ARGB format. */
    public void setTint(int color){
        tint = color;
    }

    public void setResource(Drawable e){
        if (e != null){
            resource = e;
//            if (resource instanceof SpriterDrawable){
//                ((SpriterDrawable) resource).getActivePlayer().setPivot(width/2, heigth/2);
//            }
        }
    }

    void setResourceInfo(SavedFileDecoder.ResourceInfo info){
        resourceInfo = info;
    }

    void insertPolygons(SavedFileDecoder.PhysicsShapesGroup shapesGroup){ // ides aprasymus.
        if (body == null){
            body = new PolygonBody(shapesGroup, this);
//            hasPolygons = array.size != 0;
            if (!body.hasShapes())
                setBodyOriginMiddle(true); // dedam per viduriuka.
        }
    }

    /** Creates this entity's physics. Safe to call. */
    public void loadPolygons(Physics e){
        if (body.hasShapes() && positioning != Window.fixedView) { // ant fixed pozicijos negali but jokiu polygonu.
            body.initializeShapes(e);
//            if (body.getPolygonBody() != null){ // kunas yra, viskas tvarkoj. Nenustatysi ar gerai, nes ten per listener kunas kuriamas.
            isPolygonsLoaded = true; // speliojimas. gal sukure, o gal ne.
//            }
        }
    }

    /** Destroys this entity's physics. Safe to call. */
    public void destroyPolygons(){
        if (isPolygonsLoaded){
            body.destroyBody();
            isPolygonsLoaded = false;
        }
    }

    /* para gavimas */

    /** This entity sound frustum. If not set then null. */
    public SoundFrustum getSoundFrustum(){
        return soundFrustum;
    }

    /** draw listener. If it wasn't set then null ir returned. */
    public DrawListener getDrawListener(){
        return drawListener;
    }

    /** @return true if this entity is visible on screen (not out of screen bounds).
     * If entity positioning is fixed than it might return false even if it is visible.*/
    public boolean isVisibleOnScreen(){
        return visible && drawOnScreen;
    }

    /** @return entity visible or not. */
    public boolean isVisible(){
        return visible;
    }

    /** @return is origin in middle */
    public boolean isBodyOriginMiddle() {
        return bodyOriginMiddle;
    }

//    /** Body type of this entity. */
//    public BodyDef.BodyType getBodyType() {
//        return bodyType;
//    }

    /** @return origin point of this entity */
    public Vector2 getBodyOrigin() {
        return bodyOrigin;
    }

    /** Resource used to draw this entity. */
    public Drawable getResource(){
        return resource;
    }

    /** If this Entity has body then this vector instance is always updated with body position.  */
    public Vector2 getPosition() {
        return position;
    }

    /** width of this entity. */
    public float getWidth(){
        return width;
    }

    /** height of this entity. */
    public float getHeight() {
        return height;
    }

    /** Angle of this body. Angle in radians. */
    public float getAngle() {
        return angle;
    }

    /** Positioning of this entity (absolute or fixed). Relative positioning is same as absolute.
     * @see com.engine.ui.controls.Window.Position */
    public int getPositioning() {
        return positioning;
    }

    /** ARGB */
    public int getTint() {
        return tint;
    }

    /** id of this entity. */
    public String getId() {
        return idName;
    }

    /** if this entity doesn't have body then null is return */
    public PolygonBody getPolygonBody() {
        return body;
    }

    /** @return box2d world body. if body was not created null is returned. */
    public Body getBody(){
        if (body != null)
            return body.getBody();
        else
            return null;
    }

    /** @return true if this entity has polygons. */
    public boolean hasPolygons(){
        return body.hasShapes();
    }

    /** @return true if this entity has already loaded polygons to world. */
    public boolean isPolygonsLoaded(){
        return isPolygonsLoaded;
    }

    /* veikimas, restartinimas, handlinimas */

    /** Calls frustum's {@link SoundFrustum#stop()} method. Prevents sound being mute when all sounds are stopped (by {@link com.engine.root.SoundController} etc)
     *  and frustum is not aware of it (it kinda restarts it). */
    public void stopSoundFrustum(){
        if (soundFrustum != null){
            soundFrustum.stop();
        }
    }

    /** Entity will be restarted to it's first phase(how it was defined in save file). Physics must not be created or else entity will not be
     * restarted. Entity must not be copied as copy doesn't have first phase and will not restarted.
     * Entity's drawable and flip will not be restarted.
     * Restarts: angle, size, position, positioning, bodyOrigin, bodyType, tint.
     * Visibility is set to true.*/
    public void restartEntity(){
        if (isPolygonsLoaded){
            Gdx.app.log("Entity", "Cannot restart Entity while physics are active!");
            return;
        }
        if (resourceInfo != null) { // turi but ne null
            // Kodas paimtas is template. Dalis kodo.
            SavedFileDecoder.ResourceInfo e = resourceInfo;
            rotate(e.radius);
            setSize(e.width, e.height);
            setPosition(e.x, e.y);
            setPositioning(MoreUtils.getPositionFromIndex(e.positioning));


//            setBodyOrigin(e.bodyOrigin);
//            setBodyType(e.bodyType);
//            setBodyOriginMiddle(e.isOriginMiddle);
            // daugiau ir nereik. Pats visas reiksmes restartins.
            body.reset(); // resetinam.
            SavedFileDecoder.PhysicsShapesGroup group = body.getPhysicsShapesGroup();
            setBodyOrigin(group.bodyOrigin);
            setBodyOriginMiddle(group.isOriginMiddle);
            try {
                String color = e.tint;
                int converted = MoreUtils.hexToInt(color);
                setTint(converted);
            } catch (NumberFormatException ignored) {
                // ignorinam.
            }

            // sito neliec template, bet mes palieciam
            setVisible(true);
        }else {
            Gdx.app.log("Entity", "Entity doesn't have first phase. Trying to restart copied entity?");
        }
    }

    /** if entity has body, then it updates it's location and other stuff. If there is no body, then nothing is done. */
    public void update(){
        if (isPolygonsLoaded) {
            Body body = this.body == null ? null : this.body.getBody();
            if (body != null) { // jeigu body yra.
//            float width, height;

                { // pirma padarom update.
                    // old.
                    Vector2 pos = body.getPosition();
                    Vector2 rpos = Physics.worldToPixel(pos.x, pos.y);
//                    rpos.set(rpos.x - bodyOrigin.x, rpos.y-bodyOrigin.y);
                    position.set(rpos);
                    angle = body.getAngle();
                }
//                if (body.isEnabled() && body.getType() == BodyDef.BodyType.DynamicBody) {
//                    // tada interpolate darom.
//                    Transform transform = body.getTransform();
//                    Vector2 bodyPosition = Physics.worldToPixel(transform.getPosition());
//                    float bodyAngle = body.getTransform().getRotation();
//
//                    float alpha = getPolygonBody().getWorld().getInterpolation();
//                    position.x = bodyPosition.x * alpha + position.x * (1f - alpha);
//                    position.y = bodyPosition.y * alpha + position.y * (1f - alpha);
//                    angle = bodyAngle * alpha + angle * (1f - alpha);
//                }
                position.set(position.x - bodyOrigin.x, position.y - bodyOrigin.y); // nustatom pagal origin kur yra.
            }
        }
    }

    /** draw this entity. frustum is not checked here. */
    public void draw(){
        Engine p = GdxWrapper.getInstance();
        if (drawListener != null){
            drawListener.preDraw();
        }
        p.tint(tint);
        if (resource instanceof TransformDrawable){
            ((TransformDrawable) resource).draw(p.getBatch(), position.x, position.y, bodyOrigin.x, bodyOrigin.y,
                    width, height, 1f, 1f, angle*MathUtils.radiansToDegrees);
//        }else if (resource instanceof SpriterDrawable){ // per daug darbo, matrica lengviau pasukt. Matricos sukimas proco neuzkrauna.
//            int likas = p.millis();
//            ((SpriterDrawable) resource).setAngle(angle*MathUtils.radiansToDegrees);
//            resource.draw(p.getBatch(), position.x, position.y, width, heigth);
//            System.out.println(getId() + ": " + (p.millis() - likas));
        }else { // reiktu pasukt. Sukam visa matrica.
//            int laikas = p.millis();
            if (angle != 0) {
                Matrix4 mat = p.getBatch().getTransformMatrix();
                mat.setToRotation(0, 0, 1, angle * MathUtils.radiansToDegrees);
                mat.trn(position.x + bodyOrigin.x, position.y + bodyOrigin.y, 0);
                p.getBatch().setTransformMatrix(mat);
                resource.draw(p.getBatch(), -bodyOrigin.x, -bodyOrigin.y, width, height);
//            e.end();
                mat.trn(0, 0, 0);
                mat.setToRotation(0, 0, 1, 0); // turėtų atstatyt.
//            e.begin();
                p.getBatch().setTransformMatrix(mat);
            }else {
                resource.draw(p.getBatch(), position.x, position.y, width, height);
            }
//            System.out.println(getId() + ": " + (p.millis() - laikas));
        }
//        resource.draw(p.getBatch(), position.x, position.y, width, heigth);
        p.noTint();
        if (drawListener != null){
            drawListener.postDraw();
        }
        // test
//        p.fill(0);
//        p.ellipse(position.x+bodyOrigin.x, position.y+bodyOrigin.y , 5, 5);
//        if (body.getPolygonBody() != null){
//            Body e = body.getPolygonBody();
//            Vector2 pos = e.getPosition();
//            p.fill(0xFFFF0000);
//            p.ellipse(pos.x * Physics.worldToPixel, pos.y * Physics.worldToPixel, 5, 5);
//        }
    }

    /** Updates camera frustum and sound frustum. Frustum is updated every step but if you manipulate with body (use transform), frustum can be wrong.
     * Call this to update body position and frustum. */
    public void updateFrustum(){
        handle(false);
    }

//    private Frustum frustum = new Frustum();
    final void handle(){ // del template apejimo, kad nereiktu kodo keist.
        handle(true);
    }

    // bendras metodas. updatina body pozicija, frustum ir draw gali jei reik.
    private void handle(boolean draw){
        update();

        OrthographicCamera cam = Engine.getInstance().getAbsoluteCamera();
//        float old = cam.zoom; // frustum test.
//        cam.zoom = 1f;
//        cam.update();
//        frustum.update(cam.invProjectionView);
//        cam.zoom = old;
//        cam.update();

        // didesnis fustrum dydis, nes kartais matos, kad detale atsiranda is oro kas tikrai nera gerai, geriau tegul buna piesiama netoli ribu, kad ir uz ju.
        // still better then drawing all at once.
        float size = Math.max(getWidth()/2, getHeight()/2) * 1.2f; // padidinam 10 procentu del viso pikto.
        if (angle == 0) { // kartais matos, kad nerode, todel reik pilno dydzio.
            drawOnScreen = cam.frustum.boundsInFrustum(position.x + getWidth() / 2, position.y + getHeight() / 2, 0, size, size, 0);
            handleSound(cam.position, cam.zoom, position.x + bodyOrigin.x, position.y + bodyOrigin.y);
            if (draw && drawOnScreen) {
                draw();
            }
        }else { // sitaip darom, nes tikrai nesimato, o kazkiek ten 'loss performance' del sito tai nieko tokio.
//            float size = getWidth() > getHeigth() ? getWidth() : getHeigth();
//            if (cam.frustum.boundsInFrustum(position.x + size / 2, position.y + size / 2, 0, size, size, 0)) {
//                draw();
//            }
            float rotateX = position.x, rotateY = position.y;

            float posX = rotateX + bodyOrigin.x;
            float posY = rotateY + bodyOrigin.y;
            float pointX = rotateX + getWidth()/2;
            float pointY = rotateY + getHeight()/2;
            float len = MoreUtils.dist(posX, posY, pointX, pointY);
            float angle = MathUtils.atan2(pointY - posY, pointX - posX) + getAngle();
            float rx, ry;
            rx = MathUtils.cos(angle) * len + posX;
            ry = MathUtils.sin(angle) * len + posY;
            // paziurim ar matomas ekrane.
            drawOnScreen = cam.frustum.boundsInFrustum(rx, ry, 0, size, size, 0);
            handleSound(cam.position, cam.zoom, rx, ry);
            if (draw && drawOnScreen) {
                draw();
            }
        }
    }

    // camera position, entity position. Called every time before drawing.
    private void handleSound(Vector3 cam, float zoom, float x, float y){
        if (soundFrustum != null) {
            // zoom kompensuoja kai kamera mato daugiau nei iprastai, bet garsu nera.
            // arba kai mazesne kamera ir aplink garsai is nematomu objektu.
            float dist = MoreUtils.dist(cam.x, cam.y, x, y); // pirma paziurim ant kiek nutoles objektas.

            float maxVolume = soundFrustum.getVolumeWidth() * zoom;
            float side = cam.x > x ? -1 : 1; // i kuria puse paninsims.
            if (dist < maxVolume) { // jeigu objektas pakankamai arti, darom update.

                // pan apskaiciavimas.
                float pan;
                float maxPanWidth = soundFrustum.getPanningWidth()*zoom; // max panning.
                float minPanWidth = soundFrustum.getMinPanningWidth()*zoom;
                float panDist = MoreUtils.abs(cam.x - x); // pan tik x reik skaiciuot.
                if (panDist > maxPanWidth) {
                    pan = 1 * side; // max.
                }else if (panDist < minPanWidth){
                    pan = 0; // min distance lauke. nustatom, per abi puses vienodai.
                } else {
                    pan = panDist / maxPanWidth * side; // paskaiciuojam kiek reik.
                }

                // garso nustatymai.
                float volume;
                float minVolume = soundFrustum.getMinVolumeWidth()*zoom;
                if (dist < minVolume){ // minimaliam atstume.
                    volume = 1f; // max volume.
                } else { // nustatom volume pagal atstuma.
                    volume = 1f - (dist / maxVolume);
                }

                // update kvieciam.
                soundFrustum.update(pan, volume);

                // pasakom, kad paleistu jeigu nepaleido dar.
                if (!soundFrustum.isPlaying()){
                    int time = Engine.getInstance().millis();
                    if (soundDelay < time) {
                        soundFrustum.start();
                        soundDelay = time + soundFrustum.getDelayTime();
                    }
                }
            }else if (soundFrustum.isPlaying()){ // objektas per toli.
                soundFrustum.update(1f*side, 0f); // kadangi objektas per toli.
                soundFrustum.stop();// jeigu grojo, tai pranesam, kad sustotu.
            }
        }
    }
}
