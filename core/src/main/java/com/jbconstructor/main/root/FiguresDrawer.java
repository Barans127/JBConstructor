package com.jbconstructor.main.root;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.Engine;
import com.engine.interfaces.listeners.MainDraw;

/** Class to draw line or ellipse figures. Add this to {@link com.engine.interfaces.controls.TopPainter#addPaintOnTop(MainDraw)} to draw those figures or
 * call {@link #draw()} method yourself.
 * All drawings are made by ShapeRenderer.*/
public class FiguresDrawer implements MainDraw, Pool.Poolable {
    private boolean loop; // ar figura susijungia su pradzia
    private boolean isEllipse; // ar figura bus rutulys

    private boolean drawPoints; // ar piesti taskus, kurie sujungia linijas.

    private Array<Float> x, y; // taskai..

    /* linijos spalva, storis, tasko spalva, storis. */
    /** Line and point thickness. Default - 3 */
    public float lineWeight = 3f, pointWeight = 3f;
    /** line and point color. Default - line: green, point: black.
     * ARGB format. */
    public int lineColor = 0xff00ff00, pointColor = 0xff000000;

    /** Custom points drawing. */
    public Drawable customPoint;

    public FiguresDrawer(){
        x = new Array<>();
        y = new Array<>();
    }

    /** Change point. */
    public void editPoint(float x, float y, int index){
        if (index >= 0 && index < this.y.size){
            this.x.set(index, x);
            this.y.set(index, y);
        }
    }

    /* getters setters */

    /** Adds point to list. Will not add point if figure is an ellipse with already defined point.*/
    public void addPoint(float x, float y){
        if (isEllipse){ // negalim pridet daugiau tasku jei tai ellipse.
            if (this.y.size > 0){
                Gdx.app.log("FiguresDrawer", "Point was not added as figure type is ellipse.");
                return;
            }
        }
        this.x.add(x);
        this.y.add(y);
    }

    /** removes point from list. */
    public void removePoint(int index){
        if (isEllipse){ // jeigu ellipsem tai tik vienas taskas yra.
            if (y.size > 0 && index == 0){
                x.clear();
                y.clear();
            }
        }else { // cia visom kitom figurom.
            if (index >= 0 && index < x.size) {
                this.x.removeIndex(index);
                this.y.removeIndex(index);
            }
        }
    }

    /** point at specified index... Index is not checked! */
    public float getPointX(int index){
        return x.get(index);
    }

    /** point at specified index... Index is not checked! */
    public float getPointY(int index){
        return y.get(index);
    }

    /** Size of points. */
    public int getPointsSize(){
        return Math.min(x.size, y.size);
    }

    /** Set this figure to ellipse. NOTE: Ellipse uses only first point - all other points will be removed.
     * This method will remove all other points except first point.
     * @param ellipseRadius if ellipse - false then this will be ignored. */
    public void setFigureEllipse(boolean ellipse, float ellipseRadius){
        isEllipse = ellipse;
        if (x.size > 0){
            // sitaip pasalinam nereikalingus, pvz radius.
            float x = this.x.get(0), y = this.y.get(0);
            this.x.clear();
            this.y.clear();
            this.x.add(x);
            this.y.add(y);
            if (ellipse){
                this.x.add(ellipseRadius);
            }
        }
    }

    /** set this to loop. Loop makes figure's last point connect to first point. If figure ellipse then this
     * parameter is ignored. */
    public void setLoop(boolean loop){
        this.loop = loop;
    }

    public void setPointsDrawing(boolean pointsDrawing){
        this.drawPoints = pointsDrawing;
    }

    /** is this figure ellipse. NOTE: if figure ellipse then only first point will be used. */
    public boolean isEllipse(){
        return isEllipse;
    }

    /** is this figure connect last point to first. If figure is ellipse then this is ignored. */
    public boolean isLoop(){
        return loop;
    }

    /** is points drawing */
    public boolean isPointsDrawing(){
        return drawPoints;
    }

    /* ovveride metodai. */

    @Override
    public void draw() {
        if (x.size > 0) { // turi but bent vienas taskas, kad kazka darytu.
            Engine p = Engine.getInstance();
            p.noFill();
            p.stroke(lineColor);
            p.strokeWeight(lineWeight);
            if (isEllipse) {
                float x = this.x.get(0), y = this.y.get(0);
                if (this.x.size > 1) { // ellipsej tai turi but jau du taskai.
                    float radius = this.x.get(1); // radius bus antras x saraso eilej.
                    p.ellipse(x, y, radius, radius);

                }
                if (drawPoints) {
                    if (customPoint == null) {
                        p.fill(pointColor);
                        p.noStroke();
                        p.ellipse(x, y, pointWeight, pointWeight);
                    }else {
                        // kadangi ellipse automatiskai per viduri piesia, tai sita reik nustatyt, kad irgi per viduri piestu
                        customPoint.draw(p.getBatch(), x - pointWeight/2, y - pointWeight/2, pointWeight, pointWeight);
                    }
                }
            } else {
                if (x.size > 1) { // piesiam linijas, tik jei yra tokios. Su vienu tasku nelabai iseitu.
                    for (int a = 0; a < x.size; a++) {
                        float x = this.x.get(a), y = this.y.get(a);
                        float nx, ny;
                        if (this.x.size > a + 1) { // ziurim ar toliau taskas yra.
                            // yra taskas tolimesnis
                            nx = this.x.get(a + 1);
                            ny = this.y.get(a + 1);
                        } else if (loop) { // toliau tasko nera, tai reik pazet ar figura jungias su pradzia.
                            // jungiasi su pradzia. Imam pirmaji elementa.
                            nx = this.x.get(0);
                            ny = this.y.get(0);
                        } else { // kadangi kito elemento nera ir figura nera loopine, tai tiesiog baigiam sita.
                            continue;
                        }

                        // piesiam linija.
                        p.line(x, y, nx, ny);
                    }
                }
                // piesiam taskus
                if (drawPoints) {
                    p.noStroke();
                    p.fill(pointColor);

                    for (int a = 0; a < x.size; a++) {
                        if (customPoint == null) {
                            p.ellipse(x.get(a), y.get(a), pointWeight, pointWeight);
                        }else {
                            // kadangi ellipse automatiskai per viduri piesia, tai sita reik nustatyt, kad irgi per viduri piestu
                            customPoint.draw(p.getBatch(), x.get(a) - pointWeight/2, y.get(a) - pointWeight/2, pointWeight, pointWeight);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean drop(int reason) {
        return false;
    }

    @Override
    public void reset() {
        loop = false;
        isEllipse = false;
        drawPoints = false;
        x.clear();
        y.clear();
        lineWeight = 3f;
        pointWeight = 3f;
        lineColor = 0xff00ff00;
        pointColor = 0xff000000;
        customPoint = null;
    }
}
