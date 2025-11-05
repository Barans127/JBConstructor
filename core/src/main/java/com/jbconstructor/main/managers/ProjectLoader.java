package com.jbconstructor.main.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Toast;
import com.engine.interfaces.controls.dialogs.ConfirmDialog;
import com.engine.interfaces.controls.toasts.AlertToast;
import com.engine.jbconstructor.SavedFileDecoder;
import com.jbconstructor.main.editors.ChainEdging;
import com.jbconstructor.main.editors.JointManager;
import com.jbconstructor.main.forms.editorForm.AdditionalPanel;
import com.jbconstructor.main.forms.editorForm.EditForm;
import com.jbconstructor.main.root.Element;
import com.jbconstructor.main.root.FileLoader;
import com.jbconstructor.main.root.SelectiveInterfaceController;

public class ProjectLoader {
    private String error;

    public ProjectLoader(){ }

    /** @return error while trying to load project. null if no error happened.*/
    public String getError(){
        return error;
    }

    /** creates new project. Closes old project if it is open. */
    public boolean createNewProject(String projectName, String rootLocation, String folderPath){
        error = null;
        if (Project.isProjectOpen()){
            Project.closeProject();
        }
        if (projectName == null || projectName.length() == 0) {
            error = "Project name cannot be null or zero length";
            return false;
        }
        boolean hasDirectory = false;
        if (rootLocation != null && rootLocation.length() > 0){
            hasDirectory = true;
        }
        String path;
        if (folderPath != null && folderPath.length() > 0){
            path = folderPath;
        }else {
            path = hasDirectory ? rootLocation : null;
        }
        // sukuriam nauja projekta, kur nieko nera.
        Project.getFormsManager().createEditForm(projectName);
        Project.setProjectName(projectName);
        Project.setProjectRootFolderPath(hasDirectory ? rootLocation : null);
        Project.setResourcesPath(path);
        Project.loadResourcesFromResourcesFolder();
        Project.openProject();
        return true;
    }

    public boolean openExistingProject(String savedFilePath){
        FileHandle e = Gdx.files.absolute(savedFilePath);
        SavedFileDecoder dec = new SavedFileDecoder();
        dec.readSave(e, false, false); // save failai siaip visada buna xml ir necompresinti.
//        dec.readSave(e, true, true);
        return openExistingProject(dec);
    }

    public boolean openExistingProject(SavedFileDecoder decoder){
        error = null;
        if (Project.isProjectOpen()){ // jeigu kazkas atidaryta iskart uzdarom
            Project.closeProject();
        }
        // patikrinam ar nera formu.
        if (Project.getFormsManager().getFormsSize() > 0){
            Project.closeProject(); // atliks viska.
        }
        if (decoder.getErrorMessage() != null){ // ar nuskaitant nebuvo klaidu.
            error = decoder.getErrorMessage();
            return false;
        }
        if (!decoder.isSaveFileDecoded()){ // paziurim ar nuskaitytas save failas
            error = "Save file was not read";
            return false; // nebuvo nuskaitytas save failas
        }
        if (decoder.isExportSaveFile()){ // su export situacija tokia, kad nera resource failo, reiktu is naujo ieskot kur kas. gal veliau ir padaryt.
            // arba koki export converteri, kuris pavers i normalu save. Bet cia tai ne...
            error = "Save file is exported. Cannot parse exported file";
            return false;
        }
        // viskas tvarkoj, skaitom info is save failo.
        Project.setProjectName(decoder.getProjectName());
        Project.setProjectRootFolderPath(decoder.getProjectRoot());
        Project.setResourcesPath(decoder.getProjectResourcesFolder()); // kad nekrovinetu. nereik. cia jau is save failo viska paimsim.

        Array<String> errorList = new Array<>();
        // resource perziura.
        Array<String> loadedAtlas = new Array<>();
        Array<Project.ResourceToHold> allRes = new Array<>();
        for (SavedFileDecoder.ProjectResource res : decoder.getProjectResources()){ // atlas uzkrovimas
            if (res.id != null && res.id.equals("null")){ // ignorinam situos vardus.
                continue;
            }
            if (res.isAtlas){
                if (!loadedAtlas.contains(res.atlas, false)){
                    loadedAtlas.add(res.atlas);
                    // uzkraunam atlasa
                    String id = res.atlas;
                    String path = res.path;
                    if (Resources.containsTextureAtlasKey(id)){ // tokio id neturi but.
                        error = "Project resources was not closed";
                        return false;
                    }
                    FileHandle atl = Gdx.files.absolute(path);
                    if (!atl.exists() || atl.isDirectory()){
//                        error = "Atlas file was not found: " + path; // cia kazkuom pakeist? kai del image, kur tipo ner originalo?
//                        return false; // dingo atlas.
                        errorList.add("Missing texture atlas: " + path);
                        continue; // ignoruojam dingusius failus.
                    }
                    TextureAtlas textAtlas;
                    try {
                        textAtlas = new TextureAtlas(atl);
                    }catch (GdxRuntimeException ex){ // neiskaito texture atlas.
//                        error = "Atlas file cannot be read";
//                        return false; // neuzkrove atlas.
                        errorList.add("Failed loading atlas: " + path);
                        continue; // ignuoruojam faila.
                    }
                    Resources.addTextureAtlas(id, textAtlas);
                    for (TextureAtlas.AtlasRegion e : textAtlas.getRegions()){ // uzkraunam.
//                        boolean ninePatch = e.splits != null;
                        // New libGDX method to store info about ninePatch.
                        // If this is null, than it is not ninePatch.
                        boolean ninePatch = e.findValue("split") != null;
                        if (ninePatch){ // uzkraus nine patch
                            allRes.add(new Project.ResourceToHold(e.name,
                                    new NinePatchDrawable(Resources.getNinePatch(id, e.name)), 2,
                                    id, path));
                        }else { // paprastas region.
                            allRes.add(new Project.ResourceToHold(e.name, Resources.getRegionDrawable(e.name), 1,
                                    id, path));
                        }
                    }
                }
            }
        }
        Project.instertLoadableResources(allRes);
        allRes.clear();
        FileLoader loader = new FileLoader();
        for (SavedFileDecoder.ProjectResource res : decoder.getProjectResources()) { // dar karta praeinam pro viska. uzkraunam textures. sugaudom physics
            if (res.id != null && res.id.equals("null")){
                continue; // ignorinam null values.
            }
            if (!res.isAtlas) { // o, dabar visus tik ne atlas.
                if (res.path != null && res.path.toLowerCase().endsWith(".scml")){ // animation failas.
                    if (Resources.containsAnimationKey(res.id)){
                        error = "Project resources was not closed or duplicate name found";
                        return false;
                    }
                    loader.loadResources(res.path); // loader greiciau uzkraus.

                    if (!loader.isEmpty()){
                        // visu pirma turi uzkraut musu animacija.
                        if (loader.getAnimations().size > 1 || loader.getAtlases().size > 0){ // turi but tik viena animacija...
                            // ?? Kazkas ne to.
                            for (SpriterDrawable drawable : loader.getAnimations()){
                                Resources.addDisposable(drawable);
                            }
                            for (TextureAtlas atlas : loader.getAtlases()){
                                Resources.addDisposable(atlas);
                            }
                            loader.clear(); // kazkas cia negerai. uzkrove per daug daiktu.

                            errorList.add("Failed loading animation: " + res.path);
                            continue;
                        }
                        if (loader.getAnimations().size == 0){
                            // uzkrove, bet ne animacija
                            errorList.add("Failed loading animation: " + res.path);
                            continue;
                        }
                        SpriterDrawable e = loader.getAnimations().get(0); // paimam animacija.
                        Resources.addAnimation(res.id, e); // dedam i resources.

//                        Project.ResourceToHold holder = new Project.ResourceToHold(res.id, e, 3, null,
//                                e.getAtlas() == null ? null : "", e.getName());
                        // pakeitimas tame, kad atlasa kazkodel vistiek saugodavo tuscia, tai kam isvis ten ji kist?
                        // iskart duodam atlas name - null.
                        Project.ResourceToHold holder = new Project.ResourceToHold(res.id, e, 3,
                                null, e.getName());
//                        if (res.shapes.size > 0){ // jeigu fizikos yra, idedam.
////                        allRes.get(allRes.size-1).shapes.addAll(res.shapes);
//                            for (SavedFileDecoder.PhysicsFixtureShapes fix : res.shapes){
//                                PhysicsEditor.FixtureShapeHolder sh = new PhysicsEditor.FixtureShapeHolder();
//                                sh.x.addAll(fix.x);
//                                sh.y.addAll(fix.y);
//                                sh.type = fix.type;
//                                sh.radius = fix.radius;
//                                holder.shapes.add(sh);
//                            }
//                        }

                        allRes.add(holder); // ant galo metam i musu sarasiuka.
                    }else {
                        // isvis nieko neuzkrove.
                        String mes = loader.getErrorMessage();
                        if (mes == null){
                            errorList.add("Failed loading animation: " + res.path);
                        }else {
                            errorList.add("Animation failed loading: \n" + mes);
                        }
                        loader.clear();
                        continue;
                    }
//                    for (int a = 0; a < loader.getAtlases().size; a++){
//                        String path = loader.getAtlasesPaths().get(a);
//
//                    }
//                    if (!loader.isEmpty()){
//                        Project.load(loader);
//                    }
                    // paziurim ar error nera.
                    String mes = loader.getErrorMessage();
                    if (mes != null){
                        errorList.add(mes);
                    }
                    loader.clear(); // isvalom, veliau naudosim dar karta.
                    // surandam animacijos fizikas.
//                    if (res.shapes.size > 0) {
//                        for (int a = 0; a < Project.getLoadableResourcesCount(); a++) {
//                            Project.ResourceToHold hold = Project.getLoadableResource(a);
//                            if (hold.tab == 3) {
//                                if (hold.idName.equals(res.id)){
//                                    for (SavedFileDecoder.PhysicsFixtureShapes e : res.shapes){
//                                        PhysicsEditor.FixtureShapeHolder sh = new PhysicsEditor.FixtureShapeHolder();
//                                        sh.x.addAll(e.x);
//                                        sh.y.addAll(e.y);
//                                        sh.type = e.type;
//                                        sh.radius = e.radius;
//                                        hold.shapes.add(sh);
//                                    }
////                                    hold.shapes.addAll(res.shapes);
//                                }
//                            }
//                        }
//                    }
                }else if (res.atlas == null) { // cia bus texture idejimas.
                    if (Resources.containsTextureKey(res.id)) {
                        error = "Project resources was not closed or duplicate name found";
                        return false;
                    }
                    FileHandle e = Gdx.files.absolute(res.path);
                    if (!e.exists() || e.isDirectory()) {
//                        error = "Texture file was not found: " + res.path; // cia kazkuom pakeist? kai del image, kur tipo ner originalo?
//                        return false;// dingo resource.
                        errorList.add("Texture not found: " + res.path);
                        continue; // ignoruojam. Tiesiog ignoruojam, bus pasalintas is saraso ir tiek, o kas naudojo, gaus missing resource drawable.
                    }
                    TextureRegionDrawable text = Resources.loadTexture(res.id, res.path);
                    allRes.add(new Project.ResourceToHold(res.id, text, 0, null, null));
//                    if (res.shapes.size > 0){
////                        allRes.get(allRes.size-1).shapes.addAll(res.shapes);
//                        Project.ResourceToHold hold = allRes.get(allRes.size-1);
//                        for (SavedFileDecoder.PhysicsFixtureShapes fix : res.shapes){
//                            PhysicsEditor.FixtureShapeHolder sh = new PhysicsEditor.FixtureShapeHolder();
//                            sh.x.addAll(fix.x);
//                            sh.y.addAll(fix.y);
//                            sh.type = fix.type;
//                            sh.radius = fix.radius;
//                            hold.shapes.add(sh);
//                        }
//                    }
                }
//                else { // texture region tiesig physics surandam.
//                    if (res.shapes.size > 0){ // pridedam shapes.
//                        for (int a = 0; a < Project.getLoadableResourcesCount(); a++){
//                            Project.ResourceToHold e = Project.getLoadableResource(a);
//                            if (e.getIdName().equals(res.id)){
////                                e.shapes.addAll(res.shapes);
//                                for (SavedFileDecoder.PhysicsFixtureShapes fix : res.shapes){
//                                    e.shapes.add(readFixture(fix));
//                                }
//                                break;
//                            }
//                        }
//                    }
//                }
            }
        }
        Project.instertLoadableResources(allRes);
        // resources uzkrauta judam tolyn.
        // dabar formu eile.
        for (SavedFileDecoder.ProjectForm form : decoder.getProjectForms()){ // uzkraunam kiekviena forma
            // formos nustatymai.
            // camera
            int count = 0;
            while (!Project.getFormsManager().createEditForm(form.name)){ // jei kartais atsiras duplikatu. formos sukurimas
                form.name += count;
                count++;
            }
            EditForm editForm = Project.getFormsManager().getForm(form.name); // naujai sukurta forma.
            editForm.setCustomCameraSettings(form.cameraX, form.cameraY, form.cameraZoom); // nustatom buvusius cameros nustatymus.
            editForm.showScreenBounds(form.showBounds); // nustatom kitus parametrus.
            ((SelectiveInterfaceController)editForm.getController()).disableAbsoluteDraw(!form.showAbsolute);
            ((SelectiveInterfaceController)editForm.getController()).disableFixedDraw(!form.showFix);
            editForm.getMainPanel().showCoords(form.showCoords); // hide tik zodis.
            //suzymim checkboxus.
            AdditionalPanel additionalPanel = editForm.getMainPanel().getAdditionalPanel();
            additionalPanel.getBoxes()[0].setChecked(form.showFix);
            additionalPanel.getBoxes()[1].setChecked(form.showAbsolute);
            additionalPanel.getBoxes()[2].setChecked(form.showCoords);
            additionalPanel.getBoxes()[3].setChecked(form.showBounds);

            // background spalvos radimas
            int backgroundColor;
            try {
                backgroundColor = MoreUtils.hexToInt(form.background);
            }catch (NumberFormatException ex){
                backgroundColor = 0xffffffff;
            }
            editForm.setBackgroundColor(backgroundColor);

            // chain pridejimas
            for (SavedFileDecoder.ProjectChain e : form.chainsInfos){
                addChainToForm(editForm, e);
            }

            // joint pridejimas
            for (SavedFileDecoder.JointBaseInfo e : form.jointsInfos){
                addJointToForm(editForm, e);
            }

            // toliau item pridejimas.
            String error = "";
            for (SavedFileDecoder.ResourceInfo e : form.resourceInfos){
                error = addItemsToForm(editForm, e, decoder, error);
            }

            if (error.length() > 0){ // pranesam error del dingusiu fiziku ar resursu trukumo.
                ConfirmDialog confirmDialog = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.OK);
                confirmDialog.setText(error);
                confirmDialog.show(editForm);
            }
        }
        if (Project.getFormsManager().getFormsSize() == 0){ // nesukurta nei viena forma.
            int count = 0;
            String name = "form" + count;
            while (!Project.getFormsManager().createEditForm(name)){
                name = "form" + ++count;
            }
        }

        if (errorList.size > 0){
            // yra error. Rodom juos.
            EditForm form = Project.getFormsManager().getForm(0);
            ConfirmDialog confirmDialog = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.OK);

            // kursim dabar musu legendini sarasiuka.
            StringBuilder builder = new StringBuilder();

            // sumetam i kruva
            for (String e : errorList){
                builder.append(e).append("\n");
            }

            // parodom user.
            confirmDialog.setText(builder.toString());
            confirmDialog.show(form);
        }

        Project.openProject(); // pranesam, kad projektas atidarytas.
        Project.loadResourcesFromResourcesFolder(); // tegul perziuri dar karta, ar ner nauju resource folderi.
        return true;
    }

    private String addItemsToForm(EditForm e, SavedFileDecoder.ResourceInfo info, SavedFileDecoder decoder, String error){
        // pirmieji parametrai
        float x, y, radius;
        String id;
        int positioning;
        x = info.x;
        y = info.y;
        radius = info.radius;
        id = info.idName;
        positioning = info.positioning;
        if (info.type.equals("Element") || info.type.equals("Resource")){ // paprastas image
            Element.ElementStyle st = new Element.ElementStyle();
            st.x = x;
            st.y = y;
            st.angle = radius;
            st.positioning = MoreUtils.getPositionFromIndex(positioning);
//            for (Project.ResourceToHold r : res){
//                if (r.idName.equals(info.resource)){
//                    st.image = r.e; // paimam drawable
//                    break;
//                }
//            }
//            st.tintImage = Integer.parseUnsignedInt(info.tint, 16);
            try {
                st.tintImage = MoreUtils.hexToInt(info.tint);
            }catch (NumberFormatException ex){
                st.tintImage = 0xffffffff;
            }
            st.autoSize = false;
            st.width = info.width;
            st.height = info.height;
//            st.image = Resources.getRegionRecursive(info.resource);
            st.resName = info.resource; // be sito nebezinos, kuris buvo drawable naudojama.
            if (info.resource.equals("null")){// null vadinas nera img ir viskas.
                st.image = null;
            }else {
                st.image = Resources.getDrawable(info.resource);
            }
            if (st.image == null){ // jeigu nera imam default.
                st.image = Resources.getDrawable("basicUsageNoImageKey");
                Project.addNameException(info.resource);

                String beg;
                if (error.length() == 0){
                    beg = "";
                }else {
                    beg = "\n";
                }
                error += beg + "Resource for element (" + id + ") was not found. Resource id: " + info.resource;
            }else{
                if (st.image instanceof SpriterDrawable){ // sukuriam nauja instance, kad leistu keist anime parametrus.
                    st.image = new SpriterDrawable((SpriterDrawable) st.image);
                }
            }
//            if (info.shapes.size > 0){ // dar ir fizikas pridedam jei buvo.
////                st.shapes.addAll(info.shapes);
//                for (SavedFileDecoder.PhysicsFixtureShapes fix : info.shapes){
////                    st.shapes.add(readFixture(fix));
//                    st.physicsHolder.shapes.add(readFixture(fix));
//                }
////                st.isBodyOriginMiddle = info.isOriginMiddle; // dar body parametrus sugaudom.
//                st.physicsHolder.isBodyOriginMiddle = info.isOriginMiddle;
//                if (!info.isOriginMiddle){
////                    st.bodyOrigin.set(info.bodyOrigin);
//                    st.physicsHolder.bodyOrigin.set(info.bodyOrigin);
//                }
////                st.bodyType = info.bodyType;
//                st.physicsHolder.bodyType = info.bodyType;
//            }

            // toliau skaitom fizikas.
            int physicsIndex = info.physicsShapeGroupIndex;

            if (physicsIndex > -1){
                // yra fizikos.
                // suurandam visa fiziku sarasa.
                Array<SavedFileDecoder.PhysicsShapesGroup> array = decoder.getPhysicsShapeGroups();

                SavedFileDecoder.PhysicsShapesGroup group = null;
                if (physicsIndex < array.size) {
                    group = array.get(physicsIndex);

                    // dar paziurim ar id atitinka. Tik versijoms 0.9 ir higher.
                    // zemesnes nei 0.9 neturi id tago, del ko neimanoma patikrint.
                    if (decoder.getSaveVersion() >= 0.9f && group.id != physicsIndex){
                        boolean notFound = true;
                        // neradom fizikuuu, Bandom ieskot.
                        for (int a= 0; a < array.size; a++){
                            SavedFileDecoder.PhysicsShapesGroup el = array.get(a);
                            if (el.id == physicsIndex){
                                group = el; // radom musu fizikas.
                                notFound = false;
                                break;
                            }
                        }

                        if (notFound){
                            // nerado fiziku.
                            group = null; // padarom, kad nebutu isvis.
                        }
                    }
                }

                if (group == null){
                    String begining;
                    if (error.length() == 0){
                        begining = "";
                    }else {
                        begining = "\n";
                    }

                    error += begining + "Physics was not found for element: " + id;
                }else {
                    // viskas gerai. Fizikos rastos.
                    st.physicsHolder.readDecoderInfo(group);
                }
            }

            // pridedam flipus
            st.flipX = info.flipX;
            st.flipY = info.flipY;

            Element c = st.createInterface();
            c.setOrigin(st.width/2, st.height/2); // nes nepagauna origin iskarto. tai atrodo lyg suolis.
            e.addControl(c, e.getController().getControls().size()-3);
            c.setIdName(id);

        }
        return error;
    }

    private void addChainToForm(EditForm e, SavedFileDecoder.ProjectChain chain){ // prides viena chain.
//        ChainEdging chainer = e.getMainPanel().getChainEdgePanel().getChainEdgingManager();
        ChainEdging chainer = e.getChainEdging();
        ChainEdging.Chains ch = new ChainEdging.Chains();
        ch.name = chain.name; // nustatom varda
        ch.loop = chain.loop;
        ch.isSensor = chain.isSensor;
        ch.categoryBits = (short) chain.categoryBits;
        ch.maskBits = (short) chain.maskBits;
        ch.groupIndex = (short) chain.groupIndex;
        ch.x.addAll(chain.x); // visus taskus
        ch.y.addAll(chain.y);
        ch.owner = chainer; // nurodom, kad sitas bus savininkas.
        chainer.addChain(ch, -1); // pridedam.
    }

    private void addJointToForm(EditForm form, SavedFileDecoder.JointBaseInfo joint){
        JointManager jointManager = form.getJointManager();
        JointManager.JointInfo info = jointManager.createJoint(joint.id); // gali id iskraipyt, jei duplikatas butu!
        // cia jau joint pridetas.
        if (info == null){ // jei id sugadintas ir nieko nesukure, tai zodz ignorinam.
            return;
        }
        jointManager.changeJointType(info, joint.type);

        // joint info.
        if (joint instanceof SavedFileDecoder.DistanceJointInfo){
            SavedFileDecoder.DistanceJointInfo e = (SavedFileDecoder.DistanceJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 0, info, jointManager)){
                return;
            }
            info.anchorA.set(e.anchorA);
            info.anchorB.set(e.anchorB);
            info.length = e.length;
            info.frequencyHz = e.frequencyHz;
            info.dampingRatio = e.dampingRatio;
        }else if (joint instanceof SavedFileDecoder.FrictionJointInfo){
            SavedFileDecoder.FrictionJointInfo e = (SavedFileDecoder.FrictionJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 1, info, jointManager)){
                return;
            }
            info.anchorA.set(e.anchorA);
            info.anchorB.set(e.anchorB);
            info.maxForce = e.maxForce;
            info.maxTorque = e.maxTorque;
        }else if (joint instanceof SavedFileDecoder.GearJointInfo){
            SavedFileDecoder.GearJointInfo e = (SavedFileDecoder.GearJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 2, info, jointManager)){
                return;
            }
            info.joint1ID = e.joint1Id == null || e.joint1Id.length() == 0 ? null : e.joint1Id; // kad negalvotu, kad id suteiktas
            info.joint2ID = e.joint2Id == null || e.joint2Id.length() == 0 ? null : e.joint2Id;
            info.ratio = e.ratio;
        }else if (joint instanceof SavedFileDecoder.MotorJointInfo){
            SavedFileDecoder.MotorJointInfo e = (SavedFileDecoder.MotorJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 3, info, jointManager)){
                return;
            }
            info.anchorA.set(e.linearOffset);
            info.length = e.angularOffset;
            info.maxForce = e.maxForce;
            info.maxTorque = e.maxTorque;
            info.ratio = e.correctionFactor;
        }else if (joint instanceof SavedFileDecoder.MouseJointInfo){
            SavedFileDecoder.MouseJointInfo e = (SavedFileDecoder.MouseJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 4, info, jointManager)){
                return;
            }
            info.anchorA.set(e.target);
            info.maxForce = e.maxForce;
            info.frequencyHz = e.frequencyHz;
            info.dampingRatio = e.dampingRatio;
        }else if (joint instanceof SavedFileDecoder.PrismaticJointInfo){
            SavedFileDecoder.PrismaticJointInfo e = (SavedFileDecoder.PrismaticJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 5, info, jointManager)){
                return;
            }
            info.anchorA.set(e.anchorA);
            info.anchorB.set(e.anchorB);
            info.localAxisA.set(e.localAxisA);
            info.length = e.referenceAngle;
            info.enableLimit = e.enableLimit;
            info.frequencyHz = e.lowerTranslation;
            info.dampingRatio = e.upperTranslation;
            info.enableMotor = e.enableMotor;
            info.maxForce = e.maxMotorForce;
            info.maxTorque = e.maxMotorSpeed;
        }else if (joint instanceof SavedFileDecoder.PulleyJointInfo){
            SavedFileDecoder.PulleyJointInfo e = (SavedFileDecoder.PulleyJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 6, info, jointManager)){
                return;
            }
            info.localAxisA.set(e.groundAnchorA);
            info.groundAnchorB.set(e.groundAnchorB);
            info.anchorA.set(e.anchorA);
            info.anchorB.set(e.anchorB);
            info.maxForce = e.lengthA;
            info.maxTorque = e.lengthB;
            info.ratio = e.ratio;
        }else if (joint instanceof SavedFileDecoder.RevoluteJointInfo){
            SavedFileDecoder.RevoluteJointInfo e = (SavedFileDecoder.RevoluteJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 7, info, jointManager)){
                return;
            }
            info.anchorA.set(e.anchorA);
            info.anchorB.set(e.anchorB);
            info.length = e.referenceAngle;
            info.frequencyHz = e.lowerAngle;
            info.dampingRatio = e.upperAngle;
            info.enableLimit = e.enableLimit;
            info.enableMotor = e.enableMotor;
            info.maxForce = e.motorSpeed;
            info.maxTorque = e.maxMotorTorque;
        }else if (joint instanceof SavedFileDecoder.RopeJointInfo){
            SavedFileDecoder.RopeJointInfo e = (SavedFileDecoder.RopeJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 8, info, jointManager)){
                return;
            }
            info.anchorA.set(e.anchorA);
            info.anchorB.set(e.anchorB);
            info.length = e.maxLength;
        }else if (joint instanceof SavedFileDecoder.WeldJointInfo){
            SavedFileDecoder.WeldJointInfo e = (SavedFileDecoder.WeldJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 9, info, jointManager)){
                return;
            }
            info.anchorA.set(e.anchorA);
            info.anchorB.set(e.anchorB);
            info.length = e.referenceAngle;
            info.frequencyHz = e.frequencyHz;
            info.dampingRatio = e.dampingRatio;
        }else if (joint instanceof SavedFileDecoder.WheelJointInfo){
            SavedFileDecoder.WheelJointInfo e = (SavedFileDecoder.WheelJointInfo) joint;
            if (logErrorJoint(info.getJointType(), 10, info, jointManager)){
                return;
            }
            info.anchorA.set(e.anchorA);
            info.anchorB.set(e.anchorB);
            info.localAxisA.set(e.localAxisA);
            info.enableMotor = e.enableMotor;
            info.maxTorque = e.maxMotorTorque;
            info.maxForce = e.motorSpeed;
            info.frequencyHz = e.frequencyHz;
            info.dampingRatio = e.dampingRatio;
        }

        // bendra info
        info.bodyA = joint.bodyA == null || joint.bodyA.length() == 0 ? null : joint.bodyA;// jeigu ne null tai jis galvoja, kad cia valid id.
        info.bodyB = joint.bodyB == null || joint.bodyB.length() == 0 ? null : joint.bodyB;
        info.bodyAIsResource = joint.bodyAResource;
        info.bodyBIsResource = joint.bodyBResource;
        info.collideConnected = joint.collideConnected;
    }

    private boolean logErrorJoint(int type, int preferredType, JointManager.JointInfo info, JointManager jointManager){
        if (type != preferredType){
            jointManager.removeJoint(info); // pametam, nes sukure.
            AlertToast toast = new AlertToast("Failed loading joint with id: " + info.getJointID() + ". Joint types doesn't match!");
            toast.show(Toast.SHORT);
            return true;
        }
        return false;
    }
}
