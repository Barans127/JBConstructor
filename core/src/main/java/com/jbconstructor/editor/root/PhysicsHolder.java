package com.jbconstructor.editor.root;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.engine.ui.controls.toasts.AlertToast;
import com.engine.jbconstructor.SavedFileDecoder;
import com.jbconstructor.editor.dialogs.physicsEditor.PhysicsEditor;

import java.io.Serializable;
import java.util.ArrayList;

/** Class to store physics properties. */
public class PhysicsHolder implements Serializable {
    /** Info about shapes on this body. */
    public final ArrayList<PhysicsEditor.FixtureShapeHolder> shapes = new ArrayList<>();

    /** body type */
    public BodyDef.BodyType bodyType = BodyDef.BodyType.DynamicBody;

    /** if body origin is not set to middle then these will be used. */
    public final Vector2 bodyOrigin = new Vector2();

    /** is body origin set to middle. If true then bodyOrigin points will be ignored and elements middle position will be taken. */
    public boolean isBodyOriginMiddle = true;

    /** Should this body be prevented from rotating? Useful for characters. **/
    public boolean fixedRotation = false;

    /** Is this a fast moving body that should be prevented from tunneling through other moving bodies? Note that all bodies are
     * prevented from tunneling through kinematic and static bodies. This setting is only considered on dynamic bodies.
     * NOTE: You should use this flag sparingly since it increases processing time. **/
    public boolean bulletBody = false;

    /** Linear damping is use to reduce the linear velocity. The damping parameter can be larger than 1.0f but the damping effect
     * becomes sensitive to the time step when the damping parameter is large. **/
    public float linearDamping = 0;

    /** Angular damping is use to reduce the angular velocity. The damping parameter can be larger than 1.0f but the damping effect
     * becomes sensitive to the time step when the damping parameter is large. **/
    public float angularDamping = 0;

    /** Scale the gravity applied to this body. **/
    public float gravityScale = 1f;

    /** The angular velocity of the body. **/
    public float angularVelocity = 0;

    /** The linear velocity of the body's origin in world co-ordinates. **/
    public final Vector2 linearVelocity = new Vector2(0,0);

    /* veikimo metodai */

    public void copyHolder(PhysicsHolder e){
        if (e != null && e != this){
            // null tai aisku ka rags.
            // nereik saves kopint irgi.
            shapes.clear(); // pirma isvalom senas shape.

            // toliau viska nukopinam.
//            shapes.addAll(e.shapes);
            for (PhysicsEditor.FixtureShapeHolder holder : e.shapes){
                shapes.add(new PhysicsEditor.FixtureShapeHolder().copy(holder));
            }
            bodyType = e.bodyType;
            bodyOrigin.set(e.bodyOrigin);
            isBodyOriginMiddle = e.isBodyOriginMiddle;
            fixedRotation = e.fixedRotation;
            bulletBody = e.bulletBody;
            linearDamping = e.linearDamping;
            angularDamping = e.angularDamping;
            gravityScale = e.gravityScale;
            angularVelocity = e.angularVelocity;
            linearVelocity.set(e.linearVelocity);
        }else {
            // kad zinot, gal pravers.
            Gdx.app.log("PhysicsHolder", "Shape was not copied.");
        }
    }

    /** Does this holder has shapes. */
    public boolean hasShapes(){
        return shapes.size() > 0;
    }

    /** Reads and sets all physics shape info to this holder. */
    public void readDecoderInfo(SavedFileDecoder.PhysicsShapesGroup e){
        if (e != null){
            shapes.clear();// senasias pametam.

            for (SavedFileDecoder.PhysicsFixtureShapes holder : e.shapes){
                shapes.add(readFixture(holder));
            }

            // toliau paprasti para.
            bodyType = e.bodyType;
            bodyOrigin.set(e.bodyOrigin);
            isBodyOriginMiddle = e.isOriginMiddle;
            fixedRotation = e.fixedRotation;
            bulletBody = e.bullet;
            linearDamping = e.linearDamping;
            angularDamping = e.angularDamping;
            gravityScale = e.gravityScale;
            angularVelocity = e.angularVelocity;
            linearVelocity.set(e.linearVelocity);
        }else {
            Gdx.app.log("PhysicsHolder", "PhysicsShapeGroup was not read.");
        }
    }

    // nuskatom fixture elementa is save file decoder.
    private PhysicsEditor.FixtureShapeHolder readFixture(SavedFileDecoder.PhysicsFixtureShapes fix){
        PhysicsEditor.FixtureShapeHolder sh = new PhysicsEditor.FixtureShapeHolder();
//        sh.x.addAll(fix.x);
//        sh.y.addAll(fix.y);
        for (float x : fix.x){
            sh.x.add(x);
        }
        for (float y : fix.y){
            sh.y.add(y);
        }
        sh.type = fix.type;
        sh.radius = fix.radius;
        sh.isSensor = fix.isSensor;
        sh.density = fix.density;
        sh.friction = fix.friction;
        sh.restitution = fix.restitution;
        sh.maskBits = fix.maskBits;
        sh.categoryBits = fix.categoryBits;
        sh.groupIndex = fix.groupIndex;
        return sh;
    }

    /* statiniai, lengvam priejimui. */

    /** power of two. Good for category bits. Can read hex numbers (hex numbers must start with 0x).
     * @return value converted to short. If failed then default value is returned and error message as toast shown up. */
    public static short parsePowerOfTwo(String text, short defaultValue){
        if (text.length() == 0){
            return 0;
        }

        try {
            short e;
            if (text.startsWith("0x")){ // ziurim ar hex skaicius.
                if (text.length() == 2){ // nedarase.
                    throw new NumberFormatException("missing numbers");
                }
                // verciam.
                e = Short.parseShort(text.substring(2), 16);
            }else { // paprastas skaicius.
                e = Short.parseShort(text);
            }

            // darom power of two.
            return (short) MathUtils.nextPowerOfTwo(e);
        }catch (NumberFormatException ex){
            AlertToast alertToast = new AlertToast("Failed converting to number: " + text + "\nTip: For hex numbers use 0x before number");
            alertToast.show();
            return defaultValue;
        }
    }

    /** Good for mask bits. Reads "|" and hex numbers (hex must start with 0x).
     * Mask bits are using numbers of power of two! example: 1|2|4|8|0x10. */
    // skaitys viskas kas eina per "|", ir dar skaitys hex skaicius, jie turi prasidet su "0x"
    public static short parseMaskBit(String text, short defaultValue){
        // ant tusciu nieko
        if (text.length() == 0){
            return 0;
        }
        try {
            // turi ta bitu kirtimo dalyka.
            if (text.contains("|")) {
                // turi ta kirtimo linija.
                String[] pieces = text.split("\\|");
                // pirma visus paversim i skaicius.
                short[] numbers = new short[pieces.length];
                for (int a = 0; a < pieces.length; a++) {
                    String n = pieces[a].trim();
                    if (n.length() == 0){ // tuscia. dedam 0.
                        numbers[a] = 0;
                        continue;
                    }
                    if (n.startsWith("0x")) { // hex skaicius.
                        // hex skaicius.
                        if (n.length() == 2){ // nedarasytas iki galo.
                            throw new NumberFormatException("missing numbers");
                        }
                        numbers[a] = Short.parseShort(n.substring(2), 16);
                    }else { // paprastas skaicius.
                        numbers[a] = Short.parseShort(n);
                    }
                }

                // o dabar viska suklijuojam.
                short anw = 0;
                for (short e : numbers){
                    anw |= e;
                }
                return anw;
            } else {
                // vientisas skaicius.
                if (text.startsWith("0x")){ // hex skaicius.
                    if (text.length() == 2){ // nedarasytas iki galo.
                        throw new NumberFormatException("missing numbers");
                    }
                    return Short.parseShort(text.substring(2), 16);
                }else { // paprastas skaicius.
                    return Short.parseShort(text);
                }
            }
        }catch (NumberFormatException ex){ // kazkas kazkur negerai.
            AlertToast alertToast = new AlertToast("Failed converting to number: " + text + "\nTip: For hex numbers use 0x before number");
            alertToast.show();
            return defaultValue;
        }
    }
}
