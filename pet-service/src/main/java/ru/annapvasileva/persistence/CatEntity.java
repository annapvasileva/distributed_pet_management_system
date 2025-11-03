package ru.annapvasileva.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import ru.annapvasileva.cats.Colors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name="cats")
@NoArgsConstructor() // Lombok will create a constructor
@AllArgsConstructor()
public class CatEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @Column(name="name", nullable=false)
    private String name;

    @Column(name="date_of_birth", nullable=false)
    private LocalDate birthDate;

    @Column(name="breed", nullable=false)
    private String breed;

    @Enumerated(EnumType.STRING)
    private Colors color;

    @Column(name="owner_id", nullable=false)
    private UUID ownerId;

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "cat_friends",
            joinColumns = @JoinColumn(name = "first_cat_id"),
            inverseJoinColumns = @JoinColumn(name = "second_cat_id")
    )
    private List<CatEntity> friends = new ArrayList<>();
}
