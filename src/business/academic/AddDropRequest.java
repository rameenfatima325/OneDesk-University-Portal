package business.academic;

import business.shared.AcademicRequest;
import business.shared.NotificationService;
import business.shared.TrackingService;
import business.shared.exceptions.DeadlineExceededException;
import db.academic.AddDropDAO;

import java.time.LocalDate;


public class AddDropRequest extends AcademicRequest {

    public enum RequestType { ADD, DROP, WITHDRAW }

    private RequestType requestType;
    private String      courseCode;
    private String      reason;
    private String      supportingDocPath;
    private String      trackingId;


    private static final LocalDate ADD_DROP_DEADLINE = LocalDate.now().plusDays(30);

    public AddDropRequest(int studentId) {
        super(studentId);
    }


    private interface ValidationStrategy {
        boolean validate(AddDropRequest r);
    }

    private static final ValidationStrategy ADD_STRATEGY = r ->
            r.courseCode != null && !r.courseCode.isBlank()
         && r.reason     != null && !r.reason.isBlank();

    private static final ValidationStrategy DROP_STRATEGY = r ->
            r.courseCode != null && !r.courseCode.isBlank()
         && r.reason     != null && r.reason.trim().length() >= 10; // DROP requires a proper reason

    private static final ValidationStrategy WITHDRAW_STRATEGY = r ->
            r.reason            != null && r.reason.trim().length() >= 20   // formal justification required
         && r.supportingDocPath != null && !r.supportingDocPath.isBlank();  // doc mandatory

    private ValidationStrategy getStrategy() {
        return switch (requestType) {
            case ADD      -> ADD_STRATEGY;
            case DROP     -> DROP_STRATEGY;
            case WITHDRAW -> WITHDRAW_STRATEGY;
        };
    }

    @Override
    public boolean validate() {
        if (requestType == null) return false;
        return getStrategy().validate(this);
    }


    public boolean isWithinDeadline() {
        // WITHDRAW is allowed any time during the semester — no deadline
        if (requestType == RequestType.WITHDRAW) return true;
        return !LocalDate.now().isAfter(ADD_DROP_DEADLINE);
    }

    public void checkDeadlineOrThrow() throws DeadlineExceededException {
        if (!isWithinDeadline()) {
            throw new DeadlineExceededException(
                    "The add/drop window closed on " + ADD_DROP_DEADLINE +
                    ". Please contact the Academic Office for exceptions.");
        }
    }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;

        business.shared.EligibilityService.requireStandardEligibility(this.studentId);

        AddDropDAO dao = new AddDropDAO();
        if (!dao.verifyStudentExists(this.studentId)) return false;

        if (!isWithinDeadline()) {
            this.status = business.shared.RequestStatus.FLAGGED_FOR_REVIEW;
        }

        this.trackingId = TrackingService.generate("ADR");
        if (dao.saveAddDropRequest(this)) {
            NotificationService.sendToAcademicOffice(
                    "Add/Drop/Withdraw (" + requestType.name() + ")", this.trackingId);
            return true;
        }
        return false;
    }

    public RequestType getRequestType()              { return requestType; }
    public void        setRequestType(RequestType t) { this.requestType = t; }
    public String      getCourseCode()               { return courseCode; }
    public void        setCourseCode(String c)       { this.courseCode = c; }
    public String      getReason()                   { return reason; }
    public void        setReason(String r)           { this.reason = r; }
    public String      getSupportingDocPath()        { return supportingDocPath; }
    public void        setSupportingDocPath(String p){ this.supportingDocPath = p; }
    public String      getTrackingId()               { return trackingId; }

    public static LocalDate getDeadline() { return ADD_DROP_DEADLINE; }
}
