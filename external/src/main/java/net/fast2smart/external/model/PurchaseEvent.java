package net.fast2smart.external.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.model.Partner;
import net.fast2smart.legacy.model.Purchase;
import org.springframework.hateoas.ResourceSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Objects;

/**
 * Created by markus on 22/10/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseEvent extends ResourceSupport {

    private Long cardnumber;
    private Partner partner;
    private BigDecimal amount;
    private Currency currency;
    private Long basePoints;
    private Long statusPoints;
    private Long memberId;
    private LocalDateTime date;

    public PurchaseEvent() {
    }

    @JsonCreator
    public PurchaseEvent(@JsonProperty(value = "cardnumber", required = true) Long cardnumber, @JsonProperty(value = "partner", required = true) Partner partner, @JsonProperty(value = "amount", required = true) BigDecimal amount, @JsonProperty(value = "currency", defaultValue = "EUR") Currency currency, @JsonProperty(value = "basePoints", required = true) Long basePoints, @JsonProperty(value = "statusPoints", defaultValue = "0") Long statusPoints, @JsonProperty(value = "date", required = true) LocalDateTime date) {
        this(null, cardnumber, partner, amount, currency, basePoints, statusPoints, date);
    }

    public PurchaseEvent(Long memberId, Long cardnumber, Partner partner, BigDecimal amount, Currency currency, Long basePoints, Long statusPoints, LocalDateTime date) {
        this.cardnumber = cardnumber;
        this.partner = partner;
        this.amount = amount;
        this.currency = currency;
        this.basePoints = basePoints;
        this.statusPoints = statusPoints;
        this.memberId = memberId;
        this.date = date;
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

    public Long getstatusPoints() {
        return statusPoints;
    }

    public void setstatusPoints(Long statusPoints) {
        this.statusPoints = statusPoints;
    }

    @JsonIgnore
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Purchase toPurchase(Member member) {
        return new Purchase(member, partner, amount, currency, basePoints, statusPoints, date);
    }

    public static PurchaseEvent fromPurchase(Purchase purchase) {
        return new PurchaseEvent(purchase.getMember().getId(), purchase.getMember().getCardnumber(), purchase.getPartner(), purchase.getAmount(), purchase.getCurrency(), purchase.getBasePoints(), purchase.getStatusPoints(), purchase.getDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseEvent that = (PurchaseEvent) o;
        return Objects.equals(cardnumber, that.cardnumber) &&
                partner == that.partner &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(currency, that.currency) &&
                Objects.equals(basePoints, that.basePoints) &&
                Objects.equals(statusPoints, that.statusPoints) &&
                Objects.equals(memberId, that.memberId) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardnumber, partner, amount, currency, basePoints, statusPoints, memberId, date);
    }

    @Override
    public String toString() {
        return "PurchaseEvent{" +
                "cardnumber=" + cardnumber +
                ", partner=" + partner +
                ", amount=" + amount +
                ", currency=" + currency +
                ", basePoints=" + basePoints +
                ", statusPoints=" + statusPoints +
                ", memberId=" + memberId +
                ", date=" + date +
                '}';
    }
}
