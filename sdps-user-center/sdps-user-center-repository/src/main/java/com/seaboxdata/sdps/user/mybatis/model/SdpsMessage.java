package com.seaboxdata.sdps.user.mybatis.model;


import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;

/**
 * @author pengsong
 */
@Data
@TableName("sdps_message")
public class SdpsMessage{
    private Long id;
    //消息内容
    private String messageContent;
    //发送人id
    private Long senderId;
    //发送人
    private String senderName;
    //接收人id
    private Long receiverId;
    //接收人
    private String receiverName;
    //是否已读
    private Integer isAlreadyRead;
    //前往路径
    private String url;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //逻辑删除
    private Integer isDelete;
    //类型
    private Integer type;
}
