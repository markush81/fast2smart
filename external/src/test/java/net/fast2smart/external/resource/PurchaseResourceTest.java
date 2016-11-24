package net.fast2smart.external.resource;

import com.jayway.jsonassert.JsonAssert;
import net.fast2smart.external.AbstractMvcTest;
import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.model.Partner;
import net.fast2smart.legacy.model.Purchase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;

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
public class PurchaseResourceTest extends AbstractMvcTest {

    @Test
    public void create() throws Exception {
        LocalDateTime enrolmentDate = LocalDateTime.now();
        LocalDateTime purchaseDate = LocalDateTime.now();
        when(memberService.getByCardnumber(eq(3204015863694L))).thenReturn(new Member(1L, "Helbig", "Markus", 3204015863694L, Partner.HOLIDAY, enrolmentDate));
        when(accountService.bookPurchase(any(Purchase.class))).thenReturn(new Purchase(1L, new Member(1L, "Helbig", "Markus", 3204015863694L, Partner.HOLIDAY, enrolmentDate), Partner.HOLIDAY, BigDecimal.valueOf(4.99), Currency.getInstance("EUR"), 5L, 45L, purchaseDate));

        String createdPurchase = String.format("{\"cardnumber\":3204015863694,\"partner\":\"HOLIDAY\",\"amount\":4.99,\"currency\":\"EUR\",\"basePoints\":5,\"statusPoints\":45,\"date\":\"%s\"}", purchaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        mvc.perform(post("/purchases").contentType(MediaType.APPLICATION_JSON).content(String.format("{\"cardnumber\":3204015863694,\"partner\":\"HOLIDAY\",\"amount\":4.99,\"currency\":\"EUR\",\"basePoints\":5,\"statusPoints\":45,\"date\":\"%s\"}", purchaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(status().isOk())
                .andExpect(content().json(createdPurchase));

        verify(memberService, times(1)).getByCardnumber(eq(3204015863694L));
        verify(accountService, times(1)).bookPurchase(eq(new Purchase(new Member(1L, "Helbig", "Markus", 3204015863694L, Partner.HOLIDAY, enrolmentDate), Partner.HOLIDAY, BigDecimal.valueOf(4.99), Currency.getInstance("EUR"), 5L, 45L, purchaseDate)));
        verify(kafkaProducer, times(1)).send(argThat(new ArgumentMatcher<ProducerRecord<Long, String>>() {
            @Override
            public boolean matches(Object item) {
                @SuppressWarnings("unchecked") ProducerRecord<Long, String> producerRecord = (ProducerRecord<Long, String>) item;
                JsonAssert.with(producerRecord.value())
                        .assertEquals("member.id", 1).and()
                        .assertEquals("member.cardnumber", 3204015863694L).and()
                        .assertEquals("currency", "EUR").and()
                        .assertEquals("basePoints", 5).and()
                        .assertEquals("statusPoints", 45).and()
                        .assertEquals("amount", 4.99).and()
                        .assertEquals("date", purchaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                return producerRecord.topic().equals("purchases"); //save, since JsonAssert will fail early
            }
        }));
    }

    @Test
    public void createUnknownMember() throws Exception {
        when(memberService.getByCardnumber(eq(3204015863694L))).thenReturn(null);

        mvc.perform(post("/purchases").contentType(MediaType.APPLICATION_JSON).content("{\"cardnumber\":3204015863694,\"partner\":\"HOLIDAY\",\"amount\":4.99,\"currency\":\"EUR\",\"basePoints\":5,\"statusPoints\":45, \"date\":\"2014-06-19T22:07:00\"}"))
                .andExpect(status().isBadRequest());

        verify(memberService, times(1)).getByCardnumber(eq(3204015863694L));
        verify(accountService, times(0)).bookPurchase(any(Purchase.class));
        verify(kafkaTemplate, times(0)).send(any(), any(), any());
    }

}