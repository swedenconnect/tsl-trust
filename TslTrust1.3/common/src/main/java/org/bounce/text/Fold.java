/*
 * Copyright 2017 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bounce.text;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Element;

/**
 * Representation of a Folded area.
 *
 * @author Edwin Dankert <edankert@gmail.com>
 * @version $Revision: 1.4 $, $Date: 2008/04/16 19:36:18 $
 */
class Fold {
	static final String FOLD_LIST_ATTRIBUTE = "org.bounce.text.FoldList";
	static final String FOLDS_UPDATED_ATTRIBUTE = "org.bounce.text.FoldsUpdated";
	
	private List<Fold> children = null;
	private int fixedStart = -1;
	private Element start;
	private Element end;

	private int range = -1;

	public Fold(Element start, Element end) {
		this.start = start;
		this.end = end;

		range = getEnd() - getStart();

		children = new ArrayList<Fold>();
	}

	/**
	 * @return the starting line of the fold.
	 */
	public int getStart() {
		if (fixedStart == -1) {
			return getRealStart();
		}

		return fixedStart;
	}

	/**
	 * @return the ending line of the fold.
	 */
	public int getEnd() {
		if (range == -1) {
			return getRealEnd();
		}

		return getStart() + range;
	}

	private int getRealStart() {
		fixedStart = start.getParentElement().getElementIndex(start.getStartOffset());
		return fixedStart;
	}

	private int getRealEnd() {
		return end.getParentElement().getElementIndex(end.getStartOffset());
	}

	public boolean contains(int index) {
		if (index > getStart() && index < getEnd()) {
			return true;
		}

		return false;
	}

	public void add(Fold fold) {
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).contains(fold.getStart())) {
				children.get(i).add(fold);
				return;
			}
		}

		children.add(fold);
	}

	public void remove(int start, int end) {
		List<Fold> temp = new ArrayList<Fold>(children);
		for (int i = 0; i < temp.size(); i++) {
			Fold f = (Fold) temp.get(i);

			if (f.contains(start) || f.contains(end)) {
				f.remove(start, end);
				children.remove(f);

				for (Fold child : f.getChildren()) {
					children.add(child);
				}

				f.shallowCleanup();
			}
		}
	}

	public List<Fold> getChildren() {
		return children;
	}

	public void update() {
		List<Fold> folds = new ArrayList<Fold>(children);
		for (int i = 0; i < folds.size(); i++) {
			Fold fold = (Fold) folds.get(i);

			if (!fold.isValid()) {
				fold.update();
				children.remove(fold);

				List<Fold> fs = fold.getChildren();
				for (int j = 0; j < fs.size(); j++) {
					children.add(fs.get(j));
				}
			}
		}
	}

	public boolean isValid() {
		return (range == (getRealEnd() - getRealStart()));
	}

	public void shallowCleanup() {
		children.clear();
		start = null;
		end = null;
	}

	public void cleanup() {
		for (int i = 0; i < children.size(); i++) {
			Fold child = children.get(i);
			child.cleanup();
		}

		shallowCleanup();
	}
}
