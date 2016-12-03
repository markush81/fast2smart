package net.fast2smart.external.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.fast2smart.legacy.model.Member;
import org.springframework.hateoas.ResourceSupport;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by markus on 22/10/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalMember extends ResourceSupport {

    private Long memberId;
    private String lastname;
    private String firstname;
    private Card card;

    private LocalDateTime enrolmentDate;

    @SuppressWarnings({"squid:S1186"})
    public ExternalMember() {
    }

    @JsonCreator
    public ExternalMember(@JsonProperty(value = "lastname", required = true) String lastname, @JsonProperty(value = "firstname", required = true) String firstname, @JsonProperty(value = "card", required = true) Card card, @JsonProperty(value = "enrolmentDate", required = true) LocalDateTime enrolmentDate) {
        this(null, lastname, firstname, card, enrolmentDate);
    }

    public ExternalMember(Long memberId, String lastname, String firstname, Card card, LocalDateTime enrolmentDate) {
        this.memberId = memberId;
        this.lastname = lastname;
        this.firstname = firstname;
        this.card = card;
        this.enrolmentDate = enrolmentDate;
    }

    public static ExternalMember fromMember(Member member) {
        return new ExternalMember(member.getId(), member.getLastname(), member.getFirstname(), new Card(member.getCardnumber(), member.getPartner()), member.getEnrolmentDate());
    }

    @JsonIgnore
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public LocalDateTime getEnrolmentDate() {
        return enrolmentDate;
    }

    public void setEnrolmentDate(LocalDateTime enrolmentDate) {
        this.enrolmentDate = enrolmentDate;
    }

    public Member toMember() {
        return new Member(lastname, firstname, card.getNumber(), card.getPartner(), enrolmentDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalMember that = (ExternalMember) o;
        return Objects.equals(memberId, that.memberId) &&
                Objects.equals(lastname, that.lastname) &&
                Objects.equals(firstname, that.firstname) &&
                Objects.equals(card, that.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, lastname, firstname, card);
    }

    @Override
    public String toString() {
        return "ExternalMember{" +
                "memberId=" + memberId +
                ", lastname='" + lastname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", card=" + card +
                '}';
    }
}
