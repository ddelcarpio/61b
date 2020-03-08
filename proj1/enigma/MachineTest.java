package enigma;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Machine class.
 *  @author Daniel del Carpio
 */

public class MachineTest {
    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);
    /* ***** TESTING UTILITIES ***** */
    private Machine machine;

    /** Creates a default machine */
    public MachineTest() {
        ArrayList<Rotor> rotors;
        rotors = new ArrayList<>();
        rotors.add(new MovingRotor("I",
                new Permutation(NAVALA.get("I"), UPPER), "Q"));
        rotors.add(new MovingRotor("II",
                new Permutation(NAVALA.get("II"), UPPER), "E"));
        rotors.add(new MovingRotor("III",
                new Permutation(NAVALA.get("III"), UPPER), "V"));
        rotors.add(new MovingRotor("IV",
                new Permutation(NAVALA.get("IV"), UPPER), "J"));
        rotors.add(new MovingRotor("V",
                new Permutation(NAVALA.get("V"), UPPER), "Z"));
        rotors.add(new MovingRotor("VI",
                new Permutation(NAVALA.get("VI"), UPPER), "ZM"));
        rotors.add(new MovingRotor("VII",
                new Permutation(NAVALA.get("VII"), UPPER), "ZM"));
        rotors.add(new MovingRotor("VIII",
                new Permutation(NAVALA.get("VIII"), UPPER), "ZM"));
        rotors.add(new FixedRotor("Beta",
                new Permutation(NAVALA.get("Beta"), UPPER)));
        rotors.add(new FixedRotor("Gamma",
                new Permutation(NAVALA.get("Gamma"), UPPER)));
        rotors.add(new Reflector("B",
                new Permutation(NAVALA.get("B"), UPPER)));
        rotors.add(new Reflector("C",
                new Permutation(NAVALA.get("C"), UPPER)));

        machine = new Machine(UPPER, 5, 3, rotors);

    }


    /* ***** TESTS ***** */

    @Test
    public void checkVar() {
        MachineTest test1 = new MachineTest();
        Machine m = test1.machine;
        assertEquals(5, machine.numRotors());
        assertEquals(3, machine.numPawls());
    }

    @Test
    public void checkExample() {
        MachineTest t1 = new MachineTest();
        Machine navy = t1.machine;
        navy.insertRotors(new String[] {"B", "Beta", "III", "IV", "I"});
        navy.setRotors("AXLE");
        navy.setPlugboard(new Permutation("(YF) (ZH)", UPPER));
        assertEquals(25, navy.convert(24));

        navy.setRotors("AXLE");
        assertEquals("Z", navy.convert("Y"));

    }

}