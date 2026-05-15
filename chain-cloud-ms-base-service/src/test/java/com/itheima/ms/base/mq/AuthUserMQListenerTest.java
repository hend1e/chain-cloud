package com.itheima.ms.base.mq;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthUserMQListenerTest {

    @Autowired
    AuthUserMQListener authUserMQListener;

    @Test
    void listenUserMsg() {
        String s = "{\"type\":\"USER\",\"operation\":\"DEL\",\"content\":[{\"id\":\"1014219558409471937\",\"superior\":\"-1\",\"account\":\"xxxx\",\"name\":\"xxxx\",\"orgId\":\"987327492140340001\",\"orgName\":null,\"stationId\":\"981223703335410625\",\"stationName\":null,\"email\":null,\"mobile\":\"13838383883\",\"sex\":{\"desc\":\"男\",\"code\":\"M\"},\"status\":true,\"avatar\":\"\",\"workDescribe\":\"\",\"passwordErrorLastTime\":null,\"passwordErrorNum\":0,\"passwordExpireTime\":null,\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\"lastLoginTime\":null,\"roles\":null,\"roleNames\":null,\"userGroupsNames\":null,\"administrator\":false}]}\n";
        authUserMQListener.listenUserMsg(s);
    }
}