package space.itoncek.spc3.generics;

import java.io.Closeable;

public interface Manager extends Closeable {
	void registerPaths();
}
