package org.redhat.bpm.model;

import java.util.ArrayList;
import java.util.List;


public class Page<T> {

    private int total;
    private int page;
    private int pageSize;
    private boolean asc;
    private List<T> items = new ArrayList<>();

    public Page(int total, int page, int pageSize, boolean asc) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.asc = asc;
    }

    public Page(int total, int page, int pageSize, boolean asc, List<T> items) {
        this(total, page, pageSize, asc);
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotal() {
        return total;
    }

    public boolean isAsc() {
        return asc;
    }

    public List<T> getItems() {
        return items;
    }

}
