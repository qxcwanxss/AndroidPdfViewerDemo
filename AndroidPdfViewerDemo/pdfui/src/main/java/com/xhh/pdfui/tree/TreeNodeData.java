package com.xhh.pdfui.tree;

import java.io.Serializable;
import java.util.List;

/**
 * 树形控件数据类（会用于页面间传输，所以需实现Serializable 或 Parcelable）
 * 作者：齐行超
 * 日期：2019.08.07
 */
public class TreeNodeData implements Serializable {
    //名称
    private String name;
    //页码
    private int pageNum;
    //是否已展开（用于控制树形节点图片显示，即箭头朝向图片）
    private boolean isExpanded;
    //展示级别(1级、2级...，用于控制树形节点缩进位置)
    private int treeLevel;
    //子集（用于加载子节点，也用于判断是否显示箭头图片，如集合不为空，则显示）
    private List<TreeNodeData> subset;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public int getTreeLevel() {
        return treeLevel;
    }

    public void setTreeLevel(int treeLevel) {
        this.treeLevel = treeLevel;
    }

    public List<TreeNodeData> getSubset() {
        return subset;
    }

    public void setSubset(List<TreeNodeData> subset) {
        this.subset = subset;
    }
}
