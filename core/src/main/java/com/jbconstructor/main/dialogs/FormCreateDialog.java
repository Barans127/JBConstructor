package com.jbconstructor.main.dialogs;

import com.badlogic.gdx.utils.Align;
import com.engine.core.Resources;
import com.engine.interfaces.controls.PopUp;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.controls.widgets.TextBox;
import com.engine.interfaces.listeners.ClickListener;
import com.jbconstructor.main.managers.Project;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

public class FormCreateDialog extends PopUp {
    private TextBox name, location, folder;
    private CreateFormDialogListener listener;
    private ChooseFolder chooser;

    private Label mainLabel;
    private Button accept, cancel;

    public FormCreateDialog() {
        super(800, 400);

        // main label
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.textSize = 89;
        lst.horizontalAlign = Align.center;
        lst.width = getWidth()*0.9f;
        lst.height = lst.textSize;
//        lst.height = 80;
        lst.autoSize = false;
        lst.x = 0;
        lst.y = getHeight()-lst.height-10;
        lst.text = "Create new project";
        addControl(mainLabel = new Label(lst)); // bendras viso projekto.

        // project name label.
        lst.textOffsetX = getWidth()*0.1f;
        lst.textSize = 45;
//        lst.height = 40;
        lst.height = lst.textSize;
        lst.y -= lst.height/2; // nezinau kodel reik is dvieju dalint, bet kitaip neveik.
        lst.text = "Project name*:";
        lst.horizontalAlign = Align.left;
        addControl(new Label(lst)); // projekto vardas

        // project root location.
        lst.text = "Project root location:";
        lst.y -= lst.height*2;
        addControl(new Label(lst)); // location

        // project resources folder.
        lst.text = "Project resources folder:";
        lst.y -= lst.height*2;
        addControl(new Label(lst)); // folder

        // textboxai
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.background = Resources.getDrawable("whiteSystemColor");
        tst.textSize = 45;
        tst.autoSize = false;
        tst.width = getWidth()*0.8f;
        tst.height = tst.textSize;
        tst.x = getWidth()*0.1f;
        tst.y = getHeight()-160;
        tst.verticalAlign = Align.center;
        name = new TextBox(tst);
        addControl(name);

        tst.y -= tst.height*2;
        tst.lockText = true;
        location = new TextBox(tst);
        addControl(location);

        tst.y -= tst.height*2;
        folder = new TextBox(tst);
        addControl(folder);

        // butonai
        Button.ButtonStyle bst = new Button.ButtonStyle();
        bst.background = tst.background;
        bst.normalColor = 0xFF0000FF;
        bst.text = "Create";
        Button cr = new Button(bst);
        cr.auto(); // sugaudom dydi.
        cr.setPosition(getWidth()-cr.getWidth()-10, 10);
        cr.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                onCreateForm();
            }
        });
        addControl(accept = cr);

        bst.text = "Cancel";
        Button cl = new Button(bst);
        cl.auto();
        cl.setPosition(getWidth() - cr.getWidth() - cl.getWidth() - 20, 10);
        cl.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                onCancel();
            }
        });
        addControl(cancel = cl);

        // symbol butons folderio lokacijoms.
        SymbolButton.SymbolButtonStyle sst = new SymbolButton.SymbolButtonStyle();
        sst.background = tst.background;
        sst.symbol = Resources.getTextureDrawable("startFormSearchKey");
        sst.autoSize = false;
        sst.width = sst.height = tst.height;
        sst.x = tst.x + tst.width + 10;
        sst.y = tst.y + tst.height*2;
        sst.normalColor = 0x00000000;
        sst.onColor = 0xFFFF5500;
        sst.pressedColor = 0xFFAA5500;
        SymbolButton loc = new SymbolButton(sst);
        loc.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                locationSearch();
            }
        });
        addControl(loc);
        sst.y = tst.y;
        SymbolButton fol = new SymbolButton(sst);
        fol.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                textureLocationSearch();
            }
        });
        addControl(fol);

        chooser = new ChooseFolder();
    }

    /* veikimas */

    private void locationSearch(){
        prepareFileChooser("root", 0);
    }

    private void textureLocationSearch(){
        prepareFileChooser("textures", 1);
    }

    private void prepareFileChooser(String text, final int state){
        FileChooser ch = Project.getFileChooser();
        final JFileChooser jh = ch.getFileChooser();
        jh.setMultiSelectionEnabled(false);
        jh.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // siuo atveju mum reik tik directories.
        jh.setDialogTitle("Choose "+text+" folder");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (jh) {
                    if (location.getText().length() > 0) {
                        jh.setCurrentDirectory(new File(location.getText())); // kad eitu jau i esama folderi.
                    }else {
                        jh.setCurrentDirectory(new File("."));
                    }
                    chooser.state = state; // kad textures folderio paiesaka.
                    FileChooser ch = Project.getFileChooser();
                    ch.open(chooser, false);
                }
            }
        });
//        synchronized (jh){ // yra taip, kad ten tipo liecia GUI, o jo liest negali sitas thread, todel sita mygdom, dedam i kita thread,
//            try { // o tas atlikes darba vel sita prikels.
//                jh.wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//            jh.setCurrentDirectory(new File("/.")); // jei nera lokacijos, tai tiesiog sitoks.
//        chooser.state = state; // kad textures folderio paiesaka.
//        ch.open(chooser, false);
//        return ch;
    }

    private void onCreateForm(){// create mygtuko paspaudimas
        if (listener!=null){
            if (listener.onCreate(name.getText(), location.getText(), folder.getText())){
                close();
            }
        }else { // jei ner listener uzdarom
            close();
        }
    }

    private void onCancel(){ // cancel paspaudimas
        if (listener != null)
            listener.cancel();
        close(); // close this pop up.
    }

    /* textobx gavimas */

    public TextBox getNameInput() {
        return name;
    }

    public TextBox getLocationInput() {
        return location;
    }

    public TextBox getFolderInput() {
        return folder;
    }

    /** @return "Create new project" label. */
    public Label getMainLabel() {
        return mainLabel;
    }

    public Button getAcceptButton(){
        return accept;
    }

    public Button getCancelButton(){
        return cancel;
    }

    /* override */

    @Override
    public void onOpen() {
        name.setText("");
        location.setText("");
        folder.setText(""); // belekoks default.
        super.onOpen();
    }

    @Override
    protected boolean closeButtonPressed() {
        if (listener != null)
            listener.cancel();
        return super.closeButtonPressed();
    }

    /* listener */

    public void setFormCreateListener(CreateFormDialogListener e){
        listener = e;
    }

    public CreateFormDialogListener getListener(){
        return listener;
    }

    public interface CreateFormDialogListener{
        /** called when create button was clicked.
         * @return true form will be closed, false form will not be closed */
        public boolean onCreate(String name, String rootLocation, String folderPath);
        public void cancel();
    }

    private class ChooseFolder implements FileChooser.FileChooseListener{
        int state;

        @Override
        public void fileChosen(JFileChooser chooser) {
            if (state == 0){
                location.setText(chooser.getSelectedFile().getAbsolutePath());
            }else {
                folder.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
}
