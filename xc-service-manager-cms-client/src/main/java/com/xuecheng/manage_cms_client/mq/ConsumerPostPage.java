package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * 监听mq,接收页面发布的消息
 */
@Component
@Slf4j
public class ConsumerPostPage {

    @Autowired
    PageService pageService;
    @Autowired
    CmsPageRepository cmsPageRepository;


    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String msg){
        //解析消息
        Map map = JSON.parseObject(msg, Map.class);
        log.info("receive cms post page:{}",msg.toString());
//取出页面id
        String pageId = (String) map.get("pageId");
//查询页面信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(!optional.isPresent()){
            log.error("receive cms post page,cmsPage is null:{}",msg.toString());
            return ;
        }
//将页面保存到服务器物理路径
        pageService.savePageToServerPath(pageId);

    }

}
