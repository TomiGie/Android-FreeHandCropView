package jp.itnav.freehandcropsample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

public class CropActivity extends AppCompatActivity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        imageView = (ImageView) findViewById(R.id.cropped_image_view);
        setCroppedImage(imageView);
    }

    private void setCroppedImage(ImageView view) {
        // FreeHandCropView切り取りされたBitmapがキャッシュに保存されているので
        // BitMapを取り出してImageViewにセットする
        Bitmap croppedImage = FreeHandCropView.getBitmapFromMemCache();
        view.setImageBitmap(croppedImage);
    }
}
