package com.dongnao.mvc.service.impl;

import com.dongnao.mvc.annotation.MyService;
import com.dongnao.mvc.service.TestService;

import java.util.Map;

@MyService("testService")
public class TestServiceImpl implements TestService {
    @Override
    public int insert(Map map) {
        return 0;
    }

    @Override
    public int delete(Map map) {
        return 0;
    }

    @Override
    public int update(Map map) {
        return 0;
    }

    @Override
    public int select(Map map) {
        return 0;
    }
}
