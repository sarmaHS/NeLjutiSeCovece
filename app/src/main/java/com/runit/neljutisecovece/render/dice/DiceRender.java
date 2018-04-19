package com.runit.neljutisecovece.render.dice;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.runit.neljutisecovece.model.Cell;

/**
 * Created by Radovan Ristovic on 4/17/2018.
 * Quantox.com
 * radovanr995@gmail.com
 */

public class DiceRender {
    private Paint fill = new Paint();
    private Paint number = new Paint();

    public DiceRender() {
        fill.setColor(Color.BLACK);
        fill.setAntiAlias(true);
        number.setColor(Color.WHITE);
        number.setAntiAlias(true);
        number.setTextSize(50);
    }

    public void renderDice(Canvas canvas, int diceNumber) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), fill);
        canvas.drawText(String.valueOf(diceNumber), canvas.getHeight() / 2, canvas.getHeight() / 2, number);
    }

    public boolean isDiceRolling() {
        return false;
    }
}
