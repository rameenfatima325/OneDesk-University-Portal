package business.support;


public interface EligibilityStrategy {

    boolean isEligible(double cgpa, double familyIncome);
    String getCriteriaDescription();
}
