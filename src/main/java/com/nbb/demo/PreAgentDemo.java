package com.nbb.demo;

import java.lang.instrument.Instrumentation;

/**
 * 在主程序之前运行的Agent
 * @author 胡鹏
 */
public class PreAgentDemo {


    public static void premain(String args, Instrumentation inst) {
        System.out.println("PreAgentDemo 运行。。。");
        System.out.println("PreAgentDemo 接收到参数 args=" + args);

    }
}
