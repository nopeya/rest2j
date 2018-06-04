package cn.nopeya.rest2j.core;

import javax.servlet.http.HttpServletRequest;

public interface HandlerProvider extends Provider {
	/**
	 * Init provider by a Ioc provider
	 * @param packageName 
	 * e.g, com.company.pro, org.organization.pro, etc
	 */
	public void init(IocProvider iocProvider)  throws Exception ;
	
	/**
	 * Get a resource handler by the url of a request.
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public ResourceHandle getHandle(HttpServletRequest request) throws Exception ;
}
