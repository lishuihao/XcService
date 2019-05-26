package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitMqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteReqository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService{

    @Autowired
    CmsPageRepository repository;

    @Autowired
    CmsConfigRepository configRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteReqository cmsSiteReqository;

    /**
     * 页面查询方法
     * @param page  页码，从1开始计数
     * @param size  每页记录数
     * @param queryPageRequest  查询条件
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {

        if(queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }

        //自定义条件查询
        //定义一个条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        ExampleMatcher withMatcher = exampleMatcher.withMatcher("pageAliase",
                ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //设置条件值(站点Id)
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置条件值(模板Id)
        if(StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置条件值(页面别名，模糊查询)
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        Example<CmsPage> example =Example.of(cmsPage,withMatcher);
        //定义条件对象
        if(page<=0){
            page=1;
        }
        page = page-1;

        if(size<=0){
            size=10;
        }

        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = repository.findAll(example,pageable);
        QueryResult<CmsPage> queryResult= new QueryResult<>();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        QueryResponseResult result = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return result;
    }

    /*
    新增页面
     */
    public CmsPageResult add(CmsPage cmsPage){
        //校验,页面名称，站点Id,页面webpath的唯一性
        //根据校验,页面名称，站点Id,页面webpath查询cms_page集合，如果查到，说明此页面已经存在，反之继续添加
        CmsPage page = repository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(!(page == null)){
            //页面已经存在，抛出异常
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        cmsPage.setPageId(null);
        CmsPage save = repository.save(cmsPage);
        CmsPageResult result = new CmsPageResult(CommonCode.SUCCESS,save);
        return result;
    }

    /*
    根据id查询页面
     */
    public CmsPage findById(String id){
        Optional<CmsPage> optional = repository.findById(id);
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return null;
    }

    /*
    修改页面
     */
    public CmsPageResult update(String id,CmsPage cmsPage){
        //根据id查询页面信息
        CmsPage page = this.findById(id);
        if(page != null){
            //设置要修改的数据
            page.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            page.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            page.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            page.setPageName(cmsPage.getPageName());
            //更新访问路径
            page.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            page.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataUrl
            page.setDataUrl(cmsPage.getDataUrl());

            //调教修改
            CmsPage save = repository.save(page);
            return new CmsPageResult(CommonCode.SUCCESS,save);
        }
        //修改失败
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    /*
    根据iD删除页面
     */
    public ResponseResult delete(String id){
        //先查询一下
        Optional<CmsPage> optional = repository.findById(id);
        if(optional.isPresent()){
            //执行删除
            repository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 根据id查询cmsConfig
     */
    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> optional = configRepository.findById(id);
        if (optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }

    /**
     * 页面静态化
     */
       /*
        静态化程序获取页面的DataUrl

        静态化程序远程请求DataUrl获取数据模型

        静态化程序获取页面的模板信息

        执行页面静态化
         */
    public String getPageHtml(String pageId){
     //1.获取数据模型
        Map model = getModelByPageId(pageId);
        if(model == null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面模板信息
        String template = getTemplateByPageId(pageId);
        if(StringUtils.isEmpty(template)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态化
        String html = gennerateHtml(template, model);
        return html;
    }

    //获取数据模型
    private Map getModelByPageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if(cmsPage == null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXCIST);
        }
        //取出页面的dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            //页面的url为空异常
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate请求Dataurl获取数据
        ResponseEntity<Map> entity = restTemplate.getForEntity(dataUrl, Map.class);
        return  entity.getBody();
    }

    /*
    根据页面id获取模板
     */
    private String getTemplateByPageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if(cmsPage == null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXCIST);
        }
        //获取页面的模板id
        String templateId = cmsPage.getTemplateId();
        if(StringUtils.isEmpty(templateId)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //获取模板信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if(optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件id
            String fileId = cmsTemplate.getTemplateFileId();
            //从gridFs中取模板文件的内容
                GridFSFile one = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
            //打开一个下载流
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(one.getObjectId());
            //GridFsResource对象，获取流
            GridFsResource gridFsResource=new GridFsResource(one,gridFSDownloadStream);
            //从流中取数据
            try {
                String string = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return string;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  null;
    }

    //执行静态化
    private String gennerateHtml(String templateContent,Map model){
        //创建配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContent);
        //向configutration中配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板内容
        try {
            Template template = configuration.getTemplate("template");
            //调用api进行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    //执行页面的发布
    public ResponseResult post(String pageId){
        //执行页面静态化
        String pageHtml = this.getPageHtml(pageId);
        //将页面静态化的文件存储到gridFs中
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        //向mq发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //保存html到gridFs
    private CmsPage saveHtml(String pageId,String htmlContent){

        //先得到页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CommonCode.InVALID_PARAM);
        }
        ObjectId objectId = null;
        //将htmlContent 内容转为输入流
        try {
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //将html文件内容保存到gridFs中
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将html的Id更新到cmsPage中
        cmsPage.setHtmlFileId(objectId.toHexString());
        repository.save(cmsPage);
        return cmsPage;
    }

    //向mq发送消息
    private void sendPostPage(String pageId){
        //得到页面信息
        CmsPage cmsPage = this.findById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CommonCode.InVALID_PARAM);
        }
        //拼装消息对象
        Map<String,String> msg = new HashMap<>();
        msg.put("pageId",pageId);
        //转成json串
        String jsonString = JSON.toJSONString(msg);
        System.out.println(jsonString);
        //发送给mq
        String siteId = cmsPage.getSiteId();
        rabbitTemplate.convertAndSend(RabbitMqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,jsonString);
    }

    //保存页面,有则更新，没有，则添加
    public CmsPageResult save(CmsPage cmsPage) {
        //判断页面是否存在
        CmsPage page = repository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(page != null){
            //进行更新
            return this.update(page.getPageId(),cmsPage);
        }
        //进行添加
        return this.add(cmsPage);

    }

    //一键发布
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {

        //将页面信息存储到cmspage集合中
        CmsPageResult save = this.save(cmsPage);
        if(!save.isSuccess()){
            //抛出异常
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面id
        CmsPage cmsPagesave = save.getCmsPage();
        String pageId = cmsPagesave.getPageId();
        //执行页面发布(先静态化，保存gridFs,向MQ发送消息)
        ResponseResult post = this.post(pageId);
        if(!post.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼接页面Url
        //取出站点Id
        String siteId = cmsPagesave.getSiteId();
        CmsSite site = this.findCmsSiteById(siteId);
        String pageUrl = site.getSiteDomain()+site.getSiteWebPath()+cmsPagesave.getPageWebPath()+cmsPage.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }

    //根据站点id查询站点信息
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteReqository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }


}
