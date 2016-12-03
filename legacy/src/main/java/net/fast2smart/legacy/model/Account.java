package net.fast2smart.legacy.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by markus on 11/11/2016.
 */
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne(optional = false)
    private Member member;
    @Column(nullable = false)
    private Long basePoints;
    @Column(nullable = false)
    private Long statusPoints;
    @Column(nullable = false)
    private LocalDateTime lastUpdate;

    @SuppressWarnings({"squid:S1186"})
    public Account() {
    }

    public Account(Member member) {
        this(null, member, 0L, 0L, LocalDateTime.now());
    }

    public Account(Member member, LocalDateTime lastUpdate) {
        this(null, member, 0L, 0L, lastUpdate);
    }

    public Account(Member member, Long basePoints, Long statusPoints, LocalDateTime lastUpdate) {
        this(null, member, basePoints, statusPoints, lastUpdate);
    }

    public Account(Long id, Member member, Long basePoints, Long statusPoints, LocalDateTime lastUpdate) {
        this.id = id;
        this.member = member;
        this.basePoints = basePoints;
        this.statusPoints = statusPoints;
        this.lastUpdate = lastUpdate;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Long getBasePoints() {
        return basePoints;
    }

    public void setBasePoints(Long basePoints) {
        this.basePoints = basePoints;
    }

    public Long getstatusPoints() {
        return statusPoints;
    }

    public void setstatusPoints(Long statusPoints) {
        this.statusPoints = statusPoints;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void update(Long basePoints, Long statusPoints, LocalDateTime purchaseDate) {
        this.basePoints = this.basePoints + basePoints;
        this.statusPoints = this.statusPoints + statusPoints;
        this.lastUpdate = purchaseDate;
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
        Account account = (Account) o;
        return Objects.equals(id, account.id) &&
                Objects.equals(member, account.member) &&
                Objects.equals(basePoints, account.basePoints) &&
                Objects.equals(statusPoints, account.statusPoints) &&
                Objects.equals(lastUpdate, account.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, basePoints, statusPoints, lastUpdate);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", member=" + member +
                ", basePoints=" + basePoints +
                ", statusPoints=" + statusPoints +
                ", lastUpdate=" + lastUpdate +
                '}';
    }


}
