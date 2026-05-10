package business.shared;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public abstract class Request implements Submittable {
    protected int requestId;
    protected int studentId;
    protected String category;
    protected String description;
    protected RequestStatus status;
    protected Priority priority;
    protected Date submissionDate;
    protected Date resolvedAt;
    protected String adminRemarks;

    protected List<StatusChangeObserver> observers = new ArrayList<>();

    public Request(int studentId) {
        this.studentId = studentId;
        this.status = RequestStatus.PENDING; // Default status
    }

    public void addObserver(StatusChangeObserver observer) {
        this.observers.add(observer);
    }

    public void updateStatus(RequestStatus newStatus) {
        RequestStatus oldStatus = this.status;
        this.status = newStatus;

        for (StatusChangeObserver observer : observers) {
            observer.onStatusChange(this, oldStatus, newStatus);
        }
    }

    @Override
    public abstract boolean submit() throws business.shared.exceptions.EligibilityException;

    @Override
    public abstract boolean validate();

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RequestStatus getStatus() { return status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Date getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(Date submissionDate) { this.submissionDate = submissionDate; }

    public Date getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Date resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }
}