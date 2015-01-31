package net.zhuoweizhang.keybored;

import android.app.*;
import android.inputmethodservice.*;
import android.os.*;
import android.view.*;

import java.io.*;

import fi.iki.elonen.NanoHTTPD;
import static fi.iki.elonen.NanoHTTPD.*;


public class KeyboredService extends InputMethodService {
	public static final String hardcodedText = "Gary Oakenshield";
	public int textIndex = 0;
	private NanoHTTPD httpd;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 8: // BACKSPACE
				if (getCurrentInputConnection() == null) break;
				getCurrentInputConnection().deleteSurroundingText(1, 0);
				break;
			default:
				sendKeyChar((char) msg.arg1);
			}
			System.out.println("Sending " + msg.arg1);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		httpd = new NanoHTTPD(27490) {
			public Response serve(IHTTPSession session) {
				if (session.getUri().equals("/keypress")) {
					System.out.println(session.getParms());
					handler.sendMessage(handler.obtainMessage(1337, session.getParms().get("k").charAt(0), 0));
					return new Response(Response.Status.OK, MIME_PLAINTEXT, "");
				} else {
					try {
						return new Response(Response.Status.OK, MIME_HTML, getAssets().open("index.html"));
					} catch (IOException ie2) {
						throw new RuntimeException(ie2);
					}
				}
			}
		};
		try {
			httpd.start();
		} catch (IOException ie) {
			throw new RuntimeException(ie);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		httpd.stop();
	}
}