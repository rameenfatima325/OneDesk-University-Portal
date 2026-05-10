package business.shared;

import business.document.DegreeVerificationRequest;
import business.document.TranscriptRequest;
import business.document.EnrollmentLetterRequest;
import business.document.IDCardRequest;


public class NotificationService {

    public static void sendToDocumentationDept(DegreeVerificationRequest req) {
        System.out.println("[System Notification] Degree Verification Request " + req.getTrackingId() + " sent to Documentation Department.");
    }

    public static void sendToExaminationDept(TranscriptRequest req) {
        System.out.println("[System Notification] Transcript Request " + req.getTrackingId() + " forwarded to Examination Department.");
    }

    public static void sendToDocumentationDept(EnrollmentLetterRequest req) {
        System.out.println("[System Notification] Enrollment Letter Request " + req.getTrackingId() + " sent to Documentation Department.");
    }

    public static void sendToDocumentationDept(IDCardRequest req) {
        System.out.println("[System Notification] ID Card Request " + req.getTrackingId() + " sent to Documentation Department.");
    }

    public static void sendToAcademicOffice(String requestLabel, String trackingId) {
        System.out.println("[System Notification] " + requestLabel + " Request "
                + trackingId + " forwarded to Academic Office.");
    }
}