package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFSTest {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    //存文件
    @Test
    public void testGridFs() throws FileNotFoundException {
        //定义file
        File file = new File("d:/course.ftl");
        //定义文件输入流
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectId store = gridFsTemplate.store(fileInputStream, "course.ftl");
        System.out.println(store);
    }


    //测试取文件
    @Test
    public void queryFile() throws IOException {
        GridFSFile one = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5c4d625c3e675e379c7b80e3")));
        //打开一个下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(one.getObjectId());
        //GridFsResource对象，获取流
        GridFsResource gridFsResource=new GridFsResource(one,gridFSDownloadStream);
        //从流中取数据
        String string = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
        System.out.println(string);

    }

    }
