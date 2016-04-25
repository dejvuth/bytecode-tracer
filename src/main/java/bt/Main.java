package bt;

import java.util.ArrayList;
import java.util.List;

public class Main {
	
	private static Class<?> getClass(char desc) throws ClassNotFoundException {
		switch (desc) {
		case 'V':
            return void.class;
        case 'Z':
            return boolean.class;
        case 'C':
            return char.class;
        case 'B':
            return byte.class;
        case 'S':
            return short.class;
        case 'I':
            return int.class;
        case 'F':
            return float.class;
        case 'J':
            return long.class;
        case 'D':
            return double.class;
		}
		throw new ClassNotFoundException(Character.toString(desc));
	}
	
	private static Class<?> getClass(String desc) throws ClassNotFoundException {
		if (desc.length() == 1)
			return getClass(desc.charAt(0));
		if (desc.startsWith("L") && desc.endsWith(";"))
			return Class.forName(desc.substring(1, desc.length()-1).replace('/', '.'));

		// Array e.g. [I or [[java/lang/Integer;
		return Class.forName(desc.replace('/', '.'));
	}
	
	private static Class<?>[] getArgumentClasses(String methodDescriptor) throws ClassNotFoundException {
		if (methodDescriptor == null)
			return null;
		
		if (!methodDescriptor.startsWith("(") || methodDescriptor.indexOf(')') < 0)
			throw new ClassNotFoundException(methodDescriptor);

		String params = methodDescriptor.substring(1, methodDescriptor.indexOf(')'));
		List<Class<?>> list = new ArrayList<>();
		for (int i = 0; i < params.length(); i++) {
			char c = params.charAt(i);
			if (c == 'L') {
				// Example: Ljava/lang/Integer;
				int j = i+1;
				while (params.charAt(j) != ';')
					j++;
				list.add(getClass(params.substring(i, j+1)));
				i = j;
			} else if (c == '[') {
				// Examples: [[I or [Ljava/lang/Integer;
				int j = i+1;
				while (params.charAt(j) == '[')
					j++;
				if (params.charAt(j) == 'L') {
					while (params.charAt(j) != ';')
						j++;
					list.add(getClass(params.substring(i, j+1)));
				} else {
					list.add(getClass(params.substring(i, j+1)));
				}
				i = j;
			} else {
				list.add(getClass(c));
			}
		}
		return list.toArray(new Class<?>[list.size()]);
	}
	
	private static Object cast(Class<?> c, String s) {
		if (c == boolean.class)
			return Boolean.parseBoolean(s);
		if (c == char.class)
			return s.charAt(0);
		if (c == byte.class)
			return Byte.parseByte(s);
		if (c == short.class)
			return Short.parseShort(s);
		if (c == int.class)
			return Integer.parseInt(s);
		if (c == float.class)
			return Float.parseFloat(s);
		if (c == long.class)
			return Long.parseLong(s);
		if (c == double.class)
			return Double.parseDouble(s);
		throw new UnsupportedOperationException(c.toString());
	}
	
	private static void printUsage() {
		System.out.println("Invalid arguments.");
		System.out.println("Expected: package.className methodName [methodDescriptor arg1 arg2 ...]");
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			printUsage();
			return;
		}
		
		Tracer tracer = new Tracer(args[0]);
		String[] is;
		if (args.length == 2) {
			is = tracer.run(args[1]);
		} else {
			// Finds parameter classes from the method descriptor
			Class<?>[] paramClasses = getArgumentClasses(args[2]);
			if (paramClasses.length != args.length - 3) {
				printUsage();
				return;
			}

			Object[] targs = new Object[paramClasses.length];
			for (int i = 0; i < paramClasses.length; i++) {
				targs[i] = cast(paramClasses[i], args[3+i]);
			}
			is = tracer.run(args[1], getArgumentClasses(args[2]), targs);
		}
		for (String s : is) {
			System.out.println(s);
		}
	}
}
