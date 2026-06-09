package cn.jinynet.site.api.admin;

import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.site.service.UserInfoService;
import cn.jinynet.site.entity.UserContact;
import cn.jinynet.site.entity.UserEducation;
import cn.jinynet.site.entity.UserInfo;
import cn.jinynet.site.entity.UserSkills;
import cn.jinynet.site.entity.UserWork;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 个人信息管理接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserInfoApi {

    private final UserInfoService userInfoService;

    /**
     * 获取用户基本信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfo> getUserInfo() {
        return Result.success(userInfoService.getUserInfo());
    }

    /**
     * 更新用户基本信息
     *
     * @param userInfo 用户信息
     * @return 更新后的用户信息
     */
    @PutMapping("/info")
    public Result<UserInfo> updateUserInfo(@RequestBody UserInfo userInfo) {
        return Result.success(userInfoService.updateUserInfo(userInfo));
    }

    /**
     * 获取技能列表
     *
     * @return 技能列表
     */
    @GetMapping("/skills")
    public Result<List<UserSkills>> getSkills() {
        return Result.success(userInfoService.getSkills());
    }

    /**
     * 创建技能
     *
     * @param skill 技能实体
     * @return 创建的技能
     */
    @PostMapping("/skills")
    public Result<UserSkills> createSkill(@RequestBody UserSkills skill) {
        return Result.success(userInfoService.createSkill(skill));
    }

    /**
     * 更新技能
     *
     * @param skill 技能实体
     * @return 更新后的技能
     */
    @PutMapping("/skills")
    public Result<UserSkills> updateSkill(@RequestBody UserSkills skill) {
        return Result.success(userInfoService.updateSkill(skill));
    }

    /**
     * 删除技能
     *
     * @param id 技能ID
     * @return 是否删除成功
     */
    @DeleteMapping("/skills/{id}")
    public Result<Boolean> deleteSkill(@PathVariable long id) {
        return Result.success(userInfoService.deleteSkill(id));
    }

    /**
     * 获取联系方式列表
     *
     * @return 联系方式列表
     */
    @GetMapping("/contacts")
    public Result<List<UserContact>> getContacts() {
        return Result.success(userInfoService.getContacts());
    }

    /**
     * 创建联系方式
     *
     * @param contact 联系方式实体
     * @return 创建的联系方式
     */
    @PostMapping("/contacts")
    public Result<UserContact> createContact(@RequestBody UserContact contact) {
        return Result.success(userInfoService.createContact(contact));
    }

    /**
     * 更新联系方式
     *
     * @param contact 联系方式实体
     * @return 更新后的联系方式
     */
    @PutMapping("/contacts")
    public Result<UserContact> updateContact(@RequestBody UserContact contact) {
        return Result.success(userInfoService.updateContact(contact));
    }

    /**
     * 删除联系方式
     *
     * @param id 联系方式ID
     * @return 是否删除成功
     */
    @DeleteMapping("/contacts/{id}")
    public Result<Boolean> deleteContact(@PathVariable long id) {
        return Result.success(userInfoService.deleteContact(id));
    }

    /**
     * 获取教育经历列表
     *
     * @return 教育经历列表
     */
    @GetMapping("/educations")
    public Result<List<UserEducation>> getEducations() {
        return Result.success(userInfoService.getEducations());
    }

    /**
     * 创建教育经历
     *
     * @param education 教育经历实体
     * @return 创建的教育经历
     */
    @PostMapping("/educations")
    public Result<UserEducation> createEducation(@RequestBody UserEducation education) {
        return Result.success(userInfoService.createEducation(education));
    }

    /**
     * 更新教育经历
     *
     * @param education 教育经历实体
     * @return 更新后的教育经历
     */
    @PutMapping("/educations")
    public Result<UserEducation> updateEducation(@RequestBody UserEducation education) {
        return Result.success(userInfoService.updateEducation(education));
    }

    /**
     * 删除教育经历
     *
     * @param id 教育经历ID
     * @return 是否删除成功
     */
    @DeleteMapping("/educations/{id}")
    public Result<Boolean> deleteEducation(@PathVariable long id) {
        return Result.success(userInfoService.deleteEducation(id));
    }

    /**
     * 获取工作经验列表
     *
     * @return 工作经验列表
     */
    @GetMapping("/works")
    public Result<List<UserWork>> getWorkExperiences() {
        return Result.success(userInfoService.getWorkExperiences());
    }

    /**
     * 创建工作经验
     *
     * @param work 工作经验实体
     * @return 创建的工作经验
     */
    @PostMapping("/works")
    public Result<UserWork> createWorkExperience(@RequestBody UserWork work) {
        return Result.success(userInfoService.createWorkExperience(work));
    }

    /**
     * 更新工作经验
     *
     * @param work 工作经验实体
     * @return 更新后的工作经验
     */
    @PutMapping("/works")
    public Result<UserWork> updateWorkExperience(@RequestBody UserWork work) {
        return Result.success(userInfoService.updateWorkExperience(work));
    }

    /**
     * 删除工作经验
     *
     * @param id 工作经验ID
     * @return 是否删除成功
     */
    @DeleteMapping("/works/{id}")
    public Result<Boolean> deleteWorkExperience(@PathVariable long id) {
        return Result.success(userInfoService.deleteWorkExperience(id));
    }
}