package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    public CmsTemplateResult findAllTemplates(){
        List<CmsTemplate> all = cmsTemplateRepository.findAll();
        CmsTemplateResult result = new CmsTemplateResult(CommonCode.SUCCESS, all);
        return result;
    }

}
