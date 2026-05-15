package com.itheima.ms.transport.mq.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthOrgContent {
    private Long id;
    private String name;
    private Long parentId;
    private String managerId;
    private Boolean status;
}
