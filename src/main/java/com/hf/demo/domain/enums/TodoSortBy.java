package com.hf.demo.domain.enums;

public enum TodoSortBy {
    CREATED_TIME;

    public String getColumn() {
        return "created_time";
    }
}
