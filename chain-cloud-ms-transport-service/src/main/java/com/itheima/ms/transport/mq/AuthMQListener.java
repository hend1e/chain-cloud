package com.itheima.ms.transport.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.itheima.chaincloud.api.aigc.AIGCFeign;
import com.itheima.chaincloud.common.constant.Constants;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.common.util.StrUtil;
import com.itheima.ms.transport.entity.node.AgencyEntity;
import com.itheima.ms.transport.entity.node.BaseEntity;
import com.itheima.ms.transport.entity.node.OLTEntity;
import com.itheima.ms.transport.entity.node.TLTEntity;
import com.itheima.ms.transport.enums.OrganTypeEnum;
import com.itheima.ms.transport.mq.bean.AuthOrgContent;
import com.itheima.ms.transport.mq.bean.AuthOrgMessage;
import com.itheima.ms.transport.service.IService;
import com.itheima.ms.transport.utils.OrganServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 对于权限管家系统消息的处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthMQListener {

    private static final String TYPE_ORG = "ORG";
    private static final String OP_ADD = "ADD";
    private static final String OP_UPDATE = "UPDATE";
    private static final String OP_DEL = "DEL";
    private final AIGCFeign aigcFeign;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = Constants.MQ.Queues.AUTH_TRANSPORT),
            exchange = @Exchange(name = "${rabbitmq.exchange}", type = ExchangeTypes.TOPIC),
            key = "#"
    ))
    public void listenAgencyMsg(String msg) {
        log.info("接收到消息 -> {}", msg);

        if (StrUtil.isBlank(msg)) {
            log.warn("权限系统消息为空，忽略处理");
            return;
        }

        AuthOrgMessage authOrgMessage;
        try {
            authOrgMessage = JSONUtil.toBean(msg, AuthOrgMessage.class);
        } catch (Exception e) {
            log.error("权限系统消息反序列化失败，msg={}", msg, e);
            return;
        }

        if (authOrgMessage == null) {
            log.warn("权限系统消息反序列化结果为空，msg={}", msg);
            return;
        }

        if (StrUtil.notEquals(authOrgMessage.getType(), TYPE_ORG)) {
            log.debug("非机构消息，type={}", authOrgMessage.getType());
            return;
        }

        if (CollUtil.isEmpty(authOrgMessage.getContent())) {
            log.warn("机构消息content为空，msg={}", msg);
            return;
        }

        var operation = authOrgMessage.getOperation();
        for (var content : authOrgMessage.getContent()) {
            try {
                this.invoke(content, operation);
            } catch (Exception e) {
                log.error("处理机构消息失败，operation={}, content={}",
                        operation, JSONUtil.toJsonStr(content), e);
            }
        }
    }

    private void invoke(AuthOrgContent content, String operation) {
        if (!this.checkContent(content, operation)) {
            return;
        }

        var organType = this.resolveOrganType(content);
        if (organType == null) {
            organType = this.resolveOrganTypeByAI(content);
            if (organType == null) {
                log.warn("无法识别机构类型，bid={}, name={}", content.getId(), content.getName());
                return;
            }
        }

        var entity = this.buildEntity(content, organType);
        if (entity == null) {
            log.warn("构建机构实体失败，bid={}, name={}, organType={}", content.getId(), content.getName(), organType);
            return;
        }
        var service = OrganServiceFactory.getBean(organType.getCode());

        log.info("开始处理机构消息，operation={}, organType={}, bid={}, name={}", operation, organType.getCode(), content.getId(), content.getName());
        this.execute(operation, service, entity);
        log.info("机构消息处理完成，operation={}, organType={}, bid={}, name={}", operation, organType.getCode(), content.getId(), content.getName());
    }

    private boolean checkContent(AuthOrgContent content, String operation) {
        if (content == null) {
            log.warn("机构节点内容为空，忽略处理");
            return false;
        }
        if (ObjectUtil.isEmpty(content.getId())) {
            log.warn("机构bid为空，content={}", JSONUtil.toJsonStr(content));
            return false;
        }
        if (StrUtil.isBlank(content.getName())) {
            log.warn("机构名称为空，content={}", JSONUtil.toJsonStr(content));
            return false;
        }
        if (!isValidOperation(operation)) {
            log.warn("未知操作类型，operation={}, content={}", operation, JSONUtil.toJsonStr(content));
            return false;
        }
        return true;
    }

    private boolean isValidOperation(String operation) {
        return StrUtil.equalsAnyIgnoreCase(operation, OP_ADD, OP_UPDATE, OP_DEL);
    }

    private OrganTypeEnum resolveOrganType(AuthOrgContent content) {
        var name = content.getName();
        if (StrUtil.endWith(name, "转运中心")) {
            return OrganTypeEnum.OLT;
        }
        if (StrUtil.endWith(name, "分拣中心")) {
            return OrganTypeEnum.TLT;
        }
        if (StrUtil.endWith(name, "营业部")) {
            return OrganTypeEnum.AGENCY;
        }
        return null;
    }

    private BaseEntity buildEntity(AuthOrgContent content, OrganTypeEnum organType) {
        BaseEntity entity;
        switch (organType) {
            case OLT:
                entity = new OLTEntity();
                entity.setParentId(0L);
                break;
            case TLT:
                entity = new TLTEntity();
                entity.setParentId(content.getParentId());
                break;
            case AGENCY:
                entity = new AgencyEntity();
                entity.setParentId(content.getParentId());
                break;
            default:
                return null;
        }
        entity.setBid(content.getId());
        entity.setName(content.getName());
        entity.setStatus(content.getStatus());
        return entity;
    }

    private void execute(String operation, IService service, BaseEntity entity) {
        switch (operation.toUpperCase()) {
            case OP_ADD:
                service.create(entity);
                break;
            case OP_UPDATE:
                service.update(entity);
                break;
            case OP_DEL:
                service.deleteByBid(entity.getBid());
                break;
            default:
                log.warn("未支持的操作类型，operation={}, bid={}", operation, entity.getBid());
        }
    }

    private OrganTypeEnum resolveOrganTypeByAI(AuthOrgContent content) {
        try {
            var organTypeResponse = aigcFeign.identifyOrganType(content.getName());
            if (organTypeResponse == null) {
                log.warn("AI服务返回空结果，bid={}, name={}", content.getId(), content.getName());
                return null;
            }
            var confidenceThreshold = 0.8;
            if (organTypeResponse.getConfidence() < confidenceThreshold) {
                log.warn("AI识别置信度低于阈值，置信度：{}，bid={}, name={}, organTypeResponse={}", organTypeResponse.getConfidence(), content.getId(), content.getName(), organTypeResponse);
                return null;
            }
            var organType = organTypeResponse.getOrganType();
            if (StrUtil.equalsIgnoreCase("UNKNOWN", organType)) {
                log.warn("AI返回未知机构类型：{}", organTypeResponse);
                return null;
            }
            return OrganTypeEnum.valueOf(organType);
        } catch (Exception e) {
            log.error("调用AI机构类型识别服务失败，bid={}, name={}", content.getId(), content.getName(), e);
        }
        return null;
    }

}
