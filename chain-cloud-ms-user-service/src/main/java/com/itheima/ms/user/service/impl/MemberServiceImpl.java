package com.itheima.ms.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ms.user.entity.MemberEntity;
import com.itheima.ms.user.mapper.MemberMapper;
import com.itheima.ms.user.service.MemberService;
import org.springframework.stereotype.Service;


/**
 * 用户表  服务类实现
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, MemberEntity>
        implements MemberService {

}
