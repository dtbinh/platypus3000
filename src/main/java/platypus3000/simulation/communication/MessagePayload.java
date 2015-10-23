package platypus3000.simulation.communication;

/**
 * The interface for the payloads. Each payload needs to have a deep copy function for prevent interferences.
 */
public interface MessagePayload {
     public MessagePayload deepCopy();
}
