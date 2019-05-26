package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 *
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository repository;

    @Test
    public void testFindAll(){
        List<CmsPage> all = repository.findAll();
        System.out.println(all);
    }

    @Test
    public void testFindPage(){
        //分页参数
        int page=0;//页码从0开始
        int size=10;
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = repository.findAll(pageable);
        System.out.println(all);
    }

    @Test
    public void testFindAllByExample(){
        //分页参数
        int page=0;//页码从0开始
        int size=10;
        Pageable pageable = PageRequest.of(page,size);
        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //要查询5a751fab6abb5044e0d19ea1页面
        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        //设置模板Id条件
        cmsPage.setTemplateId("5a962bf8b00ffc514038fafa");
        //设置页面别名
        cmsPage.setPageAliase("轮播");
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        ExampleMatcher withMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //ExampleMatcher.GenericPropertyMatchers.contains() 包含匹配
//        ExampleMatcher.GenericPropertyMatchers.startsWith() 前缀匹配
//        ExampleMatcher.GenericPropertyMatchers.endsWith() 末未匹配
        //定义Example
        Example<CmsPage> example = Example.of(cmsPage,withMatcher);
        Page<CmsPage> all = repository.findAll(example, pageable);
        System.out.println(all);
    }

}
