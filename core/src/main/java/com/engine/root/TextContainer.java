package com.engine.root;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.engine.core.Engine;

import static com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import static com.engine.core.ErrorMenu.ErrorType;

/**
 * Draws text in specified rectangle area.
 */
public class TextContainer {
    private String realText;
    private float width, height; // teksto lauko dydis.
    private float textSize = 12; // teksto dydis
    private BitmapFont font; // stilius
    private GlyphLayout layout; // laikomas tekstas
    private Color textColor, forceColor, tmpColor; // spalva, forceColor leis keist spalva kartu su forceTint. tmp spalva sujunks abi kai reiks, kad nesimaisytu jos.
    private Engine p;
    private int horizontal, vertical; // text aligment
    private Array<GlyphLayout.GlyphRun> lines; // laikyt nerodomoms eilutems.
    private Array<Float> linesPosition;
    private int lineStartIndex, visibleLinesCount;
    private int fontDefaultSize;
    private float visibleTextHeight;
    private boolean updateRequired;
    private boolean textFit;

    public TextContainer(BitmapFont font){
        p = GdxPongy.getInstance();
        lines = new Array<>();
        linesPosition = new Array<>();
        if (font == null){
            p.setError("BitmapFont cannot be null", ErrorType.WrongPara);
            return;
        }
        this.font = font;
        checkFontSize();
        textColor = new Color(0, 0, 0, 1);
        forceColor = new Color(p.getForceColor());
        tmpColor = new Color();
        layout = new GlyphLayout();
        horizontal = Align.left;
        vertical = Align.top;
        realText = "";
    }

    public TextContainer(String text, float width, float height, float textSize, BitmapFont font){
        this(font);
        setText(text);
        setTextSize(textSize);
        setBounds(width, height);
    }

    public TextContainer(String text, float width, float height, float textSize){
        this(text, width, height, textSize, GdxPongy.getInstance().getFont());
    }

    public TextContainer(){
        this(GdxPongy.getInstance().getFont());
    }

    /** Pieš tekstą pagal nurodytus bounds. */
    public void drawText(float x, float y){ // tik pieš, nereiks kiekvieną kart apskaičiuot ar telpa tekstas.
//        if (!forceColor.equals(p.getForceColor())){ // reisk force spalva pakito.
//            forceColor.set(p.getForceColor());
//            updateRequired = true;
//        }
        if (p.hasForceColorChanged()){ // keis teksto spalva, kai force spalva buvo pakeista. Force spalva turi keistis tuo momentu kai tekstas yra matomas,
            // kitaip force spalva nepaveiks teksto spalvos.
            // it assumes that force tint is applied everytime before text drawing.
//            forceColor.set(p.getForceColor()); // force spalva pasiimsim tik per update. Kam dabar?
            if (!forceColor.equals(p.getForceColor()))
                updateRequired = true; // force spalva pasiims update.
        }

        if (updateRequired) {
            checkBounds();
            updateRequired = false;
        }

        /*bounds test*/
//        p.noFill();
//        p.stroke(255,0,0);
//        p.rect(x, y, width, height);

        font.getData().setScale(textSize/fontDefaultSize); // fonto scalinimas pagal nurodyta dydi
        if (vertical == Align.center) {
            y += height / 2 + visibleTextHeight / 2;
        } else if (vertical == Align.bottom) {
            y = y + visibleTextHeight;
        } else if (vertical == Align.top) {
            y += height;
        }
        p.text(layout, x, y, font);

        /* xAdvance test. */
//        p.strokeWeight(2f);
//        p.stroke(0,0,255);
//        p.noFill();
//        float dx = x + layout.runs.get(0).x + layout.runs.get(0).xAdvances.get(0);
//        p.line(dx, y,dx,  y - height);
    }

    /** nustatys naujas ribas, kuriuose talpinamas tekstas */
    public void setBounds(float width, float height){
        if (width < 0) {
            width = 0;
        }
        if (height < 0) {
            height = 0;
        }
        if (this.width == width && this.height == height){ // ribos nepasikeitė nieko keist nereik.
            return;
        }
        this.width = width;
        this.height = height;
        updateRequired = true;
    }

    public void setText(String text){
        if (text == null) {
            text = "null";
        }
//        text = text.trim();
        if (text.equals(realText)){
            return;
        }
        realText = text;
        updateRequired = true;
    }

    public void setTextSize(float size){
        if (size <= 0)
            size = 12;
        textSize = size;
        updateRequired = true;
    }

    public void setTextAlign(int horizontal, int vertical){
        this.horizontal = horizontal;
        this.vertical = vertical;
        updateRequired = true;
    }

    public void setTextColor(int color){
        if (color >= 0 && color <= 255){
            textColor.set(color/255, color/255, color/255, 1);
        }else
            Color.argb8888ToColor(this.textColor, color);
        updateRequired = true;
    }

    public void setTextColor(Color color){
        textColor.set(color);
        updateRequired = true;
    }

    public void setTextColor(float r, float g, float b, float a){
        textColor.set(r, g, b, a);
        updateRequired = true;
    }

    public void setStyle(BitmapFont font){
        if (font != null){
            this.font = font;
            checkFontSize();
            updateRequired = true;
        }
    }

    /** leis keist nematomą tekstą, tarsi scrollint. */
    public void startLine(int index){
        if (updateRequired){
            checkBounds();
            updateRequired = false;
        }
        if (index < 0 || index >= lines.size || lines.size <= 1){
            return;
        }
        if (index+visibleLinesCount > lines.size){
            index = lines.size-visibleLinesCount;
        }
//        int oldIndex = lineStartIndex;
        lineStartIndex = index;
        layout.runs.clear(); // išvalyt dabar esamas eilutes.
//        float diffrencef = font.getLineHeight();
        float diffrence = font.getLineHeight();
//        float y = 0;
        for (int a = 0; a < lines.size; a++){ // pozicija?
            GlyphRun e = lines.get(a);
            float y = linesPosition.get(a) - (diffrence * lineStartIndex);
            float capHeight = font.getCapHeight();
            if (!(-y+capHeight > height)){ // eilute tilpo.
                layout.runs.add(e);
            }
//            y += diffrence;
        }
    }

    /** Grąžins visą originalų tekstą. */
    public String getText(){
        return realText;
    }

    public GlyphLayout getLayout(){
        return layout;
    }

    public float getWidth(){
        return width;
    }

    /** Text container height. Height in which text is being fitted. */
    public float getHeight(){
        return height;
    }

    public float getTextWidth(){
        return layout.width;
    }

    public float getTextHeight(){
        return layout.height;
    }

    public BitmapFont getFont(){
        return font;
    }

    public BitmapFont getFontScaled(){
        return getFontScaled(textSize);
    }

    public BitmapFont getFontScaled(float size){
        font.getData().setScale(size/fontDefaultSize);
        return font;
    }

    public float getTextSize(){
        return textSize;
    }

    public Color getTextColor(){
        return textColor;
    }

    public int getHorizontalAlign(){
        return horizontal;
    }

    public int getVerticalAlign(){
        return vertical;
    }

    /** Eilučių skaičius. */
    public int getLinesCount(){
        return lines.size;
    }

    public int getVisibleLinesCount(){
        return visibleLinesCount;
    }

    public int getLineStartIndex(){
        return lineStartIndex;
    }

    /** @return true if text fitted in this block. false if text was cut. */
    public boolean isTextFit(){
        return textFit;
    }

    public void clear(){
        lines.clear();
        linesPosition.clear();
        layout.reset();
        lineStartIndex = 0;
        visibleLinesCount = 0;
        visibleTextHeight = 0;
        realText = "";
        updateRequired = false;
    }

    @Override
    public String toString() {
        return realText;
    }

    private void checkFontSize(){
        font.getData().setScale(1f);
//        fontDefaultSize = MathUtils.round(font.getCapHeight() * 1.4848f); // turetu padaryt default dydį.
        fontDefaultSize = (int) font.getLineHeight();
    }

    public boolean isBoundsUpdated(){
        return !updateRequired;
    }

    public void updateBounds(){
        checkBounds();
        updateRequired = false;
    }

    private void checkBounds(){
        // šitas turės apskaičiuot ar telpa tekstas ir kiek jo nukirpt jei netelpa.
        // textSize ir text height santykis apie 1.49f textSize/textHeight
        lines.clear();
        linesPosition.clear();
        layout.reset();
        font.getData().setScale(textSize/fontDefaultSize); // kad būtų reikiamas dydis.
//        if (width <= font.getCapHeight() || height <= font.getSpaceWidth()){ // jei net raide netelpa, tai nieko nerodys.
        if (width <= font.getCapHeight() || height <= font.getSpaceXadvance()){ // jei net raide netelpa, tai nieko nerodys.
            layout.setText(font, "");
            lineStartIndex = 0;
            visibleTextHeight = 0;
            visibleLinesCount = 0;
            textFit = false;
            return;
        }
        tmpColor.set(textColor); // nustatom tikraja teksto spalva, o tada dauginsim su force spalva.
        forceColor.set(p.getForceColor()); // atnaujinam force spalva pries daran update.
//        layout.reset();
        layout.setText(font, realText, tmpColor.mul(forceColor), width, horizontal, true); // įdės originalų tekstą.
//        layout.setText(font, realText);
        for (int a = 0; a < layout.runs.size; a++){
            GlyphRun e = layout.runs.get(a);
            this.lines.add(e);
            linesPosition.add(e.y);
        }
        if (layout.height > height){ // tekstas netilpo.
            int visibleLines = 0;
            float capHeight = font.getCapHeight();
            for (int a = layout.runs.size-1; a >= 0; a--){
                GlyphRun e = layout.runs.get(a);
                if (-e.y+capHeight > height){ // eilutė netilpo.
                    layout.runs.removeIndex(a); // išims nematomas eilutes, kad jų nerodytų. textHeight paliks toks pat.
//                    layout.height -= capHeight;
                }else{
//                    if (a != 0)
                    visibleLines++;
                }
            }
            visibleTextHeight = capHeight + (visibleLines-1) * font.getLineHeight(); // capHeight skaitos kaip line.
            visibleLinesCount = visibleLines;
//            layout.height = visibleTextHeight;
            textFit = false;
        }else{
            visibleTextHeight = layout.height;
            visibleLinesCount = lines.size;
            textFit = true;
        }
        lineStartIndex = 0;
    }
}
