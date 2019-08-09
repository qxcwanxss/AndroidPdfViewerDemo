package com.xhh.pdfui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.github.barteksc.pdfviewer.util.FileUtils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.xhh.pdfui.preview.GridAdapter;
import com.xhh.pdfui.preview.PreviewUtils;

import java.io.File;

/**
 * UI页面：PDF预览缩略图（注意：此页面，需多关注内存管控）
 * <p>
 * 1、用于显示Pdf缩略图信息
 * 2、点击缩略图，带回Pdf页码到前一个页面
 * <p>
 * 作者：齐行超
 * 日期：2019.08.07
 */
public class PDFPreviewActivity extends AppCompatActivity implements GridAdapter.GridEvent {

    RecyclerView recyclerView;
    Button btn_back;
    PdfiumCore pdfiumCore;
    PdfDocument pdfDocument;
    String assetsFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIUtils.initWindowStyle(getWindow(), getSupportActionBar());
        setContentView(R.layout.activity_preview);

        initView();//初始化控件
        setEvent();
        loadData();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        btn_back = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.rv_grid);
    }

    /**
     * 设置事件
     */
    private void setEvent() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //回收内存
                recycleMemory();

                PDFPreviewActivity.this.finish();
            }
        });

    }

    /**
     * 加载数据
     */
    private void loadData() {
        //加载pdf文件
        loadPdfFile();

        //获得pdf总页数
        int totalCount = pdfiumCore.getPageCount(pdfDocument);

        //绑定列表数据
        GridAdapter adapter = new GridAdapter(this, pdfiumCore, pdfDocument, assetsFileName, totalCount);
        adapter.setGridEvent(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);
    }

    /**
     * 加载pdf文件
     */
    private void loadPdfFile() {
        Intent intent = getIntent();
        if (intent != null) {
            assetsFileName = intent.getStringExtra("AssetsPdf");
            if (assetsFileName != null) {
                loadAssetsPdfFile(assetsFileName);
            } else {
                Uri uri = intent.getData();
                if (uri != null) {
                    loadUriPdfFile(uri);
                }
            }
        }
    }

    /**
     * 加载assets中的pdf文件
     */
    void loadAssetsPdfFile(String assetsFileName) {
        try {
            File f = FileUtils.fileFromAsset(this, assetsFileName);
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfiumCore = new PdfiumCore(this);
            pdfDocument = pdfiumCore.newDocument(pfd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 基于uri加载pdf文件
     */
    void loadUriPdfFile(Uri uri) {
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
            pdfiumCore = new PdfiumCore(this);
            pdfDocument = pdfiumCore.newDocument(pfd);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * 点击缩略图，带回Pdf页码到前一个页面
     *
     * @param position 页码
     */
    @Override
    public void onGridItemClick(int position) {
        //回收内存
        recycleMemory();

        //返回前一个页码
        Intent intent = new Intent();
        intent.putExtra("pageNum", position);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * 回收内存
     */
    private void recycleMemory(){
        //关闭pdf对象
        if (pdfiumCore != null && pdfDocument != null) {
            pdfiumCore.closeDocument(pdfDocument);
            pdfiumCore = null;
        }
        //清空图片缓存，释放内存空间
        PreviewUtils.getInstance().getImageCache().clearCache();
    }
}
