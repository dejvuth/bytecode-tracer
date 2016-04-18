package test.bt;

import org.objectweb.asm.Type;


public class TestUtils {
	
	static boolean isPrint = true;
	
	public static String getLabelName(Class<?> c, String mname, Class<?>[] params) 
			throws NoSuchMethodException, SecurityException {
		return Type.getInternalName(c)
				+ "." + mname 
				+ Type.getMethodDescriptor(c.getMethod(mname, params));
	}

	public static void print(String[] is, String lname) {
		if (!isPrint)
			return;
		
		for (int i = 0; i < is.length; i++) {
			System.out.print("\"" + is[i].replace(lname, "\" + lname + \"") + "\"");

			if (i < is.length - 1)
				System.out.println(",");
			else
				System.out.println();
		}
	}
}
