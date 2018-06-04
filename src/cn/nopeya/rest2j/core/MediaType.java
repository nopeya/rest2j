package cn.nopeya.rest2j.core;

public class MediaType {

    public static final String CHARSET_PARAMETER = "charset";
    public static final String CHARSET_UTF_8 = "utf-8";
    
    public static final String MEDIA_TYPE_WILDCARD = "*";
    public final static String WILDCARD = "*/*";
    public final static String APPLICATION_XML = "application/xml";
    public final static String APPLICATION_ATOM_XML = "application/atom+xml";
    public final static String APPLICATION_XHTML_XML = "application/xhtml+xml";
    public final static String APPLICATION_SVG_XML = "application/svg+xml";
    public final static String APPLICATION_JSON = "application/json";
    public final static String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public final static String MULTIPART_FORM_DATA = "multipart/form-data";
    public final static String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public final static String TEXT_PLAIN = "text/plain";
    public final static String TEXT_XML = "text/xml";
    public final static String TEXT_HTML = "text/html";
    

    public static final String TYPE_DEFAULT = APPLICATION_JSON;
    public static final String CHARSET_DEFAULT = CHARSET_UTF_8;
    
    private String type;
    private String charSet;
    
    public MediaType(String type, String charSet) {
		this.type = type;
		this.charSet = charSet;
	}
    
	public MediaType(String type) {
		this.type = type;
		this.charSet = CHARSET_DEFAULT;
	}

	
	
	public MediaType() {
		this.type = TYPE_DEFAULT;
		this.charSet = CHARSET_DEFAULT;
	}

	@Override
    public String toString() {
    	return String.format("%s;%s=%s", type, CHARSET_PARAMETER, charSet);
    }
}
