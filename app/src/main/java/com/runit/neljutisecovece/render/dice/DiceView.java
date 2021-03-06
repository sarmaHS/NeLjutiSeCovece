package com.runit.neljutisecovece.render.dice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import com.runit.neljutisecovece.R;
import com.runit.neljutisecovece.util.JobExecutor;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Radovan Ristovic on 4/19/2018.
 * Quantox.com
 * radovanr995@gmail.com
 */

public class DiceView extends View {
    private Bitmap mDiceSprite;
    private final DiceRollCallBack mDiceRollCallback;
    private int mCurDiceNumber = -1;
    static final int FPS = 180;
    static final long TIME_PER_FRAME = (long) (1000f / FPS);
    static final int TOTAL_IMAGES_IN_SPRITE = 24;
    int mImageCounter = TOTAL_IMAGES_IN_SPRITE;
    private CountDownLatch mImageDrawn = new CountDownLatch(1);
    private Paint paint = new Paint();
    private Rect src = new Rect();
    private Rect dst;

    public interface DiceRollCallBack {
        void onDiceRendered();
    }

    public DiceView(Context context, DiceRollCallBack diceRollCallBack) {
        super(context);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        this.mDiceSprite = BitmapFactory.decodeResource(context.getResources(), R.drawable.dice_sprite);
        this.mDiceRollCallback = diceRollCallBack;
        this.paint.setAntiAlias(true);
    }

    public void drawDice(int diceNumber) {
        this.mCurDiceNumber = diceNumber;
        newTask();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dst == null) {
            dst = new Rect(0, 0, this.mDiceSprite.getHeight(), this.mDiceSprite.getHeight());
            src.top = 0;
            src.bottom = this.mDiceSprite.getHeight();
        }
        Log.d("DiceView", "ImageCounter: " + String.valueOf(mImageCounter));

        if (mImageCounter == -1) {
            mImageCounter = 0;
        }
        if (mImageCounter == 0) {
            src.left = (TOTAL_IMAGES_IN_SPRITE + this.mCurDiceNumber - 1) * src.bottom;
            src.right = (TOTAL_IMAGES_IN_SPRITE + this.mCurDiceNumber - 1) * src.bottom + src.bottom;
        } else {
            src.left = (TOTAL_IMAGES_IN_SPRITE - mImageCounter) * src.bottom;
            src.right = (TOTAL_IMAGES_IN_SPRITE - mImageCounter) * src.bottom + src.bottom;
        }
        canvas.drawBitmap(this.mDiceSprite, src, dst, paint);
        super.onDraw(canvas);
        mImageDrawn.countDown();
    }


    private void newTask() {
        mImageCounter = TOTAL_IMAGES_IN_SPRITE;
        mImageDrawn = new CountDownLatch(1);
        JobExecutor.execute(() -> {
            while (mImageCounter >= 0) {
                long startTime = System.currentTimeMillis();
                DiceView.this.postInvalidate();
                try {
                    mImageDrawn.await();
                    mImageCounter--;
                    mImageDrawn = new CountDownLatch(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    long timeToWait = TIME_PER_FRAME - (startTime - System.currentTimeMillis());
                    if (timeToWait > 0)
                        Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            notifyObserver();
        });
    }

    private void notifyObserver() {
        JobExecutor.executeOnUI(mDiceRollCallback::onDiceRendered);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt("last_number_rolled", this.mCurDiceNumber);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            this.mCurDiceNumber = bundle.getInt("last_number_rolled");
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }
}
