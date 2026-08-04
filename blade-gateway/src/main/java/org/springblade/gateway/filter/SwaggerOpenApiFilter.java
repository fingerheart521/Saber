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
package org.springblade.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.gateway.provider.RequestProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Swagger OpenAPI 服务地址重写过滤器
 *
 * @author Chill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SwaggerOpenApiFilter implements GlobalFilter, Ordered {

	private static final Pattern API_DOCS_PATTERN = Pattern.compile("^/([^/]+)/v3/api-docs(?:/.*)?$");
	private static final int FILTER_ORDER = -2;

	private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter;
	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String originalRequestUrl = RequestProvider.getOriginalRequestUrl(exchange);
		String originalPath = UriComponentsBuilder.fromUriString(originalRequestUrl).build().getPath();
		Matcher matcher = API_DOCS_PATTERN.matcher(originalPath);
		if (!matcher.matches()) {
			return chain.filter(exchange);
		}

		String servicePath = "/" + matcher.group(1);
		ModifyResponseBodyGatewayFilterFactory.Config config = new ModifyResponseBodyGatewayFilterFactory.Config()
			.setRewriteFunction(String.class, String.class,
				(serverWebExchange, body) -> Mono.justOrEmpty(rewriteServers(body, servicePath)));
		return modifyResponseBodyFilter.apply(config).filter(exchange, chain);
	}

	private String rewriteServers(String body, String servicePath) {
		if (body == null || body.isBlank()) {
			return body;
		}
		try {
			JsonNode root = objectMapper.readTree(body);
			if (!(root instanceof ObjectNode openApi)) {
				return body;
			}
			ArrayNode servers = objectMapper.createArrayNode();
			servers.addObject()
				.put("url", servicePath)
				.put("description", "Gateway server url");
			openApi.set("servers", servers);
			return objectMapper.writeValueAsString(openApi);
		} catch (JsonProcessingException exception) {
			log.warn("重写聚合接口文档服务地址失败: {}", servicePath, exception);
			return body;
		}
	}

	@Override
	public int getOrder() {
		return FILTER_ORDER;
	}

}
