package com.creatifcubed.simpleapi;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class SimpleDoc {
	private Document doc;

	public SimpleDoc() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			this.doc = docBuilder.newDocument();
		} catch (Exception ex) {
			throw new SimpleException(ex);
		}
	}

	public SimpleDoc(String path) {
		this.load(path);
	}

	public Document getDocument() {
		return this.doc;
	}

	public void save(String path) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(this.doc);

			StreamResult result = new StreamResult(new File(path));

			transformer.transform(source, result);
		} catch (Exception ex) {
			throw new SimpleException(ex);
		}
	}

	public void load(String path) {
		try {
			File source = new File(path);
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			this.doc = docBuilder.parse(source);
			this.doc.getDocumentElement().normalize();
		} catch (Exception ex) {
			throw new SimpleException(ex);
		}
	}
}
