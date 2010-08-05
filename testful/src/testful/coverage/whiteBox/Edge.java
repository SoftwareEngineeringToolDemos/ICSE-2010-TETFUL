/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.coverage.whiteBox;

import java.io.Serializable;

public abstract class Edge implements Serializable {

	private static final long serialVersionUID = 5704806706857061576L;
	protected final Block from;
	protected Block to;

	public Edge(Block from) {
		this.from = from;
		from.post.add(this);
	}

	public void setTo(Block to) {
		this.to = to;
		to.pre.add(this);
	}

	public Block getFrom() {
		return from;
	}

	public Block getTo() {
		return to;
	}
}
