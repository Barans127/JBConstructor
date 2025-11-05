package com.jbconstructor.main.editors;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.InterfacesController;
import com.jbconstructor.main.managers.Project;

import java.util.List;

/** Undo Redo controller. Manages all undo and redo. */
public class MoveController {
    private Pool<MoveTracker> pool;
    private Array<MoveTracker> activeTracks, redoTracks;
    private Array<Interface> tempList;
    private Array<Float> flushSettings;

    private int maxMoves; // nu tipo kiek daugiausiai undo bus.


    public MoveController(){
        maxMoves = Resources.getPropertyInt("maxUndoMoves", 20);
        pool = new Pool<MoveTracker>(maxMoves) {
            @Override
            protected MoveTracker newObject() {
                return new MoveTracker();
            }
        };
        activeTracks = new Array<>(maxMoves);
        redoTracks = new Array<>(maxMoves);
        tempList = new Array<>();
        flushSettings = new Array<>();
    }

    public void setMaxMoves(int maxMoves){ // neperskaiciuoja undu, nors turetu.
        if (maxMoves > 0){
            this.maxMoves = maxMoves;
        }
    }

    public void setInterfaces(List<Interface> e){
        tempList.clear();
        for (Interface a : e){
            tempList.add(a);
        }
    }

    public void setInterfaces(Array<Interface> e){
        tempList.clear();
        tempList.addAll(e);
    }

    /** for custom intarface append. */
    public Array<Interface> getList(){
        return tempList;
    }

    public Array<MoveTracker> getUndoTracks() {
        return activeTracks;
    }

    public Array<MoveTracker> getRedoTracks() {
        return redoTracks;
    }

    public void moved(int id, String info){
        MoveTracker e = pool.obtain();
        e.setMove(id, info);
        e.setControls(tempList);
        tempList.clear();
        if (activeTracks.size == maxMoves){
            pool.free(activeTracks.removeIndex(0));
        }
        activeTracks.add(e);
        for (int a = redoTracks.size-1; a >=0; a--){
            pool.free(redoTracks.removeIndex(a));
        }
        redoTracks.clear(); // del viso pikto.
        Project.getSaveManager().triggerSave(); // pranesam, kad kazkas pasikeite.4
    }

    public void moved(int id, float... additionalSett){
        MoveTracker e = pool.obtain();
        e.setMove(id, additionalSett);
        e.setControls(tempList);
        tempList.clear();
        if (activeTracks.size == maxMoves){
            pool.free(activeTracks.removeIndex(0));
        }
        activeTracks.add(e);
        for (int a = redoTracks.size-1; a >=0; a--){
            pool.free(redoTracks.removeIndex(a));
        }
        redoTracks.clear(); // del viso pikto.
        Project.getSaveManager().triggerSave(); // pranesam, kad kazkas pasikeite.
    }

    /** flush settings will be used */
    public void moved(int id){
        float[] e = new float[flushSettings.size];
        for (int a = 0; a < flushSettings.size; a++){
            e[a] = flushSettings.get(a);
        }
        moved(id, e);
        flushSettings.clear();
    }

//    /** when interfaceController is needed */
//    public void moved(int id, int idp, InterfacesController e){
//        moved(id, idp);
//        activeTracks.get(activeTracks.size-1).setController(e);
//    }
    /** use with flushSettings. */
    public void moved(int id, InterfacesController e){
        moved(id);
        activeTracks.get(activeTracks.size-1).setController(e);
    }

    /** for chains undo redo actions. */
    public void moved(int id, ChainEdging.Chains e, float... set){
        moved(id, set);
        activeTracks.get(activeTracks.size - 1).setChain(e);
    }

    /** for chains. flush settings used. */
    public void moved(int id, ChainEdging.Chains e){
        float[] array = new float[flushSettings.size];
        for (int a = 0; a < array.length; a++){
            array[a] = flushSettings.get(a);
        }
        moved(id, e, array);
        flushSettings.clear();
    }

    public void undo(){
        if (activeTracks.size > 0) {
            MoveTracker e = activeTracks.get(activeTracks.size - 1);
            e.act();
            activeTracks.removeIndex(activeTracks.size-1);
            redoTracks.add(e);
            Project.getSaveManager().triggerSave(); // pranesam, kad kazkas pasikeite.
        }
    }

    public void redo(){
        if (redoTracks.size > 0){
            MoveTracker e = redoTracks.get(redoTracks.size - 1);
            e.act();
            redoTracks.removeIndex(redoTracks.size-1);
            activeTracks.add(e);
            Project.getSaveManager().triggerSave(); // pranesam, kad kazkas pasikeite.
        }
    }

    public Array<Float> getFlushSettings() {
        return flushSettings;
    }
}
