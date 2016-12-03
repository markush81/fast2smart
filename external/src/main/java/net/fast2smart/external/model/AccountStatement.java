package net.fast2smart.external.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.fast2smart.legacy.model.Account;
import org.springframework.hateoas.ResourceSupport;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by markus on 22/10/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountStatement extends ResourceSupport {

    private Long cardnumber;
    private Long basePoints;
    private Long statusPoints;
    private LocalDateTime lastUpdate;

    @SuppressWarnings({"squid:S1186"})
    public AccountStatement() {
    }

    public AccountStatement(Long cardnumber, Long basePoints, Long statusPoints, LocalDateTime lastUpdate) {
        this.cardnumber = cardnumber;
        this.basePoints = basePoints;
        this.statusPoints = statusPoints;
        this.lastUpdate = lastUpdate;
    }

    public static AccountStatement fromAccount(Account account) {
        return new AccountStatement(account.getMember().getCardnumber(), account.getBasePoints(), account.getstatusPoints(), account.getLastUpdate());
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

    public Long getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(Long cardnumber) {
        this.cardnumber = cardnumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AccountStatement that = (AccountStatement) o;
        return Objects.equals(cardnumber, that.cardnumber) &&
                Objects.equals(basePoints, that.basePoints) &&
                Objects.equals(statusPoints, that.statusPoints) &&
                Objects.equals(lastUpdate, that.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), basePoints, statusPoints, lastUpdate);
    }

    @Override
    public String toString() {
        return "AccountStatement{" +
                "basePoints=" + basePoints +
                ", statusPoints=" + statusPoints +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
