package com.tortoise.mybatis;


import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * MyBatis 控制台日志，只支持Hikari代理的数据源
 */
public class HikariMyBatisConsoleLog implements ClassFileTransformer, DefaultTransform {
    @Override
    public String agent() {
        return "System.out.println(\"............................................................................\"); \r\n" +
                "String str = statement.toString();\r\n" +
                "String sql = str.substring(str.indexOf(\":\") + 1);\r\n" +
                "String dbType = statement.getConnection().getMetaData().getDatabaseProductName().toLowerCase();\r\n" +
                "System.out.println(com.alibaba.druid.sql.SQLUtils.format(sql, dbType));\r\n" +
                "System.out.println(\"............................................................................\"); \r\n"
                ;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return defaultTransform(className, classfileBuffer);
    }
}
