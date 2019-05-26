package com.xuecheng.manage_course;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRibbon {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsPageClient cmsPageClient; //接口代理对象，由feign生成代理对象

    @Test
    public void testRibbon(){
        //确定要获取的服务名称   XC-SERVICE-MANAGE-CMS
        String serviceId="XC-SERVICE-MANAGE-CMS";
        //ribbon客户端首先从eureka中获取服务列表,根据服务名
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://"+serviceId+"/cms/page/get/5a754adf6abb500ad05688d9", Map.class);
        Map map = forEntity.getBody();
        System.out.println(map);
    }

    @Test
    public void testFeginClient(){
        //发起远程调用
        CmsPage cmsPageById = cmsPageClient.findCmsPageById("5a754adf6abb500ad05688d9");
        System.out.println(cmsPageById);
    }

}
