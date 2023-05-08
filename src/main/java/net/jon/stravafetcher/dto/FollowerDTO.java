package net.jon.stravafetcher.dto;

public class FollowerDTO {
    private String firstName;
    private String lastName;
    private long count;

    public FollowerDTO(String firstName, String lastName, long numComments) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.count = numComments;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    // toString method for debugging purposes
    @Override
    public String toString() {
        return "FollowerDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", count=" + count +
                '}';
    }
}
