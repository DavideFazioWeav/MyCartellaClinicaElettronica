package it.angelo.MyCartellaClinicaElettronica.user.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import it.angelo.MyCartellaClinicaElettronica.utils.entities.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table(name="MEDICAL_RECORD")
public class MedicalRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String description;
    private String patientHistory;

    private boolean isActive = true;

    @ManyToOne
    private User patient;

    @ManyToOne
    private User doctor;

    //set di referti medici
    //mappedBy deve contenere il nome dell'istanza del proprietario della relazione che è: medicalRecord
    @OneToMany(mappedBy = "medicalRecord")
    @JsonBackReference //necessario per evitare Infinite recursion (StackOverflowError)
    private Set<MedicalReport> medicalReport;

}