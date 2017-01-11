package ua.in.smartdev.incrementalbuild.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.functions.Action2;
import rx.functions.Func0;

public class ListCollector {

		
	public static <T> Func0<List<T>> asArrayList() {
		return new Func0<List<T>>() {

			@Override
			public List<T> call() {
				return new ArrayList<T>();
			}
			
		};
	}
	
	public static <T> Func0<List<T>> toNewArrayList(final int capacity) {
		return new Func0<List<T>>() {

			@Override
			public List<T> call() {
				return new ArrayList<T>();
			}
			
		};
	}
	
	public static <T> Action2<List<T>, T> addAll() {
		return null;
	}
}
