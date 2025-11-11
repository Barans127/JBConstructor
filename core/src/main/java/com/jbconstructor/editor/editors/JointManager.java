package com.jbconstructor.editor.editors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class JointManager {
    /*
    * Joint type:
    * 0 - distance joint
    * 1 - friction joint
    * 2 - gear joint
    * 3 - motor joint
    * 4 - mouse joint
    * 5 - prismatic joint
    * 6 - pulley joint
    * 7 - revolute joint
    * 8 - rope joint
    * 9 - weld joint
    * 10 - wheel joint
     */
    private Array<JointInfo> joints;

    public JointManager(){
        joints = new Array<>();
    }

    /* joints control */

    /** creates new joint and adds it to list. id cannot be duplicate. Duplicate id will be renamed. */
    public JointInfo createJoint(String id){
        if (id == null || id.length() == 0){
            return null;
        }
        JointInfo e = new JointInfo(); // sukuriam nauja
//        changeJointType(e, type); // nustatom jo typa.
        changeJointId(e, id); // idedam legalu id.
        joints.add(e);// dedam i sarasus.
        return e;
    }

    /** creates new joint and adds it to list. Id must be unique and not match with other joints ids. */
    public JointInfo copyJoint(String id, JointInfo toCopy){
        if (toCopy == null){
            Gdx.app.log("JointManager", "There is nothing to copy...");
            return null;
        }
        JointInfo e = createJoint(id);
        if (e == null){
            Gdx.app.log("JointManager", "Something wrong with id. Id must not be null and zero length!");
            return null;
        }
        // pradedam viska copyint.
        e.jointType = toCopy.getJointType();
        e.bodyA = toCopy.bodyA;
        e.bodyB = toCopy.bodyB;
        e.bodyAIsResource = toCopy.bodyAIsResource;
        e.bodyBIsResource = toCopy.bodyBIsResource;
        e.collideConnected = toCopy.collideConnected;
        e.anchorA.set(toCopy.anchorA);
        e.anchorB.set(toCopy.anchorB);
        e.localAxisA.set(toCopy.localAxisA);
        e.groundAnchorB.set(toCopy.groundAnchorB);
        e.length = toCopy.length;
        e.frequencyHz = toCopy.frequencyHz;
        e.dampingRatio = toCopy.dampingRatio;
        e.maxForce = toCopy.maxForce;
        e.maxTorque = toCopy.maxTorque;
        e.joint1ID = toCopy.joint1ID;
        e.joint2ID = toCopy.joint2ID;
        e.ratio = toCopy.ratio;
        e.enableLimit = toCopy.enableLimit;
        e.enableMotor = toCopy.enableMotor;
        // grazinam musu katik nucopinta joint
        return e;
    }

    public void changeJointId(JointInfo e, String id){
        if (id != null && id.length() > 0) {
            // paziurim ar turimas id legalus.
            String name = id;
            int num = 1;
            MAIN:
            while (true) {
                for (JointInfo jointInfo : joints) {
                    if (jointInfo != e && jointInfo.jointID.equals(name)) { // saves neimt ir ziurim ar toks id yra.
                        // toks id yra.
                        name = id + num; // perdarom name.
                        num++; // pakeliam skaitliuka
                        continue MAIN; // ziurim is naujo su naujai formuotu vardu.
                    }
                }
                break; // atejus cia stabdom, kadangi tokio vardo ner.
            }

            // cia atejus id jau bus legalus.
            String old = e.getJointID();
            e.jointID = name;

            // dabar paziurim ar nera gear joint pasirinke si joint.
            for (int a = 0; a < joints.size; a++){
                JointInfo info = joints.get(a);
                if (info != e){ // saves neims.
                    if (info.getJointType() == 2){ // tik gear joint apply sitam dalykui.
                        if (info.joint1ID != null && info.joint1ID.equals(old)){ // id yra ir jis atitinka senaji varda
                            info.joint1ID = name; // keiciam id.
                        }
                        if (info.joint2ID != null && info.joint2ID.equals(old)){
                            info.joint2ID = name; // keiciam id. Ta pati situacija kaip su pirmu.
                        }
                        // nestabdom loop, nes gali ir daugiau but gear jointu.
                    }
                }
            }
        }
    }

    /** Changes joint type. Also resets it's info. */
    public void changeJointType(JointInfo e, int type){
        if (e != null){
            if (type >= 0 && type < 11) { // turi but teisingas type.
                e.jointType = type;
                // resetinam bendra info
                // bendros info resetint nereik!
//                e.bodyA = null;
//                e.bodyB = null;
//                e.bodyAIsResource = false;
//                e.bodyBIsResource = false;
//                e.collideConnected = false;
                // resetinam pagal joint tipa
                switch (e.jointType) {
                    case 0: // distance joint
                        e.anchorA.set(0,0);
                        e.anchorB.set(0,0);
                        e.length = 1;
                        e.frequencyHz = 0;
                        e.dampingRatio = 0;
                        break;
                    case 1: // friction joint
                        e.anchorA.set(0,0);
                        e.anchorB.set(0,0);
                        e.maxForce = 0;
                        e.maxTorque = 0;
                        break;
                    case 2: // gear joint
                        e.joint1ID = null;
                        e.joint2ID = null;
                        e.ratio = 1;
                        break;
                    case 3: // motor joint
                        e.anchorA.set(0,0);
                        e.length = 0;
                        e.maxForce = 1;
                        e.maxTorque = 1;
                        e.ratio = 0.3f;
                        break;
                    case 4: // mouse joint
                        e.anchorA.set(0,0);
                        e.maxForce = 0;
                        e.frequencyHz = 5;
                        e.dampingRatio = 0.7f;
                        break;
                    case 5: // prismatic joint
                        e.anchorA.set(0,0);
                        e.anchorB.set(0,0);
                        e.localAxisA.set(1, 0);
                        e.length = 0;
                        e.enableLimit = false;
                        e.frequencyHz = 0;
                        e.dampingRatio = 0;
                        e.enableMotor = false;
                        e.maxForce = 0;
                        e.maxTorque = 0;
                        break;
                    case 6: // pulley joint
                        e.localAxisA.set(-1, 1);
                        e.groundAnchorB.set(1,1);
                        e.anchorA.set(-1,0);
                        e.anchorB.set(1,0);
                        e.maxForce = 0;
                        e.maxTorque = 0;
                        e.ratio = 1;
                        break;
                    case 7: // revolute joint
                        e.anchorA.set(0,0);
                        e.anchorB.set(0,0);
                        e.length = 0;
                        e.enableLimit = false;
                        e.frequencyHz = 0;
                        e.dampingRatio = 0;
                        e.enableMotor = false;
                        e.maxForce = 0;
                        e.maxTorque = 0;
                        break;
                    case 8: // rope joint
                        e.anchorA.set(-1,0);
                        e.anchorB.set(1,0);
                        e.length = 0;
                        break;
                    case 9: // weld joint
                        e.anchorA.set(0,0);
                        e.anchorB.set(0,0);
                        e.length = 0;
                        e.frequencyHz = 0;
                        e.dampingRatio = 0;
                        break;
                    case 10: // wheel joint
                        e.anchorA.set(0,0);
                        e.anchorB.set(0,0);
                        e.localAxisA.set(1,0);
                        e.enableMotor = false;
                        e.maxTorque = 0;
                        e.maxForce = 0;
                        e.frequencyHz = 2;
                        e.dampingRatio = 0.7f;
                        break;
                }
                // paziurim ar sitas joint nepriklause gear joint sarasui. Jei pakeite type ne i
                // prismatic ir revolute tai turim salint is saraso ji.
                if (e.getJointType() != 5 && e.getJointType() != 7){
                    for (int a= 0; a < joints.size; a++){
                        JointInfo info = joints.get(a);
                        if (info != e){ // neturi but tas pats
                            if (info.joint1ID != null && info.joint1ID.equals(e.getJointID())){
                                info.joint1ID = null; // atitiko. Salinam, joint nebetinkamas.
                            }
                            if (info.joint2ID != null && info.joint2ID.equals(e.getJointID())){
                                info.joint2ID = null; // atitiko. Salinam!
                            }
                        }
                    }
                }
            }
//            else {
//                Gdx.app.log("JointManager", "Joint with type: " + type + " doesn't exist!");
//            }
        }
    }

    /** Removes given joint from list. */
    public void removeJoint(JointInfo e){
        joints.removeValue(e, true);
    }

    /** Removes joint with given id */
    public void removeJoint(String id){
        for (JointInfo e : joints){
            if (e.getJointID().equals(id)){
                removeJoint(e);
                return;
            }
        }
    }

    /** removes joint at specified index. */
    public void removeJoint(int index){
        if (index >= 0 && index < joints.size)
            joints.removeIndex(index);
    }

    /* getters */

    /** @return joint from id. If joint with given id doesn't exist then null is returned. */
    public JointInfo getJointInfo(String id){
        if (id == null){
            return null;
        }
        for (JointInfo e : joints){
            if (e.jointID.equals(id)){
                return e;
            }
        }
        return null;
    }

    /** joint info by index. */
    public JointInfo getJointInfo(int index){
        return index >= 0 && index < joints.size ? joints.get(index) : null;
    }

    /** size of all joints. */
    public int getJointSize(){
        return joints.size;
    }

    /** class which holds info about joint. */
    public class JointInfo{
        /* info apie pati joint */
        private int jointType = -1;
        private String jointID;

        /* bendra joint info */
        /** Id of body. */
        public String bodyA, bodyB;
        /** true: body from resources, false: from chain list. */
        public boolean bodyAIsResource, bodyBIsResource;
        public boolean collideConnected = false;

        /* joint info, visu joint... kokiam joint info reik, tokia ir naudos. */
        /** Motor joint linear offset.
        * mouse joint vietoj target. */
        public final Vector2 anchorA = new Vector2(), anchorB = new Vector2();

        /** pulley joint: groundAnchorA */
        public final Vector2 localAxisA = new Vector2();

        public final Vector2 groundAnchorB = new Vector2();

        /** distance joint.
        * angular offset.
        * reference angle */
        public float length = 1f;
        /** prismatic joint: lower translation */
        public float frequencyHz;
        /** prismatic joint: upper translation */
        public float dampingRatio;

        /** pulley joint: lengthA.
         * revolute joint: motor speed. */
        public float maxForce;
        /** prismatic joint: max motor speed.
         * pulley joint: lengthB */
        public float maxTorque;

        /** Gear joint. Joint id. Joint must be prismatic or revolute. */
        public String joint1ID, joint2ID;
        /** gear joint ratio.
        * motor joint: correction factor. */
        public float ratio;

        public boolean enableLimit, enableMotor;

        private JointInfo(){} // neleidziam kurt instanciju.

        public int getJointType(){
            return jointType;
        }

        public String getJointID(){
            return jointID;
        }

//        public String getJointTypeText(){
//            switch (jointType){
//                case 0:
//                    return "Distance joint";
//                case 1:
//                    return "Friction joint";
//                case 2:
//                    return "Gear joint";
//                case 3:
//                    return "Motor joint";
//                case 4:
//                    return "Mouse joint";
//                case 5:
//                    return "Prismatic joint";
//            }
//        }
    }
}
