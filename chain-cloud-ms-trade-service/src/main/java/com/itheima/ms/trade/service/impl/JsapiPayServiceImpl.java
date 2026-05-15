package com.itheima.ms.trade.service.impl;

import com.itheima.chaincloud.common.exception.CCException;
import com.itheima.ms.trade.constant.Constants;
import com.itheima.ms.trade.constant.TradingCacheConstant;
import com.itheima.ms.trade.constant.TradingConstant;
import com.itheima.ms.trade.entity.TradingEntity;
import com.itheima.ms.trade.enums.TradingEnum;
import com.itheima.ms.trade.handler.BeforePayHandler;
import com.itheima.ms.trade.handler.HandlerFactory;
import com.itheima.ms.trade.handler.JsapiPayHandler;
import com.itheima.ms.trade.service.JsapiPayService;
import com.itheima.ms.trade.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JsapiPayServiceImpl implements JsapiPayService {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private TradingService tradingService;
    @Resource
    private BeforePayHandler beforePayHandler;

    @Override
    public TradingEntity createJsapiTrading(TradingEntity tradingEntity) {
        //交易前置处理：检测交易单参数
        Boolean flag = beforePayHandler.checkCreateTrading(tradingEntity);
        if (!flag) {
            throw new CCException(TradingEnum.NATIVE_PAY_FAIL);
        }
        tradingEntity.setEnableFlag(Constants.YES);
        tradingEntity.setTradingType(TradingConstant.TRADING_TYPE_FK);

        //对交易订单加锁
        Long productOrderNo = tradingEntity.getProductOrderNo();
        String key = TradingCacheConstant.CREATE_PAY + productOrderNo;
        RLock lock = redissonClient.getFairLock(key);
        try {
            //获取锁
            if (lock.tryLock(TradingCacheConstant.REDIS_WAIT_TIME, TimeUnit.SECONDS)) {

                //交易前置处理：幂等性处理
                this.beforePayHandler.idempotentCreateTrading(tradingEntity);

                //调用不同的支付渠道进行处理
                JsapiPayHandler jsapiPayHandler = HandlerFactory.get(tradingEntity.getTradingChannel(), JsapiPayHandler.class);
                jsapiPayHandler.createJsapiTrading(tradingEntity);

                //新增或更新交易数据
                flag = this.tradingService.saveOrUpdate(tradingEntity);
                if (!flag) {
                    throw new CCException(TradingEnum.SAVE_OR_UPDATE_FAIL);
                }

                return tradingEntity;
            }
            throw new CCException(TradingEnum.NATIVE_PAY_FAIL);
        } catch (CCException e) {
            throw e;
        } catch (Exception e) {
            log.error("Jsapi预创建异常: tradingDTO = {}", tradingEntity, e);
            throw new CCException(TradingEnum.NATIVE_PAY_FAIL);
        } finally {
            lock.unlock();
        }
    }
}
