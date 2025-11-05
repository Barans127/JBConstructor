package com.jbconstructor.main.forms.editorForm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.engine.core.Engine;
import com.engine.core.Resources;
import com.engine.interfaces.controls.Connected;
import com.engine.interfaces.controls.Interface;
import com.engine.interfaces.controls.InterfacesController;
import com.engine.interfaces.controls.PanelHost;
import com.engine.interfaces.controls.dialogs.PopMeniu;
import com.engine.interfaces.controls.views.ListView;
import com.engine.interfaces.controls.views.Panel;
import com.engine.interfaces.controls.widgets.CheckBox;
import com.engine.interfaces.controls.widgets.Label;
import com.engine.interfaces.controls.widgets.SymbolButton;
import com.engine.interfaces.controls.widgets.TextBox;
import com.engine.root.GdxPongy;
import com.jbconstructor.main.root.Element;
import com.jbconstructor.main.root.Resizer;
import com.jbconstructor.main.root.SelectiveInterfaceController;

import java.util.ArrayList;
import java.util.List;

/** Balloon which shows info about elements on edit form. It can filter them, select them. */
public class SearchBalloon extends PopMeniu {
    private EditForm editForm;
    private Resizer resizer;
    // mygtukas, kuris atidarys si balloon.
    private SymbolButton controlButton;
    private Drawable searchDrawable;

    // custom item kurimui.
    private Pool<ElementItem> itemPool;
    private SymbolButton.SymbolButtonStyle sst;

    // elementu laikymas.
    private Array<ElementItem> active; // absoliuciai visi elementai eiles tvarka (Eiles tvarka turi but teisinga).
    private Array<ElementItem> selected; // elementai matomi list view liste. Greitesniam trinimui is listview...

    // user interface dalys.
    private ListView listView;
    private CheckBox caseSensitive, selectedView;
    private Label itemCount, visibleCount;
    private TextBox searchBar;

    public SearchBalloon(EditForm editForm, SymbolButton controlButton){
        this.editForm = editForm;
        resizer = editForm.getSizer();
        // sugaudom musu atidarymo mygtuka.
        this.controlButton = controlButton;

        // toks balloon dydis bus.
        setSize(325, 600);
        setAnimatingTime(0.3f);
//        useAnimation(false);

        // kuriam array instancijas.
        active = new Array<>();
        selected = new Array<>();

        // susirandam panele i kuria mesim kontroles.
        Panel panel = getController();
        // truputi redaguojam.
        Drawable drawable = Resources.getDrawable("systemWhiteRect");
        if (drawable instanceof TextureRegionDrawable || drawable instanceof SpriteDrawable){
            drawable = Resources.createNinePatchDrawable("systemWhiteRect", 12, 12, 12, 12);
        }
        panel.setBackground(drawable);
        panel.tintBackground(0xffaaaaaa);

        // search laukas.
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.autoSize = false;
        tst.x = 8.5f;
        tst.y = 554;
        tst.width = 308;
        tst.height = 40;
        tst.verticalAlign = Align.center;
        tst.textOffsetX = 5;

        // search combobox.
        TextBox textBox = new TextBox(tst);
        textBox.setTextListener(new TextBox.TextChangedListener() {
            @Override
            public boolean textChanged(String old, String current, char a, TextBox owner) {
                filterUpdate(current);
                return false;
            }
        });
        panel.addControl(searchBar = textBox);

        // search mygtukas.
        SymbolButton.SymbolButtonStyle sbt = new SymbolButton.SymbolButtonStyle();
        sbt.autoSize = false;
        sbt.normalColor = 0x00ffffff;
        sbt.symbolWidth = 40;
        sbt.symbolHeight = 40;
        sbt.symbol = Resources.getDrawable("startFormSearchKey");
        searchDrawable = sbt.symbol; // mum dar jos prireiks.
        sbt.width = 40;
        sbt.height = 40;
        sbt.x = 274;
        sbt.y = 555;

        // pats mygtukas.
        SymbolButton symbolButton = new SymbolButton(sbt);
        symbolButton.setUnClickable(true); // tik del grozio...
        panel.addControl(symbolButton);

        // list view kuriam elementai bus.
        ListView.ListViewStyle lst = new ListView.ListViewStyle();
        lst.autoSize = false;
        lst.y = 37;
        lst.width = 325;
        lst.height = 432;
        lst.itemWidth = 324;
        lst.itemHeight = 50; // plius minus tiek..

        lst.separatorSize = 5;
        lst.offsetY = 10;
        lst.offsetEndY = 10;
        lst.rowCount = 1;

        // pats listView.
        ListView listView = new ListView(lst);
        panel.addControl(listView);
        this.listView = listView;

        // symbol button style.
        sst = new SymbolButton.SymbolButtonStyle();
//        sst.autoSize = false;
        sst.normalColor = 0x00ffffff;
        sst.symbolWidth = lst.itemHeight;
        sst.symbolHeight = lst.itemHeight;

        // pool.
        itemPool = new Pool<ElementItem>() {
            @Override
            protected ElementItem newObject() {
                return new ElementItem(sst);
            }
        };

        // case sensitive checkbox.
        CheckBox.CheckBoxStyle cst = new CheckBox.CheckBoxStyle();
        cst.x = 7;
        cst.y = 526;
        cst.text = "Case sensitive";
        cst.checked = true;
        cst.box = Resources.getDrawable("defaultCheckBox");
        cst.checkedBox = Resources.getDrawable("defaultCheckBoxTicked");
        cst.textSize = 30;

        CheckBox checkBox = new CheckBox(cst);
        checkBox.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                filterUpdate(searchBar.getText());
            }
        });
        panel.addControl(caseSensitive = checkBox);

        // only selected checkbox.
        cst.y = 498;
        cst.text = "Show only selected";
        cst.checked = false;

        CheckBox selected = new CheckBox(cst);
        selected.setCheckListener(new CheckBox.CheckedListener() {
            @Override
            public void onCheck(boolean checked) {
                filterUpdate(searchBar.getText()); // tiesiog taip update padaro.
            }
        });
        panel.addControl(selectedView = selected);

        // selected item count label.
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.autoSize = false;
        labelStyle.x = 7;
        labelStyle.y = 473;
        labelStyle.width = 312;
        labelStyle.height = 25;
        labelStyle.textSize = 30;
        labelStyle.text = "Selected items: 0";
        labelStyle.verticalAlign = Align.center;

        Label label = new Label(labelStyle);
        label.setUnClickable(true);
        panel.addControl(itemCount = label);

        // kiek isviso matoma daiktu.
        labelStyle.y = 7;
        labelStyle.text = "Visible items: 0";

        Label visible = new Label(labelStyle);
        visible.setUnClickable(true);
        panel.addControl(visibleCount = visible);

    }

    /* update. */

    private void filterUpdate(String text){
        if (text.equals("")){ // nera teksto. Rodom viska.
            setAllItemVisible(selectedView.isChecked() ? selected : active);
            return;
        }
        // tekstas yra. Filtruojam.
        // imam case sensitive.
        boolean caseSensitive = this.caseSensitive.isChecked();

        String filter;
        if (caseSensitive){
            filter = text; // nieko ypatingo. Imam toks koks yra.
        }else {
            filter = text.toLowerCase(); // darom lowerCase, kadangi ignorinsim didziasias raides.
        }

        // isvalom listView.
        listView.removeAllItems();
//        visibleElements.clear(); // isvalom matoma lista.

        // parenkam array is kurios imsim info.
        Array<ElementItem> array = selectedView.isChecked() ? selected : active;

        int count = 0;
        for (ElementItem e : array){
            // pagal case sensitive situacija darom veiksma..
            String id = caseSensitive ? e.item.getIdName() : e.item.getIdName().toLowerCase();

            if (id.startsWith(filter)){
//                visibleElements.add(e);
                listView.addControl(e);
                count++;
            }
        }

        visibleCount.setText("Items: " + count);
    }

    private void setAllItemVisible(Array<ElementItem> elementItems){
        // salinam viska.
        listView.removeAllItems();

        // pasirupinam, kad nereiktu kaskart array didint.
        // darom active dydzio.
        PanelHost host = listView.getHost();
        List<Interface> list = host.getControls();
        if (list instanceof ArrayList){
            ((ArrayList<Interface>) list).ensureCapacity(active.size);
        }

        int count = 0;
        // sumetam viska i list view.
        for (ElementItem item : elementItems){
            listView.addControl(item);
            count++;
        }

        visibleCount.setText("Items: " + count);

        listView.auto();
    }

    private void updateSelectedItems(){
        resizer.releaseAll();

        for (ElementItem e : selected){
            resizer.addEditableControl(e.item);
        }

        itemCount.setText("Selected items: " + selected.size);

        if (selected.size == 1){
            editForm.getMainPanel().focusControl();
        }

        editForm.checkControlInfo();

        // atnaujinam musu button.
        if (selected.size == 0 || selected.size > 1){
            itemSelected(null);
        }else {
            itemSelected(selected.get(0).item);
        }
    }

    @Override
    protected void onShow() {
        super.onShow();

        // tiesiog sumetam visus active elementus.
        // filtras vistiek isjungtas.
        searchBar.setText("");
        setAllItemVisible(active);

        selectedView.setChecked(false);

        // atnaujinam selected.
        listView.getHost().informGroup(-20200503, 0);

        selected.clear();

        boolean firstItem = true;
        for (Interface in : resizer.getControls()) {
            for (ElementItem e : active) {
                if (e.item == in){
                    e.select(); // pazymim.
                    if (firstItem){
                        // pats pirmasis elementas.
                        // reik nustumt offset iki jo.
                        Vector2 pos = e.getPosition();
                        Vector2 listViewPos = listView.getPosition();
                        float listViewHeight = listView.getHeight();
                        float itemHeight = e.getHeight();
                        float dif = pos.y - listViewPos.y - listViewHeight + itemHeight;

                        listView.setScrollOffset(0, dif);
                        // ka toliau nestumtu.
                        firstItem = false;
                    }
                    break; // jei radom toliau net neziurim.
                }
            }
        }

        itemCount.setText("Selected items: " + selected.size);
    }

    /* perdavimo linija. */

    /* tiesiog atnaujint matoma mygtuka. */
    void itemSelected(Interface e){
        if (e != null){
            // parenkam ko reik.
            if (e instanceof Element) {
                Element element = (Element) e;
                controlButton.setSymbol(element.getImage());
                controlButton.setSymbolTint(element.getImageTint());
            }
            controlButton.setText(e.getIdName());
        }else {
            // nieko ner.
            controlButton.setSymbol(searchDrawable);
            controlButton.setText("Search item");
            controlButton.setSymbolTint(0xffffffff);
        }
    }

    /** Updates element list. */
    public void remapView(){
        /* Sitam metode per naujo sudesim active listo item ir matomus item. */
        itemPool.freeAll(active);
        active.clear();
//        visibleElements.clear();
        selected.clear();

        // mes zinom, kad cia sitas controller.
        SelectiveInterfaceController controller = (SelectiveInterfaceController) editForm.getController();

        // einam per items.
        List<Interface> exceptionList = controller.getExceptionList();
        int index = 0;
        for (Interface e : controller.getControls()){
            if (exceptionList.contains(e)){
                continue;
            }

            if (e instanceof Element){
                // tai musu elementas.
                Element element = (Element) e;
//                element.setIdName("item" + index);
                ElementItem elementItem = itemPool.obtain();
                // nustatom id name, kad controlleris nekurtu nauju (tai kainuoja daug cpu laiko).
                elementItem.setIdName("item" + index); // jokio laukimo.
                elementItem.item = e;
//                elementItem.itemIndex = index;
                elementItem.setText(element.getIdName());
                elementItem.setSymbol(element.getImage());
                elementItem.setSymbolTint(element.getImageTint());
//                elementItem.customSymbolSize(30, 30);
                index++;

                // pridedam i active list. Vaizda kitur updatinsim.
                active.add(elementItem);
            }
        }
    }

//    /** Shows this item info on control button. */
//    public void elementSelected(List<Interface> list){
//        /* Sitam metode gali but null value, tai reisk atzymet viska.
//        * Listas kuriame pazymetos kontroles. Kitaip tariant resizer kontroles. */
//    }

    /* keiciam atsiradimo animacija. */

    @Override
    public boolean keyDown(int keycode) {
        // mes norim iskart uzdaryt sita. Darom taip, kad praleistu search bar focusa.
        if (Input.Keys.ESCAPE == keycode){
            hide();
            return true;
        }
        return super.keyDown(keycode);
    }

    @Override
    public boolean keyTyped(char e) {
        // kad iskartu pradetu rasyt terp search lauko.
        if (!searchBar.isFocused()){
            searchBar.getFocus();
        }
        return super.keyTyped(e);
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

    /* custom list view item. Sis item laikys info apie esama elementa. */

    private class ElementItem extends SymbolButton implements Connected, Pool.Poolable {
        private Interface item; // musu laikomas item.
//        private int itemIndex; // sio item index, greitesniam priejimui prie itemu.
        private boolean selected = false;

        ElementItem(SymbolButtonStyle st) {
            super(st);
        }

        private void select(){
            // toliau tiesiog pasizymim.
            setNormalColor(0xFFFF5500);
            Array<ElementItem> array = SearchBalloon.this.selected;
            if (!array.contains(this, true)) { // kad duplikatu nedet.
                array.add(this);
            }
            selected = true;
        }

        @Override
        public void setController(InterfacesController v) {
            super.setController(v);

            // pridedam connection.
            listView.getHost().addConnection(this);
        }

        @Override
        public void reset() {
            item = null;
//            itemIndex = -1;

            // salinam connection.
            listView.getHost().removeConnection(this);

            selected = false;
        }

        @Override
        protected void onClick() {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                if (selected){ // atsizymes.
                    setNormalColor(0x00ffffff);
                    selected = false;
                    SearchBalloon.this.selected.removeValue(this, true);

                    updateSelectedItems();
                    return;
                }
            } else {
                if (!selected ||  SearchBalloon.this.selected.size > 1) {
                    // cia atzymim kitus.
                    listView.getHost().inform(this, 0);
                    selected = false; // kad per naujo selectintu
                }
            }
            if (!selected) {
                select();
                updateSelectedItems(); // updatinam lista.
            }
        }

        @Override
        public void inform(int reason) {
            setNormalColor(0x00ffffff);
            selected = false;
            SearchBalloon.this.selected.removeValue(this, true);
        }

        @Override
        public int getGroup() {
            return -20200503;
        }
    }
}
