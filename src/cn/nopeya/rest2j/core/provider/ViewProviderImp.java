package cn.nopeya.rest2j.core.provider;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.nopeya.rest2j.core.ModelAndView;
import cn.nopeya.rest2j.core.ViewProvider;
import cn.nopeya.rest2j.exception.ProviderInitException;
import cn.nopeya.rest2j.exception.ViewNotFoundException;

public class ViewProviderImp implements ViewProvider  {
	
	private Map<String, File> viewTemplateProvider = new HashMap<>();
	private String basePath;
	
	@Override
	public void init(String path) throws Exception {
		setBasePath(path);
		initViewProvider(basePath);
	}

	@Override
	public Map<String, File> getProvider() {
		return viewTemplateProvider;
	}

	public String parse(ModelAndView mv) throws Exception {
		if (viewTemplateProvider.isEmpty()) { 
			throw new ViewNotFoundException("View template provider is empty.");
		}
		File file = viewTemplateProvider.get(mv.getView());
		StringBuffer sb = new StringBuffer();
		RandomAccessFile ra = new RandomAccessFile(file, "r");
		try{
			String line = null;
			while(null != (line = ra.readLine())){
				Pattern pattern = Pattern.compile("@\\{(.+?)\\}",Pattern.CASE_INSENSITIVE);
				Matcher m = pattern.matcher(line);
				while (m.find()) {
					for (int i = 1; i <= m.groupCount(); i ++) {
						String paramName = m.group(i);
						Object paramValue = mv.getModel().get(paramName);
						if(null == paramValue){ continue; }
						line = line.replaceAll("@\\{" + paramName + "\\}", paramValue.toString());
					}
				}
				sb.append(line);
			}
		}finally{
			ra.close();
		}
		return sb.toString();
	}
	
	private void initViewProvider(String path) throws Exception {
		beforeInit();
		getViews(path);
		afterInit();
	}

	private void beforeInit() {
		System.out.println("Initializing view template provider...");
	}

	private void afterInit() {
		System.out.println("View template provider initialization has been completed. Template size : " + viewTemplateProvider.size());
	}

	private void getViews(String path) throws Exception {
		path = (path + "/").replaceAll("//+", "/");
		File dir = new File(path);
		if (!dir.exists()) {
			throw new ProviderInitException("View provider initialization error, check the configuration please.");
		}
		for (File file : dir.listFiles()) {
			String filePath = path + file.getName();
			if (file.isDirectory()) {
				getViews(filePath);
			} else {
				viewTemplateProvider.put(filePath.replace(basePath, ""), file);
			}
		}
	}
	
	private void setBasePath(String path) {
		basePath = ViewProviderImp.class.getClassLoader().getResource("/").getPath().replace("classes", path);
	}
}
