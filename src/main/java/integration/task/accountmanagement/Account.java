package integration.task.accountmanagement;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "email")
    private String email;

    @Column(name = "salesforce_id")
    private String salesforceId;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    public void setSalesforceId(String salesforceId) {
        this.salesforceId = salesforceId;
    }

    public boolean hasValidEmail() {
        return this.email != null && this.email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
}
