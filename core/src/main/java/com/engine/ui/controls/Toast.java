package com.engine.ui.controls;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.engine.core.Engine;
import com.engine.core.ErrorMenu;
import com.engine.core.MoreUtils;
import com.engine.core.Resources;
import com.engine.ui.controls.toastAnimation.ToastDropAnimation;
import com.engine.ui.controls.widgets.Label;
import com.engine.ui.controls.widgets.SymbolButton;
import com.engine.ui.listeners.MainDraw;

/** Show some information to a user for a limited amount of time. Like alerts, achievement unlocks etc. */
public class Toast implements MainDraw {
    /** default toast text style. Values can be changed in config file or directly here. */
    public static final ToastTextStyle TOAST_TEXT_STYLE = new ToastTextStyle();
    /** default toast symbol style. Values can be changed in config file or directly here. */
    public static final ToastSymbolStyle TOAST_SYMBOL_STYLE = new ToastSymbolStyle();

    // For easier access to time control.
    // Users might use it or not.
    public static final int SHORT = 4000;
    public static final int LONG = 8000;

    /* pagrindiniai */
    private ToastTextStyle currentToastTextStyle;
    private ToastSymbolStyle currentToastSymbolStyle;
//    private Panel host; // pagrindinis. Jame bus galima det ka nori. visa isvaizda.
    private final PanelHost host;
    private ToastAnimation animation;
    private int toastLifeTime;
    private int time;

    /* dydzio parametrai */
    private float vertical = 1f, horizontal = 0.5f; // procentai. toast pastatymas ekrane. Toast uz ekrano nelys.
//    private float width, height;

    private float offsetX = Resources.getPropertyFloat("ToastOffsetX", 0),
            offsetY = Resources.getPropertyFloat("ToastOffsetY", 0); // sitas leistu list uz ekrano. arba atvirksciai.

    /** Toast with <code>ToastDropAnimation</code> and default align values.
     * @param time Toast showing time in milliseconds. */
    public Toast(int time){
        this(time, new ToastDropAnimation(), Resources.getPropertyFloat("ToastAlignVertical", 1f),
                Resources.getPropertyFloat("ToastAlignHorizontal", 0.5f));
    }

    /** @param time Toast showing time in milliseconds.
     * @param animation which will be used to animate toast show.
     * @param vertical vertical align on screen. Value should be between 0-1
     * @param horizontal horizontal align on screen. Value should be between 0-1 */
    public Toast(int time, ToastAnimation animation, float vertical, float horizontal){
        host = new PanelHost(0, 0, 10, 10);
        if (time <= 0){
            Engine.getInstance().setError("Toast: time cannot be <= 0", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        if (animation == null){
            Engine.getInstance().setError("Toast: animation cannot be null", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        toastLifeTime = time;
        setToastAnimation(animation);
        this.vertical = MoreUtils.inBounds(vertical, 0, 1);
        this.horizontal = MoreUtils.inBounds(horizontal, 0, 1);
        // background
        Drawable e = Resources.getDrawable(Resources.getProperty("ToastDrawableKey", "defaultToastImg"));
        if (e == null){
            String path = Resources.getProperty("ToastDrawable", "resources/ui/tobula.png");
            if (MoreUtils.existFile(path)){
                Resources.loadTexture(Resources.getProperty("ToastDrawableKey", "defaultToastImg"), path);
                e = Resources.getDrawable(Resources.getProperty("ToastDrawableKey", "defaultToastImg")); // dabar turetu but.
            }else{
                e = Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor")); // nieko nerado tai desim balta spalva. pats kalts.
            }
        }
        setBackground(e);
    }

    /** Toast with default parameters. */
    public Toast(){
        this(Resources.getPropertyInt("ToastTime", 4000));
    }

    /* veikimas */

    void appear(){ // cia tarsi update. per sita pasakys, kad toast igavo vaizdo kontrole.
        animation.appear();
        time = Engine.getInstance().millis();
    }

    public void show(int time){
        setToastTime(time);
        show();
    }

    /** show toast. */
    public void show(){
        TopPainter.addToast(this); // tiesiog idedam i sarasa.
//        animation.appear();
//        time = Engine.getInstance().millis();
    }

    /** hide toast if it's visible */
    public void hide(){
        animation.disappear(); // pasakom animacija, kad laikas pasisalint.
    }

    /** completely removes toast from screen (no animation disappears immediately). */
    public void clearToast(){
        TopPainter.removeToast(this);
    }

    /** removes all controls from host. */
    public void clearHost(){
        for (int a = host.getControls().size()-1; a >= 0; a--){
            host.removeControl(host.getControls().get(a));
        }
    }

    /** Call this when you manipulated with host size. It uses host size to place it on screen by given align parameters. */
    public void calculatePosition(){
        Engine p = Engine.getInstance();
        float offsetX = p.getScreenOffsetX(), offsetY = p.getScreenOffsetY();
        float screenWidth = p.getScreenWidth();
        float screenHeight = p.getScreenHeight(); // sugaudom ekrano dydi.
        float x = screenWidth * horizontal - host.getWidth()/2 + offsetX; // nustatom kur x prasides.
        if (x < offsetX) // neleidziam islyst uz ekrano ribu.
            x = offsetX;
        else if (x + host.getWidth() > screenWidth + offsetX){
            x = screenWidth - host.getWidth() + offsetX;
        }
        x += this.offsetX; // pridedam offset. Jokios kontroles.
        float y = screenHeight * vertical - host.getHeight()/2 + offsetY;
        if (y < offsetY)
            y = offsetY;
        else if (y + host.getHeight() > screenHeight + offsetY)
            y = screenHeight - host.getHeight() + offsetY;
        y += this.offsetY; // jokios kontroles.
        host.setPosition(x, y);
    }

    /* ivedimas */

    /** Creates label by given style and place it on toast.
     * NOTE: text is not wrapped. If text size is bigger than screen size then label size will be shrunk (some text might become invisible).  */
    public void setText(Label.LabelStyle text){
        if (text == null){
            Engine.getInstance().setError("Toast: Labelstyle cannot be null", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        clearHost(); // isvalom viska kas buvo jame.
        Label e = text.createInterface();
        e.auto(); // sugaudom dydi.
        Engine p = Engine.getInstance();
        if (e.getWidth() > p.getScreenWidth()){ // sumazinam, kad tilptu i ekrana.
            e.setSize(p.getScreenWidth()*0.9f, e.getHeight());
        }
        if (e.getHeight() > p.getScreenHeight()){
            e.setSize(e.getWidth(), p.getScreenHeight()*0.95f);
        }
        float padx = 0, pady = 0;
        if (text instanceof ToastTextStyle){
            padx = ((ToastTextStyle) text).paddingX;
            pady = ((ToastTextStyle) text).paddingY;
        }
        host.setSize(e.getWidth() + padx, e.getHeight() + pady);
        e.setPosition(text.x + padx/2,  text.y + pady/2);
        host.addControl(e);
        calculatePosition();
    }

    /** Creates symbol button (symbol and text aligns together in that button). Place this button on toast. This button cannot be pressed as inputs doesn't
     * work on toast.
     * NOTE: text is not wrapped. If text size is bigger than screen size then label size will be shrunk (some text might become invisible).*/
    public void setSymbolText(SymbolButton.SymbolButtonStyle symbol){
        if (symbol == null){
            Engine.getInstance().setError("Toast: SymbolStyle cannot be null", ErrorMenu.ErrorType.WrongPara);
            return;
        }
        clearHost();
        SymbolButton e = symbol.createInterface();
        e.auto();
        Engine p = Engine.getInstance();
        if (e.getWidth() > p.getScreenWidth()){ // sumazinam, kad tilptu i ekrana.
            e.setSize(p.getScreenWidth()*0.9f, e.getHeight());
        }
        if (e.getHeight() > p.getScreenHeight()){
            e.setSize(e.getWidth(), p.getScreenHeight()*0.95f);
        }
        float padx = 0, pady = 0;
        if (symbol instanceof ToastSymbolStyle){
            padx = ((ToastSymbolStyle) symbol).paddingX;
            pady = ((ToastSymbolStyle) symbol).paddingY;
        }
        host.setSize(e.getWidth()+padx, e.getHeight() + pady);
        e.setPosition(symbol.x + padx/2,  symbol.y + pady/2);
        host.addControl(e);
        calculatePosition();
    }

    /** Creates label from default labelStyle template */
    public void setText(String text){
        ToastTextStyle TOAST_TEXT_STYLE = currentToastTextStyle == null ? Toast.TOAST_TEXT_STYLE : currentToastTextStyle;
        TOAST_TEXT_STYLE.text = text;
        if (TOAST_TEXT_STYLE.toastBackground != null){
            setBackground(TOAST_TEXT_STYLE.toastBackground);
            tintBackground(TOAST_TEXT_STYLE.toastTintBackground);
        }
        setText(TOAST_TEXT_STYLE);
    }

    /** Creates Symbol button from default SymbolButtonStyle template. */
    public void setSymbolText(Drawable symbol, String text){
        ToastSymbolStyle TOAST_SYMBOL_STYLE = currentToastSymbolStyle == null ? Toast.TOAST_SYMBOL_STYLE : currentToastSymbolStyle;
        TOAST_SYMBOL_STYLE.symbol = symbol;
        TOAST_SYMBOL_STYLE.text = text;
        if (TOAST_SYMBOL_STYLE.toastBackground != null){
            setBackground(TOAST_SYMBOL_STYLE.toastBackground);
            tintBackground(TOAST_SYMBOL_STYLE.toastTintBackground);
        }
        setSymbolText(TOAST_SYMBOL_STYLE);
    }

    /* parametru keitimass */

    /** set custom Toast style */
    public void setToastTextStyle(ToastTextStyle tst){
        currentToastTextStyle = tst;
    }

    /** set custom Toast style */
    public void setToastSymbolStyle(ToastSymbolStyle tst){
        currentToastSymbolStyle = tst;
    }

    /** Toast time in milliseconds. */
    public Toast setToastTime(int time){
        if (time > 0){
            toastLifeTime = time;
        }
        return this;
    }

    /** tints toast background if it has one. */
    public void tintBackground(int color){
        host.tintBackground(color);
    }

    /** This toast background. if null than toast will be with no background */
    public void setBackground(Drawable e){
        host.setBackground(e);
    }

    /** Animation for this toast. */
    public void setToastAnimation(ToastAnimation e){
        if (e == null){ // tiesiog ignoruojam null.
            return;
        }
        animation = e;
        animation.setToast(this); // nustatom, kad sitas bus owneris animacijos.
    }

    /** toast size. If you use {@link #setText(String)} or {@link #setSymbolText(Drawable, String)} than size is automatically set. */
    public void setSize(float width, float height){
        host.setSize(width, height);
        calculatePosition();
    }

    /** Toast offset. This can be used to move toast behind screen etc */
    public void setOffset(float offsetX, float offsetY){
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /** Align of this toast. Value should be between 0-1. */
    public void setToastAlign(float horizontal, float vertical){
        this.horizontal = MoreUtils.inBounds(horizontal, 0, 1);
        this.vertical = MoreUtils.inBounds(vertical, 0, 1);
        calculatePosition();
    }

    /** Vertical align of this toast. Value should be [0-1]. */
    public void setToastVerticalAlign(float vertical){
        this.vertical = MoreUtils.inBounds(vertical, 0, 1);
        calculatePosition();
    }

    /** Horizontal align of this toast. Value should be [0-1]. */
    public void setToastHorizontalAlign(float horizontal){
        this.horizontal = MoreUtils.inBounds(horizontal, 0, 1);
        calculatePosition();
    }

    /* parametru atidavimas */

    /** symbol type style of this toast. */
    public ToastSymbolStyle getToastSymbolStyle(){
        return currentToastSymbolStyle == null ? TOAST_SYMBOL_STYLE : currentToastSymbolStyle;
    }

    /** text type style of this toast. */
    public ToastTextStyle getToastTextStyle(){
        return currentToastTextStyle == null ? TOAST_TEXT_STYLE : currentToastTextStyle;
    }

    public int getToastLifeTime(){
        return toastLifeTime;
    }

    public ToastAnimation getAnimation(){
        return animation;
    }

    public float getHeight() {
        return host.getHeight();
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getWidth() {
        return host.getWidth();
    }

    public PanelHost getHost() {
        return host;
    }

    public float getVerticalPosition(){
        return vertical;
    }

    public float getHorizontalPosition() {
        return horizontal;
    }

    /* piesimas. */

    /** called before every drawing of toast. */
    protected void background(){
        animation.drawBackground();
    }

    /** called after all toast drawings. */
    protected void topDraw(){
        animation.drawOnTop();
    }

    @Override
    public final void draw() {
        // piesimas
        background();
        host.fixHandle();
        animation.update();
        topDraw();

        // skaiciavimai
        if (time + toastLifeTime < Engine.getInstance().millis()){ // atejo laikas, uzdarom.
            hide();
        }
    }

    @Override
    public boolean drop(int reason) { // nenaudojamas
        return false;
    } // toast dropo nebus.

    /* default klases */

    public static class ToastTextStyle extends Label.LabelStyle{
        /** default background for toast. It will be used with {@link #setText(String)} */
        public Drawable toastBackground;
        /** background tint if background is set. */
        public int toastTintBackground = 0xFFFFFFFF;
        /** This toast padding. When {@link #setText(String)}  is called this will be used to make toast size (this will be added to real size).*/
        public float paddingX = Resources.getPropertyFloat("ToastPaddingX", 0),
                paddingY = Resources.getPropertyFloat("ToastPaddingY", 0);

        public ToastTextStyle(){
            textSize = Resources.getPropertyFloat("ToastTextSize", 50f);
            horizontalAlign = Resources.getPropertyInt("ToastTextHorizontalAlign", Align.center);
            verticalAlign = Resources.getPropertyInt("ToastTextVerticalAlign", Align.center);
            textColor = MoreUtils.hexToInt(Resources.getProperty("ToastTextColor", "ff000000"));
        }
    }

    public static class ToastSymbolStyle extends SymbolButton.SymbolButtonStyle{
        /** default background for toast. It will be used with {@link #setSymbolText(Drawable, String)} */
        public Drawable toastBackground;
        /** background tint if background is set. */
        public int toastTintBackground = 0xFFFFFFFF;
        /** This toast padding. When {@link #setSymbolText(Drawable, String)}  is called this will be used to make toast size (it will be added to real size).*/
        public float paddingX = Resources.getPropertyFloat("ToastPaddingX", 0),
                paddingY = Resources.getPropertyFloat("ToastPaddingY", 0);

        public ToastSymbolStyle(){
            textSize = Resources.getPropertyFloat("ToastTextSize", 50f);
            horizontalAlign = Resources.getPropertyInt("ToastTextHorizontalAlign", Align.center);
            verticalAlign = Resources.getPropertyInt("ToastTextVerticalAlign", Align.center);
            background = Resources.getDrawable(Resources.getProperty("whiteColor", "whiteSystemColor")); // padarom permatoma tiesiog. Nemanau, kad jo reik isvis
            normalColor = 0x00FFFFFF;
            symbolWidth = textSize*2.5f;
            symbolHeight = textSize*2.5f;
            textColor = MoreUtils.hexToInt(Resources.getProperty("ToastTextColor", "ff000000"));
        }
    }
}
