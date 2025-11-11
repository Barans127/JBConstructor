package com.jbconstructor.editor.managers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;
import com.engine.core.Resources;
import com.engine.root.GdxWrapper;
import com.jbconstructor.editor.dialogs.SwitchFormsDialog;
import com.jbconstructor.editor.forms.editorForm.EditForm;

/** class to store different editForms. Allows to have more than one form per project. */
public class FormsManager {
//    private Array<EditForm> existingForms;
    private Array<String> formsKeys, formNames; // formu raktai, kad butu galima prieit prie ju.
    private Array<TextureRegionDrawable> formSS;
    private int counter;

    FormsManager(){
//        existingForms = new Array<>();
        formsKeys = new Array<>();
        formNames = new Array<>();
        formSS = new Array<>();
    }

    public boolean createEditForm(String formName){
        synchronized (this) {
            if (formName == null || formName.length() == 0) {
                System.out.println("Edit form name should not be null or zero length");
                return false;
            }
            if (formNames.contains(formName, false)) {
                System.out.println("Edit form creation failed. Forms name already exists.");
                return false;
            }
            while (true) {
                String name = "EditForm" + ++counter;
                if (createNewForm(name, formName)) {
                    formNames.add(formName);
                    return true;
                }
            }
        }
    }

    /** @return true if form was created. false if name already exists or is null */
    private boolean createNewForm(String name, String formName){

        if (name == null)
            return false;
//        for (String keys : formsKeys){
//            if (keys.equals(name)){
//                return false;
//            }
//        }
        Engine p = GdxWrapper.getInstance();
        if (p.existFormKey(name)) // jeigu toks raktas jau yra.
            return false;
        EditForm e = new EditForm(formName);
        p.addForm(e, name);
//        existingForms.add(e);
        formsKeys.add(name);
        formSS.add(null);
        return true;
    }

    public boolean containsFormName(String name){
        return formNames.contains(name, false);
    }

    /** @return form index */
    public int getFormIndex(String name){
        int index = 0;
        for (String names : formNames){
            if (name.equals(names)){
//                GdxPongy.getInstance().getForm(formsKeys.get(index));
                return index;
            }
            index++;
        }
        return -1; // nieko nerado.
    }

    public EditForm getForm(int index){
        if (index >= 0 && index < formsKeys.size){
            return (EditForm) GdxWrapper.getInstance().getForm(formsKeys.get(index));
        }else
            return null;
    }

    public EditForm getForm(String name){
        return getForm(getFormIndex(name));
    }

    /** @return form name */
    public String getFormName(int index){
        if (index >= 0 && index < formNames.size){
            return formNames.get(index);
        }
        return null;
    }

    /** changes form name. if form name already exist, form name will not be changed.
     * @return if form name was changed. */
    public boolean changeFormsName(String name, int index){
        if (index >= 0 && index < formNames.size) {
            if (formNames.contains(name, false)) {
                return false;
            }
            formNames.set(index, name);
            EditForm e = (EditForm) GdxWrapper.getInstance().getForm(formsKeys.get(index));
//            e.formNameWasChanged(name); // pranesim..
            SwitchFormsDialog dialog = e.getSwitchFormsDialog();
            dialog.nameWasChanged(name);
            return true;
        }else
            return false;
    }

    /** @return form SS. if ss was not set than null will be returned. */
    public Drawable getFormSS(int index){
        if (index >= 0 && index < formSS.size){
            return formSS.get(index);
        }
        return null;
    }

    /** sets new ss for current form. old ss will be disposed. */
    public void setFormSS(Texture e, int index){
        if (index >=0 && index < formSS.size && e != null) {
            TextureRegionDrawable dr = new TextureRegionDrawable(new TextureRegion(e));
            TextureRegionDrawable old = formSS.get(index);
            formSS.set(index, dr);
            if (old != null){
                Resources.addDisposable(old.getRegion().getTexture());
            }
        }
    }

    /** size of existing forms */
    public int getFormsSize(){
        return formNames.size;
    }

    public void removeForm(int index){
        synchronized (this) {
            if (index >= 0 && index < formsKeys.size) {
//            if (formsKeys.size == 1){
//                return; // paskutines formos nesalinsim. Turi but daugiau nei viena.
//            }
                Engine p = GdxWrapper.getInstance();
                p.removeForm(formsKeys.get(index)); // pasalinimas is formos.
//            existingForms.removeIndex(index);
                formsKeys.removeIndex(index);
                formNames.removeIndex(index);
                if (formSS.get(index) != null) {
                    Resources.addDisposable(formSS.get(index).getRegion().getTexture());
                }
                formSS.removeIndex(index);
            }
        }
    }

    public void removeForm(String name){
        int index = 0;
        for (String names : formNames){
            if (name.equals(names)){
                removeForm(index);
                return;
            }
            index++;
        }
    }

    /** switch between forms. */
    public void switchForms(int index, boolean useAnimation){
        if (index >= 0 && index < formsKeys.size){
            if (useAnimation){
                Engine p = GdxWrapper.getInstance();
                p.achangeState(formsKeys.get(index));
            }else {
//                Engine.changeState(formsKeys.get(index));
                Engine.getInstance().changeState(formsKeys.get(index)); // engine metodas pakeistas i instancini.
            }
        }
    }

//    public Array<String> getFormsKeys() {
//        return formsKeys;
//    }
}
