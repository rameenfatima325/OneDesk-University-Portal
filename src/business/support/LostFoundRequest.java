package business.support;

import business.shared.SupportRequest;
import business.shared.RequestStatus;

import java.util.Date;

public class LostFoundRequest extends SupportRequest {

    private LostFoundType requestType;
    private String itemName;
    private ItemCategory category;
    private String location;
    private Date reportDate;
    private String contact;
    private String storageLocation; // only used when FOUND

    public LostFoundRequest(int studentId) {
        super(studentId);
        this.status = RequestStatus.PENDING;
        this.submissionDate = new Date();
    }

    @Override
    public boolean validate() {
        if (requestType == null) return false;
        if (itemName == null || itemName.trim().isEmpty()) return false;
        if (category == null) return false;
        if (location == null || location.trim().isEmpty()) return false;
        if (reportDate == null) return false;
        if (contact == null || contact.trim().isEmpty()) return false;
        return true;
    }

    @Override
    public boolean submit() throws business.shared.exceptions.EligibilityException {
        if (!validate()) return false;

        business.shared.EligibilityService.requireStandardEligibility(this.studentId);
        this.status = RequestStatus.PENDING;
        return true;
    }

    /**
     * Basic match-finding logic: checks if the given found item
     * name and category are similar to this (lost) item.
     */
    public boolean searchMatch(String foundItemName, ItemCategory foundCategory) {
        if (this.requestType != LostFoundType.LOST) return false;
        if (foundCategory != this.category) return false;
        return foundItemName != null &&
               foundItemName.trim().equalsIgnoreCase(this.itemName.trim());
    }

    public void markAsResolved() {
        updateStatus(RequestStatus.RESOLVED);
    }

    // Getters & Setters
    public LostFoundType getRequestType() { return requestType; }
    public void setRequestType(LostFoundType requestType) { this.requestType = requestType; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category.name(); }
    public void setCategory(ItemCategory category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
}
