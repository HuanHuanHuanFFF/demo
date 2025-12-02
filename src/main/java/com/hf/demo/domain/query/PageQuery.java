package com.hf.demo.domain.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQuery {
    @Min(1)
    private Long pageIndex;
    @Min(1)
    @Max(50)
    private Long pageSize;
}
