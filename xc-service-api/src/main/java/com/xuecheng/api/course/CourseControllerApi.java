package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "课程管理接口",description = "提供课程的增删改查")
public interface CourseControllerApi {
    @ApiOperation("课程计划查询")
    TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("添加课程计划")
    ResponseResult addTeachplan(Teachplan teachplan);

    @ApiOperation("分页查询我的课程")
    QueryResponseResult findCourseList(
            int page,
            int size,
            CourseListRequest courseListRequest
    );
    @ApiOperation("添加课程")
    AddCourseResult addCourseBase(CourseBase courseBase);

    @ApiOperation("根据课程id查询课程")
    CourseBase getCourseBaseById(String courseId);

    @ApiOperation("根据课程id修改课程")
    ResponseResult updateCourseBase(String courseId,CourseBase courseBase);

    @ApiOperation("获取课程营销信息")
    CourseMarket getCourseMarketById(String courseId);

    @ApiOperation("更新课程营销信息")
    ResponseResult updateCourseMarket(String id,CourseMarket courseMarket);

    @ApiOperation("添加课程图片")
    ResponseResult addCoursePic(String courseId,String pic);

    @ApiOperation("查询课程图片")
    CoursePic findCoursePic(String courseId);

    @ApiOperation("删除课程图片")
    ResponseResult deleteCoursePic(String courseId);

    @ApiOperation("课程视图查询")
    CourseView courseView(String courseId);

    @ApiOperation("预览课程")
    CoursePublishResult preview(String id);

    @ApiOperation("预览发布")
    CoursePublishResult publish(String id);

    @ApiOperation("保存课程计划与媒资文件的关联")
    ResponseResult saveMedia(TeachplanMedia teachplanMedia);


}
