package jp.itnav.freehandcropsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.picture);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(new FreeHandCropView(this, bitmap));
    }
}
