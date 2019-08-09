package com.xhh.pdfui.tree;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xhh.pdfui.R;
import com.xhh.pdfui.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形控件适配器
 * 作者：齐行超
 * 日期：2019.08.07
 */
public class TreeAdapter extends RecyclerView.Adapter<TreeAdapter.TreeNodeViewHolder> {
    //上下文
    private Context context;
    //数据
    public List<TreeNodeData> data;
    //展示数据（由层级结构改为平面结构）
    public List<TreeNodeData> displayData;
    //treelevel间隔(dp)
    private int maginLeft;
    //委托对象
    private TreeEvent delegate;

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param data    数据
     */
    public TreeAdapter(Context context, List<TreeNodeData> data) {
        this.context = context;
        this.data = data;
        maginLeft = UIUtils.dip2px(context, 20);
        displayData = new ArrayList<>();

        //数据转为展示数据
        dataToDiaplayData(data);
    }

    /**
     * 数据转为展示数据
     *
     * @param data 数据
     */
    private void dataToDiaplayData(List<TreeNodeData> data) {
        for (TreeNodeData nodeData : data) {
            displayData.add(nodeData);
            if (nodeData.isExpanded() && nodeData.getSubset() != null) {
                dataToDiaplayData(nodeData.getSubset());
            }
        }
    }

    /**
     * 数据集合转为可显示的集合
     */
    private void reDataToDiaplayData() {
        if (this.data == null || this.data.size() == 0) {
            return;
        }
        if(displayData == null){
            displayData = new ArrayList<>();
        }else{
            displayData.clear();
        }
        dataToDiaplayData(this.data);
        notifyDataSetChanged();
    }

    @Override
    public TreeNodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tree_item, null);
        return new TreeNodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TreeNodeViewHolder holder, int position) {
        final TreeNodeData data = displayData.get(position);
        //设置图片
        if (data.getSubset() != null) {
            holder.img.setVisibility(View.VISIBLE);
            if (data.isExpanded()) {
                holder.img.setImageResource(R.drawable.arrow_h);
            } else {
                holder.img.setImageResource(R.drawable.arrow_v);
            }
        } else {
            holder.img.setVisibility(View.INVISIBLE);
        }
        //设置图片偏移位置
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.img.getLayoutParams();
        int ratio = data.getTreeLevel() <= 0? 0 : data.getTreeLevel()-1;
        params.setMargins(maginLeft * ratio, 0, 0, 0);
        holder.img.setLayoutParams(params);

        //显示文本
        holder.title.setText(data.getName());
        holder.pageNum.setText(String.valueOf(data.getPageNum()));

        //图片点击事件
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //控制树节点展开、折叠
                data.setExpanded(!data.isExpanded());
                //刷新数据源
                reDataToDiaplayData();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //回调结果
                if(delegate!=null){
                    delegate.onSelectTreeNode(data);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayData.size();
    }

    /**
     * 定义RecyclerView的ViewHolder对象
     */
    class TreeNodeViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title;
        TextView pageNum;

        public TreeNodeViewHolder(View view) {
            super(view);
            img = view.findViewById(R.id.iv_arrow);
            title = view.findViewById(R.id.tv_title);
            pageNum = view.findViewById(R.id.tv_pagenum);
        }
    }

    /**
     * 接口：Tree事件
     */
    public interface TreeEvent{
        /**
         * 当选择了某tree节点
         * @param data tree节点数据
         */
        void onSelectTreeNode(TreeNodeData data);
    }

    /**
     * 设置Tree的事件
     * @param treeEvent Tree的事件对象
     */
    public void setTreeEvent(TreeEvent treeEvent){
        this.delegate = treeEvent;
    }
}
