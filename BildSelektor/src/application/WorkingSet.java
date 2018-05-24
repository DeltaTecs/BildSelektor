package application;

import java.util.ArrayList;
import java.util.Collection;

import javafx.scene.image.Image;

public class WorkingSet {

	private ArrayList<Image> base = new ArrayList<Image>();
	private ArrayList<Image> trash = new ArrayList<Image>();
	private ArrayList<Image> copy = new ArrayList<Image>();
	private ArrayList<Image> base_keep = new ArrayList<Image>();


	private boolean first = false;
	private int startSize = 0;
	private WorkingSetInfo info = null;

	public WorkingSet(Collection<Image> base, Collection<Image> trash, Collection<Image> copy, boolean first) {
		super();
		this.first = first;
		if (base != null)
			this.base.addAll(base);
		if (trash != null)
			this.trash.addAll(trash);
		if (copy != null)
			this.copy.addAll(copy);
		if (first)
			startSize = base.size();
	}
	
	public void stamp() {
//		info = WorkingSetInfo.gen(base.get(0));
	}
	

	public ArrayList<Image> getBase() {
		return base;
	}

	public ArrayList<Image> getTrash() {
		return trash;
	}

	public ArrayList<Image> getCopy() {
		return copy;
	}

	public boolean isFirst() {
		return first;
	}

	public int getStartSize() {
		return startSize;
	}

	public ArrayList<Image> getBase_keep() {
		return base_keep;
	}
	
	public WorkingSetInfo getInfo() {
		return info;
	}
	
}
