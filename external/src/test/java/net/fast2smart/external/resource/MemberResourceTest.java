package net.fast2smart.external.resource;

import com.jayway.jsonassert.JsonAssert;
import net.fast2smart.external.AbstractMvcTest;
import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.model.Partner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by markus on 22/10/2016.
 */
@WebMvcTest
@RunWith(SpringRunner.class)
public class MemberResourceTest extends AbstractMvcTest {

    @Test
    public void create() throws Exception {
        LocalDateTime enrolmentDate = LocalDateTime.now();
        String createdMember = String.format("{\"lastname\":\"Helbig\",\"firstname\":\"Markus\",\"card\":{\"number\":3207595640976,\"partner\":\"HOLIDAY\"},\"enrolmentDate\":\"%s\"}", enrolmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        when(memberService.enrolMember(any(Member.class))).thenReturn(new Member(1L, "Helbig", "Markus", 3207595640976L, Partner.HOLIDAY, enrolmentDate));

        mvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON).content(String.format("{\"lastname\":\"Helbig\",\"firstname\":\"Markus\",\"card\":{\"number\":3207595640976,\"partner\":\"HOLIDAY\"},\"enrolmentDate\":\"%s\"}", enrolmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(status().isOk())
                .andExpect(content().json(createdMember));

        verify(memberService, times(1)).enrolMember(eq(new Member("Helbig", "Markus", 3207595640976L, Partner.HOLIDAY, enrolmentDate)));
        verify(kafkaTemplate, times(1)).send(eq("enrolments"), eq(1L), argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object item) {
                JsonAssert.with((String) item)
                        .assertEquals("id", 1).and()
                        .assertEquals("lastname", "Helbig").and()
                        .assertEquals("firstname", "Markus").and()
                        .assertEquals("cardnumber", 3207595640976L).and()
                        .assertEquals("partner", "HOLIDAY").and()
                        .assertEquals("enrolmentDate", enrolmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                return true; //safe, since JsonAssert will fail early
            }
        }));
    }

}