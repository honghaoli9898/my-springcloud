package com.seaboxdata.sdps.item.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.SdpsItem;
import com.seaboxdata.sdps.common.core.model.SdpsTenantResource;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.item.dto.item.ItemDto;
import com.seaboxdata.sdps.item.vo.item.DataHistogramRequest;
import com.seaboxdata.sdps.item.vo.item.ItemReourceRequest;
import com.seaboxdata.sdps.item.vo.item.ItemRequest;

public interface IItemService extends ISuperService<SdpsItem> {

	public Long insertItem(SdpsItem item);

	public void insertItemAndResource(SdpsItem item, ItemReourceRequest resources);

	public boolean insertResources(ItemReourceRequest resources);

	public void deleteItems(ItemRequest request);

	public void updateResourcesById(SdpsTenantResource resources);

	public PageResult<ItemDto> findItemMembers(PageRequest<ItemRequest> request);

	public PageResult<ItemDto> findGroups(PageRequest<ItemRequest> request);

	public void updateItem(SdpsItem item);

	public List<SdpsTenantResource> findItemResourcesById(ItemRequest request);

	public void insertMemberByItemId(ItemRequest request);

	public void insertGroupByItemId(ItemRequest request);

	public void deleteGroupByItemId(ItemRequest request);

	public void deleteMembersByItemId(ItemRequest request);

	public void updateMembersByItemId(ItemRequest request);

	public void updateGroupByItemId(ItemRequest request);

	public List<ItemDto> selectAllMembersByItemId(ItemRequest request);

	public List<ItemDto> selectAllGroupByItemId(ItemRequest request);

	public List<ItemDto> selectItemByExample(ItemRequest request);

	public List<ItemDto> selectItemByUser(Long userId, boolean isManager);

	public List<ItemDto> selectNotExisitMemberByItemId(Long itemId);

	public List<ItemDto> selectNotExisitGroupByItemId(Long itemId);

	public List<SdpsCluster> selectClusterByItemId(Long id);
	
	public List<ItemDto> selectCurrentUserHasItemInfo(SysUser sysUser);

	public PageResult<ItemDto> findItems(Integer page, Integer size,
			ItemRequest param);

	public List<ItemDto> findItemsByUser(ItemRequest request);

	public JSONObject selectItemRankInfo();

	public JSONObject selectDataRankInfo();

	public JSONObject getHistogram(DataHistogramRequest request) throws Exception;

	public void updateItemYarnResources(ItemReourceRequest resources);
}
