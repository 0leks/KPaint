package ok.kpaint.history;

import ok.kpaint.gui.layers.*;

public class Edit implements ApplyEdit, GetInverse {
	private ApplyEdit apply;
	private GetInverse getInverse;
	public Edit(ApplyEdit apply, GetInverse getInverse) {
		this.apply = apply;
		this.getInverse = getInverse;
	}
	
	@Override
	public void apply() {
		apply.apply();
	}
	@Override
	public Edit getInverse() {
		return getInverse.getInverse();
	}
	
}
