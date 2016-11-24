package net.fast2smart.external.resource;

import net.fast2smart.external.model.AccountStatement;
import net.fast2smart.legacy.model.Account;
import net.fast2smart.legacy.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping
class AccountResource {

    @Autowired
    private AccountService accountService;

    @RequestMapping(path = {"/members/{cardnumber}/account"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AccountStatement> get(@PathVariable Long cardnumber) {
        Account account = accountService.getByCardnumber(cardnumber);
        if (account == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(enrichAccountStatement(AccountStatement.fromAccount(account)));
    }

    private AccountStatement enrichAccountStatement(AccountStatement result) {
        Long cardnumber = result.getCardnumber();
        result.add(linkTo(methodOn(AccountResource.class, cardnumber).get(cardnumber)).withSelfRel());
        result.add(linkTo(methodOn(MemberResource.class, cardnumber).get(cardnumber)).withRel("member"));
        result.add(linkTo(methodOn(PurchaseEventResource.class, cardnumber).getByCardNumber(cardnumber)).withRel("purchases"));
        return result;
    }
}
