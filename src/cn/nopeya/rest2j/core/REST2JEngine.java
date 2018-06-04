package cn.nopeya.rest2j.core;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.nopeya.rest2j.exception.AppInitionalizationException;
import cn.nopeya.rest2j.exception.BadRequestException;
import cn.nopeya.rest2j.exception.ConfigException;
import cn.nopeya.rest2j.exception.InternalServerErrorException;
import cn.nopeya.rest2j.exception.NotFoundException;

public class REST2JEngine extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String HEADER_IFMODSINCE = "If-Modified-Since";
    private static final String HEADER_LASTMOD = "Last-Modified";
    
    private Config config;
    private AppContext application;
    
	private boolean allowAORS;    // 是否跨域
	
	@Override
	public void init() throws ServletException {
		try {
			startUp();
			run();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppInitionalizationException("Application Initionalization error.");
		}
	}
	/**
	 * customize
	 */
	protected void startUp() {}
	
	private void run() throws Exception {
		setAllowCORS(); // 是否允许跨域
		application.start();
	}

	protected void setConig(String configLocation) {
		if (null == configLocation || "".equals(configLocation.trim())) {
			throw new ConfigException("No configuration file has been set.");
		}
		config = new Config(configLocation);
		application = new AppContext(config); // 上下文
	}

	/**
	 * dispatcher
	 * @param request
	 * @param response
	 */
	private void dispatch(HttpServletRequest request, HttpServletResponse response) {
        try {
    		doCORS(request, response);
			ResourceHandle handle = application.getHandlerProvider().getHandle(request);
			if (null == handle) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				render("rest2j 404 error!", response);
				return;
			}
			Object obj = handle.invoke(request, response);
			handleResponse(obj, response);
		} catch (BadRequestException badRequestException) {
			badRequestException.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (NotFoundException notFoundException) {
			notFoundException.printStackTrace();
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} catch (InternalServerErrorException internalServerErrorException) {
			internalServerErrorException.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Handle the response of the method.
	 * @param obj
	 * @param response
	 * @throws Exception
	 */
	private void handleResponse(Object obj, HttpServletResponse response) throws Exception {
		if (null != obj) {
			if (obj instanceof ModelAndView) {
				String str = application.getViewProvider().parse((ModelAndView) obj);
				render(str, response);
			} else if (obj instanceof Response) {
				Response res = (Response) obj;
				// 状态
				response.setStatus(res.getStatus());
				// 返回类型
				MediaType contentType = res.getContentType();
				if (null != contentType) {
					response.setContentType(contentType.toString());
				} else {
					response.setContentType(new MediaType().toString());
				}
				// 响应头
				Map<String, String> headers = res.getHeaders();
				if (!headers.isEmpty()) {
					headers.forEach((key, value) -> {
						response.addHeader(key, value);
					});
				}
				handleResponse(res.getEntity(), response);
			} else {
				render(obj.toString(), response);
			}
		}
	}
	
	private void render(String str, HttpServletResponse response) throws Exception {
		response.getWriter().append(str).flush();
	}
	
	private void doCORS(HttpServletRequest request, HttpServletResponse response) {
		if (allowAORS) {
			response.setHeader("Access-Control-Allow-Origin", "*"); // 跨域
	        response.setHeader("Access-Control-Allow-Methods", "*");
		}
	}

	private void setAllowCORS() {
		allowAORS = Boolean.parseBoolean(config.get("allow_cors"));
	}

	protected void setViewProvider(ViewProvider provider) {
		application.setViewProvider(provider);
	}

	protected void setHandlerProvider(HandlerProvider provider) {
		application.setHandlerProvider(provider);
	}

	protected void setIocProvider(IocProvider provider) {
		application.setIocProvider(provider);
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		resp.setCharacterEncoding("utf-8");
		System.out.println("Received a request: " + req.getRequestURI());
		String method = req.getMethod();
        if (method.equals(HTTP.GET)) {
            long lastModified = getLastModified(req);
            if (lastModified != -1) {
                long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
                if (ifModifiedSince < lastModified) 
                    maybeSetLastModified(resp, lastModified);
                else 
                    resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
        } else if (method.equals(HTTP.HEAD)) {
            long lastModified = getLastModified(req);
            maybeSetLastModified(resp, lastModified);
        }
        dispatch(req, resp);
	}
	
	/**
	 * @param resp
	 * @param lastModified
	 * @see HttpServlet#maybeSetLastModified
	 */
	private void maybeSetLastModified(HttpServletResponse resp,
	            long lastModified) {
		if (resp.containsHeader(HEADER_LASTMOD))
			return;
		if (lastModified >= 0)
			resp.setDateHeader(HEADER_LASTMOD, lastModified);
	}
}
