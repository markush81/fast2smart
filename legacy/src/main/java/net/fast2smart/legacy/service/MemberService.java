package net.fast2smart.legacy.service;

import net.fast2smart.legacy.model.Account;
import net.fast2smart.legacy.model.Member;
import net.fast2smart.legacy.repository.AccountRepository;
import net.fast2smart.legacy.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by markus on 10/11/2016.
 */
@Service
public class MemberService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Transactional
    public Member enrolMember(Member member) {
        Member result = memberRepository.save(member);
        accountRepository.save(new Account(result, member.getEnrolmentDate()));
        return result;
    }

    public Member getByCardnumber(Long cardnumber) {
        return memberRepository.findByCardnumber(cardnumber);
    }

    public Iterable<Member> getAll() {
        return memberRepository.findAll();
    }

    public Member getById(Long id) {
        return memberRepository.findOne(id);
    }
}
