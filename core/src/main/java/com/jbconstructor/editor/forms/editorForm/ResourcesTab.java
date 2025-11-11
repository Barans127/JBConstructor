package com.jbconstructor.editor.forms.editorForm;

import com.engine.core.Resources;
import com.engine.ui.controls.Window;
import com.engine.ui.controls.views.Panel;
import com.engine.ui.controls.widgets.Button;
import com.engine.ui.controls.widgets.SymbolButton;
import com.engine.ui.controls.widgets.TabControl;
import com.engine.ui.listeners.ClickListener;
import com.jbconstructor.editor.dialogs.FileChooser;
import com.jbconstructor.editor.managers.Project;
import com.jbconstructor.editor.root.FileLoader;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Klase, kuri laikys images taba ir interface langa (galbut irgi tab) */
public class ResourcesTab extends TabControl implements EditMainPanel.ControlPanel {
    private ImagesTab imgTab;
    private SymbolButton fileLoad; // uzkrovimo mygtukas
    private float oldX; // animacijai.
    private OpenImages list; // load files listener.

    public ResourcesTab(EditForm form) {
        super(new TabControlStyle());
        //        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        // tabo isvaizda.
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        buttonStyle.background = Resources.getDrawable("halfWhiteColor");
        buttonStyle.textSize = 30;
        float height = p.getScreenHeight() * 0.8f, width = p.getScreenWidth()*0.2f;
        setMaxSize(width, height);
        setButtonsPosition(ButtonsPosition.TOP);
        setbuttonStyle(buttonStyle);

        // default paneliu style.
        Panel.PanelStyle st = new Panel.PanelStyle();
        st.width = width;
        st.height = height;

        { // images tab.
            Panel tab1 = new Panel(st);
            tab1.setBackground(buttonStyle.background);
            tab1.tintBackground(0xFFBFFCC6);
            addTab(tab1, "Images");

            // load button.
            SymbolButton.SymbolButtonStyle bst = new SymbolButton.SymbolButtonStyle();
            bst.background = buttonStyle.background;
            bst.text = "";
            bst.autoSize = false;
            bst.width = bst.height = p.getScreenWidth()*0.02f;
            bst.normalColor = 0x00000000;
            bst.onColor = 0xFFFF5500;
            bst.pressedColor = 0xAAFF5500;
            bst.positioning = Window.Position.absolute;
            bst.symbol = Resources.getTextureDrawable("mainEditorLoadFileKey");
            oldX = bst.x = 1030;
            bst.y = 620;
            SymbolButton e = new SymbolButton(bst);
            list = new OpenImages();
            e.setClickListener(new ClickListener() {
                @Override
                public void onClick() {
                    FileChooser ch = Project.getFileChooser();
                    JFileChooser jh = ch.getFileChooser();
//                    jh.setSelectedFiles(new File[]{});
                    jh.setMultiSelectionEnabled(true);
                    jh.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    jh.setDialogTitle("Select Resource folder or resources");
                    jh.setFileFilter(new FileNameExtensionFilter("Project resources", "jpg", "png", "gif", "bmp", "txt", "scml"));
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            FileChooser ch = Project.getFileChooser();
                            JFileChooser jh = ch.getFileChooser();
                            if (Project.getProjectRootFolderPath() == null) {
                                jh.setCurrentDirectory(new File("."));
                            }else {
                                jh.setCurrentDirectory(new File(Project.getProjectRootFolderPath()));
                            }
                            ch.open(list, true); // kadangi image krovimas tai tik main thread.
                        }
                    });
                }
            });
            // load mygtukas.
            tab1.addControl(fileLoad = e);

            // pats tabas.
            tab1.addControl(imgTab = new ImagesTab(form));
        }
        { // interface tab
            Panel tab1 = new Panel(st);
            tab1.setBackground(buttonStyle.background);
            tab1.tintBackground(0xFFBFFCC6);
            addTab(tab1, "Interface");
        }
    }

//    /** @return tabs who holds all resources. */
//    ListView getResourcesTab(int index){
//        return imgTab.getTab(index);
//    }

    /** Moves resources button. */
    void mainPanelMoved(float offsetX){
        fileLoad.setPosition(oldX + offsetX, fileLoad.getPosition().y);
    }

    /** Resources tab. Tab who holds all resources used in project. This is just tab. */
    public ImagesTab getImgTab(){ // visu resource tab
        return imgTab;
    }

    /** Listener who can load images. */
    public OpenImages getOpenImagesListener(){
        return list;
    }

//    @Override
//    public void setVisible(boolean visible) {
//        super.setVisible(visible);
//        if (visible) // cia atnaujinam resource lista.
                // per daznai atnaujins ta lista. Geriau det i onShow metoda.
//            imgTab.updateUsableResourceList();
//    }

    public static class OpenImages implements FileChooser.FileChooseListener{

        @Override
        public void fileChosen(JFileChooser chooser) {
            File[] files = chooser.getSelectedFiles(); // gaunas sarasa pasirinktu failu.
            FileLoader loader = new FileLoader(); // pasiruosiam loaderi.
            for (File e : files){
                loader.loadResources(e.getPath()); // uzloadinam files.
            }
            Project.load(loader, false); // ikeliam juos i projecta
//            imgTab.updateUsableResourceList(); // tegul susigaudo pats.
            chooser.setSelectedFile(new File("")); // isvalom selectoriu.
        }
    }
}
