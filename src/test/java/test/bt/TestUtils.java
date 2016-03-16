package test.bt;

public class TestUtils {

	public static void print(String[] is, String lname) {
		for (int i = 0; i < is.length; i++) {
			System.out.print("\"" + is[i].replace(lname, "\" + lname + \"") + "\"");

			if (i < is.length - 1)
				System.out.println(",");
			else
				System.out.println();
		}
	}
}
