package com.davemorrissey.labs.subscaleview.sample.crop;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageRegionDecoder;
import com.davemorrissey.labs.subscaleview.sample.R;

public class CropActivity extends Activity {

    private static final int IMAGE_PICK = 105;

    private SubsamplingScaleImageView imageView;
    private View cropperView;
    private Uri selectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        View upperMaskView = findViewById(R.id.upper_mask);
        ColorDrawable upperMaskViewColor = (ColorDrawable) upperMaskView.getBackground();

        cropperView = findViewById(R.id.cropper);
        cropperView.setBackgroundDrawable(new HoleInDrawable(upperMaskViewColor.getColor()));

        Button button = (Button) findViewById(R.id.button_pick);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case IMAGE_PICK:
                if(resultCode == RESULT_OK){
                    selectedImage = imageReturnedIntent.getData();
                    initialiseImage(ImageSource.uri(selectedImage));
                }
        }
    }

    private void initialiseImage(ImageSource imageSource) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;

        imageView = (SubsamplingScaleImageView)findViewById(R.id.imageView);
        imageView.setPanOffset(width, width);
        imageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);
        imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
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

        imageView.setImage(imageSource);


        findViewById(R.id.button_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImage == null) {
                    return;
                }

                Bitmap bitmap = doCrop();
                CropResultActivity.croppedBitmap = bitmap;

                Intent intent = new Intent(CropActivity.this, CropResultActivity.class);
                startActivity(intent);
            }
        });
    }

    private Bitmap doCrop() {
        PointF leftSource = imageView.viewToSourceCoord(cropperView.getLeft(), cropperView.getTop());
        PointF rightSource = imageView.viewToSourceCoord(cropperView.getRight(), cropperView.getBottom());

        ImageRegionDecoder decoder = new SkiaImageRegionDecoder();

        try {
            decoder.init(this, selectedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (decoder.isReady()) {
            Rect rect = new Rect((int) leftSource.x, (int)leftSource.y,
                    (int)rightSource.x, (int)rightSource.y);

            Rect targetRect = rotateRect(rect);

            Bitmap bitmap = decoder.decodeRegion(
                    targetRect,
                    1);

            decoder.recycle();

            int rotation = imageView.getAppliedOrientation();
            if (rotation > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);

                Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);

                bitmap.recycle();

                return resultBitmap;
            }

            return bitmap;
        }

        return null;
    }

    private Rect rotateRect(Rect sRect) {
        Rect target = new Rect();
        int rotation = imageView.getAppliedOrientation();
        int sWidth = imageView.getSWidth();
        int sHeight = imageView.getSHeight();

        if (rotation == 0) {
            target.set(sRect);
        } else if (rotation == 90) {
            target.set(sRect.top, sHeight - sRect.right, sRect.bottom, sHeight - sRect.left);
        } else if (rotation == 180) {
            target.set(sWidth - sRect.right, sHeight - sRect.bottom, sWidth - sRect.left, sHeight - sRect.top);
        } else {
            target.set(sWidth - sRect.bottom, sRect.left, sWidth - sRect.top, sRect.right);
        }

        return target;
    }
}
