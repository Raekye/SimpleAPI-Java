package com.creatifcubed.simpleapi;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SimpleXMLSettings implements SimpleISettings {
	private String path;
	private SimpleDoc source;
	private Map<String, Object> tmpData;

	public SimpleXMLSettings(String path) {
		this.path = path;
		this.load(path);
		this.tmpData = new HashMap<String, Object>();
	}

	@Override
	public void load(String path) {
		try {
			this.source = new SimpleDoc(path);
		} catch (SimpleException ex) {
			if (FileNotFoundException.class.isInstance(ex.getCause())) {
				this.source = new SimpleDoc();
				Document doc = this.source.getDocument();
				doc.appendChild(doc.createElement("root"));
			} else {
				throw ex;
			}
		}
	}

	@Override
	public void load() {
		this.load(this.path);
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public void save(String path) {
		this.source.save(path);
	}

	@Override
	public void save() {
		this.source.save(this.path);
	}

	@Override
	public void put(String key, Object value) {
		String[] levels = key.split("\\p{Punct}");
		Document doc = this.source.getDocument();

		Node curNode = followPath(levels, doc);

		if (curNode instanceof Element) {
			((Element) curNode).setAttribute("value", value.toString());
		}

	}

	@Override
	public boolean has(String key) {
		return this.get(key) != null;
	}

	@Override
	public String getString(String key) {
		return this.getString(key, null);
	}

	@Override
	public String getString(String key, String def) {
		String str = this.get(key);
		if (str == null) {
			return def;
		}
		return str;
	}

	@Override
	public Integer getInt(String key) {
		return this.getInt(key, null);
	}

	@Override
	public Integer getInt(String key, Integer def) {
		String bin = this.get(key);
		if (bin == null) {
			return def;
		}
		Integer x = def;
		try {
			x = Integer.parseInt(bin);
		} catch (NumberFormatException ignor) {
			// ignore
		}
		return x;
	}

	@Override
	public Boolean getBool(String key) {
		return this.getBool(key, null);
	}

	@Override
	public Boolean getBool(String key, Boolean def) {
		String bin = this.get(key);
		if (bin == null) {
			return def;
		}
		return Boolean.parseBoolean(bin);
	}

	@Override
	public Double getDouble(String key) {
		return this.getDouble(key, null);
	}

	@Override
	public Double getDouble(String key, Double def) {
		String bin = this.get(key);
		if (bin == null) {
			return def;
		}
		Double d = def;
		try {
			d = Double.parseDouble(bin);
		} catch (NumberFormatException ignore) {
			//
		}
		return d;
	}

	@Override
	public void tmpPut(String key, Object o) {
		this.tmpData.put(key, o);
	}

	@Override
	public boolean tmpHas(String key) {
		return this.tmpData.containsKey(key);
	}

	@Override
	public String tmpGetString(String key, String def) {
		if (this.tmpData.containsKey(key)) {
			Object o = this.tmpData.get(key);
			if (o instanceof String) {
				return (String) o;
			}
		}
		return def;
	}

	@Override
	public Integer tmpGetInt(String key, Integer def) {
		if (this.tmpData.containsKey(key)) {
			Object o = this.tmpData.get(key);
			if (o instanceof Integer) {
				return (Integer) o;
			}
		}
		return def;
	}

	@Override
	public Double tmpGetDouble(String key, Double def) {
		if (this.tmpData.containsKey(key)) {
			Object o = this.tmpData.get(key);
			if (o instanceof Double) {
				return (Double) o;
			}
		}
		return def;
	}

	@Override
	public Collection<Object> tmpGetCollection(String key) {
		if (this.tmpData.containsKey(key)) {
			Object o = this.tmpData.get(key);
			if (o instanceof Collection) {
				return (Collection<Object>) o;
			}
		}
		return null;
	}

	@Override
	public Map<Object, Object> tmpGetMap(String key) {
		if (this.tmpData.containsKey(key)) {
			Object o = this.tmpData.get(key);
			if (o instanceof Map) {
				return (Map<Object, Object>) o;
			}
		}
		return null;
	}

	@Override
	public Object tmpRemove(String key) {
		return this.tmpData.remove(key);
	}

	@Override
	public Object tmpGetObject(String key) {
		return this.tmpData.get(key);
	}

	@Override
	public void remove(String key) {
		Node n = followPath(key.split("\\p{Punct}"), this.source.getDocument());
		n.getParentNode().removeChild(n);
	}

	public String get(String key) {
		String[] levels = key.split("\\p{Punct}");
		Document doc = this.source.getDocument();

		Node curNode = followPath(levels, doc);

		if (curNode instanceof Element) {
			Element e = (Element) curNode;
			if (e.hasAttribute("value")) {
				return e.getAttribute("value");
			}
		}

		return null;
	}

	private static Node followPath(String[] path, Document doc) {
		Node root = doc.getDocumentElement();
		Node curNode = root;

		for (String separator : path) {
			Node nextNode = findNode(curNode.getChildNodes(), separator);
			if (nextNode == null) {
				nextNode = doc.createElement(separator);
				curNode.appendChild(nextNode);
			}
			curNode = nextNode;
		}

		return curNode;
	}

	private static Node findNode(NodeList list, String key) {
		if (list != null) {
			for (int i = 0; i < list.getLength(); i++) {
				Node n = list.item(i);
				if (n instanceof Element) {
					Element e = (Element) n;
					if (e.getTagName().equals(key)) {
						return e;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void putCollection(String key, Collection<Object> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Object> getCollection(String key) {
		throw new UnsupportedOperationException();
	}
}
