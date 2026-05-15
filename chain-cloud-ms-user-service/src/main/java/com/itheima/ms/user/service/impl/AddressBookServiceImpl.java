package com.itheima.ms.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.ms.user.entity.AddressBookEntity;
import com.itheima.ms.user.mapper.AddressBookMapper;
import com.itheima.ms.user.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 用户表  服务类实现
 */
@Slf4j
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBookEntity>
        implements AddressBookService {

}
