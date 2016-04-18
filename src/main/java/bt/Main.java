package bt;

public class Main {
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Invalid arguments. Expected: package.className methodName");
			return;
		}
		
		Tracer tracer = new Tracer(args[0]);
		String[] is = tracer.run(args[1]);
		for (String s : is) {
			System.out.println(s);
		}
	}
}
