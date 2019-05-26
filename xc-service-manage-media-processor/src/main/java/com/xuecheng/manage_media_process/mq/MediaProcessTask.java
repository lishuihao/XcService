package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {

    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg_path;

    @Value("${xc-service-manage-media.video-location}")
    String video_location;

    @Autowired
    MediaFileRepository mediaFileRepository;


    //接收视频处理的消息，进行视频处理
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg){
        //1.解析消息内容，得到MediaId
        Map map = JSON.parseObject(msg, Map.class);
        String mediaId = (String) map.get("mediaId");
        //2.拿mediaId去数据库中查询media媒资文件信息，拿到文件地址
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            return;
        }
        MediaFile mediaFile = optional.get();
        String fileType = mediaFile.getFileType();
        if(!fileType.equals("avi")){
            //无需处理
            mediaFile.setFileStatus("303004");
            mediaFileRepository.save(mediaFile);
            return;
        }else {
            //需要处理
            mediaFile.setFileStatus("303001");//处理中
            mediaFileRepository.save(mediaFile);
        }
        String filePath=video_location+mediaFile.getFilePath()+mediaFile.getFileName();
        String mp4_home=video_location+mediaFile.getFilePath();

        //3.使用ffmpegUtils将avi文件生成mp4
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,filePath,mediaFile.getFileId()+".mp4",mp4_home);
        String result = mp4VideoUtil.generateMp4();
        if(result == null || !result.equals("success")){
            //处理失败
            mediaFile.setFileStatus("303003");
            //定义mediaFileProcess_m3u8
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            //记录失败原因
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //4.将mp4生成m3u8和ts文件
        String mp4Video_path=video_location+mediaFile.getFilePath()+mediaFile.getFileId()+".mp4";
        //m3u8文件名称
        String m3u8_name = mediaFile.getFileId()+".m3u8";
        //m3u8文件所在目录
        String m3u8_path=video_location+mediaFile.getFilePath()+"hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path,mp4Video_path,m3u8_name,m3u8_path);
        //生成m3u8和ts文件
        String tsresult = hlsVideoUtil.generateM3u8();
        if(tsresult == null || !tsresult.equals("success")){
            //处理失败
            mediaFile.setFileStatus("303003");
            //定义mediaFileProcess_m3u8
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            //记录失败原因
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //处理成功
        //获取ts文件列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        mediaFile.setProcessStatus("303002");
        //定义mediaFileProcess_m3u8
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

        //保存fileUrl(就是视频播放的相对路径)
        String fileUrl=mediaFile.getFilePath()+"hls/"+m3u8_name;
        mediaFile.setFileUrl(fileUrl);
        mediaFileRepository.save(mediaFile);
    }

}
