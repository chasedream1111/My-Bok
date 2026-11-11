package com.gsy.bok.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public final class OssUtil {

    /** 头像大小上限：2MB */
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024L;
    /** 允许的图片后缀 */
    private static final Set<String> ALLOWED_SUFFIX = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${oss.endpoint}")

    private String endpoint;

    @Value("${oss.region}")

    private String region;

    @Value("${oss.bucket}")

    private String bucket;

    @Value("${oss.dirprefix:bok/user/}")

    private String dirprefix;

    @Value("${oss.accessKeyId}")

    private String accessKeyId;

    @Value("${oss.accessKeySecret}")

    private String accessKeySecret;

    private OssUtil() {
    }

    /**
     * 把前端上传的文件流写入 OSS，返回可访问的 URL。
     *
     * @param file 前端 multipart 上传的文件
     * @return 该对象在 OSS 上的完整访问地址
     */
    public String upload_image(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 头像上传免登录（注册流程要用），这里做最小兜底，别让人随便往 OSS 塞东西
        String suffix = suffixOf(file.getOriginalFilename()).replace(".", "");
        if (!ALLOWED_SUFFIX.contains(suffix)) {
            throw new IllegalStateException("只支持 jpg / png / webp 格式的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalStateException("图片不能超过 2MB");
        }

        // 对象名用 UUID，避免同名覆盖；后缀只保留字母数字，防止路径穿越和奇怪字符
        String objectName = dirprefix + LocalDate.now() + "/"
                + UUID.randomUUID().toString().replace("-", "")
                + suffixOf(file.getOriginalFilename());

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(new DefaultCredentialProvider(accessKeyId, accessKeySecret))
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            ossClient.putObject(new PutObjectRequest(bucket, objectName, inputStream, metadata));

            return "https://" + bucket + "." + endpoint.replace("https://", "").replace("http://", "") + "/" + objectName;
        } catch (OSSException oe) {
            // 服务端明确拒绝：权限、Bucket 不存在、签名错误等
            throw new IllegalStateException("OSS 上传失败: " + oe.getErrorCode() + " - " + oe.getErrorMessage(), oe);
        } catch (ClientException ce) {
            // 客户端侧问题：网络不通、Endpoint 写错等
            throw new IllegalStateException("OSS 客户端异常: " + ce.getMessage(), ce);
        } catch (IOException e) {
            throw new IllegalStateException("读取上传文件失败", e);
        } finally {
            ossClient.shutdown();
        }
    }

    private static String suffixOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return "";
        }
        String ext = originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
        return ext.matches("[a-z0-9]{1,10}") ? "." + ext : "";
    }
}
