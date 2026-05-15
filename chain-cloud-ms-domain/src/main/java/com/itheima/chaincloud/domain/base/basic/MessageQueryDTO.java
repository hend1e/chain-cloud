package com.itheima.chaincloud.domain.base.basic;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 消息查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("消息查询")
public class MessageQueryDTO {

    /**
     * 1：用户端，2：司机端，3：快递员端，4：后台管理系统
     */
    @Max(value = 4, message = "类型值必须是1、2、3、4")
    @Min(value = 1, message = "类型值必须是1、2、3、4")
    @NotNull(message = "业务类型不能为空")
    private Integer bussinessType;

    /**
     * 消息接受者
     */
    private Long userId;

    /**
     * 消息类型，300：快递员端公告，301：寄件相关消息，302：签收相关消息，303：快件取消消息,304:派件相关消息,200：司机端公告，201：司机端系统通知
     */
    private Integer contentType;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 页面大小
     */
    private Integer pageSize;

    /**
     * 消息是否已读，0：未读，1：已读
     */
    private Integer isRead;
}