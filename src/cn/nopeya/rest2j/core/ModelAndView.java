package cn.nopeya.rest2j.core;

import java.util.Map;

public class ModelAndView {
	String view;
	Map<String, Object> model;
	
	public ModelAndView(String view) {
		this.view = view;
	}

	public ModelAndView(String view, Map<String, Object> model) {
		this.view = view;
		this.model = model;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public Map<String, Object> getModel() {
		return model;
	}

	public void setModel(Map<String, Object> model) {
		this.model = model;
	}
	
}
