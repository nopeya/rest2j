package cn.nopeya.rest2j.core;

public class AppContext {

	/**
	 * Ioc Provider ���������ļ��ڵ��ֶ���
	 */
	private static final String IOC_PACKAGE_PROPERTY = "ioc_package";
	/**
	 * View Template Provider �ļ���·���������ļ��ڵ��ֶ���
	 */
	private static final String VIEW_TEMPLATE_PATH_PROPERTY = "template_root";
	
	private Config config;
	private IocProvider iocProvider;
	private HandlerProvider handlerProvider;
	private ViewProvider viewProvider;

	private long startTime;
	private long endTime;
	
	public AppContext (Config config) {
		this.config = config;
	}
	
	public void start() throws Exception {
		beforeStart();
		// ��ʼ��ioc�����ڵ���
		if (null != iocProvider) {
			iocProvider.init(config.get(IOC_PACKAGE_PROPERTY));
		}
		// ��ʼ����������
		if (null != handlerProvider) {
			handlerProvider.init(iocProvider);
		}
		// ��ʼ����ͼģ��
		if (null != viewProvider) {
			viewProvider.init(config.get(VIEW_TEMPLATE_PATH_PROPERTY));
		}
		afterStart();
	}
	
	private void beforeStart() {
		startTime = System.currentTimeMillis();
	}

	private void afterStart() {
		endTime = System.currentTimeMillis();
		System.err.println(String.format("application load in %s ms", endTime-startTime));
	}

	public void setIocProvider(IocProvider provider) {
		this.iocProvider = provider;
	}

	public void setHandlerProvider(HandlerProvider provider) {
		this.handlerProvider = provider;
	}

	public void setViewProvider(ViewProvider provider) {
		this.viewProvider = provider;
	}

	public IocProvider getIocProvider() {
		return iocProvider;
	}

	public HandlerProvider getHandlerProvider() {
		return handlerProvider;
	}

	public ViewProvider getViewProvider() {
		return viewProvider;
	}
}
