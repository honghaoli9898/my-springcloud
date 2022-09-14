package com.seaboxdata.sdps.user.service.impl;

import com.seaboxdata.sdps.user.api.SecrecyKeyService;
import com.seaboxdata.sdps.user.mybatis.mapper.SecrecyKeyMapper;
import com.seaboxdata.sdps.user.mybatis.model.SecrecyKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecrecyKeyServiceImpl implements SecrecyKeyService {

    @Autowired
    private SecrecyKeyMapper secrecyKeyMapper;

    @Override
    public List<SecrecyKey> selectList(SecrecyKey secrecyKey) {
        return secrecyKeyMapper.selectList(secrecyKey);
    }

    @Override
    public List<SecrecyKey> selectListByName(SecrecyKey secrecyKey) {
        return secrecyKeyMapper.selectListByName(secrecyKey);
    }

//    @Override
//    public SecrecyKey selectById(Integer id) {
//        return secrecyKeyMapper.selectById(id);
//    }

    @Override
    public void insert(SecrecyKey secrecyKey) {
        secrecyKeyMapper.insert(secrecyKey);
    }

    @Override
    public void update(SecrecyKey secrecyKey) {
        secrecyKeyMapper.update(secrecyKey);
    }

    @Override
    public void delete(Long[] ids) {
        secrecyKeyMapper.delete(ids);
    }
}
