package net.fast2smart.legacy.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by markus on 22/10/2016.
 */
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String lastname;
    @Column(nullable = false)
    private String firstname;
    @Column(nullable = false, unique = true)
    private Long cardnumber;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Partner partner;
    @Column(nullable = false)
    private LocalDateTime enrolmentDate;

    @SuppressWarnings({"squid:S1186"})
    public Member() {
    }

    public Member(String lastname, String firstname, Long cardnumber, Partner partner, LocalDateTime enrolmentDate) {
        this(null, lastname, firstname, cardnumber, partner, enrolmentDate);
    }

    public Member(Long id, String lastname, String firstname, Long cardnumber, Partner partner, LocalDateTime enrolmentDate) {
        this.id = id;
        this.lastname = lastname;
        this.firstname = firstname;
        this.cardnumber = cardnumber;
        this.partner = partner;
        this.enrolmentDate = enrolmentDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public Long getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(Long cardnumber) {
        this.cardnumber = cardnumber;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public LocalDateTime getEnrolmentDate() {
        return enrolmentDate;
    }

    public void setEnrolmentDate(LocalDateTime enrolmentDate) {
        this.enrolmentDate = enrolmentDate;
    }

    @Override
    @SuppressWarnings({"squid:S1067"})
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(id, member.id) &&
                Objects.equals(lastname, member.lastname) &&
                Objects.equals(firstname, member.firstname) &&
                Objects.equals(cardnumber, member.cardnumber) &&
                partner == member.partner &&
                Objects.equals(enrolmentDate, member.enrolmentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastname, firstname, cardnumber, partner, enrolmentDate);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", lastname='" + lastname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", cardnumber=" + cardnumber +
                ", partner=" + partner +
                ", enrolmentDate=" + enrolmentDate +
                '}';
    }
}
