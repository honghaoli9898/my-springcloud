package com.seaboxdata.sdps.seaboxProxy.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;
import com.seaboxdata.sdps.common.framework.enums.DirFileType;
import com.seaboxdata.sdps.common.framework.enums.QueryEnum;
import com.seaboxdata.sdps.seaboxProxy.config.CommonConstraint;
import com.seaboxdata.sdps.seaboxProxy.config.DynamicDataSourceConfig;
import com.seaboxdata.sdps.seaboxProxy.mapper.SeaBoxStatMapper;
import com.seaboxdata.sdps.seaboxProxy.service.IStorageResourceService;
import com.seaboxdata.sdps.seaboxProxy.util.SeaboxDateUtil;

@Service
public class SeaBoxStorageResourceServiceImpl implements
		IStorageResourceService {
	@Autowired
	private DynamicDataSourceConfig dynamicDataSourceConfig;
	@Autowired
	private SeaBoxStatMapper seaBoxStatMapper;

	@Override
	public Page<StorgeDirInfo> getItemStorage(StorgeRequest storgeRequest) {
		checkParam(storgeRequest,true);
		List<StorgeDirInfo> endDayData = (Page<StorgeDirInfo>) getEndDayData(
				storgeRequest, 0);
		Map<String, List<StorgeDirInfo>> startDayMap = getStartDayData(
				endDayData, storgeRequest, 0);
		return (Page<StorgeDirInfo>) getResult(endDayData, startDayMap,
				storgeRequest.getOrderColumnType(), 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page<StorgeDirInfo> getFileStorageByTenant(
			StorgeRequest storgeRequest) {
		checkParam(storgeRequest,false);
		storgeRequest.setTenants((Set<String>) Convert.toCollection(
				HashSet.class, String.class, storgeRequest.getTenant()));
		List<StorgeDirInfo> endDayData = getEndDayData(storgeRequest, 1);
		Map<String, List<StorgeDirInfo>> startDayMap = getStartDayData(endDayData, storgeRequest, 1);
		return (Page<StorgeDirInfo>)getResult(endDayData, startDayMap,
				storgeRequest.getOrderColumnType(), 1);
	}

	@Override
	public Page<StorgeDirInfo> subStorgeTrend(StorgeRequest storgeRequest) {
		checkParam(storgeRequest,false);
		if (QueryEnum.PATH.equals(storgeRequest.getStorageType())) {
			checkPathParam(storgeRequest);
			storgeRequest.getTypes().add(DirFileType.UNKOWN.getIndex());
		}
		Page<StorgeDirInfo> endDayData = (Page<StorgeDirInfo>) getEndDayData(
				storgeRequest, 2);
		System.out.println(endDayData.getResult());
		Map<String, List<StorgeDirInfo>> startDayMap = getStartDayData(
				endDayData, storgeRequest, 2);
		List<StorgeDirInfo> result = getResult(endDayData, startDayMap,
				storgeRequest.getOrderColumnType(), 1);

		if (QueryEnum.DB.equals(storgeRequest.getStorageType())
				|| QueryEnum.TABLE.equals(storgeRequest.getStorageType())) {
			setDbAndTableName(result);
		}
		return (Page<StorgeDirInfo>) result;
	}

	@Override
	public Page<StorgeDirInfo> subStorgeRank(StorgeRequest storgeRequest) {
		checkParam(storgeRequest,false);
		if (QueryEnum.PATH.equals(storgeRequest.getStorageType())) {
			checkPathParam(storgeRequest);
			storgeRequest.getTypes().add(DirFileType.UNKOWN.getIndex());
		}
		List<StorgeDirInfo> endDayData = (Page<StorgeDirInfo>) getEndDayData(
				storgeRequest, 2);

		List<StorgeDirInfo> result = getResult(endDayData,
				MapUtil.newHashMap(), storgeRequest.getOrderColumnType(), 1);
		if (QueryEnum.DB.equals(storgeRequest.getStorageType())
				|| QueryEnum.TABLE.equals(storgeRequest.getStorageType())) {
			setDbAndTableName(result);
		}
		return (Page<StorgeDirInfo>) result;
	}

	private List<StorgeDirInfo> getResult(List<StorgeDirInfo> endDayData,
			Map<String, List<StorgeDirInfo>> startDayMap, String orderColumn,
			Integer type) {
		if (CollUtil.isEmpty(endDayData)) {
			if (endDayData instanceof Page) {
				endDayData = new Page<StorgeDirInfo>();
			} else {
				endDayData = CollUtil.newArrayList();
			}
		}
		List<StorgeDirInfo> list = null;
		for (StorgeDirInfo data : endDayData) {
			switch (type) {
			case 0:
				list = startDayMap.get(data.getTenant());
				break;
			case 1:
				list = startDayMap.get(data.getPath());
				break;
			default:
				break;
			}
			System.out.println(data);
			data.setTotalFileSizeStr(getNetFileSizeDescription(data
					.getTotalFileSize()));
			if (CollUtil.isEmpty(list)) {
				data.setStartTotalFileSizeStr("0");
				data.setStartTotalFileSize(0L);
				setInc(orderColumn, data, null);
			} else {
				data.setStartTotalFileSizeStr(getNetFileSizeDescription(list
						.get(0).getTotalFileSize()));
				data.setStartTotalFileSize(list.get(0).getTotalFileSize());
				setInc(orderColumn, data, list.get(0));
			}
		}
		return endDayData;
	}

	private StorgeRequest checkParam(StorgeRequest storgeRequest,boolean isChangeColumn) {
		String datasourceKey = storgeRequest.getClusterId()
				+ CommonConstraint.phoenix;
		storgeRequest.setDatasourceKey(datasourceKey);
		setTypes(storgeRequest);
		SeaboxDateUtil.setDayInfo(storgeRequest);
		dynamicDataSourceConfig.changePhoenixDataSource(
				storgeRequest.getClusterId(), datasourceKey);
		if (StrUtil.isBlank(storgeRequest.getOrderColumnType())
				|| StrUtil.equalsAnyIgnoreCase(
						storgeRequest.getOrderColumnType(), "1")) {
			storgeRequest.setOrderColumnType("1");
			if (isChangeColumn) {
				storgeRequest.setOrderColumn("totalFileSize");
			} else {
				storgeRequest.setOrderColumn("total_file_size");
			}
		} else if (StrUtil.equalsAnyIgnoreCase(
				storgeRequest.getOrderColumnType(), "2")) {
			if (isChangeColumn) {
				storgeRequest.setOrderColumn("totalFileNum");
			} else {
				storgeRequest.setOrderColumn("total_file_num");
			}
		} else if (StrUtil.equalsAnyIgnoreCase(
				storgeRequest.getOrderColumnType(), "3")) {
			if (isChangeColumn) {
				storgeRequest.setOrderColumn("totalSmallFileNum");
			} else {
				storgeRequest.setOrderColumn("total_small_file_num");
			}

		}
		return storgeRequest;
	}

	private void setTypes(StorgeRequest storgeRequest) {
		if (Objects.nonNull(storgeRequest.getStorageType())) {
			switch (storgeRequest.getStorageType()) {
			case DB:
				if (StrUtil.isBlank(storgeRequest.getType())) {
					storgeRequest.setTypes(CollUtil.newArrayList(
							DirFileType.DATABASE_HIVE.getIndex(),
							DirFileType.DATABASE_EXTERNAL_HIVE.getIndex(),
							DirFileType.DATABASE_HBASE.getIndex()));
					break;
				}
				switch (storgeRequest.getType().toLowerCase()) {
				case "hive":
					storgeRequest.setTypes(CollUtil.newArrayList(
							DirFileType.DATABASE_HIVE.getIndex(),
							DirFileType.TABLE_EXTERNAL_HIVE.getIndex()));
					storgeRequest.setNames(StrUtil.splitTrim(
							storgeRequest.getDbName(), ","));
					break;
				case "hbase":
					storgeRequest
							.setTypes(CollUtil
									.newArrayList(DirFileType.DATABASE_HBASE
											.getIndex()));
					storgeRequest.setNames(StrUtil.splitTrim(
							storgeRequest.getDbName(), ","));
					break;
				default:
					throw new BusinessException("未匹配到正确的参数");
				}
				break;
			case TABLE:
				if (StrUtil.isBlank(storgeRequest.getType())) {
					storgeRequest.setTypes(CollUtil.newArrayList(
							DirFileType.TABLE_HIVE.getIndex(),
							DirFileType.TABLE_EXTERNAL_HIVE.getIndex(),
							DirFileType.TABLE_HBASE.getIndex()));
					break;
				}
				switch (storgeRequest.getType().toLowerCase()) {
				case "hive":
					storgeRequest.setTypes(CollUtil.newArrayList(
							DirFileType.TABLE_HIVE.getIndex(),
							DirFileType.TABLE_EXTERNAL_HIVE.getIndex()));
					if (StrUtil.isBlank(storgeRequest.getTable())) {
						storgeRequest.setNames(null);
					} else {
						storgeRequest.setNames(StrUtil.splitTrim(
								storgeRequest.getTable(), ","));
					}
					break;
				case "hbase":
					storgeRequest.setTypes(CollUtil
							.newArrayList(DirFileType.TABLE_HBASE.getIndex()));
					if (StrUtil.isBlank(storgeRequest.getTable())) {
						storgeRequest.setNames(null);
					} else {
						storgeRequest.setNames(StrUtil.splitTrim(
								storgeRequest.getTable(), ","));
					}
					break;
				default:
					throw new BusinessException("未匹配到正确的参数");
				}
				break;
			case PATH:
				storgeRequest.setTypes(CollUtil.newArrayList(
						DirFileType.DATABASE_HBASE.getIndex(),
						DirFileType.DATABASE_HIVE.getIndex(),
						DirFileType.DATABASE_EXTERNAL_HIVE.getIndex(),
						DirFileType.PROJECT.getIndex()));
				break;
			default:
				throw new BusinessException("未匹配到正确的参数");
			}
			return;
		}

		storgeRequest.setTypes(CollUtil.newArrayList(
				DirFileType.DATABASE_HBASE.getIndex(),
				DirFileType.DATABASE_HIVE.getIndex(),
				DirFileType.DATABASE_EXTERNAL_HIVE.getIndex(),
				DirFileType.PROJECT.getIndex()));
	}

	private void setInc(String type, StorgeDirInfo first, StorgeDirInfo second) {
		BigDecimal incResult = null;
		Long inc = null;

		switch (type) {
		case "1":
			Long firstFileSize = first.getTotalFileSize();
			Long secondFileSize = Objects.nonNull(second) ? second
					.getTotalFileSize() : 0L;
			inc = firstFileSize - secondFileSize;
			first.setInc(getNetFileSizeDescription(inc));
			if (inc == 0L) {
				first.setIncRate("0");
			} else {
				first.setIncRate(NumberUtil.div(
						new BigDecimal(inc),
						firstFileSize != 0L ? new BigDecimal(firstFileSize)
								: new BigDecimal(1L), 4, RoundingMode.HALF_UP)
						.toString());
			}
			break;
		case "2":
			Long firstFileNum = first.getTotalFileNum();
			Long secondFileNum = Objects.nonNull(second) ? second
					.getTotalFileNum() : 0L;
			inc = firstFileNum - secondFileNum;
			incResult = new BigDecimal(inc).setScale(0, RoundingMode.HALF_UP);
			first.setInc(incResult.toString());
			first.setIncRate(NumberUtil.div(
					new BigDecimal(inc),
					firstFileNum != 0L ? new BigDecimal(firstFileNum)
							: new BigDecimal(1L), 4, RoundingMode.HALF_UP)
					.toString());
			break;
		case "3":
			Long firstSmallFileNum = first.getTotalSmallFileNum();
			Long secondSmallFileNum = Objects.nonNull(second) ? second
					.getTotalSmallFileNum() : 0L;
			inc = firstSmallFileNum - secondSmallFileNum;
			incResult = new BigDecimal(inc).setScale(0, RoundingMode.HALF_UP);
			first.setInc(incResult.toString());
			first.setIncRate(NumberUtil.div(
					new BigDecimal(inc),
					firstSmallFileNum != 0L ? new BigDecimal(firstSmallFileNum)
							: new BigDecimal(1L), 4, RoundingMode.HALF_UP)
					.toString());
			break;
		default:
			break;
		}
	}

	private List<StorgeDirInfo> getEndDayData(StorgeRequest storgeRequest,
			Integer type) {
		if (Objects.nonNull(storgeRequest.getPage())
				&& Objects.nonNull(storgeRequest.getSize())) {
			PageHelper.startPage(storgeRequest.getPage(),
					storgeRequest.getSize());
		}
		storgeRequest.setIsStartDay(false);
		switch (type) {
		case 0:
			return seaBoxStatMapper.getItemStorage(storgeRequest,
					storgeRequest.getDatasourceKey());
		case 1:
			return seaBoxStatMapper.getFileStorageByTenant(storgeRequest,
					storgeRequest.getDatasourceKey());
		case 2:
			return seaBoxStatMapper.selectSubPathTrend(storgeRequest,
					storgeRequest.getDatasourceKey());
		default:
			throw new BusinessException("为匹配到正确的模式");
		}

	}

	private Map<String, List<StorgeDirInfo>> getStartDayData(
			List<StorgeDirInfo> endDayData, StorgeRequest storgeRequest,
			Integer type) {
		Map<String, List<StorgeDirInfo>> startDayMap = null;
		storgeRequest.setPath(null);
		storgeRequest.setPathDepth(null);
		storgeRequest.setIsStartDay(true);
		storgeRequest.setNames(null);
		storgeRequest.setTypes(null);
		if (CollUtil.isEmpty(endDayData)) {
			startDayMap = MapUtil.newHashMap();
		} else {
			List<String> param = null;
			List<StorgeDirInfo> startDayData = null;
			switch (type) {
			case 0:
				param = endDayData.stream().map(StorgeDirInfo::getTenant)
						.collect(Collectors.toList());
				storgeRequest.setTenants(param);
				startDayData = seaBoxStatMapper.getItemStorage(storgeRequest,
						storgeRequest.getDatasourceKey());
				break;
			case 1:
				param = endDayData.stream().map(StorgeDirInfo::getPath)
						.collect(Collectors.toList());
				storgeRequest.setPaths(param);

				startDayData = seaBoxStatMapper.getFileStorageByTenant(
						storgeRequest, storgeRequest.getDatasourceKey());
				break;
			case 2:
				param = endDayData.stream().map(StorgeDirInfo::getPath)
						.collect(Collectors.toList());
				storgeRequest.setPaths(param);
				startDayData = seaBoxStatMapper.selectSubPathTrend(
						storgeRequest, storgeRequest.getDatasourceKey());
				break;
			default:
				break;
			}

			if (CollUtil.isEmpty(startDayData)) {
				startDayMap = MapUtil.newHashMap();
			} else {
				switch (type) {
				case 0:
					startDayMap = startDayData.stream().collect(
							Collectors.groupingBy(StorgeDirInfo::getTenant));
					break;
				case 1:
					startDayMap = startDayData.stream().collect(
							Collectors.groupingBy(StorgeDirInfo::getPath));
					break;
				case 2:
					startDayMap = startDayData.stream().collect(
							Collectors.groupingBy(StorgeDirInfo::getPath));
					break;
				default:
					break;
				}

			}
		}

		return startDayMap;
	}

	public static String getNetFileSizeDescription(long size) {
		StringBuffer bytes = new StringBuffer();
		DecimalFormat format = new DecimalFormat("###.0");
		if (size >= 1024 * 1024 * 1024) {
			double i = (size / (1024.0 * 1024.0 * 1024.0));
			bytes.append(format.format(i)).append("GB");
		} else if (size >= 1024 * 1024) {
			double i = (size / (1024.0 * 1024.0));
			bytes.append(format.format(i)).append("MB");
		} else if (size >= 1024) {
			double i = (size / (1024.0));
			bytes.append(format.format(i)).append("KB");
		} else if (size < 1024) {
			if (size <= 0) {
				bytes.append("0B");
			} else {
				bytes.append((int) size).append("B");
			}
		}
		return bytes.toString();
	}

	private void setDbAndTableName(List<StorgeDirInfo> result) {
		List<Integer> hive = CollUtil.newArrayList(101, 102, 201, 202);
		List<Integer> hbase = CollUtil.newArrayList(103, 203);
		result.forEach(data -> {
			if (hive.contains(data.getType())) {
				data.setDbType("hive");
			}
			if (hbase.contains(data.getType())) {
				data.setDbType("hbase");
			}
			if (StrUtil.isNotBlank(data.getTypeValue())) {
				List<String> names = StrUtil.splitTrim(data.getTypeValue(), ".");
				data.setDbName(names.get(0));
				if (names.size() == 2) {
					data.setTableName(names.get(1));
				}
			}
		});
	}

	private void checkPathParam(StorgeRequest storgeRequest) {
		if (StrUtil.isBlank(storgeRequest.getPath())) {
			storgeRequest.setPath("/");
		}
		if (Objects.isNull(storgeRequest.getPathDepth())) {
			storgeRequest.setPathDepth(StrUtil.splitTrim(
					storgeRequest.getPath(), "/").size());
		}
	}

	public static void main(String[] args) {
		System.out.println(StrUtil.splitTrim("/", "/").size() + 1);
	}
}
