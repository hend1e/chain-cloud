package com.itheima.chaincloud.api.auth;

import com.itheima.auth.sdk.dto.LoginDTO;
import com.itheima.chaincloud.domain.auth.domain.LoginRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@ApiIgnore
@FeignClient(name = "chain-cloud-ms-auth", contextId = "MSAuth", path = "/auth")
public interface AuthFeign {

    /**
     * 登录
     *
     * @param account     账号
     * @param password    密码
     * @param userType    用户类型，1-管理员 2-快递员 3-司机 4-用户
     * @param loginIp     登录 IP 地址
     * @param deviceInfo  设备信息
     * @return 登录信息
     */
   @PostMapping("/login")
   LoginDTO login(@RequestParam("account") String account,
                  @RequestParam("password") String password,
                  @RequestParam("userType") Integer userType,
                  @RequestParam("loginIp") String loginIp,
                  @RequestParam("deviceInfo") String deviceInfo);

    /**
     * 查询登录记录（最近 10 条）
     *
     * @param account  登录账号
     * @param userType 用户类型，1-管理员 2-快递员 3-司机 4-用户
     * @return 登录记录列表
     */
   @GetMapping("/login/record")
  List<LoginRecordDTO> queryRecordList(@RequestParam("account") String account,
                                       @RequestParam("userType") Integer userType);
}
