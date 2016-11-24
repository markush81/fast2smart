package net.fast2smart.external.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.fast2smart.legacy.model.Partner;
import net.fast2smart.legacy.model.Treatment;
import org.springframework.hateoas.ResourceSupport;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by markus on 22/10/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalTreatment extends ResourceSupport {

    private Long cardnumber;
    private Partner partner;
    private String headline;
    private LocalDateTime assigned;

    public ExternalTreatment() {
    }

    public ExternalTreatment(Long cardnumber, Partner partner, String headline, LocalDateTime assigned) {
        this.cardnumber = cardnumber;
        this.partner = partner;
        this.headline = headline;
        this.assigned = assigned;
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

    public static ExternalTreatment fromTreatment(Treatment treatment) {
        return new ExternalTreatment(treatment.getMember().getCardnumber(), treatment.getPartner(), treatment.getHeadline(), treatment.getAssigned());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ExternalTreatment externalTreatment = (ExternalTreatment) o;
        return Objects.equals(cardnumber, externalTreatment.cardnumber) &&
                partner == externalTreatment.partner &&
                Objects.equals(headline, externalTreatment.headline) &&
                Objects.equals(assigned, externalTreatment.assigned);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cardnumber, partner, headline, assigned);
    }

    @Override
    public String toString() {
        return "ExternalTreatment{" +
                "cardnumber=" + cardnumber +
                ", partner=" + partner +
                ", headline='" + headline + '\'' +
                ", assigned=" + assigned +
                '}';
    }

}
