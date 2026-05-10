package business.academic;

import business.shared.AcademicRequest;
import business.shared.NotificationService;
import business.shared.TrackingService;
import db.academic.CourseIssueDAO;



public class CourseRegistrationIssue extends AcademicRequest {

    public enum IssueType { WRONG_SECTION, NOT_ENROLLED, DUPLICATE, OTHER }
    public enum Urgency   { LOW, MEDIUM, HIGH }

    private String    courseCode;
    private String    section;
    private String    semester;
    private IssueType issueType;
    private String    issueDescription;
    private Urgency   urgency = Urgency.MEDIUM;
    private String    trackingId;

    public CourseRegistrationIssue(int studentId) {
        super(studentId);
    }

    @Override
    public boolean validate() {
        if (courseCode       == null || courseCode.isBlank())          return false;
        if (semester         == null || semester.isBlank())            return false;
        if (issueType        == null)                                  return false;
        if (issueDescription == null || issueDescription.isBlank())    return false;
        return true;
    }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;


        business.shared.EligibilityService.requireStandardEligibility(this.studentId);
        CourseIssueDAO dao = new CourseIssueDAO();
        if (!dao.verifyStudentExists(this.studentId)) return false;

        this.trackingId = TrackingService.generate("CRI");
        if (dao.saveCourseIssue(this)) {
            NotificationService.sendToAcademicOffice("Course Registration Issue", this.trackingId);
            return true;
        }
        return false;
    }

    public String categorizeIssue() {
        return switch (issueType) {
            case WRONG_SECTION -> "Section Correction Desk";
            case NOT_ENROLLED  -> "Enrollment Desk";
            case DUPLICATE     -> "Records Desk";
            case OTHER         -> "General Academic Office";
        };
    }

    public String    getCourseCode()          { return courseCode; }
    public void      setCourseCode(String c)  { this.courseCode = c; }
    public String    getSection()             { return section; }
    public void      setSection(String s)     { this.section = s; }
    public String    getSemester()            { return semester; }
    public void      setSemester(String s)    { this.semester = s; }
    public IssueType getIssueType()           { return issueType; }
    public void      setIssueType(IssueType t){ this.issueType = t; }
    public String    getIssueDescription()    { return issueDescription; }
    public void      setIssueDescription(String d) { this.issueDescription = d; }
    public Urgency   getUrgency()             { return urgency; }
    public void      setUrgency(Urgency u)    { this.urgency = u; }
    public String    getTrackingId()          { return trackingId; }
}
