package com.jbconstructor.main.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.engine.core.Resources;
import com.engine.interfaces.controls.PopUp;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.listeners.ClickListener;
import com.jbconstructor.main.forms.editorForm.EditForm;
import com.jbconstructor.main.managers.Project;

public class SettingPicker extends PopUp {
    private EditForm editForm;

    // parametrai
    private PrefixIdDialog prefixIdDialog;
    private BackgroundColorChangeListener backgroundColorChangeListener;

    public SettingPicker(EditForm e) {
        super(647, 200);

        this.editForm = e;

        SymbolButton.SymbolButtonStyle sst = new SymbolButton.SymbolButtonStyle();
        sst.textSize = 30;
        sst.background = Resources.getDrawable("halfWhiteColor");
        sst.normalColor = 0x00000000;
        sst.onColor = 0xFFFF5500;
        sst.pressedColor = 0xFFAA5500;
        sst.symbol = Resources.getTextureDrawable("settingPickerPrefixPickKey");
        sst.text = "";
        sst.x = 31;
        sst.autoSize = false;
        sst.width = 169;
        sst.height = 161;
        sst.y = 19;
        sst.textSize = 30;
        sst.position = SymbolButton.TextPosition.DOWN;
        sst.symbolWidth = 70;
        sst.symbolHeight = 70;
        sst.text = "Change id prefix";

        // mygtukai
        SymbolButton prefixChoose = new SymbolButton(sst);
        prefixChoose.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                editForm.getMainPanel().getAdditionalPanel().close();
                SettingPicker.this.close();
                prefixIdDialog.open();
            }
        });

        sst.x = 233;
        sst.symbol = Resources.getDrawable("settingPickerBackgroundPickKey");
        sst.text = "Change background color";

        SymbolButton backgroundColorChoose = new SymbolButton(sst);
        backgroundColorChoose.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                editForm.getMainPanel().getAdditionalPanel().close();
                SettingPicker.this.close(); // uzsidarom, gi nepatogu kai jis stovi atsidares.
                ColorPicker e = editForm.getColorPicker();
                e.setColorPickerListener(backgroundColorChangeListener);
                e.show(editForm.getBackgroundColor());
            }
        });

        sst.x = 435;
        sst.symbol = Resources.getDrawable("shortCutIcon");
        sst.text = "Short cuts";

        SymbolButton shortCuts = new SymbolButton(sst);
        shortCuts.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                new ShortCutDialog().open();
            }
        });

        addControl(prefixChoose, backgroundColorChoose, shortCuts);

        // uzkraunam ko reik
        prefixIdDialog = new PrefixIdDialog(e);
        backgroundColorChangeListener = new BackgroundColorChangeListener();
    }

    /** Prefix dialog. */
    public PrefixIdDialog getPrefixIdDialog(){
        return prefixIdDialog;
    }

    private class BackgroundColorChangeListener implements ColorPicker.ColorPickerListener {

        BackgroundColorChangeListener(){}

        @Override
        public void setColor(Color color) {
            int old = editForm.getBackgroundColor();
            int newColor = Color.argb8888(color);
            if (old != newColor) {
                editForm.setBackgroundColor(newColor);

                Project.save();
            }
        }

        @Override
        public void cancel() {}
    }

    private class ShortCutDialog extends PopUp{
        ShortCutDialog(){
            super(1016, 633);

            // title label;
            Label.LabelStyle lst = new Label.LabelStyle();
            lst.autoSize = false;
            lst.x = 22;
            lst.y = 581;
            lst.width = 503;
            lst.height = 51;
            lst.textSize = 56;
            lst.horizontalAlign = Align.center;
            lst.verticalAlign = Align.center;
            lst.text = "Editor short cuts";

            addControl(lst.createInterface());

            // paprastas tekstas.
            lst.x = 20;
            lst.y = 0;
            lst.textSize = 30;
            lst.verticalAlign = Align.top;
            lst.horizontalAlign = Align.left;
            lst.width = 996;
            lst.height = 568;
            lst.shrinkText = false;

            lst.text = "SHIFT - hold it and drag resource from resource panel to editor field. It will place element with fixed position.\n" +
                    "CTRL - hold it and select elements for multi selection or deselection.\n" +
                    "SPACE - hold it and move field around.\n" +
                    "CTRL+C - copy selected elements.\n" +
                    "CTRL+V - paste copied elements.\n" +
                    "CTRL+Z - undo last move.\n" +
                    "CTRL+SHIFT+Z - redo last undo move.\n" +
                    "CTRL+A - opens additional panel.\n" +
                    "CTRL+S - saves project.\n" +
                    "TAB - opens form selection dialog.\n" +
                    "CTRL+X - hides main panel.\n" +
                    "CTRL+Q - opens chains panel.\n" +
                    "CTRL+SPACE - recenter camera and zooms to 100%.\n" +
                    "CTRL+F - opens prefix dialog.\n" +
                    "CTRL+J - opens joints panel.\n" +
                    "CTRL+E - focuses on selected element (if there are more than 1 element selected then focuses on first one).";

            addControl(lst.createInterface());
        }
    }
}
