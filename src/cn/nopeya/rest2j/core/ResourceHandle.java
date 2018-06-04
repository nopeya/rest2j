package cn.nopeya.rest2j.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.nopeya.rest2j.annotation.param.PathParam;
import cn.nopeya.rest2j.annotation.param.RequestBody;
import cn.nopeya.rest2j.annotation.param.RequestParam;
import cn.nopeya.rest2j.exception.BadRequestException;
import cn.nopeya.rest2j.exception.InternalServerErrorException;
import cn.nopeya.rest2j.exception.NotFoundException;

public class ResourceHandle {
	
	private Pattern pattern;
	private Object instance;
	private Method method;
	private Map<String, Integer> pathParamPositionMap = new HashMap<String, Integer>();
	
	public ResourceHandle(Pattern pattern, Object instance, Method method) {
		super();
		this.pattern = pattern;
		this.instance = instance;
		this.method = method;
	}

	public Object invoke(HttpServletRequest request, HttpServletResponse response) throws BadRequestException, 
								NotFoundException, InternalServerErrorException, Exception {
		try {
			Object[] paramValues = getParamValues(request, response);
			Object object = method.invoke(instance, paramValues);
			return object;
		} catch (BadRequestException badRequestException) {
			throw badRequestException;
		} catch (NotFoundException notFoundException) {
			throw notFoundException;
		} catch (InternalServerErrorException internalServerErrorException) {
			throw internalServerErrorException;
		} catch (Exception exception) {
			throw exception;
		}
	}

	/**
	 * 获取方法参数值
	 * @param request
	 * @return
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	private Object[] getParamValues(HttpServletRequest request, HttpServletResponse response) throws BadRequestException, UnsupportedEncodingException, IOException {
		String url = getRequestUrl(request);
		Map<Integer, String> pathParamValueMap = getPathParamValueMap(url);
		Class<?>[] paramTypes = method.getParameterTypes();
		Object[] paramValues = new Object[paramTypes.length];
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		for (int i = 0; i < paramTypes.length; i++) {
			Class<?> paramType = paramTypes[i];
			if (paramType == HttpServletRequest.class) {
				paramValues[i] = request;
			} else if (paramType == HttpServletResponse.class) {
				paramValues[i] = response;
			} else {
				Annotation[] annotations = parameterAnnotations[i];
				if (annotations.length == 0) {
					// 没有注解，默认为null
					paramValues[i] = getDefaultNull(paramType);
					continue;
				} 
				//
				Annotation annotation = getParamAnnotation(annotations);
				if (annotation instanceof PathParam) {
					// path param
					String pathParamName  = ((PathParam) annotation).value().trim();
					if (!pathParamPositionMap.containsKey(pathParamName)) {
						// 判断是否在路径中注册过
						// 没有则不可用，抛出异常
						throw new InternalServerErrorException(String.format("can not found param '%s' on path", pathParamName));
					} else {
						int pathParamPostion = pathParamPositionMap.get(pathParamName);
						String value = pathParamValueMap.get(pathParamPostion);
						paramValues[i] = castParamValue(value, paramType);
					}
				} else if (annotation instanceof RequestParam) {
					// 请求参数 或表单参数
					String requestParamName = ((RequestParam) annotation).value().trim();
					if (paramType.isArray()) {
						// 数组
						Class<?> componentType = paramType.getComponentType();
						String[] values = request.getParameterValues(requestParamName);
						paramValues[i] = convertValuesTo(values, componentType);
					} else {
						// 不是数组 直接获取
						String value = request.getParameter(requestParamName);
						paramValues[i] = castParamValue(value, paramType);
					}
				} else if (annotation instanceof RequestBody) {
					BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));
					StringBuffer sb = new StringBuffer("");
					String temp;
					while ((temp = br.readLine()) != null) {
						sb.append(temp);
					}
					br.close();
					paramValues[i] = castParamValue(sb.toString(), paramType);
				} else {
					paramValues[i] = null;
					continue;
				}
			}
		}
		
		return paramValues;
	}

	/**
	 * 
	 * @param values
	 * @param componentType
	 * @return
	 */
	private Object convertValuesTo(String[] values, Class<?> componentType) throws BadRequestException {
		try {
			if (String.class == componentType) {		// String
				return values;
			} else if (int.class == componentType) {		// int
				int[] result = new int[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Integer.parseInt(values[i]);
				}
				return result;
			} else if (Integer.class == componentType) {		// Integer
				Integer[] result = new Integer[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Integer.parseInt(values[i]);
				}
				return result;
			} else if (boolean.class == componentType) {		// boolean
				boolean[] result = new boolean[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Boolean.parseBoolean(values[i]);
				}
				return result;
			} else if (Boolean.class == componentType) {		// Boolean
				Boolean[] result = new Boolean[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Boolean.parseBoolean(values[i]);
				}
				return result;
			} else if (long.class == componentType) {			// long
				long[] result = new long[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Long.parseLong(values[i]);
				}
				return result;
			} else if (Long.class == componentType) {			// Long
				Long[] result = new Long[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Long.parseLong(values[i]);
				}
				return result;
			} else if (short.class == componentType) {			// short
				short[] result = new short[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Short.parseShort(values[i]);
				}
				return result;
			} else if (Short.class == componentType) {			// Short
				Short[] result = new Short[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Short.parseShort(values[i]);
				}
				return result;
			} else if (float.class == componentType) {			// float
				float[] result = new float[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Float.parseFloat(values[i]);
				}
				return result;
			} else if (Float.class == componentType) {			// Float
				Float[] result = new Float[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Float.parseFloat(values[i]);
				}
				return result;
			} else if (double.class == componentType) {			// double
				double[] result = new double[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Double.parseDouble(values[i]);
				}
				return result;
			} else if (Double.class == componentType) {			// Double
				Double[] result = new Double[values.length];
				for (int i = 0; i < values.length; i++) {
					result[i] = Double.parseDouble(values[i]);
				}
				return result;
			}
			return null;
		} catch (Exception e) {
			throw new BadRequestException("convert param error.");
		}
		
	}

	/**
	 * 获取一个默认的空值
	 * @param type
	 * @return
	 */
	private Object getDefaultNull(Class<?> type) {
		if (type.isPrimitive()) {
			return 0;
		}
		return null;
	}

	/**
	 * 参数值类型转换
	 * @param value
	 * @param clazz
	 * @return
	 */
	private Object castParamValue(String value, Class<?> clazz) throws BadRequestException {
		try {
			if (null == value) {
				return value;
			}
			if (String.class == clazz) {		// String
				return value;
			} else if (int.class == clazz
					|| Integer.class == clazz) {		// int Integer
				return Integer.valueOf(value);
			} else if (short.class == clazz
					|| Short.class == clazz) {		// short Short
				return Short.parseShort(value);
			} else if (long.class == clazz
					|| Long.class == clazz) {		// long Long
				return Long.parseLong(value);
			} else if (float.class == clazz
					|| Float.class == clazz) {		// float Float
				return Float.parseFloat(value);
			} else if (double.class == clazz
					|| Double.class == clazz) {		// double Double
				return Double.parseDouble(value);
			} else if (boolean.class == clazz
					|| Boolean.class == clazz) {		// boolean Boolean
				return Boolean.parseBoolean(value);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new BadRequestException("convert param error");
		}
		
	}

	/**
	 * 从请求路径里提取参数值
	 * @param url
	 * @return
	 */
	private Map<Integer, String> getPathParamValueMap(String url) {
		Map<Integer, String> map = new HashMap<>();
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				map.put(i - 1, matcher.group(i));
			}
		}
		return map;
	}

	/**
	 * 获取请求路径
	 * @param request
	 * @return
	 */
	private String getRequestUrl(HttpServletRequest request) {
		String url = request.getRequestURI();
		String contextPath = request.getContextPath();
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		return url;
	}

	/**
	 * 获取参数注解
	 * @param annotations
	 * @return
	 */
	private Annotation getParamAnnotation(Annotation[] annotations) {
		Annotation paramAnnotation = null;
		int count = 0;
		for (Annotation annotation : annotations) {
			if (annotation instanceof PathParam
					|| annotation instanceof RequestParam
					|| annotation instanceof RequestBody) {
				count++;
				paramAnnotation = annotation;
			}
		}
		if (count > 1) {
			throw new RuntimeException("在一个参数上设置了多个参数注解.");
		}
		return paramAnnotation;
	}
	
	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Map<String, Integer> getPathParamPositionMap() {
		return pathParamPositionMap;
	}

	public void setPathParamPositionMap(Map<String, Integer> pathParamPositionMap) {
		this.pathParamPositionMap = pathParamPositionMap;
	}
}
