package com.itheima.ms.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.chaincloud.domain.auth.domain.LoginRecordDTO;
import com.itheima.ms.auth.entity.LoginRecordEntity;

import java.util.List;

public interface LoginRecordService extends IService<LoginRecordEntity> {

    /**
     * 查询登录记录（最近10条）
     *
     * @param account  登录账号
     * @param userType 用户类型，1-管理员 2-快递员 3-司机 4-用户
     * @return 登录记录列表
     */
    List<LoginRecordDTO> queryRecordList(String account, Integer userType);

    /**
     * 保存登录记录
     *
     * @param account     登录账号
     * @param userType    用户类型，1-管理员 2-快递员 3-司机 4-用户
     * @param loginIp     登录 IP 地址
     * @param deviceInfo  设备信息
     * @param loginStatus 登录状态，0-失败 1-成功
     * @param failReason  失败原因
     */
    void saveLoginRecord(String account, Integer userType, String loginIp,
                         String deviceInfo, Integer loginStatus, String failReason);
}
