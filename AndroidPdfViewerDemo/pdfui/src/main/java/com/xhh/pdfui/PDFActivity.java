package com.xhh.pdfui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.PdfDocument;
import com.xhh.pdfui.tree.TreeNodeData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * UI页面：PDF阅读
 * <p>
 * 主要功能：
 * 1、接收传递过来的pdf文件（包括assets中的文件名、文件uri）
 * 2、显示PDF文件
 * 3、接收目录页面、预览页面返回的PDF页码，跳转到指定的页面
 * <p>
 * 作者：齐行超
 * 日期：2019.08.07
 */
public class PDFActivity extends AppCompatActivity implements
        OnPageChangeListener,
        OnLoadCompleteListener,
        OnPageErrorListener {
    //PDF控件
    PDFView pdfView;
    //按钮控件：返回、目录、缩略图
    Button btn_back, btn_catalogue, btn_preview;
    //页码
    Integer pageNumber = 0;
    //PDF目录集合
    List<TreeNodeData> catelogues;

    //pdf文件名（限：assets里的文件）
    String assetsFileName;
    //pdf文件uri
    Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIUtils.initWindowStyle(getWindow(), getSupportActionBar());//设置沉浸式
        setContentView(R.layout.activity_pdf);

        initView();//初始化view
        setEvent();//设置事件
        loadPdf();//加载PDF文件
    }

    /**
     * 初始化view
     */
    private void initView() {
        pdfView = findViewById(R.id.pdfView);
        btn_back = findViewById(R.id.btn_back);
        btn_catalogue = findViewById(R.id.btn_catalogue);
        btn_preview = findViewById(R.id.btn_preview);
    }

    /**
     * 设置事件
     */
    private void setEvent() {
        //返回
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PDFActivity.this.finish();
            }
        });
        //跳转目录页面
        btn_catalogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PDFActivity.this, PDFCatelogueActivity.class);
                intent.putExtra("catelogues", (Serializable) catelogues);
                PDFActivity.this.startActivityForResult(intent, 200);
            }
        });
        //跳转缩略图页面
        btn_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PDFActivity.this, PDFPreviewActivity.class);
                intent.putExtra("AssetsPdf", assetsFileName);
                intent.setData(uri);
                PDFActivity.this.startActivityForResult(intent, 201);
            }
        });
    }

    /**
     * 加载PDF文件
     */
    private void loadPdf() {
        Intent intent = getIntent();
        if (intent != null) {
            assetsFileName = intent.getStringExtra("AssetsPdf");
            if (assetsFileName != null) {
                displayFromAssets(assetsFileName);
            } else {
                uri = intent.getData();
                if (uri != null) {
                    displayFromUri(uri);
                }
            }
        }
    }

    /**
     * 基于assets显示 PDF 文件
     *
     * @param fileName 文件名称
     */
    private void displayFromAssets(String fileName) {
        pdfView.fromAsset(fileName)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // 单位 dp
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
    }

    /**
     * 基于uri显示 PDF 文件
     *
     * @param uri 文件路径
     */
    private void displayFromUri(Uri uri) {
        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // 单位 dp
                .onPageError(this)
                .load();
    }

    /**
     * 当成功加载PDF：
     * 1、可获取PDF的目录信息
     *
     * @param nbPages the number of pages in this PDF file
     */
    @Override
    public void loadComplete(int nbPages) {
        //获得文档书签信息
        List<PdfDocument.Bookmark> bookmarks = pdfView.getTableOfContents();
        if (catelogues != null) {
            catelogues.clear();
        } else {
            catelogues = new ArrayList<>();
        }
        //将bookmark转为目录数据集合
        bookmarkToCatelogues(catelogues, bookmarks, 1);
    }

    /**
     * 将bookmark转为目录数据集合（递归）
     *
     * @param catelogues 目录数据集合
     * @param bookmarks  书签数据
     * @param level      目录树级别（用于控制树节点位置偏移）
     */
    private void bookmarkToCatelogues(List<TreeNodeData> catelogues, List<PdfDocument.Bookmark> bookmarks, int level) {
        for (PdfDocument.Bookmark bookmark : bookmarks) {
            TreeNodeData nodeData = new TreeNodeData();
            nodeData.setName(bookmark.getTitle());
            nodeData.setPageNum((int) bookmark.getPageIdx());
            nodeData.setTreeLevel(level);
            nodeData.setExpanded(false);
            catelogues.add(nodeData);
            if (bookmark.getChildren() != null && bookmark.getChildren().size() > 0) {
                List<TreeNodeData> treeNodeDatas = new ArrayList<>();
                nodeData.setSubset(treeNodeDatas);
                bookmarkToCatelogues(treeNodeDatas, bookmark.getChildren(), level + 1);
            }
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
    }

    @Override
    public void onPageError(int page, Throwable t) {
    }

    /**
     * 从缩略图、目录页面带回页码，跳转到指定PDF页面
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            int pageNum = data.getIntExtra("pageNum", 0);
            if (pageNum > 0) {
                pdfView.jumpTo(pageNum);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //是否内存
        if (pdfView != null) {
            pdfView.recycle();
        }
    }
}
