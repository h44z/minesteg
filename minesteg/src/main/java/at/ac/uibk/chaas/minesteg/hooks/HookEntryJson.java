package at.ac.uibk.chaas.minesteg.hooks;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * A class that represents the configuration structure for a code hook in JSON.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class HookEntryJson {
    private String sourceMethodName;
    private String rawCode;
    private String destinationMethodName;
    private List<String> destinationMethodArgTypes;
    private int destinationLineNumber;
    private LinkedHashMap<String, String> arguments;

    /**
     * @return the source method name.
     */
    public String getSourceMethodName() {
        return sourceMethodName;
    }

    /**
     * @param sourceMethodName the source method name.
     */
    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    /**
     * @return the raw java code.
     */
    public String getRawCode() {
        return rawCode;
    }

    /**
     * @param rawCode the raw java code.
     */
    public void setRawCode(String rawCode) {
        this.rawCode = rawCode;
    }

    /**
     * @return the destination method name.
     */
    public String getDestinationMethodName() {
        return destinationMethodName;
    }

    /**
     * @param destinationMethodName the destination method name.
     */
    public void setDestinationMethodName(String destinationMethodName) {
        this.destinationMethodName = destinationMethodName;
    }

    /**
     * @return the destination method argument types.
     */
    public List<String> getDestinationMethodArgTypes() {
        return destinationMethodArgTypes;
    }

    /**
     * @param destinationMethodArgTypes the destination method argument types.
     */
    public void setDestinationMethodArgTypes(List<String> destinationMethodArgTypes) {
        this.destinationMethodArgTypes = destinationMethodArgTypes;
    }

    /**
     * @return the destination line number.
     */
    public int getDestinationLineNumber() {
        return destinationLineNumber;
    }

    /**
     * @param destinationLineNumber the destination line number.
     */
    public void setDestinationLineNumber(int destinationLineNumber) {
        this.destinationLineNumber = destinationLineNumber;
    }

    /**
     * @return the arguments for the destination method.
     */
    public LinkedHashMap<String, String> getArguments() {
        return arguments;
    }

    /**
     * @param arguments the arguments for the destination method.
     */
    public void setArguments(LinkedHashMap<String, String> arguments) {
        this.arguments = arguments;
    }
}
