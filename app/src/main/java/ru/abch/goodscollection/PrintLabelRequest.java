package ru.abch.goodscollection;

public class PrintLabelRequest {
	GoodsLabel[] labels;
	public int counter;
	public PrintLabelRequest(int counter, GoodsLabel[] labels) {
		this.counter = counter;
		this.labels = labels;
	}
}
