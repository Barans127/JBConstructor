package com.engine.jbconstructor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.XmlReader;
import com.engine.core.MoreUtils;

import java.io.IOException;

/** Reads JBConstructor saved files. decodes them. */
public class SavedFileDecoder {
    /** physics fixtures types. */
    public static final int POLYGON = 0, CIRCLE = 1, CHAIN = 2, EDGE = 3;

    private FileHandle saveFile;

    private String projectName, projectRoot, projectResourcesFolder;
    private boolean exportSave;

    private Array<ProjectResource> projectResources;
    private Array<ProjectForm> projectForms;
    private Array<PhysicsShapesGroup> shapesGroups;

    private String errorMessage = null;

    private float saveVersion;

    private boolean saveWasRead;

    public SavedFileDecoder(){
        projectForms = new Array<>();
        shapesGroups = new Array<>();
    }

    /* informacijos gavimas */

    /** Save file version. If save file was not read then 0. */
    public float getSaveVersion(){
        return saveVersion;
    }

    public boolean isSaveFileDecoded(){
        return saveWasRead;
    }

    /** @return project name. if error occurred or file was not yet red then null is returned. */
    public String getProjectName(){
        return projectName;
    }

    /** @return project root folder. if error occurred or file was not yet red then null is returned. */
    public String getProjectRoot() {
        return projectRoot;
    }

    /** @return folder path, from where new resources may be scanned. */
    public String getProjectResourcesFolder() {
        return projectResourcesFolder;
    }

    /** @return determines if save file is exporterd file or is it normal save file. */
    public boolean isExportSaveFile(){
        return exportSave;
    }

    /** @return error message. If no error occurred then null is returned. */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** @return project forms info */
    public Array<ProjectForm> getProjectForms() {
        return projectForms;
    }

    /** @return project resources info */
    public Array<ProjectResource> getProjectResources() {
        return projectResources;
    }

    /** @return physics. Use {@link ResourceInfo#physicsShapeGroupIndex} to get physics by index.  */
    public Array<PhysicsShapesGroup> getPhysicsShapeGroups(){
        return shapesGroups;
    }

    /* Save file nuskaitimas. */

    /** save file which was read. null if no save file was read yet.
     * Null if save was read not from file. */
    public FileHandle getSaveFile() {
        return saveFile;
    }

    /** reads given file. If error occurs it is written to error message.
     * @param json is this file written in json format.
     * @param compressed was this file compressed and should be decompressed before reading.*/
    public void readSave(FileHandle save, boolean json, boolean compressed){
        errorMessage = null; // isvalom klaidas.
//        saveWasRead = true;
        if (!save.exists() || save.isDirectory()){
            errorMessage = "Cannot read given file. File doesn't exist or file is directory.";
            return;
        }
        saveFile = save;
        try {
            String info = save.readString();
            if (json){
                readJsonSave(info, compressed);
            }else {
                if (compressed){
                    try {
                        byte[] array = Base64Coder.decode(info);
                        String decompressed = MoreUtils.decompress(array); // bandom decompressint.
                        readSave(decompressed);
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorMessage = "Failed decompressing info";
                    }
                }else {
                    readSave(info);
                }
            }
        }catch (GdxRuntimeException ex){
            ex.printStackTrace();
            errorMessage = "Cannot read given file: " + save.name();
        }
    }

    /** Reads json and decompress if compressed */
    public void readJsonSave(String info, boolean compressed){
        errorMessage = null;
        if (info == null || info.length() == 0){
            errorMessage = "Info is empty...";
            return;
        }

        if (compressed){
            try {
                byte[] array = Base64Coder.decode(info);
                String decompressed = MoreUtils.decompress(array); // bandom decompressint.
                readJsonSave(decompressed);
            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = "Failed decompressing info";
            }
        }else {
            readJsonSave(info); // nieko ypatingo.
        }
    }

    /** Reads json info. Must be decompressed! If compressed file use {@link #readJsonSave(String, boolean)} */
    public void readJsonSave(String info){
        errorMessage = null;
        if (info == null || info.length() == 0){
            errorMessage = "File is empty...";
            return;
        }

        Json json = new Json();
        try {
            SavedFileDecoderInfo decoderInfo = json.fromJson(SavedFileDecoderInfo.class, info);
            /* main info. */
            saveVersion = decoderInfo.version; // sugaudom save versija.
            exportSave = true; // json failai tik exportinami yra.
            projectResources = null; // exportintas file. Nereik tu resources.
            projectName = decoderInfo.projectName;
            projectRoot = decoderInfo.projectResourceFile;
            projectResourcesFolder = null;

            projectForms.clear(); // pirma issivalom del viso pykto.
            projectForms.addAll(decoderInfo.projectForms); // sumetam viska.

            // sumetam fizikos grupes.
            shapesGroups.clear();
            shapesGroups.addAll(decoderInfo.shapesGroup);

//            // o, dabar tokia operacija kur xml nera. Kadangi json suspaustas, fizikos nera idetos i resourcus. Dabar reiks jas sugaudet ir sudet...
//            for (ProjectForm form : projectForms){
//                // einam per visas formas ir visus resursus tikrinsim.
//                for (ResourceInfo e : form.resourceInfos){
//                    int index = e.physicsShapeGroupIndex;
//                    if (index >= 0){
//                        // turi fizikas.
//                        if (index < decoderInfo.shapesGroup.size){
//                            // pirma paimam tuo main.
//                            PhysicsShapesGroup group = decoderInfo.shapesGroup.get(index);
//                            e.bodyType = group.bodyType;
//                            e.isOriginMiddle = group.isOriginMiddle;
//                            if (!e.isOriginMiddle){ // jeigu origin ne middle, tai reik paimt sita origin.
//                                e.bodyOrigin.set(group.bodyOrigin);
//                            }
//
//                            // dabar imsim taskus ir visa kita.
//                            e.shapes.addAll(group.shapes); // tiesiog sitaip viska sumetam ir tiek.
//                        }else {
//                            // jei cia atejo, tai kazkas ne to
//                            if (errorMessage == null){
//                                errorMessage = "";
//                            }
//                            errorMessage = "Unable to recreate physics for this element: " + e.idName + "\n";
//                        }
//                    }
//                }
//            }

            // pazymim, kad nuskaite.
            saveWasRead = true;
        }catch (SerializationException ex){
            ex.printStackTrace();
            errorMessage = "Failed to read save information";
        }
    }

    /** Reads xml save from string. String must be xml type and not compressed. */
    public void readSave(String info){
        errorMessage = null;
        saveWasRead = true;
        if (info == null){
            errorMessage = "Save file info cannot be null";
            return;
        }
//        saveFile = save;
        projectForms.clear();
        XmlReader reader = new XmlReader();
        XmlReader.Element jbcData;
        try {
            jbcData = reader.parse(info);
        }catch (SerializationException ex){
            if (errorMessage == null){
                errorMessage = "";
            }
            errorMessage += "Cannot read file. File is corrupted.";
            return;
        }
        if (jbcData.getName().equals("JBC_data")){
            saveVersion = jbcData.getFloatAttribute("version", 0); // sio save file versija.
            XmlReader.Element project, resources;
            project = jbcData.getChildByName("Project");
            if (project == null){
                if (errorMessage == null){
                    errorMessage = "";
                }
                errorMessage += "Cannot parse file.";
                return;
            }
            // projekto duomenys
            String pname;
            String pfolder = null;
            boolean export;
            try {
                pname = project.getAttribute("name");
                export = project.getBooleanAttribute("export", false);
                if (!export) {
//                    proot = project.getAttribute("root");
                    pfolder = project.getAttribute("resourcesFolder");
                }else {
                    projectRoot = project.getAttribute("resources"); // resources txt failas.
                }
            }catch (GdxRuntimeException ex){
                if (errorMessage == null){
                    errorMessage = "";
                }
                errorMessage += "Project name or root was not located.";
                return;
            }
            projectName = pname;
//            projectRoot = proot;
            if (!export) {
                if (saveFile != null) { // root folderi sugaudom is failo vietos, kur jis yra - ten ir root.
                    projectRoot = saveFile.file().getParent().replace("\\", "/");
                    projectResourcesFolder = pfolder;
                    if (pfolder != null && pfolder.startsWith(".")) {
                        projectResourcesFolder = pfolder.replaceFirst(".", projectRoot);
                    }
                } else {
                    projectRoot = null;
                    projectResourcesFolder = null;
                }
            }
            exportSave = export;
            // resources
            resources = jbcData.getChildByName("Resources");
            if (resources != null){
                readProjectResources(resources); // nuskaitom visas resources jei tokios yra.
            }else {
                projectResources = new Array<>(); // tiesiog tuscia palikt
            }
            //formos
            Array<XmlReader.Element> forms = jbcData.getChildrenByName("FORM");
            for (XmlReader.Element form : forms){
                readForm(form);
            }

            // physics nuskaitymas.
            if (saveVersion >= 0.9f){
                shapesGroups.clear(); // apsivalom..
                XmlReader.Element phcs = jbcData.getChildByName("Phcs");
                if (phcs != null){// turi but fizikos skyrius.
                    Array<XmlReader.Element> groups = phcs.getChildrenByName("Body");
                    for (XmlReader.Element e : groups){ // einam per visus body.
                        PhysicsShapesGroup group = readPhysicsShapeGroup(e); // dedam i grupes.
                        if (group != null){
                            shapesGroups.add(group);
                        }
                    }
                }
            }
        }else {
            // klaida, nes ne tas kazkas.
            if (errorMessage == null){
                errorMessage = "";
            }
            errorMessage += "Cannot parse file.";
        }
    }

    private void readForm(XmlReader.Element form){
        XmlReader.Element cam = form.getChildByName("camera"); // kameros nustatymai
        float x = 640, y = 320, zoom = 1f;
        if (cam != null) {
            x = cam.getFloatAttribute("x", 640);
            y = cam.getFloatAttribute("y", 320);
            zoom = cam.getFloatAttribute("zoom", 1f);
        }
        // nustatymai
        boolean showF = true, showA = true, showC = true, showB = true;
        XmlReader.Element set = form.getChildByName("Settings");
        if (set != null) {
            showA = set.getBooleanAttribute("showAbsolute", true);
            showB = set.getBooleanAttribute("screenBounds", true);
            showC = set.getBooleanAttribute("showCoords", true);
            showF = set.getBooleanAttribute("showFixed", true);
        }
        // chain edge nuskaitymas
        XmlReader.Element chains = form.getChildByName("chains");
        Array<ProjectChain> chainsInfo = new Array<>();
        if (chains != null) { // paziurim ar toks isvis yra.
            for (XmlReader.Element chain : chains.getChildrenByName("chain")){
                ProjectChain e = readChain(chain);
                if (e != null){ // jeigu nebuvo corrupted tai prides. Corrupted chain ignoruos.
                    chainsInfo.add(e);
                }
            }
        }

        XmlReader.Element joints = form.getChildByName("joints");
        Array<JointBaseInfo> jointsInfo = new Array<>();
        if (joints != null){
            for (XmlReader.Element joint : joints.getChildrenByName("joint")){
                JointBaseInfo baseInfo = readJointInfo(joint);
                if (baseInfo != null){
                    jointsInfo.add(baseInfo);
                }
            }
        }

        // item pridejimas
        Array<ResourceInfo> items = new Array<>();
        for (XmlReader.Element item : form.getChildrenByName("Item")){
            items.add(readItem(item));
        }
        // viska sumetam i forma.
        ProjectForm pform = new ProjectForm();
        pform.name = form.getAttribute("name", "No name");
        pform.background = form.getAttribute("background", "ffffffff"); // balta jei nera
        pform.cameraX = x;
        pform.cameraY = y;
        pform.cameraZoom = zoom;
        pform.showAbsolute = showA;
        pform.showBounds = showB;
        pform.showCoords = showC;
        pform.showFix = showF;
        pform.resourceInfos.addAll(items);
        pform.chainsInfos.addAll(chainsInfo); // jeigu nieko ner, tai tiesiog duos tuscia array.
        pform.jointsInfos.addAll(jointsInfo); // same as chains.
        projectForms.add(pform);
    }

    private ProjectChain readChain(XmlReader.Element chain){
        ProjectChain info = new ProjectChain();
        info.name = chain.getAttribute("name", null);
        info.loop = chain.getBooleanAttribute("loop", false);
        info.isSensor = chain.getBooleanAttribute("isSensor", false);
        info.maskBits = chain.getIntAttribute("maskBits", -1);
        info.categoryBits = chain.getIntAttribute("categoryBits", 1);
        info.groupIndex = chain.getIntAttribute("groupIndex", 0);
        for (XmlReader.Element point : chain.getChildrenByName("point")){
            float x, y;
            try {
                x = point.getFloatAttribute("x");
                y = point.getFloatAttribute("y");
            }catch (GdxRuntimeException ex){
                // ka darom klaidos atveju? lauzom visa, ar ignoruojam?
                return null; // lauzom visa. chain is corrupted.
            }
            info.x.add(x);
            info.y.add(y);
        }
        return info;
    }

    private ResourceInfo readItem(XmlReader.Element item){
        ResourceInfo info = new ResourceInfo();
        info.type = item.getAttribute("type", null);
        XmlReader.Element param = item.getChildByName("param"); // paprastieji parametrai.
        if (param == null){
            if (errorMessage == null){
                errorMessage = "";
            }
            errorMessage += "item parameters not found.";
            return info;
        }
        // paprastuju parametru nuskaitymas.
        info.x = param.getFloatAttribute("x", 0);
        info.y = param.getFloatAttribute("y", 0);
        info.radius = param.getFloatAttribute("angle", 0);
        info.positioning = param.getIntAttribute("positioning",2);
        info.idName = param.getAttribute("idName", null);
        info.width = param.getFloatAttribute("width", 100);
        info.height = param.getFloatAttribute("height", 100);
        info.resource = param.getAttribute("resource", null);
        // naujos value.
        info.flipX = param.getBooleanAttribute("flipX", false);
        info.flipY = param.getBooleanAttribute("flipY", false);
        // naujai vadinama element, senesniems projektas buvo vadinama resource. Naudojam abu, nes yra daug senu projektu.
        if (info.type.equals("Element") || info.type.equals("Resource")){ // tai paveikslelis.
            info.tint = param.getAttribute("tint", "0");
            if (saveVersion < 0.9f) { // senesnes versijos.
//                info.shapes.addAll(readOldPhysics(param.getChildByName("phcs"), info));
                info.physicsShapeGroupIndex = readOldPhysicsVersion08OrLower(param.getChildByName("phcs"));
            }else {
                // 0.9 versijai uztenka grupe idet. Fizikos nuskaitomos kitur.
                info.physicsShapeGroupIndex = param.getIntAttribute("physicsGroupIndex", -1);
            }
        }
        // toliau reiktu daugiau info skaityt, bet cia jau su kitais interface.
        return info;
    }

    // visi resoursai.
    private void readProjectResources(XmlReader.Element resources){
        // pirmiausia atlas uzkraut.
        Array<ProjectResource> files = new Array<>();
        for (XmlReader.Element atlas : resources.getChildrenByName("Atlas")){ // atlas nuskaitymas.
            String name, path;
            try {
                name = atlas.getAttribute("name");
                path = atlas.getAttribute("path"); // // nuskaitom path. Pakeiciam taska i musu failo esama vieta.
                if (path != null && path.startsWith(".") && projectRoot != null){
                    path = path.replace("\\", "/").replaceFirst(".", projectRoot);
                }
            }catch (GdxRuntimeException ex){
                if (errorMessage == null){
                    errorMessage = "";
                }
                errorMessage += ex.getMessage() + ". Cannot locate atlas without missing attributes.\n";
                continue;
            }
            ProjectResource res = new ProjectResource(); // trys parametrai reikalingi atlasui.
            res.isAtlas = true;
            res.atlas = name;
            res.path = path;
            files.add(res);
        }
        for (XmlReader.Element resource : resources.getChildrenByName("resource")){
            String id, atlas = null, path = null;
            try {
                id = resource.getAttribute("id");
            }catch (GdxRuntimeException ex){
                if (errorMessage == null){
                    errorMessage = "";
                }
                errorMessage += ex.getMessage() + ". Id attribute for resource is needed.";
                continue;
            }
            if (resource.hasAttribute("atlas")){
                atlas = resource.getAttribute("atlas");
            }
            if (resource.hasAttribute("path")){ // nuskaitom path. Pakeiciam taska i musu failo esama vieta.
                path = resource.getAttribute("path");
                if (path != null && path.startsWith(".") && projectRoot != null){
                    path = path.replace("\\", "/").replaceFirst(".", projectRoot);
                }
            }
            ProjectResource res = new ProjectResource();
            res.id = id;
            res.path = path;
            res.atlas = atlas;
            res.isAtlas = false;
//            if (resource.hasChild("phcs")) {
//                res.shapes.addAll(readPhysics(resource.getChildByName("phcs"), null));
//            }
            files.add(res);
        }
        projectResources = files;
    }

    /* nuskaitys phcs esancius elementus. Senesnems jbconstructor versijoms. Mazesnems versijoms nei <0.9*/
    private int readOldPhysicsVersion08OrLower(XmlReader.Element phcs){
//        Array<PhysicsFixtureShapes> shapes = new Array<>();
        if (phcs == null)
//            return shapes;
            return -1;
//        if (resourceInfo != null){ // reiskia cia entity. turi body, ne projekto save file.
        PhysicsShapesGroup group = new PhysicsShapesGroup(); // senoms versijoms kaskart kuriam visa nauja grupe.
        XmlReader.Element body = phcs.getChildByName("Body");
        if (body != null){
            boolean middle;
            float x = 0, y = 0;
            BodyDef.BodyType e;
            middle = body.getBooleanAttribute("originMiddle", true); // sugaudom origin
            if (!middle){
                x = body.getFloatAttribute("x", 0);
                y = body.getFloatAttribute("y", 0);
            }
            String bodyType;
            bodyType = body.getAttribute("BodyType", "DynamicBody"); // sugaudom body type.
            switch (bodyType) {
                case "KinematicBody":
                    e = BodyDef.BodyType.KinematicBody;
                    break;
                case "StaticBody":
                    e = BodyDef.BodyType.StaticBody;
                    break;
                default:
                    e = BodyDef.BodyType.DynamicBody;
                    break;
            }
//            resourceInfo.bodyType = e;
//            resourceInfo.isOriginMiddle = middle;
//            resourceInfo.bodyOrigin.x = x;
//            resourceInfo.bodyOrigin.y = y;
            // senoms fizikoms tik tiek elementu, todel dauigau net neziurim. Ner tolko.
            group.bodyType = e;
            group.isOriginMiddle = middle;
            group.bodyOrigin.set(x, y);
        }
//        }
        for (XmlReader.Element figure : phcs.getChildrenByName("figure")){
            // ims pirma pasitaikiusia forma.
//            EditForm form = Project.getFormsManager().getForm(0);
//            PhysicsFixtureShapes shapeHolder;
////            if (form == null){
////                // sukurt tiesiog tokia forma.
////                Project.getFormsManager().createEditForm("default form");
////                form = Project.getFormsManager().getForm(0);
//////                if (errorMessage == null){
//////                    errorMessage = "";
//////                }
//////                errorMessage += "Cannot locate any forms instace. Physics shapes cannot be created whithout it.\n";
//////                return shapes;
////            }
//            shapeHolder = new PhysicsFixtureShapes();
//            shapeHolder.type = figure.getIntAttribute("type", 0);
//            shapeHolder.radius = figure.getFloatAttribute("radius", 0);
//            shapeHolder.density = figure.getFloatAttribute("density", 0);
//            shapeHolder.friction = figure.getFloatAttribute("friction", 0.2f);
//            shapeHolder.restitution = figure.getFloatAttribute("restitution", 0);
//            shapeHolder.maskBits = (short) figure.getFloatAttribute("maskBits", -1);
//            shapeHolder.categoryBits = (short) figure.getFloatAttribute("categoryBits", 0x0001);
//            shapeHolder.groupIndex = (short) figure.getFloatAttribute("group", 0);
//            shapeHolder.isSensor = figure.getBooleanAttribute("sensor", false);
//            for (XmlReader.Element point : figure.getChildrenByName("point")){
//                float x, y;
//                x = point.getFloatAttribute("x", 0);
//                y = point.getFloatAttribute("y", 0);
//                shapeHolder.x.add(x);
//                shapeHolder.y.add(y);
//            }
////            shapes.add(shapeHolder);
//            group.shapes.add(shapeHolder);
            readFixture(figure, group);
        }
        shapesGroups.add(group);
        group.id = shapesGroups.size-1; // butinai id suteikiam.
        return shapesGroups.size-1; // paskutini elementa grazinam tiesiog.
    }

    private PhysicsShapesGroup readPhysicsShapeGroup(XmlReader.Element body){
        if (body == null){
            return null;
        }

        // pirma sukuriam nauja grupe.
        PhysicsShapesGroup group = new PhysicsShapesGroup();
        // nustatom fizikos grupes id. Sis id naudojamas tik nustatyt ar si grupe tikrai priklauso tam tikram elementui...
        group.id = body.getIntAttribute("id", -1);

        // surandam body type.
        String stringBodyType = body.getAttribute("BodyType", "DynamicBody");
        BodyDef.BodyType bodyType;
        switch (stringBodyType){
            case "StaticBody":
                bodyType = BodyDef.BodyType.StaticBody;
                break;
            case "KinematicBody":
                bodyType = BodyDef.BodyType.KinematicBody;
                break;
            default:
                bodyType = BodyDef.BodyType.DynamicBody;
                break;
        }

        // sugaudom parametrus.
        boolean middle = body.getBooleanAttribute("originMiddle", false);
        boolean fixedRotation = body.getBoolean("fixedRotation", false);
        boolean bullet = body.getBooleanAttribute("bullet", false);
        float linearDamping = body.getFloatAttribute("linearDamping", 0);
        float angularDamping = body.getFloatAttribute("angularDamping", 0);
        float gravityScale = body.getFloatAttribute("gravityScale", 1f);
        float angularVelocity = body.getFloatAttribute("angularVelocity", 0);

        // sudedam parametrus.
        group.bodyType = bodyType;
        group.isOriginMiddle = middle;
        group.fixedRotation = fixedRotation;
        group.bullet = bullet;
        group.linearDamping = linearDamping;
        group.angularDamping = angularDamping;
        group.gravityScale = gravityScale;
        group.angularVelocity = angularVelocity;

        if (!middle){
            // origin ne middle. Bandom rast origin.
            XmlReader.Element originElement = body.getChildByName("bodyOrigin");
            if (originElement != null){ // radom.
                float x = originElement.getFloatAttribute("x", 0);
                float y = originElement.getFloatAttribute("y", 0);

                group.bodyOrigin.set(x, y);
            }
        }

        // linear velocity surandam
        XmlReader.Element linearVelocity = body.getChildByName("linearVelocity");
        if (linearVelocity != null){
            float x = linearVelocity.getFloatAttribute("x", 0);
            float y = linearVelocity.getFloatAttribute("y", 0);

            group.linearVelocity.set(x, y);
        }

        // toliau fixturas sugaudom.
        for (XmlReader.Element fixture : body.getChildrenByName("figure")){
            readFixture(fixture, group); // cia viska sumes ir sudes kaip priklauso.
        }

//        BodyDef.BodyType bodyType = body.getAttribute("BodyType", "");
        return group;

    }

    private void readFixture(XmlReader.Element figure, PhysicsShapesGroup group){
        // nuskaitom viena is fixturu
        PhysicsFixtureShapes shapeHolder;
        shapeHolder = new PhysicsFixtureShapes();
        shapeHolder.type = figure.getIntAttribute("type", 0);
        shapeHolder.radius = figure.getFloatAttribute("radius", 0);
        shapeHolder.density = figure.getFloatAttribute("density", 0);
        shapeHolder.friction = figure.getFloatAttribute("friction", 0.2f);
        shapeHolder.restitution = figure.getFloatAttribute("restitution", 0);
        shapeHolder.maskBits = (short) figure.getFloatAttribute("maskBits", -1);
        shapeHolder.categoryBits = (short) figure.getFloatAttribute("categoryBits", 0x0001);
        shapeHolder.groupIndex = (short) figure.getFloatAttribute("group", 0);
        shapeHolder.isSensor = figure.getBooleanAttribute("sensor", false);
        for (XmlReader.Element point : figure.getChildrenByName("point")){
            float x, y;
            x = point.getFloatAttribute("x", 0);
            y = point.getFloatAttribute("y", 0);
            shapeHolder.x.add(x);
            shapeHolder.y.add(y);
        }
        group.shapes.add(shapeHolder);
    }

    private JointBaseInfo readJointInfo(XmlReader.Element joint){
        String id = joint.getAttribute("id", null); // jointo id.
        if (id == null){
            return null; // kazkas negerai.
        }
        // surenkam bendra info pries kuriant joint.
        int type = joint.getIntAttribute("type", -1);
        String bodyA = joint.getAttribute("bodyA", null);
        String bodyB = joint.getAttribute("bodyB", null);
        boolean bodyAResource = joint.getBooleanAttribute("bodyAResource", false);
        boolean bodyBResource = joint.getBooleanAttribute("bodyBResource", false);
        boolean collideConnected = joint.getBooleanAttribute("colliedConnected", false);

        JointBaseInfo baseInfo;
        // toliau kuriam pagal tai koks type.
        switch (type){
            case 0:{ // distance
                //sumetam visa info
                DistanceJointInfo e = new DistanceJointInfo();
                e.length = joint.getFloatAttribute("length", 1);
                e.frequencyHz = joint.getFloatAttribute("frequencyHz", 0);
                e.dampingRatio = joint.getFloatAttribute("dampingRatio", 0);
                // anchor info
                XmlReader.Element anchorA = joint.getChildByName("anchorA");
                if (anchorA != null){
                    e.anchorA.set(anchorA.getFloatAttribute("x", 0), anchorA.getFloatAttribute("y", 0));
                }
                XmlReader.Element anchorB = joint.getChildByName("anchorB");
                if (anchorB != null){
                    e.anchorB.set(anchorB.getFloatAttribute("x", 0), anchorB.getFloatAttribute("y", 0));
                }
                // prilyginam.
                baseInfo = e;
                break;
            }
            case 1: { // friction
                FrictionJointInfo e = new FrictionJointInfo();
                e.maxForce = joint.getFloatAttribute("maxForce", 0);
                e.maxTorque = joint.getFloatAttribute("maxTorque", 0);
                // anchor info
                XmlReader.Element anchorA = joint.getChildByName("anchorA");
                if (anchorA != null){
                    e.anchorA.set(anchorA.getFloatAttribute("x", 0), anchorA.getFloatAttribute("y", 0));
                }
                XmlReader.Element anchorB = joint.getChildByName("anchorB");
                if (anchorB != null){
                    e.anchorB.set(anchorB.getFloatAttribute("x", 0), anchorB.getFloatAttribute("y", 0));
                }
                // prilyginam.
                baseInfo = e;
                break;
            }
            case 2: { // gear
                GearJointInfo e = new GearJointInfo();
                e.joint1Id = joint.getAttribute("joint1Id", null);
                e.joint2Id = joint.getAttribute("joint2Id", null);
                e.ratio = joint.getFloatAttribute("ratio", 1);
                baseInfo = e;
                break;
            }
            case 3: { // motor
                MotorJointInfo e = new MotorJointInfo();
                e.angularOffset = joint.getFloatAttribute("angularOffset", 0);
                e.maxForce = joint.getFloatAttribute("maxForce", 1);
                e.maxTorque = joint.getFloatAttribute("maxTorque", 1);
                e.correctionFactor = joint.getFloatAttribute("correctionFactor", 0.3f);
                XmlReader.Element linearOffset = joint.getChildByName("linearOffset");
                if (linearOffset != null){
                    e.linearOffset.set(linearOffset.getFloatAttribute("x", 0), linearOffset.getFloatAttribute("y", 0));
                }
                baseInfo = e;
                break;
            }
            case 4: { // mouse
                MouseJointInfo e = new MouseJointInfo();
                e.maxForce = joint.getFloatAttribute("maxForce", 0);
                e.frequencyHz = joint.getFloatAttribute("frequencyHz", 5);
                e.dampingRatio = joint.getFloatAttribute("dampingRatio", 0.7f);
                XmlReader.Element target = joint.getChildByName("target");
                if (target != null){
                    e.target.set(target.getFloatAttribute("x", 0), target.getFloatAttribute("y", 0));
                }
                baseInfo = e;
                break;
            }
            case 5: { // prismatic
                PrismaticJointInfo e = new PrismaticJointInfo();
                e.referenceAngle = joint.getFloatAttribute("referenceAngle", 0);
                e.enableLimit = joint.getBooleanAttribute("enableLimit", false);
                e.lowerTranslation = joint.getFloatAttribute("lowerTranslation", 0);
                e.upperTranslation = joint.getFloatAttribute("upperTranslation", 0);
                e.enableMotor = joint.getBooleanAttribute("enableMotor", false);
                e.maxMotorForce = joint.getFloatAttribute("maxForce", 0);
                e.maxMotorSpeed = joint.getFloatAttribute("motorSpeed", 0);
                // anchor info
                XmlReader.Element anchorA = joint.getChildByName("anchorA");
                if (anchorA != null){
                    e.anchorA.set(anchorA.getFloatAttribute("x", 0), anchorA.getFloatAttribute("y", 0));
                }
                XmlReader.Element anchorB = joint.getChildByName("anchorB");
                if (anchorB != null){
                    e.anchorB.set(anchorB.getFloatAttribute("x", 0), anchorB.getFloatAttribute("y", 0));
                }
                XmlReader.Element localAxisA = joint.getChildByName("localAxisA");
                if (localAxisA != null){
                    e.localAxisA.set(localAxisA.getFloatAttribute("x", 1), localAxisA.getFloatAttribute("y", 0));
                }
                baseInfo = e;
                break;
            }
            case 6: { // pulley
                PulleyJointInfo e = new PulleyJointInfo();
                e.lengthA = joint.getFloatAttribute("lengthA", 0);
                e.lengthB = joint.getFloatAttribute("lengthB", 0);
                e.ratio = joint.getFloatAttribute("ratio", 0);
                XmlReader.Element anchorA = joint.getChildByName("anchorA");
                if (anchorA != null){
                    e.anchorA.set(anchorA.getFloatAttribute("x", -1), anchorA.getFloatAttribute("y", 0));
                }
                XmlReader.Element anchorB = joint.getChildByName("anchorB");
                if (anchorB != null){
                    e.anchorB.set(anchorB.getFloatAttribute("x", 1), anchorB.getFloatAttribute("y", 0));
                }
                XmlReader.Element groundAnchorA = joint.getChildByName("groundAnchorA");
                if (groundAnchorA != null){
                    e.groundAnchorA.set(groundAnchorA.getFloatAttribute("x", -1), groundAnchorA.getFloatAttribute("y", 1));
                }
                XmlReader.Element groundAnchorB = joint.getChildByName("groundAnchorB");
                if (groundAnchorB != null){
                    e.groundAnchorB.set(groundAnchorB.getFloatAttribute("x", 1), groundAnchorB.getFloatAttribute("y", 1));
                }
                baseInfo = e;
                break;
            }
            case 7: { // revolute
                RevoluteJointInfo e = new RevoluteJointInfo();
                e.referenceAngle = joint.getFloatAttribute("referenceAngle", 0);
                e.enableLimit = joint.getBooleanAttribute("enableLimit", false);
                e.lowerAngle = joint.getFloatAttribute("lowerAngle", 0);
                e.upperAngle = joint.getFloatAttribute("upperAngle", 0);
                e.enableMotor = joint.getBooleanAttribute("enableMotor", false);
                e.motorSpeed = joint.getFloatAttribute("motorSpeed", 0);
                e.maxMotorTorque = joint.getFloatAttribute("motorTorque", 0);
                XmlReader.Element anchorA = joint.getChildByName("anchorA");
                if (anchorA != null){
                    e.anchorA.set(anchorA.getFloatAttribute("x", 0), anchorA.getFloatAttribute("y", 0));
                }
                XmlReader.Element anchorB = joint.getChildByName("anchorB");
                if (anchorB != null){
                    e.anchorB.set(anchorB.getFloatAttribute("x", 0), anchorB.getFloatAttribute("y", 0));
                }
                baseInfo = e;
                break;
            }
            case 8: { // rope
                RopeJointInfo e = new RopeJointInfo();
                e.maxLength = joint.getFloatAttribute("maxLength", 0);
                XmlReader.Element anchorA = joint.getChildByName("anchorA");
                if (anchorA != null){
                    e.anchorA.set(anchorA.getFloatAttribute("x", 0), anchorA.getFloatAttribute("y", 0));
                }
                XmlReader.Element anchorB = joint.getChildByName("anchorB");
                if (anchorB != null){
                    e.anchorB.set(anchorB.getFloatAttribute("x", 0), anchorB.getFloatAttribute("y", 0));
                }
                baseInfo = e;
                break;
            }
            case 9: { // weld
                WeldJointInfo e = new WeldJointInfo();
                e.referenceAngle = joint.getFloatAttribute("referenceAngle", 0);
                e.frequencyHz = joint.getFloatAttribute("frequencyHz", 0);
                e.dampingRatio = joint.getFloatAttribute("dampingRatio", 0);
                XmlReader.Element anchorA = joint.getChildByName("anchorA");
                if (anchorA != null){
                    e.anchorA.set(anchorA.getFloatAttribute("x", 0), anchorA.getFloatAttribute("y", 0));
                }
                XmlReader.Element anchorB = joint.getChildByName("anchorB");
                if (anchorB != null){
                    e.anchorB.set(anchorB.getFloatAttribute("x", 0), anchorB.getFloatAttribute("y", 0));
                }
                baseInfo = e;
                break;
            }
            case 10: { // wheel
                WheelJointInfo e = new WheelJointInfo();
                e.enableMotor = joint.getBooleanAttribute("enableMotor", false);
                e.maxMotorTorque = joint.getFloatAttribute("motorTorque", 0);
                e.motorSpeed = joint.getFloatAttribute("motorSpeed", 0);
                e.frequencyHz = joint.getFloatAttribute("frequencyHz", 0);
                e.dampingRatio = joint.getFloatAttribute("dampingRatio", 0);
                // anchor info
                XmlReader.Element anchorA = joint.getChildByName("anchorA");
                if (anchorA != null){
                    e.anchorA.set(anchorA.getFloatAttribute("x", 0), anchorA.getFloatAttribute("y", 0));
                }
                XmlReader.Element anchorB = joint.getChildByName("anchorB");
                if (anchorB != null){
                    e.anchorB.set(anchorB.getFloatAttribute("x", 0), anchorB.getFloatAttribute("y", 0));
                }
                XmlReader.Element localAxisA = joint.getChildByName("localAxisA");
                if (localAxisA != null){
                    e.localAxisA.set(localAxisA.getFloatAttribute("x", 1), localAxisA.getFloatAttribute("y", 0));
                }
                baseInfo = e;
                break;
            }
            default: { // nera type, kuriam be type.
                baseInfo = new JointBaseInfo();
                break;
            }
        }
        baseInfo.type = type;
        baseInfo.id = id;
        baseInfo.bodyA = bodyA;
        baseInfo.bodyB = bodyB;
        baseInfo.bodyAResource = bodyAResource;
        baseInfo.bodyBResource = bodyBResource;
        baseInfo.collideConnected = collideConnected;
        return baseInfo;
    }

    /* Klases su info. */

    /** Item information in form. */
    public static class ResourceInfo{
        public String type;
//        public transient BodyDef.BodyType bodyType;
//        public transient boolean isOriginMiddle = true;
//        public transient final Vector2 bodyOrigin = new Vector2();

        // paprastieji kintamieji, tinka paprastiem image.
        public float x, y, radius, width, height;
        public int positioning;
        public String idName, tint, resource;
        public boolean flipX, flipY;

        // fizikos. tik image priklauso.
//        public transient final Array<PhysicsFixtureShapes> shapes = new Array<>();
        /** This is used when decoder decodes exported file from json. This loads physics points to this resource. */
        public int physicsShapeGroupIndex = -1;
    }

    /** use this to get resources path and other stuff. */
    public static class ProjectResource{
        public String id;
        public String path;
        public String atlas;

        public boolean isAtlas;

//        public final Array<PhysicsFixtureShapes> shapes = new Array<>();
    }

    /** project forms informations */
    public static class ProjectForm{
        public String name;
        public transient float cameraX, cameraY, cameraZoom;
        public transient boolean showFix = true, showAbsolute = true, showCoords = true, showBounds = true;
        public String background;

        public Array<ResourceInfo> resourceInfos = new Array<>();
        public Array<ProjectChain> chainsInfos = new Array<>();
        public Array<JointBaseInfo> jointsInfos = new Array<>();
    }

    public static class ProjectChain{
        public String name;
        public boolean loop;
        public boolean isSensor;

        // turetu but short, bet xml neskaito ju todel int naudojam.
        public int maskBits = -1;
        public int categoryBits = 0x0001;
        public int groupIndex;

        public Array<Float> x = new Array<>(), y = new Array<>();
    }

    public static class PhysicsFixtureShapes{
        /** kg/m^2. It's mass. */
        public float density = 0;
        /** [0, 1], sliding other body */
        public float friction = 0.2f;
        /** [0, 1]. bouncing */
        public float restitution = 0;
        public boolean isSensor = false;
        /** The collision category bits. Normally you would just set one bit. */
        public short categoryBits = 0x0001;

        /** The collision mask bits. This states the categories that this shape would accept for collision. */
        public short maskBits = -1;

        /** Collision groups allow a certain group of objects to never collide (negative) or always collide (positive). Zero means no
         * collision group. Non-zero group filtering always wins against the mask bits. */
        public short groupIndex = 0;

        public Array<Float> x = new Array<>(), y = new Array<>();
        /**
         0 - polygon
         1 - circle
         2 - chain
         3 - edge
         */
        public int type;
        public float radius;
    }

    /** info about one resource physics. */
    public static class PhysicsShapesGroup{
        public int id;
        public BodyDef.BodyType bodyType = BodyDef.BodyType.DynamicBody;
        public boolean isOriginMiddle = true;
        public final Vector2 bodyOrigin = new Vector2();
        public boolean fixedRotation = false;
        public boolean bullet = false;
        public float linearDamping = 0;
        public float angularDamping = 0;
        public float gravityScale = 1f;
        public float angularVelocity = 0;
        public final Vector2 linearVelocity = new Vector2();

        public final Array<PhysicsFixtureShapes> shapes = new Array<>();
    }

    public static class SavedFileDecoderInfo{
        public float version; // nustatyt kuri save versija.

        public String projectName;
        public String projectResourceFile;
//        public boolean export;
        public Array<ProjectForm> projectForms = new Array<>();

        public Array<PhysicsShapesGroup> shapesGroup = new Array<>();

    }

    /* joint infos
    * Visa info galima rast terp libgdx joint. */

    /** Joint info base. */
    public static class JointBaseInfo{
        public String bodyA, bodyB;
        public boolean bodyAResource, bodyBResource;
        public boolean collideConnected;
        public int type;
        public String id;
    }

    public static class DistanceJointInfo extends JointBaseInfo{
        public final Vector2 anchorA = new Vector2(), anchorB = new Vector2();
        public float length = 1;
        public float frequencyHz;
        public float dampingRatio;
    }

    public static class FrictionJointInfo extends JointBaseInfo{
        public final Vector2 anchorA = new Vector2(), anchorB = new Vector2();
        public float maxForce, maxTorque;
    }

    public static class GearJointInfo extends JointBaseInfo{
        public String joint1Id, joint2Id;
        public float ratio = 1;
    }

    public static class MotorJointInfo extends JointBaseInfo{
        public final Vector2 linearOffset = new Vector2();
        public float angularOffset;
        public float maxForce = 1, maxTorque = 1;
        public float correctionFactor = 0.3f;
    }

    public static class MouseJointInfo extends JointBaseInfo{
        public final Vector2 target = new Vector2();
        public float maxForce;
        public float frequencyHz = 5, dampingRatio = 0.7f;
    }

    public static class PrismaticJointInfo extends JointBaseInfo{
        public final Vector2 anchorA = new Vector2(), anchorB = new Vector2();
        public final Vector2 localAxisA = new Vector2(1, 0);
        public float referenceAngle = 0;
        public boolean enableLimit, enableMotor;
        public float lowerTranslation, upperTranslation;
        public float maxMotorForce, maxMotorSpeed;
    }

    public static class PulleyJointInfo extends JointBaseInfo{
        public final Vector2 groundAnchorA = new Vector2(-1, 1), groundAnchorB = new Vector2(1,1);
        public final Vector2 anchorA = new Vector2(-1,0), anchorB = new Vector2(1,0);
        public float lengthA, lengthB;
        public float ratio = 1;
    }

    public static class RevoluteJointInfo extends JointBaseInfo{
        public final Vector2 anchorA = new Vector2(), anchorB = new Vector2();
        public float referenceAngle;
        public boolean enableLimit, enableMotor;
        public float lowerAngle, upperAngle;
        public float motorSpeed, maxMotorTorque;
    }

    public static class RopeJointInfo extends JointBaseInfo{
        public final Vector2 anchorA = new Vector2(), anchorB = new Vector2();
        public float maxLength;
    }

    public static class WeldJointInfo extends JointBaseInfo{
        public final Vector2 anchorA = new Vector2(), anchorB = new Vector2();
        public float referenceAngle;
        public float frequencyHz, dampingRatio;
    }

    public static class WheelJointInfo extends JointBaseInfo{
        public final Vector2 anchorA = new Vector2(), anchorB = new Vector2();
        public final Vector2 localAxisA = new Vector2(1, 0);
        public boolean enableMotor;
        public float maxMotorTorque, motorSpeed;
        public float frequencyHz, dampingRatio;
    }
}
