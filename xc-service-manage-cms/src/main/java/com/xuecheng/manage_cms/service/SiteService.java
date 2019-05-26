package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResultCode;
import com.xuecheng.manage_cms.dao.CmsSiteReqository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteService {

    @Autowired
    CmsSiteReqository cmsSiteReqository;

    public QueryResponseResult findAllSites() {
        List<CmsSite> all = cmsSiteReqository.findAll();
        QueryResult queryReslut = new QueryResult();
        queryReslut.setList(all);
        QueryResponseResult result = new QueryResponseResult(CommonCode.SUCCESS,queryReslut);
        return result;
    }
}
