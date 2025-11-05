package com.engine.interfaces.controls.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.engine.core.Engine;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.views.ListView;
import com.engine.interfaces.controls.views.Panel;
import com.engine.interfaces.listeners.inputListeners.RightClickListener;
import com.engine.root.GdxPongy;

/** Balloon pop up which can be called from Interface right click. Represents menu similar to the right click menus. */
public class RightClickMenu extends PopMeniu implements RightClickListener{
//    private ListView listView;
    private Interface e;

    public RightClickMenu(){
        this(new RightClickMenuStyle());
    }

    /** Creates menu with custom panel in it. */
    public RightClickMenu(Panel e){
        super(e);
        useAnimation(false);
        setAnimatingTime(0.3f);
    }

    /** creates menu with Panel in it. */
    public RightClickMenu(Panel.PanelStyle st){
        super(st);
        useAnimation(false); // lame ta animacija
        setAnimatingTime(0.3f); // default yra 1, bet tai labai letai.
    }

    public Interface getRightClickedInterface() {
        return e;
    }

    /** show or hide controls. List will be updated automatically */
    public void setControlVisible(int controlId, boolean visible){
        Panel e = getController();
        int len = e.getHost().getControls().size();
        if (controlId >= 0 && controlId < len){
            e.getHost().getControls().get(controlId).setVisible(visible);
//            e.update();

            if (e instanceof ListView){ // apeinam ta klaida, kai list view neupdatina.
                ((ListView) e).update();
            }
        }
    }

    /** enable or disable controls */
    public void setControlEnabled(int controlId, boolean enable){
        Panel e = getController();
        int len = e.getHost().getControls().size();
        if (controlId >= 0 && controlId < len){
            e.getHost().getControls().get(controlId).setEnabled(enable);
//            e.update();
        }
    }

    @Override
    protected void onHide() {
        super.onHide();
        e = null; // nereikia cia laikyt.
    }

    @Override
    protected void draw(float x, float y, float width, float height, float offsetX, float offsetY) {
        if (isAnimating()){
            Engine p = GdxPongy.getInstance();
            float nheight = height * getAnimationProgress();
            if (p.pushScissor(x, y+height-nheight, width, nheight)){
                super.draw(x, y, width,height, offsetX, offsetY);
                p.popScissor();
            }
        }else {
            super.draw(x, y, width, height, offsetX, offsetY);
        }
    }

//    @Override
//    public ListView getController() {
//        return (ListView) super.getController();
//    }

    @Override
    public void rightClick(Interface owner, float x, float y, float offsetX, float offsetY) {
        if (!beforeRightClickOpen(owner, x, y, offsetX, offsetY)) {
            float nx, ny;
            int pos = owner.getAbstractPositioning();
            if (pos == Window.absoluteView) { // reik perskaiciuot.
                Engine p = GdxPongy.getInstance();
                Vector3 cord = p.worldToScreenCoords(x + offsetX, y + offsetY);
                cord = p.screenToFixedCoords(cord.x, Gdx.graphics.getHeight() - cord.y);
                nx = cord.x;
                ny = cord.y;
            } else {
                nx = x + offsetX;
                ny = y + offsetY;
            }
            ny = ny - getHeight();
            setPosition(nx, ny);
            show();
            e = owner;
        }
    }

//    private int getOwnerPos(InterfacesController e){
//        int pos = e.getPositioning();
//        while (pos == Window.absoluteView && e instanceof PanelHost){ // jeigu bus fixed tai stabdys. jeigu ne tai eis iki galo.
//            e = ((PanelHost) e).getController();
//            pos = e.getPositioning();
//        }
//        return pos;
//    }

    /** a little bit prepared class for right click with items which goes down. Use buttons or something like that. */
    public static class RightClickMenuStyle extends ListView.ListViewStyle{
        public RightClickMenuStyle(){
            width = 200; // irgi apytiksliai.
            rowCount = 1;
            columnCount = 1;
            separatorSize = 0;
            itemWidth = width;
            itemHeight = 40; // apytiksliai teksto dydzio
        }
    }

    /** Called when user right clicked on something. Return true to cancel menu opening. */
    protected boolean beforeRightClickOpen(Interface owner, float x, float y, float offsetX, float offsetY){
        return false;
    }
}
