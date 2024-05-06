package com.bgfang.transform;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * TODO 一句话描述
 *
 * @author zhuxiong
 * 2024/4/25 下午2:18
 */
public class OnsMessageProducerClassFileTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        //类的路径替 / 换成 .
        className = className.replace('/', '.');
        //指定子类目录
        if (!className.startsWith("wangjubao.base.ons.MessageProducer")) {
            return classfileBuffer;
        }

        ClassPool classPool = ClassPool.getDefault();
        classPool.importPackage("wangjubao.base.ons.gray.OnsGrayRuleUtil");

        CtClass ctClass = null;
        try {
            //获取到 ctClass
            ctClass = classPool.get(className);
            //拿方法
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();

            for (CtMethod ctMethod : declaredMethods) {
                extendSendMsgExMethod(ctMethod, classPool, ctClass);
                extendSendOneWayMsgMethod(ctMethod, classPool, ctClass);
                extendSendAsyncMsgMethod(ctMethod, classPool, ctClass);
            }

            return ctClass.toBytecode();
        } catch (NotFoundException | CannotCompileException | IOException e) {
            e.printStackTrace();
        } finally {
            if (ctClass != null) {
                ctClass.detach();
            }
        }

        return classfileBuffer;
    }

    private static void extendSendMsgExMethod(CtMethod ctMethod, ClassPool classPool, CtClass ctClass) throws CannotCompileException, IOException {
        //判断方法名称
        if (!ctMethod.getName().equals("sendMsgEx")) {
            return;
        }
        //方法内部增加一行代码
        ctMethod.insertBefore("$2 = OnsGrayRuleUtil.getGrayTopicName($2);");
    }

    private static void extendSendOneWayMsgMethod(CtMethod ctMethod, ClassPool classPool, CtClass ctClass) throws CannotCompileException, IOException {
        //判断方法名称
        if (!ctMethod.getName().equals("sendOneWayMsg")) {
            return;
        }
        String methodSignature = ctMethod.getSignature();
        String signature = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z";
        if (!methodSignature.equals(signature)) {
            return;
        }
        //方法内部增加一行代码
        ctMethod.insertBefore("$2 = OnsGrayRuleUtil.getGrayTopicName($2);");
    }

    private static void extendSendAsyncMsgMethod(CtMethod ctMethod, ClassPool classPool, CtClass ctClass) throws CannotCompileException, IOException {
        //判断方法名称
        if (!ctMethod.getName().equals("sendAsyncMsg")) {
            return;
        }
        String methodSignature = ctMethod.getSignature();
        String signature = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;Lcom/aliyun/openservices/ons/api/SendCallback;)V";

        if (!methodSignature.equals(signature)) {
            return;
        }
        //方法内部增加一行代码
        ctMethod.insertBefore("$2 = OnsGrayRuleUtil.getGrayTopicName($2);");
    }
}
