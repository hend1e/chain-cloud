package com.itheima.chaincloud.api.user;

import com.itheima.chaincloud.common.util.PageResponse;
import com.itheima.chaincloud.domain.user.domain.MemberDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@FeignClient(name = "chain-cloud-ms-user", path = "/member", contextId = "Member")
@ApiIgnore
public interface MemberFeign {
    /**
     * 分页查询
     *
     * @param page     页码
     * @param pageSize 页大小
     * @return 会员分页数据
     */
    @GetMapping("page")
    PageResponse<MemberDTO> page(@RequestParam("page") Integer page, @RequestParam("pageSize") Integer pageSize);

    /**
     * 新增
     *
     * @param entity 会员信息
     */
    @PostMapping
    void save(@RequestBody MemberDTO entity);

    /**
     * 修改
     *
     * @param id     会员ID
     * @param entity 会员信息
     */
    @PutMapping("/{id}")
    void update(@PathVariable(name = "id") Long id, @RequestBody MemberDTO entity);

    /**
     * 删除
     *
     * @param id 会员ID
     */
    @DeleteMapping("/{id}")
    void del(@PathVariable(name = "id") Long id);

    /**
     * 根据ID查询详情
     *
     * @param id 会员ID
     * @return 会员详情
     */
    @GetMapping("detail/{id}")
    MemberDTO detail(@PathVariable(name = "id") Long id);

    /**
     * 根据openId查询详情
     *
     * @param openId 微信openId
     * @return 会员详情
     */
    @GetMapping("openId/{openId}")
    MemberDTO detailByOpenId(@PathVariable(name = "openId") String openId);
}
