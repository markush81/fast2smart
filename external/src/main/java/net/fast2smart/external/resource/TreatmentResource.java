package net.fast2smart.external.resource;

import com.google.common.collect.Lists;
import net.fast2smart.external.model.ExternalTreatment;
import net.fast2smart.legacy.model.Treatment;
import net.fast2smart.legacy.service.TreatmentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping
class TreatmentResource {

    private TreatmentService treatmentService;

    public TreatmentResource(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @RequestMapping(path = {"/members/{cardnumber}/treatments"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<ExternalTreatment>> get(@PathVariable Long cardnumber) {
        Iterable<Treatment> treatments = treatmentService.getByCardnumber(cardnumber);
        return ResponseEntity.ok(Lists.newArrayList(treatments).stream().map(ExternalTreatment::fromTreatment).map(this::enrichExternalTreatment).collect(Collectors.toList()));
    }

    private ExternalTreatment enrichExternalTreatment(ExternalTreatment result) {
        Long cardnumber = result.getCardnumber();
        result.add(linkTo(methodOn(TreatmentResource.class, cardnumber).get(cardnumber)).withSelfRel());
        result.add(linkTo(methodOn(MemberResource.class, cardnumber).get(cardnumber)).withRel("member"));
        return result;
    }
}
