package com.tortoise;


import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Objects;

/**
 * MyBatis 控制台日志，只支持Hikari代理的数据源
 */
public class HikariMyBatisConsoleLog implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
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
                System.out.println(declaredMethod.getSignature());
                // 只处理void parameterize(Statement statement)方法
                if (Objects.equals("(Ljava/sql/Statement;)V", declaredMethod.getSignature())) {
                    // 插入自定义逻辑
                    declaredMethod.insertAfter(
                            "System.out.println(\"............................................................................\"); \r\n" +
                                    "String str = statement.toString();\r\n" +
                                    "String sql = str.substring(str.indexOf(\":\") + 1);\r\n" +
                                    "String dbType = statement.getConnection().getMetaData().getDatabaseProductName().toLowerCase();\r\n" +
                                    "System.out.println(com.alibaba.druid.sql.SQLUtils.format(sql, dbType));\r\n" +
                                    "System.out.println(\"............................................................................\"); \r\n"
                    );
                }
            }
            return ctClass.toBytecode();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }
}
