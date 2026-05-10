package business.shared;

public interface StatusChangeObserver {
    void onStatusChange(Request request, RequestStatus oldStatus, RequestStatus newStatus);
}