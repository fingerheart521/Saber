/**
 * Copyright (c) 2018-2099, Chill Zhuang 庄骞 (bladejava@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springblade.resource.endpoint;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springblade.core.oss.QiniuTemplate;
import org.springblade.core.oss.model.BladeFile;
import org.springblade.core.oss.model.OssFile;
import org.springblade.core.swagger.annotation.ApiOrder;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

/**
 * 对象存储端点
 *
 * @author Chill
 */
@RestController
@AllArgsConstructor
@RequestMapping("/oss/endpoint")
@ApiOrder
@Tag(name = "对象存储端点", description = "对象存储端点")
public class OssEndpoint {

	private static final String EXPERT_BUCKET = "expert";
	private static final long MAX_EXPERT_FILE_SIZE = 10 * 1024 * 1024L;
	private static final Set<String> EXPERT_FILE_TYPES = Set.of("pdf", "jpg", "jpeg", "png");

	private QiniuTemplate qiniuTemplate;

	/**
	 * 创建存储桶
	 */
	@SneakyThrows
	@PostMapping("/make-bucket")
	public R makeBucket(@RequestParam String bucketName) {
		qiniuTemplate.makeBucket(bucketName);
		return R.success("创建成功");
	}

	/**
	 * 创建存储桶
	 */
	@SneakyThrows
	@PostMapping("/remove-bucket")
	public R removeBucket(@RequestParam String bucketName) {
		qiniuTemplate.removeBucket(bucketName);
		return R.success("删除成功");
	}

	/**
	 * 拷贝文件
	 */
	@SneakyThrows
	@PostMapping("/copy-file")
	public R copyFile(@RequestParam String fileName, @RequestParam String destBucketName, String destFileName) {
		qiniuTemplate.copyFile(fileName, destBucketName, destFileName);
		return R.success("操作成功");
	}

	/**
	 * 获取文件信息
	 */
	@SneakyThrows
	@GetMapping("/stat-file")
	public R<OssFile> statFile(@RequestParam String fileName) {
		return R.data(qiniuTemplate.statFile(fileName));
	}

	/**
	 * 获取文件相对路径
	 */
	@SneakyThrows
	@GetMapping("/file-path")
	public R<String> filePath(@RequestParam String fileName) {
		return R.data(qiniuTemplate.filePath(fileName));
	}


	/**
	 * 获取文件外链
	 */
	@SneakyThrows
	@GetMapping("/file-link")
	public R<String> fileLink(@RequestParam String fileName) {
		return R.data(qiniuTemplate.fileLink(fileName));
	}

	/**
	 * 上传文件
	 */
	@SneakyThrows
	@PostMapping("/put-file")
	public R<BladeFile> putFile(@RequestParam MultipartFile file) {
		BladeFile bladeFile = qiniuTemplate.putFile(file.getOriginalFilename(), file.getInputStream());
		return R.data(bladeFile);
	}

	/**
	 * 上传文件
	 */
	@SneakyThrows
	@PostMapping("/put-file-by-name")
	public R<BladeFile> putFile(@RequestParam String fileName, @RequestParam MultipartFile file) {
		BladeFile bladeFile = qiniuTemplate.putFile(fileName, file.getInputStream());
		return R.data(bladeFile);
	}

	/**
	 * 上传专家证明附件
	 */
	@PostMapping("/put-file-bucket")
	public R<BladeFile> putFileBucket(@RequestParam String bucketName, @RequestParam MultipartFile file) {
		if (!EXPERT_BUCKET.equals(bucketName)) {
			return R.fail("仅允许上传到expert存储桶");
		}
		if (file == null || file.isEmpty()) {
			return R.fail("证明附件不能为空");
		}
		if (file.getSize() > MAX_EXPERT_FILE_SIZE) {
			return R.fail("证明附件不能超过10MB");
		}

		String originalName = file.getOriginalFilename();
		if (!StringUtils.hasText(originalName)) {
			return R.fail("证明附件名称不能为空");
		}
		if (originalName.length() > 100) {
			return R.fail("证明附件名称不能超过100个字符");
		}
		int dotIndex = originalName.lastIndexOf('.');
		String fileType = dotIndex < 0 ? "" : originalName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
		if (!EXPERT_FILE_TYPES.contains(fileType)) {
			return R.fail("证明附件仅支持PDF、JPG、JPEG、PNG格式");
		}

		return R.data(qiniuTemplate.putFile(EXPERT_BUCKET, originalName, file));
	}

	/**
	 * 删除文件
	 */
	@SneakyThrows
	@PostMapping("/remove-file")
	public R removeFile(@RequestParam String fileName) {
		qiniuTemplate.removeFile(fileName);
		return R.success("操作成功");
	}

	/**
	 * 批量删除文件
	 */
	@SneakyThrows
	@PostMapping("/remove-files")
	public R removeFiles(@RequestParam String fileNames) {
		qiniuTemplate.removeFiles(Func.toStrList(fileNames));
		return R.success("操作成功");
	}

}
