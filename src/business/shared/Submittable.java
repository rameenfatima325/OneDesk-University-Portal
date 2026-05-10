package business.shared;

public interface Submittable {
    boolean submit() throws business.shared.exceptions.EligibilityException;
    boolean validate();
}