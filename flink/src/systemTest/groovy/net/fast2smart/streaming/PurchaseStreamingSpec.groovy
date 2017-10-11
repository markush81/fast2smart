package net.fast2smart.streaming

import groovy.sql.Sql
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Systemtest for all variations of Streaming Job
 */
class PurchaseStreamingSpec extends Specification {

    def conditions = new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25)

    @Shared
            sql = Sql.newInstance("jdbc:h2:../db/fast2smart;AUTO_SERVER=TRUE", "sa", "", "org.h2.Driver")

    @Shared
            cqlsh = Sql.newInstance("jdbc:c*:datastax://localhost/fast2smart", "cassandra", "cassandra", "com.github.cassandra.jdbc.CassandraDriver")

    def "enroled member should get a new delta entry"() {
        given: "clean database for this member"
        def card = 3204728185984
        cleanDb(card)
        and: "fresh enroled member"
        def member = enrolMember(card)
        def memberId = memberId(member.card.number)
        and: "member does a purchase"
        purchase(member.card.number)
        expect: "correct account balances"
        def account = account(member.card.number)
        assert account.basePoints == 5
        and: "there should now be a delta entry"
        conditions.eventually {
            def deltas = deltas(memberId)
            deltas.size() == 1
            assert deltas[0].amount == 50
        }
    }

    def "enroled member with given deltas before purchase should have higher delta"() {
        given: "clean database for this member"
        def card = 3204728185984
        cleanDb(card)
        and: "fresh enroled member"
        def member = enrolMember(card)
        def memberId = memberId(member.card.number)
        and: "already given delta"
        insertPresentDelta(memberId, 100, LocalDateTime.now())
        and: "member does a purchase"
        purchase(member.card.number)
        expect: "correct account balances"
        def account = account(member.card.number)
        assert account.basePoints == 5
        and: "there should now be a delta entry"
        conditions.eventually {
            def deltas = deltas(memberId)
            deltas.size() == 1
            assert deltas[0].amount == 150
        }
    }

    def "enroled member with given deltas after purchase should have same delta"() {
        given: "clean database for this member"
        def card = 3204728185984
        cleanDb(card)
        and: "fresh enroled member"
        def member = enrolMember(card)
        def memberId = memberId(member.card.number)
        and: "already given delta"
        insertPresentDelta(memberId, 100, LocalDateTime.now().plusDays(1))
        and: "member does a purchase"
        purchase(member.card.number)
        expect: "correct account balances"
        def account = account(member.card.number)
        assert account.basePoints == 5
        and: "there should now be a delta entry"
        conditions.eventually {
            def deltas = deltas(memberId)
            deltas.size() == 1
            assert deltas[0].amount == 100
        }
    }

    def "enroled member with no deltas yet but aggregated amount should get a treatment and have a new delta"() {
        given: "clean database for this member"
        def card = 3204728185984
        cleanDb(card)
        and: "fresh enroled member"
        def member = enrolMember(card)
        def memberId = memberId(member.card.number)
        and: "already given aggregated amount yeserterday"
        insertPresentBatch(memberId, 150, LocalDateTime.now().minusDays(1))
        and: "member does a purchase"
        purchase(member.card.number)
        expect: "correct account balances"
        def account = account(member.card.number)
        assert account.basePoints == 5
        and: "there should now be a delta entry"
        conditions.eventually {
            def deltas = deltas(memberId)
            deltas.size() == 1
            assert deltas[0].amount == 50

            def treatments = treatments(memberId)
            assert treatments.size() == 1
        }
    }

    def "enroled member with no deltas yet but aggregated amount should not get a treatment but have a new delta"() {
        given: "clean database for this member"
        def card = 3204728185984
        cleanDb(card)
        and: "fresh enroled member"
        def member = enrolMember(card)
        def memberId = memberId(member.card.number)
        and: "already given aggregated amount yeserterday"
        insertPresentBatch(memberId, 149, LocalDateTime.now().minusDays(1))
        and: "member does a purchase"
        purchase(member.card.number)
        expect: "correct account balances"
        def account = account(member.card.number)
        assert account.basePoints == 5
        and: "there should now be a delta entry"
        conditions.eventually {
            def deltas = deltas(memberId)
            deltas.size() == 1
            assert deltas[0].amount == 50

            def treatments = treatments(memberId)
            assert treatments.size() == 0
        }
    }

    def "enroled member with 'future' delta and aggregated amount should not get a treatment and have same delta"() {
        given: "clean database for this member"
        def card = 3204728185984
        cleanDb(card)
        and: "fresh enroled member"
        def member = enrolMember(card)
        def memberId = memberId(member.card.number)
        and: "already given delta"
        insertPresentDelta(memberId, 100, LocalDateTime.now().plusDays(1))
        and: "already given aggregated amount yeserterday"
        insertPresentBatch(memberId, 50, LocalDateTime.now().minusDays(1))
        and: "member does a purchase"
        purchase(member.card.number)
        expect: "correct account balances"
        def account = account(member.card.number)
        assert account.basePoints == 5
        and: "there should now be a delta entry"
        conditions.eventually {
            def deltas = deltas(memberId)
            deltas.size() == 1
            assert deltas[0].amount == 100

            def treatments = treatments(memberId)
            assert treatments.size() == 0
        }
    }

    def "enroled member with delta and aggregated amount should get a treatment and have an updated delta"() {
        given: "clean database for this member"
        def card = 3204728185984
        cleanDb(card)
        and: "fresh enroled member"
        def member = enrolMember(card)
        def memberId = memberId(member.card.number)
        and: "already given delta"
        insertPresentDelta(memberId, 100, LocalDateTime.now())
        and: "already given aggregated amount yeserterday"
        insertPresentBatch(memberId, 50, LocalDateTime.now().minusDays(1))
        and: "member does a purchase"
        purchase(member.card.number)
        expect: "correct account balances"
        def account = account(member.card.number)
        assert account.basePoints == 5
        and: "there should now be a delta entry"
        conditions.eventually {
            def deltas = deltas(memberId)
            deltas.size() == 1
            assert deltas[0].amount == 150

            def treatments = treatments(memberId)
            assert treatments.size() == 1
        }
    }

    def "enroled member with newer delta and aggregated amount should not get a treatment but have an updated delta"() {
        given: "clean database for this member"
        def card = 3204728185984
        cleanDb(card)
        and: "fresh enroled member"
        def member = enrolMember(card)
        def memberId = memberId(member.card.number)
        and: "already given delta but yesterday"
        insertPresentDelta(memberId, 100, LocalDateTime.now().plusDays(1))
        and: "already given aggregated amount yeserterday"
        insertPresentBatch(memberId, 49, LocalDateTime.now().minusDays(1))
        and: "member does a purchase"
        purchase(member.card.number)
        expect: "correct account balances"
        def account = account(member.card.number)
        assert account.basePoints == 5
        and: "there should now be a delta entry"
        conditions.eventually {
            def deltas = deltas(memberId)
            deltas.size() == 1
            assert deltas[0].amount == 100

            def treatments = treatments(memberId)
            assert treatments.size() == 0
        }
    }

    //HELPER SECTION
    def enrolMember(card) {
        def http = new RESTClient('http://localhost:8080')
        http.request(Method.POST, ContentType.JSON) { req ->
            uri.path = '/members'
            body = [lastname: 'Mustermann', firstname: 'Max', card: [number: card, partner: 'BOOKS'], enrolmentDate: LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)]
            response.success = { resp, json ->
                return json
            }
            response.failure = { resp ->
                println resp.status
            }
        }
    }

    def purchase(card, amount = 4.99, basePoints = 5, statusPoints = 45, partner = 'BOOKS', date = LocalDateTime.now()) {
        def http = new RESTClient('http://localhost:8080')
        http.request(Method.POST, ContentType.JSON) { req ->
            uri.path = '/purchases'
            body = [cardnumber: card, partner: partner, date: date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), amount: amount, currency: 'EUR', basePoints: basePoints, statusPoints: statusPoints]
            response.success = { resp, json ->
                return json
            }
            response.failure = { resp ->
                println resp.status
            }
        }
    }


    def account(card) {
        def http = new RESTClient('http://localhost:8080')
        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = "/members/$card/account"
            response.success = { resp, json ->
                return json
            }
            response.failure = { resp ->
                println resp.status
            }
        }
    }

    def insertPresentDelta(memberId, amount, date) {
        cqlsh.execute("INSERT INTO fast2smart.member_delta_balance (member,year,month,amount,maxdate) VALUES(${memberId},${date.getYear()},${date.getMonthValue()},${amount},${date.toString()})")
    }

    def insertPresentBatch(memberId, amount, date) {
        cqlsh.execute("INSERT INTO fast2smart.member_monthly_balance (member,year,month,amount,maxdate) VALUES(${memberId},${date.getYear()},${date.getMonthValue()},${amount},${date.toString()})")
    }

    def deltas(memberId) {
        cqlsh.rows("SELECT * FROM fast2smart.member_delta_balance WHERE member = ${memberId};")
    }

    def treatments(memberId) {
        sql.rows("SELECT * FROM treatment WHERE member_id = ${memberId};")
    }

    def memberId(card) {
        def rows = sql.rows("SELECT id FROM member WHERE cardnumber = $card;")
        if (rows.size() > 0) {
            rows[0].id
        } else {
            null
        }
    }

    def cleanDb(card) {
        def memberId = memberId(card)
        if (memberId != null) {
            sql.execute("DELETE FROM account WHERE member_id = $memberId;")
            sql.execute("DELETE FROM purchase WHERE member_id = $memberId;")
            sql.execute("DELETE FROM treatment WHERE member_id = $memberId;")
            sql.execute("DELETE FROM member WHERE id = $memberId;")
            cqlsh.execute("DELETE FROM fast2smart.member_delta_balance WHERE member = $memberId;")
            cqlsh.execute("DELETE FROM fast2smart.member_monthly_balance WHERE member = $memberId;")
        }
    }
}