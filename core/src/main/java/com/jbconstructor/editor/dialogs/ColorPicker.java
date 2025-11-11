package com.jbconstructor.editor.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.engine.animations.spriter.SpriterDrawable;
import com.engine.core.ErrorMenu;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.Control;
import com.engine.ui.controls.PopUp;
import com.engine.ui.controls.widgets.Button;
import com.engine.ui.controls.widgets.Label;
import com.engine.ui.controls.widgets.SImage;
import com.engine.ui.controls.widgets.ScrollBar;
import com.engine.ui.controls.widgets.TextBox;
import com.engine.ui.listeners.ClickListener;
import com.engine.ui.listeners.FocusListener;
import com.engine.ui.listeners.OnlyNumbers;
import com.engine.root.GdxWrapper;

public class ColorPicker extends PopUp {
    private Pixmap pickerMap; // sitas leidzia gaut norima spalva is image.
    private Color colorPicker; // spalva
    private SImage colorShow; // laukas kuris rodo kaip spalva atrodo
    private Drawable white; // kad butu galima keist image

    // argb
    private int firstColor; // kai ijungia

    private TextBox[] colors; // teksto laukai
    private ScrollBar[] bars; // scrolai

    private DraggableSImage image;

    private ColorPickerListener list; // listener

    public ColorPicker(){
        super(GdxWrapper.getInstance().getScreenWidth()*0.7f, GdxWrapper.getInstance().getScreenHeight()*0.83f);
//        Resources.addImage("colorDialogPicker", "resources/ui/colorPicker.png");
        Drawable picker = Resources.getDrawable("defaultColorPickerSelection"); // pagrindinis image
        if (picker == null){
            GdxWrapper.getInstance().setError("Color picker: Default drawable was not found.", ErrorMenu.ErrorType.ControlsError);
            return;
        }
//        Texture text = Resources.getTexture("colorDialogPicker"); // image, pixmapui
//        text.getTextureData().prepare();
//        pickerMap = text.getTextureData().consumePixmap(); // pixmap, kad butu galima gaut reikiama pixelio spalva

        GdxWrapper.addDisposable(pickerMap);
        SImage.SImageStyle st = new SImage.SImageStyle();
        st.showContour = false;
        st.image = picker;
        st.autoSize = false;
        st.width = st.height = getWidth()*0.4f;
        changeSelectionImage(picker);
        DraggableSImage e = new DraggableSImage(st); // dragint per pixmap
        e.placeInMiddle(st.width/1.7f, getHeight()-st.width/1.7f);
        addControl(image = e);
        colorPicker = new Color();
        // labels
        float jump = st.width*0.2f;
        Label.LabelStyle lst = new Label.LabelStyle();
        lst.textSize = st.width*0.14f;
        lst.x = st.width + st.width/4;
        lst.y = getHeight() - getHeight()*0.2f;
        Label r = new Label("R:", lst); // r
        addControl(r);
        lst.y -= jump;
        Label g = new Label("G:", lst); // g
        addControl(g);
        lst.y -= jump;
        Label b = new Label("B:", lst); // b
        addControl(b);
        lst.y -= jump;
        Label a = new Label("A:", lst); // a
        addControl(a);
        lst.y -= jump*1.5f;
        lst.x -= lst.textSize/2;
        Label hex = new Label("HEX:", lst); // hex
        addControl(hex);

        // textBoxes
        FocusListener foc = new FocusListener() {
            @Override
            public void onLostFocus(Control e) {
                if (e == colors[4]){
                    hexTextChanged();
                }else {
                    changedText();
                }
            }

            @Override
            public void onFocus(Control e) { }
        };
        colors = new TextBox[5];
        OnlyNumbers list = new OnlyNumbers();
        list.setLowestNumber(0);
        list.setMaxNumber(255);
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        TextBox.TextBoxStyle tst = new TextBox.TextBoxStyle();
        tst.verticalAlign = Align.center;
        tst.background = Resources.getDrawable("whiteSystemColor");
        white = tst.background;
        tst.textSize = lst.textSize;
        tst.autoSize = false;
        tst.height = tst.textSize;
        tst.width = 100;
        tst.maxLength = 3;
        r.updateTextBounds();
        tst.x = st.width + st.width/4 + lst.textSize*2;
        tst.y = getHeight() - getHeight()*0.2f;
        TextBox tr = new TextBox(tst); // r textBox
        tr.setFocusListener(foc);
        tr.setTextListener(list);
        addControl(colors[0] = tr);
        tst.y -= jump;
        TextBox tg = new TextBox(tst); // g textBox
        tg.setTextListener(list);
        tg.setFocusListener(foc);
        addControl(colors[1] = tg);
        tst.y -= jump;
        TextBox tb = new TextBox(tst); // b textBox
        tb.setTextListener(list);
        tb.setFocusListener(foc);
        addControl(colors[2] = tb);
        tst.y -= jump;
        TextBox ta = new TextBox(tst);// a textBox
        ta.setTextListener(list);
        ta.setFocusListener(foc);
        addControl(colors[3] = ta);
        tst.y -= jump*1.5f;
        tst.maxLength = 8;
        tst.width = tst.width*3f;
        tst.horizontalAlign = Align.center;
        TextBox thex = new TextBox(tst); // hex textBox
        thex.setFocusListener(foc);
        addControl(colors[4] = thex);

        // tas kuris keis spalva.
        st.image = tst.background;
        st.height = st.width/4;
        st.width = st.width/2;

        SImage spalva = new SImage(st);
        spalva.placeInMiddle(getWidth()*0.4f/1.7f, st.width/1.7f);
        addControl(colorShow = spalva);

        // mygtukai
        Button.ButtonStyle bst = new Button.ButtonStyle();
        bst.background = tst.background;
        bst.normalColor = 0xFF0000FF;
        bst.onColor = 0xFFFF0000;
        bst.pressedColor = 0xFFAA0000;
        bst.textSize = tst.textSize/1.5f;
        bst.autoSize = false;
        bst.width = tst.width/1.9f;
        bst.height = bst.textSize;
        Button ok = new Button("OK", bst);
        ok.placeInMiddle(getWidth() - bst.width/1.7f, bst.height);
        ok.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                okClick();
            }
        });
        addControl(ok);
        Button cancel = new Button("Cancel", bst);
        cancel.placeInMiddle(getWidth() - bst.width/1.7f * 3f, bst.height);
        cancel.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                cancelClick();
            }
        });
        addControl(cancel);
        Button restore = new Button("Restore", bst);
        restore.placeInMiddle(getWidth() - bst.width/1.7f *5f, bst.height);
        restore.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                restoreClick();
            }
        });
        addControl(restore);

        // scroll bars
        bars = new ScrollBar[4];
        ScrollBar.ScrollBarStyle sst = new ScrollBar.ScrollBarStyle();
//        Resources.addImage("scrollBarLine", "resources/ui/scrollbar-1ff.png");
        sst.background = Resources.getDrawable("defaultScrollBarLine");
        sst.scrollBar = Resources.getDrawable("defaultPopUpBackground");
        sst.normalColor = 0xFFAAAAAA;
        sst.onColor = 0xFFFF0000;
        sst.pressedColor = 0xFF0000FF;
        sst.tintBackground = 0xFF111111;
        sst.jumps = 256;
        sst.width = getWidth()/4;
        sst.height = tst.textSize;
        sst.x = tst.x + 120;
        sst.y = getHeight() - getHeight()*0.2f;
        sst.barSize = sst.width/8;
        sst.fixedStop = false;
        sst.smooth = true;
        ScrollBar rbar = new ScrollBar(sst);
        rbar.setScrollListener(new ScrollBar.ScrollListener() {
            @Override
            public void onScroll(int currentValue) {
                colors[0].setText(currentValue+"");
                colorPicker.r = currentValue/255f;
                setHex();
            }
        });
        addControl(bars[0] = rbar);
        sst.y -= jump;
        ScrollBar gbar = new ScrollBar(sst);
        gbar.setScrollListener(new ScrollBar.ScrollListener() {
            @Override
            public void onScroll(int currentValue) {
                colors[1].setText(currentValue+"");
                colorPicker.g = currentValue/255f;
                setHex();
            }
        });
        addControl(bars[1] = gbar);
        sst.y -= jump;
        ScrollBar bbar = new ScrollBar(sst);
        bbar.setScrollListener(new ScrollBar.ScrollListener() {
            @Override
            public void onScroll(int currentValue) {
                colors[2].setText(currentValue+"");
                colorPicker.b = currentValue/255f;
                setHex();
            }
        });
        addControl(bars[2] = bbar);
        sst.y -= jump;
        ScrollBar abar = new ScrollBar(sst);
        abar.setScrollListener(new ScrollBar.ScrollListener() {
            @Override
            public void onScroll(int currentValue) {
                colors[3].setText(currentValue+"");
                colorPicker.a = currentValue/255f;
                setHex();
            }
        });
        addControl(bars[3] = abar);
    }

    public void setColorPickerListener(ColorPickerListener e){
        list = e;
    }

    private void changedText(){ // r g b a textboxam
        int r, g, b, a;
        try{
            r = Integer.parseInt(colors[0].getText());
            g = Integer.parseInt(colors[1].getText());
            b = Integer.parseInt(colors[2].getText());
            a = Integer.parseInt(colors[3].getText());
        }catch (NumberFormatException n){
            r = g = b = a = 0;
        }
        bars[0].setValue(r);
        bars[1].setValue(g);
        bars[2].setValue(b);
        bars[3].setValue(a);
        colorPicker.set(r/255f, g/255f, b/255f, a/255f);
        setHex();
    }

    private void hexTextChanged(){ // hex textboxui
        String hex = colors[4].getText();
        hex = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        int a, r, g, b;
        try {
            a = Integer.valueOf(hex.substring(0, 2), 16);
            r = Integer.valueOf(hex.substring(2, 4), 16);
            g = Integer.valueOf(hex.substring(4, 6), 16);
            b = hex.length() != 8 ? 255 : Integer.valueOf(hex.substring(6, 8), 16);
        }catch (NumberFormatException f){
            colors[4].setText("ERROR!");
            return;
        }
        colorPicker.set(r/255f, g/255f, b/255f, a/255f);
        colors[0].setText((int) (colorPicker.r * 255) + "");
        colors[1].setText((int) (colorPicker.g * 255) + "");
        colors[2].setText((int) (colorPicker.b * 255) + "");
        colors[3].setText((int) (colorPicker.a * 255) + "");
        int color = Color.argb8888(colorPicker);
        colorShow.setColors(color, color, color);
    }

    private void setHex(){ // nustato hex teksto lauko teksta
        int color = Color.argb8888(colorPicker);
        colorShow.setColors(color, color, color);
        String hex = Integer.toHexString(
                (int) (colorPicker.a * 255) << 24 | (int) (colorPicker.r * 255) << 16 |
                        (int) (colorPicker.g *255) << 8 | (int) (colorPicker.b * 255));
        while (hex.length() < 8)
            hex = "0" + hex;
        colors[4].setText(hex);
    }

    private void okClick(){
        if (list != null)
            list.setColor(colorPicker);
        close();
    }

    private void cancelClick(){
        if (list != null)
            list.cancel();
        close();
    }

    private void restoreClick(){
        Color.argb8888ToColor(colorPicker, firstColor);
        colors[0].setText((int) (colorPicker.r * 255) + "");
        colors[1].setText((int) (colorPicker.g * 255) + "");
        colors[2].setText((int) (colorPicker.b * 255) + "");
        colors[3].setText((int) (colorPicker.a * 255) + "");
        bars[0].setValue((int) (colorPicker.r * 255));
        bars[1].setValue((int) (colorPicker.g * 255));
        bars[2].setValue((int) (colorPicker.b * 255));
        bars[3].setValue((int) (colorPicker.a * 255));
        setHex();
    }

    /** ARGB format */
    public void show(int color){
        firstColor = color;
        colorShow.setImage(white);
        restoreClick();
        open();
    }

    public void show(int color, Drawable e){
        firstColor = color;
        if (e == null)
            colorShow.setImage(white);
        else
            colorShow.setImage(e);
        restoreClick();
        open();
    }

    public Drawable getSelectionImage(){
        return image.getImage();
    }

//    public void setScrollBarsDrawables(Drawable scrollBar, Drawable scrollStick){
//        for (ScrollBar e : bars){
//            e.set
//        }
//    }

    /** Image which is used to drag on it and select color from it. SpriterAnimationDrawable cannot be used here!*/
    public void changeSelectionImage(Drawable e){ // gal kada, kai prireiks.
        if (e != null){
            if (pickerMap != null) {
                GdxWrapper.removeDisposable(pickerMap, true);
                pickerMap = null;
            }
            if (e instanceof SpriterDrawable) {
                p.setError("ColorPicker: Drawable for selection cannot be spriter animation.", ErrorMenu.ErrorType.WrongPara);
                return;
            }else if (e instanceof SpriteDrawable){ // nieko ypatingo tiesiog paimam textura.
                Sprite a = ((SpriteDrawable) e).getSprite();
                if (a instanceof TextureAtlas.AtlasSprite){
                    createPixMap(null, (TextureAtlas.AtlasSprite) a);
                }else {
                    Texture te = a.getTexture();
                    te.getTextureData().prepare();
                    pickerMap = te.getTextureData().consumePixmap();
                }
            }else if (e instanceof TextureRegionDrawable){
                TextureRegion reg = ((TextureRegionDrawable) e).getRegion();
                if (reg instanceof TextureAtlas.AtlasRegion) {
                    createPixMap((TextureAtlas.AtlasRegion) reg, null); // sukuriam pixmap is atlaso.
                }else { // nieko ypatingo.
                    Texture te = reg.getTexture();
                    te.getTextureData().prepare();
                    pickerMap = te.getTextureData().consumePixmap();
                }
            }
            if (image != null)
                image.setImage(e); // nustatom nauja image.
            GdxWrapper.addDisposable(pickerMap);
        }
    }

    private void createPixMap(TextureAtlas.AtlasRegion e, TextureAtlas.AtlasSprite e1){
        Texture whole;
        TextureAtlas.AtlasRegion region;
        if (e != null)
            region = e;
        else
            region = e1.getAtlasRegion();

        whole = region.getTexture();
        whole.getTextureData().prepare();
        Pixmap visas = whole.getTextureData().consumePixmap(); // visa texture.

        pickerMap = new Pixmap(region.getRegionWidth(), region.getRegionHeight(), Pixmap.Format.RGBA8888);
        for (int x = 0; x < region.getRegionWidth(); x++) { // pernarsom visus pixel.
            for (int y = 0; y < region.getRegionHeight(); y++) {
                int colorInt = visas.getPixel(region.getRegionX() + x, region.getRegionY() + y);
                // you could now draw that color at (x, y) of another pixmap of the size (regionWidth, regionHeight)
                pickerMap.drawPixel(x, y, colorInt);
            }
        }
        visas.dispose(); // sito tai jau nebereik.
    }

    public interface ColorPickerListener{
        void setColor(Color color);
        void cancel();
    }

    private class DraggableSImage extends SImage{
        private Drawable cross;
        private float cx, cy, mx, my, length; // kryziui
        private float size;

        private float ratioX, ratioY;

        DraggableSImage(SImageStyle style) {
            super(style);
//            Resources.addImage("colorDialogCross", "resources/ui/colorCross.png");
            cross = Resources.getDrawable("defaultColorPickCross");
            calculateSize();
        }

        private void calculateSize(){
            Vector2 pos = getPosition();
            cx = pos.x;
            cy = pos.y;
            mx = cx + getWidth()/2;
            my = cy + getHeight()/2;
            size = 32;
            length = getWidth()/2;
            ratioX = pickerMap.getWidth()/getWidth();
            ratioY = pickerMap.getHeight()/getHeight();
        }

        @Override
        protected void giveCords(float x, float y) {
            super.giveCords(x, y);
            calculateSize();
        }

        @Override
        protected void sizeUpdated() {
            super.sizeUpdated();
            calculateSize();
        }

        @Override
        protected void isvaizda(float x, float y) {
            super.isvaizda(x, y);
            cross.draw(p.getBatch(), cx, cy, size, size);
        }

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            if (super.touchDown(x, y, pointer, button)) {
                calcPoint(x, y);
                return true;
            }
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (super.pan(x, y, deltaX, deltaY))
                return true;
            if (isPressed()) {
                calcPoint(x, y);
                return true;
            }
            return false;
        }

        private void calcPoint(float x, float y){
            if (MoreUtils.dist(x, y, mx, my) > length){
                float angle = MathUtils.atan2(y - my, x - mx);
                cx = MathUtils.cos(angle) * (length - 5) + mx - size/2;
                cy = MathUtils.sin(angle) * (length - 5) + my - size/2;
            }else {
                cx = x - size/2;
                cy = y - size/2;
            }
            Vector2 pos = getPosition();
            int pixelrgba = pickerMap.getPixel( (int) (ratioX*(cx - pos.x + size/2)), pickerMap.getHeight() - (int) (ratioY*(cy - pos.y + size/2)));
            Color.rgba8888ToColor(colorPicker, pixelrgba);
            colors[0].setText((int) (colorPicker.r * 255) + "");
            colors[1].setText((int) (colorPicker.g * 255) + "");
            colors[2].setText((int) (colorPicker.b * 255) + "");
            colors[3].setText((int) (colorPicker.a * 255) + "");
            bars[0].setValue((int) (colorPicker.r * 255));
            bars[1].setValue((int) (colorPicker.g * 255));
            bars[2].setValue((int) (colorPicker.b * 255));
            bars[3].setValue((int) (colorPicker.a * 255));
            setHex();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        GdxWrapper.removeDisposable(pickerMap, true);
//        pickerMap.dispose();
    }
}
