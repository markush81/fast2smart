package net.fast2smart.legacy.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Objects;

/**
 * Created by markus on 22/10/2016.
 */
@Entity
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(optional = false)
    private Member member;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Partner partner;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private Currency currency;
    @Column(nullable = false)
    private Long basePoints;
    private Long statusPoints;
    @Column(nullable = false)
    private LocalDateTime date;

    @SuppressWarnings({"squid:S1186"})
    public Purchase() {
    }

    public Purchase(Member member, Partner partner, BigDecimal amount, Currency currency, Long basePoints, LocalDateTime date) {
        this(null, member, partner, amount, currency, basePoints, null, date);
    }

    public Purchase(Member member, Partner partner, BigDecimal amount, Currency currency, Long basePoints, Long statusPoints, LocalDateTime date) {
        this(null, member, partner, amount, currency, basePoints, statusPoints, date);
    }

    @SuppressWarnings({"squid:S00107"})
    public Purchase(Long id, Member member, Partner partner, BigDecimal amount, Currency currency, Long basePoints, Long statusPoints, LocalDateTime date) {
        this.id = id;
        this.member = member;
        this.partner = partner;
        this.amount = amount;
        this.currency = currency;
        this.basePoints = basePoints;
        this.statusPoints = statusPoints;
        this.date = date;
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

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Long getBasePoints() {
        return basePoints;
    }

    public void setBasePoints(Long basePoints) {
        this.basePoints = basePoints;
    }

    public Long getStatusPoints() {
        return statusPoints;
    }

    public void setStatusPoints(Long statusPoints) {
        this.statusPoints = statusPoints;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
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
        Purchase purchase = (Purchase) o;
        return Objects.equals(id, purchase.id) &&
                Objects.equals(member, purchase.member) &&
                partner == purchase.partner &&
                Objects.equals(amount, purchase.amount) &&
                Objects.equals(currency, purchase.currency) &&
                Objects.equals(basePoints, purchase.basePoints) &&
                Objects.equals(statusPoints, purchase.statusPoints) &&
                Objects.equals(date, purchase.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, partner, amount, currency, basePoints, statusPoints, date);
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "id=" + id +
                ", member=" + member +
                ", partner=" + partner +
                ", amount=" + amount +
                ", currency=" + currency +
                ", basePoints=" + basePoints +
                ", statusPoints=" + statusPoints +
                ", date=" + date +
                '}';
    }
}
