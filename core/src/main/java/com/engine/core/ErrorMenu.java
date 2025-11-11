package com.engine.core;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.engine.ui.controls.Form;
import com.engine.ui.controls.views.ScrollView;
import com.engine.ui.controls.widgets.Button;
import com.engine.ui.controls.widgets.Label;
import com.engine.ui.listeners.ClickListener;
import com.engine.root.GdxWrapper;

/** Used to show errors without halting the app. Now it is not used. */
@Deprecated
public class ErrorMenu extends Form {
    private Engine p;
    //	private Button back;
    private String[] types;
    private Label messages;
    private Array<String> errors;

    //	private static boolean important;
    public enum ErrorType {
        UnknowError(0), MissingResource(1), ControlsError(2), GameError(3), WrongPara(4);
        private final int type;

        ErrorType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
//	public static final int UnknowError = 0;
//	public static final int MissingResourse = 1;
//	public static final int MissingLanguage = 2;
//	public static final int ControlsError = 3;
//	public static final int GameError = 4;

    public ErrorMenu() {
        errors = new Array<>(3);
        setPositioning(Position.fixed);
        types = new String[6];
        types[0] = "Unknow error:\n";
        types[1] = "Missing resource:\n";
//        types[2] = "Missing language pack:\n";
        types[2] = "Controls error:\n";
        types[3] = "Game error:\n";
        types[4] = "Illegal parameters:\n";
//		important = true;
//		setMenu();
    }

//	public static void setErrorNotImportant(){
//		important = false;
//	}

    @Override
    protected void fixBackground() {
        if (p == null) {
            p = GdxWrapper.getInstance();
            if (p == null) {
                throw new RuntimeException("Main instance is not initialized");
            }
            setMenu();
        }
        for (int a = errors.size-1; a >= 0; a--){
            addErrorText(errors.get(a));
            errors.removeIndex(a);
        }
        p.background(GdxWrapper.color(237, 245, 168));
    }

//	@Override
//	public boolean onKeyPress(int k){
//		if (k == Input.Keys.ESCAPE){
//			p.exit();
//		}
//		return false;
//	}

    @Override
    public boolean beforeKeyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            System.exit(0);
        }
        return false;
    }

    private void setMenu() {
        Label err = new Label("Upss, something wrong...");
        err.setTextSize(p.getScreenWidth()/16);
        err.setSize(p.getScreenWidth(), p.getScreenHeight()*0.2f);
        err.setPosition(0, p.getScreenHeight()*0.8f);
        err.setTextAlign(Align.center, Align.center);
        addControl(err);
//        Resources.addImage("whiteSystemColor", "resources/ui/balta.png");
        Button exit = new Button("Exit");
        exit.setPosition(p.getScreenWidth()*0.9f, p.getScreenHeight()*0.1f);
        exit.setColors(0xFFAA0000, 0xFF0000FF, 0xFFFF0000);
        exit.setTextSize(p.getScreenWidth()/32);
        exit.setClickListener(new ClickListener() {
            @Override
            public void onClick() {
                System.exit(0);
            }
        });
        exit.setBackground(Resources.getDrawable("whiteSystemColor"));
        if (Gdx.app.getType() != Application.ApplicationType.Desktop){
            exit.setVisible(false); // zodziu nereik jo.
        }
        addControl(exit);
        ScrollView errInfo = new ScrollView();
//		errInfo.setTextSize(40);
//		errInfo.setTextColor(0xFFFF0000);
//        errInfo.setPosition(200, 20);
//		errInfo.setTextAlign(Align.left, Align.top);
        errInfo.setSize(p.getScreenWidth()*0.8f, p.getScreenHeight()*0.8f);
        errInfo.setPosition(p.getScreenWidth()/2 - errInfo.getWidth()/2, 0);

        Label.LabelStyle st = new Label.LabelStyle();
        st.textColor = 0xFFFF0000;
        st.autoSize = false;
        st.width = errInfo.getWidth();
        st.height = errInfo.getHeight();
        st.textSize = p.getScreenWidth()/32;
        Label e = new Label("", st);
        errInfo.addControl(messages = e);
//		errInfo.setShowableCount(3);
        addControl(errInfo);
    }

    private void addErrorText(String err){
        String message = err == null ? "null" : err;
        messages.setText(messages.getText() + "\n" + message);
    }

    void setErrorText(String err, ErrorType type) {
        String message = err == null ? "null" : err;
        errors.add(types[type.getType()] + message);
    }

    void setErrorText(String err){
        errors.add(err);
    }
}
