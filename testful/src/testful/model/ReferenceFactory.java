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

package testful.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import testful.utils.JavaUtils;
import ec.util.MersenneTwisterFast;

public class ReferenceFactory implements Serializable {

	private static final long serialVersionUID = 4422953344530793154L;

	private final Map<Clazz, Integer> refNum;
	private transient Map<Clazz, Reference[]> refMap;
	private transient Reference[] all;

	public ReferenceFactory(TestCluster cluster, int cutSize, int auxSize) {
		Set<Clazz> refClasses = new TreeSet<Clazz>();
		for(Clazz c : cluster.getCluster())
			refClasses.add(c.getReferenceClazz());

		refNum = new TreeMap<Clazz, Integer>();
		for(Clazz c : refClasses)
			refNum.put(c, c == cluster.getCut() ? cutSize : auxSize);

		createRefMap();
	}

	/**
	 * Create a reference factory with map.get(clazz) entries for each clazz used as key
	 * @param map indicates how many references create for each clazz
	 */
	public ReferenceFactory(Map<Clazz, Integer> map) {
		refNum = map;
		createRefMap();
	}

	private void createRefMap() {

		int idGenerator = 0;
		refMap = new HashMap<Clazz, Reference[]>();
		for(Entry<Clazz, Integer> e : refNum.entrySet()) {
			Reference[] refs = new Reference[e.getValue()];

			for(int i = 0; i < refs.length; i++)
				refs[i] = new Reference(e.getKey(), i, idGenerator++);

			refMap.put(e.getKey(), refs);
		}
	}

	/**
	 * Returns, for each type, the number of references
	 * @return the refNum the map between the type and the number of references
	 */
	public Map<Clazz, Integer> getRefNum() {
		return refNum;
	}

	/**
	 * Returns the set of references (not ordered)
	 * @return an unordered set of references
	 */
	public Reference[] getReferences() {
		if(all == null) {
			if(refMap == null) createRefMap();

			int num = 0;
			for(Reference[] refs : refMap.values())
				num += refs.length;

			int i = 0;
			all = new Reference[num];
			for(Reference[] refs : refMap.values())
				for(Reference ref : refs)
					all[i++] = ref;

			Arrays.sort(all);
		}

		return all;
	}

	public Reference[] getReferences(Clazz c) {
		if(refMap == null) createRefMap();

		return refMap.get(c.getReferenceClazz());
	}

	public Reference getReference(Clazz c, MersenneTwisterFast random) {
		if(refMap == null) createRefMap();

		Reference refs[] = refMap.get(c.getReferenceClazz());

		if(refs == null) return null;

		return refs[random.nextInt(refs.length)];
	}

	@Override
	public int hashCode() {
		return 31 + Arrays.hashCode(getReferences());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof ReferenceFactory)) return false;
		ReferenceFactory other = (ReferenceFactory) obj;

		return JavaUtils.similar(getReferences(), other.getReferences());
	}

	/**
	 * Converts a reference of another instance of a ReferenceFatory into a
	 * reference of this instance of the ReferenceFactory
	 *
	 * @param ref a reference
	 * @return a reference of this instance of the ReferenceFactory
	 */
	public Reference adapt(Reference ref) {
		if(ref == null) return null;
		if(refMap == null) createRefMap();

		return refMap.get(ref.getClazz())[ref.getPos()];
	}

	/**
	 * Converts an array of references of another instance of a ReferenceFatory
	 * into an array of references of this instance of the ReferenceFactory
	 *
	 * @param array an array of references
	 * @return an array of references of this instance of the ReferenceFactory
	 */
	public Reference[] adapt(Reference[] array) {
		Reference[] ret = new Reference[array.length];

		for(int i = 0; i < array.length; i++)
			ret[i] = adapt(array[i]);

		return ret;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		final Reference[] all = getReferences();
		for(int i = 0; i < all.length; i++)
			sb.append(" ").append(i).append(": ").append(all[i].toString()).append("\n");

		return sb.toString();
	}
}
