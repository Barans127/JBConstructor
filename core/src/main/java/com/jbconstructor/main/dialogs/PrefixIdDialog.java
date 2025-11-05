package com.jbconstructor.main.dialogs;

import com.badlogic.gdx.utils.Align;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.PopUp;
import com.engine.interfaces.controls.dialogs.InputTextDialog;
import com.engine.interfaces.controls.views.PreparedListView;
import com.engine.interfaces.controls.widgets.Button;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.listeners.ClickListener;
import com.jbconstructor.main.forms.editorForm.EditForm;

public class PrefixIdDialog extends PopUp {
    private EditForm owner;
//    private Array<String> prevPrefix;

    private Label currentPrefix;
    private PreparedListView preparedListView;

    PrefixIdDialog(EditForm e) {
        super(640, 400);

        owner = e;
//        prevPrefix = new Array<>();

        // Labels
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.textSize = 40;
        lst.text = "Current id prefix: not set";
        lst.autoSize = false;
        lst.width = 440;
        lst.height = lst.textSize * 2;
        lst.x = 18;
        lst.y = 300;
        // prefix aprasyms
        currentPrefix = new Label(lst);
        addControl(currentPrefix);

        // previous label
        lst.textSize = 45;
        lst.x = 18;
        lst.y = 250;
        lst.width = 415;
        lst.height = lst.textSize;
        lst.text = "Previous used:";
        // label itself
        addControl(lst.createInterface());

        //buttons.
        Button.ButtonStyle bst = new Button.ButtonStyle();
        bst.textSize = 45;
        bst.autoSize = false;
        bst.background = Resources.getDrawable("whiteSystemColor");
        bst.normalColor = 0xff0000ff;
        bst.text = "SET";
        bst.x = 520;
        bst.y = 335;
        bst.width = 90;
        bst.height = bst.textSize;
        // set mygtukas
        Button set = new Button(bst);
        set.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                setClick();
            }
        });
        addControl(set);

        // prefix clear mygtukas.
        bst.text = "UNSET";
        bst.y += -55;

        // clear mygtukas
        Button prefixClear = new Button(bst);
        prefixClear.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                clearPrefix();
            }
        });
        addControl(prefixClear);

        // use mygtukas
        bst.x = 530;
        bst.y = 20;
        bst.textSize = 45;
        bst.width = 100;
        bst.height = bst.textSize;
        bst.text = "USE";
        Button use = new Button(bst);
        use.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                useClick();
            }
        });
        addControl(use);

        // mygtukai listo valdymui.
        // remove mygtukas
        bst.x = 510;
        bst.width = 120;
        bst.text = "REMOVE";
        bst.y = 105; // 20y + 35height + 60tarpas
        Button remove = new Button(bst);
        remove.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                removeClick();
            }
        });
        addControl(remove);

        // clear mygtukas
        bst.text = "CLEAR";
        bst.y = bst.y + 50; // 85y + 35height + 15tarpas
        Button clear = new Button(bst);
        clear.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                clearClick();
            }
        });
        addControl(clear);

        // listview su previous used.
        PreparedListView.PreparedListViewStyle pst = new PreparedListView.PreparedListViewStyle();
        pst.rowCount = 1;
        pst.offsetX = 0;
        pst.offsetEndX = 0;
        pst.offsetY = 0;
        pst.offsetEndY = 0;
        pst.separatorSize = 0;
        pst.autoSize = false;
        pst.width = 480;
        pst.height = 235;
        pst.x = 18;
        pst.y = 15;
        pst.itemWidth = pst.width;
        pst.activeItemsStyle.textSize = 40;
        pst.activeItemsStyle.horizontalAlign = Align.left;
        pst.activeItemsStyle.normalColor = 0x00ffffff;
        pst.normalState = 0x00ffffff;
        pst.itemHeight = 40;

        preparedListView = new PreparedListView(pst);
//        preparedListView.setActiveItemClickListener(new PreparedListView.ActiveItemClickListener() {
//            @Override
//            public void onActiveItemClick(Interface activeItem, int index) {
//
//            }
//        });
        addControl(preparedListView);

//        for (int a = 0; a < 10; a++) {
//            preparedListView.addItem("item" + a);
//        }
    }

    private void clearPrefix(){ // istrinam prefix.
        owner.setPrefixId(null);
        updatePrefixName();
    }

    private void setClick() {
        InputTextDialog inputTextDialog = new InputTextDialog("Enter prefix");
        String prefix = owner.getPrefixId();
        inputTextDialog.getInput().setText(prefix == null ? "" : prefix);
        inputTextDialog.setInputDialogListener(new InputTextDialog.InputDialogListener() {
            @Override
            public void onInput(String input) {
                if (input.equals(owner.getPrefixId())){
                    return;
                }
                owner.setPrefixId(input);
                updatePrefixName();
                if (input.length() > 0){
                    // patikrinam ar toks jau yra.
                    for (Interface e : preparedListView.getHost().getControls()){
                        if (e instanceof Button){
                            if (((Button) e).getText().length() > 0){ // yra close mygtukas, ji ignoruojam.
                                if (((Button) e).getText().equals(input)){
                                    return;
                                }
                            }
                        }
                    }
                    preparedListView.addItem(input);
                }
            }

            @Override
            public void cancel() {}
        });
        inputTextDialog.open();
    }

    private void useClick() {
        Button e = preparedListView.getSelectedItem();
        if (e != null){
            owner.setPrefixId(e.getText());
            updatePrefixName();
        }
    }

    // remove button click method.
    private void removeClick(){
        Button e = preparedListView.getSelectedItem();
        if (e != null){
            preparedListView.removeControl(e);
        }
    }

    // clear button click method.
    private void clearClick(){
        preparedListView.clear();
    }

    private void updatePrefixName(){
        currentPrefix.setText("Current id prefix: " + (owner.getPrefixId() == null ? "not set" : owner.getPrefixId()));
    }
}
