package com.jbconstructor.main.editors;

import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.Window;
import com.engine.interfaces.controls.dialogs.ConfirmDialog;
import com.jbconstructor.main.forms.editorForm.EditForm;

import java.util.List;

/** class which allows to copy interface and paste new interface which is similar to first one. */
public class CopyManager {
    private Array<Interface.InterfaceStyle> copies;
    private Array<Interface> tmpInterfaceList;

    private float jumpX, jumpY; // default 25 bus.

    public CopyManager(){
        copies = new Array<>();
        tmpInterfaceList = new Array<>();
        jumpX = Resources.getPropertyFloat("copyJumpX", 25);
        jumpY = Resources.getPropertyFloat("copyJumpY", 25);
    }

    /** size of how much copies will jump forward (this will avoid copies take over original copy). */
    public void setJumpSize(float jumpX, float jumpY){
        this.jumpX = jumpX;
        this.jumpY = jumpY;
    }

    /** copies interfaces styles from given list */
    public void copy(List<Interface> list){
        // perorderinimui reiks situ masyvu.
        Interface[] interfaces = new Interface[list.size()];
        int[] indexes = new int[list.size()];

        // susirandam edit forma. Sioj vietoj perdeliosim interfaces, kad jie eitu tokia pat tvarka kaip ir nukopinti
        // listas paduos is resizer o ten tvarka gali but neteisinga (ir buna daznai).
        Window window = Engine.getInstance().getActiveForm();
        if (window instanceof EditForm){
            EditForm form = (EditForm) window;

            // pirma sugaudom indexus kur butent paduodas interface yra.
            List<Interface> controls = form.getController().getControls();
            for (int a = 0; a < controls.size(); a++){
                Interface e = controls.get(a);
                for (int k = 0; k < list.size(); k++){
                    Interface compare = list.get(k);
                    if (e == compare){
                        indexes[k] = a;
                    }
                }
            }

            int currentIndex = 0; // kurioj vietoj idet interfaca.
            int lowestRank = -1; // nustatyt, kad tu paciu neimtume.
            // o cia jau galim permetyt pagal reikiama orderi.
            while (currentIndex < interfaces.length){
                Interface selected = null; // dabartinis pasirinkimas.
                int lastRank = controls.size(); // zemiausias rankas.
                for (int a = 0; a < list.size(); a++){
                    // viskas paprasta. Turi but mazesnis uz last rank, bet didesnis uz pries tai buvusi maziausia.
                    if (indexes[a] < lastRank && indexes[a] > lowestRank){
                        lastRank = indexes[a];
                        selected = list.get(a);
                    }
                }
                // jei kartais kazkas ne taip.
                if (selected == null){
                    ConfirmDialog e = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.OK);
                    e.setText("Unknown error occurred while copying.");
                    e.show();
                    return;
                }
                // sededam info ten kur reik.
                interfaces[currentIndex] = selected;
                lowestRank = lastRank; // pazymim zemiausia ranka.
                // kito ieskosim.
                currentIndex++;
            }
        }else {
            ConfirmDialog e = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.OK);
            e.setText("Unknown error occurred while copying.");
            e.show();
            return;
        }

        copies.clear();
        tmpInterfaceList.clear();
        // isdeliosim pagal edit formos orderi. Peroderinti normaliai dabar.
        for (Interface e : interfaces){
            Interface.InterfaceStyle st = e.getStyle();
            st.x += jumpX; // persokimas i prieki, kad kopija nesoktu ant originalo ir taip pasisleptu uz jo.
            st.y += jumpY;
            copies.add(st);
        }
    }

    /** creates copies of interfaces which was copied.
     * @return list of copies. */
    public Array<Interface> paste(){
        tmpInterfaceList.clear();
        for (Interface.InterfaceStyle e : copies){
            tmpInterfaceList.add(e.createInterface());
            e.x += jumpX; // po kiekvieno paste - suolis.
            e.y += jumpY;
        }
        return tmpInterfaceList;
    }

    /** @return copied interfaces. <code>paste()</code> must be called before otherwise list will be empty or have old copies. */
    public Array<Interface> getCopies(){
        return tmpInterfaceList;
    }

    /** @return true if copy list is not empty. */
    public boolean isCopied(){
        return copies.size > 0;
    }

    /** clears all copies. */
    public void clear(){
        tmpInterfaceList.clear();
        copies.clear();
    }
}
