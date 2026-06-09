package cn.jinynet.site.service;

import cn.jinynet.site.entity.*;
import lombok.RequiredArgsConstructor;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.DeleteResult;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.ast.query.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户信息服务
 *
 * @author jinty
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final JSqlClient sqlClient;

    /**
     * 获取用户基本信息
     *
     * @return 用户信息
     */
    public UserInfo getUserInfo() {
        return sqlClient.createQuery(UserInfoTable.$)
                .select(UserInfoTable.$)
                .fetchOptional()
                .orElse(null);
    }

    /**
     * 更新用户基本信息
     *
     * @param userInfo 用户信息
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserInfo updateUserInfo(UserInfo userInfo) {
        UserInfo existing = getUserInfo();
        if (existing != null) {
            return sqlClient.saveCommand(UserInfoDraft.$.produce(userInfo, draft -> {
                draft.setId(existing.id());
            }))
                    .setMode(SaveMode.UPDATE_ONLY)
                    .execute()
                    .getModifiedEntity();
        } else {
            return sqlClient.saveCommand(userInfo)
                    .setMode(SaveMode.INSERT_ONLY)
                    .execute()
                    .getModifiedEntity();
        }
    }

    /**
     * 获取技能列表
     *
     * @return 技能列表
     */
    public List<UserSkills> getSkills() {
        List<Order> orders = Order.makeOrders(UserSkillsTable.$, "sortOrder asc, createdAt desc");
        return sqlClient.createQuery(UserSkillsTable.$)
                .orderBy(orders)
                .select(UserSkillsTable.$)
                .execute();
    }

    /**
     * 创建技能
     *
     * @param skill 技能实体
     * @return 创建的技能
     */
    @Transactional(rollbackFor = Exception.class)
    public UserSkills createSkill(UserSkills skill) {
        return sqlClient.saveCommand(skill)
                .setMode(SaveMode.INSERT_ONLY)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 更新技能
     *
     * @param skill 技能实体
     * @return 更新后的技能
     */
    @Transactional(rollbackFor = Exception.class)
    public UserSkills updateSkill(UserSkills skill) {
        return sqlClient.saveCommand(skill)
                .setMode(SaveMode.UPDATE_ONLY)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 删除技能
     *
     * @param id 技能ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSkill(long id) {
        DeleteResult deleteResult = sqlClient.deleteById(UserSkills.class, id);
        return deleteResult.getAffectedRowCount(UserSkills.class) > 0;
    }

    /**
     * 获取联系方式列表
     *
     * @return 联系方式列表
     */
    public List<UserContact> getContacts() {
        List<Order> orders = Order.makeOrders(UserContactTable.$, "sortOrder asc, createdAt desc");
        return sqlClient.createQuery(UserContactTable.$)
                .orderBy(orders)
                .select(UserContactTable.$)
                .execute();
    }

    /**
     * 创建联系方式
     *
     * @param contact 联系方式实体
     * @return 创建的联系方式
     */
    @Transactional(rollbackFor = Exception.class)
    public UserContact createContact(UserContact contact) {
        return sqlClient.saveCommand(contact)
                .setMode(SaveMode.INSERT_ONLY)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 更新联系方式
     *
     * @param contact 联系方式实体
     * @return 更新后的联系方式
     */
    @Transactional(rollbackFor = Exception.class)
    public UserContact updateContact(UserContact contact) {
        return sqlClient.saveCommand(contact)
                .setMode(SaveMode.UPDATE_ONLY)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 删除联系方式
     *
     * @param id 联系方式ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteContact(long id) {
        DeleteResult deleteResult = sqlClient.deleteById(UserContact.class, id);
        return deleteResult.getAffectedRowCount(UserContact.class) > 0;
    }

    /**
     * 获取教育经历列表
     *
     * @return 教育经历列表
     */
    public List<UserEducation> getEducations() {
        List<Order> orders = Order.makeOrders(UserEducationTable.$, "sortOrder asc, createdAt desc");
        return sqlClient.createQuery(UserEducationTable.$)
                .orderBy(orders)
                .select(UserEducationTable.$)
                .execute();
    }

    /**
     * 创建教育经历
     *
     * @param education 教育经历实体
     * @return 创建的教育经历
     */
    @Transactional(rollbackFor = Exception.class)
    public UserEducation createEducation(UserEducation education) {
        return sqlClient.saveCommand(education)
                .setMode(SaveMode.INSERT_ONLY)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 更新教育经历
     *
     * @param education 教育经历实体
     * @return 更新后的教育经历
     */
    @Transactional(rollbackFor = Exception.class)
    public UserEducation updateEducation(UserEducation education) {
        return sqlClient.saveCommand(education)
                .setMode(SaveMode.UPDATE_ONLY)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 删除教育经历
     *
     * @param id 教育经历ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEducation(long id) {
        DeleteResult deleteResult = sqlClient.deleteById(UserEducation.class, id);
        return deleteResult.getAffectedRowCount(UserEducation.class) > 0;
    }

    /**
     * 获取工作经验列表
     *
     * @return 工作经验列表
     */
    public List<UserWork> getWorkExperiences() {
        List<Order> orders = Order.makeOrders(UserWorkTable.$, "sortOrder asc, createdAt desc");
        return sqlClient.createQuery(UserWorkTable.$)
                .orderBy(orders)
                .select(UserWorkTable.$)
                .execute();
    }

    /**
     * 创建工作经验
     *
     * @param work 工作经验实体
     * @return 创建的工作经验
     */
    @Transactional(rollbackFor = Exception.class)
    public UserWork createWorkExperience(UserWork work) {
        return sqlClient.saveCommand(work)
                .setMode(SaveMode.INSERT_ONLY)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 更新工作经验
     *
     * @param work 工作经验实体
     * @return 更新后的工作经验
     */
    @Transactional(rollbackFor = Exception.class)
    public UserWork updateWorkExperience(UserWork work) {
        return sqlClient.saveCommand(work)
                .setMode(SaveMode.UPDATE_ONLY)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 删除工作经验
     *
     * @param id 工作经验ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWorkExperience(long id) {
        DeleteResult deleteResult = sqlClient.deleteById(UserWork.class, id);
        return deleteResult.getAffectedRowCount(UserWork.class) > 0;
    }
}