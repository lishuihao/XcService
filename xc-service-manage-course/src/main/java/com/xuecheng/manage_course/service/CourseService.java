package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachPlanMediaRepository teachPlanMediaRepository;

    @Autowired
    TeachPlanMediaPubRepository teachPlanMediaPubRepository;


    @Value("${course‐publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course‐publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course‐publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course‐publish.siteId}")
    private String publish_siteId;
    @Value("${course‐publish.templateId}")
    private String publish_templateId;
    @Value("${course‐publish.previewUrl}")
    private String previewUrl;

    /*
    课程计划的查询
     */
    public TeachplanNode findTeachplanList(String courseId){
        return teachplanMapper.selectList(courseId);
    }

    /*
    添加课程计划
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if(teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) ||
            StringUtils.isEmpty(teachplan.getPname())
        ){
            ExceptionCast.cast(CommonCode.InVALID_PARAM);
        }
        //课程Id
        String courseid = teachplan.getCourseid();
        //页面传入的parentId
        String parentid = teachplan.getParentid();
        if(StringUtils.isEmpty(parentid)){
            //为空，取出该课程的根节点
            parentid = this.getTeachplanRoot(courseid);
        }
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        //父节点的grade
        String grade = parentNode.getGrade();
        //新节点
        Teachplan teachplanNew = new Teachplan();
        //将页面提交的teachPlan信息拷贝到teacheNew对象中
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        if(grade.equals("1")){
            teachplanNew.setGrade("2");//设置级别,根据父节点级别来设置
        }else{
            teachplanNew.setGrade("3");//设置级别,根据父节点级别来设置
        }
        teachplanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /*
    查询课程根节点,如果查询不到，要自动添加根节点
     */
    private String getTeachplanRoot(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return null;
        }
        CourseBase courseBase = optional.get();
        //查询课程根节点
        List<Teachplan> list = this.teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if(list == null || list.size()<=0){
            //查询不到，要自动添加根节点
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        //返回根节点Id
        return list.get(0).getId();
    }

    public QueryResponseResult findCourseList(int page, int size, CourseListRequest
            courseListRequest) {
        if(courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        if(page == 0){
            page = 1;
        }
        if(size == 0){
            size=20;
        }
        PageHelper.startPage(page,size);
        Page<CourseInfo> courseInfoPage = courseMapper.findCourseListPage(courseListRequest);
        //查询到的分页结果
        List<CourseInfo> result = courseInfoPage.getResult();
        //查询到的总记录条数
        long total = courseInfoPage.getTotal();
        QueryResult queryResult = new QueryResult();
        queryResult.setList(result);
        queryResult.setTotal(total);
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    /*
    添加课程
     */
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase){
        //课程状态默认为未发布
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    /*
     * 根据课程Id查询课程
     * @param courseId
     * @return
     */
    public CourseBase getCourseBaseById(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            return courseBase;
        }
        return null;
    }

    /*
    根据课程id修改课程
     */
    @Transactional
    public ResponseResult updateCoursebase(String id, CourseBase courseBase) {
        CourseBase one = this.getCourseBaseById(id);
        if(one == null){
            ExceptionCast.cast(CommonCode.InVALID_PARAM);
        }
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        CourseBase base = courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /*
    根据iD获取课程营销信息
     */
    public CourseMarket getCourseMarketById(String courseId){
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    /*
    根据Id更新课程营销信息，如果不存在，则插入
     */
    @Transactional
    public CourseMarket updateCourseMarket(String id, CourseMarket courseMarket){
        CourseMarket market = this.getCourseMarketById(id);
        if(market == null){
            if(courseMarket != null){
                market = new CourseMarket();
                BeanUtils.copyProperties(courseMarket,market);
                market.setId(id);
                CourseMarket save = courseMarketRepository.save(market);
                return save;
            }
            ExceptionCast.cast(CommonCode.InVALID_PARAM);
        }
        market.setCharge(courseMarket.getCharge());
        market.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
        market.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
        market.setPrice(courseMarket.getPrice());
        market.setQq(courseMarket.getQq());
        market.setValid(courseMarket.getValid());
        CourseMarket save = courseMarketRepository.save(market);
        return save;
    }

    /*
    向课程管理数据库来添加课程与图片的关联信息
     */
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        //课程图片的信息
        CoursePic coursePic = null;
        //先查询课程图片，如果有，就直接设置变成更新
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if(optional.isPresent()){
            coursePic = optional.get();
        }
        if(coursePic == null){
        coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    //删除课程图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        //当返回值大于0，表示删除成功的记录数
        long l = coursePicRepository.deleteByCourseid(courseId);
        if(l > 0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //查询课程视图
    public CourseView getCourseView(String courseId) {

        CourseView courseView = new CourseView();

        //基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
        if(courseBaseOptional.isPresent()){
            courseView.setCourseBase(courseBaseOptional.get());
        }
        //图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            courseView.setCoursePic(picOptional.get());
        }
        //营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(courseId);
        if(courseMarketOptional.isPresent()){
            courseView.setCourseMarket(courseMarketOptional.get());
        }
        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }


    /*
    课程预览
     */
    public CoursePublishResult preview(String id) {
        CourseBase courseBase = this.findCourseBaseById(id);
        //请求cms拼装页面
        //准备cmspage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setDataUrl(publish_dataUrlPre+id);
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(courseBase.getName()); //页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);
        //远程调用cms
        CmsPageResult result = cmsPageClient.saveCmsPage(cmsPage);
        if(!result.isSuccess()){
            //抛出异常
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CmsPage page = result.getCmsPage();
        String pageId = page.getPageId();
        //拼装页面预览的url
        String preview=previewUrl+pageId;
        //返回CoursePublishResult对象(当中已经包含了页面预览的url)
            return new CoursePublishResult(CommonCode.SUCCESS,preview);
    }

    @Transactional
    //课程发布
    public CoursePublishResult publish(String id) {
        //查询课程
        CourseBase courseBase = findCourseBaseById(id);

        //调用Cms一键发布接口，将课程详情页面发布到服务器
        //准备页面信息
        CmsPage cmsPage =  new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setDataUrl(publish_dataUrlPre+id);
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(courseBase.getName()); //页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);

        if(!cmsPostPageResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }

        //保存课程的发   布状态为  已发布
        CourseBase courseBase1 = saveCoursePubState(id);
        if(courseBase1 == null){
            ExceptionCast.cast(CommonCode.FAIL);
        }

        //保存课程索引信息
        //1.创建一个叫CoursePub对象
        CoursePub coursePub = createCoursePub(id);
        //2.将CoursePub对象保存到数据库
        saveCoursePub(id,coursePub);
        //缓存课程信息

        //得到页面Url
        String pageUrl = cmsPostPageResult.getPageUrl();

        //向teachplanPub中保存课程媒资信息
        saveTeachPlanMediaPub(id);

        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    //向teachplanPub中保存课程媒资信息
    private void saveTeachPlanMediaPub(String courseId){
        //先删除teachPlanMediaPub中的数据
        teachPlanMediaPubRepository.deleteByCourseId(courseId);
        //从teachPlanMedia中查询
        List<TeachplanMedia> mediaList = teachPlanMediaRepository.findByCourseId(courseId);
        //将mediaList插入到teachplanPub中
        List<TeachplanMediaPub>  teachplanMediaPubs = new ArrayList<>();

        for (TeachplanMedia teachplanMedia : mediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }

        //将课程媒资信息插入到pub中
        teachPlanMediaPubRepository.saveAll(teachplanMediaPubs);

    }

    //更新课程发布状态
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //更新发布状态
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }

    //创建CoursePub对象
    private CoursePub createCoursePub(String courseId){
        CoursePub coursePub = new CoursePub();
        //根据课程Id查询course_base
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            //将courseBase属性拷贝到coursePub中
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(courseId);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        String jsonString = JSON.toJSONString(teachplanNode);
        //最终将课程计划信息json串保存到course_pub中
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    //将CoursePub保存到数据库
    private CoursePub saveCoursePub(String courseId,CoursePub coursePub){

        CoursePub coursePubNew = null;

        //根据课程Id查询CoursePub
        Optional<CoursePub> optional = coursePubRepository.findById(courseId);
        if(optional.isPresent()){
            coursePubNew = optional.get();
        }else {
            coursePubNew = new CoursePub();
        }

        //将coursePub对象中的信息保存到coursePubNew中（拷贝）
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(courseId);
        //时间戳,给logstach使用
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    //保存课程计划与媒资文件的关联信息
    public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.InVALID_PARAM);
        }
        //校验课程计划是否为第三级
        //查询课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.InVALID_PARAM);
        }
        //查询到教学计划
        Teachplan teachplan = optional.get();
        //取出等级
        String grade = teachplan.getGrade();
        if(StringUtils.isEmpty(grade) || !grade.equals("3")){
            ExceptionCast.cast(CourseCode.COURSE_MEDIS_TEACHPLAN_GRADEERROR);
        }

        //查询teachplanmeida
        Optional<TeachplanMedia> mediaOptional = teachPlanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if(mediaOptional.isPresent()){
          one = mediaOptional.get();
        }else {
            one = new TeachplanMedia();
        }
        //将one保存或更新到数据库
        one.setCourseId(teachplan.getCourseid());//课程Id
        one.setMediaId(teachplanMedia.getMediaId());//媒资文件Id
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件的原始Id
        one.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件的Url
        one.setTeachplanId(teachplanId);
        teachPlanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }
}
