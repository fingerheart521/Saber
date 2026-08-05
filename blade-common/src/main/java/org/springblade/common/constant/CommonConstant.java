package org.springblade.common.constant;

import org.springblade.core.launch.constant.AppConstant;

/**
 * 通用常量
 *
 * @author Chill
 */
public interface CommonConstant {

	/**
	 * ldg 模块名称
	 */
	String APPLICATION_LDG_NAME = AppConstant.APPLICATION_NAME_PREFIX + "ldg";

	/**
	 * flow 模块名称
	 */
	String APPLICATION_FLOW_NAME = AppConstant.APPLICATION_NAME_PREFIX + "flow";

	/**
	 * 招采服务模块名称
	 */
	String APPLICATION_PROCUREMENT_NAME = AppConstant.APPLICATION_NAME_PREFIX + "procurement";

	/**
	 * sword 系统名
	 */
	String SWORD_NAME = "sword";

	/**
	 * saber 系统名
	 */
	String SABER_NAME = "saber";

	/**
	 * 顶级父节点id
	 */
	Long TOP_PARENT_ID = 0L;

	/**
	 * 顶级父节点名称
	 */
	String TOP_PARENT_NAME = "顶级";


	/**
	 * 默认密码
	 */
	String DEFAULT_PASSWORD = "123456";

	/**
	 * 数据权限类型
	 */
	Integer DATA_SCOPE_CATEGORY = 1;

	/**
	 * 接口权限类型
	 */
	Integer API_SCOPE_CATEGORY = 2;

}
