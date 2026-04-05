package org.springframework.samples.petclinic.appointment;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

	/**
	 * Find appointments by vet id and date.
	 * @param vetId the vet id
	 * @param appointmentDate the appointment date
	 * @return a list of appointments
	 */
	@Transactional(readOnly = true)
	List<Appointment> findByVetIdAndAppointmentDate(Integer vetId, LocalDate appointmentDate);

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

}
