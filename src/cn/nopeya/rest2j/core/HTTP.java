package cn.nopeya.rest2j.core;

/**
 * Enumerate the Method of HTTP
 */
public enum HTTP {
	GET, 
	HEAD, 
	POST, 
	PUT, 
	DELETE, 
	CONNECT, 
	OPTIONS, 
	TRACE, 
	PATCH,
	;
	
	/**
	 * Convert a HTTP Method string into the corresponding HTTP Method.
	 * @param String of the HTTP Method
	 * @return
	 */
	public static HTTP get(final String methodString) {
		for (HTTP method : HTTP.values()) {
			if (methodString.toUpperCase().equals(method.toString())) {
				return method;
			}
		}
		return null;
	}
}
