/*
 * Aprof - Java Memory Allocation Profiler
 * Copyright (C) 2002-2013  Devexperts LLC
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

package com.devexperts.aproftest;

import java.io.*;
import java.util.ArrayList;

import com.devexperts.aprof.AProfSizeUtil;
import com.devexperts.aprof.Configuration;

import static com.devexperts.aproftest.TestUtil.fmt;

/**
 * @author Dmitry Paraschenko
 */
class DeserializationTest implements TestCase {
	private static final int READS = 10;
	private static final int LIST_SIZE = 100000;
	private static final int COUNT = READS * LIST_SIZE;

	private final byte[] serializedData;

	public DeserializationTest() {
		ArrayList<Entity> serializedObject = new ArrayList<Entity>();
		for (int i = 0; i < LIST_SIZE; i++)
			serializedObject.add(new Entity(i));
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(serializedObject);
			serializedData = baos.toByteArray();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	public String name() {
		return "deserialization";
	}

	public String verifyConfiguration(Configuration configuration) {
		return null;
	}

	public String[] getCheckedClasses() {
		return new String[] {getClass().getName() + "$"};
	}

	public String getExpectedStatistics() {
		int objSize = AProfSizeUtil.getObjectSize(new Entity(0)) << AProfSizeUtil.SIZE_SHIFT;
		return fmt(
			"Allocated {size} bytes in {count} objects in 1 locations of 1 classes\n" +
			"-------------------------------------------------------------------------------\n" +
			"{class}$Entity: {size} (_%) bytes in {count} (_%) objects (avg size {objSize} bytes)\n" +
			"\tsun.reflect.GeneratedSerializationConstructorAccessor.newInstance: {size} (_%) bytes in {count} (_%) objects\n" +
			"\t\tjava.io.ObjectInputStream.readObject: {size} (_%) bytes in {count} (_%) objects\n" +
			"\t\t\t{class}.doTest: {size} (_%) bytes in {count} (_%) objects\n",
			"class=" + getClass().getName(),
			"size=" + fmt(objSize * COUNT),
			"count=" + fmt(COUNT),
			"objSize=" + objSize);
	}

	public void doTest() throws Exception {
		for (int i = 0; i < READS; i++) {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializedData));
			ois.readObject();
			ois.close();
		}
	}

	private static class Entity implements Serializable {
		private final int value;

		private Entity(int value) {
			this.value = value;
		}
	}
}
