package com.tortoise;


import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Objects;

/**
 * MyBatis 控制台日志，只支持Druid代理的数据源
 */
public class DruidMyBatisConsoleLog implements ClassFileTransformer {
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
                // 只处理void parameterize(Statement statement)方法
                if (Objects.equals("(Ljava/sql/Statement;)V", declaredMethod.getSignature())) {
                    // 插入自定义逻辑
                    declaredMethod.insertAfter(
                            "System.out.println(\"............................................................................\"); \r\n" +
                                    "String sql = ((com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl) ((com.alibaba.druid.pool.DruidPooledPreparedStatement) ((org.apache.ibatis.logging.jdbc.PreparedStatementLogger) java.lang.reflect.Proxy.getInvocationHandler(statement)).getPreparedStatement()).getRawStatement()).getSql();\r\n" +
                                    "com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl param = (com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl) ((com.alibaba.druid.pool.DruidPooledPreparedStatement) ((org.apache.ibatis.logging.jdbc.PreparedStatementLogger) java.lang.reflect.Proxy.getInvocationHandler(statement)).getPreparedStatement()).getRawStatement(); \r\n" +
                                    "for (int i = 0; i < param.getParametersSize(); i++) {\r\n" +
                                    "if (param.getParameter(i) != null) {\r\n" +
                                    "if (param.getParameter(i).getValue() instanceof String) { \r\n" +
                                    "sql = sql.replaceFirst(\"\\\\?\", \"'\" + param.getParameter(i).getValue() + \"'\");\r\n" +
                                    "} else {\r\n" +
                                    "sql = sql.replaceFirst(\"\\\\?\", String.valueOf(param.getParameter(i).getValue()));\r\n" +
                                    "}\r\n" +
                                    "}\r\n" +
                                    "}\r\n" +
                                    "String dbType = ((com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl) ((com.alibaba.druid.pool.DruidPooledPreparedStatement) ((org.apache.ibatis.logging.jdbc.PreparedStatementLogger) java.lang.reflect.Proxy.getInvocationHandler(statement)).getPreparedStatement()).getRawStatement()).getSqlStat().getDbType();\r\n" +
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
