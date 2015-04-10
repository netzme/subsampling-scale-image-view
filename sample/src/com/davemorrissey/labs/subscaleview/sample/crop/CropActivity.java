package com.davemorrissey.labs.subscaleview.sample.crop;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageRegionDecoder;
import com.davemorrissey.labs.subscaleview.sample.R;

public class CropActivity extends Activity {

    private SubsamplingScaleImageView imageView;
    private View cropperView;

    private String[] images = {"emma.jpg", "squirrel.jpg"};
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        cropperView = findViewById(R.id.cropper);

        initialiseImage(images[index]);
        initializeSwitch();
    }

    private int adjustIndex() {
        index = ++index % 2;
        return index;
    }

    private void initialiseImage(String image) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;

        imageView = (SubsamplingScaleImageView)findViewById(R.id.imageView);
        imageView.setPanOffset(width, width);
        imageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);
        imageView.setImage(ImageSource.asset(image));
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);

        imageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
            @Override
            public void onReady() {
            }

            @Override
            public void onImageLoaded() {
                float minScale;
                if (imageView.getSHeight() > imageView.getSWidth()) {
                    minScale = (float) width / imageView.getSWidth();
                } else {
                    minScale = (float) width / imageView.getSHeight();
                }
                imageView.setMinScale(minScale);
            }

            @Override
            public void onPreviewLoadError(Exception e) {

            }

            @Override
            public void onImageLoadError(Exception e) {

            }

            @Override
            public void onTileLoadError(Exception e) {

            }
        });

        findViewById(R.id.button_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = doCrop(images[index]);
                CropResultActivity.croppedBitmap = bitmap;

                Intent intent = new Intent(CropActivity.this, CropResultActivity.class);
                startActivity(intent);
            }
        });
    }

    private Bitmap doCrop(String image) {
        PointF leftSource = imageView.viewToSourceCoord(cropperView.getLeft(), cropperView.getTop());
        PointF rightSource = imageView.viewToSourceCoord(cropperView.getRight(), cropperView.getBottom());

        ImageRegionDecoder decoder = new SkiaImageRegionDecoder();
        final String ASSET_SCHEME = "file:///android_asset/";

        try {
            decoder.init(this, Uri.parse(ASSET_SCHEME + image));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (decoder.isReady()) {
            Bitmap bitmap = decoder.decodeRegion(
                    new Rect((int) leftSource.x, (int)leftSource.y, (int)rightSource.x, (int)rightSource.y),
                    1);

            decoder.recycle();
            return bitmap;
        }

        return null;
    }

    private void initializeSwitch() {
        Button button = (Button) findViewById(R.id.button_switch);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustIndex();
                initialiseImage(images[index]);
            }
        });
    }
}
