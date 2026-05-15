package com.itheima.ms.transport.mq.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthOrgMessage {
    private String type;
    private String operation;
    private List<AuthOrgContent> content = List.of();
}
