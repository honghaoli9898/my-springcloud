package com.seaboxdata.sdps.seaboxProxy.service;

import com.seaboxdata.sdps.common.framework.bean.dto.FileMergeDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.HiveTableDTO;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeDetailRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeRequest;

public interface ISeaBoxFileMergeService  {
    /**
     * 资源管理 -> 小文件合并 -> 校验表合并
     * @param fileMergeRequest 请求参数
     * @return
     */
    HiveTableDTO checkTableMerge(FileMergeRequest fileMergeRequest);

    /**
     * 资源管理 -> 小文件合并 -> 下一步
     * @param fileMergeDetailRequest
     * @return
     */
    FileMergeDTO getFileMergeDetail(FileMergeDetailRequest fileMergeDetailRequest);
}
