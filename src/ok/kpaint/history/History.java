package ok.kpaint.history;

import java.awt.image.*;
import java.util.*;

import ok.kpaint.*;

public class History {
	
	private static LinkedList<Edit> edits = new LinkedList<>();
	private static int stackPointer = 0;
	
	public static void push(Edit edit) {
		while(stackPointer < edits.size()) {
			edits.removeLast();
		}
		edits.addLast(edit);
		stackPointer++;
	}
	public static void undo() {
		if(stackPointer <= 0) {
			return;
		}
		stackPointer--;
		Edit toapply = edits.get(stackPointer);
		edits.set(stackPointer, toapply.getInverse());
		toapply.apply();
	}
	public static void redo() {
		if(stackPointer >= edits.size()) {
			return;
		}
		Edit toapply = edits.get(stackPointer);
		edits.set(stackPointer, toapply.getInverse());
		toapply.apply();
		stackPointer++;
	}
}
