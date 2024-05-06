package com.bgfang;

import io.oneagent.service.ClassLoaderHandler;

/**
 * TODO 一句话描述
 *
 * @author zhuxiong
 * 2024/4/24 下午4:43
 */
public class DemoPluginClassLoaderHandler implements ClassLoaderHandler {

    @Override
    public Class<?> loadClass(String name) {
        if (name.startsWith("com.bgfang")) {
            try {
                Class<?> clazz = this.getClass().getClassLoader().loadClass(name);
                return clazz;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
