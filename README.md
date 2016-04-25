# bytecode-tracer

A library for tracing executed bytecode instructions.

**Usage:** Simply instantiate `Tracer` with a class name
and call `run` with a method name, parameter classes, and argument values.

For example, to trace the following method:

```
package test.bt;

public class Demo {
	public static int[] createIntArray(int size) {
		if (size < 0)
			throw new IllegalArgumentException();
		return new int[size];
	}
}
```

with the argument `4`:

```
Tracer tracer = new Tracer("test.bt.Demo");
String[] is = tracer.run("createIntArray", new Class<?>[] { int.class }, 4);
```

The returned array of strings is the list of executed bytecode instructions:

```
LABEL test/bt/DemoTest.createIntArray(I)[I0
ILOAD 0
IFGE test/bt/DemoTest.createIntArray(I)[I2 test/bt/DemoTest.createIntArray(I)[I1
LABEL test/bt/DemoTest.createIntArray(I)[I2
ILOAD 0
NEWARRAY 10
ARETURN
```

Alternatively, the library also comes with a command-line tool.
Compiling with `gradle fatJar` will create `bytecode-tracer-all-0.1.jar`
which can be used as follows:

```
java -cp .:bytecode-tracer-all-0.1.jar bt.Main "test.bt.DemoTest" "createIntArray" "(I)[I" "4"
```

to print out the above list of bytecode instructions.

The library uses [ASM](http://asm.ow2.org/) to inject and trace executions.
As a result, it also returns non-standard bytecode instructions from ASM
such as `LABEL` in the above example.
