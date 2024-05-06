package com.bgfang;

import com.alibaba.bytekit.ByteKit;
import com.alibaba.fastjson.JSON;
import io.oneagent.plugin.PluginActivator;
import io.oneagent.plugin.PluginContext;
import io.oneagent.service.ClassLoaderHandlerManager;

import java.lang.instrument.Instrumentation;

public class DemoActivator implements PluginActivator {
    private String name = this.getClass().getSimpleName();

    @Override
    public boolean enabled(PluginContext context) {
        System.out.println("enabled " + this.getClass().getName());
        System.err.println(this.getClass().getSimpleName() + ": " + JSON.toJSONString(this));

        System.err.println("bytekit url: " + ByteKit.class.getProtectionDomain().getCodeSource().getLocation());
        return true;
    }

    @Override
    public void init(PluginContext context) throws Exception {
        // 注册自定义的ClassLoaderHandler，让被增强的类可以加载到指定的类
        ClassLoaderHandlerManager loaderHandlerManager = context.getComponentManager().getComponent(ClassLoaderHandlerManager.class);
        loaderHandlerManager.addHandler(new DemoPluginClassLoaderHandler());

        System.out.println("init " + this.getClass().getName());
        Instrumentation instrumentation = context.getInstrumentation();
        String args = context.getProperty("args");
        DemoAgent.init(true,args,instrumentation);     }

    @Override
    public void start(PluginContext context) throws Exception {
        System.out.println("start " + this.getClass().getName());
    }

    @Override
    public void stop(PluginContext context) throws Exception {
        System.out.println("stop " + this.getClass().getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
