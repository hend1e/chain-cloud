package com.itheima.chaincloud.domain.base.basic;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 最新消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("最新消息")
public class LatestMessageDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 消息类型，300：快递员端公告，301：寄件相关消息，302：签收相关消息，303：快件取消消息，200：司机端公告，201：司机端系统通知
     */
    private Integer contentType;

    /**
     * 相关id
     */
    private Long relevantId;

    /**
     * 创建时间
     */
    private LocalDateTime created;
}