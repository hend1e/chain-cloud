package com.itheima.ms.search.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Person对象
 *
 * @Author itcast
 * @Create 2023/3/7 15:33
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Person {

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 姓名
     */
    private String name;

    /**
     * 电话
     */
    private String phone;

    /**
     * 地址
     */
    private String address;
}
