package coLaon.ClaonBack.center.service;

import coLaon.ClaonBack.center.domain.Center;
import coLaon.ClaonBack.center.domain.CenterImg;
import coLaon.ClaonBack.center.domain.Charge;
import coLaon.ClaonBack.center.domain.HoldInfo;
import coLaon.ClaonBack.center.domain.OperatingTime;
import coLaon.ClaonBack.center.domain.SectorInfo;
import coLaon.ClaonBack.center.dto.CenterCreateRequestDto;
import coLaon.ClaonBack.center.dto.CenterDetailResponseDto;
import coLaon.ClaonBack.center.dto.CenterResponseDto;
import coLaon.ClaonBack.center.dto.CenterSearchResponseDto;
import coLaon.ClaonBack.center.dto.HoldInfoResponseDto;
import coLaon.ClaonBack.center.dto.CenterPreviewResponseDto;
import coLaon.ClaonBack.center.dto.CenterListOption;
import coLaon.ClaonBack.center.repository.CenterBookmarkRepository;
import coLaon.ClaonBack.center.repository.CenterRepository;
import coLaon.ClaonBack.center.repository.HoldInfoRepository;
import coLaon.ClaonBack.center.repository.ReviewRepositorySupport;
import coLaon.ClaonBack.common.domain.Pagination;
import coLaon.ClaonBack.common.domain.PaginationFactory;
import coLaon.ClaonBack.common.exception.ErrorCode;
import coLaon.ClaonBack.common.exception.NotFoundException;
import coLaon.ClaonBack.common.exception.UnauthorizedException;
import coLaon.ClaonBack.common.validator.IsAdminValidator;
import coLaon.ClaonBack.post.repository.PostRepositorySupport;
import coLaon.ClaonBack.user.domain.User;
import coLaon.ClaonBack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CenterService {
    private final UserRepository userRepository;
    private final CenterRepository centerRepository;
    private final HoldInfoRepository holdInfoRepository;
    private final ReviewRepositorySupport reviewRepositorySupport;
    private final PostRepositorySupport postRepositorySupport;
    private final CenterBookmarkRepository centerBookmarkRepository;
    private final PaginationFactory paginationFactory;

    @Transactional
    public CenterResponseDto create(
            String userId,
            CenterCreateRequestDto requestDto
    ) {
        User admin = userRepository.findById(userId).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.USER_DOES_NOT_EXIST,
                        "이용자를 찾을 수 없습니다."
                )
        );

        IsAdminValidator.of(admin.getEmail()).validate();

        Center center = this.centerRepository.save(
                Center.of(
                        requestDto.getName(),
                        requestDto.getAddress(),
                        requestDto.getTel(),
                        requestDto.getWebUrl(),
                        requestDto.getInstagramUrl(),
                        requestDto.getYoutubeUrl(),
                        requestDto.getImgList()
                                .stream().map(dto -> CenterImg.of(dto.getUrl()))
                                .collect(Collectors.toList()),
                        requestDto.getOperatingTimeList()
                                .stream().map(dto -> OperatingTime.of(dto.getDay(), dto.getStart(), dto.getEnd()))
                                .collect(Collectors.toList()),
                        requestDto.getFacilities(),
                        requestDto.getChargeList()
                                .stream().map(dto -> Charge.of(dto.getName(), dto.getFee()))
                                .collect(Collectors.toList()),
                        requestDto.getChargeImg(),
                        requestDto.getHoldInfoImg(),
                        requestDto.getSectorInfoList()
                                .stream().map(dto -> SectorInfo.of(dto.getName(), dto.getStart(), dto.getEnd()))
                                .collect(Collectors.toList())
                )
        );

        return CenterResponseDto.from(
                center,
                requestDto.getHoldInfoList()
                        .stream()
                        .map(holdInfo -> this.holdInfoRepository.save(
                                HoldInfo.of(
                                        holdInfo.getName(),
                                        holdInfo.getImg(),
                                        center
                                )))
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public CenterDetailResponseDto findCenter(String userId, String centerId) {
        userRepository.findById(userId).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.USER_DOES_NOT_EXIST,
                        "이용자를 찾을 수 없습니다."
                )
        );

        Center center = centerRepository.findById(centerId).orElseThrow(
                () -> new NotFoundException(
                        ErrorCode.DATA_DOES_NOT_EXIST,
                        "암장 정보를 찾을 수 없습니다."
                )
        );

        Boolean isBookmarked = centerBookmarkRepository.findByUserIdAndCenterId(userId, centerId).isPresent();
        Integer postCount = postRepositorySupport.countByCenterExceptBlockUser(centerId, userId);
        Integer reviewCount = reviewRepositorySupport.countByCenterExceptBlockUser(centerId, userId);

        return CenterDetailResponseDto.from(
                center,
                holdInfoRepository.findAllByCenter(center),
                isBookmarked,
                postCount,
                reviewCount
        );
    }

    @Transactional(readOnly = true)
    public List<HoldInfoResponseDto> findHoldInfoByCenterId(String userId, String centerId) {
        userRepository.findById(userId).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.USER_DOES_NOT_EXIST,
                        "이용자를 찾을 수 없습니다."
                )
        );

        Center center = centerRepository.findById(centerId).orElseThrow(
                () -> new NotFoundException(
                        ErrorCode.DATA_DOES_NOT_EXIST,
                        "암장 정보를 찾을 수 없습니다."
                )
        );

        return holdInfoRepository.findAllByCenter(center)
                .stream()
                .map(HoldInfoResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CenterSearchResponseDto> searchCenter(String userId, String keyword) {
        userRepository.findById(userId).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.USER_DOES_NOT_EXIST,
                        "이용자를 찾을 수 없습니다."
                )
        );

        return centerRepository.searchCenter(keyword)
                .stream()
                .map(CenterSearchResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Pagination<CenterPreviewResponseDto> findCenterListByOption(String userId, CenterListOption option, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.USER_DOES_NOT_EXIST,
                        "이용자를 찾을 수 없습니다."
                )
        );

        if (option == CenterListOption.BOOKMARK) {
            return findBookMarkedCenters(userId, pageable);
        } else if (option == CenterListOption.MY_AROUND) {
            return findMyAroundCenters(userId, pageable);
        } else if (option == CenterListOption.NEW_SETTING) {
            return findNewSettingCenters(pageable);
        } else if (option == CenterListOption.NEWLY_REGISTERED) {
            return findNewlyRegisteredCenters(pageable);
        }
        return null;
    }

    private Pagination<CenterPreviewResponseDto> findMyAroundCenters(String userId, Pageable pageable) {
        // TODO implement this.
        return null;
    }

    private Pagination<CenterPreviewResponseDto> findNewSettingCenters(Pageable pageable) {
        // TODO implement this.
        return null;
    }

    private Pagination<CenterPreviewResponseDto> findBookMarkedCenters(String userId, Pageable pageable) {
        return paginationFactory.create(
                centerRepository.findBookmarkedCenter(userId, pageable).map(CenterPreviewResponseDto::from)
        );
    }

    private Pagination<CenterPreviewResponseDto> findNewlyRegisteredCenters(Pageable pageable) {
        LocalDateTime standardDate = LocalDate.now().atStartOfDay().minusDays(7);
        return paginationFactory.create(
                centerRepository.findNewlyCreatedCenter(standardDate, pageable).map(CenterPreviewResponseDto::from)
        );
    }
}
