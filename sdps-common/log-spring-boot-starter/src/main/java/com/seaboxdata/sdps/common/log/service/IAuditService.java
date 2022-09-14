package com.seaboxdata.sdps.common.log.service;

import com.seaboxdata.sdps.common.log.model.Audit;

/**
 * 审计日志接口
 *
 */
public interface IAuditService {
    void save(Audit audit);
}
