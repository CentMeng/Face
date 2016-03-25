###遇到的坑

- 首先是获取Bitmap对象

imageView.getBackground()，是获取它的背景图片；

在调用getDrawingCache的时候要注意下面2点：

在调用getDrawingCache()方法从ImageView对象获取图像之前，一定要调用setDrawingCacheEnabled(true)方法：

imageview.setDrawingCacheEnabled(true);

否则，无法从ImageView对象iv_photo中获取图像；

在调用getDrawingCache()方法从ImageView对象获取图像之后，一定要调用setDrawingCacheEnabled(false)方法：

imageview.setDrawingCacheEnabled(false);

以清空画图缓冲区，否则，下一次从ImageView对象iv_photo中获取的图像，还是原来的图像。

- 然后是异常解决

<B>主要是以下异常</B>

IllegalArgumentException if the Bitmap dimensions don't match the dimensions defined at initialization 

查看源码可知：
 

        if (!sInitialized) {
            return 0;
        }
        if (bitmap.getWidth() != mWidth || bitmap.getHeight() != mHeight) {
            throw new IllegalArgumentException(
                    "bitmap size doesn't match initialization");
        }
        if (faces.length < mMaxFaces) {
            throw new IllegalArgumentException(
                    "faces[] smaller than maxFaces");
        }
        int numFaces = fft_detect(bitmap);
        if (numFaces >= mMaxFaces)
            numFaces = mMaxFaces;
        for (int i=0 ; i<numFaces ; i++) {
            if (faces[i] == null)
                faces[i] = new Face();
            fft_get_face(faces[i], i);
        }
        return numFaces; 
       
 - FaceDetector注意点
 
 官方要求必须是RGB_565否则识别不出来， 需做如下处理
 
 
            Bitmap faceBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);

