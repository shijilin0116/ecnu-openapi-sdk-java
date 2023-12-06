package com.ecnu.example;

import com.ecnu.OAuth2Client;
import com.ecnu.common.ApiConfig;
import com.ecnu.common.OAuth2Config;
import com.ecnu.example.entity.Fake;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @description Sync To Database Example
 */

public class SyncToDBExample {
    public static void main(String[] args) {
        /*
        public class OAuth2Config {
            private String clientId; // 必须
            private String clientSecret; // 必须
            private String baseUrl; // 默认 https://api.ecnu.edu.cn
            private List<String> scopes; //默认 ["ECNU-Basic"]
            private Integer timeout; //默认10秒
            private Boolean debug; //默认 false, 如果开启 debug，会打印出请求和响应的详细信息，对于数据同步类接口而言可能会非常大
        }
        */
        OAuth2Config cf = OAuth2Config.builder()
                .clientId("clientId")
                .clientSecret("clientSecret")
                .build();
        OAuth2Client client = OAuth2Client.getClient();
        client.initOAuth2ClientCredentials(cf);

        // -------初始化数据库配置----------
        // 创建Hibernate核心类，读取配置文件
        // hibernate.cfg.xml配置文件参考https://hibernate.net.cn/docs/14.html
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        //创建session工厂
        SessionFactory sf;
        try {
            sf = configuration.buildSessionFactory();
        } catch (HibernateException e) {
            throw new RuntimeException(e);
        }
        //获取session
        Session session = sf.openSession();

        // ---------全量同步，以组织机构接口同步为例------------
        ApiConfig config = ApiConfig.builder()
                .apiPath("/api/v1/sync/fake")
                .pageSize(100)
                .build();
        config.setParam("ts", 0);
        try {
            // 首次同步时，添加参数 ts=0，同步当前全部有效数据
            // 如果未创建表会自动根据 model 建表
            Integer count = client.syncToDB(config, session, Fake.class);
            System.out.println("DB：首次同步，从接口获取到" + count + "条数据");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            session.close();
            sf.close();
        }
    }
}
