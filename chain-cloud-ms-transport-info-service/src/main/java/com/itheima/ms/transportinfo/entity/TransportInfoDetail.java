package com.itheima.ms.transportinfo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportInfoDetail {

    private Long created;
    private String info;
    private String status;
}
