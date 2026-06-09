package cn.jinynet.site.api.home;

import cn.dev33.satoken.stp.StpUtil;
import cn.jinynet.site.entity.dto.PublicUserWorkDetail;
import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.site.entity.*;
import cn.jinynet.site.entity.dto.UserInfoDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开用户信息接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class PublicUserApi {
    private final JSqlClient sqlClient;
    /**
     * 获取用户基本信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfo> getUserInfo() {
        UserInfo userInfo = sqlClient.createQuery(UserInfoTable.$)
                .select(UserInfoTable.$.fetch(UserInfoDetail.METADATA.getFetcher()))
                .fetchOptional()
                .orElse(null);
        
        // 动态设置在线状态（瞬态字段，不存储到数据库）
        if (userInfo != null) {
            // 使用Jimmer的动态属性API设置瞬态字段
            userInfo = UserInfoDraft.$.produce(userInfo, draft -> {
                draft.setOnline(StpUtil.isLogin(1L));
            });
        }
        
        return Result.success(userInfo);
    }

    /**
     * 获取技能列表
     *
     * @return 技能列表
     */
    @GetMapping("/skills")
    public Result<List<UserSkills>> getSkills() {
        List<UserSkills> skills = sqlClient.createQuery(UserSkillsTable.$)
                .orderBy(UserSkillsTable.$.sortOrder().asc())
                .select(UserSkillsTable.$)
                .execute();
        return Result.success(skills);
    }

    /**
     * 获取联系方式列表
     *
     * @return 联系方式列表
     */
    @GetMapping("/contacts")
    public Result<List<UserContact>> getContacts() {
        List<UserContact> contacts = sqlClient.createQuery(UserContactTable.$)
                .orderBy(UserContactTable.$.sortOrder().asc())
                .select(UserContactTable.$)
                .execute();
        return Result.success(contacts);
    }

    /**
     * 获取教育经历列表
     *
     * @return 教育经历列表
     */
    @GetMapping("/educations")
    public Result<List<UserEducation>> getEducations() {
        List<UserEducation> educations = sqlClient.createQuery(UserEducationTable.$)
                .orderBy(UserEducationTable.$.sortOrder().asc())
                .select(UserEducationTable.$)
                .execute();
        return Result.success(educations);
    }

    /**
     * 获取工作经验列表
     *
     * @return 工作经验列表
     */
    @GetMapping("/works")
    public Result<List<UserWork>> getWorks() {
        List<UserWork> works = sqlClient.createQuery(UserWorkTable.$)
                .orderBy(UserWorkTable.$.sortOrder().asc())
                .select(UserWorkTable.$.fetch(PublicUserWorkDetail.METADATA.getFetcher()))
                .execute();
        return Result.success(works);
    }
}
