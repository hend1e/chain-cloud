package com.itheima.ms.aigc.controller;

import com.itheima.ms.aigc.service.OrganTypeService;
import com.itheima.chaincloud.domain.aigc.domain.OrganTypeResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(tags = "AI 机构类型识别接口")
@RequestMapping("/aigc/organ")
@RequiredArgsConstructor
public class OrganTypeAgentController {

    private final OrganTypeService organTypeService;

    /**
     * 机构类型识别
     *
     * @param organInfo 机构信息（包含名称、ID、父级信息等），示例：
     * <pre>
     * {
     *   "name": "上海浦东直营网点",
     *   "id": "977263044792942657",
     *   "parentId": "973902113476182273",
     *   "operation": "UPDATE",
     *   "status": true,
     *   "knownRules": {
     *     "转运中心": "OLT",
     *     "分拣中心": "TLT",
     *     "营业部": "AGENCY"
     *   },
     *   "allowedTypes": ["OLT", "TLT", "AGENCY", "UNKNOWN"]
     * }
     * </pre>
     * @return 机构类型识别结果
     */
    @PostMapping("/identify-type")
    @ApiOperation("机构类型识别 - 根据机构信息判断机构类型（一级、二级、网点）")
    public OrganTypeResponse identifyOrganType(
            @ApiParam("机构信息") @RequestParam("organInfo") String organInfo) {
        return organTypeService.identifyOrganType(organInfo);
    }
}