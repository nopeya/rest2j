# rest2j
  rest2j是一个简易的服务发布框架，核心目标是使用注解方便地构建REST API。
  
## 使用准备
  1. 将rest2j-x-x.jar文件拷贝到项目WebRoot/WEB-INF/lib目录下  
  2. 在src根目录下创建properties配置文件
  
## 使用
  1. 创建项目入口文件， 重写启动事件，配置插件
  
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
   
  2. 发布资源服务
  
  ```java
	@Register
	@Path("/book")
	public class BookModule {

		@Inject
		private BookService service;

		/**
		 * index
		 * @return
		 */
		@Publish(HTTP.GET)
		public Response index() {
			List<Book> list = service.list();
			Object json = JSON.toJSON(list);
			return Response.build(json);
		}

		/**
		 * create a resource
		 * @param json
		 * @return
		 * @throws Exception
		 */
		@Publish(HTTP.POST)
		public Response add(@RequestBody String json) throws Exception {
			Book book = JSON.parseObject(json, Book.class);
			Book newBook = service.add(book);
			Response response = Response.build(JSON.toJSONString(newBook), Response.SC_CREATED);
			response.addHeader("Location", "http://localhost:8080/ProDemo/book/" + newBook.getId());
			return response;
		}

		/**
		 * create a sub resource
		 * @param json
		 * @param response
		 * @return
		 * @throws Exception
		 */
		@Publish(HTTP.POST)
		@Path("/{bookid}/record")
		public Response addRecord(@RequestBody String json, @PathParam("bookid") int bookId) throws Exception {
			Record record = JSON.parseObject(json, Record.class);
			Date date = new Date(System.currentTimeMillis());
			record.setBookId(bookId);
			record.setBtime(date);
			Record newRecord = service.addRecord(record);
			Response response = Response.build(JSON.toJSONString(newRecord), Response.SC_CREATED);
			response.addHeader("Location", "http://localhost:8080/ProDemo/book/" + bookId + "/record/" + newRecord.getId());
			return response;
		}

		/**
		 * get a resource
		 * @param id
		 * @return
		 */
		@Publish(HTTP.GET)
		@Path("/{id}")
		public JSON getBook(@PathParam("id") int id) {
			Book book = service.get(id);
			JSONObject object =  (JSONObject) JSONObject.toJSON(book);
			return object;
		}

		/**
		 * get a sub resource
		 * @param id
		 * @return
		 */
		@Publish(HTTP.GET)
		@Path("/{id}/record/")
		public Response getRecord(@PathParam("id") int id) {
			List<Record> list = service.recordList(id);
			Object json = JSON.toJSON(list);
			return Response.build(json);
		}

		/**
		 * update any field
		 * @param json
		 * @param id
		 * @return
		 * @throws Exception
		 */
		@Publish(HTTP.PUT)
		@Path("/{id}")
		public Response update(@RequestBody String json, @PathParam("id") int id) throws Exception {
			Book book = JSON.parseObject(json, Book.class);
			service.update(book, id);
			Response response = Response.build(Response.SC_NO_CONTENT);
			return response;
		}

		/**
		 * update some field
		 * @param json
		 * @param id
		 * @return
		 * @throws Exception
		 */
		@Publish(HTTP.PATCH)
		@Path("/{id}")
		public Response set(@RequestBody String json, @PathParam("id") int id) throws Exception {
			service.update(json, id);
			Response response = Response.build(Response.SC_NO_CONTENT);
			return response;
		}

		/**
		 * delete a resource
		 * @param id
		 * @return
		 * @throws Exception
		 */
		@Publish(HTTP.DELETE)
		@Path("/{id}")
		public Response del(@PathParam("id") int id) throws Exception {
			service.del(id);
			Response response = Response.build(Response.SC_NO_CONTENT);
			return response;
		}

		/**
		 * redirect the request to the target
		 * @return
		 */
		@Publish(HTTP.GET)
		@Path("/activity/oldplan")
		public Response redirect() {
			String target = "http://localhost:8080/ProDemo/book/activity/half-price";
	//		Response response = Response.build(Response.SC_MULTIPLE_CHOICES);
			Response response = Response.build(Response.SC_FOUND);
			response.addHeader("location", target);
			return response;
		}

		@Publish(HTTP.GET)
		@Path("/activity/half-price")
		public String target() {
			return "welcome to half-price activity.";
		}

		/**
		 * return a page
		 * @return
		 */
		@Publish(HTTP.GET)
		@Path("/home")
		public ModelAndView home() {
			Map<String, Object> model = new HashMap<>();
			model.put("app", "rest2j");
			return new ModelAndView("home.html", model);
		}
	}
  ```
