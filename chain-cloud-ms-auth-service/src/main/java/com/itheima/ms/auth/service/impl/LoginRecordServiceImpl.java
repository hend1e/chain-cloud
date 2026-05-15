package com.itheima.ms.auth.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.chaincloud.common.util.BeanUtil;
import com.itheima.chaincloud.domain.auth.domain.LoginRecordDTO;
import com.itheima.ms.auth.entity.LoginRecordEntity;
import com.itheima.ms.auth.mapper.LoginRecordMapper;
import com.itheima.ms.auth.service.LoginRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginRecordServiceImpl extends ServiceImpl<LoginRecordMapper, LoginRecordEntity> implements LoginRecordService {

    @Override
    public List<LoginRecordDTO> queryRecordList(String account, Integer userType) {
        var queryWrapper = Wrappers.<LoginRecordEntity>lambdaQuery()
                .eq(StrUtil.isNotEmpty(account), LoginRecordEntity::getAccount, account)
                .eq(ObjectUtil.isNotEmpty(userType), LoginRecordEntity::getUserType, userType)
                .orderByDesc(LoginRecordEntity::getLoginTime)
                .last("LIMIT 10");

        var recordList = super.list(queryWrapper);
        return CollStreamUtil.toList(recordList, entity -> BeanUtil.toBean(entity, LoginRecordDTO.class));
    }

    @Async("loginRecordExecutor")
    @Override
    public void saveLoginRecord(String account, Integer userType, String loginIp,
                                String deviceInfo, Integer loginStatus, String failReason) {
        LoginRecordEntity loginRecord = LoginRecordEntity.builder()
                .account(account)
                .userType(userType)
                .loginIp(loginIp)
                .deviceInfo(deviceInfo)
                .loginStatus(loginStatus)
                .failReason(loginStatus == 1 ? null : failReason)
                .loginTime(LocalDateTime.now())
                .build();

        super.save(loginRecord);

        log.debug("保存登录记录，账号：{}, 用户类型：{}, 状态：{}", account, userType, loginStatus == 1 ? "成功" : "失败");
    }
}
