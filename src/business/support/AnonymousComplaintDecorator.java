package business.support;

import business.shared.RequestStatus;
import business.shared.Priority;
import business.shared.StatusChangeObserver;

import java.util.Date;


public class AnonymousComplaintDecorator extends GeneralComplaint {

    private final GeneralComplaint wrapped;

    public AnonymousComplaintDecorator(GeneralComplaint complaint) {
        super(0); // anonymous — no real studentId
        this.wrapped = complaint;
    }

    @Override
    public boolean validate() { return wrapped.validate(); }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException { return wrapped.submit(); }

    @Override public int getStudentId()      { return 0; }
    @Override public String getAdminRemarks(){ return wrapped.getAdminRemarks(); }

    @Override public String getCategory()    { return wrapped.getCategory(); }
    @Override public String getSeverity()    { return wrapped.getSeverity(); }
    @Override public String            getSubject()     { return wrapped.getSubject(); }
    @Override public String            getDescription() { return wrapped.getDescription(); }
    @Override public RequestStatus     getStatus()      { return wrapped.getStatus(); }
    @Override public Priority          getPriority()    { return wrapped.getPriority(); }
    @Override public Date              getSubmissionDate(){ return wrapped.getSubmissionDate(); }
    @Override public int               getRequestId()   { return wrapped.getRequestId(); }
    @Override public boolean           isAnonymous()    { return true; }

    @Override
    public void updateStatus(RequestStatus newStatus) { wrapped.updateStatus(newStatus); }

    @Override
    public void addObserver(StatusChangeObserver observer) { wrapped.addObserver(observer); }
}
