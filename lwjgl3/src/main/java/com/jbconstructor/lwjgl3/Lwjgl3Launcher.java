package com.jbconstructor.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.engine.core.Engine;
import com.engine.core.ImagesLoader;
import com.engine.core.Resources;
import com.engine.core.StartListener;
import com.engine.interfaces.controls.Toast;
import com.jbconstructor.main.forms.StartForm;
import com.jbconstructor.main.managers.Project;
import com.jbconstructor.main.managers.ProjectLoader;
import com.jbconstructor.main.managers.SaveManager;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
//    private static String[] args;

    public static void main(String[] args) {

        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication(args);
    }

    private static Lwjgl3Application createApplication(final String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1280, 720);
        config.setTitle("JBConstructor");
        config.setWindowIcon("resources/icons/ico128.png",
                "resources/icons/ico32.png", "resources/icons/ico16.png");

        // Some things from libGDX setup.

        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        config.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

        //// This should improve compatibility with Windows machines with buggy OpenGL drivers, Macs
        //// with Apple Silicon that have to emulate compatibility with OpenGL anyway, and more.
        //// This uses the dependency `com.badlogicgames.gdx:gdx-lwjgl3-angle` to function.
        //// You can choose to remove the following line and the mentioned dependency if you want; they
        //// are not intended for games that use GL30 (which is compatibility with OpenGL ES 3.0).
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);


        return new Lwjgl3Application(Engine.initialize(new StartListener() {
            @Override
            public void startup() {
                init(args);
            }
        }), config);
    }

    private static void init(String[] args){
        Engine e = Engine.getInstance();

        ImagesLoader loader = new ImagesLoader();
        loader.readFileForResources("resources names.txt"); // uzkraunam visa ui.
        loader.loadAllNow();
        Resources.readSettings("jbcConfig.conf"); // nuskaitom nustatymus.
        e.achangeState(e.addForm(new StartForm(), "mainStartForm"));
        Engine.addDisposable(new ProjectDisposer());


        Gdx.graphics.setContinuousRendering(false); // nereik sitam projekte continous.


        // Now check if user opened constructor file. Load project if that was a constructor file.
        if (args != null) {
            for (String text : args) {
                if (text != null && !text.isEmpty()) {
                    FileHandle fileHandle = Gdx.files.absolute(text);
                    if (fileHandle.exists()){
                        ProjectLoader projectLoader = new ProjectLoader();
                        if (projectLoader.openExistingProject(text)){ // uzkrove forma
                            Project.getFormsManager().switchForms(0, true); // jungiam i musu projekta
                            StartForm.projectLoaded(Project.getProjectName(), text); // pasizymim, kad paskutinis sitas buvo uzkrautas.
                        }else { // nepavyko. Nieko tokio, pranesam, kad failed.
                            Toast errorMessage = new Toast(Toast.SHORT);
                            errorMessage.setText(projectLoader.getError());
                            errorMessage.show();
                        }
                        break;
                    }
                }
            }
        }
    }

    // handles project closure. Saves unsaved data before closing the app.
    // All saving occurs into autoSave file, so it can be restored if needed.
    private static class ProjectDisposer implements Disposable {

        @Override
        public void dispose() {
            if (Project.isProjectOpen()) { // kad neliktu kokiu siuksliu kartais.
                SaveManager saveManager = Project.getSaveManager();
                if (!saveManager.isProjectAutoSaved()){ // tikrinam ar buvo autosave ivykes.
                    saveManager.forceAutoSave(); // tiesiog dedam autosave ir visks.
                }
                Project.closeProject();
            }
        }
    }

//    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
//        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
//        configuration.setTitle("JBConstructor");
//        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
//        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
//        configuration.useVsync(true);
//        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
//        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
//        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
//        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
//        //// useful for testing performance, but can also be very stressful to some hardware.
//        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
//
//        configuration.setWindowedMode(640, 480);
//        //// You can change these files; they are in lwjgl3/src/main/resources/ .
//        //// They can also be loaded from the root of assets/ .
//        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
//
//        //// This should improve compatibility with Windows machines with buggy OpenGL drivers, Macs
//        //// with Apple Silicon that have to emulate compatibility with OpenGL anyway, and more.
//        //// This uses the dependency `com.badlogicgames.gdx:gdx-lwjgl3-angle` to function.
//        //// You can choose to remove the following line and the mentioned dependency if you want; they
//        //// are not intended for games that use GL30 (which is compatibility with OpenGL ES 3.0).
//        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);
//
//        return configuration;
//    }
}
