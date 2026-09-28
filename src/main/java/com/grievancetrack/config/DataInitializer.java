package com.grievancetrack.config;

import com.grievancetrack.entity.*;
import com.grievancetrack.entity.enums.GrievanceStatus;
import com.grievancetrack.entity.enums.NotificationType;
import com.grievancetrack.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final GrievanceRepository grievanceRepository;
    private final EscalationRepository escalationRepository;
    private final RatingRepository ratingRepository;
    private final NotificationRepository notificationRepository;

    public DataInitializer(DepartmentRepository departmentRepository,
                           CategoryRepository categoryRepository,
                           GrievanceRepository grievanceRepository,
                           EscalationRepository escalationRepository,
                           RatingRepository ratingRepository,
                           NotificationRepository notificationRepository) {
        this.departmentRepository = departmentRepository;
        this.categoryRepository = categoryRepository;
        this.grievanceRepository = grievanceRepository;
        this.escalationRepository = escalationRepository;
        this.ratingRepository = ratingRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (grievanceRepository.count() > 0) {
            log.info("Grievance database already has records. Skipping sample grievance seeding.");
            return;
        }

        log.info("Initializing GrievanceTrack seed data...");

        // 1. Seed or lookup Departments
        Department roadDept = departmentRepository.findByNameIgnoreCase("Roads Department")
                .orElseGet(() -> departmentRepository.save(new Department(
                        "Roads Department",
                        "Chief Engineer Rajesh Sharma",
                        "roads@civicgov.org / +91-9876543201"
                )));

        Department waterDept = departmentRepository.findByNameIgnoreCase("Water Supply Department")
                .orElseGet(() -> departmentRepository.save(new Department(
                        "Water Supply Department",
                        "Superintending Engineer Priya Sundaram",
                        "water@civicgov.org / +91-9876543202"
                )));

        Department sanitationDept = departmentRepository.findByNameIgnoreCase("Sanitation Department")
                .orElseGet(() -> departmentRepository.save(new Department(
                        "Sanitation Department",
                        "Health Officer Anil Kumar",
                        "sanitation@civicgov.org / +91-9876543203"
                )));

        Department generalDept = departmentRepository.findByNameIgnoreCase("General Civic Department")
                .orElseGet(() -> departmentRepository.save(new Department(
                        "General Civic Department",
                        "Civic Welfare Director Sunita Rao",
                        "civic@civicgov.org / +91-9876543204"
                )));

        // 2. Seed or lookup Categories with fixed SLAs
        Category roadCat = categoryRepository.findByNameIgnoreCase("Road")
                .orElseGet(() -> categoryRepository.save(new Category("Road", 3, roadDept)));
        Category waterCat = categoryRepository.findByNameIgnoreCase("Water")
                .orElseGet(() -> categoryRepository.save(new Category("Water", 2, waterDept)));
        Category garbageCat = categoryRepository.findByNameIgnoreCase("Garbage")
                .orElseGet(() -> categoryRepository.save(new Category("Garbage", 1, sanitationDept)));
        Category otherCat = categoryRepository.findByNameIgnoreCase("Other")
                .orElseGet(() -> categoryRepository.save(new Category("Other", 5, generalDept)));

        // 3. Seed Realistic Grievances across diverse statuses
        LocalDateTime now = LocalDateTime.now();

        // GRV-000001: RESOLVED & RATED
        LocalDateTime grv1Created = now.minusDays(5);
        LocalDateTime grv1Deadline = grv1Created.plusDays(roadCat.getSlaDays());
        Grievance grv1 = new Grievance(
                "GRV-000001",
                "Anita Desai",
                "anita.desai@example.com",
                "9876541101",
                "Deep dangerous potholes on 4th Main Road near City Hospital causing severe traffic bottlenecks.",
                "4th Main Road, Indiranagar, Ward 12",
                roadCat,
                roadDept,
                grv1Created,
                grv1Deadline
        );
        grv1.setStatus(GrievanceStatus.RESOLVED);
        grv1.setResolvedAt(grv1Created.plusDays(2));
        Grievance savedGrv1 = grievanceRepository.save(grv1);

        Rating r1 = new Rating(savedGrv1, 5, "Potholes were filled within 48 hours, excellent prompt work by the team!", grv1.getResolvedAt().plusHours(2));
        savedGrv1.setRating(ratingRepository.save(r1));
        notificationRepository.save(new Notification(savedGrv1, "Grievance GRV-000001 status changed from IN_PROGRESS to RESOLVED.", NotificationType.STATUS_CHANGE));
        notificationRepository.save(new Notification(savedGrv1, "Citizen submitted a 5-star rating for resolved grievance GRV-000001.", NotificationType.RATING_SUBMITTED));

        // GRV-000002: IN_PROGRESS (ON TRACK)
        LocalDateTime grv2Created = now.minusHours(18);
        LocalDateTime grv2Deadline = grv2Created.plusDays(waterCat.getSlaDays());
        Grievance grv2 = new Grievance(
                "GRV-000002",
                "Vikram Malhotra",
                "vikram.m@example.com",
                "9876541102",
                "Contaminated tap water with yellowish tint and foul smell affecting 20 households in the lane.",
                "Green Glen Layout, Block B, Ward 18",
                waterCat,
                waterDept,
                grv2Created,
                grv2Deadline
        );
        grv2.setStatus(GrievanceStatus.IN_PROGRESS);
        Grievance savedGrv2 = grievanceRepository.save(grv2);
        notificationRepository.save(new Notification(savedGrv2, "Grievance GRV-000002 assigned to Water Supply Department.", NotificationType.ASSIGNMENT));
        notificationRepository.save(new Notification(savedGrv2, "Grievance GRV-000002 status changed from ASSIGNED to IN_PROGRESS.", NotificationType.STATUS_CHANGE));

        // GRV-000003: ESCALATED (SLA BREACHED)
        LocalDateTime grv3Created = now.minusDays(3);
        LocalDateTime grv3Deadline = grv3Created.plusDays(garbageCat.getSlaDays()); // 1 day SLA -> breached 2 days ago
        Grievance grv3 = new Grievance(
                "GRV-000003",
                "Sneha Patil",
                "sneha.p@example.com",
                "9876541103",
                "Overflowing commercial garbage dump not cleared for 3 days attracting stray animals and pests.",
                "Market Road opposite Metro Pillar 45",
                garbageCat,
                sanitationDept,
                grv3Created,
                grv3Deadline
        );
        grv3.setStatus(GrievanceStatus.ESCALATED);
        Grievance savedGrv3 = grievanceRepository.save(grv3);

        Escalation esc3 = new Escalation(
                savedGrv3,
                "Zonal Senior Commissioner - Sanitation Department",
                grv3Deadline.plusMinutes(5),
                "SLA exceeded without resolution"
        );
        savedGrv3.setEscalation(escalationRepository.save(esc3));
        notificationRepository.save(new Notification(savedGrv3, "Grievance GRV-000003 has been automatically ESCALATED to Senior Officer due to SLA violation.", NotificationType.SLA_ESCALATION));

        // GRV-000004: ASSIGNED (ON TRACK)
        LocalDateTime grv4Created = now.minusHours(8);
        LocalDateTime grv4Deadline = grv4Created.plusDays(otherCat.getSlaDays());
        Grievance grv4 = new Grievance(
                "GRV-000004",
                "Manoj Joshi",
                "manoj.j@example.com",
                "9876541104",
                "Streetlight pole leaning dangerously over pedestrian footpath with exposed live electrical wires.",
                "Cross Road 7, Near Public Park Gate 2",
                otherCat,
                generalDept,
                grv4Created,
                grv4Deadline
        );
        grv4.setStatus(GrievanceStatus.ASSIGNED);
        Grievance savedGrv4 = grievanceRepository.save(grv4);
        notificationRepository.save(new Notification(savedGrv4, "Grievance GRV-000004 assigned to General Civic Department.", NotificationType.ASSIGNMENT));

        // GRV-000005: PENDING (DUE SOON)
        LocalDateTime grv5Created = now.minusHours(16);
        LocalDateTime grv5Deadline = grv5Created.plusDays(garbageCat.getSlaDays()); // 1 day SLA -> 8 hrs left
        Grievance grv5 = new Grievance(
                "GRV-000005",
                "Farhan Akhtar",
                "farhan.a@example.com",
                "9876541105",
                "Illegal dumping of construction debris blocking the main entrance of residential lane 4.",
                "12th Avenue, Sector 4, Outer Ring Road",
                garbageCat,
                sanitationDept,
                grv5Created,
                grv5Deadline
        );
        grv5.setStatus(GrievanceStatus.PENDING);
        grievanceRepository.save(grv5);

        // GRV-000006: RESOLVED & RATED
        LocalDateTime grv6Created = now.minusDays(4);
        LocalDateTime grv6Deadline = grv6Created.plusDays(waterCat.getSlaDays());
        Grievance grv6 = new Grievance(
                "GRV-000006",
                "Deepa Nair",
                "deepa.n@example.com",
                "9876541106",
                "Main water pipeline burst flooding the road with clean drinking water near traffic signal.",
                "Junction of 80ft Road and 14th Cross",
                waterCat,
                waterDept,
                grv6Created,
                grv6Deadline
        );
        grv6.setStatus(GrievanceStatus.RESOLVED);
        grv6.setResolvedAt(grv6Created.plusDays(1).plusHours(4));
        Grievance savedGrv6 = grievanceRepository.save(grv6);

        Rating r6 = new Rating(savedGrv6, 4, "Leakage was stopped within a day. Good coordination.", grv6.getResolvedAt().plusHours(3));
        savedGrv6.setRating(ratingRepository.save(r6));
        notificationRepository.save(new Notification(savedGrv6, "Grievance GRV-000006 status changed from IN_PROGRESS to RESOLVED.", NotificationType.STATUS_CHANGE));
        notificationRepository.save(new Notification(savedGrv6, "Citizen submitted a 4-star rating for resolved grievance GRV-000006.", NotificationType.RATING_SUBMITTED));

        log.info("GrievanceTrack seed data initialization finished successfully!");
    }
}
