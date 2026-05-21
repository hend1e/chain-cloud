package com.itheima.chaincloud.api.aigc;

import com.itheima.chaincloud.domain.aigc.domain.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@FeignClient(name = "chain-cloud-ms-aigc", contextId = "AIGC", path = "/aigc")
public interface AIGCFeign {

    /**
     * 登录账号的风险评估
     *
     * @param account    评估账号
     * @param userType   用户类型，1-管理员 2-快递员 3-司机 4-用户
     * @param loginIp    登录 IP 地址
     * @param deviceInfo 设备信息
     * @return 风险评估结果
     */
    @PostMapping("/risk/login")
    RiskLoginResponse assessLoginRisk(@RequestParam("account") String account,
                                      @RequestParam("userType") Integer userType,
                                      @RequestParam("loginIp") String loginIp,
                                      @RequestParam("deviceInfo") String deviceInfo);

    /**
     * refreshToken 风险评估
     *
     * @param body 评估的信息
     * @return 风险评估结果
     */
    @PostMapping("/risk/refreshToken")
    RiskRefreshTokenResponse assessRefreshTokenRisk(@RequestBody String body);

    /**
     * 包裹物品检查 - 判断物品分类及是否禁寄
     *
     * @param itemName 物品名称
     * @return 检查结果
     */
    @PostMapping("/parcel/item/check")
    ParcelItemCheckResponse checkParcelItem(@RequestParam("itemName") String itemName);

    /**
     * 运费计算 - 根据运输距离和重量计算运费
     *
     * @param question 运输信息描述（例如：从北京到上海，5kg）
     * @return 运费计算结果
     */
    @PostMapping("/carriage/compute")
    ComputeCarriageResponse computeCarriage(@RequestParam("question") String question);

    /**
     * 机构类型识别 - 根据机构信息判断机构类型（一级、二级、网点）
     *
     * @param organInfo 机构信息（包含名称、ID、父级信息等），示例：
     *                  <pre>
     *                                                                     {
     *                                                                       "id": "977263044792942657",
     *                                                                       "name": "上海浦东直营网点",
     *                                                                       "knownRules": {
     *                                                                         "转运中心": "OLT",
     *                                                                         "分拣中心": "TLT",
     *                                                                         "营业部": "AGENCY"
     *                                                                       },
     *                                                                       "allowedTypes": ["OLT", "TLT", "AGENCY", "UNKNOWN"]
     *                                                                     }
     *                                                                     </pre>
     * @return 机构类型识别结果
     */
    @PostMapping("/organ/identify-type")
    OrganTypeResponse identifyOrganType(@RequestParam("organInfo") String organInfo);

    /**
     * 路线成本因子识别 - 根据路线信息分析成本计算因子
     *
     * @param routeInfo 路线信息，示例：
     *                  <pre>
     *                                                                     请根据以下新建路线信息输出路线 cost 修正因子 JSON。
     *
     *                                                                     【路线基础信息】
     *                                                                     - lineType: TRUNK_LINE
     *                                                                     - startOrganId: 1001
     *                                                                     - endOrganId: 2001
     *                                                                     - startOrganName: 广州转运中心
     *                                                                     - endOrganName: 武汉转运中心
     *                                                                     - startOrganLevel: OLT
     *                                                                     - endOrganLevel: OLT
     *                                                                     - startCoordinate: 113.2644,23.1291
     *                                                                     - endCoordinate: 114.3054,30.5931
     *
     *                                                                     【基础成本信息】
     *                                                                     - unitCostByType: 0.80
     *                                                                     - baseCost: 4077.00
     *
     *                                                                     【程序已知上下文】
     *                                                                     - sameCity: false
     *                                                                     - sameProvince: false
     *                                                                     - crossProvince: true
     *                                                                     - preCalculatedDistanceKm: 1770.90
     *                                                                     - preCalculatedDurationHour: 19.20
     *                                                                     - straightLineDistanceKm: 1320.00
     *
     *
     *                                                                     要求：
     *                                                                     1. 你可以自主决定是否调用高德 MCP 工具；
     *                                                                     2. 如果已有输入足够，请减少无意义工具调用；
     *                                                                     3. 你只能输出 JSON；
     *                                                                     4. 不要输出 finalCost；
     *                                                                     5. 所有 factor 必须在合法范围内；
     *                                                                     6. 信息不足时采用保守输出。
     *                                                                     </pre>
     * @return 路线成本因子识别结果
     */
    @PostMapping("/cost-factor/identify-factor")
    CostFactorResponse identifyCostFactor(@RequestParam("routeInfo") String routeInfo);

    /**
     * 服务范围分析
     *
     * @param question 发件人地址信息，示例：
     *                 <pre>
     *                                                                 上海市浦东新区航头镇航都路18号
     *                                                                 </pre>
     * @return 服务范围分析结果，包含推荐站点、候选站点、置信度等信息
     */
    @PostMapping("/service-scope/analyze")
    ServiceScopeResponse analyzeServiceScope(@RequestParam("question") String question);

    /**
     * 快递员智能调度 - 根据快递员信息和位置进行智能调度
     *
     * @param request 调度请求信息，包含快递员ID列表、位置信息等，示例：
     *                <pre>
     *                                                             {
     *                                                               "courierIds": [1, 2, 3],
     *                                                               "longitude": 116.111,
     *                                                               "latitude": 39.0
     *                                                             }
     *                                                             </pre>
     * @return 调度结果，包含选中的快递员ID和选择原因
     */
    @PostMapping("/courier-dispatch/dispatch")
    CourierDispatchResponse courierDispatch(@RequestBody CourierDispatchRequest request);

    /**
     * 运输订单智能调度
     *
     * @param request 调度请求信息（包含运输订单信息、会话信息等）
     *                示例：
     *                {
     *                "total_weight": 10,
     *                "total_volume": 2,
     *                "created": 1710000000,
     *                "route_order_count": 20
     *                }
     * @return 调度结果（包含优先级评分和原因）
     */
    @PostMapping("/transport-order-dispatch/dispatch")
    TransportOrderDispatchResponse transportOrderDispatch(@RequestBody String request);

    /**
     * 运输任务预测
     *
     * @param question 任务预测问题内容（JSON格式的运输信息）
     *                 示例：
     *                 {
     *                 "usedWeightRatio": 0.75,
     *                 "usedVolumeRatio": 0.70,
     *                 "orderCount": 15,
     *                 "avgWeight": 120.0,
     *                 "stdWeight": 30.0,
     *                 "maxOrderWeightRatio": 0.2,
     *                 "skippedOrderCount": 2,
     *                 "remainingOrderCount": 5,
     *                 "remainingHighPriorityRatio": 0.3,
     *                 "rounds": 2
     *                 }
     * @return 任务预测结果（包含预测标签、置信度和原因）
     */
    @PostMapping("/transport-task-dispatch/chat")
    TransportTaskDispatchResponse transportTaskDispatch(@RequestBody String question);

}
