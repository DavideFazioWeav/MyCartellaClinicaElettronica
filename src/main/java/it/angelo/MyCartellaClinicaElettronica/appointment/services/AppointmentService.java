package it.angelo.MyCartellaClinicaElettronica.appointment.services;

import it.angelo.MyCartellaClinicaElettronica.appointment.entities.Appointment;
import it.angelo.MyCartellaClinicaElettronica.appointment.entities.AppointmentDTO;
import it.angelo.MyCartellaClinicaElettronica.appointment.entities.CalendarDay;
import it.angelo.MyCartellaClinicaElettronica.appointment.entities.TimeSlot;
import it.angelo.MyCartellaClinicaElettronica.appointment.repositories.AppointmentRepository;
import it.angelo.MyCartellaClinicaElettronica.appointment.repositories.CalendarDayRepository;
import it.angelo.MyCartellaClinicaElettronica.user.entities.User;
import it.angelo.MyCartellaClinicaElettronica.user.repositories.UserRepository;
import it.angelo.MyCartellaClinicaElettronica.user.utils.Roles;
import lombok.Data;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ToString
@Service
@Data
public class AppointmentService {

    Logger logger = LoggerFactory.getLogger(AppointmentService.class);
    int lineGetter = new Exception().getStackTrace()[0].getLineNumber();


    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarDayRepository calendarDayRepository;

    private boolean free = false;

    /**
     * Questo metodo serve per creare un nuovo appuntamento e popolare la tabella appointment con i dati essenziali presi
     * in input da il parametero che gli passiamo.
     * @param appointmentInput consiste nel body della richiesta
     * @return appointment
     * @throws Exception
     */
    private Appointment newAppointment(AppointmentDTO appointmentInput) throws Exception {
        //creo un nuovo appuntamento
        Appointment appointment = new Appointment();

        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setAddress(appointmentInput.getAddress());
        appointment.setCity(appointmentInput.getCity());
        appointment.setDescription(appointmentInput.getDescription());
        appointment.setState(appointmentInput.getState());
        appointment.setNumber(appointmentInput.getNumber());
        appointment.setZipCode(appointmentInput.getZipCode());
        appointment.setTimeSlot(appointmentInput.getTimeSlot());
        appointment.setAppointmentStart(appointmentInput.getAppointmentStart());
        appointment.setAppointmentEnd(appointmentInput.getAppointmentEnd());
        appointment.setAppointmentDate((LocalDate.from(appointmentInput.getAppointmentStart())));

        //check for patient
        if (appointmentInput.getPatient() == null) throw new Exception("Patient not found");
        Optional<User> patient = userRepository.findById(appointmentInput.getPatient());
        if (!patient.isPresent() || !Roles.hasRole(patient.get(), Roles.PATIENT))
            throw new Exception("Patient not found");
        appointment.setPatient(patient.get());

        System.out.println("-------------------SAVED---------------\n" +
                "APPOINTMENT SAVED!!!\n" +
                "<-    DATE    ->\n" +
                "DATE FROM SAVED: " + LocalDate.from(appointmentInput.getAppointmentStart()) + "\n" +
                "SLOT FROM SAVED: " + appointmentInput.getTimeSlot() + "\n" +
                "-------------------SAVED---------------" );
        return appointment;
    }

    /**
     * Questo metodo serve per creare un nuovo calendarDay e popolare la tabella calendarDay con i dati essenziali presi
     * in input da il parametero che gli passiamo.
     * @param appointmentInput consiste nel body della richiesta
     * @return calendarDay
     */
    private CalendarDay newCalendarDay(AppointmentDTO appointmentInput){
        //Creo un nuovo calendarDay
        CalendarDay calendarDay = new CalendarDay();
        calendarDay.setAppointment(calendarDay.getAppointment());

        //Seleziona lo slot dato in ingresso occupato
        switch (appointmentInput.getTimeSlot()) {
            case TIME_SLOT_08_00_09_00:
                calendarDay.setTimeSlot1IsAvailable(false);
                break;
            case TIME_SLOT_09_00_10_00:
                calendarDay.setTimeSlot2IsAvailable(false);
                break;
            case TIME_SLOT_10_00_11_00:
                calendarDay.setTimeSlot3IsAvailable(false);
                break;
            case TIME_SLOT_11_00_12_00:
                calendarDay.setTimeSlot4IsAvailable(false);
                break;
            case TIME_SLOT_15_00_16_00:
                calendarDay.setTimeSlot5IsAvailable(false);
                break;
            case TIME_SLOT_16_00_17_00:
                calendarDay.setTimeSlot6IsAvailable(false);
                break;
            case TIME_SLOT_17_00_18_00:
                calendarDay.setTimeSlot7IsAvailable(false);
                break;
            case TIME_SLOT_18_00_19_00:
                calendarDay.setTimeSlot8IsAvailable(false);
                break;
        }
        System.out.println("-------------------SAVED---------------\n" +
                "APPOINTMENT SAVED!!!\n" +
                "<-    DATE    ->\n" +
                "DATE FROM SAVED: " + LocalDate.from(appointmentInput.getAppointmentStart()) + "\n" +
                "SLOT FROM SAVED: " + appointmentInput.getTimeSlot() + "\n" +
                "-------------------SAVED---------------" );
        return calendarDay;
    }

    /**
     * save an appointment
     *
     * @param appointmentInput is an appointmentDTO object
     * @return an appointment
     * @throws Exception a generic exception can be thrown
     */
    public Appointment save(AppointmentDTO appointmentInput) throws Exception {

        logger.debug(String.format(" \'/save\' method called at %s at line# %d by %s",
                AppointmentService.class , lineGetter, appointmentInput.getNumber()));

        // rappresenta un utente autenticato, la gestione è demandata a JwtTokenFilter class
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Boolean isDoctor = userRepository.getSingleUserIdRoleId(appointmentInput.getDoctor()) == 4;

        if (userRepository.findUserById(appointmentInput.getDoctor()) == null && !isDoctor){
            free = true;//sistemare controller
        }
        //Appointment appointment = null;
        int timeSlotSetting = 0;

        //Lista dei giorni presenti in tabella. Utilizziamo il metodo findByDate
        List<Date> listOfDay = calendarDayRepository.findAllDateByDate(LocalDate.from(appointmentInput.getAppointmentStart()));
        System.out.println("LIST OF DAY");
        System.out.println(listOfDay);
        System.out.println(LocalDate.from(appointmentInput.getAppointmentStart()).toString());

        List<Long> listOfDoctorId = calendarDayRepository.findAllDoctorId();
        List<Long> listOfUserIdRoleDoctor = userRepository.getAllUserIdByRoleId();

        //Scorriamo la lista dei giorni
        for (int i = 0; i < listOfDay.size(); i++) {
            Date date = listOfDay.get(i);
            //Se appointmentStart(che mi viene passato da AppointmentDTO) è uguale alla data presente in lista
            if (!listOfDay.isEmpty()) {
                System.out.println("DATES ARE EQUALS!!!");
                System.out.println("<- COMPARE DATES ->");
                System.out.println("DATE FROM DATABASE: " + date);
                System.out.println("DATE FROM FRONT-END: " + LocalDate.from(appointmentInput.getAppointmentStart()));
                System.out.println("-------------------OK-OK---------------");
                //Controlliamo se in questa data il dottore ha già appuntamenti
                for (int y = 0; y < listOfUserIdRoleDoctor.size(); y++) {
                    Long userDoctor = listOfUserIdRoleDoctor.get(y);
                    for (int j = 0; j < listOfDoctorId.size(); j++) {
                        Long doctorId = listOfDoctorId.get(j);
                        if(appointmentInput.getDoctor().equals(doctorId) && doctorId.equals(userDoctor)){
                            List<Long> calendarIdByCalendarDoctor = calendarDayRepository.findCalendarIdFromCalendarDoctorByDoctorId(appointmentInput.getDoctor());
                            List<Long> calendarIdByCalendarDay = calendarDayRepository.findCalendarIdFromCalendarDayByDate(date);
                            System.out.println("primo passo dati raccolti");
                            for (int k = 0; k < calendarIdByCalendarDoctor.size(); k++) {
                                Long id1 = calendarIdByCalendarDoctor.get(k);
                                System.out.println("passo 2 = " + id1);
                                for (int l = 0; l < calendarIdByCalendarDay.size(); l++) {
                                    Long id2 = calendarIdByCalendarDay.get(l);
                                    System.out.println("passo 3 = " + id2);
                                    if(id1.equals(id2)){
                                        Long calendarId = id1;
                                        System.out.println("calendarId = " + calendarId);
                                        System.out.println("CHECK SLOT");
                                        //Controlla se lo slot selezionato è fià occupato
                                        if(appointmentInput.getTimeSlot().equals(TimeSlot.TIME_SLOT_08_00_09_00) && !calendarDayRepository.findTimeSlot1FromId(calendarId)){
                                            System.out.println("SOLT 1 IS EMPTY");
                                            free = true;
                                            return null;
                                        }else if(appointmentInput.getTimeSlot().equals(TimeSlot.TIME_SLOT_09_00_10_00) && !calendarDayRepository.findTimeSlot2FromId(calendarId)){
                                            System.out.println("SOLT 2 IS EMPTY");
                                            free = true;
                                            return null;
                                        }else if(appointmentInput.getTimeSlot().equals(TimeSlot.TIME_SLOT_10_00_11_00) && !calendarDayRepository.findTimeSlot3FromId(calendarId)){
                                            System.out.println("SOLT 3 IS EMPTY");
                                            free = true;
                                            return null;
                                        }else if(appointmentInput.getTimeSlot().equals(TimeSlot.TIME_SLOT_11_00_12_00) && !calendarDayRepository.findTimeSlot4FromId(calendarId)){
                                            System.out.println("SOLT 4 IS EMPTY");
                                            free = true;
                                            return null;
                                        }else if(appointmentInput.getTimeSlot().equals(TimeSlot.TIME_SLOT_15_00_16_00) && !calendarDayRepository.findTimeSlot5FromId(calendarId)){
                                            System.out.println("SOLT 5 IS EMPTY");
                                            free = true;
                                            return null;
                                        }else if(appointmentInput.getTimeSlot().equals(TimeSlot.TIME_SLOT_16_00_17_00) && !calendarDayRepository.findTimeSlot6FromId(calendarId)){
                                            System.out.println("SOLT 6 IS EMPTY");
                                            free = true;
                                            return null;
                                        }else if(appointmentInput.getTimeSlot().equals(TimeSlot.TIME_SLOT_17_00_18_00) && !calendarDayRepository.findTimeSlot7FromId(calendarId)){
                                            System.out.println("SOLT 7 IS EMPTY");
                                            free = true;
                                            return null;
                                        }else if(appointmentInput.getTimeSlot().equals(TimeSlot.TIME_SLOT_18_00_19_00) && !calendarDayRepository.findTimeSlot8FromId(calendarId)){
                                            System.out.println("SOLT 8 IS EMPTY");
                                            free = true;
                                            return null;
                                        }else{
                                            free = false;
                                            System.out.println("DATES ARE EQUALS ENTER INTO SWITCH!!!");
                                            //Estraggo il timeSlot tramite la data match-ata nel ciclo if
                                            calendarDayRepository.findTimeSlotFromDate(date);
                                            //verifico in quale casistica ricade il timeSlot estratto
                                            switch (appointmentInput.getTimeSlot()) {
                                                case TIME_SLOT_08_00_09_00:
                                                    System.out.println("SLOT 1 - 08:00/09:00");
                                                    System.out.println("Date from findTimeSlotFromDateBEFORE - " + date);
                                                    //setto il timeSlot su falso
                                                    calendarDayRepository.updateTimeSlot1FromId(Boolean.FALSE, calendarId);
                                                    System.out.println("Date from findTimeSlotFromDateAFTER - " + date);
                                                    timeSlotSetting = 1;
                                                    break;
                                                case TIME_SLOT_09_00_10_00:
                                                    System.out.println("SLOT 2 - 09:00/10:00");
                                                    System.out.println("Date from findTimeSlotFromDateBEFORE - " + date);
                                                    calendarDayRepository.updateTimeSlot2FromId(Boolean.FALSE, calendarId);
                                                    System.out.println("Date from findTimeSlotFromDateAFTER - " + date);
                                                    timeSlotSetting = 2;
                                                    break;
                                                case TIME_SLOT_10_00_11_00:
                                                    System.out.println("SLOT 3 - 10:00/11:00");
                                                    System.out.println("Date from findTimeSlotFromDateBEFORE - " + date);
                                                    calendarDayRepository.updateTimeSlot3FromId(Boolean.FALSE, calendarId);
                                                    System.out.println("Date from findTimeSlotFromDateAFTER - " + date);
                                                    timeSlotSetting = 3;
                                                    break;
                                                case TIME_SLOT_11_00_12_00:
                                                    System.out.println("SLOT 4 - 11:00/12:00");
                                                    System.out.println("Date from findTimeSlotFromDateBEFORE - " + date);
                                                    calendarDayRepository.updateTimeSlot4FromId(Boolean.FALSE, calendarId);
                                                    System.out.println("Date from findTimeSlotFromDateAFTER - " + date);
                                                    timeSlotSetting = 4;
                                                    break;
                                                case TIME_SLOT_15_00_16_00:
                                                    System.out.println("SLOT 5 - 15:00/16:00");
                                                    System.out.println("Date from findTimeSlotFromDateBEFORE - " + date);
                                                    calendarDayRepository.updateTimeSlot5FromId(Boolean.FALSE, calendarId);
                                                    System.out.println("Date from findTimeSlotFromDateAFTER - " + date);
                                                    timeSlotSetting = 5;
                                                    break;
                                                case TIME_SLOT_16_00_17_00:
                                                    System.out.println("SLOT 6 - 16:00/17:00");
                                                    System.out.println("Date from findTimeSlotFromDateBEFORE - " + date);
                                                    calendarDayRepository.updateTimeSlot6FromId(Boolean.FALSE, calendarId);
                                                    System.out.println("Date from findTimeSlotFromDateAFTER - " + date);
                                                    timeSlotSetting = 6;
                                                    break;
                                                case TIME_SLOT_17_00_18_00:
                                                    System.out.println("SLOT 7 - 17:00/18:00");
                                                    System.out.println("Date from findTimeSlotFromDateBEFORE - " + date);
                                                    calendarDayRepository.updateTimeSlot7FromId(Boolean.FALSE, calendarId);
                                                    System.out.println("Date from findTimeSlotFromDateAFTER - " + date);
                                                    timeSlotSetting = 7;
                                                    break;
                                                case TIME_SLOT_18_00_19_00:
                                                    System.out.println("SLOT 8 - 18:00/19:00");
                                                    System.out.println("Date from findTimeSlotFromDateBEFORE - " + date);
                                                    calendarDayRepository.updateTimeSlot8FromId(Boolean.FALSE, calendarId);
                                                    System.out.println("Date from findTimeSlotFromDateAFTER - " + date);
                                                    timeSlotSetting = 8;
                                                    break;
                                            }
                                            Appointment appointment = newAppointment(appointmentInput);
                                            appointment.setCreatedBy(user);
                                            //setto il doctor
                                            appointment.setDoctor(userRepository.findUserById(appointmentInput.getDoctor()));
                                            //inserisce la chiave esterna(calendar_day_id) nella tabella appointment
                                            appointment.setCalendarDay(calendarDayRepository.findAllById(calendarId));
                                            //salviamo l'appuntamento
                                            appointmentRepository.save(appointment);
                                            return appointment;
                                        }
                                    }else{
                                        System.out.println("Si ferma qui");
                                    }
                                }
                            }
                        }else{//sistemare bug, quando in una data è gia presente un appuntamento tornare alla variabile free false

                            System.out.println("Nella data selezionata il dottore non ha ancora appuntamenti");

                            //se abbiamo finito di scorrere la lista
                            if (listOfDay.size()-1 == i && listOfDoctorId.size()-1 == j && listOfUserIdRoleDoctor.size()-1 == y) {
                                if (userRepository.findUserById(appointmentInput.getDoctor()) == null && !isDoctor){
                                    free = true;//sistemare controller
                                }else{
                                    User doctor = userRepository.findUserById(appointmentInput.getDoctor());

                                    System.out.println("CREATE NEW DATE APPOINTMENT");
                                    //creiamo un nuovo appuntamento
                                    Appointment appointment = newAppointment(appointmentInput);
                                    appointment.setDoctor(doctor);
                                    appointment.setCreatedBy(user);

                                    CalendarDay calendarDay = newCalendarDay(appointmentInput);
                                    calendarDay.setDay(appointment.getAppointmentDate());
                                    appointment.setCalendarDay(calendarDay);
                                    calendarDayRepository.save(calendarDay);
                                    appointmentRepository.save(appointment);
                                    calendarDayRepository.updateCalendarDoctor(calendarDay.getId(), doctor.getId());
                                    return appointment;
                                }
                            }
                            //
                        }
                    }
                }
            }else{
                //altrimenti, se in calendar_day non troviamo corrispondenza della data passata da front-end
                System.out.println("DATES NOT ARE EQUALS!!!");
                System.out.println("<- COMPARE DATES ->");
                System.out.println("DATE FROM DATABASE: " + date);
                System.out.println("DATE FROM FRONT-END: " + LocalDate.from(appointmentInput.getAppointmentStart()));
                System.out.println("-------------------OK-OK---------------");

                //se abbiamo finito di scorrere la lista
                if (listOfDay.size()-1 == i) {
                    if (userRepository.findUserById(appointmentInput.getDoctor()) == null && !isDoctor){
                        free = true;
                    }else{
                        User doctor = userRepository.findUserById(appointmentInput.getDoctor());

                        System.out.println("CREATE NEW DATE APPOINTMENT");
                        //creiamo un nuovo appuntamento
                        Appointment appointment = newAppointment(appointmentInput);
                        appointment.setDoctor(doctor);
                        appointment.setCreatedBy(user);

                        CalendarDay calendarDay = newCalendarDay(appointmentInput);
                        calendarDay.setDay(appointment.getAppointmentDate());
                        appointment.setCalendarDay(calendarDay);
                        calendarDayRepository.save(calendarDay);
                        appointmentRepository.save(appointment);
                        calendarDayRepository.updateCalendarDoctor(calendarDay.getId(), doctor.getId());
                        return appointment;
                    }
                }
            }
        }
        return null;
    }


    public Appointment update(Long id, Appointment appointmentInput) {
        logger.debug(String.format(" \'/update\' method called at %s at line# %d by %s",
                AppointmentService.class , lineGetter, appointmentInput.getNumber()));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (appointmentInput == null) return null;
        appointmentInput.setId(id);
        appointmentInput.setUpdatedAt(LocalDateTime.now());
        appointmentInput.setUpdatedBy(user);
        return appointmentRepository.save(appointmentInput);
    }

    public boolean canEdit(Long id) {
        logger.debug(String.format(" \'/canEdit\' method called at %s at line# %d by ID %s",
                AppointmentService.class , lineGetter, id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (!appointment.isPresent()) return false;
        return appointment.get().getCreatedBy().getId() == user.getId();
    }
}

