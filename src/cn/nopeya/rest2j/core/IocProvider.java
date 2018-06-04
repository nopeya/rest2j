package cn.nopeya.rest2j.core;

public interface IocProvider extends Provider {
	/**
	 * Init provider by the package name
	 * @param packageName 
	 * e.g, com.company.pro, org.organization.pro, etc
	 */
	public void init(String packageName) throws Exception;
}
