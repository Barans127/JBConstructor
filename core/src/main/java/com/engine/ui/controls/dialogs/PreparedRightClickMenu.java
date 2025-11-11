package com.engine.ui.controls.dialogs;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.engine.core.ErrorMenu;
import com.engine.core.Resources;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.views.ListView;
import com.engine.ui.controls.widgets.Button;
import com.engine.ui.controls.widgets.SymbolButton;
import com.engine.ui.listeners.ClickListener;
import com.engine.root.GdxWrapper;

/** class which makes easy to create right click menu as drop down list menu.
 * You can add text or some image and text. */
public class PreparedRightClickMenu extends RightClickMenu {
    private int maxItemsToShow = 10;
    private SymbolButton.SymbolButtonStyle style;
    private MeniuItemClickedListener listener;

    public PreparedRightClickMenu(){
        this(new RightClickMenuStyle());
    }

    public PreparedRightClickMenu(ListView.ListViewStyle st){
        super(new ListView(st));
        style = new SymbolButton.SymbolButtonStyle();
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        style.background = Resources.getDrawable("whiteSystemColor");
        style.textSize = 40;
        style.normalColor = 0xFFFFFFFF;
        style.onColor = 0xFFFF5500;
        style.pressedColor = 0xFFCC5500;
        style.position = SymbolButton.TextPosition.RIGHT;
    }

    /* parametrai */

    public void setMaxShowableItems(int max){
        if (max <= 0){
            GdxWrapper.getInstance().setError("PreparedRightClickMenu: max showable items cannot be <= 0", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        maxItemsToShow = max;
    }

    public int getMaxShowableItems(){
        return maxItemsToShow;
    }

    /* listener */

    public void setMenuItemClickListener(MeniuItemClickedListener e){
        listener = e;
    }

    public MeniuItemClickedListener getListener() {
        return listener;
    }

    /* itemo style. */

    /** manipulate this before adding items to menu. */
    public SymbolButton.SymbolButtonStyle getStyle() {
        return style;
    }

    /* pridejimas i lista. */

    public void addListItem(String name){
        if (name == null)
            name = "null";
        style.text = name;
        Button e = new Button(style);
        addListItem(e);
    }

    public void addListItem(String name, Drawable e, int tintSymbol){
        if (name == null)
            name = "null";
        style.text = name;
        style.symbol = e;
        style.symbolTint = tintSymbol;
        SymbolButton b = new SymbolButton(style);
        addListItem(b);
    }

    private void addListItem(Button e){
        int count = getController().getHost().getControls().size(); // -1 nes pirma button ignoruot reik.
        if (count > maxItemsToShow)
            count = maxItemsToShow;
//        e.auto();
        setSize(getWidth(), ((ListView)getController()).getItemHeight()*count);
        getController().addControl(e);
        e.setClickListener(new ButtonClick(e));
    }

    /* papildomi */

    public interface MeniuItemClickedListener{
        /** @param owner interface on which right click occurred
         *  @param index pressed index of controls
         *  @param itemName name showed on list.*/
        void clickedItem(Control owner, int index, String itemName);
    }

    private class ButtonClick implements ClickListener{
        private Button own;

        public ButtonClick(Button owner){
            own = owner;
        }

        @Override
        public void onClick() {
            if (listener != null) {
                int count = 0;
                for (Control e : getController().getHost().getControls()) {
                    if (own == e){
                        listener.clickedItem(getRightClickedInterface(), count-1, own.getText()); // ignoruojam pirmaji.
                        break;
                    }
                    count++;
                }
            }
            PreparedRightClickMenu.this.hide(); // visada uzdarom po paspaudimo.
        }
    }
}
