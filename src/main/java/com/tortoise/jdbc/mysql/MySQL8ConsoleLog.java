package com.tortoise.jdbc.mysql;

import com.tortoise.jdbc.mysql.DefaultTransform;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * MyBatis 控制台日志，只支持Hikari代理的数据源
 */
public class MySQL8ConsoleLog implements ClassFileTransformer, DefaultTransform {
    @Override
    public String agent() {
        return "System.out.println(\"............................................................................\"); \r\n" +
                "String sql = ((com.mysql.cj.ClientPreparedQuery) this.query).asSql();\r\n" +
                "String dbType = this.getConnection().getMetaData().getDatabaseProductName().toLowerCase();\r\n" +
                "System.out.println(com.alibaba.druid.sql.SQLUtils.format(sql, dbType));\r\n" +
                "System.out.println(\"............................................................................\"); \r\n"
                ;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return defaultTransform(className, "com.mysql.cj.jdbc.ClientPreparedStatement", classfileBuffer);
    }
}
