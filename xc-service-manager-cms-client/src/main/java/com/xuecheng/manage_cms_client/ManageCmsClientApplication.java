package com.xuecheng.manage_cms_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("com.xuecheng.framework.domain.cms")//ɨ��ʵ����
@ComponentScan(basePackages={"com.xuecheng.framework"})//ɨ��common�µ�������
@ComponentScan(basePackages={"com.xuecheng.manage_cms_client"})
public class ManageCmsClientApplication {

    public static void main(String[] args){
        SpringApplication.run(ManageCmsClientApplication.class);
    }


}
