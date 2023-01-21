package com.example.iType;

public class Word {
	public String word;
	int frequency;

	public Word(String word) {
		this.word = word;
		this.frequency = 1;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	@Override
	public boolean equals(Object word) {
		if (((Word) this).word.equals(((Word) word).word)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return this.word;
	}

}
