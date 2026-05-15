package com.itheima.chaincloud.mq.exception;

import com.itheima.chaincloud.mq.entity.FailMsgEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzj
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MsgException extends RuntimeException {

    private FailMsgEntity failMsgEntity;

}
