package com.zielm.wallbounce;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class GameView extends SurfaceView implements Runnable {
	
	public TextView textTop;
	public TextView textBottom;
	Thread thread;
	Activity activity;
	int lifes;
	int level;
	boolean hasTouch;
	int touchX;
	int touchY;
	Random rand = new Random();
	int R = 20;
	Paint circlePaint;
	Paint bgPaint;
	Paint blockPaint;
	Paint newBlockPaint;
	Bitmap block1;
	Bitmap fblock1;
	Bitmap block2;
	Bitmap fblock2;
	Bitmap ball;
	byte[][] map;
	{
		circlePaint = new Paint();
		circlePaint.setARGB(0xff, 0x78, 0xa1, 0x55);
		bgPaint = new Paint();
		bgPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		blockPaint = new Paint();
		blockPaint.setARGB(0xf0, 0xa0, 0xa0, 0xa0);
		newBlockPaint = new Paint();
		newBlockPaint.setARGB(0x40, 0xa0, 0xa0, 0xa0);
	}
	
	class Ball {
		float x, y;
		float vx, vy;
		
		public Ball() {
			x = rand.nextInt(getWidth() - 4 * R) + (R*2);
			y = rand.nextInt(getHeight() - 4 * R) + (R*2);
			vx = 200 * (rand.nextBoolean()? 1: -1);
			vy = 200 * (rand.nextBoolean()? 1: -1);
		}
		float lastX, lastY;
		void tick() {
			DELTA = 0.015f;
			//if(DELTA > (R / 200f)) DELTA = R / 200f;
			lastX = x;
			lastY = y;
			x += vx * DELTA;
			y += vy * DELTA;
			canvas.drawCircle(x, y, R / 2, circlePaint);
			//canvas.drawBitmap(ball, x - R/2, y - R/2, null);
			boolean ch = false;
			if(x < R/2 || x > getWidth() - R/2) { ch = true; vx *= -1; }
			if(y < R/2 || y > getHeight() - R/2) { ch = true; vy *= -1; }
			if(ch) return;
			
			maybeOops();
			collide();
		}
		void collide() {
			if(map[(int)(x / R)][(int)(y / R)] == 1) {
				// check horizontal line
				boolean both = map[(int)(lastX / R)][(int)(y / R)] == 1 && map[(int)(x / R)][(int)(lastY / R)] == 1;
				if((int)(y/R) != (int)(lastY/R)) {
					if(map[(int)(x / R)][(int)(lastY / R)] != 1 || both) {
						vy *= -1;
						y += vy * DELTA;
					}
				}
				if((int)(x/R) != (int)(lastX/R)) {
					if(map[(int)(lastX / R)][(int)(y / R)] != 1 || both) {
						vx *= -1;
						x += vx * DELTA;
					}
				}
			}
		}
		void maybeOops() {
			int x1 = (int)Math.floor(x / R);
			if(x1 < 0) return;
			int x2 = (int)Math.ceil(x / R);
			int y1 = (int)Math.floor(y / R);
			if(y1 < 0) return;
			int y2 = (int)Math.ceil(y / R);
			if(map[x1][y1] == 2) oops();
			else if(map[x1][y2] == 2) oops();
			else if(map[x2][y1] == 2) oops();
			else if(map[x2][y2] == 2) oops();
		}
	}
	
	List<Ball> balls = new ArrayList<Ball>();
	
	public GameView(Context context, AttributeSet set) {
		super(context, set);
		thread = new Thread(this);
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				synchronized (GameView.this) {
					if(event.getAction() == MotionEvent.ACTION_DOWN) {
						touchX = (int) event.getX();
						touchY = (int) event.getY();
						hasTouch = true;
					}
				}
				return false;
			}
		});
	}
	void myRedraw() {
		Canvas canvas = getHolder().lockCanvas();
		if(canvas != null) {
			System.err.println("Drawing!");
			onDraw(canvas);
			getHolder().unlockCanvasAndPost(canvas);
		}
	}
	@Override
	public void onDraw(Canvas canvas) {
		int w = getWidth();
		if(promo == null) promo = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(activity.getResources(), com.zielm.wallbounce.R.drawable.wallbouncepromo),
				w, w * 387 / 640, true);
		canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);
		canvas.drawBitmap(promo, 0, 0, null);
	};
	
	Bitmap promo;
	void init() {
		textTop.setText("Touch on a map to create partings. Avoid balls.");
		textBottom.setText("Fill 60% of area to go to next level.");

		block1 = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(activity.getResources(), com.zielm.wallbounce.R.drawable.block1),
				R, R, true);
		fblock1 = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(activity.getResources(), com.zielm.wallbounce.R.drawable.fblock1),
				R, R, true);
		block2 = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(activity.getResources(), com.zielm.wallbounce.R.drawable.block2),
				R, R, true);
		fblock2 = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(activity.getResources(), com.zielm.wallbounce.R.drawable.fblock2),
				R, R, true);
		ball = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(activity.getResources(), com.zielm.wallbounce.R.drawable.ball),
				R, R, true);
		
	}
	
	void pause() {
		pause = !pause;
	}
	
	void newGame() {
		running = true;
		try {
			thread.interrupt();
		} catch(Exception ex) {}
		thread = new Thread(this);
		try {
			thread.start();
		} catch(Exception ex) {}
	}
	
	void formatLabels() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if(lifes > 0) {
					int filledPerc = (int)((filled / (float)(mw * mh)) * 100);
					textTop.setText("Lifes: " + lifes + "          Filled: " + filledPerc + "%");
					textBottom.setText("Level: " + level);
				} else {
					textTop.setText("Game over! Press New game to start from beggining.");
					textBottom.setText("Level: " + level);
				}
			}
		});
	}
	
	void initGame() {
		lifes = 4;
		level = 0;
		startLevel();
		formatLabels();
	}
	
	void startLevel() {
		level++;
		filled = 0;
		balls.clear();
		for(int i=0; i<level; i++) {
			balls.add(new Ball());
		}
		mw = getWidth() / R + 2;
		mh = getHeight() / R + 2;
		map = new byte[mw][];
		for(int i=0; i<mw; i++) {
			map[i] = new byte[mh];
		}
		for(int i=0; i<mw; i++) {
			map[i][0] = 1;
			map[i][mh - 3] = 1;
		}
		for(int i=0; i<mh; i++) {
			map[0][i] = 1;
			map[mw - 3][i] = 1;
		}
		/*for(int i=0; i<mw; i++) {
			map[i][5] = 1;
		}*/
	}
	
	int mw, mh;
	long lastTick;
	float DELTA;
	Canvas canvas;
	
	synchronized void tick() {
		long time = System.currentTimeMillis();
		canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);
		DELTA = (float)(time - lastTick) / 1000f; 
		lastTick = time;
		for(Ball b: balls) {
			b.tick();
		}
		for(int x=0; x<mw; x++) {
			for(int y=0; y<mh; y++) {
				if(map[x][y] != 0) {
					//canvas.drawRect(x * R, y * R, (x + 1) * R, (y + 1) * R, map[x][y] == 1 ? blockPaint : newBlockPaint);
					boolean f = ((x % 7) + ((x + y) % 13) + (y * 31) % 7) % 2 == 1;
					canvas.drawBitmap(map[x][y] == 1? (f?fblock1:fblock2):(f?block1:block2), x * R, y * R, null);
				}
			}
		}
		if(hasTouch) {
			//if(touchX < R * 7 || touchX >= getWidth() - R * 7) {
				makeY(touchY / R);
			//}
			//if(touchY < R * 7 || touchY >= getWidth() - R * 7) {
				makeX(touchX / R);
			//}
			hasTouch = false;
		}
		if(time >= timeForSolid && timeForSolid != 0) {
			timeForSolid = 0;
			for(int i=0; i<mw; i++) {
				for(int j=0; j<mh; j++) {
					if(map[i][j] == 2) {
						map[i][j] = 1;
						filled ++;
					}
				}
			}

			if(filled >= 0.6 * mw * mh) {
				startLevel();
			}
			formatLabels();
		}
	}
	
	void makeX(int x){
		for(int i=0; i<mh; i++) {
			if(map[x][i] != 1) map[x][i] = 2;
		}
		startCountdown();
	}
	void makeY(int y) {
		for(int i=0; i<mw; i++) {
			if(map[i][y] != 1) map[i][y] = 2;
		}
		startCountdown();
	}
	
	long timeForSolid = 0;
	void startCountdown() {
		timeForSolid = System.currentTimeMillis() + 300;
	}
	void oops() {
		timeForSolid = 0;
		lifes --;
		if(lifes <= 0) {
			running = false;
			formatLabels();
		}
		for(int i=0; i<mw; i++) {
			for(int j=0; j<mh; j++) {
				if(map[i][j] == 2) {
					map[i][j] = 0;
				}
			}
		}
		formatLabels();
	}
	int filled;
	volatile boolean running = true; 
	volatile boolean pause = false;
	public void run() {
		initGame();
		lastTick = System.currentTimeMillis();
		try{
			while(running) {
				while(pause) Thread.sleep(1000);
				canvas = getHolder().lockCanvas();
				if(canvas == null) continue;
				//draw=false;
				for(int i=0; i<3; i++)tick();
				//draw=true;
				tick();
				getHolder().unlockCanvasAndPost(canvas);
				Thread.sleep(80);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
