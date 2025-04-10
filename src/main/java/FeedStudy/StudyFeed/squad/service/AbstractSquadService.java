package FeedStudy.StudyFeed.squad.service;

import FeedStudy.StudyFeed.squad.dto.SquadCreateRequestDto;
import FeedStudy.StudyFeed.global.service.RegionService;
import FeedStudy.StudyFeed.squad.entity.Squad;
import FeedStudy.StudyFeed.squad.entity.SquadMember;
import FeedStudy.StudyFeed.user.entity.User;
import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.MemberException;
import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
import FeedStudy.StudyFeed.squad.repository.SquadReportRepository;
import FeedStudy.StudyFeed.squad.repository.SquadRepository;
import FeedStudy.StudyFeed.user.repository.UserRepository;
import FeedStudy.StudyFeed.global.type.AttendanceStatus;
import FeedStudy.StudyFeed.global.type.RecruitStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractSquadService implements SquadServiceImpl {
    protected final SquadRepository squadRepository;
    protected final UserRepository userRepository;
    protected final SquadMemberRepository squadMemberRepository;
    protected final RegionService regionService;
    protected final SquadReportRepository squadReportRepository;

    protected Squad findSquadById(Long squadId) {
        return squadRepository.findById(squadId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임을 찾을수가 없습니다."));
    }

    protected User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 User을 찾을수가 없습니다."));
    }

    protected void validateSquadLeader(Squad squad, User user) {
        if (!squad.getLeader().equals(user)) {
            throw new IllegalArgumentException("모임장만 수행할 수 있습니다.");
        }
    }

    protected void validateTargetIsLeader(Squad squad, SquadMember squadMember) {
        if (squad.getLeader().equals(squadMember.getUser())) {
            throw new IllegalArgumentException("모임장은 자신을 퇴출할 수 없습니다.");
        }
    }

    protected User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    protected void validateAgeRestriction(SquadCreateRequestDto requestDto) {
        if (requestDto.getMinAge() != null && requestDto.getMaxAge() != null) {
            throw new SquadException(ErrorCode.UNABLE_TO_USE_MIN_MAX_AGE);
        }

    }



    protected void validateBySquadAndUser(Squad squad, User user) {
        if (squadMemberRepository.existsBySquadAndUser(squad, user)) {
            throw new IllegalArgumentException("이미 참가한 모임입니다.");
        }
    }

    protected SquadMember validateUserInSquad(Squad squad, User user) {
        return squadMemberRepository.findBySquadAndUser(squad, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 이 모임에 속하지 않습니다."));
    }


    protected void validatePeopleNum(Squad squad) {
        if (squad.getCurrentPeopleNum() >= squad.getPeopleNum()) {
            throw new IllegalArgumentException("모임 정원이 가득 차 있어 가입할 수 없습니다.");
        }

    }


    protected void validateGender(Squad squad, User newUser) {
        if (!squad.getSquadGender().matches(newUser.getGender())) {
            throw new SquadException(ErrorCode.GENDER_ERROR);
        }
    }


    protected void validateMinAge(Squad squad, int userAge) {
        if (squad.getMinAge() != null && userAge < squad.getMinAge()) {
            throw new IllegalArgumentException("가입 할수 있는 최소 나이보다 적습니다.");
        }

    }

    protected void validateMaxAge(Squad squad, int userAge) {
        if (squad.getMaxAge() != null && userAge > squad.getMaxAge()) {
            throw new IllegalArgumentException("가입 할수 있는 최대 나이를 초과했습니다.");
        }

    }

    protected SquadMember findSquadMemberById(Long squadMemberId) {
        return squadMemberRepository.findById(squadMemberId)
                .orElseThrow(() -> new IllegalArgumentException("참가 정보를 찾을 수 없습니다."));

    }

  protected void validateStatus(SquadMember squadMember) {
      if (squadMember.getAttendanceStatus() != AttendanceStatus.PENDING) {
          throw new IllegalArgumentException("대기 상태의 참가자만 승인할수 있습니다.");
      }
  }




  protected void validateCheckClosed(Squad squad) {
      if (squad.getRecruitStatus() == RecruitStatus.CLOSED) {
          throw new IllegalArgumentException("이미 모집이 마감된 모임입니다.");
      }

  }

  protected void validateBannedUser(SquadMember squadMember) {
      if (squadMember.getAttendanceStatus() == AttendanceStatus.BANNED) {
          throw new IllegalArgumentException("이미 강퇴된 유저입니다.");
      }
  }

  protected void validateReporter(User reporter, User reportedUser) {
      if (reporter.equals(reportedUser)) {
          throw new IllegalArgumentException("자신을 신고할 수 없습니다.");
      }
  }

  protected void validateReporterAndUserAndSquad(User reporter, User reportedUser, Squad squad) {
      if (squadReportRepository.existsByReporterAndReportedUserAndSquad(reporter, reportedUser, squad)) {
          throw new IllegalArgumentException("이미 해당 사용자를 신고하셨습니다.");
      }
  }







}
