# rest2j
  rest2j是一个简易的服务发布框架，核心目标是方便地构建REST API。
  
# 使用准备
  1 将rest2j-x-x.jar文件拷贝到项目WebRoot/WEB-INF/lib目录下
  2 在src根目录下创建properties配置文件
  
# 使用
  1 创建项目入口文件， 重写启动事件，配置插件。
   ```java
   @WebServlet(name="Application", urlPatterns="/*")
public class Application extends REST2JEngine {
	private static final long serialVersionUID = 3098992381506693800L;
	@Override
	protected void startUp() {
		setConig("application");
		setIocProvider(new IocProviderImp());
		setHandlerProvider(new HandlerProviderImp());
		setViewProvider(new ViewProviderImp());
	}
}
   ```
  2 发布资源服务
  
  
  
