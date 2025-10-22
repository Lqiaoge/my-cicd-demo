package com.windcore.service;

import com.windcore.model.FileMetadata;
import java.util.List;

/**
 * 文件服务接口
 * 
 * @author windcore
 */
public interface FileService {
    
    /**
     * 根据MD5查找文件
     * 
     * @param md5 文件MD5值
     * @return 文件元数据，如果不存在返回null
     */
    FileMetadata findByMd5(String md5);
    
    /**
     * 根据ID查找文件
     * 
     * @param id 文件ID
     * @return 文件元数据，如果不存在返回null
     */
    FileMetadata findById(String id);
    
    /**
     * 保存文件元数据
     * 
     * @param fileMetadata 文件元数据
     * @return 保存后的文件元数据
     */
    FileMetadata save(FileMetadata fileMetadata);
    
    /**
     * 更新文件元数据
     * 
     * @param fileMetadata 文件元数据
     * @return 更新后的文件元数据
     */
    FileMetadata update(FileMetadata fileMetadata);
    
    /**
     * 删除文件
     * 
     * @param id 文件ID
     * @return 是否删除成功
     */
    boolean deleteById(String id);
    
    /**
     * 根据业务类型查找文件列表
     * 
     * @param businessType 业务类型
     * @return 文件列表
     */
    List<FileMetadata> findByBusinessType(String businessType);
    
    /**
     * 根据文件名模糊查询
     * 
     * @param fileName 文件名关键字
     * @return 文件列表
     */
    List<FileMetadata> findByFileNameLike(String fileName);
    
    /**
     * 检查文件是否存在
     * 
     * @param md5 文件MD5值
     * @return 是否存在
     */
    boolean existsByMd5(String md5);
    
    /**
     * 获取文件总数
     * 
     * @return 文件总数
     */
    long count();
    
    /**
     * 获取指定业务类型的文件总数
     * 
     * @param businessType 业务类型
     * @return 文件总数
     */
    long countByBusinessType(String businessType);
}