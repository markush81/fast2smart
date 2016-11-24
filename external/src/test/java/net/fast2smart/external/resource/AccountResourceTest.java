package net.fast2smart.external.resource;

import net.fast2smart.external.AbstractMvcTest;
import net.fast2smart.legacy.model.Account;
import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.model.Partner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by markus on 22/10/2016.
 */
@WebMvcTest
@RunWith(SpringRunner.class)
public class AccountResourceTest extends AbstractMvcTest {

    @Test
    public void getAccount() throws Exception {
        LocalDateTime enrolmentDate = LocalDateTime.now();
        LocalDateTime lastUpdate = LocalDateTime.now();
        Member member = new Member(1L, "Helbig", "Markus", 3204015863694L, Partner.HOLIDAY, enrolmentDate);
        when(accountService.getByCardnumber(eq(3204015863694L))).thenReturn(new Account(member, 45L, 100L, lastUpdate));

        mvc.perform(get("/members/3204015863694/account"))
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("{\"cardnumber\": 3204015863694, \"basePoints\": 45, \"statusPoints\": 100, \"lastUpdate\": \"%s\"}", lastUpdate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(accountService, times(1)).getByCardnumber(eq(3204015863694L));
    }

    @Test
    public void getUnknownMember() throws Exception {
        when(accountService.getByCardnumber(eq(3204015863694L))).thenReturn(null);

        mvc.perform(get("/members/3204015863694/account"))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getByCardnumber(eq(3204015863694L));
    }
}