package org.springframework.samples.petclinic.appointment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.samples.petclinic.chat.RagEmbeddingService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
class AppointmentController {

	private final OwnerRepository owners;

	private final PetRepository pets;

	private final VetRepository vets;

	private final AppointmentRepository appointments;

	private final RagEmbeddingService ragEmbeddingService;

	public AppointmentController(OwnerRepository owners, PetRepository pets, VetRepository vets,
			AppointmentRepository appointments, RagEmbeddingService ragEmbeddingService) {
		this.owners = owners;
		this.pets = pets;
		this.vets = vets;
		this.appointments = appointments;
		this.ragEmbeddingService = ragEmbeddingService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("appointment")
	public Appointment loadPetWithAppointment(@PathVariable(value = "ownerId", required = false) Integer ownerId,
			@PathVariable(value = "petId", required = false) Integer petId,
			@PathVariable(value = "appointmentId", required = false) Integer appointmentId, Map<String, Object> model) {

		if (ownerId == null || petId == null) {
			return new Appointment();
		}

		Optional<Owner> optionalOwner = owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));

		Pet pet = owner.getPet(petId);
		model.put("pet", pet);
		model.put("owner", owner);
		model.put("vets", vets.findAll());

		Appointment appointment;
		if (appointmentId != null) {
			appointment = pet.getAppointment(appointmentId);
			if (appointment == null) {
				throw new IllegalArgumentException("Appointment not found with id: " + appointmentId);
			}
		}
		else {
			appointment = new Appointment();
			pet.addAppointment(appointment);
		}
		appointment.setPet(pet);
		return appointment;
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
	public String initNewAppointmentForm(@RequestParam(value = "vetId", required = false) Integer vetId,
			@RequestParam(value = "appointmentDate", required = false) LocalDate date,
			@ModelAttribute Appointment appointment, Map<String, Object> model) {

		if (date == null) {
			date = LocalDate.now();
			if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
				date = date.plusDays(date.getDayOfWeek() == DayOfWeek.SATURDAY ? 2 : 1);
			}
		}

		if (appointment.getAppointmentDate() == null) {
			appointment.setAppointmentDate(date);
		}

		if (vetId != null) {
			Optional<Vet> vet = vets.findById(vetId);
			vet.ifPresent(appointment::setVet);
		}

		populateAvailableSlots(appointment, model);

		return "pets/createOrUpdateAppointmentForm";
	}

	@PostMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
	public String processNewAppointmentForm(@ModelAttribute Owner owner, @PathVariable int petId,
			@Valid Appointment appointment, BindingResult result, Map<String, Object> model,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			populateAvailableSlots(appointment, model);
			return "pets/createOrUpdateAppointmentForm";
		}

		// Ensure time isn't already booked
		if (appointment.getVet() != null && appointment.getAppointmentDate() != null
				&& appointment.getAppointmentTime() != null) {
			List<Appointment> existing = appointments.findByVetIdAndAppointmentDate(appointment.getVet().getId(),
					appointment.getAppointmentDate());
			boolean isBooked = existing.stream()
				.anyMatch(a -> a.getAppointmentTime().equals(appointment.getAppointmentTime()));
			if (isBooked) {
				result.rejectValue("appointmentTime", "duplicate",
						"This time slot is already booked for the selected vet.");
				populateAvailableSlots(appointment, model);
				return "pets/createOrUpdateAppointmentForm";
			}
		}

		if (owner != null) {
			this.owners.save(owner);
			this.ragEmbeddingService.ingestAppointments(java.util.List.of(appointment));
		}
		redirectAttributes.addFlashAttribute("message", "Your appointment has been booked");
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/appointments/{appointmentId}/edit")
	public String initUpdateAppointmentForm(@RequestParam(value = "vetId", required = false) Integer vetId,
			@RequestParam(value = "appointmentDate", required = false) LocalDate date,
			@ModelAttribute Appointment appointment, Map<String, Object> model) {

		if (date != null) {
			appointment.setAppointmentDate(date);
		}
		if (vetId != null) {
			Optional<Vet> vet = vets.findById(vetId);
			vet.ifPresent(appointment::setVet);
		}

		populateAvailableSlots(appointment, model);

		return "pets/createOrUpdateAppointmentForm";
	}

	@PostMapping("/owners/{ownerId}/pets/{petId}/appointments/{appointmentId}/edit")
	public String processUpdateAppointmentForm(@ModelAttribute Owner owner, @PathVariable int petId,
			@Valid Appointment appointment, BindingResult result, Map<String, Object> model,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			populateAvailableSlots(appointment, model);
			return "pets/createOrUpdateAppointmentForm";
		}

		// Ensure time isn't already booked
		if (appointment.getVet() != null && appointment.getAppointmentDate() != null
				&& appointment.getAppointmentTime() != null) {
			List<Appointment> existing = appointments.findByVetIdAndAppointmentDate(appointment.getVet().getId(),
					appointment.getAppointmentDate());
			boolean isBooked = existing.stream()
				.anyMatch(a -> !a.getId().equals(appointment.getId())
						&& a.getAppointmentTime().equals(appointment.getAppointmentTime()));
			if (isBooked) {
				result.rejectValue("appointmentTime", "duplicate",
						"This time slot is already booked for the selected vet.");
				populateAvailableSlots(appointment, model);
				return "pets/createOrUpdateAppointmentForm";
			}
		}

		if (appointment != null) {
			this.appointments.save(appointment);
			this.ragEmbeddingService.ingestAppointments(java.util.List.of(appointment));
		}
		redirectAttributes.addFlashAttribute("message", "Your appointment has been updated");
		return "redirect:/owners/{ownerId}";
	}

	@PostMapping("/owners/{ownerId}/pets/{petId}/appointments/{appointmentId}/delete")
	@Transactional
	public String processDeleteAppointmentForm(@PathVariable int ownerId, @PathVariable int petId,
			@PathVariable int appointmentId, RedirectAttributes redirectAttributes) {
		Optional<Appointment> optionalAppointment = this.appointments.findById(appointmentId);
		if (optionalAppointment.isPresent()) {
			Appointment appointment = optionalAppointment.get();
			Pet pet = appointment.getPet();
			if (pet != null) {
				pet.getAppointments().remove(appointment);
			}
			this.appointments.delete(appointment);
			redirectAttributes.addFlashAttribute("message", "Appointment successfully deleted.");
		}
		return "redirect:/appointments";
	}

	@GetMapping("/appointments")
	public String showCalendar(@RequestParam(value = "date", required = false) LocalDate date,
			Map<String, Object> model) {
		if (date == null) {
			date = LocalDate.now();
		}

		LocalDate monday = date.with(DayOfWeek.MONDAY);
		LocalDate friday = date.with(DayOfWeek.FRIDAY);

		List<Appointment> appointmentsThisWeek = appointments.findByAppointmentDateBetween(monday, friday);

		List<LocalDate> days = List.of(monday, monday.plusDays(1), monday.plusDays(2), monday.plusDays(3), friday);
		List<LocalTime> times = List.of(LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
				LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0),
				LocalTime.of(16, 0));

		Map<LocalTime, Map<LocalDate, List<Appointment>>> calendarData = new LinkedHashMap<>();
		for (LocalTime time : times) {
			Map<LocalDate, List<Appointment>> dayMap = new HashMap<>();
			for (LocalDate day : days) {
				dayMap.put(day, new ArrayList<>());
			}
			calendarData.put(time, dayMap);
		}

		for (Appointment appt : appointmentsThisWeek) {
			if (calendarData.containsKey(appt.getAppointmentTime())
					&& calendarData.get(appt.getAppointmentTime()).containsKey(appt.getAppointmentDate())) {
				calendarData.get(appt.getAppointmentTime()).get(appt.getAppointmentDate()).add(appt);
			}
		}

		model.put("days", days);
		model.put("times", times);
		model.put("calendarData", calendarData);
		model.put("currentWeekStart", monday);
		model.put("previousWeek", monday.minusWeeks(1));
		model.put("nextWeek", monday.plusWeeks(1));

		return "appointments/appointmentsList";
	}

	@GetMapping("/appointments/new")
	public String initNewAppointmentForm(@RequestParam(value = "date", required = false) LocalDate date,
			@RequestParam(value = "time", required = false) LocalTime time, Map<String, Object> model) {

		Appointment appointment = new Appointment();
		if (date != null) {
			appointment.setAppointmentDate(date);
		}
		if (time != null) {
			appointment.setAppointmentTime(time);
		}

		model.put("appointment", appointment);
		populateAvailableSlots(appointment, model);
		return "appointments/createAppointmentForm";
	}

	@PostMapping("/appointments/new")
	public String processNewAppointmentForm(@Valid Appointment appointment, BindingResult result,
			@RequestParam("ownerId") Integer ownerId, @RequestParam("petId") Integer petId,
			@RequestParam("vetId") Integer vetId, Map<String, Object> model, RedirectAttributes redirectAttributes) {

		if (ownerId == null || petId == null || vetId == null) {
			result.reject("error.missingFields", "Please select an owner, pet and veterinarian.");
			populateAvailableSlots(appointment, model);
			return "appointments/createAppointmentForm";
		}

		// Load and link entities BEFORE checking for errors to satisfy validation
		Optional<Pet> pet = pets.findById(petId);
		Optional<Vet> vet = vets.findById(vetId);

		if (!pet.isPresent() || !vet.isPresent()) {
			result.reject("error.invalidEntities", "Invalid pet or veterinarian selected.");
		}
		else {
			appointment.setPet(pet.get());
			appointment.setVet(vet.get());
		}

		if (result.hasErrors()) {
			populateAvailableSlots(appointment, model);
			return "appointments/createAppointmentForm";
		}

		// Conflict check
		List<Appointment> existing = appointments.findByVetIdAndAppointmentDate(vetId,
				appointment.getAppointmentDate());
		boolean isBooked = existing.stream()
			.anyMatch(a -> a.getAppointmentTime().equals(appointment.getAppointmentTime()));
		if (isBooked) {
			result.rejectValue("appointmentTime", "duplicate",
					"This time slot is already booked for the selected vet.");
			populateAvailableSlots(appointment, model);
			return "appointments/createAppointmentForm";
		}

		this.appointments.save(appointment);
		this.ragEmbeddingService.ingestAppointments(java.util.List.of(appointment));

		redirectAttributes.addFlashAttribute("message", "Appointment successfully booked.");
		return "redirect:/appointments";
	}

	private void populateAvailableSlots(Appointment appointment, Map<String, Object> model) {
		LocalDate date = appointment.getAppointmentDate();
		Vet vet = appointment.getVet();
		List<LocalTime> availableSlots = new ArrayList<>();
		if (date != null && date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
			availableSlots
				.addAll(List.of(LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0), LocalTime.of(12, 0),
						LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0)));

			if (vet != null) {
				List<Appointment> bookedAppointments = appointments.findByVetIdAndAppointmentDate(vet.getId(), date);
				List<LocalTime> bookedTimes = bookedAppointments.stream()
					.filter(a -> appointment.isNew() || !a.getId().equals(appointment.getId()))
					.map(Appointment::getAppointmentTime)
					.toList();
				availableSlots.removeAll(bookedTimes);
			}
		}
		model.put("availableSlots", availableSlots);
	}

}
