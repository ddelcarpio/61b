package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotor that has no ratchet and does not advance.
 *  @author Daniel Del Carpio
 */
class FixedRotor extends Rotor {

    /** A non-moving rotor named NAME whose permutation at the 0 setting
     * is given by PERM. */
    FixedRotor(String name, Permutation perm) {
        super(name, perm);
    }

    /** Return false if a fixed rotor does not have a ratchet
     *  and can't move. */
    @Override
    boolean rotates() {
        return false;
    }

    /** Return true if a fixed rotor can reflect. */
    @Override
    boolean reflecting() {
        return true;
    }
}
