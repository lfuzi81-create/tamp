package com.tamp.common.dto;

import java.util.List;

/**
 * 分页响应封装
 */
public class PageResult<T> {

    /** 数据列表 */
    private List<T> list;

    /** 当前页码（从1开始） */
    private long pageNum;

    /** 每页大小 */
    private long pageSize;

    /** 总记录数 */
    private long total;

    /** 总页数 */
    private long pages;

    public PageResult() {}

    public PageResult(List<T> list, long pageNum, long pageSize, long total) {
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (total + pageSize - 1) / pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    /**
     * 从 Spring Data Page 对象转换
     */
    public static <T> PageResult<T> of(org.springframework.data.domain.Page<T> page) {
        return new PageResult<>(
            page.getContent(),
            page.getNumber() + 1,
            page.getSize(),
            page.getTotalElements()
        );
    }
}
