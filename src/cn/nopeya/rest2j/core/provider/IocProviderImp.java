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
		// ����ʵ����ioc����
		classCache.forEach(className -> {
			try {
				Class<?> clazz = Class.forName(className);
				// ��Registerע�����ע�ᵽIoc������
				if (!clazz.isAnnotationPresent(Register.class)) {return ;}
				// 
				String customize = clazz.getAnnotation(Register.class).value();
				String id = createIocID(clazz, customize);
				if (iocProvider.containsKey(id)) {
					throw new RuntimeException(String.format("id '%s' �ظ�", id));
				}
				Object newInstance = null;
				try {
					newInstance = clazz.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(String.format("����  '%s' ��ʼ��ʧ�ܡ�", clazz.getTypeName()));
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
	 * ��ȡ����IOC�����ڵ�ID
	 * ����Զ�������Ϊ����Ĭ��������
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
					boolean newInstance = inject.another(); // �Ƿ�Ҫע���µ�ʵ��
					String customize = inject.value().trim();
					if (!"".equals(customize)) {
						// �����֣�ֱ�Ӳ���
						// �Ҳ������׳��쳣, ���Ͳ�ƥ�����׳��쳣
						if (iocProvider.containsKey(customize)) {
							Object iocInstance = iocProvider.get(customize);
							if (type.isAssignableFrom(iocInstance.getClass())) {
								if (newInstance) {
									// �Ƿ�ע���µ�ʵ��
									field.set(obj, iocInstance.getClass().newInstance());
								} else {
									field.set(obj, iocInstance);
								}
							} else 
								throw new RuntimeException(String.format("id '%s' ��ʵ�����ֶ�%s���Ͳ�ƥ��", customize, field.getName()));
						} else 
							throw new RuntimeException(String.format("�Ҳ���id '%s' ��ʵ��", customize));
					} else {
						// û������
						if (iocProvider.containsKey(type.getName())) {
							// ��ͨ��ʹ�����ͻ�ȡ
							field.set(obj, iocProvider.get(type.getName()));
						} else {
							// �ӿڻ�����࣬ʹ�÷������͵ĵ�һ��ʵ��
							// û�н�����׳��쳣
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
								throw new RuntimeException(String.format("δ�ҵ��������� '%s' ��ʵ��", type.getName()));
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
					// ����Ϊ��ʱǰ����һ����Ҫȥ��
					path = path.replaceAll("^\\.", "");
					classCache.add(path.replace(".class", ""));
				}
			}
		}
	}
}
