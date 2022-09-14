package com.seaboxdata.sdps.uaa.example.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 测试 bean
 *
 * @author hermes-di
 */
public class TestBean implements Serializable {

	private static final long serialVersionUID = 8427521092357141246L;
	private int anInt;
	private Integer integer;
	private String string;
	private List<String> stringList;
	private Map<String, Object> objectMap;

	public int getAnInt() {
		return anInt;
	}

	public void setAnInt(int anInt) {
		this.anInt = anInt;
	}

	public Integer getInteger() {
		return integer;
	}

	public void setInteger(Integer integer) {
		this.integer = integer;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public List<String> getStringList() {
		return stringList;
	}

	public void setStringList(List<String> stringList) {
		this.stringList = stringList;
	}

	public Map<String, Object> getObjectMap() {
		return objectMap;
	}

	public void setObjectMap(Map<String, Object> objectMap) {
		this.objectMap = objectMap;
	}

	@Override
	public String toString() {
		return "TestBean{" + "anInt=" + anInt + ", integer=" + integer
				+ ", string='" + string + '\'' + ", stringList=" + stringList
				+ ", objectMap=" + objectMap + '}';
	}
}
