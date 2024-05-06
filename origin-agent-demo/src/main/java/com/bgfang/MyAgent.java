package com.bgfang;

import com.bgfang.transform.OnsMessageConsumerClassFileTransformer;
import com.bgfang.transform.OnsMessageProducerClassFileTransformer;

import java.lang.instrument.Instrumentation;

/**
 * TODO 一句话描述
 *
 * @author zhuxiong
 * 2024/4/25 下午2:01
 */
public class MyAgent {
    public static void premain(String args, Instrumentation inst) {
        init(true, args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        init(false, args, inst);
    }

    public static synchronized void init(boolean premain, String args, Instrumentation inst) {

        System.err.println("MyAgent 开始执行了！ , 参数是 " + args);

        //注册我们的函数
        inst.addTransformer(new OnsMessageProducerClassFileTransformer());
        inst.addTransformer(new OnsMessageConsumerClassFileTransformer());

        System.out.println("DemoAgent started.");
    }

}
