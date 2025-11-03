    package FeedStudy.StudyFeed.squad.service;

    import FeedStudy.StudyFeed.block.entity.Block;
    import FeedStudy.StudyFeed.block.repository.BlockRepository;
    import FeedStudy.StudyFeed.global.config.DistributeLock;
    import FeedStudy.StudyFeed.global.dto.DataResponse;
    import FeedStudy.StudyFeed.global.exception.ErrorCode;
    import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
    import FeedStudy.StudyFeed.global.jwt.JwtUtil;
    import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
    import FeedStudy.StudyFeed.global.service.RegionService;
    import FeedStudy.StudyFeed.global.type.Gender;
    import FeedStudy.StudyFeed.global.type.JoinType;
    import FeedStudy.StudyFeed.global.type.MembershipStatus;
    import FeedStudy.StudyFeed.squad.dto.*;
    import FeedStudy.StudyFeed.squad.entity.Squad;
    import FeedStudy.StudyFeed.squad.entity.SquadMember;
    import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
    import FeedStudy.StudyFeed.squad.repository.SquadRepository;
    import FeedStudy.StudyFeed.user.dto.UserSimpleDto;
    import FeedStudy.StudyFeed.user.entity.User;
    import FeedStudy.StudyFeed.user.repository.UserRepository;
    import jakarta.transaction.Transactional;
    import org.springframework.dao.DataIntegrityViolationException;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.support.TransactionSynchronization;
    import org.springframework.transaction.support.TransactionSynchronizationManager;

    import java.time.LocalDate;
    import java.time.LocalTime;
    import java.util.*;
    import java.util.stream.Collectors;
    import java.util.stream.Stream;

    @Service
    public class SquadService extends ASquadService {


        private final RegionService regionService;

        private static final int MIN_ALLOWED_AGE = 50;

        public SquadService(SquadRepository squadRepository, UserRepository userRepository,
                            SquadMemberRepository squadMemberRepository, FirebaseMessagingService firebaseMessagingService,
                            BlockRepository blockRepository,
                            RegionService regionService) {
            super(squadRepository, userRepository, squadMemberRepository, firebaseMessagingService, blockRepository);
            this.regionService = regionService;
        }


        @Transactional
        public SquadCreateResponse createSquad(SquadRequest req, User user) {

            regionService.checkRegion(req.getRegionMain(), req.getRegionSub());

            if (req.getMinAge() < MIN_ALLOWED_AGE) {
                throw new SquadException(ErrorCode.AGE_RANGE_INVALID);
            }

            if (req.getMinAge() > req.getMaxAge()) {
                throw new SquadException(ErrorCode.AGE_RANGE_INVALID);
            }
            if (Boolean.TRUE.equals(req.getTimeSpecified()) && req.getTime() == null) {
                throw new SquadException(ErrorCode.TIME_REQUIRED_WHEN_SPECIFIED);
            }

            if (req.getDate() == null || req.getDate().isBefore(LocalDate.now())) {
                throw new SquadException(ErrorCode.SQUAD_TIME_PASSED);
            }

            if (user.getAge() < MIN_ALLOWED_AGE) {
                throw new SquadException(ErrorCode.AGE_NOT_ALLOWED);
            }

            // 정원은 최소 1(리더 포함). 1 미만이면 불가
            if (req.getMaxParticipants() < 1) {
                throw new SquadException(ErrorCode.SQUAD_MEMBER_COUNT_EXCEEDED);
            }

            Squad squad = squadRepository.save(Squad.create(user, req));

            // 4) 리더 멤버 등록 (리더는 JOINED 이지만 isOwner=true → joinParticipant에서 카운트 증가 X)
            SquadMember owner = SquadMember.createOwner(user, squad);
            squadMemberRepository.save(owner);

            return new SquadCreateResponse("created", squad.getId());
        }


        @DistributeLock(keyPrefix = "squad:", argIndex = 0, waitTime = 3, leaseTime = 10)
        @Transactional
        public Squad updateSquad(Long squadId, User user, UpdateSquadRequest req) {
            Squad squad = findSquad(squadId);
            validateOwner(user, squad);

            // 1️⃣ 변경 후 유효값 계산
            int newMinAge = Optional.ofNullable(req.getMinAge()).orElse(squad.getMinAge());
            int newMaxAge = Optional.ofNullable(req.getMaxAge()).orElse(squad.getMaxAge());
            int newMaxParticipants = Optional.ofNullable(req.getMaxParticipants()).orElse(squad.getMaxParticipants());
            Gender newGender = Optional.ofNullable(req.getGenderRequirement()).orElse(squad.getGenderRequirement());
            LocalDate newDate = Optional.ofNullable(req.getDate()).orElse(squad.getDate());
            boolean newTimeSpecified = Optional.ofNullable(req.getTimeSpecified()).orElse(squad.isTimeSpecified());
            LocalTime newTime = newTimeSpecified
                    ? Optional.ofNullable(req.getTime()).orElse(squad.getTime())
                    : null;
            String newRegionMain = Optional.ofNullable(req.getRegionMain()).orElse(squad.getRegionMain());
            String newRegionSub  = Optional.ofNullable(req.getRegionSub()).orElse(squad.getRegionSub());

            // 2️⃣ 기본 검증
            if (newMinAge < 0 || newMinAge > newMaxAge)
                throw new SquadException(ErrorCode.AGE_RANGE_INVALID);
            if (newTimeSpecified && newTime == null)
                throw new SquadException(ErrorCode.TIME_REQUIRED_WHEN_SPECIFIED);
            if (newDate.isBefore(LocalDate.now()))
                throw new SquadException(ErrorCode.SQUAD_TIME_PASSED);
            if (newMaxParticipants < 1)
                throw new SquadException(ErrorCode.SQUAD_MEMBER_COUNT_EXCEEDED);

            // 3️⃣ 지역 변경 검증
            if (!Objects.equals(squad.getRegionMain(), newRegionMain) ||
                !Objects.equals(squad.getRegionSub(), newRegionSub)) {
                regionService.checkRegion(newRegionMain, newRegionSub);
            }

            // 4️⃣ 현재 멤버와 충돌 검증 (락 구간이라 안정적)
            validateAgeConflictOnUpdate(squad.getId(), squad.getUser().getId(), newMinAge, newMaxAge);
            validateGenderConflictOnUpdate(squad.getId(), newGender);

            // 5️⃣ 정원 변경 (DB 원자적 업데이트)
            if (newMaxParticipants != squad.getMaxParticipants()) {
                int rows = squadRepository.tryUpdateMaxParticipants(squadId, newMaxParticipants);
                if (rows == 0)
                    throw new SquadException(ErrorCode.SQUAD_MEMBER_COUNT_EXCEEDED);

                squad.applyMaxParticipantsFromService(newMaxParticipants);
            }

            // 6️⃣ 나머지 필드 반영
            squad.updateExceptCapacity(req);

            // 7️⃣ closed 동기화
            refreshClosedByCapacity(squad);

            return squad;
        }

        @DistributeLock(keyPrefix = "squad:", argIndex = 0, waitTime = 3, leaseTime = 10)
        @Transactional
        public void deleteSquad(Long squadId, User user, boolean isForcedDelete) {
            Squad squad = findSquad(squadId);
            validateOwner(user, squad);
            validateDeleteMember(squad, isForcedDelete); // currentCount==1 등 정책 확인


            squadRepository.delete(squad);


        }

        public DataResponse mySquad(User user, Pageable pageable) {

            Pageable fixedPageable = forceSize20(pageable);

            Page<Squad> page = squadRepository.findAllMine(user.getId(), fixedPageable);


            List<SquadSimpleDto> list = page.getContent().stream()
                    .map(squad -> {
                        boolean isOwner = squad.getUser().getId().equals(user.getId());
                        String membership = isOwner ? "OWNER" : "JOINED";


                        Long pendingCount = null;
                        if (isOwner && squad.getJoinType() == JoinType.APPROVAL) {
                            pendingCount = squadMemberRepository.countPendingBySquadId(squad.getId());
                        }

                        return SquadSimpleDto.toDto(squad, membership, pendingCount);
                    })
                    .toList();

            return new DataResponse(list, page.hasNext());

        }

        @DistributeLock(keyPrefix = "squad:", argIndex = 1, waitTime = 3, leaseTime = 5)
        @Transactional
        public Map<String, String> joinSquad(User user, Long squadId) {
            // 1) 스쿼드 조회 & 기본 검증
            Squad squad = findSquad(squadId);
            validateIsClosed(squad);
            validateTimePassed(squad);
            validateGenderEligibility(user, squad);
            validateAgeEligibility(user, squad);

            // 2) 기존 멤버 상태 확인 (중복 처리 방지)
            Optional<SquadMember> existing = squadMemberRepository.findBySquadAndUser(squad, user);
            if (existing.isPresent()) {
                return switch (existing.get().getMembershipStatus()) {
                    case JOINED   -> Map.of("status", "joined");
                    case PENDING  -> Map.of("status", "pending");
                    case REJECTED -> throw new SquadException(ErrorCode.SQUAD_REJECTED);
                    case KICKED_OUT -> throw new SquadException(ErrorCode.SQUAD_KICKED_OUT);
                };
            }

            // 3) JOIN 처리
            if (squad.getJoinType() == JoinType.DIRECT) {
                // 3-1) 정원 원자적 선점
                int updated = squadRepository.tryIncreaseCount(squadId);
                if (updated == 0) {
                    throw new SquadException(ErrorCode.SQUAD_FULL);
                }

                // 3-2) 멤버 행 생성(JOINED)
                SquadMember member = SquadMember.createJoined(user, squad); // ← 없으면 create 후 status=JOINED 세터
                try {
                    squadMemberRepository.save(member);
                } catch (DataIntegrityViolationException dup) {
                    // 동시 요청으로 이미 들어온 경우: 선점했지만 중복 삽입이면 되돌림(선택)
                    // rollback에 맡겨도 되지만, 보수적으로 되돌리고 알림 생략
                    throw new SquadException(ErrorCode.ALREADY_JOINED);
                }

                // 3-3) 마감 동기화(정원 꽉 찼으면 닫기)
                squadRepository.closeIfFull(squadId);

                // 3-4) 알림 after-commit
                enqueueAfterCommit(() -> {
                    var tokens = squadMemberRepository.findJoinedFcmTokens(squadId, user.getId());
                    if (!tokens.isEmpty()) {
                        firebaseMessagingService.sendToUsers(
                                true,
                                tokens,
                                squad.getTitle(),
                                "새로운 멤버가 들어왔어요! 어서 인사해보세요 👉🏻",
                                squad.getId() + ",squad"
                        );
                    }
                });

                return Map.of("status", "approved");

            } else { // JoinType.APPROVAL
                // 3-1) 멤버 행 생성(PENDING) — 정원 증가 없음
                SquadMember member = SquadMember.createPending(user, squad); // ← 없으면 create 후 status=PENDING 세터
                try {
                    squadMemberRepository.save(member);
                } catch (DataIntegrityViolationException dup) {
                    throw new SquadException(ErrorCode.ALREADY_JOINED);
                }

                // 3-2) 오너 알림 after-commit
                enqueueAfterCommit(() -> {
                    User owner = squad.getUser();
                    if (Boolean.TRUE.equals(owner.getSquadChatAlarm()) && owner.getFcmToken() != null) {
                        firebaseMessagingService.sendToUser(
                                true,
                                owner.getFcmToken(),
                                squad.getTitle(),
                                "새로운 신청자가 생겼어요! 누구일까요? 👀",
                                squad.getId() + ",squad"
                        );
                    }
                });

                return Map.of("status", "requested");
            }
        }


        public DataResponse homeSquad(Pageable pageable, SquadFilterRequest req) {

            Pageable fixedPageable = forceSize20(pageable);

            Page<Squad> page = squadRepository.findFilteredSquads(req.getCategory(), req.getRegionMain(),
                    req.getRegionSub(), req.isRecruitingOnly(), fixedPageable);

            List<SquadSimpleDto> list = page.getContent().stream()
                    .map(s -> SquadSimpleDto.toDto(s, null, null)).toList();

            return new DataResponse(list, page.hasNext());

        }

        @Transactional
        public SquadDetailDto detail(User user, long squadId) {
            Squad squad = squadRepository.findById(squadId)
                    .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));

            boolean isOwner = Objects.equals(squad.getUser().getId(), user.getId());

            List<UserSimpleDto> participants = squadMemberRepository.findJoinedParticipants(squadId);

            boolean hasPending = false;
            if (isOwner && squad.getJoinType() == JoinType.APPROVAL) {
                hasPending = squadMemberRepository.existsPending(squadId);
            }

            String myMembership = isOwner
                    ? "OWNER"
                    : squadMemberRepository.findMyMembership(squadId, user.getId())
                    .map(ms -> switch (ms) {
                        case JOINED     -> "JOINED";
                        case PENDING    -> "PENDING";
                        case REJECTED   -> "REJECTED";
                        case KICKED_OUT -> "KICKED";
                    })
                    .orElse("NONE");

            return SquadDetailDto.toDto(user, squad, participants, hasPending, myMembership);
        }


        @Transactional
        public List<UserSimpleDto> getPendingApplicants(User owner, Long squadId) {
            Squad squad = squadRepository.findById(squadId)
                    .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));

            if (squad.getJoinType() == JoinType.DIRECT) {
                throw new SquadException(ErrorCode.NOT_APPROVAL_SQUAD);
            }
            if (!Objects.equals(owner.getId(), squad.getUser().getId())) {
                throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
            }
            return squadMemberRepository.findPendingApplicants(squadId);
        }


        @Transactional
        public void closeSquad(User user, Long squadId) {
            Squad squad = findSquad(squadId);
            validateOwner(user, squad);

            if (squad.isClosed()) {
                throw new SquadException(ErrorCode.SQUAD_ALREADY_CLOSED);
            }

            squad.close();
        }


        @DistributeLock(keyPrefix = "squad:", argIndex = 2, waitTime = 3, leaseTime = 10)
        @Transactional
        public void approveParticipant(User owner, Long targetUserId, Long squadId) {

            Squad squad = findSquad(squadId);
            validateOwner(owner, squad);

            // 대상 사용자/요청 존재 확인
            User target = findUser(targetUserId);
            SquadMember member = squadMemberRepository.findBySquadAndUser(squad, target)
                    .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));
            if (member.isOwner()) {
                throw new SquadException(ErrorCode.NOT_SQUAD_OWNER); // 오너 승인 방지(안전망)
            }
            if (member.getMembershipStatus() != MembershipStatus.PENDING) {
                throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
            }

            // 1) 정원 원자적 선점
            int inc = squadRepository.tryIncreaseCount(squadId);
            if (inc == 0) {
                throw new SquadException(ErrorCode.SQUAD_FULL);
            }

            // 2) 조건부 승인(PENDING -> JOINED)
            int upd = squadMemberRepository.approveIfPending(squadId, targetUserId);
            if (upd == 0) {
                // 다른 트랜잭션이 먼저 승인/거절/강퇴했을 수 있음 → 카운트 롤백
                squadRepository.tryDecreaseCount(squadId);
                throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
            }

            // 3) 마감 동기화
            squadRepository.closeIfFull(squadId);

            // 4) after-commit 알림
            final String title = squad.getTitle();
            final String data = squad.getId() + ",squad";
            enqueueAfterCommit(() -> {
                firebaseMessagingService.sendToUser(
                        target.getFeedAlarm(),
                        target.getFcmToken(),
                        title,
                        "모임의 멤버가 되었습니다! 어서 인사해보세요🎉",
                        data
                );
                var fcmTokens = squadMemberRepository.findJoinedFcmTokens(squadId, target.getId());
                if (!fcmTokens.isEmpty()) {
                    firebaseMessagingService.sendToUsers(
                            true, fcmTokens, title,
                            "새로운 멤버가 들어왔어요! 어서 인사해보세요 👉🏻",
                            data
                    );
                }
            });
        }

        @DistributeLock(keyPrefix = "squad:", argIndex = 2, waitTime = 3, leaseTime = 10)
        @Transactional
        public void rejectParticipant(User owner, Long targetUserId, Long squadId) {
            Squad squad = findSquad(squadId);
            validateOwner(owner, squad);

            // 존재 확인(친절한 에러 위해)
            User target = findUser(targetUserId);
            squadMemberRepository.findBySquadAndUser(squad, target)
                    .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));

            // 1) 조건부 업데이트로 원자적으로 거절
            int rows = squadMemberRepository.rejectIfPending(squadId, targetUserId);
            if (rows == 0) {
                // 이미 처리됨(승인/거절/강퇴 등) → PENDING 아님
                throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
            }

            // 2) 알림은 커밋 이후
            enqueueAfterCommit(() -> firebaseMessagingService.sendToUser(
                    target.getFeedAlarm(),
                    target.getFcmToken(),
                    squad.getTitle(),
                    "모임 신청 결과를 확인해보세요 👉",
                    squadId + ",squad"
            ));

        }

        @DistributeLock(keyPrefix = "squad:", argIndex = 2, waitTime = 3, leaseTime = 10)
        @Transactional
        public void kickOffParticipant(User owner, Long targetUserId, Long squadId) {
            Squad squad = findSquad(squadId);
            validateOwner(owner, squad);

            User target = findUser(targetUserId);
            SquadMember member = squadMemberRepository.findBySquadAndUser(squad, target)
                    .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));

            if (member.isOwner()) {
                throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
            }

            // 1) 조건부 상태 변경: JOINED -> KICKED_OUT (원자)
            int rows = squadMemberRepository.kickIfJoined(squadId, targetUserId);
            if (rows == 0) {
                // 이미 LEAVE 했거나, 다른 트랜잭션이 먼저 KICK/REJECT 했거나, JOINED가 아님
                throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
            }

            // 2) 인원 감소 원자 수행
            int dec = squadRepository.tryDecreaseCount(squadId);
            if (dec == 0) {
                // 방어적: 이론상 없지만, 동시성 극단 상황 대비
                // 여기서 롤백을 던져 일관성을 맞추거나, 에러 로그만 남겨도 됨.
                throw new SquadException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            // 3) closed 동기화
            squadRepository.openIfNotFull(squadId); // 인원 줄었으니 열릴 수 있음

            // 4) 알림은 커밋 후
            enqueueAfterCommit(() -> firebaseMessagingService.sendToUser(
                    target.getFeedAlarm(),
                    target.getFcmToken(),
                    squad.getTitle(),
                    "이 모임의 멤버로 활동이 어렵게 되었어요🥲",
                    squadId + ",squad"
            ));

        }

        @DistributeLock(keyPrefix = "squad:", argIndex = 1, waitTime = 3, leaseTime = 10)
        @Transactional
        public void leaveSquad(User user, Long squadId) {
            Squad squad = findSquad(squadId);

            // 존재/권한 체크(친절한 에러 메시지 위해 1회 조회)
            SquadMember member = squadMemberRepository.findBySquadAndUser(squad, user)
                    .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));
            if (member.isOwner()) {
                throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
            }

            // 1) JOINED 상태면 원자적으로 LEAVE 처리
            int changed = squadMemberRepository.leaveIfJoined(squadId, user.getId());
            if (changed > 0) {
                int dec = squadRepository.tryDecreaseCount(squadId);
                if (dec == 0) {
                    // 이론상 거의 불가하지만, 극단 레이스 대비
                    throw new SquadException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
                squadRepository.openIfNotFull(squadId);
                return;
            }

            // 2) JOINED가 아니었다면 PENDING 철회 시도
            int deletedPending = squadMemberRepository.deleteIfPending(squadId, user.getId());
            if (deletedPending > 0) {
                return;
            }

            // 3) 여기까지 왔다면 이미 다른 트랜잭션에서 상태가 바뀐 것(강퇴/거절 등)
            throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
        }





        private Pageable forceSize20(Pageable pageable) {
            Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Order.asc("date"),
                    Sort.Order.asc("time"), Sort.Order.desc("createdAt"));

            return PageRequest.of(pageable.getPageNumber(), 20, sort);
        }


        private void enqueueAfterCommit(Runnable task) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { task.run(); }
            });
        }
    }









