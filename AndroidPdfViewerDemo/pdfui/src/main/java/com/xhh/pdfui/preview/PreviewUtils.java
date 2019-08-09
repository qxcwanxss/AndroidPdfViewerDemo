package com.xhh.pdfui.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 预览缩略图工具类
 *
 * 1、pdf页面转为缩略图
 * 2、图片缓存管理（仅保存到内存，可使用LruCache，注意空间大小控制）
 * 3、多线程管理（线程并发、阻塞、Future任务取消）
 *
 * 作者：齐行超
 * 日期：2019.08.08
 */
public class PreviewUtils {
    //图片缓存管理
    private ImageCache imageCache;
    //单例
    private static PreviewUtils instance;
    //线程池
    ExecutorService executorService;
    //线程任务集合（可用于取消任务）
    HashMap<String, Future> tasks;

    /**
     * 单例（仅主线程调用，无需做成线程安全的）
     *
     * @return PreviewUtils实例对象
     */
    public static PreviewUtils getInstance() {
        if (instance == null) {
            instance = new PreviewUtils();
        }
        return instance;
    }

    /**
     * 默认构造函数
     */
    private PreviewUtils() {
        //初始化图片缓存管理对象
        imageCache = new ImageCache();
        //创建并发线程池(建议大于1屏grid item的数量)
        executorService = Executors.newFixedThreadPool(20);
        //创建线程任务集合
        tasks = new HashMap<>();
    }

    /**
     * 从pdf文件中加载图片
     *
     * @param context     上下文
     * @param imageView   图片控件
     * @param pdfiumCore  pdf核心对象
     * @param pdfDocument pdf文档对象
     * @param pdfName     pdf文件名称
     * @param pageNum     pdf页码
     */
    public void loadBitmapFromPdf(final Context context,
                                  final ImageView imageView,
                                  final PdfiumCore pdfiumCore,
                                  final PdfDocument pdfDocument,
                                  final String pdfName,
                                  final int pageNum) {
        //判断参数合法性
        if (imageView == null || pdfiumCore == null || pdfDocument == null || pageNum < 0) {
            return;
        }

        try {
            //缓存key
            final String keyPage = pdfName + pageNum;

            //为图片控件设置标记
            imageView.setTag(keyPage);

            Log.i("PreViewUtils", "加载pdf缩略图：" + keyPage);

            //获得imageview的尺寸（注意：如果使用正常控件尺寸，太占内存了）
            /*int w = imageView.getMeasuredWidth();
            int h = imageView.getMeasuredHeight();
            final int reqWidth = w == 0 ? UIUtils.dip2px(context,100) : w;
            final int reqHeight = h == 0 ? UIUtils.dip2px(context,150) : h;*/

            //内存大小= 图片宽度 * 图片高度 * 一个像素占的字节数（RGB_565 所占字节：2）
            //注意：如果使用正常控件尺寸，太占内存了，所以此处指定四缩略图看着会模糊一点
            final int reqWidth = 100;
            final int reqHeight = 150;

            //从缓存中取图片
            Bitmap bitmap = imageCache.getBitmapFromLruCache(keyPage);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }

            //使用缓存线程池管理子线程
            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    //打开页面（调用renderPageBitmap方法之前，必须确保页面已open，重要）
                    pdfiumCore.openPage(pdfDocument, pageNum);

                    //调用native方法，将Pdf页面渲染成图片
                    final Bitmap bm = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.RGB_565);
                    pdfiumCore.renderPageBitmap(pdfDocument, bm, pageNum, 0, 0, reqWidth, reqHeight);

                    //切回主线程，设置图片
                    if (bm != null) {
                        //将图片加入缓存
                        imageCache.addBitmapToLruCache(keyPage, bm);

                        //切回主线程加载图片
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (imageView.getTag().toString().equals(keyPage)) {
                                    imageView.setImageBitmap(bm);
                                    Log.i("PreViewUtils", "加载pdf缩略图：" + keyPage + "......已设置！！");
                                }
                            }
                        });
                    }
                }
            });

            //将任务添加到集合
            tasks.put(keyPage, future);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 取消从pdf文件中加载图片的任务
     *
     * @param keyPage 页码
     */
    public void cancelLoadBitmapFromPdf(String keyPage) {
        if (keyPage == null || !tasks.containsKey(keyPage)) {
            return;
        }
        try {
            Log.i("PreViewUtils", "取消加载pdf缩略图：" + keyPage);
            Future future = tasks.get(keyPage);
            if (future != null) {
                future.cancel(true);
                Log.i("PreViewUtils", "取消加载pdf缩略图：" + keyPage + "......已取消！！");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获得图片缓存对象
     * @return 图片缓存
     */
    public ImageCache getImageCache(){
        return imageCache;
    }

    /**
     * 图片缓存管理
     */
   public class ImageCache {
        //图片缓存
        private LruCache<String, Bitmap> lruCache;

        //构造函数
        public ImageCache() {
            //初始化 lruCache
            //int maxMemory = (int) Runtime.getRuntime().maxMemory();
            //int cacheSize = maxMemory/8;
            int cacheSize = 1024 * 1024 * 30;//暂时设定30M
            lruCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes() * value.getHeight();
                }
            };
        }

        /**
         * 从缓存中取图片
         * @param key 键
         * @return 图片
         */
        public synchronized Bitmap getBitmapFromLruCache(String key) {
            if(lruCache!= null) {
                return lruCache.get(key);
            }
            return null;
        }

        /**
         * 向缓存中加图片
         * @param key 键
         * @param bitmap 图片
         */
        public synchronized void addBitmapToLruCache(String key, Bitmap bitmap) {
            if (getBitmapFromLruCache(key) == null) {
                if (lruCache!= null && bitmap != null)
                    lruCache.put(key, bitmap);
            }
        }

        /**
         * 清空缓存
         */
        public void clearCache(){
            if(lruCache!= null){
                lruCache.evictAll();
            }
        }
    }
}
