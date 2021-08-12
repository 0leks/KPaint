package ok.kpaint.history;

import java.util.*;
import java.util.concurrent.*;

public class History {
	private static BlockingDeque<Edit> newEdits = new LinkedBlockingDeque<>();
	private static LinkedList<Edit> edits = new LinkedList<>();
	private volatile static int stackPointer = 0;
	
	static {
		Thread newEditParser = new Thread(() -> {
			try {
				while(true) {
					Edit edit = newEdits.pollFirst(Long.MAX_VALUE, TimeUnit.DAYS);
					synchronized(edits) {
						while(stackPointer < edits.size()) {
							edits.removeLast();
						}
						edits.addLast(edit);
						stackPointer++;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		newEditParser.start();
	}
	
	public synchronized static void push(Edit edit) {
		newEdits.addLast(edit);
	}
	public static void undo() {
		synchronized(edits) {
			if(stackPointer <= 0) {
				return;
			}
			stackPointer--;
			Edit toapply = edits.get(stackPointer);
			edits.set(stackPointer, toapply.getInverse());
			toapply.apply();
		}
	}
	public static void redo() {
		synchronized(edits) {
			if(stackPointer >= edits.size()) {
				return;
			}
			Edit toapply = edits.get(stackPointer);
			edits.set(stackPointer, toapply.getInverse());
			toapply.apply();
			stackPointer++;
		}
	}
}
