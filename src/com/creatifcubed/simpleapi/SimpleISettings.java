package com.creatifcubed.simpleapi;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SimpleISettings {
	public void load(String path);

	public void load();

	public void save();

	public void save(String path);

	public void setPath(String path);

	public String getPath();

	public String getString(String key);

	public String getString(String key, String def);

	public void put(String key, Object o);

	public boolean has(String key);

	public void remove(String key);

	public Integer getInt(String key);

	public Integer getInt(String key, Integer def);

	public Boolean getBool(String key);

	public Boolean getBool(String key, Boolean def);

	public Double getDouble(String key);

	public Double getDouble(String key, Double def);

	public void putCollection(String key, Collection<Object> c);

	public List<Object> getCollection(String key);

	public void tmpPut(String key, Object o);

	public boolean tmpHas(String key);

	public String tmpGetString(String key, String def);

	public Integer tmpGetInt(String key, Integer def);

	public Double tmpGetDouble(String key, Double def);

	public Collection<Object> tmpGetCollection(String key);

	public Map<Object, Object> tmpGetMap(String key);

	public Object tmpGetObject(String key);

	public Object tmpRemove(String key);

}
