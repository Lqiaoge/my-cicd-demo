package com.windcore.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 乔哥
 * @version v1.0
 * @Date: 2025-01-07 22:49
 * @Desctiption: 分页参数
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
