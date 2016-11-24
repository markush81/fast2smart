package net.fast2smart.legacy.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by markus on 11/11/2016.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "partner", "headline"}))
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long _id;
    @OneToOne(optional = false)
    private Member member;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Partner partner;
    @Column(nullable = false)
    private String headline;
    @Column(nullable = false)
    private LocalDateTime assigned;

    public Treatment() {
    }

    public Treatment(Member member, Partner partner, String headline, LocalDateTime assigned) {
        this.member = member;
        this.partner = partner;
        this.headline = headline;
        this.assigned = assigned;
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long _id) {
        this._id = _id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public LocalDateTime getAssigned() {
        return assigned;
    }

    public void setAssigned(LocalDateTime assigned) {
        this.assigned = assigned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Treatment treatment = (Treatment) o;
        return Objects.equals(_id, treatment._id) &&
                Objects.equals(member, treatment.member) &&
                partner == treatment.partner &&
                Objects.equals(headline, treatment.headline) &&
                Objects.equals(assigned, treatment.assigned);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, member, partner, headline, assigned);
    }

    @Override
    public String toString() {
        return "Treatment{" +
                "_id=" + _id +
                ", member=" + member +
                ", partner=" + partner +
                ", headline='" + headline + '\'' +
                ", assigned=" + assigned +
                '}';
    }
}
