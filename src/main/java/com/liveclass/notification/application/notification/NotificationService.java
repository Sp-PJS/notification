package com.liveclass.notification.application.notification;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.liveclass.notification.application.notification.dto.NotificationCreate;
import com.liveclass.notification.application.notification.dto.NotificationCreateRequest;
import com.liveclass.notification.application.notification.dto.NotificationPendingEvent;
import com.liveclass.notification.application.notification.dto.NotificationResponse;
import com.liveclass.notification.application.notification.dto.NotificationStatusResponse;
import com.liveclass.notification.application.notification.port.NotificationPublisherPort;
import com.liveclass.notification.application.notification.port.NotificationRepositoryPort;
import com.liveclass.notification.application.notification.port.NotificationSenderPort;
import com.liveclass.notification.domain.notification.Notification;
import com.liveclass.notification.domain.notification.NotificationChannel;
import com.liveclass.notification.domain.notification.NotificationStatus;
import com.liveclass.notification.global.exception.BusinessException;
import com.liveclass.notification.global.exception.ErrorCode;
import com.liveclass.notification.global.exception.NotificationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepositoryPort notificationRepositoryPort;
	private final NotificationSenderPort notificationSenderPort;
	private final NotificationPublisherPort publisherPort;

	// 알림 발송 요청 등록
	@Transactional
	public UUID registerNotificationRequest(UUID authenticatedUserId, NotificationCreate request) {
		// 테스트 편의성을 위해 동일한 유저인지 아닌지 검증
		if (!request.userId().equals(authenticatedUserId)) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		// TODO: eventKey 값은(REG_COMP_유저UUID_강의UUID 형태), 스케쥴러에 의해 PENDING 상태로 저장하고 알림 발송 요청 등록 API 이벤트 발행
		// 엔티티 unique 제약조건(uk_notification_event_key) 충족을 위한 키 조립
		String eventKey = request.notificationType() + request.userId() + "_" + request.courseId();
		String content = "[" + request.notificationType() + "] 알림 요청이 접수되었습니다. (참조: " + request.courseId() + ")";

		Notification notification = Notification.builder()
			.userId(request.userId())
			.channel(request.channel())
			.eventKey(eventKey)
			.content(content)
			.build();

		Notification saved = notificationRepositoryPort.save(notification);

		// 알림 발송 요청 등록 이벤트 발행
		publisherPort.publish(new NotificationPendingEvent(saved.getId()));

		return saved.getId();
	}

	// 특정 알림 요청의 현재 상태 단건 조회
	public NotificationStatusResponse getNotificationStatus(UUID authenticatedUserId, UUID notificationId) {

		Notification notification = notificationRepositoryPort.findById(notificationId)
			.orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

		// 자격 증명 검증
		if (!notification.getUserId().equals(authenticatedUserId)) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		return new NotificationStatusResponse(
			notification.getStatus()
		);
	}

	// 사용자 알림 목록 조회(읽음/안읽음 필터 포함)
	public List<NotificationResponse> getNotificationsByUserId(UUID authenticatedUserId, UUID userId, Boolean isRead) {
		// 자격 증명 검증
		if (!userId.equals(authenticatedUserId)) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		return notificationRepositoryPort.findAllByUserId(userId).stream()
			.map(this::convertToResponse)
			// 쿼리 스트링 파라미터가 비어있으면 전체 통과, 값이 있으면 일치 여부 필터링
			.filter(response -> isRead == null || response.isRead() == isRead)
			.collect(Collectors.toList());
	}

	private NotificationResponse convertToResponse(Notification n) {
		// 발송이 성공(SUCCESS)한 상태를 '읽음(true)'으로 간주
		boolean isRead = (n.getStatus() == NotificationStatus.SUCCESS);

		return new NotificationResponse(
			n.getId(),
			n.getChannel(),
			n.getStatus(),
			n.getContent(),
			isRead,
			n.getCreatedAt()
		);
	}

	// 수강 신청 / 취소 알림 발송 처리
	@Transactional
	public void processNotification(NotificationCreateRequest request) {
		// 유니크 제약조건을 통한 최초 저장 (동시성 멱등성 보장)
		Notification notification = Notification.builder()
			.userId(request.userId())
			.channel(request.channel())
			.eventKey(request.eventKey())
			.content(request.content())
			.build();

		notificationRepositoryPort.save(notification);

		// 처리중 상태
		notification.processing();

		try {
			// 알림 채널별 발송 포트 위임
			if (request.channel() == NotificationChannel.IN_APP) {
				notificationSenderPort.sendInApp(request);
			} else if (request.channel() == NotificationChannel.EMAIL) {
				notificationSenderPort.sendEmail(request);
			}

			// 발송 성공으로 상태 변경
			notification.success();

		} catch (Exception e) {
			log.error("알림 발송 실패 처리 진행 - eventKey: {}", request.eventKey(), e);
			// 발송실패 상태 변경, 에러 메시지 저장, 재시도 횟수 +1
			notification.fail(e.getMessage());
			throw new NotificationException(ErrorCode.NOTIFICATION_QUEUE_FULL);
		}
	}

	// 알림 발송 요청 등록 처리
	@Transactional
	public void PendingNotification(UUID notificationId) {

		// notificationId 기준 DB에 저장된 알림 조회
		Notification notification = notificationRepositoryPort.findById(notificationId)
			.orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

		// 처리중 상태
		notification.processing();

		try {
			// 엔티티 데이터를 DTO로 변경
			NotificationCreateRequest request = new NotificationCreateRequest(
				notification.getUserId(),
				notification.getChannel(),
				notification.getEventKey(),
				notification.getContent()
			);

			// 알림 채널별 포트 위임 발송
			if (notification.getChannel() == NotificationChannel.IN_APP) {
				notificationSenderPort.sendInApp(request);
			} else if (notification.getChannel() == NotificationChannel.EMAIL) {
				notificationSenderPort.sendEmail(request);
			}

			// 발송 성공 상태
			notification.success();
			log.info("[알림 발송 요청 등록 API - 발송 완료] 알림 ID: {}, 비즈니스 키: {}", notificationId, notification.getEventKey());

		} catch (Exception e) {
			log.error("[알림 발송 요청 등록 API - 발송 실패] 알림 ID: {}, 에러: {}", notificationId, e.getMessage(), e);

			// 발송 실패 상태 및 에러 메시지 저장, 재시도 횟수 +1
			notification.fail(e.getMessage());

			// 상위 예외 클래스 호출을 위한 커스텀 예외 클래스 호출
			throw new NotificationException(ErrorCode.NOTIFICATION_QUEUE_FULL);
		}
	}

	// 재시도(스케줄러) 중 에러 발생 시, 재시도 횟수 +1, 에러 메시지 저장을 위한 메서드
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleRecoveryFailure(java.util.UUID notificationId, String errorMessage, int maxRetryLimit) {
		Notification notification = notificationRepositoryPort.findById(notificationId)
			.orElseThrow(() -> new com.liveclass.notification.global.exception.BusinessException(
				com.liveclass.notification.global.exception.ErrorCode.NOTIFICATION_NOT_FOUND));

		// 실패 이력 적재 및 카운트 +1 증가
		notification.fail(errorMessage);

		if (notification.getRetryCount() >= maxRetryLimit) {

			notification.permanentFail(notification.getLastErrorMessage());

			log.error("[최종 실패] 알림 ID: {}", notification.getId());
		}
	}
}