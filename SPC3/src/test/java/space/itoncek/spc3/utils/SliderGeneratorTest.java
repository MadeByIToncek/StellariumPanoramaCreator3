package space.itoncek.spc3.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import space.itoncek.spc3.database.StartEndTransition;
import space.itoncek.spc3.database.Target;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

class SliderGeneratorTest {
	@Test
	void generateScript() {
		Target start = Target.generateObjectTarget("Mars", LocalDate.now(), LocalTime.now());
		Target end = Target.generateAzAltTarget(180,30, LocalDate.now(), LocalTime.now());
		StartEndTransition set = StartEndTransition.createTransition(start,end);
		set.setUuid(UUID.randomUUID());

		String s = SliderGenerator.generateScript(set,true);
		System.out.println(s);
		assertTrue(true);
	}
}