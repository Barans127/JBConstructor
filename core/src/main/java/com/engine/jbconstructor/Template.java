package com.engine.jbconstructor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.badlogic.gdx.physics.box2d.joints.GearJoint;
import com.badlogic.gdx.physics.box2d.joints.GearJointDef;
import com.badlogic.gdx.physics.box2d.joints.MotorJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.PulleyJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.core.Engine;
import com.engine.core.ErrorMenu;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.Form;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.Window;
import com.engine.physics.Physics;
import com.engine.root.GdxWrapper;

import java.util.HashMap;

/** template as forms appearance.  */
public class Template {
    private Constructor owner;

    private String idName;

    private Array<Entity> entities;
    private Array<Control> interfaces;
    private Array<ChainBody> chains;
//    private boolean isChainsLoaded = false;

    private int backgroundColor;

    Template(Constructor owner){
        entities = new Array<>();
        chains = new Array<>();
        interfaces = new Array<>();
        this.owner = owner;
    }

    // para gavimas

    /** background color which is used in form. */
    public int getBackgroundColor(){
        return backgroundColor;
    }

    /** All interfaces created with this template. */
    public Array<Control> getInterfaces() {
        return interfaces;
    }

    /** Interface by given id name. */
    public Control getInterface(String name){
        for (int a= 0; a < interfaces.size; a++){
            Control e = interfaces.get(a);
            if (e.getIdName().equals(name)){
                return e;
            }
        }
        return null;
    }

    /** Interface by id from interfaces list. */
    public Control getInterface(int index){
        if (index >= 0 && index < interfaces.size){
            return interfaces.get(index);
        }
        return null;
    }

    public ChainBody getChainBody(String name){
        for (ChainBody e : chains){
//            if (.) // reik id name.
            if (e.getName().equals(name)){
                return e;
            }
        }
        return null;
    }

    public ChainBody getChainBody(int index){
        if (index >= 0 && index < chains.size){
            return chains.get(index);
        }
        return null;
    }

    public Array<ChainBody> getChains() {
        return chains;
    }

    /** @return entity by given name. null if not found */
    public Entity getEntity(String id){
        for (Entity e : entities){
            if (e.getId().equals(id)){
                return e;
            }
        }
        return null;
    }

    /** @return entity by given index. null if not found */
    public Entity getEntity(int index){
        if (index >= 0 && index < entities.size){
            return entities.get(index);
        }
        return null;
    }

    public Array<Entity> getEntities() {
        return entities;
    }

    /** Constructor of this template */
    public Constructor getConstructor() {
        return owner;
    }

    /** @return forms name. */
    public String getIdName(){
        return idName;
    }

    /* veikimas */

    /** Stops/restarts sound frustums.
     * @see Entity#stopSoundFrustum()  */
    public void stopSoundFrustums(){
        for (Entity e : entities){
            e.stopSoundFrustum();
        }
    }

    /** ARGB format. Change color before adding template to form else no effect or add this color to form yourself. */
    public void setBackgroundColor(int color){
        backgroundColor = color;
    }

    /** Inserts given Entity to list. Entity must not have duplicate id or else it will not be added to list.
     * @return if entity is added to list or not. */
    public boolean insertEntity(Entity e){
        return insertEntity(e, -1);
    }

    /** Inserts given Entity to list. Entity must not have duplicate id or else it will not be added to list.
     * @return if entity is added to list or not. */
    public boolean insertEntity(Entity e, int index){
        if (e != null){
            // tikrinam ar id tokio ner.
            for (Entity a : entities){
                if (a.getId().equals(e.getId())){
                    return false;
                }
            }
            // patikrinam index
            if (index >= 0 && index < entities.size){
                entities.insert(index, e);
            }else {
                entities.add(e);
            }
            return true;
        }
        return false;
    }

    /** Duplicates Entity and adds it to list.
     * NOTE: duplicated entity physics body is not created. You must create it yourself. */
    public Entity duplicateEntity(Entity e){
        return duplicateEntity(e, -1);
    }

    /** duplicates entity and adds it to given index. If index is out of bounds then entity is placed at the end of the list.
     * NOTE: duplicated entity physics body is not created. You must create it yourself. */
    public Entity duplicateEntity(Entity e, int index){
        // pirma randam id duplikatus.
        if (e == null){
            return null;
        }
        final String name = e.getId();
        String nName = name;
        int nameIndex = 0;
        MAIN:
        while (true){
            for (Entity a : entities){
                if (a.getId().equals(nName)){
                    nName = name + nameIndex;
                    nameIndex++;
                    continue MAIN;
                }
            }
            break;
        }
        // duplikatas pasalintas galim jau kurt nauja entity.
        // dabar nukopijuojam viska is old entity i musu nauja entity.
        Entity entity = new Entity(nName);
        entity.setResource(e.getResource());
//        entity.insertPolygons(e.getPolygonBody().getShapesInfo());
        entity.insertPolygons(e.getPolygonBody().getPhysicsShapesGroup());
        entity.setAngle(e.getAngle());
        entity.setSize(e.getWidth(), e.getHeight());
        entity.setPosition(e.getPosition());
        entity.setPositioning(MoreUtils.getPositionFromIndex(e.getPositioning()));

        entity.setBodyOrigin(e.getBodyOrigin());
//        entity.setBodyType(e.getBodyType());
        entity.setBodyOriginMiddle(e.isBodyOriginMiddle());
        entity.setTint(e.getTint());
        Body b = e.getBody();
        entity.setVisible(e.isVisible(), b == null ? e.isVisible() : b.isActive());
        // viska sumete dabar reik paziuret ar index ribose
        // tada idedam i sarasa pagal indexa.
        if (index >= 0 && index < entities.size){ // index ribose
            entities.insert(index, entity);
        }else { // index uz ribu.
            entities.add(entity); // tiesiog idedam i sarasa.
        }
        return entity;
    }

    /** Removes entity from given index. */
    public void removeEntity(int index){
        if (index >= 0 && index < entities.size){
            entities.removeIndex(index);
        }
    }

    /** Removes given Entity from list. */
    public void removeEntity(Entity e){
        entities.removeValue(e, true);
    }

    /** adds this template to given form. */
    public void insertToForm(Form e){
        if (e != null)
            e.setFormsTemplate(this); // tiesiog nustatys save i template.
    }

    /** loads physics to given physics world */
    public void loadPhysicsBodies(Physics world){
        if (world != null){
            for (ChainBody e : chains){
                e.initializeChain(world);
            }
            for (Entity e : entities){
                e.loadPolygons(world);
            }
        }
    }

    /** Clears physics. Physics can be recreated. Use {@link #loadPhysicsBodies(Physics)} to recreate physics. */
    public void destroyPhysicsBodies(){
        for (ChainBody e : chains){
            e.destroyChain();
        }
        for (Entity e : entities){
            e.destroyPolygons();
        }
    }

    /* joint initialization
    * Kad patys joint tokie nevisada pastovus, tai nelaikysim cia ju
    * juos tiesiog is save file sukurs ir atiduos. Joint nebus priziurimi. */

    /** Create joint def. If you are creating gear joint use other methods: {@link #createGearJointDef(String, Joint, Joint)}
     * or {@link #createGearJointDef(String, Physics, Array)}
     * NOTE: If you are creating joint with max force and/or max torque and you need these values to be multiplied with body mass then use method: {@link #createJointDef(String, boolean, boolean)}*/
    public JointDef createJointDef(String id){
        return createJointDef(id, null, null, null, null, false, false);
    }

    /** Create joint def. If you are creating gear joint use other methods: {@link #createGearJointDef(String, Joint, Joint)}
     * or {@link #createGearJointDef(String, Physics, Array)}
     * @param useAMass or useBMass - max force and max torque will be multiplied by body a and/or body b mass. If both are true then body a and body b mass is summed.
     * In order to use body mass as multiplier body itself must be a dynamic body or else it is ignored.
      */
    public JointDef createJointDef(String id, boolean useAMass, boolean useBMass){
        return createJointDef(id, null, null, null, null, useAMass, useBMass);
    }

    /** Create gear joint with already created prismatic or revolute joints */
    public JointDef createGearJointDef(String id, Joint joint1, Joint joint2){
        return createJointDef(id, joint1, joint2, null, null, false, false);
    }

    /** Create gear joint and other joints needed.
     * NOTE: World must not be stepping!
     * @param fillJoint prismatic and revolute joint will be created and added to this list. List can be null.*/
    public JointDef createGearJointDef(String id, Physics world, Array<Joint> fillJoint){
        return createJointDef(id, null, null, world, fillJoint, false, false);
    }

    /** creates Joint def from save file. Joint info must be completely filled in constructor.
     * @return created joint def. If not found null. */
    private JointDef createJointDef(String id, Joint joint1, Joint joint2, Physics physics, Array<Joint> fillJoint, boolean bodyAMass, boolean bodyBMass){
        SavedFileDecoder.ProjectForm form = null;
        for (SavedFileDecoder.ProjectForm e : owner.getDecoder().getProjectForms()){
            if (e.name.equals(getIdName())){
                form = e;
            }
        }
        if (form == null){
            Engine.getInstance().setError("Template: Cannot found template: " + getIdName(), ErrorMenu.ErrorType.ControlsError);
            return null;
        }
        for (SavedFileDecoder.JointBaseInfo e : form.jointsInfos){
            if (e.id.equals(id)){
                // surandam bodies.
                Body A = findBody(e.bodyA, e.bodyAResource);
                Body B = findBody(e.bodyB, e.bodyBResource);

                // neradom bodies.
                if (A == null || B == null){
                    Engine.getInstance().setError("Template",  "Failed creating joint def. Bodies was not found! Did you loaded physics?");
                    return null;
                }

                // apsirasom mass jei jos reiks. Ja pridesim prie max force arba torque.
                // kunas turi but dinaminis kitu atveju ignorinsim.
                float mass = 0;
                boolean noMass = true; // nustatyt ar yra mase.
                if (bodyAMass && A.getType() == BodyDef.BodyType.DynamicBody){
                    mass = A.getMass(); // a kuno mase
                    noMass = false;
                }
                if (bodyBMass && B.getType() == BodyDef.BodyType.DynamicBody){
                    mass += B.getMass(); // b kuno mase, ja pridedam prie mases jei kartais a mase prideta kartu.
                    noMass = false;
                }
                if (noMass){ // nera mases. dedam 1f kad nekeistu values.
                    mass = 1f; // nieko nekeis tada.
                }

                // joint kurimas.
                JointDef base;
                switch (e.type){
                    case 0:{ // distance
                        SavedFileDecoder.DistanceJointInfo info = (SavedFileDecoder.DistanceJointInfo) e;
                        DistanceJointDef def = new DistanceJointDef();
                        def.dampingRatio = info.dampingRatio;
                        def.frequencyHz = info.frequencyHz;
                        def.length = info.length * Physics.pixelToWord; // length reik paverst i world units.
                        def.localAnchorA.set(info.anchorA.x * Physics.pixelToWord, info.anchorA.y * Physics.pixelToWord);
                        def.localAnchorB.set(info.anchorB.x * Physics.pixelToWord, info.anchorB.y * Physics.pixelToWord);

                        base = def;
                        break;
                    }case 1: { // friction
                        SavedFileDecoder.FrictionJointInfo info = (SavedFileDecoder.FrictionJointInfo) e;
                        FrictionJointDef def = new FrictionJointDef();
                        def.localAnchorA.set(info.anchorA.x * Physics.pixelToWord, info.anchorA.y * Physics.pixelToWord);
                        def.localAnchorB.set(info.anchorB.x * Physics.pixelToWord, info.anchorB.y * Physics.pixelToWord);
                        def.maxForce = info.maxForce * mass; // dauginam is mases jei to reik
                        def.maxTorque = info.maxTorque * mass;

                        base = def;
                        break;
                    }case 2: { // gear
                        // gear bus du variantai. Arba user paduos tuos du jointus, arba user lieps cia sukurt
                        // paduodams dar ir array i kuri ides dar du katik sukurtus jointus.
                        SavedFileDecoder.GearJointInfo info = (SavedFileDecoder.GearJointInfo) e;
                        GearJointDef def = new GearJointDef();
                        def.ratio = info.ratio;

                        if (joint1 != null && joint2 != null){ // jointai jau sukurti
                            if ((joint1 instanceof RevoluteJoint || joint1 instanceof PrismaticJoint) &&
                                    (joint2 instanceof RevoluteJoint || joint2 instanceof PrismaticJoint)){
                                def.joint2 = joint2;
                                def.joint1 = joint1;
                            }else {
                                Engine.getInstance().setError("Template", "Cannot create gear joint. Gear joint must have prismatic or revolute" +
                                        " joints. Joints provided are not prismatic or revolute.");
                                return null;
                            }
                        }else { // jointai dar nesukurti ir tures but sukurti dabar.
                            if (physics == null){
                                // world butinas!
                                Engine.getInstance().setError("Template: Cannot create gear joint without world instance!", ErrorMenu.ErrorType.ControlsError);
                                return null;
                            }else {
                                JointDef def1 = createJointDef(info.joint1Id);
                                JointDef def2 = createJointDef(info.joint2Id);
                                if (def1 == null || def2 == null) { // nerado joint. Kazkas tokio. Dar error turetu but pries sita, tai rodys kame bedos.
                                    Engine.getInstance().setError("Template: Failed creating joints for gear joint! No prismatic or revolute joint found.", ErrorMenu.ErrorType.UnknowError);
                                    return null;
                                }else { // kurs joint tiesiogiai. world neturi but stepe!
                                    if ((def1 instanceof PrismaticJointDef || def1 instanceof RevoluteJointDef) && (def2 instanceof PrismaticJointDef ||
                                            def2 instanceof RevoluteJointDef)) {
                                        def.joint1 = physics.createJoint(def1);
                                        def.joint2 = physics.createJoint(def2);
                                    }else {
                                        Engine.getInstance().setError("Template", "Cannot create gear joint. Gear joint must have prismatic or revolute" +
                                                " joints. Joints provided are not prismatic or revolute.");
                                        return null;
                                    }
                                }
                            }
                        }

                        // metam i lista jei toki dave.
                        if (fillJoint != null){
                            fillJoint.add(def.joint1, def.joint2);
                        }

                        base = def;
                        break;
                    }case 3:{ // motor joint
                        SavedFileDecoder.MotorJointInfo info = (SavedFileDecoder.MotorJointInfo) e;
                        MotorJointDef def = new MotorJointDef();
                        def.angularOffset = info.angularOffset;
                        def.linearOffset.set(info.linearOffset.x * Physics.pixelToWord, info.linearOffset.y * Physics.pixelToWord);
                        def.correctionFactor = info.correctionFactor;
                        def.maxForce = info.maxForce * mass;
                        def.maxTorque = info.maxTorque * mass;

                        base = def;
                        break;
                    }case 4: { // mouse joint
                        SavedFileDecoder.MouseJointInfo info = (SavedFileDecoder.MouseJointInfo) e;
                        MouseJointDef def = new MouseJointDef();
                        def.dampingRatio = info.dampingRatio;
                        def.frequencyHz = info.frequencyHz;
                        def.maxForce = info.maxForce * mass;
                        def.target.set(info.target.x * Physics.pixelToWord, info.target.y * Physics.pixelToWord);

                        base = def;
                        break;
                    }case 5: { // prismatic
                        SavedFileDecoder.PrismaticJointInfo info = (SavedFileDecoder.PrismaticJointInfo) e;
                        PrismaticJointDef def = new PrismaticJointDef();
                        def.enableLimit = info.enableLimit;
                        def.enableMotor = info.enableMotor;
                        def.localAnchorA.set(info.anchorA.x * Physics.pixelToWord, info.anchorA.y * Physics.pixelToWord);
                        def.localAnchorB.set(info.anchorB.x * Physics.pixelToWord, info.anchorB.y * Physics.pixelToWord);
                        def.localAxisA.set(info.localAxisA); // cia nereik keist, nes cia kaip ir kampas.
                        def.lowerTranslation = info.lowerTranslation * Physics.pixelToWord; // kaip ir pozicijos nustatymai
                        def.upperTranslation = info.upperTranslation * Physics.pixelToWord; // reiktu i world unit verst.
                        def.maxMotorForce = info.maxMotorForce * mass;
                        def.motorSpeed = info.maxMotorSpeed;
                        def.referenceAngle = info.referenceAngle;

                        base = def;
                        break;
                    }case 6:{ // pulley
                        SavedFileDecoder.PulleyJointInfo info = (SavedFileDecoder.PulleyJointInfo) e;
                        PulleyJointDef def = new PulleyJointDef();
                        def.localAnchorA.set(info.anchorA.x * Physics.pixelToWord, info.anchorA.y * Physics.pixelToWord);
                        def.localAnchorB.set(info.anchorB.x * Physics.pixelToWord, info.anchorB.y * Physics.pixelToWord);
                        def.groundAnchorA.set(info.groundAnchorA.x * Physics.pixelToWord, info.groundAnchorA.y * Physics.pixelToWord);
                        def.groundAnchorB.set(info.groundAnchorB.x * Physics.pixelToWord, info.groundAnchorB.y * Physics.pixelToWord);
                        def.lengthA = info.lengthA * Physics.pixelToWord; // reik ilgi keist i world units.
                        def.lengthB = info.lengthB * Physics.pixelToWord;
                        def.ratio = info.ratio;

                        base = def;
                        break;
                    }case 7: { // revolute
                        SavedFileDecoder.RevoluteJointInfo info = (SavedFileDecoder.RevoluteJointInfo) e;
                        RevoluteJointDef def = new RevoluteJointDef();
                        def.localAnchorA.set(info.anchorA.x * Physics.pixelToWord, info.anchorA.y * Physics.pixelToWord);
                        def.localAnchorB.set(info.anchorB.x * Physics.pixelToWord, info.anchorB.y * Physics.pixelToWord);
                        def.enableLimit = info.enableLimit;
                        def.enableMotor = info.enableMotor;
                        def.lowerAngle = info.lowerAngle;
                        def.upperAngle = info.upperAngle;
                        def.maxMotorTorque = info.maxMotorTorque * mass;
                        def.motorSpeed = info.motorSpeed;
                        def.referenceAngle = info.referenceAngle;

                        base = def;
                        break;
                    }case 8: { // rope
                        SavedFileDecoder.RopeJointInfo info = (SavedFileDecoder.RopeJointInfo) e;
                        RopeJointDef def = new RopeJointDef();
                        def.localAnchorA.set(info.anchorA.x * Physics.pixelToWord, info.anchorA.y * Physics.pixelToWord);
                        def.localAnchorB.set(info.anchorB.x * Physics.pixelToWord, info.anchorB.y * Physics.pixelToWord);
                        def.maxLength = info.maxLength * Physics.pixelToWord;// reik keist ilgi i world units.

                        base = def;
                        break;
                    }case 9: { // weld joint
                        SavedFileDecoder.WeldJointInfo info = (SavedFileDecoder.WeldJointInfo) e;
                        WeldJointDef def = new WeldJointDef();
                        def.localAnchorA.set(info.anchorA.x * Physics.pixelToWord, info.anchorA.y * Physics.pixelToWord);
                        def.localAnchorB.set(info.anchorB.x * Physics.pixelToWord, info.anchorB.y * Physics.pixelToWord);
                        def.dampingRatio = info.dampingRatio;
                        def.frequencyHz = info.frequencyHz;
                        def.referenceAngle = info.referenceAngle;

                        base = def;
                        break;
                    }case 10: { // wheel
                        SavedFileDecoder.WheelJointInfo info = (SavedFileDecoder.WheelJointInfo) e;
                        WheelJointDef def = new WheelJointDef();
                        def.localAnchorA.set(info.anchorA.x * Physics.pixelToWord, info.anchorA.y * Physics.pixelToWord);
                        def.localAnchorB.set(info.anchorB.x * Physics.pixelToWord, info.anchorB.y * Physics.pixelToWord);
                        def.dampingRatio = info.dampingRatio;
                        def.enableMotor = info.enableMotor;
                        def.frequencyHz = info.frequencyHz;
                        def.localAxisA.set(info.localAxisA); // nereik vertimo, kampas lyg ir
                        def.maxMotorTorque = info.maxMotorTorque * mass;
                        def.motorSpeed = info.motorSpeed;

                        base = def;
                        break;
                    }
                    default: // neimanoma sukurt, neuzbaigtas joint.
                        Engine.getInstance().setError("Template: Cannot create joint. Joint doesn't have type selected!", ErrorMenu.ErrorType.ControlsError);
                        return null;
                }

//                base.bodyA = findBody(e.bodyA, e.bodyAResource);
//                base.bodyB = findBody(e.bodyB, e.bodyBResource);
                base.bodyA = A;
                base.bodyB = B;
                base.collideConnected = e.collideConnected;

//                if (base.bodyA == null || base.bodyB == null){
//                    Engine.getInstance().setError("Template: Failed creating joint def. Bodies was not found! Did you loaded physics?", ErrorMenu.ErrorType.UnknowError);
//                    return null;
//                }

                return base;
            }
        }
        return null;
    }

    // searches through entities and chains.
    private Body findBody(String id, boolean isEntity){
        if (isEntity){
            for (Entity e : entities){
                if (e.getId().equals(id)){
                    return e.getBody();
                }
            }
        }else {
            for (ChainBody e : chains){
                if (e.getName().equals(id)){
                    return e.getBody();
                }
            }
        }
        return null; // nieko nerado.
    }

    /* joint kurimas. */

    /** creates gear joint from save file with given id.
     * Joints are created not safely! Call this method outside world step!
     * @param joints other joints will be created and added to this list. Can be null.*/
    public GearJoint createGearJoint(Physics world, String id, Array<Joint> joints){
        JointDef def = createGearJointDef(id, world, joints);
        if (def != null){
            return (GearJoint) world.createJoint(def);
        }else {
            Engine.getInstance().setError("Template", "Cannot find gear joint def with id: " + id);
            return null;
        }
    }

    /** creates gear joint from save file with given id. Provided joints must not be null or else this method
     * will try to create it and definitely fail. Provided joints must be prismatic or revolute or else creation
     * will fail.
     * Joints are created not safely! Call this method outside world step!*/
    public GearJoint createGearJoint(Physics world, String id, Joint joint1, Joint joint2){
        JointDef def = createGearJointDef(id, joint1, joint2);
        if (def != null){
            return (GearJoint) world.createJoint(def);
        }else {
            Engine.getInstance().setError("Template", "Cannot find gear joint def with id: " + id);
            return null;
        }
    }

    /** create joint from save file with given id. Don't create gear joints here!
     * For gear joints use {@link #createGearJoint(Physics, String, Array)} or {@link #createGearJoint(Physics, String, Joint, Joint)}
     * Joints are created not safely! Call this method outside world step!*/
    public Joint createJoint(Physics world, String id){
        JointDef def = createJointDef(id);
        if (def == null){
            Engine.getInstance().setError("Template: Joint def not found with id: " + id, ErrorMenu.ErrorType.ControlsError);
            return null;
        }
        return world.createJoint(def);
    }

    /** create joint from save file with given id. For gear joints use {@link #createGearJointDef(String, Physics, Array)} or {@link #createGearJointDef(String, Joint, Joint)}
     * Joints are created not safely! Call this method outside world step!
     * @param useAMass or useBMass - max force and max torque will be multiplied by body a and/or body b mass. If both are true then body a and body b mass is summed.
     * In order to use body mass as multiplier body itself must be a dynamic body or else it is ignored.
     * */
    public Joint createJoint(Physics world, String id, boolean useAMass, boolean useBMass){
        JointDef def = createJointDef(id, useAMass, useBMass);
        if (def == null){
            Engine.getInstance().setError("Template: Joint def not found with id: " + id, ErrorMenu.ErrorType.ControlsError);
            return null;
        }
        return world.createJoint(def);
    }

    /* visu joint kurimas. */

    /** Will create all joints. Joints are created not safely! Call this method outside world step!
     * Joints who belongs to gear joint will not be added to list therefore you need to get those joints from gear joint!
     * @param useAMass or useBMass - max force and max torque will be multiplied by body a and/or body b mass. If both are true then body a and body b mass is summed.
     * In order to use body mass as multiplier body itself must be a dynamic body or else it is ignored.
     * @param jointList hashmap where joints will be placed. Joint id is used as a key. Can be null. */
    public void createAllJoints(Physics world, HashMap<String, Joint> jointList, boolean useAMass, boolean useBMass){
//        Array<JointDef> defs = new Array<>();
        HashMap<String, JointDef> defs = new HashMap<>();
        createAllJointsDef(world, defs, useAMass, useBMass);
//        for (JointDef e : defs){
//            Joint joint = world.createJoint(e);
//            if (jointList != null){
//                jointList.add(joint);
//            }
//        }
        // kuriam jointus ir dedam i lista.
        for (String key : defs.keySet()){
            JointDef def = defs.get(key);
            Joint joint = world.createJoint(def);
            if (jointList != null){
                jointList.put(key, joint);
            }
        }
    }

    /** Creates all joints def from save file. If joint is in gear joint list, then this joint def will not be created but joint will be created and placed
     * inside gear joint def.
     * @param world gear joint will need world to create joints. If there is gear joint then this method must be called outside world step!
     * @param jointDefs hashmap where all joint def will be placed. Joint id will be used as a key.
     * @param useAMass or useBMass - max force and max torque will be multiplied by body a and/or body b mass. If both are true then body a and body b mass is summed.
     * In order to use body mass as multiplier body itself must be a dynamic body or else it is ignored.
     * */
    public void createAllJointsDef(Physics world, HashMap<String, JointDef> jointDefs, boolean useAMass, boolean useBMass){
        if (jointDefs != null){
            SavedFileDecoder.ProjectForm form = null;
            for (SavedFileDecoder.ProjectForm e : owner.getDecoder().getProjectForms()){
                if (e.name.equals(getIdName())){
                    form = e;
                }
            }
            if (form == null){
                Engine.getInstance().setError("Template: Cannot find template: " + getIdName(), ErrorMenu.ErrorType.ControlsError);
                return;
            }
            // susirandam visus gear jointus.
            Array<SavedFileDecoder.GearJointInfo> gears = new Array<>();
            for (SavedFileDecoder.JointBaseInfo e : form.jointsInfos){
                if (e.type == 2){
                    gears.add((SavedFileDecoder.GearJointInfo) e);
                }
            }

            Array<SavedFileDecoder.JointBaseInfo> creatableDefs = new Array<>();
            // dabar imsim tik tuos jointus, kuriuos galim kurt.
            for (SavedFileDecoder.JointBaseInfo e : form.jointsInfos){
                if (e.type == 2){ // gear jointus ignorinsim, juos atskirai kursim.
//                    creatableDefs.add(e);
                    continue;
                }
                if (e.type == 5 || e.type == 7){ // tik sitie type gali but gear jointe.
                    boolean addToList = true;
                    for (SavedFileDecoder.GearJointInfo info : gears){
                        if (e.id.equals(info.joint1Id) || e.id.equals(info.joint2Id)){
                            addToList = false;
                            break; // priklauso gear jointui. Ignorinam
                        }
                    }
                    if (addToList) // nepriklauso.
                        creatableDefs.add(e);
                }else {
                    creatableDefs.add(e);
                }
            }

            // pirma kuriam gear jointus.
            for (SavedFileDecoder.GearJointInfo e : gears){
//                jointDefs.add(createGearJointDef(e.id, world, null));
                jointDefs.put(e.id, createGearJointDef(e.id, world, null));
            }

            // toliau paprasti joint
            for (SavedFileDecoder.JointBaseInfo e : creatableDefs){
//                jointDefs.add(createJointDef(e.id));
                jointDefs.put(e.id, createJointDef(e.id, useAMass, useBMass));
            }
        }
    }

    /* handle */

    public void handle(){
        for (int a = 0; a < entities.size; a++){
            Entity e = entities.get(a);
            if (e.isVisible() && e.getPositioning() != Window.fixedView){ // jeigu ne fixed, tai visada bus tik absolute. relative nieko nepakeis.
                e.handle();
            }
        }
    }

    public void fixHandle(){
        for (int a = 0; a < entities.size; a++){
            Entity e = entities.get(a);
            if (e.isVisible() && e.getPositioning() == Window.fixedView){
                e.draw(); // ant fixed update nedarys. vistiek ten tik su box2d fizikom susyje.
            }
        }
    }

    boolean create(SavedFileDecoder.ProjectForm info){ // subuildint viska cia tik.
        idName = info.name;

        // susigaudom musu background.
        int background;
        try {
            background = MoreUtils.hexToInt(info.background);
        }catch (NumberFormatException ex){
            background = 255;
        }
        this.backgroundColor = background;

        chains.clear();
        entities.clear();
        for (SavedFileDecoder.ProjectChain e : info.chainsInfos) { // chain pridejimas.
            ChainBody ch = new ChainBody(e);
            chains.add(ch);
        }
        for (SavedFileDecoder.ResourceInfo e : info.resourceInfos){
            if (e.type.equals("Resource") || e.type.equals("Element")){ // paprastas resource. Kitaip tariant entity.
                Drawable res = Resources.getDrawable(e.resource);
                if (res == null){
                    if (owner.ignoreNullResources){
                        Gdx.app.log("Template", "Did constructor loaded resources? Resource with given key was not found. Key: " + e.resource);
                        // suteikiam white image.
                        res = Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor"));
                    }else {
                        GdxWrapper.getInstance().setError("Did constructor loaded resources? Resource with given key was not found. Key: " + e.resource,
                                ErrorMenu.ErrorType.MissingResource);
                        return false;
                    }
                }
                Entity entity = new Entity(e.idName);
//                entity.setResource(res);
                if (e.flipX || e.flipY){
                    if (res instanceof SpriterDrawable){ // cia musu animacijos
                        // nestabdom nieko nedarom. Tiesiog padarom kopija ir flipinam.
                        SpriterDrawable spriterDrawable = new SpriterDrawable((SpriterDrawable) res); // padarom spriter kopija. nes darysim flip, originalo imt negalim
                        spriterDrawable.flipAnimation(e.flipX, e.flipY); // paverciam.
                        entity.setResource(spriterDrawable);
                    }else if (res instanceof TextureRegionDrawable) { // cia turetu but texturos ir texture regionai is atlas.
                        // kadangi reik flip daryt, tai turesim kurt kopija ir sprite, kuris ir padarys flipa.
                        Sprite sprite;
                        TextureRegion region = ((TextureRegionDrawable) res).getRegion();
                        if (region instanceof TextureAtlas.AtlasRegion) { // gali but paprastas region.
                            TextureAtlas.AtlasRegion atlasRegion = (TextureAtlas.AtlasRegion) region;
                            sprite = new TextureAtlas.AtlasSprite(atlasRegion);
                        } else if (region instanceof TextureAtlas.AtlasSprite) { // bet gali but ir toks.
                            sprite = new TextureAtlas.AtlasSprite((TextureAtlas.AtlasSprite) region);
                        } else { // o jei nei toks nei anoks, tai toks.
                            sprite = new Sprite(region);
                        }
                        sprite.flip(e.flipX, e.flipY);
                        entity.setResource(new SpriteDrawable(sprite));
                    }else if (res instanceof SpriteDrawable){ // jei kartai tai Sprite drawable.
                        Sprite sprite = ((SpriteDrawable) res).getSprite();
                        Sprite nSprite;
                        if (sprite instanceof TextureAtlas.AtlasSprite){
                            nSprite = new TextureAtlas.AtlasSprite((TextureAtlas.AtlasSprite) sprite);
                        }else {
                            nSprite = new Sprite(sprite);
                        }
                        nSprite.flip(e.flipX, e.flipY);
                        entity.setResource(new SpriteDrawable(nSprite));
                    }else { // ne tas resource, kad butu galima flippint.
                        entity.setResource(res);
                    }
                }else {
                    entity.setResource(res); // dedam toki koki dave.
                }

                // susirandam physics grupe.
                int index = e.physicsShapeGroupIndex;
                SavedFileDecoder.PhysicsShapesGroup group;
                if (index > -1){
                    SavedFileDecoder decoder = owner.getDecoder();
                    Array<SavedFileDecoder.PhysicsShapesGroup> physicsShapesGroups = decoder.getPhysicsShapeGroups();
                    if (index < physicsShapesGroups.size){ // turi tilpt sarase.
                        group = physicsShapesGroups.get(index); // teoriskai turi index atitikt grupes id.
                        // versijoms zemesnems nei 0.9 negalim patikrint ar teisingos fizikos parinktos, nes tiesiog jose nera id irasyto,
                        // todel jei index neatitinka, bet versija ne 0.9 ignorinam klaida.
                        if (decoder.getSaveVersion() >= 0.9f && group.id != index){ // paziurim ar tikrai tos fizikos.
                            // jeigu nerado, tai bandom ieskot.
                            boolean found = false;
//                            for (SavedFileDecoder.PhysicsShapesGroup shapesGroup : physicsShapesGroups){ // nested loop error...
                            for (int a= 0; a < physicsShapesGroups.size; a++){
                                SavedFileDecoder.PhysicsShapesGroup shapesGroup = physicsShapesGroups.get(a);
                                if (shapesGroup.id == index){
                                    group = shapesGroup; // perstatom naujas fizikas
                                    found = true;
                                    break;
                                }
                            }

                            if (!found){
                                // nerado.. Error gal mest?
                                Gdx.app.log("Template", "Physics was not found for element: " + e.idName);
                                group = null; // nope. nera fiziku.
                            }
//                            Engine.getInstance().setError("Template", "Error reading physics! Physics index and id doesn't match!");
//                            return false;
                        }
                    }else {
                        Gdx.app.log("Template", "Cannot locate physics for entity: " + e.idName);
                        group = null; // kazkas negerai. Gal buvo liestas save failas rankiniu budu???
                    }
                }else {
                    group = null;
                }

//                entity.insertPolygons(e.shapes);
                entity.insertPolygons(group);
                entity.rotate(e.radius);
                entity.setSize(e.width, e.height);
                entity.setPosition(e.x, e.y);
                entity.setPositioning(MoreUtils.getPositionFromIndex(e.positioning));

//                entity.setBodyOriginMiddle(e.isOriginMiddle);
                entity.setBodyOriginMiddle(group == null || group.isOriginMiddle);
                if (group != null) {
//                    entity.setBodyOrigin(e.bodyOrigin);
                    entity.setBodyOrigin(group.bodyOrigin);
                }
//                entity.setBodyType(e.bodyType);
                try {
                    String color = e.tint;
                    int converted = MoreUtils.hexToInt(color);
                    entity.setTint(converted);
                }catch (NumberFormatException ex){
                    continue; // ignoruojam sita.
                }
                entity.setResourceInfo(e); // nustatom sita, del galimybes restartint entity.
                entities.add(entity);
            }
        }
        return true;
    }

}
