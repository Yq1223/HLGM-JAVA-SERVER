package com.wool.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wool.common.Constants;
import com.wool.common.R;
import com.wool.dto.WoolInfoDTO;
import com.wool.service.WoolInfoService;
import com.wool.vo.ImportResultVO;
import com.wool.vo.WoolInfoVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/wool")
public class WoolInfoController {

    private final WoolInfoService woolInfoService;

    public WoolInfoController(WoolInfoService woolInfoService) {
        this.woolInfoService = woolInfoService;
    }

    /**
     * 获取已上线信息列表(公开，无需登录)
     * GET /api/wool/list?pageNum=1&pageSize=10&keyword=xxx
     */
    @GetMapping("/list")
    public R<Page<WoolInfoVO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<WoolInfoVO> page = woolInfoService.listOnline(pageNum, pageSize, keyword);
        return R.ok(page);
    }

    /**
     * 获取羊毛详情(需登录)
     * GET /api/wool/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public R<WoolInfoVO> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        Integer role = (Integer) request.getAttribute(Constants.ATTR_USER_ROLE);

        // 未登录时userId为null，getDetail会处理
        WoolInfoVO vo = woolInfoService.getDetail(id, userId, role);
        return R.ok(vo);
    }

    /**
     * 发布羊毛信息
     * POST /api/wool/publish
     */
    @PostMapping("/publish")
    public R<Long> publish(@Valid @RequestBody WoolInfoDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        Long id = woolInfoService.publish(dto, userId);
        return R.ok(id);
    }

    /**
     * 修改自己的信息
     * PUT /api/wool/update/{id}
     */
    @PutMapping("/update/{id}")
    public R<?> update(@PathVariable Long id, @Valid @RequestBody WoolInfoDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        woolInfoService.update(id, dto, userId);
        return R.ok();
    }

    /**
     * 删除自己的信息
     * DELETE /api/wool/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public R<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        woolInfoService.delete(id, userId);
        return R.ok();
    }

    /**
     * 查询我的信息
     * GET /api/wool/mine?pageNum=1&pageSize=10
     */
    @GetMapping("/mine")
    public R<Page<WoolInfoVO>> mine(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        Page<WoolInfoVO> page = woolInfoService.myList(userId, pageNum, pageSize);
        return R.ok(page);
    }

    /**
     * 批量导入薅羊毛信息
     * POST /api/wool/import
     * Content-Type: multipart/form-data
     */
    @PostMapping("/import")
    public R<ImportResultVO> importExcel(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.ATTR_USER_ID);
        ImportResultVO result = woolInfoService.batchImport(file, userId);
        return R.ok(result);
    }
}
