package com.davemorrissey.labs.subscaleview.sample.crop;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Created by andreas on 4/10/15.
 */
public class HoleInDrawable extends Drawable {

    private Canvas temp;
    private Paint eraser;
    private Bitmap bitmap;
    private int bgColor;


    public HoleInDrawable(int outsideBgColor) {
        setOutsideBackgroundColor(outsideBgColor);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        initBitmap();
    }

    private void initBitmap() {
        Rect bounds = getBounds();
        bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        temp = new Canvas(bitmap);

        eraser = new Paint();
        eraser.setAntiAlias(true);
        eraser.setColor(0xFFFFFFFF);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public void draw(Canvas canvas) {
        if (bitmap == null) {
            initBitmap();
        }

        bitmap.eraseColor(Color.TRANSPARENT);
        temp.drawColor(bgColor);

        Rect bounds = getBounds();
        temp.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), (float) bounds.width() / 2, eraser);

        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    public void setOutsideBackgroundColor(int bgColor) {
        this.bgColor = bgColor;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
