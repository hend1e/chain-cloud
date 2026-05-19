package com.itheima.ms.web.manager.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.itheima.auth.sdk.common.Result;
import com.itheima.auth.sdk.dto.*;
import com.itheima.chaincloud.api.base.common.WorkSchedulingFeign;
import com.itheima.chaincloud.common.util.AuthTemplateThreadLocal;
import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.common.vo.R;
import com.itheima.chaincloud.domain.base.basic.WorkSchedulingDTO;
import com.itheima.chaincloud.domain.base.enums.StatusEnum;
import com.itheima.chaincloud.domain.base.enums.WorkUserTypeEnum;
import com.itheima.ms.web.manager.service.AuthService;
import com.itheima.ms.web.manager.vo.agency.AgencySimpleVO;
import com.itheima.ms.web.manager.vo.auth.CourierVO;
import com.itheima.ms.web.manager.vo.auth.SysUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 鉴权服务
 * 登录 验证码 员工列表 快递员列表 角色
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String CAPTCHA_REDIS_PREFIX = "CAPTCHA_";

    @Value("${role.courier}")
    private String roleId;
    private final WorkSchedulingFeign workSchedulingFeign;
    private final StringRedisTemplate stringRedisTemplate;

    // 验证码生成器
    private final CodeGenerator captchaGenerator = new MathGenerator(1);

    /**
     * 登录
     *
     * @param login 用户登录信息
     * @return 登录结果
     */
    @Override
    public R<LoginDTO> login(LoginParamDTO login) {
        return R.error("用户名或密码错误");
    }

    @Override
    public void createCaptcha(String key, HttpServletResponse response) throws IOException {
        //1. 生成验证码，指定宽、高、字符个数、干扰线条数
        var lineCaptcha = CaptchaUtil.createLineCaptcha(115, 42, 0, 10);
        //1.1 设置生成器为数字计算验证码生成器并且指定参与计算最大数字位数为1，也就是个位数相加
        lineCaptcha.setGenerator(captchaGenerator);
        //1.2 获取生成的验证码值
        String code = lineCaptcha.getCode();

        //2. 将验证码的值写入到redis，有效期为1分钟
        String redisKey = CAPTCHA_REDIS_PREFIX + key;
        this.stringRedisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(1));

        //3. 输出到页面，设置页面不缓存
        response.setHeader(HttpHeaders.PRAGMA, "No-cache");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "No-cache");
        response.setDateHeader(HttpHeaders.EXPIRES, 0L);
        lineCaptcha.write(response.getOutputStream());
    }

    @Override
    public boolean check(String key, String value) {
        //TODO 待实现
        return false;
    }

    /**
     * 转换用户
     *
     * @param userDTO 用户DTO
     * @return 用户VO
     */
    @Override
    public SysUserVO parseUser2Vo(UserDTO userDTO) {
        SysUserVO vo = new SysUserVO();
        //填充基本信息
        vo.setUserId(userDTO.getId());
        vo.setAvatar(userDTO.getAvatar());
        vo.setEmail(userDTO.getEmail());
        vo.setMobile(userDTO.getMobile());
        vo.setAccount(userDTO.getAccount());
        vo.setName(userDTO.getName());
        vo.setStatus(userDTO.isStatus() ? StatusEnum.NORMAL.getCode() : StatusEnum.DISABLED.getCode());

        //处理所属机构信息
        AgencySimpleVO agency = new AgencySimpleVO();
        agency.setName(userDTO.getOrgName());
        vo.setAgency(agency);

        //处理岗位信息
        vo.setStationName(userDTO.getStationName());
        // 角色
        vo.setRoleNames(userDTO.getRoleNames());
        return vo;
    }

    /**
     * 获取用户信息
     *
     * @param id 用户id
     * @return 执行结果
     */
    @Override
    public SysUserVO user(Long id) {
        Result<UserDTO> result = AuthTemplateThreadLocal.get().opsForUser().getUserById(id);
        if (result.getCode() != 0) {
            return new SysUserVO();
        }
        return parseUser2Vo(result.getData());
    }

    /**
     * 批量获取用户信息
     *
     * @param ids 用户id
     * @return 执行结果
     */
    @Override
    public List<SysUserVO> users(List<Long> ids) {
        List<Long> longList = ids.stream().filter(Objects::nonNull).collect(Collectors.toList());
        Result<List<UserDTO>> result = AuthTemplateThreadLocal.get().opsForUser().list(longList);
        if (result.getCode() != 0) {
            return new ArrayList<>();
        }
        return result.getData().parallelStream().map(this::parseUser2Vo).collect(Collectors.toList());
    }

    /**
     * 员工分页
     *
     * @param page     页数
     * @param pageSize 页大小
     * @param agencyId 机构ID
     * @return 员工列表
     */
    @Override
    public PageResponse<SysUserVO> findUserByPage(Integer page, Integer pageSize, Long agencyId, String account, String name, String mobile) {
        Result<PageDTO<UserDTO>> result = AuthTemplateThreadLocal.get().opsForUser().getUserByPage(new UserPageDTO(page, pageSize, account, name, ObjectUtil.isNotEmpty(agencyId) ? agencyId : null, mobile));
        return getPageResponseR(page, pageSize, result);
    }

    /**
     * 快递员分页
     *
     * @param page     页数
     * @param pageSize 页大小
     * @param name     名称
     * @param mobile   手机号
     * @return 快递员列表
     */
    @Override
    public PageResponse<SysUserVO> findCourierByPage(Integer page, Integer pageSize, String name, String mobile, String account, Long orgId) {
        UserPageDTO userPageDTO = new UserPageDTO(page, pageSize, account, name, orgId, mobile);
        userPageDTO.setRoleId(roleId);
        Result<PageDTO<UserDTO>> result = AuthTemplateThreadLocal.get().opsForUser().getUserByPage(userPageDTO);

        // 转换vo
        PageResponse<SysUserVO> pageResponseR = getPageResponseR(page, pageSize, result);
        if (CollUtil.isEmpty(pageResponseR.getItems())) {
            return pageResponseR;
        }

        List<Long> userIds = pageResponseR.getItems().parallelStream().map(SysUserVO::getUserId).collect(Collectors.toList());
        if (CollUtil.isEmpty(userIds)) {
            return pageResponseR;
        }

        // 补充数据
        String bidStr = CollUtil.isEmpty(userIds) ? "" : CharSequenceUtil.join(",", userIds);
        List<WorkSchedulingDTO> workSchedulingDTOS = workSchedulingFeign.monthSchedule(bidStr, null, WorkUserTypeEnum.COURIER.getCode().byteValue(), LocalDateTimeUtil.toEpochMilli(LocalDateTimeUtil.now()));
        if (CollUtil.isEmpty(workSchedulingDTOS)) {
            return pageResponseR;
        }
        Map<Long, Boolean> workMap = workSchedulingDTOS.parallelStream().filter(workSchedulingDTO -> ObjectUtil.isNotEmpty(workSchedulingDTO.getWorkSchedules())).collect(Collectors.toMap(WorkSchedulingDTO::getUserId, workSchedulingDTO -> workSchedulingDTO.getWorkSchedules().get(0)));

        pageResponseR.getItems().parallelStream().forEach(userDTO -> {
            // 上班状态
            try {
                Boolean aBoolean = workMap.get(userDTO.getUserId());
                if (ObjectUtil.isNotEmpty(aBoolean)) {
                    userDTO.setWorkStatus(aBoolean ? 1 : 0);
                }
            } catch (Exception ignored) {
                log.info("Exception:{}", ignored.getMessage());
            }
        });
        return pageResponseR;
    }

    /**
     * 转换用户返回结果
     *
     * @param page     页数
     * @param pageSize 页大小
     * @param result   用户信息
     * @return 用户信息
     */
    private PageResponse<SysUserVO> getPageResponseR(@RequestParam(name = "page") Integer page, @RequestParam(name = "pageSize") Integer pageSize, Result<PageDTO<UserDTO>> result) {
        if (result.getCode() == 0 && ObjectUtil.isNotEmpty(result.getData())) {
            PageDTO<UserDTO> userPage = result.getData();
            //处理对象转换
            List<SysUserVO> voList = userPage.getRecords().parallelStream().map(this::parseUser2Vo).collect(Collectors.toList());
            return PageResponse.of(voList, page, pageSize, userPage.getTotal() % userPage.getSize(), userPage.getTotal());
        }
        return PageResponse.getInstance();
    }

    /**
     * 根据机构查询快递员
     *
     * @param agencyId 机构id
     * @return 快递员列表
     */
    @Override
    public List<CourierVO> findByAgencyId(Long agencyId) {
        //构件查询条件
        UserPageDTO userPageDTO = new UserPageDTO(1, 999, null, null, agencyId, null);
        userPageDTO.setRoleId(roleId);

        //分页查询
        Result<PageDTO<UserDTO>> result = AuthTemplateThreadLocal.get().opsForUser().getUserByPage(userPageDTO);
        if (ObjectUtil.isEmpty(result.getData().getRecords())) {
            return Collections.emptyList();
        }

        //组装响应结果
        return result.getData().getRecords().stream().map(userDTO -> BeanUtil.toBean(userDTO, CourierVO.class)).collect(Collectors.toList());
    }


}
