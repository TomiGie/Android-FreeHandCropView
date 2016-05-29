package jp.itnav.freehandcropsample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.image_pc);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addFreeHandCropView();
    }

    private void addFreeHandCropView() {
        // activity_main.xmlのLinearLayoutに
        // FreeHandCropViewをaddする
        LinearLayout container = (LinearLayout) findViewById(R.id.container);

        FreeHandCropView.ImageCropListener listener = new FreeHandCropView.ImageCropListener() {
            @Override
            public void onClickDialogPositiveButton() {
                Intent intent = new Intent(MainActivity.this, CropActivity.class);
                intent.putExtra(FreeHandCropView.INTENT_KEY_CROP, true);
                startActivity(intent);
            }

            @Override
            public void onClickDialogNegativeButton() {
                // ignore
            }
        };

        container.addView(new FreeHandCropView(this, bitmap, listener),
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
}
