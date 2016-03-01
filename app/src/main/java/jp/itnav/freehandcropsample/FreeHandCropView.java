package jp.itnav.freehandcropsample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;


public class FreeHandCropView extends ImageView implements View.OnTouchListener {
    public static final String INTENT_KEY_CROP = "crop";
    public static final String CACHE_KEY = "bitmap";

    public static List<Point> points;
    boolean flgPathDraw = true;
    boolean bFirstPoint = false;
    private Point firstPoint = null;
    private Point lastPoint = null;

    private final Bitmap originalImageBitmap;
    private int canvasWidth;
    private int canvasHeight;
    private Paint paint;
    private Context context;
    private static LruCache<String, Bitmap> mMemoryCache;


    public static Bitmap getBitmapFromMemCache() {
        return mMemoryCache.get(CACHE_KEY);
    }

    public FreeHandCropView(Context c, Bitmap bm) {
        super(c);

        context = c;
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[] { 10, 20 }, 0));
        paint.setStrokeWidth(5);
        paint.setColor(Color.WHITE);

        this.setOnTouchListener(this);
        points = new ArrayList<>();

        bFirstPoint = false;
        this.originalImageBitmap = bm;

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

    }

    public FreeHandCropView(Context context, AttributeSet attrs, Bitmap bm) {
        super(context, attrs);
        this.context = context;
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);

        this.setOnTouchListener(this);
        points = new ArrayList<>();
        bFirstPoint = false;
        this.originalImageBitmap = bm;
    }

    public void addBitmapToMemoryCache(Bitmap bitmap) {
        if (getBitmapFromMemCache() == null) {
            mMemoryCache.put(CACHE_KEY, bitmap);
        }
    }

    private float calcBitmapScale(int canvasWidth, int canvasHeight, int bmpWidth, int bmpHeight) {

        // 最初は幅で調べる
        float scale = (float)canvasWidth / (float)bmpWidth;
        float tmp = bmpHeight * scale;

        // 画像の高さがキャンバスの高さより小さい（余白ができてしまう場合）高さの方で横幅をスケールする
        if (tmp < canvasHeight) {
            scale = (float)canvasHeight / (float)bmpHeight;
            return scale;
        }

        return scale;
    }

    public void onDraw(Canvas canvas) {
        // キャンバスのサイズ
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        // ビットマップのサイズ
        int bmpWidth = this.originalImageBitmap.getWidth();
        int bmpHeight = this.originalImageBitmap.getHeight();

        // 画面サイズに合う様に縦横スケール値を求める（最大限画面に収まる様に努力する）
        float toCanvasScale = this.calcBitmapScale(canvasWidth, canvasHeight, bmpWidth, bmpHeight);

        // キャンバスの大きさに画像を合わせたときにサイズのずれがどれくらいあるか
        float diffX = (bmpWidth * toCanvasScale - canvasWidth);
        float diffY = (bmpHeight * toCanvasScale - canvasHeight);

        // すみを残して中心から取り出す様にする
        float addX = (diffX / toCanvasScale) / 2;
        float addY = (diffY / toCanvasScale) / 2;

        // Bitmapを表示する
//        Paint paint = new Paint();
        // 画像の切り取り位置を調整して画像の中心がキャンバスの中心に来る様にする
        Rect rSrc = new Rect((int)addX, (int)addY,
                (int)((canvasWidth / toCanvasScale) + addX), (int)((canvasHeight / toCanvasScale) + addY));
        RectF rDest = new RectF(0, 0, canvasWidth, canvasHeight);
//        canvas.drawBitmap(originalImageBitmap, rSrc, rDest, null);
        // ----------

        canvas.drawBitmap(originalImageBitmap, 0, 0, null);

        Path cropAreaPath = new Path();
        boolean isFirstPoint = true;

        for (int i = 0; i < points.size(); i += 2) {
            Point point = points.get(i);
            if (isFirstPoint) {
                isFirstPoint = false;
                // 最初の処理でPathのx,y座標をpointの座標に移動する
                cropAreaPath.moveTo(point.x, point.y);
            } else if (i < points.size() - 1) {
                Point next = points.get(i + 1);
                cropAreaPath.quadTo(point.x, point.y, next.x, next.y);
            } else {
                lastPoint = points.get(i);
                cropAreaPath.lineTo(point.x, point.y);
            }
        }
        canvas.drawPath(cropAreaPath, paint);
    }

    public boolean onTouch(View view, MotionEvent event) {
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        if (flgPathDraw) {
            if (bFirstPoint) {
                if (comparePoint(firstPoint, point)) {
                    // points.add(point);
                    points.add(firstPoint);
                    flgPathDraw = false;
                    showCropDialog();
                } else {
                    points.add(point);
                }
            } else {
                points.add(point);
            }

            if (!(bFirstPoint)) {

                firstPoint = point;
                bFirstPoint = true;
            }
        }

        invalidate();
        Log.e("Hi  ==>", "Size: " + point.x + " " + point.y);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d("Action up***>", "called");
            lastPoint = point;
            if (flgPathDraw) {
                if (points.size() > 12) {
                    if (!comparePoint(firstPoint, lastPoint)) {
                        flgPathDraw = false;
                        points.add(firstPoint);
                        showCropDialog();
                    }
                }
            }
        }

        return true;
    }

    private boolean comparePoint(Point first, Point current) {
        int left_range_x = (int) (current.x - 3);
        int left_range_y = (int) (current.y - 3);

        int right_range_x = (int) (current.x + 3);
        int right_range_y = (int) (current.y + 3);

        if ((left_range_x < first.x && first.x < right_range_x)
                && (left_range_y < first.y && first.y < right_range_y)) {
            if (points.size() < 10) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    public void fillinPartofPath() {
        Point point = new Point();
        point.x = points.get(0).x;
        point.y = points.get(0).y;

        points.add(point);
        invalidate();
    }

    public void resetView() {
        points.clear();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        flgPathDraw = true;
        invalidate();
    }

    private void showCropDialog() {
        Bitmap croppedImage = cropImage(this.originalImageBitmap);
        addBitmapToMemoryCache(croppedImage);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        intent = new Intent(context, CropActivity.class);
                        intent.putExtra(INTENT_KEY_CROP, true);
                        context.startActivity(intent);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        bFirstPoint = false;
                        resetView();

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Do you Want to save Crop or Non-crop image?")
                .setPositiveButton("Crop", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show()
                .setCancelable(false);
    }

    private Bitmap cropImage(Bitmap image) {
        Bitmap cropImage = Bitmap.createBitmap(canvasWidth, canvasHeight, image.getConfig());
        Canvas canvas = new Canvas(cropImage);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Path path = new Path();
        for (int i = 0; i < FreeHandCropView.points.size(); i++) {
            path.lineTo(FreeHandCropView.points.get(i).x, FreeHandCropView.points.get(i).y);
        }
        canvas.drawPath(path, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(originalImageBitmap, 0, 0, paint);

        return cropImage;
    }

    class Point {
        public float dy;
        public float dx;
        float x, y;

        @Override
        public String toString(){
            return x + ", " + y;
        }
    }
}
