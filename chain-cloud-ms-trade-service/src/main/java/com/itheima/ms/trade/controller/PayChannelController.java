package com.itheima.ms.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.trade.domain.PayChannelDTO;
import com.itheima.ms.trade.entity.PayChannelEntity;
import com.itheima.ms.trade.service.PayChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @ClassName PayChannelController.java
 * @Description 支付通道
 */
@RestController
@RequestMapping("payChannel")
@Slf4j
@Api(tags = "支付通道")
public class PayChannelController {

    @Resource
    private PayChannelService payChannelService;

    /**
     * 支付通道列表
     *
     * @param payChannelDTO 查询条件
     * @return 分页数据对象
     */
    @PostMapping("page/{pageNum}/{pageSize}")
    @ApiOperation(value = "查询支付通道分页", notes = "查询支付通道分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payChannelDTO", value = "支付通道查询对象", required = true),
            @ApiImplicitParam(name = "pageNum", value = "页码"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数")
    })
    public PageResponse<PayChannelDTO> findPayChannelPage(
            @RequestBody PayChannelDTO payChannelDTO,
            @PathVariable("pageNum") int pageNum,
            @PathVariable("pageSize") int pageSize) {
        Page<PayChannelEntity> payChannelVoPage = payChannelService.findPayChannelPage(payChannelDTO, pageNum, pageSize);
        return new PageResponse<>(payChannelVoPage, PayChannelDTO.class);
    }

    /**
     * 添加支付通道
     *
     * @param payChannelDTO 对象信息
     */
    @PostMapping
    @ApiOperation(value = "添加支付通道", notes = "添加支付通道")
    @ApiImplicitParam(name = "payChannelDTO", value = "支付通道对象", required = true)
    public void createPayChannel(@RequestBody PayChannelDTO payChannelDTO) {
        PayChannelEntity payChannel = this.payChannelService.createPayChannel(payChannelDTO);
        if (null != payChannel) {
            return;
        }
        throw new CCException("添加支付通道失败", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * 修改支付通道
     *
     * @param payChannelDTO 对象信息
     */
    @PatchMapping
    @ApiOperation(value = "修改支付通道", notes = "修改支付通道")
    @ApiImplicitParam(name = "payChannelDTO", value = "支付通道对象", required = true)
    public void updatePayChannel(@RequestBody PayChannelDTO payChannelDTO) {
        Boolean flag = this.payChannelService.updatePayChannel(payChannelDTO);
        if (flag) {
            return;
        }
        throw new CCException("修改支付通道失败", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * 删除支付通道
     *
     * @param payChannelDTO 查询对象
     */
    @DeleteMapping
    @ApiOperation(value = "删除支付通道", notes = "删除支付通道")
    @ApiImplicitParam(name = "payChannelDTO", value = "支付通道查询对象", required = true)
    public void deletePayChannel(@RequestBody PayChannelDTO payChannelDTO) {
        String[] checkedIds = payChannelDTO.getCheckedIds();
        Boolean flag = this.payChannelService.deletePayChannel(checkedIds);
        if (flag) {
            return;
        }
        throw new CCException("删除支付通道失败", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @PostMapping("update-payChannel-enableFlag")
    @ApiOperation(value = "修改支付通道状态", notes = "修改支付通道状态")
    @ApiImplicitParam(name = "payChannelDTO", value = "支付通道查询对象", required = true)
    public void updatePayChannelEnableFlag(@RequestBody PayChannelDTO payChannelDTO) {
        Boolean flag = this.payChannelService.updatePayChannel(payChannelDTO);
        if (flag) {
            return;
        }
        throw new CCException("修改支付通道状态失败", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
