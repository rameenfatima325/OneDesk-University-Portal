package business.support;

public class MeritEligibilityStrategy implements EligibilityStrategy {

    private static final double MERIT_CGPA_THRESHOLD = 3.5;

    @Override
    public boolean isEligible(double cgpa, double familyIncome) {
        return cgpa >= MERIT_CGPA_THRESHOLD;
    }

    @Override
    public String getCriteriaDescription() {
        return "Merit scholarship requires CGPA ≥ 3.5";
    }
}
