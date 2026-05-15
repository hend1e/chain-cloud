package com.itheima.chaincloud.domain.base.truck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机构id
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganIdsDTO {

    /**
     * 起始机构id
     */
    private Long startOrganId;

    /**
     * 结束机构id
     */
    private Long endOrganId;
}