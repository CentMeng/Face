package com.test.face;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Animatable;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * 使用SimpleDraweeView加载图片并根据人脸来显示图片，防止人脸被遮挡
 */
public class MainActivity extends Activity {

    private SimpleDraweeView sdv_picture;

    /**
     * 输入地址
     */
    private EditText et_url;

    /**
     * 人脸识别最多数
     */
    public final static int FACE_COUNT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        et_url = (EditText) findViewById(R.id.et_url);
        sdv_picture = (SimpleDraweeView) findViewById(R.id.sdv_picture);
    }


    /**
     * Fresco getController
     *
     * @param sdv                SimpleDraweeView
     * @param url                图片地址
     * @param controllerListener 下载图片后执行事件
     * @return
     */
    private static DraweeController getController(SimpleDraweeView sdv, String url, ControllerListener controllerListener) {
        ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .setProgressiveRenderingEnabled(true)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder().setImageRequest(request)
                .setControllerListener(controllerListener)
                .setOldController(sdv.getController())
                .build();
        return controller;
    }


    /**
     * 人脸识别
     *
     * @param bitmap
     * @return 人脸中间位置
     */
    public PointF setFace(Bitmap bitmap) {
        FaceDetector fd;
        FaceDetector.Face[] faces = new FaceDetector.Face[FACE_COUNT];
        PointF midpoint = new PointF();
        int count = 0;
        try {
            //     这是关键点，官方要求必须是RGB_565否则识别不出来
            Bitmap faceBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
            bitmap.recycle();
            // 宽高不等的话会报异常（IllegalArgumentException if the Bitmap dimensions don't match the dimensions defined at initialization），这个可以从源码中看出
            fd = new FaceDetector(faceBitmap.getWidth(), faceBitmap.getHeight(), FACE_COUNT);
            count = fd.findFaces(faceBitmap, faces);
            faceBitmap.recycle();
            Toast.makeText(MainActivity.this, "有" + count + "张脸", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("检测脸部", "setFace(): " + e.toString());
            return midpoint;
        }

        // 检测出来的脸部获取位置，用于设置SimpleDraweeView
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                try {
                    faces[i].getMidPoint(midpoint);
                } catch (Exception e) {
                    Log.e("检测脸部", "setFace(): face " + i + ": " + e.toString());
                }
            }
        }

        return midpoint;
    }

    public void check(View view) {
        sdv_picture.setController(getController(sdv_picture, et_url.getText().toString().trim(), new ControllerListener<ImageInfo>() {
            @Override
            public void onSubmit(String id, Object callerContext) {

            }

            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                /**在调用getDrawingCache的时候要注意下面2点：
                 在调用getDrawingCache()方法从ImageView对象获取图像之前，一定要调用setDrawingCacheEnabled(true)方法：
                 imageview.setDrawingCacheEnabled(true);
                 否则，无法从ImageView对象iv_photo中获取图像； **/
                sdv_picture.setDrawingCacheEnabled(true);
                Bitmap bitmap = sdv_picture.getDrawingCache();
                PointF pointF = setFace(bitmap);
                //按百分比算，防止偏差
                PointF pointF1 = new PointF(pointF.x / (float) sdv_picture.getWidth(), pointF.y / (float) sdv_picture.getHeight());
                //设置聚焦点
                sdv_picture.getHierarchy()
                        .setActualImageFocusPoint(pointF1);
                /**在调用getDrawingCache()方法从ImageView对象获取图像之后，一定要调用setDrawingCacheEnabled(false)方法：
                 imageview.setDrawingCacheEnabled(false);
                 以清空画图缓冲区，否则，下一次从ImageView对象iv_photo中获取的图像，还是原来的图像。**/
                sdv_picture.setDrawingCacheEnabled(false);
            }

            @Override
            public void onIntermediateImageSet(String id, ImageInfo imageInfo) {

            }

            @Override
            public void onIntermediateImageFailed(String id, Throwable throwable) {

            }

            @Override
            public void onFailure(String id, Throwable throwable) {

            }

            @Override
            public void onRelease(String id) {

            }
        }));
        sdv_picture.setAspectRatio(1.618f);
    }

}
