package business.shared;

import business.academic.AttendanceCorrectionRequest;
import business.academic.CourseRegistrationIssue;
import business.academic.AddDropRequest;
import business.academic.AcademicRecordCorrection;
import business.document.TranscriptRequest;
import business.document.DegreeVerificationRequest;
import business.document.EnrollmentLetterRequest;
import business.document.IDCardRequest;
import business.support.FeeSupportRequest;
import business.support.LostFoundRequest;
import business.support.ScholarshipQuery;
import business.support.GeneralComplaint;



public class RequestFactory {

    public static Request createRequest(String requestType, int studentId) {

        if (requestType == null || requestType.trim().isEmpty()) {
            throw new IllegalArgumentException("Request type cannot be null or empty.");
        }

        switch (requestType.toUpperCase().trim()) {


            case "ATTENDANCE_CORRECTION":
                return new AttendanceCorrectionRequest(studentId);

            case "COURSE_REGISTRATION":
                return new CourseRegistrationIssue(studentId);

            case "ADD_DROP_WITHDRAWAL":
                return new AddDropRequest(studentId);

            case "RECORD_CORRECTION":
                return new AcademicRecordCorrection(studentId);


            case "FEE_SUPPORT":
                return new FeeSupportRequest(studentId);

            case "LOST_AND_FOUND":
                return new LostFoundRequest(studentId);

            case "SCHOLARSHIP_QUERY":
                return new ScholarshipQuery(studentId);

            case "GENERAL_COMPLAINT":
                return new GeneralComplaint(studentId);



            case "TRANSCRIPT_ISSUANCE":
                return new TranscriptRequest(studentId);

            case "DEGREE_VERIFICATION":
                return new DegreeVerificationRequest(studentId);

            case "VERIFICATION_LETTER":
                return new EnrollmentLetterRequest(studentId);

            case "ID_CARD":
                return new IDCardRequest(studentId);


            default:
                throw new IllegalArgumentException("Unknown Request Type: " + requestType +
                        ". Please check the UI string passed to the RequestFactory.");
        }
    }
}
