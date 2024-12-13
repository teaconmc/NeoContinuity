package me.pepperbell.ctm.api.client;

public interface ProcessingDataProvider {
	<T> T getData(ProcessingDataKey<T> key);
}
