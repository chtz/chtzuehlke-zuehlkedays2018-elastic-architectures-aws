package com.zuehlke.poc.aws.txt2speech;

public class Text2SpechRequest { 
	private String id;
	private String text;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "Text2SpechRequest [id=" + id + ", text=" + text + "]";
	}
}
