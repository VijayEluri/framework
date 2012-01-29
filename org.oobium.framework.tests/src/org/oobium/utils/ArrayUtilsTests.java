package org.oobium.utils;

import static org.junit.Assert.*;
import static org.oobium.utils.ArrayUtils.*;

import org.junit.Test;

public class ArrayUtilsTests {

	@Test
	public void testReverseArray() throws Exception {
		assertArrayEquals(new Integer[] { 3, 2, 1}, reverse(new Integer[] { 1, 2, 3 }));
	}
	
}
