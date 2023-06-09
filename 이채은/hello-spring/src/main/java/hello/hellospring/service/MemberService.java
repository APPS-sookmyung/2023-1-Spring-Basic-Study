package hello.hellospring.service;

import hello.hellospring.domain.Member;
import hello.hellospring.repository.MemberRepository;
import hello.hellospring.repository.MemoryMemberRepository;

import java.util.List;
import java.util.Optional;

public class MemberService {
        private final MemberRepository memberRepository=new MemoryMemberRepository();

    public MemberService(MemberRepository memberRepository) {
    }

    public Long join(Member member){
            //같은 이름이 있늕 중복회원은 가입불가
            validateDuplicateMember(member);
            memberRepository.save(member);
            return member.getId();
        }

    private void validateDuplicateMember(Member member) {
        memberRepository.findByName(member.getName())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 회원입니다.");
        });
    }
    public List<Member>findMembers(){
            return memberRepository.findAll();
    }
    public  Optional<Member>findOne(Long memberId){
            return memberRepository.findById(memberId);
    }
}
