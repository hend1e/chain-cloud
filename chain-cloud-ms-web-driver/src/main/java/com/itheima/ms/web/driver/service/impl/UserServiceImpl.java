package com.itheima.ms.web.driver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.itheima.auth.sdk.common.Result;
import com.itheima.auth.sdk.dto.UserDTO;
import com.itheima.chaincloud.api.base.truck.TruckFeign;
import com.itheima.chaincloud.api.base.truck.TruckTypeFeign;
import com.itheima.chaincloud.api.base.user.DriverFeign;
import com.itheima.chaincloud.domain.base.truck.TruckDTO;
import com.itheima.chaincloud.domain.base.truck.TruckTypeDTO;
import com.itheima.chaincloud.domain.base.user.TruckDriverDTO;
import com.itheima.chaincloud.api.driver.DriverJobFeign;
import com.itheima.chaincloud.domain.driver.domain.DriverJobStatisticsDTO;
import com.itheima.ms.web.driver.service.UserService;
import com.itheima.ms.web.driver.vo.response.TaskReportVO;
import com.itheima.ms.web.driver.vo.response.TruckDetailVO;
import com.itheima.ms.web.driver.vo.response.UserDetailVO;
import com.itheima.chaincloud.common.util.AuthTemplateThreadLocal;
import com.itheima.chaincloud.common.util.UserThreadLocal;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private DriverFeign driverFeign;
    @Resource
    private TruckFeign truckFeign;
    @Resource
    private TruckTypeFeign truckTypeFeign;
    @Resource
    private DriverJobFeign driverJobFeign;

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @Override
    public UserDetailVO userDetail() {
        //获取用户id
        Long userId = UserThreadLocal.getUserId();

        //根据用户id获取用户详细信息
        Result<UserDTO> result = AuthTemplateThreadLocal.get().opsForUser().getUserById(userId);

        //将用户信息封装到vo
        UserDTO userDTO = result.getData();
        UserDetailVO userDetailVO = BeanUtil.toBean(userDTO, UserDetailVO.class);
        userDetailVO.setNumber(String.valueOf(userId));
        userDetailVO.setPhone(userDTO.getMobile());
        return userDetailVO;
    }

    /**
     * 获取车辆信息
     *
     * @return 车辆信息
     */
    @Override
    public TruckDetailVO truckDetail() {
        //获取用户id
        Long userId = UserThreadLocal.getUserId();
        //根据用户id查询司机相关信息
        var truckDriverDto = driverFeign.findOneDriver(userId);
        //根据车辆id查询车辆信息
        var truckDto = truckFeign.fineById(truckDriverDto.getTruckId());
        //根据车型id查询车型信息
        var truckTypeDto = truckTypeFeign.fineById(truckDto.getTruckTypeId());

        //车辆信息封装为vo
        TruckDetailVO truckDetailVO = BeanUtil.toBean(truckDto, TruckDetailVO.class);
        truckDetailVO.setTruckType(truckTypeDto.getName());
        return truckDetailVO;
    }

    /**
     * 任务数据
     *
     * @param year  年份
     * @param month 月份
     * @return 任务相关统计
     */
    @Override
    public TaskReportVO taskReport(String year, String month) {
        Long userId = UserThreadLocal.getUserId();

        String dateTime = year + "-" + month;
        DriverJobStatisticsDTO driverJobStatisticsDTO = driverJobFeign.jobMonthlyStatistics(String.valueOf(userId), dateTime);
        return BeanUtil.toBean(driverJobStatisticsDTO, TaskReportVO.class);
    }
}
