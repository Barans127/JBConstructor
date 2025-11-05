package com.jbconstructor.main.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.engine.interfaces.controls.Form;
import com.engine.interfaces.controls.Window;
import com.engine.root.GdxPongy;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

// apejimas. leidz naudot swing formas, tarsi jos susidirbtu su libGDX sistema. Tik Desktop version.
public class FileChooser implements Runnable, Disposable{
    private JFrame frame;
    private JFileChooser chooser;
    private boolean isRunning = true, needLoad = true;
    private Thread worker;
    private FileChooseListener listener;
    private final Object lock; // tik del thread miegojimo.
    private boolean onlyMainThread;

    public FileChooser(){
        worker = new Thread(this);
        lock = new Object();
    }

    @Override
    public void run() {
        while (isRunning) {
            Window e = GdxPongy.getInstance().getActiveForm();
            Form form = null;
//            boolean act = false; // atsimins pries tai buvusia pozicija.
            synchronized (e) {
                if (e instanceof Form) { // jei tai normali forma, tai disablinam ja.
                    form = (Form) e;
//                    act = form.isActive();
                    form.addOnTop(this);
                }
            }
            load();
            int res = chooser.showOpenDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                //Do some stuff
                if (listener != null){
                    if (onlyMainThread){
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                listener.fileChosen(chooser);
                                listener = null;
                            }
                        });
                    }else {
                        listener.fileChosen(chooser);
                        listener = null;
                    }
                }
            }
            if (form != null){
                synchronized (form) { // enablinam is naujo.
//                    form.setAction(act);
                    form.removeTopItem(this);
                }
            }
            try {
                synchronized (lock) {
                    lock.wait(); // tiesiog miegam, nereiks is naujo kurt threadu.
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void load(){
        if (needLoad) {
            chooser = new JFileChooser();
            frame = new JFrame();
            frame.setVisible(true);
            frame.toFront();
            frame.setAlwaysOnTop(true);
            frame.setVisible(false);
            needLoad = false;
        }
    }

    public void open(FileChooseListener listener){
        open(listener, false);
    }

    /** open file choose dialog.
     * @param e listener, will be called when files will be chosen.
     * @param onlyInMainThread if true, than listener will be called in main draw thread, if false then
     * listener will be called in current thread. Convenient if listener should load some images.*/
    public void open(FileChooseListener e, boolean onlyInMainThread){
        listener = e;
        if (worker.getState() == Thread.State.NEW){
            worker.start();
        }else {
            synchronized (lock) {
                lock.notify();
            }
        }
        this.onlyMainThread = onlyInMainThread;
    }

    public void stopWorker(){
        isRunning = false;
        synchronized (lock) {
            lock.notify();
        }
    }

    public JFileChooser getFileChooser() {
        load();
        return chooser;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    @Override
    public void dispose() {
        isRunning = false;
        synchronized (lock) {
            lock.notify();
        }
        frame.dispose();
    }

    public interface FileChooseListener{
        public void fileChosen(JFileChooser chooser);
    }
}
