package com.bgfang.transform;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

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
public class OnsMessageConsumerClassFileTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        //类的路径替 / 换成 .
        className = className.replace('/', '.');
        //指定子类目录
        if (!className.startsWith("wangjubao.base.ons.MessageConsumer")) {
            return classfileBuffer;
        }

        ClassPool classPool = ClassPool.getDefault();

        CtClass ctClass = null;
        try {
            //获取到 ctClass
            ctClass = classPool.get(className);
            classPool.importPackage("wangjubao.base.ons.gray.OnsGrayRuleUtil");
            classPool.importPackage("com.aliyun.openservices.ons.api.PropertyKeyConst");

            //添加一个成员变量group
            CtField group = new CtField(classPool.get("java.lang.String"), "group", ctClass);
            group.setModifiers(Modifier.PRIVATE);
            ctClass.addField(group);
            CtField consumerProperties = new CtField(classPool.get("java.util.Properties"), "properties", ctClass);
            consumerProperties.setModifiers(Modifier.PRIVATE);
            ctClass.addField(consumerProperties);


            //拿构造函数，在构造函数中给group赋值
            CtConstructor[] declaredConstructors = ctClass.getDeclaredConstructors();
            // 目前所有的MessageConsumer的构造方法中，groupId 都是第三个参数，所以很简单的进行扩展
            for(CtConstructor ctConstructor : declaredConstructors) {
                ctConstructor.insertBefore("this.group = $3;");

                String content = "this.properties = consumerProperties;";

                ctConstructor.instrument(new ExprEditor(){
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("existingMethod")) {
                            m.replace(
                                    // 定义局部变量localVar
                                    "int localVar = 10; " +
                                            // 调用setMemberCode
                                            content
                            );
                        }
                    }
                });
                ctConstructor.insertAfter("this.properties = consumerProperties;");
            }

            // 添加重构rebuildConsumer方法
            String newMethodContent = "public void rebuildConsumer(String newGroupName) {\n" +
                    "        this.properties.put(PropertyKeyConst.GROUP_ID, newGroupName);\n" +
                    "        this.msgConsumer = ONSFactory.createConsumer(this.properties);\n" +
                    "    }";
            CtMethod rebuildConsumer = CtMethod.make(newMethodContent, ctClass);
            ctClass.addMethod(rebuildConsumer);


            //拿方法
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();

            for (CtMethod ctMethod : declaredMethods) {
                //判断方法名称
                if (ctMethod.getName().equals("subscribeMsg")) {

                    System.out.println("Method found: " + ctMethod.getName());
                    //方法内部增加一行代码
                    ctMethod.insertBefore("$2 = OnsGrayRuleUtil.getGrayTopicName($2);");

                    break;
                }
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
}
