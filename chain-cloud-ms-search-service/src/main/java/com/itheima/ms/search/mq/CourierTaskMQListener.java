package com.itheima.ms.search.mq;

import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.chaincloud.common.vo.TransportOrderMsg;
import com.itheima.chaincloud.domain.search.domain.CourierTaskDTO;
import com.itheima.ms.search.entity.CourierTaskEntity;
import com.itheima.ms.search.service.CourierTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static com.itheima.ms.search.enums.ExceptionEnum.ES_ACCESS_ERROR;

/**
 * 快递员任务消息处理
 */
@Slf4j
@Component
public class CourierTaskMQListener {

    @Resource
    private CourierTaskService courierTaskService;
    @Resource
    private ElasticsearchClient client;
    @Value("${cc.es.index_name}")
    private String indexName;

    /**
     * 新增/更新快递员任务
     *
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.COURIER_TASK_SAVE_OR_UPDATE),
            exchange = @Exchange(name = Constants.MQ.Exchanges.COURIER_TASK, type = ExchangeTypes.TOPIC),
            key = Constants.MQ.RoutingKeys.COURIER_TASK_SAVE_OR_UPDATE
    ))
    public void listenCourierTaskCreateMsg(String msg) {
        log.info("接收到新增/更新快递员任务的消息 ({})-> {}", Constants.MQ.Queues.COURIER_TASK_SAVE_OR_UPDATE, msg);
        CourierTaskDTO courierTaskDTO = JSONUtil.toBean(msg, CourierTaskDTO.class);
        // 具体业务逻辑的处理
        this.courierTaskService.saveOrUpdate(courierTaskDTO);
    }

    /**
     * 创建运单后同步数据到es
     *
     * @param msg 消息
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.SEARCH_TRANSPORT_ORDER_CREATED),
            exchange = @Exchange(name = Constants.MQ.Exchanges.TRANSPORT_ORDER_DELAYED, type = ExchangeTypes.TOPIC, delayed = Constants.MQ.DELAYED),
            key = Constants.MQ.RoutingKeys.TRANSPORT_ORDER_CREATE
    ))
    public void listenTransportOrderCreatedMsg(String msg) {
        log.info("接收到新增运单的消息 ({})-> {}", Constants.MQ.Queues.SEARCH_TRANSPORT_ORDER_CREATED, msg);

        // 1.解析运单数据
        TransportOrderMsg transportOrderMsg = JSONUtil.toBean(msg, TransportOrderMsg.class);

        // 2.根据订单id查询快递员任务
        List<CourierTaskEntity> entities = courierTaskService.findByOrderId(transportOrderMsg.getOrderId());
        String courierTaskId = entities.get(0).getId().toString();

        // 3.构建更新对象，更新运单号
        CourierTaskEntity entity = new CourierTaskEntity();
        entity.setTransportOrderId(transportOrderMsg.getId());

        // 4.进行增量更新
        try {
            client.update(u -> u
                            .index(indexName)
                            .id(courierTaskId)
                            .doc(entity)
                    , CourierTaskEntity.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CCException(ES_ACCESS_ERROR);
        }
    }
}
