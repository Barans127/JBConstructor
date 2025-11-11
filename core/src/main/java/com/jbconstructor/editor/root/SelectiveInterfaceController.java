package com.jbconstructor.editor.root;

import com.engine.ui.controls.Control;
import com.engine.ui.controls.ControlHost;
import com.engine.ui.controls.Window;
import com.jbconstructor.editor.forms.editorForm.EditForm;

import java.util.ArrayList;

/** allows to select items when they are pressed. */
public class SelectiveInterfaceController extends ControlHost {
    private EditForm main;
    private Resizer sizer;
    private Selector selector;
    private ArrayList<Control> exceptionList;
    private boolean selectionActive = true;
    private boolean disableAbsoluteDraw, disableFixedDraw;

    private SelectedInterfaceListener listener;

    public SelectiveInterfaceController(){
        exceptionList = new ArrayList<>();
        setEnableFrustum(true);
    }

    public void setMainForm(EditForm e){
        main = e;
    }

    public void setResizer(Resizer e){
        sizer = e;
    }

    public void setSelector(Selector e){
        if (selector != null)
            selector.setSelectionListener(null);
        selector = e;
        if (selector != null){
            selector.setSelectionListener(new Selector.SelectionListener() {
                @Override
                public void onSelect(Control e) {
                    if (e == sizer)
                        return;
                    for (Control c : exceptionList){
                        if (c == e){
                            return;
                        }
                    }
//                    int pos = e.getPositioning() == Window.relativeView ? getPositioning() : e.getPositioning();
//                    if ((disableFixedDraw && pos == Window.fixedView) || (disableAbsoluteDraw && pos == Window.absoluteView)){
//                        return;
//                    }
                    sizer.addEditableControl(e);
                    main.checkControlInfo();
                }

                @Override
                public void onDiselect(Control e) {
                    sizer.removeEditableControl(e);
                    main.checkControlInfo();
                }

                @Override
                public void massDiselection() {
                    sizer.releaseAll();
                    main.checkControlInfo();
                }
            });
        }
    }

    /** ignores this interface when selecting. allows to work properly. */
    public void addException(Control e){
        if (e == null || exceptionList.contains(e)){
            return;
        }
        exceptionList.add(e);
    }

    public void removeException(Control e){
        exceptionList.remove(e);
    }

    public ArrayList<Control> getExceptionList() {
        return exceptionList;
    }

    /** turn on and off selection. */
    public void enableSelection(boolean enable){
        selectionActive = enable;
    }

    public void setSelectionListener(SelectedInterfaceListener e){
        listener = e;
    }

    public boolean isDisableAbsoluteDraw() {
        return disableAbsoluteDraw;
    }

    public boolean isDisableFixedDraw() {
        return disableFixedDraw;
    }

    /** hide absolute controls */
    public void disableAbsoluteDraw(boolean disableAbsoluteDraw) {
        this.disableAbsoluteDraw = disableAbsoluteDraw;
    }

    /** hide fixed controls. */
    public void disableFixedDraw(boolean disableFixedDraw) {
        this.disableFixedDraw = disableFixedDraw;
    }

    //    @Override
//    public boolean addControl(Interface contr) {
//        return super.addControl(contr);
//    }

    @Override
    protected void touchDown(Control c, float x, float y, int pointer, int button) {
        if (selectionActive){
            if (c == sizer || c == selector)
                return;
            for (Control e : exceptionList){
                if (c == e){
                    return;
                }
            }
            c.release(); // neleist paspaust. ignoruot paspaudima.
            int pos = c.getPositioning() == Window.relativeView ? getPositioning() : c.getPositioning();
            if ((disableFixedDraw && pos == Window.fixedView) || (disableAbsoluteDraw && pos == Window.absoluteView)){
                return;
            }
            if (listener != null)
                listener.interfaceSelected(c);
        }
    }

    @Override
    public boolean addControl(Control contr) {
        main.updateList();
        return super.addControl(contr);
    }

    @Override
    public void removeControl(Control e) {
        super.removeControl(e);
        main.updateList();
        sizer.releaseAll();
    }

    public void removeControlWithoutSizerUpdate(Control e){
        super.removeControl(e);
        main.updateList();
    }

    @Override
    public boolean addControl(Control contr, int index) {
        main.updateList();
        return super.addControl(contr, index);
    }

    @Override
    protected boolean checkControlIdName(Control e, String futureName) {
        if (super.checkControlIdName(e, futureName)){
            main.updateList();
            return true;
        }
        return false;
    }

//    @Override
//    protected void drawInterfaces(boolean isAbsoluteDraw) {
//        if (disableAbsoluteDraw && isAbsoluteDraw) {
//            return;
//        }else if (disableFixedDraw && !isAbsoluteDraw) {
//            return;
//        }
//        super.drawInterfaces(isAbsoluteDraw);
//    }

    @Override
    protected boolean amIVisible(Control e) { // jei disablinta absolute ir fixed, tai paziurim ar piest galima.
        if (e == sizer || e == selector)
            return true;
        for (Control a : exceptionList){
            if (e == a){
                return true;
            }
        }
        // detale gali but uz ekrano ribu.
        if (!super.amIVisible(e)){
            return false;
        }
        int pos = e.getPositioning() == Window.relativeView ? getPositioning() : e.getPositioning();
        return (!disableFixedDraw || pos != Window.fixedView) && (!disableAbsoluteDraw || pos != Window.absoluteView);
    }


//    @Override
//    protected boolean drawInterface(Interface e, boolean isFixed) {
//        if (e == sizer || e == selector)
//            return true;
//        for (Interface a : exceptionList){
//            if (e == a){
//                return true;
//            }
//        }
//        int pos = e.getPositioning() == Window.relativeView ? getPositioning() : e.getPositioning();
//        return (!disableFixedDraw || pos != Window.fixedView) && (!disableAbsoluteDraw || pos != Window.absoluteView);
//    }

    public interface SelectedInterfaceListener{
        void interfaceSelected(Control e);
    }
}
