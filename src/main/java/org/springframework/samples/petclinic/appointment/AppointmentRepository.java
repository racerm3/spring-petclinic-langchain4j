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
	 * Find an appointment by its id.
	 * @param id the appointment id
	 * @return an optional appointment
	 */
	@Transactional(readOnly = true)
	Optional<Appointment> findById(Integer id);

}
