package net.fast2smart.external.resource;

import com.google.common.collect.Lists;
import net.fast2smart.external.AbstractMvcTest;
import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.model.Partner;
import net.fast2smart.legacy.model.Treatment;
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
public class TreatmentResourceTest extends AbstractMvcTest {

    @Test
    public void getTreatments() throws Exception {
        LocalDateTime enrolmentDate = LocalDateTime.now();
        LocalDateTime assignDate = LocalDateTime.now();
        Member member = new Member(1L, "Helbig", "Markus", 3204015863694L, Partner.HOLIDAY, enrolmentDate);
        when(treatmentService.getByCardnumber(eq(3204015863694L))).thenReturn(Lists.newArrayList(new Treatment(member, Partner.SUPERMARKET, "5x Points Booster", assignDate)));

        mvc.perform(get("/members/3204015863694/treatments"))
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("[{\"cardnumber\": 3204015863694, \"partner\": \"SUPERMARKET\", \"headline\": \"5x Points Booster\", \"assigned\": \"%s\"}]", assignDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(treatmentService, times(1)).getByCardnumber(eq(3204015863694L));
    }

    @Test
    public void getEmptyList() throws Exception {
        when(treatmentService.getByCardnumber(eq(3204015863694L))).thenReturn(Lists.newArrayList());

        mvc.perform(get("/members/3204015863694/treatments"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(treatmentService, times(1)).getByCardnumber(eq(3204015863694L));
    }
}