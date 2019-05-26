package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
@Slf4j
public class ChooseCourseTask {

    @Autowired
    TaskService taskService;

    @Scheduled(cron = "0/3 * * * * *")
    //定时发送添加选课任务
    public void sendChooseCourseTask(){
        //得到1分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> xcTaskList = taskService.findXcTaskList(time, 100);
        System.out.println(xcTaskList);

        //调用service发布消息,将添加选课的任务发送给mq
        for(XcTask xcTask : xcTaskList){
            //取任务
            if(taskService.getTask(xcTask.getId(),xcTask.getVersion())>0){
                String exchange = xcTask.getMqExchange();//交换机
                String routingkey = xcTask.getMqRoutingkey();
                taskService.publish(xcTask,exchange,routingkey);
            }
        }
    }


}
