package com.tortoise;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * MyBatis 控制台日志，只支持Druid代理的数据源
 */
public class DruidMyBatisConsoleLog implements ClassFileTransformer, AgentString {
    @Override
    public String agent() {
        return "System.out.println(\"............................................................................\"); \r\n" +
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
                ;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return defaultTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }
}
