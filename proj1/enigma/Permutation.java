package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Daniel Del Carpio
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles;
        int open = 0;
        int closed = 0;
        for (int i = 0; i < _cycles.length(); i += 1) {
            if (_cycles.charAt(i) == '(') {
                open += 1;
            }
            if (_cycles.charAt(i) == ')') {
                closed += 1;
            }
        }
        if (open != closed) {
            throw new EnigmaException("Wrong Number of setting arguments.");
        }

        permutations = new int[size()];
        int traverse = 0;
        for (int i = 0; i < _cycles.length(); i++) {
            if ((int) _cycles.charAt(i) >= 'A') {
                permutations[traverse] = (int) _cycles.charAt(i);
                traverse += 1;
            }
        }

    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    void addCycle(String cycle) {
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        if (_cycles.length() == 0) {
            return p;
        }

        int val = wrap(p) + 'A';
        char charval = (char) val;
        char out = permute(charval);
        return (int) out - 'A';
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        if (_cycles.length() == 0) {
            return c;
        }
        int val = wrap(c) + 'A';
        char temp = (char) val;
        int temp2 = invert(temp);
        return temp2 - 'A';
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (_cycles.length() == 0) {
            return p;
        }

        for (int i = 0; i < permutations.length; i++) {
            if ((char) permutations[i] == p) {
                int stringIndex = _cycles.indexOf(p);
                if (_cycles.charAt(stringIndex + 1) == ')') {
                    int start = stringIndex;
                    char startChar = _cycles.charAt(stringIndex);
                    while (startChar != '(') {
                        start = start - 1;
                        startChar = _cycles.charAt(start);
                    }
                    return _cycles.charAt(start + 1);
                }
                return (char) permutations[i + 1];
            }
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (_cycles.length() == 0) {
            return c;
        }

        for (int i = 0; i < permutations.length; i++) {
            if ((char) permutations[i] == c) {
                int stringIndex = _cycles.indexOf(c);
                if (_cycles.charAt(stringIndex - 1) == '(') {
                    int start = stringIndex;
                    char first = _cycles.charAt(start);
                    while (first != ')') {
                        start = start + 1;
                        first = _cycles.charAt(start);
                    }
                    return _cycles.charAt(start - 1);
                }
                return (char) permutations[i - 1];
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** String of cycles as stated in config file. */
    private String _cycles;

    /** Return the permutations array used to initialize this Permutation. */
    private int[] permutations;
}
