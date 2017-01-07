import static org.junit.Assert.*;

import org.junit.Test;

public class FooTest {

	@Test
	public void foo() {
		assertEquals("buz", new Foo().bar("buz"));
	}
}
