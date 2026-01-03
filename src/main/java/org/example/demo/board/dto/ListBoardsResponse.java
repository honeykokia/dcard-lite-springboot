package org.example.demo.board.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ListBoardsResponse {

    @Min(1)
    private int page;

    @Min(1)
    private int pageSize;

    @Min(0)
    private long total;

    @NotNull
    @Valid
    private List<BoardItem> items;

    public ListBoardsResponse() {
    }

    public ListBoardsResponse(int page, int pageSize, long total, List<BoardItem> items) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<BoardItem> getItems() {
        return items;
    }

    public void setItems(List<BoardItem> items) {
        this.items = items;
    }
}
