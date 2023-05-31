package com.tortoise;

import java.lang.instrument.Instrumentation;

/**
 * @author wb
 * date 2023/5/30 11:20
 */
public class Agent {
// -javaagent:D:\work\mybatis-log-agent\target\mybatis-log-agent-1.0-jar-with-dependencies.jar=druid

    public static void agentmain(String arg, Instrumentation instrumentation) {
        // 使用Druid数据源
        if ("druid".equals(arg)) {
            instrumentation.addTransformer(new DruidMyBatisConsoleLog());
        } else {
            // 非Druid数据源
            instrumentation.addTransformer(new HikariMyBatisConsoleLog());
        }
    }


}
