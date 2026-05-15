package com.itheima.chaincloud.mq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.chaincloud.mq.entity.FailMsgEntity;
import com.itheima.chaincloud.mq.mapper.FailMsgMapper;
import com.itheima.chaincloud.mq.service.FailMsgService;
import com.itheima.chaincloud.mq.service.MQService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * 失败消息处理服务
 *
 * @author zzj
 * @version 1.0
 */
@Service
@ConditionalOnBean(MQService.class)
public class FailMsgServiceImpl extends ServiceImpl<FailMsgMapper, FailMsgEntity>
        implements FailMsgService {
}
