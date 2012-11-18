package com.zielm.wallbounce;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		view = (GameView) findViewById(R.id.gameView1);
		((Button)findViewById(R.id.pause)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				view.pause();
			}
		});
		((Button)findViewById(R.id.newGame)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				view.newGame();
			}
		});
		view.activity = this;
		view.textTop = ((TextView)findViewById(R.id.textTop));
		view.textBottom = ((TextView)findViewById(R.id.textBottom));
		view.init();
		(new Thread() {
			public void run() {
				try { Thread.sleep(500); } catch(InterruptedException ex) {}
				view.myRedraw();
			}
		}).start();
	}
	GameView view;
	@Override
	protected void onResume() {
		super.onResume();
		view.pause = false;
	}
	@Override
	protected void onPause() {
		super.onResume();
		view.pause = true;
	}
}
