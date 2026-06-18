package com.wool.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.common.*;
import com.wool.dto.AuditDTO;
import com.wool.dto.WoolInfoDTO;
import com.wool.dto.WoolInfoImportDTO;
import com.wool.entity.User;
import com.wool.entity.WoolInfo;
import com.wool.mapper.UserMapper;
import com.wool.mapper.WoolInfoMapper;
import com.wool.service.PointsService;
import com.wool.service.WoolInfoService;
import com.wool.vo.ImportResultVO;
import com.wool.vo.WoolInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WoolInfoServiceImpl implements WoolInfoService {

    private static final Logger log = LoggerFactory.getLogger(WoolInfoServiceImpl.class);

    private final WoolInfoMapper woolInfoMapper;
    private final UserMapper userMapper;
    private final PointsService pointsService;

    public WoolInfoServiceImpl(WoolInfoMapper woolInfoMapper, UserMapper userMapper, PointsService pointsService) {
        this.woolInfoMapper = woolInfoMapper;
        this.userMapper = userMapper;
        this.pointsService = pointsService;
    }

    @Override
    public Page<WoolInfoVO> listOnline(int pageNum, int pageSize, String keyword) {
        Page<WoolInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WoolInfo> wrapper = new LambdaQueryWrapper<WoolInfo>()
                .eq(WoolInfo::getStatus, WoolStatus.ONLINE.code)
                .like(StringUtils.hasText(keyword), WoolInfo::getTitle, keyword)
                .orderByDesc(WoolInfo::getCreatedAt);

        Page<WoolInfo> result = woolInfoMapper.selectPage(page, wrapper);
        return convertPage(result);
    }

    @Override
    public WoolInfoVO getDetail(Long id, Long currentUserId, Integer currentUserRole) {
        WoolInfo info = woolInfoMapper.selectById(id);
        if (info == null) {
            throw new BizException("信息不存在");
        }

        if (currentUserId == null) {
            throw new BizException(401, "请先登录");
        }

        boolean isOwner = info.getUserId().equals(currentUserId);
        boolean isAdmin = currentUserRole != null && currentUserRole == Constants.ROLE_ADMIN;

        if (info.getStatus() == WoolStatus.ONLINE.code || isOwner || isAdmin) {
            UpdateWrapper<WoolInfo> uw = new UpdateWrapper<>();
            uw.eq("id", id).setSql("view_count = view_count + 1");
            woolInfoMapper.update(null, uw);

            WoolInfoVO vo = toVO(info);
            User author = userMapper.selectById(info.getUserId());
            if (author != null) {
                vo.setAuthorName(author.getNickname());
            }
            return vo;
        }

        throw new BizException("无权查看该信息");
    }

    @Override
    @Transactional
    public Long publish(WoolInfoDTO dto, Long userId) {
        WoolInfo info = new WoolInfo();
        BeanUtils.copyProperties(dto, info);
        info.setUserId(userId);
        info.setStatus(WoolStatus.PENDING.code);
        info.setViewCount(0);
        woolInfoMapper.insert(info);
        log.info("用户[{}]发布羊毛信息[id={}]，待审核", userId, info.getId());
        return info.getId();
    }

    @Override
    @Transactional
    public void update(Long id, WoolInfoDTO dto, Long userId) {
        WoolInfo info = woolInfoMapper.selectById(id);
        if (info == null) {
            throw new BizException("信息不存在");
        }
        if (!info.getUserId().equals(userId)) {
            throw new BizException("只能修改自己发布的信息");
        }

        BeanUtils.copyProperties(dto, info);
        info.setStatus(WoolStatus.PENDING.code);
        info.setRejectReason("");
        woolInfoMapper.updateById(info);
        log.info("用户[{}]修改羊毛信息[id={}]，重置为待审核", userId, id);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        WoolInfo info = woolInfoMapper.selectById(id);
        if (info == null) {
            throw new BizException("信息不存在");
        }
        if (!info.getUserId().equals(userId)) {
            throw new BizException("只能删除自己发布的信息");
        }
        woolInfoMapper.deleteById(id);
        log.info("用户[{}]删除羊毛信息[id={}]", userId, id);
    }

    @Override
    @Transactional
    public void audit(Long id, AuditDTO dto, Long adminId) {
        WoolInfo info = woolInfoMapper.selectById(id);
        if (info == null) {
            throw new BizException("信息不存在");
        }
        if (info.getStatus() != WoolStatus.PENDING.code) {
            throw new BizException("只能审核待审核状态的信息");
        }

        if (dto.getAction() == 1) {
            info.setStatus(WoolStatus.ONLINE.code);
            info.setRejectReason("");
            woolInfoMapper.updateById(info);

            pointsService.addPoints(info.getUserId(), 1, PointsChangeType.PUBLISH_REWARD.code,
                    "信息审核通过奖励", info.getId());
            log.info("管理员[{}]审核通过信息[id={}]，发布者[{}]获得1积分", adminId, id, info.getUserId());
        } else if (dto.getAction() == 2) {
            info.setStatus(WoolStatus.REJECTED.code);
            info.setRejectReason(dto.getRejectReason() != null ? dto.getRejectReason() : "");
            woolInfoMapper.updateById(info);
            log.info("管理员[{}]驳回信息[id={}]，理由: {}", adminId, id, info.getRejectReason());
        } else {
            throw new BizException("无效的审核操作");
        }
    }

    @Override
    @Transactional
    public void toggleOnline(Long id, boolean online, Long adminId) {
        WoolInfo info = woolInfoMapper.selectById(id);
        if (info == null) {
            throw new BizException("信息不存在");
        }

        if (online) {
            if (info.getStatus() != WoolStatus.OFFLINE.code && info.getStatus() != WoolStatus.REJECTED.code) {
                throw new BizException("当前状态不可上线");
            }
            info.setStatus(WoolStatus.ONLINE.code);
        } else {
            if (info.getStatus() != WoolStatus.ONLINE.code) {
                throw new BizException("当前状态不可下线");
            }
            info.setStatus(WoolStatus.OFFLINE.code);
        }

        info.setRejectReason("");
        woolInfoMapper.updateById(info);
        log.info("管理员[{}]{}信息[id={}]", adminId, online ? "上线" : "下线", id);
    }

    @Override
    @Transactional
    public void adminDelete(Long id, Long adminId) {
        WoolInfo info = woolInfoMapper.selectById(id);
        if (info == null) {
            throw new BizException("信息不存在");
        }
        woolInfoMapper.deleteById(id);
        log.info("管理员[{}]删除信息[id={}]", adminId, id);
    }

    @Override
    public Page<WoolInfoVO> adminList(int pageNum, int pageSize, Integer status, String keyword) {
        Page<WoolInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WoolInfo> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(WoolInfo::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(WoolInfo::getTitle, keyword);
        }
        wrapper.orderByDesc(WoolInfo::getCreatedAt);

        Page<WoolInfo> result = woolInfoMapper.selectPage(page, wrapper);
        return convertPage(result);
    }

    @Override
    public Page<WoolInfoVO> myList(Long userId, int pageNum, int pageSize) {
        Page<WoolInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WoolInfo> wrapper = new LambdaQueryWrapper<WoolInfo>()
                .eq(WoolInfo::getUserId, userId)
                .orderByDesc(WoolInfo::getCreatedAt);

        Page<WoolInfo> result = woolInfoMapper.selectPage(page, wrapper);
        return convertPage(result);
    }

    @Override
    @Transactional
    public ImportResultVO batchImport(MultipartFile file, Long userId) {
        ImportResultVO result = new ImportResultVO();
        List<WoolInfoImportDTO> dataList = new ArrayList<>();

        // 1. 读取Excel文件
        try {
            EasyExcel.read(file.getInputStream(), WoolInfoImportDTO.class, new ReadListener<WoolInfoImportDTO>() {
                @Override
                public void invoke(WoolInfoImportDTO data, AnalysisContext context) {
                    dataList.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 读取完成
                }
            }).sheet().doRead();
        } catch (Exception e) {
            log.error("Excel文件读取失败", e);
            throw new BizException("Excel文件读取失败，请检查文件格式");
        }

        if (dataList.isEmpty()) {
            throw new BizException("Excel文件中没有数据");
        }

        // 2. 逐行校验并插入
        for (int i = 0; i < dataList.size(); i++) {
            WoolInfoImportDTO dto = dataList.get(i);
            int rowNum = i + 2; // Excel行号(第1行是表头)

            // 校验必填字段
            if (!StringUtils.hasText(dto.getTitle())) {
                result.addFail("第" + rowNum + "行: 标题不能为空");
                continue;
            }
            if (dto.getTitle().length() > 128) {
                result.addFail("第" + rowNum + "行: 标题长度不能超过128个字符");
                continue;
            }
            if (!StringUtils.hasText(dto.getContent())) {
                result.addFail("第" + rowNum + "行: 内容不能为空");
                continue;
            }

            // 构建实体并插入
            WoolInfo info = new WoolInfo();
            info.setUserId(userId);
            info.setTitle(dto.getTitle().trim());
            info.setContent(dto.getContent().trim());
            info.setCategory(dto.getCategory() != null ? dto.getCategory().trim() : "");
            info.setSourceUrl(dto.getSourceUrl() != null ? dto.getSourceUrl().trim() : "");
            info.setClaimSteps(dto.getClaimSteps() != null ? dto.getClaimSteps().trim() : "");
            info.setStatus(WoolStatus.PENDING.code);
            info.setViewCount(0);

            woolInfoMapper.insert(info);
            result.addSuccess();
            log.info("批量导入: 用户[{}]导入信息[id={}]，标题={}", userId, info.getId(), info.getTitle());
        }

        log.info("批量导入完成: 成功{}条, 失败{}条", result.getSuccessCount(), result.getFailCount());
        return result;
    }

    // ---------- 辅助方法 ----------

    private Page<WoolInfoVO> convertPage(Page<WoolInfo> page) {
        Map<Long, String> authorMap = page.getRecords().stream()
                .map(WoolInfo::getUserId)
                .distinct()
                .map(uid -> userMapper.selectById(uid))
                .filter(u -> u != null)
                .collect(Collectors.toMap(User::getId, User::getNickname));

        Page<WoolInfoVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(info -> {
            WoolInfoVO vo = toVO(info);
            vo.setAuthorName(authorMap.getOrDefault(info.getUserId(), "匿名"));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    private WoolInfoVO toVO(WoolInfo info) {
        WoolInfoVO vo = new WoolInfoVO();
        BeanUtils.copyProperties(info, vo);
        WoolStatus ws = WoolStatus.of(info.getStatus());
        vo.setStatusDesc(ws != null ? ws.desc : "未知");
        return vo;
    }
}
