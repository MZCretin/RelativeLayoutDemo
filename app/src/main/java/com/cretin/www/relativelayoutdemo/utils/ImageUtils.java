package com.cretin.www.relativelayoutdemo.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cretin on 15/12/22.
 */
public class ImageUtils {
    private static final int TYPE_FILE_IMAGE = 1;
    private static final int TYPE_FILE_VEDIO = 2;
    private static final String FILE_ROOT_PATH = "/sdcard/cretin/";

    /***
     * 动态设置inSampleSize 倍数
     *
     * @param pathName  图片路径
     * @param reqWidth  要压缩的宽度
     * @param reqHeight 要压缩的高度
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(String pathName,
                                                         int reqWidth, int reqHeight) {
        // 首先设置 inJustDecodeBounds=true 来获取图片尺寸
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        // 计算 inSampleSize 的值
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // 根据计算出的 inSampleSize 来解码图片生成Bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
    public static Bitmap createScaleBitmap(Bitmap src, int dstWidth,
                                           int dstHeight) {
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    // 从Resources中加载压缩图片
    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options); // 读取图片长款
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight); // 计算inSampleSize
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options); // 载入一个稍大的缩略图
        return createScaleBitmap(src, reqWidth, reqHeight); // 进一步得到目标大小的缩略图
    }

    // 从sd卡上加载压缩图片
    public static Bitmap decodeSampledBitmapFromFSDCard(String pathName,
                                                        int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }

    // 从sd卡上加载压缩图片
    public static Bitmap decodeBitmapFromFSDCard(String pathName) {
        return BitmapFactory.decodeFile(pathName);
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /***
     * 将bitmap保存在SD jpeg格式
     *
     * @param bitmap   图片bitmap
     * @param filePath 要保存图片路径
     * @param quality  压缩质量值
     */
    public static void saveImage(Bitmap bitmap, String filePath, int quality) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(filePath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            // 如果图片还没有回收，强制回收
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
                System.gc();
            }
        } catch (Exception e) {

        }
    }

    /**
     * 旋转图片
     *
     * @param angle  旋转角度
     * @param bitmap 要处理的Bitmap
     * @return 处理后的Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizedBitmap != bitmap && bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return resizedBitmap;
    }

    /**
     * 生成输出文件
     *
     * @param fileType
     * @param directoryPath
     * @param format
     * @return
     */
    public static File getOutFile(int fileType, String directoryPath, String format) {
        File mediaStorageDir = null;
        //判断SDCard的状态
        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_REMOVED.equals(storageState)) {
            //SDCard不存在 则使用手机内存进行存储
            mediaStorageDir = new File(Environment.getDataDirectory(), directoryPath);
        } else {
            //SDCard存在 创建目录
            mediaStorageDir = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    , directoryPath);
        }

        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }

        //生成文件
        File file = null;
        if (TextUtils.isEmpty(format) || format.equals("")) {
            file = new File(getFilePath(mediaStorageDir, fileType));
        } else {
            file = new File(getFilePath(mediaStorageDir, fileType, format));
        }
        return file;
    }

    /**
     * 生成输出文件路径
     *
     * @param mediaStorageDir
     * @param fileType
     * @return
     */
    public static String getFilePath(File mediaStorageDir, int fileType) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String filePath = mediaStorageDir.getPath() + File.separator;
        if (fileType == TYPE_FILE_IMAGE) {
            filePath += ("IMG_" + timeStamp + ".png");
        } else if (fileType == TYPE_FILE_VEDIO) {
            filePath += ("VIDEO_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return filePath;
    }

    /**
     * 生成输出文件路径
     *
     * @param mediaStorageDir 存储的文件
     * @param fileType        类型
     * @param format          生成的文件名的格式
     * @return
     */
    public static String getFilePath(File mediaStorageDir, int fileType, String format) {
        String timeStamp = new SimpleDateFormat(format)
                .format(new Date());
        String filePath = mediaStorageDir.getPath() + File.separator;
        if (fileType == TYPE_FILE_IMAGE) {
            filePath += ("IMG_" + timeStamp + ".jpg");
        } else if (fileType == TYPE_FILE_VEDIO) {
            filePath += ("VIDEO_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return filePath;
    }

    /**
     * 将一个View转化成一个Bitmap
     *
     * @param v
     * @return
     */
    public static Bitmap createViewBitmap(View v, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float scaleWidth = (float) width / v.getWidth();
        float scaleHeight = (float) height / v.getHeight();
        canvas.scale(scaleWidth, scaleHeight);
        v.draw(canvas);

        return bitmap;
    }


    public static Bitmap getimage(String srcPath, float hh, float ww) {
        if (hh <= 0 || ww <= 0) {
            hh = 800;
            ww = 480;
        }
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap, 100);//压缩好比例大小后再进行质量压缩
    }

    /**
     * 返回一张压缩后的图片
     *
     * @param image
     * @param size
     * @return
     */
    public static Bitmap compressImage(Bitmap image, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > size) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 通过文件名返回一个压缩的图片
     *
     * @param filePath
     * @return
     */
    public static Bitmap compressBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int height = options.outHeight;
        int width = options.outWidth;

        int inSampleSize = 1;
        int reqHeight = 1280;
        int reqWidth = 960;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 对图片进行缩放
     *
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        return Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
    }

    /**
     * 根据文件名压缩文件再保存
     *
     * @param filePath
     * @param newFilePath
     */
    public static void saveCompressFile(String filePath, String newFilePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int height = options.outHeight;
        int width = options.outWidth;

        int inSampleSize = 1;
        int reqHeight = 1280;
        int reqWidth = 960;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(new File(newFilePath)));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存Bitmap到指定文件
     *
     * @param tempBitmap
     * @param fileName
     */
    public static String saveBitmapToFile(Bitmap tempBitmap, String fileName) {
        if (TextUtils.isEmpty(getFileRootPath())) {
            return null;
        }
        String fileNames = getFileRootPath() + fileName;
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(new File(fileNames)));
            compressBitmap(tempBitmap).compress(Bitmap.CompressFormat.JPEG, 90, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileNames;
    }

    /**
     * 保存Bitmap到指定路径
     *
     * @param tempBitmap
     * @param filePath
     */
    public static void saveBitmapToPath(Bitmap tempBitmap, String filePath) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
            compressBitmap(tempBitmap).compress(Bitmap.CompressFormat.JPEG, 90, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFileRootPath() {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return "";
        }
        File file = new File(FILE_ROOT_PATH);
        if (!file.exists())
            file.mkdirs();// 创建文件夹
        return FILE_ROOT_PATH;
    }

    /**
     * 对Bitmap进行压缩
     *
     * @param image
     * @return
     */
    public static Bitmap compressBitmap(Bitmap image) {
        //防止对本数据进行就该
        Bitmap tempBitmap = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tempBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(isBm, null, options);
        int height = options.outHeight;
        int width = options.outWidth;
        //初始压缩比为1
        int inSampleSize = 1;
        // TODO: 16/3/7 动态设置
        int reqHeight = 1280;
        int reqWidth = 960;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        //重新设置压缩比
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;//降低图片从ARGB888到RGB565
        isBm = new ByteArrayInputStream(baos.toByteArray());
        return BitmapFactory.decodeStream(isBm, null, options);
    }
}
