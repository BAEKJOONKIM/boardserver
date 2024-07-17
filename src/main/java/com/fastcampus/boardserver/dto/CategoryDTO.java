package com.fastcampus.boardserver.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    public enum SortStatus{
        CATEGORY, NEWEST, OLDEST, HIGHPRICE, LOWPRICE, GRADE
    }

    private int id;
    private String name;
    private SortStatus sortStatus;
    private int searchCount;
    private int pagingStartOffset;

}
