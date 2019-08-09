package com.xhh.pdfui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.xhh.pdfui.tree.TreeAdapter;
import com.xhh.pdfui.tree.TreeNodeData;

import java.util.List;

/**
 * UI页面：PDF目录
 * <p>
 * 1、用于显示Pdf目录信息
 * 2、点击tree item，带回Pdf页码到前一个页面
 * <p>
 * 作者：齐行超
 * 日期：2019.08.07
 */
public class PDFCatelogueActivity extends AppCompatActivity implements TreeAdapter.TreeEvent {

    RecyclerView recyclerView;
    Button btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIUtils.initWindowStyle(getWindow(), getSupportActionBar());
        setContentView(R.layout.activity_catelogue);

        initView();//初始化控件
        setEvent();//设置事件
        loadData();//加载数据
    }

    /**
     * 初始化控件
     */
    private void initView() {
        btn_back = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.rv_tree);
    }

    /**
     * 设置事件
     */
    private void setEvent() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PDFCatelogueActivity.this.finish();
            }
        });
    }

    /**
     * 加载数据
     */
    private void loadData() {
        //从intent中获得传递的数据
        Intent intent = getIntent();
        List<TreeNodeData> catelogues = (List<TreeNodeData>) intent.getSerializableExtra("catelogues");

        //使用RecyclerView加载数据
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        TreeAdapter adapter = new TreeAdapter(this, catelogues);
        adapter.setTreeEvent(this);
        recyclerView.setAdapter(adapter);
    }


    /**
     * 点击tree item，带回Pdf页码到前一个页面
     *
     * @param data tree节点数据
     */
    @Override
    public void onSelectTreeNode(TreeNodeData data) {
        Intent intent = new Intent();
        intent.putExtra("pageNum", data.getPageNum());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
