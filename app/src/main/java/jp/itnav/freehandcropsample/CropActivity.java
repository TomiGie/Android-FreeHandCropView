package jp.itnav.freehandcropsample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

public class CropActivity extends AppCompatActivity {
    ImageView imageView;
    boolean crop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            crop = extras.getBoolean(FreeHandCropView.INTENT_KEY_CROP);
            Log.d("MainActivity", "crop ---> " + crop);
        }
        imageView = (ImageView) findViewById(R.id.cropped_image_view);
        setCroppedImage(imageView);
    }

    private void setCroppedImage(ImageView view) {
        Bitmap croppedImage = FreeHandCropView.getBitmapFromMemCache();
        view.setImageBitmap(croppedImage);
    }
}
