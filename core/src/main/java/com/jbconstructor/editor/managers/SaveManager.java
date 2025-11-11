package com.jbconstructor.editor.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.XmlWriter;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.Control;
import com.engine.jbconstructor.SavedFileDecoder;
import com.engine.root.GdxWrapper;
import com.jbconstructor.editor.dialogs.physicsEditor.PhysicsEditor;
import com.jbconstructor.editor.editors.ChainEdging;
import com.jbconstructor.editor.editors.JointManager;
import com.jbconstructor.editor.forms.StartForm;
import com.jbconstructor.editor.forms.editorForm.EditForm;
import com.jbconstructor.editor.root.Element;
import com.jbconstructor.editor.root.PhysicsHolder;
import com.jbconstructor.editor.root.Resizer;
import com.jbconstructor.editor.root.SelectiveInterfaceController;
import com.jbconstructor.editor.root.Selector;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

public class SaveManager {
    private volatile boolean saveNeed, autoSaveNeed;

    // auto save veikimui.
    private boolean autoSave; // true by default, bet jei projektas bent karta buvo isaugotas.
    private final Thread saver; // thread which will do autosave.
    private int autoSaveTime; // time when will auto save occurs. 10sec default.
    private volatile boolean saverIsSleeping = false;

    // sarasas naudotu resourcu projekte. cia del export
    private Array<String> list;

    public SaveManager(){
        list = new Array<>();
        saver = new Thread(new autoSaver());
        saver.setDaemon(true);
        saver.setName("Autosaver");
        autoSave = Resources.getPropertyBoolean("autoSave", true); // default true
        autoSaveTime = Resources.getPropertyInt("autoSaveTime", 10) * 1000; // default 10 sec.
        saver.start(); // paleidziam auto save, nesvarbu ar reik ar ne jo.
//        saver.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutDowner()));
    }

    /** If you something changed in project and that requires save, then this should be called.
     * Project will be marked as unsaved and auto saver will start. */
    public void triggerSave(){
        saveNeed = true;
        autoSaveNeed = true;

        // uzdedam simboli, kad butu aisku, kad neissaugotas.
        Gdx.graphics.setTitle("JBConstructor - " + Project.getProjectName() + "*");

        wakeWorker(); // pakeliam is gilaus miego autosaveri, jei auto save yra enablintas.
    }

    void projectCloses(){
        saveNeed = false;
        autoSaveNeed = false;
    }

    /** if project is saved and not need to be saved then true is returned, false otherwise */
    public boolean isProjectSaved(){
        return !saveNeed;
    }

    /** @return true if project was autoSaved. If autoSave disabled then autoSave never occurs.*/
    public boolean isProjectAutoSaved(){
        return !autoSaveNeed;
    }

    /** enable auto saving feature */
    public void enableAutoSave(boolean auto){
        autoSave = auto;
    }

    /** Is auto save enabled. */
    public boolean isAutoSaveEnabled(){
        return autoSave;
    }

    /** time in seconds. */
    public void setAutoSaveTime(float time){
        autoSaveTime = (int) (time*1000);
    }

    /** @return save time in seconds; */
    public float getAutoSaveTime(){
        return autoSaveTime/1000f;
    }

    /** wakes auto saving thread. Only wakes if auto saving is enabled and thread is in deep sleep. */
    private void wakeWorker(){
        if (autoSave && saverIsSleeping) {
            synchronized (saver) {
                saver.notify();
            }
        }
    }

    /** exports projects to given loaction. Copies all resources to given directory.
     * @param where folder path to which project will be exported. */
    public boolean exportProject(String where, boolean useJson, boolean compress){
        synchronized (saver) { // snchronizuojam, kad autosave nekeistu list saraso.
            String saveInfo;
            if (useJson){
                saveInfo = exportJson();
                if (saveInfo == null){
                    return false;
                }
            }else {
                StringWriter xml = save(true);
                if (xml == null) {
                    return false;
                }
                saveInfo = xml.toString();
            }
//            String file = where + "/" + Project.getProjectName() + ".jbcml";
            FileHandle dir = Gdx.files.absolute(where);
            if (!dir.isDirectory()) { // paziurim ar tai direktorija. Turi but direktorija, nes ten viska kelsim.
                return false;
            }
            // irasom resource faila.
            StringWriter writer = new StringWriter(); // resource file.
            writer.write("// auto generated resource file from JBConstructor\n");
            Array<String> loadedAtlases = new Array<>();
            // nuskaitom visu naudojamu resource id. is Project loadable resources list pagal id pasiimsim.
            for (int a = 0; a < Project.getLoadableResourcesCount(); a++){
                Project.ResourceToHold e = Project.getLoadableResource(a);
                if (e == null){
                    continue;
                }
                if (e.getIdName().equals(Resources.getProperty("whiteColor", "whiteSystemColor"))){
                    continue; // ignorinam sistemos image.
                }
//                int count = 0;
//                boolean found = false;
                LIST:
                for (String id : list){ // paziurim ar toks id yra sarase.
                    if (id.equals(e.getIdName())){ // radom tinkama.
                        if (e.e instanceof SpriterDrawable){ // animacija
//                            String text = e.atlasFilePath + " : " + id;
//                            writer.write(text + "\n");
                            String line = exportResourcePathFix(e.atlasFilePath, id, 2, where);
                            if (line == null)
                                return false;
                            writer.write(line + "\n");
                        } else {
                            if (e.atlasName == null){ // paprasta texture.
                                String path = ((FileTextureData)((TextureRegionDrawable) e.e).getRegion().getTexture().getTextureData()).getFileHandle().path();
//                                writer.write(path + " : " + id + "\n");
//                                writer.write(exportResourcePathFix(path, id, 0, where) + "\n");
                                String line = exportResourcePathFix(path, id, 0, where);
                                if (line == null)
                                    return false;
                                writer.write(line + "\n");
                            }else { // texture region, kuris priklauso kazkuriam atlasui.
                                for (String name : loadedAtlases){
                                    if (name.equals(e.atlasName)){ // jau buvo, ignoruojam tiesiog.
//                                        found = true;
                                        break LIST;
                                    }
                                }
                                // nera dar, atlasas dar neirasytas
//                                writer.write(exportResourcePathFix(e.atlasFilePath, e.atlasName, 1, where) + "\n");
                                String line = exportResourcePathFix(e.atlasFilePath, e.atlasName, 1, where);
                                if (line == null)
                                    return false;
                                writer.write(line + "\n");
//                                writer.write(e.atlasFilePath + " : " + e.atlasName + "\n");
                                loadedAtlases.add(e.atlasName);
                            }
                        }
//                        found = true;
                        break; // radus toliau nereik ieskot.
                    }
//                    count++;
                }
//                if (found){
//                    list.removeIndex(count);
//                }
            }
            String fileEnding;
            if (useJson){
                fileEnding = ".json";
            }else {
                fileEnding = ".export.jbcml";
            }
            FileHandle resFile = Gdx.files.absolute(where + "/" + Project.getProjectName() + "Resources.txt");
            resFile.writeString(writer.toString(), false); // irasom resource faila.
            FileHandle saveFile = Gdx.files.absolute(where + "/" + Project.getProjectName() + fileEnding);
            String info;
            if (compress){
                byte[] bytes = MoreUtils.compressText(saveInfo);
                info = new String(Base64Coder.encode(bytes));
//                info = new String(bytes);
            }else {
                info = saveInfo;
            }
            saveFile.writeString(info, false); // irasom save file. exporto pabaiga.
            return true;
        }
    }

    private void addToList(String res){
        if (!list.contains(res, false)){
            list.add(res);
        }
    }

    /** @param path current resource path. Resource will be copied to export directory and new internal path will be written to file.
     *  @return line which can be written to resources file.*/
    private String exportResourcePathFix(String path, String id, int state, String where){
        try {
            path = path.replace("\\", "/"); // sukeiciam i situo jei yra.
            FileHandle handle = Gdx.files.absolute(path);
            String resPath = where + "/resources";
            if (state == 0) { // paprasta texture
                FileHandle res = Gdx.files.absolute(resPath);
                if (!res.exists()) { // pakuriam folderius jei ner
                    res.mkdirs();
                }
                if (!res.isDirectory())
                    return null; // cia turi but direktorija, klaida jei ne
                handle.copyTo(Gdx.files.absolute(resPath)); // kopinam main.
                return "resources/" + path.substring(path.lastIndexOf("/") + 1) + " : " + id;
            } else if (state == 1) { // texture atlas.
                TextureAtlas atlas = Resources.getAtlas(id);
                String name = path.substring(path.lastIndexOf("/") + 1);
//            String lines = "";
                FileHandle location = Gdx.files.absolute(resPath + "/atlases");
                if (!location.exists()){ // pakuriam folderius.
                    location.mkdirs();
                }
                if (!location.isDirectory()){
                    return null;
                }
                handle.copyTo(location); // kopinam main. txt faila.
//            lines
                for (Texture e : atlas.getTextures()) { // nereik i lines rasyt, nes atlasas pats juos susiras.
                    String texturePath = ((FileTextureData) e.getTextureData()).getFileHandle().path();
//                lines += "resources/atlases/" + texturePath.substring(texturePath.lastIndexOf("/")+1, texturePath.length()) + "\n";
                    Gdx.files.absolute(texturePath).copyTo(location); // kopinam visas textures, kurias atlas naudos.
                }
                return "resources/atlases/" + name + " : " + id; // vel reik name.
            } else { // spriter animation, visa folderi krc kopint.
                FileHandle folder = Gdx.files.absolute(handle.file().getParent()); // ner ka keist, kopinam visa folderi ir viskas.
                String location = handle.file().getParent().replace("\\", "/");
                location = location.substring(location.lastIndexOf("/") + 1) + "/" +
                        path.substring(path.lastIndexOf("/") + 1); // idedam folder ir varda failo.
                FileHandle destination = Gdx.files.absolute(resPath + "/animations");
                if (!destination.exists()){
                    destination.mkdirs(); // pakuriam jei nera
                }
                if (!destination.isDirectory()){
                    return null; // turi but tik direktorija
                }
                folder.copyTo(destination); // nera galimybes suzinot kur texturos, todel visa folderi.
                return "resources/animations/" + location + " : " + id;
            }
        }catch (GdxRuntimeException ex){
            ex.printStackTrace();
            return null;
        }
    }

    private StringWriter save(){
        return save(false);
    }

    private String exportJson(){
        list.clear(); // cia, kad zinotu ka kopint.
        // surenkam pradine info apie pavadinima ir koks bus resource failo vardas.
        SavedFileDecoder.SavedFileDecoderInfo info = new SavedFileDecoder.SavedFileDecoderInfo();
        info.version = 0.9f; // nurodom kokia failo versija.
        info.projectName = Project.getProjectName();
        info.projectResourceFile = Project.getProjectName() + "Resources.txt";

        // toliau keliaujam per formas ir renkam is ju info.
        FormsManager manager = Project.getFormsManager();
        for (int a = 0; a < manager.getFormsSize(); a++){
            EditForm form = manager.getForm(a);

            // surasom bendra formos info.
            SavedFileDecoder.ProjectForm projectForm = new SavedFileDecoder.ProjectForm();
            projectForm.background = Integer.toHexString(form.getBackgroundColor());
            projectForm.name = manager.getFormName(a);

            // Surenkam chain info.
//            ChainEdging chainManager = form.getMainPanel().getChainEdgePanel().getChainEdgingManager();
            ChainEdging chainManager = form.getChainEdging();
            synchronized (form.getChainEdging()) { // autosave gali luzt, jei uztaikysim chain redagavima ir sito loop vaiksciojima.
                int size = chainManager.getChains().size;
                if (size > 0) { // irasynesim, tik jei bus ka irasynet.
                    for (int k = 0; k < size; k++){
                        // paimam chain.
                        ChainEdging.Chains chain = chainManager.getChains().get(k);
                        // sukuriam ta kur info desis.
                        SavedFileDecoder.ProjectChain projectChain = new SavedFileDecoder.ProjectChain();
                        //sudedam info
                        projectChain.name = chain.name;
                        projectChain.loop = chain.loop;
                        projectChain.isSensor = chain.isSensor;
                        projectChain.groupIndex = chain.groupIndex;
                        projectChain.categoryBits = chain.categoryBits;
                        projectChain.maskBits = chain.maskBits;
                        projectChain.x.addAll(chain.x);
                        projectChain.y.addAll(chain.y);

                        // paemus visa info sita info atiduodam formos info.
                        projectForm.chainsInfos.add(projectChain);
                    }
                }
            }

            // joint info irasymas.
            JointManager jointManager = form.getJointManager();
            synchronized (form.getJointManager()){
                int size = jointManager.getJointSize();
                if (size > 0){
                    for (int k = 0; k < size; k++){
                        JointManager.JointInfo joint = jointManager.getJointInfo(k);
                        SavedFileDecoder.JointBaseInfo baseInfo;

                        switch (joint.getJointType()){
                            case 0: { // distance
                                SavedFileDecoder.DistanceJointInfo e = new SavedFileDecoder.DistanceJointInfo();
                                e.length = joint.length;
                                e.frequencyHz = joint.frequencyHz;
                                e.dampingRatio = joint.dampingRatio;
                                e.anchorA.set(joint.anchorA);
                                e.anchorB.set(joint.anchorB);
                                baseInfo = e;
                                break;
                            }
                            case 1:{ // friction
                                SavedFileDecoder.FrictionJointInfo e = new SavedFileDecoder.FrictionJointInfo();
                                e.maxForce = joint.maxForce;
                                e.maxTorque = joint.maxTorque;
                                e.anchorA.set(joint.anchorA);
                                e.anchorB.set(joint.anchorB);
                                baseInfo = e;
                                break;
                            }
                            case 2:{ // gear
                                SavedFileDecoder.GearJointInfo e = new SavedFileDecoder.GearJointInfo();
                                e.joint1Id = joint.joint1ID;
                                e.joint2Id = joint.joint2ID;
                                e.ratio = joint.ratio;
                                baseInfo = e;
                                break;
                            }
                            case 3: { // motor
                                SavedFileDecoder.MotorJointInfo e = new SavedFileDecoder.MotorJointInfo();
                                e.angularOffset = joint.length;
                                e.maxForce = joint.maxForce;
                                e.maxTorque = joint.maxTorque;
                                e.correctionFactor = joint.ratio;
                                e.linearOffset.set(joint.anchorA);
                                baseInfo = e;
                                break;
                            }
                            case 4: { // mouse
                                SavedFileDecoder.MouseJointInfo e = new SavedFileDecoder.MouseJointInfo();
                                e.maxForce = joint.maxForce;
                                e.frequencyHz = joint.frequencyHz;
                                e.dampingRatio = joint.dampingRatio;
                                e.target.set(joint.anchorA);
                                baseInfo = e;
                                break;
                            }
                            case 5: { // prismatic
                                SavedFileDecoder.PrismaticJointInfo e = new SavedFileDecoder.PrismaticJointInfo();
                                e.referenceAngle = joint.length;
                                e.enableLimit = joint.enableLimit;
                                e.lowerTranslation = joint.frequencyHz;
                                e.upperTranslation = joint.dampingRatio;
                                e.enableMotor = joint.enableMotor;
                                e.maxMotorForce = joint.maxForce;
                                e.maxMotorSpeed = joint.maxTorque;
                                e.anchorA.set(joint.anchorA);
                                e.anchorB.set(joint.anchorB);
                                e.localAxisA.set(joint.localAxisA);
                                baseInfo = e;
                                break;
                            }
                            case 6: { // pulley
                                SavedFileDecoder.PulleyJointInfo e = new SavedFileDecoder.PulleyJointInfo();
                                e.lengthA = joint.maxForce;
                                e.lengthB = joint.maxTorque;
                                e.ratio = joint.ratio;
                                e.anchorA.set(joint.anchorA);
                                e.anchorB.set(joint.anchorB);
                                e.groundAnchorA.set(joint.localAxisA);
                                e.groundAnchorB.set(joint.groundAnchorB);
                                baseInfo = e;
                                break;
                            }
                            case 7: { // revolute
                                SavedFileDecoder.RevoluteJointInfo e = new SavedFileDecoder.RevoluteJointInfo();
                                e.referenceAngle = joint.length;
                                e.enableLimit = joint.enableLimit;
                                e.lowerAngle = joint.frequencyHz;
                                e.upperAngle = joint.dampingRatio;
                                e.enableMotor = joint.enableMotor;
                                e.motorSpeed = joint.maxForce;
                                e.maxMotorTorque = joint.maxTorque;
                                e.anchorA.set(joint.anchorA);
                                e.anchorB.set(joint.anchorB);
                                baseInfo = e;
                                break;
                            }
                            case 8: { // rope
                                SavedFileDecoder.RopeJointInfo e = new SavedFileDecoder.RopeJointInfo();
                                e.maxLength = joint.length;
                                e.anchorA.set(joint.anchorA);
                                e.anchorB.set(joint.anchorB);
                                baseInfo = e;
                                break;
                            }
                            case 9: { // weld
                                SavedFileDecoder.WeldJointInfo e = new SavedFileDecoder.WeldJointInfo();
                                e.referenceAngle = joint.length;
                                e.frequencyHz = joint.frequencyHz;
                                e.dampingRatio = joint.dampingRatio;
                                e.anchorA.set(joint.anchorA);
                                e.anchorB.set(joint.anchorB);
                                baseInfo = e;
                                break;
                            }
                            case 10: { // wheel
                                SavedFileDecoder.WheelJointInfo e = new SavedFileDecoder.WheelJointInfo();
                                e.enableMotor = joint.enableMotor;
                                e.maxMotorTorque = joint.maxTorque;
                                e.motorSpeed = joint.maxForce;
                                e.frequencyHz = joint.frequencyHz;
                                e.dampingRatio = joint.dampingRatio;
                                e.anchorA.set(joint.anchorA);
                                e.anchorB.set(joint.anchorB);
                                e.localAxisA.set(joint.localAxisA);
                                baseInfo = e;
                                break;
                            }
                            default: // type neparinktas dedam default paprasta.
                                baseInfo = new SavedFileDecoder.JointBaseInfo();
                                break;
                        }

                        // bendra info.
                        baseInfo.id = joint.getJointID();
                        baseInfo.type = joint.getJointType();
                        baseInfo.bodyA = joint.bodyA;
                        baseInfo.bodyB = joint.bodyB;
                        baseInfo.collideConnected = joint.collideConnected;
                        baseInfo.bodyAResource = joint.bodyAIsResource;
                        baseInfo.bodyBResource = joint.bodyBIsResource;
                        //metam i forma.
                        projectForm.jointsInfos.add(baseInfo);
                    }
                }
            }

            // dabar imsim resource infos.
            SelectiveInterfaceController selectiveInterfaceController = (SelectiveInterfaceController) form.getController();
            MAINLOOP:
            for (Control control : selectiveInterfaceController.getControls()) {
                // praleidziam to ko nereik
                if (control instanceof Resizer || control instanceof Selector) { // resizer ir selector nereik.
                    continue; // ignoruojam situos.
                }
                for (Control ex : selectiveInterfaceController.getExceptionList()) { // ignoruojamos kontroles.
                    if (control == ex) {
                        continue MAINLOOP; // exeption liste, reiks neleist.
                    }
                }
                if (control instanceof Element) {
                    Element resource = (Element) control;

                    // kuriam resource info
                    SavedFileDecoder.ResourceInfo projectResource = new SavedFileDecoder.ResourceInfo();
                    // surenkam info
                    projectResource.type = resource.getClass().getSimpleName();
                    Vector2 pos = resource.getPosition();
                    projectResource.x = pos.x;
                    projectResource.y = pos.y;
                    projectResource.radius = resource.getAngle();
                    projectResource.positioning = resource.getPositioning();
                    projectResource.idName = resource.getIdName();
                    projectResource.width = resource.getWidth();
                    projectResource.height = resource.getHeight();
                    projectResource.tint = Integer.toHexString(resource.getImageTint());
                    projectResource.resource = resource.getResName();
                    addToList(resource.getResName()); // kad kopintu musu resources i ten kur exportinam.
                    projectResource.flipX = resource.isFlippedX();
                    projectResource.flipY = resource.isFlippedY();

//                    // Dabar reik imt fizikas
//                    // Kadangi fizikos gan daznai kartojas, nes naudojama panasios specifikacijos - grupuosim
//                    // nedesim paciu fiziku, o kursim grupes ir jei tokiu pat fiziku nesaugosim. Fiziku info desim i grupes, o informacija kelsim
//                    // tik su skaicium (index, kuris nurodys kuria grupe issaugota info).
////                    Array<PhysicsEditor.FixtureShapeHolder> shapes = resource.getShapes();
                    PhysicsHolder holder = resource.getPhysicsHolder();
//                    Array<PhysicsEditor.FixtureShapeHolder> shapes = holder.shapes;
//                    if (shapes.size > 0){
//                        // yra fizikos, dabar eisim per grupes ir ziuresim ar nera tokio pacio.
//                        boolean createGroup = true;
//                        PHYSICSLOOP:
//                        for (int shapeIndex = 0; shapeIndex < info.shapesGroup.size; shapeIndex++){
//                            SavedFileDecoder.PhysicsShapesGroup e = info.shapesGroup.get(shapeIndex);
////                            if (e.bodyType != resource.getBodyType()){
//                            if (e.bodyType != holder.bodyType){
//                                continue; // neatitinka body type.
//                            }
//                            if (shapes.size != e.shapes.size){
//                                continue; // neatitinka dydis, tesiam toliau
//                            }
////                            if (e.isOriginMiddle != resource.isBodyOriginMiddle()){
//                            if (e.isOriginMiddle != holder.isBodyOriginMiddle){
//                                continue; // neatitinka origin. Tesiam toliau
//                            }
////                            Vector2 origin = resource.getBodyOrigin();
//                            Vector2 origin = holder.bodyOrigin;
//                            if (!e.isOriginMiddle && e.bodyOrigin.x != origin.x || e.bodyOrigin.y != origin.y){ // jeigu origin ne middle, tai turi atitikt origino parametrai. Jeigu middle true, tai nesvarbu.
//                                continue; // origin ne middle ir taskai neatitinka. Tesiam toliau
//                            }
//
//
//                            // dabar reiks eit per visas figuras ir ziuret ar tokia yra.
//                            for (SavedFileDecoder.PhysicsFixtureShapes saveShape : e.shapes){
//                                SHAPELOOP:
//                                for (PhysicsEditor.FixtureShapeHolder shapeHolder : shapes){
//                                    /* paziurim bendrus parametrus */
//                                    // ar turi tiek pat tasku.
//                                    if (saveShape.x.size != shapeHolder.x.size || saveShape.y.size != shapeHolder.y.size){
//                                        continue; // neatitinka pointu dydziai.
//                                    }
//
//                                    // sensor tikrinam
//                                    if (saveShape.isSensor != shapeHolder.isSensor){ // neatitinka sensor
//                                        continue;
//                                    }
//                                    if (saveShape.categoryBits != shapeHolder.categoryBits){
//                                        continue; // neatitinka bitai
//                                    }
//                                    if (saveShape.density != shapeHolder.density){
//                                        continue; // neatitinka density
//                                    }
//                                    if (saveShape.friction != shapeHolder.friction){
//                                        continue; // neatitinka friction
//                                    }
//                                    if (saveShape.groupIndex != shapeHolder.groupIndex){
//                                        continue; // neatitinka group index
//                                    }
//                                    if (saveShape.maskBits != shapeHolder.maskBits){
//                                        continue; // neatitinka mask bitai.
//                                    }
//                                    if (saveShape.radius != shapeHolder.radius){
//                                        continue; // neatitinka radius.
//                                    }
//                                    if (saveShape.restitution != shapeHolder.restitution){
//                                        continue; // neatitinka restitution
//                                    }
//                                    if (saveShape.type != shapeHolder.type){
//                                        continue; // neatitinka type.
//                                    }
//
//                                    // dabar lieka paziuret kaip su fizikos taskais.
//                                    // taskai turi eit paraleliskai todel galima abu vienu ratu tikrint ir ziuret ar vienodi.
//                                    for (int count = 0; count < saveShape.x.size && count < saveShape.y.size; count++){
//                                        float sx = saveShape.x.get(count), sy = saveShape.y.get(count);
//                                        float hx = shapeHolder.x.get(count), hy = shapeHolder.y.get(count);
//                                        if (sx != hx || sy != hy){// x arba y taskai neatitinka, einam toliau.
//                                            continue SHAPELOOP;
//                                        }
//                                    }
//
//                                    // jeigu atejo iki cia, tai radom identiskas, egzistuojancias fizikas
//                                    // vadinas sios fizikos jau yra grupese, tai tik paimsim grupes indexa.
//                                    projectResource.physicsShapeGroupIndex = shapeIndex;
//                                    createGroup = false;
//                                    break PHYSICSLOOP;
//                                }
//                            }
//                        }
//
//                        // susiziurejo, o dabar paziurim ar reik nauja fizikos grupe kurt
//                        if (createGroup){
//                            // kuriam fizikos grupe.
//                            SavedFileDecoder.PhysicsShapesGroup physicsShapesGroup = new SavedFileDecoder.PhysicsShapesGroup();
////                            physicsShapesGroup.isOriginMiddle = resource.isBodyOriginMiddle();
////                            physicsShapesGroup.bodyOrigin.set(resource.getBodyOrigin());
////                            physicsShapesGroup.bodyType = resource.getBodyType();
//                            physicsShapesGroup.isOriginMiddle = holder.isBodyOriginMiddle;
//                            physicsShapesGroup.bodyOrigin.set(holder.bodyOrigin);
//                            physicsShapesGroup.bodyType = holder.bodyType;
//
//                            // sumetam shapes.
//                            for (PhysicsEditor.FixtureShapeHolder e : shapes){
//                                SavedFileDecoder.PhysicsFixtureShapes physicsFixtureShapes = new SavedFileDecoder.PhysicsFixtureShapes();
//                                physicsFixtureShapes.density = e.density;
//                                physicsFixtureShapes.friction = e.friction;
//                                physicsFixtureShapes.restitution = e.restitution;
//                                physicsFixtureShapes.isSensor = e.isSensor;
//                                physicsFixtureShapes.categoryBits = e.categoryBits;
//                                physicsFixtureShapes.maskBits = e.maskBits;
//                                physicsFixtureShapes.groupIndex = e.groupIndex;
//                                physicsFixtureShapes.type = e.type;
//                                physicsFixtureShapes.radius = e.radius;
//
//                                physicsFixtureShapes.x.addAll(e.x);
//                                physicsFixtureShapes.y.addAll(e.y);
//                                // idedam i grupe shape.
//                                physicsShapesGroup.shapes.add(physicsFixtureShapes);
//                            }
//
//                            projectResource.physicsShapeGroupIndex = info.shapesGroup.size; // nurodom kuris, kadangi isides vienas, tai sitas tinka
//                            info.shapesGroup.add(physicsShapesGroup); // idedam grupe.
//                        }
//                    }else {
//                        // nera isvis fiziku.
//                        projectResource.physicsShapeGroupIndex = -1; // pazymim, kad nera.
//                    }

                    projectResource.physicsShapeGroupIndex = createPhysicsGroup(holder, info.shapesGroup);

                    // idedam resource i forma
                    projectForm.resourceInfos.add(projectResource);
                }
            }
            // surinkus info pacia forma deda i sarasa.
            info.projectForms.add(projectForm);
        }

        // visa info paimta ir sudeta i formas.
        // dabar versim i json viska.
        Json json = new Json();
        return json.toJson(info);
//        return json.prettyPrint(info);
    }

    private StringWriter save(boolean export){
        list.clear(); // isvalom sarasa.
        StringWriter stringWriter = new StringWriter();
        // be sito lievai. Notepadai visokie nemato, kad xml...
        if (!export) // ant export nesvaistom vietos.
            stringWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        XmlWriter xmlwriter = new XmlWriter(stringWriter);
        try {
            xmlwriter.element("JBC_data"); // atidarem pagrindini
            xmlwriter.attribute("version", 0.9);
            /* some project stuff */
            xmlwriter.element("Project");
            xmlwriter.attribute("name", Project.getProjectName());
            if (!export) {
//                xmlwriter.attribute("root", Project.getProjectRootFolderPath());
                String path = Project.getResourcesPath();
                if (path != null && path.startsWith(Project.getProjectRootFolderPath())){ // padarom localia vieta, kad butu galima pernest faila.
                    path = path.replaceFirst(Project.getProjectRootFolderPath(), ".");
                }
                xmlwriter.attribute("resourcesFolder", path);
            }else {
                xmlwriter.attribute("export", true);
                xmlwriter.attribute("resources", Project.getProjectName() + "Resources.txt"); // resources failas.
            }
            xmlwriter.pop(); // uzdarom project.

            /* usableresources atsiminimui. */
            if (!export) {
                xmlwriter.element("Resources"); // visos resource
                Array<String> atlasesCompleted = new Array<>(); // kad zinotume, kad atlas jau yra.
                for (int a = 0; a < Project.getLoadableResourcesCount(); a++) {
                    Project.ResourceToHold e = Project.getLoadableResource(a);
                    if (e.getIdName().equals(Resources.getProperty("whiteColor", "whiteSystemColor"))){
                        continue; // ignorinam sistemos image.
                    }
                    boolean showPath = true; //  jeigu atlaso dalis, tai path nereik.
                    if (e.tab != 3 && e.atlasName != null) { // is atlaso sitas.
                        boolean hasName = true;
                        for (String name : atlasesCompleted) { // ar si resource nebuvo is sito atlaso
                            if (name.equals(e.atlasName)) { // buvo jau, nekeisim.
                                hasName = false;
                                break;
                            }
                        }
                        if (hasName) { // atlasas pirma kart cia.
                            atlasesCompleted.add(e.atlasName);
                            xmlwriter.element("Atlas"); // pridedam jo aprasyma.
                            xmlwriter.attribute("name", e.atlasName);
                            String path = e.atlasFilePath;
                            if (path != null){ // darom lokalia vieta.
                                path = path.replace("\\", "/");
                                if (path.startsWith(Project.getProjectRootFolderPath())){
                                    path = path.replaceFirst(Project.getProjectRootFolderPath(), ".");
                                }
                            }
                            xmlwriter.attribute("path", path);
                            xmlwriter.pop();
                        }
//                        if (e.shapes.size == 0) { // paziurim ar yra fizikos tasku.
//                            continue;
//                        } else {
//                            showPath = false; // taskai yra, bet path nedesim.
//                        }
                        continue;
                    }
                    xmlwriter.element("resource");
                    xmlwriter.attribute("id", e.getIdName()); // isaugom is kur paimta res
                    if (e.atlasName != null) {
                        xmlwriter.attribute("atlas", e.atlasName); // kuriam atlasui priklauso, bus vietoj path.
                    }
                    if (showPath) { // path susigaudimas.
                        String path = null;
                        FileTextureData data = null;
//                int type = 0; // 0 - simple texture, 1 - region, 2 - ninepatch, 3 - sprite..
                        if (e.e instanceof TextureRegionDrawable) {
//                    type = 0;
                            data = (FileTextureData) ((TextureRegionDrawable) e.e).getRegion().getTexture().getTextureData();
                        } else if (e.e instanceof NinePatchDrawable) {
//                    type = 2;
                            data = (FileTextureData) ((NinePatchDrawable) e.e).getPatch().getTexture().getTextureData();
                        } else if (e.e instanceof SpriteDrawable) {
//                    type = 3;
                            data = (FileTextureData) ((SpriteDrawable) e.e).getSprite().getTexture().getTextureData();
                        } else if (e.e instanceof SpriterDrawable) {
                            path = ((SpriterDrawable) e.e).getName();
                        } else {
                            path = "path was not found";
                        }
                        if (data != null)
                            path = data.getFileHandle().path();
                        if (path != null){
                            path = path.replace("\\", "/"); // darom lokalia vieta.
                            if (path.startsWith(Project.getProjectRootFolderPath())){
                                path = path.replaceFirst(Project.getProjectRootFolderPath(), ".");
                            }
                        }
                        xmlwriter.attribute("path", path); /// ????
                    }
//                    if (e.shapes.size > 0) { // jei turi fizikos tasku, tai juos irgi issaugom.
//                        xmlwriter.element("phcs");
//                        for (PhysicsEditor.FixtureShapeHolder holder : e.shapes) {
//                            xmlwriter.element("figure");
//                            xmlwriter.attribute("type", holder.type);
//                            xmlwriter.attribute("radius", holder.radius);
////                            xmlwriter.element("settings");
//                            xmlwriter.attribute("density", holder.density);
//                            xmlwriter.attribute("friction", holder.friction);
//                            xmlwriter.attribute("restitution", holder.restitution);
//                            xmlwriter.attribute("sensor", holder.isSensor);
//                            xmlwriter.attribute("categoryBits", holder.categoryBits);
//                            xmlwriter.attribute("maskBits", holder.maskBits);
//                            xmlwriter.attribute("group", holder.groupIndex);
////                            xmlwriter.pop(); // uzdarom settings.
//                            for (int k = 0; k < holder.x.size; k++) {
//                                xmlwriter.element("point");
//                                xmlwriter.attribute("x", holder.x.get(k));
//                                xmlwriter.attribute("y", holder.y.get(k));
//                                xmlwriter.pop(); // uzdarom point
//                            }
//                            xmlwriter.pop();// uzdarom figure
//                        }
//                        xmlwriter.pop(); // uzdarom phcs
//                    }
                    xmlwriter.pop(); // uzdaro resource
                }
                xmlwriter.pop(); // uzdarom resources
            }

            // cia desim musu fizikas. Visu formu, visos bendrai eina.
            Array<SavedFileDecoder.PhysicsShapesGroup> physicsShapesGroups = new Array<>();
            /* toliau formu info ir ju isdestytos kontroles. */
            FormsManager manager = Project.getFormsManager();
            for (int a = 0; a < manager.getFormsSize(); a++){
                EditForm e = manager.getForm(a);
                xmlwriter.element("FORM");
                xmlwriter.attribute("name", manager.getFormName(a));
                // background.
                String backgroundHex = Integer.toHexString(e.getBackgroundColor());
                xmlwriter.attribute("background", backgroundHex);

                SelectiveInterfaceController contr = (SelectiveInterfaceController) e.getController();
                if (!export) { // nereik kameros ir settings. juos tik constructorius naudoja.
                    xmlwriter.element("camera"); // kur kamera stovejo.
                    if (GdxWrapper.getInstance().getActiveForm() != e) {
                        xmlwriter.attribute("x", e.getSavedCameraX()).attribute("y", e.getSavedCameraY())
                                .attribute("zoom", e.getSavedCameraZoom());
                    } else { // forma dabar atidaryta todel reik imt gyvus parametrus.
                        OrthographicCamera cam = GdxWrapper.getInstance().getAbsoluteCamera();
                        xmlwriter.attribute("x", cam.position.x).attribute("y", cam.position.y).attribute("zoom", cam.zoom);
                    }
                    xmlwriter.pop(); // uzdarom camera
                    xmlwriter.element("Settings"); // formos nustatymai.
                    xmlwriter.attribute("showFix", !contr.isDisableFixedDraw());
                    xmlwriter.attribute("showAbsolute", !contr.isDisableAbsoluteDraw());
                    xmlwriter.attribute("screenBounds", e.isShowingScreendBounds());
                    xmlwriter.attribute("showCoords", e.isCoordsVisible());
                    xmlwriter.pop(); // uzdarom settings
                }
                // chain edge dalys
                ChainEdging chain = e.getChainEdging();
                synchronized (e.getChainEdging()) { // autosave gali luzt, jei uztaikysim chain redagavima ir sito loop vaiksciojima.
                    if (chain.getChains().size > 0) { // irasynesim, tik jei bus ka irasynet.
                        xmlwriter.element("chains");
                        for (int j = 0; j < chain.getChains().size; j++) { // apeinam nested loop error.
                            ChainEdging.Chains ch = chain.getChains().get(j);
                            xmlwriter.element("chain");
                            xmlwriter.attribute("name", ch.name);
                            // issaugom tik jei ne default parametrai.
                            if (ch.loop)
                                xmlwriter.attribute("loop", ch.loop);
                            if (ch.isSensor)
                                xmlwriter.attribute("isSensor", ch.isSensor);
                            if (ch.maskBits != -1)
                                xmlwriter.attribute("maskBits", ch.maskBits);
                            if (ch.categoryBits != 0x0001)
                                xmlwriter.attribute("categoryBits", ch.categoryBits);
                            if (ch.groupIndex != 0)
                                xmlwriter.attribute("groupIndex", ch.groupIndex);
                            for (int k = 0; k < ch.x.size; k++) {
                                xmlwriter.element("point");
                                xmlwriter.attribute("x", ch.x.get(k));
                                xmlwriter.attribute("y", ch.y.get(k));
                                xmlwriter.pop(); // uzdarom point.
                            }
                            xmlwriter.pop(); // uzdarom chain
                        }
                        xmlwriter.pop(); // uzdarom chains
                    }
                }

                // joint dalys
                synchronized (e.getJointManager()) {
                    JointManager jointManager = e.getJointManager();
                    if (jointManager.getJointSize() > 0) { // visu pirma joint turi egzistuot.
                        xmlwriter.element("joints");
                        for (int k = 0; k < jointManager.getJointSize(); k++){
                            JointManager.JointInfo info = jointManager.getJointInfo(k);
                            xmlwriter.element("joint");
                            // bendra info.
                            xmlwriter.attribute("id", info.getJointID());
                            xmlwriter.attribute("type", info.getJointType());
                            xmlwriter.attribute("bodyA", info.bodyA == null ? "" : info.bodyA);
                            xmlwriter.attribute("bodyB", info.bodyB == null ? "" : info.bodyB);
                            xmlwriter.attribute("bodyAResource", info.bodyAIsResource);
                            xmlwriter.attribute("bodyBResource", info.bodyBIsResource);
                            xmlwriter.attribute("colliedConnected", info.collideConnected);

                            // toliau skirtingai pagal jointus.
                            switch (info.getJointType()){
                                case 0: // distance joint
                                    xmlwriter.attribute("length", info.length);
                                    xmlwriter.attribute("frequencyHz", info.frequencyHz);
                                    xmlwriter.attribute("dampingRatio", info.dampingRatio);
                                    xmlwriter.element("anchorA");
                                    xmlwriter.attribute("x", info.anchorA.x);
                                    xmlwriter.attribute("y", info.anchorA.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("anchorB");
                                    xmlwriter.attribute("x", info.anchorB.x);
                                    xmlwriter.attribute("y", info.anchorB.y);
                                    xmlwriter.pop();
                                    break;
                                case 1: // friction
                                    xmlwriter.attribute("maxForce", info.maxForce);
                                    xmlwriter.attribute("maxTorque", info.maxTorque);
                                    xmlwriter.element("anchorA");
                                    xmlwriter.attribute("x", info.anchorA.x);
                                    xmlwriter.attribute("y", info.anchorA.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("anchorB");
                                    xmlwriter.attribute("x", info.anchorB.x);
                                    xmlwriter.attribute("y", info.anchorB.y);
                                    xmlwriter.pop();
                                    break;
                                case 2: // gear
                                    xmlwriter.attribute("joint1Id", info.joint1ID == null ? "" : info.joint1ID);
                                    xmlwriter.attribute("joint2Id", info.joint2ID == null ? "" : info.joint2ID);
                                    xmlwriter.attribute("ratio", info.ratio);
                                    break;
                                case 3: // motor joint
                                    xmlwriter.attribute("angularOffset", info.length);
                                    xmlwriter.attribute("maxForce", info.maxForce);
                                    xmlwriter.attribute("maxTorque", info.maxTorque);
                                    xmlwriter.attribute("correctionFactor", info.ratio);
                                    xmlwriter.element("linearOffset");
                                    xmlwriter.attribute("x", info.anchorA.x);
                                    xmlwriter.attribute("y", info.anchorA.y);
                                    xmlwriter.pop();
                                    break;
                                case 4: // mouse joint
                                    xmlwriter.attribute("maxForce", info.maxForce);
                                    xmlwriter.attribute("frequencyHz", info.frequencyHz);
                                    xmlwriter.attribute("dampingRatio", info.dampingRatio);
                                    xmlwriter.element("target");
                                    xmlwriter.attribute("x", info.anchorA.x);
                                    xmlwriter.attribute("y", info.anchorA.y);
                                    xmlwriter.pop();
                                    break;
                                case 5: // prismatic joint
                                    xmlwriter.attribute("referenceAngle", info.length);
                                    xmlwriter.attribute("enableLimit", info.enableLimit);
                                    xmlwriter.attribute("lowerTranslation", info.frequencyHz);
                                    xmlwriter.attribute("upperTranslation", info.dampingRatio);
                                    xmlwriter.attribute("enableMotor", info.enableMotor);
                                    xmlwriter.attribute("maxForce", info.maxForce);
                                    xmlwriter.attribute("motorSpeed", info.maxTorque);
                                    xmlwriter.element("anchorA");
                                    xmlwriter.attribute("x", info.anchorA.x);
                                    xmlwriter.attribute("y", info.anchorA.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("anchorB");
                                    xmlwriter.attribute("x", info.anchorB.x);
                                    xmlwriter.attribute("y", info.anchorB.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("localAxisA");
                                    xmlwriter.attribute("x", info.localAxisA.x);
                                    xmlwriter.attribute("y", info.localAxisA.y);
                                    xmlwriter.pop();
                                    break;
                                case 6: // pulley
                                    xmlwriter.attribute("lengthA", info.maxForce);
                                    xmlwriter.attribute("lengthB", info.maxTorque);
                                    xmlwriter.attribute("ratio", info.ratio);
                                    xmlwriter.element("anchorA");
                                    xmlwriter.attribute("x", info.anchorA.x);
                                    xmlwriter.attribute("y", info.anchorA.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("anchorB");
                                    xmlwriter.attribute("x", info.anchorB.x);
                                    xmlwriter.attribute("y", info.anchorB.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("groundAnchorA");
                                    xmlwriter.attribute("x", info.localAxisA.x);
                                    xmlwriter.attribute("y", info.localAxisA.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("groundAnchorB");
                                    xmlwriter.attribute("x", info.groundAnchorB.x);
                                    xmlwriter.attribute("y", info.groundAnchorB.y);
                                    xmlwriter.pop();
                                    break;
                                case 7: // revolute
                                    xmlwriter.attribute("referenceAngle", info.length);
                                    xmlwriter.attribute("enableLimit", info.enableLimit);
                                    xmlwriter.attribute("lowerAngle", info.frequencyHz);
                                    xmlwriter.attribute("upperAngle", info.dampingRatio);
                                    xmlwriter.attribute("enableMotor", info.enableMotor);
                                    xmlwriter.attribute("motorSpeed", info.maxForce);
                                    xmlwriter.attribute("motorTorque", info.maxTorque);
                                    xmlwriter.element("anchorA");
                                    xmlwriter.attribute("x", info.anchorA.x);
                                    xmlwriter.attribute("y", info.anchorA.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("anchorB");
                                    xmlwriter.attribute("x", info.anchorB.x);
                                    xmlwriter.attribute("y", info.anchorB.y);
                                    xmlwriter.pop();
                                    break;
                                case 8: // rope
                                    xmlwriter.attribute("maxLength", info.length);
                                    xmlwriter.element("anchorA");
                                    xmlwriter.attribute("x", info.anchorA.x);
                                    xmlwriter.attribute("y", info.anchorA.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("anchorB");
                                    xmlwriter.attribute("x", info.anchorB.x);
                                    xmlwriter.attribute("y", info.anchorB.y);
                                    xmlwriter.pop();
                                    break;
                                case 9: // weld
                                    xmlwriter.attribute("referenceAngle", info.length);
                                    xmlwriter.attribute("frequencyHz", info.frequencyHz);
                                    xmlwriter.attribute("dampingRatio", info.dampingRatio);
                                    xmlwriter.element("anchorA");
                                    xmlwriter.attribute("x", info.anchorA.x);
                                    xmlwriter.attribute("y", info.anchorA.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("anchorB");
                                    xmlwriter.attribute("x", info.anchorB.x);
                                    xmlwriter.attribute("y", info.anchorB.y);
                                    xmlwriter.pop();
                                    break;
                                case 10: // wheel
                                    xmlwriter.attribute("enableMotor", info.enableMotor);
                                    xmlwriter.attribute("motorTorque", info.maxTorque);
                                    xmlwriter.attribute("motorSpeed", info.maxForce);
                                    xmlwriter.attribute("frequencyHz", info.frequencyHz);
                                    xmlwriter.attribute("dampingRatio", info.dampingRatio);
                                    xmlwriter.element("anchorA");
                                    xmlwriter.attribute("x", info.anchorA.x);
                                    xmlwriter.attribute("y", info.anchorA.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("anchorB");
                                    xmlwriter.attribute("x", info.anchorB.x);
                                    xmlwriter.attribute("y", info.anchorB.y);
                                    xmlwriter.pop();
                                    xmlwriter.element("localAxisA");
                                    xmlwriter.attribute("x", info.localAxisA.x);
                                    xmlwriter.attribute("y", info.localAxisA.y);
                                    xmlwriter.pop();
                                    break;
                            }
                            xmlwriter.pop();
                        }
                        xmlwriter.pop();
                    }
                }

                MAINLOOP:
                for (Control c : contr.getControls()){
                    if (c instanceof Resizer || c instanceof Selector){
                        continue; // ignoruojam situos.
                    }
                    for (Control ex : contr.getExceptionList()){
                        if (c == ex){
                            continue MAINLOOP; // exeption liste, reiks neleist.
                        }
                    }
                    xmlwriter.element("Item"); // daiktu info
                    xmlwriter.attribute("type", c.getClass().getSimpleName());
                    xmlwriter.element("param");
                    Vector2 pos = c.getPosition();
                    xmlwriter.attribute("x", pos.x).attribute("y", pos.y);
//                    xmlwriter.attribute("width", )
                    xmlwriter.attribute("angle", c.getAngle());
                    xmlwriter.attribute("positioning", c.getPositioning());
                    xmlwriter.attribute("idName", c.getIdName());
                    if (c instanceof Element){ // paprastas resource.
                        Element resource = (Element) c;
                        xmlwriter.attribute("width", resource.getWidth()).attribute("height", resource.getHeight());
                        String hex = Integer.toHexString(resource.getImageTint());
                        xmlwriter.attribute("tint", hex);
                        xmlwriter.attribute("resource", resource.getResName());
                        addToList(resource.getResName()); // idedam i sarasa, kad zinotume, kad sis resource yra naudojamas projekte.

                        // pridedam flip. Flip irasys tik tuo atveju jei objektas flipintas.
                        if (resource.isFlippedX()){
                            xmlwriter.attribute("flipX", true);
                        }
                        if (resource.isFlippedY()){
                            xmlwriter.attribute("flipY", true);
                        }

                        // pasigaunam fizikos grupe.
                        int index = createPhysicsGroup(resource.getPhysicsHolder(), physicsShapesGroups);

                        // irasom grupe.
                        xmlwriter.attribute("physicsGroupIndex", index);
//                        /* fizikos. */
//                        PhysicsHolder physicsHolder = resource.getPhysicsHolder();
//                        if (physicsHolder.shapes.size > 0){ // jei turi fizikos tasku, tai juos irgi issaugom.
//                            xmlwriter.element("phcs");
//                            xmlwriter.element("Body");
////                            xmlwriter.attribute("BodyType", resource.getBodyType()); // irasom body type.
////                            xmlwriter.attribute("originMiddle", resource.isBodyOriginMiddle());
////                            if (!resource.isBodyOriginMiddle()) { // jeigu jis per viduri, tai situ net nereik.
////                                xmlwriter.attribute("x", resource.getBodyOrigin().x); // irasom body origin.
////                                xmlwriter.attribute("y", resource.getBodyOrigin().y);
////                            }
//                            xmlwriter.attribute("BodyType", physicsHolder.bodyType); // irasom body type.
//                            xmlwriter.attribute("originMiddle", physicsHolder.isBodyOriginMiddle);
//                            if (!physicsHolder.isBodyOriginMiddle) { // jeigu jis per viduri, tai situ net nereik.
//                                xmlwriter.attribute("x", physicsHolder.bodyOrigin.x); // irasom body origin.
//                                xmlwriter.attribute("y", physicsHolder.bodyOrigin.y);
//                            }
//                            xmlwriter.pop(); // uzdarom body.
//                            for (PhysicsEditor.FixtureShapeHolder holder : physicsHolder.shapes){
//                                xmlwriter.element("figure");
//                                xmlwriter.attribute("type", holder.type);
//                                xmlwriter.attribute("radius", holder.radius);
////                                xmlwriter.element("settings");
//                                xmlwriter.attribute("density", holder.density);
//                                xmlwriter.attribute("friction", holder.friction);
//                                xmlwriter.attribute("restitution", holder.restitution);
//                                xmlwriter.attribute("sensor", holder.isSensor);
//                                xmlwriter.attribute("categoryBits", holder.categoryBits);
//                                xmlwriter.attribute("maskBits", holder.maskBits);
//                                xmlwriter.attribute("group", holder.groupIndex);
////                                xmlwriter.pop(); // uzdarom settings.
//                                for (int k = 0; k < holder.x.size; k++){
//                                    xmlwriter.element("point");
//                                    xmlwriter.attribute("x", holder.x.get(k));
//                                    xmlwriter.attribute("y", holder.y.get(k));
//                                    xmlwriter.pop(); // uzdarom point
//                                }
//                                xmlwriter.pop();// uzdarom figure
//                            }
//                            xmlwriter.pop(); // uzdarom phcs
//                        }
                    }
//                    else { // interface - gali but mygtukas ir t.t.
//                        // kazka daryt su juo.
//                    }
                    xmlwriter.pop(); // param uzdarymas

                    xmlwriter.pop(); // item uzdarymas
                }
                xmlwriter.pop(); // uzdarom FORM
            }

            if (physicsShapesGroups.size > 0) { // fizikas kurt tik jeigu jos yra...
                xmlwriter.element("Phcs"); // fizikos elementai.
                // fizikos grupes rasom ant pacios pabaigos, po visu form...
                for (SavedFileDecoder.PhysicsShapesGroup e : physicsShapesGroups) {
                    // pirma dedam paprastas detales.
                    xmlwriter.element("Body");
                    xmlwriter.attribute("id", e.id);

                    // dedam elementus tik jei ne default value...
                    //body
                    if (e.bodyType != BodyDef.BodyType.DynamicBody) {
                        xmlwriter.attribute("BodyType", e.bodyType);
                    }

                    // origin.
                    boolean createOrigin = !e.isOriginMiddle;
                    if (e.isOriginMiddle) {
                        xmlwriter.attribute("originMiddle", e.isOriginMiddle);
                    }

                    // fixed rotation.
                    if (e.fixedRotation) {
                        xmlwriter.attribute("fixedRotation", e.fixedRotation);
                    }

                    // bullet
                    if (e.bullet) {
                        xmlwriter.attribute("bullet", e.bullet);
                    }

                    // linear damping
                    if (e.linearDamping != 0) {
                        xmlwriter.attribute("linearDamping", e.linearDamping);
                    }

                    // angular damping
                    if (e.angularDamping != 0) {
                        xmlwriter.attribute("angularDamping", e.angularDamping);
                    }

                    // gravity scale
                    if (e.gravityScale != 1f) {
                        xmlwriter.attribute("gravityScale", e.gravityScale);
                    }

                    // angularVelocity
                    if (e.angularVelocity != 0) {
                        xmlwriter.attribute("angularVelocity", e.angularVelocity);
                    }

                    // linear velocity. atskirai eina.
                    if (e.linearVelocity.x != 0 || e.linearVelocity.y != 0) {
                        xmlwriter.element("linearVelocity");
                        xmlwriter.attribute("x", e.linearVelocity.x);
                        xmlwriter.attribute("y", e.linearVelocity.y);
                        xmlwriter.pop();
                    }

                    // body origin atskirai kuriam.
                    if (createOrigin) {
                        xmlwriter.element("bodyOrigin");
                        xmlwriter.attribute("x", e.bodyOrigin.x);
                        xmlwriter.attribute("y", e.bodyOrigin.y);
                        xmlwriter.pop();
                    }

                    // dabar dedam fixturas..
                    for (int a = 0; a < e.shapes.size; a++) {
                        SavedFileDecoder.PhysicsFixtureShapes holder = e.shapes.get(a);
                        xmlwriter.element("figure");
                        xmlwriter.attribute("type", holder.type);
                        if (holder.radius != 0)
                            xmlwriter.attribute("radius", holder.radius);
                        if (holder.density != 0)
                            xmlwriter.attribute("density", holder.density);
                        if (holder.friction != 0.2f)
                            xmlwriter.attribute("friction", holder.friction);
                        if (holder.restitution != 0)
                            xmlwriter.attribute("restitution", holder.restitution);
                        if (holder.isSensor)
                            xmlwriter.attribute("sensor", holder.isSensor);
                        if (holder.categoryBits != 1)
                            xmlwriter.attribute("categoryBits", holder.categoryBits);
                        if (holder.maskBits != -1)
                            xmlwriter.attribute("maskBits", holder.maskBits);
                        if (holder.groupIndex != 0)
                            xmlwriter.attribute("group", holder.groupIndex);
                        for (int k = 0; k < holder.x.size; k++) {
                            xmlwriter.element("point");
                            xmlwriter.attribute("x", holder.x.get(k));
                            xmlwriter.attribute("y", holder.y.get(k));
                            xmlwriter.pop(); // uzdarom point
                        }
                        xmlwriter.pop();// uzdarom figure
                    }


                    xmlwriter.pop(); // body sprogdinam.
                }
            }

            // sprogdins phcs ir jbc...
            xmlwriter.close();
//            xmlwriter.pop(); // uzdarom jbc
//            xmlwriter.flush();
//            xmlwriter.pop();
//            xmlwriter.close();
            return stringWriter;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** holder - the one which is checked if already same group exists.
     * groups - if holder is new then new group will be created and added to this list. Also this list is checked if same physics exists.
     * @return group index of this physics.*/
    private int createPhysicsGroup(PhysicsHolder holder, Array<SavedFileDecoder.PhysicsShapesGroup> groups){
        // Dabar reik imt fizikas
        // Kadangi fizikos gan daznai kartojas, nes naudojama panasios specifikacijos - grupuosim
        // nedesim paciu fiziku, o kursim grupes ir jei tokiu pat fiziku nesaugosim. Fiziku info desim i grupes, o informacija kelsim
        // tik su skaicium (index, kuris nurodys kuria grupe issaugota info).
//                    Array<PhysicsEditor.FixtureShapeHolder> shapes = resource.getShapes();
//        PhysicsHolder holder = resource.getPhysicsHolder();
        ArrayList<PhysicsEditor.FixtureShapeHolder> shapes = holder.shapes;
        if (shapes.size() > 0){
            // yra fizikos, dabar eisim per grupes ir ziuresim ar nera tokio pacio.
            MainPhysicsLoop:
            for (int shapeIndex = 0; shapeIndex < groups.size; shapeIndex++) {
                SavedFileDecoder.PhysicsShapesGroup e = groups.get(shapeIndex);
//                            if (e.bodyType != resource.getBodyType()){
                if (e.bodyType != holder.bodyType) {
                    continue; // neatitinka body type.
                }
                if (shapes.size() != e.shapes.size) {
                    continue; // neatitinka dydis, tesiam toliau
                }
//                            if (e.isOriginMiddle != resource.isBodyOriginMiddle()){
                if (e.isOriginMiddle != holder.isBodyOriginMiddle) {
                    continue; // neatitinka origin. Tesiam toliau
                }
//                            Vector2 origin = resource.getBodyOrigin();
                Vector2 origin = holder.bodyOrigin;
                if (!e.isOriginMiddle && (e.bodyOrigin.x != origin.x || e.bodyOrigin.y != origin.y)) { // jeigu origin ne middle, tai turi atitikt origino parametrai. Jeigu middle true, tai nesvarbu.
                    continue; // origin ne middle ir taskai neatitinka. Tesiam toliau
                }

                if (e.fixedRotation != holder.fixedRotation){ // fixed rotation neatitinka.
                    continue;
                }

                if (e.bullet != holder.bulletBody) { // bullet body neatitinka
                    continue;
                }

                if (e.linearDamping != holder.linearDamping) { // linear damping neatitinka.
                    continue;
                }

                if (e.angularDamping != holder.angularDamping) { // angular damping neatitink.
                    continue;
                }

                if (e.gravityScale != holder.gravityScale) { // gravity scale
                    continue;
                }

                if (e.angularVelocity != holder.angularVelocity) { // angular damping
                    continue;
                }

                Vector2 linear = holder.linearVelocity;
                if (e.linearVelocity.x != linear.x || e.linearVelocity.y != linear.y){ // neatitinka linear velocity.
                    continue;
                }

                // dabar reiks eit per visas figuras ir ziuret ar tokia yra.
                for (SavedFileDecoder.PhysicsFixtureShapes saveShape : e.shapes) {
                    boolean foundCopy = false;
                    SHAPELOOP:
                    for (PhysicsEditor.FixtureShapeHolder shapeHolder : shapes) {
                        /* paziurim bendrus parametrus */
                        // ar turi tiek pat tasku.
                        if (saveShape.x.size != shapeHolder.x.size() || saveShape.y.size != shapeHolder.y.size()) {
                            continue; // neatitinka pointu dydziai.
                        }

                        // sensor tikrinam
                        if (saveShape.isSensor != shapeHolder.isSensor) { // neatitinka sensor
                            continue;
                        }
                        if (saveShape.categoryBits != shapeHolder.categoryBits) {
                            continue; // neatitinka bitai
                        }
                        if (saveShape.density != shapeHolder.density) {
                            continue; // neatitinka density
                        }
                        if (saveShape.friction != shapeHolder.friction) {
                            continue; // neatitinka friction
                        }
                        if (saveShape.groupIndex != shapeHolder.groupIndex) {
                            continue; // neatitinka group index
                        }
                        if (saveShape.maskBits != shapeHolder.maskBits) {
                            continue; // neatitinka mask bitai.
                        }
                        if (saveShape.radius != shapeHolder.radius) {
                            continue; // neatitinka radius.
                        }
                        if (saveShape.restitution != shapeHolder.restitution) {
                            continue; // neatitinka restitution
                        }
                        if (saveShape.type != shapeHolder.type) {
                            continue; // neatitinka type.
                        }

                        // dabar lieka paziuret kaip su fizikos taskais.
                        // taskai turi eit paraleliskai todel galima abu vienu ratu tikrint ir ziuret ar vienodi.
                        for (int count = 0; count < saveShape.x.size && count < saveShape.y.size; count++) {
                            float sx = saveShape.x.get(count), sy = saveShape.y.get(count);
                            float hx = shapeHolder.x.get(count), hy = shapeHolder.y.get(count);
                            if (sx != hx || sy != hy) {// x arba y taskai neatitinka, einam toliau.
                                continue SHAPELOOP;
                            }
                        }

                        // jeigu atejo iki cia, tai radom identiskas, egzistuojancias fizikas
                        // vadinas sios fizikos jau yra grupese, tai tik paimsim grupes indexa.
//                        projectResource.physicsShapeGroupIndex = shapeIndex;
//                        createGroup = false;
//                        return shapeIndex;
//                        break PHYSICSLOOP;
                        foundCopy = true;
                        break;
                    }

                    if (!foundCopy){
                        // nerado identisko elemento...
                        continue MainPhysicsLoop; // tesiam tolimesne paieska.
                    }
                }

                // tik dabar grazinam. Tik dabar fiziko idealios.
                return shapeIndex;
            }

            // susiziurejo, o dabar paziurim ar reik nauja fizikos grupe kurt
            // kuriam fizikos grupe.
            SavedFileDecoder.PhysicsShapesGroup physicsShapesGroup = new SavedFileDecoder.PhysicsShapesGroup();
//                            physicsShapesGroup.isOriginMiddle = resource.isBodyOriginMiddle();
//                            physicsShapesGroup.bodyOrigin.set(resource.getBodyOrigin());
//                            physicsShapesGroup.bodyType = resource.getBodyType();
            physicsShapesGroup.isOriginMiddle = holder.isBodyOriginMiddle;
            physicsShapesGroup.bodyOrigin.set(holder.bodyOrigin);
            physicsShapesGroup.bodyType = holder.bodyType;
            physicsShapesGroup.fixedRotation = holder.fixedRotation;
            physicsShapesGroup.bullet = holder.bulletBody;
            physicsShapesGroup.linearDamping = holder.linearDamping;
            physicsShapesGroup.angularDamping = holder.angularDamping;
            physicsShapesGroup.gravityScale = holder.gravityScale;
            physicsShapesGroup.angularVelocity = holder.angularVelocity;
            physicsShapesGroup.linearVelocity.set(holder.linearVelocity);

            // sumetam shapes.
            for (PhysicsEditor.FixtureShapeHolder e : shapes){
                SavedFileDecoder.PhysicsFixtureShapes physicsFixtureShapes = new SavedFileDecoder.PhysicsFixtureShapes();
                physicsFixtureShapes.density = e.density;
                physicsFixtureShapes.friction = e.friction;
                physicsFixtureShapes.restitution = e.restitution;
                physicsFixtureShapes.isSensor = e.isSensor;
                physicsFixtureShapes.categoryBits = e.categoryBits;
                physicsFixtureShapes.maskBits = e.maskBits;
                physicsFixtureShapes.groupIndex = e.groupIndex;
                physicsFixtureShapes.type = e.type;
                physicsFixtureShapes.radius = e.radius;

//                physicsFixtureShapes.x.addAll(e.x);
//                physicsFixtureShapes.y.addAll(e.y);
                for (float x : e.x){
                    physicsFixtureShapes.x.add(x);
                }
                for (float y : e.y){
                    physicsFixtureShapes.y.add(y);
                }
                // idedam i grupe shape.
                physicsShapesGroup.shapes.add(physicsFixtureShapes);
            }

//                projectResource.physicsShapeGroupIndex = info.shapesGroup.size; // nurodom kuris, kadangi isides vienas, tai sitas tinka
//                info.shapesGroup.add(physicsShapesGroup); // idedam grupe.
            groups.add(physicsShapesGroup);

            physicsShapesGroup.id = groups.size-1;
            return groups.size-1; // paskutinis elementas.
        }else {
            // nera isvis fiziku.
//            projectResource.physicsShapeGroupIndex = -1; // pazymim, kad nera.
            return -1; // nera fiziku.
        }
    }

    public boolean saveProject(){
        return saveProject(false);
    }

    /** Will force autoSave to occur. AutoSave must be enabled otherwise nothing happens. */
    public void forceAutoSave(){
        if (autoSave)
            saveProject(true);
    }

    private boolean saveProject(boolean autoSave){
        synchronized (saver) {
            String root = Project.getProjectRootFolderPath();
            String fileName = Project.getProjectName();
            if (root == null) {
                // nera direktorijos kur saugot.
                return false;
            }
            StringWriter xml = save();
            if (xml == null) {
                return false;
            }
            String auto = autoSave ? ".autosave" : "";
            String file = root + "/" + fileName + auto + ".jbcml";
            FileHandle handle = Gdx.files.absolute(file);
            boolean exist = handle.exists();
            try {
                handle.writeString(xml.toString(), false);
            } catch (GdxRuntimeException ex) {
                ex.printStackTrace();
                return false;
            }
            if (!exist) { // pirma kart sukurtas save failas. Todel mes idedam, kad sita projekta atidarem.
                // daugiau sios vietos nebekvies.
                StartForm.projectLoaded(Project.getProjectName(), handle.file().getAbsolutePath());
            }

            if (autoSave){
                autoSaveNeed = false; // pazymim, kad tik autosave panaudojo.
            }else {
                saveNeed = false; // pazymim, kad normaliai save padare.
                autoSaveNeed = false;

                // nuimam star nuo title.
                Gdx.graphics.setTitle("JBConstructor - " + Project.getProjectName());
            }
            return true;
        }
    }

    private class autoSaver implements Runnable{

        @Override
        public void run() {
            while (true){
                synchronized (saver){
                    try {
                        // auto saveris ieis i deep miega kai neimanoma issaugot nes nera nurodytas folderis.
                        // taip pat jeigu isjunge auto save, tada jis uzmigs giliu miegu.
                        // jeigu saugot nereik, jis taip pat uzmigs giliu miegu, bet iskart bus prikeltas, kai tik kazkas projekte pasikeite.
                        if (!Project.hasProjectRootDirectory() || !autoSave || !saveNeed){
                            saverIsSleeping = true;
                            saver.wait();
                            saverIsSleeping = false;
                        }
                        saver.wait(autoSaveTime); // truputi nusnaus pries issaugant viska, kad nebutu taip, kad saugos be sustojimo.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // saviinimo procesas
                    if (autoSave && saveNeed && autoSaveNeed) // issaugos tik tada kai reikia ir kai ijunktas auto save funkcija.
                        saveProject(true);
                }
            }
        }
    }

    private class ShutDowner implements Runnable{

        @Override
        public void run() {
            // cia jei error. turetu iskviest sita.
            if (Project.isProjectOpen() && saveNeed) // issaugos tik tada kai nebuvo isaugota
                saveProject(true);
        }
    }
}
