package com.hf.demo.domain.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PageQuery {
    @Min(1)
    @NotNull
    private Long pageIndex;
    @Min(1)
    @Max(50)
    @NotNull
    private Long pageSize;
}
