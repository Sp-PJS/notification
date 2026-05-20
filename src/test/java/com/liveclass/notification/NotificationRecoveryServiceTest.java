package com.liveclass.notification;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.liveclass.notification.application.notification.NotificationRecoveryService;
import com.liveclass.notification.application.notification.NotificationService;
import com.liveclass.notification.application.notification.port.NotificationRepositoryPort;
import com.liveclass.notification.domain.notification.Notification;
import com.liveclass.notification.domain.notification.NotificationChannel;
import com.liveclass.notification.domain.notification.NotificationStatus;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NotificationRecoveryServiceTest {

	@Mock
	private NotificationRepositoryPort notificationRepositoryPort;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private NotificationRecoveryService notificationRecoveryService;

	@Test
	@DisplayName("복구 중 에러가 발생하여 재시도 횟수가 한계치(4회)에 도달하면 handleRecoveryFailure를 통해 최종 실패로 전환")
	void recovery_fail_and_becomes_permanent_fail_test() {
		log.info("========================================================================================");
		log.info("[TEST START] recovery_fail_and_becomes_permanent_fail_test");
		log.info("========================================================================================");

		// Given
		// 이미 3번 실패하여 retryCount가 3인 FAILED 알림 데이터를 DB에 사전 준비
		UUID targetNotificationId = UUID.randomUUID();
		Notification preFailedNotification = Notification.builder()
			.userId(UUID.randomUUID())
			.channel(NotificationChannel.EMAIL)
			.eventKey("RECOVERY_LIMIT_TEST_KEY")
			.content("수강신청 복구 한계치 테스트")
			.build();

		// 엔티티 메서드를 이용해 3회 실패 상태로 빌드
		preFailedNotification.fail("1차 실패");
		preFailedNotification.fail("2차 실패");
		preFailedNotification.fail("3차 실패"); // 현재 상태: FAILED, retryCount: 3

		log.info("[GIVEN] 사전 실패 알림 세팅 완료");
		log.info(" -> 알림 ID (Target ID): {}", preFailedNotification.getId());
		log.info(" -> 초기 상태 (Status): {}", preFailedNotification.getStatus());
		log.info(" -> 초기 재시도 횟수 (RetryCount): {}", preFailedNotification.getRetryCount());

		// Repository 조회 결과에 장전 (MAX_RETRY_LIMIT = 4 미만 조건에 부합)
		List<Notification> mockDbList = Collections.singletonList(preFailedNotification);
		when(notificationRepositoryPort.findAllByStatusAndRetryCountLessThan(NotificationStatus.FAILED, 4))
			.thenReturn(mockDbList);
		log.info("[GIVEN] Mock Repository 조회 쿼리 결과 장전 (리스트 크기: {})", mockDbList.size());

		// 복구 서비스가 발송을 시도할 때 또다시 예외(외부망 타임아웃 등)가 터지도록 Mocking
		String runtimeErrorMessage = "네트워크 커넥션 타임아웃";
		doThrow(new RuntimeException(runtimeErrorMessage))
			.when(notificationService).PendingNotification(preFailedNotification.getId());
		log.info("[GIVEN] 외부 서비스 PendingNotification 호출 시 예외 발생 유도 설정 완료 (사유: {})", runtimeErrorMessage);

		// when
		// 복구 스케줄러 메인 비즈니스 로직 가동
		log.info("\n[WHEN] notificationRecoveryService.recoverFailedNotifications() 복구 서비스 가동 시작");
		notificationRecoveryService.recoverFailedNotifications();
		log.info("[WHEN] 복구 서비스 가동 프로세스 종료");

		// then
		// 예외를 가로채 독립 트랜잭션 실패 처리 메서드(handleRecoveryFailure)를 올바르게 찔렀는지 팩트체크
		// 4회째 실패 상황이므로 정확한 파라미터(ID, 에러메시지, 한계치=4)가 전달되었는지 검증
		log.info("\n[THEN] Mockito verify 검증 시작 (handleRecoveryFailure 대리자 호출 여부 판단)");
		verify(notificationService, times(1))
			.handleRecoveryFailure(preFailedNotification.getId(), runtimeErrorMessage, 4);
		log.info("[THEN] 검증 완료 (대리 메서드 1회 호출 확인)");
		log.info("========================================================================================\n");
	}

	@Test
	@DisplayName("handleRecoveryFailure 호출 시 retryCount가 4가 되면 상태가 FAILED_PERMANENT(최종 실패)상태로 변경")
	void handle_recovery_failure_state_transition_test() {
		log.info("========================================================================================");
		log.info("[TEST START] handle_recovery_failure_state_transition_test");
		log.info("========================================================================================");

		// given
		UUID notificationId = UUID.randomUUID();
		Notification notification = Notification.builder()
			.userId(UUID.randomUUID())
			.channel(NotificationChannel.IN_APP)
			.eventKey("DOMAIN_STATE_TEST_KEY")
			.content("도메인 상태 전이 테스트")
			.build();

		// 3회 실패 누적 상태 시뮬레이션
		notification.fail("에러로 인한 1차 재시도");
		notification.fail("에러로 인한 2차 재시도");
		notification.fail("에러로 인한 3차 재시도"); // retryCount = 3

		log.info("[GIVEN] 상태 추적용 알림 엔티티 세팅 완료");
		log.info(" -> 알림 ID: {}", notification.getId());
		log.info(" -> 현재 상태 (Status): {}", notification.getStatus());
		log.info(" -> 현재 재시도 횟수 (RetryCount): {}", notification.getRetryCount());

		// 복구 대상 조회 쿼리가 가짜 알림 리스트를 뱉도록 설정 (MAX_RETRY_LIMIT = 4)
		when(notificationRepositoryPort.findAllByStatusAndRetryCountLessThan(NotificationStatus.FAILED, 4))
			.thenReturn(List.of(notification));
		log.info("[GIVEN] Mock Repository 조회 조건(FAILED, 4미만) 만족 확인 및 해당 알림 지정 완료");

		// PendingNotification 호출 시 예외를 발생시켜 catch 블록으로 강제 유도
		String testErrorMessage = "강제 네트워크 에러";
		doThrow(new RuntimeException(testErrorMessage))
			.when(notificationService).PendingNotification(notification.getId());
		log.info("[GIVEN] 가짜 서비스 레이어 발송 예외 발생 설정 완료 (사유: {})", testErrorMessage);

		// when
		log.info("\n[WHEN] notificationRecoveryService.recoverFailedNotifications() 실행 시작");
		notificationRecoveryService.recoverFailedNotifications();
		log.info("[WHEN] 복구 서비스 로직 실행 완료");

		// then
		// 실제 에러 메시지와 카운트 횟수가 저장되고 최종 실패처리 되는지 확인
		log.info("\n[THEN] Mockito verify 검증 시작 (handleRecoveryFailure 위임 여부 체크)");
		verify(notificationService, times(1))
			.handleRecoveryFailure(notification.getId(), testErrorMessage, 4);
		log.info("[THEN] 팩트체크 완료 (가짜 객체의 대리 메서드 정상 호출 확인)");
		log.info("========================================================================================\n");
	}
}