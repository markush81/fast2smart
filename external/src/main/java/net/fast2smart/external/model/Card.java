package net.fast2smart.external.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.fast2smart.legacy.model.Partner;
import org.springframework.hateoas.ResourceSupport;

import java.util.Objects;

/**
 * Created by markus on 22/10/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card extends ResourceSupport {

    private Long number;
    private Partner partner;

    @SuppressWarnings({"squid:S1186"})
    public Card() {
    }

    @JsonCreator
    public Card(@JsonProperty(value = "number", required = true) Long number, @JsonProperty(value = "partner", required = true) Partner partner) {
        this.number = number;
        this.partner = partner;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Card card = (Card) o;
        return Objects.equals(number, card.number) &&
                partner == card.partner;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, partner);
    }

    @Override
    public String toString() {
        return "Card{" +
                "number=" + number +
                ", partner=" + partner +
                '}';
    }
}
