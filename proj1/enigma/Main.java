package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Daniel del Carpio
 */
public final class Main {

    /** Machine. */
    private Machine machine;

    /** Number of Rotors. */
    private int _numRotors;

    /** Number of Pawls. */
    private int _numPawls;

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;


    /** All rotors from which rotors may be inserted into the machine. */
    private ArrayList<Rotor> _allRotors = new ArrayList<>();

    /** Keeps track of which rotor was added prior when adding rotors. */
    private int _previousAddedRotor = 0;

    /** Accumulation of all rotors to be used from config file. */
    private ArrayList<Rotor> _allRots;


    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }
        _config = getInput(args[0]);
        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {

        Machine configMach = readConfig();
        while (_input.hasNextLine()) {
            String setting = _input.nextLine();
            String[] strArr = setting.split(" ");

            if (!strArr[0].equals("*") && _input.hasNext()) {
                setting = _input.nextLine();
                _output.println();
            } else if (!strArr[0].equals("*")) {
                throw new EnigmaException("First line must be settings line.");
            }

            setUp(configMach, setting);

            String mssg = "";
            while (!_input.hasNext("\\*.*") && _input.hasNextLine()) {
                if (_input.hasNext("\\*.*")) {
                    throw new EnigmaException("Improper Format for setting.");
                }

                mssg = _input.nextLine();
                mssg = mssg.toUpperCase();
                String[] mssgArr = mssg.split("\\ ");
                String[] convertedMsgArr = new String[mssgArr.length];
                for (int i = 0; i < mssgArr.length; i++) {
                    convertedMsgArr[i] = configMach.convert(mssgArr[i]);
                }
                String output = "";
                for (String str : convertedMsgArr) {
                    output = (output + str);
                }
                output = output.trim();
                printMessageLine(output);
            }
        }

    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String next = _config.next();
            if (next.length() == 0) {
                throw new EnigmaException("Specify # rotor slots, # pawls");
            }
            _alphabet = new Alphabet();
            int rotorSlots = _config.nextInt();
            int numPawls = _config.nextInt();
            _allRots = new ArrayList<>();

            while (_config.hasNext()) {
                String name = _config.next();
                String typeAndNotches = _config.next();
                char rotorType = typeAndNotches.charAt(0);
                int typeLen = typeAndNotches.length();
                String typeNotch = typeAndNotches.substring(1, typeLen);
                String cycleBuildUp = "";
                boolean check = true;
                String x = "";
                while (check) {
                    x = _config.next();
                    if (!_config.hasNext("\\(.*")) {
                        check = false;
                    }
                    if (x.charAt(0) == ')') {
                        cycleBuildUp += x;
                    }
                    if (x.charAt(0) == '(') {
                        cycleBuildUp += x;
                    }
                }
                Permutation currPerm = new Permutation(cycleBuildUp, _alphabet);
                Rotor nextRotor = null;

                if (rotorType == 'N') {
                    nextRotor = new FixedRotor(name, currPerm);
                } else if (rotorType == 'R') {
                    nextRotor = new Reflector(name, currPerm);
                } else {
                    nextRotor = new MovingRotor(name, currPerm, typeNotch);
                }
                _allRots.add(nextRotor);
            }
            return new Machine(_alphabet, rotorSlots, numPawls, _allRots);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }


    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next().toUpperCase().trim();
            if (name.charAt(0) == '(') {
                String rest = _config.nextLine().trim();
                char cycleLast = name.charAt(name.length() - 1);
                if (cycleLast != ')') {
                    throw error("cycles must end in parenthesis");
                }
                Scanner restChecker = new Scanner(rest);
                while (restChecker.hasNext()) {
                    String scan = restChecker.next();
                    if (scan.charAt(scan.length() - 1) != ')') {
                        throw error("cycles must end in parenthesis");
                    }
                }
                Rotor rotorBefore = _allRotors.get(_previousAddedRotor - 1);
                rotorBefore.permutation().addCycle(name);
                rotorBefore.permutation().addCycle(rest);
                if (_config.hasNextLine()) {
                    return readRotor();
                }
                if (!_config.hasNextLine()) {
                    return new Reflector("tmp", new Permutation("", _alphabet));
                }
            }
            String typeNotch = _config.next().trim();
            if (!(typeNotch.charAt(0) == 'M'
                    || typeNotch.charAt(0) == 'N'
                    || typeNotch.charAt(0) == 'R')) {
                throw error("Wrong rotor types specified");
            }
            String cycles = _config.nextLine().trim();
            if (cycles.charAt(cycles.length() - 1) != ')') {
                throw error("cycles must end in parenthesis");
            }
            Permutation perm = new Permutation(cycles, _alphabet);
            if (typeNotch.charAt(0) == 'M') {
                String notches = typeNotch.substring(1);
                return new MovingRotor(name, perm, notches);
            } else if (typeNotch.charAt(0) == 'N') {
                return new FixedRotor(name, perm);
            }
            return new Reflector(name, perm);

        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        settings = settings.toUpperCase().replaceAll("\\s+", " ");
        String[] msg = settings.split(" ");

        ArrayList<String> seen = new ArrayList<>();
        for (int i = 1; i <= M.numRotors(); i++) {
            if (seen.contains(msg[i])) {
                throw new EnigmaException("Rotor is repeating.");
            } else {
                seen.add(msg[i]);
            }
        }
        String[] rotorArr = new String[M.numRotors()];
        System.arraycopy(msg, 1, rotorArr, 0, rotorArr.length);
        M.insertRotors(rotorArr);

        if (msg[M.numRotors() + 1].length() > (M.numRotors() - 1)) {
            throw new EnigmaException("Wheel settings are too long");
        }

        if (msg[M.numRotors() + 1].length() < (M.numRotors() - 1)) {
            throw new EnigmaException("Wheel settings are too short");
        }

        for (int i = 0; i < msg[M.numRotors() + 1].length(); i++) {
            if (!_alphabet.contains(msg[M.numRotors() + 1].charAt(i))) {
                throw new EnigmaException("Some characters not in alphabet.");
            }
        }

        M.setRotors(msg[M.numRotors() + 1]);
        String plugString = "";
        for (int x = M.numRotors() + 2; x < msg.length; x++) {
            plugString = plugString + msg[x];
        }

        M.setPlugboard(new Permutation(plugString, _alphabet));
    }


    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        char[] msgArr = msg.toCharArray();
        int counter = 0;
        for (char c : msgArr) {
            if (counter == 5) {
                _output.print(" ");
                counter = 0;
            }
            if (c == ' ') {
                continue;
            }
            _output.print(c);
            counter += 1;
        }
        _output.println();
    }


}
