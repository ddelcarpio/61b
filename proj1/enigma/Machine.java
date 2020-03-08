package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;


/** Class that represents a complete enigma machine.
 *  @author Daniel del Carpio
 */
class Machine {

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors used in setting line. */
    private int _numRotors;

    /** Number of pawls used in setting line. */
    private int _pawls;

    /** ArrayList of rotors in config file. */
    private ArrayList<Rotor> _allRotors;

    /** Current array of rotors used in setting line. */
    private Rotor[] _currentRotors;

    /** _plugboard permutation used. */
    private Permutation _plugBoard;


    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = new ArrayList<Rotor>(allRotors);
        _plugBoard = null;
    }
    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        int rotorcheck = 0;

        for (int i = 0; i < rotors.length; i += 1) {
            boolean inserted = false;
            for (int j = 0; j < rotorcheck; j += 1) {
                if (rotors[i].equals(_currentRotors[j].name())) {
                    throw new EnigmaException("Using more than 1 "
                            + _currentRotors[j]);
                }
            }
        }
        for (int i = 0; i < rotors.length; i += 1) {
            for (Rotor checkName : _allRotors) {
                String rotorName = rotors[i].toUpperCase();
                String check = checkName.name().toUpperCase();
                if (rotorName.equals(check)) {
                    rotorcheck += 1;
                }
            }
        }
        if (rotorcheck != rotors.length) {
            throw new EnigmaException("Rotors Not Named Correctly.");
        }


        String reflector = rotors[0];
        Rotor nullRotor = null;
        for (Rotor rotor : _allRotors) {
            if (rotor.name().equals(reflector)) {
                nullRotor = rotor;
            }
        }
        if (!nullRotor.reflecting()) {
            throw new EnigmaException("The first rotor is not a reflector"
                    + ", and it must be.");
        }
        _currentRotors = new Rotor[rotors.length];
        for (int i = 0; i < rotors.length; i += 1) {
            for (Rotor r : _allRotors) {
                String temp = rotors[i].toUpperCase();
                String temp2 = r.name().toUpperCase();
                if (temp.equals(temp2)) {
                    _currentRotors[i] = r;
                }
            }
        }

        int count = 0;
        for (int i = 0; i < _currentRotors.length; i += 1) {
            if (_currentRotors[i].rotates()) {
                count += 1;
            }
        }
        if (count != numPawls()) {
            throw new EnigmaException("Wrong number of arguments");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        Rotor rotor;
        int length = setting.length();
        int before = numRotors() - 1;
        for (int i = before; i >= 1; i -= 1){
            rotor = _currentRotors[i];
            char temp = setting.charAt(length - 1);
            rotor.set(temp);
            length -= 1;
        }
    }


    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugBoard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        _currentRotors[_currentRotors.length - 1].advance();
        int index = _plugBoard.permute(c);
        index = _currentRotors[_currentRotors.length - 1].convertForward(index);
        boolean empty = false;

        for (int i = _currentRotors.length - 2; i >= 0; i -= 1) {
            Rotor r = _currentRotors[i];
            int count = 0;
            if (_currentRotors[i + 1].atNotch()) {
                if (i + 1 == numRotors() - 1) {
                    r.advance();
                    if (_currentRotors[i].atNotch()) {
                        count += 1;
                        _currentRotors[i - 1].advance();
                    }
                    count += 1;
                }
            }
            int prev = numRotors() - 1;
            if (((i) != prev)) {
                if (r.rotates()) {
                    int[] arr = r.getNotchesArr();
                    for (int k = 0; k < arr.length; k++) {
                        int cur = arr[k];
                        int wrapped = r.permutation().wrap((r.setting()));
                        if ('A' + wrapped == cur) {
                            empty = true;
                        }
                    }
                    if (empty) {
                        if (count == 0 && _currentRotors[i - 1].rotates()) {
                            r.advance();
                            _currentRotors[i - 1].advance();
                            index = r.convertForward(index);
                            index = _currentRotors[i - 1].convertForward(index);
                            count += 1;
                            i -= 1;
                        }
                    } else if (empty) {
                        index = r.convertForward(index);
                    } else {
                        count++;
                        index = r.convertForward(index);
                    }
                }
            }
            if (!empty && !r.atNotch() && i == 3 && count == 0)  {
                index = r.convertForward(index);
            } else if ((!r.rotates()) || i == 0) {
                index = r.convertForward(index);
            }
        }

        for (int i = 1; i < _currentRotors.length; i++) {
            Rotor r = _currentRotors[i];
            index = r.convertBackward(index);
        }
        return _plugBoard.permute(index);
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        int conv;
        char[] imsg = msg.toCharArray();
        String result = "";
        for (int i = 0; i < imsg.length; i++) {
            if (imsg[i] == ' ') {
                result += " ";
                continue;
            }
            if (!(_alphabet.contains(imsg[i]))) {
                continue;
            }
            conv = _alphabet.toInt(imsg[i]);
            imsg[i] = _alphabet.toChar((convert(conv)));
            result += String.valueOf(imsg[i]);
        }
        return result;

    }
}
