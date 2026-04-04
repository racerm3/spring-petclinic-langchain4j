package org.springframework.samples.petclinic.appointment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

public interface AppointmentRepository extends Repository<Appointment, Integer> {

	/**
	 * Save an {@link Appointment} to the data store, either inserting or updating it.
	 * @param appointment the {@link Appointment} to save
	 */
	void save(Appointment appointment);

	/**
	 * Find appointments by vet id and date.
	 * @param vetId the vet id
	 * @param appointmentDate the appointment date
	 * @return a list of appointments
	 */
	@Transactional(readOnly = true)
	List<Appointment> findByVetIdAndAppointmentDate(Integer vetId, LocalDate appointmentDate);

	/**
	 * Find all appointments.
	 * @return a list of appointments
	 */
	@Transactional(readOnly = true)
	List<Appointment> findAll();

	/**
	 * Find appointments within a specific date range.
	 * @param startDate the start date
	 * @param endDate the end date
	 * @return a list of appointments
	 */
	@Transactional(readOnly = true)
	List<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);

	/**
	 * Find appointments by pet name.
	 * @param name the pet name
	 * @return a list of appointments
	 */
	@Transactional(readOnly = true)
	List<Appointment> findByPet_NameIgnoreCaseOrderByAppointmentDateAscAppointmentTimeAsc(String name);

	/**
	 * Find appointments by owner last name.
	 * @param lastName the owner last name
	 * @return a list of appointments
	 */
	@Transactional(readOnly = true)
	List<Appointment> findByPet_Owner_LastNameIgnoreCaseOrderByAppointmentDateAscAppointmentTimeAsc(String lastName);

	/**
	 * Find appointments by vet last name.
	 * @param lastName the vet last name
	 * @return a list of appointments
	 */
	@Transactional(readOnly = true)
	List<Appointment> findByVet_LastNameIgnoreCaseOrderByAppointmentDateAscAppointmentTimeAsc(String lastName);

	/**
	 * Find an appointment by its id.
	 * @param id the appointment id
	 * @return an optional appointment
	 */
	@Transactional(readOnly = true)
	Optional<Appointment> findById(Integer id);

}
