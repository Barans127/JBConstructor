package com.engine.core;

import com.badlogic.gdx.Gdx;

import java.awt.Frame;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JOptionPane;


public class ErrorHandler implements UncaughtExceptionHandler {
//	private static Engine p;
//	private static DaToast e;

//	public interface DaToast{
//		void toast(String text);
//	}

//	public static void addDaToast(DaToast e){
//		ErrorHandler.e = e;
//	}

	public ErrorHandler() {
	}

	@Override
	public void uncaughtException(Thread arg0, Throwable arg1) {
		Engine p = Engine.getInstance();
//		if (p == null)
//			p = Engine.getInstance();
		arg1.printStackTrace();
		String me = arg1.getMessage();
		if (me == null || me.equals("")){
			if (arg1 instanceof NullPointerException){
				me = "NullPointerException. Maybe some textures not loaded properly?";
			}else
				me = "Unknown error occurred";
		} else if (me.contains("java.lang.RuntimeException:")){
			try{
				me = me.split(":", 2)[1].trim(); // nuima runtime, nereikalingas.
			}catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
			}
		}
		if (arg0.getName().equals("LWJGL Application") || p == null){
			System.out.println("mire pagrindinis");
			switch (Gdx.app.getType()){
				case Android:
//					if (e != null)
//						e.toast(me);
					break;
				case Desktop:
					JOptionPane.showMessageDialog(new Frame(), me,
							"Error",
							JOptionPane.ERROR_MESSAGE);
					break;
				case iOS:
					break;
			}
			System.exit(0);
		}
		p.setError(me, ErrorMenu.ErrorType.UnknowError);
	}

}
