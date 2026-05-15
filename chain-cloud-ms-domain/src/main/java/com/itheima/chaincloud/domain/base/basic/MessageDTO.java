package com.itheima.chaincloud.domain.base.basic;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("消息")
public class MessageDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 1：用户端，2：司机端，3：快递员端，4：后台管理系统
     */
    private Integer bussinessType;

    /**
     * 消息接受者
     */
    private Long userId;

    /**
     * 消息类型，300：快递员端公告，301：寄件相关消息，302：签收相关消息，303：快件取消消息，200：司机端公告，201：司机端系统通知
     */
    private Integer contentType;

    /**
     * 消息是否已读，0：未读，1：已读
     */
    private Integer isRead;

    /**
     * 读时间
     */
    private LocalDateTime readTime;

    /**
     * 相关id
     */
    private Long relevantId;

    /**
     * 创建者
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private LocalDateTime created;

    /**
     * 更新者
     */
    private Long updateUser;

    /**
     * 更新时间
     */
    private LocalDateTime updated;
}