package parcial;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String sector;

    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    private String registeredBy;
    private double latitude;
    private double longitude;
    private Date timestamp;

    // Getters and setters

    public enum EducationLevel {
        BASIC,
        MEDIUM,
        UNIVERSITY,
        POSTGRADUATE,
        DOCTORATE
    }
}
