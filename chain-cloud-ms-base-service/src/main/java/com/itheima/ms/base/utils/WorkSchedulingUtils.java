package com.itheima.ms.base.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.itheima.chaincloud.common.util.DateUtils;
import com.itheima.chaincloud.common.util.ObjectUtil;
import com.itheima.chaincloud.domain.base.enums.WorkPatternEnum;
import com.itheima.ms.base.entity.base.WorkPatternEntity;
import com.itheima.ms.base.entity.base.WorkSchedulingEntity;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Slf4j
public class WorkSchedulingUtils {

    /**
     * 查询列表信息时是否需要查询历史列表
     * 当前月份1号查询的时候不需要查询历史数据
     * @param queryMonth 月份
     * @return 是否需要
     */
    public static boolean isQueryHistory(String queryMonth){
        LocalDateTime now = LocalDateTimeUtil.now();
        String currentMonth = LocalDateTimeUtil.format( now, DateUtils.DEFAULT_MONTH_FORMAT);
        int result = queryMonth.compareTo(currentMonth);
        if(result > 0) {
            return false;
        }else if (result == 0) {
            int dayOfMonth = now.getDayOfMonth();
            return dayOfMonth > 1;
        }else {
            return true;
        }
    }

    /**
     * 是否上班
     * @param workSchedulingEntity 排班数据
     * @param month 月
     * @param dayOfMonth 日
     * @param patternEntity 工作模式
     * @param workHistory 历史数据
     * @param forceUserWorkPattern 是否强制使用工作模式推断
     * @return 是否上班
     */
    public static boolean isWorded(WorkSchedulingEntity workSchedulingEntity, String month, int dayOfMonth,
                                   WorkPatternEntity patternEntity, Map<String,String> workHistory, Boolean forceUserWorkPattern){
        String nowDay = LocalDateTimeUtil.format(LocalDateTime.now(), DateUtils.DEFAULT_DATE_FORMAT);
        String queryDay = String.format("%s-%s", month, dayOfMonth >= 10 ? "" + dayOfMonth : "0" + dayOfMonth);
        if(nowDay.compareTo(queryDay) > 0 && !forceUserWorkPattern) {
            return CollUtil.isEmpty(workHistory) ? false : workHistory.containsKey(workSchedulingEntity.getUserId() + "" + dayOfMonth);
        }else {
            if (ObjectUtil.isEmpty(patternEntity)) {
                return false;
            }
            Byte workPatternType = patternEntity.getWorkPatternType();
            if(workPatternType.byteValue() == WorkPatternEnum.WEEKS.getType()) {
                int dayOfWeek = LocalDateTimeUtil.parse(queryDay,DateUtils.DEFAULT_DATE_FORMAT).getDayOfWeek().getValue();
                return isWorked(dayOfWeek, patternEntity);
            }else {
                long workContinueStartTime = DateUtils.getStartTime(workSchedulingEntity.getWorkContinueStartTime())
                        .toEpochSecond(ZoneOffset.of("+8"));
                long queryDayStartTime = DateUtils.getStartTime(LocalDateTimeUtil.parse(queryDay, DateUtils.DEFAULT_DATE_FORMAT))
                        .toEpochSecond(ZoneOffset.of("+8"));
                int day = (int)(queryDayStartTime - workContinueStartTime) / (24 * 3600) ;
                int dayNum = patternEntity.getRestDayNum() + patternEntity.getWorkDayNum() == 0 ? 0: day % ( patternEntity.getRestDayNum() + patternEntity.getWorkDayNum());
                return dayNum < patternEntity.getWorkDayNum();
            }
        }
    }

    private static boolean isWorked(int dayOfWeek, WorkPatternEntity entity) {
        switch (dayOfWeek) {
            case 1 :
                return entity.getMonday() == 1;
            case 2:
                return entity.getTuesday() == 1;
            case 3:
                return entity.getWednesday() == 1;
            case 4:
                return entity.getThursday() == 1;
            case 5:
                return entity.getFriday() == 1;
            case 6:
                return entity.getSaturday() == 1;
            case 7:
                return entity.getSunday() == 1;
            default:
                return false;
        }
    }
}