package it.angelo.MyCartellaClinicaElettronica.medicalArchive.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import it.angelo.MyCartellaClinicaElettronica.medicalArchive.entities.MedicalRecord;
import it.angelo.MyCartellaClinicaElettronica.user.entities.User;
import it.angelo.MyCartellaClinicaElettronica.utils.entities.BaseEntity;
import lombok.Data;

import javax.persistence.*;


// Referto medico
@Data
@Entity
@Table(name="MEDICAL_REPORT")
public class MedicalReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String anamnesis;
    private String diagnosis;
    private String therapy;
    private String prognosis;
    private String medicalExamination;

    @OneToOne
    private User patient;

    @ManyToOne
    private User doctor;

    //proprietario della relazione
    //tanti referti(MedicalReport) possono essere contenuti dentro la cartella clinica(medicalRecord)
    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    @JsonIgnore
    private MedicalRecord medicalRecord; // lo trovo in MedicalRecord mappedBy

}
