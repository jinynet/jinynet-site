package cn.jinynet.site.service;

import cn.jinynet.site.entity.FileUploadTask;
import cn.jinynet.site.entity.FileUploadTaskTable;
import cn.jinynet.site.types.enums.FileUploadTaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 文件上传任务服务
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadTaskService {
    private final JSqlClient sqlClient;

    /**
     * 获取上传任务
     *
     * @param uploadId 上传任务ID
     * @return 上传任务
     */
    public FileUploadTask getTask(String uploadId) {
        return sqlClient.createQuery(FileUploadTaskTable.$)
                .where(FileUploadTaskTable.$.uploadId().eq(uploadId))
                .select(FileUploadTaskTable.$)
                .fetchOptional()
                .orElseThrow( () -> new RuntimeException("上传任务不存在或已过期"));
    }

    /**
     * 删除上传任务记录
     *
     * @param uploadId 上传任务ID
     */
    public void deleteTask(String uploadId, FileUploadTaskStatus...  status) {
        sqlClient.createDelete(FileUploadTaskTable.$)
                .where(FileUploadTaskTable.$.uploadId().eq(uploadId))
                .where(FileUploadTaskTable.$.status().inIf(Arrays.asList(status)))

                .execute();
        log.info("已删除上传任务记录, uploadId={}", uploadId);
    }
}
