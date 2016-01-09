package com.pitsu.tools;

public class Property {

	private Integer lineNumber;
	private String key;
	private String value;
	private String fileName;
	
	public Integer getLineNumber() {
		return lineNumber;
	}
	
	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(String.format("%1$4s", lineNumber)).append(" ");
		sb.append(key).append(" = ");
		sb.append(value);
		return sb.toString();
	}

	public String toString(boolean showFileName) {
		return toString() + "\t" + "(" + fileName + ")";
	}
	
	public String toStringSimple() {
		StringBuilder sb = new StringBuilder(String.format("%1$4s", lineNumber)).append(" ");
		sb.append(key);
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} if(obj == null) {
			return false;
		} if(!(obj instanceof Property)) {
			return false;
		}
		Property other = (Property) obj;
		
		if((lineNumber != null && lineNumber.equals(other.lineNumber)) && 
			(key != null && key.equals(other.key)) &&
			(value != null && value.equals(other.value)) &&
			(fileName != null && fileName.equals(other.fileName))) {
			return true;
		}
		return false;
	}
	
	public boolean equals(String key) {
		return this.key != null && this.key.equals(key);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (lineNumber == null ? 0: lineNumber.hashCode());
		return result;
	}
	
}
