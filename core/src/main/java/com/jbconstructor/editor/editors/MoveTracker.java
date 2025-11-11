package com.jbconstructor.editor.editors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.ui.controls.Field;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.ControlHost;
import com.engine.ui.controls.Window;
import com.engine.ui.controls.widgets.SImage;
import com.engine.root.GdxWrapper;
import com.jbconstructor.editor.forms.editorForm.ChainEdgePanel;
import com.jbconstructor.editor.forms.editorForm.EditForm;
import com.jbconstructor.editor.forms.editorForm.EditMainPanel;
import com.jbconstructor.editor.managers.Project;
import com.jbconstructor.editor.root.Element;

/** This is undo or redo. Saves single move. */
public class MoveTracker implements Pool.Poolable {
    private Array<Control> clients;
    private Array<Float> additionalMemory;
//    private float v1, v2, v3, v4; // informaciniai
//    private int v5, v6;
    private String info; // informaciniai
    private ControlHost controller;
    private ChainEdging.Chains chain; // tik vienas, nes bus imanoma tik viena keist vienu metu.
//    private boolean haveNeighbour, previousNeighbour; // kad surist keleta i kruva.

    // o ce ti fujne bus.
    // 0 - cord keitimas
    // 1 - pridetas interface i forma.
    // 2 - rotation keitimas
    // 3 - positioning.
    // 4 - tint keitimas.
    // 5 - id keitimas
    // 6 - cord ir dydis kartu... // nedarysim isimciu.
    // 7 - cord ir rotation kartu // kai rotatina daug kontroliu, ju cord keiciasi.
    // 8 - interfaceControlleri id keitimas.
    // 9 - interface isemimas is formos.
    // 10 - chain tasko pridejimas
    // 11 - chain tasko isemimas. isemus paskutini taska tiesiog visa chain salint.
    // 12 - chain taskas pajudejo.
    // 13 - visos chain istrinimas.
    // 14 - visos chain pridejimas (tikriausiai tik del redo bus naudojama.)
    // 15 - visos chain judinimas.
    // 16 - horizontal flip.
    // 17 - vertical flip.
    // 18 - resource keitimas.
    private int trackId;

    MoveTracker(){
        clients = new Array<>();
        additionalMemory = new Array<>();
    }

    private void checkChainPanel(){
        // ziurim kokia panel atidaryta.
        Window window = Engine.getInstance().getActiveForm();
        if (window instanceof EditForm){
            EditForm editForm = (EditForm) window;

            // pagal info ziurim ar chain atidaryta. Jeigu daryisim chain edging pakeitimus, tai atidarom panele, kad butu matoma.
            EditMainPanel mainPanel = editForm.getMainPanel();
            EditMainPanel.ControlPanel controlPanel = mainPanel.getActivePanel();
            if (!(controlPanel instanceof ChainEdgePanel)){
                mainPanel.openControlPanel(1, true);
            }
        }
    }

    /* chain edge is using this, to check if no more chains left and it can be disposed. */
    ChainEdging.Chains getChain() {
        return chain;
    }

    void setController(ControlHost e){
        controller = e;
    }

    public void setMove(int moveId, String info){
        trackId = moveId;
        this.info = info;
    }

//    public void setMove(int moveId, float v1, float v2, float v3, float v4,
//                        int v5, int v6, String info){ // priklausys sitie nuo moveId.
//        trackId = moveId;
//        this.v1 = v1;
//        this.v2 = v2;
//        this.v3 = v3;
//        this.v4 = v4;
//        this.v5 = v5;
//        this.v6 = v6;
//        this.info = info;
//    }

    public void setMove(int moveId, float... sett){
        trackId = moveId;
        additionalMemory.clear();
        for (float e : sett){
            additionalMemory.add(e);
        }
    }

    public void setChain(ChainEdging.Chains e){
        this.chain = e;
    }

    public void setControls(Array<Control> e){
        clients.clear();
        clients.addAll(e);
    }

    public void act(){ // padarys undo, o pats i redo pavirs. ir atvirksciai.
        switch (trackId){ // undo veiksmai skirti ne interface.
            case 10: // chain taska buvo pridetas. pasalinam
            {
                int id = additionalMemory.get(0).intValue(); // Taskas kuris buvo pridetas.
                if (id >= 0 && id < chain.x.size) { // kad nebutu klaidu. KLaidos atveju nieko???
                    float oldx = chain.x.get(id), oldy = chain.y.get(id); // taskai kuriuos salinam
                    chain.x.removeIndex(id);
                    chain.y.removeIndex(id);
                    if (chain.x.size == 0){ // nebera tasku. metam is listo
                        chain.owner.removeChainQuietly(additionalMemory.get(3).intValue());
                    }
                    chain.owner.callListOnRemovePoint(chain);
                    // virstam i redo.
                    trackId = 11;
                    additionalMemory.set(1, oldx);
                    additionalMemory.set(2, oldy);
//                    if (!TopPainter.containsMainDraw(chain.owner)){ // jeigu chainDraw panele isjunkta, nebus matoma undo padariniai, todel ijungiam matomuma.
//                        TopPainter.addPaintOnTop(chain.owner, false);
//                    }

                    checkChainPanel();
                }
            }
                return;
            case 11: // chain taskas buvo isimtas, grazinam.
            {
                int id = additionalMemory.get(0).intValue(); // Taskas kuris buvo pridetas.
                chain.x.insert(id, additionalMemory.get(1));
                chain.y.insert(id, additionalMemory.get(2));
                if (chain.x.size == 1){ // taskas is nulio buvo vel grazintas. reiskia chain nera sarase. grazinam atgal.
                    chain.owner.addChain(chain, additionalMemory.get(3).intValue());
                }
                chain.owner.callListOnAddPoint(chain);
                trackId = 10;// virtimas i redo. nieko keist nereik.
//                if (!TopPainter.containsMainDraw(chain.owner)){ // jeigu chainDraw panele isjunkta, nebus matoma undo padariniai, todel ijungiam matomuma.
//                    TopPainter.addPaintOnTop(chain.owner, false);
//                }

                checkChainPanel();
                return;
            }
            case 12: // chain taskas pakeite vieta. grazinam i pirmine.
            {
                int id = additionalMemory.get(0).intValue(); // Taskas kuris buvo pridetas.
                if (id >= 0 && id < chain.x.size) { // kad nebutu klaidu. KLaidos atveju nieko???
                    float oldx = chain.x.get(id), oldy = chain.y.get(id);
                    chain.x.set(id, additionalMemory.get(1));
                    chain.y.set(id, additionalMemory.get(2));
                    chain.owner.callListOnMovedPoint(chain, additionalMemory.get(1), additionalMemory.get(2));
                    // virtimas i redo
                    additionalMemory.set(1, oldx);
                    additionalMemory.set(2, oldy);
//                    if (!TopPainter.containsMainDraw(chain.owner)){ // jeigu chainDraw panele isjunkta, nebus matoma undo padariniai, todel ijungiam matomuma.
//                        TopPainter.addPaintOnTop(chain.owner, false);
//                    }

                    checkChainPanel();
                }
            }
                return;
            case 13: // visa chain buvo istrinta. grazinam atgal.
            {
                int id = additionalMemory.get(0).intValue(); // chain id.
                ChainEdging owner = chain.owner;
                owner.addChain(chain, id);
                trackId = 14; // virsmas i redo.
//                if (!TopPainter.containsMainDraw(chain.owner)){ // jeigu chainDraw panele isjunkta, nebus matoma undo padariniai, todel ijungiam matomuma.
//                    TopPainter.addPaintOnTop(chain.owner, false);
//                }
                checkChainPanel();
            }
                return;
            case 14: { // visa chain buvo grazinta po istrinimo. istrinam vel.
                int id = additionalMemory.get(0).intValue(); // chain id.
                ChainEdging owner = chain.owner;
                owner.removeChainQuietly(id);
                trackId = 13; // redo....
//                if (!TopPainter.containsMainDraw(chain.owner)) { // jeigu chainDraw panele isjunkta, nebus matoma undo padariniai, todel ijungiam matomuma.
//                    TopPainter.addPaintOnTop(chain.owner, false);
//                }

                checkChainPanel();
                return;
            }
            case 15: // visos chain judinimas.
                // surandam atskaitos taska.
                float sx, sy;
                sx = additionalMemory.get(0);
                sy = additionalMemory.get(1);

                // redui pradiniai taskai.
                float ax = chain.x.get(0);
                float ay = chain.y.get(0);

                // reik visus taskus perkeist.
                for (int a = 2, b = 0; a < additionalMemory.size; a+=2, b++){
//                    float x = chain.x.get(b);
//                    float y = chain.y.get(b);

                    // perkeliam pagal atskaitos taska.
                    float nx = sx + additionalMemory.get(a);
                    float ny = sy + additionalMemory.get(a+1);

                    // atnaujinam.
                    chain.x.set(b, nx);
                    chain.y.set(b, ny);
                }

                // perstatymui atgal i senaja pozicija.
                additionalMemory.set(0, ax);
                additionalMemory.set(1, ay);

                checkChainPanel();
                return;
        }
        // veikimas tik su Interface'ais.
        int count = 0;
        for (Control e : clients) {
            Vector2 pos = e.getPosition();
            switch (trackId) {
                case 1: // buvo prideta i forma, isemimas.
                    ControlHost v = e.getController();
                    int id = 0;
                    for (Control a : v.getControls()){
                        if (a == e){
                            break;
                        }
                        id++;
                    }
//                    v5 = id; // buvusi vieta controleri. pravers kai redo bandys grazint i forma.
//                    additionalMemory.clear();
//                    additionalMemory.add((float) id);
                    additionalMemory.set(count, (float) id);
                    v.removeControl(e);
                    controller = v;
//                    trackId = 9; // redo tures grazint i forma.
                    break;
                case 2: // rotate, cord
                case 7:
                    e.setAngle(e.getAngle()-additionalMemory.get(0));
                    if (trackId == 7){ // jetus kletus.... turi perstatyt. stolen from resizer onRotate.
                        float midX, midY;
                        midX = additionalMemory.get(1);
                        midY = additionalMemory.get(2);
                        Engine p = GdxWrapper.getInstance();
                        // pasigaut vidurio cord, nes fix ir absolute gali skirtis.
                        int positioning = Window.fixedView;
                        int clientPositioning = e.getPositioning() == Window.relativeView ? e.getController().getPositioning() : e.getPositioning();
                        if (positioning != clientPositioning){ // pozicijos susigaudymas, kad viektu ir su absolute, ir su fixed.
//                            Vector3 camera = p.getAbsoluteCamera().position;
//                            float x = camera.x - p.getScreenWidth()/2, y = camera.y - p.getScreenHeight()/2;
////                            if (positioning != Window.absoluteView) { // fixed, controles absolute.
//                            x = -x;
//                            y = -y;
////                            }
//                            midX -= x;
//                            midY -= y;
                            Vector3 cord = p.fixedToScreendCoords(midX, midY); // resizer coord versim i absolute cord
                            cord = p.screenToWorldCoords(cord.x, Gdx.graphics.getHeight() - cord.y);
                            midX = cord.x;
                            midY = cord.y;
                        }
                        Vector2 emid;
                        if (e instanceof Field) emid = ((Field) e).getMiddlePoint(); // biski matematikos, perskaiciuosim kordinates is naujo.
                        else emid = e.getPosition();
//                        float eRadius = e.getRadius();
                        float dist = MoreUtils.dist(midX, midY, emid.x, emid.y);
                        float angleDif = (MathUtils.degreesToRadians * (-additionalMemory.get(0)));
                        float angle = (float) Math.atan2(emid.y - midY, emid.x - midX) + angleDif;
                        float rx, ry; // vidurio cord.
                        rx = (float) (Math.cos(angle) * dist + midX);
                        ry = (float) (Math.sin(angle) * dist + midY);
//                        e.setRadius(eRadius + (v1));
                        if (e instanceof Field){
                            ((Field) e).placeInMiddle(rx, ry);
                        }else {
                            e.setPosition(rx, ry);
                        }
                    }
//                    v1 *= -1; // virsmas i redo.
                    break;
                case 3: // positioning.
                    if (count < additionalMemory.size){
                        int oldPos = e.getPositioning();
                        int nPos = additionalMemory.get(count).intValue();
                        Window.Position cPos;
                        switch (nPos){
                            case 0:
                                cPos = Window.Position.absolute;
                                break;
                            case 1:
                                cPos = Window.Position.fixed;
                                break;
                            default:
                                cPos = Window.Position.relative;

                        }
                        e.setPositioning(cPos);
                        additionalMemory.set(count, (float) oldPos);
                    }
                    break;
                case 4: { // tint
                    if (count < additionalMemory.size) {
                        if (e instanceof SImage) {
                            int old = ((SImage) e).getImageTint();
                            ((SImage) e).tintImage(additionalMemory.get(count).intValue());
                            additionalMemory.set(count, (float) old);
                        }
                    }
                }
                    break;
                case 5: { // name keitimas.
                    String old = e.getIdName();
                    if (e.setIdName(info)) {
                        info = old;
                    }
                    break;
                }
                case 0: // cord, size
                case 6:
                    float x, y, oldx, oldy;
                    int c = count * 4; // is 4, nes 4 kintamieji.
                    x = additionalMemory.get(c);
                    y = additionalMemory.get(c+1);
                    oldx = pos.x;
                    oldy = pos.y;
                    e.setPosition(x, y); // atstatymas i senas cord.
                    additionalMemory.set(c, oldx); // virtimas i redo.
                    additionalMemory.set(c+1, oldy);
                    if (trackId == 6) {
                        float width, height, oldw, oldh;
                        if (e instanceof Field) {
                            width = additionalMemory.get(c+2);
                            height = additionalMemory.get(c+3);
                            oldw = ((Field) e).getWidth();
                            oldh = ((Field) e).getHeight();
                            ((Field) e).setSize(width, height); // atstatymas i sena dydi.
                            ((Field) e).setOriginMiddle();
                            additionalMemory.set(c+2, oldw); // redo
                            additionalMemory.set(c+3, oldh);
                        }
                    }
                    break;
                case 8: // kontroliu perdeliojimas.
                    int oldId = 0;
                    int nId = additionalMemory.get(count).intValue();
                    ControlHost controller = e.getController();
                    for (Control a : controller.getControls()){
                        if (a == e){
                            additionalMemory.set(count, (float) oldId);
                            break;
                        }
                        oldId++;
                    }
                    controller.removeControl(e);
                    controller.addControl(e, nId);
                    break;
                case 9: // buvo pasalinta is formos pridejimas atgal.
                    this.controller.addControl(e, additionalMemory.get(count).intValue());
//                    trackId = 1;
                    break;
                case 16:// horizontal flip.
                    // tiesiog flipinam horizontaliai, nieko ypatingo.
                    if (e instanceof Element){
                        Element element = (Element) e;
                        element.setFlip(!element.isFlippedX(), element.isFlippedY());
                    }
                    break;
                case 17: // vertical flip.
                    // tiesiog flipinam verticaliai., nieko ypatingo.
                    if (e instanceof Element){
                        Element element = (Element) e;
                        element.setFlip(element.isFlippedX(), !element.isFlippedY());
                    }
                    break;
                case 18: // resource keitimas.
                    // pirma susirandam resource.
                    if (e instanceof Element) {
                        Element element = (Element) e;
                        for (int a = 0; a < Project.getLoadableResourcesCount(); a++) {
                            Project.ResourceToHold hold = Project.getLoadableResource(a);
                            if (hold != null) {
                                if (hold.getIdName().equals(info)) {
                                    // radom senaji.
                                    String old = element.getResName();

                                    element.setResName(hold.getIdName());
                                    element.setImage(hold.e);

                                    info = old; // virtimas i redo.

                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
            count++;
        }
        switch (trackId){ // virtimas i redo.
            case 1:
                trackId = 9;
                break;
            case 9:
                trackId = 1;
                controller = null; // nebereik.
                break;
//            case 0:
            case 2:
            case 7:
//            case 6:
//                v1 *= -1;
//                v2 *= -1;
//                v3 *= -1;
//                v4 *= -1;
                additionalMemory.set(0, additionalMemory.get(0) * -1);
//                additionalMemory.set(1, additionalMemory.get(1) * -1);
//                additionalMemory.set(2, additionalMemory.get(2) * -1);
//                additionalMemory.set(3, additionalMemory.get(3) * -1);
                break;
        }
    }

    @Override
    public void reset() {
        clients.clear();
        additionalMemory.clear();
        controller = null;
        info = null;
        if (chain != null){
            if (chain.owner != null){ // jau buvo released.
                chain.owner.releaseChain(chain);
            }
            chain = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        reset();
//        v1 = v2 = v3 = v4 = v5 = v6 = 0;
    }
}
