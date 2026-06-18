package com.wool.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.dto.AuditDTO;
import com.wool.dto.WoolInfoDTO;
import com.wool.vo.ImportResultVO;
import com.wool.vo.WoolInfoVO;
import org.springframework.web.multipart.MultipartFile;

public interface WoolInfoService {

    /**
     * 分页查询已上线信息(公开)
     */
    Page<WoolInfoVO> listOnline(int pageNum, int pageSize, String keyword);

    /**
     * 查看详情(需登录, 校验状态)
     */
    WoolInfoVO getDetail(Long id, Long currentUserId, Integer currentUserRole);

    /**
     * 发布羊毛信息
     */
    Long publish(WoolInfoDTO dto, Long userId);

    /**
     * 修改自己的信息
     */
    void update(Long id, WoolInfoDTO dto, Long userId);

    /**
     * 删除自己的信息
     */
    void delete(Long id, Long userId);

    /**
     * 管理员审核
     */
    void audit(Long id, AuditDTO dto, Long adminId);

    /**
     * 管理员上线/下线
     */
    void toggleOnline(Long id, boolean online, Long adminId);

    /**
     * 管理员删除任意信息
     */
    void adminDelete(Long id, Long adminId);

    /**
     * 管理员分页查询(所有状态)
     */
    Page<WoolInfoVO> adminList(int pageNum, int pageSize, Integer status, String keyword);

    /**
     * 查询我的信息
     */
    Page<WoolInfoVO> myList(Long userId, int pageNum, int pageSize);

    /**
     * 批量导入薅羊毛信息
     *
     * @param file   Excel文件(.xlsx)
     * @param userId 当前用户ID
     * @return 导入结果(成功数/失败数/失败详情)
     */
    ImportResultVO batchImport(MultipartFile file, Long userId);
}
