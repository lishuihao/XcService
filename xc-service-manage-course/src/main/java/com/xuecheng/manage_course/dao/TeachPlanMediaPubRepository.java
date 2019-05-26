package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachPlanMediaPubRepository extends JpaRepository<TeachplanMediaPub,String> {
        //根据课程Id删除记录
    long deleteByCourseId(String courseId);
}
