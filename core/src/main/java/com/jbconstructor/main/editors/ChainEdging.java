package com.jbconstructor.main.editors;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.Engine;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Inputs;
import com.engine.interfaces.controls.TopPainter;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.listeners.MainDraw;
import com.engine.root.GdxPongy;

public class ChainEdging implements MainDraw, Inputs{
    private final Engine p = GdxPongy.getInstance();
    private final MoveController undoredo;
    /* isvaizda */
    private SpriteDrawable boxes; // galima panaudot ir kaip linija lengvai.
//    private Drawable line;

    /* veikimas */
    private float x, y; // for touch down, pan ir t.t.
    private boolean isJustCreated, isPressed;
    private float startX, startY;
//    private boolean showRects; // tik kai editinama edges.
    private Pool<Chains> chains;
    private Array<Chains> activeChains;
    private int activeChain = -1, pressedRect = -1;
    private float boxSize; // kvadrato dydis.
    private float lineWeight;

    // visos chain judinimas.
    private boolean moveChainMode = false;
    private Array<Float> offsets; // naudosim apskaiciuot kiekvieno tasko offset.
    private float offsetX, offsetY; // del undo controller. Reik pasizymet pradzia...

    /* nustatymai */
    private int lineColor, rectColor, pressedRectColor;

    /* listener */
    private ChainsListener listener;

    public ChainEdging(MoveController e){
        undoredo = e;
        boxSize = Resources.getPropertyFloat("chainBoxSize", 10);
        lineWeight = Resources.getPropertyFloat("chainLineWeight", 5f);
        try { // nors vienas blogai parasytas, tai visu neskaitys.
            lineColor = Integer.parseUnsignedInt(Resources.getProperty("chainLineColor", "FF00FF00"), 16);
            rectColor = Integer.parseUnsignedInt(Resources.getProperty("chainBoxColor", "FF000000"), 16);
            pressedRectColor = Integer.parseUnsignedInt(Resources.getProperty("chainPressedBoxColor", "FFFF0000"), 16);
        }catch (NumberFormatException ex){
            lineColor = 0xFF00FF00;
            rectColor = 0xFF000000;
            pressedRectColor = 0xFFFF0000;
        }
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        boxes = (SpriteDrawable) Resources.getDrawable("halfWhiteColor");
        chains = new Pool<Chains>() {
            @Override
            protected Chains newObject() {
                return new Chains();
            }
        };
        activeChains = new Array<>();

        offsets = new Array<>(); // temporary move chain offset.
    }

    /* getMethods */

    public int getActiveChainIndex(){
        return activeChain;
    }

    /** returns active chains list. */
    public Array<Chains> getChains() {
        return activeChains;
    }

    public Chains getActiveChain(){
        if (activeChain < 0){
            return null;
        }else
            return activeChains.get(activeChain);
    }

    public float getBoxSize() {
        return boxSize;
    }

    public float getLineWidth() {
        return lineWeight;
    }

    public int getLineColor() {
        return lineColor;
    }

    public int getRectColor() {
        return rectColor;
    }

    public int getPressedRectColor() {
        return pressedRectColor;
    }

    public ChainsListener getListener(){
        return listener;
    }

    public int getActiveRectIndex(){
        return pressedRect;
    }

    /** Is all chain mode active? */
    public boolean isMoveChainModeActive(){
        return moveChainMode;
    }

    /* parametru keitimas */

    /** switches to whole chain mode. Will not switch to this mode if chain is not selected.
     * @return whether mode was turned on or not. */
    public boolean enableChainMoveMode(boolean enable){
        if (moveChainMode != enable){
            if (enable){
                // ijungiam moda.
                if (activeChain > -1){
                    // chain parinktas.
                    moveChainMode = enable;
                    pressedRect = -1; // nuimam aktyvu taska.
                    isPressed = false;
                    return true;
                }
            }else {
                // isjungt paprasta.
                moveChainMode = enable;
                isPressed = false;
                return true;
            }
        }
        return false;
    }

    public void setChainsListener(ChainsListener e){
        listener = e;
    }

    public void setColors(int lineColor, int rectColor, int pressedRectColor){
        this.lineColor = lineColor;
        this.rectColor = rectColor;
        this.pressedRectColor = pressedRectColor;
    }

    public void setLineWidth(float width){
        if (width > 0){
            lineWeight = width;
        }
    }

    public void setBoxSize(float size){
        if (size > 0){
            boxSize = size;
        }
    }

    public void setActiveChain(int activeChain) {
        if (activeChain >=0 && activeChain < activeChains.size) {
            this.activeChain = activeChain;
            pressedRect = activeChains.get(activeChain).x.size-1; // paskutinis elemtas.
        }else this.activeChain = -1;
    }

    public void removeChain(int index){
        if (index >= 0 && index < activeChains.size){
            Chains e = activeChains.removeIndex(index);
            undoredo.moved(13, e, index);
            activeChain = -1;
            pressedRect = -1;
            if (listener!=null){
                listener.onChainRemove(e);
            }
        }
    }

    public void addChain(Chains e, int index){ // undo controleriui. labiausiai...
        if (index >= 0 && index < activeChains.size) {
            activeChains.insert(index, e);
        }else {
            activeChains.add(e);
        }
        if (listener!=null){
            listener.onChainAdd(e);
        }
    }

    /** undo will not be informed about this. Undo controller use this. Use normal <code>removeChain</code> */
    public void removeChainQuietly(int index){
        if (index >= 0 && index < activeChains.size){
            Chains e = activeChains.removeIndex(index);
            activeChain = -1;
            pressedRect = -1;
            if (listener!=null){
                listener.onChainRemove(e);
            }
        }
    }

    /** point will be moved if chain is selected and rect is active */
    public void movePoint(float x, float y){
        if (activeChain > -1 && pressedRect > -1){ // taskas yra aktyvus ir pasirinktas.
            Chains e = activeChains.get(activeChain);
            float ox = e.x.get(pressedRect), oy = e.y.get(pressedRect);
            e.x.set(pressedRect, x);
            e.y.set(pressedRect, y);
            undoredo.moved(12, e, pressedRect, ox, oy);
            callListOnMovedPoint(e, x, y);
        }
    }

    /* pats veikimas */

    public void callListOnRemovePoint(Chains e){
        if (listener != null){
            pressedRect = -1;
            int count = e.x.size-1;
            listener.onLinesChange(e, Math.max(count, 0));
        }
    }

    public void callListOnAddPoint(Chains e){
        if (listener != null){
            listener.onLinesChange(e, e.x.size-1);
        }
    }

    public void callListOnMovedPoint(Chains e, float x, float y){
        if (listener != null){
            listener.moved(e, x, y);
        }
    }

    public void deleteActivePoint(){
        if (activeChain >= 0 && activeChain < activeChains.size)
            if (pressedRect >= 0 && pressedRect < activeChains.get(activeChain).x.size){
                float x,y;
                Chains e = activeChains.get(activeChain);
                x = e.x.get(pressedRect);
                y = e.y.get(pressedRect);
                e.x.removeIndex(pressedRect);
                e.y.removeIndex(pressedRect);
                undoredo.moved(11, e, pressedRect, x, y);
                pressedRect = -1;
                if (listener != null){
                    int count = e.x.size-1;
                    listener.onLinesChange(e, Math.max(count, 0));
                }
            }
    }

    public void releaseChain(Chains e){
        if (e != null) {
            if (!activeChains.contains(e, true)) {
                for (MoveTracker a : undoredo.getUndoTracks()){
                    if (a.getChain() == e){
                        return; // yra undo sarase, neliest.
                    }
                }
                for (MoveTracker a : undoredo.getRedoTracks()){
                    if (a.getChain() == e){
                        return; // yra redo sarase irgi neliest.
                    }
                }
                chains.free(e);
            }
        }
    }

    private void drawChain(Chains e, boolean withRects){
        for (int a = 0; a < e.x.size; a++){
            float x = e.x.get(a);
            float y = e.y.get(a);
            if (a + 1 < e.x.size){ // jeigu nera kito tasko, tai nebereik piest linijos.
                float x1 = e.x.get(a+1), y1 = e.y.get(a+1);
                float length = MoreUtils.dist(x, y, x1, y1);
                float angle = (float) Math.atan2(y1 - y, x1 - x); // kampas tarp tasku.
                p.tint(lineColor);
                // darom su lineWeight del tikslesnio liniju piesimo.
                boxes.draw(p.getBatch(), x, y - lineWeight/2, 0, lineWeight/2, length, lineWeight, 1, 1, angle* MathUtils.radiansToDegrees);
            }
            if (withRects){ // piesiam aktyvuji rect.
                int color;
                // kai visos chain move modas ijungtas, tai visi rect turi palikt aktyvios spalvos.
                if (moveChainMode || pressedRect == a) {
                    color = pressedRectColor;
                }else{
                    color = rectColor;
                }
                p.tint(color);
                boxes.draw(p.getBatch(),x - boxSize/2, y - boxSize/2, boxSize, boxSize);
//                p.noTint();
            }
            p.noTint();
        }
        if (e.loop && e.x.size > 2){ // turi but maziausiai 3 taskai, kad loop nupiestum.
            float x2 = e.x.get(0), y2 = e.y.get(0); // pirmasis elementas
            float x1 = e.x.get(e.x.size-1), y1 = e.y.get(e.y.size-1); // antras elementas
            float length = MoreUtils.dist(x1, y1, x2, y2);
            float angle = (float) Math.atan2(y2 - y1, x2-x1);
            p.tint(lineColor);
            boxes.draw(p.getBatch(), x1, y1, 0, 0, length, lineWeight, 1, 1, angle*MathUtils.radiansToDegrees);
            p.noTint();
        }
    }

    /* implemented metodai. */

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) { // cord nereik verst. Toppainter pavers.
        this.x = x;
        this.y = y;
        return false; // ignoruojam.
//        if (activeChain == -1){ // naujo chain kurimas
//            Chains e = chains.obtain();
//            e.owner = this;
//            activeChains.add(e); // pridedam i sarasa.
//            e.x.add(x); // iskart kuriam nauja taska.
//            e.y.add(y);
//            pressedRect = 0;
//            activeChain = activeChains.size-1; // nustatom naujai sukurta grandine kaip pagrindine.
//            isJustCreated = true;
//        }else {
//            Chains e = activeChains.get(activeChain);
//            for (int a = e.x.size-1; a >= 0; a--){ // nuo virsaus i apacia.
//                float mx = e.x.get(a), my = e.y.get(a); // ar buvo ant egzistuojancio paspausta.
//                if (Resources.dist(mx, my, x, y) <= boxSize){ // rutulio spindulys.
//                    pressedRect = a;
//                    startX = e.x.get(pressedRect);
//                    startY = e.y.get(pressedRect);
//                    isJustCreated = false;
//                    return true; // paspaude ant esamo rect, nekuriam naujo.
//                }
//            }
//            // nebuvo ant esamu rect paspausta. kuriam nauja.
//            e.x.add(x);
//            e.y.add(y);
//            pressedRect = e.x.size-1;
//            isJustCreated = true;
//        }
//        return true; // veiks per visa. per visa absolute.
    }

    public boolean touchDown(){ // apejimas. kad veiktu main panel.
        if (activeChain == -1){ // naujo chain kurimas
            Chains e = chains.obtain();
            e.owner = this;
            String name = null;
            int count = 1; // pradesem skaiciavima ne nuo 0, o nuo 1.
            CHECK:
            while (name == null) {
                String tryname = "chain" + count;
                for (Chains a : activeChains) {
                    if (a.name.equals(tryname)){
                        count++;
                        continue CHECK;
                    }
                }
                name = tryname;
                e.name = name;
            }
            activeChains.add(e); // pridedam i sarasa.
            e.x.add(x); // iskart kuriam nauja taska.
            e.y.add(y);
            pressedRect = 0;
            activeChain = activeChains.size-1; // nustatom naujai sukurta grandine kaip pagrindine.
            isJustCreated = true;
            if (listener !=null){
                listener.onChainAdd(e);
                listener.onLinesChange(e, 0); // nes tikrai 0
                listener.activeRectSelected(e, pressedRect, x, y);
            }
        }else { // chain yra parinktas.
            Chains e = activeChains.get(activeChain);
            if (moveChainMode){ // viso chain judinimas.
                offsets.clear(); // isvalom jei buvo.

                // ziurimm kiek isviso tasku yra.
                if (e.x.size == 0){
                    // kazkas negerai. Nera tasku.
                    // nieko nedarom.
                    return true; // vistiek input paimam.
                }

                offsetX = e.x.get(0);
                offsetY = e.y.get(0);

                // apskaiciuojam tasku offset.
                for (int a = 0; a < e.x.size; a++){
                    offsets.add(e.x.get(a) - x); // pirma x apskaiciuojam.
                    offsets.add(e.y.get(a) - y); // po to y.
                }

            }else { // paprastai paspaude. kuriam nauja taska arba esama pazymim.
                for (int a = e.x.size - 1; a >= 0; a--) { // nuo virsaus i apacia.
                    float mx = e.x.get(a), my = e.y.get(a); // ar buvo ant egzistuojancio paspausta.
                    if (MoreUtils.dist(mx, my, x, y) <= boxSize) { // rutulio spindulys.
                        pressedRect = a;
                        startX = e.x.get(pressedRect);
                        startY = e.y.get(pressedRect);
                        isJustCreated = false;
                        isPressed = true;
                        if (listener != null) {
                            listener.activeRectSelected(e, pressedRect, startX, startY);
                        }
                        return true; // paspaude ant esamo rect, nekuriam naujo.
                    }
                }
                // nebuvo ant esamu rect paspausta. kuriam nauja.
                if (pressedRect == -1 || pressedRect == e.x.size - 1) {
                    e.x.add(x);
                    e.y.add(y);
                    pressedRect = e.x.size - 1;
                } else {
                    e.x.insert(pressedRect + 1, x);
                    e.y.insert(pressedRect + 1, y);
                    pressedRect++;
                }
                isJustCreated = true;
                if (listener != null) {
                    listener.onLinesChange(e, e.x.size - 1);
                    listener.activeRectSelected(e, pressedRect, x, y);
                }
            }
        }
        isPressed = true;
        return true; // veiks per visa. per visa absolute.
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        this.x = x;
        this.y = y;
        return false;
//        if (pressedRect == -1 || activeChain == -1)// del viso pikto.
//            return false;
//        Chains e = activeChains.get(activeChain);
//        e.x.set(pressedRect, x); // tiesiog sekioja pelyte.
//        e.y.set(pressedRect, y);
//        return true; // per visa absolute.
    }

    public boolean pan(){
        if (!isPressed || activeChain == -1)// del viso pikto.
            return false; // chain turi but aktyvus.

        Chains e = activeChains.get(activeChain);
        if (moveChainMode){ // chain judinimas. Visos chain.
            changeChainPosition(e);
        }else if (pressedRect != -1) {
            e.x.set(pressedRect, x); // tiesiog sekioja pelyte.
            e.y.set(pressedRect, y);
            if (listener != null) {
                listener.moved(e, x, y);
            }
        }else { // pressed rect == -1
            return false;
        }
        return true; // per visa absolute.
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        this.x = x;
        this.y = y;
        return false;
//        Chains e = activeChains.get(activeChain);
//        if (isJustCreated){ // taskas tik buvo sukurtas.
//            undoredo.moved(10, e, pressedRect, e.x.get(pressedRect), e.y.get(pressedRect));
//        }else { // taskas tik pakeite savo vieta.
//            undoredo.moved(12, e, pressedRect, startX, startY);
//        }
//        return true; // per visa absolute.
    }

    public boolean panStop(){
        if (!isPressed)
            return false;
        Chains e = activeChains.get(activeChain);
        if (moveChainMode){
            if (e != null){
                // e gali but null ant sito modo.
                changeChainPosition(e);

                // darom undo judesi.
                Array<Float> flush = undoredo.getFlushSettings();
                flush.clear();
                // atskaitos taskas.
                float sx = e.x.get(0), sy = e.y.get(0);
//                float sx = offsetX, sy = offsetY;
                flush.add(offsetX);
                flush.add(offsetY);

                // sumetam viska.
                for (int a= 0; a < e.x.size; a++){
                    flush.add(e.x.get(a) - sx);
                    flush.add(e.y.get(a) - sy);
                }

                // fiksuojam undo.
                undoredo.moved(15, e);

                offsets.clear(); // nebereik...
            }
        } else if (isJustCreated){ // taskas tik buvo sukurtas.
            undoredo.moved(10, e, pressedRect, e.x.get(pressedRect), e.y.get(pressedRect), activeChain);
        }else { // taskas tik pakeite savo vieta.
            undoredo.moved(12, e, pressedRect, startX, startY);
        }
        isPressed = false;
        return true; // per visa absolute.
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        this.x = x;
        this.y = y;
        return false;
//        if (isJustCreated){ // naujas taskas buvo sukurtas, kitu atveju paspaude tik ant esamo tasko, nereik judesio irasyt.
//            Chains e = activeChains.get(activeChain);
//            undoredo.moved(10, e, pressedRect, e.x.get(pressedRect), e.y.get(pressedRect));
//            return true; // true tik tada kai sukure nauja taska.
//        }
//        return false; // kitu atveju nesvarbu.
    }

    public boolean tap(){
        if (!isPressed)
            return false;
        if (moveChainMode){ // chain nepajudejo. Nieko nedarom.
            offsets.clear();
        } else  if (isJustCreated){ // naujas taskas buvo sukurtas, kitu atveju paspaude tik ant esamo tasko, nereik judesio irasyt.
            Chains e = activeChains.get(activeChain);
            undoredo.moved(10, e, pressedRect, e.x.get(pressedRect), e.y.get(pressedRect), activeChain);
            isPressed = false;
            return true; // true tik tada kai sukure nauja taska.
        }
        isPressed = false;
        return false; // kitu atveju nesvarbu.
    }

    /* vidiniai */

    private void changeChainPosition(Chains e){
        for (int a = 0, b = 0; a < e.x.size; a++, b +=2 ){
            float ox = offsets.get(b); /// x
            float oy = offsets.get(b+1); // y

            // nustatom nauja pozicija pagal mouse cords.
            e.x.set(a, x + ox);
            e.y.set(a, y + oy);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean mouseMoved(float x, float y) {
        return false;
    }

    @Override
    public boolean keyTyped(char e) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

    @Override
    public void release() {
//        showRects = false;
        activeChain = -1;
        pressedRect = -1;
        isPressed = false;
    }

    @Override
    public int getPositioning() {
        return Window.absoluteView; // box2D world veiks tik ant absolute coord. nelabai eis maisyt.
    }

    @Override
    public void draw() {
        int count = 0;
        for (Chains e : activeChains){
            if (activeChain != count){
                drawChain(e, false);
            }
            count++;
        }
        if (activeChains.size == 0){
            return;
        }
        if (activeChain >= 0){
            drawChain(activeChains.get(activeChain), true);
        }
    }

    @Override
    public boolean drop(int reason) { // drop tik jei formos keitimas.
        if (reason != TopPainter.formChangeDrop){
            TopPainter.addInputsListener(this); // grazinam atgal, nes stpdv pamete.
            return true;
        }
        return false;
    }

    public interface ChainsListener{
        void moved(Chains moved, float x, float y);
        void onChainAdd(Chains added);
        void onChainRemove(Chains removed);
        void onLinesChange(Chains e, int lineCount);
        void activeRectSelected(Chains e, int index, float x, float y);
    }

    /** class to hold chain edge points. */
    public static class Chains implements Pool.Poolable{
        public String name;
        public ChainEdging owner;
        public Array<Float> x, y;
        public boolean loop;

        public boolean isSensor;
        public short maskBits = -1;
        public short categoryBits = 0x0001;
        public short groupIndex = 0;

//        public boolean isSensor;

        public Chains(){
            x = new Array<>();
            y = new Array<>();
        }

        @Override
        public void reset() {
            x.clear();
            y.clear();
            owner = null;
            name = null;
            loop = false;
        }
    }
}
