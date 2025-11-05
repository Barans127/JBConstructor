package com.jbconstructor.main.root;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.engine.animations.spriter.SpriterDrawable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * load resources from default resources folder.
 */

public class FileLoader {
    private Array<TextureAtlas> atlases;
    private Array<String> textures, atlasesPaths, animationAtlas;
    private Array<SpriterDrawable> animations;

    private String error;

    public FileLoader(){
        atlases = new Array<>();
        textures = new Array<>();
        atlasesPaths = new Array<>();
        animations = new Array<>();

        // nustatyt kuris atlas skirtas spriterDrawable ir jo nedet kaip atlas failo. (galima ir atskirai uzkraut jei tikrai zmogui reik).
        animationAtlas = new Array<>();
    }

    public void loadResources(String pathFolder){
        Array<String> textureList, atlasList;
        textureList = new Array<>();
        atlasList = new Array<>();
        loadFiles(Gdx.files.absolute(pathFolder), textureList, atlasList);
        MAIN:
        for (String e : atlasList){ // texture atlas uzkrovimas
            // paziurim gal tai spirter failas.
            String smallE = e.toLowerCase(); // kad nereiktu kas kart mazint.
            if (smallE.endsWith(".txt") || smallE.endsWith(".atlas")) { // atlas file visada buna txt
                try { // gal atlas file.
                    for (String path : atlasesPaths){ // tikrinam duplikatus.
                        if (path.toLowerCase().equals(smallE)){
//                            throw new GdxRuntimeException("duplikatas");
                            continue MAIN; // duplikatas. Judam tolyn.
                        }
                    }
                    for (String path : animationAtlas){ // tikrinam ar atlas nepaimtas is spriterDrawable.
                        if (path.toLowerCase().equals(smallE)){ // paimtas is spriter drawable.
//                            throw new GdxRuntimeException("SpriterDrawable file");
                            continue MAIN; // spriter drawable. judam tolyn.
                        }
                    }
                    TextureAtlas atlas = new TextureAtlas(e); // sakykim failas yra atlaso.
                    atlases.add(atlas);
                    atlasesPaths.add(e);
                    for (Texture a : atlas.getTextures()) {
                        TextureData data = a.getTextureData();
                        if (data instanceof FileTextureData) {
                            String compare = ((FileTextureData) data).getFileHandle().path();
                            for (int count = textureList.size - 1; count >= 0; count--) { // patikrinam atlase nera tokiu image ir jei yra pasalinam is cia.
                                if (textureList.get(count).equals(compare)) {
                                    textureList.removeIndex(count);
                                }
                            }
                        }
                    }
                } catch (GdxRuntimeException ex) {
                    // neina uzkraut atlaso. ignoruojam ir judam toliau.
                    if (error == null){
                        error = ex.getMessage();
                    }else {
                        error += "\n" + ex.getMessage();
                    }
                }
            }
        }
        for (String e : atlasList){
            if (e.toLowerCase().endsWith(".scml")){ // spriter failas.
                try {
//                    String atlas;
                    String file = e.substring(0, e.lastIndexOf(".")) + "Resources.txt";
                    TextureAtlas atlas = null;
                    for (int a = 0; a < atlasesPaths.size; a++){
                        // ziurim ar atlasas uzkrautas jau.
                        if (atlasesPaths.get(a).toLowerCase().equals(file.toLowerCase())){
                            atlas = atlases.get(a); // atlasas uzkrautas.
                            animationAtlas.add(atlasesPaths.get(a)); // dedam i ignor lista.

                            // salinam is atlas failu, kad nemaisytu.
                            atlases.removeIndex(a);
                            atlasesPaths.removeIndex(a);
                            break; // radom atlasa, iseinam.
                        }
                    }
                    FileHandle atl = Gdx.files.absolute(file);
                    if (atlas == null) { // dar karta patikrinam, gal ten tiesiog faila pdave, o salia sitas stovi.
                        if (atl.exists()) {
                            try {
                                //                                atlas = atl.name();
                                atlas = new TextureAtlas(atl);
//                                atlases.add(atlas); // ir prie to pacio i textures atlasa pridedam.
//                                atlasesPaths.add(file);
                                animationAtlas.add(file); // dedam i ignor lista, kad nedetu prie atlas failu.
//                                test.dispose(); // sitas labai negerai, veltui krovimas...
                            } catch (GdxRuntimeException ex2) {
                                atlas = null;
                            }
                        }
                    }
                    SpriterDrawable drawable = new SpriterDrawable(e, null, true); // aha, kaip del atlaso?
                    if (atlas == null) {
                        drawable.load(new File(e));
                    } else {
//                        String name = file.substring(file.lastIndexOf("/")+1, file.length()-1);
                        drawable.loadFromTextureAtlas(Gdx.files.absolute(e).file(), atlas);
                    }
                    animations.add(drawable);
                }catch (RuntimeException ex1){
                    // ne tas failas, judam tolyn
                    if (error == null){
                        error = ex1.getMessage();
                    }else {
                        error += "\n" + ex1.getMessage();
                    }
                }
            }
        }
        textures.addAll(textureList);
    }

    public Array<String> getTexturesPathList() {
        return textures;
    }

    public Array<TextureAtlas> getAtlases() {
        return atlases;
    }

    public Array<String> getAtlasesPaths() {
        return atlasesPaths;
    }

    public Array<SpriterDrawable> getAnimations() {
        return animations;
    }

    /** Error message. If error didn't happened then null */
    public String getErrorMessage(){
        return error;
    }

    /** Cleans loader. list will become empty. Does not dispose resources. Dispose resources if they are not needed. */
    public void clear(){
        animations.clear();
        atlasesPaths.clear();
        atlases.clear();
        textures.clear();

        error = null;

        animationAtlas.clear();
    }

    /** @return if loader has empty lists true, false otherwise */
    public boolean isEmpty(){
        return animations.size == 0 && atlases.size == 0 && textures.size == 0;
    }

    /** lazy usage. */
    public String atlasPath(TextureAtlas e){
        int count = 0;
        for (TextureAtlas a : atlases){
            if (e == a){
                return atlasesPaths.get(count);
            }
            count++;
        }
        return null; // not found.
    }

    private void loadFiles(FileHandle file, Array<String> textureList, Array<String> atlasList) {
        if (file.isDirectory()){ // jei direktorija, tai pertikrins per naujo
            for (FileHandle e : file.list()){
                loadFiles(e, textureList, atlasList); // perziures visa direktorija is naujo.
            }
        }else if (file.exists()){ // jei failas, tai jo path isaugos.
            InputStream fileStream;
            try {
                fileStream = file.read();
                BufferedImage img = ImageIO.read(fileStream); // patikrin ar image
                if (img == null){
                    atlasList.add(file.path()); // jei ne image, tai i atlas
                }else {
                    boolean add = true;
                    for (String list : textures){ // nededa, duplikatu.
                        if (list.equals(file.path())){
                            add = false;
                        }
                    }
                    if (add)
                        textureList.add(file.path()); // jei image i textures
                }
                fileStream.close();
            } catch (GdxRuntimeException | IOException ex){
                ex.printStackTrace();
            }
        }
    }
}
