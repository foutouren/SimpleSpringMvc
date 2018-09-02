package com.dongnao.mvc.controller;

import com.dongnao.mvc.annotation.MyController;
import com.dongnao.mvc.annotation.MyQualifier;
import com.dongnao.mvc.annotation.MyRequestMapping;
import com.dongnao.mvc.service.TestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MyController
@MyRequestMapping("/test")
public class TestController {

    @MyQualifier("testService")
    private TestService testService;

    @MyRequestMapping("/dotest")
    public void doTest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String name) {
        System.out.println("====" + name);

        /*testService.insert();
        testService.update();
        testService.select();
        testService.delete();*/

        try {
            httpServletResponse.getWriter().write(name);
//            httpServletResponse.getWriter().print(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
