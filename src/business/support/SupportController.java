package business.support;

import business.shared.RequestStatus;
import business.shared.exceptions.InvalidAmountException;
import business.shared.exceptions.EligibilityException;
import db.support.FeeSupportDAO;
import db.support.LostFoundDAO;
import db.support.ScholarshipDAO;
import db.support.ComplaintDAO;

import java.sql.SQLException;
import java.util.List;


public class SupportController {

    private final FeeSupportDAO  feeSupportDAO  = new FeeSupportDAO();
    private final LostFoundDAO   lostFoundDAO   = new LostFoundDAO();
    private final ScholarshipDAO scholarshipDAO = new ScholarshipDAO();
    private final ComplaintDAO   complaintDAO   = new ComplaintDAO();


    public FeeSupportRequest createFeeRequest(int studentId, FeeType feeType,
                                              double amount, FeeRequestType requestType,
                                              String justification, String docPath)
            throws business.shared.exceptions.EligibilityException, InvalidAmountException, SQLException {

        FeeSupportRequest req = new FeeSupportRequest(studentId);
        req.setFeeType(feeType);
        req.setOutstandingAmount(amount);   // throws InvalidAmountException if <= 0
        req.setRequestType(requestType);
        req.setJustification(justification);
        req.setDocPath(docPath);

        if (!req.submit())
            throw new IllegalStateException("Fee request validation failed.");

        feeSupportDAO.insert(req);
        return req;
    }

    public List<FeeSupportRequest> getFeeRequestsByStudent(int studentId) throws SQLException {
        return feeSupportDAO.selectByStudentId(studentId);
    }

    public List<FeeSupportRequest> getAllFeeRequests() throws SQLException {
        return feeSupportDAO.selectAll();
    }

    public void resolveFeeRequest(int requestId, String adminRemarks) throws SQLException {
        feeSupportDAO.updateStatus(requestId, RequestStatus.RESOLVED, adminRemarks);
    }

    public void updateFeeRequestStatus(int requestId, RequestStatus status,
                                       String adminRemarks) throws SQLException {
        feeSupportDAO.updateStatus(requestId, status, adminRemarks);
    }



    public LostFoundRequest reportLostItem(int studentId, String itemName,
                                           ItemCategory category, String location,
                                           java.util.Date reportDate, String description,
                                           String contact) throws business.shared.exceptions.EligibilityException, SQLException {

        LostFoundRequest req = new LostFoundRequest(studentId);
        req.setRequestType(LostFoundType.LOST);
        req.setItemName(itemName);
        req.setCategory(category);
        req.setLocation(location);
        req.setReportDate(reportDate);
        req.setDescription(description);
        req.setContact(contact);

        if (!req.submit())
            throw new IllegalStateException("Lost item report validation failed.");

        lostFoundDAO.insertLostItem(req);
        return req;
    }

    public LostFoundRequest reportFoundItem(int studentId, String itemName,
                                            ItemCategory category, String locationFound,
                                            java.util.Date reportDate, String storageLocation,
                                            String contact) throws business.shared.exceptions.EligibilityException, SQLException {

        LostFoundRequest req = new LostFoundRequest(studentId);
        req.setRequestType(LostFoundType.FOUND);
        req.setItemName(itemName);
        req.setCategory(category);
        req.setLocation(locationFound);
        req.setReportDate(reportDate);
        req.setStorageLocation(storageLocation);
        req.setContact(contact);

        if (!req.submit())
            throw new IllegalStateException("Found item report validation failed.");

        lostFoundDAO.insertFoundItem(req);
        return req;
    }

    public List<LostFoundRequest> getLostItemsByStudent(int studentId) throws SQLException {
        return lostFoundDAO.selectLostByStudentId(studentId);
    }

    public List<LostFoundRequest> getAllFoundItems() throws SQLException {
        return lostFoundDAO.selectAllFoundItems();
    }

    public List<LostFoundRequest> searchMatchingFoundItems(String itemName,
                                                           ItemCategory category)
            throws SQLException {
        return lostFoundDAO.searchMatches(itemName, category);
    }

    public void markLostFoundResolved(int itemId, LostFoundType type) throws SQLException {
        lostFoundDAO.updateStatus(itemId, type, RequestStatus.RESOLVED, "");
    }

    public List<LostFoundRequest> getAllLostItems() throws SQLException {
        return lostFoundDAO.selectAllLostItems();
    }

    // Admin: set any status on a lost or found item
    public void updateLostFoundStatus(int itemId, LostFoundType type,
                                      RequestStatus status) throws SQLException {
        lostFoundDAO.updateStatus(itemId, type, status, "");
    }



    public ScholarshipQuery submitScholarshipQuery(int studentId,
                                                   ScholarshipType type,
                                                   double cgpa, double familyIncome,
                                                   String queryText, String docPath)
            throws EligibilityException, SQLException {

        ScholarshipQuery query = new ScholarshipQuery(studentId);
        query.setScholarshipType(type);
        query.setCgpa(cgpa);
        query.setFamilyIncome(familyIncome);
        query.setQueryText(queryText);
        query.setDocPath(docPath);

        // Throws EligibilityException if not eligible
        query.assertEligible();

        if (!query.submit())
            throw new IllegalStateException("Scholarship query validation failed.");

        scholarshipDAO.insert(query);
        return query;
    }

    public List<ScholarshipQuery> getScholarshipQueriesByStudent(int studentId)
            throws SQLException {
        return scholarshipDAO.selectByStudentId(studentId);
    }

    public List<ScholarshipQuery> getAllScholarshipQueries() throws SQLException {
        return scholarshipDAO.selectAll();
    }

    public void updateScholarshipStatus(int queryId, RequestStatus status,
                                        String adminRemarks) throws SQLException {
        scholarshipDAO.updateStatus(queryId, status, adminRemarks);
    }



    public GeneralComplaint submitComplaint(int studentId, ComplaintCategory category,
                                            Severity severity, String subject,
                                            String description, boolean isAnonymous)
            throws business.shared.exceptions.EligibilityException, SQLException {

        GeneralComplaint base = new GeneralComplaint(studentId);
        base.setCategory(category);
        base.setSeverity(severity);
        base.setSubject(subject);
        base.setDescription(description);
        base.setAnonymous(isAnonymous);

        if (!base.submit())
            throw new IllegalStateException("Complaint validation failed.");

        // Use Decorator if anonymous so the DAO only ever sees the anonymized object
        GeneralComplaint toSave = isAnonymous ? new AnonymousComplaintDecorator(base) : base;
        complaintDAO.insert(toSave);
        return base;
    }

    public List<GeneralComplaint> getComplaintsByStudent(int studentId) throws SQLException {
        return complaintDAO.selectByStudentId(studentId);
    }

    public List<GeneralComplaint> getAllComplaints() throws SQLException {
        return complaintDAO.selectAll();
    }

    public void updateComplaintStatus(int complaintId, RequestStatus status,
                                      String resolutionRemarks) throws SQLException {
        complaintDAO.updateStatus(complaintId, status, resolutionRemarks);
    }
}