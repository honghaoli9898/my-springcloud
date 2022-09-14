package com.seaboxdata.sdps.bigdataProxy.service;

import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeDetailRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeRequest;

public interface IFileMergeService {

    /**
     * 校验表是否能合并
     * @param fileMergeRequest 请求参数
     * @return
     */
    Result checkTableMerge(FileMergeRequest fileMergeRequest);

    /**
     * 回显要合并的路径信息
     * @param fileMergeDetailRequest
     * @return
     */
    Result getFileMergeDetail(FileMergeDetailRequest fileMergeDetailRequest);

}
