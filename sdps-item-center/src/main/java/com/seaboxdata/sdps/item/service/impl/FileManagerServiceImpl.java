package com.seaboxdata.sdps.item.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.constant.ClusterConstants;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.SdpsItem;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.item.dto.item.ItemDto;
import com.seaboxdata.sdps.item.mapper.SdpsFileManagerMapper;
import com.seaboxdata.sdps.item.mapper.SdpsItemMapper;
import com.seaboxdata.sdps.item.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.item.model.SdpsFileManager;
import com.seaboxdata.sdps.item.service.FileService;
import com.seaboxdata.sdps.item.service.IFileManagerService;
import com.seaboxdata.sdps.item.service.IItemService;
import com.seaboxdata.sdps.item.vo.item.ItemRequest;
import com.seaboxdata.sdps.item.vo.script.ScriptRequest;

@Slf4j
@Service
public class FileManagerServiceImpl extends
		ServiceImpl<SdpsFileManagerMapper, SdpsFileManager> implements
		IFileManagerService {
	private static final List<String> allowFileTypes = CollUtil.newArrayList();
	static {
		allowFileTypes.add("py");
		allowFileTypes.add("sh");
		allowFileTypes.add("jar");
		allowFileTypes.add("zip");
		allowFileTypes.add("sql");
	}

	@Autowired
	private SdpsItemMapper itemMapper;
	@Autowired
	private SdpsServerInfoMapper rangerInfoMapper;
	@Autowired
	private final Map<String, FileService> fileServiceMap = new ConcurrentHashMap<>();
	@Autowired
	private IItemService itemService;

	@Override
	@Transactional
	public Object uploadFile(SysUser sysUser, Integer clusterId, Long itemId,
			MultipartFile file, String belong, String serverType) {
		String type = checkFileType(file);
		String path = null;
		if (ServerTypeEnum.S.name().equalsIgnoreCase(serverType)) {

			switch (belong) {
			case "O":
				path = ClusterConstants.OWN_FILE_PATH
						.concat(sysUser.getUsername()).concat("/")
						.concat(file.getOriginalFilename());
				break;
			case "P":
				if (Objects.isNull(itemId)) {
					throw new BusinessException("项目id不能为空");
				}
				SdpsItem sdpsItem = itemMapper.selectById(itemId);
				path = ClusterConstants.PROJECT_FILE_PREFIX
						.concat(sdpsItem.getIden()).concat("/")
						.concat(file.getOriginalFilename());
				break;
			default:
				break;
			}
		}
		long count = this.count(new QueryWrapper<SdpsFileManager>().eq(
				"cluster_id", clusterId).eq("path", path));
		if (count > 0L) {
			throw new BusinessException("上传文件已存在");
		}
		if (!fileServiceMap.containsKey(ServerTypeEnum
				.getCodeByName(serverType))) {
			throw new BusinessException("该类型服务暂未提供此功能");
		}
		SdpsFileManager sdpsFileManager = new SdpsFileManager();
		sdpsFileManager.setBelong(belong);
		sdpsFileManager.setClusterId(Long.valueOf(clusterId));
		sdpsFileManager.setItemId(itemId);
		sdpsFileManager.setCreateUsername(sysUser.getUsername());
		sdpsFileManager.setCreateUser(sysUser.getId());
		sdpsFileManager.setPath(path);
		sdpsFileManager.setCreateTime(DateUtil.date());
		sdpsFileManager.setName(file.getOriginalFilename());
		sdpsFileManager.setType(type);
		sdpsFileManager.setServerType(serverType);
		log.info("保存文件:{}",path);
		this.save(sdpsFileManager);
		SdpsServerInfo sdpsServerInfo = rangerInfoMapper.selectServerInfo(
				clusterId, serverType);
		return fileServiceMap.get(ServerTypeEnum.getCodeByName(serverType))
				.upload(file, sdpsServerInfo, clusterId,
						path.substring(0, path.lastIndexOf("/")),sysUser.getUsername());

	}

	private String checkFileType(MultipartFile file) {
		try {
			String fileName = file.getOriginalFilename();
			String type = FileTypeUtil.getType(file.getInputStream(), fileName);
			if (StrUtil.isBlank(type)) {
				type = fileName.substring(fileName.lastIndexOf(".") + 1);
			}
			if (StrUtil.isBlank(type)) {
				throw new BusinessException("上传文件不符合要求");
			}
			// 根据首部字节判断文件类型
			if (CollUtil.contains(allowFileTypes, type.toLowerCase())) {
				return type;
			}
		} catch (Exception e) {
			log.error("上传文件失败", e);
			throw new BusinessException("上传文件失败");
		}
		throw new BusinessException("上传文件不符合要求");
	}

	@Override
	public void downloadFile(Long fileId) {
		SdpsFileManager sdpsFileManager = this.getById(fileId);
		Long clusterId = sdpsFileManager.getClusterId();
		String serverType = sdpsFileManager.getServerType();
		String path = sdpsFileManager.getPath();
		SdpsServerInfo sdpsServerInfo = rangerInfoMapper.selectServerInfo(
				clusterId.intValue(), serverType);
		fileServiceMap.get(ServerTypeEnum.getCodeByName(serverType)).download(
				path, sdpsServerInfo, clusterId.intValue(), serverType);
	}

	@Override
	@Transactional
	public void removeFile(Long fileId,String username) {
		SdpsFileManager sdpsFileManager = this.getById(fileId);
		Long clusterId = sdpsFileManager.getClusterId();
		String serverType = sdpsFileManager.getServerType();
		String path = sdpsFileManager.getPath();
		SdpsServerInfo sdpsServerInfo = rangerInfoMapper.selectServerInfo(
				clusterId.intValue(), serverType);
		this.removeById(fileId);
		fileServiceMap.get(ServerTypeEnum.getCodeByName(serverType)).delete(
				path, sdpsServerInfo, clusterId.intValue(), serverType,username);
	}

	@Override
	public Page<SdpsFileManager> pageList(SysUser sysUser,
			PageRequest<ScriptRequest> request) {
		Long userId = sysUser.getId();
		List<Long> itemIds = itemService.selectItemByExample(new ItemRequest())
				.stream().map(ItemDto::getId).collect(Collectors.toList());
		request.getParam().setIds(itemIds);
		request.getParam().setUserId(userId);
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<SdpsFileManager> result = this.baseMapper.pageList(request
				.getParam());
		return result;
	}

}
