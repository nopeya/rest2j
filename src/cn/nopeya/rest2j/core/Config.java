package cn.nopeya.rest2j.core;

import java.util.ResourceBundle;

import cn.nopeya.rest2j.exception.ConfigException;

public class Config {
	private ResourceBundle config;
	
	public Config(String location) {
		config = ResourceBundle.getBundle(location);
	}
	
	public String get(String key) {
		if (null != config) {
			return config.getString(key);
		}
		throw new ConfigException("Error config. Check the configuration. key:" + key);
	}
}
