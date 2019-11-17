package at.ac.uibk.chaas.minesteg.hooks;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

/**
 * A class that represents the configuration structure for a code hook.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class HookEntry {
    private final Logger logger = LogManager.getLogger();

    private String sourceMethodName;
    private String rawCode;
    private String destinationMethodName;
    private Class<?>[] destinationMethodArgTypes;
    private String destinationMethodNameSimple;
    private String destinationClassName;
    private int destinationLineNumber;
    private LinkedHashMap<String, Class<?>> arguments;

    // calculated attributes
    private boolean isValid = true;
    private int dstMethodStartLine = -1;
    private int dstMethodEndLine = -1;
    private List<String> dstMethodLines = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param json the json representation of this object.
     */
    HookEntry(HookEntryJson json) {
        this.sourceMethodName = json.getSourceMethodName();
        this.rawCode = json.getRawCode();
        this.destinationMethodName = json.getDestinationMethodName();
        this.destinationMethodNameSimple = destinationMethodName.substring(destinationMethodName.lastIndexOf('.') + 1);
        this.destinationClassName = destinationMethodName.substring(0, destinationMethodName.lastIndexOf('.'));
        this.destinationLineNumber = json.getDestinationLineNumber();

        this.arguments = new LinkedHashMap<>(json.getArguments().size());
        json.getArguments().forEach((arg, type) -> {
            try {
                Class<?> argClass = HelperUtil.parseClassType(type);

                this.arguments.put(arg, argClass);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        });

        final List<Class<?>> typeList = new ArrayList<>(json.getDestinationMethodArgTypes().size());
        json.getDestinationMethodArgTypes().forEach(type -> {
            try {
                Class<?> argClass = HelperUtil.parseClassType(type);
                typeList.add(argClass);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        });
        this.destinationMethodArgTypes = typeList.toArray(new Class[0]);

        validate();
    }

    /**
     * @return the source method name.
     */
    public String getSourceMethodName() {
        return sourceMethodName;
    }

    /**
     * @return the destination method name.
     */
    public String getDestinationMethodName() {
        return destinationMethodName;
    }

    /**
     * @return the destination line number.
     */
    public int getDestinationLineNumber() {
        return destinationLineNumber;
    }

    /**
     * @return the arguments for the method that should be called.
     */
    public LinkedHashMap<String, Class<?>> getArguments() {
        return arguments;
    }

    /**
     * @return the destination class name.
     */
    public String getDestinationClassName() {
        return destinationClassName;
    }

    /**
     * @return the destination method start line.
     */
    public int getDstMethodStartLine() {
        return dstMethodStartLine;
    }

    /**
     * @return the destination method end line.
     */
    public int getDstMethodEndLine() {
        return dstMethodEndLine;
    }

    /**
     * @return true if entry is valid.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Validate the entry.
     */
    private void validate() {
        validateSourceMethod();
        validateDestinationMethod();
    }

    /**
     * Validate the given source method.
     */
    private void validateSourceMethod() {
        if (sourceMethodName == null && rawCode == null) {
            isValid = false;
        } else if (sourceMethodName == null && !rawCode.isEmpty()) {
            isValid = true;
        } else {
            // check the source class
            try {
                Class<?> srcClass = HelperUtil.parseClassType(sourceMethodName.substring(0, sourceMethodName.lastIndexOf('.')));

                // check source method
                try {
                    Class<?>[] parameters = new Class[0];

                    if (this.arguments != null && this.arguments.size() > 0) {
                        parameters = arguments.values().toArray(new Class[0]);
                    }

                    Method srcMethod = srcClass.getDeclaredMethod(sourceMethodName.substring(sourceMethodName.lastIndexOf('.') + 1), 
                                                                    parameters);

                    // check parameter count
                    if (srcMethod.getParameterCount() != arguments.size()) {
                        isValid = false;
                    }
                } catch (NoSuchMethodException e) {
                    isValid = false;
                }
            } catch (IllegalArgumentException e) {
                isValid = false;
            }
        }
    }

    /**
     * Validate the given destination method.
     */
    private void validateDestinationMethod() {
        if (destinationMethodName == null) {
            isValid = false;
        } else {
            Method dstMethod = null;
            Class<?> dstClass = null;
            // check the destination class
            try {
                dstClass = HelperUtil.parseClassType(destinationClassName);

                // check destination method
                try {
                    dstMethod = dstClass.getDeclaredMethod(destinationMethodNameSimple, destinationMethodArgTypes);
                } catch (NoSuchMethodException e) {
                    isValid = false;
                }
            } catch (IllegalArgumentException e) {
                isValid = false;
            }

            // Validate arguments
            if (dstClass != null && dstMethod != null) {
                try {
                    File sourceFile = HelperUtil.getFileForClass(dstClass);
                    if (!findDestinationMethodLineNumbers(sourceFile)) {
                        isValid = false;
                    }
                } catch (FileNotFoundException e) {
                    isValid = false;
                }

                if (dstMethodStartLine == -1 || dstMethodEndLine == -1) {
                    isValid = false;
                } else {
                    // check if method contains parameters (simply check if string is contained)
                    for (String argument : arguments.keySet()) {
                        if (argument.startsWith("this.")) {
                            continue; // skip validation of object instance variables
                        }

                        boolean argFound = false;
                        for (String line : dstMethodLines) {
                            if (line.contains(argument)) {
                                argFound = true;
                                break;
                            }
                        }

                        if (!argFound) {
                            isValid = false;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Find the line numbers for the destination method.
     *
     * @param sourceFile the java source file where the destination method is declared.
     * @return true if method was found.
     */
    private boolean findDestinationMethodLineNumbers(File sourceFile) {
        try {
            List<String> lines = Files.readAllLines(sourceFile.toPath());
            Stack<Character> parenthesis = new Stack<>();
            Stack<Character> argumentParenthesis = new Stack<>();
            boolean methodFound = false;
            int foundArguments = 0;

            for (int i = 0; i < lines.size(); i++) {
                // remove spaces to simplify matching
                String cleanLine = lines.get(i).replaceAll("\\s+", "");
                if (!methodFound &&
                        (cleanLine.startsWith("public")
                                || cleanLine.startsWith("protected")
                                || cleanLine.startsWith("private"))
                        && cleanLine.contains(destinationMethodNameSimple + "(")) {
                    dstMethodStartLine = i + 1; // we start to count at 0, so we need to add 1
                }

                if (dstMethodStartLine != -1 && !methodFound) {
                    // scan for argument types
                    ArrayList<Class<?>> argClasses = new ArrayList<>(Arrays.asList(destinationMethodArgTypes));
                    for (Iterator it = argClasses.iterator(); it.hasNext(); ) {
                        Class<?> argClass = (Class<?>) it.next();
                        String simpleClassName = argClass.getSimpleName();
                        if (lines.get(i).contains(simpleClassName)) {
                            it.remove();
                            foundArguments++;
                        }
                    }

                    if (cleanLine.contains(")")) {
                        if (foundArguments == destinationMethodArgTypes.length) {
                            methodFound = true;
                        } else {
                            dstMethodStartLine = -1;
                            foundArguments = 0;
                        }
                    }
                }

                // Find end parenthesis if start line was found
                if (dstMethodStartLine != -1 && methodFound) {
                    dstMethodLines.add(lines.get(i)); // store line, we need to search it later

                    for (int j = 0; j < cleanLine.length(); j++) {
                        char c = cleanLine.charAt(j);

                        if (c == '{' || c == '}') {
                            // same parenthesis again -> push to stack
                            if (parenthesis.empty() || parenthesis.peek() == c) {
                                parenthesis.push(c);
                            } else { // closing parenthesis -> pop stack
                                parenthesis.pop();
                            }
                        }
                    }

                    // if stack is empty, the end of the method has been found
                    if (parenthesis.empty()) {
                        dstMethodEndLine = i;
                        break; // finish processing file
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Parsing java source file failed: {} : {}", sourceFile, e);
            return false;
        }

        return true;
    }

    /**
     * Patch the destination method. Insert the source method call or code.
     *
     * @param offset offset, if one file gets patched multiple times.
     * @throws InvalidHookEntryException if this entry is not valid.
     */
    public void patchDestination(int offset) throws InvalidHookEntryException {
        if (!isValid) {
            throw new InvalidHookEntryException();
        }

        try {
            File sourceFile = HelperUtil.getFileForClass(destinationClassName);
            int newLineNumber = dstMethodStartLine + destinationLineNumber + offset;

            // Read all lines to an array
            List<String> lines = Files.readAllLines(sourceFile.toPath(), Charset.defaultCharset());

            StringBuilder functionCall = new StringBuilder();
            if (sourceMethodName != null && !sourceMethodName.isEmpty()) {
                functionCall.append(sourceMethodName);
                functionCall.append("(");
                if (arguments != null && !arguments.isEmpty()) {
                    functionCall.append(String.join(",", arguments.keySet()));
                }
                functionCall.append(");");
            } else {
                functionCall.append(rawCode);
            }

            // check if line is already patched
            if (newLineNumber < lines.size() && lines.get(newLineNumber).equals(functionCall.toString())) {
                logger.warn("Line {} already patched!", newLineNumber);
            } else {

                // Insert new line at given position
                lines.add(newLineNumber, functionCall.toString());


                // Write back all lines
                Files.write(sourceFile.toPath(), lines, Charset.defaultCharset());
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HookEntry hookEntry = (HookEntry) o;
        return getDestinationLineNumber() == hookEntry.getDestinationLineNumber() &&
                getSourceMethodName().equals(hookEntry.getSourceMethodName()) &&
                getDestinationMethodName().equals(hookEntry.getDestinationMethodName()) &&
                Objects.equals(getArguments(), hookEntry.getArguments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceMethodName(), getDestinationMethodName(), getDestinationLineNumber(), getArguments());
    }

    @Override
    public String toString() {
        return "HookEntry{" +
                "sourceMethodName='" + sourceMethodName + '\'' +
                ", destinationMethodName='" + destinationMethodName + '\'' +
                ", destinationLineNumber=" + destinationLineNumber +
                '}';
    }
}
