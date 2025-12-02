package com.hf.demo.domain.query;

import com.hf.demo.domain.enums.SortDir;
import com.hf.demo.domain.enums.TodoSortBy;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TodoPageQuery extends PageQuery {
    private TodoSortBy sortBy = TodoSortBy.CREATED_TIME;
    private SortDir sortDir = SortDir.DESC;
}
