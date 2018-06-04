package cn.nopeya.rest2j.core.provider;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import cn.nopeya.rest2j.annotation.Path;
import cn.nopeya.rest2j.annotation.Publish;
import cn.nopeya.rest2j.core.HTTP;
import cn.nopeya.rest2j.core.HandlerProvider;
import cn.nopeya.rest2j.core.IocProvider;
import cn.nopeya.rest2j.core.ResourceHandle;
import cn.nopeya.rest2j.exception.ProviderInitException;

public class HandlerProviderImp implements HandlerProvider {

	private Map<String, EnumMap<HTTP, ResourceHandle>> handleProvider = new HashMap<>();

	@Override
	public void init(IocProvider iocProvider) throws Exception {
		initHandleProvider(iocProvider);
	}
	
	@Override
	public Map<String, EnumMap<HTTP, ResourceHandle>> getProvider() {
		return handleProvider;
	}

	public ResourceHandle getHandle(HttpServletRequest request) {
		if (handleProvider.isEmpty()) { return null;}
		String url = request.getRequestURI();
		String contextPath = request.getContextPath();
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		for (String patternUrl : handleProvider.keySet()) {
			Pattern pattern = Pattern.compile(patternUrl);
			Matcher matcher = pattern.matcher(url);
			if (matcher.matches()) {
				EnumMap<HTTP, ResourceHandle> enumMap = handleProvider.get(patternUrl);
				HTTP method = HTTP.get(request.getMethod());
				if (enumMap.containsKey(method)) {
					return enumMap.get(method);
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void initHandleProvider(IocProvider iocProvider) throws InstantiationException, IllegalAccessException {
		beforeInit();
		if (null == iocProvider) {
			throw new ProviderInitException("Handler provider can not initionalization with a null Ioc provider.");
		}
		Map<String, Object> provider = (Map<String, Object>) iocProvider.getProvider();
		if (provider.isEmpty()){return ;}
		for (Entry<String, Object> entry : provider.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			String dominUrl = ""; // ���ϵ�path
			if (clazz.isAnnotationPresent(Path.class)) {
				dominUrl += clazz.getAnnotation(Path.class).value().trim();
			}
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (!method.isAnnotationPresent(Publish.class)) {
					// ע���� HTTP Method���ܷ���
					continue ;
				}
				String subUrl = ""; // �����ϵ�path
				if (method.isAnnotationPresent(Path.class)) {
					subUrl = method.getAnnotation(Path.class).value().trim();
				}
				String url = ("/" + dominUrl + "/" + subUrl + "/").replaceAll("/+", "/"); // ͷβ��б��, ȥ���ظ���б��
				String patternUrl = getPatternString(url);
				if (!handleProvider.containsKey(patternUrl)) {
					// ��û�������Դ���򴴽���Դӳ��
					EnumMap<HTTP, ResourceHandle> value = new EnumMap<>(HTTP.class);
					handleProvider.put(patternUrl, value);
				}
				EnumMap<HTTP, ResourceHandle> enumMap = handleProvider.get(patternUrl);
				HTTP httpMethod = method.getAnnotation(Publish.class).value();
				if (enumMap.containsKey(httpMethod)) {
					throw new RuntimeException(String.format("��Դ'%s'��'%s'�����Ѵ���.", url, httpMethod));
				}
				Pattern pattern = Pattern.compile(patternUrl);
				ResourceHandle resourceHandle = new ResourceHandle(pattern, entry.getValue(), method);
				Map<String, Integer> pathParamPositionMap = pathParamPositionMapping(url); // ��ȡpath��Ĳ���
				resourceHandle.setPathParamPositionMap(pathParamPositionMap);
				enumMap.put(httpMethod, resourceHandle);
			}
		}
		afterInit();
	}
	
	
	
	private void beforeInit() {
		System.out.println("Initializing Handler provider...");
	}
	
	private void afterInit() {
		System.out.println("Handler provider initialization has been completed. Resource size : " + handleProvider.size());
	}
	
	/**
	 * ������ԴurlΪ����ģʽ���ַ���
	 * �м�ƥ����б��
	 * ��βƥ��0-n��б��
	 * ������{param}��ʽ����
	 * @param url
	 * @return
	 */
	private String getPatternString(String url) {
		url = url.replaceAll("/", "/+"); // б��ƥ����
		url = url.replaceAll("(^/\\+)|(/\\+$)", "/*"); // ͷβб��ƥ��0������
		url = url.replaceAll("(\\{[^/]{1,}?\\})", "([^/]{1,})"); // �滻����
		return url;
	}

	/**
	 * ��ȡPath�ڲ�����λ��ӳ��
	 * @param path
	 * @return
	 */
	private Map<String, Integer> pathParamPositionMapping(String path) {
		Map<String, Integer> pathParamPositionMap = new HashMap<>();
		Pattern pattern = Pattern.compile("\\{([^/]{1,}?)\\}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(path);
		int index = 0;
		while (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String paramName = matcher.group(i);
				if (pathParamPositionMap.containsKey(paramName)) {
					throw new RuntimeException(String.format("��·�� '%s' ���ظ������˲��� '%s'", path, paramName));
				} else {
					pathParamPositionMap.put(paramName, index);
					index++;
				}
			}
		}
		return pathParamPositionMap;
	}
}
