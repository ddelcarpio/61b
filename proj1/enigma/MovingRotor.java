package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Daniel Del Carpio
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */

    /** Permutation of rotor. */
    private Permutation _perm;

    /** My number of notches. */
    private String _notches;

    /** Moving Rotor Class.
     *
     * @param name name of Rotor
     * @param perm permutation of Rotor
     * @param notches of Rotor */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;

    }

    /** Returns ASCII version of characters in notches. */
    int[] getNotchesArr() {
        int[] temp = new int[_notches.length()];
        for (int i = 0; i < _notches.length(); i += 1) {
            temp[i] = (_notches.charAt(i));
        }
        return temp;
    }

    @Override
    boolean atNotch() {
        for (int i = 0; i < _notches.length(); i += 1) {
            int current = _notches.charAt(i);
            if (('A' + permutation().wrap((setting() - 1))) == current) {
                return true;
            }
        }
        return false;
    }

    @Override
    void advance() {
        set(permutation().wrap(setting() + 1));
    }

    @Override
    boolean rotates() {
        return true;
    }





}
