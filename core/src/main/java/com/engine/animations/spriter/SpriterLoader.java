package com.engine.animations.spriter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.brashmonkey.spriter.Data;
import com.brashmonkey.spriter.File;
import com.brashmonkey.spriter.FileReference;
import com.brashmonkey.spriter.Loader;
import com.engine.core.Resources;

/**
 * Spiter animation loader. Used to load resources for animation.
 * By default spriter animations would use only separate textures, which is not
 * efficient, so using this loader it is possible to use packed resources into
 * TextureAtlas.
 */

public class SpriterLoader extends Loader<TextureRegion> implements Disposable {
    private boolean loadFromAtlas;
    private TextureAtlas atlas; // jeigu texturos is atlaso bus naudojamos.
    private boolean isLoaded = false;

//    /** @param path path to scml file. */
//    public SpriterLoader(String path){
//        super(new SCMLReader(path).getData());
//    }

    /**
     * Creates a loader with the given Spriter data.
     *
     * @param data the generated Spriter data
     */
    public SpriterLoader(Data data) {
        super(data);
    }

    public Data getData(){
        return data;
    }

    public boolean isLoaded(){
        return isLoaded;
    }

    public String getRootFolderPath(){
        return root;
    }

    /** @return spriter atlas, if resources are not from atlas then null is returned*/
    public TextureAtlas getAtlas() {
        return atlas;
    }

    /** @param root spriter file
     *  @param atlasFile atlas file name*/
    public void loadFromTextureAtlas(java.io.File root, String atlasFile){
        if (!isLoaded) {
            loadFromAtlas = true;
            try {
                this.atlas = new TextureAtlas(Gdx.files.absolute(root.getParent() + "/" + atlasFile));
                for (Texture e : atlas.getTextures()){
                    e.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                }
            }catch (GdxRuntimeException ex){
//                GdxPongy.getInstance().setError("Cannot load atlas file: " + atlasFile, ErrorMenu.ErrorType.ControlsError);
//                return;
                throw new RuntimeException("Cannot load atlas file: " + atlasFile);
            }
            super.load(root.getParent());
            isLoaded = true;
        }else {
//            GdxPongy.getInstance().setError("Files already have been loaded.", ErrorMenu.ErrorType.ControlsError);
            throw new RuntimeException("Files have already been loaded");
        }
    }

    public void loadFromTextureAtlas(java.io.File root, TextureAtlas atlas){
        if (!isLoaded){
            if (atlas == null){
//                GdxPongy.getInstance().setError("SpriterLoader: Atlas cannot be null.", ErrorMenu.ErrorType.WrongPara);
                throw new RuntimeException("Atlas cannot be null");
//                return;
            }
            loadFromAtlas = true;
            this.atlas = atlas;
            super.load(root.getParent());
            isLoaded = true;
        }else {
//            GdxPongy.getInstance().setError("Files already have been loaded.", ErrorMenu.ErrorType.ControlsError);
            throw new RuntimeException("Files have already been loaded");
        }
    }

    /** changes current atlas or images to new atlas.
     * @param dispose if true, and before was images, all images will be disposed. if there was atlas, old atlas will be disposed.
     * @return old atlas. null if no atlas was before*/
    public TextureAtlas changeTextureAtlas(TextureAtlas newAtlas, boolean dispose){
        if (isLoaded) {
            if (atlas == null)
                return null;
            if (newAtlas == null) {
//                GdxPongy.getInstance().setError("new atlas cannot be null", ErrorMenu.ErrorType.WrongPara);
                throw new RuntimeException("Atlas cannot be null");
//                return null;
            }
            if (!loadFromAtlas) {
                if (dispose) { // buvo ne is atlas.
                    for (TextureRegion e : resources.values()) {
                        Resources.addDisposable(e.getTexture());
                    }
                }
            }
            resources.clear();
            loadFromAtlas = true;
            TextureAtlas old = atlas;
            this.atlas = newAtlas;
            super.load(new java.io.File(root).getParent());
            if (dispose && old != null) {
                Resources.addDisposable(old);
            }
            return old;
        }else {
//            GdxPongy.getInstance().setError("SpriterLoader: load animation before changing atlases.", ErrorMenu.ErrorType.ControlsError);
            throw new RuntimeException("Animations is not loaded. Cannot change atlas");
//            return null;
        }
    }

    @Override
    public void load(String root) {
        if (!isLoaded) {
            loadFromAtlas = false;
            super.load(root);
            isLoaded = true;
        }else {
//            GdxPongy.getInstance().setError("Files already have been loaded.", ErrorMenu.ErrorType.ControlsError);
            throw new RuntimeException("Files have already been loaded");
        }
    }

    @Override
    public void dispose() {
        if (!loadFromAtlas) {
            for (TextureRegion e : resources.values()) {
                Resources.addDisposable(e.getTexture());
            }
        }else {
            Resources.addDisposable(atlas);
            atlas = null;
        }
        super.dispose();
    }

    @Override
    protected TextureRegion loadResource(FileReference ref) {
        if (loadFromAtlas){
            String name = data.getFile(ref).name;
            if (name.contains(".")){
                name = name.substring(0, name.lastIndexOf("."));
            }
//            TextureAtlas.AtlasRegion reg = atlas.newSpri(name);
            Sprite reg = atlas.createSprite(name); // atlas sprite atvercia detale jei ana paversta.
            if (reg == null){
//                GdxPongy.getInstance().setError("SpriterLoader: Texture atlas doesn't have regions needed for animation. Is this correct " +
//                        "Texture atlas?", ErrorMenu.ErrorType.ControlsError);
                throw new RuntimeException("Texture atlas doesn't have regions needed for animation. Did you loaded correct atlas?");
//                return null;
            }
            return reg;
        }else {
            File res = data.getFile(ref);
            FileHandle file = Gdx.files.absolute(root + "/" + res.name);
            if (!file.exists()){
//                GdxPongy.getInstance().setError("Cannot locate file: " + res.name, ErrorMenu.ErrorType.MissingResource);
//                return new TextureRegion(new Texture(10, 10, Pixmap.Format.RGB888));
                throw new RuntimeException("Cannot locate file: " + res.name);
            }
            Texture e = new Texture(file);
            e.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return new TextureRegion(e); // tikrai absolute?
        }
    }
}
