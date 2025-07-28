    package FeedStudy.StudyFeed.squad.service;

    import FeedStudy.StudyFeed.block.repository.BlockRepository;
    import FeedStudy.StudyFeed.global.dto.DataResponse;
    import FeedStudy.StudyFeed.global.exception.ErrorCode;
    import FeedStudy.StudyFeed.global.exception.exceptiontype.SquadException;
    import FeedStudy.StudyFeed.global.jwt.JwtUtil;
    import FeedStudy.StudyFeed.global.service.FirebaseMessagingService;
    import FeedStudy.StudyFeed.global.service.RegionService;
    import FeedStudy.StudyFeed.global.type.AttendanceStatus;
    import FeedStudy.StudyFeed.global.type.JoinType;
    import FeedStudy.StudyFeed.squad.dto.SquadDetailDto;
    import FeedStudy.StudyFeed.squad.dto.SquadFilterRequest;
    import FeedStudy.StudyFeed.squad.dto.SquadRequest;
    import FeedStudy.StudyFeed.squad.dto.SquadSimpleDto;
    import FeedStudy.StudyFeed.squad.entity.Squad;
    import FeedStudy.StudyFeed.squad.entity.SquadMember;
    import FeedStudy.StudyFeed.squad.repository.SquadMemberRepository;
    import FeedStudy.StudyFeed.squad.repository.SquadRepository;
    import FeedStudy.StudyFeed.squad.util.ChatTokenProvider;
    import FeedStudy.StudyFeed.user.dto.UserSimpleDto;
    import FeedStudy.StudyFeed.user.entity.User;
    import FeedStudy.StudyFeed.user.repository.UserRepository;
    import jakarta.transaction.Transactional;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.util.*;
    import java.util.stream.Collectors;

    @Service
    public class SquadService extends ASquadService {


        private final JwtUtil jwtUtil;
        private final ChatTokenProvider chatTokenProvider;
        private final RegionService regionService;

        public SquadService(SquadRepository squadRepository, UserRepository userRepository,
                            SquadMemberRepository squadMemberRepository, FirebaseMessagingService firebaseMessagingService,
                            BlockRepository blockRepository, JwtUtil jwtUtil, ChatTokenProvider chatTokenProvider,
                            RegionService regionService) {
            super(squadRepository, userRepository, squadMemberRepository, firebaseMessagingService, blockRepository);
            this.jwtUtil = jwtUtil;
            this.chatTokenProvider = chatTokenProvider;
            this.regionService = regionService;
        }


        @Transactional
        public Map<String, String> createSquadWithToken(SquadRequest req, User user) {

            regionService.checkRegion(req.getRegionMain(), req.getRegionSub());

            if(req.getMinAge() > req.getMaxAge()) {
                throw new SquadException(ErrorCode.AGE_RANGE_INVALID);
            }


            Squad squad = Squad.create(user ,req);
            squad = squadRepository.save(squad);

            SquadMember squadMember = SquadMember.create(user, squad);
            squadMemberRepository.save(squadMember);

            String chatToken = chatTokenProvider.createSquadChatToken(user, squad);


            return Map.of(
                    "status", "created",
                    "squadId", squad.getId().toString(),
                    "chatToken", chatToken
            );
        }

        @Transactional
        public Squad updateSquad(Long squadId, User user, SquadRequest req) {
            Squad squad = findSquad(squadId);

            boolean regionChanged = !Objects.equals(squad.getRegionMain(), req.getRegionMain())
                        || !Objects.equals(squad.getRegionSub(), req.getRegionSub());

            if(regionChanged) {
                regionService.checkRegion(req.getRegionMain(), req.getRegionSub());
            }

            validateOwner(user, squad);
            validateAgeRange(squad, req);
            validateGender(squad, req);
            validateMemberCount(squad, req);

            squad.update(req);

            squadRepository.save(squad);
            return squad;
        }

        @Transactional
        public void deleteSquad(Long squadId, User user, boolean isForcedDelete) {

            Squad squad = findSquad(squadId);
            validateOwner(user, squad);
            validateDeleteMember(squad, isForcedDelete);
            List<SquadMember> members = squad.getMembers();
            squad.getMembers().clear();
            squadMemberRepository.deleteAll(members);
            squadRepository.delete(squad);
        }

        public DataResponse mySquad(User user, Pageable pageable) {
            Page<Squad> squads = squadRepository.findByUser(user, pageable);
            List<SquadSimpleDto> squadDtos = squads.getContent().stream().map(SquadSimpleDto::toDto).toList();
            return new DataResponse(squadDtos, squads.hasNext());

        }

        public Map<String, String> joinOrGetChatToken(User user, Long squadId) {

            Squad squad = squadRepository.findByIdWithParticipants(squadId)
                    .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));

            Optional<SquadMember> members = squadMemberRepository.findBySquadAndUser(squad, user);

            if(members.isPresent()) {
                SquadMember squadMember = members.get();

                return switch (squadMember.getAttendanceStatus()) {
                    case JOINED -> Map.of("status", "joined", "chatToken", chatTokenProvider.createSquadChatToken(user, squad));
                    case PENDING -> Map.of("status", "pending");
                    case REJECTED -> throw new SquadException(ErrorCode.SQUAD_REJECTED);
                    case KICKED_OUT -> throw new SquadException(ErrorCode.SQUAD_KICKED_OUT);
                };
            }

            joinSquad(user, squad);

            if(squad.getJoinType() == JoinType.DIRECT) {
                List<String> fcmTokens = squad.getMembers().stream()
                        .filter(member -> member.getAttendanceStatus() == AttendanceStatus.JOINED)
                        .filter(member -> !member.getUser().getId().equals(user.getId()))
                        .map(member -> member.getUser())
                        .filter(u -> Boolean.TRUE.equals(u.getSquadChatAlarm()))
                        .map(u -> u.getFcmToken())
                        .filter(obj -> Objects.nonNull(obj))
                        .toList();

                if(!fcmTokens.isEmpty()) {
                    String pushTitle = squad.getTitle();
                    String pushContent = "새로운 멤버가 들어왔어요! 어서 인사해보세요 👉🏻";
                    String data = squad.getId() + ",squad";

                    firebaseMessagingService.sendCommentNotificationToMany(true, fcmTokens, pushTitle, pushContent, data);
                }



            } else if(squad.getJoinType() == JoinType.APPROVAL) {
                User owner = squad.getUser();
                if(Boolean.TRUE.equals(owner.getSquadChatAlarm()) && owner.getFcmToken() != null) {
                    String pushTitle = squad.getTitle();
                    String pushContent = "새로운 신청자가 생겼어요! 누구일까요? 👀";
                    String data = squad.getId() + ",squad";
                    firebaseMessagingService.sendCommentNotification(true, owner.getFcmToken(),
                            pushTitle, pushContent, data);
                }
            }


            return Map.of("status", squad.getJoinType().equals(JoinType.APPROVAL) ? "requested" : "approved");

        }

        public DataResponse homeSquad(User user, Pageable pageable, SquadFilterRequest req) {
            List<User> excludedUser = new ArrayList<>();
            if (user != null) {
                excludedUser = getExcludedUsers(user);
            }
            Page<Squad> squads;

            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

            if (excludedUser.isEmpty()) {
                squads = squadRepository.findFilteredSquads(req.getCategory(), req.getRegionMain(),
                        req.getRegionSub(), req.isRecruitingOnly(), sevenDaysAgo, pageable);

            } else {
                squads = squadRepository.findFilteredSquadsWithExclusion(req.getCategory(), req.getRegionMain(),
                        req.getRegionSub(), req.isRecruitingOnly(), excludedUser, sevenDaysAgo, pageable);
            }
            return new DataResponse(squads.getContent().stream().map(SquadSimpleDto::toDto).toList(), squads.hasNext());
        }

        public SquadDetailDto detail(User user, long squadId) {
            Squad squad = findSquad(squadId);
            return SquadDetailDto.toDto(user, squad);
        }

        public List<UserSimpleDto> getParticipants(User user, Long squadId) {
            Squad squad = squadRepository.findById(squadId)
                    .orElseThrow(() -> new SquadException(ErrorCode.SQUAD_NOT_FOUND));

            if (squad.getJoinType().equals(JoinType.DIRECT)) {
                throw new SquadException(ErrorCode.NOT_APPROVAL_SQUAD);
            }
            if (user.getId() != squad.getUser().getId()) {
                throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
            }
            return squad.getMembers().stream()
                    .filter(member -> member.getAttendanceStatus() == AttendanceStatus.PENDING)
                    .map(members -> UserSimpleDto.toDto(members.getUser()))
                    .collect(Collectors.toList());
        }


        public void closeSquad(User user, Long squadId) {
            Squad squad = findSquad(squadId);

            validateOwner(user, squad);
            squad.setClosed(true);
            squadRepository.save(squad);
        }


        public void joinSquad(User user, Squad squad) {
            validateAlreadyJoined(user, squad);
            validateIsClosed(squad);
            validateTimePassed(squad);
            validateFullJoined(squad);
            validateGender(user ,squad);
            validateAgeRange(user , squad);

            SquadMember squadMember = SquadMember.create(user, squad);
            squadMember = squadMemberRepository.save(squadMember);
            squad.joinParticipant(squadMember);
            squadRepository.save(squad);

        }



        public void approveParticipant(User user, Long userId, Long squadId) {
            Squad squad = findSquad(squadId);
            User members = findUser(userId);


            if (!Objects.equals(squad.getUser().getId(), user.getId())) {
                throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
            }

            long joinedCount = squad.getMembers().stream()
                    .filter(member -> member.getAttendanceStatus() == AttendanceStatus.JOINED).count();

            if (joinedCount >= squad.getMaxParticipants()) {
                throw new SquadException(ErrorCode.SQUAD_FULL);
            }

            SquadMember squadMember = squad.getMembers().stream()
                    .filter(m -> m.getUser().equals(members))
                    .findAny().orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));

            if(squadMember.getAttendanceStatus() == AttendanceStatus.PENDING) {

                squadMember.setAttendanceStatus(AttendanceStatus.JOINED);
                squadMemberRepository.save(squadMember);
            }


            String title = squad.getTitle();
            String data = squad.getId() + ",squad";

            String fcmToken = members.getFcmToken();
            boolean alarm = members.getFeedAlarm();
            String contentToMember = "모임의 멤버가 되었습니다! 어서 인사해보세요🎉";

            firebaseMessagingService.sendCommentNotification(alarm, fcmToken, title, contentToMember, data);

            List<String> fcmTokens = squad.getMembers().stream()
                    .filter(m -> m.getAttendanceStatus() == AttendanceStatus.JOINED)
                    .filter(m -> !m.getUser().getId().equals(members.getId()))
                    .map(m -> m.getUser())
                    .filter(u -> Boolean.TRUE.equals(u.getSquadChatAlarm()))
                    .map(u -> u.getFcmToken())
                    .filter(Objects::nonNull)
                    .toList();


            if(!fcmTokens.isEmpty()) {
                String contentToOthers = "새로운 멤버가 들어왔어요! 어서 인사해보세요 👉🏻";
                firebaseMessagingService.sendCommentNotificationToMany(true, fcmTokens, title, contentToOthers, data);
            }

        }

        public void rejectParticipant(User user, Long userId, Long squadId) {
            Squad squad = findSquad(squadId);
            User members = findUser(userId);
            if (!Objects.equals(squad.getUser().getId(), user.getId())) {
                throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
            }
            SquadMember squadMember = squad.getMembers().stream()
                    .filter(m -> m.getUser().equals(members))
                    .findAny().orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));
            squadMember.setAttendanceStatus(AttendanceStatus.REJECTED);
            squadMemberRepository.save(squadMember);

            firebaseMessagingService.sendCommentNotification(members.getFeedAlarm(),
                    members.getFcmToken(), squad.getTitle(), "모임 신청 결과를 확인해보세요 👉", squadId + ",squad");

        }


        public void kickOffParticipant(User user, Long userId, Long squadId) {
            Squad squad = findSquad(squadId);

            User members = findUser(userId);

            if (squad.getUser().getId() != user.getId()) {
                throw new SquadException(ErrorCode.NOT_SQUAD_OWNER);
            }

            SquadMember participant = squad.getMembers().stream().filter(m -> m.getUser() == members)
                    .findAny().orElseThrow(() -> new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND));
            participant.setAttendanceStatus(AttendanceStatus.KICKED_OUT);
            squad.decreaseCurrentCount();
            squadRepository.save(squad);
            squadMemberRepository.save(participant);

            firebaseMessagingService.sendCommentNotification(members.getFeedAlarm(),
                    members.getFcmToken(), squad.getTitle(), "이 모임의 멤버로 활동이 어렵게 되었어요🥲", squadId + ",squad");

        }

        public void leaveSquad(User user, Long squadId) {
            Squad squad = findSquad(squadId);

            SquadMember squadMember = squad.getMembers().stream()
                    .filter(m -> m.getUser().getId() == user.getId())
                    .findAny().orElseThrow();
            squad.getMembers().remove(squadMember);
            squadMemberRepository.delete(squadMember);
        }


        public Map<String, String> refreshSquadChatToken(Long squadId, User user) {
            Squad squad = findSquad(squadId);

            boolean isMember = squad.getMembers().stream()
                    .anyMatch(member -> member.getUser().getId().equals(user.getId()));

            if(!isMember) {
                throw new SquadException(ErrorCode.SQUAD_MEMBER_NOT_FOUND);
            }

            String squadChatToken = chatTokenProvider.createSquadChatToken(user, squad);
            return Map.of("chatToken", squadChatToken);
        }






        @Transactional
        public void disbannedSquad(Squad squad) {
            List<SquadMember> members = squadMemberRepository.findAllBySquad(squad);

            squadMemberRepository.deleteAll(members);


            squadRepository.delete(squad);
        }




    //
    //
    //    public boolean isAgeInRange(int userAge, Age age) {
    //        return switch (age) {
    //            case TEEN -> userAge >= 10 && userAge < 20;e
    //            case TWENTIES -> userAge >= 20 && userAge < 30;
    //            case THIRTIES -> userAge >= 30 && userAge < 40;
    //            case FORTIES -> userAge >= 40 && userAge < 50;
    //            case FIFTIES -> userAge >= 50;
    //            case ALL -> true; // 연령 무관
    //        };
    //    }





        private List<User> getExcludedUsers(User currentUser) {
            List<User> blockedUsers = new ArrayList<>(blockRepository.findByBlocker(currentUser).stream()
                    .map(block -> block.getBlocked())
                    .toList());

            List<User> blockedByUsers = blockRepository.findByBlocked(currentUser).stream()
                    .map(block -> block.getBlocker())
                    .toList();

            blockedUsers.addAll(blockedByUsers);
            return blockedUsers.stream().distinct().toList();

        }

    }









