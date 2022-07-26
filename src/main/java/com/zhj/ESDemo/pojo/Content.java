package com.zhj.ESDemo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Topic
 * Description
 *
 * @author zhouh
 * @version 1.0
 * Create by 2022/7/24 20:44
 */
//@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class Content {
    private String title;
    private String img;
    private String price;
}

