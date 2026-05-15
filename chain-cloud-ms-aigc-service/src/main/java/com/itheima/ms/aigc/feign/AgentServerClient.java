package com.itheima.ms.aigc.feign;

import com.itheima.ms.aigc.feign.dto.ChatRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(value = "chain-cloud-agent-server", contextId = "AgentServer", path = "/")
@FeignClient(value = "sl-agent-server", contextId = "AgentServer", path = "/")
public interface AgentServerClient {

    // 默认会话ID，接口不需要特殊设置时使用
    String DEFAULT_SESSION_ID = "1";

    // 默认用户TOKEN，接口不需要特殊设置时使用
    String DEFAULT_USER_TOKEN = "DEFAULT_USER_TOKEN";

    /**
     * 非流式对话
     *
     * @param chatRequestDTO 对话参数
     * @return 对话结果
     */
    @PostMapping("/chat")
    String chat(@RequestBody ChatRequestDTO chatRequestDTO);

}
