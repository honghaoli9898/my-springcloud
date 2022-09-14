package com.seaboxdata.sdps.user.mybatis.mapper;

import com.seaboxdata.sdps.user.mybatis.model.SecrecyKey;

import java.util.List;

public interface SecrecyKeyMapper {
    List<SecrecyKey> selectList(SecrecyKey secrecyKey);
    List<SecrecyKey> selectListByName(SecrecyKey secrecyKey);
//    SecrecyKey selectById(Integer id);
    void insert(SecrecyKey secrecyKey);
    void update(SecrecyKey secrecyKey);
    void delete(Long[] ids);
}
