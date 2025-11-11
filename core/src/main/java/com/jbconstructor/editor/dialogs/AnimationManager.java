package com.jbconstructor.editor.dialogs;

import com.badlogic.gdx.utils.Align;
import com.brashmonkey.spriter.Player;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.core.Resources;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.PopUp;
import com.engine.ui.controls.dialogs.ConfirmDialog;
import com.engine.ui.controls.views.PreparedListView;
import com.engine.ui.controls.widgets.CheckBox;
import com.engine.ui.controls.widgets.Label;
import com.engine.ui.controls.widgets.SImage;
import com.engine.ui.controls.widgets.SymbolButton;
import com.engine.ui.controls.widgets.TextBox;
import com.engine.ui.listeners.ClickListener;
import com.engine.ui.listeners.FocusListener;

public class AnimationManager extends PopUp {
    private SpriterDrawable inUse;

    private CheckBox animationPlay;
    private TextBox animationSpeed;
    private SImage placeToShow;
    private PreparedListView animationList;

    public AnimationManager() {
        super(852, 531);

        // labeliai
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.autoSize = false;
//        lst.horizontalAlign = Align.center;
        lst.verticalAlign = Align.center;
        lst.x = 47;
        lst.y = 423;
        lst.width = 573;
        lst.height = 87;
        lst.textSize = 56;
        lst.text = "Animation managing";

        addControl(new Label(lst));

        lst.horizontalAlign = Align.center;
        lst.x = 48;
        lst.y = 340;
        lst.width = 301;
        lst.height = 61;
        lst.textSize = 33;
        lst.text = "Available animations:";

        addControl(new Label(lst));

        lst.horizontalAlign = Align.right;
        lst.x = 394;
        lst.y = 308;
        lst.width = 240;
        lst.height = 47;
        lst.textSize = 33;
        lst.text = "Animation speed:";

        addControl(new Label(lst));

        // checkbox control
        CheckBox.CheckBoxStyle cst = new CheckBox.CheckBoxStyle();
//        cst.autoSize = false;
        cst.x = 621;
        cst.y = 373;
        cst.width = 181;
        cst.height = 62;
        cst.textSize = 33;
        cst.text = "Play animation";
        cst.box = Resources.getDrawable("defaultCheckBox");
        cst.checkedBox = Resources.getDrawable("defaultCheckBoxTicked");
        cst.horizontalAlign = Align.center;
        cst.verticalAlign = Align.center;

        CheckBox checkBox = new CheckBox(cst);
        checkBox.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                checkBoxTick(checked);
            }
        });
        addControl(checkBox);
        animationPlay = checkBox;

        // textbox.
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.autoSize = false;
        tst.background = Resources.getDrawable("whiteSystemColor");
        tst.verticalAlign = Align.center;
        tst.x = 640;
        tst.y = 305;
        tst.width = 188;
        tst.height = 50;
        tst.textSize = 51;
        tst.textOffsetX = 5;

        TextBox textBox = new TextBox(tst);
        textBox.setFocusListener(new FocusListener() {
            @Override
            public void onLostFocus(Control e) {
                textBoxTextChanged(animationSpeed.getText());
            }

            @Override
            public void onFocus(Control e) {}
        });
        addControl(textBox);
        animationSpeed = textBox;

        // animacijos vieta.
        SImage.SImageStyle sst = new SImage.SImageStyle();
        sst.keepRatio = false;
        sst.autoSize = false;
        sst.width = 358;
        sst.height = 240;
        sst.x = 474;
        sst.y = 24;
        sst.image = Resources.getDrawable("whiteSystemColor");

        SImage sImage = new SImage(sst);
        addControl(sImage);
        placeToShow = sImage;

        // mygtuks
        SymbolButton.SymbolButtonStyle ist = new SymbolButton.SymbolButtonStyle();
        ist.autoSize = false;
        ist.width = 60;
        ist.height = 60;
        ist.symbolWidth = 60;
        ist.symbolHeight = 60;
        ist.x = 379;
        ist.y = 36;
        ist.text = "";
        ist.originX = ist.width/2;
        ist.originY = ist.height/2;
        ist.symbol = Resources.getDrawable("additionalPanelDownListKey");
        ist.background = Resources.getDrawable("whiteSystemColor");
        ist.normalColor = 0x00000000;
        ist.onColor = 0xFFFF5500;
        ist.pressedColor = 0xFFAA5500;
        ist.rotatable = true;
        ist.angle = 90;

        SymbolButton symbolButton = new SymbolButton(ist);
        symbolButton.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                animationKeyPressed();
            }
        });
        addControl(symbolButton);

        // list view animacijoms
        PreparedListView.PreparedListViewStyle pst = new PreparedListView.PreparedListViewStyle();
        pst.autoSize = false;
        pst.x = 48;
        pst.y = 31;
        pst.width = 300;
        pst.height = 302;
        pst.itemWidth = pst.width;
        pst.itemHeight = 45;
        pst.separatorSize = 0;
        pst.activeItemsStyle.textSize = 45;
        pst.rowCount = 1;
//        pst.activeItemsStyle.height = 45;

        PreparedListView preparedListView = new PreparedListView(pst);
        addControl(preparedListView);

        animationList = preparedListView;
    }

    private void textBoxTextChanged(String text){
        if (inUse != null){
            try {
                float num = Float.parseFloat(text);
                inUse.setAnimationSpeed(num);
            }catch (NumberFormatException ex){
                animationSpeed.setText("ERROR!");
            }
        }else {
            noAnimation();
        }
    }

    private void checkBoxTick(boolean tick){
        if (inUse != null){
            inUse.enableUpdate(tick);
        }else {
            noAnimation();
        }
    }

    // kai paspaude mygtuka
    private void animationKeyPressed(){
        if (inUse != null){
            int index = animationList.getSelectedItemIndex();
            if (index >= 0){
                inUse.switchAnimations(index, 0.1f);// sitas ijungia update animacijos.
                // del sito reik pranest musu checkboxui
                animationPlay.setChecked(true); // tiesiog uzzimim.
            }
        }else {
            noAnimation();
        }
    }

    private void noAnimation(){
        close();
        ConfirmDialog e = new ConfirmDialog(ConfirmDialog.ConfirmDialogType.OK);
        e.show("Unexpected error has occurred!");
    }

    public void show(SpriterDrawable e){
        if (e != null){
            // paziurim ar leidziama play.
            animationPlay.setChecked(e.isUpdateEnabled());
            // koks greitis
            animationSpeed.setText(e.getAnimationSpeed() + "");
            // idedam vaizda
            placeToShow.setImage(e);
            // isrenkam visas animacijas
            Player player = e.getActivePlayer().getFirstPlayer();
            for (int a = 0; a < player.getEntity().animations(); a++){
                animationList.addItem(player.getEntity().getAnimation(a).name);
            }
            inUse = e;
            open();
        }
    }

    @Override
    protected void onClose() {
        super.onClose();
        inUse = null;
        animationList.clear(); // isvalom viska. kam ten reik.
    }
}
