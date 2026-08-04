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
package org.springblade.flow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.secure.annotation.PreAuth;
import org.springblade.core.swagger.annotation.ApiOrder;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.constant.RoleConstant;
import org.springblade.flow.service.IProcessDefinitionService;
import org.springblade.flow.vo.ProcessDefinitionVO;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * 流程定义控制器
 *
 * @author Chill
 */
@RestController
@AllArgsConstructor
@RequestMapping("/process/definition")
@ApiOrder
@Tag(name = "流程定义", description = "流程定义部署与管理")
public class ProcessDefinitionController {

	private final IProcessDefinitionService processDefinitionService;

	@GetMapping("/list")
	@Operation(summary = "流程定义分页", description = "分页查询当前租户的流程定义")
	public R<IPage<ProcessDefinitionVO>> list(
		@RequestParam(defaultValue = "1") Integer current,
		@RequestParam(defaultValue = "10") Integer size,
		@RequestParam(required = false) String key,
		@RequestParam(required = false) String name) {
		return R.data(processDefinitionService.page(current, size, key, name));
	}

	@PostMapping(value = "/deploy", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "部署流程定义", description = "上传并部署BPMN流程文件")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ProcessDefinitionVO> deploy(
		@Parameter(description = "BPMN流程文件", required = true)
		@RequestPart("file") MultipartFile file,
		@Parameter(description = "流程分类")
		@RequestParam(required = false) String category) {
		return R.data(processDefinitionService.deploy(file, category));
	}

	@PostMapping("/suspend")
	@Operation(summary = "挂起流程定义", description = "挂起流程定义及其流程实例")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<Void> suspend(
		@Parameter(description = "流程定义ID", required = true)
		@RequestParam String processDefinitionId) {
		return R.status(processDefinitionService.suspend(processDefinitionId));
	}

	@PostMapping("/activate")
	@Operation(summary = "激活流程定义", description = "激活流程定义及其流程实例")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<Void> activate(
		@Parameter(description = "流程定义ID", required = true)
		@RequestParam String processDefinitionId) {
		return R.status(processDefinitionService.activate(processDefinitionId));
	}

	@GetMapping("/resource")
	@Operation(summary = "下载流程定义", description = "下载流程定义对应的BPMN资源")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public ResponseEntity<Resource> resource(
		@Parameter(description = "流程定义ID", required = true)
		@RequestParam String processDefinitionId) {
		Resource resource = processDefinitionService.download(processDefinitionId);
		String filename = resource.getFilename() == null ? "process.bpmn20.xml" : resource.getFilename();
		ContentDisposition contentDisposition = ContentDisposition.attachment()
			.filename(filename, StandardCharsets.UTF_8)
			.build();
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
			.contentType(MediaType.APPLICATION_XML)
			.body(resource);
	}

	@PostMapping("/remove")
	@Operation(summary = "删除流程定义", description = "删除无关联流程实例的部署版本")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<Void> remove(
		@Parameter(description = "流程定义ID", required = true)
		@RequestParam String processDefinitionId) {
		return R.status(processDefinitionService.remove(processDefinitionId));
	}

}
