package com.itheima.ms.transportinfo.service;

public interface BloomFilterService {

    void init();

    boolean add(Object obj);

    boolean contains(Object obj);
}
