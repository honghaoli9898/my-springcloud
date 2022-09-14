package com.seaboxdata.sdps.user.service.impl;

import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.user.api.IUserGroupService;
import com.seaboxdata.sdps.user.mybatis.mapper.UserGroupMapper;
import com.seaboxdata.sdps.user.mybatis.model.UserGroup;

/**
 * @author 作者 owen E-mail: 624191343@qq.com
 */
@Slf4j
@Service
public class UserGroupServiceImpl extends
		SuperServiceImpl<UserGroupMapper, UserGroup> implements
		IUserGroupService {
	@Autowired
	private SqlSessionFactory sqlSessionFactory;

	@Override
	@Transactional
	public boolean deletUserGroupItems(List<UserGroup> userGroups) {
		userGroups
				.forEach(obj -> {
					QueryWrapper<UserGroup> queryWrapper = new QueryWrapper<UserGroup>();
					if (Objects.nonNull(obj.getUserId())) {
						queryWrapper.eq("user_id", obj.getUserId());
					}
					if (Objects.nonNull(obj.getGroupId())) {
						queryWrapper.eq("group_id", obj.getUserId());
					}
					remove(queryWrapper);
				});
		return true;
	}

	@Transactional
	@Override
	public int insertBatch(List<UserGroup> list) {
		// 打开批处理
		SqlSession sqlSession = sqlSessionFactory.openSession(
				ExecutorType.BATCH, false);
		UserGroupMapper mapper = sqlSession.getMapper(UserGroupMapper.class);

		// 1000提交一次 大数据量情况下防止内存溢出
		for (int i = 0; i <= list.size() - 1; i++) {
			UserGroup userGroup = list.get(i);
			mapper.delete(new QueryWrapper<UserGroup>().eq("user_id",
					userGroup.getUserId()).eq("group_id",
					userGroup.getGroupId()));
			if ((i+1) % 999 == 0) {
				sqlSession.commit();
				sqlSession.clearCache();
			}
		}
		sqlSession.commit();
		sqlSession.clearCache();
		return this.baseMapper.insertBatchSomeColumn(list);
	}

}