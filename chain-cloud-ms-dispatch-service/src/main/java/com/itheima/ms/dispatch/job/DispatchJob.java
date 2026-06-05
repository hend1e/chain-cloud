package com.itheima.ms.dispatch.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 调度运输任务
 * <p>
 * 核心能力：
 * 1. 使用 Redis ZSet 实现"优先级调度"
 * 2. 支持跳过大单（避免阻塞）
 * 3. 批量拉取减少 Redis IO
 * 4. 无递归，避免栈溢出
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchJob {

    /**
     * XXL-Job入口
     * <p>
     * 分片广播模式：
     * 每个执行器只处理一部分车辆，避免重复调度
     */
    @XxlJob("transportTask")
    public void transportTask() {
        //TODO 待实现
    }

}
