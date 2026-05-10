package business.document;

import business.shared.Request;
import business.shared.RequestFactory;

public class DocumentRequestController {

    public TranscriptRequest handleTranscriptRequest(int studentId, String type, String purpose, int copies, String mode, String address) throws business.shared.exceptions.EligibilityException {
        Request base = RequestFactory.createRequest("TRANSCRIPT_ISSUANCE", studentId);
        TranscriptRequest request = (TranscriptRequest) base;

        request.setTranscriptType(type);
        request.setPurpose(purpose);
        request.setCopies(copies);
        request.setDeliveryMode(mode);
        request.setMailingAddress(address);

        return (request.submit()) ? request : null;
    }

    public DegreeVerificationRequest handleDegreeVerification(int studentId, int year, String program, String type, String purpose, int copies, boolean isUrgent) throws business.shared.exceptions.EligibilityException {
        Request base = RequestFactory.createRequest("DEGREE_VERIFICATION", studentId);
        DegreeVerificationRequest request = (DegreeVerificationRequest) base;
        request.setGraduationYear(year);
        request.setDegreeProgram(program);
        request.setVerificationType(type);
        request.setPurpose(purpose);
        request.setCopies(copies);
        request.setUrgent(isUrgent);

        return request.submit() ? request : null;
    }

    public EnrollmentLetterRequest handleEnrollmentLetterRequest(int studentId, String type, String addressedTo, String language, String purpose, int copies) throws business.shared.exceptions.EligibilityException {
        Request base = RequestFactory.createRequest("VERIFICATION_LETTER", studentId);
        EnrollmentLetterRequest request = (EnrollmentLetterRequest) base;
        request.setLetterType(type);
        request.setAddressedTo(addressedTo);
        request.setLanguage(language);
        request.setPurpose(purpose);
        request.setCopies(copies);

        return request.submit() ? request : null;
    }

    public IDCardRequest handleIDCardRequest(int studentId, String type, String reason, String address) throws business.shared.exceptions.EligibilityException {
        Request base = RequestFactory.createRequest("ID_CARD", studentId);
        IDCardRequest request = (IDCardRequest) base;
        request.setRequestType(type);
        request.setReplacementReason(reason);
        request.setDeliveryAddress(address);

        return request.submit() ? request : null;
    }
}