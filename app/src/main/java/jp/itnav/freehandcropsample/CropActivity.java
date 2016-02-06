package jp.itnav.freehandcropsample;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

public class CropActivity extends AppCompatActivity {
    ImageView compositeImageView;
    boolean crop;
    private LruCache<String, Bitmap> mMemoryCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            crop = extras.getBoolean(FreeHandCropView.INTENT_KEY_CROP);
            Log.d("MainActivity", "crop ---> " + crop);
        }


        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        setCroppedImage();
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private void setCroppedImage() {
        int widthOfScreen = 0;
        int heightOfScreen = 0;

        DisplayMetrics dm = new DisplayMetrics();
        try {
            getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        catch (Exception e) {

        }
        widthOfScreen = dm.widthPixels;
        heightOfScreen = dm.heightPixels;

        compositeImageView = (ImageView) findViewById(R.id.cropped_image_view);
        Bitmap originalImage = getBitmapFromMemCache(FreeHandCropView.CACHE_KEY);
        if (originalImage != null) {
            Bitmap croppedImage = Bitmap.createBitmap(widthOfScreen, heightOfScreen, originalImage.getConfig());

            Canvas canvas = new Canvas(croppedImage);
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            Path path = new Path();
            for (int i = 0; i < FreeHandCropView.points.size(); i++) {
                path.lineTo(FreeHandCropView.points.get(i).x, FreeHandCropView.points.get(i).y);
            }
            canvas.drawPath(path, paint);
            if (crop) {
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            } else {
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            }
            canvas.drawBitmap(originalImage, 0, 0, paint);
            compositeImageView.setImageBitmap(croppedImage);
        }
    }
}
