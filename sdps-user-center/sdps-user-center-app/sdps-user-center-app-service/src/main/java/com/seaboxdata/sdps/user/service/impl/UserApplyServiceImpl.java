package com.seaboxdata.sdps.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.user.api.IUserApplyService;
import com.seaboxdata.sdps.user.api.IUserService;
import com.seaboxdata.sdps.user.mybatis.dto.user.UserApplyDto;
import com.seaboxdata.sdps.user.mybatis.mapper.UserApplyMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.UserApprovalMapper;
import com.seaboxdata.sdps.user.mybatis.model.UserApply;
import com.seaboxdata.sdps.user.mybatis.model.UserApproval;
import com.seaboxdata.sdps.user.vo.user.ApplyVo;
import com.seaboxdata.sdps.user.vo.user.PageApplyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class UserApplyServiceImpl extends SuperServiceImpl<UserApplyMapper, UserApply> implements IUserApplyService {

    @Autowired
    public IUserService userService;

    @Autowired
    public UserApplyMapper userApplyMapper;

    @Autowired
    public UserApprovalMapper userApprovalMapper;


    /**
     * 应用申请
     * @param userApply
     */
    @Override
    @Transactional
    public void addUserApplyInfo(UserApply userApply) {

        // 设置申请用户id
        userApply.setSqUserId(userService.selectByUsername(userApply.getSqUserName()).getId());

        /*Date currentDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String dateStr = sdf.format(new Date());
        try {
            currentDate = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        // 申请时间
        userApply.setApplyTime(new Date());

        // 申请次数如何增1
        userApply.setApplyNum(1l);

        // 审批人
        userApply.setSpUserName("admin");


        this.save(userApply);

        UserApproval userApproval = new UserApproval();

        userApproval.setSqId(userApply.getId());
        userApproval.setSpUserId(userService.selectByUsername("admin").getId());
        userApproval.setSpUserName("admin");
        userApproval.setSqUserId(userApply.getSqUserId());
        userApproval.setSqUserName(userApply.getSqUserName());
        userApproval.setApplyContent(userApply.getApplyContent());
        userApproval.setApplyReason(userApply.getApplyReason());
        userApproval.setApplyTime(userApply.getApplyTime());
        userApproval.setSpStatus(0);
        userApproval.setSpTime(new Date());
        userApproval.setAction(0);
        userApproval.setSpReason(null);

        userApprovalMapper.insert(userApproval);

    }

    /**
     * 审批管理-我的申请(查询当前用户的申请表单信息)
     * @param userName
     * @return
     */
    @Override
    public List<UserApplyDto> selectUserApplyInfo(String userName) {
        List<UserApplyDto> userApplyDtos = userApplyMapper.selectInfoByUser(userName);

        return userApplyDtos;
    }

    /**
     * 审批管理-我的申请(筛选功能（null 未选择，0全部，1待办，2已完成）)
     * 申请内容（模糊查询）
     * 状态（null 未选择，0待审批，1已通过，2已驳回）
     */
    @Override
    public List<UserApplyDto> selectUserApplyInfoByWhere(String userName, String applyContent, Integer spStatus ) {

        List<UserApplyDto> userApplyDtos = userApplyMapper.selectInfoByWhere(userName, applyContent, spStatus);


        return userApplyDtos;
    }

    /**
     * 审批管理-我的申请(筛选功能（null 未选择，0全部，1待办，2已完成）)
     * 申请内容（模糊查询）
     * 状态（null 未选择，0待审批，1已通过，2已驳回）
     * 分页查询
     */
    @Override
    public PageResult<UserApplyDto> selectUserApplyInfoPage(PageApplyRequest pageApplyRequest) {

        int pageSize = pageApplyRequest.getPageSize();
        int pageNum = pageApplyRequest.getPageNum();
        PageHelper.startPage(pageNum, pageSize);
        Page<UserApplyDto> userApplyDtos = userApplyMapper.selectInfoPage(pageApplyRequest.getUserName(),
                pageApplyRequest.getApplyContent(),pageApplyRequest.getSpStatus());

        return PageResult.<UserApplyDto> builder().code(0).data(userApplyDtos.getResult())
                .count(userApplyDtos.getTotal()).msg("查询成功").build();
    }





    /**
     * 更新申请表单信息（再次申请）
     */
    @Override
    @Transactional
    public void updateUserApplyInfo(ApplyVo applyVo) {
        /*String userName = userApply.getSqUserName();
        String applyContent = userApply.getApplyContent();
        String applyReason = userApply.getApplyReason();

        // 再次申请时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String applyTime = sdf.format(new Date());

        // Date applyTime = new Date();
        userApplyMapper.updateApplyInfo(userName, applyContent, applyReason, applyTime);
        userApprovalMapper.updateApprovalInfo(userName, applyContent, applyReason, applyTime);*/


        QueryWrapper<UserApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", applyVo.getSqId());
        UserApply userApply = userApplyMapper.selectOne(queryWrapper);

        userApply.setApplyReason(applyVo.getApplyReason());
        userApply.setApplyNum(userApply.getApplyNum()+1l);
        userApply.setApplyTime(new Date());

        UpdateWrapper<UserApply> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", applyVo.getSqId());
        this.update(userApply,updateWrapper);





        QueryWrapper<UserApproval> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("sq_id", applyVo.getSqId());
        UserApproval userApproval = userApprovalMapper.selectOne(queryWrapper2);

        userApproval.setApplyReason(applyVo.getApplyReason());
        userApproval.setApplyTime(userApply.getApplyTime());
        userApproval.setSpStatus(0);
        userApproval.setSpTime(null);
        userApproval.setAction(0);
        userApproval.setSpReason(null);

        UpdateWrapper<UserApproval> updateWrapper2 = new UpdateWrapper<>();
        updateWrapper2.eq("sq_id", applyVo.getSqId());
        userApprovalMapper.update(userApproval,updateWrapper2);

    }

    @Override
    public UserApply selectBySpStatus(String spStatus) {
        return null;
    }
}
