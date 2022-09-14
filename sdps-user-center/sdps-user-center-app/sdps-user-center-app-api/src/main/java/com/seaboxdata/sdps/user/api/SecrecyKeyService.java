package com.seaboxdata.sdps.user.api;

import com.seaboxdata.sdps.user.mybatis.model.SecrecyKey;

import java.util.List;

public interface SecrecyKeyService {

    List<SecrecyKey> selectList(SecrecyKey secrecyKey);

    List<SecrecyKey> selectListByName(SecrecyKey secrecyKey);

    void insert(SecrecyKey secrecyKey);

    void update(SecrecyKey secrecyKey);

    void delete(Long[] ids);

}
