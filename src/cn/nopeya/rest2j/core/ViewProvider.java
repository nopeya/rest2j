package cn.nopeya.rest2j.core;

public interface ViewProvider extends Provider {
	/**
	 * Init provider by the folder location relative to location of 'WebRoot/WEB-INF/'
	 * @param packageName 
	 * e.g, '/layouts/demo', 'view/templates/domin', etc
	 */
	public void init(String location) throws Exception;
	
	public String parse(ModelAndView mv)  throws Exception ;
}
