package com.tortoise.mybatis;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.util.Objects;

/**
 * @author wb
 * date 2023/6/1 16:14
 */
public interface DefaultTransform {
    /**
     * agent逻辑
     */
    String agent();

    default byte[] defaultTransform(String className, byte[] classfileBuffer) {
        //java自带的方法不进行处理
        if (className.startsWith("java") || className.startsWith("sun")) {
            return classfileBuffer;
        }

        // 好像使用premain这个className是没问题的，但使用attach时className的.变成了/，所以如果是attach，那么这里需要替换
        className = className.replace('/', '.');

        // 指定需要处理的类
        if (!className.endsWith("org.apache.ibatis.executor.statement.PreparedStatementHandler")) {
            return classfileBuffer;
        }

        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.get(className);
            // 指定需要处理的函数
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods("parameterize");

            for (CtMethod declaredMethod : declaredMethods) {
                // 只处理void parameterize(Statement statement)方法
                if (Objects.equals("(Ljava/sql/Statement;)V", declaredMethod.getSignature())) {
                    // 插入自定义逻辑
                    declaredMethod.insertAfter(agent());
                }
            }
            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }
}
