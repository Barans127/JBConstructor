package com.engine.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Pool;
import com.engine.animations.Counter;
import com.engine.interfaces.controls.Window;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Some useful or useless methods. */
public final class MoreUtils {
    private final static Pool<Counter> counters;

    static {
        counters = new Pool<Counter>() {
            @Override
            protected Counter newObject() {
                return new Counter();
            }
        };
    }

    private MoreUtils(){}

    /* counteriai. */

    /** @return Counter instance. Counter instances can be reused. */
    public static Counter getCounter() {
        return counters.obtain();
    }

    /** if you don't need counter anymore then place it here. */
    public static void freeCounter(Counter e) {
        if (e != null) {
            counters.free(e);
        }
    }

    /* positioning */

    /** if you need to get positioning enum but you have positioning index you can use this method to get that enum based on your int positioning.
     * @return if index is out of bounds then relative positioning is returned. */
    public static Window.Position getPositionFromIndex(int index){
        switch (index) {
            case Window.absoluteView:
                return Window.Position.absolute;
            case Window.fixedView:
                return Window.Position.fixed;
            default:
                return Window.Position.relative;
        }
    }

    /* file handle */

    /** check if file exists. */
    public static boolean existFile(String rname) {
        if (rname == null) {
            return false;
        }
        FileHandle file = Gdx.files.internal(rname);
        return file.exists();
    }

    /** path fix for android. Android does not accept -> "." or "..". We need to take care of that.
     * It is useful when root+path is combined. Example root: resources/image/
     * path: ../image.jpg. From these two we get: resources/image.jpg.
     * This method is used by {@link ImagesLoader} and {@link SoundLoader}*/
    public static String fixPath(String path){
        if (path.contains("./") || path.contains("..")){
            // imam zemyn dvigubus taskus...
            while (path.contains("..")){ // ziurim ar yra tokiu kur nori grizt per folderi atgal.
//				int a = test.indexOf("..");
                int left = 2; // 2, nes du bruksniukai turi but.
                String[] parts = path.split("\\.\\.", 2); // kertam i dvi dali per du taskus.
                if (!parts[0].startsWith("/"))
                    parts[0] = "/" + parts[0]; // bug fix kai ignorindavo pirmaji folderi, todel tiesiog dadedam sita zenkliuka, jie kas ji vel apacioj nukirs.
                int index = parts[0].length()-1; // imam paskutini simboli.
                while (left > 0){
                    char e = parts[0].charAt(index); // paziurim koks simbolis
                    if (e == '/'){ // jeigu sitas bruksniukas...
                        left--; // paziurim kelinta kart randam
                        if (left == 0){ // antra kart radom, kertam zemyn.
                            parts[0] = parts[0].substring(0, index); // nukertam iki to symnolio.
                        }
                    }
                    index--;
                    if (index < 0) // jei kartais kazkas kazko nerastu tai tiesiog stabdom viska.
                        break;
                }
                path = parts[0] + parts[1]; // suklijuojam atgal jau be dvigubu tasku.
            }
            // dabar beliko vietisus taskus imt zemyn...
            String[] pointParts = path.split("\\./");// kertam per tokius vietisus taskus.
            String fin = "";
            for (String sss : pointParts) { // sudedam atgal be tasku (juos pats split nukirto).
                fin += sss;
            }
            if (fin.startsWith("/")) // dar paziurim kad neprasidedu situo bruksniuku, kitaip andoird vel neras kur failas..
                fin = fin.substring(1); // jeigu prasideda, tai kertam ta bruksniuka zemyn.
//            System.out.println("susitvarkom su tasku: " + fin);
            return fin;
        }else
            return path;
    }

    /** Extracts file name from given path. It thinks that path is defined with "/" forward slashes.
     * If you have backward slashes as separators then you must replace it before using this method.*/
    public static String getFileName(String path){
        if (path == null || path.isEmpty()){
            return path; // nieko ner. grazinam ta pati.
        }

        String fullName;
        if (path.contains("/")){ // path ne vien vardas. nukertam kelia. paliekam tik varda.
            int index = path.lastIndexOf("/");
            if (index + 1 == path.length()){
                return ""; // toliau nieko neber.
            }
            fullName = path.substring(index+1).trim();
        }else {
            fullName = path.trim();
        }

        // paziurim ar turi extension
        int index = fullName.lastIndexOf(".");
        if (index == -1){
            return fullName; // neturi.
        }else { // turejo, bet nebetures.
            return fullName.substring(0, index);
        }
    }

    /* matematiniai dalykai. */

    /** @return distance between given points. */
    public static float dist(Vector2 point, Vector2 point2){
        return dist(point.x, point.y, point2.x, point2.y);
    }

    /** @return distance between given points. */
    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(sq(x2 - x1) + sq(y2 - y1));
    }

    /** @return squared value */
    public static float sq(float e) {
        return e * e;
    }

    /** @return positive value */
    public static int abs(int value) {
        return value < 0 ? value * -1 : value;
    }

    /** @return positive value */
    public static float abs(float value) {
        return value < 0 ? value * -1 : value;
    }

    /** @return given vector speed. */
    public static float speed(float x, float y) {
        return (float) Math.sqrt(sq(x) + sq(y));
    }

    /** @return given vector speed. */
    public static float speed(Vector2 e) {
        return speed(e.x, e.y);
    }

    /**
     * Rotates given point around given rotation point.
     * @param point Point to rotate.
     * @param rotateX rotation point x
     * @param rotateY rotation point y
     * @param originX offset of rotation x point
     * @param originY offset of rotation y point
     * @param degrees angle in degrees.
     * @return rotated point around rotation point.
     */
    public static Vector2 rotatePoint(Vector2 point, float rotateX, float rotateY,
                                      float originX, float originY, float degrees){
        float posX = rotateX + originX;
        float posY = rotateY + originY;
//			int mx = InputHandler.rmouseX;
//			int my = InputHandler.rmouseY;
        float len = dist(posX, posY, point.x, point.y);
        float rad = degrees * MathUtils.degreesToRadians;
        float angle = MathUtils.atan2(point.y - posY, point.x - posX) + rad;
        float rx, ry;
        rx = MathUtils.cos(angle) * len + posX;
        ry = MathUtils.sin(angle) * len + posY;
        return point.set(rx, ry);
    }

    /** @return value which is in given bounds. */
    public static float inBounds(float value, float min, float max){
        if (min > max){
            throw new IllegalArgumentException("min value cannot be higher than max value");
        }
        if (value < min)
            value = min;
        else if (value > max){
            value = max;
        }
        return value;
    }

    /** @return value which is in given bounds. */
    public static int inBounds(int value, int min, int max){
        if (min > max){
            throw new IllegalArgumentException("min value cannot be higher than max value");
        }
        if (value < min)
            value = min;
        else if (value > max){
            value = max;
        }
        return value;
    }

    /**
     * Checks if given line is crossing circle.
     * @param ax, ay - first line point.
     * @param bx, by - end line point.
     * @param cx, cy - circle point.
     * @param r - circle diameter.
     * @return true if line is crossing circle, false otherwise.*/
    public static boolean isLineCrossingCircle(float ax, float ay, float bx, float by, float cx, float cy, float r){
        ax -= cx;
        ay -= cy;
        bx -= cx;
        by -= cy;
        float c = sq(ax) + sq(ay) - sq(r);
        float b = 2 * (ax * (bx - ax) + ay * (by - ay));
        float a = sq(bx - ax) + sq(by - ay);
        float disc = sq(b) - 4 * a * c;
        if (disc <= 0)
            return false;
        float sqrtdisc = (float) Math.sqrt(disc);
        float t1 = (-b + sqrtdisc) / (2*a);
        float t2 = (-b - sqrtdisc) / (2*a);
        return (0 < t1 && t1 < 1) || (0 < t2 && t2 < 1);
    }

    /** rounds float.
     * @param value to round.
     * @param roundValue value of how many numbers should be after dot.
     * @return rounded value.*/
    public static float roundFloat(float value, int roundValue){
        return BigDecimal.valueOf(value).setScale(roundValue, BigDecimal.ROUND_FLOOR).floatValue();
//        BigDecimal a = new BigDecimal(value+"");
//        BigDecimal roundOff = a.setScale(roundValue, BigDecimal.ROUND_FLOOR);
//        return roundOff.floatValue();

//        float rounder = 1/roundValue; // float value error. buna daznai klaida...
//        int roundedValue = (int) (value * rounder); // nukerpam galune.
//        float lastValue = roundedValue * roundValue;
//        String sValue = String.valueOf(lastValue); // check if all correct.
//        if (sValue.indexOf(".") == 0)
//        return roundedValue * roundValue;
    }

    /* string hex i int. */

    /** convert hex string color to int color. ARGB or RGB format.
     * @return ARGB int color format.
     * @throws NumberFormatException if hex cannot be converted. */
    public static int hexToInt(String hex) throws NumberFormatException{
        boolean withAlpha;
        int len;
        if (hex.length() != 6 && hex.length() != 8){
            throw new NumberFormatException("Hex length must be 6 or 8.");
        }
        withAlpha = hex.length() != 6;
        float a;
        if (withAlpha) {
            a = Integer.valueOf(hex.substring(0, 2),16) / 255f;
            len = 8;
        }else {
            a = 1f;
            len = 6;
        }
        float r = Integer.valueOf(hex.substring(len - 6, len - 4),16) / 255f;
        float g = Integer.valueOf(hex.substring(len - 4, len - 2),16) / 255f;
        float b = Integer.valueOf(hex.substring(len - 2, len),16) / 255f;
        Color converter = new Color(r, g, b, a);
        return Color.argb8888(converter);
    }

    /* listu */

    /** Check if list has duplicates. */
    public boolean isDuplicates(final Object[] zipcodelist) {
        Set<Object> lump = new HashSet<>();
        for (Object i : zipcodelist) {
            if (lump.contains(i))
                return true;
            lump.add(i);
        }
        return false;
    }

    /* physics palengvinimui. */

    /** creates fixture definition from fixture itself (convenient when fixture needs to be destroyed and recreated later).
     * @return fixture definition. If you pass null as fixture then null is returned. */
    public static FixtureDef createFixtureDefFromFixture(Fixture fixture){
        if (fixture != null) {
            FixtureDef def = new FixtureDef();
            def.density = fixture.getDensity();
            def.friction = fixture.getFriction();
            def.isSensor = fixture.isSensor();
            def.restitution = fixture.getRestitution();
            def.shape = fixture.getShape();
            def.filter.categoryBits = fixture.getFilterData().categoryBits;
            def.filter.groupIndex = fixture.getFilterData().groupIndex;
            def.filter.maskBits = fixture.getFilterData().maskBits;
            return def;
        }else
            return null;
    }

    /* teksto manipuliacija. */

    /** Compress text using GZIP. If text is null or empty then null is returned. If you want to save compressed text to a file then
     *  use {@link com.badlogic.gdx.utils.Base64Coder} to encode compressed text. */
    public static byte[] compressText(String str){
        if (str == null || str.isEmpty()) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes("UTF8"));
            gzip.flush();
            gzip.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Decompress text using GZIP. If you are reading encoded text from a file then use {@link com.badlogic.gdx.utils.Base64Coder} to decode this text first.*/
    public static String decompress(final byte[] compressed) throws IOException {
        if ((compressed == null) || (compressed.length == 0)) {
            return "";
        }
        final StringBuilder outStr = new StringBuilder();
        // check if compressed with GZIP
        if (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8))) {
            final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF8"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outStr.append(line);
            }

            gis.close();
            bufferedReader.close();
        } else {
            outStr.append(Arrays.toString(compressed));
        }
        return outStr.toString();
    }
}
