package com.itheima.ms.transportinfo.mq;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.transport.OrganFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.vo.TransportInfoMsg;
import com.itheima.chaincloud.domain.transport.domain.OrganDTO;
import com.itheima.ms.transportinfo.entity.TransportInfoDetail;
import com.itheima.ms.transportinfo.service.TransportInfoService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TransportInfoMQListener {

    @Resource
    private OrganFeign organFeign;
    @Resource
    private TransportInfoService transportInfoService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.TRANSPORT_INFO_APPEND),
            exchange = @Exchange(name = Constants.MQ.Exchanges.TRANSPORT_INFO, type = ExchangeTypes.TOPIC),
            key = Constants.MQ.RoutingKeys.TRANSPORT_INFO_APPEND
    ))
    public void listenTransportInfoMsg(String msg) {
        TransportInfoMsg transportInfoMsg = JSONUtil.toBean(msg, TransportInfoMsg.class);
        Long organId = transportInfoMsg.getOrganId();
        String transportOrderId = Convert.toStr(transportInfoMsg.getTransportOrderId());
        String info = transportInfoMsg.getInfo();

        if (StrUtil.contains(info, "$organId")) {
            OrganDTO organDTO = this.organFeign.queryById(organId);
            if (organDTO == null) {
                return;
            }
            info = StrUtil.replace(info, "$organId", organDTO.getName());
        }

        TransportInfoDetail infoDetail = TransportInfoDetail.builder()
                .info(info)
                .status(transportInfoMsg.getStatus())
                .created(transportInfoMsg.getCreated()).build();

        this.transportInfoService.saveOrUpdate(transportOrderId, infoDetail);
    }
}
