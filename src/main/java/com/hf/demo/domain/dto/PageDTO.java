package com.hf.demo.domain.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageDTO<T> implements Serializable {
    private Long pageIndex;
    private Long pageSize;
    private Long total;
    private Long pages;
    private List<T> rows;

    public static <T> PageDTO<T> create(Page<T> page) {
        PageDTO<T> pageResult = new PageDTO<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setRows(page.getRecords());
        pageResult.setPageIndex(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setPages(page.getPages());
        return pageResult;
    }

}
