package cn.nopeya.rest2j.core.provider;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import cn.nopeya.rest2j.annotation.Inject;
import cn.nopeya.rest2j.annotation.Register;
import cn.nopeya.rest2j.core.IocProvider;

public class IocProviderImp implements IocProvider {
	
	private List<String> classCache = new ArrayList<String>();
	private Map<String, Object> iocProvider = new ConcurrentHashMap<>();

	@Override
	public void init(String packageName) {
		initClassCache(packageName);
		initIocProvider();
		injection();
	}
	
	@Override
	public Map<String, Object> getProvider() {
		return iocProvider;
	}

	private void initIocProvider() {
		beforeInitIoc();
		if (classCache.isEmpty()) {return;}
		// 加载实例到ioc容器
		classCache.forEach(className -> {
			try {
				Class<?> clazz = Class.forName(className);
				// 用Register注解才能注册到Ioc容器内
				if (!clazz.isAnnotationPresent(Register.class)) {return ;}
				// 
				String customize = clazz.getAnnotation(Register.class).value();
				String id = createIocID(clazz, customize);
				if (iocProvider.containsKey(id)) {
					throw new RuntimeException(String.format("id '%s' 重复", id));
				}
				Object newInstance = null;
				try {
					newInstance = clazz.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(String.format("类型  '%s' 初始化失败。", clazz.getTypeName()));
				}
				iocProvider.put(id, newInstance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		afterInitIoc();
	}

	private void beforeInitIoc() {
		System.out.println("Initializing Ioc provider...");
	}
	
	private void afterInitIoc() {
		System.out.println("Ioc provider initialization has been completed. Ioc instance size : " + iocProvider.size());
	}

	/**
	 * 获取类在IOC容器内的ID
	 * 如果自定义名字为空则默认用类名
	 * @param clazz
	 * @param customize
	 * @return
	 */
	private String createIocID(Class<?> clazz, String customize) {
		return !"".equals(customize.trim()) ? customize : clazz.getName();
	}

	private void injection() {
		if (iocProvider.isEmpty()) {return ;}
		try {
			for (Entry<String, Object> entry : iocProvider.entrySet()) {
				Object obj = entry.getValue();
				Field[] fields = obj.getClass().getDeclaredFields();
				for (Field field : fields) {
					if (!field.isAnnotationPresent(Inject.class)) {continue ;}
					field.setAccessible(true);
					Class<?> type = field.getType();
					Inject inject = field.getAnnotation(Inject.class);
					boolean newInstance = inject.another(); // 是否要注入新的实例
					String customize = inject.value().trim();
					if (!"".equals(customize)) {
						// 有名字，直接查找
						// 找不到则抛出异常, 类型不匹配则抛出异常
						if (iocProvider.containsKey(customize)) {
							Object iocInstance = iocProvider.get(customize);
							if (type.isAssignableFrom(iocInstance.getClass())) {
								if (newInstance) {
									// 是否注入新的实例
									field.set(obj, iocInstance.getClass().newInstance());
								} else {
									field.set(obj, iocInstance);
								}
							} else 
								throw new RuntimeException(String.format("id '%s' 的实例和字段%s类型不匹配", customize, field.getName()));
						} else 
							throw new RuntimeException(String.format("找不到id '%s' 的实例", customize));
					} else {
						// 没有名字
						if (iocProvider.containsKey(type.getName())) {
							// 普通类使用类型获取
							field.set(obj, iocProvider.get(type.getName()));
						} else {
							// 接口或抽象类，使用符合类型的第一个实例
							// 没有结果则抛出异常
							boolean isFound = false;
							for (Entry<String, Object> entity : iocProvider.entrySet()) {
								Object iocInstance = entity.getValue();
								if (type.isAssignableFrom(iocInstance.getClass())) {
									if (newInstance) {
										field.set(obj, iocInstance.getClass().newInstance());
									} else {
										field.set(obj, iocInstance);
									}
									isFound = true;
									break;
								}
							}
							if (!isFound) {
								throw new RuntimeException(String.format("未找到符合类型 '%s' 的实例", type.getName()));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initClassCache(String packageName) {
		URL url = IocProviderImp.class.getResource("/" + packageName.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		for (File file : dir.listFiles()) {
			String path = packageName + "." + file.getName();
			if (file.isDirectory()) {
				initClassCache(path);
			} else {
				if (file.getName().endsWith(".class")) {
					// 包名为空时前面多出一个点要去掉
					path = path.replaceAll("^\\.", "");
					classCache.add(path.replace(".class", ""));
				}
			}
		}
	}
}
