package com.engine.animations;

import com.badlogic.gdx.graphics.Color;

public class ColorChanger extends Counter {
    private int oa, or, og, ob; // pradinis.
    private int oad, ord, ogd, obd; // skirtumai.
    private int ga, gr,gg, gb; // galutinis.

    private Color color; // naujos spalvos talpinimas.

    private ColorChangerListener listener;

    public ColorChanger(){
        color = new Color();
    }

    /** Change color in time.
     * @param startColor - start color. ARGB format.
     * @param goalColor - color in which start color will be changed. ARGB format.
     * @param time - How long color change should occur. Time in seconds.*/
    public void startColorChange(int startColor, int goalColor, float time){
//        // start spalva
//        oa = (startColor & 0xff000000) >>> 24;
//        or = (startColor & 0x00ff0000) >>> 16;
//        og = (startColor & 0x0000ff00) >>> 8;
//        ob = startColor & 0x000000ff;
//
//        // tikslo spalva
//        ga = (goalColor & 0xff000000) >>> 24;
//        gr = (goalColor & 0x00ff0000) >>> 16;
//        gg = (goalColor & 0x0000ff00) >>> 8;
//        gb = goalColor & 0x000000ff;
//
//        // ju skirtumai.
//        oad = ga - oa;
//        ord = gr - or;
//        ogd = gg - og;
//        obd = gb - ob;
        setColorValues(startColor, goalColor, time);

        // paleidziam counteri paprasta.
//        startCount(0, 1, time);
        startCount();
    }

    /** Set colors but doesn't start changing them. Call method {@link #startCount()} to start color changing. */
    public void setColorValues(int startColor, int goalColor, float time){
        // start spalva
        oa = (startColor & 0xff000000) >>> 24;
        or = (startColor & 0x00ff0000) >>> 16;
        og = (startColor & 0x0000ff00) >>> 8;
        ob = startColor & 0x000000ff;

        // tikslo spalva
        ga = (goalColor & 0xff000000) >>> 24;
        gr = (goalColor & 0x00ff0000) >>> 16;
        gg = (goalColor & 0x0000ff00) >>> 8;
        gb = goalColor & 0x000000ff;

        // ju skirtumai.
        oad = ga - oa;
        ord = gr - or;
        ogd = gg - og;
        obd = gb - ob;

        setValues(0, 1, time);
    }

    /* listener idejimas. */

    public void setColorChangerListener(ColorChangerListener e ){
        listener = e;
    }

    public ColorChangerListener getColorChangerListener(){
        return listener;
    }

    /* veikimas. */

    @Override
    protected void step(float old, float current) {
        if (current == getGoalValue()) { // paskutiniam stepe, tiesiog nustatom koks yra.
            color.set(gr/255f, gg/255f, gb/255f, ga/255f);
        }else { // nustatom pagal esama situacija.
            /*
            * Prie senosios spalvos pridedam skirtumus, kuriuos dauginsim is counter skaiciaus. Taip
            * spalva taip smoothly persijunks i kita spalva.
             */
            float c = getCurrentValue();
            float a = oad * c, r = ord * c, g = ogd * c, b = obd * c;
            color.set((or + r)/255f, (og + g) / 255f, (ob + b) / 255f,
                    (oa + a) / 255f);
        }
        if (listener != null){
            listener.onColorChange(color);
        }

        super.step(old, current); //paprasto counter listener kvietimas cia.
    }

    /** current color. */
    public Color getColor(){
        return color;
    }

    /** current color. ARGB format. */
    public int getColorInt(){
        return Color.argb8888(color);
    }

    @Override
    public void reset() {
        super.reset();
        listener = null;
    }

    /* listener */

    public interface ColorChangerListener{
        /** Called every time when color is changed. */
        void onColorChange(Color color);
    }
}
