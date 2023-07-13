package com.tortoise;

import com.tortoise.jdbc.mysql.MySQLVersion;
import com.tortoise.jdbc.mysql.MySQL5ConsoleLog;
import com.tortoise.jdbc.mysql.MySQL8ConsoleLog;
import com.tortoise.mybatis.DruidMyBatisConsoleLog;
import com.tortoise.mybatis.HikariMyBatisConsoleLog;

import java.lang.instrument.Instrumentation;

/**
 * @author wb
 * date 2023/5/30 11:20
 */
public class Agent {
// -javaagent:D:\work\mybatis-log-agent\target\mybatis-log-agent-1.0-jar-with-dependencies.jar=druid

    public static void premain(String args, Instrumentation instrumentation) {
        String[] params = args.split(",");
        String type = params[0];
        String version = params[1];
        if (Resolver.MYBATIS.name().equals(type)) {
            // 使用Druid数据源
            if (DatasourceType.DRUID.name().equals(version)) {
                instrumentation.addTransformer(new DruidMyBatisConsoleLog());
            } else if (DatasourceType.HIKARICP.name().equals(version)) {
                // 非Druid数据源
                instrumentation.addTransformer(new HikariMyBatisConsoleLog());
            }
        } else if (Resolver.JPA.name().equals(type)) {

        } else if (Resolver.JDBC.name().equals(type)) {
            if (MySQLVersion.MySQL8.name().equals(version)) {
                instrumentation.addTransformer(new MySQL8ConsoleLog());
            } else if (MySQLVersion.MySQL5.name().equals(version)) {
                instrumentation.addTransformer(new MySQL5ConsoleLog());
            }
        } else {
            System.out.println("该探针只支持3种格式：jdbc、jpa、mybatis");
        }


    }


}
