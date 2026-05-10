package business.academic;

import business.shared.Request;
import business.shared.RequestFactory;
import business.shared.exceptions.DeadlineExceededException;

import java.time.LocalDate;


public class AcademicRequestController {

    /* ---------------------- UC1 ---------------------- */
    public AttendanceCorrectionRequest handleAttendanceCorrection(
            int studentId, String courseName, String semester,
            LocalDate classDate, String reason, String docPath) throws business.shared.exceptions.EligibilityException {

        Request base = RequestFactory.createRequest("ATTENDANCE_CORRECTION", studentId);
        AttendanceCorrectionRequest r = (AttendanceCorrectionRequest) base;
        r.setCourseName(courseName);
        r.setSemester(semester);
        r.setClassDate(classDate);
        r.setReason(reason);
        r.setSupportingDocPath(docPath);

        return r.submit() ? r : null;
    }

    /* ---------------------- UC2 ---------------------- */
    public CourseRegistrationIssue handleCourseRegistrationIssue(
            int studentId, String courseCode, String section, String semester,
            CourseRegistrationIssue.IssueType type, String description,
            CourseRegistrationIssue.Urgency urgency) throws business.shared.exceptions.EligibilityException {

        Request base = RequestFactory.createRequest("COURSE_REGISTRATION", studentId);
        CourseRegistrationIssue r = (CourseRegistrationIssue) base;
        r.setCourseCode(courseCode);
        r.setSection(section);
        r.setSemester(semester);
        r.setIssueType(type);
        r.setIssueDescription(description);
        if (urgency != null) r.setUrgency(urgency);

        return r.submit() ? r : null;
    }

    /* ---------------------- UC3 ---------------------- */
    public AddDropRequest handleAddDropWithdrawal(
            int studentId, AddDropRequest.RequestType type,
            String courseCode, String reason, String docPath)
            throws business.shared.exceptions.EligibilityException {

        Request base = RequestFactory.createRequest("ADD_DROP_WITHDRAWAL", studentId);
        AddDropRequest r = (AddDropRequest) base;
        r.setRequestType(type);
        r.setCourseCode(courseCode);
        r.setReason(reason);
        r.setSupportingDocPath(docPath);

        // Per Rule 4: late submissions are no longer blocked here — AddDropRequest.submit()
        // will detect the missed deadline and place the request in FLAGGED_FOR_REVIEW
        // status for the Academic Office to consider as a special case.

        return r.submit() ? r : null;
    }

    /* ---------------------- UC4 ---------------------- */
    public AcademicRecordCorrection handleRecordCorrection(
            int studentId, AcademicRecordCorrection.RecordType type,
            String incorrectValue, String correctValue,
            String justification, String evidencePath) throws business.shared.exceptions.EligibilityException {

        Request base = RequestFactory.createRequest("RECORD_CORRECTION", studentId);
        AcademicRecordCorrection r = (AcademicRecordCorrection) base;
        r.setRecordType(type);
        r.setIncorrectValue(incorrectValue);
        r.setCorrectValue(correctValue);
        r.setJustification(justification);
        if (evidencePath != null && !evidencePath.isBlank()) r.attachEvidence(evidencePath);

        return r.submit() ? r : null;
    }
}
